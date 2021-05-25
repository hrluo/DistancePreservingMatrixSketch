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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataSource {
    private int nRows;
    private int nCols;
    private double[][] data;
    private String[] columnNames;
    private String fileName;
    public double[] minValues;
    public double[] maxValues;

    public DataSource(String fileName) {
        /* this code reads only classic, strict CSV files. Namely, comma separators and a header record with labels */
        this.fileName = fileName;
        try {
            readDataRecords();

        } catch (java.io.IOException ie) {
            System.out.println("Error reading data file.");
            System.exit(1);
        }
    }

    public int getNumRows() {
        return nRows;
    }

    public int getNumCols() {
        return nCols;
    }

    public double[][] getData() {
        return data;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    private java.io.BufferedReader openDataFile() {
        java.io.BufferedReader bufferedReader = null;
        try {
            bufferedReader = new java.io.BufferedReader(new java.io.FileReader(fileName));
        } catch (java.io.FileNotFoundException fe) {
            System.out.println("Dataset file not found.");
            System.exit(1);
        }
        return bufferedReader;
    }

    private void processColumnNamesRecord() throws IOException {
        java.io.BufferedReader bufferedReader = openDataFile();

        String record = bufferedReader.readLine();
        String[] fields = getFields(record);
        nCols = fields.length;
        nRows = countRows();
        System.out.println("Rows and columns in file: " + nRows+" "+nCols);
        columnNames = new String[nCols];
        System.arraycopy(fields, 0, columnNames, 0, nCols);
    }

    private void readDataRecords() throws IOException {
        java.io.BufferedReader bufferedReader = openDataFile();
        String record;

        processColumnNamesRecord();

        bufferedReader.readLine();

        data = new double[nRows][nCols];
        minValues = new double[nCols];
        maxValues = new double[nCols];
        for (int j = 0; j < nCols; j++) {
            minValues[j] = Double.POSITIVE_INFINITY;
            maxValues[j] = Double.NEGATIVE_INFINITY;
        }
        for (int i = 0; i < nRows; i++) {
            record = bufferedReader.readLine();
            if (record == null) {
                nRows = i;
                break;
            }
            String[] fields = getFields(record);
            processRecord(fields, i);
        }
        bufferedReader.close();
        normalize();
    }

    private void processRecord(String[] fields, int row) {
        for (int j = 0; j < nCols; j++) {
            try {
                data[row][j] = Double.parseDouble(fields[j]);
                if (!Double.isNaN(data[row][j])) {
                    minValues[j] = Math.min(data[row][j], minValues[j]);
                    maxValues[j] = Math.max(data[row][j], maxValues[j]);
                }
            } catch (NumberFormatException e) {
                data[row][j] = Double.NaN;
            }
        }
    }

    private void normalize() {
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                data[i][j] = (data[i][j] - minValues[j]) / (maxValues[j] - minValues[j]);
            }
        }
    }

    private String[] getFields(String record) {
        if (record == null)
            return null;
        record = record.trim();
        return record.split(",");
    }

    private int countRows() {
        int count = 0;
        try {

            // make a connection to the file
            Path file = Paths.get(fileName);
            // read all lines of the file
            count = (int) Files.lines(file).count();

        } catch (Exception e) {
            e.getStackTrace();
        }
        return count;
    }
}