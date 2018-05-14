package org.gdprcmplib;

import android.databinding.BindingAdapter;
import android.view.View;

final class BindingUtils {

    private BindingUtils() {
    }

    @BindingAdapter("viewWidth")
    public static void setViewWidth(View view, float percentageOfScreen) {
        int screenWidth = ScreenUtils.getScreenWidth(view.getContext());
        float viewWidth = screenWidth * percentageOfScreen;
        view.getLayoutParams().width = (int)viewWidth;
    }
}
