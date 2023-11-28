package com.begenuin.library.views

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.begenuin.library.R
import com.begenuine.feedscreensdk.common.Constants
import com.begenuin.library.common.Utility
import com.begenuin.library.common.customViews.SparkView
import com.begenuin.library.core.enums.FeedViewType
import com.begenuin.library.data.viewmodel.LoopSuggestionResponseListener
import com.begenuin.library.data.viewmodel.LoopSuggestionsViewModel
import com.begenuin.library.SDKInitiate
import com.begenuin.library.common.DownloadVideo
import com.begenuin.library.common.customViews.CustomTextView
import com.begenuin.library.common.customViews.DisplayPictureView
import com.begenuin.library.common.customViews.TextureImageView
import com.begenuin.library.core.enums.ExploreVideoType
import com.begenuin.library.core.interfaces.FeedAdapterListener
import com.begenuin.library.core.interfaces.LoopSuggestionPagerEventListener
import com.begenuin.library.data.model.ChatModel
import com.begenuin.library.data.model.ConversationModel
import com.begenuin.library.data.model.DiscoverModel
import com.begenuin.library.data.model.GroupModel
import com.begenuin.library.data.viewmodel.ExploreViewModel
import com.begenuin.library.data.viewmodel.FeedViewModel
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.card.MaterialCardView
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Objects

class FeedRTAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder?> {
    private val data: ArrayList<ExploreViewModel<*>>
    private val activity: Activity
    private val context: Context
    private var feedAdapterListener: FeedAdapterListener? = null
    var isFeedFragment2 = false
    var isMyProfile = false
    private var isMuted = false
    var type: String? = null
    var tag: String? = null
    private val userId: String
    private val halfHeightPX: Int
    private val NORMAL_TYPE = 1
    private val END_OF_FEED_TYPE = 2
    private var suggestedLoops: ArrayList<ConversationModel>? = null
    private var feedViewType: Int? = null
    private var feedPagerPosition = 0
    private var isEndOfSuggestedLoops = false
    private var suggestedLoopPageNo = 2
    private var loopSuggestionPagerEventListener: LoopSuggestionPagerEventListener? = null

    constructor(activity: Activity, context: Context, data: ArrayList<ExploreViewModel<Any>>) {
        this.data = data as ArrayList<ExploreViewModel<*>>
        this.activity = activity
        this.context = context
        halfHeightPX = (Utility.getScreenWidthHeight(activity)!![1] / 2 - Utility.dpToPx(
            70f,
            activity
        )).toInt()
        //TODO: Need to get userId from SDK initiate
        //userId = Utility.getLoggedInUserId(activity)
        userId = SDKInitiate.userId
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == END_OF_FEED_TYPE) {
            val v: View = inflater.inflate(R.layout.end_of_feed_item, parent, false)
            EndOfFeedViewHolder(v)
        } else {
            val v: View = inflater.inflate(R.layout.discover_video_item, parent, false)
            ViewHolder(v)
        }
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        val exploreViewModel: ExploreViewModel<*> = data[position]
        return if (exploreViewModel.type == ExploreVideoType.END_OF_FEED) {
            END_OF_FEED_TYPE
        } else {
            NORMAL_TYPE
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        Utility.showLog("Player", "OnBindViewHolder $position")
        val exploreViewModel: ExploreViewModel<*> = data[position]
        if (exploreViewModel.type == ExploreVideoType.END_OF_FEED) {
            val holder = viewHolder as EndOfFeedViewHolder
            if (suggestedLoops!!.size > 0) {
                if (data.size == 1) {
                    //empty feed
                    holder.tvFeedEndMsg.visibility = View.GONE
                    holder.ivAllCaughtUp.visibility = View.GONE
                    if (feedViewType == FeedViewType.SUBSCRIPTIONS.value) {
                        holder.tvFeedEndDesc.text =
                            context.getString(R.string.empty_feed_subscriptions)
                    } else if (feedViewType == FeedViewType.MY_LOOPS.value) {
                        holder.tvFeedEndDesc.text = context.getString(R.string.empty_feed_my_loops)
                    }
                } else {
                    holder.tvFeedEndMsg.visibility = View.VISIBLE
                    holder.ivAllCaughtUp.visibility = View.VISIBLE
                    if (feedViewType == FeedViewType.SUBSCRIPTIONS.value) {
                        holder.tvFeedEndDesc.text =
                            context.getString(R.string.subscriptions_end_desc)
                    } else if (feedViewType == FeedViewType.MY_LOOPS.value) {
                        holder.tvFeedEndDesc.text = context.getString(R.string.my_loop_end_desc)
                    }
                    holder.vpLoopSuggestions.setVisibility(View.VISIBLE)
                    holder.btnGoToForYou.visibility = View.GONE
                }
            } else {
                if (feedViewType == FeedViewType.SUBSCRIPTIONS.value) {
                    holder.tvFeedEndDesc.text =
                        context.getString(R.string.empty_suggestions_subscriptions)
                } else if (feedViewType == FeedViewType.MY_LOOPS.value) {
                    holder.tvFeedEndDesc.text =
                        context.getString(R.string.empty_suggestions_my_loops)
                }
                holder.vpLoopSuggestions.setVisibility(View.GONE)
                holder.btnGoToForYou.visibility = View.VISIBLE
            }
            holder.llLoader.setTag(Constants.LL_LOADER + position)
            holder.llLoader.setVisibility(View.GONE)
            holder.btnGoToForYou.setOnClickListener { loopSuggestionPagerEventListener?.onGoToFeedClicked() }
            holder.vpLoopSuggestions.setAdapter(SliderLoopSuggestionRecyclerAdapter(
                activity,
                suggestedLoops!!,
                feedViewType!!,
                object : ButtonClickListener {
                    override fun onRequestAPIComplete(isSuccess: Boolean) {}
                    override fun onSubscribeAPIComplete(isSuccess: Boolean) {
                        loopSuggestionPagerEventListener?.onApiComplete(isSuccess)
                    }

                    override fun onSubscribe(position: Int, isSubscribe: Boolean) {
                        loopSuggestionPagerEventListener?.onButtonClick()
                        if (isSubscribe && position < suggestedLoops!!.size - 1) {
                            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                                holder.vpLoopSuggestions.setCurrentItem(
                                    position + 1,
                                    true
                                )
                            }, 500)
                        }
                    }

                    override fun onRequestToJoin(position: Int) {
                        if (position < suggestedLoops!!.size - 1) {
                            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                                holder.vpLoopSuggestions.setCurrentItem(
                                    position + 1,
                                    true
                                )
                            }, 500)
                        }
                    }
                }
            ))
            holder.vpLoopSuggestions.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                /*int index;
                int previousState = ViewPager2.SCROLL_STATE_IDLE;*/
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                }

                 override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    //index = position;
                    loopSuggestionPagerEventListener?.onItemScroll()
                    if (!isEndOfSuggestedLoops && position >= suggestedLoops!!.size - 3 &&
                        LoopSuggestionsViewModel.apiStatus[feedPagerPosition] !== FeedViewModel.APIStatus.IN_PROGRESS
                    ) {
                        LoopSuggestionsViewModel.suggestLoops(
                            activity,
                            suggestedLoopPageNo,
                            feedPagerPosition,
                            object : LoopSuggestionResponseListener {
                                override fun onFetchComplete(
                                    list: List<ConversationModel>,
                                    isEndOfLoops: Boolean
                                ) {
                                    isEndOfSuggestedLoops = isEndOfLoops
                                    suggestedLoopPageNo++
                                    val newStartPos = suggestedLoops!!.size
                                    suggestedLoops!!.addAll(list)
                                    Objects.requireNonNull(
                                        holder.vpLoopSuggestions
                                            .adapter
                                    )
                                        .notifyItemRangeInserted(
                                            newStartPos,
                                            list.size
                                        )
                                }

                            })
                    }
                 }


                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    /*if(LoopSuggestionsViewModel.INSTANCE.getApiStatus()[feedPagerPosition] != FeedViewModel.APIStatus.IN_PROGRESS
                                    && index >= suggestedLoops.size() - 1 && previousState == ViewPager2.SCROLL_STATE_DRAGGING
                                    && state == ViewPager2.SCROLL_STATE_IDLE){
                        loopSuggestionPagerEventListener.onEndingOverScroll();
                    } else if(LoopSuggestionsViewModel.INSTANCE.getApiStatus()[feedPagerPosition] != FeedViewModel.APIStatus.IN_PROGRESS
                            && index <= 0 && previousState == ViewPager2.SCROLL_STATE_DRAGGING
                            && state == ViewPager2.SCROLL_STATE_IDLE){
                        loopSuggestionPagerEventListener.onBeginningOverScroll();
                    }
                    previousState = state;*/
                }
            })
            holder.vpLoopSuggestions.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            holder.vpLoopSuggestions.clipToPadding = false
            holder.vpLoopSuggestions.clipChildren = false
            holder.vpLoopSuggestions.offscreenPageLimit = 1
            holder.vpLoopSuggestions.setPageTransformer(ViewPager2.PageTransformer { page: View, position1: Float ->
                val offset =
                    position1 * -(2 * Utility.dpToPx(8f, activity) + Utility.dpToPx(16f, activity))
                if (ViewCompat.getLayoutDirection(holder.vpLoopSuggestions) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    page.translationX = -offset
                } else {
                    page.translationX = offset
                }
            })
        } else {
            val holder = viewHolder as ViewHolder
            holder.flMain.tag = exploreViewModel
            holder.mTextureVideoView?.tag = Constants.VIDEO + position
            holder.ivThumbnail.tag = Constants.IMAGE + position
            holder.ivSaveVideo.tag = Constants.SAVE_VIDEO_LL + position
            if (exploreViewModel.type == ExploreVideoType.PUBLIC_VIDEO) {
                holder.ivRepost.tag = Constants.LL_REPOST + position
                holder.ivShare.tag = Constants.SHARE_VIDEO + position
                holder.ivLink.tag = Constants.LINK + position
                holder.ivMoreOptions.tag = Constants.MORE_OPTIONS_LAYOUT + position
                holder.llBottomLayout.tag = Constants.LL_BOTTOM + position
                holder.llWhoCanSee.tag = Constants.WHO_CAN_SEE_LL + position
                holder.rlDesc.tag = Constants.RL_DESC + position
                holder.tvDesc.tag = Constants.TV_DESC + position
                holder.tvDescSingle.tag = Constants.TV_DESC_SINGLE + position
                holder.sparkView.setTag(Constants.LL_SPARK + position)
                holder.tvSparks.tag = Constants.TV_SPARK + position
                holder.ivRTShare.tag = Constants.FAKE_TAG + position
                holder.ivRTLink.tag = Constants.FAKE_TAG + position
                holder.ivRTRepost.tag = Constants.FAKE_TAG + position
                holder.ivRTMoreOptions.tag = Constants.FAKE_TAG + position
                holder.llRTBottomLayout.tag = Constants.FAKE_TAG + position
                holder.llWhoCanSeeRT.tag = Constants.FAKE_TAG + position
                holder.rlRTDesc.tag = Constants.FAKE_TAG + position
                holder.tvRTDesc.tag = Constants.FAKE_TAG + position
                holder.tvRTDescSingle.tag = Constants.FAKE_TAG + position
                holder.sparkViewRT.setTag(Constants.FAKE_TAG + position)
                holder.tvRTSparks.tag = Constants.FAKE_TAG + position
            } else if (exploreViewModel.type == ExploreVideoType.RT) {
                holder.ivRTShare.tag = Constants.SHARE_VIDEO + position
                holder.ivRTLink.tag = Constants.LINK + position
                holder.ivRTRepost.tag = Constants.LL_REPOST + position
                holder.ivRTMoreOptions.tag = Constants.MORE_OPTIONS_LAYOUT + position
                holder.llRTBottomLayout.setTag(Constants.LL_BOTTOM + position)
                holder.llWhoCanSeeRT.setTag(Constants.WHO_CAN_SEE_LL + position)
                holder.tvRTDesc.tag = Constants.TV_DESC + position
                holder.tvRTDescSingle.tag = Constants.TV_DESC_SINGLE + position
                holder.rlRTDesc.tag = Constants.RL_DESC + position
                holder.sparkViewRT.setTag(Constants.LL_SPARK + position)
                holder.tvRTSparks.tag = Constants.TV_SPARK + position
                holder.ivShare.tag = Constants.FAKE_TAG + position
                holder.ivLink.tag = Constants.FAKE_TAG + position
                holder.ivRepost.tag = Constants.FAKE_TAG + position
                holder.ivMoreOptions.tag = Constants.FAKE_TAG + position
                holder.llBottomLayout.tag = Constants.FAKE_TAG + position
                holder.llWhoCanSee.tag = Constants.FAKE_TAG + position
                holder.rlDesc.tag = Constants.FAKE_TAG + position
                holder.tvDesc.tag = Constants.FAKE_TAG + position
                holder.tvDescSingle.tag = Constants.FAKE_TAG + position
                holder.sparkView.setTag(Constants.FAKE_TAG + position)
                holder.tvSparks.tag = Constants.FAKE_TAG + position
            }
            holder.rlOverlay.tag = Constants.OVERLAY + position
            holder.cardSubscribe.tag = Constants.LL_SUBSCRIBE + position
            holder.cardAddQuestion.tag = Constants.LL_ASK_QUESTION + position
            holder.tvAddQuestion.tag = Constants.TV_ASK_QUESTION + position
            holder.ivMuteIcon.tag = Constants.IV_MUTE + position
            if (isMuted) {
                holder.ivMuteIcon.visibility = View.VISIBLE
            } else {
                holder.ivMuteIcon.visibility = View.GONE
            }
            if (exploreViewModel.type == ExploreVideoType.PUBLIC_VIDEO) {
                holder.ivMoreOptions.visibility = View.VISIBLE
                holder.llBottomLayout.visibility = View.VISIBLE
                holder.llRTBottomLayout.visibility = View.GONE
                val discoverVO: DiscoverModel = exploreViewModel.obj as DiscoverModel
                if (!TextUtils.isEmpty(discoverVO.resolution)) {
                    val resolution: List<String> = discoverVO.resolution.split("x")
                    if (resolution.size > 1) {
                        holder.ivThumbnail.setImageHeightWidth(
                            resolution[0].toInt(),
                            resolution[1].toInt()
                        )
                    }
                }
                if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(discoverVO.userId) && discoverVO.userId!!.contentEquals(userId)
                ) {
                    if (discoverVO.isFlag == 1) {
                        holder.ivFlagged.visibility = View.VISIBLE
                        holder.llWhoCanSee.visibility = View.VISIBLE
                    } else if (discoverVO.settings != null && !discoverVO.settings.discoverable
                    ) {
                        holder.ivFlagged.visibility = View.GONE
                        holder.llWhoCanSee.visibility = View.VISIBLE
                    } else {
                        holder.ivFlagged.visibility = View.GONE
                        holder.llWhoCanSee.visibility = View.GONE
                    }
                } else {
                    holder.ivFlagged.visibility = View.GONE
                    holder.llWhoCanSee.visibility = View.GONE
                }
                if (!TextUtils.isEmpty(discoverVO.recordedByText)) {
                    holder.tvRecordedByText.visibility = View.VISIBLE
                    holder.tvRecordedByText.text = discoverVO.recordedByText
                } else {
                    holder.tvRecordedByText.visibility = View.GONE
                }
                if (!TextUtils.isEmpty(exploreViewModel.link)) {
                    holder.ivLink.visibility = View.VISIBLE
                } else {
                    holder.ivLink.visibility = View.GONE
                }
                if (isMyProfile && TextUtils.isEmpty(exploreViewModel.getNickName())) {
//                    val userName: String =
//                        SharedPrefUtils.getStringPreference(activity, Constants.PREF_NK_NAME)
                  //TODO: Need to get user data
                   val userName: String = " "
                    holder.tvPublicUserName.text = String.format("@%s", userName)
                } else {
                    holder.tvPublicUserName.text =
                        String.format("@%s", exploreViewModel.nickName)
                }
                if (!TextUtils.isEmpty(discoverVO.createdAtTime)) {
                    holder.tvOwnerConversationAt.text = java.lang.String.format(
                        "%s %s",
                        Utility.timeFormat(discoverVO.createdAtTime.toLong() / 1000),
                        context.resources.getString(R.string.ago)
                    )
                } else {
                    holder.tvOwnerConversationAt.text =
                        context.resources.getString(R.string.just_now)
                }
                if (TextUtils.isEmpty(discoverVO.description)) {
                    holder.rlDesc.visibility = View.GONE
                } else {
                    holder.rlDesc.visibility = View.VISIBLE
                    holder.tvDesc.text = discoverVO.description
                    holder.tvDesc.movementMethod = ScrollingMovementMethod.getInstance()
                    holder.tvDescSingle.text = discoverVO.description
                    applyLayoutTransition(holder.llBottomLayout)
                }
                if (isMyProfile) {
                    holder.ivRepost.visibility = View.GONE
                    holder.ivSaveVideo.visibility = View.GONE
                    //if discover QR code has value then hide download video as this is upload by other's user not logged in user and for login user shows visible
                    if (TextUtils.isEmpty(discoverVO.qrCode)) holder.ivDownloadVideo.visibility =
                        View.VISIBLE else holder.ivDownloadVideo.visibility = View.GONE
                    if (discoverVO.videoId!!.contentEquals("-101")) {
                        holder.ivMoreOptions.imageTintList = ColorStateList.valueOf(
                            context.resources.getColor(
                                R.color.white_opacity40,
                                null
                            )
                        )
                    } else {
                        holder.ivMoreOptions.imageTintList = ColorStateList.valueOf(
                            context.resources.getColor(
                                R.color.colorWhite,
                                null
                            )
                        )
                    }
                } else {
                    //Utility.getLoggedInUserId(context.activity))
                    val loggedinUserId: String= ""
                    if (!TextUtils.isEmpty(discoverVO.userId) && discoverVO.userId!!.contentEquals(loggedinUserId)) {
                        holder.ivSaveVideo.visibility = View.GONE
                        holder.ivDownloadVideo.visibility = View.VISIBLE
                    } else {
                        holder.ivSaveVideo.visibility = View.VISIBLE
                        holder.ivDownloadVideo.visibility = View.GONE
                    }
                    holder.ivReply.visibility = View.VISIBLE
                }
                if (!TextUtils.isEmpty(discoverVO.sparkCount)) {
                    holder.tvSparks.text = Utility.formatNumber(discoverVO.sparkCount.toLong())
                } else {
                    holder.tvSparks.text = "0"
                }
                if (discoverVO.isSparked) {
                    holder.sparkView.setSpark()
                } else {
                    holder.sparkView.setUnSpark()
                }
                try {
                    if (!TextUtils.isEmpty(discoverVO.profileImageS)) {
                        setUserImage(
                            holder.llDp,
                            discoverVO.profileImageS,
                            discoverVO.avatar
                        )
                    } else {
                        setUserImage(
                            holder.llDp,
                            discoverVO.profileImage,
                            discoverVO.avatar
                        )
                    }
                } catch (e: Exception) {
                    Utility.showLogException(e)
                }
            } else if (exploreViewModel.type == ExploreVideoType.RT) {
                holder.llComments.visibility = View.VISIBLE
                holder.llRTBottomLayout.visibility = View.VISIBLE
                holder.llBottomLayout.visibility = View.GONE
                if (exploreViewModel.isRepostSourceAvailable) {
                    holder.ivRTRepost.visibility = View.VISIBLE
                } else {
                    holder.ivRTRepost.visibility = View.GONE
                }
                val conversationModel: ConversationModel =
                    exploreViewModel.obj as ConversationModel
                holder.tvRTUserName.text = String.format("@%s", exploreViewModel.getNickName())
                holder.tvRTOwnerConversationAt.text = java.lang.String.format(
                    "%s %s",
                    Utility.timeFormat(conversationModel.getCreatedAtTime().toLong() / 1000),
                    context.resources.getString(R.string.ago)
                )
                try {
                    if (conversationModel.getChats().size > 0) {
                        val chatModel: ChatModel =
                            conversationModel.chats[conversationModel.chats.size - 1]
                        if (chatModel.owner != null) {
                            if (!TextUtils.isEmpty(chatModel.owner.profileImageS)) {
                                setUserImage(
                                    holder.llUserDp,
                                    chatModel.owner.profileImageS,
                                    chatModel.owner.isAvatar
                                )
                            } else {
                                setUserImage(
                                    holder.llUserDp,
                                    chatModel.owner.profileImage,
                                    chatModel.owner.isAvatar
                                )
                            }
                        }
                        if (!TextUtils.isEmpty(chatModel.sparkCount)) {
                            holder.tvRTSparks.text =
                                Utility.formatNumber(chatModel.sparkCount.toLong())
                        } else {
                            holder.tvRTSparks.text = "0"
                        }
                        if (chatModel.isSparked) {
                            holder.sparkViewRT.setSpark()
                        } else {
                            holder.sparkViewRT.setUnSpark()
                        }
                    }
                } catch (e: Exception) {
                    Utility.showLogException(e)
                }
                if (!TextUtils.isEmpty(exploreViewModel.repostOwnerName)) {
                    holder.cardReposted.visibility = View.VISIBLE
                    val ogPosterName = "@" + exploreViewModel.repostOwnerName
                    holder.tvOgOwnerName.text = ogPosterName
                } else {
                    holder.cardReposted.visibility = View.GONE
                }
                if (!TextUtils.isEmpty(conversationModel.recordedByText)) {
                    holder.tvRTRecordedByText.visibility = View.VISIBLE
                    holder.tvRTRecordedByText.text = conversationModel.recordedByText
                } else {
                    holder.tvRTRecordedByText.visibility = View.GONE
                }
                if (!TextUtils.isEmpty(exploreViewModel.link)) {
                    holder.ivRTLink.visibility = View.VISIBLE
                } else {
                    holder.ivRTLink.visibility = View.GONE
                }
                if (TextUtils.isEmpty(conversationModel.commentsCount) || conversationModel.commentsCount
                        .equals("0", ignoreCase = true)
                ) {
                    holder.tvComments.text = "0"
                } else {
                    holder.tvComments.text = conversationModel.commentsCount
                }
                showGroupInfo(holder, conversationModel)
                showSubscriberInfo(holder, conversationModel)
            }
            holder.mTextureVideoView?.requestFocus()
        }
    }

    private fun showGroupInfo(holder: ViewHolder, conversationModel: ConversationModel) {
        val groupModel: GroupModel = conversationModel.group
        if (conversationModel.group != null) {
            holder.tvRTName.text = groupModel.name
            holder.tvRTDesc.text = groupModel.description
            holder.tvRTDesc.movementMethod = ScrollingMovementMethod.getInstance()
            holder.tvRTDescSingle.text = groupModel.description
            /*if (TextUtils.isEmpty(groupModel.getSmallDp())) {
                holder.llRTDp.setDpWithInitials(groupModel.getName(), groupModel.getColorCode(), groupModel.getTextColorCode());
            } else {
                holder.llRTDp.setDpWithImage(activity, false, groupModel.getDp(), groupModel.getSmallDp(), false);
            }*/
        }
        if (isVideoOwner(conversationModel)) {
            holder.ivRTMoreOptions.visibility = View.GONE
        } else {
            holder.ivRTMoreOptions.visibility = View.VISIBLE
        }
        if (conversationModel.settings != null && !conversationModel.settings
                .discoverable
        ) {
            holder.llWhoCanSeeRT.visibility = View.VISIBLE
        } else {
            holder.llWhoCanSeeRT.visibility = View.GONE
        }
    }

    private fun showSubscriberInfo(holder: ViewHolder, conversationModel: ConversationModel) {
        if (conversationModel.isSubscriber || conversationModel.memberInfo != null) {
            holder.cardSubscribe.visibility = View.GONE
            holder.cardAddQuestion.visibility = View.VISIBLE
            holder.tvAddQuestion.text = context.resources.getString(R.string.ask_question)
            /*if (conversationModel.getMemberInfo() != null) {
                holder.ivParticipate.setVisibility(View.VISIBLE);
            } else {
                holder.ivParticipate.setVisibility(View.GONE);
            }*/
        } else {
            holder.cardSubscribe.visibility = View.VISIBLE
            holder.cardSubscribe.alpha = 1f
            holder.cardSubscribe.setBackgroundResource(R.drawable.view_group_subscribe)
            //Text on Subscribe button turned to "Subscribed" during subscribe click management
            //should revert back to "Subscribe" when the view is recycled. The following 2 lines
            //handle that.
            val tvSubscribeSubscribed: CustomTextView =
                holder.cardSubscribe.findViewById(R.id.tvSubscribeSubscribed)
            tvSubscribeSubscribed.setText(R.string.subscribe)
            holder.cardAddQuestion.setVisibility(View.GONE)
            holder.tvAddQuestion.text = ""
            //holder.ivParticipate.setVisibility(View.GONE);
        }
    }

    private fun setUserImage(dp: DisplayPictureView, profileImage: String, isAvtar: Boolean) {
        dp.setDpWithImage(activity, isAvtar, profileImage, "", false)
    }

    private fun isVideoOwner(conversationModel: ConversationModel): Boolean {
        return conversationModel.ownerId.equals(userId, ignoreCase = true)
    }

    fun setMuted(isMuted: Boolean) {
        this.isMuted = isMuted
    }

    override fun onViewAttachedToWindow(viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is ViewHolder) {
            val holder = viewHolder as ViewHolder
            Utility.showLog("PlayerAD", "OnViewAttach " + holder.mTextureVideoView?.tag)
            val exploreViewModel: ExploreViewModel<*> =
                holder.flMain.tag as ExploreViewModel<*>
            if (holder.player == null) {
                if (exploreViewModel.type == ExploreVideoType.PUBLIC_VIDEO) {
                    val discoverVO: DiscoverModel = exploreViewModel.obj as DiscoverModel
                    if (discoverVO != null) {
                        discoverVO.isViewCountUpdated = false
                    }
                } else if (exploreViewModel.type == ExploreVideoType.RT) {
                    val conversationModel: ConversationModel =
                        exploreViewModel.obj as ConversationModel
                    if (conversationModel.chats != null && conversationModel.chats.size > 0) {
                        val chatModel: ChatModel =
                            conversationModel.chats[conversationModel.chats.size - 1]
                        chatModel.isViewCountUpdated = false
                    }
                    Utility.showLog("Player", exploreViewModel.feedURL)
                }
                holder.player = ExoPlayer.Builder(activity).build()
            }
            Glide.with(context).asDrawable().load(exploreViewModel.feedThumbnail).into(holder.ivThumbnail)
            holder.player!!.addMediaItem(MediaItem.fromUri(exploreViewModel.feedURL))
            holder.player!!.prepare()
            holder.player!!.seekTo(0L)
            holder.playerListener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        holder.mTextureVideoView?.alpha = 1f
                    } else if (playbackState == Player.STATE_ENDED) {
                        holder.player!!.seekTo(0)
                        holder.player!!.playWhenReady = true
                    }
                    super<Player.Listener>.onPlaybackStateChanged(playbackState)
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super<Player.Listener>.onMediaItemTransition(mediaItem, reason)
                }

                override fun onPlayerError(error: PlaybackException) {
                    super<Player.Listener>.onPlayerError(error)
                }
            }
            holder.player!!.addListener(holder.playerListener as Player.Listener)
            holder.player!!.playWhenReady = false
            holder.mTextureVideoView?.player = holder.player
            super.onViewAttachedToWindow(holder)
        } else {
            super.onViewAttachedToWindow(viewHolder as EndOfFeedViewHolder)
        }
    }

    override fun onViewDetachedFromWindow(viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is ViewHolder) {
            val holder = viewHolder as ViewHolder
            if (holder.mTextureVideoView != null && holder.player != null) {
                Utility.showLog("PlayerAD", "OnDetachAttach " + holder.mTextureVideoView.getTag())
                holder.mTextureVideoView.alpha = 0f
                if (holder.playerListener != null) {
                    holder.player!!.removeListener(holder.playerListener!!)
                }
                holder.player!!.stop()
                holder.player!!.clearMediaItems()
                holder.player = null
            }
            super.onViewDetachedFromWindow(holder)
        } else {
            val holder = viewHolder as EndOfFeedViewHolder
            super.onViewDetachedFromWindow(holder)
        }
    }

    override fun onViewRecycled(viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is ViewHolder) {
            val holder = viewHolder as ViewHolder
            if (holder.mTextureVideoView != null && holder.player != null) {
                Utility.showLog("Player", "OnViewRecycled " + holder.mTextureVideoView.getTag())
                holder.mTextureVideoView.alpha = 0f
                holder.player!!.stop()
                holder.player!!.clearMediaItems()
                holder.player = null
            }
        }
        super.onViewRecycled(viewHolder)
    }

    fun setInterfaceListener(feedAdapterListener: FeedAdapterListener?) {
        this.feedAdapterListener = feedAdapterListener
    }

    fun setEndOfFeedData(
        list: ArrayList<ConversationModel>?,
        isEndOfLoops: Boolean,
        feedPagerPosition: Int,
        loopSuggestionPagerEventListener: LoopSuggestionPagerEventListener?
    ) {
        this.feedPagerPosition = feedPagerPosition
        isEndOfSuggestedLoops = isEndOfLoops
        this.loopSuggestionPagerEventListener = loopSuggestionPagerEventListener
        if (feedPagerPosition == 0) {
            feedViewType = FeedViewType.MY_LOOPS.value
        } else if (feedPagerPosition == 1) {
            feedViewType = FeedViewType.SUBSCRIPTIONS.value
        }
        val exploreViewModel: ExploreViewModel<*> = ExploreViewModel<Any?>()
        exploreViewModel.type = ExploreVideoType.END_OF_FEED
        data.add(exploreViewModel)
        suggestedLoops = list
        notifyDataSetChanged()
    }

    inner class EndOfFeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var vpLoopSuggestions: ViewPager2
        var tvFeedEndDesc: TextView
        var btnGoToForYou: Button
        var llLoader: LinearLayout
        var ivAllCaughtUp: ImageView
        var tvFeedEndMsg: TextView

        init {
            vpLoopSuggestions = itemView.findViewById<ViewPager2>(R.id.vpLoopSuggestions)
            tvFeedEndDesc = itemView.findViewById<TextView>(R.id.tvFeedEndDesc)
            btnGoToForYou = itemView.findViewById<Button>(R.id.btnGoToForYou)
            llLoader = itemView.findViewById<LinearLayout>(R.id.llLoader)
            ivAllCaughtUp = itemView.findViewById<ImageView>(R.id.ivAllCaughtUp)
            tvFeedEndMsg = itemView.findViewById<TextView>(R.id.tvFeedEndMsg)
        }
    }

    inner class ViewHolder @SuppressLint("ClickableViewAccessibility") constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val flMain: FrameLayout
        val mTextureVideoView: StyledPlayerView?
        val ivThumbnail: TextureImageView

        //private final CustomIcon llSaveVideo, llShare, llReply, llLink, llDownloadVideo, llRepost, llMoreOptions;
        val ivSaveVideo: ImageView
        val ivDownloadVideo: ImageView
        val ivLink: ImageView
        val ivRepost: ImageView
        val ivShare: ImageView
        val ivReply: ImageView
        val ivMoreOptions: ImageView

        //private final CustomIcon llRTLink, llRTShare, llRTRepost, llRTMoreOptions, llParticipate;
        val ivRTLink: ImageView
        val ivRTRepost: ImageView
        val ivRTShare: ImageView
        val ivRTMoreOptions: ImageView
        private val ivParticipate: ImageView
        val llComments: LinearLayout
        private val llRTSpark: LinearLayout
        private val llSpark: LinearLayout
        val sparkViewRT: SparkView
        val sparkView: SparkView
        val llBottomLayout: LinearLayout
        private val llBottomInnerLayout: LinearLayout
        val llRTBottomLayout: LinearLayout
        private val llRTBottomInnerLayout: LinearLayout
        val tvPublicUserName: TextView
        val tvRTUserName: TextView
        val tvDesc: TextView
        val tvDescSingle: TextView
        val tvRTDesc: TextView
        val tvRTDescSingle: TextView
        val rlDesc: RelativeLayout
        val rlRTDesc: RelativeLayout
        val tvRTName: TextView
        val tvComments: TextView
        val tvAddQuestion: TextView
        val tvSparks: TextView
        val tvRTSparks: TextView
        val tvRecordedByText: TextView
        val tvRTRecordedByText: TextView
        val tvOgOwnerName: TextView
        var player: ExoPlayer? = null
        var playerListener: Player.Listener? = null
        private val tvName: TextView
        private val tvGenuinLogo: TextView
        private val tvFromUserName: TextView
        private val tvFromCameraRoll: TextView
        val tvOwnerConversationAt: TextView
        val tvRTOwnerConversationAt: TextView
        private val tvUserNameWaterMark: TextView
        private val tvFullNameWaterMark: TextView
        private val tvBioWaterMark: TextView
        private val ivWaterMarkProfile: CircleImageView
        val llDp: DisplayPictureView
        val llUserDp: DisplayPictureView
        val rlOverlay: RelativeLayout
        val llWhoCanSee: LinearLayout
        val llWhoCanSeeRT: LinearLayout
        val ivFlagged: ImageView
        val ivMuteIcon: ImageView
        val cardSubscribe: CardView
        val cardReposted: CardView
        val cardAddQuestion: MaterialCardView

        init {
            flMain = itemView.findViewById<FrameLayout>(R.id.flMain)
            mTextureVideoView = itemView.findViewById<StyledPlayerView>(R.id.videoView)
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail)
            ivSaveVideo = itemView.findViewById<ImageView>(R.id.ivSaveVideo)
            ivShare = itemView.findViewById<ImageView>(R.id.ivShare)
            ivReply = itemView.findViewById<ImageView>(R.id.ivReply)
            ivLink = itemView.findViewById<ImageView>(R.id.ivLink)
            llBottomLayout = itemView.findViewById<LinearLayout>(R.id.llBottomLayout)
            llRTBottomLayout = itemView.findViewById<LinearLayout>(R.id.llRTBottomLayout)
            ivDownloadVideo = itemView.findViewById<ImageView>(R.id.ivDownloadVideo)
            tvName = itemView.findViewById<TextView>(R.id.tvName)
            tvGenuinLogo = itemView.findViewById<TextView>(R.id.tvGenuinLogo)
            tvUserNameWaterMark = itemView.findViewById<TextView>(R.id.tvUserNameWaterMark)
            tvFullNameWaterMark = itemView.findViewById<TextView>(R.id.tvFullNameWaterMark)
            tvBioWaterMark = itemView.findViewById<TextView>(R.id.tvBioWaterMark)
            tvFromUserName = itemView.findViewById<TextView>(R.id.tvFromUserName)
            tvFromCameraRoll = itemView.findViewById<TextView>(R.id.tvFromCameraRoll)
            ivWaterMarkProfile = itemView.findViewById<CircleImageView>(R.id.ivWaterMarkProfile)
            tvRTName = itemView.findViewById<TextView>(R.id.tvRTName)
            tvComments = itemView.findViewById<TextView>(R.id.tvComments)
            llComments = itemView.findViewById<LinearLayout>(R.id.llComments)
            ivRTLink = itemView.findViewById<ImageView>(R.id.ivRTLink)
            ivRTShare = itemView.findViewById<ImageView>(R.id.ivRTShare)
            llDp = itemView.findViewById(R.id.llDp)
            llUserDp = itemView.findViewById(R.id.llUserDp)
            tvPublicUserName = itemView.findViewById<TextView>(R.id.tvPublicUserName)
            tvRTUserName = itemView.findViewById<TextView>(R.id.tvRTUserName)
            tvDesc = itemView.findViewById<TextView>(R.id.tvDesc)
            tvDescSingle = itemView.findViewById<TextView>(R.id.tvDescSingle)
            tvRTDesc = itemView.findViewById<TextView>(R.id.tvRTDesc)
            tvRTDescSingle = itemView.findViewById<TextView>(R.id.tvRTDescSingle)
            rlDesc = itemView.findViewById<RelativeLayout>(R.id.rlDesc)
            rlRTDesc = itemView.findViewById<RelativeLayout>(R.id.rlRTDesc)
            tvRecordedByText = itemView.findViewById<TextView>(R.id.tvRecordedByText)
            tvRTRecordedByText = itemView.findViewById<TextView>(R.id.tvRTRecordedByText)
            rlOverlay = itemView.findViewById<RelativeLayout>(R.id.rlOverlay)
            llWhoCanSee = itemView.findViewById<LinearLayout>(R.id.llWhoCanSee)
            llWhoCanSeeRT = itemView.findViewById<LinearLayout>(R.id.llWhoCanSeeRT)
            ivFlagged = itemView.findViewById<ImageView>(R.id.ivFlagged)
            ivMuteIcon = itemView.findViewById<ImageView>(R.id.ivMuteIcon)
            ivRTRepost = itemView.findViewById<ImageView>(R.id.ivRTRepost)
            ivRepost = itemView.findViewById<ImageView>(R.id.ivRepost)
            tvOgOwnerName = itemView.findViewById<TextView>(R.id.tvOgOwnerName)
            cardReposted = itemView.findViewById<CardView>(R.id.cardReposted)
            llBottomInnerLayout = itemView.findViewById<LinearLayout>(R.id.llBottomInnerLayout)
            llRTBottomInnerLayout = itemView.findViewById<LinearLayout>(R.id.llRTBottomInnerLayout)
            ivMoreOptions = itemView.findViewById<ImageView>(R.id.ivMoreOptions)
            ivRTMoreOptions = itemView.findViewById<ImageView>(R.id.ivRTMoreOptions)
            ivParticipate = itemView.findViewById<ImageView>(R.id.ivParticipate)
            tvOwnerConversationAt = itemView.findViewById<TextView>(R.id.tvOwnerConversationAt)
            tvRTOwnerConversationAt = itemView.findViewById<TextView>(R.id.tvRTOwnerConversationAt)
            cardSubscribe = itemView.findViewById<CardView>(R.id.cardSubscribe)
            cardAddQuestion = itemView.findViewById<MaterialCardView>(R.id.cardAddQuestion)
            tvAddQuestion = itemView.findViewById<TextView>(R.id.tvAddQuestion)
            sparkView = itemView.findViewById(R.id.sparkView)
            llSpark = itemView.findViewById<LinearLayout>(R.id.llSpark)
            tvSparks = itemView.findViewById<TextView>(R.id.tvSparks)
            sparkViewRT = itemView.findViewById(R.id.sparkViewRT)
            llRTSpark = itemView.findViewById<LinearLayout>(R.id.llRTSpark)
            tvRTSparks = itemView.findViewById<TextView>(R.id.tvRTSparks)
            val llRTBottom: LinearLayout = itemView.findViewById<LinearLayout>(R.id.llRTBottom)
            llBottomInnerLayout.post(Runnable {
                val layoutParams: LinearLayout.LayoutParams =
                    llBottomInnerLayout.getLayoutParams() as LinearLayout.LayoutParams
                layoutParams.topMargin = halfHeightPX
                llBottomInnerLayout.setLayoutParams(layoutParams)
            })
            llRTBottomInnerLayout.post(Runnable {
                val layoutParams: LinearLayout.LayoutParams =
                    llRTBottomInnerLayout.getLayoutParams() as LinearLayout.LayoutParams
                layoutParams.topMargin = halfHeightPX
                llRTBottomInnerLayout.setLayoutParams(layoutParams)
            })
            llRTSpark.setOnClickListener{
                feedAdapterListener?.onSparkClicked()
            }
            llSpark.setOnClickListener{
                feedAdapterListener?.onSparkClicked()
            }
            cardAddQuestion.setOnClickListener {
                feedAdapterListener?.onAskQuestionClicked()
            }
            ivRTRepost.setOnClickListener {
                feedAdapterListener?.onRepostClicked()
            }
            ivRepost.setOnClickListener {
                feedAdapterListener?.onRepostClicked()
            }
            llWhoCanSee.setOnClickListener(View.OnClickListener { view: View? ->
                if (feedAdapterListener != null) {
                    val exploreViewModel: ExploreViewModel<*> = data[getAbsoluteAdapterPosition()]
                    if (exploreViewModel.type == ExploreVideoType.PUBLIC_VIDEO) {
                        val discoverModel: DiscoverModel =
                            exploreViewModel.getObj() as DiscoverModel
                        if (discoverModel.getIsFlag() === 1) {
                            feedAdapterListener!!.onFlagVideoClicked()
                        } else {
                            feedAdapterListener!!.onUnlistedClicked()
                        }
                    } else {
                        feedAdapterListener!!.onUnlistedClicked()
                    }
                }
            })
            llWhoCanSeeRT.setOnClickListener(View.OnClickListener { view: View? ->
                feedAdapterListener?.onUnlistedClicked()
            })
            ivFlagged.setOnClickListener {
                feedAdapterListener?.onFlagVideoClicked()
            }
            ivShare.setOnClickListener {
                feedAdapterListener?.onShareClick()
            }
            ivRTShare.setOnClickListener {
                feedAdapterListener?.onShareClick()
            }
            ivReply.setOnClickListener {
                feedAdapterListener?.onReplyClick()
            }
            ivParticipate.setOnClickListener {
                feedAdapterListener?.onParticipateClicked()
            }
            ivLink.setOnClickListener {
                feedAdapterListener?.onLinkClick()
            }
            ivRTLink.setOnClickListener {
                feedAdapterListener?.onLinkClick()
            }
            ivSaveVideo.setOnClickListener {
                feedAdapterListener?.onSaveClick()
            }
            ivMoreOptions.setOnClickListener {
                feedAdapterListener?.onMoreOptionsClicked()
            }
            ivRTMoreOptions.setOnClickListener {
                feedAdapterListener?.onMoreOptionsClicked()
            }
            llDp.setOnClickListener { v ->
                feedAdapterListener?.onProfileClick(getAbsoluteAdapterPosition())
            }
            llUserDp.setOnClickListener { v ->
                feedAdapterListener?.onProfileClick(getAbsoluteAdapterPosition())
            }
            tvPublicUserName.setOnClickListener {
                feedAdapterListener?.onProfileClick(getAbsoluteAdapterPosition())
            }
            tvRTUserName.setOnClickListener {
                feedAdapterListener?.onProfileClick(getAbsoluteAdapterPosition())
            }
            cardReposted.setOnClickListener {
                feedAdapterListener?.onProfileClick(getAbsoluteAdapterPosition())
            }
            tvOgOwnerName.setOnClickListener {
                feedAdapterListener?.onRepostOwnerClicked(getAbsoluteAdapterPosition())
            }
            ivDownloadVideo.setOnClickListener {
                if (feedAdapterListener != null) {
                    val exploreViewModel: ExploreViewModel<*> = data[getAbsoluteAdapterPosition()]
                    if (exploreViewModel.type == ExploreVideoType.PUBLIC_VIDEO) {
                        val model: DiscoverModel = exploreViewModel.getObj() as DiscoverModel
                        Utility.printErrorLog("status: " + model.getImageUploadStatus() + " videoStatus: " + model.getVideoUploadStatus())
                        val downloadVideo = DownloadVideo(activity)
                        val textViewArrayList = ArrayList<TextView>()
                        textViewArrayList.add(tvGenuinLogo)
                        textViewArrayList.add(tvFromCameraRoll)
                        textViewArrayList.add(tvName)
                        textViewArrayList.add(tvUserNameWaterMark)
                        textViewArrayList.add(tvFromUserName)
                        textViewArrayList.add(tvFullNameWaterMark)
                        textViewArrayList.add(tvBioWaterMark)
                        downloadVideo.initDownload(model, textViewArrayList, ivWaterMarkProfile)
                        feedAdapterListener!!.onDownloadVideoClick(
                            absoluteAdapterPosition,
                            model,
                            downloadVideo
                        )
                    }
                }
            }
            llRTBottom.setOnClickListener{
                feedAdapterListener?.onDetailsClicked()
            }
            cardSubscribe.setOnClickListener {
                feedAdapterListener?.onSubscribeClicked()
            }
            llComments.setOnClickListener{
                feedAdapterListener?.onCommentsClicked()
            }
            tvDesc.setOnTouchListener { view: View, motionEvent: MotionEvent? ->
                view.parent.requestDisallowInterceptTouchEvent(true)
                false
            }
            tvDesc.setOnClickListener {
                feedAdapterListener?.onDescriptionClicked()
            }
            rlDesc.setOnClickListener {
                feedAdapterListener?.onDescriptionClicked()
            }
            tvRTDesc.setOnTouchListener { view: View, motionEvent: MotionEvent? ->
                view.parent.requestDisallowInterceptTouchEvent(true)
                false
            }
            tvRTDesc.setOnClickListener {
                feedAdapterListener?.onDescriptionClicked()
            }
            rlRTDesc.setOnClickListener{
                feedAdapterListener?.onDescriptionClicked()
            }
            rlOverlay.setOnClickListener{
                feedAdapterListener?.onOverlayClicked()
            }
        }
    }

    private fun getContentId(pos: Int): String {
        return data[pos].getFeedId()
    }

    private fun getContentCategory(pos: Int): String {
        return if (data[pos].type == ExploreVideoType.PUBLIC_VIDEO) {
            Constants.CATEGORY_PUBLIC_VIDEO
        } else {
            Constants.CATEGORY_RT
        }
    }

    private val eventRecordScreen: String?
        private get() = if (isFeedFragment2) {
            var eventType = type
            if (type.equals("hashtags", ignoreCase = true)) {
                eventType = Constants.SCREEN_EXPLORE
            }
            eventType
        } else {
            Constants.SCREEN_FEED
        }

    private fun applyLayoutTransition(viewGroup: ViewGroup) {
        val transition = LayoutTransition()
        transition.setDuration(300)
        transition.enableTransitionType(LayoutTransition.CHANGING)
        viewGroup.layoutTransition = transition
    }

    fun removeAt(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, data.size)
    }
}