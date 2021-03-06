/*********************************************************************
 *
 * $Id: pic24config.php 15525 2014-03-20 17:22:17Z seb $
 *
 * Implements yFindGenericSensor(), the high-level API for GenericSensor functions
 *
 * - - - - - - - - - License information: - - - - - - - - - 
 *
 *  Copyright (C) 2011 and beyond by Yoctopuce Sarl, Switzerland.
 *
 *  Yoctopuce Sarl (hereafter Licensor) grants to you a perpetual
 *  non-exclusive license to use, modify, copy and integrate this
 *  file into your software for the sole purpose of interfacing
 *  with Yoctopuce products.
 *
 *  You may reproduce and distribute copies of this file in
 *  source or object form, as long as the sole purpose of this
 *  code is to interface with Yoctopuce products. You must retain
 *  this notice in the distributed source file.
 *
 *  You should refer to Yoctopuce General Terms and Conditions
 *  for additional information regarding your rights and
 *  obligations.
 *
 *  THE SOFTWARE AND DOCUMENTATION ARE PROVIDED 'AS IS' WITHOUT
 *  WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING 
 *  WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO
 *  EVENT SHALL LICENSOR BE LIABLE FOR ANY INCIDENTAL, SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA,
 *  COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY OR 
 *  SERVICES, ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT 
 *  LIMITED TO ANY DEFENSE THEREOF), ANY CLAIMS FOR INDEMNITY OR
 *  CONTRIBUTION, OR OTHER SIMILAR COSTS, WHETHER ASSERTED ON THE
 *  BASIS OF CONTRACT, TORT (INCLUDING NEGLIGENCE), BREACH OF
 *  WARRANTY, OR OTHERWISE.
 *
 *********************************************************************/

package com.yoctopuce.YoctoAPI;
import org.json.JSONException;
import org.json.JSONObject;
import static com.yoctopuce.YoctoAPI.YAPI.SafeYAPI;

    //--- (YGenericSensor return codes)
    //--- (end of YGenericSensor return codes)
//--- (YGenericSensor class start)
/**
 * YGenericSensor Class: GenericSensor function interface
 * 
 * The Yoctopuce application programming interface allows you to read an instant
 * measure of the sensor, as well as the minimal and maximal values observed.
 */
public class YGenericSensor extends YSensor
{
//--- (end of YGenericSensor class start)
//--- (YGenericSensor definitions)
    /**
     * invalid signalValue value
     */
    public static final double SIGNALVALUE_INVALID = YAPI.INVALID_DOUBLE;
    /**
     * invalid signalUnit value
     */
    public static final String SIGNALUNIT_INVALID = YAPI.INVALID_STRING;
    /**
     * invalid signalRange value
     */
    public static final String SIGNALRANGE_INVALID = YAPI.INVALID_STRING;
    /**
     * invalid valueRange value
     */
    public static final String VALUERANGE_INVALID = YAPI.INVALID_STRING;
    protected double _signalValue = SIGNALVALUE_INVALID;
    protected String _signalUnit = SIGNALUNIT_INVALID;
    protected String _signalRange = SIGNALRANGE_INVALID;
    protected String _valueRange = VALUERANGE_INVALID;
    protected UpdateCallback _valueCallbackGenericSensor = null;
    protected TimedReportCallback _timedReportCallbackGenericSensor = null;

    /**
     * Deprecated UpdateCallback for GenericSensor
     */
    public interface UpdateCallback {
        /**
         * 
         * @param function      : the function object of which the value has changed
         * @param functionValue : the character string describing the new advertised value
         */
        void yNewValue(YGenericSensor function, String functionValue);
    }

    /**
     * TimedReportCallback for GenericSensor
     */
    public interface TimedReportCallback {
        /**
         * 
         * @param function : the function object of which the value has changed
         * @param measure  : measure
         */
        void timedReportCallback(YGenericSensor  function, YMeasure measure);
    }
    //--- (end of YGenericSensor definitions)


    /**
     * 
     * @param func : functionid
     */
    protected YGenericSensor(String func)
    {
        super(func);
        _className = "GenericSensor";
        //--- (YGenericSensor attributes initialization)
        //--- (end of YGenericSensor attributes initialization)
    }

    //--- (YGenericSensor implementation)
    @Override
    protected void  _parseAttr(JSONObject json_val) throws JSONException
    {
        if (json_val.has("signalValue")) {
            _signalValue =  json_val.getDouble("signalValue")/65536.0;
        }
        if (json_val.has("signalUnit")) {
            _signalUnit =  json_val.getString("signalUnit"); ;
        }
        if (json_val.has("signalRange")) {
            _signalRange =  json_val.getString("signalRange"); ;
        }
        if (json_val.has("valueRange")) {
            _valueRange =  json_val.getString("valueRange"); ;
        }
        super._parseAttr(json_val);
    }

    /**
     * Changes the measuring unit for the measured value.
     * Remember to call the saveToFlash() method of the module if the
     * modification must be kept.
     * 
     * @param newval : a string corresponding to the measuring unit for the measured value
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int set_unit(String  newval)  throws YAPI_Exception
    {
        String rest_val;
        rest_val = newval;
        _setAttr("unit",rest_val);
        return YAPI.SUCCESS;
    }

    /**
     * Changes the measuring unit for the measured value.
     * Remember to call the saveToFlash() method of the module if the
     * modification must be kept.
     * 
     * @param newval : a string corresponding to the measuring unit for the measured value
     * 
     * @return YAPI_SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int setUnit(String newval)  throws YAPI_Exception

    { return set_unit(newval); }

    /**
     * Returns the measured value of the electrical signal used by the sensor.
     * 
     * @return a floating point number corresponding to the measured value of the electrical signal used by the sensor
     * 
     * @throws YAPI_Exception
     */
    public double get_signalValue() throws YAPI_Exception
    {
        if (_cacheExpiration <= SafeYAPI().GetTickCount()) {
            if (load(YAPI.SafeYAPI().DefaultCacheValidity) != YAPI.SUCCESS) {
                return SIGNALVALUE_INVALID;
            }
        }
        return Math.round(_signalValue * 1000) / 1000;
    }

    /**
     * Returns the measured value of the electrical signal used by the sensor.
     * 
     * @return a floating point number corresponding to the measured value of the electrical signal used by the sensor
     * 
     * @throws YAPI_Exception
     */
    public double getSignalValue() throws YAPI_Exception

    { return get_signalValue(); }

    /**
     * Returns the measuring unit of the electrical signal used by the sensor.
     * 
     * @return a string corresponding to the measuring unit of the electrical signal used by the sensor
     * 
     * @throws YAPI_Exception
     */
    public String get_signalUnit() throws YAPI_Exception
    {
        if (_cacheExpiration == 0) {
            if (load(YAPI.SafeYAPI().DefaultCacheValidity) != YAPI.SUCCESS) {
                return SIGNALUNIT_INVALID;
            }
        }
        return _signalUnit;
    }

    /**
     * Returns the measuring unit of the electrical signal used by the sensor.
     * 
     * @return a string corresponding to the measuring unit of the electrical signal used by the sensor
     * 
     * @throws YAPI_Exception
     */
    public String getSignalUnit() throws YAPI_Exception

    { return get_signalUnit(); }

    /**
     * Returns the electric signal range used by the sensor.
     * 
     * @return a string corresponding to the electric signal range used by the sensor
     * 
     * @throws YAPI_Exception
     */
    public String get_signalRange() throws YAPI_Exception
    {
        if (_cacheExpiration <= SafeYAPI().GetTickCount()) {
            if (load(YAPI.SafeYAPI().DefaultCacheValidity) != YAPI.SUCCESS) {
                return SIGNALRANGE_INVALID;
            }
        }
        return _signalRange;
    }

    /**
     * Returns the electric signal range used by the sensor.
     * 
     * @return a string corresponding to the electric signal range used by the sensor
     * 
     * @throws YAPI_Exception
     */
    public String getSignalRange() throws YAPI_Exception

    { return get_signalRange(); }

    /**
     * Changes the electric signal range used by the sensor.
     * 
     * @param newval : a string corresponding to the electric signal range used by the sensor
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int set_signalRange(String  newval)  throws YAPI_Exception
    {
        String rest_val;
        rest_val = newval;
        _setAttr("signalRange",rest_val);
        return YAPI.SUCCESS;
    }

    /**
     * Changes the electric signal range used by the sensor.
     * 
     * @param newval : a string corresponding to the electric signal range used by the sensor
     * 
     * @return YAPI_SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int setSignalRange(String newval)  throws YAPI_Exception

    { return set_signalRange(newval); }

    /**
     * Returns the physical value range measured by the sensor.
     * 
     * @return a string corresponding to the physical value range measured by the sensor
     * 
     * @throws YAPI_Exception
     */
    public String get_valueRange() throws YAPI_Exception
    {
        if (_cacheExpiration <= SafeYAPI().GetTickCount()) {
            if (load(YAPI.SafeYAPI().DefaultCacheValidity) != YAPI.SUCCESS) {
                return VALUERANGE_INVALID;
            }
        }
        return _valueRange;
    }

    /**
     * Returns the physical value range measured by the sensor.
     * 
     * @return a string corresponding to the physical value range measured by the sensor
     * 
     * @throws YAPI_Exception
     */
    public String getValueRange() throws YAPI_Exception

    { return get_valueRange(); }

    /**
     * Changes the physical value range measured by the sensor. The range change may have a side effect
     * on the display resolution, as it may be adapted automatically.
     * 
     * @param newval : a string corresponding to the physical value range measured by the sensor
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int set_valueRange(String  newval)  throws YAPI_Exception
    {
        String rest_val;
        rest_val = newval;
        _setAttr("valueRange",rest_val);
        return YAPI.SUCCESS;
    }

    /**
     * Changes the physical value range measured by the sensor. The range change may have a side effect
     * on the display resolution, as it may be adapted automatically.
     * 
     * @param newval : a string corresponding to the physical value range measured by the sensor
     * 
     * @return YAPI_SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int setValueRange(String newval)  throws YAPI_Exception

    { return set_valueRange(newval); }

    /**
     * Retrieves a generic sensor for a given identifier.
     * The identifier can be specified using several formats:
     * <ul>
     * <li>FunctionLogicalName</li>
     * <li>ModuleSerialNumber.FunctionIdentifier</li>
     * <li>ModuleSerialNumber.FunctionLogicalName</li>
     * <li>ModuleLogicalName.FunctionIdentifier</li>
     * <li>ModuleLogicalName.FunctionLogicalName</li>
     * </ul>
     * 
     * This function does not require that the generic sensor is online at the time
     * it is invoked. The returned object is nevertheless valid.
     * Use the method YGenericSensor.isOnline() to test if the generic sensor is
     * indeed online at a given time. In case of ambiguity when looking for
     * a generic sensor by logical name, no error is notified: the first instance
     * found is returned. The search is performed first by hardware name,
     * then by logical name.
     * 
     * @param func : a string that uniquely characterizes the generic sensor
     * 
     * @return a YGenericSensor object allowing you to drive the generic sensor.
     */
    public static YGenericSensor FindGenericSensor(String func)
    {
        YGenericSensor obj;
        obj = (YGenericSensor) YFunction._FindFromCache("GenericSensor", func);
        if (obj == null) {
            obj = new YGenericSensor(func);
            YFunction._AddToCache("GenericSensor", func, obj);
        }
        return obj;
    }

    /**
     * Registers the callback function that is invoked on every change of advertised value.
     * The callback is invoked only during the execution of ySleep or yHandleEvents.
     * This provides control over the time when the callback is triggered. For good responsiveness, remember to call
     * one of these two functions periodically. To unregister a callback, pass a null pointer as argument.
     * 
     * @param callback : the callback function to call, or a null pointer. The callback function should take two
     *         arguments: the function object of which the value has changed, and the character string describing
     *         the new advertised value.
     * @noreturn
     */
    public int registerValueCallback(UpdateCallback callback)
    {
        String val;
        if (callback != null) {
            YFunction._UpdateValueCallbackList(this, true);
        } else {
            YFunction._UpdateValueCallbackList(this, false);
        }
        _valueCallbackGenericSensor = callback;
        // Immediately invoke value callback with current value
        if (callback != null && isOnline()) {
            val = _advertisedValue;
            if (!(val.equals(""))) {
                _invokeValueCallback(val);
            }
        }
        return 0;
    }

    @Override
    public int _invokeValueCallback(String value)
    {
        if (_valueCallbackGenericSensor != null) {
            _valueCallbackGenericSensor.yNewValue(this, value);
        } else {
            super._invokeValueCallback(value);
        }
        return 0;
    }

    /**
     * Registers the callback function that is invoked on every periodic timed notification.
     * The callback is invoked only during the execution of ySleep or yHandleEvents.
     * This provides control over the time when the callback is triggered. For good responsiveness, remember to call
     * one of these two functions periodically. To unregister a callback, pass a null pointer as argument.
     * 
     * @param callback : the callback function to call, or a null pointer. The callback function should take two
     *         arguments: the function object of which the value has changed, and an YMeasure object describing
     *         the new advertised value.
     * @noreturn
     */
    public int registerTimedReportCallback(TimedReportCallback callback)
    {
        if (callback != null) {
            YFunction._UpdateTimedReportCallbackList(this, true);
        } else {
            YFunction._UpdateTimedReportCallbackList(this, false);
        }
        _timedReportCallbackGenericSensor = callback;
        return 0;
    }

    @Override
    public int _invokeTimedReportCallback(YMeasure value)
    {
        if (_timedReportCallbackGenericSensor != null) {
            _timedReportCallbackGenericSensor.timedReportCallback(this, value);
        } else {
            super._invokeTimedReportCallback(value);
        }
        return 0;
    }

    /**
     * Continues the enumeration of generic sensors started using yFirstGenericSensor().
     * 
     * @return a pointer to a YGenericSensor object, corresponding to
     *         a generic sensor currently online, or a null pointer
     *         if there are no more generic sensors to enumerate.
     */
    public  YGenericSensor nextGenericSensor()
    {
        String next_hwid;
        try {
            String hwid = SafeYAPI().resolveFunction(_className, _func).getHardwareId();
            next_hwid = SafeYAPI().getNextHardwareId(_className, hwid);
        } catch (YAPI_Exception ignored) {
            next_hwid = null;
        }
        if(next_hwid == null) return null;
        return FindGenericSensor(next_hwid);
    }

    /**
     * Starts the enumeration of generic sensors currently accessible.
     * Use the method YGenericSensor.nextGenericSensor() to iterate on
     * next generic sensors.
     * 
     * @return a pointer to a YGenericSensor object, corresponding to
     *         the first generic sensor currently online, or a null pointer
     *         if there are none.
     */
    public static YGenericSensor FirstGenericSensor()
    {
        String next_hwid = SafeYAPI().getFirstHardwareId("GenericSensor");
        if (next_hwid == null)  return null;
        return FindGenericSensor(next_hwid);
    }

    //--- (end of YGenericSensor implementation)
}

