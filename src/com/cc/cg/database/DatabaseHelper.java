package com.cc.cg.database;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
/**
 *
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "autotid.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Project, Integer> projectDao = null;
    private Dao<Activity, Integer> activityDao = null;
    private Dao<WorkPlan, Integer> workPlanDao = null;
    private Dao<TimeSlot, Integer> timeSlotDao = null;
    private Dao<TimeGroup, Integer> timeGroupDao = null;
    private Dao<PositionReport, Integer> positionReportDao = null;

    public DatabaseHelper(Context context) {
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
	try{
	    Log.d(TAG, "onCreate");
	    TableUtils.createTable(connectionSource, Project.class);
	    TableUtils.createTable(connectionSource, Activity.class);
	    TableUtils.createTable(connectionSource, WorkPlan.class);
	    TableUtils.createTable(connectionSource, TimeSlot.class);
	    TableUtils.createTable(connectionSource, TimeGroup.class);
	    //TableUtils.createTable(connectionSource, PositionReport.class);
	    
	    Dao<Project, Integer> projectDao = getProjectDao();
	    Dao<Activity, Integer> activityDao = getActivityDao();
	    Dao<WorkPlan, Integer> workPlanDao = getWorkPlanDao();
	    Dao<TimeSlot, Integer> timeSlotDao = getTimeSlotDao();
	    Dao<TimeGroup, Integer> timeGroupDao = getTimeGroupDao();
	    Dao<PositionReport, Integer> positionReportDao = getPositionReportDao();
	    
	    Log.d(TAG, "created new entries in onCreate");
	} 
	catch (SQLException e) {
	    Log.e(TAG, "Can't create database", e);
	    throw new RuntimeException(e);
	}
    }
    
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
	try {
	    Log.d(TAG, "onUpgrade");
	    TableUtils.dropTable(connectionSource, Project.class, true);
	    TableUtils.dropTable(connectionSource, Activity.class, true);
	    TableUtils.dropTable(connectionSource, WorkPlan.class, true);
	    TableUtils.dropTable(connectionSource, TimeGroup.class, true);
	    TableUtils.dropTable(connectionSource, TimeSlot.class, true);
	    //TableUtils.dropTable(connectionSource, PositionReport.class, true);

	    onCreate(db, connectionSource);
	} 
	catch (SQLException e) {
	    Log.e(TAG, "Can't drop databases", e);
	    throw new RuntimeException(e);
	}
    }

    public Dao<Project, Integer> getProjectDao() throws SQLException {
	if (projectDao == null) {
	    projectDao = getDao(Project.class);
	}
	return projectDao;
    }

    public Dao<Activity, Integer> getActivityDao() throws SQLException {
	if(activityDao == null) {
	    activityDao = getDao(Activity.class);
	}
	return activityDao;
    }

    public Dao<WorkPlan, Integer> getWorkPlanDao() throws SQLException {
	if(workPlanDao == null) {
	    workPlanDao = getDao(WorkPlan.class);
	}
	return workPlanDao;
    }

    public Dao<TimeSlot, Integer> getTimeSlotDao() throws SQLException {
	if(timeSlotDao == null) {
	    timeSlotDao = getDao(TimeSlot.class);
	}
	return timeSlotDao;
    }

    public Dao<TimeGroup, Integer> getTimeGroupDao() throws SQLException {
	if(timeGroupDao == null) {
	    timeGroupDao = getDao(TimeGroup.class);
	}
	return timeGroupDao;
    }

    public Dao<PositionReport, Integer> getPositionReportDao() throws SQLException {
	if(positionReportDao == null) {
	    positionReportDao = getDao(PositionReport.class);
	}
	return positionReportDao;
    }

	public void close() {
	super.close();
	projectDao = null;
	activityDao = null;
	workPlanDao = null;
	timeSlotDao = null;
	timeGroupDao = null;
	positionReportDao = null;
    }
}