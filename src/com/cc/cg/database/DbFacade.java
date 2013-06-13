package com.cc.cg.database;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;

import android.content.Context;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.sql.SQLException;

import com.cc.cg.PersistentValues;

/**
 * Simple database operations are done directly where needed in the code. More complex database 
 * operations and/or database operations that done at several places in the code are however
 * centralised to this class.
 */
public class DbFacade {

    private static final String TAG = "DbFacade";
    private DatabaseHelper _helper;
    private PersistentValues _pv;

    public DbFacade(Context context){
	OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);
	_helper = (DatabaseHelper)OpenHelperManager.getHelper(context);
	_pv = new PersistentValues(context);
    }

    protected void finalize() throws Throwable {
	try {
	    OpenHelperManager.release();
	} finally {
	    super.finalize();
	}
    }

    /**
     * @return timeslot currently open, if any; maximum one timeslot can be open here
     */
    public TimeSlot getCurrentTimeSlot(){
	TimeSlot slot = null;
	try{
	    Dao<TimeSlot, Integer> timeSlotDao = _helper.getTimeSlotDao();
	    List<TimeSlot> list = _helper.getTimeSlotDao().queryBuilder()
		.orderBy(TimeSlot.STOP_FIELD_NAME, true).where()
		.eq(TimeSlot.STATE_FIELD_NAME, TimeSlot.State.STARTED)
		.query();
	    if(list.size() > 1) 
		Log.wtf(TAG, "Number of open timeslots more that 1! No=" + list.size());

	    if(list.size() > 0) slot = list.get(list.size()-1);
	}
	catch(SQLException x){
	    Log.e(TAG, x.getMessage(), x);
	}

	return slot;
    }

    /**
     * True if any timeslot is currently in an open state.
     */
    public boolean isReporting(){
	return getCurrentTimeSlot() != null;
    }


    /**
     * If a project/activity is opened two times with a short time interval, only 
     * one timeslot shall be created in the database. This method finds out if 
     * such a timeslot exists and re-opens it in that case. 
     * @return True - if a recent timeslot was opened.
     */
    private boolean reOpenRecentTimeSlot(int projectId, int activityId) throws SQLException {
	Log.d(TAG, "reOpenRecentTimeSlot, p=" + projectId + "a=" + activityId);
	boolean isTimeSlotReOpened = false;

	long recent = java.util.Calendar.getInstance().getTime().getTime() - 
	    (com.cc.cg.Constants.REOPEN_DELTA  * 1000); 
	Date recentDate = new Date(recent);
	List<TimeSlot> results =
	    _helper.getTimeSlotDao().queryBuilder()
	    .orderBy(TimeSlot.STOP_FIELD_NAME, true).where()
	    .gt(TimeSlot.STOP_FIELD_NAME, recentDate).and()
	    .eq(TimeSlot.STATE_FIELD_NAME, TimeSlot.State.STOPPED)
	    .query();
	if(results.size() > 0){	   
	    TimeSlot timeSlot = results.get(results.size()-1); // Only consider the most recent
	    Log.d(TAG, "Recent time slots found. No entries=" + results.size() + "," + timeSlot);
	    if(projectId == timeSlot.getProject().getId() && 
	       activityId == timeSlot.getActivity().getId() &&
	       TimeGroup.getDayCodeForToday() == timeSlot.getGroup().getId()){

		Log.d(TAG, "Yes, same project and activity --> to be reopened");
	    
		TimeGroup timeGroup = timeSlot.getGroup();
		timeGroup.subTotal(timeSlot.getTotal());
		timeSlot.reOpen();
		_helper.getTimeGroupDao().update(timeGroup);
		_helper.getTimeSlotDao().update(timeSlot);
		isTimeSlotReOpened = true;
	    }
	}

	return isTimeSlotReOpened;
    }


    /**
     * Opens a new timeslot in the database, or re-opens a recent one if such an entry
     * exists for the project/activity.
     * @return True  - if a new TimeSlot was created
     *         False - a recent TimeSlot was re-opened
     */
    public boolean registerWorkStart(int projectId, int activityId, boolean isAutomatic){
	Log.d(TAG, "registerWorkStart project=" + projectId + " activity=" + activityId +
	      " isautomatic=" + isAutomatic);

	registerWorkStop(isAutomatic); // Make sure that all TimeSlots are closed
	boolean isWorkStartRegistered = false;
        try{
	    isWorkStartRegistered = reOpenRecentTimeSlot(projectId, activityId);
	    if(!isWorkStartRegistered) {
		Project project = _helper.getProjectDao().queryForId(projectId);
		Activity activity = _helper.getActivityDao().queryForId(activityId);
		Date today = new Date();
		if(project != null && activity != null){
		    int day = TimeGroup.getDayCode(today);
		    TimeGroup timeGroup = _helper.getTimeGroupDao().queryForId(day);
		    if(timeGroup == null){
			Log.d(TAG, "No TimeGroup found for today");
			timeGroup = new TimeGroup(today);
			_helper.getTimeGroupDao().create(timeGroup);
			Log.d(TAG, "Created new time group");
		    }
		    TimeSlot time = new TimeSlot(timeGroup);
		    time.setProject(project);
		    time.setActivity(activity);
		    time.setStart(new Date());
		    time.isAutomatic(isAutomatic);
		    _helper.getTimeSlotDao().create(time);
		    int id = _helper.getTimeSlotDao().extractId(time);
		    isWorkStartRegistered = true;
		    Log.d(TAG, "New id=" + id);
		}
            }
	}
	catch(SQLException x){
	    Log.e(TAG, x.getMessage(), x);
	}

	return isWorkStartRegistered;
    }


    /**
     * Closes currently open TimeSlot(s).
     */
    public void registerWorkStop(boolean isAutomatic) {
	Log.d(TAG, "registerWorkStop");

	try{
	    Dao<TimeSlot, Integer> timeSlotDao = _helper.getTimeSlotDao();
	    List<TimeSlot> list = _helper.getTimeSlotDao().queryForEq(TimeSlot.STATE_FIELD_NAME,                                                                                                                     
								     TimeSlot.State.STARTED);

	    if(list.size() > 1)
		Log.wtf(TAG, "!!!More than one time slot is open!!! No=" + list.size());


	    if(list.size() == 0) Log.d(TAG, "No timeslot to close");
	    else{
		Calendar today = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		cal.setTime(list.get(0).getStart());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		// Timeslots are indexed per day, so if we have a long time registration spanning
		// over one or several days, we create one timeslot per day.
		while(cal.get(Calendar.YEAR) < today.get(Calendar.YEAR) || 
		      cal.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR)){
		    cal.roll(Calendar.DAY_OF_YEAR, true);
		    Log.d(TAG, "Yes, midnight passed once. New date=" + cal.getTime());
		    TimeGroup timeGroup = 
			_helper.getTimeGroupDao().queryForId(TimeGroup.getDayCode(cal.getTime()));
		    if(timeGroup == null){
			timeGroup = new TimeGroup(cal.getTime());
			_helper.getTimeGroupDao().create(timeGroup);
		    }
		    TimeSlot timeSlot = new TimeSlot(timeGroup);
		    timeSlot.setStart(cal.getTime());
		    timeSlot.setProject(list.get(0).getProject());
		    timeSlot.setActivity(list.get(0).getActivity());
		    if(timeSlot.isAutomatic() && isAutomatic) 
			timeSlot.isAutomatic(true);
		    else 
			timeSlot.isAutomatic(false);
		    _helper.getTimeSlotDao().create(timeSlot);
		    list.add(timeSlot);
		    Log.d(TAG, "One new timeslot created: " + timeSlot);
		}

		// Now close the timeslot(s) including the ones we just opened for the previous 
		// day(s).
		for(TimeSlot timeSlot : list){
		    timeSlot.setStop();
		    Log.d(TAG, "Stopping " + timeSlot.toString());
		    TimeGroup timeGroup = timeSlot.getGroup();
		    timeGroup.addTotal(timeSlot.getTotal());
		    _helper.getTimeSlotDao().update(timeSlot);
		    _helper.getTimeGroupDao().update(timeGroup);
		}
	    }

	}
	catch(SQLException x) { 
	    Log.e(TAG, x.getMessage(), x); 
	}

    }

}