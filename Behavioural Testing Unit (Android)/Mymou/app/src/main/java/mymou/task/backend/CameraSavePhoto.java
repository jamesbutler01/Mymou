package mymou.task.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import androidx.preference.PreferenceManager;
import mymou.Utils.FolderManager;

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
    private Context mContext;
    private FolderManager folderManager = new FolderManager();

    public CameraSavePhoto(Image image, String timestampU, Context context) {
        mImage = image;
        timestamp = timestampU;
        day = folderManager.getBaseDate();
        mContext = context;
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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        Bitmap bitmapCropped;
        if (settings.getBoolean("crop_photos", false)) {
            // For some reason photo is rotated 90 degrees so adjust crop settings accordingly
            int cropLeft = settings.getInt("crop_bottom", 0);
            int cropRight = settings.getInt("crop_top", 0);
            int cropTop = settings.getInt("crop_right", 0);
            int cropBottom = settings.getInt("crop_left", 0);

            int startX = cropLeft;
            int startY = cropTop;
            int endX = bitmap.getWidth() - cropLeft - cropRight;
            int endY = bitmap.getHeight() - cropTop - cropBottom;

            bitmapCropped = Bitmap.createBitmap(bitmap, startX, startY, endX, endY);

        } else {
            // Just take whole image
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

        // Run image through faceRecog
        TaskManager.setFaceRecogPrediction(intArray);

        //Save pixel values
        saveIntArray(intArray);

        //Save photo as jpeg
        savePhoto(bitmapCropped);
        timestamp+=2;
        savePhoto(bitmap);
    }

    // TODO: Merge savephoto and saveintarray to reduce repeated code
    private void savePhoto(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bytes = stream.toByteArray();
        File folder = folderManager.getImageFolder();
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
        File folder = folderManager.getIntArrayFolder();
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

