package com.cc.cg;

import android.app.TabActivity;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class MyTabActivity extends TabActivity implements OnTabChangeListener {

    private final static String TAG = "MyTabActivity";
    private final static int SELECTED_COLOR = Color.parseColor("#1F1F1F"); 
    private final static int OTHER_COLOR = Color.parseColor("#383838");

    private PersistentValues pv;

    protected void onCreate(Bundle savedInstanceState) {

	Log.i(TAG, "onCreate thread=" + Thread.currentThread().getId());
	super.onCreate(savedInstanceState);

	pv = new PersistentValues(this);

	final TabHost tabHost = getTabHost();

	tabHost.setBackgroundColor(Color.parseColor("#FF9B00"));
	TextView tab1 = new TextView(this);
	tab1.setText("Tid");
	tab1.setGravity(android.view.Gravity.CENTER);
	tab1.setTextSize(18.0f);
	tab1.setTextColor(Color.parseColor("#E8E8E8"));
	TextView tab2 = new TextView(this);
	tab2.setText("Projekt");
	tab2.setTextSize(18.0f);
	tab2.setTextColor(Color.parseColor("#E8E8E8"));
	tab2.setGravity(android.view.Gravity.CENTER);
	TextView tab3 = new TextView(this);
	tab3.setText("Rapport");
	tab3.setTextSize(18.0f);
	tab3.setTextColor(Color.parseColor("#E8E8E8"));
	tab3.setGravity(android.view.Gravity.CENTER);
	TextView tab4 = new TextView(this);
	tab4.setText("Karta");
	tab4.setTextSize(18.0f);
	tab4.setTextColor(Color.parseColor("#E8E8E8"));
	tab4.setGravity(android.view.Gravity.CENTER);

	tabHost.addTab(tabHost.newTabSpec("Tid").setIndicator(tab1)
	       .setContent(new Intent(this, TimeActivity.class)));
	tabHost.addTab(tabHost.newTabSpec("Projekt").setIndicator(tab2)
		       .setContent(new Intent(this, ProjectListActivity.class)));
	tabHost.addTab(tabHost.newTabSpec("Rapport").setIndicator(tab3)
		       .setContent(new Intent(this, ReportActivity.class)));
	tabHost.addTab(tabHost.newTabSpec("Karta").setIndicator(tab4)
		       .setContent(new Intent(this, MyMapView.class)));
	tabHost.setOnTabChangedListener(this);

	// From Android 2.2
	getTabWidget().setStripEnabled(true);

	setColors();
	
    }

    public void onStart() {
	super.onStart();

	String tabIndex = pv.getCurrentTab();
	Log.d(TAG, "Setting current tab to: " + tabIndex);

	if(tabIndex.length() > 0)
	    getTabHost().setCurrentTabByTag(tabIndex);
    }

    public void onTabChanged(String tabId) {
	Log.d(TAG, "On tab changed to: " + tabId);
	pv.setCurrentTab(tabId);
	setColors();
    }

    private void setColors(){

	TabHost tabHost = getTabHost();
	int currTab = tabHost.getCurrentTab();
	

	for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
	    if(i == currTab){
		tabHost.getTabWidget().getChildAt(currTab).setBackgroundColor(SELECTED_COLOR);
		tabHost.getTabWidget().getChildAt(currTab).setDrawingCacheBackgroundColor(SELECTED_COLOR);
	    }
	    else{
		tabHost.getTabWidget().getChildAt(i).setBackgroundColor(OTHER_COLOR);
		tabHost.getTabWidget().getChildAt(currTab).setDrawingCacheBackgroundColor(SELECTED_COLOR);
	    }
	    tabHost.getTabWidget().getChildAt(i).getLayoutParams().height = 70;
	}

    }

}