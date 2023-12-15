package com.begenuin.library.data.eventbus

class ConversationDeleteEvent {
    /*
     * This event is triggered when a user deletes a conversation,
     * or receives a socket event in case the other user in the
     * conversation does so.
     */
    var chatId : String = ""
}