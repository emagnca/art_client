package com.cc.cg;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

public class SettingsActivity extends Activity {

    private final static String TAG = "SettingsActivity";

    public void onCreate(Bundle b) {    
	Log.d(TAG, "onCreate");
        
        super.onCreate(b);
        setContentView(R.layout.settings);

	final PersistentValues pv = new PersistentValues(this);

	final Button buttonOk = (Button)findViewById(R.id.ok);
	final EditText server = (EditText)findViewById(R.id.address);
	final EditText user = (EditText)findViewById(R.id.user); 
	final EditText key = (EditText)findViewById(R.id.key); 

	server.setText(pv.getServer());
	user.setText(Integer.toString(pv.getUser()));
	key.setText(pv.getApiKey());

	buttonOk.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
		    try{
			String mykey = key.getText().toString().trim();
			pv.setServer(server.getText().toString().trim());
			pv.setUser(Integer.parseInt(user.getText().toString()));
			pv.setApiKey(mykey);
			SessionContext.instance().setApiKey(mykey);
			finish();
		    }
		    catch(NumberFormatException x){
			AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this).create();
			alertDialog.setTitle("Felaktig indata");
			alertDialog.setMessage("Användare måste vara ett nummer.");
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
					      "Ok",
					      new DialogInterface.OnClickListener() {
						  public void onClick(DialogInterface dialog, int which) {
						      return;
						  }});
			alertDialog.show();
		    }
		}
	    });

	buttonOk.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_off));

    }

}