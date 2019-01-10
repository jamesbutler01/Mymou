package com.example.jbutler.mymou

import android.util.Log

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * Writes linked String into  CURRENT_DATE.txt
 */
internal class LogEvent(private val message: String) : Runnable {

    override fun run() {
        val folderManager = FolderManager()
        val appFolder = folderManager.getFolderName()
        var fileName = SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().time)
        fileName = "$fileName.txt"
        val savefile = File(appFolder, fileName)
        try {
            val fileOutputStream = FileOutputStream(savefile, true)
            val printWriter = PrintWriter(fileOutputStream)
            printWriter.println(message)
            printWriter.flush()
            printWriter.close()
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Log.d("LogEvent", "Data logged")
    }

}