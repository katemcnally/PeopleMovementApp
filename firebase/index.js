const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
var clustering = require('k8dbscan');

//FUNCTION USED TO TEST IF FIREBASE FUNCTIONS ARE WORKING PROPERLY 
exports.locationImage = functions.database //this is a function using the database
	.ref('/locations map/{location}')
	.onWrite((change, context) => {

	//location welcome
	const location = change.after.val()
	console.log(location)
	if(location.lat > 37 && location.lon > -78)
		//console.log("Welcome to dc!!! :)")
		location.welcome = "Welcome to DC!"
	else if(location.lat == 40.7128 && location.lon == -74.0060)
		location.welcome = "Welcome to New York City!"
	else
		location.welcome = ""

	console.log("Working")

	//return change.data.ref.child('welcome').set(location.welcome);
	const ref = change.after.ref.child('welcome');
	return ref.set(location.welcome);
	//return change.ref.parent.child('welcome').set(location.welcome);
})


/* cluster function 
1. Add all locations to array
2. Call DBSCAN method on array
3. Call centroid function on array of clusters
4. Return centroid and 4 outside points to array section of database 
Deploys when locations_map is written to
*/
exports.cluster = functions.database
	.ref('/locations map/')
	.onWrite((change, context) => {

	console.log("Beginning cluster function...");

	//add locations to array
	var locationsArray = [];
	var location_input = [];
	// look at each child of locations map and add it to array
	console.log(change.after.val());
	var locations_map = change.after.val();
	
	console.log("LOCATIONS MAP ");
	console.log(locations_map);


	//CREATE LOCATIONS_ARRAY
	for (var key in locations_map) {
    	if (locations_map.hasOwnProperty(key)) {
    		//key = location name
    		//value = set under location
    		var lat = locations_map[key].lat;
    		var lon = locations_map[key].lon;
    		var location1 = [lat, lon];
    		locationsArray.push(location1);
        	console.log(locations_map[key]);
    	}
	}
	//CHECK IF CREATION IS WORKING
	if(locationsArray.length > 75){
		console.log("locations Array looks like: " + locationsArray);
	}
	else{
		console.log("Locations Array not working")
	}

	//CREATE CLUSTERS from LOCATIONS_ARRAY
	clusters_array = cluster_creation(locationsArray);
	console.log("Clusters array [0][0] = " + clusters_array[0][0]);
	//console.log("Clusters array [1][0] = " + clusters_array[1][0]); // Note -- might have only one cluster

	//seeing if clusters array has clusters to be checked
	if(clusters_array.length > 0){
		console.log("Clusters array length: " + clusters_array.length)
	}
	else{
		console.log("Clusters array not working")
	}

	//CREATE ARRAY OF CENTROIDS
	var centroids_array = [];
	centroids_array = median(clusters_array);

	console.log(centroids_array);

	//CREATE ARRAY OF MINx, MAXx, MINy, MAXy

	//IF PERSON IN CLUSTER, ADD CLUSTER ID TO DATABASE ENTRY

	//ADD CENTROIDS TO DATABASE
	const ref = change.after.ref.parent.child('clusters').child('array');
	return ref.set(clusters_array);
	//return event.data.ref.parent.child('clusters').child('array').set(clusters_array);
	
	//loop through clusters and see if user is in cluster

	//if path includes cluster, re-route 
	//if(cluster.size >= 100)
	//	cluster.message = "Cluster is over 150 people"
		//deploy new function to reroute

	//return locationsArray;

})

/* currentTime function
1. set currentTime under events to the current time, this will constantly be changing to trigger events
*/

exports.currentTime = functions.database
	.ref('/events/')
	.onUpdate(event => {

	var date = new Date();
	var hours = (date.getHours() + 7) % 12;
	var minutes = "0" + date.getMinutes();
	var seconds = "0" + date.getSeconds();

	var formattedTime = hours + ':' + minutes.substr(-2) + ':' + seconds.substr(-2);

	//return event.data.ref.child('currentTime').set(formattedTime); 


})

/* displayRoute function 
1. Loop through events
2. If event startTime is now or past and status is INCOMPLETE, then status to ongoing
Deploy when events is updated 

*/

exports.displayRoute = functions.database
	.ref('/events/{userEvent}')
	.onUpdate(event => {

	console.log("Beginning displayRoute function...");

	const userEvent = event.data.val();

    //var date = new Date(serverTimestamp*1000);
    var date = new Date();
	// Hours part from the timestamp
	var hours = (date.getHours() + 7) % 12;
	// Minutes part from the timestamp
	var minutes = "0" + date.getMinutes();
	// Seconds part from the timestamp
	var seconds = "0" + date.getSeconds();

	// Will display time in 10:30:23 format
	var formattedTime = hours + ':' + minutes.substr(-2) + ':' + seconds.substr(-2);

    //console.log(current_time);
    console.log(formattedTime);

    if(hours == userEvent.hour && userEvent.status != "COMPLETE"){ //&& minutes >= userEvent.minutes && userEvent.status == "INCOMPLETE"){
		return event.data.ref.child('status').set("ONGOING");
    }
    else
    
    return event.data.ref.child('time').set(formattedTime);

    /*

	for (var key in events) {
    	if (events.hasOwnProperty(key)) {
    		//key = location name
    		//value = set under location
    		var startTime = events[key].time;
    		var status = events[key].status;

    		if(startTime >= current_time && status == INCOMPLETE){
    			return event.data.ref.child('status').set("ONGOING");
    		}
    	}
	}
	*/
})

/* endTrip function
1. changes trip to complete if the event location is equal to the end location set for the event
*/
exports.endTrip = functions.database
	.ref('/events/{userEvent}')
	.onUpdate(event => {

	console.log("Beginning endTrip function...");

	const userEvent = event.data.val();
	var groupLat = userEvent.groupLoc.lat;
	var groupLon = userEvent.groupLoc.lon;
	var endLat = userEvent.endLoc.lat;
	var endLon = userEvent.endLoc.lon;

    if(groupLat == endLat && groupLon == endLon){ //&& minutes >= userEvent.minutes && userEvent.status == "INCOMPLETE"){
		return event.data.ref.child('status').set("COMPLETE");
    }
    //else if
    
    //return event.data.ref.child('time').set(formattedTime);

    /*

	for (var key in events) {
    	if (events.hasOwnProperty(key)) {
    		//key = location name
    		//value = set under location
    		var startTime = events[key].time;
    		var status = events[key].status;

    		if(startTime >= current_time && status == INCOMPLETE){
    			return event.data.ref.child('status').set("ONGOING");
    		}
    	}
	}
	*/
})


/* groupAssociation function --- may change to APP side function !!!!!!!
1. Loop through users
2. If user has groupID that is ONGOING, change current_group_ID to groupID that is ONGOING
Deploy when events is updated 

exports.groupAssociation = functions.database
	.ref('/events/')
	.onUpdate(event => {

	console.log("Beginning groupAssociation function...");

	var users = event.data.adminRef.parent.child('users').val();

	for (var key in users) {
    	if (users.hasOwnProperty(key)) {
    		//key = location name
    		//value = set under location
    		var startTime = events[key].time;
    		var status = events[key].status;

    		if(startTime >= admin.database.ServerValue.TIMESTAMP && status == INCOMPLETE){
    			return event.data.ref.child('status').set("ONGOING");
    		}
    	}
	}

})

*/


function cluster_creation(locationsArray){

	//if points need not be in a cluster (i.e. person is in no cluster), do not add them to any cluster

	var dbscan = new clustering.DBSCAN();
	var clusters_array = dbscan.run(locationsArray, .001, 15);

	console.log("Noise: ");
	console.log(dbscan.noise);

	console.log("clusters[i].length = "+clusters_array[0].length);

	var clusters = [];

	//console.log(locationsArray[0]);

	
	for(var i = 0; i<clusters_array.length; i++){
		var array1 = [];
		for(var j = 0; j<clusters_array[i].length; j++){
			array1.push(locationsArray[clusters_array[i][j]]);
			// clusters_array[i].splice(j, 1, locationsArray[i,j]);
			// clusters.push(locationsArray[clusters_array[i][j]]);
			// clusters.push([i, locationsArray[clusters_array[i][j]]]);
		}
		//Array1 is the array of the cluster for clusters[i]
		console.log("Array1 = " + array1);
		//Median_val is the center point
		var median_val = median(array1);
		//cluster_size is the size of the cluster for cluster[i]
		//Padded with 0.1 to make it a double
		var cluster_size = array1.length + 0.1;
		//push size
		median_val.push(cluster_size);
		//push median
		clusters.push(median_val);
		
	}

	return clusters;
}

function median(values) {

    //lon first: add all lon - divide by size
    //lat second: add all lat - divide by size

    var center = [];

    var lon_sum = 0;
    var lat_sum = 0;

    console.log(values[0]);

    for (var i = 0; i<values.length; i++){
		lon_sum += values[i][0];
    	lat_sum += values[i][1];
    }

    console.log("lon sum = " + lon_sum);
    console.log("lat sum = " + lat_sum);

    lon_sum = lon_sum/values.length;
   	lat_sum = lat_sum/values.length;

   	center.push(lon_sum, lat_sum);

    return center;
}
