package com.cc.cg;

import android.content.Context;
import java.io.*;

public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;
    private Context context;
    private PersistentValues pv;
    private static long timestamp = 0;

    public MyUncaughtExceptionHandler(Context context) {
        this.context = context;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	this.pv = new PersistentValues(context);
    }

    public void uncaughtException(Thread t, Throwable e) {
	if(System.currentTimeMillis() - timestamp > 60000){
	    timestamp = System.currentTimeMillis();
	    android.telephony.SmsManager sm = android.telephony.SmsManager.getDefault();
	    String msg = "Art har kraschat. Uid=" + pv.getUser();
	    sm.sendTextMessage(Constants.ADMIN_PHONE_NUMBER, null, msg, null, null);
	    RemoteLog remoteLog = new RemoteLog(context);
	    remoteLog.dumpStackTrace(e);
	    defaultUEH.uncaughtException(t, e);
	}
    }
}

