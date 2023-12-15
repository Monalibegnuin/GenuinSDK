package com.begenuin.library.data.eventbus

class LoopVideoAPICompleteEvent {
    /*
        This event is triggered when API call after loop video
        upload returns success.
    */
    var localVideoPath: String? = null

    var createdLoopId: String = ""

    var newMessageId: String = ""
}