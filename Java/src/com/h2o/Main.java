package com.h2o;

public class Main {
    public static void main(String[] args) {
        System.out.println("Sketch ");

        if (args[1].equalsIgnoreCase("columns")) {
            ColumnSketcher cs = new ColumnSketcher(args[0], .95);
            cs.compute();
        } else if (args[1].equalsIgnoreCase("rows")) {
            RowSketcher rs = new RowSketcher(args[0], 0);
            rs.compute();
        }
    }
}
