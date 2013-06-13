package com.cc.cg;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.util.Log;

import com.cc.cg.database.*;
import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

public class SimpleListActivity extends OrmLiteBaseListActivity<DatabaseHelper> {

    private final static String TAG = "SimpleListActivity";

    public void onCreate(Bundle b){
	super.onCreate(b);

	getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, 
			     WindowManager.LayoutParams.FLAG_BLUR_BEHIND); 

	try{
	    int type = getIntent().getIntExtra("form.type", -1);
	    setListAdapter(type == Constants.SIMPLE_PROJECT_TYPE? 
			   new MyProjectAdapter() : new MyActivityAdapter());
	}
	catch(SQLException x){
	    Log.e(TAG, x.getMessage(), x);
	    finish();
	}
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
	Project project = (Project)l.getItemAtPosition(position);
	Log.d(TAG, project.toFullString());
	Intent intent = new Intent();
	intent.putExtra(Constants.PARAM_LATITUDE, project.getLatitude());
	intent.putExtra(Constants.PARAM_LONGITUDE, project.getLongitude());
	setResult(RESULT_OK, intent);	
	finish();
    }

    public class MyProjectAdapter extends BaseAdapter {
	private List<Project> projects;

	MyProjectAdapter() throws SQLException {
   		Dao<Project, Integer> projectDao = getHelper().getProjectDao();
		projects = projectDao.queryForAll();
	}

	public int getCount() { return projects.size(); }
	public Object getItem(int position) { return projects.get(position); }
	public long getItemId(int position) { return position; }

	public View getView(int position, View convertView, ViewGroup parent) {
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 64);
            TextView textView = new TextView(SimpleListActivity.this);
            textView.setLayoutParams(lp);
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
	    textView.setTextColor(Color.BLUE);
            textView.setPadding(36, 0, 0, 0);
	    textView.setTextSize(20);
            textView.setText(projects.get(position).toString());
 	    return textView;
	}
    }

    public class MyActivityAdapter extends BaseAdapter {
	private List<Activity> activities;

	MyActivityAdapter() throws SQLException {
   		Dao<Activity, Integer> activityDao = getHelper().getActivityDao();
		activities = activityDao.queryForAll();
	}

	public int getCount() { return activities.size(); }
	public Object getItem(int position) { return activities.get(position); }
	public long getItemId(int position) { return position; }

	public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(SimpleListActivity.this);
	    textView.setTextColor(Color.CYAN);
            textView.setText(activities.get(position).toString());
 	    return textView;
	}
    }

}