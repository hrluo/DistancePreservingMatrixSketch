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
        String task = "sketch";
        String fileName = "data.csv";
        String type = "row";
        double radius = 0.;

        System.out.println("Enter type of sketch (row or col)");
        type = scanner.nextLine();

        System.out.println("Enter input file name");
        fileName = scanner.nextLine();

        if (type.equalsIgnoreCase("row")) {
            System.out.println("Computing row sketch ...");
            System.out.println("Enter radius of enclosing balls (entering 0 means program will choose)");
            radius = Double.parseDouble(scanner.nextLine());

            System.out.println("Input file name is " + fileName);
            System.out.println("Input radius is " + radius);
            RowSketcher rs = new RowSketcher(fileName, radius);
            rs.compute();
        }
        if (type.equalsIgnoreCase("col")) {
            System.out.println("Computing column sketch ...");
            System.out.println("Enter maximum Frobenius Correlation (before stopping search)");
            double maxCorrelation = Double.parseDouble(scanner.nextLine());

            System.out.println("Input file name is " + fileName);
            System.out.println("Maximum Frobenius Correlation is " + maxCorrelation);
            ColumnSketcher cs = new ColumnSketcher(fileName, maxCorrelation);
            cs.compute();
        }
    }
}
