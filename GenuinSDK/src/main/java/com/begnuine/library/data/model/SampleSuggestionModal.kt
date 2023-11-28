package com.begnuine.library.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class GuidelinesResponse(
    val code: Int? = 400,
    val message: String? = "something went wrong",
    @SerializedName("data")
    @Expose
    val data: Data?
) : Serializable

data class Data(

    @SerializedName("guidelines")
    @Expose
    val guidelines: List<GuideLineModel>
) : Serializable

data class GuideLineModel(
    @SerializedName("id")
    @Expose
    var id: Int = 0,
    @SerializedName("title")
    @Expose
    var title: String,
    @SerializedName("description")
    @Expose
    var description: String,
    @SerializedName("position")
    @Expose
    var position: Int,
    @SerializedName("guideline_id")
    @Expose
    var guideLineId: Int = 0,
    var drawable: Int = 0,
    var isExpanded: Boolean = false
) : Serializable
