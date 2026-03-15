package org.banana.project.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private const val APP_NAME = "BananaMobile"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    private fun log(priority: Int, levelString: String, message: String, throwable: Throwable? = null) {
        val stackTrace = Thread.currentThread().stackTrace
        var caller: StackTraceElement? = null
        
        for (i in 3 until stackTrace.size) {
            val element = stackTrace[i]
            if (element.className != AppLogger::class.java.name && !element.className.startsWith("java.lang.Thread")) {
                caller = element
                break
            }
        }
        
        val fileName = caller?.fileName ?: "Unknown"
        val lineNumber = caller?.lineNumber ?: -1
        
        val dateTime = dateFormat.format(Date())
        val formattedMessage = "[$dateTime] [$levelString] [$APP_NAME] [$fileName:$lineNumber] $message"
        
        Log.println(priority, APP_NAME, formattedMessage)
        throwable?.let {
            Log.println(priority, APP_NAME, Log.getStackTraceString(it))
        }
    }

    fun d(message: String) = log(Log.DEBUG, "DEBUG", message)
    fun i(message: String) = log(Log.INFO, "INFO", message)
    fun w(message: String) = log(Log.WARN, "WARN", message)
    fun e(message: String, throwable: Throwable? = null) = log(Log.ERROR, "ERROR", message, throwable)
}
