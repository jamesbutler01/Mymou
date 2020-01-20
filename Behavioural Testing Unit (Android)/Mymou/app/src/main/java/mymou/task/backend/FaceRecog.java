package mymou.task.backend;

import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


/**
 * Facial Recognition module
 *
 * Receives image input from CameraSavePhoto and return prediction from network
 * Expensive to instantiate, so instantiate once at beginning of a session
 *      As has to read in weights from CSV files on start up
 *
 * Dependencies:
 *      Uses opencsv parser library
 *          Download from http://opencsv.sourceforge.net/
 *      Which in turn requires Apache Commons Lang 3.6 and Commons BeanUtils
 *          Download from http://commons.apache.org/proper/commons-beanutils/index.html and
 *          http://commons.apache.org/proper/commons-lang/download_lang.cgi respectively
 *      Place the three .jar file in libs folder
 *      Build --> Edit Libraries and Dependencies --> Add opencsv JAR
 */

final class FaceRecog {

    private String TAG = "FaceRecog";
    public boolean instantiated_successfully = true;
    public String error_message;
    double[][] wi, wo;
    double mean, var;

    public FaceRecog() {
        wi = loadWeights("wi.txt");
        wo = loadWeights("wo.txt");
        double[][] meanAndVar;
        try {
            meanAndVar = loadWeights("meanAndVar.txt");
            mean = meanAndVar[0][0];
            var = meanAndVar[1][0];
        } catch (NullPointerException e){
            instantiated_successfully = false;
            error_message = "Weights for neural network not found (\'meanAndVar.txt\'). Disable Facial recognition in settings to fix this error message, or supply weights for the network to use";
        }
        Log.d(TAG,"faceRecog instantiated.."+mean+" "+var);
    }

    public final double[][] loadWeights(String fileName) {
        // Read all
        String path= Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mymou/" +
                fileName;
        try {
            CSVReader csvReader = new CSVReader(new FileReader(new File(path)));
            List<String[]> list = csvReader.readAll();
            // Convert to 2D array
            double[][] dataArr = convertStringListToFloatArray(list);
            return dataArr;
        } catch (IOException e) {
            //TODO: No file present
        }

        Log.d(TAG,"Couldn't open CSV files");
        return null;
    }

    private final double[][] convertStringListToFloatArray(List<String[]> stringList) {
        //Convert List String to String matrix
        String[][] stringArray = new String[stringList.size()][];
        stringArray = stringList.toArray(stringArray);

        //Then convert String matrix to Double Matrix
        int rows = stringArray.length;
        int columns = stringArray[0].length;
        Log.d(TAG,""+rows+" "+columns);
        double[][] doubleArray = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j ++) {
                try {
                    doubleArray[i][j] = Double.parseDouble(stringArray[i][j]);
                } catch (NumberFormatException e) {
                  Log.d(TAG,e+" "+stringArray[i][j]);
                }
            }
        }
        return doubleArray;
    }

    //Convert 1D int array to 2D double array
    //Also adds extra point to the array
    public static double[][] convertIntToDoubleArray(int[] source) {
        int length = source.length;
        double[][] dest = new double[1][length + 1];
        for(int i=0; i<length; i++) {
            dest[0][i] = source[i];
        }

        //Add extra point at end to compensate for bias weight
        dest[0][length] = 0;

        return dest;
    }


    // Standardise array by subtracting mean and setting variance to 1
    private double[][] standardiseArray(double[][] input) {
        double[][] output = new double[input.length][input[0].length];
        int rows = input.length;
        int columns = input[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j ++) {
                output[i][j] = (input[i][j] - mean) / var;
            }
        }
        return output;
    }

    public int idImage(int[] input) {
        Log.d(TAG,"FaceRecog started");

        //Convert photo int array to double array
        double[][] doubleArray = convertIntToDoubleArray(input);

        //Subtract training mean and variance
        double[][] standardisedArray = standardiseArray(doubleArray);

        // Hidden activations
        double[][] sumHidden = MatrixMaths.dot(standardisedArray, wi);
        double[][] activationHidden = MatrixMaths.tanh(sumHidden);

        // Output activations
        double[][] sumOutput = MatrixMaths.dot(activationHidden, wo);
        double[][] activationOutput = MatrixMaths.softmax(sumOutput);

        Log.d(TAG,"FaceRecog finished");

        if (activationOutput[0][0] > activationOutput[0][1]) {
            return 1;  // Monkey O
        } else {
            return 0;  // Monkey V
        }
    }

}



