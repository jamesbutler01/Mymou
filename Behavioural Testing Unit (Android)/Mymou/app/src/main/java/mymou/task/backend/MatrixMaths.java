package mymou.task.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.StrictMath.abs;

public class MatrixMaths {

    //Tranpose matrix
    public static double[][] transpose(double[][] input) {
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
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                output[i][j] = Math.tanh(input[i][j]);
            }
        }
        return output;
    }

    //Apply sigmoid function to all values in array
    public static double[][] sigmoid(double[][] input) {
        double[][] output = new double[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
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
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (input[i][j] > max) {
                    max = input[i][j];
                }
            }
        }

        //Mean-subtracted exponential
        double[][] middleLayer = new double[input.length][input[0].length];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                middleLayer[i][j] = Math.exp(input[i][j] - max);
            }
        }

        //Find sum
        int sum = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sum += middleLayer[i][j];
            }
        }

        //Divide by sum
        double[][] output = new double[middleLayer.length][middleLayer[0].length];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                output[i][j] = middleLayer[i][j] / sum;
            }
        }

        return output;
    }

    //Multiply two 1D arrays
    public static double[] multiply(double[] A, double[] B) {
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
                    "rows and columns (" + oneColumns + " columns and " + twoRows + " rows)");
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

    // For a range of size max_n returns n random elements
    public static int[] randomNoRepeat(int n, int max_n) {
        // Make range
        ArrayList<Integer> range = new ArrayList<Integer>();
        for (int i = 0; i < max_n; i++) range.add(i);

        // Shuffle the range
        Collections.shuffle(range);

        // Take first elements up to n
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            out[i] = range.get(i);
        }

        return out;
    }

    private static int[] convertImageNumToCoords(int imageNum, int width) {
        int[] locs = new int[2];
        locs[0] = (imageNum % width);
        locs[1] = (imageNum - imageNum % width) / width;
        return locs;
    }

    public static int[][] generateTransitionMatrix(int xlength, int ylength, boolean torus) {
        int numStimuli = xlength * ylength;
        int[][] transMatrix = new int[numStimuli][numStimuli];

        for (int i = 0; i < numStimuli; i++) {
            int[] startLocs = convertImageNumToCoords(i, xlength);
            for (int j = 0; j < numStimuli; j++) {
                int[] endLocs = convertImageNumToCoords(j, xlength);
                if (!torus) {
                    transMatrix[i][j] = abs(endLocs[0] - startLocs[0]) + abs(endLocs[1] - startLocs[1]);
                } else {
                    int xdist = abs(endLocs[0] - startLocs[0]);
                    int ydist = abs(endLocs[1] - startLocs[1]);
                    if (xdist > xlength / 2) {
                        xdist = xlength - xdist; // Go the other way as shorter
                    }
                    if (ydist > ylength / 2) {
                        ydist = ylength - ydist;
                    }
                    transMatrix[i][j] = xdist + ydist;
                }
            }
        }
        return transMatrix;

    }
}
