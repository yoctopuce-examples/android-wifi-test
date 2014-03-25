package com.example.wifitest.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;


public class MainActivity extends ActionBarActivity {

    private static final String DEBUG_TAG = "WIFITEST";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

    public void updateInFg(View view)
    {
        BgSensorsService.takePicture(this, new SensorsValue());
    }


    public void updateInBg(View view)
    {
        BgSensorsService.takePicture(this, new SensorsValue());
        Intent i = new Intent(this, BgSensorsService.class);
        startService(i);
    }



}

