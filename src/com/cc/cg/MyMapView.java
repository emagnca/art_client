package com.cc.cg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import android.graphics.drawable.Drawable;

import android.view.GestureDetector.SimpleOnGestureListener;

public class MyMapView extends MapActivity 
    implements EventDispatcher.PositionListener {

    private final static String TAG = "MyMapView";

    private TextView myLongitude, myLatitude;
    private CheckBox mySatellite, myTraffic;

    private MapView myMapView;
    private MapOverlay myOverlay;
    private MapController myMapController;

    private static GeoPoint savedGP = null;
    public static GeoPoint getSavedPoint() { return savedGP; }

    protected void onCreate(Bundle b) {

	Log.d(TAG, "onCreate");

	super.onCreate(b);
	setContentView(R.layout.mymapview);

	myMapView = (MapView)findViewById(R.id.mapview);
	myMapController = myMapView.getController();
	myMapView.setBuiltInZoomControls(true);
	
	myLongitude = (TextView)findViewById(R.id.longitude);
	myLatitude = (TextView)findViewById(R.id.latitude);
	mySatellite = (CheckBox)findViewById(R.id.satellite);
	myTraffic = (CheckBox)findViewById(R.id.traffic);
	mySatellite.setOnClickListener(mySatelliteOnClickListener);
	myTraffic.setOnClickListener(myTrafficOnClickListener);
	
	myMapView.setSatellite(mySatellite.isChecked());
	myMapView.setTraffic(myTraffic.isChecked());
	
	Drawable projectMarker = getResources().getDrawable(R.drawable.bluesquare);
	Drawable myMarker = getResources().getDrawable(R.drawable.pinksquare);
	projectMarker.setBounds(0, 0, projectMarker.getIntrinsicWidth(), 
				projectMarker.getIntrinsicHeight());
	myMarker.setBounds(0, 0, myMarker.getIntrinsicWidth(), 
			   myMarker.getIntrinsicHeight());

	myMapView.getOverlays().add(new MapGestureDetectorOverlay(new GestureListener()));

	myOverlay = new MapOverlay(this, projectMarker, myMarker);
	myMapView.getOverlays().add(myOverlay);
				    
	EventDispatcher.instance().registerPositionListener(this); 
    }

    public void onStart(){
	Log.d(TAG, "onStart");
	super.onStart();
    }

    public void onResume(){
	Log.d(TAG, "onResume");
	super.onResume();
	myOverlay.loadProjects();
	PersistentValues pv = new PersistentValues(this);
	int latitude = pv.getLastKnownLatitude();
	int longitude = pv.getLastKnownLongitude();
	if(latitude == -1){
	    latitude = 57662980;
	    longitude = 11872440;
	}
	GeoPoint initGeoPoint = new GeoPoint(latitude, longitude);
	centerLocation(initGeoPoint);
    }

    public void onDestroy(){
	super.onDestroy();
	EventDispatcher.instance().unregisterPositionListener(this);
    }

    protected boolean isRouteDisplayed() {
	return false;
    }

    private void centerLocation(GeoPoint centerGeoPoint)
    {
	myMapController.animateTo(centerGeoPoint);


	myLongitude.setText(" Lon: "+
			    String.valueOf((float)centerGeoPoint.getLongitudeE6()/1000000)
			    );
	myLatitude.setText(" Lat: "+
			   String.valueOf((float)centerGeoPoint.getLatitudeE6()/1000000)
			   );
    };

    private CheckBox.OnClickListener mySatelliteOnClickListener =
	new CheckBox.OnClickListener(){
	    public void onClick(View v) {
		myMapView.setSatellite(mySatellite.isChecked());
	    }
	};

    private CheckBox.OnClickListener myTrafficOnClickListener =
	new CheckBox.OnClickListener(){
	    public void onClick(View v) {
		myMapView.setTraffic(myTraffic.isChecked());
	    }
	};



    private long lastTouchTime = -1;

    public void touch(MotionEvent event){
	myLatitude.setText(" Lon: " + String.valueOf((float)myMapView.getMapCenter().getLatitudeE6()/1000000));
	myLongitude.setText(" Lat: " + String.valueOf((float)myMapView.getMapCenter().getLongitudeE6()/1000000));
    }

    public void movedIntoProjectArea(long projectId){
	myOverlay.loadProjects();
	myMapView.invalidate();
    }
    public void movedOutOfProjectArea(){
	myOverlay.loadProjects();
	myMapView.invalidate();
    }
    public void newPosition(){
	myOverlay.loadProjects();
	myMapView.invalidate();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
	menu.add(0, Menu.FIRST+1, 0, "Sök projekt");
	return super.onCreateOptionsMenu(menu);
    }

   
    protected void onActivityResult(int requestCode, int resultCode,
				    Intent data) {
	Log.d(TAG, "Got result back, code=" + resultCode);
	
	if (resultCode == RESULT_OK){
	    Bundle bundle = data.getExtras();
	    double latitude = bundle.getDouble(Constants.PARAM_LATITUDE, -1);
	    double longitude = bundle.getDouble(Constants.PARAM_LONGITUDE, -1);
	    if(latitude != -1 && longitude != -1){
		int lat = (int)(latitude * 1000000);
		int lon = (int)(longitude * 1000000);
		PersistentValues pv = new PersistentValues(this);
 		pv.setLastKnownPosition(lat, lon);
	    }
	}
	
    }

    public boolean onOptionsItemSelected(MenuItem item) {
	if(item.getItemId() == Menu.FIRST+1){
	    Intent intent = new Intent(MyMapView.this, SimpleListActivity.class);
	    intent.putExtra("form.type", Constants.SIMPLE_PROJECT_TYPE);
	    startActivityForResult(intent, 0);
	}
	return true;
    }


    
    class GestureListener extends SimpleOnGestureListener {

	public void onLongPress(MotionEvent event) {
	    
	    if (event.getPointerCount() > 1) return;

	    savedGP = myMapView.getProjection().fromPixels((int) event.getX(),
							   (int) event.getY());
	    
	    double lat = ((double)savedGP.getLatitudeE6())/1000000;
	    double lon = ((double)savedGP.getLongitudeE6())/1000000;
	    Log.d(TAG, "onLongPress at lat=" + lat + " lon=" + lon);
	    Toast.makeText(MyMapView.this, "Position sparad vid latitude=" + lat + " ,longitude=" + lon, Toast.LENGTH_SHORT).show();

	}
    }


}