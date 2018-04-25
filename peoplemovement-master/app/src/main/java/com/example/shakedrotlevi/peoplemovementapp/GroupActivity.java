package com.example.shakedrotlevi.peoplemovementapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shakedrotlevi on 2/13/18.
 */

public class GroupActivity extends AppCompatActivity {
    TextView name;
    TextView startName;
    TextView endName;
    TextView time;
    TextView description;
    Button join;
    Group groupEvent;
    String eventsID;
    String groupID;
    ArrayList<String> users = new ArrayList<String>();
    String user = "";


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(GroupActivity.this, MainActivity.class));
                    return true;
                case R.id.navigation_map:
                    startActivity(new Intent(GroupActivity.this, MapsActivity.class));
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        final String group = i.getStringExtra("group");
        setContentView(R.layout.activity_group);

        name = (TextView)findViewById(R.id.name);
        startName = (TextView)findViewById(R.id.startLoc);
        endName = (TextView)findViewById(R.id.endLoc);
        time = (TextView)findViewById(R.id.time);
        description = (TextView)findViewById(R.id.description);

        join = (Button)findViewById(R.id.join);
        //final Group groupEvent;

        Query query = FirebaseDatabase.getInstance().getReference().child("events").orderByChild("name").equalTo(group);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventsID = (String)dataSnapshot.getKey();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    groupID = (String)childDataSnapshot.getKey();
                    name.setText((String)childDataSnapshot.child("name").getValue());
                    startName.setText((String)childDataSnapshot.child("startName").getValue());
                    endName.setText((String)childDataSnapshot.child("endName").getValue());
                    time.setText((String)childDataSnapshot.child("time").getValue());


                    double startLat = (Double)childDataSnapshot.child("startLoc").child("lat").getValue();
                    double startLon = (Double)childDataSnapshot.child("startLoc").child("lon").getValue();

                    LocationObject startLoc = new LocationObject(startLat,startLon);

                    double endLat = (Double)childDataSnapshot.child("endLoc").child("lat").getValue();
                    double endLon = (Double)childDataSnapshot.child("endLoc").child("lon").getValue();

                    LocationObject endLoc = new LocationObject(endLat, endLon);
                    String status = (String)childDataSnapshot.child("status").getValue();
                    String time = (String)childDataSnapshot.child("time").getValue();
                    long hour = (long) childDataSnapshot.child("hour").getValue();
                    long minutes = (long) childDataSnapshot.child("minutes").getValue();
                    description.setText((String)childDataSnapshot.child("description").getValue());
                    ArrayList<String> temp  =(ArrayList<String>) childDataSnapshot.child("members").getValue();
                    users = (ArrayList<String>)temp.clone();
                    LocationObject tempLoc = new LocationObject(-33.87365, 151.20689);
                    groupEvent = new Group( (String)name.getText(), startLoc, endLoc, (String)startName.getText(), (String)endName.getText(), (String)description.getText(), user, tempLoc, users, status, time, hour,minutes);


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        join.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                user = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (user != null) {
                    // User is signed in
                    if(users.contains(user) == false) {
                        users.add(user);
                        FirebaseDatabase.getInstance().getReference().child(eventsID).child(groupID).child("members").setValue(users);

                    }


                } else {
                    // No user is signed in
                }
            }
        });


    }
}
