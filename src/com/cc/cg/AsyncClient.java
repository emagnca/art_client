package com.cc.cg;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import com.cc.cg.database.*;
import com.cc.cg.json.JsonClient;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
Class that contains cmethods and classes used for asynchronous sending to 
the pp server, and that are used at several places in the application. 
The classes implement Runnable so that they might be started in a separate thread.  
**/
public class AsyncClient{

    private final static String TAG = "AsyncClient";
    private Context context;

    public AsyncClient(Context context){
	this.context = context;
	OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);
    }
    
    /**
     * Sends a time report to the server, in a separate thread with help of the inner class 
     * AsyncRemoteReporter.
     */
    public void sendReport(TimeSlot timeSlot){
	Log.d(TAG, "sendReport()" + timeSlot);
	PersistentValues pv = new PersistentValues(context);
	try{
	    DatabaseHelper helper = (DatabaseHelper)OpenHelperManager.getHelper(context);
	    Project project = helper.getProjectDao().queryForId(timeSlot.getProject().getId());
	    Activity activity = helper.getActivityDao().queryForId(timeSlot.getActivity().getId());
	    TimeGroup timeGroup = helper.getTimeGroupDao().queryForId(timeSlot.getGroup().getId());
	    if(project == null || activity == null){
		Log.e(TAG, "Activity or Project does not exist for TimeSlot: " + timeSlot.toString());
		Toast.makeText(context, "Activitet eller projekt saknas. Kan ej repportera :-(", 
			       Toast.LENGTH_SHORT).show();
	    }
	    else{
		int pid = project.getId();
		int aid = activity.getId();
		int uid = pv.getUser();
		long start = timeSlot.getStart().getTime()/1000;
		long stop = timeSlot.getStop().getTime()/1000;
		boolean b = timeSlot.isAutomatic();
		AsyncRemoteReporter reporter = new AsyncRemoteReporter(pv.getServer(), timeGroup, timeSlot, 
								       pid, aid, uid, start, stop, b);
		ThreadPool.instance().execute(reporter);
	    }
	}
	catch(java.sql.SQLException x){
	    Log.e(TAG, x.getMessage(), x);
	    Toast.makeText(context, "Kan ej repportera :-(. Var god försök senare", 
			   Toast.LENGTH_SHORT).show();
	}
	catch(NumberFormatException x){
	    Log.e(TAG, x.getMessage(), x);
	}
    }

    /**
     * Class that do the actual sending of a time report. Implements Runnable so that it can be done in a 
     * separate thread.
     */
    private class AsyncRemoteReporter implements Runnable {
	private String uri;
	private TimeGroup timeGroup;
	private TimeSlot timeSlot;
	private int project, activity, user;
	private long start, stop;
	private boolean isAutomatic;
	
	public AsyncRemoteReporter(String uri, TimeGroup timeGroup, TimeSlot timeSlot, 
				   int proj, int act, int user,
				   long start, long stop, boolean isAutomatic){
	    this.uri = uri;
	    this.timeGroup = timeGroup;
	    this.timeSlot = timeSlot;
	    this.project = proj;
	    this.activity = act;
	    this.user = user;
	    this.start = start;
	    this.stop = stop;
	    this.isAutomatic = isAutomatic;
	}
	
	public void run(){
	    Log.d(TAG, "Actual send of time report");
	    Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(context));
	    
	    try{
		JsonClient jsonClient = new JsonClient();
		jsonClient.sendTimeReport(uri, project, activity, user,
					  start, stop, isAutomatic, "");
		timeSlot.approve();
		timeGroup.approve();
		DatabaseHelper helper = (DatabaseHelper)OpenHelperManager.getHelper(context);
		helper.getTimeSlotDao().update(timeSlot);
		helper.getTimeGroupDao().update(timeGroup);
	    }
	    catch(Exception x){ //SqlException or if post request fails
		Log.e(TAG, x.getMessage(), x);
		//Toast.makeText(context, "Kan ej repportera :-(. Serverproblem. Var god försök senare.", 
		//       Toast.LENGTH_SHORT).show();
	    }
	}
    }
}