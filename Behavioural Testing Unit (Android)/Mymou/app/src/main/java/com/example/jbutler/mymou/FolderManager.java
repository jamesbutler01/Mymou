package com.example.jbutler.mymou;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by jbutler on 18/09/2018.
 * Checks if session folder already exists, and creates it if not
 */

public class FolderManager {

    public FolderManager() {
        Log.d("foldermanager","instantiated");
        String folderName = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mymou/" + folderName;
        File appFolder = new File(path);
        if (!appFolder.exists()) {
            Log.d("foldermanager","folder not made.."+path);
            appFolder.mkdir();
            makefoldersforsession(path);
        }
    }

    public File getfoldername() {
        Log.d("foldermanager","getfoldername()");
        String folderName = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mymou/" + folderName;
        File appFolder = new File(path);
        if (!appFolder.exists()) {
            Log.d("foldermanager","getfoldername: folder not made.."+path);
            appFolder.mkdir();
            makefoldersforsession(path);
        }
        return appFolder;
    }

    public void makefoldersforsession(String path) {
        Log.d("foldermanager","making folders..");
        makefolder(path, "i");
        makefolder(path, "f");
        makefolder(path, "O");
        makefolder(path, "V");
    }

    private void makefolder(String path, String suffix) {
        File imagefolder = new File(path + "/" + suffix + "/");
        imagefolder.mkdir();
    }
}
