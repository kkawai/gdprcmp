package org.gdprcmplib;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONObject;

import java.util.Date;

public class CmpDetailsActivity extends AppCompatActivity {

    public static final String TAG = "CmpDetailsActivity";
    private GdprData data;
    private ConsentStringParser consentString;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private boolean isAllowBackButton=true;
    private boolean defaultConsentAll;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gdpr_detailed_layout);
        recyclerView = findViewById(R.id.recyclerView);

        try {
            isAllowBackButton = getIntent().getBooleanExtra(Config.CMP_ALLOW_BACK_BUTTON, true);
            defaultConsentAll = getIntent().getBooleanExtra(Config.CMP_DEFAULT_CONSENT_ALL, true);
        } catch (Exception e) {
            MLog.e(TAG, "onCreate() trapped exception while getting CMP_ALLOW_BACK_BUTTON from intent", e);
        }

        new AsyncTask<Void, Void, GdprData>() {
            @Override
            protected GdprData doInBackground(Void... voids) {
                try {
                    loadConsentString();
                    if (getIntent().hasExtra("data")) {
                        MLog.d(TAG,"got serialized data from intent");
                        data = (GdprData) getIntent().getSerializableExtra("data");
                        //if we got data from intent, then it was already initialized
                        //with the rangeConsent string
                    } else {
                        MLog.d(TAG,"fetch remote gdpr data");
                        JSONObject jsonObject = new HttpMessage(Config.VENDOR_LIST_URL).getJSONObject();
                        data = new GdprData(jsonObject);
                        if (data != null && consentString != null) {
                            data.initStateWith(consentString);
                        }
                        if (data != null && consentString == null) {
                            data.setDefaultConsent(defaultConsentAll);
                        }
                    }
                    if (data != null) {
                        if (data.isAll(true)) {
                            updateToggleButtonState(true);
                        } else if (data.isAll(false)) {
                            updateToggleButtonState(false);
                        }
                    }
                    return data;
                } catch (Exception e) {
                    MLog.e(TAG, "doInBackground() failed", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(GdprData gdprData) {
                if (data == null) {
                    finish(CmpActivityResult.RESULT_COULD_NOT_FETCH_VENDOR_LIST);
                    return;
                }
                renderUI();
            }
        }.execute();
        findViewById(R.id.mainView).setVisibility(GDPRUtil.isValidSdkKey(this) ? View.GONE : View.VISIBLE);
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
            View.OnClickListener urlClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Pair<GdprPurpose, GdprVendor> pair = getListItem(position);
                    if (pair.second != null) {
                        try {
                            view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(pair.second.getPolicyUrl())));
                        }catch (Exception e) {
                            MLog.e(TAG,"Could not view privacy policy url",e);
                        }
                    } else {
                        updateCheckbox();
                    }
                }
            };
            //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
            //startActivity(browserIntent);
            itemView.setOnClickListener(clickListener);
            checkbox.setOnClickListener(clickListener);
            descr.setOnClickListener(urlClickListener);
        }

        private void updateCheckbox() {
            final int position = getAdapterPosition();
            Pair<GdprPurpose, GdprVendor> pair = getListItem(position);
            if (pair.first != null) {
                pair.first.setAllowed(!pair.first.isAllowed());
                data.updateAllowedVendorsByPurpose(pair.first);
                //myAdapter.notifyDataSetChanged(); causes crash!!
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
                /*if (vendor.getLegIntPurposes().size() > 0) {
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
                }*/
                featuresAndPurposes.setVisibility(View.GONE);
                checkbox.setChecked(vendor.isAllowed());
            }

        }
    }

    private Pair<GdprPurpose, GdprVendor> getListItem(int position) {
        if (position < data.getPurposes().size()) {
            GdprPurpose purpose = data.getPurposes().get(position);
            return new Pair<>(purpose, null);
        } else {
            GdprVendor vendor = data.getVendors().get(position - data.getPurposes().size());
            return new Pair<>(null, vendor);
        }
    }

    public void onSave(View view) {
        if (consentString != null) {
            try {
                update(consentString);
                return;
            } catch (Exception e) {
                MLog.e(TAG, "onSave failed to update.", e);
            }
        }
        try {
            create();
        } catch (Exception e) {
            MLog.e(TAG, "onSave failed to create.", e);
            finish(CmpActivityResult.RESULT_FAILED_TO_WRITE_CONSENT_STRING);
        }
    }

    private void update(ConsentStringParser consentString) throws Exception {
        consentString.setVersion(1 + consentString.getVersion());
        consentString.setConsentRecordLastUpdated(new Date().getTime());
        consentString.setVendorListVersion(getVendorListVersion());
        consentString.setCmpVersion(Config.CMP_VERSION);
        consentString.setConsentScreen(Config.CMP_SCREEN_ID_2);
        consentString.bitwiseConsent(data);
        persist(consentString);
    }

    private void create() throws Exception {
        long date = new Date().getTime();
        ConsentStringParser parser =
                new ConsentStringParser(1, date, date,
                        Config.CMP_ID, Config.CMP_VERSION, Config.CMP_SCREEN_ID_1,
                        Config.DEFAULT_CMP_LANGUAGE,
                        getVendorListVersion());
        parser.bitwiseConsent(data);
        persist(parser);
    }

    private void persist(ConsentStringParser consentString) throws Exception {
        GDPRUtil.setGDPRConsentString(this, consentString.getEncodedConsentString());
        finish(CmpActivityResult.RESULT_CONSENT_CUSTOM_PARTIAL);
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

    private void updateToggleButtonState(final boolean isOn) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ToggleButton)findViewById(R.id.toggle)).setChecked(isOn);
            }
        });
    }

    public void onBuy(View view) {
        try {
            view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://gdpr-sdk.com/")));
        }catch (Exception e) {
            MLog.e(TAG,"Could not view privacy policy url",e);
        }
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

    private void finish(int resultCode) {
        setResult(resultCode);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isAllowBackButton)
            super.onBackPressed();
    }

    private int getVendorListVersion() {
        return data != null ? data.getVendorListVersion() : 1;
    }
}
