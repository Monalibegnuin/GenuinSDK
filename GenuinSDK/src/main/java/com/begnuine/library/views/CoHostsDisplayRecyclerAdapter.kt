package com.begnuine.library.views

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.begnuine.library.common.Utility
import com.begnuine.library.R
import com.begnuine.library.data.model.LottieAnimModel
import com.begnuine.library.data.model.MembersModel

class CoHostsDisplayRecyclerAdapter(val context: Context, private val memberList: List<MembersModel>) : RecyclerView.Adapter<CoHostsDisplayRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_cohost_display, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val member = memberList[position]
        viewHolder.tvMemberName.text = String.format("@%s", member.nickname)
        if (!TextUtils.isEmpty(member.memberRole) && member.memberRole.equals(
                "1",
                ignoreCase = true
            )
        ) {
            viewHolder.ivOwner.visibility = View.VISIBLE
        } else {
            viewHolder.ivOwner.visibility = View.GONE
        }
        if (member.isAvatar) {
            val res = context.resources.getIdentifier(
                member.profileImage,
                "raw", context.packageName
            )
            val color: Drawable =
                ColorDrawable(context.resources.getColor(LottieAnimModel.getMapData()[res]!!, null))
            viewHolder.ivMember.visibility = View.GONE
            viewHolder.ivMember1.visibility = View.VISIBLE
            viewHolder.animationMember.visibility = View.VISIBLE
            viewHolder.ivMember1.setImageDrawable(color)
            viewHolder.animationMember.setAnimation(res)
        } else {
            viewHolder.animationMember.visibility = View.GONE
            viewHolder.ivMember.visibility = View.VISIBLE
            viewHolder.ivMember1.visibility = View.GONE
            if (TextUtils.isEmpty(member.profileImageL)) {
                Utility.displayProfileImage(context as Activity, member.profileImage, viewHolder.ivMember)
            } else {
                Utility.displayProfileImage(context as Activity, member.profileImageL, viewHolder.ivMember)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        if (holder.animationMember.visibility == View.VISIBLE) {
            holder.animationMember.playAnimation()
        }
        super.onViewAttachedToWindow(holder)
    }
    override fun getItemCount(): Int {
        return memberList.size
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        var tvMemberName: TextView = ItemView.findViewById(R.id.tvMemberName)
        var ivMember: ImageView = ItemView.findViewById(R.id.ivMember)
        var ivMember1: ImageView = ItemView.findViewById(R.id.ivMember1)
        var ivOwner: ImageView = ItemView.findViewById(R.id.ivOwner)
        var animationMember: LottieAnimationView = ItemView.findViewById(R.id.animationMember)
        var llMain: LinearLayout = ItemView.findViewById(R.id.llMain)
//        init {
//            ItemView.setOnClickListener{
//                val intent = Intent(context, ViewProfileActivity::class.java)
//                intent.putExtra("userId", memberList[absoluteAdapterPosition].userId)
//                context.startActivity(intent)
//                (context as Activity).overridePendingTransition(
//                    R.anim.slide_in_right,
//                    R.anim.slide_out_left
//                )
//            }
//        }
    }

}