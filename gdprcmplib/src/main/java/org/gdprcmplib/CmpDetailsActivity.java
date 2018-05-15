package org.gdprcmplib;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
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
    private MyAdapter myAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gdpr_detailed_layout);
        recyclerView = findViewById(R.id.recyclerView);

        new AsyncTask<Void, Void, GdprData>() {
            @Override
            protected GdprData doInBackground(Void... voids) {
                try {
                    if (getIntent().hasExtra("data")) {
                        MLog.d(TAG,"got serialized data from intent");
                        return (GdprData) getIntent().getSerializableExtra("data");
                    }
                    MLog.d(TAG,"fetch gdpr data");
                    JSONObject jsonObject = new HttpMessage(Config.VENDOR_LIST_URL).getJSONObject();
                    return new GdprData(jsonObject);
                } catch (Exception e) {
                    MLog.e(TAG, "doInBackground() failed", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(GdprData gdprData) {
                if (gdprData == null) {
                    finish();
                    return;
                }
                CmpDetailsActivity.this.data = gdprData;
                renderUI();
            }
        }.execute();

    }

    private void renderUI() {
        myAdapter = new MyAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CmpDetailsActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail, parent, false);
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return data.getPurposes().size() + data.getVendors().size();
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
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateCheckbox();
                }
            };
            itemView.setOnClickListener(clickListener);
            checkbox.setOnClickListener(clickListener);
        }



        private void updateCheckbox() {
            int position = getAdapterPosition();
            Pair<GdprPurpose, GdprVendor> pair = getListItem(position);
            if (pair.first != null) {
                pair.first.setAllowed(!pair.first.isAllowed());
            } else {
                pair.second.setAllowed(!pair.second.isAllowed());
            }
            myAdapter.notifyItemChanged(position);
        }

        void bind(int position) {
            Pair<GdprPurpose, GdprVendor> pair = getListItem(position);

            if (pair.first != null) {
                GdprPurpose purpose = pair.first;
                name.setText(purpose.getName());
                descr.setText(purpose.getDescr());
                featuresAndPurposes.setVisibility(View.GONE);
                checkbox.setChecked(purpose.isAllowed());
            } else {
                GdprVendor vendor = pair.second;
                name.setText(vendor.getName());
                descr.setText(vendor.getPolicyUrl());
                if (vendor.getLegIntPurposes().size() > 0) {
                    featuresAndPurposes.setVisibility(View.VISIBLE);
                    StringBuilder sb = new StringBuilder(128);
                    for (int i = 0; i < vendor.getLegIntPurposes().size(); i++) {
                        GdprPurpose purpose = vendor.getLegIntPurposes().get(i);
                        //sb.append("· ").append(purpose.getName()).append(" -> ").append(purpose.getDescr()).append("\n");
                        sb.append("· ").append(purpose.getName()).append("\n");
                    }
                    featuresAndPurposes.setText(sb.toString());
                } else {
                    featuresAndPurposes.setVisibility(View.GONE);
                }
                checkbox.setChecked(vendor.isAllowed());
            }

        }
    }

    private Pair<GdprPurpose, GdprVendor> getListItem(int position) {
        if (position < data.getPurposes().size()) {
            GdprPurpose purpose = data.getPurposes().get(position);
            Pair<GdprPurpose, GdprVendor> pair = new Pair<>(purpose, null);
            return pair;
        } else {
            GdprVendor vendor = data.getVendors().get(position - data.getPurposes().size());
            Pair<GdprPurpose, GdprVendor> pair = new Pair<>(null, vendor);
            return pair;
        }
    }

    public void onSave(View view) {
    }

    public void onToggle(View view) {
        ToggleButton toggleButton = (ToggleButton) view;
        MLog.d(TAG, "toggleButton state: " + toggleButton.isChecked());
        for (int i = 0; i < data.getPurposes().size(); i++) {
            GdprPurpose purpose = data.getPurposes().get(i);
            purpose.setAllowed(toggleButton.isChecked());
        }
        for (int i = 0; i < data.getVendors().size(); i++) {
            GdprVendor vendor = data.getVendors().get(i);
            vendor.setAllowed(toggleButton.isChecked());
        }
        myAdapter.notifyDataSetChanged();
    }
}
