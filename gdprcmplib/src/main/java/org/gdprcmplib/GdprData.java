package org.gdprcmplib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GdprData implements Serializable {
    private static final String TAG = "GdprData";

    static final int NUM_PURPOSES = 5;
    static final int NUM_FEATURES = 3;
    private Map<Integer, GdprPurpose> purposesMap = new HashMap<>(NUM_PURPOSES);
    private List<GdprPurpose> purposes = new ArrayList<>(NUM_PURPOSES);
    private Map<Integer, GdprFeature> featuresMap = new HashMap<>(NUM_FEATURES);
    private List<GdprVendor> vendors = new ArrayList<>(500);

    private SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private long lastUpdated;
    private int vendorListVersion=1;
    public GdprData(JSONObject jsonObject) {
        try {
            String s = jsonObject.optString("lastUpdated");
            if (s != null && s.contains("T")) {
                s = s.substring(0,s.indexOf('T'));
                try {
                    lastUpdated = SDF.parse(s).getTime();
                }catch (Exception e) {
                    MLog.e(TAG,"Could not parse date: "+s);
                }
            }
            vendorListVersion = jsonObject.optInt("vendorListVersion");
            JSONArray purposesArr = jsonObject.optJSONArray("purposes");
            for (int i=0;purposesArr != null && i < purposesArr.length();i++) {
                GdprPurpose p = new GdprPurpose(purposesArr.getJSONObject(i));
                purposesMap.put(p.getId(),p);
                purposes.add(p);
            }
            JSONArray featuresArr = jsonObject.optJSONArray("features");
            for (int i=0;featuresArr != null && i < featuresArr.length();i++) {
                GdprFeature f = new GdprFeature(featuresArr.getJSONObject(i));
                featuresMap.put(f.getId(),f);
            }
            JSONArray vendorsArr = jsonObject.optJSONArray("vendors");
            for (int i=0;vendorsArr != null && i < vendorsArr.length();i++) {
                GdprVendor vendor = new GdprVendor(vendorsArr.getJSONObject(i), purposesMap, featuresMap);
                this.vendors.add(vendor);
            }
            Collections.sort(vendors);
            Collections.sort(purposes);
        }catch (Exception e) {
            MLog.e(TAG, "GdprData(jsonObject) failed",e);
        }
    }

    public Date getLastUpdated() {
        return new Date(lastUpdated);
    }

    public int getVendorListVersion() {
        return vendorListVersion;
    }

    public List<GdprPurpose> getPurposes() {
        return purposes;
    }

    public List<GdprVendor> getVendors() {
        return vendors;
    }

    void initStateWith(ConsentStringParser consentString) {
        if (purposes != null) {
            for (int i=0; i < purposes.size();i++) {
                GdprPurpose purpose = purposes.get(i);
                purpose.setAllowed(consentString.isPurposeAllowed(purpose.getId()));
            }
        }
        if (vendors != null) {
            for (int i=0; i < vendors.size();i++) {
                GdprVendor vendor = vendors.get(i);
                vendor.setAllowed(consentString.isVendorAllowed(vendor.getId()));
            }
        }
    }

    public void updateAllowedVendorsByPurpose(GdprPurpose purpose) {
        for (int i=0;i<vendors.size();i++) {
            GdprVendor vendor = vendors.get(i);
            vendor.setAllowed(purpose);
        }
    }

    public boolean isAll(boolean isConsent) {
        for (int i=0;i<purposes.size();i++) {
            if (purposes.get(i).isAllowed() != isConsent) {
                return false;
            }
        }
        for (int i=0;i<vendors.size();i++) {
            if (vendors.get(i).isAllowed() != isConsent) {
                return false;
            }
        }
        return true;
    }
}
