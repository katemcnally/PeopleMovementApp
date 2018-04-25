package com.example.shakedrotlevi.peoplemovementapp;

import android.app.TimePickerDialog;
import java.util.Calendar;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.VideoView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shakedrotlevi on 12/8/17.
 */

/* have the following 5 fields:
Group name, event start time, event start location, event end location, event description"=
 */
public class CreateGroupActivity extends AppCompatActivity{
    private TextView mTextMessage;
    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    LocationObject startLoc, endLoc;
    String startName, endName;
    int pickedHour=0;
    int pickedMin = 0;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    startActivity(new Intent(CreateGroupActivity.this, MainActivity.class));
                    return true;
                case R.id.navigation_map:
                    Log.d("myTag", "Clicked Map");
                    startActivity(new Intent(CreateGroupActivity.this, MapsActivity.class));
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
                case R.id.navigation_join:
                    startActivity(new Intent(CreateGroupActivity.this, SearchActivity.class));
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_group);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        PlaceAutocompleteFragment autocompleteFragmentStart = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);


        PlaceAutocompleteFragment autocompleteFragmentEnd = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment1);

        autocompleteFragmentStart.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(" The Place: ", "Place: " + place.getName());
                startLoc= new LocationObject(place.getLatLng().latitude,place.getLatLng().longitude);
                startName = (String)place.getName();

            }

            @Override
            public void onError(Status status) {
                Log.i(" The Place: ", "An error occurred: " + status);
            }
        });
        autocompleteFragmentEnd.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(" The Place: ", "Place: " + place.getName());
                endLoc= new LocationObject(place.getLatLng().latitude,place.getLatLng().longitude);

                endName = (String)place.getName();
            }

            @Override
            public void onError(Status status) {
                Log.i(" The Place: ", "An error occurred: " + status);
            }
        });

        final EditText timePicker1 = (EditText) findViewById(R.id.editView2);

        timePicker1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateGroupActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        timePicker1.setText(( selectedHour + ":" + selectedMinute));
                        pickedHour = selectedHour;
                        pickedMin = selectedMinute;

                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });


    }
    public void onClickAdd(View button) {
        // Do click handling here
        final EditText nameField = (EditText) findViewById(R.id.editView1);
        String name = nameField.getText().toString();

        final EditText timeField = (EditText) findViewById(R.id.editView2);
        String time = timeField.getText().toString();

        final EditText descriptionField = (EditText) findViewById(R.id.editView5);
        String description = descriptionField.getText().toString();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ArrayList<String> members= new ArrayList<String>();
        members.add(user);
        LocationObject tempLoc = new LocationObject(-33.87365, 151.20689);
        Group group= new Group(name, startLoc, endLoc, startName, endName, description, user, tempLoc, members, "INCOMPLETE", time, pickedHour,pickedMin);

        DatabaseReference refEvents = database.getReference("events");
        refEvents.push().setValue(group);
    }
}
