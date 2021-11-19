/*
 * ColumnSketcher -- A matrix sketch algorithm.
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
import java.util.Set;
import java.util.TreeSet;

public class ColumnSketcher {

    private int nRows, nCols;
    private int numberOfColumns;
    private int nDistances;
    private double[][] data;
    private String[] colNames;
    private int[] colIndices;
    private double[] distancesBetweenRowsOnAllColumns;
    private double[] distancesBetweenRowsOnSelectedColumns;
    private double[] distancesBetweenRowsOnSelectedColumn;
    private DataSource dataSource;

    public ColumnSketcher(String fileName, String normalize, int numberOfColumns) {
        this.dataSource = new DataSource(fileName, normalize);
        data = dataSource.getData();
        colNames = dataSource.getColumnNames();
        this.nRows = dataSource.getNumRows();
        this.nCols = dataSource.getNumCols();
        this.numberOfColumns = numberOfColumns;
        nDistances = this.nRows * (this.nRows - 1) / 2;
    }

    public int[] getColIndices() {
        return colIndices;
    }

    public void compute() {
        computeDistancesOnAllColumns();
        Set<Integer> selectedColumns = selectBestColumns();
//        computeDistancesOnSelectedColumns(selectedColumns);
        writeColumnSketch(selectedColumns);
    }

    private void computeDistancesOnAllColumns() {
        distancesBetweenRowsOnAllColumns = new double[nDistances];
        for (int k = 0; k < nCols; k++) {
            int ij = 0;
            for (int i = 1; i < nRows; i++) {
                double[] ri = new double[]{data[i][k]};
                for (int j = 0; j < i; j++) {
                    double[] rj = new double[]{data[j][k]};
                    double distance = squaredDistance(ri, rj);
                    if (!Double.isNaN(distance))
                        distancesBetweenRowsOnAllColumns[ij] += distance;
                    ij++;
                }
            }
        }
    }

    private Set<Integer> selectBestColumns() {
        Set<Integer> selectedColumns = new TreeSet();
        int bestColumn = -1;

        distancesBetweenRowsOnSelectedColumn = new double[nDistances];
        distancesBetweenRowsOnSelectedColumns = new double[nDistances];
        double previousBestCorrelation = 0.0;
        for (int j = 0; j < nCols; j++) {
            double bestCorrelation = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < nCols; k++) {
                if (selectedColumns.contains(k) || colNames[j].equals("frequencies"))
                    continue;
                computeDistancesOnSelectedColumn(k);
                double r = 0;
                r = frobenius(distancesBetweenRowsOnAllColumns, distancesBetweenRowsOnSelectedColumn);
                if (r > bestCorrelation) {
                    bestColumn = k;
                    bestCorrelation = r;
                }
            }
            if (bestCorrelation < previousBestCorrelation || selectedColumns.size() >= numberOfColumns)
                break;
            System.out.println("best column " + j + " " + bestColumn + " " + colNames[bestColumn] + " " + bestCorrelation);
            computeDistancesOnSelectedColumn(bestColumn);
            System.arraycopy(distancesBetweenRowsOnSelectedColumn, 0, distancesBetweenRowsOnSelectedColumns, 0, nDistances);
            selectedColumns.add(bestColumn);
            previousBestCorrelation = bestCorrelation;
        }
        return selectedColumns;
    }

    private void computeDistancesOnSelectedColumn(int column) {
        /* all possible pairs of distinct rows */
        double[] ri = new double[1];
        double[] rj = new double[1];
        int ij = 0;
        for (int i = 1; i < nRows; i++) {
            ri[0] = data[i][column];
            for (int j = 0; j < i; j++) {
                rj[0] = data[j][column];
                double distance = squaredDistance(ri, rj);
                if (!Double.isNaN(distance))
                    distancesBetweenRowsOnSelectedColumn[ij] = distance + distancesBetweenRowsOnSelectedColumns[ij];
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

    private void writeColumnSketch(Set selectedColumns) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("colsketch.csv", "UTF-8");
        } catch (Exception e) {
            System.out.println("Unable to allocate column sketch file");
        }

        Object[] s = selectedColumns.toArray();

        /* write header */
        colIndices = new int[selectedColumns.size()];
        for (int j = 0; j < selectedColumns.size(); j++) {
            colIndices[j] = (int) s[j];
            if (j < selectedColumns.size() -1)
                writer.print(colNames[(Integer) s[j]]+",");
            else
                writer.println(colNames[(Integer) s[j]]);
        }

        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < selectedColumns.size(); j++) {
                data[i][j] = (dataSource.maxValues[j] - dataSource.minValues[j]) * data[i][(Integer) s[j]] + dataSource.minValues[(Integer) s[j]];
                if (j < (selectedColumns.size() -1)) {
                    writer.print(data[i][(Integer) s[j]] + ",");
                } else {
                    writer.println(data[i][(Integer) s[j]]);
                }
            }
        }
        writer.close();
        System.out.println ("ColumnSketcher output file name is colsketch.csv");
        System.out.println ("ColumnSketcher output number of columns "+ selectedColumns.size());
    }
}
