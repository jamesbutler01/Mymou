package com.example.jbutler.mymou;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Writes linked String into  CURRENT_DATE.txt
 */
class LogEvent implements Runnable {

    private final String message;

    public LogEvent(String msg) {
        message = msg;
    }

    @Override
    public void run() {
        FolderManager folderManager = new FolderManager();
        File appFolder = folderManager.getFolderName();
        String fileName = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        fileName = fileName + ".txt";
        File savefile = new File(appFolder, fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(savefile, true);
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
        Log.d("LogEvent","Data logged");
    }

}