package com.begnuine.library.common.customViews.customscrollview.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.begnuine.library.common.customViews.customscrollview.DiscreteScrollView;

public class ScrollListenerAdapter<T extends RecyclerView.ViewHolder> implements DiscreteScrollView.ScrollStateChangeListener<T> {

    private final DiscreteScrollView.ScrollListener<T> adapter;

    public ScrollListenerAdapter(@NonNull DiscreteScrollView.ScrollListener<T> adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onScrollStart(@NonNull T currentItemHolder, int adapterPosition) {

    }

    @Override
    public void onScrollEnd(@NonNull T currentItemHolder, int adapterPosition) {

    }

    @Override
    public void onScroll(float scrollPosition,
                         int currentIndex, int newIndex,
                         @Nullable T currentHolder, @Nullable T newCurrentHolder) {
        adapter.onScroll(scrollPosition, currentIndex, newIndex, currentHolder, newCurrentHolder);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScrollListenerAdapter) {
            return adapter.equals(((ScrollListenerAdapter<?>) obj).adapter);
        } else {
            return super.equals(obj);
        }
    }
}
