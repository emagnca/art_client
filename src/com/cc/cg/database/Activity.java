package com.cc.cg.database;

import com.j256.ormlite.field.DatabaseField;

public class Activity {

    public static final String ID_FIELD_NAME = "id";
    public static final String NAME_FIELD_NAME = "name";
    public static final String IS_ACTIVE_FIELD_NAME = "active";
    public static final String TIMESTAMP_FIELD_NAME = "timestamp";

    @DatabaseField(id = true, columnName = ID_FIELD_NAME)
	int id;
    @DatabaseField(index = true, columnName = NAME_FIELD_NAME)
	String name;
    @DatabaseField(defaultValue = "True", columnName = IS_ACTIVE_FIELD_NAME)
	boolean isActive;    
    @DatabaseField(defaultValue = "0", columnName = TIMESTAMP_FIELD_NAME)
	long timestamp;    
    
    Activity() {}
    
    public Activity(int id, String name, long timestamp) {
	this.id = id;
	this.name = name;
	this.timestamp = timestamp;
	this.isActive = true;
    }
    
    public int getId(){ return id; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public long getTimestamp(){ return timestamp; }
    public void setTimestamp(long timestamp){ this.timestamp = timestamp; }
    public void isActive(boolean isActive) { this.isActive = isActive; }
    public boolean isActive() { return isActive; }

    public String toFullString() {
	StringBuilder sb = new StringBuilder();
	sb.append("id=").append(id);
	sb.append(", ").append("name=").append(name);
	sb.append(", ").append("timestamp=").append(timestamp);
	sb.append(", ").append("isActive=").append(isActive);
	return sb.toString();
    }

    public String toString(){
	return name;
    }

}
