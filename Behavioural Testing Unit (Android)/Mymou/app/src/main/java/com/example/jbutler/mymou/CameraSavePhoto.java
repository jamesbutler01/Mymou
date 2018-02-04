package com.example.jbutler.mymou;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Saves linked file into linked filename
 */
class CameraSavePhoto implements Runnable {

    private final Image mImage;
    private final String timestamp;

    public CameraSavePhoto(Image image, String timestampU) {
        Log.d("CameraSavePhotoThree", "CameraSavePhoto instantiated");
        mImage = image;
        timestamp = timestampU;

    }

    @Override
    public void run() {
        Log.d("CameraSavePhotoThree", "Saving photo");
        String logTimestamp = new SimpleDateFormat("HHmmss_SSS").format(Calendar.getInstance().getTime());
        Log.d("Timer","Photo passed to CameraSavePhotoThree at "+logTimestamp);
        // Convert photo (176 x 144) to byte array (1x25344) to bitmap (176 x 144)
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Log.d("CameraSavePhotoTwo", "Original width: "+bitmap.getWidth()+", original height: "
                +bitmap.getHeight());

        // Trim bitmap as you want:
        int trimLeft = 28;
        int trimRight = 68;
        int trimTop = 31;
        int trimBottom = 31;
        int startX = trimLeft;
        int startY = trimTop;
        int endX = bitmap.getWidth() - trimLeft - trimRight;
        int endY = bitmap.getHeight() - trimTop - trimBottom;

        Bitmap bitmapTrimmed = Bitmap.createBitmap(bitmap, startX, startY, endX, endY);
        Log.d("CameraSavePhotoTwo", "Bitmap width: "+bitmapTrimmed.getWidth()+", bitmap height: "
                +bitmapTrimmed.getHeight());
        int x = bitmapTrimmed.getWidth();
        int y = bitmapTrimmed.getHeight();
        int[] intArray = new int[x * y];
        bitmapTrimmed.getPixels(intArray, 0, x, 0, 0, x, y);
        for(int i = 0; i < intArray.length; i++) {
            intArray[i] = Color.red(intArray[i]); //Any colour will do as greyscale
        }

        //Log text data with photoId
        MainMenu.setMonkeyId(intArray);

        //Save pixel values
        saveIntArray(intArray);

        //Save photo as jpeg
        savePhoto(bitmapTrimmed);

    }

    private void savePhoto(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bytes = stream.toByteArray();
        String folderName = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        String path= Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mymou/" + folderName;
        File appFolder = new File(path);
        if (!appFolder.exists())
            appFolder.mkdir();
        path = path + "/i/";
        File actualFolder = new File(path);
        if (!actualFolder.exists())
            actualFolder.mkdir();
        String fileName = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        fileName = fileName + "_" + timestamp + ".jpg";
        File backupFile = new File(actualFolder, fileName);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(backupFile);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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
        Log.d("CameraSavePhotoThree", "Saving array");
        String folderName = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mymou/" + folderName;
        File appFolder = new File(path);
        if (!appFolder.exists())
            appFolder.mkdir();
        path = path + "/f/";
        File actualFolder = new File(path);
        if (!actualFolder.exists())
            actualFolder.mkdir();
        String fileName = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        fileName = "f"+fileName + "_" + timestamp + ".txt";
        File backupFile = new File(actualFolder, fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(backupFile, true);
            PrintWriter printWriter = new PrintWriter(fileOutputStream);
            int n = intArray.length;
            Log.d("CameraSavePhotoThree", Integer.toString(n));
            for(int i = 0; i < n; i++) {
                printWriter.println(Double.toString(intArray[i]));
                printWriter.flush();
            }
            printWriter.close();
            fileOutputStream.close();
            Log.d("CameraSavePhotoThree", "Array saved");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

