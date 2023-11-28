package com.begnuine.library.core.interfaces;

public interface LoopSuggestionPagerEventListener {
    void onButtonClick();
    void onItemScroll();
    void onApiComplete(boolean isSuccess);
    void onGoToFeedClicked();
}
