package org.gdprcmplib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GdprData {
    private static final String TAG = "GdprData";

    static final int NUM_PURPOSES = 5;
    static final int NUM_FEATURES = 3;
    static Map<Integer, GdprPurpose> PURPOSES = new HashMap<>(NUM_PURPOSES);
    static Map<Integer, GdprFeature> FEATURES = new HashMap<>(NUM_FEATURES);
    static List<GdprVendor> VENDORS = new ArrayList<>(200);

    private SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private long lastUpdated;
    private int vendorListVersion;
    public GdprData(JSONObject jsonObject) {
        try {
            String s = jsonObject.getString("lastUpdated");
            if (s.contains("T")) {
                s = s.substring(0,s.indexOf('T'));
            }
            try {
                lastUpdated = SDF.parse(s).getTime();
            }catch (Exception e) {
                MLog.e(TAG,"Could not parse date: "+s);
            }
            vendorListVersion = jsonObject.getInt("vendorListVersion");
            JSONArray purposes = jsonObject.getJSONArray("purposes");
            for (int i=0;i < purposes.length();i++) {
                GdprPurpose p = new GdprPurpose(purposes.getJSONObject(i));
                PURPOSES.put(p.getId(),p);
            }
            JSONArray features = jsonObject.getJSONArray("features");
            for (int i=0;i < features.length();i++) {
                GdprFeature f = new GdprFeature(features.getJSONObject(i));
                FEATURES.put(f.getId(),f);
            }
            JSONArray vendors = jsonObject.getJSONArray("vendors");
            for (int i=0;i < vendors.length();i++) {
                GdprVendor vendor = new GdprVendor(vendors.getJSONObject(i));
                VENDORS.add(vendor);
            }
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
}
