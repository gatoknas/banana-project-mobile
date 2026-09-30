package org.banana.project

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.banana.project.utils.AppLogger

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.fatal(
                event = "uncaught_application_crash",
                message = "Fatal crash on thread ${thread.name}: ${throwable.message}",
                throwable = throwable
            )
            defaultHandler?.uncaughtException(thread, throwable)
        }

        AppLogger.i("application_lifecycle", "MyApplication initialized successfully")
    }
}