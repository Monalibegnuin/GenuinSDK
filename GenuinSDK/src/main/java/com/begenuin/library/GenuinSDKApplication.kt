package com.begenuin.library

import android.app.Application
import android.app.NotificationManager
import androidx.core.content.ContextCompat.getSystemService
import com.begenuin.library.common.AppVisibilityDetector
import com.begenuin.library.data.db.QueryDataHelper

class GenuinSDKApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        initDB()
        initVisibilityDetector()
    }

    private fun initDB() {
        if (dbHelper == null) {
            dbHelper = QueryDataHelper(this)
            dbHelper!!.openDatabase()
        }
    }

    private fun initVisibilityDetector() {
        AppVisibilityDetector.init(instance, object : AppVisibilityDetector.AppVisibilityCallback {
            override fun onAppGotoForeground() {
                isInForGround = true
                try {
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(1001)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onAppGotoBackground() {
                isInForGround = false
            }
        })
    }

    companion object {
        lateinit var instance: GenuinSDKApplication
        var isInForGround = true
        var dbHelper: QueryDataHelper? = null
    }
}