package org.gdprcmplib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class GdprCmp {

    private void GdprCmp() {}

    /**
     * Starts the main CMP activity for gathering user consent.
     *
     * @param activity - parent activity
     * @param requestCode - activity request code
     * @param allowBackButton - if true, allows user to back out of CMP screen.  Otherwise, no.
     * @param defaultConsentAll - if true and if the first time and user goes thru to CMP Details, then
     *                          all consent items will be checked on.  Otherwise, all checked off.
     */
    public static void startCmpActivityForResult(Activity activity, int requestCode, boolean allowBackButton, boolean defaultConsentAll) {
        Intent intent = new Intent(activity, CmpActivity.class);
        intent.putExtra(Config.CMP_ALLOW_BACK_BUTTON, allowBackButton);
        intent.putExtra(Config.CMP_DEFAULT_CONSENT_ALL, defaultConsentAll);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Starts the CMP Details activity for gathering user consent.
     *
     * @param activity - parent activity
     * @param requestCode - activity request code
     * @param allowBackButton - if true, allows user to back out of CMP screen.  Otherwise, no.
     * @param defaultConsentAll - if true and if the first time, then all consent items will be checked on.
     *                          Otherwise, all checked off.
     */
    public static void startCmpDetailsActivityForResult(Activity activity, int requestCode, boolean allowBackButton, boolean defaultConsentAll) {
        Intent intent = new Intent(activity, CmpDetailsActivity.class);
        intent.putExtra(Config.CMP_ALLOW_BACK_BUTTON, allowBackButton);
        intent.putExtra(Config.CMP_DEFAULT_CONSENT_ALL, defaultConsentAll);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Stores the consent string to default shared preferences following IAB specs.
     *
     * @param context
     * @param iabConsentString
     */
    public static void setGDPRConsentString(final Context context, final String iabConsentString) {
        GDPRUtil.setGDPRConsentString(context, iabConsentString);
    }

    /**
     * Sets the app as being subject to GDPR or not.
     *
     * @param context
     * @param isSubjectToGDPR - true sets this app as being subject to GDPR.  false, not subject.
     */
    public static void setIsSubjectToGDPR(final Context context, final boolean isSubjectToGDPR) {
        GDPRUtil.setIsSubjectToGDPR(context, isSubjectToGDPR);
    }

    /**
     * Indicates if the app is subject to GDPR.  If 'setIsSubjectToGDPR' has not been invoked yet,
     * then checks device settings to see if it's in a EU country.  If in EU country, then returns true.
     *
     * @param context
     * @return
     */
    public static boolean isSubjectToGDPR(Context context) {
        return GDPRUtil.isSubjectToGDPR(context);
    }

    /**
     * Returns the existing consent string stored in shared preferences.
     *
     * @param context
     * @return consent string. null, if not yet stored.
     */
    public static String getGDPRConsentString(Context context) {
        return GDPRUtil.getGDPRConsentString(context);
    }

    /**
     * Indicates if consent string has been set.
     *
     * @param context
     * @return
     */
    public static boolean hasGDPRConsentString(Context context) {
        return !TextUtils.isEmpty(GDPRUtil.getGDPRConsentString(context));
    }

    /**
     * Removes GDPR settings from shared preferences of this app.  Useful for testing.
     * Otherwise, not recommended.
     *
     * @param context
     */
    public static void clearGDPRSettings(Context context) {
        GDPRUtil.clearGDPRSettings(context);
    }

}
