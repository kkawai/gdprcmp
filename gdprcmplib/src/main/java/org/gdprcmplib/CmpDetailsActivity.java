package org.gdprcmplib;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ToggleButton;

import org.json.JSONObject;

public class CmpDetailsActivity extends AppCompatActivity {

    public static final String TAG = "CmpDetailsActivity";
    private GdprData data;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.gdpr_detailed_layout);
        new AsyncTask<Void,Void,GdprData>() {
            @Override
            protected GdprData doInBackground(Void... voids) {
                try {
                    JSONObject jsonObject = new HttpMessage(Config.VENDOR_LIST_URL).getJSONObject();
                    return new GdprData(jsonObject);
                }catch(Exception e) {
                    MLog.e(TAG,"doInBackground() failed",e);
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

    public void onSave(View view) {
    }

    public void onToggle(View view) {
        ToggleButton toggleButton = (ToggleButton)view;
        MLog.d(TAG,"toggleButton state: "+toggleButton.isChecked());
    }
}
