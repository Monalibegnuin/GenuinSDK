package com.begnuine.library.views

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.begenuine.feedscreensdk.common.Constants
import com.begnuine.library.common.Utility
import com.begnuine.library.core.enums.FeedViewType
import com.begnuine.library.R
import com.begnuine.library.common.customViews.CustomTextView
import com.begnuine.library.core.interfaces.ResponseListener
import com.begnuine.library.data.model.ConversationModel
import com.begnuine.library.data.remote.BaseAPIService
import org.json.JSONObject

class SliderLoopSuggestionRecyclerAdapter(
    val context: Context,
    private val loopList: List<ConversationModel>,
    private val feedViewType: Int,
    val buttonClickListener: ButtonClickListener
) : RecyclerView.Adapter<SliderLoopSuggestionRecyclerAdapter.ItemViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.fragment_slider_loop_suggestion,
                    parent,
                    false
                )
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return loopList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val conversationModel = loopList[position]
        conversationModel.isSubscriber
        val group = loopList[position].group
        if(feedViewType == FeedViewType.MY_LOOPS.value){
            holder.btnSubscribe.visibility = View.GONE
            holder.btnJoinAsCoHost.visibility = View.VISIBLE
            if(!conversationModel.isRequestToJoinSent){
                holder.btnJoinAsCoHost.backgroundTintList =
                    context.resources.getColorStateList(R.color.colorPrimary, null)
                holder.btnJoinAsCoHost.text = context.getString(R.string.request_to_join)
                holder.btnJoinAsCoHost.isEnabled = true
            } else{
                holder.btnJoinAsCoHost.backgroundTintList =
                    context.resources.getColorStateList(R.color.color_949494, null)
                holder.btnJoinAsCoHost.text = context.getString(R.string.pending)
                holder.btnJoinAsCoHost.isEnabled = false
            }
            holder.llCoHosts.visibility = View.VISIBLE
            holder.rvCoHosts.visibility = View.VISIBLE
            //holder.viewDivider2.visibility = View.VISIBLE
            if(holder.rvCoHosts.adapter == null) {
                val gridLayoutManager =
                    GridLayoutManager(context, 4)//, RecyclerView.HORIZONTAL,false)
                holder.rvCoHosts.layoutManager = gridLayoutManager
                val displayMembersList = if (group?.members?.size!! <= 4) {
                    group.members
                } else {
                    group.members.subList(0, 3)
                }
                holder.rvCoHosts.adapter = CoHostsDisplayRecyclerAdapter(
                    context,
                    displayMembersList
                )
            } else {
                holder.rvCoHosts.adapter!!.notifyDataSetChanged()
            }
        } else if(feedViewType == FeedViewType.SUBSCRIPTIONS.value){
            holder.btnSubscribe.visibility = View.VISIBLE
            if(!conversationModel.isSubscriber){
                holder.btnSubscribe.backgroundTintList =
                    context.resources.getColorStateList(R.color.colorPrimary, null)
                holder.btnSubscribe.text = context.getString(R.string.subscribe)
            } else {
                holder.btnSubscribe.backgroundTintList =
                    context.resources.getColorStateList(R.color.color_949494, null)
                holder.btnSubscribe.text = context.getString(R.string.subscribed)
            }
            holder.btnJoinAsCoHost.visibility = View.GONE
            holder.llCoHosts.visibility = View.GONE
            holder.rvCoHosts.visibility = View.GONE
            //holder.viewDivider2.visibility = View.GONE
        }
        if(!TextUtils.isEmpty(group!!.name)){
            holder.tvGroupName.text = group.name
        }
        /*if (TextUtils.isEmpty(group.dp)) {
            holder.llDp.setDpWithInitials(group.name, group.colorCode, group.textColorCode)
        } else {
            holder.llDp.setDpWithImage(context as Activity, false, group.dp, group.smallDp, true)
        }
        if (!TextUtils.isEmpty(group.noOfViews)) {
            holder.tvViews.text = Utility.formatNumber(group.noOfViews.toLong())
        } else {
            holder.tvViews.text = "0"
        }
        if (!TextUtils.isEmpty(group.noOfVideos)) {
            holder.tvVideos.text = Utility.formatNumber(group.noOfVideos.toLong())
        } else {
            holder.tvVideos.text = "0"
        }
        if (!TextUtils.isEmpty(group.noOfSubscribers)) {
            holder.tvSubscribers.text = Utility.formatNumber(group.noOfSubscribers.toLong())
        } else {
            holder.tvSubscribers.text = "0"
        }*/
        if (!TextUtils.isEmpty(group.description)) {
            holder.tvGroupDesc.visibility = View.VISIBLE
            holder.tvGroupDesc.text = group.description
        } else {
            holder.tvGroupDesc.visibility = View.GONE
        }
    }

    private fun callApiToSubscribe(position: Int, isSubscribe: Boolean){
        try {
            val module = Constants.SUBSCRIBE_RT
            val jsonObject = JSONObject()
            val conversationModel = loopList[position]
            jsonObject.put("subscribe", isSubscribe)
            jsonObject.put("chat_id", conversationModel.chatId)
            BaseAPIService(
                context,
                module,
                Utility.getRequestBody(jsonObject.toString()),
                true,
                object : ResponseListener {
                    override fun onSuccess(response: String) {
                        buttonClickListener.onSubscribeAPIComplete(true)
                    }

                    override fun onFailure(error: String) {
                        buttonClickListener.onSubscribeAPIComplete(false)
                    }
                },
                "POST",
                false
            )
        } catch (e: Exception) {
            Utility.showLogException(e)
        }
    }
    private fun callApiToRequestToJoin(position: Int){
        try {
            val module = Constants.REQUEST_TO_JOIN
            val jsonObject = JSONObject()
            val conversationModel = loopList[position]
            jsonObject.put("chat_id", conversationModel.chatId)
            BaseAPIService(
                context,
                module,
                Utility.getRequestBody(jsonObject.toString()),
                true,
                object : ResponseListener {
                    override fun onSuccess(response: String) {
                        buttonClickListener.onRequestAPIComplete(true)
                    }
                    override fun onFailure(error: String) {
                        buttonClickListener.onRequestAPIComplete(false)
                    }
                },
                "POST",
                false
            )
        } catch (e: java.lang.Exception) {
            Utility.showLogException(e)
        }
    }

    inner class ItemViewHolder(itemView: View):  RecyclerView.ViewHolder(itemView){
        val rvCoHosts: RecyclerView = itemView.findViewById(R.id.rvCoHosts)
        val tvGroupName: CustomTextView =itemView.findViewById(R.id.tvGroupName)
        /*val llDp: DisplayPictureView = itemView.findViewById(R.id.llDp)
        val tvViews: TextView = itemView.findViewById(R.id.tvViews)
        val tvSubscribers: TextView = itemView.findViewById(R.id.tvSubscribers)
        val tvVideos: TextView = itemView.findViewById(R.id.tvVideos)*/
        val tvGroupDesc: CustomTextView = itemView.findViewById(R.id.tvGroupDesc)
        //val viewDivider2: View = itemView.findViewById(R.id.viewDivider2)
        val llCoHosts: LinearLayout = itemView.findViewById(R.id.llCoHosts)
        val btnJoinAsCoHost: Button = itemView.findViewById(R.id.btnJoinAsCoHost)
        val btnSubscribe: Button = itemView.findViewById(R.id.btnSubscribe)

        init {
            btnSubscribe.setOnClickListener{
                if(feedViewType == FeedViewType.SUBSCRIPTIONS.value) {
                    if(btnSubscribe.text == context.getString(R.string.subscribe)){
                        callApiToSubscribe(absoluteAdapterPosition,true)
                        loopList[absoluteAdapterPosition].isSubscriber = true
                        notifyItemChanged(absoluteAdapterPosition)
                        buttonClickListener.onSubscribe(absoluteAdapterPosition, true)
                    } else {
                        callApiToSubscribe(absoluteAdapterPosition, false)
                        loopList[absoluteAdapterPosition].isSubscriber = false
                        notifyItemChanged(absoluteAdapterPosition)
                        buttonClickListener.onSubscribe(absoluteAdapterPosition, false)
                    }
                }
            }
            btnJoinAsCoHost.setOnClickListener{
                if(feedViewType == FeedViewType.MY_LOOPS.value && btnJoinAsCoHost.text == context.getString(R.string.request_to_join)){
                    callApiToRequestToJoin(absoluteAdapterPosition)
                    loopList[absoluteAdapterPosition].isRequestToJoinSent = true
                    notifyItemChanged(absoluteAdapterPosition)
                    buttonClickListener.onRequestToJoin(absoluteAdapterPosition)
                }
            }
        }

    }

}
interface ButtonClickListener{
    fun onSubscribe(position: Int, isSubscribe: Boolean)
    fun onRequestToJoin(position: Int)
    fun onSubscribeAPIComplete(isSuccess: Boolean)
    fun onRequestAPIComplete(isSuccess: Boolean)
}