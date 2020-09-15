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

public class DataSource {
    private int nRows;
    private int nCols;
    private double[][] normalizedData;
    private double[][] rawData;
    private String[] columnNames;
    private String fileName;
    private double[] minValues;
    private double[] maxValues;

    public DataSource(String fileName) {
        /* this code reads only classic, strict CSV files. Namely, comma separators and a header record with labels */
        this.fileName = fileName;
        try {
            countRows();
            readDataRecords();

        } catch (java.io.IOException ie) {
            System.out.println("Error reading normalizedData file.");
        }
        System.out.println("Input data rows, cols " + nRows + " " + nCols);
        normalize();
    }

    public int getNumRows() {
        return nRows;
    }

    public int getNumCols() {
        return nCols;
    }

    public double[][] getNormalizedData() {
        return normalizedData;
    }

    public double[][] getRawData() {
        return rawData;
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
        }
        return bufferedReader;
    }

    private void countRows() throws IOException {
        java.io.BufferedReader bufferedReader = openDataFile();

        readColumnNamesRecord();
        nRows = 0;
        while (bufferedReader.readLine() != null) {
            nRows++;
        }
        nRows--;
        bufferedReader.close();
    }

    private void readColumnNamesRecord() throws IOException {
        java.io.BufferedReader bufferedReader = openDataFile();

        String record = bufferedReader.readLine();
        String[] fields = getFields(record);
        nCols = fields.length;
        columnNames = new String[nCols];
        System.arraycopy(fields, 0, columnNames, 0, nCols);
    }

    private void readDataRecords() throws IOException {
        java.io.BufferedReader bufferedReader = openDataFile();
        String record;

        bufferedReader.readLine();    // skip column labels record

        normalizedData = new double[nRows][nCols];
        rawData = new double[nRows][nCols];
        minValues = new double[nCols];
        maxValues = new double[nCols];
        for (int j = 0; j < nCols; j++) {
            minValues[j] = Double.POSITIVE_INFINITY;
            maxValues[j] = Double.NEGATIVE_INFINITY;
        }
        for (int i = 0; i < nRows; i++) {
            record = bufferedReader.readLine();
            if (record == null)
                break;
            String[] fields = getFields(record);
            processRecord(fields, i);
        }
        bufferedReader.close();
    }

    private void processRecord(String[] fields, int row) {
        for (int j = 0; j < nCols; j++) {
            try {
                rawData[row][j] = Double.parseDouble(fields[j]);
                if (!Double.isNaN(rawData[row][j])) {
                    minValues[j] = Math.min(rawData[row][j], minValues[j]);
                    maxValues[j] = Math.max(rawData[row][j], maxValues[j]);
                }
            } catch (NumberFormatException e) {
                rawData[row][j] = Double.NaN;
            }
        }
    }

    private void normalize() {
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                normalizedData[i][j] = (rawData[i][j] - minValues[j]) / (maxValues[j] - minValues[j]);
            }
        }
    }

    private String[] getFields(String record) {
        if (record == null)
            return null;
        record = record.trim();
        return record.split(",");
    }
}