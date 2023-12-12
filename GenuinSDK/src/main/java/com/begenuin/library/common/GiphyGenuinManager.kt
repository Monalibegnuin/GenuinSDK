package com.begenuin.library.common

import com.giphy.sdk.core.models.enums.RenditionType
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.themes.GPHCustomTheme
import com.giphy.sdk.ui.themes.GPHTheme
import com.giphy.sdk.ui.views.GiphyDialogFragment

class GiphyGenuinManager {

    var giphyDialogFragment: GiphyDialogFragment? = null

    fun getGiphyDialogInstance(): GiphyDialogFragment? {
        if (giphyDialogFragment == null) {
            GPHCustomTheme.channelColor = 0xffD8D8D8.toInt()
            GPHCustomTheme.handleBarColor = 0xff888888.toInt()
            GPHCustomTheme.backgroundColor = 0xCC000000.toInt()
            GPHCustomTheme.dialogOverlayBackgroundColor = 0xFF4E4E4E.toInt()
            GPHCustomTheme.textColor = 0xff0645FF.toInt()
            GPHCustomTheme.activeTextColor = 0xFFFFFFFF.toInt()
            GPHCustomTheme.imageColor = 0xC09A9A9A.toInt()
            GPHCustomTheme.activeImageColor = 0xFFFFFFFF.toInt()
            GPHCustomTheme.searchBackgroundColor = 0xFF4E4E4E.toInt()
            GPHCustomTheme.searchQueryColor = 0xffffffff.toInt()
            GPHCustomTheme.suggestionBackgroundColor = 0x00000000.toInt()
            GPHCustomTheme.moreByYouBackgroundColor = 0xFFF1F1F1.toInt()
            GPHCustomTheme.backButtonColor = 0xFFFFFFFF.toInt()

            val settings = GPHSettings(theme = GPHTheme.Custom)
//            GPHContentType.gif,
            settings.mediaTypeConfig = arrayOf(
                GPHContentType.recents,
                GPHContentType.sticker,
                GPHContentType.text,
                GPHContentType.emoji
            )
            settings.stickerColumnCount = 3
            settings.showSuggestionsBar = false
            if(Giphy.recents.count > 0) {
                settings.selectedContentType = GPHContentType.recents
            }else{
                settings.selectedContentType = GPHContentType.sticker
            }
            settings.renditionType = RenditionType.downsized
//            settings.mediaTypeConfig = arrayOf(GPHContentType.gif)
            giphyDialogFragment = GiphyDialogFragment.newInstance(settings)

        }
        return giphyDialogFragment
    }
}