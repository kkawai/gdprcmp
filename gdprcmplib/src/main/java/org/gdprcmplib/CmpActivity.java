package org.gdprcmplib;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;

public class CmpActivity extends AppCompatActivity {

    public static final String TAG = "CmpActivity";
    private GdprData data;
    private ConsentStringParser consentString;
    private static final int REQUEST_CODE = 8;
    private boolean isAllowBackButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.gdpr_layout);
        BindingUtils.setViewWidth(findViewById(R.id.i_consent), .50f);
        BindingUtils.setViewWidth(findViewById(R.id.i_do_not_consent), .50f);

        try {
            isAllowBackButton = getIntent().getBooleanExtra(Config.CMP_ALLOW_BACK_BUTTON, false);
        } catch (Exception e) {
            MLog.e(TAG, "onCreate() trapped exception while getting CMP_ALLOW_BACK_BUTTON from intent", e);
        }

        new AsyncTask<Void, Void, GdprData>() {
            @Override
            protected GdprData doInBackground(Void... voids) {
                try {
                    loadConsentString();
                    JSONObject jsonObject = new HttpMessage(Config.VENDOR_LIST_URL).getJSONObject();
                    data = new GdprData(jsonObject);
                    if (data != null && consentString != null) {
                        data.initStateWith(consentString);
                    }
                    return data;
                } catch (Exception e) {
                    MLog.e(TAG, "doInBackground() failed", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(GdprData data) {
                if (data == null) {
                    finish(Config.RESULT_COULD_NOT_FETCH_VENDOR_LIST);
                    return;
                }
            }
        }.execute();

        findViewById(R.id.mainView).setVisibility(GDPRUtil.isValidSdkKey(this) ? View.GONE : View.VISIBLE);
    }

    private void finish(int resultCode) {
        setResult(resultCode);
        finish();
    }

    private void loadConsentString() {
        try {
            String consentString = GDPRUtil.getGDPRConsentString(this);
            if (!TextUtils.isEmpty(consentString)) {
                this.consentString = new ConsentStringParser(consentString);
            }
        } catch (Exception e) {
            MLog.e(TAG, "loadConsentString() failed", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            finish(Config.RESULT_CONSENT_CUSTOM_PARTIAL);
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
        if (consentString != null) {
            try {
                update(consentString, isConsent);
                return;
            } catch (Exception e) {
                MLog.e(TAG, "consent failed to update. isConsent: " + isConsent + " failed", e);
            }
        }
        try {
            create(isConsent);
        } catch (Exception e) {
            MLog.e(TAG, "consent failed to create.  isConsent: "+ isConsent + " failed", e);
        }
    }

    private void good() {

    }

    private void update(ConsentStringParser consentString, boolean isConsent) throws Exception {
        consentString.setVersion(1 + consentString.getVersion());
        consentString.setConsentRecordLastUpdated(new Date().getTime());
        consentString.setVendorListVersion(getVendorListVersion());
        consentString.setCmpVersion(Config.CMP_VERSION);
        consentString.setConsentScreen(Config.CMP_SCREEN_ID_1);
        consentString.consent(getMaxConsentId(), isConsent);
        persist(consentString, isConsent);
    }

    private void persist(ConsentStringParser consentString, boolean isConsent) throws Exception {
        if (GDPRUtil.isSubjectToGDPR(this)) {
            GDPRUtil.setGDPRInfo(this, true, consentString.getEncodedConsentString());
        }
        finish(isConsent ? Config.RESULT_CONSENT_ALL : Config.RESULT_CONSENT_NONE);
    }

    private void create(boolean isConsent) throws Exception {
        long date = new Date().getTime();
        ConsentStringParser parser =
                new ConsentStringParser(1, date, date,
                        Config.CMP_ID, Config.CMP_VERSION, Config.CMP_SCREEN_ID_1,
                        Config.DEFAULT_CMP_LANGUAGE,
                        getVendorListVersion());
        parser.consent(getMaxConsentId(), isConsent);
        persist(consentString, isConsent);
    }

    private int getMaxConsentId() {
        if (data != null && data.getVendors() != null && !data.getVendors().isEmpty()) {
            Collections.sort(data.getVendors());
            return data.getVendors().get(data.getVendors().size() - 1).getId();
        } else {
            return Config.DEFAULT_MAX_VENDOR_ID;
        }
    }

    private int getVendorListVersion() {
        return data != null ? data.getVendorListVersion() : 1;
    }

    public void onDoNotConsent(View view) {
        consent(false);
    }

    public void onMoreDetailsClicked(View view) {
        Intent intent = new Intent(this, CmpDetailsActivity.class);
        if (data != null) {
            intent.putExtra("data", data);
        }
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
