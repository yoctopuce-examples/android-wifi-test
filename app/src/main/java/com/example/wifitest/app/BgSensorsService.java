package com.example.wifitest.app;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YHumidity;
import com.yoctopuce.YoctoAPI.YLightSensor;
import com.yoctopuce.YoctoAPI.YPressure;
import com.yoctopuce.YoctoAPI.YTemperature;
import com.yoctopuce.YoctoAPI.YWakeUpMonitor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class BgSensorsService extends IntentService {

    private static final String TAG = "BgSensorsService";
    private static final long NETWORK_DETECTION_TIMEOUT = 600; // 10 minutes
    private static final int WAKEUP_INTERVAL = 1200;// 20 minutes
    public static final String ACTION_NEW_SENSORS_VALUE = "ACTION_NEW_SENSORS_VALUE";
    private WifiManager _wifiManager;
    private SensorDatabaseHelper _sensorDatabaseHelper;

    public BgSensorsService()
    {
        super(TAG);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        _wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        _sensorDatabaseHelper = SensorDatabaseHelper.get(getApplicationContext());
    }



    @Override
    protected void onHandleIntent(Intent intent)
    {
        // retrieve values form all sensors in a efficient way
        SensorsValue sensorsValue = GetSensorsValue();
        // save it to the database
        _sensorDatabaseHelper.insertSensorValue(sensorsValue);
        // notify all listeners that we have a new value in the database
        sendBroadcast(new Intent(ACTION_NEW_SENSORS_VALUE));
    }


    /**
     * Retrieves Values for of all Yoctopuce sensors connected
     *
     * @return a SensorValue object with all value set
     */
    private SensorsValue GetSensorsValue()
    {
        SensorsValue result = new SensorsValue();
        //Activate WiFi HotSpot
        if (!setAPState(true)) {
            return result;
        }
        boolean ok = false;
        long timeout = System.currentTimeMillis() + NETWORK_DETECTION_TIMEOUT * 1000;
        // wait at max NETWORK_DETECTION_TIMEOUT seconds for the YoctoHub-Wireless to connect
        // to the Wifi WiFi HotSpot
        do {
            ArrayList<String> ips = apGetIPs();
            // test all IP that are connected to the WiFi HotSpot
            for (String ip : ips) {
                try {
                    // test if this IP is a YoctoHub-Wireless
                    YAPI.RegisterHub(ip);
                    ok = true;
                } catch (YAPI_Exception e) {
                    // the IP is not Yoctopuce hub
                    YAPI.UnregisterHub(ip);
                }
            }
            if (!ok) {
                // if the YoctoHub-Wirless is not here sleep 1 second
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return result;
                }
            }
        } while (!ok && timeout > System.currentTimeMillis());

        if (ok) {
            try {
                //  get value from all sensors
                YHumidity yHumidity = YHumidity.FirstHumidity();
                if (yHumidity != null) {
                    result.setHumidity(yHumidity.get_currentValue());
                }
                YTemperature yTemperature = YTemperature.FirstTemperature();
                if (yTemperature != null) {
                    result.setTemperature(yTemperature.get_currentValue());
                }
                YPressure yPressure = YPressure.FirstPressure();
                if (yPressure != null) {
                    result.setPressure(yPressure.get_currentValue());
                }
                YLightSensor yLightSensor = YLightSensor.FirstLightSensor();
                if (yLightSensor != null) {
                    result.setLight(yLightSensor.get_currentValue());

                }
                // find Wake up Monitors of the YoctoHub-Wireless
                YWakeUpMonitor hub = YWakeUpMonitor.FirstWakeUpMonitor();
                if (hub != null) {
                    // create Pending intent to start this service again
                    Intent intent = new Intent(this, BgSensorsService.class);
                    PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    // compute next wake up time (seconds since EPOCH)
                    long now = System.currentTimeMillis() / 1000;
                    long nextWakeup = (now + WAKEUP_INTERVAL);
                    // Program the next wake for YoctoHub-Wireless
                    hub.set_nextWakeUp(nextWakeup);
                    // put the YoctoHub-Wireless in deep sleep until next wake up
                    hub.set_sleepCountdown(2);
                    // Program the Android Alarm manager to start this service again
                    // at the next wake up time.
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                        am.set(AlarmManager.RTC, nextWakeup * 1000, pendingIntent);
                    } else {
                        am.setExact(AlarmManager.RTC, nextWakeup * 1000, pendingIntent);
                    }
                }
                // release all resources used by the Yoctopuce API
                YAPI.FreeAPI();
            } catch (YAPI_Exception e) {
                e.printStackTrace();
            }
        }
        // disable the WiFi HotSpot to reduce power consumption
        setAPState(false);
        return result;
    }


    /**
     * Retrieve a String array with the IP of all device connected to the WiFi access point
     * this is done by parsing the content of /proc/net/arp
     *
     * @return the IP of all device connected to the WiFi access point
     */
    private ArrayList<String> apGetIPs()
    {
        ArrayList<String> res = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if ((splitted != null) && (splitted.length >= 4)) {
                    // Basic sanity check
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        res.add(splitted[0]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }


    /**
     * Enable or disable the Android WiFi HotSpot
     *
     * @param on true to enable it false to disable it.
     * @return true on success false on error
     */
    private boolean setAPState(boolean on)
    {
        _wifiManager.setWifiEnabled(false);
        //USE REFLECTION TO GET METHOD "SetWifiAPEnabled"
        try {
            Method method = _wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(_wifiManager, null, on);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Register/unregister a PendingIntent that will start this service
     *
     * @param ctx  a Android Context
     * @param isOn true register the service false to unregister the service
     */
    public static void SetServiceAlarm(Context ctx, boolean isOn)
    {
        Intent intent = new Intent(ctx, BgSensorsService.class);
        PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent, 0);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            // triger the next call as soon as possible.
            long nextWakeup = System.currentTimeMillis();
            // we use the alarm using RTC with exact time (otherwise we will probably be
            // out of sync with the YoctoHubWireless)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                am.set(AlarmManager.RTC, nextWakeup, pendingIntent);
            } else {
                am.setExact(AlarmManager.RTC, nextWakeup, pendingIntent);
            }
        } else {
            // cancel any pending call to the service
            if (pendingIntent != null) {
                am.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    /**
     * Check if there is a pending call to this service
     *
     * @param ctx a Android Context
     * @return true if a call to the service is pending
     */
    public static boolean isServiceAlarmOn(Context ctx)
    {
        Intent i = new Intent(ctx, BgSensorsService.class);
        PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }


}
