package org.gdprcmplib;

import org.json.JSONObject;

public class GdprPurpose {
    private static final String TAG = "GdprPurpose";
    private int id;
    private String name;
    private String descr;

    public GdprPurpose(int id, String name, String descr) {
        this.id = id;
        this.name = name;
        this.descr = descr;
    }

    public GdprPurpose(JSONObject jsonObject) {
        try {
            id = jsonObject.optInt("id");
            name = jsonObject.optString("name");
            descr = jsonObject.optString("description");
        }catch (Exception e) {
            MLog.e(TAG,"GdprPurpose(jsonObject) failed",e);
        }
    }

    public GdprPurpose() {
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
        GdprPurpose other = (GdprPurpose)obj;
        return id == other.id;
    }

    @Override
    public String toString() {
        return "GDPR Purpose.  id: " + id + " name: "+name;
    }
}
