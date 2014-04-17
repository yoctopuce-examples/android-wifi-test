package com.example.wifitest.app;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SensorsValue {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
    private long _id;
    private double _humidity;
    private double _temperature;
    private double _pressure;
    private double _light;
    private long _timestamp;

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

    public double getHumidity()
    {
        return _humidity;
    }

    public void setHumidity(double humidity)
    {
        _humidity = humidity;
    }

    public double getTemperature()
    {
        return _temperature;
    }

    public void setTemperature(double temperature)
    {
        _temperature = temperature;
    }

    public double getPressure()
    {
        return _pressure;
    }

    public void setPressure(double pressure)
    {
        _pressure = pressure;
    }

    public double getLight()
    {
        return _light;
    }

    public void setLight(double light)
    {
        _light = light;
    }

    @Override
    public String toString()
    {
        Date date = new Date(_timestamp);
        return "time=" + sdf.format(date) +
                " temperature=" + _temperature +
                " humidity=" + _humidity +
                " pressure=" + _pressure +
                " light=" + _light;
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
