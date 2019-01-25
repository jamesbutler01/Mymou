package com.example.jbutler.mymou

import android.os.Environment
import android.util.Log

import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * Created by jbutler on 18/09/2018.
 * Checks if session folder already exists, and creates it if not
 */

internal class FolderManager {
    init {
        Log.d("FolderManager", "instantiated")
        val thisPath = makeName()
        val appFolder = File(thisPath)
        if (!appFolder.exists()) {
            Log.d("FolderManager", "Didn't exist, creating...$thisPath")
            appFolder.mkdirs()
            makeFoldersForSession(thisPath)
        }
    }

    fun getFolderName(): File {
        Log.d("FolderManager", "getFolderName()")
        val thisPath = makeName()
        val appFolder = File(thisPath)
        if (!appFolder.exists()) {
            Log.d("FolderManager", "Didn't exist, creating...$thisPath")
            appFolder.mkdirs()
            makeFoldersForSession(thisPath)
        }
        return appFolder
    }

    private fun makeFoldersForSession(path: String) {
        Log.d("FolderManager", "making sub-folders..")
        makeFolder(path, "i")
        makeFolder(path, "f")
        makeFolder(path, "O")
        makeFolder(path, "V")
    }

    private fun makeName(): String {
        val folderName = SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().time)
        return Environment.getExternalStorageDirectory().absolutePath + "/Mymou/" + folderName
    }

    private fun makeFolder(path: String, suffix: String) {
        val thisFolder = File("$path/$suffix/")
        thisFolder.mkdirs()
    }
}
