package com.begenuin.library.core.interfaces

import com.begenuin.library.data.model.CommunityModel


interface FeedCommunityListInterface {
    fun onCommunityListLoaded(communityList: ArrayList<CommunityModel>)
}