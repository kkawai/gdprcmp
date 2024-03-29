package org.gdprcmplib;

final class Config {
    static final boolean IS_FREE_FOR_ALL = true;
    static final boolean IS_LOGGING_ENABLED = false;
    static final String VENDOR_LIST_URL = "https://vendorlist.consensu.org/vendorlist.json";
    static final String LANGUAGE_SPECIFIC_URL = "https://vendorlist.consensu.org/purposes-REPLACEME.json";
    static final int CMP_ID = 99;
    static final int CMP_VERSION = 3; //todo increment whenever you do release
    static final int CMP_SCREEN_ID_1 = 1;
    static final int CMP_SCREEN_ID_2 = 2;
    static final int DEFAULT_MAX_VENDOR_ID = 121;

    public static final String CMP_ALLOW_BACK_BUTTON = "bundle_key_cmp_allow_back_button";
    public static final String CMP_DEFAULT_CONSENT_ALL = "bundle_key_cmp_default_consent_all";
}
