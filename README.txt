To be able to compile the project (on any Unix based OS)
========================================================
1. In the android gui, install Google Inc.:Google APIs:17 or higher. Other versions might work, but ant.properties will have to be updated.
2. Update local.properties to point to your local android installation

Short description of the code
=============================
There are 3 Android Activities that define the 3 different windows where the user can interact with the application. Those are:
TimeActivity.java - With the start/stop button 
MapView.java - Defines the Map with the projects
ReportActivity.java - Shows the time reports

The is one background service:
LocationService.java - Receives location updates from the network/gps

The is one BroadCast receiver:
PositionReporter.java -  Started with a regular interval by the alarm service. It is responsible for updating the position to the server, and at the same time synchronize the projects and activities if necessary.

Registration of PositionReporter at the alarm service, and startup of the LocationService is done at boot. This is handled by:
AutoStart.java

The database access is handled by OrmLite. The database related classes found in the database com/cc/cg/database. DbFacade.java contains some more complex interaction with the database. Simpler database interaction like creation/deletion/search is done directly with the OrmLite interface.

The communication of the server is done json-formatted objects. This is handled by classes in com/cc/cg/json. The google gson library is used. JsonModel.java defines the objects that are coded/decoded to/from json. JsonClient.java contains helper operations used by classes that need to communicate with the server.

Each call to the server is done in a separate thread. Classes the communicate with the server delagate the call to an inner class that implements the Runnable interface, so that it is possible to start it in a new thread.   

