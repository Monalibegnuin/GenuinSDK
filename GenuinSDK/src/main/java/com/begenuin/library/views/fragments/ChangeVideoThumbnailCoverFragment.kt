package com.begenuin.library.views.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.begenuin.library.common.Utility
import com.begenuin.library.data.model.PlayerHelperModel
import com.begenuin.library.data.model.VideoModel
import com.begenuin.library.data.remote.BaseAPIService
import com.begenuin.library.databinding.FragmentChangeThubmnilCoverBinding
import com.begenuin.library.views.activities.CameraNewActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.MediaItem.ClippingConfiguration
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.ceil
import com.begenuin.library.R


class ChangeVideoThumbnailCoverFragment : Fragment(), View.OnClickListener,
    SeekBar.OnSeekBarChangeListener {

    private lateinit var context: CameraNewActivity

    private val startTrimMillis: Int = 0
    private var playEndPos: Int = -1
    private var playStartPos: Int = -1
    private var isSeeking: Boolean = false
    private var THUMB_WIDTH: Int = -1
    private var selectedPos: Int = -1
    private var THUMB_HEIGHT: Int = 0
    private lateinit var player: ExoPlayer

    /*  private var previewWidth = 0
      private var previewHeight: Int = 0*/
    private var mLastClickTime: Long = 0
    private var frameVideoPos = 0
    private var frameCount = 0
    private val TOTAL_THUMB_COUNT = 15
    private var TOTAL_DURATION: Long = 0
    private val isShowSection = false
    private lateinit var backgroundExecutor: ExecutorService
    private var futures: ArrayList<Future<*>> = ArrayList()
    private val DEFAULT_THUMB_PADDING_PX = 30f
    private val DEFAULT_THUMB_HEIGHT_PX = 48f
    private val ROUNDED_CORNER_IMAGE_RADIUS = 2
    private val ROUNDED_CORNER_BORDER_IMAGE = 2
    private val ROUNDED_CORNER_BORDER_COLOR = Color.WHITE
    private var customProgress: Int = 0
    private var isFromIntentData = false

    private val playerHelper = ArrayList<PlayerHelperModel>()
    private var seekArray = IntArray(2)
    private var originalBmp: Bitmap? = null

    private var isDoneClicked: Boolean = false
    private var isCancelClicked: Boolean = false
    private var mediaPath: String = ""
    private val mediaMetadataRetriever = MediaMetadataRetriever()
    lateinit var thumbnailBinding: FragmentChangeThubmnilCoverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as CameraNewActivity
        THUMB_HEIGHT = Utility.dpToPx(DEFAULT_THUMB_HEIGHT_PX, context).toInt()
        backgroundExecutor = Executors.newCachedThreadPool()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        thumbnailBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.fragment_change_thubmnil_cover, null, false)
        return thumbnailBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntent()
        setClickListener()
        if (context.videoList.size > 0) {
            Handler(Looper.getMainLooper()).postDelayed({
                /* previewHeight = llVideoView.height
                 previewWidth = previewHeight * 9 / 16
                 val params: LinearLayout.LayoutParams =
                     LinearLayout.LayoutParams(previewWidth, previewHeight)
                 videoView.layoutParams = params*/
                refreshThumbnailView()
            }, 200)
        }
    }

    private fun setClickListener() {
        thumbnailBinding.llThumbnilCancel.setOnClickListener(this)
        thumbnailBinding.llThumbnilDone.setOnClickListener(this)
    }

    private fun getDataFromIntent() {
        if ((context.progressArray == null || context.progressArray.isEmpty()) && context.videoProgress == 0)
            return

        isFromIntentData = true
        customProgress = context.videoProgress
        seekArray = context.progressArray
    }

    private fun refreshThumbnailView() {
        frameVideoPos = 0
        frameCount = 0
        setVideoThumbVisibility(0)
        calculationForFrames()
        //setVideoFrames()
        updateVideoProgress()
    }

    private fun calculationForFrames() {
        TOTAL_DURATION = 0
        var tempDuration: Long = 0
        var isStartSelected = false
        var isEndSelected = false
        var startPositionMillis: Long = 0
        var endPositionMillis: Long = 0
        var startPos = -1
        for (i in context.videoList.indices) {
            val video = context.videoList[i]
            val isTrimmed: Boolean = isTrimmed(video)
            TOTAL_DURATION += if (isTrimmed) {
                (context.videoList[i].trimDuration * 1000).toLong()
            } else {
                (context.videoList[i].actualDuration * 1000).toLong()
            }
            if (video.isFullTrim) {
                if (!isStartSelected) {
                    isStartSelected = true
                    startPos = i
                    selectedPos = i
                    if (i != 0) {
                        context.videoList[0].isSelected = false
                        context.videoList[i].isSelected = true
                    }
                    startPositionMillis = if (isTrimmed) {
                        video.fullTrimStartMillis + TOTAL_DURATION - (context.videoList[i].trimDuration * 1000).toLong()
                    } else {
                        video.fullTrimStartMillis + TOTAL_DURATION - (context.videoList[i].actualDuration * 1000).toLong()
                    }
                    tempDuration = TOTAL_DURATION
                } else if (isStartSelected && !isEndSelected) {
                    isEndSelected = true
                    endPositionMillis = if (isTrimmed) {
                        video.fullTrimEndMillis + TOTAL_DURATION - (context.videoList[i].trimDuration * 1000).toLong()
                    } else {
                        video.fullTrimEndMillis + TOTAL_DURATION - (context.videoList[i].actualDuration * 1000).toLong()
                    }
                }
            }
        }
        if (startPos != -1 && !isEndSelected) {
            val video = context.videoList[startPos]
            endPositionMillis = if (isTrimmed(video)) {
                video.fullTrimEndMillis + tempDuration - (context.videoList[startPos].trimDuration * 1000).toLong()
            } else {
                video.fullTrimEndMillis + tempDuration - (context.videoList[startPos].actualDuration * 1000).toLong()
            }
        }
        for (i in context.videoList.indices) {
            val video = context.videoList[i]
            var countValue: Double = if (isTrimmed(video)) {
                TOTAL_THUMB_COUNT * context.videoList[i].trimDuration * 1000 / TOTAL_DURATION
            } else {
                TOTAL_THUMB_COUNT * context.videoList[i].actualDuration * 1000 / TOTAL_DURATION
            }
            countValue = String.format(Locale.ENGLISH, "%.2f", countValue).toDouble()
            context.videoList[i].thumbCount = ceil(countValue).toInt()
            context.videoList[i].thumbFloatCount = countValue
        }
//        THUMB_WIDTH = ((DeviceUtil.getDeviceWidth() - Utility.dpToPx(
//            DEFAULT_THUMB_PADDING_PX,
//            context
//        )) / TOTAL_THUMB_COUNT).toInt()
        THUMB_WIDTH = 100
        val diff = if (TOTAL_DURATION > 40000) {
            500
        } else {
            300
        }
        thumbnailBinding.videoChangeThumbnilView.mLeftProgressPos = 0
        thumbnailBinding.videoChangeThumbnilView.mRightProgressPos = TOTAL_DURATION
        thumbnailBinding.videoChangeThumbnilView.initRangeSeekbar(context)
        setVideoThumbVisibility(0)
        thumbnailBinding.videoChangeThumbnilView.videoMultiChangeCoverViewBinding.videoSeekBar?.max = (TOTAL_DURATION - diff).toInt()
        thumbnailBinding.videoChangeThumbnilView.videoMultiChangeCoverViewBinding.videoSeekBar?.setOnSeekBarChangeListener(this)
        if (context.isFullTrim) {
            thumbnailBinding.videoChangeThumbnilView.mLeftProgressPos = startPositionMillis
            thumbnailBinding.videoChangeThumbnilView.mRightProgressPos = endPositionMillis
            thumbnailBinding.videoChangeThumbnilView.mRangeSeekBarView?.setStartEndTime(
                startPositionMillis,
                endPositionMillis
            )
            thumbnailBinding.videoChangeThumbnilView.mRedProgressBarPos = startPositionMillis
            thumbnailBinding.videoChangeThumbnilView.mRangeSeekBarView?.selectedMinValue = startPositionMillis
            thumbnailBinding.videoChangeThumbnilView.mRangeSeekBarView?.selectedMaxValue = endPositionMillis
            thumbnailBinding.videoChangeThumbnilView.mRangeSeekBarView?.invalidate()
        }
    }

//    private fun generateBitmapFromProgress(
//        time: Long,
//        pair: Pair<String, Boolean>?,
//        callback: SingleCallback<Bitmap?, Long?>,
//    ) {
//        try {
//            val task = GenerateCoverImageTask(time, pair, callback)
//
//            futures.add(backgroundExecutor.submit(task))
//            originalBmp = null
//        } catch (ex: Exception) {
//            ex.printStackTrace()
//        }
//    }
//
//    private fun setVideoFrames() {
//        val video = context.videoList[frameVideoPos]
//        videoThumbInBackground(video) { bitmap: Bitmap?, _: Int? ->
//            if (bitmap != null) {
//                if (!isShowSection) {
//                    UiThreadExecutor.runTask("1", {
//                        if (!isAdded) {
//                            return@runTask
//                        }
//                        videoChangeThumbnilView.mVideoThumbAdapter?.addBitmaps(bitmap)
//                        frameCount++
//                        if (frameCount == video.thumbCount) {
//                            frameCount = 0
//                            frameVideoPos++
//                            if (frameVideoPos < context.videoList.size) {
//                                setVideoFrames()
//                            } else {
//                                Utility.printErrorLog("inside else frameVideoPos: $frameVideoPos , count:$frameCount")
//                                if (frameCount == 0 && isFromIntentData) {
//                                    setVideoThumbVisibility(255)
//                                    onProgressChanged(
//                                        videoChangeThumbnilView.videoSeekBar,
//                                        customProgress,
//                                        true
//                                    )
//                                } else if (frameCount == 0) {
//                                    setVideoThumbVisibility(255)
//                                    Utility.printErrorLog("inside elseif case frameVideoPos: $frameVideoPos and count: $frameCount and now setting up thumb and playing video")
//                                    videoChangeThumbnilView.videoSeekBar.thumb =
//                                        generateThumbFromBitmap(bitmap)
//                                }
//                            }
//                        } else {
//                            Utility.printErrorLog("outer else frameVideoPos: $frameVideoPos , count:$frameCount")
//                        }
//                    }, 0L)
//                }
//            }
//        }
//    }

    private fun setVideoThumbVisibility(alpha: Int) {
        thumbnailBinding.videoChangeThumbnilView.videoMultiChangeCoverViewBinding.videoSeekBar.thumb.mutate().alpha = alpha
    }

//    private fun videoThumbInBackground(
//        video: VideoModel,
//        callback: SingleCallback<Bitmap?, Int?>,
//    ) {
//        BackgroundExecutor.execute(object :
//            BackgroundExecutor.Task("1", 0L, "M_" + video.videoFileList[0].filePath) {
//            override fun execute() {
//                try {
//                    val isRetake = video.videoRetakeFileList.size > 0
//                    if (isRetake) {
//                        mediaMetadataRetriever.setDataSource(
//                            context,
//                            Uri.parse(video.videoRetakeFileList[0].filePath)
//                        )
//                    } else {
//                        mediaMetadataRetriever.setDataSource(
//                            context,
//                            Uri.parse(video.videoFileList[0].filePath)
//                        )
//                    }
//                    // Retrieve media data use microsecond
//                    var count = 0
//                    var minusDuration: Long = 0
//                    val isTrimmed = isTrimmed(video)
//                    val interval: Long
//                    if (isTrimmed) {
//                        interval = (video.trimDuration * 1000 / (video.thumbCount - 1)).toLong()
//                        if (isRetake) {
//                            for (j in video.videoRetakeFileList.indices) {
//                                val videoFile = video.videoRetakeFileList[j]
//                                if (video.trimStartMillis > videoFile.trimEndMillis + minusDuration) {
//                                    count++
//                                    minusDuration += video.videoRetakeFileList[count - 1].trimEndMillis
//                                }
//                            }
//                            mediaMetadataRetriever.setDataSource(
//                                context,
//                                Uri.parse(video.videoRetakeFileList[count].filePath)
//                            )
//                        } else {
//                            for (j in video.videoFileList.indices) {
//                                val videoFile = video.videoFileList[j]
//                                if (video.trimStartMillis > videoFile.trimEndMillis + minusDuration) {
//                                    count++
//                                    minusDuration += video.videoFileList[count - 1].trimEndMillis
//                                }
//                            }
//                            mediaMetadataRetriever.setDataSource(
//                                context,
//                                Uri.parse(video.videoFileList[count].filePath)
//                            )
//                        }
//                    } else {
//                        interval =
//                            (video.actualDurationWithoutSpeed * 1000 / (video.thumbCount - 1)).toLong()
//                    }
//                    for (i in 0 until video.thumbCount) {
//                        /* if (!multiTaskFilePath.equals(video.videoFileList[0].filePath, ignoreCase = true)) {
//                             break
//                         }*/
//                        var frameTime: Long
//                        frameTime = if (isTrimmed) {
//                            interval * i + video.trimStartMillis - minusDuration
//                        } else {
//                            interval * i - minusDuration
//                        }
//                        if (isRetake) {
//                            if (count < video.videoRetakeFileList.size && frameTime > video.videoRetakeFileList[count].trimEndMillis) {
//                                count++
//                                if (video.videoRetakeFileList.size > count) {
//                                    mediaMetadataRetriever.setDataSource(
//                                        context, Uri.parse(
//                                            video.videoRetakeFileList[count].filePath
//                                        )
//                                    )
//                                    minusDuration += video.videoRetakeFileList[count - 1].trimEndMillis
//                                    frameTime = 0
//                                }
//                            }
//                        } else {
//                            if (count < video.videoFileList.size && frameTime > video.videoFileList[count].trimEndMillis) {
//                                count++
//                                if (video.videoFileList.size > count) {
//                                    mediaMetadataRetriever.setDataSource(
//                                        context, Uri.parse(
//                                            video.videoFileList[count].filePath
//                                        )
//                                    )
//                                    minusDuration += video.videoFileList[count - 1].trimEndMillis
//                                    frameTime = 0
//                                }
//                            }
//                        }
//                        Utility.showLog("Tag", frameTime.toString() + "")
//                        var bitmap =
//                            createBitmapFromMetaData(mediaMetadataRetriever, (frameTime * 1000))
//                                ?: continue
//                        try {
//                            if (i == (video.thumbCount - 1)) {
//                                val width = video.thumbFloatCount + 1 - video.thumbCount
//                                Utility.printErrorLog("width: $width")
//                                var bitmapWidth = (THUMB_WIDTH * width).toInt()
//                                Utility.printErrorLog("bitmapWidth: $bitmapWidth")
//                                if (bitmapWidth < 1) {
//                                    bitmapWidth = 1
//                                }
//                                bitmap = Bitmap.createScaledBitmap(
//                                    bitmap,
//                                    bitmapWidth,
//                                    THUMB_HEIGHT,
//                                    false
//                                )
//                            } else {
//                                Utility.printErrorLog("bitmapWidth: creating a scaled bitmap")
//                                bitmap = Bitmap.createScaledBitmap(
//                                    bitmap,
//                                    THUMB_WIDTH,
//                                    THUMB_HEIGHT,
//                                    false
//                                )
//                            }
//                            var isFront = false
//                            if (isRetake && count < video.videoRetakeFileList.size) {
//                                isFront = video.videoRetakeFileList[count].isFront
//                            } else if (count < video.videoFileList.size) {
//                                isFront = video.videoFileList[count].isFront
//                            }
//                            if (isFront) {
//                                bitmap = Utility.createFlippedBitmap(bitmap, true, false)
//                            }
//                        } catch (t: Throwable) {
//                            t.printStackTrace()
//                        }
//                        callback.onSingleCallback(bitmap, interval.toInt())
//                    }
//                } catch (e: Throwable) {
//                    e.printStackTrace()
//                }
//            }
//        })
//    }

    private fun updateVideoProgress() {
        var duration: Long = 0
        playStartPos = -1
        playEndPos = -1
        for (i in context.videoList.indices) {
            val video = context.videoList[i]
            val isTrimmed = isTrimmed(video)
            duration += if (isTrimmed) {
                (context.videoList[i].trimDuration * 1000).toLong()
            } else {
                (context.videoList[i].actualDuration * 1000).toLong()
            }
            if (thumbnailBinding.videoChangeThumbnilView.mLeftProgressPos <= duration && playStartPos == -1) {
                selectedPos = i
                playStartPos = i
                if (isTrimmed) {
                    video.fullTrimStartMillis =
                        thumbnailBinding.videoChangeThumbnilView.mLeftProgressPos + (context.videoList[i].trimDuration * 1000).toLong() - duration
                    video.fullTrimEndMillis = (context.videoList[i].trimDuration * 1000).toLong()
                } else {
                    video.fullTrimStartMillis =
                        thumbnailBinding.videoChangeThumbnilView.mLeftProgressPos + (context.videoList[i].actualDuration * 1000).toLong() - duration
                    video.fullTrimEndMillis = (context.videoList[i].actualDuration * 1000).toLong()
                }
            }
            if (playStartPos >= 0 && thumbnailBinding.videoChangeThumbnilView.mRightProgressPos <= duration) {
                playEndPos = i
                if (playStartPos != playEndPos) {
                    video.fullTrimStartMillis = 0L
                }
                if (isTrimmed) {
                    video.fullTrimEndMillis =
                        thumbnailBinding.videoChangeThumbnilView.mRightProgressPos + (context.videoList[i].trimDuration * 1000).toLong() - duration
                } else {
                    video.fullTrimEndMillis =
                        thumbnailBinding.videoChangeThumbnilView.mRightProgressPos + (context.videoList[i].actualDuration * 1000).toLong() - duration
                }
                break
            }
            if (playStartPos != i) {
                video.fullTrimStartMillis = 0L
                if (isTrimmed) {
                    video.fullTrimEndMillis = (context.videoList[i].trimDuration * 1000).toLong()
                } else {
                    video.fullTrimEndMillis = (context.videoList[i].actualDuration * 1000).toLong()
                }
            }
        }
        setVideoPlay()
    }

    private fun setVideoPlay() {
        stopPlaying()
        val mediaItems = ArrayList<MediaItem>()
        playerHelper.clear()
        for (i in context.videoList.indices) {
            val video = context.videoList[i]
            val isTrimmed = isTrimmed(video)
            var isFirst = true
            var lastEndMillis: Long = 0
            val isRetake = video.videoRetakeFileList.size > 0
            val size = if (isRetake) video.videoRetakeFileList.size else video.videoFileList.size
            for (j in 0 until size) {
                val videoFile =
                    if (isRetake) video.videoRetakeFileList[j] else video.videoFileList[j]
                var startPositionMs: Long
                var endPositionMs: Long
                if (isTrimmed) {
                    if (lastEndMillis > video.trimEndMillis) {
                        break
                    }
                    lastEndMillis += videoFile.trimEndMillis
                    if (video.trimStartMillis > lastEndMillis) {
                        continue
                    }
                    if (isFirst) {
                        isFirst = false
                        startPositionMs =
                            video.trimStartMillis - (lastEndMillis - videoFile.trimEndMillis)
                    } else {
                        startPositionMs = videoFile.trimStartMillis
                    }
                    endPositionMs = if (lastEndMillis > video.trimEndMillis) {
                        videoFile.trimEndMillis - (lastEndMillis - video.trimEndMillis)
                    } else {
                        videoFile.trimEndMillis
                    }
                    val clippingConfiguration = ClippingConfiguration.Builder()
                        .setStartPositionMs(startPositionMs)
                        .setEndPositionMs(endPositionMs)
                        .setStartsAtKeyFrame(true)
                        .build()
                    val builder = MediaItem.Builder()
                        .setUri(videoFile.filePath)
                        .setClippingConfiguration(clippingConfiguration)
                        //.setMediaId(videoFile.isFront.toString() + )
                        .build()
                    mediaItems.add(builder)
                    val playerHelperModel = PlayerHelperModel()
                    playerHelperModel.isFront = videoFile.isFront
                    playerHelperModel.selectedPos = i
                    playerHelper.add(playerHelperModel)
                } else {
                    startPositionMs = videoFile.trimStartMillis
                    endPositionMs = videoFile.trimEndMillis
                    val clippingConfiguration = ClippingConfiguration.Builder()
                        .setStartPositionMs(startPositionMs)
                        .setEndPositionMs(endPositionMs)
                        .setStartsAtKeyFrame(true)
                        .build()
                    val builder = MediaItem.Builder()
                        .setUri(videoFile.filePath)
                        .setClippingConfiguration(clippingConfiguration)
                        .build()
                    mediaItems.add(builder)
                    val playerHelperModel = PlayerHelperModel()
                    playerHelperModel.isFront = videoFile.isFront
                    playerHelperModel.selectedPos = i
                    playerHelper.add(playerHelperModel)
                }
            }
        }
        player = ExoPlayer.Builder(context).build()
        player.addMediaItems(mediaItems)
        player.prepare()
        player.addListener(playListener)
        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        player.repeatMode = Player.REPEAT_MODE_ALL
        thumbnailBinding.videoView.player = player
        selectedPos = if (playStartPos == -1) {
            0
        } else {
            playStartPos
        }
        setVideoAdjustment()
        player.playWhenReady = false
        val progress: Int = if (customProgress == 0) getProgress() else customProgress
        thumbnailBinding.videoChangeThumbnilView.videoMultiChangeCoverViewBinding.videoSeekBar?.progress = progress
        //allTimerCounter()
    }


    private fun stopPlaying() {
        if (this::player.isInitialized) {
            player.removeListener(playListener)
            player.playWhenReady = false
            player.stop()
            player.clearMediaItems()
        }
    }

    private fun getProgress(): Int {
        val totalProgress: Int
        var cumulativePos = 0
        for (i in 0 until (player.currentPeriodIndex)) {
            cumulativePos += ((player.getMediaItemAt(i).clippingConfiguration.endPositionMs) - (player.getMediaItemAt(
                i
            ).clippingConfiguration.startPositionMs)).toInt()
        }
        totalProgress = if (isShowSection) {
            (startTrimMillis + cumulativePos + (player.currentPosition)).toInt()
        } else {
            (cumulativePos + (player.currentPosition)).toInt()
        }
        return totalProgress
    }

    override fun onDetach() {
        stopPlaying()
        if (this::player.isInitialized) {
            player.release()
        }
        super.onDetach()
    }

    private val playListener: Player.Listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            getRelativeFrontBack()
        }

        override fun onRenderedFirstFrame() {
            if (getRelativeFrontBack()) {
                thumbnailBinding.videoView.videoSurfaceView?.scaleX = -1f
            } else {
                thumbnailBinding.videoView.videoSurfaceView?.scaleX = 1f
            }
            // onRenderedFirstFrame is called before onMediaItemTransition sometimes so we need to have below code.
            Handler(Looper.getMainLooper()).postDelayed({
                if (getRelativeFrontBack()) {
                    thumbnailBinding.videoView.videoSurfaceView?.scaleX = -1f
                } else {
                    thumbnailBinding.videoView.videoSurfaceView?.scaleX = 1f
                }
            }, 100)
        }
    }

    private fun getRelativeFrontBack(): Boolean {
        val currentPlayPos = player.currentPeriodIndex
        selectedPos = playerHelper[currentPlayPos].selectedPos
        return playerHelper[currentPlayPos].isFront
    }

    private fun getRelativeFrontBack(currentPlayPos: Int): Boolean {
        selectedPos = playerHelper[currentPlayPos].selectedPos
        return playerHelper[currentPlayPos].isFront
    }

    private fun isTrimmed(video: VideoModel): Boolean {
        return video.trimDuration > 0
    }

    private fun getActualSeekProgress(progress: Int): IntArray {
        var actualDuration = 0
        var mediaItemIndex = 0
        var seekProgress = 0
        var leftHandlePos = -1
        val leftProgressPos: Long = thumbnailBinding.videoChangeThumbnilView.mLeftProgressPos
        for (i in 0 until (player.mediaItemCount)) {
            /*
           player.mediaItemCount = total 2 videos got 1. front and 2. back with start and stop btn,
             if we shoot with only 1 cam then count will be 1
             */
            seekProgress = progress - actualDuration
            actualDuration += ((player.getMediaItemAt(i).clippingConfiguration.endPositionMs) - (player.getMediaItemAt(
                i
            ).clippingConfiguration.startPositionMs)).toInt()
            if (leftHandlePos == -1 && leftProgressPos < actualDuration) {
                leftHandlePos = i
            }
            if (progress <= actualDuration) {
                mediaItemIndex = i
                if (leftHandlePos == mediaItemIndex) {
                    seekProgress = progress - leftProgressPos.toInt()
                }
                if (leftHandlePos != -1) {
                    mediaItemIndex -= leftHandlePos
                }
                break
            }
        }
        Utility.showLog("Tag", "$mediaItemIndex : $seekProgress")
        return intArrayOf(mediaItemIndex, seekProgress)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
        Utility.printErrorLog("onProgressChanged: $progress")
        if (b) {

            Utility.showLog("Tag", progress.toString() + "")
            if (progress < thumbnailBinding.videoChangeThumbnilView.mLeftProgressPos) {
                thumbnailBinding.videoChangeThumbnilView.videoMultiChangeCoverViewBinding.videoSeekBar.progress =
                    thumbnailBinding.videoChangeThumbnilView.mLeftProgressPos.toInt()
            } else if (progress > thumbnailBinding.videoChangeThumbnilView.mRightProgressPos) {
                thumbnailBinding.videoChangeThumbnilView.videoMultiChangeCoverViewBinding.videoSeekBar.progress =
                    thumbnailBinding. videoChangeThumbnilView.mRightProgressPos.toInt()
            }
            if (SystemClock.elapsedRealtime() - mLastClickTime < 50) {
                return
            }
            mLastClickTime = SystemClock.elapsedRealtime()

            //this.customProgress = seekBar.progress
            isFromIntentData = false
            seekArray = getActualSeekProgress(seekBar.progress)
            this.customProgress = seekBar.progress
            player.playWhenReady = false

            Utility.printErrorLog("start : " + seekArray[0] + " end: " + seekArray[1])

            player.seekTo(seekArray[0], seekArray[1].toLong())

            //find path from media item refer exoplayer
            val mediaItem = player.getMediaItemAt(seekArray[0])
            val startPos = mediaItem.clippingConfiguration.startPositionMs

            Utility.printErrorLog("startPos: $startPos")
            Utility.printErrorLog("startPos finding path localConfiguration available: ${mediaItem.localConfiguration == null}")
            Utility.printErrorLog("startPos Path : ${mediaItem.localConfiguration?.uri}")
            mediaPath = mediaItem.localConfiguration?.uri.toString()
            val currentFileData = Pair(mediaPath, getRelativeFrontBack(seekArray[0]))

            //OLD:
            // val currentFileData = getVideoFileIndex(seekArray[0])

//            generateBitmapFromProgress(
//                seekArray[1].toLong() + startPos,
//                currentFileData
//            ) { bitmap, time ->
//                if (bitmap != null) {
//                    context.runOnUiThread {
//                        if (time == seekArray[1].toLong()) {
//                            originalBmp = bitmap
//                            if (isDoneClicked) {
//                                mediaMetadataRetriever.release()
//                                context.onChangeCoverDoneBtnClick(
//                                    originalBmp,
//                                    seekArray,
//                                    customProgress,
//                                    mediaPath,
//                                    getRelativeFrontBack(seekArray[0])
//                                )
//                            } else if (isCancelClicked) {
//                                backManage()
//                            }
//                        }
//                        seekBar.thumb = generateThumbFromBitmap(bitmap)
//                        isFromIntentData = false
//                    }
//                }
//            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        isSeeking = true
        player.setSeekParameters(SeekParameters.NEXT_SYNC)
        player.playWhenReady = false
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        isSeeking = false
        player.setSeekParameters(SeekParameters.EXACT)
        if (thumbnailBinding.videoView != null) {
            val seekParams: IntArray = getActualSeekProgress(seekBar.progress)
            player.seekTo(seekParams[0], seekParams[1].toLong())
            player.playWhenReady = false
        }
    }

//    inner class GenerateCoverImageTask(
//        var time: Long,
//        private var pair: Pair<String, Boolean>?,
//        var callback: SingleCallback<Bitmap?, Long?>,
//    ) : Runnable {
//
//        private var originalBitmap: Bitmap? = null
//        private var formattedBitmap: Bitmap? = null
//
//        override fun run() {
//            try {
//                if (Thread.currentThread().isInterrupted) {
//                    return
//                } else {
//                    val uri = Uri.parse(pair?.first)
//                    val isFront = pair?.second!!
//
//                    mediaMetadataRetriever.setDataSource(context, uri)
//
//                    if (Thread.currentThread().isInterrupted) {
//                        return
//                    }
//
//                    originalBitmap = createBitmapFromMetaData(mediaMetadataRetriever, (time * 1000))
//
//                    val scaledBitmap = Bitmap.createScaledBitmap(
//                        originalBitmap!!,
//                        THUMB_WIDTH,
//                        THUMB_HEIGHT,
//                        false
//                    )
//
//                    formattedBitmap = if (isFront) {
//                        Utility.createFlippedBitmap(scaledBitmap, true, false)
//                    } else {
//                        scaledBitmap
//                    }
//
//                    if (Thread.currentThread().isInterrupted) {
//                        return
//                    }
//
//                    callback.onSingleCallback(formattedBitmap, time)
//                }
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//            }
//        }
//    }

    private fun createBitmapFromMetaData(
        mediaMetadataRetriever: MediaMetadataRetriever,
        time: Long,
    ): Bitmap? {
        val option = MediaMetadataRetriever.OPTION_CLOSEST_SYNC
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            mediaMetadataRetriever.getScaledFrameAtTime(
                time,
                option,
                THUMB_WIDTH,
                THUMB_HEIGHT
            )
        } else {
            mediaMetadataRetriever.getFrameAtTime(time, option)
        }
    }

    override fun onClick(v: View) {
        BaseAPIService.showProgressDialog(context)
        var future: Future<*>? = null
        var cancelledCount = 0
        if (futures.isNotEmpty()) {
            future = futures[futures.size - 1]
            var i = 1
            for (futureObj in futures) {
                if (i == futures.size) {
                    continue
                } else if (!futureObj.isDone) {
                    futureObj.cancel(true)
                    cancelledCount++
                }
                i++
            }
        }

        when (v.id) {

//            R.id.llThumbnilDone -> {
//                future?.let {
//                    if (originalBmp != null) {
//                        mediaMetadataRetriever.release()
//                        Utility.showLog("Pos", "$TOTAL_DURATION  <=> $customProgress")
//                        context.onChangeCoverDoneBtnClick(
//                            originalBmp,
//                            seekArray,
//                            customProgress,
//                            mediaPath,
//                            getRelativeFrontBack(seekArray[0])
//                        )
//                    } else {
//                        isDoneClicked = true
//                    }
//                } ?: run {
//                    mediaMetadataRetriever.release()
//                    context.onChangeCoverDoneBtnClick(
//                        originalBmp,
//                        seekArray,
//                        customProgress,
//                        mediaPath,
//                        getRelativeFrontBack(seekArray[0])
//                    )
//                }
//            }
//            R.id.llThumbnilCancel -> {
//                future?.let {
//                    if (originalBmp != null) {
//                        backManage()
//                    } else {
//                        isCancelClicked = true
//                    }
//                } ?: run {
//                    backManage()
//                }
//            }
        }

        backgroundExecutor.shutdown()
    }

    fun backManage() {
        stopPlaying()
        mediaMetadataRetriever.release()
        context.onChangeCoverCancelBtnClick()
    }

//    private fun generateThumbFromBitmap(bitmap: Bitmap): BitmapDrawable {
//        val formattedBitmap = Utility.getRoundedCornerBitmap(
//            context, bitmap,
//            ROUNDED_CORNER_BORDER_COLOR,
//            ROUNDED_CORNER_IMAGE_RADIUS,
//            ROUNDED_CORNER_BORDER_IMAGE
//        )
//
//        return Utility.convertBitmapToDrawable(context, formattedBitmap)
//    }

    private fun setVideoAdjustment() {
        val video = context.videoList[selectedPos]
        val size = video.previewSize

        Utility.printErrorLog("~~~~ Ratio: Video Width: ${size.width} Height: ${size.height}")

        val previewHeight = thumbnailBinding.llVideoView.height
        val previewWidth = (previewHeight * size.height) / size.width

        Utility.printErrorLog("~~~~ Ratio: previewHeight Width: $previewWidth Height: $previewHeight")

        val params = thumbnailBinding.llVideoView.layoutParams as RelativeLayout.LayoutParams
        params.height = previewHeight
        params.width = previewWidth
        thumbnailBinding.llVideoView.layoutParams = params
    }
}