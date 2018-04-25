package com.example.shakedrotlevi.peoplemovementapp;

/**
 * Created by shakedrotlevi on 11/13/17.
 */

public class Location {
    double lat, lon;
    String welcome;

    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        welcome = "";
    }
    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }


}
