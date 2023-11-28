package com.begenuin.library.core.interfaces

interface OnVideoDownload {
    fun onSuccessfullyDownloadVideo()

    fun onDownloadVideoFailure(code: Int)
}