package com.cc.cg.json;

import java.util.Collection;

public class JsonModel {

    public static class SimpleProject {
	public int id;
        public String name;
        public double latitude;
        public double longitude;
	public String address;
	public String postcode;
	public long timestamp;
	public String info;

	public String toString(){
	    StringBuffer sb = new StringBuffer();
	    sb.append("Id=");
	    sb.append(id);
	    sb.append(",Name=");
	    sb.append(name);
	    sb.append(",latitude=");
	    sb.append(latitude);
	    sb.append(",longitude=");
	    sb.append(longitude);
	    sb.append(",address=");
	    sb.append(address);
	    sb.append(",postcode=");
	    sb.append(postcode);
	    sb.append(",timestamp=");
	    sb.append(timestamp);
	    return sb.toString();
	}
    }

    public static class SimpleActivity {
        public int id;
        public String name;
	public long timestamp;

	public String toString(){
	    StringBuffer sb = new StringBuffer();
	    sb.append("Id=");
	    sb.append(id);
	    sb.append(",Name=");
	    sb.append(name);
	    sb.append(",timestamp=");
	    sb.append(timestamp);
	    return sb.toString();
	}
    }

    public static class SimplePlan {
        public int id;
        public String project;
	public String activity;
	public String start;
	public String stop;
	public String comment;
	public long timestamp;

	public String toString(){
	    StringBuffer sb = new StringBuffer();
	    sb.append("Id=");
	    sb.append(id);
	    sb.append(",Project=");
	    sb.append(project);
	    sb.append(",Activity=");
	    sb.append(activity);
	    sb.append(",Start=");
	    sb.append(start);
	    sb.append(",stop=");
	    sb.append(stop);
	    return sb.toString();
	}
    }

    public static class SimpleObject {
	public Collection<SimpleActivity> activities;
	public Collection<SimpleProject> projects;
	public Collection<SimplePlan> plans;
	public String message;
    }

    public static class SimpleUser {
        public int user;
	public SimpleUser(int user){
	    this.user = user;
	}
    }

    public static class Point{
	Point(double latitude, double longitude){
	    this.latitude = latitude;
	    this.longitude = longitude;
	}
	public double latitude;
	public double longitude;
    }

    public static class PositionReport {
	private int user;
	private String date;
	private double latitude;
	private double longitude;
	private int batteryLevel;
	private int project;
	private int activity;
	private boolean isVisible;

	public PositionReport(int user, String date,
			      double lat, double lon,
			      int batteryLevel, int project,
			      int activity, boolean isVisible){
	    this.user = user; 
	    this.date = date;
	    this.latitude = lat; 
	    this.longitude = lon;
	    this.batteryLevel = batteryLevel;
	    this.project = project; 
	    this.activity = activity;
	    this.isVisible = isVisible;
	}
	public String toString(){
	    StringBuffer sb = new StringBuffer("User=");
	    sb.append(user);
	    sb.append(",latitude=");
	    sb.append(latitude);
	    sb.append(",longitude=");
	    sb.append(latitude);
	    sb.append(",project=");
	    sb.append(project);
	    sb.append(",activity=");
	    sb.append(activity);
	    sb.append(",isVisible=");
	    sb.append(isVisible);
	    return sb.toString();
	}
    }

    public static class TimeReport {
	private int project;
	private int activity;
	private int user;
	private long start;
	private long stop;
	private boolean isAutomatic;

	public TimeReport(int project, int activity, int user,
			  long start, long stop, 
			  boolean isAutomatic){
	    this.project = project;
	    this.activity = activity;
	    this.user = user;
	    this.start = start;
	    this.stop = stop;
	    this.isAutomatic = isAutomatic;
	}
    }

    public static class Log {
	private int user;
	private String msg;

	public Log(int user, String msg){
	    this.user = user;
	    this.msg = msg;
	}
    }


    public static class ProjectPositionUpdate {
	private int project;
	private double latitude;
	private double longitude;

	public ProjectPositionUpdate(int project, double lat, double lon){
	    this.project = project;
	    this.latitude = lat;
	    this.longitude = lon;
	}
    }
}