package mymou.task.backend

import android.content.Context
import mymou.Utils.FolderManager
import java.io.File
import java.io.IOException
import java.io.FileWriter

/**
 * WriteDataToFile Writes String into CURRENT_DATE.txt using a runnable
 *
 * TODO: convert from thread+runnable into a coroutine
 *
 * @property message - message to log
 */
internal class WriteDataToFile(private val message: String, private val context: Context) : Runnable {
    override fun run() {
        val folderManager = FolderManager(context)
        val appFolder = folderManager.getSessionFolder()
        val fileName = "${folderManager.getBaseDate()}.txt"
        val saveFile = File(appFolder, fileName)
        try {
            FileWriter(saveFile, true).apply {
                write("$message\n")
                close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}