package org.gdprcmplib;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

class UiUtils {

    private UiUtils() {
    }

    static void setViewWidth(View view, float percentageOfScreen) {
        int screenWidth = ScreenUtils.getScreenWidth(view.getContext());
        float viewWidth = screenWidth * percentageOfScreen;
        view.getLayoutParams().width = (int)viewWidth;
    }

    /**
     * A obfuscated security measure to make the buy button harder to hack via xml.
     * Not good programming, but that's the point.  To discourage hacking.
     * @param view
     */
    static void setBuyButton(View view) {
        Button button = (Button)view;
        view.getLayoutParams().height = (int)dpToPix(button.getContext(),60f);
        view.setVisibility(GDPRUtil.isValidSdkKey((Activity)view.getContext()) ? View.GONE : View.VISIBLE);
    }

    static float dpToPix(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    static void showSuccessDialog(final Activity activity, final int resultCode) {
        if (GDPRUtil.isValidSdkKey(activity)) {
            activity.setResult(resultCode);
            activity.finish();
        } else {
            new AlertDialog.Builder(activity).setMessage(R.string.buy_msg).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    activity.setResult(resultCode);
                    activity.finish();
                }
            }).show();
        }
    }
}
