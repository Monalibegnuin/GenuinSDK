package com.begenuin.library.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class StepModel(@SerializedName("id"           ) var id          : Int?     = null,
                     @SerializedName("is_completed" ) var isCompleted : Boolean? = null,
                     @SerializedName("timestamp"    ) var timestamp   : Int?     = null
): Serializable
