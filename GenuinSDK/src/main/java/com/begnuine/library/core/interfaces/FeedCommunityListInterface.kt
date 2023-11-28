package com.begnuine.library.core.interfaces

import com.begnuine.library.data.model.CommunityModel


interface FeedCommunityListInterface {
    fun onCommunityListLoaded(communityList: ArrayList<CommunityModel>)
}