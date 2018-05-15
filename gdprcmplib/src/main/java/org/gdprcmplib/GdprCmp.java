package org.gdprcmplib;

import android.content.Context;
import android.content.Intent;

public class GdprCmp {

    private void GdprCmp() {}

    public static void startCmpActivity(Context context) {
        context.startActivity(new Intent(context, CmpActivity.class));
    }

    public static void startCmpDetailsActivity(Context context) {
        context.startActivity(new Intent(context, CmpDetailsActivity.class));
    }

    public static void setGDPRInfo(final Context context, final boolean isSubjectToGDPR, final String iabConsentString) {
        GDPRUtil.setGDPRInfo(context, isSubjectToGDPR, iabConsentString);
    }

    public static boolean isSubjectToGDPR(Context context) {
        return GDPRUtil.isSubjectToGDPR(context);
    }

    public static String getGDPRConsentString(Context context) {
        return GDPRUtil.getGDPRConsentString(context);
    }
}
