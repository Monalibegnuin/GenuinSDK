package com.begenuin.library.views

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.begenuin.library.R
import com.begenuin.library.SDKInitiate
import com.begenuin.library.common.Utility
import com.begenuin.library.common.customViews.CustomTextView
import com.begenuin.library.common.customViews.TextureImageView
import com.begenuin.library.core.enums.ExploreVideoType
import com.begenuin.library.core.enums.PeekSource
import com.begenuin.library.core.enums.VideoConvType
import com.begenuin.library.data.model.ChatModel
import com.begenuin.library.data.model.ConversationModel
import com.begenuin.library.data.model.DiscoverModel
import com.begenuin.library.data.model.GroupModel
import com.begenuin.library.data.model.LottieAnimModel
import com.begenuin.library.data.model.MembersModel
import com.begenuin.library.data.model.MessageModel
import com.begenuin.library.data.viewmodel.ExploreViewModel
import com.begenuin.library.peekandpop.PeekAndPop
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import de.hdodenhof.circleimageview.CircleImageView

class PeekViewAdapter(private var peekAndPop: PeekAndPop, val context: Activity) {
    lateinit var recyclerView: RecyclerView
    var player: ExoPlayer
    lateinit var videoView: StyledPlayerView
    lateinit var ivProgressBar: CircularProgressIndicator
    var cvVideo: CardView
    private var optionsData = ArrayList<String>()
    private var exploreViewModel: ExploreViewModel<*>? = null
    private var messageModel: MessageModel? = null
    private var chatModel: ChatModel? = null
    private lateinit var chatGroupModel: GroupModel
    private var isExploreViewModel = true
    private var isMessageModel = false
    var isMyProfile: Boolean = false
    var videoPosition = -1
    var source = PeekSource.EXPLORE
    //var replyCommentInterface: LongPressRecyclerActionInterface? = null
    private var selfUserId = ""

    init {
        selfUserId = SDKInitiate.userId
        peekAndPop.isBlurBackground = true
        player = ExoPlayer.Builder(context).build()
        cvVideo = peekAndPop.peekView.findViewById(R.id.cvVideo)
        adjustAspectRatio()
    }

    fun setExploreViewModel(exploreViewModel: ExploreViewModel<*>) {
        this.exploreViewModel = exploreViewModel
        isExploreViewModel = true
    }

    fun setChatModel(chatModel: ChatModel, chatGroupModel: GroupModel) {
        this.chatModel = chatModel
        this.chatGroupModel = chatGroupModel
        isExploreViewModel = false
    }

    fun setMessageModel(messageModel: MessageModel) {
        this.messageModel = messageModel
        isMessageModel = true
    }

    fun setRecyclerOptionsData(source: PeekSource, videoType: String) {
        optionsData.clear()
        this.source = source
        if (source == PeekSource.EXPLORE || source == PeekSource.HASHTAG) {
            if (videoType == "RT") {
                optionsData.add(context.getString(R.string.watch_roundtable))
                optionsData.add(context.getString(R.string.comment))
                optionsData.add(context.getString(R.string.share))
            } else if (videoType == "Public") {
                optionsData.add(context.getString(R.string.reply))
                optionsData.add(context.getString(R.string.view_profile))
                optionsData.add(context.getString(R.string.share))
            }
        } else if (source == PeekSource.SAVED) {
            optionsData.add(context.getString(R.string.reply))
            optionsData.add(context.getString(R.string.unsave))
            optionsData.add(context.getString(R.string.share))
        } else if (source == PeekSource.INBOX) {
            when (videoType) {
                "RT" -> {
                    optionsData.add(context.getString(R.string.view_profile))
                    optionsData.add(context.getString(R.string.comment))
                    optionsData.add(context.getString(R.string.share))
                }
                "DM" -> {
                    optionsData.add(context.getString(R.string.view_profile))
                    optionsData.add(context.getString(R.string.comment))
                }
                "Reaction" -> {
                    optionsData.add(context.getString(R.string.view_profile))
                    optionsData.add(context.getString(R.string.comment))
                }
                "Group" -> {
                    optionsData.add(context.getString(R.string.view_profile))
                    optionsData.add(context.getString(R.string.comment))
                }
                else -> {}
            }
        }
    }

    fun setRecyclerOptionsData(
        source: PeekSource,
        videoType: String,
        isMyProfile: Boolean,
        isFlagged: Boolean
    ) {
        optionsData.clear()
        this.source = source
        if (source == PeekSource.RTVIDEOTAB_PROFILE || source == PeekSource.ALLVIDEOTAB_PROFILE || source == PeekSource.PROFILEVIDEOTAB_PROFILE) {
            this.isMyProfile = isMyProfile
            if (videoType == "RT") {
                optionsData.add(context.getString(R.string.watch_roundtable))
                optionsData.add(context.getString(R.string.comment))
                optionsData.add(context.getString(R.string.share))
            } else if (videoType == "Public") {
                if (isMyProfile) {
                    if (isFlagged) {
                        optionsData.add(context.getString(R.string.why_flagged))
                    }
                    optionsData.add(context.getString(R.string.download))
                    optionsData.add(context.getString(R.string.share))
                } else {
                    optionsData.add(context.getString(R.string.reply))
                    optionsData.add(context.getString(R.string.share))
                }
            }
        }
    }

    fun setRecyclerOptionsDataForLoopPosts(
        isLoopOwner: Boolean,
        isMyVideo: Boolean,
        isPinned: Boolean
    ) {
        optionsData.clear()
        if (isLoopOwner) {
            if (isPinned) {
                optionsData.add(context.getString(R.string.unpin_from_loop))
            } else {
                optionsData.add(context.getString(R.string.pin_to_loop))
            }
        }
        optionsData.add(context.getString(R.string.comment))
        optionsData.add(context.getString(R.string.share))
        if (!isMyVideo && !isLoopOwner) {
            optionsData.add(context.getString(R.string.report))
        }
    }

    private fun initRecyclerView() {
        recyclerView = peekAndPop.peekView.findViewById(R.id.peek_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = PeekViewRecyclerAdapter(
            peekAndPop,
            optionsData,
            context,
            isMessageModel,
            messageModel,
            isExploreViewModel,
            exploreViewModel,
            chatModel,
            isMyProfile,
            videoPosition,
            source
        )
    }

    fun initViews() {
        initRecyclerView()
        videoView = peekAndPop.peekView.findViewById(R.id.videoView)
        ivProgressBar = peekAndPop.peekView.findViewById(R.id.ivProgressBar)
        val llRTBottom: LinearLayout = peekAndPop.peekView.findViewById(R.id.llRTBottom)
        val llRTOwner: LinearLayout = peekAndPop.peekView.findViewById(R.id.llRTOwner)
        val llReposted: LinearLayout = peekAndPop.peekView.findViewById(R.id.llReposted)
        val tvReposterOwnerName: CustomTextView =
            peekAndPop.peekView.findViewById(R.id.tvReposterOwnerName)
        val tvOgOwnerName: CustomTextView = peekAndPop.peekView.findViewById(R.id.tvOgOwnerName)
        val llPublicVideoBottom: LinearLayout =
            peekAndPop.peekView.findViewById(R.id.llPublicVideoBottom)
        val tvPublicUserName: CustomTextView =
            peekAndPop.peekView.findViewById(R.id.tvPublicUserName)
        //val tvPublicDesc: ExpandableTextView = peekAndPop.peekView.findViewById(R.id.tvPublicDesc)
        val tvRTUserName: CustomTextView = peekAndPop.peekView.findViewById(R.id.tvRTUserName)
        val tvRTName: CustomTextView = peekAndPop.peekView.findViewById(R.id.tvRTName)
        //val tvRTDesc: ExpandableTextView = peekAndPop.peekView.findViewById(R.id.tvRTDesc)
       // val llRTDp: DisplayPictureView = peekAndPop.peekView.findViewById(R.id.llRTDp)
        if (isMessageModel) {
            llRTBottom.visibility = View.GONE
            llPublicVideoBottom.visibility = View.VISIBLE
            if (isNotSelf(messageModel?.owner?.userId.toString())) {
                tvPublicUserName.text = String.format("@%s", messageModel?.owner?.userName)
            } else {
                tvPublicUserName.text = context.getString(R.string.you)
            }
            setChatModelDP(messageModel?.owner)
//            if (!TextUtils.isEmpty(messageModel?.messageSummary)) {
//                tvPublicDesc.visibility = View.VISIBLE
//                tvPublicDesc.text = messageModel?.messageSummary
//            } else {
//                tvPublicDesc.visibility = View.GONE
//            }
        } else if (isExploreViewModel) {
            if (exploreViewModel?.type == ExploreVideoType.PUBLIC_VIDEO) {
                val discoverVO = exploreViewModel?.obj as DiscoverModel
                llRTBottom.visibility = View.GONE
                llPublicVideoBottom.visibility = View.VISIBLE
                setExploreViewModelDP(discoverVO)
                tvPublicUserName.text = String.format("@%s", exploreViewModel?.nickName)
               // tvPublicDesc.visibility = View.GONE
            } else if (exploreViewModel?.type == ExploreVideoType.RT) {
                llRTBottom.visibility = View.VISIBLE
                llPublicVideoBottom.visibility = View.GONE
                val conversationModel = exploreViewModel?.obj as ConversationModel
                val groupModel = conversationModel.group
                if (!TextUtils.isEmpty(exploreViewModel?.repostOwnerName)) {
                    llReposted.visibility = View.VISIBLE
                    llRTOwner.visibility = View.GONE
                    var rePosterName = "@" + exploreViewModel?.nickName
                    var ogPosterName = "@" + exploreViewModel?.repostOwnerName
                    var rePosterLen = rePosterName.length
                    var ogPosterLen = ogPosterName.length
                    val totalLen = 24
                    if (rePosterLen > ogPosterLen) {
                        ogPosterLen = Math.min(ogPosterLen, totalLen / 2)
                        rePosterLen = Math.min(rePosterLen, totalLen - ogPosterLen)
                        if (ogPosterName.length > totalLen / 2) {
                            ogPosterName = ogPosterName.substring(0, ogPosterLen - 3) + "..."
                        }
                        if (rePosterName.length > totalLen - ogPosterLen) {
                            rePosterName = rePosterName.substring(0, rePosterLen - 3) + "..."
                        }
                    } else {
                        rePosterLen = Math.min(rePosterLen, totalLen / 2)
                        ogPosterLen = Math.min(ogPosterLen, totalLen - rePosterLen)
                        if (rePosterName.length > totalLen / 2) {
                            rePosterName = rePosterName.substring(0, rePosterLen - 3) + "..."
                        }
                        if (ogPosterName.length > totalLen - rePosterLen) {
                            ogPosterName = ogPosterName.substring(0, ogPosterLen - 3) + "..."
                        }
                    }
                    tvReposterOwnerName.text = rePosterName
                    tvOgOwnerName.text = ogPosterName
                } else {
                    llReposted.visibility = View.GONE
                    llRTOwner.visibility = View.VISIBLE
                    tvRTUserName.text = String.format("@%s", exploreViewModel?.nickName)
                }
                tvRTName.text = groupModel.name
                //tvRTDesc.visibility = View.GONE
//                if (TextUtils.isEmpty(groupModel.smallDp)) {
//                    llRTDp.setDpWithInitials(
//                        groupModel.name,
//                        groupModel.colorCode,
//                        groupModel.textColorCode
//                    )
//                } else {
//                    llRTDp.setDpWithImage(
//                        context,
//                        false,
//                        groupModel.dp,
//                        groupModel.smallDp,
//                        false
//                    )
//                }
            }
        } else {//chatModel
            val owner = chatModel?.owner
            val repostModel = chatModel?.repostModel
            if (chatModel?.convType == VideoConvType.ROUND_TABLE.value) { //RT Video
                llRTBottom.visibility = View.VISIBLE
                llPublicVideoBottom.visibility = View.GONE
               // tvRTDesc.visibility = View.GONE
                if (repostModel != null && repostModel.owner != null) { //reposted video
                    llReposted.visibility = View.VISIBLE
                    llRTOwner.visibility = View.GONE
                    var rePosterName = "@" + owner?.nickname
                    var ogPosterName = "@" + repostModel.owner.nickname
                    var rePosterLen = rePosterName.length
                    var ogPosterLen = ogPosterName.length
                    val totalLen = 24
                    if (rePosterLen > ogPosterLen) {
                        ogPosterLen = Math.min(ogPosterLen, totalLen / 2)
                        rePosterLen = Math.min(rePosterLen, totalLen - ogPosterLen)
                        if (ogPosterName.length > totalLen / 2) {
                            ogPosterName = ogPosterName.substring(0, ogPosterLen - 3) + "..."
                        }
                        if (rePosterName.length > totalLen - ogPosterLen) {
                            rePosterName = rePosterName.substring(0, rePosterLen - 3) + "..."
                        }
                    } else {
                        rePosterLen = Math.min(rePosterLen, totalLen / 2)
                        ogPosterLen = Math.min(ogPosterLen, totalLen - rePosterLen)
                        if (rePosterName.length > totalLen / 2) {
                            rePosterName = rePosterName.substring(0, rePosterLen - 3) + "..."
                        }
                        if (ogPosterName.length > totalLen - rePosterLen) {
                            ogPosterName = ogPosterName.substring(0, ogPosterLen - 3) + "..."
                        }
                    }
                    tvReposterOwnerName.text = rePosterName
                    tvOgOwnerName.text = ogPosterName
                } else {
                    llReposted.visibility = View.GONE
                    llRTOwner.visibility = View.VISIBLE
                    tvRTUserName.text = String.format("@%s", owner?.nickname)
                }
                tvRTName.text = chatGroupModel.name
//                if (TextUtils.isEmpty(chatGroupModel.smallDp)) {
//                    llRTDp.setDpWithInitials(
//                        chatGroupModel.name,
//                        chatGroupModel.colorCode,
//                        chatGroupModel.textColorCode
//                    )
//                } else {
//                    llRTDp.setDpWithImage(
//                        context,
//                        false,
//                        chatGroupModel.dp,
//                        chatGroupModel.smallDp,
//                        false
//                    )
//                }
            } else if (chatModel?.convType == VideoConvType.DIRECT.value || chatModel?.convType == VideoConvType.REACTION.value) { //Public Video
                llPublicVideoBottom.visibility = View.VISIBLE
                llRTBottom.visibility = View.GONE
                //tvPublicDesc.visibility = View.GONE
                setChatModelDP(owner)
                tvPublicUserName.text = owner?.nickname
                tvPublicUserName.text = String.format("@%s", owner?.nickname)
                //tvPublicDesc.visibility = View.GONE
            } else if (chatModel?.convType == VideoConvType.GROUP.value) {
                llPublicVideoBottom.visibility = View.GONE
                llRTBottom.visibility = View.VISIBLE
                llReposted.visibility = View.GONE
                llRTOwner.visibility = View.VISIBLE
                //tvRTDesc.visibility = View.GONE
                tvRTUserName.text = String.format("@%s", owner?.nickname)
                tvRTName.text = chatGroupModel.name
//                if (TextUtils.isEmpty(chatGroupModel.smallDp)) {
//                    llRTDp.setDpWithInitials(
//                        chatGroupModel.name,
//                        chatGroupModel.colorCode,
//                        chatGroupModel.textColorCode
//                    )
//                } else {
//                    llRTDp.setDpWithImage(
//                        context,
//                        false,
//                        chatGroupModel.dp,
//                        chatGroupModel.smallDp,
//                        false
//                    )
//                }
            }

        }
    }

    private fun isNotSelf(userId: String): Boolean {
        return selfUserId != userId
    }

    private fun adjustAspectRatio() {
        val screenDimensions = Utility.getScreenWidthHeight(context)
        val screenWidth = screenDimensions?.get(0)
        val videoViewWidth = screenWidth?.minus(Utility.dpToPx(114f, context))
        val videoViewHeight = (videoViewWidth?.times(16f))?.div(9f)
        val params = cvVideo.layoutParams
        params.height = videoViewHeight!!.toInt()
        params.width = videoViewWidth.toInt()
        cvVideo.layoutParams = params
    }

    fun playVideo() {
        val ivThumbnail: TextureImageView = peekAndPop.peekView.findViewById(R.id.ivThumbnail)
        if (isMessageModel) {
            Glide.with(context).asDrawable()
                .load(messageModel?.getVideoThumbnailURL())
                .error(R.drawable.ic_no_preivew)
                .into(ivThumbnail)
        } else if (isExploreViewModel) {
            Glide.with(context).asDrawable()
                .load(exploreViewModel?.imageURL)
                .error(R.drawable.ic_no_preivew)
                .into(ivThumbnail)
        } else {
            if (!TextUtils.isEmpty(chatModel?.imagePath)) {
                Glide.with(context).asDrawable()
                    .load(chatModel?.imagePath)
                    .error(R.drawable.ic_no_preivew)
                    .into(ivThumbnail)
            } else if (!TextUtils.isEmpty(chatModel?.localVideoPath)) {
                Glide.with(context).asDrawable()
                    .load(chatModel?.localVideoPath)
                    .error(R.drawable.ic_no_preivew)
                    .into(ivThumbnail)
            } else {
                if (!TextUtils.isEmpty(chatModel?.videoThumbnailLarge)) {
                    Glide.with(context).asDrawable()
                        .load(chatModel?.videoThumbnailLarge)
                        .error(R.drawable.ic_no_preivew)
                        .into(ivThumbnail)
                } else {
                    Glide.with(context).asDrawable()
                        .load(chatModel?.thumbnailUrl)
                        .error(R.drawable.ic_no_preivew)
                        .into(ivThumbnail)
                }

            }
        }
        videoView.player = player
        videoView.alpha = 0f
        ivProgressBar.visibility = View.VISIBLE
        var videoURL = MediaItem.fromUri("")
        videoURL = if (isMessageModel) {
            MediaItem.fromUri(messageModel?.getVideoURL().toString())
        } else if (isExploreViewModel) {
            MediaItem.fromUri(exploreViewModel!!.feedURL)
        } else {
            if (!TextUtils.isEmpty(chatModel?.videoUrlM3U8)) {
                MediaItem.fromUri(chatModel!!.videoUrlM3U8)
            } else {
                MediaItem.fromUri(chatModel!!.videoUrl)
            }
        }
        player.addMediaItem(videoURL)
        player.prepare()
        player.playWhenReady = true
        val playbackChangeListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        videoView.alpha = 1f
                        ivProgressBar.visibility = View.GONE
                    }
                    Player.STATE_ENDED -> {
                        player.seekTo(0, 0L)
                        player.playWhenReady = true
                        ivProgressBar.visibility = View.VISIBLE
                    }
                    Player.STATE_BUFFERING -> {
                        ivProgressBar.visibility = View.VISIBLE
                    }
                }
                super.onPlaybackStateChanged(playbackState)
            }
        }
        player.addListener(playbackChangeListener)
        videoView.player = player
    }

    private fun setChatModelDP(owner: MembersModel?) {
        try {
            val ivVideoProfile: CircleImageView =
                peekAndPop.peekView.findViewById(R.id.ivVideoProfile)
            val animationView: LottieAnimationView =
                peekAndPop.peekView.findViewById(R.id.animationView)
            if (owner!!.isAvatar) {
                val res = context.resources.getIdentifier(
                    owner.profileImage,
                    "raw", context.packageName
                )
                val color: Drawable = ColorDrawable(
                    context.resources.getColor(
                        LottieAnimModel.getMapData()[res]!!,
                        null
                    )
                )
                ivVideoProfile.setImageDrawable(color)
                animationView.visibility = View.VISIBLE
                animationView.setAnimation(res)
                animationView.playAnimation()
            } else {
                animationView.visibility = View.GONE
                if (!TextUtils.isEmpty(owner.profileImageS)) {
                    Utility.displayProfileImage(
                        context,
                        owner.profileImageS,
                        ivVideoProfile
                    )
                } else {
                    Utility.displayProfileImage(
                        context,
                        owner.profileImage,
                        ivVideoProfile
                    )
                }
            }
        } catch (e: Exception) {
            Utility.showLogException(e)
        }
    }

    private fun setExploreViewModelDP(discoverVO: DiscoverModel) {
        try {
            val ivVideoProfile: CircleImageView =
                peekAndPop.peekView.findViewById(R.id.ivVideoProfile)
            val animationView: LottieAnimationView =
                peekAndPop.peekView.findViewById(R.id.animationView)
            if (discoverVO.avatar) {
                val res = context.resources.getIdentifier(
                    discoverVO.profileImage,
                    "raw", context.packageName
                )
                val color: Drawable = ColorDrawable(
                    context.resources.getColor(
                        LottieAnimModel.getMapData()[res]!!,
                        null
                    )
                )
                ivVideoProfile.setImageDrawable(color)
                animationView.visibility = View.VISIBLE
                animationView.setAnimation(res)
                animationView.playAnimation()
            } else {
                animationView.visibility = View.GONE
                if (!TextUtils.isEmpty(discoverVO.profileImageS)) {
                    Utility.displayProfileImage(
                        context,
                        discoverVO.profileImageS,
                        ivVideoProfile
                    )
                } else {
                    Utility.displayProfileImage(
                        context,
                        discoverVO.profileImage,
                        ivVideoProfile
                    )
                }
            }
        } catch (e: Exception) {
            Utility.showLogException(e)
        }
    }
}