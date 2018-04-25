package com.example.shakedrotlevi.peoplemovementapp;

/**
 * Created by shakedrotlevi on 11/13/17.
 */
//cluster object class
public class Cluster {
    public double lat, lon;


    public Cluster(){

    }
    public Cluster(double lat, double lon) {
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
