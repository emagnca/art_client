package com.cc.cg;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Calendar;

import com.cc.cg.database.DbFacade;

/**
 * Starts up different services at startup.
 */
public class AutoStart extends BroadcastReceiver {
 
    private final static String TAG = "AutoStart";

    private final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";
    private final String SHUTDOWN_ACTION = "android.intent.action.ACTION_SHUTDOWN";

    /**
     * Abstract method of BroadcastReceiver. Starts up the services at boot and
     * stops the time reporting when power is turned off.
     */
     public void onReceive(Context context, Intent intent) {
	 RemoteLog remoteLog = new RemoteLog(context); 
	 PersistentValues pv = new PersistentValues(context);
	 Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(context));
	 if(intent.getAction().equals(BOOT_COMPLETED_ACTION)){
	     Log.i(TAG, "Boot complete. Thread=" + Thread.currentThread().getId());
	     remoteLog.i(TAG, "BOOT COMPLETE" );
	     startLocationService(context);
	     startBatteryService(context);
	     startPositionReporter(context);
	     if(!pv.isNormalShutdown()){
		 android.telephony.SmsManager sm = android.telephony.SmsManager.getDefault();
		 String msg = "Art kan ha kraschat. Uid=" + pv.getUser();
		 sm.sendTextMessage(Constants.ADMIN_PHONE_NUMBER, null, msg, null, null);
		 remoteLog.dumpLog();
	     }
	     pv.isNormalShutdown(false);
	 }
	 else if(intent.getAction().equals(SHUTDOWN_ACTION)){
	     Log.i(TAG, "Shutdown received. Thread=" + Thread.currentThread().getId());
	     remoteLog.i(TAG, Constants.PHONE_OFF_MSG);
	     Intent stopIntent = new Intent(context, com.cc.cg.LocationService.class);
	     context.stopService(stopIntent);
	     stopIntent = new Intent(context, com.cc.cg.BatteryLoggerService.class);
	     context.stopService(stopIntent);
	     DbFacade db = new DbFacade(context);
	     db.registerWorkStop(true);
	     pv.isNormalShutdown(true);
	 }
	 else
	     Log.d(TAG, "Received unexpected intent: " + intent.getAction());	
     }

    /**
     * Starts up the locationservice
     */
    public static void startLocationService(final Context context){
	Log.d(TAG, "startLocationService");
	new Thread(){ 
	    public void run(){
		Intent intent = new Intent(context, com.cc.cg.LocationService.class);
		context.startService(intent);
	    }
	}.start();
    }

    /**
     * Starts up the battery sevice
     */
    public static void startBatteryService(Context context){
	Log.d(TAG, "startBatteryService");
	     Intent intent = new Intent(context, com.cc.cg.BatteryLoggerService.class);
	     context.startService(intent);
    }

    /**
     * Registers PositionReporter in the Android alarm service so that it is executed
     * at regular intervals.
     */
    public static void startPositionReporter(Context context){
	Log.d(TAG, "startPositionReporter");
	Intent intent = new Intent(context, PositionReporter.class);
	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
	AlarmManager alarmManager = (AlarmManager)(context.getSystemService(Context.ALARM_SERVICE));
	Calendar calendar = Calendar.getInstance();
	calendar.setTimeInMillis(System.currentTimeMillis());
	long interval = Constants.POSITION_REPORT_INTERVAL * 1000; 
	Log.d(TAG, "Setting position interval to: " + interval);
	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, pendingIntent);
    }
}
