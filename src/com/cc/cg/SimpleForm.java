package com.cc.cg;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.util.Log;
import android.graphics.Color;

import java.sql.SQLException;

import com.cc.cg.database.Activity;
import com.cc.cg.database.Project;
import com.cc.cg.database.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class SimpleForm extends OrmLiteBaseActivity<DatabaseHelper> implements OnClickListener {
      
    private final static String TAG = "=== ProjectForm ===";
    private EditText edit;
    private Button buttonOk;
    private Button buttonCancel;
    private int type;

    public void onCreate(Bundle b) {
	super.onCreate(b);

	String prompt = getIntent().getStringExtra("form.prompt");
	type = getIntent().getIntExtra("form.type", -1);

        Log.d(TAG, "OnCreate type=" + type);
        setContentView(R.layout.simple_form);

	edit = (EditText)findViewById(R.id.edit);
	buttonOk = (Button)findViewById(R.id.ok);
	buttonCancel = (Button)findViewById(R.id.cancel);

	buttonOk.setOnClickListener(this);
	buttonCancel.setOnClickListener(this);

	edit.setText(prompt);
    }

    public void onClick(View v){
	Log.d(TAG, "onClick");

	if((Button)v == buttonOk){
	    if(type == Constants.SIMPLE_PROJECT_TYPE)
		createProject();
	    else if(type == Constants.SIMPLE_ACTIVITY_TYPE)
		createActivity();
	    setResult(Constants.DATA_MODIFIED);
	}
	    else{
		Log.d(TAG, "Project creation was cancelled");
		setResult(RESULT_CANCELED);
	    }
	finish();
    }
    

    private void createProject(){
	/*
	try{
	    Project project = new Project(edit.getText().toString());
	    getHelper().getProjectDao().create(project);
	    Log.d(TAG, "Created project: " + project.getName());
	}
	catch(SQLException x){
	    Log.e(TAG, x.getMessage(), x);
	}
	*/
    }

    private void createActivity(){
	/*
	try{
	    Activity activity = new Activity(edit.getText().toString());
	    getHelper().getActivityDao().create(activity);
	    Log.d(TAG, "Created activity: " + activity.getName());
	}
	catch(SQLException x){
	    Log.e(TAG, x.getMessage(), x);
	}
	*/
    }
    
	
}

