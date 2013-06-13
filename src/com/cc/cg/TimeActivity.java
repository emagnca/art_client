package com.cc.cg;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.CloseableIterator;

import com.cc.cg.database.*;

import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.util.*;

/**
 * Class that implements the activity where one can start, stop and see the time reporting.
 */
public class TimeActivity extends OrmLiteBaseActivity<DatabaseHelper> implements EventDispatcher.PositionListener
{
    private final static String TAG = "TimeActivity";
    private final static String TIME_FORMAT = "HH:mm:ss";

    private TextView _chronometer, _timeInterval, _projectLabel, _activityLabel;
    private Button _button;
    private CheckBox _isAutomatic, _isVisible;
    private String _timeIntervalPrefix;
    private MyTimer _timer = new MyTimer();
    private PersistentValues _pv;
    private DbFacade _db;

    /**
     * The activity's onCreate lifecycle method. Creates the different items of the gui.
     *
     * @param bundle    state possibly saved from onSaveInstance method
     */
    public void onCreate(Bundle bundle)
    {
	Log.i(TAG, "OnCreate thread=" + Thread.currentThread().getId());
	
        super.onCreate(bundle);

        setContentView(R.layout.main);

	_isAutomatic = (CheckBox) findViewById(R.id.isAutomatic);
	_isVisible = (CheckBox) findViewById(R.id.isVisible);
	_chronometer = (TextView) findViewById(R.id.chronometer);
	_timeInterval = (TextView) findViewById(R.id.timeInterval);
	_button = (Button) findViewById(R.id.reportButton);
	_projectLabel = (TextView)findViewById(R.id.project);  
	_activityLabel = (TextView)findViewById(R.id.activity);  
	_pv = new PersistentValues(this);
	_db = new DbFacade(this);

	OnLongClickListener labelListener = new OnLongClickListener() {
		public boolean onLongClick(android.view.View v) {
		    Log.d(TAG, "projectLabel clicked");
		    Intent intent = new Intent(TimeActivity.this, ProjectListActivity.class);
		    startActivityForResult(intent, 0);
		    return true;
		}
	    };
	
	_isAutomatic.setOnCheckedChangeListener(new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
		    Log.d(TAG, "onCheckedChanged isAutomatic=" + isChecked);
		    _pv.isReportingAutomatic(isChecked);
		    if(isChecked){
			Log.d(TAG, "IsChecked=true, isRep=" + _db.isReporting() + " currp=" + _pv.getCurrentProject()); 
			if(SessionContext.instance().getLastProjectVisited() < 0)
			    stopReporting();
			else{
			    _pv.setCurrentProject(SessionContext.instance().getLastProjectVisited());
			    startReporting();
			}
		    }
		    updateGui();
		}});

	_isVisible.setOnCheckedChangeListener(new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
		    Log.d(TAG, "onCheckedChanged isVisible=" + isChecked);
		    _pv.isVisible(isChecked);
		}
	    });

	_projectLabel.setOnLongClickListener(labelListener);
	_activityLabel.setOnLongClickListener(labelListener);
	_button.setOnClickListener(new TimeButtonClickListener());
	
	Log.d(TAG, "Is timereportService running? " + SessionContext.instance().isLocationServiceUp());
	// Starts the various services. This should not be necessary, but useful when transferring the app
	// to a phone which already has it installed.
	if(!SessionContext.instance().isLocationServiceUp()){
	    Log.d(TAG, "Starting services");
	    AutoStart.startLocationService(this);
	    AutoStart.startBatteryService(this);
	    AutoStart.startPositionReporter(this);
	    //Intent myIntent = new Intent(this, LocationService.class);
	    //startService(myIntent);
	}
    }

    /**
     * The activity's onDestroy lifecycle method.
     */
    public void onDestroy()
    {
	Log.d(TAG, "OnDestroy isFinishing=" + isFinishing());
	super.onDestroy();
    }
            
    /**
     * The activity's onSaveInstance is used to save the state. Not used by this class since the state
     * is persisted with the class PersistentValues.
     */
    protected void onSaveInstanceState(Bundle bundle) 
    {
	Log.d(TAG, "OnSaveInstance");
    }

    /**
     * The activity's onStart lifecycle method. Used to restore the content of the activity's 
     * graphical components.
     */ 
    public void onStart()
    {
	Log.d(TAG, "OnStart isReportingAutomatic=" + _pv.isReportingAutomatic());

	super.onStart();

	if(_db.isReporting())
	    startReporting();
	EventDispatcher.instance().registerPositionListener(this);

	Log.d(TAG, "Out of onStart");
    }

    /**
     * The activity's onStop life cycle method.
     */
    public void onStop()
    {
	Log.d(TAG, "OnStop");
	super.onStop();
	EventDispatcher.instance().unregisterPositionListener(this);
	_timer.stop();
    }

    /**
     * The activity's onPause life cycle method.
     */            
    public void onPause()
    {
	Log.d(TAG, "OnPause");
	super.onPause();
    }
            
    /**
     * The activity's onResume life cycle method.
     */
    public void onResume()
    {
	Log.d(TAG, "OnResume");
	super.onResume();
	updateGui();
    }


    /**
     * Returns result from the ProjectList activity. Inherited from Activity.
     * Updates the gui if a new project and/or activity were selected.
     */
    /*
    protected void onActivityResult(int requestCode, 
				    int resultCode,
				    Intent data) {
	Log.d(TAG, "Got result back from project list. Code=" + resultCode); 
	if (resultCode == Constants.DATA_MODIFIED) {

	    Log.d(TAG, "Data was modified");
	    try{
		TimeSlot timeSlot = _db.getCurrentTimeSlot();
		if(timeSlot == null)
		    Log.d(TAG, "Got null timeslot back");
		else
		    Log.d(TAG, "Got timeslot back: " + timeSlot.toString());
		if (timeSlot != null){
		    int pid = _pv.getCurrentProject();
		    int aid = _pv.getCurrentActivity();
		    Log.d(TAG, "New activityId = " + aid);
		    Activity activity = getHelper().getActivityDao().queryForId(aid);
		    if(activity != null){
			timeSlot.setActivity(activity);
			Log.d(TAG, "Updated timeslot to activity:" + activity);
		    }
		    if (!_pv.isReportingAutomatic()){
			Project project = getHelper().getProjectDao().queryForId(pid);
			if(project != null){
			    timeSlot.setProject(project);
			    Log.d(TAG, "Updated timeslot to project:" + project);
			}
		    }
		    getHelper().getTimeSlotDao().update(timeSlot);
		}
		updateGui();
	    }
	    catch(SQLException x) { Log.e(TAG, x.getMessage(), x); }
	    
	}
    }
    */

    /**
     * Returns all timeslots for today.
     */
    private CloseableIterator<TimeSlot> getWorkForToday() throws SQLException {
	TimeGroup timeGroup = getHelper().getTimeGroupDao().queryForId(TimeGroup.getDayCode(new Date()));
	Log.d(TAG, "Timegroup == null? " + timeGroup == null? "Yup":"Nop"); 
	if(timeGroup != null){
	    Log.d(TAG, "Slots==null? " + timeGroup.getSlots() == null? "Yup":"Nop");  
	    if(timeGroup.getSlots() != null)
		Log.d(TAG, "Slotscount " + timeGroup.getSlots().size());  
	}
	return timeGroup == null || timeGroup.getSlots() == null? null : timeGroup.getSlots().iteratorThrow();
    }

    /**
     * Get total amount of seconds worked for today.
     */
    public int getWorkTotalForToday() {
	int total = 0;
	try{
	    CloseableIterator<TimeSlot> itr = getWorkForToday();
	    if(itr != null){
		while(itr.hasNext()) total += itr.next().getTotal();
		itr.close();
	    }
	}
	catch(SQLException x) { Log.e(TAG, x.getMessage(), x); }
	return total;
   }

    /**
     * Updates the gui's visible components.
     */
    private void updateGui(){
	updateButtons();
	updateLabels();
	updateChronometer();
    }

    /**
     * Updates the button and checkbox.
     */
    private void updateButtons(){

	boolean isAutomatic = _pv.isReportingAutomatic();
	boolean isVisible = _pv.isVisible();

	_isAutomatic.setChecked(isAutomatic);
	_isVisible.setChecked(isVisible);
	_button.setEnabled(!isAutomatic);
	if(isAutomatic){
	    _button.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_off));
	    _button.setTextColor(Color.BLACK);
	}
	else{
	    Drawable d = _db.isReporting()? 
		getResources().getDrawable(R.drawable.button_stop) :
		getResources().getDrawable(R.drawable.button_start);
	    String s = _db.isReporting()? "Stopp" : "Start";
	    _button.setBackgroundDrawable(d);
	    _button.setText(s);
	    _button.setTextColor(Color.WHITE);
	}
    }


    /**
     * Updates the chronometer (sic!)
     */
    private void updateChronometer(){

	Log.d(TAG, "setChronometer");

	Date start = null, stop = null;
	int workedTime = 0;
	try{
	    CloseableIterator<TimeSlot> itr = getWorkForToday();
	    
	    if(itr != null){
		while(itr.hasNext()){
		    TimeSlot time = itr.next();
		    Log.d(TAG, "Entry: " + time.toString());
		    TimeSlot.State state = time.getState();
		    if(state == TimeSlot.State.STARTED){
			if(start == null || time.getStart().before(start)) start = time.getStart();
		    }
		    else if(state == TimeSlot.State.STOPPED || state == TimeSlot.State.APPROVED){
			if(start == null || time.getStart().before(start)) start = time.getStart();
			if(stop == null || time.getStop().after(stop)) stop = time.getStop();
		    }
		    
		    workedTime += time.getTotal();
		    Log.d(TAG, "WorkedTime=" + workedTime);
		}
		itr.close();
	    }
	    Log.d(TAG, "Work found for today. Start = " + start + " stop=" + stop + ", total=" + workedTime) ;
	    
	}
	catch(SQLException x){ Log.e(TAG, "Failed to close itr", x); }
	    
	Log.d(TAG, "setChronometer start=" + start);
	if(start == null){
	    _timeIntervalPrefix = null;
	    _timeInterval.setText("");
	}
	else{ 
	    SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT);
	    _timeIntervalPrefix = formatter.format(start) + " - ";
	    if(stop != null)
		_timeInterval.setText(_timeIntervalPrefix + formatter.format(stop));
	}
	
	_chronometer.setText(format(workedTime));
	
    }


    /**
     * Updates the activity and project text labels.
     */ 
    private void updateLabels(){
	int currentProject= _pv.getCurrentProject();
	int currentActivity = _pv.getCurrentActivity();
	Project project = null;
	Activity activity = null;
	
	try{
	    project = getHelper().getProjectDao().queryForId(currentProject);
	    activity = getHelper().getActivityDao().queryForId(currentActivity);
	}
	catch(SQLException x){
	   Log.e(TAG, x.getMessage(), x);
	}
	String projectName = project != null? project.getName() : "Inget projekt valt";
	String activityName = activity != null? activity.getName() : "Ingen aktivitet vald";

	_activityLabel.setText(activityName);
	_projectLabel.setText(projectName);
    }


    /**
     * Called when the time reporting is stopped while this activity is active, either because
     * the stop button is pushed, or because the phone moved out from project area
     * with automatic reporting selected.
     */
    private void stopReporting(){
	Log.d(TAG, "stopReporting");
	_db.registerWorkStop(false); 
	_timer.stop();
	updateGui();
    }

    /**
     * Starts time reporing, either because the start button is pushed or because the phone
     * has moved into a project area while this activity is active.
     */
    private void startReporting(){
	Log.d(TAG, "startReporting, timeIntervalPrefix=" + _timeIntervalPrefix);

	if(_db.registerWorkStart(_pv.getCurrentProject(), 
				 _pv.getCurrentActivity(),
				 false)){
	    if(_timeIntervalPrefix == null)
		_timeIntervalPrefix = new SimpleDateFormat("HH:mm:ss").format(new Date()) + " - ";
	    _timer.start();
	}
	else {
	    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
	    alertDialog.setTitle("");
	    alertDialog.setMessage("Kunde ej starta tidsrapportering. Du måste välja projekt och aktivitet.");
	    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, 
				  "Ok", 
				  new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int which) {
					  return;
				      }});
	    alertDialog.show();
	}

    }

    /**
     * Called by the EventDispatcher when phone moved into a project area.
     *
     * @param projectId     the identifier of the project
     */
    public void movedIntoProjectArea(long projectId){
	Log.i(TAG, "Moved into project no: " + projectId + 
	      " isautomatic=" + _pv.isReportingAutomatic());
	if(_pv.isReportingAutomatic())
	    startReporting();
	updateGui();
    }

    /**
     * Called by the EventDispatcher when phone moved out of project area.
     *
     * @param projectId     the identifier of the project
     */
    public void movedOutOfProjectArea(){
	Log.i(TAG, "Moved out of project area is automatic" +
	      _pv.isReportingAutomatic());
	if(_pv.isReportingAutomatic())
	    stopReporting();
	updateGui();
    }

    /**
     * Called by the EventDispatcher when the phone's position has changed.
     */
    public void newPosition(){
	Log.d(TAG, "New position");
    }
    
    /**
     * Creates the option's menu. Inherited from Activity.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
	//menu.add(0, Menu.FIRST+1, 0, "Visa projekt");
	menu.add(0, Menu.FIRST+2, 0, "Visa inställningar");
	return super.onCreateOptionsMenu(menu);
    }

    /**
     * Called when one of the item's in the option menu has been selected. 
     * Inherited from Activity.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
	if(item.getItemId() == Menu.FIRST+1){
	    Intent intent = new Intent(TimeActivity.this, ProjectListActivity.class);
	    startActivityForResult(intent, 0);
	}
	else if(item.getItemId() == Menu.FIRST+2){
	    Intent intent = new Intent(TimeActivity.this, SettingsActivity.class);
	    startActivity(intent);
	}
	return true;
    }

    /**
     * Formats the chronometer's text string.
     *
     * @param The amout of time worked in milliseconds. 
     */
    private String format(long millis){
	long seconds = millis/1000;
	long hours = seconds/3600;
	seconds %= 3600;
	long minutes = seconds/60;
	seconds = seconds%60;
	StringBuilder sb = new StringBuilder();
	if(hours < 10) sb.append("0");
	sb.append(hours);
	sb.append(":");
	if(minutes < 10) sb.append("0");
	sb.append(minutes);
	sb.append(":");
	if(seconds < 10) sb.append("0");
	sb.append(seconds);
	return sb.toString();
    }
    

    /**
     * Class that knows starts and stops the regular updates of the chronometer.
     */
    private class MyTimer {
	Timer timer = null;
	TickListener tickListener = null;
	void start(){
	    Log.i(TAG, "Starting timer");
	    if(timer != null) stop();
	    timer = new Timer();
	    tickListener = new TickListener();
	    timer.schedule(new MyTimerTask(tickListener), 0, 1000);
	}
	void stop(){
	    Log.i(TAG, "Stopping timer");
	    if(tickListener != null)
		tickListener.stop();
	    if(timer != null){
		timer.cancel();
		timer.purge();
	    }
	    timer = null;
	    tickListener = null;
	}
    }

    /**
     * Class that is called every second and updates the 
     * chronometer.
     */
    private class TickListener implements Runnable{
	Date date = new Date();
	long timestamp = System.currentTimeMillis();
	long workedTime = 0;
	SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT);

	TickListener(){
	    workedTime = getWorkTotalForToday(); 
	    if(_timeIntervalPrefix == null)
		_timeIntervalPrefix = formatter.format(System.currentTimeMillis()) + " - ";
	}

	void tick(){
	    long newTimestamp = System.currentTimeMillis();
	    workedTime += newTimestamp-timestamp;
	    timestamp = newTimestamp;
	    _timeInterval.post(this); // post this to the GUI thread
	}

	public void run(){
	    date.setTime(System.currentTimeMillis());
	    _chronometer.setText(format(workedTime));
	    _timeInterval.setText(_timeIntervalPrefix + formatter.format(date));
	}

	void stop(){
	    Log.d(TAG, "stopping ticklistener updating db with worktime=" + workedTime + "timestamp=" + timestamp);
	}
    }


    /**
     * Timertask that calls a TickListener when the chronometer is to be updated.
     */
    private class MyTimerTask extends TimerTask {
	TickListener callback;
	MyTimerTask(TickListener cbk){
	    callback = cbk;
	}
	public void run(){
	    callback.tick();
	}
    }


    /**
     * Class that listens to clicks on the start/stop button.
     */
    private class TimeButtonClickListener implements OnClickListener 
    {
	public void onClick(View v) 
	{
	    boolean isReporting = _db.isReporting();
	    Log.i(TAG, "start/stop button clicked is reporting=" + isReporting);
	    if(isReporting){
		stopReporting();
	    }
	    else if(_pv.getCurrentActivity() != -1){
		startReporting();
	    }    
	    updateGui();
	}
    }
	
}
      


    
    
	    
