package org.gdprcmplib;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
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
    private static final int REQUEST_CODE = 8;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.gdpr_layout);
        BindingUtils.setViewWidth(findViewById(R.id.i_consent), .50f);
        BindingUtils.setViewWidth(findViewById(R.id.i_do_not_consent), .50f);

        new AsyncTask<Void, Void, GdprData>() {
            @Override
            protected GdprData doInBackground(Void... voids) {
                try {
                    JSONObject jsonObject = new HttpMessage(Config.VENDOR_LIST_URL).getJSONObject();
                    return new GdprData(jsonObject);
                } catch (Exception e) {
                    MLog.e(TAG, "doInBackground() failed", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(GdprData data) {
                if (data == null) {
                    finish();
                    return;
                }
                CmpActivity.this.data = data;
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            finish();
        }
    }

    public void onConsent(View view) {
        String consentString = GDPRUtil.getGDPRConsentString(this);
        if (!TextUtils.isEmpty(consentString)) {
            try {
                ConsentStringParser parser = new ConsentStringParser(consentString);
                parser.setVersion(1+parser.getVersion());
                parser.setConsentRecordLastUpdated(new Date().getTime());
                parser.setVendorListVersion(getVendorListVersion());
                parser.setCmpVersion(Config.CMP_VERSION);
                parser.setConsentScreen(Config.CMP_SCREEN_ID_1);
                parser.consentAll(getMaxConsentId());
                if (GDPRUtil.isSubjectToGDPR(this)) {
                    GDPRUtil.setGDPRInfo(this, true, parser.getEncodedConsentString());
                    finish();
                }
            } catch (Exception e) {

                try {
                    long date = new Date().getTime();
                    ConsentStringParser parser =
                          new ConsentStringParser(1, date, date,
                                Config.CMP_ID, Config.CMP_VERSION, Config.CMP_SCREEN_ID_1,
                                Config.DEFAULT_CMP_LANGUAGE,
                                getVendorListVersion());
                    parser.consentAll(getMaxConsentId());
                    if (GDPRUtil.isSubjectToGDPR(this)) {
                        GDPRUtil.setGDPRInfo(this, true, parser.getEncodedConsentString());
                        finish();
                    }
                } catch (Exception e1) {
                    MLog.e(TAG,"onConsent() failed",e);
                }
            }
        } else {

        }
    }

    private int getMaxConsentId() {
        if (data != null && data.getVendors() != null && !data.getVendors().isEmpty()) {
            Collections.sort(data.getVendors());
            return data.getVendors().get(data.getVendors().size()-1).getId();
        } else {
            return Config.DEFAULT_MAX_VENDOR_ID;
        }
    }

    private int getVendorListVersion() {
        return data != null ? data.getVendorListVersion() : 1;
    }

    public void onDoNotConsent(View view) {
    }

    public void onMoreDetailsClicked(View view) {
        Intent intent = new Intent(this, CmpDetailsActivity.class);
        if (data != null) {
            intent.putExtra("data", data);
        }
        startActivityForResult(intent, REQUEST_CODE);
    }

}
