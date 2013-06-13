package com.cc.cg;

import android.content.Context;
import android.content.SharedPreferences;

public class PersistentValues {

    private static final String PREFS_NAME = "TactPreferences1";
    private static final String PREF_PROJECT = "Project";
    private static final String PREF_ACTIVITY = "Activity";
    private static final String PREF_IS_REPORTING = "IsReporting";
    private static final String PREF_WORKED_TIME = "WorkedTime";
    private static final String PREF_TIMESTAMP = "Timestamp";
    private static final String PREF_LATITUDE = "Latitude";
    private static final String PREF_LONGITUDE = "Longitude";
    private static final String PREF_AUTOMATIC = "Automatic";
    private static final String PREF_VISIBLE = "Visible";
    private static final String PREF_CURRENT_TAB = "CurrTab";
    private static final String PREF_ACTIVITY_TIMESTAMP = "ActTimestamp";
    private static final String PREF_PROJECT_TIMESTAMP = "ProjTimestamp";
    private static final String PREF_POSITION_INTERVAL = "PositionInterval";
    private static final String PREF_SERVER = "Address";
    private static final String PREF_USER = "UserId";
    private static final String PREF_KEY = "ApiKey";
    private static final String PREF_MSG = "Message";
    private static final String PREF_NORMALSHUTDOWN = "NormalShutdown";


    private SharedPreferences _values;

    public PersistentValues(Context context){
	_values = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getCurrentProject(){
	return _values.getInt(PREF_PROJECT, -1);
    }

    public void setCurrentProject(int project){
	SharedPreferences.Editor editor = _values.edit();
	editor.putInt(PREF_PROJECT, project);
	editor.commit();
    }

    public int getCurrentActivity(){
	return _values.getInt(PREF_ACTIVITY, 1); // Default to first activity
    }

    public void setCurrentActivity(int activity){
	SharedPreferences.Editor editor = _values.edit();
	editor.putInt(PREF_ACTIVITY, activity);
	editor.commit();
    }

    /* ================================================== */


    public int getLastKnownLatitude(){
	return _values.getInt(PREF_LATITUDE, -1);
    }

    public int getLastKnownLongitude(){
	return _values.getInt(PREF_LONGITUDE, -1);
    }

    public void setLastKnownPosition(int latitude, int longitude){
	SharedPreferences.Editor editor = _values.edit();
	editor.putInt(PREF_LATITUDE, latitude);
	editor.putInt(PREF_LONGITUDE, longitude);
	editor.commit();
    }

    public boolean isReportingAutomatic(){
	return _values.getBoolean(PREF_AUTOMATIC, true);
    }

    public void isReportingAutomatic(boolean b){
	SharedPreferences.Editor editor = _values.edit();
	editor.putBoolean(PREF_AUTOMATIC, b);
	editor.commit();
    }

    public boolean isVisible(){
	return _values.getBoolean(PREF_VISIBLE, true);
    }

    public void isVisible(boolean b){
	SharedPreferences.Editor editor = _values.edit();
	editor.putBoolean(PREF_VISIBLE, b);
	editor.commit();
    }

    public String getCurrentTab(){
	return _values.getString(PREF_CURRENT_TAB, "");
    }

    public void setCurrentTab(String tab){
	SharedPreferences.Editor editor = _values.edit();
	editor.putString(PREF_CURRENT_TAB, tab);
	editor.commit();
    }

    public long getLastProjectTimestamp(){
	return _values.getLong(PREF_PROJECT_TIMESTAMP, -1);
    }

    public void setLastProjectTimestamp(long timestamp){
	SharedPreferences.Editor editor = _values.edit();
	editor.putLong(PREF_PROJECT_TIMESTAMP, timestamp);
	editor.commit();
    }

    public long getLastActivityTimestamp(){
	return _values.getLong(PREF_ACTIVITY_TIMESTAMP, -1);
    }

    public void setLastActivityTimestamp(long timestamp){
	SharedPreferences.Editor editor = _values.edit();
	editor.putLong(PREF_ACTIVITY_TIMESTAMP, timestamp);
	editor.commit();
    }

    public int getUser(){
	return _values.getInt(PREF_USER, Constants.DEFAULT_USER);
    }

    public void setUser(int user){
	SharedPreferences.Editor editor = _values.edit();
	editor.putInt(PREF_USER, user);
	editor.commit();
    }

    public String getApiKey(){
	return _values.getString(PREF_KEY, "");
    }

    public void setApiKey(String key){
	SharedPreferences.Editor editor = _values.edit();
	editor.putString(PREF_KEY, key);
	editor.commit();
    }

    public Integer getPositionInterval(){
	return _values.getInt(PREF_POSITION_INTERVAL, 30);
    }

    public void setPositionInterval(int interval){
	SharedPreferences.Editor editor = _values.edit();
	editor.putInt(PREF_POSITION_INTERVAL, interval);
	editor.commit();
    }

    public String getServer(){
	return _values.getString(PREF_SERVER, Constants.DEFAULT_SERVER);
    }

    public void setServer(String server){
	SharedPreferences.Editor editor = _values.edit();
	editor.putString(PREF_SERVER, server);
	editor.commit();
    }

    public boolean isNormalShutdown(){
	return _values.getBoolean(PREF_NORMALSHUTDOWN, true);
    }

    public void isNormalShutdown(boolean b){
	SharedPreferences.Editor editor = _values.edit();
	editor.putBoolean(PREF_NORMALSHUTDOWN, b);
	editor.commit();
    }

 
}