package com.cc.cg;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;

import java.util.*;
import java.sql.SQLException;


import com.j256.ormlite.android.apptools.OrmLiteBaseService;
import com.cc.cg.database.*;

/**
 * Class that implements the service that handles location updates. It subscribes to periodic updates rather than
 * using LocationManager.AddProximityAlert to have better control. The principle is to use network location and
 * only turn on the gps if the network location isn't accurate enough; to save battery.
 */
public class LocationService extends OrmLiteBaseService<DatabaseHelper> implements EventDispatcher.ProjectListener {
        
    private final static String TAG = "LocationService";

    private LocationManager _locationManager;
    private MyLocationListener _listenerGps, _listenerNetwork; 
    private PersistentValues _pv;
    private DbFacade _db;
    private RemoteLog _remoteLog;
    private List<Project> _projects;

    /**
     * The service onCreate lifecycle method. Inherited from android.app.Service.
     *
     * @param bundle    state possibly saved from onSaveInstance method
     */
    public void onCreate() {
	Log.i(TAG, "onCreate Thread=" + Thread.currentThread().getId());
	super.onCreate();
	Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(this));
	_remoteLog = new RemoteLog(this);
	_pv = new PersistentValues(this);
	_db = new DbFacade(this);
	_locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	loadProjects();
	EventDispatcher.instance().registerProjectListener(this);
	// Somebody shall set the api key for the session. For debugging it is 
	// better here that in AutoStart, that would otherwise be a natural.
	//com.cc.cg.json.JsonClient.instance().initKeyStore(this);
	SessionContext.instance().setApiKey(_pv.getApiKey()); 
    }

    /**
     * The service onDestroy lifecycle method. Inherited from android.app.Service.
     */
    public void onDestroy() {
	Log.d(TAG, "onDestroy");
	SessionContext.instance().isLocationServiceUp(false);
	EventDispatcher.instance().unregisterProjectListener(this);
	_locationManager.removeUpdates(_listenerGps);
	_locationManager.removeUpdates(_listenerNetwork);
	super.onDestroy();
    }


    /**
     * Interited from android.app.Service. Called when the service is started.
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
	_remoteLog.i(TAG, "LocationService starting up");

	List<String> providers = _locationManager.getAllProviders();
	for(String s : providers){
	    Log.d(TAG, "provider=" + s);
	}
	//_locationManager.addGpsStatusListener(new MyGpsListener());
	checkGps();

	Log.d(TAG, "=================Known projects============= no=" + _projects.size());
	for(Project project : _projects)
	    Log.d(TAG, project.getName() + ":" + project.getLatitude() + project.getLongitude());

	if(_locationManager != null){
	    SessionContext.instance().isLocationServiceUp(true);
	    _listenerGps = new MyLocationListener();
	    
	    _listenerNetwork = new MyLocationListener();
	    _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.NETWORK_UPDATE_INTERVAL*1000, 
	    					    0, _listenerNetwork);
	    
	    _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.GPS_UPDATE_INTERVAL*1000, 
	    				    0, _listenerGps);
	    Log.d(TAG, "!Listeners started!");
	    _listenerGps.isRequested(true);
	    _listenerNetwork.isRequested(true);
	}

	return super.onStartCommand(intent, flags, startId);
    }
    

    /**
     * Inherited from android.app.Service. Not useed since no interface is published with aidl.
     */
    public IBinder onBind(Intent arg0) {
	return null;
    }

    /**
     * Loads projects from database into memory
     */
    private void loadProjects(){
	try{
	    //_projects =  getHelper().getProjectDao().queryForAll(); 

	    _projects = 
		getHelper().getProjectDao().queryBuilder().where().eq(Project.IS_ACTIVE_FIELD_NAME, 
								      true).query();
	}
	catch(SQLException x){
	    Log.e(TAG, x.getMessage(), x);
	}
    }

    /**
     * Implementation of the EventDispatcher.ProjectListener interface.
     */
    public void projectsModified(){
	Log.i(TAG, "Got message that projects modified");
	loadProjects();
    }

    /**
     * Class that registers for updates from the Android Location service.
     * Implements android.location.LocationListener and adds
     * indicator if it's actively listening for updates or not.
     */
    private class MyLocationListener implements LocationListener{
	boolean _isRequested = false;

	public void onLocationChanged(Location location){
	    Log.i(TAG, "New location from " + location.getProvider() + 
		  ".Thread=" + Thread.currentThread().getId());
	    handleNewPosition(location);
	    SessionContext.instance().setLastPositionUpdate();
	}
	public void onProviderEnabled(String provider) {
	    Log.d(TAG, provider + " is enabled");
	    if(provider == LocationManager.NETWORK_PROVIDER && !_listenerGps.isRequested()){
		_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.GPS_UPDATE_INTERVAL*1000, 
							0, _listenerGps);
		_listenerGps.isRequested(true);
	    }
	    else if(provider == LocationManager.GPS_PROVIDER && !_listenerNetwork.isRequested()){
		_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.NETWORK_UPDATE_INTERVAL*1000, 
							0, _listenerNetwork);
		_listenerNetwork.isRequested(true);
	    }
	}

	public void onProviderDisabled(String provider) {
	    Log.d(TAG, provider + " is disabled");
	}
	public void onStatusChanged(String provider, int status, Bundle extras) {
	    Log.d(TAG, provider + " new status=" + status);
	}
	private void isRequested(boolean b){ _isRequested = b; }
	private boolean isRequested(){ return _isRequested; }
    };
    

    /**
     * Contains the actual logic for how to handle a new location received from the system.
     * @param location  - a new location 
     */
    public void handleNewPosition(Location location){
	double distance = Constants.MAX_DISTANCE + 1;
	int closestProject = -1;
	String name = "";
	
	boolean isBetterLocation = isBetterLocation(location);
	Log.d(TAG, location.getProvider() + " - new location latitude=" + location.getLatitude() +
	      " longitude=" + location.getLongitude() +
	      " accuracy: " + location.getAccuracy() + " meters" + 
	      " isBetter: " + isBetterLocation);
	
	if(isBetterLocation){
	    Log.i(TAG, "Yes, got a better location from " + location.getProvider() + 
		  ":lat=" + location.getLatitude() + ",lon=" + location.getLongitude());
	    SessionContext.instance().setLastKnownLocation(location);
	    _pv.setLastKnownPosition((int)(location.getLatitude() * 1000000),
				     (int)(location.getLongitude() * 1000000));

	    // 1. Find the closest project
	    float result[] = new float[1];
	    for (Project project : _projects) {
		if(project.getLatitude() > 0){
		    Location.distanceBetween(location.getLatitude(), location.getLongitude(), 
					     project.getLatitude(), project.getLongitude(),
					     result);
		    Log.i(TAG, "Distance from project: " + project.getName() + " is " + result[0] + " meters. lat=" + 
			  + project.getLatitude() + ", lon=" + project.getLongitude()); 
		    if(result[0] <= Constants.MAX_DISTANCE && result[0] < distance){
			closestProject = project.getId();
			distance = result[0];
			name = project.getName();
		    }
		}
	    }
	    
	    // 2. If the closest project is another than last time we came here, actions shall be taken
	    int lastId = SessionContext.instance().getLastProjectVisited();
	    Log.d(TAG, "Closest project=" + closestProject + " lastid=" + lastId);
	    if(closestProject != -1 && lastId != closestProject){
		Log.d(TAG, "Closest project is: " + closestProject + " at " + distance + " meters");
		SessionContext.instance().setLastProjectVisited(closestProject);
		_pv.setCurrentProject((int)closestProject);
		sendNotification(true, closestProject, name);
		EventDispatcher.instance().signalMovedIntoProjectArea(closestProject);
		if(_pv.isReportingAutomatic())
		    _db.registerWorkStart(closestProject, _pv.getCurrentActivity(), true);
	    }
	    else if(closestProject == -1 && lastId != -1) {
		SessionContext.instance().setLastProjectVisited(-1);
		Log.d(TAG, "Setting isreporting=false");
		_pv.setCurrentProject(-1);
		sendNotification(false, lastId, name);
		EventDispatcher.instance().signalMovedOutOfProjectArea();
		if(_pv.isReportingAutomatic())
		    _db.registerWorkStop(true);
	    }
	    else{
		Location lastLocation = SessionContext.instance().getLastKnownLocation();
		Location.distanceBetween(location.getLatitude(), 
					 location.getLongitude(), 
					 lastLocation.getLatitude(), 
					 lastLocation.getLongitude(),
					 result);
		if(result[0] > 50)
		    EventDispatcher.instance().signalNewPosition();
	    }
	}
	
	checkProviders(location);
    }

    /**
     * Sends a notification about project.
     * @param isInArea  - true if the phone is within a project area
     * @param id        - the project's id
     * @param name      - the project's name 
     */
    private void sendNotification(boolean isInArea, long id, String name){

	Log.d(TAG, "Sending notification");

	NotificationManager notificationManager =
	    (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
	Notification myNotification = new Notification(isInArea? R.drawable.on:R.drawable.off,
						       "Art meddelande!",
						       System.currentTimeMillis());

	Context context = getApplicationContext();
	String notificationTitle = isInArea? "In i projekt" : "Ut ur projekt";
	String notificationText = name;
	if(isInArea) notificationText += " är nu aktivt. \nLogga in..."; 
	    else notificationText += " är nu inaktivt. \nLogga ut...";

	PendingIntent pendingIntent
		    = PendingIntent.getBroadcast(getBaseContext(),
						 0, new Intent("com.cc.cg.action.move"),
						 PendingIntent.FLAG_UPDATE_CURRENT);
	myNotification.defaults |= Notification.DEFAULT_SOUND;
	myNotification.defaults |= Notification.DEFAULT_VIBRATE;
	myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
	myNotification.setLatestEventInfo(context,
					  "Art meddelar...",
					  notificationText,
					  pendingIntent);
	notificationManager.notify(1, myNotification);
    }


    /**
     * Checks for a new network location if it is good enough not to have to use the gps.
     * @param loation  - a new location
     */
    private void checkProviders(Location location){
	Log.d(TAG, "In checkProviders provider: " + location.getProvider());
	if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
	    Log.d(TAG, "Network provider accuracy: " + location.getAccuracy() +
		  " isGpsRequested: " + _listenerGps.isRequested() +
		  " min nw accuracy: " + Constants.MIN_NETWORK_ACCURACY);
	    if(location.getAccuracy() < Constants.MIN_NETWORK_ACCURACY && _listenerGps.isRequested()){
		Log.d(TAG, "Turning off gps");
		_locationManager.removeUpdates(_listenerGps);
		_listenerGps.isRequested(false);
	    }
	    else if(location.getAccuracy() >= Constants.MIN_NETWORK_ACCURACY && !_listenerGps.isRequested()){
		Log.d(TAG, "Turning on gps");
		_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
							Constants.GPS_UPDATE_INTERVAL*1000, 
							0, 
							_listenerGps);
		_listenerGps.isRequested(true);
	    }
	}
    }
    
    /**
     * Checks if a new location received from the system is better than the previous best location known.
     * @param   newLocation  - a new location
     * @returns true if the new location shall be taken into consideration
     */
    private boolean isBetterLocation(Location newLocation) {
	
	Log.d(TAG, "Is better location?" + newLocation);
	Location lastKnownLocation = SessionContext.instance().getLastKnownLocation();
	if (lastKnownLocation == null) 
	    return true;

	float result[] = new float[1];       
	Location.distanceBetween(newLocation.getLatitude(), newLocation.getLongitude(), 
				 lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
				 result);
	float metersFromOldLocation = result.length > 0? result[0]:0;

	Log.d(TAG, "Meters from last location = " + metersFromOldLocation);

	long timeDelta = newLocation.getTime() - lastKnownLocation.getTime();
	int maxUpdateInterval = Constants.GPS_UPDATE_INTERVAL;
	if (Constants.NETWORK_UPDATE_INTERVAL > maxUpdateInterval) 
	    maxUpdateInterval = Constants.NETWORK_UPDATE_INTERVAL;
	boolean isClearlyNewer = timeDelta > (maxUpdateInterval + 120);
	boolean isClearlyOlder = timeDelta < -120000;
	boolean isNewer = timeDelta > 0;

	Log.d(TAG, "IsClearlyNewer=" + isClearlyNewer + ",isClearlyOlder=" + isClearlyOlder);
	if(isClearlyNewer && newLocation.getAccuracy() <= 100) return true;
	else if (isClearlyOlder) return false;
	
	int accuracyDelta = (int) (newLocation.getAccuracy() - lastKnownLocation.getAccuracy());
	boolean isLessAccurate = accuracyDelta > 0;
	boolean isMoreAccurate = accuracyDelta < 0;
	boolean isClearlyLessAccurate = accuracyDelta > 200;

	boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
						    lastKnownLocation.getProvider());

	Log.d(TAG, "IsMoreAccurate=" + isMoreAccurate + 
	      ",metersFromOldLocation=" + metersFromOldLocation +
	      ",maxDistance=" + Constants.MAX_DISTANCE + 
	      ",newAccuracy=" + newLocation.getAccuracy() +
	      ",isNewer=" + isNewer + ",isLessAcurate=" + isLessAccurate);
	
	return isMoreAccurate 
	    || (metersFromOldLocation >= Constants.MAX_DISTANCE && 
		newLocation.getAccuracy() <= Constants.MAX_DISTANCE) 
	    || (metersFromOldLocation >= Constants.MAX_DISTANCE*4 && // to cover for rapid movement 
		metersFromOldLocation >= newLocation.getAccuracy())
	    || (isNewer && !isLessAccurate);
    }

    /**
     * Answers if two providers are the same.
     */
    private boolean isSameProvider(String provider1, String provider2) {
	if (provider1 == null) {
	    return provider2 == null;
	}
	return provider1.equals(provider2);
    }

    /**
     * Listens to modifications in the GPS subscription.
     */ 
    private class MyGpsListener implements GpsStatus.Listener {
	public void onGpsStatusChanged(int event) {
	    switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
		Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS");
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
		Log.d(TAG, "GPS_EVENT_FIRST_FIX");
                break;
            case GpsStatus.GPS_EVENT_STARTED:
		Log.d(TAG, "GPS_EVENT_STARTED");
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
		Log.d(TAG, "GPS_EVENT_STOPPED");
                break;
	    }
	}
    }

    /**
     * Checks how many satellites that are used by GPS.
     */
    private int checkGps(){
	Log.d(TAG, "checkGps");
	int numberOfSatellites = 0;
	GpsStatus gpsStatus = _locationManager.getGpsStatus(null);
	Iterator<GpsSatellite> itr = gpsStatus.getSatellites().iterator();
	while(itr.hasNext()){
	    numberOfSatellites++;
	    GpsSatellite satellite = itr.next();
	    Log.d(TAG, satellite.getPrn() + " was used in fix: " + satellite.usedInFix());
	}
	return numberOfSatellites;
    }

}
        
