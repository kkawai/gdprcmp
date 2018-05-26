package org.gdprcmplib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class GdprCmp {

    private void GdprCmp() {}

    public static void startCmpActivityForResult(Activity activity, int requestCode, boolean allowBackButton) {
        Intent intent = new Intent(activity, CmpActivity.class);
        intent.putExtra(Config.CMP_ALLOW_BACK_BUTTON, allowBackButton);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startCmpDetailsActivityForResult(Activity activity, int requestCode, boolean allowBackButton) {
        Intent intent = new Intent(activity, CmpDetailsActivity.class);
        intent.putExtra(Config.CMP_ALLOW_BACK_BUTTON, allowBackButton);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void setGDPRConsentString(final Context context, final String iabConsentString) {
        GDPRUtil.setGDPRConsentString(context, iabConsentString);
    }

    public static void setIsSubjectToGDPR(final Context context, final boolean isSubjectToGDPR) {
        GDPRUtil.setIsSubjectToGDPR(context, isSubjectToGDPR);
    }

    public static boolean isSubjectToGDPR(Context context) {
        return GDPRUtil.isSubjectToGDPR(context);
    }

    public static String getGDPRConsentString(Context context) {
        return GDPRUtil.getGDPRConsentString(context);
    }

    public static boolean hasGDPRConsentString(Context context) {
        return !TextUtils.isEmpty(GDPRUtil.getGDPRConsentString(context));
    }

    public static void clearGDPRSettings(Context context) {
        GDPRUtil.clearGDPRSettings(context);
    }
}
