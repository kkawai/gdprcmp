package org.gdprcmplib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GdprVendor implements Serializable {
    private static final String TAG = "GdprVendor";
    private int id;
    private String name;
    private String policyUrl;
    private boolean isAllowed;

    private List<GdprPurpose> purposes;
    private List<GdprFeature> features;
    private List<GdprPurpose> legIntPurposes;

    public GdprVendor(JSONObject jsonObject, Map<Integer, GdprPurpose> purposesMap, Map<Integer, GdprFeature> featuresMap) {
        try {
            id = jsonObject.optInt("id");
            name = jsonObject.optString("name");
            policyUrl = jsonObject.optString("policyUrl");
            purposes = new ArrayList<>(GdprData.NUM_PURPOSES);
            features = new ArrayList<>(GdprData.NUM_FEATURES);
            legIntPurposes = new ArrayList<>(GdprData.NUM_PURPOSES);

            JSONArray purposeIds = jsonObject.optJSONArray("purposeIds");
            JSONArray legIntPurposeIds = jsonObject.optJSONArray("legIntPurposeIds");
            JSONArray featureIds = jsonObject.optJSONArray("featureIds");

            for (int i=0;purposeIds != null && i < purposeIds.length();i++) {
                purposes.add(purposesMap.get(purposeIds.getInt(i)));
            }
            for (int i=0;legIntPurposeIds != null && i < legIntPurposeIds.length();i++) {
                legIntPurposes.add(purposesMap.get(legIntPurposeIds.getInt(i)));
            }
            for (int i=0;featureIds != null && i < featureIds.length();i++) {
                features.add(featuresMap.get(featureIds.getInt(i)));
            }
        }catch (Exception e) {
            MLog.e(TAG,"GdprPurpose(jsonObject) failed",e);
        }
    }

    public GdprVendor(int id, String name, String policyUrl) {
        this.id = id;
        this.name = name;
        this.policyUrl = policyUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPolicyUrl() {
        return policyUrl;
    }

    public void setPolicyUrl(String policyUrl) {
        this.policyUrl = policyUrl;
    }

    public List<GdprPurpose> getPurposes() {
        return purposes;
    }

    public void setPurposes(List<GdprPurpose> purposes) {
        this.purposes = purposes;
    }

    public List<GdprFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<GdprFeature> features) {
        this.features = features;
    }

    public List<GdprPurpose> getLegIntPurposes() {
        return legIntPurposes;
    }

    public void setLegIntPurposes(List<GdprPurpose> legIntPurposes) {
        this.legIntPurposes = legIntPurposes;
    }

    public boolean hasPurpose(GdprPurpose purpose) {
        if (purposes == null) return false;
        return purposes.contains(purpose);
    }

    public boolean hasLegIntPurpose(GdprPurpose purpose) {
        if (legIntPurposes == null) return false;
        return legIntPurposes.contains(purpose);
    }

    public boolean hasFeature(GdprFeature feature) {
        if (features == null) return false;
        return features.contains(feature);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        GdprVendor other = (GdprVendor)obj;
        return id == other.id;
    }

    public boolean isAllowed() {
        return isAllowed;
    }

    public void setAllowed(boolean allowed) {
        isAllowed = allowed;
    }

    @Override
    public String toString() {
        return "GDPR Vendor.  id: " + id + " name: "+name;
    }
}
