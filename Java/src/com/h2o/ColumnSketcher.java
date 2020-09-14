package com.h2o;

import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

public class ColumnSketcher {

    private int nRows, nCols;
    private int nDistances;
    private double[][] normalizedData;
    private double[][] rawData;
    private String[] colNames;
    private double[] distancesBetweenRowsOnAllColumns;
    private double[] distancesBetweenRowsOnSelectedColumns;
    private double[] distancesBetweenRowsOnSelectedColumn;
    private double maxCorrelation;

    public ColumnSketcher(String fileName, double maxCorrelation) {
        DataSource dataSource = new DataSource(fileName);
        this.maxCorrelation = maxCorrelation;
        normalizedData = dataSource.getNormalizedData();
        rawData = dataSource.getRawData();
        colNames = dataSource.getColumnNames();
        nRows = dataSource.getNumRows();
        nCols = dataSource.getNumCols();
        nDistances = nRows * (nRows - 1) / 2;
    }

    public void compute() {
        computeDistancesOnAllColumns();
        Set<Integer> selectedColumns = selectBestColumns();
//        computeDistancesOnSelectedColumns(selectedColumns);
        System.out.println ("ColumnSketcher output number of columns "+ selectedColumns.size());
        writeSketch(selectedColumns);
    }

    private void computeDistancesOnAllColumns() {
        distancesBetweenRowsOnAllColumns = new double[nDistances];
        for (int k = 0; k < nCols; k++) {
            int ij = 0;
            for (int i = 1; i < nRows; i++) {
                double[] ri = new double[]{normalizedData[i][k]};
                for (int j = 0; j < i; j++) {
                    double[] rj = new double[]{normalizedData[j][k]};
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
                if (selectedColumns.contains(k))
                    continue;
                computeDistancesOnSelectedColumn(k);
                double r = 0;
                r = frobenius(distancesBetweenRowsOnAllColumns, distancesBetweenRowsOnSelectedColumn);
                if (r > bestCorrelation) {
                    bestColumn = k;
                    bestCorrelation = r;
                }
            }
            if (bestCorrelation < previousBestCorrelation || bestCorrelation > maxCorrelation)
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
            ri[0] = normalizedData[i][column];
            for (int j = 0; j < i; j++) {
                rj[0] = normalizedData[j][column];
                double distance = squaredDistance(ri, rj);
                if (!Double.isNaN(distance))
                    distancesBetweenRowsOnSelectedColumn[ij] = distance + distancesBetweenRowsOnSelectedColumns[ij];
                ij++;
            }
        }
    }

    private void computeDistancesOnSelectedColumns(Set<Integer> selectedColumns) {
        /* all possible pairs of distinct rows */
        /* this makes an enormous file, so it is not used as a default */
        distancesBetweenRowsOnSelectedColumns = new double[nDistances];
        int nSelected = selectedColumns.size();
        double[] ri = new double[nSelected];
        double[] rj = new double[nSelected];

        int ij = 0;
        for (int i = 1; i < nRows; i++) {
            int k = 0;
            for (Integer selectedColumn : selectedColumns) {
                ri[k] = normalizedData[i][selectedColumn];
                k++;
            }
            for (int j = 0; j < i; j++) {
                k = 0;
                for (Integer selectedColumn : selectedColumns) {
                    rj[k] = normalizedData[j][selectedColumn];
                    k++;
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

    private void writeSketch(Set selectedColumns) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("sketch.csv", "UTF-8");
        } catch (Exception e) {
            System.out.println("Unable to allocate column sketch file");
        }

        Object[] s = selectedColumns.toArray();

        /* write header */
        for (int j = 0; j < selectedColumns.size(); j++) {
            if (j < selectedColumns.size() -1)
                writer.print(colNames[(Integer) s[j]]+",");
            else
                writer.println(colNames[j]);
        }

        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < selectedColumns.size(); j++) {
                if (j < selectedColumns.size() -1) {
                    writer.print(rawData[i][(Integer) s[j]] + ",");
                } else {
                    writer.println(rawData[i][(Integer) s[j]]);
                }
            }
        }
        writer.close();
    }
}
