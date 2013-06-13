package com.cc.cg.database;

import java.text.*;
import java.util.*;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;


public class PositionReport {

    @DatabaseField(generatedId = true)
	private int id;
    @DatabaseField()
	private String timestamp;
    @DatabaseField()
	private String user;
    @DatabaseField()
	private double latitude;
    @DatabaseField()
	private double longitude;
    @DatabaseField()
	private int batteryLevel;
    @DatabaseField()
	private String project;
    @DatabaseField()
	private String activity;
    
    public PositionReport() {}
  
    public PositionReport(String timestamp, String user,
			  double latitude, double longitude, 
			  int batteryLevel, String project,
			  String activity){
	this.timestamp = timestamp;
	this.user = user;
	this.latitude = latitude;
	this.longitude = longitude;
	this.batteryLevel = batteryLevel;
	this.project = project;
	this.activity = activity;
    }

    public String getTimestamp() { return timestamp; }
    public String getUser() { return user; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getBatteryLevel() { return batteryLevel; }
    public String getProject() { return project; }
    public String getActivity() { return activity; }

    public String toString(){
	return user + ":" + timestamp;
    }
}
