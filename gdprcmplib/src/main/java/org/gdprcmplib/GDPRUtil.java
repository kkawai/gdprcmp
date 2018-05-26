package org.gdprcmplib;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic utility to support all GDPR related functionality.
 */
class GDPRUtil {

    private static final String TAG = "GDPRUtil";
    private static final String OFFICIAL_APPLICABLE = "IABConsent_SubjectToGDPR";
    private static final String OFFICIAL_STRING = "IABConsent_ConsentString";

    private GDPRUtil() {
        //Private Empty Constructor
    }

    private static Set<String> EU_COUNTRIES = new HashSet<>(28);

    static {
        EU_COUNTRIES.add("GB");
        EU_COUNTRIES.add("DE");
        EU_COUNTRIES.add("PL");
        EU_COUNTRIES.add("FR");
        EU_COUNTRIES.add("IT");
        EU_COUNTRIES.add("ES");
        EU_COUNTRIES.add("RO");
        EU_COUNTRIES.add("SE");
        EU_COUNTRIES.add("BG");
        EU_COUNTRIES.add("NL");
        EU_COUNTRIES.add("GR");
        EU_COUNTRIES.add("HR");
        EU_COUNTRIES.add("IE");
        EU_COUNTRIES.add("CZ");
        EU_COUNTRIES.add("AT");
        EU_COUNTRIES.add("HU");
        EU_COUNTRIES.add("FI");
        EU_COUNTRIES.add("DK");
        EU_COUNTRIES.add("BE");
        EU_COUNTRIES.add("PT");
        EU_COUNTRIES.add("MT");
        EU_COUNTRIES.add("CY");
        EU_COUNTRIES.add("LT");
        EU_COUNTRIES.add("SK");
        EU_COUNTRIES.add("SI");
        EU_COUNTRIES.add("EE");
        EU_COUNTRIES.add("LV");
        EU_COUNTRIES.add("LU");
    }

    private static boolean isGDPRRegion(Context context) {
        try {
            String locale = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0).getCountry();
            } else {
                locale = context.getResources().getConfiguration().locale.getCountry();
            }
            return EU_COUNTRIES.contains(locale);
        } catch (Exception e) {
            MLog.e(TAG, "isGDPRRegion() failed", e);
        }
        return false;
    }

    static void clearGDPRSettings(final Context context) {
        try {
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove(OFFICIAL_APPLICABLE).commit();
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove(OFFICIAL_STRING).commit();
        } catch (Exception e) {
            MLog.e(TAG, "setIsSubjectToGDPR() failed", e);
        }
    }

    static void setIsSubjectToGDPR(final Context context, final boolean isSubjectToGDPR) {
        try {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(OFFICIAL_APPLICABLE, isSubjectToGDPR ? "1" : "0").commit();
        } catch (Exception e) {
            MLog.e(TAG, "setIsSubjectToGDPR() failed", e);
        }
    }

    static void setGDPRConsentString(final Context context, String iabConsentString) {
        try {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(OFFICIAL_STRING, iabConsentString).apply();
        } catch (Exception e) {
            MLog.e(TAG, "setGDPRConsentString failed.", e);
        }
    }

    /**
     * Implemenation yet to be determined.  We check if GDPR has been
     * set by the developer.
     *
     * @return true - developer has determined this is a GDPR applicable region
     * and that boolean has been stored somewhere.
     * false - developer has determined this is NOT GDPR applicable region
     * and that boolean has been stored somewhere.
     * null - developer has not determined this is a GDPR applicable region
     * so we must deploy our own determination.
     */
    static boolean isSubjectToGDPR(Context context) {
        //First, check the iab spec
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        if (prefs.contains(OFFICIAL_APPLICABLE)) {
            try {
                return prefs.getString(OFFICIAL_APPLICABLE, "0").equals("1");
            } catch (Exception e) {
                MLog.e(TAG, "isSubjectToGDPR failed", e);
            }
        }
        return isGDPRRegion(context);
    }

    /**
     * Implemenation yet to be determined.  We check if GDPR rangeConsent string has been
     * set by the developer.
     *
     * @return - String saved by the developer.  If null, then developer has simply
     * not set it.
     */
    public static String getGDPRConsentString(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //first try the official iab spec string
        if (prefs.contains(OFFICIAL_STRING)) {
            try {
                return prefs.getString(OFFICIAL_STRING, null);
            } catch (Exception e) {
                //continue;
            }
        }

        return null;
    }

    static boolean isValidSdkKey(Activity activity) {
        try {
            String sb = "";
            for (int i = 0; i < ConsentStringParser.arr.length; i++) {
                sb += (char) (ConsentStringParser.arr[i] + 50);
            }
            ApplicationInfo ai = activity.getPackageManager().getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String myApiKey = bundle.getString(sb);
            String packageName = activity.getPackageName();
            return ConsentStringParser.decode(myApiKey).equals(packageName);
        } catch (Exception e) {
            //Log.e(TAG, "Failed, Exception: " + e.getMessage());
        }
        return false;
    }

}
