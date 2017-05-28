package us.xingkong.gcubusstudentclient.gbn;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SeaLynn0 on 2017/5/17.
 */

public class Point extends JSONObject{

    private double longitude;
    private double latitude;

    public Point(double lon,double lat)
    {
        longitude = lon;
        latitude = lat;
    }

    public Point(String jsonData) throws JSONException {
        super(jsonData);
        longitude = super.getDouble("longitude");
        latitude =  super.getDouble("latitude");
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
