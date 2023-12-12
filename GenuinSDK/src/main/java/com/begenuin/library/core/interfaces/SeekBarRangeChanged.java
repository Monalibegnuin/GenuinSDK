package com.begenuin.library.core.interfaces;

public interface SeekBarRangeChanged {
    void onRangeChanged(long start, long end,boolean isLeft);
    void onRangeSelection(long start, long end,boolean isLeft);
    void onRangeStart(boolean isLeft);
    void onRangeEnd();
}
