package com.cc.cg;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

/**
 * Service that listens to event sent out by Android when the battery status
 * has changed. The battery level is saved in the SessionContext so that
 * it can be accessed from elsewhere.
 */
public class BatteryLoggerService extends Service {
 
    private final static String TAG = "!!! BatteryLoggerService !!!";

    /**
     * Anonymous inner class that receives the battery changed broadcasts from Android.
     */
    private BroadcastReceiver myReceiver = new BroadcastReceiver(){
	    public void onReceive(Context context, Intent intent){
		String action = intent.getAction();
		if(action.equals(Intent.ACTION_BATTERY_CHANGED)){
			int batteryLevel = intent.getIntExtra("level", 0);
			SessionContext.instance().setBatteryLevel(batteryLevel);
			int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
			String batteryStatus;
			if(status == BatteryManager.BATTERY_STATUS_CHARGING){
			    batteryStatus = "Charging"; 
			} else if(status == BatteryManager.BATTERY_STATUS_DISCHARGING){
			    batteryStatus = "Dis-charging";
			} else if(status == BatteryManager.BATTERY_STATUS_NOT_CHARGING){
			    batteryStatus = "Not charging";
			} else if(status == BatteryManager.BATTERY_STATUS_FULL){
			    batteryStatus = "Full";
			} else {
			    batteryStatus = "";
			}
         
			Log.w(TAG, "Battery level: " + batteryLevel + "%, status: " + batteryStatus); 
		}
	    }
	};
      
    /**
     * Inherited from Service, but not used since this Service isn't published externally.
     */
    public IBinder onBind(Intent arg0){
	return null;
    }

    /**
     * Inherited from Service.
     */
    public void onCreate() {
	super.onCreate();
	IntentFilter intentFilter = new IntentFilter();
	intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
	registerReceiver(myReceiver, intentFilter);
    }

    /**
     * Inherited from Service.
     */
     public void onDestroy() {
	 super.onDestroy();
	 unregisterReceiver(myReceiver);
     }

}
