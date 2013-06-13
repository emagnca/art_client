For instruction about the full project, including the server part, see http://www.autotid.nu (swedish)

To be able to compile the project and install the android client (from any Unix based OS)
=========================================================================================
1. In the android gui, install Google Inc.:Google APIs:17 or higher. Other versions might work, but ant.properties will have to be updated.
2. Update local.properties to point to your local android installation
3. adb debug install
4. Start ART, or restart the phone. Time reporting should start within a few minutes if location services are activated on the phone.

Short description of the code
=============================
There are 5 Android Activities that define the 4 different windows where the user can interact with the application. Those are:
MyTabActivity.java - That contains the next four activities in four different tabs
TimeActivity.java - With the start/stop button. Will show the time worked, and different options 
ProjectListActivity.java - Lists available projects and activities
MapView.java - Defines the Map with the projects
ReportActivity.java - Shows the time reports

The is one background service:
LocationService.java - Receives location updates from the network/gps

The is two BroadCast receivers:
PositionReporter.java - Started with a regular interval by the alarm service. It is responsible for updating the position to the server, and at the same time synchronize the projects and activities if necessary.
AutoStart.java - Listens to boot up and shutdown of the phone, and handles egistration of PositionReporter at the alarm service, and startup of the LocationService is done at boot. 

The database access is handled by OrmLite. The database related classes found in the database com/cc/cg/database. DbFacade.java contains some more complex interaction with the database. Simpler database interaction like creation/deletion/search is done directly with the OrmLite interface.

The communication of the server is done json-formatted objects. This is handled by classes in com/cc/cg/json. The google gson library is used. JsonModel.java defines the objects that are coded/decoded to/from json. JsonClient.java contains helper operations used by classes that need to communicate with the server.

Each call to the server is done in a separate thread. Classes the communicate with the server delagate the call to an inner class that implements the Runnable interface, so that it is possible to start it in a new thread. Threads are kept in a thread pool.



