const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
var clusterfck = require("clusterfck");
var clusterMaker = require('clusters');

/*exports.locationImage = functions.database //this is a function using the database
	.ref('/locations map/{location}')
	.onWrite(event => {

	//location welcome
	const location = event.data.val()
	if(location.lat > 37 && location.lon > -78)
		location.welcome = "Welcome to DC!"
	else if(location.lat == 40.7128 && location.lon == -74.0060)
		location.welcome = "Welcome to New York City!"
	else
		location.welcome = ""

	console.log("Working")

	return event.data.ref.child('welcome').set(location.welcome);
})*/

exports.cluster = functions.database
	.ref('/locations map/')
	.onWrite(event => {

	console.log("Made it here");

	//add locations to array
	var locationsArray = [];
	var location_input = [];
	// look at each child of locations map and add it to array
	var locations_map = event.data.val();
	
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
	console.log(clusters_array);

	//seeing if clusters array has clusters to be checked
	if(clusters_array.length > 0){
		console.log("Clusters array length: " + clusters_array.length)
	}
	else{
		console.log("Clusters Array not working")
	}

	//CREATE ARRAY OF CENTROIDS
	var centroids_array = [];
	centroids_array = set_centroid(clusters_array);

	//ADD CENTROIDS TO DATABASE
	return event.data.adminRef.parent.child('clusters').child('array').set(centroids_array);

	//loop through clusters and see if user is in cluster

	//if path includes cluster, re-route 
	//if(cluster.size >= 100)
	//	cluster.message = "Cluster is over 150 people"
		//deploy new function to reroute

	//return locationsArray;

})

function cluster_creation(locationsArray){

	//better format locationsArray
	//console.log("locations array = " + locationsArray);

	clusterMaker.k(4);
	clusterMaker.data(locationsArray);
	clusters_array = clusterMaker.clusters();
	//console.log(cluster_array);
	//var clusters_array = clusterfck.kmeans(locationsArray, 5); //creates array of arrays of clusters
	//var centroids = kmeans.centroids;

	return clusters_array;
}

function set_centroid(clusters_array){

	//LOOP AND SET EACH CENTROID
	//clusters_array = clusters_array[0];
	var centroids = [];
	for (var key in clusters_array) {
    	if (clusters_array.hasOwnProperty(key)) {
    		//key = n/a
    		//value = entire cluster
    		var location1 = clusters_array[key].centroid;
    		centroids.push(location1);
    	}
	}
	console.log("CENTROID ARRAY");
	console.log(centroids);
	return centroids;
}
