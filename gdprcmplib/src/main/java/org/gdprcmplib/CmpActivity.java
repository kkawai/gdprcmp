package org.gdprcmplib;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.json.JSONObject;

public class CmpActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity2";
    private TextView textView;
    private GdprData data;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gdpr_layout);
        textView = findViewById(R.id.text);
        textView.setText("Loading vendor list...");
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
                try {
                    textView.setText("Vendor list size: " + GdprData.VENDORS.size() + " lastUpdated: "+gdprData.getLastUpdated());
                }catch (Exception e) {
                    textView.setText("error displaying vendor list: "+e);
                }
            }
        }.execute();
    }

}
