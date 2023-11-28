package com.begenuin.library.common

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ShareCompat.IntentBuilder
import com.begenuin.library.R
import com.begenuine.feedscreensdk.common.Constants
import com.begenuine.feedscreensdk.common.ShowLogger
import com.bumptech.glide.Glide
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import java.io.IOException
import java.util.Locale

object Utility {

    @JvmStatic
        fun isNetworkAvailable(context: Context?): Boolean {
            if (context == null) return false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                return if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    true
                } else capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            }
        } else {
            try {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    return true
                }
            } catch (e: Exception) {
                showLogException(e)
            }
        }
            return false
        }

    @JvmStatic
    fun printErrorLog(string: String) {
       showLog("EEEEEE", "" + string)
    }

    @JvmStatic
    fun showLog(tag: String?, message: String?) {
        //if (BuildConfig.DEBUG) {
            Log.e(tag, message!!)
        //}
    }

    @JvmStatic
    fun showLogException(e: java.lang.Exception) {
       showLog("Tag", e.message)
       // if (BuildConfig.DEBUG) {
            ShowLogger().log("", e)
       // }
    }

    @JvmStatic
    fun bodyToString(request: RequestBody?): String? {
        return try {
            val buffer = Buffer()
            if (request != null) request.writeTo(buffer) else return ""
            buffer.readUtf8()
        } catch (e: IOException) {
            "did not work"
        }
    }

    @JvmStatic
    fun getAFTopic(context: Context?, topic: String): String? {
        return if (topic.equals(context?.resources?.getString(R.string.topic1), ignoreCase = true)) {
            context?.resources?.getString(R.string.af_topic1)
        } else if (topic.equals(context?.resources?.getString(R.string.topic2), ignoreCase = true)) {
            context?.resources?.getString(R.string.af_topic2)
        } else if (topic.equals(context?.resources?.getString(R.string.topic3), ignoreCase = true)) {
            context?.resources?.getString(R.string.af_topic3)
        } else if (topic.equals(context?.resources?.getString(R.string.topic4), ignoreCase = true)) {
            context?.resources?.getString(R.string.af_topic4)
        } else {
            ""
        }
    }

    @JvmStatic
    fun getRequestBody(jsonStr: String?): RequestBody? {
         showLog("request:", jsonStr)
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStr)
    }

    @JvmStatic
    fun formatNumber(count: Long): String? {
        if (count < 1000) return "" + count
        val exp = (Math.log(count.toDouble()) / Math.log(1000.0)).toInt()
        return String.format(
            Locale.ENGLISH, "%.1f%c", count / Math.pow(1000.0, exp.toDouble()),
            "kMBTPE"[exp - 1]
        )
    }

    @JvmStatic
    fun showToast(context: Context?, text: String?) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    @JvmStatic
    fun loadBitmapFromView(v: View): Bitmap? {
        return if (v.measuredHeight <= 0) {
            v.measure(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val b = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            v.layout(0, 0, v.measuredWidth, v.measuredHeight)
            v.draw(c)
            b
        } else {
            val b = Bitmap.createBitmap(
                v.width, v.height,
                Bitmap.Config.ARGB_8888
            )
            val c = Canvas(b)
            v.layout(0, 0, v.width, v.height)
            v.draw(c)
            b
        }
    }

    @JvmStatic
    fun loadFixedWidthBitmapFromView(v: View): Bitmap? {
        return loadFixedWidthBitmapFromView(v, 420)
    }

    @JvmStatic
    fun loadFixedWidthBitmapFromView(v: View, width: Int): Bitmap? {
        return if (v.measuredHeight <= 0) {
            v.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val b = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            v.layout(0, 0, v.measuredWidth, v.measuredHeight)
            v.draw(c)
            b
        } else {
            val b = Bitmap.createBitmap(
                width, v.height,
                Bitmap.Config.ARGB_8888
            )
            val c = Canvas(b)
            v.layout(0, 0, width, v.height)
            v.draw(c)
            b
        }
    }

    @JvmStatic
    fun displayProfileImage(context: Context?, imagePath: String?, imageView: ImageView?) {
        Glide.with(context!!).asDrawable().load(imagePath).placeholder(R.color.color_E7E7E7)
            .error(R.drawable.ic_no_profile).into(imageView!!)
    }

    @JvmStatic
    fun dpToPx(dp: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

    fun pxToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getScreenWidthHeight(mActivity: Activity): IntArray? {
        val screenWidth: Int
        val screenHeight: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = mActivity.windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            screenWidth = windowMetrics.bounds.width() - insets.left - insets.right
            screenHeight = windowMetrics.bounds.height() - insets.top - insets.bottom
            showLog("RHeight", "$screenWidth : $screenHeight")
        } else {
            val displayMetrics = DisplayMetrics()
            mActivity.display!!.getMetrics(displayMetrics)
             showLog(
                "RHeight1",
                displayMetrics.widthPixels.toString() + " : " + displayMetrics.heightPixels
            )
            screenWidth = displayMetrics.widthPixels
            screenHeight = displayMetrics.heightPixels
        }
        return intArrayOf(screenWidth, screenHeight)
    }

    fun timeFormat(timeStamp: Long): String {
        val difference = System.currentTimeMillis() / 1000 - timeStamp
        val formattedTime: String
        if (difference < 60) {
            formattedTime = "Just now"
        } else if (difference < 3600) {
            val temp = (difference / 60).toInt()
            formattedTime = if (temp == 1) {
                "1m"
            } else {
                temp.toString() + "m"
            }
        } else if (difference < 3600 * 24) {
            val temp = (difference / 3600).toInt()
            formattedTime = if (temp == 1) {
                "1h"
            } else {
                temp.toString() + "h"
            }
        } else {
            var temp = (difference / (3600 * 24)).toInt()
            if (temp == 1) {
                formattedTime = "1d"
            } else if (temp < 7) {
                formattedTime = temp.toString() + "d"
            } else if (temp == 7) {
                formattedTime = "1w"
            } else {
                temp = temp / 7
                formattedTime = temp.toString() + "w"
            }
        }
        return formattedTime
    }

    fun showMuteUnMuteToast(context: Activity, drawable: Int) {
        val li = context.layoutInflater
        val layout: View = li.inflate(R.layout.custom_toast_layout, null)
        val imageView = layout.findViewById<ImageView>(R.id.imageViewSaved)
        imageView.setImageResource(drawable)
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, -80)
        toast.setView(layout)
        toast.show()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ toast.cancel() }, 500)
    }

    fun showCustomToast(context: Activity, text: String?) {
        var textView: TextView
        //Creating the LayoutInflater instance
        val li = context.layoutInflater
        //Getting the View object as defined in the customtoast.xml file
        val layout = li.inflate(R.layout.custom_toast_layout, null)
        //        textView = layout.findViewById(R.id.text);
//        textView.setText(text);
        //Creating the Toast object
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -80)
        toast.setView(layout) //setting the view of custom toast layout
        toast.show()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ toast.cancel() }, 500)
    }

    fun shareVideoLink(context: Context?, shareURL: String, contentId: String, from: String) {
        var shareURL = shareURL
        //TODO: used it from sharedPreferences, how will we get the dat
        val userName: String = ""
        //val userName: String = getCurrentUserNickName(context)
        if (!TextUtils.isEmpty(userName)) {
            // UTM parameters added.
            shareURL += if (shareURL.contains("?")) {
                "&" + Constants.DEEP_LINK_UTM_SOURCE
            } else {
                "?" + Constants.DEEP_LINK_UTM_SOURCE
            }
            shareURL += "&" + Constants.DEEP_LINK_FROM_USERNAME + userName
        }
        IntentBuilder(context!!)
            .setType("text/plain")
            .setText(shareURL)
            .setChooserTitle("Share...")
            .startChooser()
    }

    fun showCustomSubscribeToast(context: Activity) {
        val li = context.layoutInflater
        val layout = li.inflate(R.layout.custom_toast_layout, null)
        val imageView = layout.findViewById<ImageView>(R.id.imageViewSaved)
        imageView.setImageResource(R.drawable.ic_rt_subcribed)
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, -80)
        toast.setView(layout)
        toast.show()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ toast.cancel() }, 500)
    }
}