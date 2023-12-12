package com.begenuin.library.views.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.begenuin.library.R
import com.begenuin.library.common.Constants
import com.begenuin.library.common.Utility
import com.begenuin.library.core.enums.VideoConvType
import com.begenuin.library.core.interfaces.LoopsAdapterListener
import com.begenuin.library.core.interfaces.ResponseListener
import com.begenuin.library.data.model.CommunityModel
import com.begenuin.library.data.model.LoopsModel
import com.begenuin.library.data.remote.BaseAPIService
import com.begenuin.library.databinding.FragmentCommunityDetailsBinding
import com.begenuin.library.views.LoopsAdapter
import com.begenuin.library.views.activities.CameraNewActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 * Use the [CommunityDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommunityDetailsFragment : Fragment(){

    private var communityId: String = ""
    private var bottomSheetGuideLineDialog: BottomSheetDialog? = null
    var role: Int = 0
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var tabList: ArrayList<String> = ArrayList()
    var communityModel: CommunityModel? = null
    private var communityDetailsService: BaseAPIService? = null
    private var editCommunityActivityLauncher: ActivityResultLauncher<Intent>? = null
    private var isBottomSheetDrawerAutoOpenedOnce: Boolean = false

    private var invitingUserDp: String = ""
    private var invitingUserIsAvatar: Boolean = false
    private var isCommunityDataLoaded: Boolean = false
    private var isUserMetaDataLoaded: Boolean = false
    lateinit var _binding: FragmentCommunityDetailsBinding
    private var communityLoopsService: BaseAPIService? = null
    private var loopList: ArrayList<LoopsModel> = ArrayList()
    lateinit var adapter: LoopsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            communityId = it.getString("community_id").toString()
            role = it.getInt("role")
        }
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param communityId Community Id.
         * @return A new instance of fragment CommunityDetailsFragment.
         */
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
        _binding.fabPost.setOnClickListener{
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

    fun callGetCommunityLoops() {
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

//                            if (myLoops.isNotEmpty()) {
//                                if (Utility.getDBHelper() != null) {
//                                    Utility.getDBHelper().insertORUpdateLoops(myLoops)
//                                }
//                            }
                            //pendingLoopsOrMessageManagement()
//                            if (loopList.isNotEmpty()) {
//                                addCreateLoopCell()
//                                addTemplateStarter()
//                            }
                            setAdapter(false)
                        }
                        communityLoopsService = null
                        //isDataLoaded = true
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
}