package com.cc.cg.database;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import com.j256.ormlite.field.DatabaseField;

import android.util.Log;


public class TimeSlot {

    public static final String PROJECT_FIELD_NAME = "project";
    public static final String ACTIVITY_FIELD_NAME = "activity";
    public static final String GROUP_FIELD_NAME = "group";
    public static final String START_FIELD_NAME = "start"; 
    public static final String STOP_FIELD_NAME = "stop"; 
    public static final String IS_AUTOMATIC_FIELD_NAME = "isautomatic"; 
    public static final String COMMENT_FIELD_NAME = "comment"; 
    public static final String TOTAL_FIELD_NAME = "total"; 
    public static final String STATE_FIELD_NAME = "state"; 

    public enum State {
	INIT, STARTED, STOPPED, APPROVED
    }

    private static DateFormat dfHour = new SimpleDateFormat("HH:mm:ss");
    private static final String TAG = "TimeSlot";

    @DatabaseField(generatedId = true)
	private int id;
    @DatabaseField(canBeNull = false, foreign = true, columnName = PROJECT_FIELD_NAME)
	private Project project;
    @DatabaseField(canBeNull = false, foreign = true, columnName = ACTIVITY_FIELD_NAME)
	private Activity activity;
    @DatabaseField(columnName = START_FIELD_NAME)
	private Date start;
    @DatabaseField(columnName = STOP_FIELD_NAME)
        private Date stop;
    @DatabaseField(columnName = IS_AUTOMATIC_FIELD_NAME)
        private boolean isAutomatic;
    @DatabaseField(columnName = COMMENT_FIELD_NAME, defaultValue = "")
        private String comment;
    @DatabaseField(defaultValue = "0", columnName = TOTAL_FIELD_NAME)
        private int total;
    @DatabaseField(defaultValue = "INIT")
        private State state;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = GROUP_FIELD_NAME)
	private TimeGroup timeGroup;
    
    public TimeSlot() {
	state = State.INIT;
    }

    public TimeSlot(TimeGroup timeGroup) {
	state = State.INIT;
	this.timeGroup = timeGroup;
    }

    public int getId(){ return id; }

    public Date getStart(){ return start; }
    public void setStart(Date d){
	if(state == State.INIT)
	    state = State.STARTED;
	start = d;
	if(stop != null) 
	    total = (int)(stop.getTime() - start.getTime());
    }
    public Date getStop(){ return stop; }

    private void checkStop(){
	Calendar cal = Calendar.getInstance();
	cal.setTime(stop);
	int today = cal.get(Calendar.DAY_OF_YEAR);
	cal.setTime(start);
	// A timeslot is indexed per day and can thus
	// not span over midnight.
	Log.d(TAG, "Start and stop different days");
	Log.d(TAG, start + "---" + stop);
	if(today != cal.get(Calendar.DAY_OF_YEAR)){
	    cal.set(Calendar.HOUR_OF_DAY, 23);
	    cal.set(Calendar.MINUTE, 59);
	    cal.set(Calendar.SECOND, 59);
	    stop = cal.getTime();
	    Log.d(TAG, "New stop time: " + stop);
	}
    }

    public void setStop(){
	state = State.STOPPED;
	stop = new Date();
	checkStop();
	total = (int)(stop.getTime() - start.getTime());
	Log.d(TAG, "Total set to: " + total);
    }
    
    public void setStop(Date d){
	state = State.STOPPED;
	stop = d;
	checkStop();
	total = (int)(stop.getTime() - start.getTime());
	Log.d(TAG, "Total set to: " + total);
    }
   

    public void reOpen(){
	state = State.STARTED;
	stop = null;
	total = 0;
    }

    public int getTotal(){
	if(state == State.STOPPED || state == State.APPROVED) 
	    return total;
	else if(state == State.STARTED)
	    return (int)(new Date().getTime() - start.getTime());
	else 
	    return 0;
    }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity a){activity = a;}

    public Project getProject() { return project; }
    public void setProject(Project p){project = p;}

    public boolean isAutomatic() { return isAutomatic; }
    public void isAutomatic(boolean b){isAutomatic = b;}

    public String getComment() { return comment; }
    public void setComment(String c){comment = c;}

    public State getState() { return state; }
    public void approve(){ state = State.APPROVED; }

    public TimeGroup getGroup() { return timeGroup; }

    public String getAsInterval() {
	StringBuffer sb = new StringBuffer(dfHour.format(start));
	sb.append(" - ");
	if(stop != null)
	    sb.append(dfHour.format(stop));
	return sb.toString();
    }

    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("id=").append(id);
	if(start != null)
	    sb.append(", ").append("start=").append(start);
	if(stop != null)
	    sb.append(", ").append("stop=").append(stop);
	sb.append(", ").append("state=").append(state);
	if(timeGroup != null)
	    sb.append(", ").append("group=").append(timeGroup.getId());
	sb.append(", ").append("total=").append(total);
	return sb.toString();
    }
}
