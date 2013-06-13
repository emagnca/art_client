To be able to compile the project for Android 2.2
=================================================
1. Install latest android
2. Start <ANDROID_INSTALL_DIR>/tools/android, where ANDROID_INSTALL_DIR is replaced by the path to the Android installation.
3. In the android GUI select to Install android 2.2 (api 8) -> Google APIs by Google Inc.
4, Create a file local.properties and add the line sdk.dir=<ANDROID_INSTALL_DIR>
5. Install ant (at least version 1.8.2)
6. Try to compile with "ant debug". If the target is not known do 7-8 else skip those steps.
7. Remove build.xml
8. Type android update project -t "Google Inc.:Google APIs:8" -p .


To create and start an emulator
===============================
android create avd -t "Google Inc.:Google APIs:8" -n avd1
ANDROID_INSTALL_DIR/tools/emulator -avd avd1

To install to phone or emulator
===============================
$ ant debug install


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

