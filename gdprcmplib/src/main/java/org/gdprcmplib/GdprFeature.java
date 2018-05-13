package org.gdprcmplib;

import org.json.JSONObject;

public class GdprFeature {
    private static final String TAG = "GdprFeature";
    private int id;
    private String name;
    private String descr;

    public GdprFeature(int id, String name, String descr) {
        this.id = id;
        this.name = name;
        this.descr = descr;
    }

    public GdprFeature(JSONObject jsonObject) {
        try {
            id = jsonObject.optInt("id");
            name = jsonObject.optString("name");
            descr = jsonObject.optString("description");
        }catch (Exception e) {
            MLog.e(TAG,"GdprFeature(jsonObject) failed",e);
        }
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

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        GdprFeature other = (GdprFeature)obj;
        return id == other.id;
    }

    @Override
    public String toString() {
        return "GDPR Feature.  id: " + id + " name: "+name;
    }
}
