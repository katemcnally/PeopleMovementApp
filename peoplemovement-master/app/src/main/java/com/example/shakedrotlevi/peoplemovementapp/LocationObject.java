package com.example.shakedrotlevi.peoplemovementapp;

/**
 * Created by shakedrotlevi on 11/13/17.
 */

public class LocationObject {
    public double lat, lon;
    public LocationObject(){

    }
    public LocationObject(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;

    }
    public double getLat() {
        return lat;
    }


    public double getLon() {
        return lon;
    }

}
