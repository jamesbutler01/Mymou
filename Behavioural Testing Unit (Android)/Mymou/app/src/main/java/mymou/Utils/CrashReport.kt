package mymou.Utils

import android.content.Context
import android.util.Log

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Saves linked String into  CURRENT_DATE.txt
 */
class CrashReport(private val message: Throwable, context: Context) {
    private val TAG = "MymouCrashReport"

    init {
        Log.d(TAG, "Logging message: $message")
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        message.printStackTrace(pw)
        val exceptionAsString = sw.toString() // stack trace as a string
        val folderManager = FolderManager(context)
        val folderName = folderManager.getSessionFolder()
        val fileName = "crashReport.txt"
        val time = folderManager.getTimestamp()
        val toLog = "$time $exceptionAsString"
        val file = File(folderName, fileName)
        try {
            val fileOutputStream = FileOutputStream(file, true)
            val printWriter = PrintWriter(fileOutputStream)
            printWriter.println(toLog)
            printWriter.flush()
            printWriter.close()
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

}