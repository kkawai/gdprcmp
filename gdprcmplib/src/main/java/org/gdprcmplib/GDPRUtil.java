package org.gdprcmplib;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic utility to support all GDPR related functionality.
 */
final class GDPRUtil {

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

    static void setGDPRInfo(final Context context, final boolean isSubjectToGDPR, final String iabConsentString) {
        try {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(OFFICIAL_APPLICABLE, isSubjectToGDPR?"1":"0").apply();
            if (isSubjectToGDPR) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString(OFFICIAL_STRING, iabConsentString).apply();
            }
        } catch (Exception e) {
            MLog.e(TAG, "setGDPR failed.  context: " + context + " iabConstentString: " + iabConsentString, e);
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
    private static Boolean isSubjectToGDPR;
    static boolean isSubjectToGDPR(Context context) {

        if (isSubjectToGDPR != null) {
            return isSubjectToGDPR;
        }

        //First, check the iab spec
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        if (prefs.contains(OFFICIAL_APPLICABLE)) {
            try {
                isSubjectToGDPR =  prefs.getInt(OFFICIAL_APPLICABLE, -1) == 1;
                return isSubjectToGDPR;
            } catch (Exception e) {
                //continue
            }
        }

        //lastly, use our own determination if this region is subject to GDPR
        /*
         * first check if the developer has saved the GDPR applicability determination
         * which has yet to be determined.
         */
        //let's cache this value when we figure out the implementation details
        isSubjectToGDPR = isGDPRRegion(context); //for now, deploy our own GDPR determination
        return isSubjectToGDPR;
    }

    /**
     * Implemenation yet to be determined.  We check if GDPR consent string has been
     * set by the developer.
     *
     * @return - String saved by the developer.  If null, then developer has simply
     * not set it.
     */
    private static String consentString = "cannot_be_this";
    static String getGDPRConsentString(Context context) {

        if (consentString == null || !consentString.equals("cannot_be_this")) {
            return consentString;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //first try the official iab spec string
        if (prefs.contains(OFFICIAL_STRING)) {
            try {
                consentString = prefs.getString(OFFICIAL_STRING, null);
                return consentString;
            } catch (Exception e) {
                //continue;
            }
        }

        //let's cache this value when we figure out the implementation details
        consentString = null;
        return consentString;
    }

}
