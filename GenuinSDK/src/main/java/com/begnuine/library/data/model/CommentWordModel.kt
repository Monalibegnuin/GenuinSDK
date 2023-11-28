package com.begnuine.library.data.model

class CommentWordModel {
    private var length = 0
    var isMention = false
    var content: Any? = null
    private var contentText: String = ""

    fun getLength(): Int {
        length = getContentText().length
        return length
    }

    fun getContentText(): String{
        when (content) {
            is String -> {
                contentText = content as String
            }
            is MembersModel -> {
                contentText = "@${(content as MembersModel).nickname}"
            }
            is CommunityModel -> {
                contentText = (content as CommunityModel).handle
            }
        }
        return contentText
    }

    fun getContentAPIText(): String{
        when (content) {
            is String -> {
                contentText = content as String
            }
            is MembersModel -> {
                contentText = (content as MembersModel).text
            }
            is CommunityModel -> {
                contentText = (content as CommunityModel).text
            }
        }
        return contentText
    }

}