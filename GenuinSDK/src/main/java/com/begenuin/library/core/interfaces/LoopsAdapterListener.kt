package com.begenuin.library.core.interfaces

import com.begenuin.library.data.model.LoopsModel

interface LoopsAdapterListener {

    fun onLoopClicked(loop: LoopsModel)

    fun onCreateLoopClicked()

    fun onThumbnailStackClicked(loop: LoopsModel)
}