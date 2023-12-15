package com.begenuin.library.views.activities

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.begenuin.library.R
import com.begenuin.library.SDKInitiate
import com.begenuin.library.common.Constants
import com.begenuin.library.common.DownloadVideo
import com.begenuin.library.common.FlipAnimator
import com.begenuin.library.common.SparkManager
import com.begenuin.library.common.Utility
import com.begenuin.library.common.Utility.getRequestBody
import com.begenuin.library.common.Utility.showLogException
import com.begenuin.library.common.customViews.CustomTextView
import com.begenuin.library.common.customViews.DisplayPictureView
import com.begenuin.library.common.customViews.PopupMenuCustomLayout
import com.begenuin.library.common.customViews.SparkView
import com.begenuin.library.common.customViews.customscrollview.DSVScrollConfig
import com.begenuin.library.common.customViews.customscrollview.DiscreteScrollView
import com.begenuin.library.common.customViews.tooltip.SimpleTooltip
import com.begenuin.library.core.enums.ExploreVideoType
import com.begenuin.library.core.enums.FeedAudioStatus
import com.begenuin.library.core.enums.FeedViewType
import com.begenuin.library.core.enums.SparkContentType
import com.begenuin.library.core.enums.VideoConvType
import com.begenuin.library.core.interfaces.AudioMuteUnMuteInterface
import com.begenuin.library.core.interfaces.FeedAdapterListener
import com.begenuin.library.core.interfaces.FeedCommunityListInterface
import com.begenuin.library.core.interfaces.FeedViewModelListener
import com.begenuin.library.core.interfaces.LoopSuggestionPagerEventListener
import com.begenuin.library.core.interfaces.OnVideoDownload
import com.begenuin.library.core.interfaces.ResponseListener
import com.begenuin.library.data.model.ChatModel
import com.begenuin.library.data.model.CommunityModel
import com.begenuin.library.data.model.ConversationModel
import com.begenuin.library.data.model.DiscoverModel
import com.begenuin.library.data.remote.BaseAPIService
import com.begenuin.library.data.viewmodel.ExploreViewModel
import com.begenuin.library.data.viewmodel.FeedViewModel
import com.begenuin.library.data.viewmodel.LoopSuggestionResponseListener
import com.begenuin.library.data.viewmodel.LoopSuggestionsViewModel
import com.begenuin.library.views.EqualSpacingItemDecoration
import com.begenuin.library.views.FeedOptionsCommunitiesAdapter
import com.begenuin.library.views.FeedRTAdapter
import com.begenuin.library.views.ItemSelectListener
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs

class HomeScreen : AppCompatActivity(), FeedCommunityListInterface, FeedViewModelListener,
    AudioMuteUnMuteInterface, View.OnClickListener,
    DiscreteScrollView.ScrollStateChangeListener<RecyclerView.ViewHolder>,
    DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>, OnVideoDownload,
    LoopSuggestionPagerEventListener, OnAudioFocusChangeListener {

    private val downloadVideoTask: DownloadVideo? = null
    private val DEFAULT_QUALIFICATION_SPAN: Long = 200
    private var bottomSheetView: View? = null
    lateinit var includedLayout: View
    private lateinit var rvCommunities: RecyclerView
    private lateinit var llForYou: LinearLayout
    private lateinit var llCommunityList: LinearLayout
    var userId = ""
    var deviceId = ""
    var feedMenuPosition = 0
    private lateinit var relativeNoNetwork: RelativeLayout
    private lateinit var tryAgain: Button
    private lateinit var ivNoNetwork: ImageView
    private  var relativeTutorial:RelativeLayout? = null
    private lateinit var feedBlurLayoutFragment:RelativeLayout
    private lateinit var rlCommunityInfinity: RelativeLayout
    private lateinit var rlLoopInfinity: RelativeLayout
    private lateinit var llTutorial : LinearLayout
    private  var rlShadowCommunityInfinity:RelativeLayout? = null
    private lateinit var rlShadowLoopInfinity:RelativeLayout
    private var tvCommunityHandle: TextView? = null
    private  var tvShadowCommunityHandle: TextView? = null
    private  var tvLoopName:TextView? = null
    private  var tvShadowLoopName:TextView? = null
    private var ivShadowCommunityDp: DisplayPictureView? = null
    private  var ivCommunityDp:DisplayPictureView? = null
    private  var llFeedSelectCommunityDp: DisplayPictureView? = null
    private var llShadowCommunityInfinity: LinearLayout? = null
    private  var llCommunityInfinity:LinearLayout? = null
    private var llInfinityContainer: LinearLayout? = null
    private lateinit var tutorialRepost: LinearLayout
    private var btnGotItFailure: MaterialButton? = null
    private var progressTimer: ProgressBar? = null
    private var tvReactionFailure: TextView? = null
    private lateinit var recyclerViewSuccessPage: RecyclerView
    private lateinit var feedProgressBar: LottieAnimationView
    private lateinit var feedBlurLayout: RelativeLayout
    private lateinit var reactionBlurLayout:RelativeLayout
    private lateinit var cvFeedSelector: CardView
    private  var cvSearch:CardView? = null
    private var llNoCommunityLoops: LinearLayout? = null
    private  var llNewLoop:LinearLayout? = null
    private var tvFeedType: TextView? = null
    private val bottomSheetDialogReport: BottomSheetDialog? = null
    private  var bottomSheetDialogAddLocation:BottomSheetDialog? = null
    private  var bottomSheetDialog:BottomSheetDialog? = null
    private  var bottomSheetEndOfSearchDialog:BottomSheetDialog? = null
    private val mDialog: Dialog? = null
    private var discoverList: ArrayList<ExploreViewModel<Any>> = ArrayList<ExploreViewModel<Any>>()
    private var tempDiscoverVOArrayList: ArrayList<ExploreViewModel<Any>> = ArrayList<ExploreViewModel<Any>>()
    private var isSingleEvent = false
    private val reason = ""
    private var module: String? = ""
    private  var currentVideoId:String? = ""
    private  var videoId:String? = ""
    private  var chatId:String? = ""
    private val type: ExploreVideoType? = null
    private var mLastClickTime: Long = 0
    private var currentVideoPlayDuration = 0
    private  var halfDuration:Int = 0
    private var startScrollMillis: Long = 0
    private val startReportMillis: Long = 0
    private  var endReportMillis:Long = 0
    private  var startBlockUserMills:Long = 0
    private  var endBlockUserMills:kotlin.Long = 0
    private var initialYValue = 0f
    private var timestampLastClick: Long = 0
    private var repostCounter = 0
    private var targetIndex = 0
    private var isSourceLoop = false
    private var isTargetLoop:Boolean = false
    private  var isSameCommunity:Boolean = false
    private  var isSameLoop:Boolean = false

    private lateinit var feedViewPager: DiscreteScrollView
    var feedViewAdapter: FeedRTAdapter? = null

    private var linkHandler: Handler? = null
    private  var repostHandler:Handler? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private val mAnimator: ValueAnimator? = null
    private val bottomSheetDialogPrivacyOptions: BottomSheetDialog? = null
    private  var bottomSheetDialogFlaggedVideo:BottomSheetDialog? = null
    private val loginActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var customLinkTooltip: SimpleTooltip? = null
    private  var customRepostTooltip:SimpleTooltip? = null
    private val isPaused = false
    private var isLongPressed = false
    private var previousPos = 0
    private var previousSeekPos: Long = 0
    private var isFeedRefresh = false
    private var discoverModel: DiscoverModel? = null
    private var swipeCounter = 0
    private var isFeedOptionsPopupShown = false
    private var selectedCommunityId = ""
    private var selectedCommunityHandle = ""
    private var popupMenu: PopupMenuCustomLayout? = null
    private var isEndOfCommunityFeed = false
    private  var llSubscriptions:LinearLayout? = null
    private var feedMenuClickListener: View.OnClickListener? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    /*
        Flags related to End/Empty feed loader.
    */
    var isEndOfFeed = false
    var isVerticalScrollEnabled = true
    private var countDownTimer: CountDownTimer? = null
    private var isLoaderSyncWithApiResponse = false
    private var isRefreshSubscribedVideoRequestComplete = false
    private var isButtonClickedOnce = false
    private var audioMuteUnMuteInterface1: AudioMuteUnMuteInterface? = null
    private var CurrentAudioStatus = FeedAudioStatus.UNMUTED
    var no_of_videos = 0
    var isEndOfFeedResultCalled = false
    private lateinit var sdkProfile: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        feedMenuPosition= FeedViewType.FOR_YOU.value;
        audioManagerInit()
        initViews()
        FeedViewModel.getInstance().setFeedCommunityListInterface(this)
        setProgressForVideo()
        FeedViewModel.getInstance().setFeedForYouListener(this)
        FeedViewModel.getInstance().reset()
        FeedViewModel.getInstance().startLaunchMillis = System.currentTimeMillis()
        FeedViewModel.getInstance().initializeFeedData(this)
        //registerActivityCallBack()
        //callSubscribeAPIAfterLogin()
    }

    @Suppress("InvalidSetHasFixedSize")
    private fun initViews() {
        val imgCloseSuccess: ImageView = findViewById<ImageView>(R.id.img_close_success)
        val imgCloseFailure: ImageView = findViewById<ImageView>(R.id.img_close_failure)
        val relativeTutorial: RelativeLayout = findViewById<RelativeLayout>(R.id.relative_tutorial)
        relativeTutorial.visibility = View.GONE
        tutorialRepost = findViewById<LinearLayout>(R.id.tutorialRepost)
        feedProgressBar = findViewById<LottieAnimationView>(R.id.feedProgressBar)
        rlCommunityInfinity = findViewById<RelativeLayout>(R.id.rlCommunityInfinity)
        rlLoopInfinity = findViewById<RelativeLayout>(R.id.rlLoopInfinity)
        rlShadowCommunityInfinity =
            findViewById<RelativeLayout>(R.id.rlShadowCommunityInfinity)
        rlShadowLoopInfinity = findViewById<RelativeLayout>(R.id.rlShadowLoopInfinity)
        llInfinityContainer = findViewById<LinearLayout>(R.id.llInfinityContainer)
        tvCommunityHandle = findViewById<TextView>(R.id.tvCommunityHandle)
        tvShadowCommunityHandle = findViewById<TextView>(R.id.tvShadowCommunityHandle)
        tvLoopName = findViewById<TextView>(R.id.tvLoopName)
        tvShadowLoopName = findViewById<TextView>(R.id.tvShadowLoopName)
        ivCommunityDp = findViewById(R.id.ivCommunityDp)
        ivShadowCommunityDp = findViewById(R.id.ivShadowCommunityDp)
        llShadowCommunityInfinity = findViewById<LinearLayout>(R.id.llShadowCommunityInfinity)
        llCommunityInfinity = findViewById<LinearLayout>(R.id.llCommunityInfinity)
        llTutorial = findViewById<LinearLayout>(R.id.llTutorial)
        llTutorial.setOnClickListener(this)
        relativeTutorial.setOnClickListener(this)
        tutorialRepost.setOnClickListener(this)
        rlCommunityInfinity.setOnClickListener(this)
        rlLoopInfinity.setOnClickListener(this)
        cvFeedSelector = findViewById<CardView>(R.id.cvFeedSelector)
        cvFeedSelector.setOnClickListener(this)
        tvFeedType = findViewById<TextView>(R.id.tvFeedType)
        llFeedSelectCommunityDp = findViewById(R.id.llFeedSelectCommunityDp)
        llNoCommunityLoops = findViewById<LinearLayout>(R.id.llNoCommunityLoops)
        llNewLoop = findViewById<LinearLayout>(R.id.llNewLoop)
        //llNewLoop.setOnClickListener(this)
        cvSearch = findViewById<CardView>(R.id.cvSearch)
        recyclerViewSuccessPage = findViewById<RecyclerView>(R.id.recyclerview_success)
        recyclerViewSuccessPage.setHasFixedSize(true)
        recyclerViewSuccessPage.scrollToPosition(0)
        val horizontalLayout =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewSuccessPage.layoutManager = horizontalLayout
        recyclerViewSuccessPage.addItemDecoration(EqualSpacingItemDecoration(30, EqualSpacingItemDecoration.HORIZONTAL))
        feedBlurLayoutFragment = findViewById<RelativeLayout>(R.id.feedBlurLayoutFragment)
        feedBlurLayout = findViewById<RelativeLayout>(R.id.feedBlurLayout)
        feedBlurLayout.visibility = GONE
        reactionBlurLayout = findViewById<RelativeLayout>(R.id.reactionBlurLayout)
        tvReactionFailure = findViewById<TextView>(R.id.tvReactionFailure)
        imgCloseSuccess.setOnClickListener {
            feedBlurLayout.visibility = GONE
            playCurrentVideo()
        }
        imgCloseFailure.setOnClickListener {
            playCurrentVideo()
            reactionBlurLayout.visibility = View.GONE
        }
        val btnGotIt: MaterialButton = findViewById<MaterialButton>(R.id.btn_gotit)
        var btnGotItFailure = findViewById<MaterialButton>(R.id.btnGotItFailure)
        val chkSuccess: CheckBox = findViewById<CheckBox>(R.id.checkbox_reply)
        chkSuccess.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
//            SharedPrefUtils.setBoolPreference(
//                context,
//                Constants.PREF_IS_SHOWN_SUCCESS,
//                isChecked
//            )
        }
        btnGotIt.setOnClickListener {
            feedBlurLayout.visibility = View.GONE
            playCurrentVideo()
        }
        btnGotItFailure.setOnClickListener(View.OnClickListener {
            playCurrentVideo()
            reactionBlurLayout.visibility = View.GONE
//            if (btnGotItFailure.text.toString().equals(resources.getString(R.string.go_to_inbox), ignoreCase = true)) {
//                goToInbox()
//            }
        })
        progressTimer = findViewById<ProgressBar>(R.id.progressTimer)
        feedViewPager = findViewById(R.id.feedViewPager)
        includedLayout = findViewById<View>(R.id.fragment_feed_no_internet)
        relativeNoNetwork =  findViewById<RelativeLayout>(R.id.relative_nonetowrk)
        relativeNoNetwork.visibility = View.GONE
        tryAgain= includedLayout.findViewById<Button>(R.id.btntryAgain)
        ivNoNetwork = includedLayout.findViewById<ImageView>(R.id.ivNoNetwork)
        ivNoNetwork.setImageResource(R.drawable.no_network_black)
        includedLayout.visibility = View.GONE
        tryAgain.setOnClickListener {
            discoverVideos(true, true)
        }
        initFeedDisplayOptions()
        // Set max width of cvFeedSelector to half of the screen width
        val clFeedOptionsContainer: ConstraintLayout = findViewById<ConstraintLayout>(R.id.clFeedOptionsContainer)
        val dimens: IntArray? = Utility.getScreenWidthHeight(this)
        val maxHalfScreenWidth = (dimens?.get(0) ?: 0) / 2 - Utility.dpToPx(16f, this).toInt()
        val c = ConstraintSet()
        c.clone(clFeedOptionsContainer)
        c.constrainMaxWidth(R.id.cvFeedSelector, maxHalfScreenWidth)
        c.applyTo(clFeedOptionsContainer)
        sdkProfile = findViewById(R.id.sdkProfile)
        sdkProfile.setOnClickListener(this)
    }

    private fun initFeedDisplayOptions() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.layout_feed_selector_menu, null)
        llForYou = popupView.findViewById<LinearLayout>(R.id.llForYou)
        val llSubscriptions = popupView.findViewById<LinearLayout>(R.id.llSubscriptions)
        val llMyLoops = popupView.findViewById<LinearLayout>(R.id.llMyLoops)
        popupMenu = PopupMenuCustomLayout(this, popupView) { isFeedOptionsPopupShown = false }
        rvCommunities = popupView.findViewById<RecyclerView>(R.id.rvCommunities)
        llCommunityList = popupView.findViewById<LinearLayout>(R.id.llCommunityList)
        rvCommunities.layoutManager = LinearLayoutManager(this)
        rvCommunities.adapter = FeedOptionsCommunitiesAdapter(
            this,
            FeedViewModel.getInstance().masterCommunitiesList,
            object : ItemSelectListener {
                override fun onItemSelect(community: CommunityModel) {
                    llForYou.background = ContextCompat.getDrawable(this@HomeScreen, R.color.colorWhite)
                    llSubscriptions.background = ContextCompat.getDrawable(this@HomeScreen,R.color.colorWhite)
                    llMyLoops.background = ContextCompat.getDrawable(this@HomeScreen, R.color.colorWhite)
                    selectedCommunityId = community.communityId
                    selectedCommunityHandle = community.handle
                    switchFeed(FeedViewType.COMMUNITY.value, community)
                    popupMenu!!.dismiss()
                }
            }, selectedCommunityId
        )
        if (FeedViewModel.getInstance().masterCommunitiesList.isEmpty()) {
            llCommunityList.visibility = GONE
        } else {
            llCommunityList.visibility = View.VISIBLE
        }
        feedMenuClickListener = View.OnClickListener { menuItemId: View ->
            llForYou.background = ContextCompat.getDrawable(this, R.color.colorWhite)
            llSubscriptions.background = ContextCompat.getDrawable(this, R.color.colorWhite)
            llMyLoops.background = ContextCompat.getDrawable(this, R.color.colorWhite)
            if (rvCommunities.adapter != null) {
                (rvCommunities.adapter as FeedOptionsCommunitiesAdapter?)?.removeHighlight()
            }
            selectedCommunityId = ""
            selectedCommunityHandle = ""
            if (menuItemId.id == R.id.llForYou) {
                llForYou.background = ContextCompat.getDrawable(this, R.color.color_E7E7E7)
                switchFeed(FeedViewType.FOR_YOU.value, null)
            } else if (menuItemId.id == R.id.llSubscriptions) {
                llSubscriptions.background = ContextCompat.getDrawable(this, R.color.color_E7E7E7)
                switchFeed(FeedViewType.SUBSCRIPTIONS.value, null)
            } else if (menuItemId.id == R.id.llMyLoops) {
                llMyLoops.background = ContextCompat.getDrawable(this, R.color.color_E7E7E7)
                switchFeed(FeedViewType.MY_LOOPS.value, null)
            }
            popupMenu!!.dismiss()
        }
        llForYou.setOnClickListener(feedMenuClickListener)
        llSubscriptions.setOnClickListener(feedMenuClickListener)
        llMyLoops.setOnClickListener(feedMenuClickListener)
        if (feedMenuPosition == FeedViewType.FOR_YOU.value) {
            llForYou.background = ContextCompat.getDrawable(this, R.color.color_E7E7E7)
        } else if (feedMenuPosition == FeedViewType.SUBSCRIPTIONS.value) {
            llSubscriptions.background = ContextCompat.getDrawable(this, R.color.color_E7E7E7)
        } else if (feedMenuPosition == FeedViewType.MY_LOOPS.value) {
            llMyLoops.background = ContextCompat.getDrawable(this, R.color.color_E7E7E7)
        }
    }

    private fun audioManagerInit() {
        audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setOnAudioFocusChangeListener(this)
                    .build()
        }
    }

    private fun playCurrentVideo() {
        if (!::feedViewPager.isInitialized) {
            return
        }
        val textureVideoView: StyledPlayerView = getTextureVideoView(feedViewPager.currentItem)
        if (CurrentAudioStatus === FeedAudioStatus.UNMUTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioManager != null) {
                audioManager?.requestAudioFocus(audioFocusRequest!!) //onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioManager != null) {
                audioManager?.abandonAudioFocusRequest(audioFocusRequest!!)
            }
        }
        if (textureVideoView.player != null) {
            if ((bottomSheetDialogAddLocation == null || !bottomSheetDialogAddLocation!!.isShowing) && (bottomSheetEndOfSearchDialog == null || !bottomSheetEndOfSearchDialog!!.isShowing)) {
                if (discoverList.size > feedViewPager.currentItem)  //&& shouldVideoPlay()
                {
                    textureVideoView.player!!.playWhenReady = true
                    Utility.showLog("Tag", "Video play $feedMenuPosition")
                }
            }
        }
        muteUnMuteManagement(false)
    }

    private fun muteUnMuteManagement(isShowToast: Boolean) {
        if (isShowToast && isPaused) {
            if (CurrentAudioStatus === FeedAudioStatus.MUTED) {
                Utility.showMuteUnMuteToast(this, R.drawable.ic_mute)
            } else if (CurrentAudioStatus === FeedAudioStatus.UNMUTED) {
                Utility.showMuteUnMuteToast(this, R.drawable.ic_unmute)
            }
        }
        val pos = feedViewPager.currentItem
        val styledPlayerView = getTextureVideoView(pos)

        if (styledPlayerView != null && styledPlayerView.player != null) {
            styledPlayerView.player!!.volume =
                (if (CurrentAudioStatus === FeedAudioStatus.MUTED || CurrentAudioStatus === FeedAudioStatus.MUTED_BY_AUDIO_FOCUS) 0 else 1).toFloat()
        }
        val ivCurrent: ImageView = getMuteIconIv(pos)
        if (CurrentAudioStatus === FeedAudioStatus.MUTED || CurrentAudioStatus === FeedAudioStatus.MUTED_BY_AUDIO_FOCUS) {
            Handler().postDelayed({
                ivCurrent.visibility =
                    if (CurrentAudioStatus === FeedAudioStatus.MUTED || CurrentAudioStatus === FeedAudioStatus.MUTED_BY_AUDIO_FOCUS) View.VISIBLE else GONE
            }, 300)
        } else {
            ivCurrent.visibility = GONE
        }
        val ivPrevious : ImageView = getMuteIconIv(pos)
        ivPrevious.visibility =
            if (CurrentAudioStatus === FeedAudioStatus.MUTED || CurrentAudioStatus === FeedAudioStatus.MUTED_BY_AUDIO_FOCUS) View.VISIBLE else GONE
        val ivNext: ImageView = getMuteIconIv(pos + 1)
        ivNext.visibility =
            if (CurrentAudioStatus === FeedAudioStatus.MUTED || CurrentAudioStatus === FeedAudioStatus.MUTED_BY_AUDIO_FOCUS) View.VISIBLE else GONE
        feedViewAdapter?.setMuted(CurrentAudioStatus === FeedAudioStatus.MUTED || CurrentAudioStatus === FeedAudioStatus.MUTED_BY_AUDIO_FOCUS)
    }

    private fun getTextureVideoView(position: Int): StyledPlayerView {
        return feedViewPager.findViewWithTag(Constants.VIDEO + position)
    }

    private fun getMuteIconIv(pos: Int): ImageView {
        return feedViewPager.findViewWithTag(Constants.IV_MUTE + pos)
    }

    @SuppressLint("HardwareIds")
    fun discoverVideos(isLoading: Boolean, isNewDiscover: Boolean) {
        //userId = SharedPrefUtils.getStringPreference(context, Constants.PREF_USER)
        //TODO: Need to get it from app
        userId = SDKInitiate.userId
        deviceId =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        if (discoverList.size == 0) {
            llInfinityContainer!!.visibility = View.GONE
        }
        if (!Utility.isNetworkAvailable(this)) {
            includedLayout.visibility = View.VISIBLE
            relativeNoNetwork.visibility = View.VISIBLE
            feedProgressBar.visibility = GONE
            return
        } else {
            includedLayout.visibility = GONE
            relativeNoNetwork.visibility = GONE
        }
        if (feedMenuPosition == FeedViewType.MY_LOOPS.value) {
            if (FeedViewModel.getInstance().statusForMyLoops === FeedViewModel.APIStatus.IN_PROGRESS) {
                feedProgressBar.visibility = View.VISIBLE
                return
            } else if (FeedViewModel.getInstance().statusForMyLoops === FeedViewModel.APIStatus.NONE) {
                if (isNewDiscover) {
                    feedProgressBar.visibility = View.VISIBLE
                }
            } else if (FeedViewModel.getInstance().statusForMyLoops === FeedViewModel.APIStatus.COMPLETED && !TextUtils.isEmpty(
                    FeedViewModel.getInstance().getResponse(feedMenuPosition)
                )
            ) {
                setDiscoverResponse()
                FeedViewModel.getInstance().setEmptyResponse(feedMenuPosition)
                return
            }
        } else if (feedMenuPosition == FeedViewType.SUBSCRIPTIONS.value) {
            if (FeedViewModel.getInstance().statusForSubscriptions === FeedViewModel.APIStatus.IN_PROGRESS) {
                feedProgressBar.visibility = View.VISIBLE
                return
            } else if (FeedViewModel.getInstance().statusForSubscriptions === FeedViewModel.APIStatus.NONE) {
                if (isNewDiscover) {
                    feedProgressBar.visibility = View.VISIBLE
                }
            } else if (FeedViewModel.getInstance().statusForSubscriptions === FeedViewModel.APIStatus.COMPLETED && !TextUtils.isEmpty(
                    FeedViewModel.getInstance().getResponse(feedMenuPosition)
                )
            ) {
                setDiscoverResponse()
                FeedViewModel.getInstance().setEmptyResponse(feedMenuPosition)
                return
            }
        } else if (feedMenuPosition == FeedViewType.FOR_YOU.value) {
            if (FeedViewModel.getInstance().statusForYou === FeedViewModel.APIStatus.IN_PROGRESS) {
                feedProgressBar.visibility = View.VISIBLE
                return
            } else if (FeedViewModel.getInstance().statusForYou === FeedViewModel.APIStatus.NONE) {
                if (isNewDiscover) {
                    feedProgressBar.visibility = View.VISIBLE
                }
            } else if (FeedViewModel.getInstance().statusForYou === FeedViewModel.APIStatus.COMPLETED && !TextUtils.isEmpty(
                    FeedViewModel.getInstance().getResponse(feedMenuPosition)
                )
            ) {
                setDiscoverResponse()
                FeedViewModel.getInstance().setEmptyResponse(feedMenuPosition)
                return
            }
        } else {
            if (FeedViewModel.getInstance().statusForCommunityVideos === FeedViewModel.APIStatus.IN_PROGRESS) {
                feedProgressBar.visibility = View.VISIBLE
                return
            } else if (FeedViewModel.getInstance().statusForCommunityVideos === FeedViewModel.APIStatus.NONE) {
                if (isNewDiscover && llNoCommunityLoops!!.visibility != View.VISIBLE) {
                    feedProgressBar.visibility = View.VISIBLE
                }
            } else if (FeedViewModel.getInstance().statusForCommunityVideos === FeedViewModel.APIStatus.COMPLETED && !TextUtils.isEmpty(
                    FeedViewModel.getInstance().getResponse(feedMenuPosition)
                )
            ) {
                setDiscoverResponse()
                FeedViewModel.getInstance().setEmptyResponse(feedMenuPosition)
                return
            }
        }
        val searchText = FeedViewModel.getInstance().searchText
        if (!TextUtils.isEmpty(searchText)) {
//            FeedViewModel.getInstance()
//                .searchVideos(this, isNewDiscover, isLoading)
        } else {
            if (feedMenuPosition == FeedViewType.MY_LOOPS.value) {
                FeedViewModel.getInstance().feedForMyLoopsVideos(this, isNewDiscover)
            } else if (feedMenuPosition == FeedViewType.SUBSCRIPTIONS.value) {
                FeedViewModel.getInstance().feedForMySubscriptionsVideos(this, isNewDiscover)
            } else if (feedMenuPosition == FeedViewType.FOR_YOU.value) {
                FeedViewModel.getInstance().feedForYouVideos(
                    this,
                    isNewDiscover,
                    isLoading
                )
            } else {
                FeedViewModel.getInstance()
                    .feedCommunityVideos(this, isNewDiscover, selectedCommunityId)
            }
        }
    }

    private fun setDiscoverResponse() {
        try {
            feedProgressBar.visibility = GONE
            discoverList.clear()
            discoverList.addAll(FeedViewModel.getInstance().getMasterDiscoverArr(feedMenuPosition))
            if (discoverList.size > 0) {
                setAdapter(0)
                if (TextUtils.isEmpty(FeedViewModel.getInstance().searchText) && !FeedViewModel.getInstance().isDataDogLogged) {
                    FeedViewModel.getInstance().isDataDogLogged = true
//                    Utility.sendDataDogLatencyLogs(
//                        Constants.HOME_SCREEN_LOADED,
//                        FeedViewModel.getInstance().startLaunchMillis
//                    )
                }
            } else {
                onEmptyFeedData(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCommunityListLoaded(communityList: ArrayList<CommunityModel>) {
        if (rvCommunities != null && rvCommunities.adapter != null) {
            if (communityList.isEmpty()) {
                llCommunityList.visibility = GONE
            } else if (llCommunityList != null && rvCommunities.adapter != null) {
                llCommunityList.visibility = View.VISIBLE
                rvCommunities.adapter!!.notifyDataSetChanged()
            }
        }
    }

    override fun onFeedDataLoaded(
        discoverVOArrayList: java.util.ArrayList<ExploreViewModel<Any>>?,
        isNewDiscover: Boolean
    ) {
        feedProgressBar!!.visibility = GONE
        llNoCommunityLoops!!.visibility = GONE
        try {
            //try-catch, not null check added. Redundant 'assert' removed.
            if (feedMenuPosition == FeedViewType.SUBSCRIPTIONS.value && isEndOfFeed) {
                isRefreshSubscribedVideoRequestComplete = true
                tempDiscoverVOArrayList.clear()
                tempDiscoverVOArrayList.addAll(discoverVOArrayList!!)
                //context.setRefreshHome(false)
                FeedViewModel.getInstance().deepLinkVideoId = ""
                FeedViewModel.getInstance().lastPos = 0
                if (isLoaderSyncWithApiResponse) {
                    feedViewPager.findViewWithTag<View>(Constants.LL_LOADER + feedViewPager.currentItem).visibility =
                        GONE
                    feedViewAdapter = null
                    discoverList.clear()
                    setLoadedFeedData(discoverVOArrayList, isNewDiscover)
                    feedViewPager.setScrollConfig(DSVScrollConfig.ENABLED)
                    isVerticalScrollEnabled = true
                    setLoadedFeedData(tempDiscoverVOArrayList, true)
                    isEndOfFeedResultCalled = false
                    isButtonClickedOnce = false
                    isRefreshSubscribedVideoRequestComplete = false
                    isLoaderSyncWithApiResponse = false
                }
            } else {
                setLoadedFeedData(discoverVOArrayList, isNewDiscover)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onFeedCacheDataLoaded(discoverVOArrayList: ArrayList<ExploreViewModel<Any>>?) {
        feedProgressBar!!.visibility = GONE
        //setRefreshHome(false) //TODO: No refresh on pull
        FeedViewModel.getInstance().deepLinkVideoId = ""
        discoverList.clear()
        feedViewAdapter = null
        discoverList.addAll(discoverVOArrayList!!)
        setAdapter(0)
    }

    override fun onEmptyFeedData(isNewDiscover: Boolean) {
        feedProgressBar!!.visibility = GONE

        if (isNewDiscover) {
            discoverList.clear()
            llInfinityContainer!!.visibility = View.GONE
        }

        if (FeedViewModel.getInstance().isEndOfSearch) {
//            if (discoverList.size == 0 && context.isFromSearch) {
//                //context.isFromSearch = false
//                prevSearchText = ""
//                context.searchFragment.setNoSearchResults()
//            }
        } else if (feedMenuPosition == FeedViewType.COMMUNITY.value) {
            isEndOfCommunityFeed = true
            if (discoverList.size == 0) {
                if (feedViewAdapter != null) {
                    feedViewAdapter!!.notifyDataSetChanged()
                }
                llNoCommunityLoops!!.visibility = View.VISIBLE
                FeedViewModel.getInstance().statusForCommunityVideos = FeedViewModel.APIStatus.NONE
            }
        } else {
            if (feedViewAdapter == null) {
                setAdapter(0)
            }
            if (!isEndOfFeedResultCalled) {
                if (discoverList.size == 0) {
                    feedProgressBar!!.visibility = View.VISIBLE
                }
                if (feedMenuPosition == FeedViewType.MY_LOOPS.value) {
                    FeedViewModel.getInstance().statusForMyLoops = FeedViewModel.APIStatus.NONE
                } else if (feedMenuPosition == FeedViewType.SUBSCRIPTIONS.value) {
                    FeedViewModel.getInstance().statusForSubscriptions =
                        FeedViewModel.APIStatus.NONE
                }
                if (LoopSuggestionsViewModel.apiStatus[feedMenuPosition] != FeedViewModel.APIStatus.IN_PROGRESS
                ) {
                    LoopSuggestionsViewModel.suggestLoops(
                        this,
                        1,
                        feedMenuPosition,
                        object : LoopSuggestionResponseListener {
                            override fun onFetchComplete(
                                list: List<ConversationModel>,
                                isEndOfLoops: Boolean
                            ) {
                                feedViewAdapter!!.setEndOfFeedData(
                                    list as ArrayList<ConversationModel>?,
                                    isEndOfLoops,
                                    feedMenuPosition,
                                    this@HomeScreen
                                )
                                feedProgressBar!!.visibility = GONE
                                isEndOfFeedResultCalled = true
                            }

                        }
                    )
                }
            }
        }
    }

    override fun onFeedDataFailure(code: String?, isSearch: Boolean) {
        if (code.equals("429", ignoreCase = true))
            managementForRefreshFeed()
    }

    override fun muteViaFocusLoss() {
        muteAudio(true)
    }

    override fun unMuteSound() {
        unMuteAudio()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.llNewLoop -> {
                //TODO: No redirection as of now
//                val intent = Intent(context, CameraNewActivity::class.java)
//                intent.putExtra("from", Constants.FROM_ROUND_TABLE)
//                intent.putExtra("community_id", selectedCommunityId)
//                intent.putExtra("community_handle", selectedCommunityHandle)
//                startActivity(intent)
//                context.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
            }

            R.id.cvFeedSelector -> showFeedSelectOptions()

            R.id.relative_tutorial, R.id.llTutorial -> {
                relativeTutorial!!.visibility = GONE
                replyClickManage()
            }

            R.id.tutorialRepost -> {
                //tutorialRepost.visibility = GONE
                repostVideoManagement()
            }

            //R.id.ivClearSearchText -> FeedViewModel.getInstance().searchText = ""
            R.id.cvSearch -> {
                //TODO: No redirection as of now
//                if (!Utility.isNetworkAvailable(context)) {
//                    return
//                }
//                val i = Intent(context, SearchNewActivity::class.java)
//                startActivity(i)
//                context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            R.id.rlCommunityInfinity -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                val exploreViewModel: ExploreViewModel<Any> =
                    discoverList[feedViewPager.currentItem]
                if (exploreViewModel.type === ExploreVideoType.RT) {
                    val conversationModel = exploreViewModel.obj as ConversationModel
                    if (conversationModel.community != null) {
                        val iCommunityDetails = Intent(this, CommunityDetailsActivity::class.java)
                        iCommunityDetails.putExtra("community_id", conversationModel.community.communityId)
                        iCommunityDetails.putExtra("role", conversationModel.community.role)
                        startActivity(iCommunityDetails)
                        overridePendingTransition(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                    }
//                val tag = "CommunityDetails"
//                val fragment: CommunityDetailsFragment = CommunityDetailsFragment.newInstance(
//                    communityId = selectedCommunityId, 1
//                )
//                val fragmentManager: FragmentManager = supportFragmentManager
//                    val fragmentTransaction = fragmentManager.beginTransaction()
//                    fragmentTransaction.add(R.id.profile_fragment_container, fragment, tag)
//                    fragmentTransaction.addToBackStack(tag)
//                    fragmentTransaction.commit()
                    //}
                }
            }

            R.id.rlLoopInfinity -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                val pos = feedViewPager.currentItem
                val exploreLoopViewModel: ExploreViewModel<Any> = discoverList[pos]
                if (exploreLoopViewModel.type === ExploreVideoType.RT) {
                    val conversationModel = exploreLoopViewModel.obj
                    pauseCurrentVideo(false)
                    //TODO: no redirection as of now
//                    val fragmentManager: FragmentManager = getChildFragmentManager()
//                    val fragmentTransaction = fragmentManager.beginTransaction()
//                    fragmentTransaction.setCustomAnimations(
//                        R.anim.slide_in_right,
//                        R.anim.slide_out_left,
//                        R.anim.slide_in_left,
//                        R.anim.slide_out_right
//                    )
//                    val fragment: FeedLoopFragment = FeedLoopFragment.newInstance(
//                        discoverList[pos].feedId,
//                        discoverList[pos].convId,
//                        context.mainPager.getCurrentItem(),
//                        conversationModel.group,
//                        conversationModel.settings,
//                        conversationModel.shareURL,
//                        conversationModel.isSubscriber,
//                        conversationModel.memberInfo,
//                        "",
//                        true
//                    )
//                    val tag = "feedloop"
//                    fragmentTransaction.add(R.id.profile_fragment_container, fragment, tag)
//                    fragmentTransaction.addToBackStack(tag)
//                    fragmentTransaction.commit()
                }
            }
            R.id.sdkProfile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun showFeedSelectOptions() {
        if (!Utility.isNetworkAvailable(this)) {
            return
        }
        if (popupMenu != null) {
            if (rvCommunities.adapter != null) {
                if (!TextUtils.isEmpty(selectedCommunityId)) {
                    (rvCommunities.adapter as FeedOptionsCommunitiesAdapter?)!!.highlightCommunity(
                        selectedCommunityId
                    )
                }
            }
            popupMenu?.show(cvFeedSelector)
            isFeedOptionsPopupShown = true
        }
    }
    override fun onScrollStart(currentItemHolder: RecyclerView.ViewHolder, adapterPosition: Int) {
        Utility.showLog("scroll", "start $adapterPosition")
    }

    override fun onScrollEnd(currentItemHolder: RecyclerView.ViewHolder, adapterPosition: Int) {
        Utility.showLog("scroll", "start $adapterPosition")
    }

    override fun onScroll(
        scrollPosition: Float,
        currentPosition: Int,
        newPosition: Int,
        currentHolder: RecyclerView.ViewHolder?,
        newCurrent: RecyclerView.ViewHolder?
    ) {
        Utility.showLog("Scroll", "$scrollPosition $currentPosition $newPosition")
        if (targetIndex != newPosition) {
            setDataForInfinityView(currentPosition, newPosition)
        }
        targetIndex = newPosition

        val absScrollPosition = abs(scrollPosition)
        if (absScrollPosition > 0.5) {
            if (isTargetLoop) {
                llInfinityContainer!!.visibility = View.VISIBLE
            } else {
                llInfinityContainer!!.visibility = View.GONE
            }
        } else if (isSourceLoop) {
            llInfinityContainer!!.visibility = View.VISIBLE
        } else {
            llInfinityContainer!!.visibility = View.GONE
        }
       // if (!isSameCommunity) {
            if (absScrollPosition > 0.5) {
                rlShadowCommunityInfinity!!.alpha = 1f
                rlCommunityInfinity!!.alpha = 0f
            } else {
                rlShadowCommunityInfinity!!.alpha = 0f
                rlCommunityInfinity!!.alpha = 1f
            }
            rlCommunityInfinity!!.rotationX = scrollPosition * (-1 * 180f)
            rlShadowCommunityInfinity!!.rotationX = scrollPosition * (-1 * 180f) - 180
       // }

        if (!isSameLoop) {
            if (absScrollPosition > 0.5) {
                rlShadowLoopInfinity.alpha = 1f
                rlLoopInfinity.alpha = 0f
            } else {
                rlShadowLoopInfinity.alpha = 0f
                rlLoopInfinity.alpha = 1f
            }
            rlLoopInfinity.rotationX = scrollPosition * (-1 * 180f)
            rlShadowLoopInfinity.rotationX = scrollPosition * (-1 * 180f) - 180
        }
    }

    private fun setDataForInfinityView(currentIndex: Int, newIndex: Int) {
        isSourceLoop = false
        isTargetLoop = false
        isSameCommunity = false
        isSameLoop = false
        var sourceModel: ConversationModel? = null
        var targetModel: ConversationModel? = null
        var sourceCommunityModel: CommunityModel? = null
        var targetCommunityModel: CommunityModel? = null
        if (currentIndex < discoverList.size) {
            val exploreSourceModel: ExploreViewModel<Any> = discoverList[currentIndex]
            isSourceLoop = exploreSourceModel.type === ExploreVideoType.RT
            if (isSourceLoop) {
                sourceModel = exploreSourceModel.obj as ConversationModel?
                if (sourceModel!!.group != null) {
                    tvLoopName!!.text = sourceModel.group.name
                }
                sourceCommunityModel = sourceModel.community
                if (sourceCommunityModel != null) {
                    llCommunityInfinity!!.visibility = View.VISIBLE
                    tvCommunityHandle!!.text = sourceModel.community.handle
                    if (TextUtils.isEmpty(sourceCommunityModel.dp)) {
                        ivCommunityDp!!.setDpWithInitials(
                            sourceCommunityModel.name,
                            sourceCommunityModel.colorCode,
                            sourceCommunityModel.textColorCode
                        )
                    } else {
                        ivCommunityDp!!.setDpWithImage(
                            this,
                            false,
                            sourceCommunityModel.dp,
                            sourceCommunityModel.dpS,
                            false
                        )
                    }
                } else {
                    llCommunityInfinity!!.visibility = View.VISIBLE
                }
            }
        }
        if (newIndex < discoverList.size) {
            val exploreTargetModel: ExploreViewModel<Any> = discoverList[newIndex]
            isTargetLoop = exploreTargetModel.type === ExploreVideoType.RT
            if (isTargetLoop) {
                targetModel = exploreTargetModel.obj as ConversationModel?
                if (targetModel!!.group != null) {
                    tvShadowLoopName!!.text = targetModel.group.name
                }
                targetCommunityModel = targetModel.community
                if (targetCommunityModel != null) {
                    llShadowCommunityInfinity!!.visibility = View.VISIBLE
                    tvShadowCommunityHandle!!.text = targetModel.community.handle
                    if (TextUtils.isEmpty(targetCommunityModel.dp)) {
                        ivShadowCommunityDp!!.setDpWithInitials(
                            targetCommunityModel.name,
                            targetCommunityModel.colorCode,
                            targetCommunityModel.textColorCode
                        )
                    } else {
                        ivShadowCommunityDp!!.setDpWithImage(
                            this,
                            false,
                            targetCommunityModel.dp,
                            targetCommunityModel.dpS,
                            false
                        )
                    }
                } else {
                    llShadowCommunityInfinity!!.visibility = View.VISIBLE
                }
            }
        }
        if (sourceModel != null && targetModel != null) {
            if (isSourceLoop && isTargetLoop) {
                isSameLoop = sourceModel.chatId.equals(targetModel.chatId, true)
                isSameCommunity =
                    if (sourceCommunityModel != null && targetCommunityModel != null) {
                        val sourceCommunityId = sourceCommunityModel.communityId
                        val targetCommunityId = targetCommunityModel.communityId
                        sourceCommunityId.equals(targetCommunityId, ignoreCase = true)
                    } else {
                        sourceCommunityModel == null && targetCommunityModel == null
                    }
            }
        }
    }


    override fun onCurrentItemChanged(viewHolder: RecyclerView.ViewHolder?, position: Int) {
        if (position == -1) {
            return
        }
        try {
            resetInfinity()
            //try-catch, not null check added. Redundant 'assert' removed.
            if (feedMenuPosition == FeedViewType.MY_LOOPS.value || feedMenuPosition == FeedViewType.SUBSCRIPTIONS.value) {
                isEndOfFeed = viewHolder is FeedRTAdapter.EndOfFeedViewHolder
            } else {
                isEndOfFeed = false
            }
            startScrollMillis = System.currentTimeMillis()
            //        downloadCancelManagement();
            val exploreViewModel: ExploreViewModel<Any> = discoverList[position];
            var discoverVO: DiscoverModel? = null
            var conversationModel: ConversationModel? = null
            if (exploreViewModel.type === ExploreVideoType.PUBLIC_VIDEO) {
                llInfinityContainer!!.visibility = View.GONE
                discoverVO = exploreViewModel.obj as DiscoverModel?
                currentVideoId = discoverVO!!.videoId
                val videoUrl = discoverVO.videoUrl
                val finalUrl = videoUrl.substring(videoUrl.lastIndexOf('/') + 1)
                //TODO: Need to ask about this
//                val file: File = File(getCacheDir(), finalUrl)
//                if (file.exists()) {
//                    discoverVO.localVideoPath = file.absolutePath
//                    Utility.getDBHelper().insertOrUpdateVideoCache(
//                        file.name,
//                        "",
//                        Utility.formatDownloadDate(),
//                        videoUrl,
//                        ""
//                    )
//                }
            } else if (exploreViewModel.type === ExploreVideoType.RT) {
                llInfinityContainer!!.visibility = View.VISIBLE
                conversationModel = exploreViewModel.obj as ConversationModel?
                if (conversationModel!!.community != null) {
                    val communityModel = conversationModel.community
                    llCommunityInfinity!!.visibility = View.VISIBLE
                    tvCommunityHandle!!.text = communityModel.handle
                    if (TextUtils.isEmpty(communityModel.dp)) {
                        ivCommunityDp!!.setDpWithInitials(
                            communityModel.name,
                            communityModel.colorCode,
                            communityModel.textColorCode
                        )
                    } else {
                        ivCommunityDp!!.setDpWithImage(
                            this,
                            false,
                            communityModel.dp,
                            communityModel.dpS,
                            false
                        )
                    }
                } else {
                    llCommunityInfinity!!.visibility = View.INVISIBLE
                }
                if (conversationModel.group != null) {
                    tvLoopName!!.text = conversationModel.group.name
                }
            }
            if (feedBlurLayout.visibility == View.VISIBLE || feedBlurLayoutFragment.visibility == View.VISIBLE) {
                pauseCurrentVideo(false)
            }
           // val previousTextureVideoView = getTextureVideoView(previousPos)
            no_of_videos++
            if (getLinkLayout() != null) {
                getLinkLayout()?.visibility = GONE
            }
            if (CurrentAudioStatus === FeedAudioStatus.MUTED_BY_AUDIO_FOCUS) {
                CurrentAudioStatus = FeedAudioStatus.UNMUTED
                unMuteAudio()
            }
            if (getMuteIconIv() != null) {
                if (CurrentAudioStatus === FeedAudioStatus.MUTED) {
                    getMuteIconIv()?.visibility = View.VISIBLE
                } else if (CurrentAudioStatus === FeedAudioStatus.UNMUTED) {
                    getMuteIconIv()?.visibility = GONE
                }
            }
            val textureVideoView = getTextureVideoView(position)
            if (textureVideoView.player != null) {
                    if (discoverVO != null) {
                        discoverVO.isEventLogged = false
                        discoverVO.isViewCountUpdated = false
                    } else if (conversationModel != null) {
                        val chatModel = conversationModel.chats[conversationModel.chats.size - 1]
                        if (chatModel != null) {
                            chatModel.isEventLogged = false
                            chatModel.isViewCountUpdated = false
                        }
                    }
                    textureVideoView.player!!.volume =
                        (if (CurrentAudioStatus === FeedAudioStatus.MUTED) 0 else 1).toFloat()

                if (CurrentAudioStatus === FeedAudioStatus.UNMUTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            audioManager?.requestAudioFocus(audioFocusRequest!!) //onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                        }
                textureVideoView.player!!.playWhenReady = true
            //  if (isSameTab() && (context.bottomSheetDialogCamera == null || !bottomSheetDialogCamera.isShowing()) &&
                        //  (bottomSheetDialogAddLocation == null || !bottomSheetDialogAddLocation!!.isShowing) &&
                        //  (bottomSheetEndOfSearchDialog == null || !bottomSheetEndOfSearchDialog!!.isShowing)) {
            //
            //                    }
            }
            pauseVideoByPos(position - 1)
            pauseVideoByPos(position + 1)
            if (discoverList.size > 0) {
//                if (discoverVO != null && getSaveLayout() != null) {
//                    if (discoverVO.saved) {
//                        getSaveLayout()?.setImageResource(R.drawable.ic_save_select)
//                    } else {
//                        getSaveLayout()?.setImageResource(R.drawable.ic_save_idle)
//                    }
//                }
//                if (getLinkLayout() != null) {
//                    if (!TextUtils.isEmpty(exploreViewModel.link)) {
//                        getLinkLayout()?.visibility = View.VISIBLE
//                        Handler().postDelayed({ this.showLinkToolTip() }, 300)
//                    } else {
//                        getLinkLayout()?.visibility = GONE
//                    }
//                }
//                if (getRepostLayout() != null) {
//                    if (repostCounter % 5 == 0) {
//                        Handler().postDelayed({ this.showRepostToolTip() }, 300)
//                    }
//                    repostCounter++
//                }
//
//                if (conversationModel != null && getSubscribeLayout() != null) {
//                    if (conversationModel.isSubscriber()) {
//                        getSubscribeLayout().setCustomIcon(R.drawable.ic_rt_subcribed);
//                    } else {
//                        getSubscribeLayout().setCustomIcon(R.drawable.ic_rt_subscribe);
//                    }
//                }
            }
            previousPos = position
            if (Utility.isNetworkAvailable(this)) {
                if (!FeedViewModel.getInstance().isEndOfSearch) {
                    if (TextUtils.isEmpty(FeedViewModel.getInstance().searchText)) {
                        if (feedMenuPosition == FeedViewType.FOR_YOU.value && FeedViewModel.getInstance().statusForYou !== FeedViewModel.APIStatus.IN_PROGRESS && position >= discoverList.size - 5) {
                            val isFeedExpired =
                                FeedViewModel.getInstance().expireMillis * 1000 < System.currentTimeMillis()
                            if (isFeedExpired) {
                                managementForRefreshFeed()
                            } else {
                                discoverVideos(false, false)
                            }
                        } else if (feedMenuPosition == FeedViewType.SUBSCRIPTIONS.value && FeedViewModel.getInstance().statusForSubscriptions !== FeedViewModel.APIStatus.IN_PROGRESS && position >= discoverList.size - 5) {
                            if (!isEndOfFeedResultCalled) {
                                discoverVideos(false, false)
                            }
                        } else if (feedMenuPosition == FeedViewType.MY_LOOPS.value && FeedViewModel.getInstance().statusForMyLoops !== FeedViewModel.APIStatus.IN_PROGRESS && position >= discoverList.size - 5) {
                            if (!isEndOfFeedResultCalled) {
                                discoverVideos(false, false)
                            }
                        } else if (feedMenuPosition == FeedViewType.COMMUNITY.value && FeedViewModel.getInstance().statusForCommunityVideos !== FeedViewModel.APIStatus.IN_PROGRESS && position >= discoverList.size - 5) {
                            if (!isEndOfCommunityFeed) {
                                discoverVideos(false, false)
                            }
                        }
                    } else {
                        if (FeedViewModel.getInstance().statusForYou !== FeedViewModel.APIStatus.IN_PROGRESS && position >= discoverList.size - 5) {
                            val isSearchFeedExpired =
                                FeedViewModel.getInstance().expireSearchMillis * 1000 < System.currentTimeMillis()
                            if (isSearchFeedExpired) {
                                managementForRefreshFeed()
                            } else {
                                discoverVideos(false, false)
                            }
                        }
                    }
                }
            }
            swipeCounter++
            if (swipeCounter == Constants.SWIPES_TO_COMPLETE_PROFILE_PROMPT) {
                pauseCurrentVideo(false)
                //TODO: Need to check this as no redirection is there
                //openCompleteProfileDrawerIfNeeded()
                swipeCounter = 0
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun managementForRefreshFeed() {
        FeedViewModel.getInstance().managementForRefreshFeed()
        if (discoverList.size > 10) {
            val endPos = discoverList.size - 10
            val previousTextureVideoView = getTextureVideoView(previousPos)
            if (previousTextureVideoView.player != null) {
                previousSeekPos = previousTextureVideoView.player!!.currentPosition
            }
            isFeedRefresh = true
            previousPos = previousPos - endPos
            if (previousPos < 0) {
                previousPos = 0
            }
            discoverList.removeAll(discoverList.subList(0, endPos).toSet())
            if (feedViewAdapter != null) {
                feedViewAdapter!!.notifyItemRangeRemoved(0, endPos)
                feedViewAdapter!!.notifyItemRangeChanged(0, discoverList.size)
            }
        }
        discoverVideos(false, false)
    }

    private fun pauseVideoByPos(pos: Int) {
        val textureVideoView = getTextureVideoView(pos)
        if (textureVideoView.player != null) {
            textureVideoView.player!!.playWhenReady = false
            //            ExploreViewModel exploreViewModel = discoverList.get(pos);
//            if (exploreViewModel.type == ExploreVideoType.RT) {
//                textureVideoView.getPlayer().seekTo(0L);
//            }
            collapseDescLayout(pos)
        }
    }
    private fun collapseDescLayout(pos: Int) {
        val descLayout: TextView = getDescLayout(pos)
        val singleDescLayout: TextView = getSingleDescLayout(pos)
        val overlayLayout: RelativeLayout = getOverlayLayout(pos)
        overlayLayout.visibility = GONE
        descLayout.visibility = GONE
        singleDescLayout.visibility = View.VISIBLE
    }

//    private fun showRepostToolTip() {
//        if (isEndOfFeed || discoverList.size == 0) {
//            return
//        }
////        if (!isSameTab() || getRepostLayout() == null || getRepostLayout()!!.visibility == GONE || context.getCurrentIndex() !== 0 || getChildFragmentManager().getBackStackEntryCount() > 0) {
////            return
////        }
//        if (customRepostTooltip != null && customRepostTooltip!!.isShowing) {
//            customRepostTooltip!!.dismiss()
//        }
//        if (repostHandler != null) {
//            repostHandler!!.removeCallbacksAndMessages(null)
//        }
//        customRepostTooltip = SimpleTooltip.Builder(this)
//            .anchorView(getRepostLayout())
//            .text(resources.getString(R.string.repost_tooltip))
//            .gravity(Gravity.START)
//            .animated(false)
//            .dismissOnOutsideTouch(true)
//            .dismissOnInsideTouch(true)
//            .arrowHeight(Utility.dpToPx(8f, this))
//            .arrowWidth(Utility.dpToPx(10f, this))
//            .margin(0f)
//            .isCustomTextAppearance(true)
//            .ignoreOverlay(true)
//            .build()
//        customRepostTooltip!!.show()
//        repostHandler = Handler(Looper.getMainLooper())
//        repostHandler!!.postDelayed({
//            if (!isFinishing) {
//                if (customRepostTooltip!!.isShowing) {
//                    customRepostTooltip!!.dismiss()
//                }
//            }
//        }, 2000)
//    }


    private fun getSaveLayout(): ImageView? {
        return feedViewPager.findViewWithTag(Constants.SAVE_VIDEO_LL + feedViewPager.currentItem)
    }

    fun getDescLayout(): TextView? {
        return feedViewPager.findViewWithTag(Constants.TV_DESC + feedViewPager.currentItem)
    }

    private fun getDescLayout(pos: Int): TextView {
        return feedViewPager.findViewWithTag(Constants.TV_DESC + pos)
    }

    private fun getMuteIconIv(): ImageView? {
        return feedViewPager.findViewWithTag(Constants.IV_MUTE + feedViewPager.currentItem)
    }

    fun getOverlayLayout(): RelativeLayout? {
        return feedViewPager.findViewWithTag(Constants.OVERLAY + feedViewPager.currentItem)
    }

    private fun getRepostLayout(): ImageView? {
        return feedViewPager.findViewWithTag(Constants.LL_REPOST + feedViewPager.currentItem)
    }

    private fun getOverlayLayout(pos: Int): RelativeLayout {
        return feedViewPager.findViewWithTag(Constants.OVERLAY + pos)
    }
    override fun onSuccessfullyDownloadVideo() {
        Utility.printErrorLog("download successfully")
        Utility.showToast(
            this,
            resources.getString(R.string.video_save_to_gallery)
        )
    }

    override fun onDownloadVideoFailure(code: Int) {
        if (code == -101) {
            Utility.showToast(
                this,
                resources.getString(R.string.no_internet_try_again)
            )
        } else {
            Utility.showToast(this, resources.getString(R.string.video_save_failed))
        }
    }

    override fun onButtonClick() {
        if (!isButtonClickedOnce) {
            isButtonClickedOnce = true
        }
        feedViewPager.setScrollConfig(DSVScrollConfig.DISABLED)
        isVerticalScrollEnabled = false
        start5SecTimer()
    }

    override fun onItemScroll() {
        if (isButtonClickedOnce) {
            start5SecTimer()
        }
    }

    override fun onApiComplete(isSuccess: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            FeedViewModel.getInstance().masterMySubscriptionsArr.clear()
            isRefreshSubscribedVideoRequestComplete = false
            FeedViewModel.getInstance().feedForMySubscriptionsVideos(this, false)
        }, 1000)
    }

    override fun onGoToFeedClicked() {
        try {
            feedMenuClickListener?.onClick(llForYou)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun start5SecTimer() {
        val timerDuration = (1000 * 5).toLong()
        //milliseconds
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(timerDuration, 1000) {
            override fun onTick(l: Long) {}
            override fun onFinish() {
                if (feedViewPager.findViewWithTag<View>(Constants.LL_LOADER + feedViewPager.currentItem) != null) {
                    feedViewPager.findViewWithTag<View>(Constants.LL_LOADER + feedViewPager.currentItem).visibility =
                        View.VISIBLE
                }
                if (isRefreshSubscribedVideoRequestComplete) {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            feedViewPager.setScrollConfig(DSVScrollConfig.ENABLED)
                            isVerticalScrollEnabled = true
                            feedViewAdapter = null
                            discoverList.clear()
                            setLoadedFeedData(tempDiscoverVOArrayList, true)
                            isEndOfFeedResultCalled = false
                            isButtonClickedOnce = false
                            isRefreshSubscribedVideoRequestComplete = false
                            isLoaderSyncWithApiResponse = false
                        }, 500
                    )
                } else {
                    //Sync loader with api response when time for subscribe request +
                    //time for discover request exceeds 5s.
                    isLoaderSyncWithApiResponse = true
                }
            }
        }
        countDownTimer?.start()
    }

    override fun onAudioFocusChange(i: Int) {
            if (i == AudioManager.AUDIOFOCUS_LOSS) {
                audioMuteUnMuteInterface1?.muteViaFocusLoss()
                CurrentAudioStatus = FeedAudioStatus.MUTED_BY_AUDIO_FOCUS
            } else if (i == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
                audioMuteUnMuteInterface1?.unMuteSound()
            }
        }

    private fun switchFeed(targetFeed: Int, community: CommunityModel?) {
        feedProgressBar.visibility = GONE
        llNoCommunityLoops?.visibility = GONE
        feedMenuPosition = targetFeed
        FeedViewModel.getInstance().clearFeedDataListeners()
        if (targetFeed == FeedViewType.COMMUNITY.value) {
            if (community != null) {
                tvFeedType!!.text = community.handle
                llFeedSelectCommunityDp!!.visibility = View.VISIBLE
                if (!TextUtils.isEmpty(community.dp)) {
                    llFeedSelectCommunityDp!!.setDpWithImage(
                        this,
                        false,
                        community.dp,
                        community.dp,
                        false
                    )
                } else {
                    llFeedSelectCommunityDp!!.setDpWithInitials(
                        community.name,
                        community.colorCode,
                        community.textColorCode
                    )
                }
                FeedViewModel.getInstance().setFeedCommunityVideoListener(this)
                if (FeedViewModel.getInstance().currentlyLoadedCommunity != community.communityId) {
                    FeedViewModel.getInstance().masterCommunityVideosArr.clear()
                    FeedViewModel.getInstance().statusForCommunityVideos =
                        FeedViewModel.APIStatus.NONE
                } else {
                    if (FeedViewModel.getInstance().masterCommunityVideosArr.isEmpty()) {
                        FeedViewModel.getInstance().statusForCommunityVideos =
                            FeedViewModel.APIStatus.NONE
                    }
                }
            }
        } else if (targetFeed == FeedViewType.FOR_YOU.value) {
            tvFeedType!!.text = resources.getText(R.string.for_you_small_y)
            llFeedSelectCommunityDp!!.visibility = GONE
            FeedViewModel.getInstance().setFeedForYouListener(this)
        } else if (targetFeed == FeedViewType.SUBSCRIPTIONS.value) {
            tvFeedType!!.text = resources.getText(R.string.subscriptions)
            llFeedSelectCommunityDp!!.visibility = GONE
            FeedViewModel.getInstance().setFeedSubscriberListener(this)
        } else if (targetFeed == FeedViewType.MY_LOOPS.value) {
            tvFeedType!!.text = resources.getText(R.string.my_loops)
            llFeedSelectCommunityDp!!.visibility = GONE
            FeedViewModel.getInstance().setFeedMyLoopListener(this)
        }

        // Reset flags related to end-of-feed
        isEndOfCommunityFeed = false
        //isEndOfFeed = false;
        feedViewPager.setScrollConfig(DSVScrollConfig.ENABLED)
        isVerticalScrollEnabled = true
        setLoadedFeedData(tempDiscoverVOArrayList, true)
        isEndOfFeedResultCalled = false
        isButtonClickedOnce = false
        isRefreshSubscribedVideoRequestComplete = false
        isLoaderSyncWithApiResponse = false
        clearAdapter()
        discoverList.addAll(FeedViewModel.getInstance().getMasterDiscoverArr(feedMenuPosition))
        setAdapter(0)
        refreshDataIfNeeded()
    }

    private fun clearAdapter() {
        feedViewAdapter = null
        discoverList.clear()
    }

    @Suppress("InvalidSetHasFixedSize")
    private fun setLoadedFeedData(
        discoverVOArrayList: ArrayList<ExploreViewModel<Any>>?,
        isNewDiscover: Boolean
    ) {
        if (isNewDiscover) {
            //TODO: need to check
            //this.setRefreshHome(false)
            FeedViewModel.getInstance().deepLinkVideoId = ""
            FeedViewModel.getInstance().lastPos = 0
            if (TextUtils.isEmpty(FeedViewModel.getInstance().searchText) && !FeedViewModel.getInstance().isDataDogLogged) {
                FeedViewModel.getInstance().isDataDogLogged = true
//                Utility.sendDataDogLatencyLogs(
//                    Constants.HOME_SCREEN_LOADED,
//                    FeedViewModel.getInstance().startLaunchMillis
//                )
            }
            feedViewAdapter = null
            discoverList.clear()
        }
        val position = discoverList.size
        if (discoverVOArrayList != null) {
            discoverList.addAll(discoverVOArrayList)
        }
        setAdapter(position)
    }

    @SuppressLint("ClickableViewAccessibility")
    @Suppress("InvalidSetHasFixedSize")
    private fun setAdapter(position: Int) {
        if (feedViewAdapter == null) {
            Utility.showLog("Adapter", "FeedAdapter")
            feedViewAdapter = FeedRTAdapter(this, this, discoverList)

            feedViewAdapter!!.setInterfaceListener(object : FeedAdapterListener {
                override fun onSparkClicked() {
                    manageSparkUnSpark(false)
                }
                override fun onProfileClick(pos: Int) {
                    pauseCurrentVideo(false)
                    //TODO: Need to confirm and open activity/Fragment
//                    val fragmentManager: FragmentManager = getChildFragmentManager()
//                    val fragmentTransaction = fragmentManager.beginTransaction()
//                        .setCustomAnimations(
//                            R.anim.slide_in_right,
//                            R.anim.slide_out_left,
//                            R.anim.slide_in_left,
//                            R.anim.slide_out_right
//                        )
//                    val fragment: NewProfileFragment = NewProfileFragment.Companion.newInstance(
//                        false,
//                        discoverList[pos].userId,
//                        context.mainPager.getCurrentItem()
//                    )
//                    val tag2 = "profile"
//                    fragmentTransaction.add(R.id.profile_fragment_container, fragment, tag2)
//                    fragmentTransaction.addToBackStack(tag2)
//                    fragmentTransaction.commit()
                }

                override fun onVolumeClick() {
                    if (CurrentAudioStatus === FeedAudioStatus.UNMUTED) {
                        CurrentAudioStatus = FeedAudioStatus.MUTED
                        muteAudio(false)
                    } else {
                        CurrentAudioStatus = FeedAudioStatus.UNMUTED
                        unMuteAudio()
                    }
                }

                override fun onReplyClick() {
                    replyClickManage()
                }

                override fun onSaveClick() {
                    saveClickManage()
                }

                override fun onShareClick() {
                    shareClickManage()
                }

                override fun onLinkClick() {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return
                    }
                    mLastClickTime = SystemClock.elapsedRealtime()
                    if (discoverList.size == 0) {
                        return
                    }
                    //TODO: Need to ask and add condition
                    //context.isLoggedIn()
                    if (discoverList[feedViewPager.currentItem].type === ExploreVideoType.PUBLIC_VIDEO) {
                        callClickCountApi()
                    }
                    if (Utility.isNetworkAvailable(this@HomeScreen)) {
//                        val intent = Intent(, WebViewActivity::class.java)
//                        intent.putExtra("url", discoverList[feedViewPager.currentItem].link)
//                        startActivity(intent)
//                        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
                    } else {
                        Utility.showToast(
                            this@HomeScreen,
                            this@HomeScreen.resources.getString(R.string.no_internet)
                        )
                    }
                }

                override fun onEditClicked() {}
                override fun onMoreOptionsClicked() {
                    pauseCurrentVideo(false)
                    //TODO: Need to add
                    //openBottomSheetDialog()
                }

                override fun onDownloadVideoClick(
                    position: Int,
                    model: DiscoverModel?,
                    downloadVideoTask: DownloadVideo
                ) {
                    //downloadVideoTask = downloadVideoTask
                    downloadVideoTask.setDownloadListener(this@HomeScreen)
                    if (!downloadVideoTask.isPermissionGranted) {
                        ActivityCompat.requestPermissions(
                            this@HomeScreen,
                            arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            Constants.WRITE_STORAGE_PERMISSION
                        )
                    } else {
                        downloadVideoTask.saveDownloadedVideoToGallery(this@HomeScreen)
                    }
                }

                override fun onCoverPhotoClick(position: Int) {}
                override fun onDetailsClicked() {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return
                    }
                    mLastClickTime = SystemClock.elapsedRealtime()
                    val pos = feedViewPager.currentItem
                    //TODO: Need to add FeedLoopFragment
//                    val exploreViewModel: ExploreViewModel = discoverList[pos]
//                    if (exploreViewModel.type === ExploreVideoType.RT) {
//                        val conversationModel: ConversationModel =
//                            exploreViewModel.getObj() as ConversationModel
//                        pauseCurrentVideo(false)
//                        val fragmentManager: FragmentManager = getChildFragmentManager()
//                        val fragmentTransaction = fragmentManager.beginTransaction()
//                        fragmentTransaction.setCustomAnimations(
//                            R.anim.slide_in_right,
//                            R.anim.slide_out_left,
//                            R.anim.slide_in_left,
//                            R.anim.slide_out_right
//                        )
//                        val fragment: FeedLoopFragment = FeedLoopFragment.newInstance(
//                            discoverList[pos].feedId,
//                            discoverList[pos].convId,
//                            mainPager.getCurrentItem(),
//                            conversationModel.getGroup(),
//                            conversationModel.getSettings(),
//                            conversationModel.getShareURL(),
//                            conversationModel.isSubscriber(),
//                            conversationModel.getMemberInfo(),
//                            "",
//                            true
//                        )
//                        val tag = "feedloop"
//                        fragmentTransaction.add(R.id.profile_fragment_container, fragment, tag)
//                        fragmentTransaction.addToBackStack(tag)
//                        fragmentTransaction.commit()
//                    }
                }

                override fun onCommentsClicked() {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return
                    }
                    mLastClickTime = SystemClock.elapsedRealtime()
                    //TODO: Need to check issue with ExploreViewModel
//                    val exploreViewModel: ExploreViewModel =
//                        discoverList[feedViewPager.currentItem]
//                    if (exploreViewModel.type === ExploreVideoType.RT) {
//                        val conversationModel: ConversationModel =
//                            exploreViewModel.getObj() as ConversationModel
//                        if (conversationModel.chats.size > 0) {
//                            val chatModel: ChatModel = conversationModel.getChats()
//                                .get(conversationModel.chats.size - 1)
//                            val messageModel: MessageModel = Utility.convertChatIntoMessage(
//                                chatModel,
//                                conversationModel.getChatId()
//                            )
//                            val i = Intent(this@HomeScreen, CommentsNewActivity::class.java)
//                            i.putExtra("chat_id", conversationModel.getChatId())
//                            i.putExtra("video_id", messageModel.getMessageId())
//                            i.putExtra("message_model", messageModel)
//                            i.putExtra(
//                                "loop_name",
//                                if (conversationModel.group != null) conversationModel.group
//                                    .name else ""
//                            )
//                            i.putExtra(
//                                "isOwner",
//                                conversationModel.memberInfo != null && conversationModel.memberInfo
//                                    .role!!.contentEquals("1")
//                            )
//                            startActivity(i)
//                            overridePendingTransition(
//                                R.anim.slide_in_right,
//                                R.anim.slide_out_left
//                            )
//                        }
                    //}
                }

                override fun onSubscribeClicked() {
                    subscribeClickManage()
                }

                override fun onDescriptionClicked() {
                    if (discoverList == null || discoverList.size == 0) {
                        return
                    }
                    val singleDescLayout: TextView = getSingleDescLayout()
//                    if (singleDescLayout.visibility == View.VISIBLE) {
//                        expandCollapseDescLayout(true)
//                    } else {
//                        expandCollapseDescLayout(false)
//                    }
                }

                override fun onOverlayClicked() {
                    if (discoverList == null || discoverList.size == 0) {
                        return
                    }
                    //expandCollapseDescLayout(false)
                }

                override fun onUnlistedClicked() {
                    val exploreViewModel: ExploreViewModel<Any> =
                        discoverList[feedViewPager.currentItem]
//                    if (exploreViewModel.type === ExploreVideoType.RT) {
//                        if (isOwner(exploreViewModel)) {
                    //TODO: Need to add
//                            openBottomSheetDialogForPrivacyOptions(feedViewPager.currentItem)
//                        } else {
//                            openBottomSheetDialogForSingleOption(exploreViewModel)
//                        }
//                    } else {
//                        openBottomSheetDialogForPrivacyOptions(feedViewPager.currentItem)
//                    }
                }

                override fun onFlagVideoClicked() {
                    flagVideoManagement()
                }

                override fun onRepostClicked() {
                    repostVideoManagement()
                }

                override fun onRepostOwnerClicked(pos: Int) {
                    pauseCurrentVideo(false)
                    //TODO: Need to check and add Profile Fragment
//                    val fragmentManager: FragmentManager = getChildFragmentManager()
//                    val fragmentTransaction = fragmentManager.beginTransaction()
//                        .setCustomAnimations(
//                            R.anim.slide_in_right,
//                            R.anim.slide_out_left,
//                            R.anim.slide_in_left,
//                            R.anim.slide_out_right
//                        )
//                    val fragment: NewProfileFragment = NewProfileFragment.Companion.newInstance(
//                        false,
//                        discoverList[pos].repostOwnerId,
//                        context.mainPager.getCurrentItem()
//                    )
//                    val tag2 = "profile"
//                    fragmentTransaction.add(R.id.profile_fragment_container, fragment, tag2)
//                    fragmentTransaction.addToBackStack(tag2)
//                    fragmentTransaction.commit()
                }

                override fun onParticipateClicked() {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return
                    }
                    mLastClickTime = SystemClock.elapsedRealtime()
                    if (discoverList == null || discoverList.size == 0) {
                        return
                    }
                    //TODO: Need to check and add CameraScreen for reply Fragment
//                    val exploreViewModel: ExploreViewModel =
//                        discoverList[feedViewPager.currentItem]
//                    if (exploreViewModel.type === ExploreVideoType.RT) {
//                        val conversationModel: ConversationModel =
//                            exploreViewModel.getObj() as ConversationModel
//                        Utility.goToCameraForReply(
//                            context,
//                            conversationModel.getChatId(),
//                            VideoConvType.ROUND_TABLE.getValue(),
//                            conversationModel.getGroup(),
//                            false,
//                            conversationModel.getSettings()
//                        )
//                    }
                }

                override fun onAskQuestionClicked() {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return
                    }
                    mLastClickTime = SystemClock.elapsedRealtime()
                    if (discoverList == null || discoverList.size == 0) {
                        return
                    }
                    //TODO: Need to check and add LoopQuestionAnswerActivity
//                    val exploreViewModel: ExploreViewModel =
//                        discoverList[feedViewPager.currentItem]
//                    if (exploreViewModel.type === ExploreVideoType.RT) {
//                        val conversationModel: ConversationModel =
//                            exploreViewModel.getObj() as ConversationModel
//                        val qnaIntent = Intent(this, LoopQuestionAnswerActivity::class.java)
//                        qnaIntent.putExtra("chatId", conversationModel.getChatId())
//                        startActivity(qnaIntent)
//                        overridePendingTransition(
//                            R.anim.slide_in_right,
//                            R.anim.slide_out_left
//                        )
//                    }
                }
            })
            feedViewPager.adapter = feedViewAdapter
            feedViewPager.setHasFixedSize(true)
            feedViewPager.removeScrollStateChangeListener(this)
            feedViewPager.addScrollStateChangeListener(this)
            feedViewPager.removeItemChangedListener(this)
            feedViewPager.addOnItemChangedListener(this)
            feedViewPager.setOffscreenItems(1)
            feedViewPager.setItemTransitionTimeMillis(150)
            feedViewPager.isNestedScrollingEnabled = false
            feedViewPager.setOnTouchListener { view, motionEvent ->
                val isTouchHandled: Boolean = manageTouchEvent(motionEvent)
                handler = Handler(Looper.getMainLooper())
                runnable = Runnable {
                    if (isSingleEvent) {
                        if (CurrentAudioStatus === FeedAudioStatus.MUTED || CurrentAudioStatus === FeedAudioStatus.MUTED_BY_AUDIO_FOCUS) {
                            unMuteAudio()
                        } else {
                            muteAudio(false)
                        }
                    }
                }
                isTouchHandled
            }
            if (FeedViewModel.getInstance().isSearch) {
                feedViewPager.scrollToPosition(FeedViewModel.getInstance().searchLastPos)
            } else {
                feedViewPager.scrollToPosition(FeedViewModel.getInstance().lastPos)
            }
        } else {
            feedViewAdapter!!.notifyItemRangeInserted(position, discoverList.size - 1)
            feedViewPager.post {
                if (isFeedRefresh) {
                    val previousTextureVideoView = getTextureVideoView(previousPos)
                    if (previousTextureVideoView.player != null) {
                        //if (context.getCurrentIndex() === 0 && isSameTab() && context.shouldVideoPlay()) {
                            previousTextureVideoView.player!!.playWhenReady = true
                            previousTextureVideoView.player!!.seekTo(previousSeekPos)
                        //}
                    }
                    previousSeekPos = 0
                    isFeedRefresh = false
                }
            }
        }
        //TODO: search click is not required
//        if (context.isFromSearch) {
//            context.isFromSearch = false
//            context.onBackPressed()
//        }
    }

    private fun repostVideoManagement() {
//        if (!SharedPrefUtils.getBoolPreference(context, Constants.PREF_IS_SHOWN_INTRO_REPOST)) {
//            SharedPrefUtils.setBoolPreference(context, Constants.PREF_IS_SHOWN_INTRO_REPOST, true)
            tutorialRepost.visibility = View.VISIBLE
            pauseCurrentVideo(false)
//        } else {
//            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
//                return
//            }
//            mLastClickTime = SystemClock.elapsedRealtime()
//            val exploreViewModel: ExploreViewModel = discoverList[feedViewPager.currentItem]
//            val i = Intent(context, RepostActivity::class.java)
//            i.putExtra("sourceVideoId", exploreViewModel.getConvId())
//            i.putExtra("sourceParentId", exploreViewModel.getFeedId())
//            i.putExtra("sourceThumbnail", exploreViewModel.getFeedThumbnail())
//            i.putExtra(
//                "sourceContentType",
//                if (exploreViewModel.type === ExploreVideoType.PUBLIC_VIDEO) 1 else 2
//            )
//            context.startActivity(i)
//            context.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
//        }
    }

    fun shareClickManage() {
        if (discoverList == null || discoverList.size == 0) {
            return
        }
        if (discoverList[feedViewPager.currentItem].type === ExploreVideoType.PUBLIC_VIDEO) {
            callShareCountApi()
        }
        val shareURL = discoverList[feedViewPager.currentItem].shareURL
        if (!TextUtils.isEmpty(shareURL)) {
            var from = ""
            val exploreViewModel: ExploreViewModel<Any> = discoverList[feedViewPager.currentItem]
            if (exploreViewModel.type === ExploreVideoType.PUBLIC_VIDEO) {
                from = Constants.FROM_PUBLIC_VIDEO
            } else if (exploreViewModel.type === ExploreVideoType.RT) {
                from = Constants.FROM_ROUND_TABLE
            }
            Utility.shareVideoLink(
                this,
                shareURL,
                discoverList[feedViewPager.currentItem].feedId,
                from
            )
        }

//        String text = "<!DOCTYPE html><html><body>" +
//                "<h1 style=\"background-color:powderblue;\">This is a heading</h1>" +
//                "<p style=\"background-color:tomato;\">This is a paragraph.</p>" +
//                "</body></html>";
//        final Intent shareIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
////        shareIntent.setType("message/rfc822");
//        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "The Subject");
//        shareIntent.putExtra(
//                Intent.EXTRA_TEXT,
//                Html.fromHtml(text)
//        );
//        startActivity(shareIntent);

//        Intent sendIntent = new Intent();
//        sendIntent.setAction(Intent.ACTION_SEND);
//        sendIntent.putExtra(Intent.EXTRA_SUBJECT,getResources().getString(R.string.email_subject));
//        sendIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getResources().getString(R.string.email_text)));
//        sendIntent.setType("text/plain");
//        startActivity(Intent.createChooser(sendIntent, "email"));
    }

    fun getSingleDescLayout(): TextView {
        return feedViewPager.findViewWithTag(Constants.TV_DESC_SINGLE + feedViewPager.currentItem)
    }

    private fun getSingleDescLayout(pos: Int): TextView {
        return feedViewPager.findViewWithTag(Constants.TV_DESC_SINGLE + pos)
    }
    private fun callShareCountApi() {
        try {
            module = Constants.SHARE_COUNT
            val jsonObject = JSONObject()
            BaseAPIService(
                this,
                module + currentVideoId,
                getRequestBody(jsonObject.toString()),
                true,
                null,
                "POST",
                false
            )
        } catch (e: java.lang.Exception) {
            showLogException(e)
        }
    }
    private fun callClickCountApi() {
        try {
            module = Constants.CLICK_COUNT
            val jsonObject = JSONObject()
            BaseAPIService(
                this@HomeScreen,
                module + currentVideoId,
                Utility.getRequestBody(jsonObject.toString()),
                true,
                object : ResponseListener {
                    override fun onSuccess(response: String?) {}
                    override fun onFailure(error: String?) {}
                },
                "POST",
                false
            )
        } catch (e: java.lang.Exception) {
            Utility.showLogException(e)
        }
    }
    private fun replyClickManage() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        if (Utility.isNetworkAvailable(this)) {
            val isSearched = !TextUtils.isEmpty(FeedViewModel.getInstance().searchText)
//            if (context.isLoggedIn()) {
//                try {
//                    if (discoverList.size > 0) {
//                        GenuInApplication.getInstance().createDataDogLogsReplyTracing(
//                            Constants.REPLY_CLICKED,
//                            discoverList[feedViewPager.currentItem].userId,
//                            if (isSearched) "search" else "feed",
//                            1
//                        )
//                    }
//                } catch (e: java.lang.Exception) {
//                    Utility.showLogException(e)
//                }
//                callCheckForReaction()
//            } else {
//                if (discoverList.size > 0) {
//                    val exploreViewModel: ExploreViewModel =
//                        discoverList[feedViewPager.currentItem]
//                    if (exploreViewModel.type === ExploreVideoType.PUBLIC_VIDEO) {
//                        discoverModel = exploreViewModel.getObj()
//                        goToLoginActivity(Constants.FROM_REACTION)
//                    }
//                }
//            }
        } else {
            //Utility.showToast(context, context.getResources().getString(R.string.no_internet))
        }
    }

    private fun subscribeClickManage() {
        if (discoverList == null || discoverList.size == 0) {
            return
        }
        val exploreViewModel: ExploreViewModel<Any> = discoverList[feedViewPager.currentItem]
        if (exploreViewModel.type === ExploreVideoType.RT) {
            val conversationModel = exploreViewModel.getObj() as ConversationModel
            chatId = conversationModel.chatId
           // if (context.isLoggedIn()) {
                subscribeRT(conversationModel)
//            } else {
//                goToLoginActivity(Constants.FROM_SUBSCRIBE)
//            }
        }
    }

    private fun flagVideoManagement() {
        val exploreViewModel: ExploreViewModel<Any> = discoverList[feedViewPager.currentItem]
        if (exploreViewModel.type === ExploreVideoType.PUBLIC_VIDEO) {
           // openBottomSheetDialogForFlagVideo(exploreViewModel.getFeedId())
        }
    }

    private fun subscribeRT(conversationModel: ConversationModel) {
        try {
            if (conversationModel.memberInfo != null || conversationModel.isSubscriber) {
                return
            }
            Utility.showCustomSubscribeToast(this)
//            sendDataDogLogs(
//                Constants.RT_SUBSCRIBE_CLICKED,
//                Properties(),
//                feedViewPager.currentItem
//            )
            conversationModel.isSubscriber = true
            /*if (getSubscribeLayout() != null) {
                getSubscribeLayout().setVisibility(View.GONE);
            }
            if (getAddQuestionLayout() != null) {
                getAddQuestionLayout().setVisibility(View.VISIBLE);
            }*/if (getAddQuestionLayout() != null && getSubscribeLayout() != null) {
                val tv: CustomTextView? =
                    getSubscribeLayout()?.findViewById(R.id.tvSubscribeSubscribed)
                tv?.setText(R.string.subscribed)
                getSubscribeLayout()?.setBackgroundResource(R.drawable.view_group_unsubscribe)
                FlipAnimator.verticallyFlipView(
                    this,
                    getAddQuestionLayout(),
                    getSubscribeLayout()
                )
            }
            if (getAddQuestionTextView() != null) {
                getAddQuestionTextView()?.text = resources.getString(R.string.ask_question)
            }
            val jsonObject = JSONObject()
            jsonObject.put("subscribe", true)
            jsonObject.put("chat_id", conversationModel.chatId)
            BaseAPIService(
                this,
                Constants.SUBSCRIBE_RT,
                getRequestBody(jsonObject.toString()),
                true,
                object : ResponseListener {
                    override fun onSuccess(response: String?) {
                       // Utility.logSubscribeUnsubscribe(conversationModel.chatId, true)
                    }

                    override fun onFailure(error: String?) {}
                },
                "POST",
                false
            )
        } catch (e: java.lang.Exception) {
            showLogException(e)
        }
    }

    private fun saveClickManage() {
        if (discoverList == null || discoverList.size == 0) {
            return
        }
        val exploreViewModel: ExploreViewModel<Any> = discoverList[feedViewPager.currentItem]
        if (exploreViewModel.type === ExploreVideoType.PUBLIC_VIDEO) {
            val discover = exploreViewModel.obj as DiscoverModel
            videoId = discover.videoId
            //if (context.isLoggedIn()) {
                if (!discover.saved) {
                    Utility.showCustomToast(this, getString(R.string.saved_message))
                    getSaveLayout()!!.setImageResource(R.drawable.ic_save_select)
                    discover.saved = true
                    discover.saveVideo(this)
                } else {
                    getSaveLayout()!!.setImageResource(R.drawable.ic_save_idle)
                    discover.saved = false
                    discover.unSaveVideo(this)
                }
            //TODO :No eventbus is used
                //postSaveUnSaveEvent(discover.saved, exploreViewModel)
//            } else {
//                goToLoginActivity(Constants.FROM_SAVE)
//            }
        }
    }

    private fun getSparkView(): SparkView? {
        return feedViewPager.findViewWithTag(Constants.LL_SPARK + feedViewPager.currentItem)
    }

    private fun getSparksText(): TextView? {
        return feedViewPager.findViewWithTag(Constants.TV_SPARK + feedViewPager.currentItem)
    }

    private fun getSubscribeLayout(): CardView? {
        return feedViewPager.findViewWithTag(Constants.LL_SUBSCRIBE + feedViewPager.currentItem)
    }
    private fun getAddQuestionLayout(): MaterialCardView? {
        return feedViewPager.findViewWithTag(Constants.LL_ASK_QUESTION + feedViewPager.currentItem)
    }

    private fun getAddQuestionTextView(): TextView? {
        return feedViewPager.findViewWithTag(Constants.TV_ASK_QUESTION + feedViewPager.currentItem)
    }

    private fun manageSparkUnSpark(isDoubleTap: Boolean) {
        val pos = feedViewPager.currentItem
        val model: ExploreViewModel<Any> = discoverList[pos]
        if (model.type === ExploreVideoType.PUBLIC_VIDEO) {
            val discModel = model.obj as DiscoverModel
            if (discModel.isSparked && !isDoubleTap) {
                getSparkView()?.setUnSpark(true)
                discModel.setSparkStatus(false)
                //API Call
                SparkManager.Companion.spark(
                    this,
                    discModel,
                    discModel.convId,
                    SparkContentType.PUBLIC_VIDEO.value,
                    false
                )
            } else {
                getSparkView()?.setSpark(true)
                if (!discModel.isSparked) {
                    discModel.setSparkStatus(true)
                }
                //API Call
                SparkManager.Companion.spark(
                    this,
                    discModel,
                    discModel.convId,
                    SparkContentType.PUBLIC_VIDEO.value,
                    true
                )
            }
            if (!TextUtils.isEmpty(discModel.sparkCount)) {
                getSparksText()?.text = Utility.formatNumber(discModel.sparkCount.toLong())
            }
        } else if (model.type === ExploreVideoType.RT) {
            val convModel: ConversationModel = model.getObj() as ConversationModel
            if (convModel.chats.size > 0) {
                val chatModel: ChatModel = convModel.chats[convModel.getChats().size - 1]
                if (chatModel.isSparked && !isDoubleTap) {
                    getSparkView()?.setUnSpark(true)
                    chatModel.setSparkStatus(false)
                    //API Call
                    SparkManager.Companion.spark(
                        this,
                        chatModel,
                        chatModel.getConversationId(),
                        SparkContentType.CONVERSATION_VIDEO.value,
                        false
                    )
                } else {
                    getSparkView()?.setSpark(true)
                    if (!chatModel.isSparked) {
                        chatModel.setSparkStatus(true)
                    }
                    //API Call
                    SparkManager.Companion.spark(
                        this,
                        chatModel,
                        chatModel.conversationId,
                        SparkContentType.CONVERSATION_VIDEO.value,
                        true
                    )
                }
                if (!TextUtils.isEmpty(chatModel.sparkCount)) {
                    getSparksText()?.text = Utility.formatNumber(
                        chatModel.sparkCount.toLong()
                    )
                }
            }
        }
    }
    private val longPressHandler = Handler(Looper.getMainLooper())

    private var mLongPressed = Runnable {
        isLongPressed = true
        pauseCurrentVideo(false)
    }

    private fun manageTouchEvent(event: MotionEvent): Boolean {
        val MIN_DIS = 10
        when (event.action) {
            MotionEvent.ACTION_CANCEL -> {
                isLongPressed = false
                longPressHandler.removeCallbacks(mLongPressed)
                playCurrentVideo()
                return false
            }

            MotionEvent.ACTION_DOWN -> {
                initialYValue = event.y
                isLongPressed = false
                longPressHandler.postDelayed(mLongPressed, 500)
                return false
            }

            MotionEvent.ACTION_UP -> {
                val deltaY = event.y - initialYValue
                longPressHandler.removeCallbacks(mLongPressed)
                if (Math.abs(deltaY) > MIN_DIS) {
                    if (deltaY > MIN_DIS) {
                        /*if (feedViewPager.getCurrentItem() == 0 && deltaY > 200) {
                            if (includedLayout.getVisibility() != View.VISIBLE) {
                                goToSearchFragment(false);
                                sendSearchModuleOpened(Constants.KEY_SWIPE_TO_SEARCH);
                                return true;
                            }
                        } else {*/
                        if (deltaY > 200 && feedViewPager.currentItem === discoverList.size - 1 && isEndOfFeed) {
                            if (!isVerticalScrollEnabled) {
                                countDownTimer?.cancel()
                                feedViewPager.findViewWithTag<View>(Constants.LL_LOADER + feedViewPager.currentItem).visibility =
                                    View.VISIBLE
                                isLoaderSyncWithApiResponse = true
                            }
                        } else if (discoverList.size > 0) {
                            playCurrentVideo()
                        }
                    } else {
                        if (FeedViewModel.getInstance().isEndOfSearch && feedViewPager.currentItem === discoverList.size - 1 && deltaY < -200) {
                            openBottomSheetDialogForEndOfSearch()
                            hideAllIcons()
                            return true
                        } else {
                            if (discoverList.size > 0) {
                                playCurrentVideo()
                            }
                        }
                    }
                } else {
                    if (SystemClock.elapsedRealtime() - timestampLastClick < DEFAULT_QUALIFICATION_SPAN) {
                        isSingleEvent = false
                        handler!!.removeCallbacks(runnable!!)
                        if (discoverList.size > 0) {
                            val exploreViewModel: ExploreViewModel<Any> =
                                discoverList[feedViewPager.currentItem]
                            if (exploreViewModel.type === ExploreVideoType.RT || exploreViewModel.type === ExploreVideoType.PUBLIC_VIDEO) {
                                Utility.showMuteUnMuteToast(this@HomeScreen, R.drawable.ic_bulb_filled_feed)
                                manageSparkUnSpark(true)
                            }
                            /*ExploreViewModel exploreViewModel = discoverList.get(feedViewPager.getCurrentItem());
                            if (exploreViewModel.type == ExploreVideoType.PUBLIC_VIDEO) {
                                DiscoverModel discover = (DiscoverModel) exploreViewModel.getObj();
                                if (context.isLoggedIn()) {
                                    Utility.showCustomToast(context, getString(R.string.saved_message));
                                    sendDataDogLogs(Constants.VIDEO_SAVED, new Properties(), feedViewPager.getCurrentItem());
                                    getSaveLayout().setImageResource(R.drawable.ic_save_select);
                                    discover.setSaved(true);
                                    discover.saveVideo();
                                    postSaveUnSaveEvent(true, exploreViewModel);
                                } else {
                                    videoId = discover.getVideoId();
                                    goToLoginActivity(Constants.FROM_SAVE);
                                }
                            } else if (exploreViewModel.type == ExploreVideoType.RT) {
                                ConversationModel conversationModel = (ConversationModel) exploreViewModel.getObj();
                                if (context.isLoggedIn()) {
                                    subscribeRT(conversationModel);
                                } else {
                                    chatId = conversationModel.getChatId();
                                    goToLoginActivity(Constants.FROM_SUBSCRIBE);
                                }
                            }*/
                        }
                    } else if (isLongPressed) {
                        isLongPressed = false
                        handler!!.removeCallbacks(runnable!!)
                        playCurrentVideo()
                    } else {
                        isSingleEvent = true
                        timestampLastClick = SystemClock.elapsedRealtime()
                        handler!!.postDelayed(
                            runnable!!,
                            DEFAULT_QUALIFICATION_SPAN
                        )
                    }
                }
            }
        }
        return false
    }

    fun pauseCurrentVideo(isFragmentSwitch: Boolean) {
        if (!isFragmentSwitch) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioManager != null) {
                audioManager?.abandonAudioFocusRequest(audioFocusRequest!!)
            }
        }
        //Null check for feedViewPager added.
        if (feedViewPager != null) {
            Utility.showLog("Tag", "Video paused")
            val textureVideoView = getTextureVideoView(feedViewPager.currentItem)
            if (textureVideoView.player != null) {
                textureVideoView.player!!.playWhenReady = false
                Utility.showLog("Tag", "Video paused $feedMenuPosition")
            }
        }
    }

    fun unMuteAudio() {
        if (isEndOfFeed) {
            return
        }
        CurrentAudioStatus = FeedAudioStatus.UNMUTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager?.requestAudioFocus(audioFocusRequest!!) //onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        muteUnMuteManagement(true)
    }

    private fun muteAudio(isMuteViaFocusLoss: Boolean) {
        if (isEndOfFeed) {
            return
        }
        CurrentAudioStatus = if (isMuteViaFocusLoss) {
            FeedAudioStatus.MUTED_BY_AUDIO_FOCUS
        } else {
            FeedAudioStatus.MUTED
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager?.abandonAudioFocusRequest(audioFocusRequest!!)
        }
        muteUnMuteManagement(true)
    }

    private fun openBottomSheetDialogForEndOfSearch() {
        pauseCurrentVideo(false)
        val bottomSheetEndOfSearchView: View =
            layoutInflater.inflate(R.layout.bottom_sheet_end_of_search, null)
        val goBack = bottomSheetEndOfSearchView.findViewById<MaterialButton>(R.id.btnGoBackToFeed)
        val rlCreateVideo =
            bottomSheetEndOfSearchView.findViewById<RelativeLayout>(R.id.rlCreateVideo)
        goBack.setOnClickListener { v: View? ->
            bottomSheetEndOfSearchDialog!!.dismiss()
            //showAllIcons()
        }
        rlCreateVideo.setOnClickListener { v: View? ->
            bottomSheetEndOfSearchDialog!!.dismiss()
            //showAllIcons()
            //TODO: Currently no camera but still will ask
            //visitCameraActivity()
        }
        bottomSheetEndOfSearchDialog = BottomSheetDialog(this@HomeScreen, R.style.SheetDialog1)
        bottomSheetEndOfSearchDialog!!.setContentView(bottomSheetEndOfSearchView)
        bottomSheetEndOfSearchDialog!!.setCancelable(true)
        bottomSheetEndOfSearchDialog!!.setOnCancelListener { dialog: DialogInterface? ->
            //showAllIcons()
            playCurrentVideo()
        }
        bottomSheetEndOfSearchDialog!!.setOnShowListener { dialog: DialogInterface? ->
            val bottomSheet =
                bottomSheetEndOfSearchDialog!!.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior: BottomSheetBehavior<*> =
                BottomSheetBehavior.from(bottomSheet!!)
            behavior.skipCollapsed = true
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        bottomSheetEndOfSearchDialog!!.show()
    }

    private fun getBottomLayout(): LinearLayout? {
        return feedViewPager.findViewWithTag(Constants.LL_BOTTOM + feedViewPager.currentItem)
    }

    private fun hideAllIcons() {
        if (getBottomLayout() != null) {
            getBottomLayout()?.visibility = View.INVISIBLE
        }
        progressTimer!!.visibility = View.INVISIBLE
        //TODO: What is blur item and is it need to display
        //showBottomBar(false)
    }

    private fun showAllIcons() {
        if (getBottomLayout() != null) {
            getBottomLayout()?.visibility = View.VISIBLE
        }
        progressTimer!!.visibility = View.VISIBLE
        //TODO: What is blur item and is it need to display
        //showBottomBar(true)
    }

    private fun getLinkLayout(): ImageView? {
        return feedViewPager.findViewWithTag(Constants.LINK + feedViewPager.currentItem)
    }

    private fun resetInfinity() {
        rlCommunityInfinity.alpha = 1f
        rlShadowCommunityInfinity?.alpha = 0f
        rlCommunityInfinity.rotationX = 0f
        rlShadowCommunityInfinity?.rotationX = -180f
        rlLoopInfinity.alpha = 1f
        rlShadowLoopInfinity.alpha = 0f
        rlLoopInfinity.rotationX = 0f
        rlShadowLoopInfinity.rotationX = -180f
    }

//    private fun showLinkToolTip() {
//        if (isEndOfFeed || discoverList.size == 0) {
//            return
//        }
////        if (!isSameTab() || getLinkLayout() == null || getLinkLayout()!!.visibility == GONE || context.getCurrentIndex() !== 0 || getChildFragmentManager().getBackStackEntryCount() > 0) {
////            return
////        }
//        if (customLinkTooltip != null && customLinkTooltip!!.isShowing()) {
//            customLinkTooltip!!.dismiss()
//        }
//        if (linkHandler != null) {
//            linkHandler!!.removeCallbacksAndMessages(null)
//        }
//        customLinkTooltip = SimpleTooltip.Builder(this)
//            .anchorView(getLinkLayout())
//            .text(resources.getString(R.string.see_link))
//            .gravity(Gravity.START)
//            .animated(false)
//            .dismissOnOutsideTouch(true)
//            .dismissOnInsideTouch(true)
//            .arrowHeight(Utility.dpToPx(8f, this))
//            .arrowWidth(Utility.dpToPx(10f, this))
//            .margin(0f)
//            .ignoreOverlay(true)
//            .build()
//        customLinkTooltip!!.show()
//        linkHandler = Handler(Looper.getMainLooper())
//        linkHandler!!.postDelayed(Runnable {
//            if (!isFinishing) {
//                if (customLinkTooltip!!.isShowing) {
//                    customLinkTooltip!!.dismiss()
//                }
//            }
//        }, 2000)
//    }


    private fun setProgressForVideo() {
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (isPaused || discoverList.size == 0) {
                    return
                }
                runOnUiThread {
                    try {
                        val index = feedViewPager.currentItem
                        val thisTextureVideoView = getTextureVideoView(index)
                        if (thisTextureVideoView != null) {
                            val player = thisTextureVideoView.player
                            if (player != null && player.isPlaying) {
                                val totalDuration =
                                    (player.duration / 1000).toInt().toLong()
                                currentVideoPlayDuration = (player.currentPosition / 1000).toInt()
                                halfDuration = (player.duration / 2000).toInt()
                                progressTimer!!.max = player.duration.toInt()
                                progressTimer!!.progress = player.currentPosition.toInt()
                                val exploreViewModel: ExploreViewModel<Any> = discoverList[index]
                                if (exploreViewModel.type === ExploreVideoType.PUBLIC_VIDEO) {
                                    val discoverVO =
                                        exploreViewModel.obj as DiscoverModel
                                    if (currentVideoPlayDuration == halfDuration && !discoverVO.isViewCountUpdated) {
                                        discoverVO.viewVideo(this@HomeScreen, Constants.SCREEN_HOME)
                                    }
                                } else if (exploreViewModel.type === ExploreVideoType.RT) {
                                    val conversationModel =
                                        exploreViewModel.getObj() as ConversationModel
                                    val chatModel =
                                        conversationModel.chats[conversationModel.chats.size - 1]
                                    if (chatModel != null) {
                                        if (currentVideoPlayDuration == halfDuration) {
                                            if (!chatModel.isViewCountUpdated) {
                                                chatModel.viewVideo(
                                                    this@HomeScreen,
                                                    VideoConvType.ROUND_TABLE.value,
                                                    Constants.SCREEN_HOME
                                                )
                                            }
                                        }
                                        if (currentVideoPlayDuration == 1 && !chatModel.read) {
                                            chatModel.readVideo(this@HomeScreen, userId)
                                        }
                                    }
                                }
                            }
                        } else {
                            progressTimer!!.progress = 0
                        }
                    } catch (e: java.lang.Exception) {
                        showLogException(e)
                    }
                }
            }
        }, 0, 50)
    }

    private fun callSubscribeAPIAfterLogin() {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("subscribe", true)
            jsonObject.put("chat_id", chatId)
            BaseAPIService(
                this,
                Constants.SUBSCRIBE_RT,
                getRequestBody(jsonObject.toString()),
                true,
                object : ResponseListener {
                    override fun onSuccess(response: String?) {
                        Utility.showCustomSubscribeToast(this@HomeScreen)
                       // Utility.logSubscribeUnsubscribe(chatId, true)
                    }

                    override fun onFailure(error: String?) {}
                },
                "POST",
                false
            )
        } catch (e: java.lang.Exception) {
            showLogException(e)
        }
    }

    override fun onPause() {
        pauseCurrentVideo(false)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

    }

    private fun refreshDataIfNeeded() {
        if (FeedViewModel.getInstance().masterCommunitiesList.isEmpty()) {
            FeedViewModel.getInstance().feedCommunityList(this)
        }
        if (discoverList.size == 0) {
            discoverVideos(true, true)
        } else {
            if (supportFragmentManager.backStackEntryCount == 0) {
                //if (shouldVideoPlay()) {
                    playCurrentVideo()
               // }
            }
        }
    }
}