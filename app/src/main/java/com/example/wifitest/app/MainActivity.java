package com.example.wifitest.app;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YHumidity;
import com.yoctopuce.YoctoAPI.YLightSensor;
import com.yoctopuce.YoctoAPI.YModule;
import com.yoctopuce.YoctoAPI.YPressure;
import com.yoctopuce.YoctoAPI.YTemperature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    private static final String DEBUG_TAG = "WIFITEST";
    private TextView _textViewResult;
    private WifiManager _wifiManager;
    private TextView _humitidtyTextView;
    private TextView _luminosity;
    private static final long NETWORK_DETECTION_TIMEOUT_MS = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _humitidtyTextView = (TextView) findViewById(R.id.humidity);
        _luminosity = (TextView) findViewById(R.id.luminosity);
        _textViewResult = (TextView) findViewById(R.id.result);

        Switch bgControl = (Switch) findViewById(R.id.bgcontrol);
        bgControl.setChecked(BgSensorsService.isServiceAlarmOn(getApplicationContext()));
        bgControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked) {
                    BgSensorsService.SetServiceAlarm(getApplicationContext(), true);
                } else {
                    BgSensorsService.SetServiceAlarm(getApplicationContext(), false);
                }
            }
        });

        _wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

    }

    public void updateInBg(View view)
    {
        Intent i = new Intent(this, BgSensorsService.class);
        startService(i);
    }

    public void update(View view)
    {
        String[] serials = new String[2];
        new GetMeasureBG().execute(serials);
    }

    public void takePicture(View view)
    {


        GBTakePictureNoPreview c = new GBTakePictureNoPreview(this);
        c.setLandscape();
        //c.setUseFrontCamera(false);

        //here,we are making a folder named picFolder to store pics taken by the camera using this application
        File pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String fileName = pictureDir + "/" + date + ".jpg";
        c.setFileName(fileName);
        if (c.cameraIsOk()) {
            c.takePicture();
        }
        Toast.makeText(this, "new photo "+fileName,Toast.LENGTH_LONG).show();

//        Bitmap bmp = BitmapFactory.decodeByteArray(myArray, 0, myArray.length).copy(Bitmap.Config.RGBA_8888, true); //myArray is the byteArray containing the image. Use copy() to create a mutable bitmap. Feel free to change the config-type. Consider doing this in two steps so you can recycle() the immutable bitmap.
//        Canvas canvas = new Canvas(bmp);
//        canvas.drawText("Hello Image", xposition, yposition, textpaint); //x/yposition is where the text will be drawn. textpaint is the Paint object to draw with.
//
//        OutputStream os = new FileOutputStream(dstfile); //dstfile is a File-object that you want to save to. You probably need to add some exception-handling here.
//        bmp.compress(Bitmap.CompressFormat.JPEG, 100, os); //Output as JPG with maximum quality.
//        try {
//            os.flush();
//            os.close();//Don't forget to close the stream.
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this,
                new String[]{fileName}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri)
                    {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                }
        );
    }




    void createExternalStoragePublicPicture() {
        // Create a path where we will place our picture in the user's
        // public pictures directory.  Note that you should be careful about
        // what you place here, since the user often manages these files.  For
        // pictures and other media owned by the application, consider
        // Context.getExternalMediaDir().
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture2.jpg");

        try {
            // Make sure the Pictures directory exists.
            path.mkdirs();

            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.
            InputStream is = getResources().openRawResource(R.drawable.ic_launcher);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();


            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
//            MediaScannerConnection.scanFile(this,
//                    new String[]{file.toString()}, null,
//                    new MediaScannerConnection.OnScanCompletedListener() {
//                        public void onScanCompleted(String path, Uri uri)
//                        {
//                            Log.i("ExternalStorage", "Scanned " + path + ":");
//                            Log.i("ExternalStorage", "-> uri=" + uri);
//                        }
//                    }
//            );
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }




    private class GetMeasureBG extends AsyncTask<String, String, SensorsValue> {

        @Override
        protected SensorsValue doInBackground(String[] serials)
        {
            SensorsValue result = new SensorsValue();

            publishProgress("Activate Wifi HotSpot");

            //Activate Wifi HotSpot
            if (!setAPState(true)) {
                return null;
            }
            boolean ok = false;
            long timeout = System.currentTimeMillis() + NETWORK_DETECTION_TIMEOUT_MS;
            publishProgress("Wait for YoctoHub-Wireless");
            do {
                ArrayList<String> ips = APGetIP();
                for (String ip : ips) {
                    try {
                        YAPI.RegisterHub(ip);
                        ok = true;
                    } catch (YAPI_Exception e) {
                        YAPI.UnregisterHub(ip);
                    }

                }
            } while (!ok && timeout > System.currentTimeMillis());


            publishProgress("YoctoHub-Wireless found");
            try {
                String yoctoMeteoSerial = null, yoctoLightSerial = null;
                YModule module = YModule.FirstModule();
                while (module != null) {
                    if (module.get_productName().equals("Yocto-Meteo")) {
                        publishProgress("Yocto-Meteo Found");
                        yoctoMeteoSerial = module.get_serialNumber();
                    } else if (module.get_productName().equals("Yocto-Light")) {
                        publishProgress("Yocto-Light Found");
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
                publishProgress("Free Yoctopuce API");
                YAPI.FreeAPI();
            } catch (YAPI_Exception e) {
                publishProgress("YAPI_Exception " + e.getLocalizedMessage());
                e.printStackTrace();
                return null;
            } finally {
                publishProgress("Disable Wifi HotSpot");
                setAPState(false);
            }
            return result;
        }


        @Override
        protected void onProgressUpdate(String... values)
        {
            for (String val : values) {
                _textViewResult.append(val + "\n");
            }


        }

        @Override
        protected void onPostExecute(SensorsValue sensorsValue)
        {
            if (sensorsValue != null) {
                _humitidtyTextView.setText(String.format("%f %%", sensorsValue.getHumidity()));
                _luminosity.setText(String.format("%f lux", sensorsValue.getIlumination()));
            } else {
                _humitidtyTextView.setText("error");
                _luminosity.setText("error");

            }

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


}

