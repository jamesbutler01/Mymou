package com.example.jbutler.mymou;

import java.util.Arrays;

public class MatrixMaths {

    //Tranpose matrix
    public static double[][] transpose(double [][] input){
        int rows = input[0].length;
        int cols = input.length;
        double[][] output = new double[rows][cols];
        for (int i = 0; i < cols; i++)
            for (int j = 0; j < rows; j++)
                output[j][i] = input[i][j];
        return output;
    }

    //Apply tanh function to all values in array
    public static double[][] tanh(double[][] input) {
        double[][] output = new double[input.length][input[0].length];
        for(int i = 0; i < input.length; i++) {
            for(int j = 0; j < input[0].length; j++) {
                output[i][j] = Math.tanh(input[i][j]);
            }
        }
        return output;
    }

    //Apply sigmoid function to all values in array
    public static double[][] sigmoid(double[][] input) {
        double[][] output = new double[input.length][input[0].length];
        for(int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                output[i][j] = 1 / (1 + Math.exp(-input[i][j]));
            }
        }
        return output;
    }

    //Apply softmax function to all values in array
    public static double[][] softmax(double[][] input) {
        int rows = input.length;
        int cols = input[0].length;

        //Find max
        double max = 0;
        for(int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if(input[i][j] > max) {
                    max = input[i][j];
                }
            }
        }

        //Mean-subtracted exponential
        double[][] middleLayer = new double[input.length][input[0].length];
        for(int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                middleLayer[i][j] = Math.exp(input[i][j] - max);
            }
        }

        //Find sum
        int sum = 0;
        for(int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sum += middleLayer[i][j];
            }
        }

        //Divide by sum
        double[][] output = new double[middleLayer.length][middleLayer[0].length];
        for(int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                output[i][j] = middleLayer[i][j] / sum;
            }
        }

        return output;
    }

    //Multiply two 1D arrays
    public static double[] multiply(double[]A, double[] B) {
        int size = A.length;

        if (size != B.length) {
            throw new IllegalArgumentException("Unequal sizes of matrices");
        }

        double[] C = new double[size];

        for (int i = 0; i < size; i++) { // aRow
            C[i] = A[i] * B[i];
        }

        return C;
    }

    //Dot product of 2 2D arrays
    public static double[][] dot(double[][] inputOne, double[][] inputTwo) {
        int oneRows = inputOne.length;
        int oneColumns = inputOne[0].length;
        int twoRows = inputTwo.length;
        int twoColumns = inputTwo[0].length;

        if (oneColumns != twoRows) {
            throw new IllegalArgumentException("MatrixMaths.dot: Error! Unequal number of " +
                    "rows and columns ("+oneColumns+" columns and "+twoRows+" rows)");
        }

        double[][] output = new double[oneRows][twoColumns];

        for (int i = 0; i < oneRows; i++) { // one row
            for (int j = 0; j < twoColumns; j++) { // two column
                for (int k = 0; k < oneColumns; k++) { // one column
                    output[i][j] += inputOne[i][k] * inputTwo[k][j];
                }
            }
        }
        return output;
    }
}
