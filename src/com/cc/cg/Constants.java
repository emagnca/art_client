package com.cc.cg;
public class Constants {

    public static final String ADMIN_PHONE_NUMBER = "0735304450";

    public static final String PARAM_NAME = "name";
    public static final String PARAM_TIME = "time";
    public static final String PARAM_LATITUDE = "latitude";
    public static final String PARAM_LONGITUDE = "longitude";
    public static final String PARAM_BATTERY_LEVEL = "battery";
    public static final String PARAM_PROJECT = "project";
    public static final String PARAM_ACTIVITY = "activity";
    
    public static final int DATA_MODIFIED = android.app.Activity.RESULT_FIRST_USER;

    public static final int MAX_DISTANCE = 250;
    public static final int POSITION_REPORT_INTERVAL = 300;
    public static final int REOPEN_DELTA = 15 * 60;
    public static final int NETWORK_UPDATE_INTERVAL = 180; 
    public static final int GPS_UPDATE_INTERVAL = 300;
    public static final int MIN_NETWORK_ACCURACY = 100;
    
    public static final int MAX_TIMEREPORT_DAYS = 2;
    public static final String DEFAULT_SERVER = "xyz.autotid.nu/art/v1/";
    public static final int DEFAULT_USER = 12345;

    public static final int SIMPLE_PROJECT_TYPE = 0;
    public static final int SIMPLE_ACTIVITY_TYPE = 1;
    public static final int SIMPLE_SERVER_TYPE = 2;

    public static final String RESPONSE_OK = "OK";
    public static final String RESPONSE_KO = "KO";

    public static final String PHONE_OFF_MSG = "PHONE_TURNED_OFF";
    public static final String DUMP_LOG_MSG = "__DUMP_LOG__";
}