package com.cc.cg;

import java.util.*;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import android.util.Log;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

import com.cc.cg.database.Project;
import com.cc.cg.database.DatabaseHelper;

class MapOverlay extends ItemizedOverlay<OverlayItem>{

    private final static String TAG = "MapOverlay";
    private List<MapItem> points = new ArrayList<MapItem>();
    private Drawable projectMarker, myMarker;
    private MyMapView mapView;

  public MapOverlay(MyMapView mapView, 
		    Drawable projectMarker,
		    Drawable myMarker) {
      super(boundCenterBottom(projectMarker));
      this.mapView = mapView;
      this.projectMarker = projectMarker;
      this.myMarker = myMarker;
      OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);
      //loadProjects();
  }

  //public synchronized void paint(){populate();}

  public synchronized void loadProjects(){
      Log.d(TAG, "loadProjects");

      DatabaseHelper helper = (DatabaseHelper)OpenHelperManager.getHelper(mapView);
      try{
	  List<Project> projects =  
	      helper.getProjectDao().queryBuilder().where().eq(Project.IS_ACTIVE_FIELD_NAME, 
							       true).query();		

	  points.clear();
	  for(Project project : projects){
	      Log.d(TAG, "--- project -> " + project.getName() + ", lat=" + project.getLatitude() +
		    ", lon=" + project.getLongitude());
	      if(project.getLatitude() != 0){
		  GeoPoint point = new GeoPoint((int)(project.getLatitude()*1000000), 
						(int)(project.getLongitude()*1000000));
		  addItem(new MapItem(point, project.getName(), MapItem.Type.PROJECT));
	      }
	  }
	  Location location = SessionContext.instance().getLastKnownLocation();
	  if(location != null){
	      Log.d(TAG, "Adding me at lat=" + location.getLatitude() + " lon=" + location.getLongitude());
	      GeoPoint point = new GeoPoint((int)(location.getLatitude()*1000000), 
					    (int)(location.getLongitude()*1000000));
	      addItem(new MapItem(point, "Jag", MapItem.Type.ME));
	  }
      }
      catch(java.sql.SQLException x){
	  Log.d(TAG, x.getMessage(), x);
      }
      OpenHelperManager.releaseHelper();
 
      populate();
      Log.d(TAG, "Finished loading projects");
   }


  protected synchronized OverlayItem createItem(int i) {
      return points.get(i);
  }

  public synchronized int size() {
      return points.size();
  }

  protected boolean onTap(int i){
      Log.d(TAG, "ON TAP!");
      final MapItem item = points.get(i);
      Toast.makeText(mapView, item.getName(), Toast.LENGTH_LONG).show();
      return super.onTap(i);
  }

  
  public boolean onTouchEvent(android.view.MotionEvent event, MapView mv) {
      Log.d(TAG, "Key down in overlay!");

	  
      ((MyMapView)mapView).touch(event);
     return super.onTouchEvent(event, mv);
  }
  

  public void addItem(MapItem item) {
      Log.d(TAG, "Adding item: " + item.getType());
      if(item.getType() == MapItem.Type.ME) item.setMarker(boundCenterBottom(myMarker));
      points.add(item);
  }

   
  
      public void draw(Canvas canvas, MapView mapView, 
		       boolean shadow) {
      super.draw(canvas, 
		 mapView, 
		 shadow);
   
      boundCenterBottom(projectMarker);
  }
  


}