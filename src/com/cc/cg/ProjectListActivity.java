package com.cc.cg;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.cc.cg.database.*;
import com.cc.cg.json.*;
import com.google.android.maps.GeoPoint;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.*;
import java.util.*;


public class ProjectListActivity extends OrmLiteBaseActivity<DatabaseHelper> implements EventDispatcher.ProjectListener {

    private final static String TAG = "ProjectListActivity";

    public final static int SELECT_ACTIVITY_OPTION = Menu.FIRST;
    public final static int SELECT_PROJECT_OPTION = Menu.FIRST + 1;
    public final static int INFO_OPTION = Menu.FIRST + 2;
    public final static int ADDRESS_OPTION = Menu.FIRST + 3;

    MyExpandableListAdapter adapter;

    public void onCreate(Bundle b) {
	super.onCreate(b);

	Log.i(TAG, "creating " + getClass());

	try{
	    List<Project> projects = 
		getHelper().getProjectDao().queryForAll();
	    for(Project p : projects) Log.d(TAG, p.toFullString());	    
	}
	catch(SQLException x){}

	try{
	    Dao<TimeSlot, Integer> timeSlotDao = getHelper().getTimeSlotDao();
	    List<TimeSlot> timeSlots = timeSlotDao.queryForAll();
	    for(TimeSlot t : timeSlots) Log.d(TAG, t.toString());	    
	}
	catch(SQLException x){}

	LinearLayout panel = new LinearLayout(this);//TransparentPanel(this);
	panel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	panel.setOrientation(LinearLayout.VERTICAL);
	panel.setBackgroundColor(Color.parseColor("#FFFFFF"));

	ExpandableListView elv = new ExpandableListView(this);
	adapter = new MyExpandableListAdapter();
	elv.setAdapter(adapter);
	
	/*
	final PersistentValues pv = new PersistentValues(this);
	elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener(){
		public boolean onChildClick(ExpandableListView parent, View v, int parentPos, int childPos, long id){
		    
		    int oldActivity = pv.getCurrentActivity();
		    int oldProject = pv.getCurrentProject();
		    int newActivity = ((com.cc.cg.database.Activity)adapter.getChild(parentPos, childPos)).getId();
		    int newProject = ((com.cc.cg.database.Project)adapter.getGroup(parentPos)).getId();
		    if(oldActivity == newActivity && oldProject == newProject)
			setResult(RESULT_CANCELED);
		    else{
			pv.setCurrentProject(newProject);
			pv.setCurrentActivity(newActivity);
			setResult(Constants.DATA_MODIFIED);
		    }
		    return true;
		}
	    });
	*/
	registerForContextMenu(elv);
	panel.addView(elv);
	setContentView(panel);
    }

    /**
     * The activity's onResume life cycle method.
     */
    public void onResume(){
	Log.d(TAG, "OnResume");
	super.onStop();
	adapter.reload();
	EventDispatcher.instance().registerProjectListener(this);
    }

    /**
     * The activity's onStop life cycle method.
     */
    public void onPause()
    {
	Log.d(TAG, "OnPause");
	super.onStop();
	adapter.reload();
	EventDispatcher.instance().unregisterProjectListener(this);
    }

    private void setAsActive(int project, int activity){
	Log.d(TAG, "Set as active project=" + project + ", activity=" + activity);
    }

    private Location getCurrentLocation(){
	Location location = SessionContext.instance().getLastKnownLocation();
	if(location == null){
	    Toast.makeText(ProjectListActivity.this, 
			   "Lyckades ej läsa position. Försök igen senare", 
			   Toast.LENGTH_LONG).show();
	}
	return location;
    }


	   public void onCreateContextMenu(ContextMenu menu,
					   View v,
					   ContextMenu.ContextMenuInfo menuInfo) {
	       super.onCreateContextMenu(menu, v, menuInfo);

	       ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
	       int type = ExpandableListView.getPackedPositionType(info.packedPosition);

	       Log.d(TAG, "onCreateContextMenu type=" + type);
  
	       if(type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
		   menu.add(0, ADDRESS_OPTION, 0, "Se adress");
		   menu.add(0, INFO_OPTION, 0, "Se information");
		   menu.add(0, SELECT_PROJECT_OPTION, 0, "Välj som aktiv");
	       }
	       else if(type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
		   menu.add(0, SELECT_ACTIVITY_OPTION, 0, "Välj som aktiv");
	       }
	       menu.setHeaderTitle("Vad vill du göra?");
	   }

	   public boolean onContextItemSelected(MenuItem item) {
	       Log.d(TAG, "onContextItemSelected: " + item.getItemId());
	       final ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
	       final int type = ExpandableListView.getPackedPositionType(info.packedPosition);
	       final PersistentValues pv = new PersistentValues(ProjectListActivity.this);
	       final DbFacade db = new DbFacade(this);
	       final int selection = item.getItemId();
	       if(selection == SELECT_ACTIVITY_OPTION || selection == SELECT_PROJECT_OPTION){
		   if(db.isReporting()){
		       Toast.makeText(this, "Kan inte byta projekt under pågående rapportering", 
				      Toast.LENGTH_LONG).show();
		   }
		   else{
		       int parentPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		       int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		       int project = ((Project)adapter.getGroup(parentPos)).getId();
		       pv.setCurrentProject(project);
		       if(selection == SELECT_ACTIVITY_OPTION){
			   int activity = ((Activity)adapter.getChild(parentPos, childPos)).getId();
			   pv.setCurrentActivity(activity);
		       }
		   }
	       }
	       else if(item.getItemId() == ADDRESS_OPTION){
		   int pos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		   Project project = adapter.getProject(pos);
		   AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		   alertDialog.setTitle("Address för " + project.getName());
		   alertDialog.setMessage(project.getAddress() + "\n" + project.getPostCode());
		   alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, 
					 "Ok", 
					 new DialogInterface.OnClickListener() {
					     public void onClick(DialogInterface dialog, int which) {
						 return;
					  }});
		   alertDialog.show();
	       }
	       else if(item.getItemId() == INFO_OPTION){
		   int pos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		   Project project = adapter.getProject(pos);
		   Log.d(TAG, "Info option project:" + project);
		   AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		   alertDialog.setTitle("Information om " + project.getName());
		   alertDialog.setMessage(project.getInfo());
		   alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, 
					 "Ok", 
					 new DialogInterface.OnClickListener() {
					     public void onClick(DialogInterface dialog, int which) {
						 return;
					  }});
		   alertDialog.show();
	       }
	       else if(item.getItemId() == -1 && false){
		   if(ExpandableListView.getPackedPositionType(info.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
		       int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		       Location location = getCurrentLocation();
		       if(location != null){ 
			   Project project = (Project)adapter.getGroup(groupPos);
			   AsyncRemoteUpdater updater = new AsyncRemoteUpdater(pv.getServer(), project.getId(), 
									       location.getLatitude(),
									       location.getLongitude());
			   ThreadPool.instance().execute(updater);
		       }
		   }	   
	       }

	       else if(item.getItemId() == -1 && false){
   		   if(ExpandableListView.getPackedPositionType(info.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
		       int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		       GeoPoint gp = MyMapView.getSavedPoint();
		       if(gp != null){ 
			   Project project = (Project)adapter.getGroup(groupPos);
			   AsyncRemoteUpdater updater = new AsyncRemoteUpdater(pv.getServer(), project.getId(), 
									       (double)gp.getLatitudeE6()/1000000,
									       (double)gp.getLongitudeE6()/1000000);
			   ThreadPool.instance().execute(updater);
		       }
		   }	   
	       }
	       
	       return true;
	   }
	   
	   
	   /**
	    *
	    */
	   class AsyncRemoteUpdater implements Runnable {
	       private String url;
	       private int project;
	       private double latitude, longitude;
	       
	       public AsyncRemoteUpdater(String url, int project,
					 double latitude, double longitude){
		   this.url = url;
		   this.project = project;
		   this.latitude = latitude;
		   this.longitude = longitude;
	       }
	
	       /**
		* This method will be started in another thread.
		**/
	       public void run(){
		   Log.d(TAG, "Sending project position update");

		   boolean isPostOk = false;
		   try{
		       JsonClient jsonClient = new JsonClient();
		       isPostOk = 
			   jsonClient.sendProjectPositionUpdate(url, 
								project, 
								latitude, 
								longitude); 
		   }
		   catch(Exception x){
		       Log.d(TAG, x.getMessage(), x);
		   }
		   Log.d(TAG, "Post ok? " + isPostOk);
	       }
	   }


	public boolean onCreateOptionsMenu(Menu menu) {
	    //menu.add(0, Menu.FIRST+1, 0, "Skapa projekt");
	    //menu.add(0, Menu.FIRST+2, 0, "Skapa aktivitet");
	    return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
	    if(item.getItemId() == Menu.FIRST+1){
		Intent intent = new Intent(ProjectListActivity.this, SimpleForm.class);
		intent.putExtra("form.prompt", "Projekt"); 
		intent.putExtra("form.type", Constants.SIMPLE_PROJECT_TYPE); 
		startActivityForResult(intent, 0);
	    }
	    else if(item.getItemId() == Menu.FIRST+2) {
		Intent intent = new Intent(ProjectListActivity.this, SimpleForm.class);
		intent.putExtra("form.prompt", "Aktivitet");
		intent.putExtra("form.type", Constants.SIMPLE_ACTIVITY_TYPE);
		startActivityForResult(intent, 0);
	    }
	    return true;
	}


    protected void onActivityResult(int requestCode, int resultCode,
				    Intent data) {
	Log.d(TAG, "Got result back, code=" + resultCode); 
	if (resultCode == Constants.DATA_MODIFIED) {
	    Log.d(TAG, "Updating views");
	    adapter.reload();
	    //adapter.notifyDataSetChanged();
	    //adapter.notifyDataSetInvalidated();
	}
    }

    public void projectsModified(){
	Log.d(TAG, "On projects modified");
	    runOnUiThread(
			  new Runnable(){
			      public void run(){
				  adapter.reload();
			      }
			  });

    }


    public class MyExpandableListAdapter extends BaseExpandableListAdapter {

	private List<Project> projects;
	private List<Activity> activities;

	int currentGroup = -1;
	boolean toBeReloaded = false;
	public MyExpandableListAdapter(){
	    reload();
	}

	
	public void reload(){
	    Log.d(TAG, "Reloading adapter");
	    try{
		activities = 
		    getHelper().getActivityDao().queryBuilder().where().eq(Activity.IS_ACTIVE_FIELD_NAME, 
									   true).query();
		projects = 
		    getHelper().getProjectDao().queryBuilder().where().eq(Project.IS_ACTIVE_FIELD_NAME, 
									  true).query();
		for(Project p : projects) Log.d(TAG, p.toFullString());	    

		// FIX THIS!
		//projects = 
		//    getHelper().getProjectDao().queryForAll();
		//for(Project p : projects) Log.d(TAG, p.toFullString());	    

	    }
	    catch(SQLException x){
		Log.d(TAG, x.getMessage(), x);
	    }	    

	    notifyDataSetChanged();
	}
	
        private Project getProject(int pos){
	    return projects.get(pos);
	}

	private Activity getActivity(int pos){
	    return activities.get(pos);
	}

        public Object getChild(int parentPosition, int childPosition) {
            return activities.get(childPosition);
        }

        public long getChildId(int parentPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int pos) {
            return activities.size();
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 64);

            TextView textView = new TextView(ProjectListActivity.this);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
	    textView.setTextColor(Color.parseColor("#31A6D2"));
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0);
	    textView.setTextSize(20);
            return textView;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getGroup(groupPosition).toString());
            return textView;
        }

        public View getChildView(int parentPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
	    //if(childPosition == 1) 
	    textView.setTextColor(Color.parseColor("#3DD84B"));
            textView.setText(getChild(parentPosition, childPosition).toString());
 
           return textView;
        }

        public Object getGroup(int parentPosition) {
            return projects.get(parentPosition);
        }

        public int getGroupCount() {
            return projects.size();
        }

        public long getGroupId(int parentPosition) {
            return parentPosition;
        }

	
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return false;
        }

    }


}