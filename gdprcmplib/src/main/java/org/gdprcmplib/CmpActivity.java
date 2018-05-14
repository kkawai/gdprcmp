package org.gdprcmplib;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

public class CmpActivity extends AppCompatActivity {

    public static final String TAG = "CmpActivity";
    private GdprData data;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.gdpr_layout);
        BindingUtils.setViewWidth(findViewById(R.id.i_consent),.50f);
        BindingUtils.setViewWidth(findViewById(R.id.i_do_not_consent),.50f);
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
            protected void onPostExecute(GdprData gdprData) {
                /*try {
                    textView.setText("Vendor list size: " + GdprData.VENDORS.size() + " lastUpdated: "+gdprData.getLastUpdated());
                }catch (Exception e) {
                    textView.setText("error displaying vendor list: "+e);
                }*/
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 8 && resultCode == Activity.RESULT_OK) {
            finish();
        }
    }

    public void onConsent(View view) {
    }

    public void onDoNotConsent(View view) {
    }

    public void onMoreDetailsClicked(View view) {
        Intent intent = new Intent(this, CmpDetailsActivity.class);
        startActivityForResult(intent, 8);
    }
}
