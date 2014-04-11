package com.example.wifitest.app;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YHumidity;
import com.yoctopuce.YoctoAPI.YLightSensor;
import com.yoctopuce.YoctoAPI.YPressure;
import com.yoctopuce.YoctoAPI.YTemperature;
import com.yoctopuce.YoctoAPI.YWakeUpMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BgSensorsService extends IntentService {

    public static final String ACTION_NEW_SENSORS_VALUE = "ACTION_NEW_SENSORS_VALUE";
    public static final String PREF_WAKE_UP_INTERVAL = "wakeUpInterval";
    public static final String PREF_ALERT_PHONE_NUMBER = "alertPhoneNumber";
    public static final String PREF_DETECTION_TIMEOUT = "detectionTimeout";
    public static final String PREF_LAST_BATTERY_LEVEL = "lastBatteryLevel";
    public static final String PREF_LAST_GET_SENSOR_FAILED = "lastGetSensorFailed";
    private static final String TAG = "BgSensorsService";
    private static final int RETRY_DELAY = 5 * 60000;
    private WifiManager _wifiManager;
    private SensorDatabaseHelper _sensorDatabaseHelper;
    private DateFormat _df;

    public BgSensorsService()
    {
        super(TAG);
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
            // out of sync with the YoctoHub-Wireless)
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

    @Override
    public void onCreate()
    {
        super.onCreate();
        _wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        _sensorDatabaseHelper = SensorDatabaseHelper.get(getApplicationContext());
        _df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    }

    protected void logToFile(String msg)
    {
        PrintWriter pw = null;
        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    SendSMS("unable to create " + path.getAbsolutePath());
                }
                // initiate media scan and put the new things into the path array to
                // make the scanner aware of the location and the files you want to see
            }
            String filename = path.getAbsolutePath() + "/wifitest.log";
            MediaScannerConnection.scanFile(this, new String[]{filename}, null, null);
            pw = new PrintWriter(
                    new FileWriter(filename, true));
            pw.append(_df.format(new Date())).append(":").append(msg).append("\n");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {

        // retrieve values form all sensors in a efficient way
        SensorsValue sensorsValue = GetSensorsValue();
        if (sensorsValue != null) {
            // save it to the database
            _sensorDatabaseHelper.insertSensorValue(sensorsValue);
        }
        checkBatteryLevel();

        // notify all listeners that we have a new value in the database
        sendBroadcast(new Intent(ACTION_NEW_SENSORS_VALUE));
    }

    private void SendSMS(String sms)
    {
        String phoneNo = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_ALERT_PHONE_NUMBER, null);
        if (phoneNo == null) {
            return;
        }
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, sms, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkBatteryLevel()
    {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        if (batteryStatus == null) {
            return;
        }
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 100);

        int batteryPct = level * 100 / scale;
        logToFile("battery:" + batteryPct + "%");
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int lastBattery = defaultSharedPreferences.getInt(PREF_LAST_BATTERY_LEVEL, 100);

        if (lastBattery <= 5 && batteryPct < 5) {
            //send sms
            SendSMS("low battery");
        }
        defaultSharedPreferences.edit().putInt(PREF_LAST_BATTERY_LEVEL, batteryPct).commit();
    }

    /**
     * Retrieves Values for of all Yoctopuce sensors connected
     *
     * @return a SensorValue object with all value set
     */
    private SensorsValue GetSensorsValue()
    {
        //Activate WiFi HotSpot
        if (!setAPState(true)) {
            programNextServiceStart(System.currentTimeMillis() + RETRY_DELAY);
            return null;
        }
        SensorsValue result = null;
        Date startDate = new Date();
        Date detected = null;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int detectionTimeout = getDetectionTimout(defaultSharedPreferences);
        boolean lastCallFailed = defaultSharedPreferences.getBoolean(PREF_LAST_GET_SENSOR_FAILED, false);
        long timeout = System.currentTimeMillis() + detectionTimeout * 1000;
        // wait at max _detectionTimeout seconds for the YoctoHub-Wireless to connect
        // to the Wifi WiFi HotSpot
        do {
            ArrayList<String> ips = apGetIPs();
            // test all IP that are connected to the WiFi HotSpot
            for (String ip : ips) {
                try {
                    // test if this IP is a YoctoHub-Wireless
                    YAPI.RegisterHub(ip);
                    detected = new Date();
                } catch (YAPI_Exception e) {
                    // the IP is not Yoctopuce hub
                    YAPI.UnregisterHub(ip);
                }
            }
            if (detected == null) {
                // if the YoctoHub-Wireless is not here sleep 1 second
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } while (detected == null && timeout > System.currentTimeMillis());

        if (detected != null) {
            long popTime = detected.getTime() - startDate.getTime();
            popTime /= 1000;
            logToFile("YoctoHub poped after:" + popTime + " seconds (started " + _df.format(startDate) + ")");

            result = new SensorsValue();
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
                    // compute next wake up time (seconds since EPOCH)
                    long now = System.currentTimeMillis() / 1000;
                    int wakeupInterval = getWakeUpInterval(defaultSharedPreferences);
                    long nextWakeUp = (now + wakeupInterval);
                    hub.sleepFor(wakeupInterval, 2);
                    programNextServiceStart(nextWakeUp * 1000);
                    logToFile("Call programed for " + _df.format(new Date(nextWakeUp * 1000)));
                }
                // release all resources used by the Yoctopuce API
                YAPI.FreeAPI();
            } catch (YAPI_Exception e) {
                e.printStackTrace();
                logToFile(e.getStackTraceToString());
                programNextServiceStart(System.currentTimeMillis() + RETRY_DELAY);
            }
            if (lastCallFailed) {
                SendSMS("Recover from last error");
                defaultSharedPreferences.edit().putBoolean(PREF_LAST_GET_SENSOR_FAILED, false).commit();
            }
        } else {
            String msg = "Hub did not show up after " + detectionTimeout + " seconds (started " + _df.format(startDate) + ")";
            logToFile(msg);
            //double the detection timeout
            detectionTimeout *= 2;
            SharedPreferences.Editor edit = defaultSharedPreferences.edit();
            edit.putString(PREF_DETECTION_TIMEOUT, Integer.toString(detectionTimeout));
            if (!lastCallFailed) {
                SendSMS(msg);
                edit.putBoolean(PREF_LAST_GET_SENSOR_FAILED, true);
            }
            edit.commit();
            long nextWakeUpMs = System.currentTimeMillis() + RETRY_DELAY;
            logToFile("retry programed for " + _df.format(new Date(nextWakeUpMs)));
            programNextServiceStart(nextWakeUpMs);
        }
        // disable the WiFi HotSpot to reduce power consumption
        setAPState(false);
        return result;
    }

    private int getWakeUpInterval(SharedPreferences defaultSharedPreferences)
    {
        String wakeUpStr = defaultSharedPreferences.getString(PREF_WAKE_UP_INTERVAL, null);
        return Integer.getInteger(wakeUpStr, 1200);
    }

    private int getDetectionTimout(SharedPreferences defaultSharedPreferences)
    {
        String timeoutStr = defaultSharedPreferences.getString(PREF_DETECTION_TIMEOUT, null);
        return Integer.getInteger(timeoutStr, 300);
    }

    private void programNextServiceStart(long wakeUpTimeMs)
    {
        // create Pending intent to start this service again
        Intent intent = new Intent(this, BgSensorsService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // at the next wake up time.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            am.set(AlarmManager.RTC_WAKEUP, wakeUpTimeMs, pendingIntent);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, wakeUpTimeMs, pendingIntent);
        }
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


}
