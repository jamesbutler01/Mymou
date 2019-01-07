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

class FolderManager {

    public FolderManager() {
        Log.d("FolderManager","instantiated");
        String thisPath = MakeName();
        File appFolder = new File(thisPath);
        if (!appFolder.exists()) {
            Log.d("FolderManager","Didn't exist, creating..." + thisPath);
            appFolder.mkdirs();
            MakeFoldersForSession(thisPath);
        }
    }

    public File getfoldername() {
        Log.d("FolderManager","getfoldername()");
        String thisPath = MakeName();
        File appFolder = new File(thisPath);
        if (!appFolder.exists()) {
            Log.d("FolderManager","Didn't exist, creating..." + thisPath);
            appFolder.mkdirs();
            MakeFoldersForSession(thisPath);
        }
        return appFolder;
    }

    private void MakeFoldersForSession(String path) {
        Log.d("FolderManager","making sub-folders..");
        MakeFolder(path, "i");
        MakeFolder(path, "f");
        MakeFolder(path, "O");
        MakeFolder(path, "V");
    }

    private String MakeName() {
        String folderName = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mymou/" + folderName;
        return path;
    }

    private void MakeFolder(String path, String suffix) {
        File imagefolder = new File(path + "/" + suffix + "/");
        imagefolder.mkdir();
    }
}
