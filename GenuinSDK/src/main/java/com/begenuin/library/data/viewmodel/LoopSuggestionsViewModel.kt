package com.begenuin.library.data.viewmodel

import android.content.Context
import com.begenuin.library.common.Constants
import com.begenuin.library.common.Utility
import com.begenuin.library.core.interfaces.ResponseListener
import com.begenuin.library.data.model.ConversationModel
import com.begenuin.library.data.remote.BaseAPIService
import com.google.gson.Gson
import org.json.JSONObject

object LoopSuggestionsViewModel {
    var apiStatus = arrayOf(FeedViewModel.APIStatus.NONE, FeedViewModel.APIStatus.NONE)
    //{API Status of suggestCoHostLoops, API Status of suggestSubscribeLoops}
    private var loopSuggestionService = arrayListOf<BaseAPIService?>(null, null)
    //{BaseAPIService for suggestCoHostLoops, BaseAPIService for suggestSubscribeLoops}

    /*
         FeedViewType      FeedPagerPosition    page_type   apiStatus array index
         MY_LOOPS          0                    1           0
         SUBSCRIPTIONS     1                    2           1
    */

    fun suggestLoops(context: Context, pageNo: Int, feedPagerPosition: Int, listener: LoopSuggestionResponseListener) {
        if (!Utility.isNetworkAvailable(context)) {
            apiStatus[feedPagerPosition] = FeedViewModel.APIStatus.FAILED
            return
        }

        apiStatus[feedPagerPosition] = FeedViewModel.APIStatus.IN_PROGRESS

        if(loopSuggestionService[feedPagerPosition] != null){
            loopSuggestionService[feedPagerPosition]?.cancelCall()
        }
        try {
            val map = HashMap<String, Any>()
            map["page_no"] = pageNo
            map["page_type"] = feedPagerPosition + 1
            apiStatus[feedPagerPosition] = FeedViewModel.APIStatus.IN_PROGRESS
            loopSuggestionService[feedPagerPosition] = BaseAPIService(
                context,
                Constants.EMPTY_FEED_ROUNDTABLES,
                true,
                "",
                map,
                object : ResponseListener {
                    override fun onSuccess(response: String) {
                        apiStatus[feedPagerPosition] = FeedViewModel.APIStatus.COMPLETED
                        try {
                            val list: MutableList<ConversationModel> = java.util.ArrayList()
                            val `object` = JSONObject(response)
                            val dataJson = `object`.getJSONObject(Constants.JSON_DATA)
                            val conversationsJSON =
                                dataJson.getJSONArray(Constants.CONVERSATIONS_NEW)
                            for (i in 0 until conversationsJSON.length()) {
                                val conversationModel = Gson().fromJson(
                                    conversationsJSON.getJSONObject(i).toString(),
                                    ConversationModel::class.java
                                )
                                list.add(conversationModel)
                            }
                            var isEndOfLoops = false
                            if (dataJson.has("end_of_roundtables")) {
                                isEndOfLoops = dataJson.getBoolean("end_of_roundtables")
                            }
                            listener.onFetchComplete(list, isEndOfLoops)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    override fun onFailure(error: String) {
                        apiStatus[feedPagerPosition] = FeedViewModel.APIStatus.FAILED
                    }
                },
                "GET_DATA",
                false
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
interface LoopSuggestionResponseListener {
    fun onFetchComplete(list: List<ConversationModel>, isEndOfLoops: Boolean)
}