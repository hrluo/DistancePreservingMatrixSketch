package com.h2o;

import java.io.PrintWriter;
import java.util.Random;

public class Datasets {
    public Datasets() {

    }

    public void writeDataset(int nRows, int nCols, String type) {
        PrintWriter writer = null;
        String fileName = "cluster.csv";
        if (type.equals("cluster"))
            fileName = "cluster.csv";
        if (type.equals("donut"))
            fileName = "donut.csv";
        if (type.equals("outlier"))
            fileName = "outlier.csv";
        if (type.equals("outlier2D"))
            fileName = "outlier2D.csv";
        if (type.equals("inlier2D"))
            fileName = "inlier2D.csv";
        if (type.equals("grid2D"))
            fileName = "grid2D.csv";
        if (type.equals("swiss")) {
            fileName = "swiss.csv";
        }
        try {
            writer = new PrintWriter(fileName, "UTF-8");
        } catch (Exception e) {
            System.out.println("Unable to allocate column sketch file");
        }

        Random random = new Random(123);
        /* write header */
        for (int j = 0; j < nCols; j++) {
            if (j < nCols - 1)
                writer.print("var " + j + ", ");
            else
                writer.println("var " + j);
        }

        double[] row = new double[nCols];
        double[] x = new double[]{-2., 0., 2.};
        double[] y = new double[]{-2., 2., -2.};
        double t = 0.;
        double radius = 1.0;
        double theta = 0.;
        double rho = 0.8;
        for (int i = 0; i < nRows; i++) {
            double x3 = random.nextGaussian();
            for (int j = 0; j < nCols; j++) {
                row[j] = random.nextGaussian() / 10.;
                if (type.equals("outlier2D")) {
                    if (i == 0) {
                        row[0] = .6;
                        row[1] = -.6;
                    } else if (j == 0) {
                        row[0] = x3 / 10.;
                    } else {
                        double x4 = random.nextGaussian() / 10.;
                        row[1] = rho * x3 + Math.sqrt(1.0 - rho * rho) * x4;
                    }
                } else if (type.equals("inlier2D")) {
                    row[1] = random.nextGaussian() / 10.;
                    if (theta == 0.) {
                        row[0] = 0;
                        row[1] = 0;
                    } else {
                        row[0] += radius * Math.cos(theta);
                        row[1] += radius * Math.sin(theta);
                    }
                    theta += Math.PI / nRows;
                } else if (type.equals("swiss")) {
                    if (j < 3) {
                        row[0] = t * Math.cos(t);
                        row[1] = t * Math.sin(t);
                        row[2] = row[1] + 6. * (random.nextDouble() - .5);
                    } else {
                        row[j] = random.nextGaussian();
                    }
                } else if (j == 3 || j == 4) {
                    if (type.equals("outlier")) {
                        if (i == 0) {
                            row[3] = 6.0;
                            row[4] = -6.0;
                        } else if (j == 3) {
                            row[3] = x3;
                        } else {
                            double x4 = random.nextGaussian();
                            row[4] = rho * x3 + Math.sqrt(1.0 - rho * rho) * x4;
                        }

                    } else if (type.equals("cluster")) {
                        row[3] += x[random.nextInt(3)];
                        row[4] += y[random.nextInt(3)];
                    } else if (type.equals("donut")) {
                        row[3] += radius * Math.cos(theta);
                        row[4] += radius * Math.sin(theta);
                        theta += Math.PI / nRows;
                    }
                }
                if (j == nCols - 1) {
                    writer.println(row[j]);
                } else {
                    writer.print(row[j] + ",");
                }
            }
            t += .004 * Math.PI;
        }
        writer.close();
        System.out.println("Output file name is " + fileName);
    }
}
