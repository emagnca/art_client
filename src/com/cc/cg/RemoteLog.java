package com.cc.cg;

import android.content.Context;
import android.util.Log;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.cc.cg.json.JsonClient;

public class RemoteLog {

    public enum Level { DEBUG, INFO, ERROR, FATAL }

    private final static String TAG = "RemoteLog";

    PersistentValues _pv;
    JsonClient _jsonClient;
    
    public RemoteLog(Context context){
	_pv = new PersistentValues(context);    
	_jsonClient = new JsonClient();
    }

    public void i(String tag, String msg){
	Log.i(tag, msg);
	AsyncRemoteLogger logger = new AsyncRemoteLogger(Level.INFO, tag, msg);
	ThreadPool.instance().execute(logger);
    }

    public void e(String tag, String msg, Throwable t){
	Log.e(tag, msg, t);
	AsyncRemoteLogger logger = new AsyncRemoteLogger(Level.ERROR, tag, msg);
	ThreadPool.instance().execute(logger);
    }

    public void wtf(String tag, String msg){
	Log.e(tag, msg);
	AsyncRemoteLogger logger = new AsyncRemoteLogger(Level.FATAL, tag, msg);
	ThreadPool.instance().execute(logger);
    }
    
    public void dumpLog(){
	    AsyncRemoteLogDumper logger = new AsyncRemoteLogDumper();
	    ThreadPool.instance().execute(logger);	
    }

    public void dumpStackTrace(Throwable t){
	    AsyncStackTraceDumper logger = new AsyncStackTraceDumper(t);
	    ThreadPool.instance().execute(logger);	
    }

    class AsyncRemoteLogger implements Runnable {

	private String msg;

	public AsyncRemoteLogger(Level level, String tag, String msg){
	    SimpleDateFormat dateFormat =
		new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
	    String time = dateFormat.format(new Date());
	    this.msg = time + " | " + level + ": " + tag + "-->" + msg;
	}

	public void run(){
	    try{
		if(_jsonClient.postMessage(_pv.getServer(), 
					   _pv.getUser(), 
					   msg).equals(Constants.RESPONSE_OK))
		    return;
	    }
	    catch(Exception x){} //IOException or JsonParseException
	}
    }

    class AsyncRemoteLogDumper implements Runnable {

	final static String LOG_TAGS = "AndroidRuntime:E LocationService:* PositionReporter:* " + 
	    " MyMapView:* MapOverlay:* TimeActivity:* ReportActivity:* " +
	    " DbFacade:* *:S";

	public void run(){
		try{
		    String cmd = "logcat -d -v long " + LOG_TAGS;
		    Process proc = Runtime.getRuntime().exec(cmd);

		    BufferedReader reader = new BufferedReader(new InputStreamReader
							       (proc.getInputStream()));

		    String line;
		    StringBuffer sb = new StringBuffer();
		    String separator = System.getProperty("line.separator"); 

		    while ((line = reader.readLine()) != null){
			sb.append(line);
			sb.append(separator);
		    }


		    Log.d(TAG, sb.toString());
		    _jsonClient.postTrace(_pv.getServer(), 
					  sb.toString(),
					  _pv.getUser());
		    proc.destroy();
		}
		catch(Exception x){
		    Log.d("RemoteLog", x.getMessage(), x);
		}
	}
    }

    class AsyncStackTraceDumper implements Runnable {
	private Throwable throwable;

	AsyncStackTraceDumper(Throwable t){
	    throwable = t;
	}

	public void run(){
	    try{
		Writer result = new StringWriter();
		PrintWriter printWriter = new PrintWriter(result);
		throwable.printStackTrace(printWriter);
		String stacktrace = result.toString();
		printWriter.close();
		_jsonClient.postTrace(_pv.getServer(), 
				      stacktrace,
				      _pv.getUser());
	    }
	    catch(Throwable x){
		Log.e(TAG, x.getMessage(), x);
	    }
	}
    }
}