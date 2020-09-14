package com.h2o;

public class Main {
    public static void main(String[] args) {
        System.out.println("Sketch ");

        String fileName = args[0];
        double radius = Double.parseDouble(args[1]);
        double maxCorrelation = Double.parseDouble(args[2]);
        RowSketcher rs = new RowSketcher(fileName, radius);
        rs.compute();
        ColumnSketcher cs = new ColumnSketcher("sketch.csv", maxCorrelation);
        cs.compute();
    }
}
