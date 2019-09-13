package mymou.Utils

import android.content.Context
import android.os.Environment
import android.util.Log
import mymou.preferences.PreferencesManager
import mymou.task.backend.WriteDataToFile

import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Created by jbutler on 18/09/2018.
 * Checks if session folder already exists, and creates it if not
 */

class FolderManager(private val context: Context, private val num_monkeys: Int = 0) {

    private val TAG = "FolderManager"
    private val suffixes = arrayListOf("Images","IntArrays")
    private var currentFolder: File? = null

    init {

        getSessionFolder()

    }

    fun getSessionFolder(): File {
        val appFolder = File(generateSessionFolder())
        if (!appFolder.exists()) {
            Log.d(TAG, "$appFolder doesn't exist...")
            appFolder.mkdirs()
            when (appFolder.exists()) {
                true -> makeFoldersForSession(appFolder)
                false -> Log.e(TAG,"CANNOT MAKE BASE FOLDER!!!")
            }
        }
        currentFolder = appFolder
        return appFolder
    }

    fun getImageFolder(): File? {
        return getSubFolder("Images")
    }

    fun getIntArrayFolder(): File? {
        return getSubFolder("IntArrays")
    }

    fun getMonkeyFolder(id: Int): File? {
        return getSubFolder("monkey"+id)
    }

    private fun getSubFolder(suffix: String = ""): File? {
        if (currentFolder == null) getSessionFolder()
        Log.d(TAG, "getting $currentFolder/$suffix")
        return when (suffix) {
            "" -> currentFolder
            else -> File(currentFolder,suffix)
        }
    }

    fun getBaseDate(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).format(System.currentTimeMillis()) //API < 26
        //return DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now()) //API > 25
    }

    fun getTimestamp(): String {
        return SimpleDateFormat("HHmmss_SSS", Locale.ENGLISH).format(System.currentTimeMillis()) //API < 26
    }

    private fun makeFoldersForSession(path: File) {
        Log.d(TAG, "making static sub-folders..")
        for (s in suffixes)
            makeFolder(path, s)

        Log.d(TAG, "making monkey folders..")
         for (i in 1..num_monkeys) {
            makeFolder(path, "monkey"+i)
         }

        // Add headers to text file
        val headers = PreferencesManager(context).data_headers
        Log.d(TAG, "making headers.."+headers)
        WriteDataToFile(headers, context).run()
    }

    private fun generateSessionFolder(): String {
        return Environment.getExternalStorageDirectory().absolutePath + "/Mymou/" + getBaseDate()
    }

    private fun makeFolder(path: File, suffix: String) {
        File(path,suffix).mkdirs()
    }
}
