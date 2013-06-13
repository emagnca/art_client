package com.cc.cg;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.cc.cg.database.*;
import com.cc.cg.json.JsonClient;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.io.IOException;
import java.util.*;


/**
 * Class that shows the time reports to the user. Each list entry (group) represents a day. The
 * days can be expanded so that the time slots for each day becomes visible.
 */
public class ReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    public final static String TAG = "ReportActivity";
    public final static int APPROVE_OPTION = Menu.FIRST;
    public final static int COMMENT_OPTION = Menu.FIRST + 1;
    public final static int START_TIME_OPTION = Menu.FIRST + 2;
    public final static int STOP_TIME_OPTION = Menu.FIRST + 3;

    private MyExpandableListAdapter adapter;
    private ExpandableListView elv;

    /**
     * android.app.Activity life cycle method. Creates the graphical components.
     */
    public void onCreate(Bundle b) {
	super.onCreate(b);

	Log.i(TAG, "creating " + getClass());

	LinearLayout panel = new LinearLayout(this);
	panel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	panel.setOrientation(LinearLayout.VERTICAL);

	elv = new ExpandableListView(this);
	adapter = new MyExpandableListAdapter();
	elv.setAdapter(adapter);

	elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener(){
		public boolean onChildClick(ExpandableListView parent, View v, int parentPos, int childPos, long id){
		    return true;
		}
	    });
	registerForContextMenu(elv);
	
	panel.addView(elv);
	panel.setBackgroundColor(Color.parseColor("#FFFFFF"));
	setContentView(panel);
    }


    /**
     * android.app.Activity life cycle method.
     */
    public void onResume()
    {
	Log.d(TAG, "OnResume");
	adapter.reload();
	super.onResume();
    }


    /**
     * Inherited from android.app.Context and called when the context menu is shown.
     */
    public void onCreateContextMenu(ContextMenu menu,
					View v,
					ContextMenu.ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);
	
	ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
	int type = ExpandableListView.getPackedPositionType(info.packedPosition);

	Log.d(TAG, "onCreateContextMenu tyep=" + type);
	
	if(type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
	    //menu.add(0, APPROVE_OPTION, 0, "Godkänna");  //Not implemented, yet.
	}
	else if(type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
	    int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
	    int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
	    
	    TimeSlot timeSlot = ((TimeSlot)adapter.getChild(groupPos, childPos));
	    if(timeSlot.getState() == TimeSlot.State.STOPPED){
		menu.add(0, APPROVE_OPTION, 0, "Godkänna");
		menu.add(0, COMMENT_OPTION, 0, "Kommentera");
		menu.add(0, START_TIME_OPTION, 0, "Ändra starttid");
		menu.add(0, STOP_TIME_OPTION, 0, "Ändra stopptid");
	    } 
	    else if(timeSlot.getState() == TimeSlot.State.APPROVED){
		menu.add("Ta bort");
	    }
	}
	
	menu.setHeaderTitle("Vad vill du göra?");
    }
   
    /**
     * Inherited from android.app.Activity and called when a context menu item has been selected.
     */
    public boolean onContextItemSelected(MenuItem item) {
	ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
	Log.d(TAG, "Childcount=" + ((ExpandableListView)info.targetView.getParent()).getChildCount());

	       
	int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
	ExpandableListView elv = (ExpandableListView)info.targetView.getParent();
	
	int type = ExpandableListView.getPackedPositionType(info.packedPosition);
	if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
	    int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition); 
	    Log.d(TAG, "Slot selected=" + childPos);
	    Log.d(TAG, "Group selected=" + groupPos);
	    TextView currentTimeSlotText = ((TextView)(((LinearLayout)info.targetView).getChildAt(0)));
	    TimeSlot timeSlot = (TimeSlot)(elv.getExpandableListAdapter().getChild(groupPos, childPos));
	    
	    switch(item.getItemId()){
	    case APPROVE_OPTION:
		sendReport(timeSlot, groupPos, currentTimeSlotText);
		break;
	    case COMMENT_OPTION:
		showCommentDialog(timeSlot);
		break;
	    case START_TIME_OPTION:
		showDialog(timeSlot.getId());
		break;
	    case STOP_TIME_OPTION:
		showDialog(timeSlot.getId() * -1);
		break;
	    }
	    return true;
	} else if(type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
	    Log.d(TAG, "Group selected=" + groupPos);
	    return true;
	}
	
	return false;
    }

    /**
     * Comment dialog
     */
    private void showCommentDialog(final TimeSlot timeSlot){
	AlertDialog.Builder alert = new AlertDialog.Builder(this);
	
	alert.setTitle("Kommentar (max 100 tecken)");
	
	// Set an EditText view to get user input 
	final EditText input = new EditText(this);
	alert.setView(input);

	String comment = timeSlot.getComment();
	if(comment == null) comment = "";
	input.setText(comment);

	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		    String value = input.getText().toString();
		    Log.d(TAG, "Comment: " + value);
		    timeSlot.setComment(value);
		    try{
			getHelper().getTimeSlotDao().update(timeSlot);
		    }
		    catch(SQLException x){
			Log.e(TAG, x.getMessage(), x);
		    }
		}
	    });
	
	alert.setNegativeButton("Stäng", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		}
	    });

	alert.show();
    }


    /**
     * From android.app.Activity. Callback when a dialog is created. Called from onContextItemSelected.showDialog
     * and creates the TimePicker.
     */
    public Dialog onCreateDialog(int id){
	int i = id;
	if(i < 0) i *= -1;
	TimeSlot timeSlot = null;
	try{
	    timeSlot = getHelper().getTimeSlotDao().queryForId(i);
	}
	catch(SQLException x){
	    Log.e(TAG, x.getMessage(), x);
	}
	
	Calendar c = Calendar.getInstance();
	if(timeSlot != null){
	    if(i > 0) c.setTime(timeSlot.getStart());
	    else c.setTime(timeSlot.getStop());
	}
	
	int hour = c.get(Calendar.HOUR_OF_DAY);
	int minute = c.get(Calendar.MINUTE);
	return new TimePickerDialog(this, 
				    new MyTimeListener(id),
				    hour, minute, true);
    }


    /**
     * Sends a time report to the server, in a separate thread with help of the inner class 
     * AsyncRemoteReporter.
     */
    private void sendReport(TimeSlot timeSlot, int groupPos, TextView textView){
	Log.d(TAG, "sendReport()");
	PersistentValues pv = new PersistentValues(this);
	try{
	    Project project = getHelper().getProjectDao().queryForId(timeSlot.getProject().getId());
	    Activity activity = getHelper().getActivityDao().queryForId(timeSlot.getActivity().getId());
	    TimeGroup timeGroup = getHelper().getTimeGroupDao().queryForId(timeSlot.getGroup().getId());
	    if(project == null || activity == null){
		Log.e(TAG, "Activity or Project does not exist for TimeSlot: " + timeSlot.toString());
		Toast.makeText(this, "Activitet eller projekt saknas. Kan ej rapportera :-(", 
			       Toast.LENGTH_SHORT).show();
	    }
	    else{
		int pid = project.getId();
		int aid = activity.getId();
		int uid = pv.getUser();
		long start = timeSlot.getStart().getTime()/1000;
		long stop = timeSlot.getStop().getTime()/1000;
		boolean isAutomatic = timeSlot.isAutomatic();
		String comment = timeSlot.getComment();
		AsyncRemoteReporter reporter = new AsyncRemoteReporter(pv.getServer(), timeGroup, 
								       timeSlot, textView, groupPos,
								       pid, aid, uid, start, 
								       stop, isAutomatic, comment);
		ThreadPool.instance().execute(reporter);
	    }
	}
	catch(java.sql.SQLException x){
	    Log.e(TAG, x.getMessage(), x);
	    Toast.makeText(this, "Kan ej repportera :-(. Var god försök senare", 
			   Toast.LENGTH_SHORT).show();
	}
	catch(NumberFormatException x){
	    Log.e(TAG, x.getMessage(), x);
	}
    }

    /**
     * Inner class that is used when the user wants to correct the time of a not yet approved
     * time slot.
     */
    private class MyTimeListener implements TimePickerDialog.OnTimeSetListener{

	private int timeSlotId;
	private boolean isStartTime = true;

	/**
	 * Constructore.
	 */ 
	public MyTimeListener(int timeSlotId){
	    if(timeSlotId < 0){
		isStartTime = false;
		timeSlotId *= -1;
	    }
	    this.timeSlotId = timeSlotId;
	}

	/**
	 * Defined by the OnTimeSetListener interface. Called when when the user has selected either
	 * a new start or stop time for a time slot. 
	 */
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	    try{
		TimeSlot timeSlot = getHelper().getTimeSlotDao().queryForId(timeSlotId);
		TimeGroup timeGroup = getHelper().getTimeGroupDao().queryForId(timeSlot.getGroup().getId());

		if(timeSlot == null) Log.e(TAG, "Inconsistent db. No time group for: " + timeSlot);
		else if(timeGroup == null) Log.e(TAG, "Inconsistent db. No time group for: " + timeSlot);
		else {
		    Calendar c = Calendar.getInstance();
		    c.setTime(timeSlot.getStart());
		    c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		    c.set(Calendar.MINUTE, minute);
		    c.set(Calendar.SECOND, 0);
		    Date date = c.getTime();
		    if(isStartTime){
			TimeSlot timeBefore = getHelper().getTimeSlotDao().queryForId(timeSlotId-1);
			if((timeBefore != null && timeBefore.getStop().after(date)) || timeSlot.getStop().before(date))
			    Toast.makeText(ReportActivity.this, "Ogiltligt starttid", Toast.LENGTH_SHORT).show();
			else
			    updateTime(timeSlot, timeGroup, date, true);
		    }
		    else {
			TimeSlot timeAfter = getHelper().getTimeSlotDao().queryForId(timeSlotId+1);
			if((timeAfter != null && timeAfter.getStart().before(date)) || timeSlot.getStart().after(date) ||
			   date.after(Calendar.getInstance().getTime()))
			    Toast.makeText(ReportActivity.this, "Ogiltligt sluttid", Toast.LENGTH_SHORT).show();
			else
			    updateTime(timeSlot, timeGroup, date, false);
		    }
		}
		

		adapter.reload();

		Log.d(TAG, "TimeSlot:" + timeSlot.getStart()); 
		Log.d(TAG, "New time=" + hourOfDay + ":" + minute);
	    }
	    catch(SQLException x){
		Log.e(TAG, x.getMessage(), x);
	    }
	}

	/**
	 * Updates the time slot in the database.
	 */
	private void updateTime(TimeSlot timeSlot, TimeGroup timeGroup, Date date, boolean isStartTime) throws SQLException{
	    Log.d(TAG, "Old timeSlot: " + timeSlot);
	    int oldTotal = timeSlot.getTotal();
	    if(isStartTime) timeSlot.setStart(date);
	    else timeSlot.setStop(date);
	    timeGroup.subTotal(oldTotal);
	    timeGroup.addTotal(timeSlot.getTotal());
	    getHelper().getTimeSlotDao().update(timeSlot);
	    getHelper().getTimeGroupDao().update(timeGroup);
	    Log.d(TAG, "Updated timeSlot: " + timeSlot);
	    Log.d(TAG, "Updated timeGroup: " + timeGroup);

	}

    }
	
    /**
     * Class that do the actual sending of a time report. Implements Runnable so that it can be done in a 
     * separate thread.
     */
    public class AsyncRemoteReporter implements Runnable {
	private String comment, uri;
	private int group;
	private boolean isAutomatic;
	private TimeGroup timeGroup;
	private TimeSlot timeSlot;
	private TextView textView;
	private int project, activity, user;
	private long start, stop;
	
	public AsyncRemoteReporter(String uri, TimeGroup timeGroup, TimeSlot timeSlot, 
				   TextView textView, int group, int proj, int act,
				   int user, long start, long stop, boolean isAutomatic,
				   String comment){
	    this.uri = uri;
	    this.timeGroup = timeGroup;
	    this.timeSlot = timeSlot;
	    this.textView = textView;
	    this.group = group;
	    this.project = proj;
	    this.activity = act;
	    this.user = user;
	    this.start = start;
	    this.stop = stop;
	    this.isAutomatic = isAutomatic;
	    this.comment = comment;
	}
	
	public void run(){
	    Log.d(TAG, "Actual send of time report");
	    Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(ReportActivity.this));
	    
	    try{
		JsonClient jsonClient = new JsonClient();
		jsonClient.sendTimeReport(uri, project, activity, user, 
					  start, stop, isAutomatic, comment);
		timeSlot.approve();
		timeGroup.approve();
		getHelper().getTimeSlotDao().update(timeSlot);
		getHelper().getTimeGroupDao().update(timeGroup);
		if(textView != null){
		    new Thread(new Runnable() {
			    public void run() {
				textView.post(new Runnable() {
					public void run() {
					    textView.setTextColor(Color.parseColor("#3DD84B"));
					    if(timeGroup.isApproved())
						elv.collapseGroup(group);
					}
				    });
			    }
			}).start();
		}
	    }
	    catch(Exception x){ //SqlException or if post request fails
		Log.e(TAG, x.getMessage(), x);
		//Toast.makeText(ReportActivity.this, "Kan ej repportera :-(. Serverproblem. Var god försök senare.", 
		//       Toast.LENGTH_SHORT).show();
	    }
	}
    }


    /**
     * Inner class that controls the data of the list of time reports. Each group represents a day, and the children
     * of the groups are timeslots. Most methods overrides methods in BaseExpandableListAdapter and 
     * ExpandableListAdapter.
     */
    public class MyExpandableListAdapter extends BaseExpandableListAdapter {
	
	List<TimeGroup> timeGroups;
	List<TimeSlot> timeSlots;
	int currentGroup = -1;
	//boolean toBeReloaded = false;
	
	public MyExpandableListAdapter(){
	    reload();
	}
	
	/**
	 * Loads data from the database to memory.
	 */
	void reload(){
	    Log.d(TAG, "reload()");
	    try{
		// Only show entries for last 30 days.
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -30);
		int dayCode = TimeGroup.getDayCode(c.getTime());
		Log.d(TAG, "Daycode=" + dayCode);
		timeGroups =
		    getHelper().getTimeGroupDao().queryBuilder().where().gt(TimeGroup.ID_FIELD_NAME, 
									    dayCode).query();
		timeSlots = new ArrayList<TimeSlot>();
	    }
	    catch(SQLException x){
		Log.e(TAG, x.getMessage(), x);
	    }
	    currentGroup = -1;
	    notifyDataSetChanged();
	}


	public Object getChild(int group, int child) {
	    return timeGroups.get(group).getSlot(child);
	}
	
	public long getChildId(int parentPosition, int childPosition) {
	    return childPosition;
	}
	
	public int getChildrenCount(int pos) {
	    int childCount = timeGroups.get(pos).getSlotCount(); 
	    Log.d(TAG, "getChildCount() = " + childCount);
	    return childCount;
	}
	
	public TextView getGenericView() {
	    
	    AbsListView.LayoutParams lp = 
		new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);						      
	    
	    TextView textView = new TextView(ReportActivity.this);
	    textView.setLayoutParams(lp);
	    textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
	    textView.setTextColor(Color.parseColor("#31A6D2"));
	    // Set the text starting position
	    textView.setPadding(36, 0, 0, 0);
	    textView.setTextSize(20);
	    return textView;
	}
	
	public View getGroupView(int pos, boolean isExpanded, View converView, ViewGroup parent){
	    
	    TextView textView = getGenericView();
	    TimeGroup group = timeGroups.get(pos);
	    Log.d(TAG, "TimeGroup:" + group);
	    textView.setTextColor(group.isApproved()? 
	    			  Color.parseColor("#3DD84B") :
				  Color.parseColor("#31A6D2")); 
	    
	    String txt = "";
	    try{
		int total = group.getTotal() / 1000;
		Log.d(TAG, "Total=" + total);
		int hours = total / 3600;
		total %= 3600;
		int minutes = total / 60;
		int seconds = total % 60;
		StringBuffer sb = new StringBuffer(timeGroups.get(pos).getAsDate());
		sb.append("       ");
		if(hours < 10) sb.append("0"); 
		sb.append(hours);
		sb.append(":");
		if(minutes < 10) sb.append("0");
		sb.append(minutes);
		sb.append(":");
		if(seconds < 10) sb.append("0");
		sb.append(seconds);
		txt = sb.toString();
	    }
	    catch(NumberFormatException x){
		Log.e(TAG, x.getMessage(), x);
	    }
	    textView.setText(txt);
	    
	    return textView;
	}
	
	public synchronized View getChildView(int groupPos, 
					      int childPos, boolean isLastChild,
					      View convertView, ViewGroup parent) {
	    	    	    
	    if(currentGroup != groupPos){
		currentGroup = groupPos;
		timeSlots.clear();
		timeSlots.addAll(timeGroups.get(groupPos).getSlots());
	    }
	    
	    View view;
	    
	    try{
		LinearLayout panel = new LinearLayout(ReportActivity.this);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		panel.setOrientation(LinearLayout.VERTICAL);
		panel.setGravity(Gravity.CENTER);
		
		TimeSlot timeSlot = timeSlots.get(childPos);
		TextView t1 = new TextView(ReportActivity.this);
		t1.setTextSize(16);
		t1.setText(timeSlot.getAsInterval());
		t1.setTextColor(timeSlot.getState() == TimeSlot.State.APPROVED? 
				Color.parseColor("#3DD84B") :
				Color.parseColor("#31A6D2")); 
		t1.setTypeface(Typeface.DEFAULT_BOLD);		
		panel.addView(t1, params);
		
		Project project = getHelper().getProjectDao().queryForId(timeSlot.getProject().getId());
		if(project != null){
		    TextView t2 = new TextView(ReportActivity.this);
		    t2.setTextSize(14);
		    t2.setText(project.getName());
		    t2.setTextColor(Color.GRAY);
		    panel.addView(t2, params);
		}
		
		Activity activity = getHelper().getActivityDao().queryForId(timeSlot.getActivity().getId());		
		if(activity != null){
		    TextView t3 = new TextView(ReportActivity.this);
		    t3.setTextSize(14);
		    t3.setText(activity.getName());
		    t3.setTextColor(Color.GRAY);
		    panel.addView(t3, params);
		}
		view = panel;
	    }
	    catch(SQLException x){
		Log.e(TAG, x.getMessage(), x);
		view = new TextView(ReportActivity.this);
	    }
	    
	    return view;
	}
	
	public Object getGroup(int group) {
	    return timeGroups.get(group);
	}
	
	public int getGroupCount() {
	    Log.d(TAG, "getGroupCount() size=" + timeGroups.size()); 
	    return timeGroups == null? 0 : timeGroups.size();
	}
	
	public long getGroupId(int group) {
	    Log.d(TAG, "getGroupId() group=" + group);
	    return group;
	}
	
	public boolean isChildSelectable(int groupPosition, int childPosition) {
	    return true;
	}
	
	public boolean hasStableIds() {
	    return true;
	}
	
    }
	   
}