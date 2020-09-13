package com.h2o;


import java.util.*;

public class RowSketcher {
    private double[][] data;
    private String[] colNames;
    private int nRows, nCols;
    private double delta;
    private ArrayList<double[]> exemplars;
    private ArrayList<ArrayList<Integer>> memberIndices;
    private int MAXDIMENSIONS = 500;
    public double radius;
    public static int seed = 4123;
    public static Random random = new Random(seed);

    public RowSketcher (String fileName, double radius){
        DataSource dataSource = new DataSource(fileName);
        this.radius = radius;
        data = dataSource.getData();
        colNames = dataSource.getColumnNames();
        nRows = dataSource.getNumRows();
        nCols = dataSource.getNumCols();
    }

    public ArrayList getMemberIndices() {
        return memberIndices;
    }

    public List getExemplars() {
        return exemplars;
    }

    public void compute() {
        /*
         * an exemplar is a case that is used to represent its close members
         */
        initializeParameters();
        memberIndices.add(new ArrayList<>());
        memberIndices.get(0).add(0);
        double[][] t = null;
        boolean isHighDimensional = nCols > MAXDIMENSIONS;
        if (isHighDimensional) {
            nCols = MAXDIMENSIONS;
//            t = randomGaussianProjectionMatrix(nCols, nCols);
        }
        if (radius <= 0) {
        /*
        See Doug Jungreis solution at
        https://mathoverflow.net/questions/308018/coverage-of-balls-on-random-points-in-euclidean-space?answertab=active#tab-top
        */
            double rsquare = (nCols / 6.0) - 1.744 * Math.sqrt(7.0 * nCols / 180.0);
            radius = .5 * Math.sqrt(rsquare);
            if (Double.isNaN(radius))
                radius = .5 / Math.pow(100, 1.0 / nCols);
        }
        delta = radius * radius; // because we are using squared Euclidean distances to save time
        System.out.println("radius " + radius);

        for (int i = 0; i < nRows; i++) {
            double[] row = new double[nCols];
            System.arraycopy(data[i], 0, row, 0, nCols);
            if (isHighDimensional)
                row = randomProjection(row, t);
            /* find closest exemplar and assign this row to it or start new exemplar */
            double distanceToNearestExemplar = Double.POSITIVE_INFINITY;
            int closestExemplarIndex = 0;
            int numExemplars = exemplars.size();
            int[] exemplarIndices = new int[numExemplars];
            for (int k = 0; k < numExemplars; k++) {
                exemplarIndices[k] = k;
            }
            shuffleIntArray(exemplarIndices);    // shuffle order in which exemplars are examined
            for (int k = 0; k < numExemplars; k++) {
                int nextIndex = exemplarIndices[k];
                double[] e = exemplars.get(nextIndex);
                double d = distance(e, row);
                if (d < distanceToNearestExemplar) {
                    distanceToNearestExemplar = d;
                    closestExemplarIndex = nextIndex;
                }
                if (distanceToNearestExemplar < delta)  // near enough, so escape
                    break;
            }
            if (distanceToNearestExemplar < delta) {
                /* found a close exemplar, so add to cluster */
                memberIndices.get(closestExemplarIndex).add(i);
            } else {
                /* otherwise, form a new exemplar */
                exemplars.add(row);
                ArrayList<Integer> member = new ArrayList<>();
                member.add(i);
                memberIndices.add(member);
            }
        }
        System.out.println ("Output number of rows "+ exemplars.size());
    }

    private void initializeParameters() {
        exemplars = new ArrayList<>();
        memberIndices = new ArrayList<>();
    }

    private double distance(double[] e1, double[] e2) {
        double sum = 0;
        int n = 0;
        for (int j = 0; j < e1.length; j++) {
            double d1 = e1[j];
            double d2 = e2[j];
            if (!Double.isNaN(d1) && !Double.isNaN(d2)) {
                sum += (d1 - d2) * (d1 - d2);
                n++;
            }
            if (sum > delta) // escape if distance is too large
                return sum;
        }
        sum *= (double) e1.length / n;
        if (n < (e1.length / 2.0))
            return Double.NaN;
        return sum;
    }

    private double[] randomProjection(double[] row, double[][] projectionMatrix) {
        int nProj = projectionMatrix[0].length;
        double[] r = new double[nProj];
        for (int j = 0; j < nProj; j++) {
            double n = 0;
            for (int k = 0; k < nCols; k++) {
                if (!Double.isNaN(row[k])) {
                    r[j] += row[k] * projectionMatrix[k][j];
                    n++;
                }
            }
            r[j] /= n;
        }
        return r;
    }

    private static void shuffleIntArray(int[] x) {
        for (int i = x.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int a = x[index];
            x[index] = x[i];
            x[i] = a;
        }
    }
}
