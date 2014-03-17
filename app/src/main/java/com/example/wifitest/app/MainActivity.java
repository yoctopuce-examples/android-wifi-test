package com.example.wifitest.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    private static final String DEBUG_TAG = "WIFITEST";
    private TextView _textViewResult;
    private EditText _editTextHubIp;
    private Switch _wifiSwitch;
    private WifiManager _wifiManager;
    private IntentFilter _intentFilter;
    private ConnectivityManager _connectivityManager;
    private Switch _apSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _textViewResult = (TextView) findViewById(R.id.result);
        _editTextHubIp = (EditText) findViewById(R.id.hubIp);
        _wifiSwitch = (Switch) findViewById(R.id.switch_wifi);
        _wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setWifiState(isChecked);
            }
        });

        _apSwitch = (Switch) findViewById(R.id.switch_AP);
        _apSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAPState(isChecked);
            }
        });

        _wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        _connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        _intentFilter = new IntentFilter();
        _intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        _intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        _intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        _intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);


    }

    private void setAPState(boolean isChecked)
    {
        WifiConfiguration wifi_configuration = null;
        _wifiManager.setWifiEnabled(false);

        try
        {
            //USE REFLECTION TO GET METHOD "SetWifiAPEnabled"
            Method method=_wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(_wifiManager, wifi_configuration, isChecked);
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setWifiState(boolean isChecked)
    {

        boolean res = _wifiManager.setWifiEnabled(isChecked);
        if (res) {
            dispMsg("Set Wifi to "+(isChecked?"ON":"Off"));
        } else {
            dispMsg("Unable to set  Wifi to "+(isChecked?"ON":"Off"));
        }
    }

    private BroadcastReceiver wifiChangeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    dispMsg("SCAN_RESULTS_AVAILABLE_ACTION");
                } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                    int newState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                    int oldState = intent.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                    String msg = "WIFI_STATE_CHANGED_ACTION ("+ WifiStateToString(oldState)+ " to "+WifiStateToString(newState)+")";
                    dispMsg(msg);
                } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                    boolean gained = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
                    dispMsg("SUPPLICANT_CONNECTION_CHANGE_ACTION "+(gained?"gained":"lost"));
                } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    dispMsg("NETWORK_STATE_CHANGED_ACTION");
                    NetworkInfo netinfo =intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    dispMsg(netinfo.toString());
                    String bssid = intent.getStringExtra(WifiManager.EXTRA_BSSID);
                    WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    boolean dumy = false;
                }
            }
        }

    };

    private static String WifiStateToString(int state)
    {
        switch(state){
            case WifiManager.WIFI_STATE_DISABLED:
                return "disabled";
            case WifiManager.WIFI_STATE_DISABLING:
                return "disabling";
            case WifiManager.WIFI_STATE_ENABLED:
                return "enabled";
            case WifiManager.WIFI_STATE_ENABLING:
                return "enabling";
            default:
                return "unknown";
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
        boolean enabled = _wifiManager.isWifiEnabled();
        _wifiSwitch.setChecked(enabled);
        NetworkInfo[] allNetworkInfo = _connectivityManager.getAllNetworkInfo();
        int i = 0;
        for (NetworkInfo info : allNetworkInfo) {
            dispMsg(String.format("net%d:%s", i, info.toString()));
        }

    }


    @Override
    protected void onStart()
    {
        super.onStart();
        registerReceiver(wifiChangeReciever, _intentFilter);

    }

    @Override
    protected void onStop()
    {
        unregisterReceiver(wifiChangeReciever);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void disableWifi(View v)
    {

    }

    public void enableWifi(View v)
    {

    }


    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void ping(View view)
    {
        // Gets the URL from the UI's text field.

        String stringUrl = "http://" + _editTextHubIp.getText() + "/api.xml";
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            dispMsg("No network connection available.");
        }
    }

    private void dispMsg(String msg)
    {
        Log.i(DEBUG_TAG, msg);
        _textViewResult.append("-"+msg+"\n\n");
    }


    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls)
        {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result)
        {
            dispMsg(result);
        }
    }


    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException
    {
        InputStream is = null;
        // Only display the first 2048 characters of the retrieved
        // web page content.
        int len = 2048;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }


    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException
    {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
}

