package com.begenuin.library.views

import android.app.Activity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.begenuin.library.R
import com.begenuin.library.common.customViews.DisplayPictureView
import com.begenuin.library.data.model.CommunityModel
import java.util.ArrayList

class FeedOptionsCommunitiesAdapter(
    val context: Activity,
    val communities: ArrayList<CommunityModel>,
    val itemSelectListener: ItemSelectListener,
    var highlightedCommunityId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.item_feed_community_option, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return communities.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val model = communities[position]
        holder.itemView.tag = model
        if (model.communityId == highlightedCommunityId) {
            holder.llMain.background = ContextCompat.getDrawable(context, R.color.color_E7E7E7)
        } else {
            holder.llMain.background = ContextCompat.getDrawable(context, R.color.colorWhite)
        }
        if (!TextUtils.isEmpty(model.dp)) {
            holder.llDp.setDpWithImage(context, false, model.dp, model.dp, false)
        } else {
            holder.llDp.setDpWithInitials(model.name, model.colorCode, model.textColorCode)
        }
        holder.tvCommunityName.text = model.handle
        if (position == communities.size - 1) {
            holder.itemDivider.visibility = View.GONE
        } else {
            holder.itemDivider.visibility = View.VISIBLE
        }
    }

    fun highlightCommunity(communityId: String) {
        removeHighlight()
        highlightedCommunityId = communityId
        val newHighlightedList = communities.filter { model -> model.communityId == communityId }
        if (newHighlightedList.isNotEmpty()) {
            val index = communities.indexOf(newHighlightedList[0])
            notifyItemChanged(index)
        }
    }

    fun removeHighlight() {
        val oldHighlightedList = communities.filter { model -> model.communityId == highlightedCommunityId }
        highlightedCommunityId = ""
        if (oldHighlightedList.isNotEmpty()) {
            val oldIndex = communities.indexOf(oldHighlightedList[0])
            notifyItemChanged(oldIndex)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llDp: DisplayPictureView
        val tvCommunityName: TextView
        val itemDivider: View
        val llMain: LinearLayout
        init {
            llDp = itemView.findViewById(R.id.llDp)
            tvCommunityName = itemView.findViewById(R.id.tvCommunityName)
            itemDivider = itemView.findViewById(R.id.itemDivider)
            llMain = itemView.findViewById(R.id.llMain)
            llMain.setOnClickListener {
                itemSelectListener.onItemSelect(itemView.tag as CommunityModel)
            }
        }
    }
}

interface ItemSelectListener {
    fun onItemSelect(community: CommunityModel)
}