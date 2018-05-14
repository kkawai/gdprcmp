package org.gdprcmplib;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONObject;

public class CmpDetailsActivity extends AppCompatActivity {

    public static final String TAG = "CmpDetailsActivity";
    private GdprData data;
    private RecyclerView recyclerView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gdpr_detailed_layout);
        recyclerView = findViewById(R.id.recyclerView);
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
                if (gdprData == null) {
                    //handle error
                    return;
                }
                CmpDetailsActivity.this.data = gdprData;
                /*try {
                    textView.setText("Vendor list size: " + GdprData.VENDORS.size() + " lastUpdated: "+gdprData.getLastUpdated());
                }catch (Exception e) {
                    textView.setText("error displaying vendor list: "+e);
                }*/
            }
        }.execute();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, descr, featuresAndPurposes;
        CheckBox checkbox;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            descr = itemView.findViewById(R.id.descr);
            featuresAndPurposes = itemView.findViewById(R.id.features_and_purposes);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }

    public void onSave(View view) {
    }

    public void onToggle(View view) {
        ToggleButton toggleButton = (ToggleButton)view;
        MLog.d(TAG,"toggleButton state: "+toggleButton.isChecked());
    }
}
