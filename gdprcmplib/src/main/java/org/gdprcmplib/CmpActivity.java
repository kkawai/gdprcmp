package org.gdprcmplib;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import org.json.JSONObject;

import java.util.Date;

public class CmpActivity extends AppCompatActivity {

    public static final String TAG = "CmpActivity";
    private GdprData d; //since we can't fully obfuscate an activity, do our own
    private ConsentStringParser c; //since we can't fully obfuscate an activity, do our own
    private static final int REQUEST_CODE = 8;
    private boolean isAllowBackButton;
    private boolean defaultConsentAll;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gdpr_layout);
        UiUtils.setViewWidth(findViewById(R.id.i_consent), .50f);
        UiUtils.setViewWidth(findViewById(R.id.i_do_not_consent), .50f);

        try {
            isAllowBackButton = getIntent().getBooleanExtra(Config.CMP_ALLOW_BACK_BUTTON, false);
            defaultConsentAll = getIntent().getBooleanExtra(Config.CMP_DEFAULT_CONSENT_ALL, true);
        } catch (Exception e) {
            MLog.e(TAG, "onCreate() trapped exception while getting CMP_ALLOW_BACK_BUTTON from intent", e);
        }

        new AsyncTask<Void, Void, GdprData>() {
            @Override
            protected GdprData doInBackground(Void... voids) {
                try {
                    loadConsentString();
                    String lang = (GDPRUtil.getLanguage(CmpActivity.this)+"").toLowerCase();
                    JSONObject langJSON=null;
                    String langUrl=null;
                    if (!TextUtils.isEmpty(lang) && !lang.equalsIgnoreCase("en")) {
                        try {
                            langUrl = Config.LANGUAGE_SPECIFIC_URL.replace("REPLACEME",lang);
                            MLog.d(TAG,"langUrl: "+lang + " " + langUrl);
                            langJSON = new HttpMessage(langUrl).getJSONObject();
                        }catch(Exception e) {
                            MLog.e(TAG,"Failed to get language specific remote data for: "+langUrl);
                        }
                    }
                    JSONObject vendorJSON = new HttpMessage(Config.VENDOR_LIST_URL).getJSONObject();
                    d = new GdprData(vendorJSON, langJSON);
                    if (d != null && c != null) {
                        d.initStateWith(c);
                    }
                    if (d != null && c == null) {
                        d.setDefaultConsent(defaultConsentAll);
                    }
                    return d;
                } catch (Exception e) {
                    MLog.e(TAG, "doInBackground() failed", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(GdprData data) {
                if (data == null) {
                    finish(CmpActivityResult.RESULT_COULD_NOT_FETCH_VENDOR_LIST);
                    return;
                }
            }
        }.execute();
        UiUtils.setBuyButton(findViewById(R.id.mainView));
    }

    private void finish(int resultCode) {
        setResult(resultCode);
        finish();
    }

    private void loadConsentString() {
        try {
            String consentString = GDPRUtil.getGDPRConsentString(this);
            if (!TextUtils.isEmpty(consentString)) {
                this.c = new ConsentStringParser(consentString);
            }
        } catch (Exception e) {
            MLog.e(TAG, "loadConsentString() failed", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == CmpActivityResult.RESULT_CONSENT_CUSTOM_PARTIAL) {
            finish(CmpActivityResult.RESULT_CONSENT_CUSTOM_PARTIAL);
        }
    }

    /**
     * Handle button click
     *
     * @param view
     */
    public void onConsent(View view) {
        consent(true);
    }

    private void consent(boolean isConsent) {
        if (c != null) {
            try {
                update(c, isConsent);
                return;
            } catch (Exception e) {
                MLog.e(TAG, "rangeConsent failed to update. isConsent: " + isConsent + " failed", e);
            }
        }
        try {
            create(isConsent);
        } catch (Exception e) {
            MLog.e(TAG, "rangeConsent failed to create.  isConsent: "+ isConsent + " failed", e);
            finish(CmpActivityResult.RESULT_FAILED_TO_WRITE_CONSENT_STRING);
        }
    }

    private void update(ConsentStringParser consentString, boolean isConsent) throws Exception {
        consentString.setVersion(1 + consentString.getVersion());
        consentString.setConsentRecordLastUpdated(new Date().getTime());
        consentString.setVendorListVersion(getVendorListVersion());
        consentString.setCmpVersion(Config.CMP_VERSION);
        consentString.setConsentScreen(Config.CMP_SCREEN_ID_1);
        consentString.rangeConsent(getMaxConsentId(), isConsent, !isConsent);
        persist(consentString, isConsent);
    }

    private void persist(ConsentStringParser consentString, boolean isConsent) throws Exception {
        GDPRUtil.setGDPRConsentString(this, consentString.getEncodedConsentString());
        UiUtils.showSuccessDialog(this, isConsent ? CmpActivityResult.RESULT_CONSENT_ALL : CmpActivityResult.RESULT_CONSENT_NONE);
    }

    private void create(boolean isConsent) throws Exception {
        long date = new Date().getTime();
        ConsentStringParser parser =
                new ConsentStringParser(1, date, date,
                        Config.CMP_ID, Config.CMP_VERSION, Config.CMP_SCREEN_ID_1,
                        Config.DEFAULT_CMP_LANGUAGE,
                        getVendorListVersion());
        parser.rangeConsent(getMaxConsentId(), isConsent, !isConsent);
        persist(parser, isConsent);
    }

    private int getMaxConsentId() {
        if (d != null && d.getVendors() != null && !d.getVendors().isEmpty()) {
            return d.getVendors().get(d.getVendors().size() - 1).getId();
        } else {
            return Config.DEFAULT_MAX_VENDOR_ID;
        }
    }

    private int getVendorListVersion() {
        return d != null ? d.getVendorListVersion() : 1;
    }

    public void onDoNotConsent(View view) {
        consent(false);
    }

    public void onMoreDetailsClicked(View view) {
        Intent intent = new Intent(this, CmpDetailsActivity.class);
        if (d != null) {
            intent.putExtra("d", d);
        }
        intent.putExtra(Config.CMP_ALLOW_BACK_BUTTON, isAllowBackButton);
        intent.putExtra(Config.CMP_DEFAULT_CONSENT_ALL, defaultConsentAll);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        if (isAllowBackButton)
            super.onBackPressed();
    }

    public void onBuy(View view) {
        try {
            view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://gdpr-sdk.com/")));
        }catch (Exception e) {
            MLog.e(TAG,"Could not view privacy policy url",e);
        }
    }
}
