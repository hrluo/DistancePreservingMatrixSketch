/*
 * CURSketcher -- A matrix sketch algorithm.
 *
 * Copyright 2020 by Leland Wilkinson.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License")
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 */

package com.h2o;

import java.io.PrintWriter;
import java.util.*;

public class CURSketcher {

    private int nRows, nCols;
    private int r, c, k;
    private int nDistances;
    private int[] rowPicks;
    private int[] colPicks;
    private double[][] C;
    private double[][] U;
    private double[] R;
    private double[][] A;
    private String[] colNames;
    private double[] distancesBetweenRowsOnAllColumns;
    private double[] distancesBetweenRowsOnSelectedColumns;
    private double maxCorrelation;
    private DataSource dataSource;
    private Random random;

    public CURSketcher(String fileName, String normalize, int r, int c, int k, int seed) {
        this.r = r;
        this.c = c;
        this.k = k;
        if (k == 0)
            k = Math.min(r, c);
        random = new Random(seed);
        this.dataSource = new DataSource(fileName, normalize);
        this.maxCorrelation = maxCorrelation;
        A = dataSource.getData();
        colNames = dataSource.getColumnNames();
        nRows = dataSource.getNumRows();
        nCols = dataSource.getNumCols();
        nDistances = nRows * (nRows - 1) / 2;
    }

    public int[] getRowIndices() {
        return rowPicks;
    }

    public int[] getColIndices() {
        return colPicks;
    }

    public double[][] getC() {
        return C;
    }

    public double[][] getU() {
        return U;
    }

    public double[] getR() {
        return R;
    }

    public void compute() {
        //LinearTimeCUR(A, r, c, k, random_seed=123):
        // Ref: https://www.stat.berkeley.edu/~mmahoney/pubs/matrix3_SICOMP.pdf
        // Ref: https://www.cs.utah.edu/~jeffp/teaching/cs7931-S15/cs7931/5-cur.pdf
        // Get dimensions for row and columns
        int m = nRows;
        int n = nCols;
        if (r > m || c > n || k > Math.min(r, c)) {
            System.out.println("error: rank greater than matrix dimensions.\n");
            return;
        }

        // Compute row and column norms.
        double ssA = 0.;
        double[] ssCols = new double[n];
        double[] ssRows = new double[m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double ss = A[i][j] * A[i][j];
                ssA += ss;
                ssCols[j] += ss;
                ssRows[i] += ss;
            }
        }

        // probabilities of choosing a row/column are proportional to the row/column norms.
        double[] p = new double[m];
        double[] q = new double[n];
        for (int i = 0; i < m; i++)
            p[i] = ssRows[i] / ssA;
        for (int j = 0; j < n; j++)
            q[j] = ssCols[j] / ssA;

        // Pick random rows and columns according to probabilities p[.] or q[.], avoiding repetitions.
        colPicks = sample(q, c);
        rowPicks = sample(p, r);

        double[][] C = new double[m][c];
        for (int t = 0; t < c; t++) {
            int jt = colPicks[t];
            for (int i = 0; i < r; i++) {
                C[i][t] = A[i][jt] / Math.sqrt(c * q[jt]);
            }
        }

        double[][] CC = new double[c][c];
        for (int i = 0; i < c; i++) {
            for (int j = 0; j < c; j++) {
                for (int ij = 0; ij < c; ij++)
                    CC[i][j] += C[i][ij] * C[ij][j];
            }
        }
        double[][] U = new double[m][n];
        double[][] V = new double[n][n];
        double[] D = new double[n];
        SVD svd = new SVD();
        svd.compute(CC, U, V, D);

        double[][] R = new double[r][c];
        double[][] PSI = new double[r][c];
        for (int t = 0; t < r; t++) {
            int it = rowPicks[t];
            for (int j = 0; j < c; j++) {
                R[t][j] = A[it][j] / Math.sqrt(r * p[it]);
                PSI[t][j] = C[it][j] / Math.sqrt(r * p[it]);
            }
        }

        if (D[k] <= 0) {
            for (int j = k - 1; j > 0; j--) {
                if (D[j] != 0) {
                    k = j;
                    break;
                }
            }
        }
        // Compute the CUR.
        double[][] PHI = new double[r][k];
        for (int t = 0; t < k; t++) {
            for (int j = 0; j < k; j++) {
                PHI[t][j] = (V[j][t] * V[t][j]) / (D[t] * D[t]);
            }
        }
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                for (int ij = 0; ij < m; ij++)
                    U[i][j] += PHI[i][ij] * PSI[ij][j];
            }
        }
        double r = computeFrobeniusCorrelation();
        System.out.println("Frobenius correlation " + r);

        System.out.println("Columns picked ");
        for (int j = 0; j < colPicks.length; j++) {
            if (j == colPicks.length - 1) {
                System.out.println(colPicks[j]);
            } else {
                System.out.print(colPicks[j] + ", ");
            }
        }
        writeCURSketch();
        return;
    }

    private double computeFrobeniusCorrelation() {
        computeDistancesOnAllColumns();
        computeDistancesOnSelectedColumns();
        double r = frobenius(distancesBetweenRowsOnAllColumns, distancesBetweenRowsOnSelectedColumns);
        return r;
    }

    private void computeDistancesOnAllColumns() {
        distancesBetweenRowsOnAllColumns = new double[nDistances];
        for (int k = 0; k < nCols; k++) {
            int ij = 0;
            for (int i = 1; i < nRows; i++) {
                double[] ri = new double[]{A[i][k]};
                for (int j = 0; j < i; j++) {
                    double[] rj = new double[]{A[j][k]};
                    double distance = squaredDistance(ri, rj);
                    if (!Double.isNaN(distance))
                        distancesBetweenRowsOnAllColumns[ij] += distance;
                    ij++;
                }
            }
        }
    }

    private void computeDistancesOnSelectedColumns() {
        /* all possible pairs of distinct rows */
        /* this makes an enormous file, so it is not used as a default */
        distancesBetweenRowsOnSelectedColumns = new double[nDistances];
        int nSelected = colPicks.length;
        double[] ri = new double[nSelected];
        double[] rj = new double[nSelected];

        int ij = 0;
        for (int i = 1; i < nRows; i++) {
            for (int k = 0; k < nSelected; k++) {
                ri[k] = A[i][colPicks[k]];
            }
            for (int j = 0; j < i; j++) {
                for (int k = 0; k < nSelected; k++) {
                    ri[k] = A[i][colPicks[k]];
                }
                distancesBetweenRowsOnSelectedColumns[ij] = squaredDistance(ri, rj);
                ij++;
            }
        }
    }

    private double squaredDistance(double[] e1, double[] e2) {
        double sum = 0;
        int n = 0;
        for (int j = 0; j < e1.length; j++) {
            double d1 = e1[j];
            double d2 = e2[j];
            if (!Double.isNaN(d1) && !Double.isNaN(d2)) {
                sum += (d1 - d2) * (d1 - d2);
                n++;
            }
        }
        sum *= (double) e1.length / n;
        return sum;
    }

    private static double frobenius(double[] x, double[] y) {
        if (x.length != y.length)
            return Double.NaN;
        double f = 0;
        double x2 = 0;
        double y2 = 0;
        double xy = 0;
        for (int i = 0; i < x.length; i++) {
            x2 += x[i] * x[i];
            y2 += y[i] * y[i];
            xy += x[i] * y[i];
        }
        x2 = Math.sqrt(x2);
        y2 = Math.sqrt(y2);
        f = xy / (x2 * y2);
        return f;
    }

    private void writeCURSketch() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("cursketch.csv", "UTF-8");
        } catch (Exception e) {
            System.out.println("Unable to allocate CUR sketch file");
        }

        /* write header */
        for (int j = 0; j < colPicks.length; j++) {
            if (j < colPicks.length - 1)
                writer.print(colNames[colPicks[j]] + ",");
            else
                writer.println(colNames[colPicks[j]]);
        }

        for (int i = 0; i < rowPicks.length; i++) {
            int it = rowPicks[i];
            for (int j = 0; j < colPicks.length; j++) {
                if (j < (colPicks.length - 1)) {
                    writer.print(A[it][colPicks[j]] + ",");
                } else {
                    writer.println(A[it][colPicks[j]]);
                }
            }
        }
        writer.close();
        System.out.println("CURSketcher output file name is cursketch.csv");
        System.out.println("CURSketcher output number of rows " + rowPicks.length);
        System.out.println("CURSketcher output number of columns " + colPicks.length);
    }

    public int[] sample(double[] p, int ns) {
        /* pick ns elements from integer list with probabilities p[i], i = 1, np without replacement */

        int[] picks = new int[ns];
        int np = p.length;
        List<Integer> selected = new ArrayList<>();
        while (selected.size() < ns) {
            double r = random.nextDouble();
            double b = 0;
            for (int j = 0; j < np; j++) {
                b += p[j];
                if (b > r && !selected.contains(j)) {
                    selected.add(new Integer(j));
                    break;
                }
            }
        }
        for (int i = 0; i < ns; i++) {
            picks[i] = selected.get(i);
        }
        return picks;
    }
}
