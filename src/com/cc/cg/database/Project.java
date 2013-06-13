package com.cc.cg.database;

import java.util.Date;
import com.j256.ormlite.field.DatabaseField;


public class Project {

    public static final String ID_FIELD_NAME = "id";
    public static final String NAME_FIELD_NAME = "name";
    public static final String ADDRESS_FIELD_NAME = "address";
    public static final String POSTCODE_FIELD_NAME = "postcode";
    public static final String LATITUDE_FIELD_NAME = "latitude";
    public static final String LONGITUDE_FIELD_NAME = "longitude";
    public static final String INFO_FIELD_NAME = "info";
    public static final String TIMESTAMP_FIELD_NAME = "timestamp";
    public static final String IS_ACTIVE_FIELD_NAME = "active";

    @DatabaseField(id = true, columnName = ID_FIELD_NAME)
	int id;
    @DatabaseField(index = true, columnName = NAME_FIELD_NAME)
	String name;
    @DatabaseField(defaultValue = "", columnName = ADDRESS_FIELD_NAME)
	String address;
    @DatabaseField(defaultValue = "", columnName = POSTCODE_FIELD_NAME)
	String postcode;
    @DatabaseField(defaultValue = "0", columnName = LATITUDE_FIELD_NAME)
	double latitude;
    @DatabaseField(defaultValue = "0", columnName = LONGITUDE_FIELD_NAME)
	double longitude;
    @DatabaseField(defaultValue = "", columnName = INFO_FIELD_NAME)
	String info;
    @DatabaseField(defaultValue = "0", columnName = TIMESTAMP_FIELD_NAME)
	long timestamp;    
    @DatabaseField(defaultValue = "true", columnName = IS_ACTIVE_FIELD_NAME)
	boolean isActive;    
    
    Project() {}
    
    public Project(int id, String name) {
	this.id = id;
	this.name = name;
	this.isActive = true;
    }
    
    public Project(int id, String name, String address, String postcode,
		   double latitude, double longitude, String info, long timestamp) {
	this.id = id;
	this.name = name;
	this.address = address;
	this.postcode = postcode;
	this.latitude = latitude;
	this.longitude = longitude;
	this.info = info;
	this.timestamp = timestamp;
	this.isActive = true;
    }
    
    public int getId(){ return id; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getAddress(){ return address; }
    public void setAddress(String address){ this.address = address; }
    public String getPostCode(){ return postcode; }
    public void setPostCode(String postcode){ this.postcode = postcode; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLatitude() { return latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getLongitude() { return longitude; }
    public String getInfo(){ return info; }
    public void setInfo(String info){ this.info = info; }
    public long getTimestamp(){ return timestamp; }
    public void setTimestamp(long timestamp){ this.timestamp = timestamp; }
    public void isActive(boolean isActive) { this.isActive = isActive; }
    public boolean isActive() { return isActive; }
    
    public String toFullString() {
	StringBuilder sb = new StringBuilder();
	sb.append("id=").append(id);
	sb.append(", ").append("name=").append(name);
	sb.append(", ").append("address=").append(address);
	sb.append(", ").append("postcode=").append(postcode);
	sb.append(", ").append("latitude=").append(latitude);
	sb.append(", ").append("longitude=").append(longitude);
	sb.append(", ").append("info=").append(info);
	sb.append(", ").append("timestamp=").append(timestamp);
	sb.append(", ").append("isActive=").append(isActive);
	return sb.toString();
    }

    public String toString() {
	return name;
    }
}
