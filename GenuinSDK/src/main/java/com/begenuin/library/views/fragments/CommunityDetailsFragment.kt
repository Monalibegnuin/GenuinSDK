package com.begenuin.library.views.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.begenuin.library.GenuinSDKApplication
import com.begenuin.library.R
import com.begenuin.library.common.Constants
import com.begenuin.library.common.Utility
import com.begenuin.library.common.Utility.getDBHelper
import com.begenuin.library.core.enums.VideoConvType
import com.begenuin.library.core.interfaces.LoopsAdapterListener
import com.begenuin.library.core.interfaces.ResponseListener
import com.begenuin.library.data.eventbus.CompressionCompletedEvent
import com.begenuin.library.data.eventbus.ConversationDeleteEvent
import com.begenuin.library.data.eventbus.ConversationUpdateEvent
import com.begenuin.library.data.eventbus.ConversationVideoProgressUpdateEvent
import com.begenuin.library.data.eventbus.LoopVideoAPICompleteEvent
import com.begenuin.library.data.eventbus.PublicVideoStatusChangedEvent
import com.begenuin.library.data.eventbus.VideoUploadedEvent
import com.begenuin.library.data.model.ChatModel
import com.begenuin.library.data.model.CommunityModel
import com.begenuin.library.data.model.DiscoverModel
import com.begenuin.library.data.model.LoopsModel
import com.begenuin.library.data.model.MemberInfoModel
import com.begenuin.library.data.model.MessageModel
import com.begenuin.library.data.model.VideoParamsModel
import com.begenuin.library.data.remote.BaseAPIService
import com.begenuin.library.data.remote.service.CompressionWorker
import com.begenuin.library.data.viewmodel.GenuinFFMpegManager
import com.begenuin.library.data.viewmodel.UploadQueueManager
import com.begenuin.library.data.viewmodel.VideoAPIManager
import com.begenuin.library.databinding.FragmentCommunityDetailsBinding
import com.begenuin.library.views.LoopsAdapter
import com.begenuin.library.views.activities.CameraNewActivity
import com.begenuin.library.views.adpters.UploadVideosAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.ExecutionException


/**
 * A simple [Fragment] subclass.
 * Use the [CommunityDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommunityDetailsFragment : Fragment(){

    private var communityId: String = ""
    var role: Int = 0
    var communityModel: CommunityModel? = null
    private var communityDetailsService: BaseAPIService? = null
    private var isCommunityDataLoaded: Boolean = false
    lateinit var _binding: FragmentCommunityDetailsBinding
    private var communityLoopsService: BaseAPIService? = null
    private var loopList: ArrayList<LoopsModel> = ArrayList()
    lateinit var adapter: LoopsAdapter
    private var isDataLoaded = false
    private var workManager: WorkManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            communityId = it.getString("community_id").toString()
            role = it.getInt("role")
        }
        workManager = WorkManager.getInstance(requireActivity())
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_community_details, container, false)
        return _binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(communityId: String?, role: Int) = CommunityDetailsFragment().apply {
            arguments = Bundle().apply {
                putString("community_id", communityId)
                putInt("role", role)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        if (context?.isFromCreate!! || context?.isFromDeepLink!!) {
//            ivBack.visibility = View.GONE
//            ivCloseCommunity.visibility = View.VISIBLE
//        } else {
            _binding.ivBack.visibility = View.VISIBLE
           // _binding.ivCloseCommunity.visibility = View.GONE
       // }

        _binding.ivBack.setOnClickListener {
            //context?.backManage()
            manageBackPress()
        }

//        _binding.ivCloseCommunity.setOnClickListener {
//            manageBackPress()
//        }

        setRoleActionClickListener()
        callCommunityBasicDetails()
        callGetCommunityLoops()
        failedLoopManagement()
        _binding.communityBasicDetails.fabPost.setOnClickListener{
            val intent = Intent(context, CameraNewActivity::class.java)
            intent.putExtra("from", Constants.FROM_ROUND_TABLE)
            intent.putExtra("community_id", communityId)
            intent.putExtra("community_handle", communityModel?.handle)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        }
//        if (context?.isFromDeepLink == true && context?.deepLinkType == NotificationType.COMMUNITY_JOIN.value) {
//            callMetaDataApiForCommunityInvite()
//        }
    }

    private fun showNoLoopsView() {
//        if (role == CommunityMemberRole.MODERATOR.value) {
//            addTemplateStarter()
//            addCreateLoopCell()
//        } else if (role == CommunityMemberRole.MEMBER.value) {
//            rlNoMessages.visibility = View.VISIBLE
//            tvNoLoopTitle.text = context?.resources?.getString(R.string.ready_to_start_loop)
//            tvNoLoopDesc.text = context?.resources?.getString(R.string.no_loops_community_msg)
//            llNewLoop.visibility = View.VISIBLE
//        } else {
        _binding.communityBasicDetails.rlNoMessages.visibility = View.VISIBLE
        _binding.communityBasicDetails.tvNoLoopTitle.text =  context?.resources?.getString(R.string.no_loops_yet)
        _binding.communityBasicDetails.tvNoLoopDesc.text = context?.resources?.getString(R.string.no_loops_community_non_member_msg)
        _binding.communityBasicDetails.llNewLoop.visibility = View.GONE
       // }
    }


//    @SuppressLint("SetTextI18n")
//    fun openBottomSheetDialog() {
//        val bottomSheetGuideline= layoutInflater.inflate(R.layout.bottom_sheet_guideline, null)
//        val recyclerView = bottomSheetGuideline.findViewById<RecyclerView>(R.id.recyclerView)
//        val welcomeTextLoop = bottomSheetGuideline.findViewById<TextView>(R.id.tvWelcomeText)
//        val crossButton = bottomSheetGuideline.findViewById<ImageView>(R.id.cross_button)
//        val neverMind= bottomSheetGuideline.findViewById<TextView>(R.id.never_mind)
//        val btnAcceptGuideline= bottomSheetGuideline.findViewById<MaterialButton>(R.id.btnAcceptGuideline)
//
//        welcomeTextLoop.text="Welcome to ${communityModel?.handle}"
//        val adapter = GuideLineBottomSheetAdapter(communityModel?.guideLines?: emptyList()) // Replace with your adapter
//        val layoutManager = LinearLayoutManager(requireContext()) // Replace with your desired layout manager
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = layoutManager
//
//        crossButton.setOnClickListener {
//            bottomSheetGuideLineDialog?.dismiss()
//        }
//        btnAcceptGuideline.setOnClickListener{
//            bottomSheetGuideLineDialog?.dismiss()
//            //join
//            CommunityMembershipManager.joinCommunity(
//                    context as Context, communityModel!!, Constants.SCREEN_COMMUNITY_DETAIL
//            )
//        }
//
//
//
//        neverMind.setOnClickListener {  bottomSheetGuideLineDialog?.dismiss() }
//        bottomSheetGuideLineDialog = BottomSheetDialog(requireContext(), R.style.SheetDialog1)
//        bottomSheetGuideLineDialog?.setContentView(bottomSheetGuideline)
//        bottomSheetGuideLineDialog?.setCancelable(true)
//
//        bottomSheetGuideLineDialog?.show()
//    }

    private fun setRoleActionClickListener() {
//        llRole.setOnClickListener {
//            when (role) {
//                CommunityMemberRole.MODERATOR.value -> {
//                    val i = Intent(activity, EditCommunityActivity::class.java)
//                    if (communityModel != null) {
//                        i.putExtra("community_model", communityModel)
//                    }
//                    editCommunityActivityLauncher?.launch(i)
//                    context?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//                    val map = HashMap<String, Any>()
//                    map[Constants.KEY_EVENT_RECORD_SCREEN] = Constants.SCREEN_COMMUNITY_DETAIL
//                    map[Constants.KEY_EVENT_TARGET_SCREEN] = Constants.SCREEN_COMMUNITY_EDIT
//                    map[Constants.KEY_CONTENT_ID] = communityId
//                    GenuInApplication.getInstance()
//                            .sendEventLogs(Constants.COMMUNITY_EDIT_FLOW_INITIATED, map)
//                }
//
//                CommunityMemberRole.MEMBER.value -> {
//                    //leave
//                    CommunityMembershipManager.leaveCommunity(
//                            context as Context, communityModel!!, Constants.SCREEN_COMMUNITY_DETAIL
//                    )
//                }
//
//                CommunityMemberRole.NONE.value -> {
//                    openBottomSheetDialog()
//                }
//            }
//        }
    }

    private fun setUpData() {
        if (communityModel == null) {
            return
        }
        _binding.communityBasicDetails.tvCommunityHandle.text = communityModel?.handle
        val name = communityModel?.name
        //_binding.tvCommunityName.text = name
        if (TextUtils.isEmpty(communityModel?.dp)) {
            _binding.communityBasicDetails.llCommunityDp.setDpWithInitials(
                name,
                communityModel?.colorCode,
                communityModel?.textColorCode
            )
        } else {
            _binding.communityBasicDetails.llCommunityDp.setDpWithImage(
                activity,
                false,
                communityModel?.dp,
                communityModel?.dpS,
                false
            )
        }

        if (!TextUtils.isEmpty(communityModel?.description)) {
            _binding.communityBasicDetails.tvCommunityDesc.visibility = View.VISIBLE
            _binding.communityBasicDetails.tvCommunityDesc.text = communityModel?.description
        } else {
            _binding.communityBasicDetails.tvCommunityDesc.visibility = View.GONE
        }


        // Set the community noOfLoops
        val noOfLoops = Utility.formatNumber(communityModel?.noOfLoops!!.toLong())
        _binding.communityBasicDetails.tvNoOfLoops.text = noOfLoops
        if (noOfLoops == "1") {
            _binding.communityBasicDetails.tvLoops.text = context?.resources?.getString(R.string.no_of_loop)
        } else {
            _binding.communityBasicDetails.tvLoops.text = context?.resources?.getString(R.string.no_of_loops)
        }

        // Set the community noOfVideos
        val noOfVideos = Utility.formatNumber(communityModel?.noOfVideos!!.toLong())
        _binding.communityBasicDetails.tvNoOfVideos.text = noOfVideos
        if (noOfVideos == "1") {
            _binding.communityBasicDetails.tvVideos.text =
                context?.resources?.getString(R.string.no_of_video_caps)
        } else {
            _binding.communityBasicDetails.tvVideos.text =
                context?.resources?.getString(R.string.no_of_videos_caps)
        }

        role = communityModel?.loggedInUserRole!!

        // Set the community role info
//        when (communityModel?.loggedInUserRole) {
//
//            CommunityMemberRole.MODERATOR.value -> {
//                llRole.background = AppCompatResources.getDrawable(
//                    context as Context,
//                    R.drawable.edit_option_border_bg
//                )
//                tvRoleAction.setTextColor(resources.getColor(R.color.colorPrimary, null))
//                tvRoleAction.text = context?.resources?.getString(R.string.mod_tools)
//            }
//
//            CommunityMemberRole.MEMBER.value -> {
//                llRole.background = AppCompatResources.getDrawable(
//                    context as Context,
//                    R.drawable.edit_option_border_bg
//                )
//                tvRoleAction.setTextColor(resources.getColor(R.color.colorPrimary, null))
//                tvRoleAction.text = context?.resources?.getString(R.string.joined)
//            }
//
//            else -> {
//                llRole.background =
//                    AppCompatResources.getDrawable(context as Context, R.drawable.join_option_bg)
//                tvRoleAction.setTextColor(resources.getColor(R.color.colorWhite, null))
//                tvRoleAction.text = context?.resources?.getString(R.string.join)
//            }
//        }
    }

    private fun callCommunityBasicDetails() {
        try {

            if (communityDetailsService != null) {
                communityDetailsService?.cancelCall()
            }
            val map: MutableMap<String, Any> = HashMap()
            map["community_id"] = communityId

            communityDetailsService = BaseAPIService(
                context,
                Constants.GET_COMMUNITY,
                true,
                "",
                map,
                object : ResponseListener {
                    override fun onSuccess(response: String) {
                        _binding.shimmerBasicDetails.visibility = View.GONE
                       // _binding.communityBasicDetails. = View.VISIBLE
                        val jsonObject = JSONObject(response)
                        val dataJson = jsonObject.getJSONObject(Constants.JSON_DATA)
                        communityModel =
                            Gson().fromJson(dataJson.toString(), CommunityModel::class.java)
                        if (communityModel != null && dataJson.has("social_links")) {
                            val jsonSocialLinks = dataJson.getJSONObject("social_links")

                            // Twitter link parsing
                            if (jsonSocialLinks.has("twitter")) {
                                val jsonTwitterLink = jsonSocialLinks.getJSONObject("twitter")

                                if (!jsonTwitterLink.isNull("id")) {
                                    communityModel?.twitterId = jsonTwitterLink.optString("id", "")
                                } else {
                                    communityModel?.twitterId = ""
                                }

                                communityModel?.twitterURL = jsonTwitterLink.optString("url", "")
                            }

                            // LinkedIn link parsing
                            if (jsonSocialLinks.has("linkedin")) {
                                val jsonLinkedInLink = jsonSocialLinks.getJSONObject("linkedin")

                                if (!jsonLinkedInLink.isNull("id")) {
                                    communityModel?.linkedinId =
                                        jsonLinkedInLink.optString("id", "")
                                } else {
                                    communityModel?.linkedinId = ""
                                }

                                communityModel?.linkedinURL = jsonLinkedInLink.optString("url", "")
                            }

                            // Instagram link parsing
                            if (jsonSocialLinks.has("insta")) {
                                val jsonInstaLink = jsonSocialLinks.getJSONObject("insta")

                                if (!jsonInstaLink.isNull("id")) {
                                    communityModel?.instaId = jsonInstaLink.optString("id", "")
                                } else {
                                    communityModel?.instaId = ""
                                }

                                communityModel?.instaURL = jsonInstaLink.optString("url", "")
                            }
                        }

                        setUpData()

                        var nextItem = 0
                        val totalIds = mutableListOf(1, 2, 3, 4, 5)
                        val completedStepList = mutableListOf<Int>()
                        for (item in communityModel?.communitySetupModel?.steps ?: listOf()) {
                            item.id?.let { completedStepList.add(it) }
                        }
                        for (id in totalIds) {
                            if (!completedStepList.contains(id)) {
                                nextItem = id
                                break
                            }
                        }
                        communityDetailsService = null
                        isCommunityDataLoaded = true
                    }

                    override fun onFailure(error: String) {
                        communityDetailsService = null
                        if (!TextUtils.isEmpty(error) && error.equals("404", ignoreCase = true)) {
                            Utility.showToast(
                                context,
                                context?.resources?.getString(R.string.community_longer_available)
                            )
                             manageBackPress()
                        }
                    }
                },
                "GET_DATA",
                false
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun callGetCommunityLoops() {
        try {
            if (communityLoopsService != null) {
                communityLoopsService?.cancelCall()
            }
            val map: MutableMap<String, Any> = java.util.HashMap()
            map["community_id"] = communityId

            communityLoopsService = BaseAPIService(
                context,
                Constants.GET_COMMUNITY_LOOPS,
                true,
                "",
                map,
                object : ResponseListener {
                    override fun onSuccess(response: String) {
                        val jsonObject = JSONObject(response)
                        val dataJson = jsonObject.getJSONObject(Constants.JSON_DATA)
                        if (dataJson.has("conversations")) {

                            // parsing of loops and convert it into list
                            val jsonArray = dataJson.getJSONArray("conversations")
                            val gson = Gson()
                            val loopListData = object :
                                TypeToken<java.util.ArrayList<LoopsModel?>?>() {}.type
                            val loopDataList = gson.fromJson<List<LoopsModel>>(
                                jsonArray.toString(),
                                loopListData
                            )

                            loopList.clear()

                            val myLoops = java.util.ArrayList<LoopsModel>()

                            for (i in loopDataList.indices) {
                                val model = loopDataList[i]
                                if (model.latestMessages!!.isNotEmpty()) {
                                    for (j in model.latestMessages!!.indices) {
                                        val messageModel = model.latestMessages!![j]
                                        messageModel.chatId = model.chatId
                                        messageModel.videoUploadStatus = 3
                                        messageModel.imageUploadStatus = 2
                                        messageModel.dpUploadStatus = 2
                                        messageModel.compressionStatus = 1
                                        messageModel.convType = VideoConvType.ROUND_TABLE.value
                                    }
                                }
                                loopList.add(model)
                                if (model.memberInfo != null) {
                                    model.communityId = communityId
                                    myLoops.add(model)
                                }
                            }

                            if (myLoops.isNotEmpty()) {
                                if (getDBHelper() != null) {
                                    getDBHelper()!!.insertORUpdateLoops(myLoops)
                                }
                            }
                            pendingLoopsOrMessageManagement()
//                            if (loopList.isNotEmpty()) {
//                                addCreateLoopCell()
//                                addTemplateStarter()
//                            }
                            setAdapter(false)
                        }
                        communityLoopsService = null
                        isDataLoaded = true
                    }

                    override fun onFailure(error: String) {
                        communityLoopsService = null
                    }
                },
                "GET_DATA",
                false
            )

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun setAdapter(isShimmerNeeded: Boolean, newlyCreatedLoopId: String = "") {
        var alreadyExpandedIds: List<String?> = java.util.ArrayList()
        if (loopList.isNotEmpty()) {
            alreadyExpandedIds = loopList.filter { it.isExpanded }.map { it.chatId }
        }

        if (loopList.isNotEmpty()) {
            // Filter pending upload list to show as a separate list in card
            for (loop in loopList) {
                loop.pendingUploadList = loop.latestMessages?.filter {
                    it.videoUploadStatus != 3
                }
                loop.isExpanded = alreadyExpandedIds.contains(loop.chatId) || (!TextUtils.isEmpty(
                    newlyCreatedLoopId
                ) && newlyCreatedLoopId == loop.chatId)
            }
        }
        if (isShimmerNeeded) {
            _binding.communityBasicDetails.rlNoMessages.visibility = View.GONE
            if (loopList.isEmpty()) {
                //if (role == CommunityMemberRole.MODERATOR.value || role == CommunityMemberRole.MEMBER.value) {
                    _binding.communityBasicDetails.layoutShimmerCreateLoop.visibility = View.VISIBLE
//                } else {
//                    layoutShimmerCreateLoop.visibility = View.GONE
//                }
                _binding.communityBasicDetails.shimmerScroll.visibility = View.VISIBLE
                _binding.communityBasicDetails.shimmerLoops.startShimmer()
            } else if (_binding.communityBasicDetails.shimmerLoops.isShimmerVisible) {
                _binding.communityBasicDetails.shimmerLoops.stopShimmer()
                _binding.communityBasicDetails.shimmerScroll.visibility = View.GONE
            } else {
                _binding.communityBasicDetails.shimmerScroll.visibility = View.GONE
            }
        } else {
            if (_binding.communityBasicDetails.shimmerLoops.isShimmerVisible) {
                _binding.communityBasicDetails.shimmerLoops.stopShimmer()
            }
            _binding.communityBasicDetails.shimmerScroll.visibility = View.GONE
            if (loopList.isEmpty()) {
                _binding.communityBasicDetails.rlNoMessages.visibility = View.VISIBLE
            } else {
                _binding.communityBasicDetails.rlNoMessages.visibility = View.GONE
            }
        }

        //Set adapter to show data in recyclerview
        if (!this::adapter.isInitialized) {
            adapter = activity?.let {
                LoopsAdapter(it, loopList, false, object : LoopsAdapterListener {
                    override fun onLoopClicked(loop: LoopsModel) {
//                        val i = Intent(activity, LoopDetailsActivity::class.java)
//                        i.putExtra("chat_id", loop.chatId)
//                        startActivity(i)
//                        activity?.overridePendingTransition(
//                            R.anim.slide_in_right,
//                            R.anim.slide_out_left
//                        )
                    }

                    override fun onCreateLoopClicked() {
                        //goToCameraForCreateLoop()
                    }

                    override fun onThumbnailStackClicked(loop: LoopsModel) {
//                        if (loop.latestMessages != null && loop.latestMessages!!.isNotEmpty()) {
//                            if (parentFragment != null) {
//                                val model =
//                                    (parentFragment as CommunityDetailsFragment).communityModel
//                                var communityHandle = ""
//                                if (model != null) {
//                                    communityHandle = model.handle
//                                }
//                                val fragmentManager = parentFragment?.childFragmentManager
//                                val fragmentTransaction = fragmentManager?.beginTransaction()
//                                fragmentTransaction?.setCustomAnimations(
//                                    R.anim.slide_in_right,
//                                    R.anim.slide_out_left,
//                                    R.anim.slide_in_left,
//                                    R.anim.slide_out_right
//                                )
//                                val fragment = FeedLoopFragment.newInstance(
//                                    loop.chatId,
//                                    loop.latestMessages!![0].messageId,
//                                    0,
//                                    loop.group,
//                                    loop.settings,
//                                    loop.shareUrl,
//                                    loop.isSubscriber,
//                                    Utility.prepareMemberInfo(context, loop),
//                                    communityHandle,
//                                    false
//                                )
//                                val tag = "feedloop"
//                                fragmentTransaction?.add(
//                                    R.id.community_loop_fragment_container,
//                                    fragment,
//                                    tag
//                                )
//                                fragmentTransaction?.addToBackStack(tag)
//                                fragmentTransaction?.commit()
//                            }
//                        }
                    }
                })
            }!!
            _binding.communityBasicDetails.rvCommunityLoops.parent?.requestDisallowInterceptTouchEvent(true)
            _binding.communityBasicDetails.rvCommunityLoops.layoutManager = LinearLayoutManager(context)
            _binding.communityBasicDetails.rvCommunityLoops.adapter = adapter
        } else {
            adapter.updateData(loopList)
        }
    }

    fun manageBackPress() {
        activity?.supportFragmentManager?.popBackStack()
        activity?.finish()
//        if (fragment is NewProfileFragment) {
//            val childFm = fragment.getChildFragmentManager()
//            val childIndex = childFm.backStackEntryCount
//            if (childIndex > 0) {
//                if (childIndex > 1) {
//                    val childBackStackEntry = childFm.getBackStackEntryAt(childIndex - 2)
//                    val childTag = childBackStackEntry.name
//                    val childFragment = childFm.findFragmentByTag(childTag)
//                    childFm.popBackStackImmediate()
//                    when (childFragment) {
//                        is FeedFragment2 -> {
//                            childFragment.playCurrentVideo()
//                            context?.changeStatusBarColorBlack()
//                        }
//
//                        is NewProfileFragment -> {
//                            context?.changeStatusBarColorWhite()
//                        }
//
//                        is FeedLoopFragment -> {
//                            childFragment.playCurrentVideo()
//                            context?.changeStatusBarColorBlack()
//                        }
//                    }
//                } else {
//                    childFm.popBackStackImmediate()
//                    context?.changeStatusBarColorWhite()
//                }
//            } else {
//                if (index > 0) {
//                    val backActualEntry = childFragmentManager.getBackStackEntryAt(index - 1)
//                    val tagActual = backActualEntry.name
//                    val actualFragment = childFragmentManager.findFragmentByTag(tagActual)
//                    childFragmentManager.popBackStackImmediate()
//                    when (actualFragment) {
//                        is FeedFragment2 -> {
//                            actualFragment.playCurrentVideo()
//                            context?.changeStatusBarColorBlack()
//                        }
//
//                        is NewProfileFragment -> {
//                            context?.changeStatusBarColorWhite()
//                        }
//
//                        is FeedLoopFragment -> {
//                            actualFragment.playCurrentVideo()
//                            context?.changeStatusBarColorBlack()
//                        }
//                    }
//                } else {
//                    childFragmentManager.popBackStackImmediate()
//                    context?.changeStatusBarColorWhite()
//                }
//            }
//        } else if (fragment is FeedLoopFragment) {
//            if (index > 0) {
//                val backActualEntry = childFragmentManager.getBackStackEntryAt(index - 1)
//                val tagActual = backActualEntry.name
//                val actualFragment = childFragmentManager.findFragmentByTag(tagActual)
//                childFragmentManager.popBackStackImmediate()
//                if (actualFragment is FeedFragment2) {
//                    actualFragment.playCurrentVideo()
//                    context?.changeStatusBarColorBlack()
//                } else if (actualFragment is NewProfileFragment) {
//                    context?.changeStatusBarColorWhite()
//                }
//            } else {
//                childFragmentManager.popBackStackImmediate()
//                context?.changeStatusBarColorWhite()
//                EventBus.getDefault().post(ConversationUpdateEvent(true))
//            }
//        }
    }

    @Subscribe
    fun onLoopVideoAPISuccess(loopVideoAPICompleteEvent: LoopVideoAPICompleteEvent) {
        Utility.showToast(activity, "Video Uploaded Successfully")
        activity?.runOnUiThread { handleUploadAPIComplete(loopVideoAPICompleteEvent) }
    }

    @Subscribe
    fun onVideoUploadProgress(model: ConversationVideoProgressUpdateEvent) {
        activity?.runOnUiThread {
            updateVideoProgress(model)
        }
    }
    private fun handleUploadAPIComplete(model: LoopVideoAPICompleteEvent) {
        var isDataUpdated = false

        for (i in loopList.indices) {
            val loopModel: LoopsModel =
                loopList[i]
            if (loopModel.chatId.equals("-102") || loopModel.chatId.equals("-103")) {
                continue
            }

            if (loopModel.latestMessages?.isNotEmpty() == true) {
                for (j in loopModel.latestMessages!!.indices) {
                    val messageModel = loopModel.latestMessages!![j]
                    if (!TextUtils.isEmpty(messageModel.localVideoPath) && messageModel.localVideoPath.equals(
                            model.localVideoPath,
                            ignoreCase = true
                        )
                    ) {
                        if (!TextUtils.isEmpty(model.createdLoopId)) {
                            loopModel.chatId = model.createdLoopId
                            messageModel.chatId = model.createdLoopId
                        }
                        val mainViewHolder = _binding.communityBasicDetails.rvCommunityLoops.findViewHolderForAdapterPosition(i)
                        if (mainViewHolder is LoopsAdapter.LoopsViewHolder) {
                            val viewHolder =
                                mainViewHolder.rvUploadList?.findViewHolderForAdapterPosition(
                                    j
                                ) as UploadVideosAdapter.VideosViewHolder?
                            if (viewHolder != null && mainViewHolder.rvUploadList?.adapter != null) {
                                isDataUpdated = true
                                (mainViewHolder.rvUploadList?.adapter as UploadVideosAdapter?)!!.playUploadCompleteAnim(
                                    viewHolder
                                )
                                Handler(Looper.getMainLooper()).postDelayed({
                                    if (activity?.isDestroyed == false) {
                                        syncUpdateWithDB()
                                    }
                                }, 2500)
                            }
                        }
                        adapter.notifyDataSetChanged()
                        break
                    }
                }
            }
        }
        if (!isDataUpdated) {
            syncUpdateWithDB()
        }
    }

    private fun syncUpdateWithDB() {
        val dbLoopList = getDBHelper()!!.loops
        for (i in dbLoopList.indices) {
            val dbModel = dbLoopList[i]
            val memberData = MemberInfoModel()
            memberData.status = "active"
            memberData.role = "Moderator"
            dbModel.memberInfo = memberData
            for (j in loopList.indices) {
                val model = loopList[j]
                if (dbModel.chatId.equals(model.chatId)) {
                    if (model.chatId.equals("-101")) {
                        if (model.latestMessages != null && dbModel.latestMessages != null) {
                            if (model.latestMessages!![0].localVideoPath.equals(dbModel.latestMessages!![0].localVideoPath)) {
                                val isExpanded = loopList[j].isExpanded
                                loopList[j] = dbModel
                                loopList[j].isExpanded = isExpanded
                            }
                        }
                    } else {
                        val isExpanded = loopList[j].isExpanded
                        loopList[j] = dbModel
                        loopList[j].isExpanded = isExpanded
                    }
                }
            }
        }
        val sortedList = loopList.sortedWith(compareBy { it.latestMessageAt })
        loopList = ArrayList(sortedList.reversed())
        setAdapter(false)
    }

    private fun updateVideoProgress(model: ConversationVideoProgressUpdateEvent) {
        Utility.showLog("CommunityDetailsFragment", "Video upload is in progress")
        for (i in loopList.indices) {
            val loopsModel: LoopsModel =
                loopList[i]
            if (loopsModel.chatId.equals("-102") || loopsModel.chatId.equals("-103")) {
                continue
            }
            if (loopsModel.latestMessages?.isNotEmpty() == true) {
                for (j in loopsModel.latestMessages!!.indices) {
                    val messageModel = loopsModel.latestMessages!![j]
                    if (!TextUtils.isEmpty(messageModel.localVideoPath) && messageModel.localVideoPath.equals(
                            model.localVideoPath,
                            ignoreCase = true
                        )
                    ) {
                        messageModel.uploadProgress = model.progress
                        val mainViewHolder = _binding.communityBasicDetails.rvCommunityLoops?.findViewHolderForAdapterPosition(i)
                        if (mainViewHolder is LoopsAdapter.LoopsViewHolder) {
                            val viewHolder =
                                mainViewHolder.rvUploadList?.findViewHolderForAdapterPosition(
                                    j
                                ) as UploadVideosAdapter.VideosViewHolder?
                            if (viewHolder != null && mainViewHolder.rvUploadList?.adapter != null) {
                                (mainViewHolder.rvUploadList?.adapter as UploadVideosAdapter?)!!.updateVideoProgress(
                                    viewHolder,
                                    model.progress
                                )
                            }
                        }
                        break
                    }
                }
            }
        }
    }

    private fun pendingLoopsOrMessageManagement() {
        val pendingLoops = getDBHelper()!!.getPendingCommunityLoops(communityId)
        val pendingMessages = getDBHelper()!!.pendingMessages

        if (pendingMessages.isNotEmpty()) {
            for (i in loopList.indices) {
                val model = loopList[i]
                if (model.chatId.equals("-102") || model.chatId.equals("-101") || model.chatId.equals("-103")) {
                    continue
                }
                model.latestMessages?.filter { it.videoUploadStatus != 3 }
                    ?.let { model.latestMessages?.removeAll(it.toSet()) }
                for (j in pendingMessages.indices) {
                    val messageModel = pendingMessages[j]
                    if (messageModel.chatId == model.chatId) {
                        model.latestMessages?.add(0, messageModel)
                    }
                }
                model.pendingUploadList = model.latestMessages?.filter {
                    it.videoUploadStatus != 3
                }
            }
        }

        if (pendingLoops.isNotEmpty()) {
//            if (loopList.isEmpty()) {
//                addCreateLoopCell()
//                addTemplateStarter()
//            }
            val loopsToDelete = loopList.filter { it.chatId.equals("-101") }

            if (loopsToDelete.isNotEmpty()) {
                for (i in loopsToDelete.indices) {
                    for (j in pendingLoops.indices) {
                        if (pendingLoops[j].latestMessages!![0].localVideoPath?.equals(loopsToDelete[i].latestMessages!![0].localVideoPath)!!) {
                            pendingLoops[j].isExpanded = loopsToDelete[i].isExpanded
                        }
                    }
                }
            }

            loopList.removeAll(loopList.filter { it.chatId.equals("-101") }.toSet())

            loopList.addAll(1, pendingLoops)
        }

        if (pendingLoops.isNotEmpty() || pendingMessages.isNotEmpty()) {
            failedLoopManagement()
            syncUpdateWithDB()
        }
    }

    @Subscribe
    fun onVideoUploaded(videoUploaded: VideoUploadedEvent)
    {
        val extraParams: VideoParamsModel = videoUploaded.videoParamsModel!!
        extraParams.videoURL = extraParams.videoFile
        if (getDBHelper() != null) {
            extraParams.shareURL = getDBHelper()!!.getShareURLForRT(extraParams.videoFile)
        }
        VideoAPIManager.sendVideoAPI(requireActivity(), "round_table", extraParams)
    }

    @Subscribe
    fun onCompressionCompleted(compressionCompleted: CompressionCompletedEvent)
    {
        val from = compressionCompleted.from;
        val path = compressionCompleted.path;

        if (GenuinSDKApplication.isInForGround) {
            if (from.equals(
                    Constants.FROM_PUBLIC_VIDEO,
                    ignoreCase = true
                ) || from.equals(Constants.FROM_RECORD_FOR_OTHER, ignoreCase = true)
            ) {
                val discoverModel: DiscoverModel? =
                    getDBHelper()!!.getCompressedPublicVideo(path)
                if (discoverModel != null) {
                    UploadQueueManager.getInstance()
                        .uploadPublicVideo(activity, discoverModel)
                }
            } else if (from.equals(Constants.FROM_REACTION, ignoreCase = true) || from.equals(
                    Constants.FROM_DIRECT,
                    ignoreCase = true
                ) || from.equals(Constants.FROM_GROUP, ignoreCase = true)
            ) {
                val chatModel: ChatModel? = getDBHelper()!!.getCompressedChatVideo(path)
                if (chatModel != null) {
                    //uploadReaction(chatModel)
                }
            } else if (from.equals(Constants.FROM_ROUND_TABLE, ignoreCase = true)) {
                val loopsModel = getDBHelper()!!.getLoopByLocalVideoPath(path)
                loopsModel?.let { uploadLoop(it) }
            } else if (from.equals(Constants.FROM_CHAT, ignoreCase = true)) {
                if (compressionCompleted.convType == VideoConvType.ROUND_TABLE.value) {
                    val messageModel: MessageModel? =
                        getDBHelper()!!.getLoopVideoByLocalPath(path)
                    if (messageModel != null) {
                        uploadLoopVideo(messageModel)
                    }
                } else {
//                    val chatModel: ChatModel? = Utility.getDBHelper()!!.getCompressedChatVideo(path)
//                    if (chatModel != null) {
//                        uploadChat(chatModel)
//                    }
                }
            } else if (from.equals(Constants.FROM_COMMENT, ignoreCase = true)) {
//                val commentModel: CommentModel? =
//                    Utility.getDBHelper()!!.getCompressedCommentVideo(path)
//                if (commentModel != null) {
//                    uploadComment(commentModel)
//                }
            }
        } else {
            updateRetry(from, path, compressionCompleted.convType)
            Utility.displayNotification(requireActivity(), from, path, compressionCompleted.convType)
        }
    }

    private fun uploadLoopVideo(messageModel: MessageModel?) {
        UploadQueueManager.getInstance().uploadLoopVideo(activity, messageModel)
    }

    private fun updateRetry(from: String, path: String, convType: Int) {
        if (from.equals(Constants.FROM_CHAT, ignoreCase = true)) {
            if (convType == VideoConvType.ROUND_TABLE.value) {
                if (getDBHelper() != null) {
                    val messageModel: MessageModel? =
                        getDBHelper()!!.getLoopVideoByLocalPath(path)
                    if (messageModel != null) {
                        // Update retry status for particular loop video in DB by local path
                        getDBHelper()!!.updateRetryStatusForLoopVideo(messageModel.localVideoPath, false)
                        EventBus.getDefault().post(ConversationUpdateEvent(true))
                    }
                }
            } else {
                if (getDBHelper() != null) {
                    val chatModel = getDBHelper()!!.getCompressedChatVideo(path)
                    if (chatModel != null) {
                        // Changed the way to storing retry status to DB, now it will store Retry status on the basic of localVideoPath
                        getDBHelper()!!.updateRetryStatus(chatModel.localVideoPath, true)
                        EventBus.getDefault().post(ConversationUpdateEvent(false))
                    }
                }
            }
        } else if (from.equals(Constants.FROM_REACTION, ignoreCase = true)) {
            if (getDBHelper() != null) {
                val chatModel = getDBHelper()!!.getCompressedChatVideo(path)
                if (chatModel != null) {
                    // Changed the way to storing retry status to DB, now it will store Retry status on the basic of localVideoPath
                    getDBHelper()!!.updateRetryStatus(chatModel.localVideoPath, true)
                    EventBus.getDefault()
                        .post(ConversationUpdateEvent(chatModel.convType === VideoConvType.ROUND_TABLE.value))
                }
            }
        } else if (from.equals(Constants.FROM_DIRECT, ignoreCase = true) || from.equals(Constants.FROM_GROUP, ignoreCase = true)
        ) {
            if (getDBHelper() != null) {
                // Changed the way to storing retry status to DB, now it will store Retry status on the basic of localVideoPath
                getDBHelper()!!.updateRetryStatus(path, true)
            }
            EventBus.getDefault().post(ConversationUpdateEvent(false))
        } else if (from.equals(Constants.FROM_ROUND_TABLE, ignoreCase = true)) {
            val messageModel: MessageModel? = getDBHelper()!!.getLoopVideoByLocalPath(path)
            if (messageModel != null) {
                // Update retry status for particular loop video in DB by local path
                getDBHelper()!!.updateRetryStatusForLoopVideo(messageModel.localVideoPath, true)
                EventBus.getDefault().post(ConversationUpdateEvent(true))
            }
        } else if (from.equals(Constants.FROM_COMMENT, ignoreCase = true)) {
//            if (Utility.getDBHelper() != null) {
//                Utility.getDBHelper()!!.updateCommentRetryStatus(path, true)
//            }
//            val comment = PostCommentEvent()
//            comment.isRetry = true
//            comment.localFilePath = path
//            EventBus.getDefault().post(comment)
        } else if (from.equals(Constants.FROM_PUBLIC_VIDEO, ignoreCase = true) || from.equals(
                Constants.FROM_RECORD_FOR_OTHER,
                ignoreCase = true
            )
        ) {
            if (getDBHelper() != null) {
                getDBHelper()!!.updateProfileRetryStatus(path, true)
                getDBHelper()!!.updatePublicVideoStatus(path, 1)
                getDBHelper()!!.updatePublicImageStatus(path, 1)
                val publicVideo = PublicVideoStatusChangedEvent()
                publicVideo.videoLocalPath = path
                publicVideo.isRetry = true
                publicVideo.videoUploadStatus = 1
                publicVideo.imageUploadStatus = 1
                publicVideo.apiUploadStatus = 0
                publicVideo.compressionStatus = 1
                EventBus.getDefault().post(publicVideo)
            }
        }
    }
    private fun uploadLoop(loop: LoopsModel?) {
        UploadQueueManager.getInstance().uploadLoop(requireActivity(), loop)
    }
    override fun onResume() {
        super.onResume()
        if (isDataLoaded) {
            pendingLoopsOrMessageManagement()
        }
    }

    private fun failedLoopManagement() {
        val pendingLoops = getDBHelper()!!
            .pendingLoops
        val pendingMessages = getDBHelper()!!
            .pendingMessages
        if (pendingLoops.size > 0) {
            for (i in pendingLoops.indices) {
                val loopsModel = pendingLoops[i]
                if (loopsModel.latestMessages != null && loopsModel.latestMessages!!.size > 0
                ) {
                    val messageModel: MessageModel = loopsModel.latestMessages!![0]
                    if (messageModel.compressionStatus == 0) {
                        startFFMpegCommand(
                            messageModel.ffMpegCommand!!,
                            messageModel.localVideoPath!!,
                            Constants.FROM_ROUND_TABLE,
                            VideoConvType.ROUND_TABLE.value,
                            loopsModel.chatId!!
                        )
                    } else if (!messageModel.isVideoAndImageUploaded()) {
                        uploadLoop(loopsModel)
                    } else {
                        VideoAPIManager.retryAPILoop(requireActivity(), loopsModel)
                    }
                }
            }
        }
        if (pendingMessages.size > 0) {
            for (i in pendingMessages.indices) {
                val messageModel = pendingMessages[i]
                if (messageModel.compressionStatus == 0) {
                    startFFMpegCommand(
                        messageModel.ffMpegCommand!!,
                        messageModel.localVideoPath!!,
                        Constants.FROM_CHAT,
                        VideoConvType.ROUND_TABLE.value,
                        messageModel.chatId!!
                    )
                } else if (!messageModel.isVideoAndImageUploaded()) {
                    uploadLoopVideo(messageModel)
                } else {
                    VideoAPIManager.retryAPILoopVideo(requireActivity(), messageModel)
                }
            }
        }
    }


    private fun startFFMpegCommand(
        command: String,
        tag: String,
        from: String,
        convType: Int,
        chatId: String
    ) {
        cancelLastSession(tag)
        GenuinFFMpegManager.getInstance().addValueToHashmap(tag, true)
        val mergeRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(CompressionWorker::class.java)
            .setInputData(createInputDataForUri(command, tag, from, convType, chatId))
            .addTag(tag)
            .build()
        workManager!!.enqueue(mergeRequest)
    }

    private fun createInputDataForUri(
        command: String,
        path: String,
        from: String,
        convType: Int,
        chatId: String
    ): Data {
        val builder = Data.Builder()
        builder.putString("path", path)
        builder.putString("command", command)
        builder.putString("from", from)
        builder.putString("whichSession", Constants.SESSION_MERGE)
        builder.putInt("convType", convType)
        builder.putString("chatId", chatId)
        return builder.build()
    }

    private fun cancelLastSession(tag: String) {
        val list = workManager!!.getWorkInfosByTag(tag)
        try {
            val workInfoList = list.get()
            for (workInfo in workInfoList) {
                val state = workInfo.state
                val sessionId = workInfo.outputData.getLong("sessionId", -1)
                if (sessionId != -1L) {
                    FFmpegKit.cancel(sessionId)
                }
                Utility.showLog("TAG", state.toString())
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        workManager!!.cancelAllWorkByTag(tag)
    }

    @Subscribe
    fun onConversationUpdate(conversationUpdate: ConversationUpdateEvent?) {
        activity?.runOnUiThread {
            if (conversationUpdate != null) {
                if (conversationUpdate.isRT) {
                    syncUpdateWithDB()
                }
            }
        }
    }

    @Subscribe
    fun onConversationDelete(conversationDeleteEvent: ConversationDeleteEvent?) {
        activity?.runOnUiThread {
            if (conversationDeleteEvent != null) {
                val chatId = conversationDeleteEvent.chatId
                for (i in loopList.indices) {
                    val loopModel: LoopsModel =
                        loopList[i]
                    if (loopModel.chatId.equals(chatId)) {
//                        for(item in templateListFromServer){
//                            if(item.id == loopModel.templateId){
//                                templateDataList.add(item)
//                                val templateFilteredList = templateDataList.filter { it.isLoopCreated == true }
//                                if(templateFilteredList.isNotEmpty() && templateFilteredList.size == 1){
//                                    addTemplateStarter()
//                                }
//                                break
//                            }
//                        }
                        loopList.remove(loopModel)
                        if (loopList.size == 0) {
                            loopList.clear()
                        }
                        if(loopList.isEmpty()){
                            //showNoLoopsView()
                        }
                        setAdapter(false)
                        break
                    }
                }
            }
        }
    }


}