package com.example.wifitest.app;

/**
 * Created by seb on 17.03.14.
 */
public class SensorsValue {
    private double _humidity;
    private double _temperature;
    private double _pressure;
    private double _ilumination;

    public SensorsValue()
    { }


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

    public void setIlumination(double ilumination)
    {
        _ilumination = ilumination;
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

    public double getIlumination()
    {
        return _ilumination;
    }

    @Override
    public String toString()
    {
        return "SensorsValue{" +
                "_humidity=" + _humidity +
                ", _temperature=" + _temperature +
                ", _pressure=" + _pressure +
                ", _ilumination=" + _ilumination +
                '}';
    }
}
