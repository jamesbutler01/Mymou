package com.example.jbutler.mymou;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Saves linked String into  CURRENT_DATE.txt
 */
class LogEvent implements Runnable {

    private final String message;

    public LogEvent(String msg) {
        message = msg;
    }

    @Override
    public void run() {
            File backupFile;
            File appFolder;
            String folderName = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mymou/" + folderName;
            appFolder = new File(path);
            if (!appFolder.exists())
                appFolder.mkdir();
            String fileName = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
            fileName = fileName + ".txt";
            backupFile = new File(appFolder, fileName);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(backupFile, true);
                PrintWriter printWriter = new PrintWriter(fileOutputStream);
                printWriter.println(message);
                printWriter.flush();
                printWriter.close();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("tag","Data logged");
    }

}