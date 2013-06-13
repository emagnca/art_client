package com.cc.cg;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.util.Log;
import android.util.TypedValue;

public class MsgPopupActivity extends Activity {
    
    private final static String TAG = "MsgPopupActivity";
    private TextView textView;
    private int notifId;

    protected void onCreate(Bundle bundle) {

        Log.d(TAG, "onCreate");
        super.onCreate(bundle);
	//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
	//	     WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        //setContentView(R.layout.msg);
	LinearLayout layout = new LinearLayout(this);
	layout.setOrientation(LinearLayout.VERTICAL);
	ScrollView scrollView = new ScrollView(this);
	textView = new TextView(this);
	textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
	textView.setGravity(Gravity.CENTER_HORIZONTAL);
	Button button = new Button(this);
	textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
	button.setText("Stäng");
	scrollView.addView(textView);
	layout.addView(button);
	layout.addView(scrollView);
	setContentView(layout);

        final String msg = getIntent().getStringExtra("com.cc.cg.msg");
        notifId = getIntent().getIntExtra("com.cc.cg.notifid", -1);
        Log.d(TAG, "Id=" + notifId);
	Log.d(TAG, "Msg=" + msg);
        setTitle("Meddelande från admin");
    
	textView.setText("\n" + msg);

	button.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_off));

        button.setOnClickListener(new OnClickListener(){
		public void onClick(View v){
		    Log.d(TAG, "Close was clicked");
		    NotificationManager notificationManager = 
			(NotificationManager)MsgPopupActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
		    notificationManager.cancel(notifId);
		   finish();
		}});


    }

    public void onStart()
    {
	Log.d(TAG, "OnStart");
	super.onStart();
    }

    public void onStop()
    {
	Log.d(TAG, "OnStop");
	super.onStop();
    }
            
    public void onPause()
    {
	Log.d(TAG, "OnPause");
	super.onPause();
    }
            
    public void onResume()
    {
	super.onResume();
	textView.setText(SessionContext.instance().getMessage());
      
	Log.d(TAG, "OnResume");
    }

    public void onDestroy()
    {
	Log.d(TAG, "OnDestroy");
	super.onDestroy();
    }

}