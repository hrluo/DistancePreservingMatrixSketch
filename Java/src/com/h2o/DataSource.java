package com.h2o;

import java.io.IOException;

public class DataSource {
    private int nRows;
    private int nCols;
    private double[][] data;
    private String[] columnNames;
    private String fileName;
    private double[] minValues;
    private double[] maxValues;
    private boolean isNormalized = true;

    public DataSource(String fileName) {
        /* this code reads only classic, strict CSV files. Namely, comma separators and a header file with labels */
        this.fileName = fileName;
        try {
            countRows();
            readDataRecords();

        } catch (java.io.IOException ie) {
            System.out.println("Error reading data file.");
        }
        System.out.println("Input data rows, cols " + nRows + " " + nCols);
        if (isNormalized)
            normalize();
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

        data = new double[nRows][nCols];
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
}