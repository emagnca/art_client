package com.cc.cg;

import com.cc.cg.json.JsonClient;
import com.cc.cg.json.JsonModel;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonClientTest extends ActivityInstrumentationTestCase2<MyTabActivity> {

    private String _host = "tl1.emagnca.webfactional.com";
    private int _user = 12345;
    private String _apiKey = "137dc";

    public static final String TAG = "JsonClientTest";

    public JsonClientTest() {
        super("com.cc.cg", MyTabActivity.class);
	com.cc.cg.SessionContext.instance().setApiKey(_apiKey);
    }

    public void testGetName() throws Exception 
    {
        String url = _host + "/art/v1";
        JsonClient client = JsonClient.instance();
        JsonModel.SimpleProject[] projs = client.getSimpleProjects(url, _user);
	assertTrue(true);
	Log.d(TAG, "000001");
    }

    public void testPositionReport() throws Exception 
    {
        String url = _host + "/art/v1";
	SimpleDateFormat dateFormat =
	    new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
	String time = dateFormat.format(new Date());
        JsonClient client = JsonClient.instance();
        String s = client.sendPositionReport(url, _user, time, 0, 0, 50, "TestVersion", 1, 1, false);
	assertTrue(true);
	Log.d(TAG, s);
    }

}
