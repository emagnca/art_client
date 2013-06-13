package com.cc.cg;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;


import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;

import com.cc.cg.database.*;
import com.cc.cg.json.*;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.sql.SQLException;
import java.text.SimpleDateFormat;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.CalendarContract.Events;

/**
 * Class that with a fixed interval is woken up by the alarm service.
 * It then reports the current position to the server, and also 
 * checks if there is any updates of projects/activities in the 
 * database, or any new messages to fetch. It turns out that for 
 * each call by the alarm service, a new instance is created of 
 * the class. 
 **/
public class PositionReporter extends BroadcastReceiver {

    private static final String TAG = "PositionReporter";
    private RemoteLog _remoteLog;
    private JsonClient _jsonClient = new JsonClient();

    // Not really necessary here, but kept for the moment.
    private AtomicBoolean _isInProgress = new AtomicBoolean(false);
   
    /**
     * The constructor.
     */
    public PositionReporter(){
	OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);
    }

    /**
     * The method called by the Alarm Service.
     * @param context -  context provided by the alarm service
     * @param intent  -  intent provided by alarm service, not used
     **/ 
    public void onReceive(Context context, Intent intent) {
	Log.i(TAG, "onReceive called by alarm service. Thread=" + 
	      Thread.currentThread().getId());
	_remoteLog = new RemoteLog(context); 
	if(_isInProgress.compareAndSet(false, true))
	    reportPosition(context);
	Log.i(TAG, "done with onReceive");
    }
    
    /**
     * Sending a position report to the server. This might take seconds
     * so it is done in a separate thread not to block the application.
     * @param context -  provided by the alarm service
     */
    private void reportPosition(Context context){
	Log.d(TAG, "reportPosition()");

	Location location = null;
	PersistentValues pv = new PersistentValues(context);    

	try{
	    if(pv.isVisible()){
		Log.d(TAG, "isVisible");
		Location l = SessionContext.instance().getLastKnownLocation();
		if(l!=null && l.getLatitude() != 0)
		    location = SessionContext.instance().getLastKnownLocation();
	    }
	    DatabaseHelper helper = (DatabaseHelper)OpenHelperManager.getHelper(context);
	    Dao<TimeSlot, Integer> timeSlotDao = helper.getTimeSlotDao();
	    List<TimeSlot> list = helper.getTimeSlotDao().queryForEq(TimeSlot.STATE_FIELD_NAME,
								     TimeSlot.State.STARTED);
	    int projectId = -1;
	    int activityId = -1;
	    if(list.size() > 0){
		TimeSlot timeSlot = list.get(0);
		Project project = helper.getProjectDao().queryForId(timeSlot.getProject().getId());
		Activity activity = helper.getActivityDao().queryForId(timeSlot.getActivity().getId());
		if(project != null && activity != null){
		    projectId = project.getId();
		    activityId = activity.getId();
		}		
	    }
	    
	    // AsyncRemoteReporter will send the report from another thread.
	    AsyncRemoteReporter reporter = new AsyncRemoteReporter(context, pv.getServer(), pv.getUser(), location,
								   SessionContext.instance().getBatteryLevel(),
								   projectId, activityId, pv.isVisible());
	    ThreadPool.instance().execute(reporter);
	}
	catch(SQLException x){
	    Log.d(TAG, x.getMessage(), x);
	}
    }      
     

    /**
     * Check for database updates on the server, and if any messages are to be received.
     * Also checks if any timeslots are to be sent to server, and if the location service is up.
     * @param context -  provided by the alarm service
     * @param object  -  container for projects, activities and messages returned 
     *                   for the server for the current user
     **/
    private void checkForUpdates(Context context, JsonModel.SimpleObject object){
	checkForNewSimpleProjects(context, object.projects);
	checkForNewSimpleActivities(context, object.activities);
	checkForNewSimplePlans(context, object.plans);
        checkForMessages(context, object.message);
	checkTimeSlots(context);
	checkLocationService(context);
    }
	
    /**
     * Fetch any messages that the server might have in store for us.
     * @param context - provided by the alarm service
     * @param message - message received from the server 
     **/
    private void checkForMessages(Context context, String message){
	Log.d(TAG, "checkForMessages");
	PersistentValues pv = new PersistentValues(context);
	//String msg = _jsonClient.getMessage(pv.getServer(),
	//					pv.getUser());
	if(message.startsWith(Constants.DUMP_LOG_MSG)){
	    Log.d(TAG, "Got request to dump the trace log");
	    _remoteLog.dumpLog();
	}
	/* Currently no messages sent to phone
	else if(message.length() > 0){
	    Log.d(TAG, "Got message: " + message);
	    showMessageNotification(context, message);
	}
	*/
	else
	    Log.d(TAG, "No message for me :-(");
    }

    /**
     * Checks if there are any new activities on the server.
     * @param context -  provided by the alarm service
     * @param activitiesInServer - activities received from the server for the current user
     **/
    private void checkForNewSimplePlans(Context context,
					Collection<JsonModel.SimplePlan> plansInServer){
	Log.i(TAG, "Check for plan  updates");
	SimpleDateFormat dateFormat =
	    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	try{
	    Dao<WorkPlan, Integer> workPlanDao = 
		((DatabaseHelper)OpenHelperManager.getHelper(context)).getWorkPlanDao();
	    
	    for(JsonModel.SimplePlan plan : plansInServer){
		List<WorkPlan> list = workPlanDao.queryForEq(WorkPlan.ID_FIELD_NAME, plan.id);
		    Date start = dateFormat.parse(plan.start);
		    Date stop = dateFormat.parse(plan.stop);
		if(list.size() == 0){
		    Log.d(TAG, "Inserting plan: " + plan);
		    workPlanDao.create(new WorkPlan(plan.id, plan.project, plan.activity, start.getTime(), 
						    stop.getTime(), plan.comment, plan.timestamp));

		    final ContentResolver cr = context.getContentResolver();
		    ContentValues values = new ContentValues();
		    values.put(Events.DTSTART, start.getTime());
		    values.put(Events.DTEND, stop.getTime());
		    values.put(Events.TITLE, plan.project);
		    values.put(Events.DESCRIPTION, plan.activity + "\n" + plan.comment);
		    values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
		    for(int i=1; i<=2; i++){ //To improve. For now insert into (maximum) first 2 calendars.
			values.put(Events.CALENDAR_ID, i);
			android.net.Uri uri = cr.insert(Events.CONTENT_URI, values);
		    }
		}
	    }   
	}
	catch(java.text.ParseException x){
	    Log.e(TAG, x.getMessage());
	}
	catch(java.sql.SQLException x){
	    Log.e(TAG, x.getMessage());
	}
    }

    /**
     * Checks if there are any new activities on the server.
     * @param context -  provided by the alarm service
     * @param activitiesInServer - activities received from the server for the current user
     **/
    private void checkForNewSimpleActivities(Context context,
					     Collection<JsonModel.SimpleActivity> activitiesInServer){
	Log.i(TAG, "Check for activity updates");

	try{
	    PersistentValues pv = new PersistentValues(context);
	    //JsonModel.SimpleActivity[] activitiesInServer = 
	    //	_jsonClient.getSimpleActivities(pv.getServer(), pv.getUser());
	    Set<Integer> activityIds = new HashSet<Integer>();
	    Dao<Activity, Integer> activityDao = 
		((DatabaseHelper)OpenHelperManager.getHelper(context)).getActivityDao();
	    boolean isActivitiesUpdated = false;

	    // First compare the activity on the server with the servers in the database.
	    for(JsonModel.SimpleActivity activity : activitiesInServer){
		activityIds.add(activity.id);
		isActivitiesUpdated |= checkSimpleActivity(context, activity);
	    }

	    // Then compare the activities in the database with the ones on the server
	    // to see if there are any that we could remove. To be safe, don't remove
	    // any activities if we didn't get any from the pp server.
	    List<Activity> activitiesInDb = 
		activityDao.queryBuilder().where().eq(Project.IS_ACTIVE_FIELD_NAME, 
						      true).query();		

	    if(activityIds.size() > 0){ 
		for(Activity activity : activitiesInDb){
		    Log.d(TAG, "Checking if activity to be removed: " + activity.getName());
		    if(!activityIds.contains(activity.getId())){
			Log.d(TAG, "Yes, removing activity");
			activity.isActive(false);
			activityDao.update(activity);
			isActivitiesUpdated = true;
		    }
		}
	    }

	    Log.d(TAG, "Were activities updated??? " + isActivitiesUpdated);
	    if(isActivitiesUpdated) {
		Log.i(TAG, "Yes, activities were updated. Signalling this...");
		// Handles as a modification of projects
		EventDispatcher.instance().signalProjectsModified();
	    }
	    
	}	    
	catch(Exception x){ //IOException, SQLException or JsonParseException
	    Log.e(TAG, x.getMessage(), x);
	}
	finally{
	    OpenHelperManager.release();
	}	    

    }

    
    /**
     * Check if there any new activities on the server. Also looking for updates, and
     * removes projects that no longer exist on the server.
     * @param context -  provided by the alarm service
     * @param projsInServer - projects received from the server for the current user
     */
    private void checkForNewSimpleProjects(Context context, 
					   Collection<JsonModel.SimpleProject> projsInServer){
	Log.i(TAG, "Check for project updates");
	try{
	    PersistentValues pv = new PersistentValues(context);
	    //JsonModel.SimpleProject[] projsInServer = 
	    //	_jsonClient.getSimpleProjects(pv.getServer(), pv.getUser());
	    Set<Integer> projectIds = new HashSet<Integer>();
	    Dao<Project, Integer> projectDao = ((DatabaseHelper)OpenHelperManager.getHelper(context)).getProjectDao();
	    boolean isProjectsUpdated = false;

	    // First compare the projects on the server with the servers in the database.
	    for(JsonModel.SimpleProject project : projsInServer){
		projectIds.add(project.id);
		isProjectsUpdated |= checkSimpleProject(context, project);
	    }

	    // Then compare the project in the database with the ones on the server
	    // to see if there are any that we could remove. To be safe, don't remove
	    // any projects if we didn't get any from the pp server.
	    List<Project> projsInDb = projectDao.queryBuilder().where().eq(Project.IS_ACTIVE_FIELD_NAME, 
									   true).query();		

	    if(projectIds.size() > 0){ 
		for(Project project : projsInDb){
		    Log.d(TAG, "Checking if project to be removed: " + project.getName());
		    if(!projectIds.contains(project.getId())){
			Log.d(TAG, "Yes, removing project");
			project.isActive(false);
			projectDao.update(project);
			isProjectsUpdated = true;
		    }
		}
	    }

	    Log.d(TAG, "Were projects updated??? " + isProjectsUpdated);
	    if(isProjectsUpdated) {
		Log.i(TAG, "Yes, projects were updated. Signalling this...");
		EventDispatcher.instance().signalProjectsModified();
	    }
	}	    
	catch(Exception x){ //IOException, SQLException or JsonParseException
	    Log.e(TAG, x.getMessage(), x);
	}
	finally{
	    OpenHelperManager.release();
	}
    }

    /**
     * Compares one project on the server with the one we have in the database, or add it if it 
     * doesn't exist.
     * @param  project - a project on the server
     * @return boolean - true if the database was updated   
     **/
    private boolean checkSimpleProject(Context context, JsonModel.SimpleProject project) throws SQLException {
	Log.d(TAG, "Checking if project in db: " + project.toString());

	boolean isProjectUpdated = false;
	DatabaseHelper helper = (DatabaseHelper)OpenHelperManager.getHelper(context);
	Dao<Project, Integer> projectDao = helper.getProjectDao();
	List<Project> list = projectDao.queryForEq(Project.ID_FIELD_NAME, project.id);
	if(list.size() == 0){
	    Log.d(TAG, "Adding project: " + project.name);
	    isProjectUpdated = true;
	    helper.getProjectDao().create(new Project(project.id, 
						      project.name, 
						      project.address,
						      project.postcode,
						      project.latitude, 
						      project.longitude,
						      project.info,
						      project.timestamp)); 
	} 
	else {
	    Project projectInDb = list.get(0);
	    if(projectInDb.getTimestamp() != project.timestamp ||
	       projectInDb.isActive() == false){
		Log.d(TAG, "Project was updated. Used to be: " + projectInDb.toFullString());
		projectInDb.setName(project.name);
		projectInDb.setAddress(project.address);
		projectInDb.setPostCode(project.postcode);
		projectInDb.setLatitude(project.latitude);
		projectInDb.setLongitude(project.longitude);
		projectInDb.setInfo(project.info);
		projectInDb.setTimestamp(project.timestamp);
		projectInDb.isActive(true);
		projectDao.update(projectInDb);
		Log.d(TAG, "Now set to: " + projectInDb.toFullString());
		isProjectUpdated = true;
	    }
	    else { Log.d(TAG, "Project was not updated"); }
	}	
	return isProjectUpdated;
    }

    /**
     * Compares one activity on the server with the one we have in the database, or add it if it 
     * doesn't exist.
     * @param  activity - an activity on the server
     **/
    private boolean checkSimpleActivity(Context context, JsonModel.SimpleActivity activity) throws SQLException {
	Log.d(TAG, "Checking if activity in db: " + activity.toString());

	boolean isActivityUpdated = false;
	DatabaseHelper helper = (DatabaseHelper)OpenHelperManager.getHelper(context);
	Dao<Activity, Integer> activityDao = helper.getActivityDao();
	List<Activity> list = activityDao.queryForEq(Activity.ID_FIELD_NAME, activity.id);
	if(list.size() == 0){
	    Log.d(TAG, "Adding activity: " + activity.name);
	    isActivityUpdated = true;
	    helper.getActivityDao().create(new Activity(activity.id, 
							activity.name, 
							activity.timestamp)); 
	} 
	else {
	    Activity activityInDb = list.get(0);
	    if(activityInDb.getTimestamp() != activity.timestamp ||
	       activityInDb.isActive() == false){
		Log.d(TAG, "Activity was updated. Used to be: " + activityInDb.toFullString());
		activityInDb.setName(activity.name);
		activityInDb.setTimestamp(activity.timestamp);
		activityDao.update(activityInDb);
		Log.d(TAG, "Now set to: " + activityInDb.toFullString());
		isActivityUpdated = true;
	    }
	    else { Log.d(TAG, "Activity was not updated"); }
	}	
	return isActivityUpdated;
    }


    private void checkTimeSlots(Context context){
	Calendar c = Calendar.getInstance();
	c.add(Calendar.DATE, Constants.MAX_TIMEREPORT_DAYS*-1);
	try{
	    DatabaseHelper helper = (DatabaseHelper)OpenHelperManager.getHelper(context);
	    Dao<TimeSlot, Integer> timeSlotDao = helper.getTimeSlotDao();
	    List<TimeSlot> timeSlots =
		timeSlotDao.queryBuilder().where().lt(TimeSlot.STOP_FIELD_NAME, c.getTime())
		.and().eq(TimeSlot.STATE_FIELD_NAME,TimeSlot.State.STOPPED).query();
	    Log.d(TAG, "Sending old timeslots no:" + timeSlots.size());
	    AsyncClient client = new AsyncClient(context);
	    for(TimeSlot timeSlot : timeSlots){
		Log.d(TAG, timeSlot.toString());
		client.sendReport(timeSlot);
	    }
	}
	catch(SQLException x){
	    Log.e(TAG, x.getMessage(), x);
	}
	finally{
	    OpenHelperManager.release();
	}
    }

    private void checkLocationService(Context context){
	Log.d(TAG, "Checking location service");
	if(!SessionContext.instance().isLocationServiceUp()){
	    Log.w(TAG, "Location service has stopped. Restarting...");
	    AutoStart.startLocationService(context);
	}
	else {
	    Log.d(TAG, "Location service is working");
	}
    }

    /**
     * Send a Android notification to be displayed to the user.
     * @param context  -   context provided by the alarm service
     * @param message  -   the message to be shown to the user
     **/
    private void showMessageNotification(Context context, String message){

	Log.d(TAG, "Sending notification");

	final int MY_NOTIF_ID = 2;

	NotificationManager notificationManager =
	    (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	Notification myNotification = new Notification(R.drawable.bluesquare,
						       "Admin meddelar!",
						       System.currentTimeMillis());

	String notificationTitle = "Admin meddelar";
	String notificationText = message;

	Intent intent = new Intent(context, MsgPopupActivity.class);
	// The message is passed like this because of a bug, maybe in the framework.
	// The old value remains in com.cc.cg.msg when the activity is started twice.
	SessionContext.instance().setMessage(message);  
	intent.putExtra("com.cc.cg.msg", message);
	intent.putExtra("com.cc.cg.notifid", MY_NOTIF_ID);

	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	myNotification.defaults |= Notification.DEFAULT_SOUND;
	myNotification.defaults |= Notification.DEFAULT_VIBRATE;

	myNotification.setLatestEventInfo(context,
					  "Admin meddelar...",
					  notificationText,
					  pendingIntent);
	notificationManager.notify(MY_NOTIF_ID, myNotification);
    }

    /**
     * Inner class that is responsible for actually sending the position report. This will be done in
     * a separate thread to avoid the application from being blocked while waiting for the response.
     */
    class AsyncRemoteReporter implements Runnable {
	private String host;
	private boolean isVisible;
	private double latitude, longitude;
	private int project, activity, user, battery;
	private Date timeStamp;
	private Context context;

	public AsyncRemoteReporter(Context context, String host, int user,
				   Location location, int battery, int project,
				   int activity, boolean isVisible){
	    this.context = context;
	    this.host = host;
	    this.user = user;
	    this.project = project;
	    this.activity = activity;
	    this.latitude = location==null? 0:location.getLatitude();
	    this.longitude = location==null? 0:location.getLongitude();
	    this.battery = battery;
	    this.isVisible = isVisible;
	}
	
	/**
	 * This method will be started in another thread.
	 **/
	public void run(){
	    Log.i(TAG, "Sending positionreports. Thread=" + Thread.currentThread().getId());

	    try{
		Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(context));
		String s="";
		
		SimpleDateFormat dateFormat =
		    new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
		String time = dateFormat.format(new Date());
		JsonModel.SimpleObject o = 
		    _jsonClient.sendPositionReport(host, user, time, latitude, longitude,
						   battery, project, activity, isVisible);
		checkForUpdates(context, o); 
		
		_isInProgress.set(false);	    
	    }
	    catch(Exception x){ //IOException or JsonParseException
		Log.d(TAG, x.getMessage(), x);
	    }

	}
    }
    

}
