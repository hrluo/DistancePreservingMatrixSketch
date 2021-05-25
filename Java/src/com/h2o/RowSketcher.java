/*
 * Sketcher -- A matrix sketch algorithm.
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

public class RowSketcher {
    private DataSource dataSource;
    private double[][] data;
    private String[] colNames;
    private int nRows, nCols;
    private double delta;
    private ArrayList<double[]> exemplars;
    private ArrayList<Integer> exemplarIndices;
    private ArrayList<ArrayList<Integer>> memberIndices;
    private double radius;
    public static int seed = 4123;
    public static Random random = new Random(seed);

    public RowSketcher (String fileName, double radius) {
        this.dataSource = new DataSource(fileName);
        this.radius = radius;
        data = dataSource.getData();
        colNames = dataSource.getColumnNames();
        nRows = dataSource.getNumRows();
        nCols = dataSource.getNumCols();
    }

    public List getMemberIndices() {
        return memberIndices;
    }

    public List getExemplars() {
        return exemplars;
    }

    public void compute() {
        /*
         * an exemplar is a case that is used to represent its close members
         */
        exemplars = new ArrayList<>();
        exemplarIndices = new ArrayList<>();
        memberIndices = new ArrayList<>();
        memberIndices.add(new ArrayList<>());
        memberIndices.get(0).add(0);

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
            /* find closest exemplar and assign this row to it or start new exemplar */
            double distanceToNearestExemplar = Double.POSITIVE_INFINITY;
            int closestExemplarIndex = 0;
            int numExemplars = exemplars.size();
            List exemplarVisitingOrder = new ArrayList<>();
            for (int k = 0; k < numExemplars; k++) {
                exemplarVisitingOrder.add(new Integer(k));
            }
            Collections.shuffle(exemplarVisitingOrder);
            for (int k = 0; k < numExemplars; k++) {
                Integer nextIndex = (Integer) exemplarVisitingOrder.get(k);
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
                exemplarIndices.add(i);
                ArrayList<Integer> member = new ArrayList<>();
                member.add(i);
                memberIndices.add(member);
            }
        }
        double[] frequencies = new double[exemplars.size()];
        for (int j = 0; j < nCols; j++) {
            double[] y = new double[exemplars.size()];
            for (int i = 0; i < exemplars.size(); i++) {
                y[i] = exemplars.get(i)[0];
                frequencies[i] = memberIndices.get(i).size();
            }
        }
        writeRowSketch(frequencies);
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

    private void writeRowSketch(double[] frequencies) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("rowsketch.csv", "UTF-8");
        } catch (Exception e) {
            System.out.println("Unable to allocate row sketch file");
        }

        /* write header */
        for (int j = 0; j < nCols; j++) {
            writer.print(colNames[j]+",");
        }
        writer.println("frequencies");

        for (int i = 0; i < exemplars.size(); i++) {
            int index = exemplarIndices.get(i);
            for (int j = 0; j < nCols; j++) {
                data[index][j] = (dataSource.maxValues[j] - dataSource.minValues[j]) * data[index][j] + dataSource.minValues[j];
                writer.print(data[index][j] + ",");
            }
            writer.println(frequencies[i]);
        }
        writer.close();
        System.out.println ("RowSketcher output file name is rowsketch.csv");
        System.out.println ("RowSketcher output number of rows "+ exemplars.size());
    }
}
