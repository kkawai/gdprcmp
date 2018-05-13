package org.gdprcmp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;

public class MainActivity2 extends AppCompatActivity {

    private TextView textView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);
        textView.setText("Loading vendor list...");
        new AsyncTask<Void,Void,JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                try {
                    return new HttpMessage(Config.VENDOR_LIST_URL).getJSONObject();
                }catch(Exception e) {
                    textView.setText("error fetching vendor list: "+e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject vendorList) {
                try {
                    textView.setText("Vendor list size: " + vendorList.getJSONArray("vendors").length());
                }catch (Exception e) {
                    textView.setText("error displaying vendor list: "+e);
                }
            }
        }.execute();
    }

}
