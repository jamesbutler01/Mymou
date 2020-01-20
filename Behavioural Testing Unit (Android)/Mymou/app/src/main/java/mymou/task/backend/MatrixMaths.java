package mymou.task.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.StrictMath.abs;

/**
 *
 * Basic matrix functions for use with facial recognition
 *
 */
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

    /**
     *
     * Converts unique state references to euclidean coordinate position for a square graph
     *
     * E.g., in a 10x10 graph:
     *      state 0 will return (0, 0)
     *      state 9 will return (9, 0)
     *      state 90 will return (9, 0)
     *      state 99 will return (9, 9)
     *
     * @param state Location of interest
     * @param x_width Width/height of the graph
     * @return integer array of length 2 corresponding to x and y position respectively
     */
    private static int[] convertImageNumToCoords(int state, int x_width) {
        int[] locs = new int[2];
        locs[0] = (state % x_width);
        locs[1] = (state - state % x_width) / x_width;
        return locs;
    }

    /**
     *
     * Generate a distance matrix for a graph with 4-edges per node
     * Output will be a matrix of size xlength*ylength x xlength*ylength
     * Connected nodes will equal 1
     *
     * @param xlength Size of first dimension
     * @param ylength Size of second dimension
     * @param torus If specified, graph will have no edges
     * @return Matrix of distances between all points on the graph
     *
     */
    public static int[][] generateDistanceMatrix(int xlength, int ylength, boolean torus) {
        int numStimuli = xlength * ylength;
        int[][] distMatrix = new int[numStimuli][numStimuli];

        for (int i = 0; i < numStimuli; i++) {
            int[] startLocs = convertImageNumToCoords(i, xlength);
            for (int j = 0; j < numStimuli; j++) {
                int[] endLocs = convertImageNumToCoords(j, xlength);
                if (!torus) {
                    distMatrix[i][j] = abs(endLocs[0] - startLocs[0]) + abs(endLocs[1] - startLocs[1]);
                } else {
                    int xdist = abs(endLocs[0] - startLocs[0]);
                    int ydist = abs(endLocs[1] - startLocs[1]);
                    if (xdist > xlength / 2) {
                        xdist = xlength - xdist; // Go the other way as shorter
                    }
                    if (ydist > ylength / 2) {
                        ydist = ylength - ydist;
                    }
                    distMatrix[i][j] = xdist + ydist;
                }
            }
        }
        return distMatrix;

    }
}
