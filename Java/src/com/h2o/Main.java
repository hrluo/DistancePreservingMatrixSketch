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

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        if (args == null || args.length == 0)
            return;
        long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        System.out.println(System.getProperty("java.vm.name"));
        System.out.println("heap size: " + heapSize);
        System.out.println("max memory: " + heapMaxSize);
        System.out.println("free memory: " + heapFreeSize);

        Scanner scanner = new Scanner(System.in);
        String fileName = "data.csv";
        String type = "row";
        double radius = 0.;

//        Datasets d = new Datasets();
//        d.writeDataset(1000, 100, "swiss");

        System.out.println("Enter input file name");
        fileName = scanner.nextLine();

        System.out.println("Normalize columns to [0, 1] interval? (\"yes\", \"no\")");
        String normalize = scanner.nextLine();

        System.out.println("Enter type of sketch (row, col, rowcol, cur)");
        type = scanner.nextLine();

        if (type.equalsIgnoreCase("row")) {
            System.out.println("Computing row sketch ...");
            System.out.println("Enter radius of enclosing balls (entering 0 means program will choose)");
            radius = Double.parseDouble(scanner.nextLine());

            System.out.println("Input file name is " + fileName);
            System.out.println("Input radius is " + radius);
            RowSketcher rs = new RowSketcher(fileName, normalize, radius);
            int ms1 = (int) System.currentTimeMillis();
            rs.compute();
            int ms2 = (int) System.currentTimeMillis();
            System.out.println("CPU time in milliseconds "+(ms2 - ms1));
        }

        if (type.equalsIgnoreCase("col")) {
            System.out.println("Computing column sketch ...");
            System.out.println("Enter number of sketch columns desired");
            int numberOfColumns = Integer.parseInt(scanner.nextLine());

            System.out.println("Input file name is " + fileName);
            System.out.println("Number of sketch columns desired is " + numberOfColumns);
            ColumnSketcher cs = new ColumnSketcher(fileName, normalize, numberOfColumns);
            int ms1 = (int) System.currentTimeMillis();
            cs.compute();
            int ms2 = (int) System.currentTimeMillis();
            System.out.println("CPU time in milliseconds "+(ms2 - ms1));
        }

        if (type.equalsIgnoreCase("rowcol")) {
            System.out.println("Computing row sketch ...");
            System.out.println("Enter radius of enclosing balls (entering 0 means program will choose)");
            radius = Double.parseDouble(scanner.nextLine());

            System.out.println("Input file name is " + fileName);
            System.out.println("Input radius is " + radius);
            RowSketcher rs = new RowSketcher(fileName, normalize, radius);
            rs.compute();
            int[] rowIndices = rs.getRowIndices();

            System.out.println("Computing column sketch ...");
            System.out.println("Input file name is " + fileName);
            int numberOfColumns = Integer.parseInt(scanner.nextLine());
            ColumnSketcher cs = new ColumnSketcher(fileName, normalize, numberOfColumns);

            cs.compute();
            int[] colIndices = cs.getColIndices();
            DataSource dataSource = new DataSource(fileName, normalize);
            double[][] A = dataSource.getData();
            int r = rowIndices.length;
            int c = colIndices.length;
            double[][] sketchMatrix = new double[r][c];
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < c; j++) {
                    sketchMatrix[i][j] = A[rowIndices[i]][colIndices[j]];
                }
            }
        }

        if (type.equalsIgnoreCase("cur")) {
            System.out.println("Computing cur sketch ...");
            System.out.println("Enter number of sketch rows");
            int r = Integer.parseInt(scanner.nextLine());
            System.out.println("Enter number of sketch cols");
            int c = Integer.parseInt(scanner.nextLine());

            DataSource dataSource = new DataSource(fileName, normalize);
            double[][] A = dataSource.getData();
            int nRows = A.length;
            int nCols = A[0].length;
            if (r > nRows || c > nCols) {
                System.out.println("Requested rows and/or columns exceed size of input matrix.");
                System.exit(1);
            }
            CURSketcher curs = new CURSketcher(fileName, normalize, r, c, 0, 123);
            int ms1 = (int) System.currentTimeMillis();
            curs.compute();
            int ms2 = (int) System.currentTimeMillis();
            System.out.println("CPU time in milliseconds "+(ms2 - ms1));
            int[] rowIndices = curs.getRowIndices();
            int[] colIndices = curs.getColIndices();
            double[][] sketchMatrix = new double[r][c];
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < c; j++) {
                    sketchMatrix[i][j] = A[rowIndices[i]][colIndices[j]];
                }
            }
        }
    }
}
