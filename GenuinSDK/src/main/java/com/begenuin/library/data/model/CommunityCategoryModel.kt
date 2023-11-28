package com.begenuin.library.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CommunityCategoryModel(
    @SerializedName("category_id") val id: Int?,
    @SerializedName("title") val name: String?
): Serializable
