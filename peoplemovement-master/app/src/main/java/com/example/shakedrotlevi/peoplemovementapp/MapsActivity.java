package com.example.shakedrotlevi.peoplemovementapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
//import android.graphics.Color;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.maps.android.PolyUtil;




import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;




//public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {
public class MapsActivity extends Activity implements OnMapReadyCallback {

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationRequest mLocationRequest;
    private GoogleMap map;
    Marker marker = null;
    ArrayList markerPoints= new ArrayList();
    LatLng currentLatLng, dest, origin;
    int lineOptionsSize = 0;
    double pointLat, pointLon;

    Circle clusterCircle;
    Marker groupLoc;
    ArrayList<LatLng> avoid = new ArrayList<LatLng>();
    LocationObject newLoc;
    Boolean checkedOnce = false;

    int counter=0;

    Polyline polyline = null;// = new Polyline();

    Map<String, LocationObject> locations = new HashMap<>(); // for storing locations for clusters

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference clusterArray = database.getReference("clusters");
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);    //create map fragment

        mapFragment.getMapAsync(this);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation); //set navigation menu
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    //initialize the bottom navigation menu
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(MapsActivity.this, MainActivity.class));
                    return true;
                case R.id.navigation_map:
                    return true;
                case R.id.navigation_create:
                    startActivity(new Intent(MapsActivity.this, CreateGroupActivity.class));
                    return true;
            }
            return false;
        }
    };



    //when map ready, start location updates
    @Override
    public void onMapReady(GoogleMap mapReady) {
        map = mapReady;
        startLocationUpdates( mapReady);
        //generateLocations(38.899934,-77.046641,1); //generate clusters

        generateLocations(38.898313, -77.049227,0, 28);//smith center on G
      //  generateLocations(38.899605, -77.049646,28, 50);//seh on h

    }

    //map set up
    public void setUpMap(){

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates(final GoogleMap mapReady) {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        //check if we have permissions, if not request it
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            startLocationUpdates(map);  //when we have permission, start location updates
            return;
        }


        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation(),mapReady);
                    }
                },
                Looper.myLooper());
    }


    //generate clusters
    public void generateLocations(double lat, double lon, int k, int size){

        LocationObject location1;
        //double lat=lat;, lon;
        int clusterSize = k+size;

        /* distance for creating clusters
        difference: .000365 in x lon
        difference: .000023 in y lat*/

        for(int i=k;i<clusterSize;i++){
            lat = lat + 0.00000023;
            lon = lon + 0.00000356;
            location1= new LocationObject(lat,lon);
            locations.put("location"+ i, location1);
        }

        DatabaseReference refMap = database.getReference("locations map");
        refMap.setValue(locations);

    }
    //when the user's location change
    public void onLocationChanged(final Location location, final GoogleMap mapReady) {
        // New location has now been determined

        if(marker!=null){
            marker.remove();
        }
        map = mapReady;
        map.getUiSettings().setZoomControlsEnabled(true);

        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        groupLocChanged(location);  //go to check if the user is an event's reator

        if (checkedOnce == false) {
            showClusters();
            checkMembership(mapReady);
            checkedOnce = true;
        }
    }

    //show clusters on the map
    public void showClusters(){
        clusterArray.child("array").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                GenericTypeIndicator<ArrayList<Double>> child = new GenericTypeIndicator<ArrayList<Double>>() {};
                ArrayList<Double> center = dataSnapshot.getValue(child);

                Double lat = (Double) center.get(0);
                Double lon = (Double)center.get(1);
                Double size = (Double)center.get(2);
                LatLng cluster = new LatLng(lat, lon);
                avoid.add(cluster); //add clusters to our "avoid" array
                //create the cluster representation, ratio for radius is .74
                clusterCircle = map.addCircle(new CircleOptions()
                        .center(new LatLng(lat, lon))
                        .radius(size*.74)
                        .strokeColor(android.R.color.black)
                        .fillColor(Color.argb(125,255,0,0)));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {

                if(clusterCircle!=null){
                    clusterCircle.remove();
                }
                GenericTypeIndicator<ArrayList<Double>> child = new GenericTypeIndicator<ArrayList<Double>>() {};
                ArrayList<Double> center = dataSnapshot.getValue(child);

                Double lat = (Double) center.get(0);
                Double lon = (Double)center.get(1);
                Double size = (Double)center.get(2);

                LatLng cluster = new LatLng(lat, lon);



                avoid.add(cluster); //add clusters to our "avoid" array
                //create the cluster representation
                clusterCircle = map.addCircle(new CircleOptions()
                        .center(new LatLng(lat, lon))
                        .radius(size*.74)
                        .strokeColor(android.R.color.black)
                        .fillColor(Color.argb(125,255,0,0)));


                sendDirectionRequest();

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });


    }
    //in order to show route, check what events the user is a part of
    public void checkMembership(final GoogleMap mapReady){
        //check what events user is a member of
        Query query = FirebaseDatabase.getInstance().getReference().child("events");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status;
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) { //loop through events
                    ArrayList<String> temp = (ArrayList<String>) childDataSnapshot.child("members").getValue();
                    if(temp.contains(user.getUid())){   //check if the event contains the user
                        status = (String)childDataSnapshot.child("status").getValue();//if contains user, check for status
                        if(status.equals("ONGOING")==true){ //if status is ongoing, show on map
                            origin = new LatLng((Double)childDataSnapshot.child("startLoc").child("lat").getValue(),(Double)childDataSnapshot.child("startLoc").child("lon").getValue() );
                            dest = new LatLng((Double)childDataSnapshot.child("endLoc").child("lat").getValue(),(Double)childDataSnapshot.child("endLoc").child("lon").getValue() );
                            markerPoints.add(origin);
                            markerPoints.add(dest);
                            mapReady.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                            groupLoc = map.addMarker(new MarkerOptions()
                                    .position(new LatLng((Double)childDataSnapshot.child("groupLoc").child("lat").getValue(), (Double)childDataSnapshot.child("groupLoc").child("lon").getValue()))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                            pointLat = dest.latitude;
                            pointLon = dest.longitude;
                            sendDirectionRequest(); //request route
                            break;
                        }
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //when creator's location changed and event is ONGOING
    public void groupLocChanged(final Location location){
        database.getReference("events").orderByChild("creator").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    newLoc = new LocationObject(location.getLatitude(), location.getLongitude());   //get the new group's location
                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        childDataSnapshot.child("groupLoc").getRef().setValue(newLoc);
                        if(((String)childDataSnapshot.child("status").getValue()).equals("ONGOING")==true){ //if event is ongoing, change group location
                            ArrayList<String> temp = (ArrayList<String>) childDataSnapshot.child("members").getValue();
                            if(temp.contains(user.getUid())){
                                updateGroupLoc();
                            }
                        }

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    //change group location on map when the creator's location changes
    public void updateGroupLoc(){

        if(groupLoc!=null) {
            groupLoc.remove();
        }

        groupLoc = map.addMarker(new MarkerOptions()
                .position(new LatLng(newLoc.getLat(), newLoc.getLon()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

    }


    //send directions request to google maps api
    private void sendDirectionRequest(){

        if (markerPoints.size() >= 2) { //check if we already have a start and end locations

            LatLng dest = (LatLng) markerPoints.get(1);

            LatLng waypoint = new LatLng(pointLat,pointLon);    //initialize the waypoint
            String url = getDirectionsUrl(origin, dest, waypoint);

            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);// Start downloading json data from Google Directions API

        }
    }

    //set up the url to request
    private String getDirectionsUrl(LatLng origin,LatLng dest, LatLng waypoint){

        String str_origin = "origin="+origin.latitude+","+origin.longitude;// Origin of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;// Destination of route
        String str_waypoints = "waypoints="+waypoint.latitude+","+waypoint.longitude;//waypoints
        String sensor = "sensor=false";// Sensor enabled
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+str_waypoints; // Building the parameters for the web service
        String output = "json";// Output format
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+"&alternatives=true&mode=walking";// Building the url for the web service
        return url;
    }



    // Download json data from url
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();// Creating an http connection to communicate with url
            urlConnection.connect();// Connecting to url
            iStream = urlConnection.getInputStream();// Reading data from url
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        }catch(Exception e){
            Log.d("Error downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {// Fetches data from url passed
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            String data = "";// For storing data from web service
            try{
                data = downloadUrl(url[0]);// Fetching the data from web service
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);// Invokes the thread for parsing the JSON data
        }
    }

    //Parse the Google Places in JSON format
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);// Starts parsing data
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        //Rerouting takes place here
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            if(polyline != null) {
                polyline.remove();
            }
            PolylineOptions lineOptions = new PolylineOptions();; //initialize polyline for route
            boolean isLocationOnPath = true;    //initializea variable to check if cluster on path

            for(int i=0;i<result.size();i++){ //iterate through the routes returned

                points = new ArrayList<LatLng>();
                List<HashMap<String, String>> path = result.get(i);// Fetching i-th route

                for(int j=0;j<path.size();j++){ // Fetching all the points in i-th route
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    if (twoTimes(points, position)) {   //check if it repeats a point more than twice
                        points.subList(points.indexOf(position) + 1, points.size()).clear();//if so, delete the next point
                    }
                    else {
                        points.add(position);

                    }

                }

                double tolerance = 10; // tolerance in meters for cluster detection
                isLocationOnPath=false;
                for (LatLng cluster : avoid){   //iterate through all clusters
                    //Log.d(" first cluster ", "lat: "+String.valueOf(cluster.latitude) +", lon: "+String.valueOf(cluster.longitude));
                    isLocationOnPath = PolyUtil.isLocationOnPath(cluster, points, true, tolerance); //check if cluster is on path
                    if(isLocationOnPath==true){//if cluster on path, don't use that path
                        points.clear();
                        break;
                    }
                }

                if(isLocationOnPath == false) {
                    lineOptions.addAll(points);//if cluster not on path, add all path points
                    lineOptions.width(8);
                    lineOptions.color(Color.BLACK);
                    polyline = map.addPolyline(lineOptions);
                    break;

                }
                //else{
                    points.clear();
                  /*  polyline = map.addPolyline(lineOptions);
                    if (lineOptions.getPoints().size() == 0) {  //if no points were added (cluster on path) start generating waypoints
                        counter++;
                        if(counter < 10){
                            pointLat += .000565;
                            pointLon -= -0.00054;
                        }

                        else if(counter == 10){
                            pointLat -= 0.01695;
                            pointLon += 0.0162;
                            pointLat -= .000565;
                            pointLon += -0.00054;
                        }
                        else {
                            pointLat -= .000565;
                            pointLon += -0.00054;
                        }
                        sendDirectionRequest(); //send request with new waypoints
                    }
                }*/

            }



            //once we looped through all returned paths-> continute to generate waypoints
            if(isLocationOnPath == true) {
                for (int i = 0; i < result.size(); i++) { // Traversing through all the routes

                    points = new ArrayList<LatLng>();
                    List<HashMap<String, String>> path = result.get(i);// Fetching i-th route
                    for (int j = 0; j < path.size(); j++) {// Fetching all the points in i-th route
                        HashMap<String, String> point = path.get(j);
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        if (twoTimes(points, position)) {
                            points.subList(points.indexOf(position) + 1, points.size()).clear();
                        }
                        else {
                            points.add(position);

                        }

                    }

                    double tolerance = 10; // cluster tolerance in meters
                     isLocationOnPath = false;
                    for (LatLng cluster : avoid) {
                        isLocationOnPath = PolyUtil.isLocationOnPath(cluster, points, true, tolerance);
                        if (isLocationOnPath == true) {
                            break;
                        }
                    }

                    if (isLocationOnPath == false) {
                        Log.d(" not in path ", "not in path");
                        // route = lineOptions;
                        lineOptions.addAll(points);
                        lineOptions.width(8);
                        lineOptions.color(Color.BLACK);
                        break;
                    }
                    points.clear();

                }
                polyline = map.addPolyline(lineOptions);
                if (lineOptions.getPoints().size() == 0) {  //if no points were added (cluster on path) start generating waypoints
                    counter++;
                    if(counter < 10){
                    pointLat += .000565;
                    pointLon -= -0.00054;
                    }

                    else if(counter == 10){
                        pointLat -= 0.01695;
                        pointLon += 0.0162;
                        pointLat -= .000565;
                        pointLon += -0.00054;
                    }
                    else {
                        pointLat -= .000565;
                        pointLon += -0.00054;
                    }
                    sendDirectionRequest(); //send request with new waypoints
                }
            }
        }
    }
    //check that path doesn't go through the same point twice
    public static boolean twoTimes(ArrayList<LatLng> list, LatLng position)
    {
        int numCount = 0;
        for (LatLng thisPosition : list) {
            if (thisPosition.equals(position)) numCount++;
        }

        return numCount >1;
    }
}

