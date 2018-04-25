README

Kate McNally
April 25, 2018
katemcnally@gwu.edu
People Movement

Firebase-Functions functions folder

index.js
This file holds all of the Cloud Functions and some supplementary funciton calls for the deployment of the functions to the Firebase cloud.
Functions include:
- locationImage
- cluster
- currentTime
- displayRoute
- endTrip
Supplementary functions
- cluster_creation
- median
- groupAssociation (commented out in current implementation, may need if groupAssociation moves back to backend requirement)

node_modules
This folder holds all of the node_modules needed for deployment of the cloud functions. Most of which are included with the running of "npm install". An additional node module, k8dbscan, is specified in the package.json file. It is the node module responsible for the dbscan clustering algorithm.

package.json
This file defines the functions directory and specifies which additional node modules must be included upon install (Firebase-admin, Firebase-functions, k8dbscan). 

package-lock.json
This file holds more dependencies and definitions for the functions directory. 

How to run it:
(running this code will expect that you have a firebase account already configured)
1. Import this firebase-functions folder to local device and connect with your Firebase account
2. Install npm to your local device
3. Run firebase deploy