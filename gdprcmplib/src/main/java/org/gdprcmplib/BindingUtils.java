package org.gdprcmplib;

import android.view.View;

class BindingUtils {

    private BindingUtils() {
    }

    public static void setViewWidth(View view, float percentageOfScreen) {
        int screenWidth = ScreenUtils.getScreenWidth(view.getContext());
        float viewWidth = screenWidth * percentageOfScreen;
        view.getLayoutParams().width = (int)viewWidth;
    }
}
