package com.cc.cg;

import com.cc.cg.TimeActivity;
import com.cc.cg.database.*;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;


import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import com.jayway.android.robotium.solo.Solo;

import java.util.Date;
import java.io.*;

public class TimeActivityTest extends ActivityInstrumentationTestCase2<TimeActivity> {

    TimeActivity _activity;
    Button _button;
    CheckBox _checkbox;
    DatabaseHelper _helper;
    Solo _solo;

    private final static String TAG = "TimeActivityTest";

    public TimeActivityTest() {
        super("com.cc.cg", TimeActivity.class);
    }
    @Override
    protected void setUp() throws Exception {

        super.setUp();

        setActivityInitialTouchMode(false);

	_activity = getActivity();
	_button = (Button)_activity.findViewById(com.cc.cg.R.id.reportButton);
	_checkbox = (CheckBox)_activity.findViewById(com.cc.cg.R.id.checkbox);
	_solo = new Solo(getInstrumentation(), getActivity());
	OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);
        _helper = (DatabaseHelper)OpenHelperManager.getHelper(getActivity());
    }

    
    private void prepare(){
	if(_checkbox.isChecked())
	    _checkbox.post(new CheckBoxClicker());
	if(_button.getText().equals("Stopp"))
	    _button.post(new ButtonClicker());
    }

    
    public void _testButtonLabels() {
	prepare();
	sleep(1);
	assertTrue(_button.getText().equals("Start"));
	_button.post(new ButtonClicker());
	sleep(1);
	assertTrue(_button.getText().equals("Stopp"));
	_button.post(new ButtonClicker());
	sleep(1);
	assertTrue(_button.getText().equals("Start"));
    }

    public void _testThatTimeGroupIsUpdated() {
	try{
	    prepare();
	    sleep(1);
	    TimeGroup timeGroup = _helper.getTimeGroupDao().queryForId(TimeGroup.getDayCode(new Date()));
	    int totalSlotsBefore = getActivity().getWorkTotalForToday();
	    int totalGroupBefore = timeGroup.getTotal();

	    _button.post(new ButtonClicker());
	    sleep(12);
	    _button.post(new ButtonClicker());
	    sleep(1);

	    timeGroup = _helper.getTimeGroupDao().queryForId(TimeGroup.getDayCode(new Date()));
	    int totalSlotsAfter = getActivity().getWorkTotalForToday();
	    int totalGroupAfter = timeGroup.getTotal();
	    int totalSlots = totalSlotsAfter-totalSlotsBefore;
	    int totalGroup = totalGroupAfter-totalGroupBefore;

	    Log.d(TAG, "TotalSlots=" + totalSlots);
	    Log.d(TAG, "TotalGroup=" + totalGroup);
	    assertTrue(totalSlots > 11000);
	    assertTrue(totalSlots == totalGroup);
	}
	catch(Exception x){
	    assertTrue(true);
	}
    }

    private void sleep(int seconds){
	try{
	    Thread.sleep(seconds*1000);
	}
	catch(InterruptedException x){}
    }

    private class ButtonClicker implements Runnable {
	public void run(){
	    _button.performClick();
	}
    }

    private class CheckBoxClicker implements Runnable {
	public void run(){
	    _checkbox.performClick();
	}
    }
}
