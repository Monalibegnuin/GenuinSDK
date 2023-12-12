package com.begenuin.library.views.activities

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.begenuin.library.R
import com.begenuin.library.SDKInitiate
import com.begenuin.library.common.Utility
import com.begenuin.library.common.Utility.showLogException
import com.begenuin.library.core.enums.ExploreVideoType
import com.begenuin.library.core.enums.PeekSource
import com.begenuin.library.core.interfaces.ResponseListener
import com.begenuin.library.data.model.ConversationModel
import com.begenuin.library.data.model.DiscoverModel
import com.begenuin.library.data.model.GetProfileModel
import com.begenuin.library.data.model.LottieAnimModel
import com.begenuin.library.data.remote.BaseAPIService
import com.begenuin.library.data.viewmodel.ExploreViewModel
import com.begenuin.library.common.Constants
import com.begenuin.library.views.PublicVideoAdapterNew
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.util.Locale

class ProfileActivity : AppCompatActivity(), ResponseListener {

    private var module = ""
    private lateinit var data: GetProfileModel
    private lateinit var tvUserName: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvViews: TextView
    private lateinit var tvVideos: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvVideoCountLabel: TextView
    private lateinit var tvViewCountLabel: TextView
    private lateinit var ivBack: ImageView
    private lateinit var ivProfile: CircleImageView
    private lateinit var animationView: LottieAnimationView
    private lateinit var recProfileVideos: RecyclerView
    private var bio = ""
    private var fullName = ""
    private var userName = ""
    private var profileImage = ""
    private var isAnim: Boolean = true
    lateinit var adapter: PublicVideoAdapterNew
    var allData: ArrayList<ExploreViewModel<*>> = ArrayList<ExploreViewModel<*>>()
    private var profileService: BaseAPIService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profilr)
        tvUserName = findViewById(R.id.tvUserName)
        tvBio = findViewById(R.id.tvBio)
        tvViews = findViewById(R.id.tvViews)
        tvVideos = findViewById(R.id.tvVideos)
        tvFullName = findViewById(R.id.tvFullName)
        tvVideoCountLabel = findViewById(R.id.tvVideoCountLabel)
        tvViewCountLabel = findViewById(R.id.tvViewCountLabel)
        ivProfile = findViewById(R.id.ivProfile)
        animationView = findViewById(R.id.animationView)
        recProfileVideos = findViewById(R.id.recProfileVideos)
        ivBack = findViewById(R.id.ivBack)
        val layoutManager = GridLayoutManager(this, 3)
        recProfileVideos.layoutManager = layoutManager
        adapter = PublicVideoAdapterNew(this, allData, true, PeekSource.ALLVIDEOTAB_PROFILE)
        //adapter.setClickListener(this)
        recProfileVideos.adapter = adapter
        ivBack.setOnClickListener{
            onBackPressed()
        }
        getProfileVideos(false, SDKInitiate.userId)
        getProfileDetails()
    }

    private fun getProfileDetails() {
        module = Constants.GET_PROFILE
        val userId = SDKInitiate.userId
        try {
            if (Utility.isNetworkAvailable(this)) {
                val jsonObject = JSONObject()
                jsonObject.put("user_id", userId)
                BaseAPIService(
                    this@ProfileActivity,
                    module,
                    Utility.getRequestBody(jsonObject.toString()),
                    true,
                    this,
                    "POST",
                    true
                )
            }
        } catch (e: Exception) {
            Utility.showLogException(e)
        }
    }

    override fun onSuccess(response: String?) {
        if (module == Constants.GET_PROFILE) {
            val gson = Gson()

            data = gson.fromJson(response, GetProfileModel::class.java)

            if (!TextUtils.isEmpty(data.data.views)) {
                val countText = Utility.formatNumber(data.data.views.toLong())
                tvViews.text = countText
                if (countText == "1") {
                    tvViews.text = getString(R.string.view)
                } else {
                    if(countText == "0"){
                        tvViewCountLabel.text = "0"
                    }else {
                        tvViewCountLabel.text = getString(R.string.views)
                    }
                }
            } else {
                tvViews.text = "0"
            }

            if (data.data.videos != 0) {
                val countText = Utility.formatNumber(data.data.videos.toLong())
                tvVideos.text = countText
                if (countText == "1") {
                    tvVideoCountLabel.text = getString(R.string.video_V)
                } else {
                    if(countText == "0"){
                        tvVideoCountLabel.text = "0"
                    }else {
                        tvVideoCountLabel.text = getString(R.string.video_V)
                    }
                }
            } else {
                tvVideos.text = "0"
            }

           // fullName = data.data.name
            bio = data.data.email
            userName = data.data.nickname
            profileImage = data.data.profile_image
            isAnim = data.data.isIs_avatar
            setUserProfile()
        }
    }

    override fun onFailure(error: String?) {
        Utility.showLog("API inside error", error.toString())
//        if (error != null) {
//            if (error.equals("404", false)) {
//                Utility.showToast(this@ProfileActivity, resources.getString(R.string.user_deleted_msg))
//                onBackPressed()
//            }
//        }
    }

    private fun setUserProfile() {
        if (!TextUtils.isEmpty(bio)) {
            tvBio.visibility = View.VISIBLE
            tvBio.text = bio
        } else {
            tvBio.visibility = View.GONE
        }

        tvUserName.text = String.format("@%s", userName)
        if(isAnim){
            val res = resources.getIdentifier(
                profileImage,
                "raw", packageName
            )
            val color: Drawable = ColorDrawable(
                resources.getColor(
                    LottieAnimModel.getMapData()[res]!!, null
                )
            )
            ivProfile.setImageDrawable(color)
            animationView.visibility = View.VISIBLE
            animationView.setAnimation(res)
            animationView.playAnimation()
            animationView.loop(true)
        }else{
            animationView.visibility = View.GONE
            Utility.displayProfileImage(this, profileImage, ivProfile)
        }

    }

    private fun getProfileVideos(nextPage: Boolean, userId: String) {
        if (allData.size == 0) {
//            shimmerProfile.setVisibility(View.VISIBLE)
//            shimmerProfile.startShimmer()
        }
         module = Constants.PROFILE_VIDEOS
//        if (nextPage) {
//            page = page + 1
//        }
        try {
            val map: MutableMap<String, Any> = HashMap()
            map["user_id"] = userId
            map["video_types[0]"] = ExploreVideoType.RT.value
            map["video_types[1]"] = ExploreVideoType.PUBLIC_VIDEO.value
            if (allData.size > 0) {
                val exploreViewModel: ExploreViewModel<*> = allData[allData.size - 1]
                map["last_video_id"] = exploreViewModel.convId
                map["last_video_type"] = exploreViewModel.type.value
            }
            //profileService?.cancelCall()

            profileService = BaseAPIService(
                this,
                Constants.PROFILE_VIDEOS,
                true,
                "",
                map,
                object : ResponseListener{
                    override fun onSuccess(response: String?) {
                        val jsonObject1: JSONObject
                        try {
                            //newProfilefragment.llNoVideoPublished.setVisibility(View.GONE)
                            //newProfilefragment.noVideoPostView.setVisibility(View.GONE)
                            jsonObject1 = JSONObject(response!!)
                            val message = jsonObject1.getString("message")
                            if (message.lowercase(Locale.getDefault()).contains("no public videos found")) {
                                Utility.showCustomToast(this@ProfileActivity,"No videos found")
                                return
                            }
                            if (jsonObject1.has(Constants.JSON_DATA)) {
                                val jsonObject = jsonObject1.getJSONObject(Constants.JSON_DATA)
                                val jsonArray = jsonObject.getJSONArray("videos")
                    //                    try {
                    //                        isEndOfPublicVideo = jsonObject.optBoolean("end_of_videos", false)
                    //                    } catch (exception: java.lang.Exception) {
                    //                        showLogException(exception)
                    //                    }
                                val pos = allData.size
                                if (jsonArray.length() > 0) {
                //                        val videoVOList: List<PublicVideoModel> =
                //                            Utility.getDBHelper().getPublicVideosWithLocalPath()
                                    val gson = Gson()
                                    for (i in 0 until jsonArray.length()) {
                                        val jProfileObj = jsonArray.optJSONObject(i)
                                        val videoType = jProfileObj.optString("video_type", "")
                                        val jVideoObj = jProfileObj.optJSONObject("video")
                                        if (videoType.equals(ExploreVideoType.RT.value, ignoreCase = true)) {
                                            val exploreViewRTModel: ExploreViewModel<*> =
                                                ExploreViewModel<Any?>()
                                            exploreViewRTModel.type = ExploreVideoType.RT
                                            assert(jVideoObj != null)
                                            val conversationModel: ConversationModel = gson.fromJson(jVideoObj?.toString(), ConversationModel::class.java)
                                            exploreViewRTModel.modelInterface = conversationModel
                                            allData.add(exploreViewRTModel)
                                        } else if (videoType.equals(ExploreVideoType.PUBLIC_VIDEO.value, ignoreCase = true)) {
                                            val exploreViewModel: ExploreViewModel<*> =
                                                ExploreViewModel<Any?>()
                                            exploreViewModel.type = ExploreVideoType.PUBLIC_VIDEO
                                            assert(jVideoObj != null)
                                            val discoverModel: DiscoverModel = gson.fromJson(
                                                jVideoObj?.toString(),
                                                DiscoverModel::class.java
                                            )
                                            discoverModel.videoUploadStatus = 2
                                            discoverModel.imageUploadStatus = 2
                                            discoverModel.apiStatus = 1
                                            discoverModel.compressionStatus = 1
            //                                if (videoVOList != null && videoVOList.size > 0) {
            //                                    for (j in videoVOList.indices) {
            //                                        if (discoverModel.videoId
            //                                                .contentEquals(videoVOList[j].getVideoId(), true)
            //                                        ) {
            //                                            discoverModel.localVideoPath = videoVOList[j].getLocalVideoPath()
            //                                            discoverModel.imagePath = videoVOList[j].getImagePath()
            //                                            break
            //                                        }
            //                                    }
            //                                }
                                            exploreViewModel.modelInterface = discoverModel
                                            allData.add(exploreViewModel)
                                        }
                                    }

                                    if (::adapter.isInitialized && allData.size >+ 0) {
                                        Utility.showLog("Monali", ""+allData.size)
                                        adapter.notifyItemRangeInserted(pos, allData.size - 1)
                                        adapter.notifyDataSetChanged()
                                    }
                                }
//                    if (newProfilefragment.getCurrentProfileTab() === 0) {
//                        if (allData.size == 0) {
//                            if (myProfile) {
//                                if (newProfilefragment.getContext() is ViewProfileActivity) {
//                                    newProfilefragment.noVideoPostView.setVisibility(View.GONE)
//                                } else {
//                                    newProfilefragment.noVideoPostView.setVisibility(View.VISIBLE)
//                                }
//                            }
//                            newProfilefragment.llNoVideoPublished.setVisibility(View.VISIBLE)
//                        } else {
//                            newProfilefragment.noVideoPostView.setVisibility(View.GONE)
//                            newProfilefragment.llNoVideoPublished.setVisibility(View.GONE)
//                        }
//                    }
                }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(error: String?) {
                        Utility.showLog("API inside error", error.toString())
                    }

                },
                "GET_DATA",
                true
            )
        } catch (e: java.lang.Exception) {
            showLogException(e)
        }
    }

}