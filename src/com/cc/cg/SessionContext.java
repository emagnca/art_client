package com.cc.cg;

import android.location.Location;

// Fancy name for a class for a container of some global data.

public class SessionContext {

    // Singleton
    private static SessionContext _instance = new SessionContext();
    public static SessionContext instance() { return _instance; }
    private SessionContext(){}


    private String _message = "";
    private String _version = "";
    private String _apiKey = "";
    private int _batteryLevel = -1;
    private boolean _isLocationServiceUp = false;
    private long _lastPositionUpdate = System.currentTimeMillis();
    private int _lastProjectVisited = -1000;
    private Location _lastKnownLocation = null;

    public String getMessage() { return _message; }
    public void setMessage(String message){ _message = message; }

    public String getApiKey() { return _apiKey; }
    public void setApiKey(String apiKey){ _apiKey = apiKey; }

    public int getBatteryLevel() { return _batteryLevel; }
    public void setBatteryLevel(int i) { _batteryLevel = i; } 

    public void setLastPositionUpdate(){ _lastPositionUpdate = System.currentTimeMillis(); }
    public boolean isLocationServiceUp(){ return _isLocationServiceUp && 
	    System.currentTimeMillis() - _lastPositionUpdate <= Constants.GPS_UPDATE_INTERVAL * 2000; }

    public void isLocationServiceUp(boolean b){ _isLocationServiceUp = b; }
    
    public int getLastProjectVisited(){ return _lastProjectVisited; }
    public void setLastProjectVisited(int project){ _lastProjectVisited = project; }

    public Location getLastKnownLocation(){ return _lastKnownLocation; }    
    public void setLastKnownLocation(Location loc){ _lastKnownLocation = loc; }
}