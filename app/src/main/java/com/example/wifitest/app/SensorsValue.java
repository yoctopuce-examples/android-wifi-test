package com.example.wifitest.app;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SensorsValue {

    private long _id;
    private double _humidity;
    private double _temperature;
    private double _pressure;
    private double _light;
    private long _timestamp;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");

    public SensorsValue()
    {
        _id = -1;
        _timestamp = System.currentTimeMillis();
    }


    public long getId()
    {
        return _id;
    }

    public void setId(long id)
    {
        _id = id;
    }

    public void setHumidity(double humidity)
    {
        _humidity = humidity;
    }

    public void setTemperature(double temperature)
    {
        _temperature = temperature;
    }

    public void setPressure(double pressure)
    {
        _pressure = pressure;
    }

    public void setLight(double light)
    {
        _light = light;
    }

    public double getHumidity()
    {
        return _humidity;
    }

    public double getTemperature()
    {
        return _temperature;
    }

    public double getPressure()
    {
        return _pressure;
    }

    public double getLight()
    {
        return _light;
    }




    @Override
    public String toString()
    {
        Date date = new Date(_timestamp);
        return "time=" + sdf.format(date) +
                " temperature=" + _temperature +
                " humidity=" + _humidity +
                " pressure=" + _pressure +
                " ilumination=" + _light;
    }

    public long getTime()
    {
        return _timestamp;
    }

    public long getTimestamp()
    {
        return _timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        _timestamp = timestamp;
    }
}
