package com.begenuin.library.data.remote.service

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.arthenica.ffmpegkit.*
import com.begenuin.library.common.Constants
import com.begenuin.library.common.Utility
import com.begenuin.library.core.enums.VideoConvType
import com.begenuin.library.data.eventbus.CompressionCompletedEvent
import com.begenuin.library.data.eventbus.CompressionCompletedPreviewEvent
import com.begenuin.library.data.eventbus.CompressionUpdateEvent
import com.begenuin.library.data.eventbus.PublicVideoStatusChangedEvent
import com.begenuin.library.data.viewmodel.GenuinFFMpegManager
import org.greenrobot.eventbus.EventBus

class CompressionWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private lateinit var ffmpegSession: FFmpegSession
    private var lastSessionId: Long = 0
    private lateinit var destinationPath: String
    private lateinit var from: String
    private lateinit var whichSession: String
    private lateinit var chatId: String
    private var convType: Int = 0
    private lateinit var videoName: String

    override fun doWork(): Result {
        val complexCommand = inputData.getString("command")
        destinationPath = inputData.getString("path").toString()
        Utility.showLog("Worker input destination path", destinationPath)
        from = inputData.getString("from").toString()
        whichSession = inputData.getString("whichSession").toString()
        convType = inputData.getInt("convType", 0)
        chatId = inputData.getString("chatId").toString()
        videoName = destinationPath.substring(destinationPath.lastIndexOf('/') + 1)
        return try {
            complexCommand?.let { execFFmpegBinary(it) }
            if (this::ffmpegSession.isInitialized) {
                lastSessionId = ffmpegSession.sessionId
            }
            val outputData = createOutputData(destinationPath, lastSessionId)
            Utility.showLog("TAG", "worker success")
            Result.success(outputData)
        } catch (throwable: Throwable) {
            Result.failure()
        }
    }

    private fun createOutputData(destinationPath: String, sessionId: Long): Data {
        Utility.showLog("Worker output destination path", destinationPath)
        return Data.Builder()
            .putString("path", destinationPath)
            .putLong("sessionId", sessionId)
            .build()
    }

    private fun execFFmpegBinary(command: String) = try {
        val startTime = System.currentTimeMillis()
        Utility.showLog("TAG", "Trimming Start: $startTime")
        ffmpegSession =
            FFmpegKit.executeAsync(command, FFmpegSessionCompleteCallback { session: Session ->
                when {
                    ReturnCode.isSuccess(session.returnCode) -> {
                        Utility.showLog(
                            "CompressionWorker",
                            "Async command execution completed successfully."
                        )
                        val totalTime = (System.currentTimeMillis() - startTime) / 1000f
                        Utility.showLog("TAG", "Time: $totalTime")

                        val compressCompleted =
                            CompressionCompletedPreviewEvent()
                        compressCompleted.isCompleted = true
                        compressCompleted.path = destinationPath
                        compressCompleted.whichSession = whichSession
                        EventBus.getDefault().post(compressCompleted)
                        if (whichSession == Constants.SESSION_MERGE) {
                            if (Utility.getDBHelper() != null) {
                                when (from) {
                                    Constants.FROM_PUBLIC_VIDEO, Constants.FROM_RECORD_FOR_OTHER -> {
                                        if (Utility.getDBHelper()!!.checkForPublicVideo(
                                                destinationPath
                                            )
                                        ) {
                                            Utility.getDBHelper()!!.updatePublicCompressionStatus(
                                                destinationPath,
                                                1
                                            )
                                        }
                                        sendBroadCastForCompressionDonePublicVideo(destinationPath)
                                    }
                                    Constants.FROM_COMMENT -> {
                                        Utility.getDBHelper()!!.updateCompressionStatus(
                                            destinationPath
                                        )
                                    }
                                    Constants.FROM_ROUND_TABLE -> {
                                        Utility.getDBHelper()!!.updateLoopVideoCompressionStatus(
                                            destinationPath
                                        )
                                    }
                                    Constants.FROM_CHAT -> {
                                        if (convType == VideoConvType.ROUND_TABLE.value) {
                                            Utility.getDBHelper()!!.updateLoopVideoCompressionStatus(
                                                destinationPath
                                            )
                                        } else {
                                            Utility.getDBHelper()!!.updateChatCompressionStatus(
                                                destinationPath
                                            )
                                        }
                                        sendBroadCastForChat()
                                    }
                                }
                            }
                           // var compressMode = "preview"
                            if (GenuinFFMpegManager.getInstance().isNeedToUpload(destinationPath)) {
                                //compressMode = "background"
                                val compressionCompleted1 =
                                    CompressionCompletedEvent()
                                compressionCompleted1.from = from
                                compressionCompleted1.path = destinationPath
                                compressionCompleted1.convType = convType
                                EventBus.getDefault().post(compressionCompleted1)
                            }

//                            val type = if (command.contains("libx264")) "h264" else "h265"
//                            val map: HashMap<String?, Any?> =
//                                object : HashMap<String?, Any?>() {
//                                    init {
//                                        put("latency", totalTime * 1000)
//                                        put("conv_type", getDataDogFrom())
//                                        put("compress_mode", compressMode)
//                                        put("video_name", videoName)
//                                        put("chat_id", chatId)
//                                        put("type", type)
//                                    }
//                                }
//                            GenuInApplication.getInstance().sendEventLogs(
//                                Constants.VIDEO_COMPRESS, map
//                            )
                        }
                    }
                    ReturnCode.isCancel(session.returnCode) -> {
                        val buffer = session.output
                        FFmpegKitConfig.printToLogcat(Log.INFO, buffer)
                    }
                    else -> {
                        val totalTime = (System.currentTimeMillis() - startTime) / 1000f
                        val buffer = session.output
                        FFmpegKitConfig.printToLogcat(Log.INFO, buffer)
                        val compressionCompletedPreview =
                            CompressionCompletedPreviewEvent()
                        compressionCompletedPreview.isCompleted = false
                        EventBus.getDefault().post(compressionCompletedPreview)

                        if (whichSession == Constants.SESSION_MERGE) {
                            when (from) {
                                Constants.FROM_PUBLIC_VIDEO, Constants.FROM_RECORD_FOR_OTHER -> {
                                    if (Utility.getDBHelper() != null) {
                                        Utility.getDBHelper()!!.updateProfileRetryStatus(
                                            destinationPath,
                                            true
                                        )
                                    }
                                    sendBroadCastForRetryPublicVideo(destinationPath)
                                }
                                Constants.FROM_COMMENT -> {
                                    if (Utility.getDBHelper() != null) {
                                        Utility.getDBHelper()!!.updateCommentRetryStatus(
                                            destinationPath,
                                            true
                                        )
                                    }
                                    //sendBroadCastForComment(destinationPath)
                                }
                                Constants.FROM_ROUND_TABLE -> {
                                    if (Utility.getDBHelper() != null) {
                                        Utility.getDBHelper()!!.updateLoopVideoRetryStatus(
                                            destinationPath,
                                            true
                                        )
                                    }
                                    sendBroadCastForChat()
                                }
                                else -> {
                                    if (Utility.getDBHelper() != null) {
                                        Utility.getDBHelper()!!.updateChatRetryStatus(
                                            destinationPath,
                                            true
                                        )
                                    }
                                    sendBroadCastForChat()
                                }
                            }
//                            val compressMode = if (GenuinFFMpegManager.getInstance()
//                                    .isNeedToUpload(destinationPath)
//                            ) "background" else "preview"
//                            val type = if (command.contains("libx264")) "h264" else "h265"
//                            val map: HashMap<String?, Any?> =
//                                object : HashMap<String?, Any?>() {
//                                    init {
//                                        put("latency", totalTime * 1000)
//                                        put("conv_type", getDataDogFrom())
//                                        put("compress_mode", compressMode)
//                                        put("video_name", videoName)
//                                        put("chat_id", chatId)
//                                        put("type", type)
//                                        put("reason", buffer)
//                                    }
//                                }
//                            GenuInApplication.getInstance().sendEventLogs(
//                                Constants.VIDEO_COMPRESS_FAILED, map
//                            )
                        }
                    }
                }
            })
        when (whichSession) {
            Constants.SESSION_MERGE -> {
                GenuinFFMpegManager.getInstance().lastMergeSession = ffmpegSession
            }
            else -> {}
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    private fun sendBroadCastForRetryPublicVideo(
        path: String,
    ) {
        val publicVideo =
            PublicVideoStatusChangedEvent()
        publicVideo.videoLocalPath = path
        publicVideo.isRetry = true
        publicVideo.videoUploadStatus = 0
        publicVideo.imageUploadStatus = 0
        publicVideo.apiUploadStatus = 0
        publicVideo.compressionStatus = 0
        EventBus.getDefault().post(publicVideo)
    }

    private fun sendBroadCastForCompressionDonePublicVideo(
        path: String,
    ) {
        val publicVideo =
            PublicVideoStatusChangedEvent()
        publicVideo.videoLocalPath = path
        publicVideo.isRetry = false
        publicVideo.videoUploadStatus = 0
        publicVideo.imageUploadStatus = 0
        publicVideo.apiUploadStatus = 0
        publicVideo.compressionStatus = 1
        EventBus.getDefault().post(publicVideo)
    }

    private fun sendBroadCastForChat() {
        EventBus.getDefault().post(CompressionUpdateEvent())
    }

//    private fun sendBroadCastForComment(path: String) {
//        val comment = PostCommentEvent()
//        comment.isRetry = true
//        comment.localFilePath = path
//        EventBus.getDefault().post(comment)
//    }

    private fun getDataDogFrom(): String {
        var fromStr = ""
        if (from.equals(
                Constants.FROM_PUBLIC_VIDEO,
                ignoreCase = true
            ) || from.equals(Constants.FROM_RECORD_FOR_OTHER, ignoreCase = true)
        ) {
            fromStr = Constants.CATEGORY_PUBLIC_VIDEO
        }
//        else if (from.equals(Constants.FROM_REACTION, ignoreCase = true)) {
//            fromStr = Constants.CATEGORY_REACTION
//        } else if (from.equals(Constants.FROM_DIRECT, ignoreCase = true)) {
//            fromStr = Constants.CATEGORY_DM
//        } else if (from.equals(Constants.FROM_GROUP, ignoreCase = true)) {
//            fromStr = Constants.CATEGORY_GROUP
//        }
        else if (from.equals(Constants.FROM_ROUND_TABLE, ignoreCase = true)) {
            fromStr = Constants.CATEGORY_RT
        }
//        } else if (from.equals(Constants.FROM_COMMENT, ignoreCase = true)) {
//            fromStr = Constants.CATEGORY_COMMENT
//        } else if (from.equals(Constants.FROM_CHAT, ignoreCase = true)) {
//            when (convType) {
//                1 -> {
//                    fromStr = Constants.CATEGORY_REACTION
//                }
//                2 -> {
//                    fromStr = Constants.CATEGORY_DM
//                }
//                3 -> {
//                    fromStr = Constants.CATEGORY_GROUP
//                }
//                4 -> {
//                    fromStr = Constants.CATEGORY_RT
//                }
//            }
//        }
        return fromStr
    }

    override fun onStopped() {
        if (this::ffmpegSession.isInitialized) {
            FFmpegKit.cancel(ffmpegSession.sessionId)
            Utility.showLog("TAG", "cancelled ${ffmpegSession.sessionId}")
        }
        super.onStopped()
    }
}