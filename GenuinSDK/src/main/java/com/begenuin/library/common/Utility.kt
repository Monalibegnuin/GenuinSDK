package com.begenuin.library.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ShareCompat.IntentBuilder
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.begenuin.begenuin.data.model.EditorColorsModel
import com.begenuin.library.GenuinSDKApplication
import com.begenuin.library.R
import com.begenuin.library.SDKInitiate
import com.begenuin.library.common.customViews.CustomEditTextWithError
import com.begenuin.library.core.enums.NotificationType
import com.begenuin.library.core.enums.VideoConvType
import com.begenuin.library.data.db.QueryDataHelper
import com.begenuin.library.data.model.ChatModel
import com.begenuin.library.data.model.CommentModel
import com.begenuin.library.data.model.ConversationModel
import com.begenuin.library.data.model.EditorFontModel
import com.begenuin.library.data.model.LoopsModel
import com.begenuin.library.data.model.MemberInfoModel
import com.begenuin.library.data.model.MembersModel
import com.begenuin.library.data.model.MessageModel
import com.begenuin.library.data.model.MetaDataModel
import com.begenuin.library.views.activities.HomeScreen
import com.begenuine.feedscreensdk.common.ShowLogger
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

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

    @JvmStatic
    fun pxToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @JvmStatic
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

    @JvmStatic
    fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is MarginLayoutParams) {
            val p = v.layoutParams as MarginLayoutParams
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }

    @JvmStatic
    fun getStatusBarHeight(context: Context): Int {
        var statusBarHeight = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

    @JvmStatic
    fun printResponseLog(string: String?) {
         showLog("DDDDDD Response", string)
    }

    @JvmStatic
    fun hideKeyboard(mContext: Context, editText: EditText) {
        val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
    @JvmStatic
    fun vibrateDevice(activity: Activity) {
        val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            vibrator.vibrate(50)
        }
    }

    @JvmStatic
    fun changeWindowStatusBarColor(activity: Activity, color: Int) {
        val window = activity.window
        //
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        //window.setStatusBarColor(activity.getColor(color));
        val whiteColor = R.color.colorWhite
        val blackColor = R.color.black_111111
        val translucentBlack = R.color.translucent_black
        val splashColor = R.color.splash_background
        val decorView = window.decorView
        if (color == whiteColor) {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else if (color == blackColor || color == translucentBlack || color == splashColor) {
            decorView.systemUiVisibility =
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() //set status text  light
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.statusBarColor = activity.getColor(color)
    }

    fun getColorById(context: Context, id: Int): Int {
        return context.resources.getColor(id, null)
    }

    fun setShadow(context: Context, fontColor: Int, textView: TextView) {
        if (fontColor ==  getColorById(
                context,
                R.color.colorWhite
            )
        ) {
            textView.setShadowLayer(4f, 2f, 2f, Color.BLACK)
        } else {
            textView.setShadowLayer(0f, 0f, 0f, fontColor)
        }
    }

    fun removeShadow(textView: TextView) {
        textView.setShadowLayer(0f, 0f, 0f, 0)
    }

    fun dpConversionForEditor(context: Context, value: Int): Int {
        return (context.resources.displayMetrics.density * value + 0.5f).toInt()
    }

    @JvmStatic
    fun setFontFromRes(context: Context, fondId: Int): Typeface? {
        val typeface: Typeface? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.resources.getFont(fondId)
        } else {
            ResourcesCompat.getFont(context, fondId)
        }
        return typeface
    }

    @JvmStatic
    fun getColorListFromAssets(context: Context): ArrayList<EditorColorsModel>? {
        val finalList = ArrayList<EditorColorsModel>()
        try {
            val jsonFileString: String =
                loadJSONFromAsset(context, R.raw.colors_data)
            // Utility.printErrorLog("Font - JSON: $jsonFileString")
            val jsonMain = JSONObject(jsonFileString)
            val jsonObjData = jsonMain.getJSONObject("data")
            val colorsList = jsonObjData.getJSONArray("Colors")
            val listType = object : TypeToken<List<EditorColorsModel?>?>() {}.type
            finalList.addAll(Gson().fromJson(colorsList.toString(), listType))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return finalList
    }

    fun loadJSONFromAsset(context: Context, file: Int): String {
        var data = ""
        try {
            val stream = context.resources.openRawResource(file)
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            data = buffer.toString()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return data
    }

    @JvmStatic
    fun getFontListFromJSONAssets(context: Context): ArrayList<EditorFontModel?> {
        val finalList: ArrayList<EditorFontModel?> = ArrayList<EditorFontModel?>()
        try {
            val jsonFileString: String =
                loadJSONFromAsset(context, R.raw.fonts_data)
            val jsonMain = JSONObject(jsonFileString)
            val jsonObjData = jsonMain.getJSONObject("data")
            val jsonFontsArray = jsonObjData.getJSONArray("Fonts")
            val listType = object : TypeToken<List<EditorFontModel?>?>() {}.type
            finalList.addAll(
                Gson().fromJson<Collection<EditorFontModel?>>(
                    jsonFontsArray.toString(),
                    listType
                )
            )
            val fontFields = R.font::class.java.fields
            try {
                for (field in fontFields) {
                    for (j in finalList.indices) {
                        val fontName: String? = finalList[j]?.FontName?.toLowerCase()
                        if (field.name.lowercase(Locale.getDefault()) == fontName) {
                            val model = EditorFontModel()
                            model.FontId = field.getInt(null)
                            finalList[j] = model
                        }
                    }
                }
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return finalList
    }

    @JvmStatic
    fun milliToStringMerge1(millis: Long): String? {
        val secs = millis / 1000
        val mls = millis % 1000
        return String.format(Locale.ENGLISH, "%d.%d", secs, mls)
    }

    @JvmStatic
    fun getDurationText(duration: String): String? {
        var durationText = ""
        try {
            if (!TextUtils.isEmpty(duration)) {
                val dur = duration.toInt()
                durationText = if (dur < 10) {
                    "0" + dur + "s"
                } else {
                    dur.toString() + "s"
                }
            }
        } catch (e: java.lang.Exception) {
            showLogException(e)
        }
        return durationText
    }

    @JvmStatic
    fun showKeyboard(mContext: Context, editText: EditText?) {
        val inputMethodManager =
            mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    @JvmStatic
    /*public static void insertEventToAppsFlyer(Context mContext, String key) {
        Map<String, Object> eventValue = new HashMap<>();
        eventValue.put(Constants.KEY_CONTENT_CATEGORY, key);
        AppsFlyerLib.getInstance().logEvent(mContext, key, eventValue);
    }*/
    fun milliToString(millis: Long): String? {
        val hrs = TimeUnit.MILLISECONDS.toHours(millis) % 24
        val min = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val sec = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        val mls = millis % 1000
        return String.format(Locale.ENGLISH, "%02d:%02d:%02d.%01d", hrs, min, sec, mls)
    }

    @JvmStatic
    fun getDurationInt(duration: Long): Int {
        var durationInt = 0
        try {
            val dur = (duration / 1000f).toDouble()
            durationInt = Math.round(dur).toInt()
        } catch (e: java.lang.Exception) {
             showLogException(e)
        }
        return durationInt
    }

    @JvmStatic
    fun getVideoResolution(videoWidth: Int, videoHeight: Int): String? {
        val ratio20by9 = 20f / 9f
        val ratio19by9 = 19f / 9f
        val ratio18by9 = 18f / 9f
        val ratio17by9 = 17f / 9f
        val ratio16by9 = 16f / 9f
        val deviceRatio = videoWidth * 1.0f / (videoHeight * 1.0f)
        val outPutResolution: String = if (deviceRatio >= ratio20by9) {
            "800x1776"
        } else if (deviceRatio >= ratio19by9) {
            "760x1604"
        } else if (deviceRatio >= ratio18by9) {
            "736x1472"
        } else if (deviceRatio >= ratio17by9) {
            "720x1354"
        } else if (deviceRatio >= ratio16by9) {
            "688x1220"
        } else {
            "640x960"
        }
        return outPutResolution
    }

    @JvmStatic
    fun getVideoAspectRatio(videoWidth: Int, videoHeight: Int): String? {
        val ratio: String
        val ratio20by9 = 20f / 9f
        val ratio19by9 = 19f / 9f
        val ratio18by9 = 18f / 9f
        val ratio17by9 = 17f / 9f
        val ratio16by9 = 16f / 9f
        val deviceRatio = videoWidth * 1.0f / (videoHeight * 1.0f)
        ratio = if (deviceRatio >= ratio20by9) {
            "20:9"
        } else if (deviceRatio >= ratio19by9) {
            "19:9"
        } else if (deviceRatio >= ratio18by9) {
            "18:9"
        } else if (deviceRatio >= ratio17by9) {
            "17:9"
        } else if (deviceRatio >= ratio16by9) {
            "16:9"
        } else {
            "3:2"
        }
        return ratio
    }

    @Throws(Throwable::class)
    @JvmStatic
    fun retrieveVideoFrameFromVideo(videoPath: String?, isFront: Boolean, millis: Long): Bitmap? {
        var bitmap: Bitmap
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(videoPath)
            bitmap = mediaMetadataRetriever.getFrameAtTime(
                millis * 1000,
                MediaMetadataRetriever.OPTION_CLOSEST
            )!!
            if (isFront) {
                bitmap = flipImage(bitmap)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            throw Throwable("Exception in retrieve VideoFrameFromVideo(String videoPath)" + e.message)
        } finally {
            mediaMetadataRetriever?.release()
        }
        return bitmap
    }

    @JvmStatic
    fun flipImage(src: Bitmap): Bitmap {
        // create new matrix for transformation
        val matrix = Matrix()
        matrix.preScale(-1.0f, 1.0f)
        // return transformed image
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    @JvmStatic
    fun showKeyboardForCustomEditText(mContext: Context, editText: CustomEditTextWithError?) {
        val inputMethodManager =
            mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    @JvmStatic
    fun getDBHelper(): QueryDataHelper? {
        return GenuinSDKApplication.dbHelper
    }

    @JvmStatic
    fun hideKeyboard(mContext: Activity) {
        try {
            if (mContext.currentFocus != null) {
                val imm =
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(mContext.currentFocus!!.windowToken, 0)
            }
        } catch (e: java.lang.Exception) {
           showLogException(e)
        }
    }


    fun convertChatIntoMessage(chat: ChatModel, chatId: String?): MessageModel {
        val message = MessageModel()
        message.chatId = chatId
        message.mediaUrl  = chat.videoUrl
        message.mediaUrl = chat.videoUrlM3U8
        message.thumbnailUrl = chat.thumbnailUrl
        message.videoThumbnailLarge = chat.videoThumbnailLarge
        message.videoThumbnailSmall = chat.videoThumbnailSmall
        message.localVideoPath = ""
        message.messageId = chat.conversationId
        message.messageAt  = chat.conversationAt
        message.link = chat.link
        message.isRead = chat.read
        message.isRetry = false
        message.videoUploadStatus = 3
        message.localImagePath = ""
        message.imageUploadStatus = 2
        message.dpUploadStatus =2
        message.convType = VideoConvType.ROUND_TABLE.value
        message.messageSummary = chat.description
        message.shareURL = chat.shareURL
        message.noOfViews = chat.noOfViews
        message.noOfComments =chat.noOfComments
        message.ffMpegCommand = chat.ffMpegCommand
        message.compressionStatus = 1
        if (chat.owner != null) {
            chat.owner.userName = chat.owner.nickname
        }
        message.owner = chat.owner
        message.questions = chat.questions
        var metaDataModel: MetaDataModel? = chat.metaData
        if (metaDataModel == null) {
            metaDataModel = MetaDataModel()
        }
        metaDataModel.duration = chat.duration
        metaDataModel.resolution = chat.resolution
        metaDataModel.size = chat.size
        metaDataModel.aspectRatio = chat.aspectRatio
        message.metaData =metaDataModel
        message.repostModel = chat.repostModel
        return message
    }

    fun convertLoopModelIntoConversation(
        context: Context?,
        loopsModel: LoopsModel
    ): ConversationModel {
        val conversationModel = ConversationModel()
        conversationModel.chatId = loopsModel.chatId
        conversationModel.convType = loopsModel.convType
        conversationModel.settings = loopsModel.settings
        conversationModel.noOfViews = loopsModel.noOfViews
        conversationModel.group = loopsModel.group
        if (loopsModel.latestMessages != null && loopsModel.latestMessages!!.size > 0) {
            val chats: MutableList<ChatModel> = java.util.ArrayList()
            for (k in 0 until loopsModel.latestMessages!!.size) {
                val messageModel: MessageModel = loopsModel.latestMessages!![k]
                chats.add(
                    convertMessageIntoChat(
                        messageModel,
                        loopsModel.chatId
                    )
                )
            }
            conversationModel.setChats(chats)
        }
        if (loopsModel.group != null) {
            //val userId: String = SharedPrefUtils.getStringPreference(context, Constants.PREF_USER)
            val userId: String = SDKInitiate.userId
            val memberList: List<MembersModel> = loopsModel.group!!.members
            if (memberList.isNotEmpty()) {
                for (i in memberList.indices) {
                    val membersModel: MembersModel = memberList[i]
                    if (membersModel.userId!!.contentEquals(userId)) {
                        val memberInfoModel = MemberInfoModel()
                        memberInfoModel.role = membersModel.memberRole
                        memberInfoModel.status = memberInfoModel.status
                        conversationModel.memberInfo = memberInfoModel
                        break
                    }
                }
            }
            val subscribersList: List<MembersModel> = loopsModel.group!!.getSubscribers()
            if (subscribersList.isNotEmpty()) {
                for (j in subscribersList.indices) {
                    val subscriber: MembersModel = subscribersList[j]
                    if (subscriber.userId!!.contentEquals(userId)) {
                        conversationModel.isSubscriber = true
                        break
                    }
                }
            }
        }
        return conversationModel
    }

    private fun convertMessageIntoChat(messageModel: MessageModel, chatId: String?): ChatModel {
        val chat = ChatModel()
        chat.chatId = chatId
        chat.videoUrl = messageModel.mediaUrl
        chat.videoUrlM3U8 = messageModel.mediaUrlM3U8
        chat.thumbnailUrl = messageModel.thumbnailUrl
        chat.videoThumbnailLarge = messageModel.videoThumbnailLarge
        chat.videoThumbnailSmall = messageModel.videoThumbnailSmall
        chat.localVideoPath = messageModel.localVideoPath
        chat.conversationId = messageModel.messageId
        chat.conversationAt = messageModel.messageAt
        chat.link = messageModel.link
        chat.read = messageModel.isRead
        chat.isRetry = messageModel.isRetry
        chat.videoUploadStatus = messageModel.videoUploadStatus
        chat.imagePath = messageModel.localImagePath
        chat.imageUploadStatus = messageModel.imageUploadStatus
        chat.dpUploadStatus = messageModel.dpUploadStatus
        chat.convType = messageModel.convType
        chat.description = messageModel.messageSummary
        chat.shareURL = messageModel.shareURL
        chat.noOfViews = messageModel.noOfViews
        chat.noOfComments = messageModel.noOfComments
        chat.ffMpegCommand = messageModel.ffMpegCommand
        chat.compressionStatus = messageModel.compressionStatus
        if (messageModel.owner != null) {
            messageModel.owner!!.nickname = messageModel.owner!!.getUserName()
        }
        chat.owner = messageModel.owner
        chat.questions = messageModel.questions
        val metaDataModel = chat.metaData
        chat.metaData = metaDataModel
        if (metaDataModel != null) {
            chat.duration = metaDataModel.duration
            chat.aspectRatio = metaDataModel.aspectRatio
            chat.resolution = metaDataModel.resolution
            chat.size = metaDataModel.size
        }
        chat.repostModel = messageModel.repostModel
        return chat
    }

    fun displayNotification(context: Context, from: String, path: String?, convType: Int) {
        var type: String? = null
        if (from.equals(
                Constants.FROM_PUBLIC_VIDEO,
                ignoreCase = true
            ) || from.equals(Constants.FROM_RECORD_FOR_OTHER, ignoreCase = true)
        ) {
            type = NotificationType.LOCAL_PROFILE_VIDEO.value
        } else if (from.equals(
                Constants.FROM_REACTION,
                ignoreCase = true
            ) || from.equals(
                Constants.FROM_DIRECT,
                ignoreCase = true
            ) || from.equals(Constants.FROM_GROUP, ignoreCase = true)
        ) {
            type = NotificationType.LOCAL_REPLY_SENT_VIDEO.value
        } else if (from.equals(Constants.FROM_CHAT, ignoreCase = true)) {
            type = if (convType == 4) {
                NotificationType.LOCAL_LOOP_VIDEO.value
            } else {
                NotificationType.LOCAL_INBOX_VIDEO.value
            }
        } else if (from.equals(Constants.FROM_ROUND_TABLE, ignoreCase = true)) {
            type = NotificationType.LOCAL_LOOP_VIDEO.value
        } else if (from.equals(Constants.FROM_COMMENT, ignoreCase = true)) {
            type = if (convType == 4) {
                NotificationType.LOCAL_LOOP_COMMENT_VIDEO.value
            } else {
                NotificationType.LOCAL_COMMENT_VIDEO.value
            }
        }
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val intent = Intent(context, HomeScreen::class.java)
        if (type != null) {
            if (type.equals(
                    NotificationType.LOCAL_COMMENT_VIDEO.getValue(),
                    ignoreCase = true
                ) || type.equals(
                    NotificationType.LOCAL_LOOP_COMMENT_VIDEO.getValue(),
                    ignoreCase = true
                )
            ) {
                if (Utility.getDBHelper() != null) {
                    val commentModel: CommentModel =
                        getDBHelper()!!.getCompressedCommentVideo(path)
                    val chatId: String = commentModel.getChatId()
                    val videoId: String = commentModel.getVideoId()
                    intent.putExtra("sourceId", if (TextUtils.isEmpty(chatId)) "" else chatId)
                    intent.putExtra("parentId", if (TextUtils.isEmpty(videoId)) "" else videoId)
                }
            }
            intent.putExtra("type", if (TextUtils.isEmpty(type)) "" else type)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val NOTIFICATION_CHANNEL_ID = "102"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @SuppressLint("WrongConstant") val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Notification",
                    NotificationManager.IMPORTANCE_MAX
                )
                //Configure Notification Channel
                notificationChannel.description =
                    context.resources.getString(R.string.upload_failed)
                notificationChannel.enableLights(true)
                notificationChannel.enableVibration(true)
                notificationManager.createNotificationChannel(notificationChannel)
            }
            val notificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setContentTitle("BeGenuin")
                    .setAutoCancel(true)
                    .setSound(defaultSound)
                    .setContentText(context.resources.getString(R.string.upload_failed))
                    .setContentIntent(pendingIntent)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(context.resources.getString(R.string.upload_failed))
                    )
                    .setWhen(System.currentTimeMillis())
                    .setPriority(Notification.PRIORITY_MAX)
            notificationManager.notify(1001, notificationBuilder.build())
        }
    }
}