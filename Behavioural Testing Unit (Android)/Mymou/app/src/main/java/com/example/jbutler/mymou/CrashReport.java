package com.example.jbutler.mymou;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Saves linked String into  CURRENT_DATE.txt
 */
class CrashReport implements Runnable {

    private final Throwable message;

    public CrashReport(Throwable msg) {
        message = msg;
    }

    @Override
    public void run() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        message.printStackTrace(pw);
        String exceptionAsString = sw.toString(); // stack trace as a string
        File backupFile;
        File appFolder;
        String folderName = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mymou/" + folderName;
        appFolder = new File(path);
        if (!appFolder.exists())
            appFolder.mkdir();
        String fileName = "crashReport.txt";
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(Calendar.getInstance().getTime());
        String toLog = date + " " + exceptionAsString;
        backupFile = new File(appFolder, fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(backupFile, true);
            PrintWriter printWriter = new PrintWriter(fileOutputStream);
            printWriter.println(toLog);
            printWriter.flush();
            printWriter.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}