package com.pandasdroid.wordlequest;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;

public class NoScrollLayoutManager extends GridLayoutManager {


    public NoScrollLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public NoScrollLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public NoScrollLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }
}
