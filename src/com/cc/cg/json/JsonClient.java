package com.cc.cg.json;
import android.content.Context;
import android.util.Log;
import com.cc.cg.Constants;
import com.cc.cg.SessionContext;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;



public class JsonClient {
    
    public static final int REGISTRATION_TIMEOUT = 20 * 1000; // ms  
    public static final String TAG = "JsonClient";

    // Singleton
    private HttpClient _httpClient = getHttpClient();

    private HttpClient getHttpClient() {
	HttpClient httpClient = new DefaultHttpClient();
	HttpParams params = httpClient.getParams();
	HttpConnectionParams.setConnectionTimeout(params,
						  REGISTRATION_TIMEOUT);
	HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
	return httpClient;
    }

    KeyStore trustStore;
    public void initKeyStore(Context context){
	Log.d(TAG, "initialising keystore");
	/*
	InputStream in = null;
	try {
	    trustStore = KeyStore.getInstance("BKS");
	    in = context.getResources().openRawResource(com.cc.cg.R.raw.keystore);
	    trustStore.load(in, "mypass".toCharArray());
	    Log.d(TAG, "KEYSTORE initialised!!!");
	}
	catch(Exception x){
	    Log.e(TAG, "Could not initialize keystore!" + Log.getStackTraceString(x));
	}
	finally {
	    try{in.close();}catch(IOException x){}
	}
	*/
    }

    public HttpClient getHttpsClient() {
	try {
	    //KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	    //trustStore.load(null, null);

	    SSLSocketFactory sf = new SafeSocketFactory(trustStore);
	    //sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	    HttpParams params = new BasicHttpParams();
	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	    HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	    SchemeRegistry registry = new SchemeRegistry();
	    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    registry.register(new Scheme("https", sf, 443));

	    ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	    return new DefaultHttpClient(ccm, params);
	} catch (Exception x) {
	    Log.e(TAG, "Failed to create ssl client. Exception: " + 
		  Log.getStackTraceString(x));
	    return new DefaultHttpClient();
	}
    }

    private void handleException(){
	Log.d(TAG, "HandleException");
	//_httpClient.getConnectionManager().shutdown();
	_httpClient = getHttpClient();
    }
    
    private String sendGet(String uri, String json) throws IOException {
	uri = "http://" + uri;
	Log.d(TAG, "GET uri:" + uri);
	String s = "";
	try{
	    Log.d(TAG, uri);
	    HttpGet httpget = new HttpGet(uri);
	    httpget.getParams().setParameter("JSONBODY", "json"); 
	    if(json != null && json.length() > 0) httpget.addHeader("JSON", json);
	    httpget.addHeader("APIKEY", SessionContext.instance().getApiKey()); 
	    HttpResponse httpResponse = _httpClient.execute(httpget); 
	    HttpEntity httpEntity = httpResponse.getEntity();
	    int code = httpResponse.getStatusLine().getStatusCode();
	    if(httpEntity != null){
		s = EntityUtils.toString(httpEntity, "UTF-8");
		httpEntity.consumeContent();
	    }
	    _httpClient.getConnectionManager().shutdown();
	    if(code != 200){
		throw new IOException("Got error code from server. Code=" + code);
	    }
	}
	catch(IllegalStateException x){
	    Log.e(TAG, "IllegalStateException: "+Log.getStackTraceString(x));
	    handleException();
	    throw new IOException("Tried to send request on closed connection?");
	}
	catch(Exception x){
	    Log.e(TAG, "Unexpected exception: "+Log.getStackTraceString(x));
	    handleException();
	    throw new IOException("Unexpected http problem:" + x.getMessage());
	}
	Log.d(TAG, "Returning: " + s);
	return s;
    }

    private void sendPost(String host, String content, String json) throws IOException {
	String uri = "http://" + host;
        Log.d(TAG, "POST uri:" + uri);

	try{
	    HttpPost httppost = new HttpPost(uri);
	    if(json != null && json.length() > 0) httppost.addHeader("JSON", json);
	    httppost.addHeader("APIKEY", 
			       SessionContext.instance().getApiKey()); 
	    httppost.setEntity(new StringEntity(content));
	    HttpResponse httpResponse = _httpClient.execute(httppost); 
	    HttpEntity entity = httpResponse.getEntity();
	    if(entity != null) entity.consumeContent();
	    int code = httpResponse.getStatusLine().getStatusCode();
	    _httpClient.getConnectionManager().shutdown();
	    if(code != 200){
		throw new IOException("Got error code from server. Code=" + code);
	    }
	}
	catch(IllegalStateException x){
	    Log.e(TAG, "IllegalStateException: "+Log.getStackTraceString(x));
	    handleException();
	    throw new IOException("Tried to send request on closed connection?");
	}
	catch(Exception x){
	    Log.e(TAG, "Unexpected exception: "+Log.getStackTraceString(x));
	    handleException();
	    throw new IOException("Unexpected http problem: " + x.getMessage());
	}
    }

	
    public JsonModel.SimpleProject[] getSimpleProjects(String host, int user) 
	throws IOException, JsonParseException {
	String uri = host + "/projects/";
	JsonModel.SimpleUser simpleUser =
	    new JsonModel.SimpleUser(user);
        Gson gson = new Gson();
	String json = gson.toJson(simpleUser);
	String s = sendGet(uri, json);
	Log.d(TAG, s);
        return gson.fromJson(s, JsonModel.SimpleProject[].class);
    }

    public JsonModel.SimpleActivity[] getSimpleActivities(String host, int user) 
	throws IOException, JsonParseException {
	String uri = host + "/activities/";
	JsonModel.SimpleUser simpleUser =
	    new JsonModel.SimpleUser(user);
        Gson gson = new Gson();
	String json = gson.toJson(simpleUser);
        String s = sendGet(uri, json);
	Log.d(TAG, s);
	return gson.fromJson(s, JsonModel.SimpleActivity[].class);
    }

    public String getMessage(String host, int user) 
	throws IOException, JsonParseException {
	String uri = host + "/getmessage/";
	JsonModel.SimpleUser simpleUser =
	    new JsonModel.SimpleUser(user);
	Gson gson = new Gson();
	String json = gson.toJson(simpleUser);
	return sendGet(uri, json);
    }

    public String postMessage(String host, int user, String msg) 
	throws IOException, JsonParseException {
	String uri = host + "/logmessage/";
	JsonModel.Log log = new JsonModel.Log(user, msg);
	Gson gson = new Gson();
	return sendGet(uri, gson.toJson(log));
    }
   
    public void postTrace(String host, String log, int user) throws IOException {
	JsonModel.SimpleUser simpleUser =
	    new JsonModel.SimpleUser(user);
	String uri = host + "/logtrace/";
        Gson gson = new Gson();
	String json = gson.toJson(simpleUser);
	sendPost(uri, log, json);
    }

    public JsonModel.SimpleObject 
	sendPositionReport(String host, int user, String time, double latitude, 
			   double longitude, int battery, int proj, 
			   int act, boolean isVisible)
	throws IOException, JsonParseException {
	String uri = host + "/positionreport/";
	String answer = "KO";
	JsonModel.PositionReport positionReport =
	    new JsonModel.PositionReport(user, time,
					 latitude, longitude, 
					 battery, proj, 
					 act, isVisible);
	Log.d(TAG, "Sending positionreport to: " + uri + "," + positionReport);
	Gson gson = new Gson();
	String json = gson.toJson(positionReport);
	Log.d(TAG, json);
	String s = sendGet(uri, json);
	return gson.fromJson(s, JsonModel.SimpleObject.class);
    }



    public void  sendTimeReport(String host, int project, int activity, 
				int user, long start, long stop, 
				boolean isAutomatic, String comment)
	throws IOException, JsonParseException {
	String uri = host + "/timereport/";
	Log.d(TAG, "Sending timereport to: " + uri);
	String answer = Constants.RESPONSE_KO;
	JsonModel.TimeReport timeReport =
	    new JsonModel.TimeReport(project, activity, user,
				     start, stop, isAutomatic);
	Gson gson = new Gson();
	String json = gson.toJson(timeReport);
	Log.d(TAG, json);
	if(comment==null) comment="";
	sendPost(uri, comment, json);
    }

    public boolean sendProjectPositionUpdate(String host, int project, double lat, double lon)
	throws IOException, JsonParseException {
	String uri = host + "/updateposition/";
	Log.d(TAG, "Sending project position to: " + uri);
	String answer = Constants.RESPONSE_KO;
	JsonModel.ProjectPositionUpdate update =
	    new JsonModel.ProjectPositionUpdate(project, lat, lon);
	Gson gson = new Gson();
	String json = gson.toJson(update);
	Log.d(TAG, json);
	answer = sendGet(uri, json);

	return answer.equals(Constants.RESPONSE_OK);
    }


    /**
     * Inner class handling ssl.
     */
    private class SafeSocketFactory extends SSLSocketFactory {

	SSLContext sslContext = SSLContext.getInstance("TLS");

	public SafeSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, 
							     KeyManagementException, 
							     KeyStoreException, 
							     UnrecoverableKeyException {
	    super(truststore);

	    TrustManager tm = new X509TrustManager() {
		    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		    }

		    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			Log.d(TAG, "checkClientTrusted");
			//for(X509Certificate c : chain){
			//  Log.d(TAG, "::" + c.getSubjectX500Principal().toString());
			//}
		    }

		    public X509Certificate[] getAcceptedIssuers() {
			return null;
		    }
		};

	    sslContext.init(null, new TrustManager[] { tm }, null);
	}

	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

	public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
    }
}