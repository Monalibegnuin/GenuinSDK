package com.begenuin.library.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class CommunitySetupModel() : Serializable {
    @SerializedName("steps")
    @Expose
    val steps: ArrayList<StepModel>? = null

}