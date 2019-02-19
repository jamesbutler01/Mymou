package com.example.jbutler.mymou;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

/**
 * Saves linked file into linked filename
 */
class CameraSavePhoto implements Runnable {

    private String TAG = "CameraSavePhoto";
    private final Image mImage;
    private String timestamp;
    private final String day;

    public CameraSavePhoto(Image image, String timestampU) {
        mImage = image;
        timestamp = timestampU;
        day = MainMenu.folderManager.getBaseDate();
        Log.d(TAG, " instantiated");
    }

    @Override
    public void run() {
        Log.d(TAG, "Running CameraSavePhoto..");

        // Convert photo to byte array
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        // Convert byte array to bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // Crop bitmap as you want:
        boolean cropBitmap = true;
        Bitmap bitmapCropped;
        if (cropBitmap) {
            int cropLeft = 63;
            int cropRight = 123;
            int cropTop = 95;
            int cropBottom = 31;
            int startX = cropLeft;
            int startY = cropTop;
            int endX = bitmap.getWidth() - cropLeft - cropRight;
            int endY = bitmap.getHeight() - cropTop - cropBottom;

            bitmapCropped = Bitmap.createBitmap(bitmap, startX, startY, endX, endY);
        } else {
            bitmapCropped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
        }

        // Create integer array for facerecog
        int x = bitmapCropped.getWidth();
        int y = bitmapCropped.getHeight();
        int[] intArray = new int[x * y];
        bitmapCropped.getPixels(intArray, 0, x, 0, 0, x, y);
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = Color.red(intArray[i]); //Any colour will do as greyscale
        }

        //Log text data with photoId
        if (MainMenu.useFaceRecognition) {
            TaskManager.setFaceRecogPrediction(intArray);
        }

        //Save pixel values
        saveIntArray(intArray);

        //Save photo as jpeg
        savePhoto(bitmapCropped);
    }


    private void savePhoto(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bytes = stream.toByteArray();
        File folder = MainMenu.folderManager.getSubFolder("i");
        String fileName = day + "_" + timestamp + ".jpg";
        File filetowrite = new File(folder, fileName);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(filetowrite);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "Bitmap saved "+fileName);
            mImage.close();
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveIntArray(int[] intArray) {
        File folder = MainMenu.folderManager.getSubFolder("f");
        String fileName = "f"+ day + "_" + timestamp + ".txt";
        File savefile = new File(folder, fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(savefile, true);
            PrintWriter printWriter = new PrintWriter(fileOutputStream);
            int n = intArray.length;
            for(int i = 0; i < n; i++) {
                printWriter.println(Double.toString(intArray[i]));
                printWriter.flush();
            }
            printWriter.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Int array saved"+fileName);
    }
}

