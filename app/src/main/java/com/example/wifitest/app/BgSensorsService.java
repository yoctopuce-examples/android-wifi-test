package com.example.wifitest.app;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YHumidity;
import com.yoctopuce.YoctoAPI.YLightSensor;
import com.yoctopuce.YoctoAPI.YModule;
import com.yoctopuce.YoctoAPI.YPressure;
import com.yoctopuce.YoctoAPI.YTemperature;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class BgSensorsService extends IntentService {

    private static final String TAG = "BgSensorsService";
    private static final long NETWORK_DETECTION_TIMEOUT_MS = 600000; // 60 minutes
    private static final long START_INTERVAL = 6000000;// 60 minutes
    private WifiManager _wifiManager;
    private SensorDatabaseHelper _sensorDatabaseHelper;

    public BgSensorsService()
    {
        super(TAG);
        Log.i(TAG, "New instance");
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
        Log.i(TAG, "intent received" + intent);
        SensorsValue sensorsValue = GetSensorsValue();
        Log.i(TAG, "Sensors values" + sensorsValue.toString());
        _sensorDatabaseHelper.insertSensorValue(sensorsValue);
    }

    public static void SetServiceAlarm(Context ctx, boolean isOn)
    {
        Intent intent = new Intent(ctx, BgSensorsService.class);
        PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent, 0);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), START_INTERVAL, pendingIntent);
        } else {
            if (pendingIntent != null) {
                am.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    public static boolean isServiceAlarmOn(Context ctx) {
        Intent i = new Intent(ctx, BgSensorsService.class);
        PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }




    private SensorsValue GetSensorsValue()
    {
        SensorsValue result = new SensorsValue();
        //Activate Wifi HotSpot
        if (!setAPState(true)) {
            return result;
        }
        boolean ok = false;
        long timeout = System.currentTimeMillis() + NETWORK_DETECTION_TIMEOUT_MS;
        Log.i(TAG, "Detected ip:");
        do {
            ArrayList<String> ips = APGetIP();

            for (String ip : ips) {
                Log.i(TAG, "   "+ip);
                try {
                    YAPI.RegisterHub(ip);
                    ok = true;
                } catch (YAPI_Exception e) {
                    YAPI.UnregisterHub(ip);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!ok && timeout > System.currentTimeMillis());

        try {
            String yoctoMeteoSerial = null, yoctoLightSerial = null;
            YModule module = YModule.FirstModule();
            while (module != null) {
                if (module.get_productName().equals("Yocto-Meteo")) {
                    yoctoMeteoSerial = module.get_serialNumber();
                } else if (module.get_productName().equals("Yocto-Light")) {
                    yoctoLightSerial = module.get_serialNumber();
                }
                module = module.nextModule();
            }

            if (yoctoMeteoSerial != null) {
                YHumidity yHumidity = YHumidity.FindHumidity(yoctoMeteoSerial + ".humidity");
                result.setHumidity(yHumidity.get_currentValue());
                YTemperature yTemperature = YTemperature.FindTemperature(yoctoMeteoSerial + ".temperature");
                result.setTemperature(yTemperature.get_currentValue());
                YPressure yPressure = YPressure.FindPressure(yoctoMeteoSerial + ".pressure");
                result.setPressure(yPressure.get_currentValue());
            }
            if (yoctoLightSerial != null) {
                YLightSensor yLightSensor = YLightSensor.FindLightSensor(yoctoLightSerial + ".lightSensor");
                result.setIlumination(yLightSensor.get_currentValue());
            }
            YAPI.FreeAPI();
        } catch (YAPI_Exception e) {
            e.printStackTrace();
        } finally {
            setAPState(false);
        }
        return result;
    }

    private ArrayList<String> APGetIP()
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
