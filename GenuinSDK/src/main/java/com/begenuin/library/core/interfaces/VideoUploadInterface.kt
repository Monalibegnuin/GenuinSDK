package com.begenuin.library.core.interfaces

import com.begenuin.library.data.model.LoopsModel
import com.begenuin.library.data.model.MessageModel


// Added interface for retry popup menu
interface VideoUploadInterface {

    //This function will called on Retry click
    fun onRetryClicked(messageModel: MessageModel)

    // This function will called on Retry click while creating loop
    fun onRetryLoopClicked(loopsModel: LoopsModel)

    //This function will called on Clear click
    fun onDeleteClicked(messageModel: MessageModel)
}