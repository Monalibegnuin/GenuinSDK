package com.begnuine.library.core.interfaces

interface OnVideoDownload {
    fun onSuccessfullyDownloadVideo()

    fun onDownloadVideoFailure(code: Int)
}