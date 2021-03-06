/*********************************************************************
 *
 * $Id: YFunction.java 15505 2014-03-19 17:05:31Z seb $
 *
 * YFunction Class (virtual class, used internally)
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.yoctopuce.YoctoAPI.YAPI.SafeYAPI;


//--- (generated code: YFunction class start)
/**
 * YFunction Class: Common function interface
 * 
 * This is the parent class for all public objects representing device functions documented in
 * the high-level programming API. This abstract class does all the real job, but without
 * knowledge of the specific function attributes.
 * 
 * Instantiating a child class of YFunction does not cause any communication.
 * The instance simply keeps track of its function identifier, and will dynamically bind
 * to a matching device at the time it is really being used to read or set an attribute.
 * In order to allow true hot-plug replacement of one device by another, the binding stay
 * dynamic through the life of the object.
 * 
 * The YFunction class implements a generic high-level cache for the attribute values of
 * the specified function, pre-parsed from the REST API string.
 */
public class YFunction
{
//--- (end of generated code: YFunction class start)

    public static final String FUNCTIONDESCRIPTOR_INVALID = "!INVALID!";

    protected String _className;
    protected String _func;
    protected int _lastErrorType;
    protected String _lastErrorMsg;
    protected Object _userData;
    protected HashMap<String, YDataStream> _dataStreams;
    //--- (generated code: YFunction definitions)
    /**
     * invalid logicalName value
     */
    public static final String LOGICALNAME_INVALID = YAPI.INVALID_STRING;
    /**
     * invalid advertisedValue value
     */
    public static final String ADVERTISEDVALUE_INVALID = YAPI.INVALID_STRING;
    protected String _logicalName = LOGICALNAME_INVALID;
    protected String _advertisedValue = ADVERTISEDVALUE_INVALID;
    protected UpdateCallback _valueCallbackFunction = null;
    protected long _cacheExpiration = 0;
    protected String _serial;
    protected String _funId;
    protected String _hwId;

    /**
     * Deprecated UpdateCallback for Function
     */
    public interface UpdateCallback {
        /**
         * 
         * @param function      : the function object of which the value has changed
         * @param functionValue : the character string describing the new advertised value
         */
        void yNewValue(YFunction function, String functionValue);
    }

    /**
     * TimedReportCallback for Function
     */
    public interface TimedReportCallback {
        /**
         * 
         * @param function : the function object of which the value has changed
         * @param measure  : measure
         */
        void timedReportCallback(YFunction  function, YMeasure measure);
    }
    //--- (end of generated code: YFunction definitions)



    /**
     *
     * @param func : functionid
     */
    protected YFunction(String func)
    {
        _className = "Function";
        _func = func;
        _lastErrorType = YAPI.SUCCESS;
        _lastErrorMsg = "";
        _userData = null;
        _dataStreams = new HashMap<String, YDataStream>();
        //--- (generated code: YFunction attributes initialization)
        //--- (end of generated code: YFunction attributes initialization)
    }

    protected void _throw(int error,String message) throws YAPI_Exception {
        throw new YAPI_Exception(error, message);
    }

    protected static YFunction _FindFromCache(String className, String func)
    {
        return SafeYAPI().getFunction(className, func);
    }

    protected static void _AddToCache(String className, String func, YFunction obj)
    {
        SafeYAPI().setFunction(className, func, obj);
    }


    protected static void _UpdateValueCallbackList(YFunction func, boolean add)
    {
        SafeYAPI()._UpdateValueCallbackList(func, add);
    }




    protected static void _UpdateTimedReportCallbackList(YFunction func, boolean add)
    {
        SafeYAPI()._UpdateTimedReportCallbackList(func, add);
    }


    //--- (generated code: YFunction implementation)
    protected void  _parseAttr(JSONObject json_val) throws JSONException
    {
        if (json_val.has("logicalName")) {
            _logicalName =  json_val.getString("logicalName"); ;
        }
        if (json_val.has("advertisedValue")) {
            _advertisedValue =  json_val.getString("advertisedValue"); ;
        }
    }

    /**
     * Returns the logical name of the function.
     * 
     * @return a string corresponding to the logical name of the function
     * 
     * @throws YAPI_Exception
     */
    public String get_logicalName() throws YAPI_Exception
    {
        if (_cacheExpiration <= SafeYAPI().GetTickCount()) {
            if (load(YAPI.SafeYAPI().DefaultCacheValidity) != YAPI.SUCCESS) {
                return LOGICALNAME_INVALID;
            }
        }
        return _logicalName;
    }

    /**
     * Returns the logical name of the function.
     * 
     * @return a string corresponding to the logical name of the function
     * 
     * @throws YAPI_Exception
     */
    public String getLogicalName() throws YAPI_Exception

    { return get_logicalName(); }

    /**
     * Changes the logical name of the function. You can use yCheckLogicalName()
     * prior to this call to make sure that your parameter is valid.
     * Remember to call the saveToFlash() method of the module if the
     * modification must be kept.
     * 
     * @param newval : a string corresponding to the logical name of the function
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int set_logicalName(String  newval)  throws YAPI_Exception
    {
        String rest_val;
        rest_val = newval;
        _setAttr("logicalName",rest_val);
        return YAPI.SUCCESS;
    }

    /**
     * Changes the logical name of the function. You can use yCheckLogicalName()
     * prior to this call to make sure that your parameter is valid.
     * Remember to call the saveToFlash() method of the module if the
     * modification must be kept.
     * 
     * @param newval : a string corresponding to the logical name of the function
     * 
     * @return YAPI_SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int setLogicalName(String newval)  throws YAPI_Exception

    { return set_logicalName(newval); }

    /**
     * Returns the current value of the function (no more than 6 characters).
     * 
     * @return a string corresponding to the current value of the function (no more than 6 characters)
     * 
     * @throws YAPI_Exception
     */
    public String get_advertisedValue() throws YAPI_Exception
    {
        if (_cacheExpiration <= SafeYAPI().GetTickCount()) {
            if (load(YAPI.SafeYAPI().DefaultCacheValidity) != YAPI.SUCCESS) {
                return ADVERTISEDVALUE_INVALID;
            }
        }
        return _advertisedValue;
    }

    /**
     * Returns the current value of the function (no more than 6 characters).
     * 
     * @return a string corresponding to the current value of the function (no more than 6 characters)
     * 
     * @throws YAPI_Exception
     */
    public String getAdvertisedValue() throws YAPI_Exception

    { return get_advertisedValue(); }

    /**
     * Retrieves a function for a given identifier.
     * The identifier can be specified using several formats:
     * <ul>
     * <li>FunctionLogicalName</li>
     * <li>ModuleSerialNumber.FunctionIdentifier</li>
     * <li>ModuleSerialNumber.FunctionLogicalName</li>
     * <li>ModuleLogicalName.FunctionIdentifier</li>
     * <li>ModuleLogicalName.FunctionLogicalName</li>
     * </ul>
     * 
     * This function does not require that the function is online at the time
     * it is invoked. The returned object is nevertheless valid.
     * Use the method YFunction.isOnline() to test if the function is
     * indeed online at a given time. In case of ambiguity when looking for
     * a function by logical name, no error is notified: the first instance
     * found is returned. The search is performed first by hardware name,
     * then by logical name.
     * 
     * @param func : a string that uniquely characterizes the function
     * 
     * @return a YFunction object allowing you to drive the function.
     */
    public static YFunction FindFunction(String func)
    {
        YFunction obj;
        obj = (YFunction) YFunction._FindFromCache("Function", func);
        if (obj == null) {
            obj = new YFunction(func);
            YFunction._AddToCache("Function", func, obj);
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
        _valueCallbackFunction = callback;
        // Immediately invoke value callback with current value
        if (callback != null && isOnline()) {
            val = _advertisedValue;
            if (!(val.equals(""))) {
                _invokeValueCallback(val);
            }
        }
        return 0;
    }

    public int _invokeValueCallback(String value)
    {
        if (_valueCallbackFunction != null) {
            _valueCallbackFunction.yNewValue(this, value);
        } else {
        }
        return 0;
    }

    public int _parserHelper()
    {
        return 0;
    }

    /**
     * comment from .yc definition
     */
    public  YFunction nextFunction()
    {
        String next_hwid;
        try {
            String hwid = SafeYAPI().resolveFunction(_className, _func).getHardwareId();
            next_hwid = SafeYAPI().getNextHardwareId(_className, hwid);
        } catch (YAPI_Exception ignored) {
            next_hwid = null;
        }
        if(next_hwid == null) return null;
        return FindFunction(next_hwid);
    }

    /**
     * comment from .yc definition
     */
    public static YFunction FirstFunction()
    {
        String next_hwid = SafeYAPI().getFirstHardwareId("Function");
        if (next_hwid == null)  return null;
        return FindFunction(next_hwid);
    }

    //--- (end of generated code: YFunction implementation)

    /**
     * Returns a short text that describes unambiguously the instance of the function in the form
     * TYPE(NAME)=SERIAL&#46;FUNCTIONID.
     * More precisely,
     * TYPE       is the type of the function,
     * NAME       it the name used for the first access to the function,
     * SERIAL     is the serial number of the module if the module is connected or "unresolved", and
     * FUNCTIONID is  the hardware identifier of the function if the module is connected.
     * For example, this method returns Relay(MyCustomName.relay1)=RELAYLO1-123456.relay1 if the
     * module is already connected or Relay(BadCustomeName.relay1)=unresolved if the module has
     * not yet been connected. This method does not trigger any USB or TCP transaction and can therefore be used in
     * a debugger.
     * 
     * @return a string that describes the function
     *         (ex: Relay(MyCustomName.relay1)=RELAYLO1-123456.relay1)
     */
    public String describe()
    {
        try {
            String hwid = SafeYAPI().resolveFunction(_className, _func).getHardwareId();
            return _className + "(" + _func + ")="+hwid;
        } catch (YAPI_Exception ignored) {
        }
        return _className + "(" + _func + ")=unresolved";

    }


    /**
     * Returns the unique hardware identifier of the function in the form SERIAL.FUNCTIONID.
     * The unique hardware identifier is composed of the device serial
     * number and of the hardware identifier of the function. (for example RELAYLO1-123456.relay1)
     * 
     * @return a string that uniquely identifies the function (ex: RELAYLO1-123456.relay1)
     * 
     * @throws YAPI_Exception
     */
    public String get_hardwareId() throws YAPI_Exception
    {
        return SafeYAPI().resolveFunction(_className, _func).getHardwareId();
    }

    public String getHardwareId() throws YAPI_Exception
    {
        return SafeYAPI().resolveFunction(_className, _func).getHardwareId();
    }


    /**
     * Returns the hardware identifier of the function, without reference to the module. For example
     * relay1
     * 
     * @return a string that identifies the function (ex: relay1)
     * 
     * @throws YAPI_Exception
     */
    public String get_functionId() throws YAPI_Exception
    {
        return SafeYAPI().resolveFunction(_className, _func).getFuncId();
    }

    public String getFunctionId() throws YAPI_Exception
    {
        return SafeYAPI().resolveFunction(_className, _func).getFuncId();
    }


    /**
     * Returns a global identifier of the function in the format MODULE_NAME&#46;FUNCTION_NAME.
     * The returned string uses the logical names of the module and of the function if they are defined,
     * otherwise the serial number of the module and the hardware identifier of the function
     * (for exemple: MyCustomName.relay1)
     * 
     * @return a string that uniquely identifies the function using logical names
     *         (ex: MyCustomName.relay1)
     * 
     * @throws YAPI_Exception
     */
    public String get_friendlyName() throws YAPI_Exception
    {
        YPEntry yp = SafeYAPI().resolveFunction(_className, _func);
        return yp.getFriendlyName();
    }

    public String getFriendlyName() throws YAPI_Exception
    {
        return get_friendlyName();
    }


    @Override
    public String toString()
    {
        return describe();
    }

    protected void  _parse(JSONObject json, long msValidity) throws YAPI_Exception {
        _cacheExpiration = YAPI.GetTickCount()+ msValidity;
        try {
            _parseAttr(json);
        } catch (JSONException e) {
            _throw(YAPI.IO_ERROR,e.getMessage());
        }
        _parserHelper();
    }




    // Change the value of an attribute on a device, and update cache on the fly
    // Note: the function cache is a typed (parsed) cache, contrarily to the agnostic device cache
    protected int _setAttr(String attr, String newval) throws YAPI_Exception
    {
        if (newval == null) {
            throw new YAPI_Exception(YAPI.INVALID_ARGUMENT, "Undefined value to set for attribute " + attr);
        }
        String attrname;
        String extra;
        try {
            attrname = URLEncoder.encode(attr, "ISO-8859-1");
            extra = "/" + attrname + "?" + attrname + "=" + URLEncoder.encode(newval, "ISO-8859-1");
            extra = extra.replaceAll("%21", "!")
                    .replaceAll("%23", "#").replaceAll("%24", "$")
                    .replaceAll("%27", "'").replaceAll("%28", "(").replaceAll("%29", ")")
                    .replaceAll("%2C", ",").replaceAll("%2F", "/")
                    .replaceAll("%3A", ":").replaceAll("%3B", ";").replaceAll("%3F", "?")
                    .replaceAll("%40", "@").replaceAll("%5B", "[").replaceAll("%5D", "]");
        } catch (UnsupportedEncodingException ex) {
            throw new YAPI_Exception(YAPI.INVALID_ARGUMENT, "Unsupported Encoding");
        }
        _devRequest(extra);
        if (_cacheExpiration!=0) {
            _cacheExpiration = YAPI.GetTickCount();
        }
        return YAPI.SUCCESS;
    }



    private byte[] _request(String req_first_line,byte[] req_head_and_body) throws YAPI_Exception
    {
        YDevice dev = SafeYAPI().funcGetDevice(_className, _func);
        return dev.requestHTTP(req_first_line,req_head_and_body, false);
    }

    protected int _upload(String path, byte[] content) throws YAPI_Exception
    {
        Random randomGenerator = new Random();
        String boundary;
        String request = "POST /upload.html HTTP/1.1\r\n";
        String mp_header = "Content-Disposition: form-data; name=\""+path+"\"; filename=\"api\"\r\n"+
                    "Content-Type: application/octet-stream\r\n"+
                    "Content-Transfer-Encoding: binary\r\n\r\n";
        // find a valid boundary
        do {
            boundary = String.format("Zz%06xzZ", randomGenerator.nextInt(0x1000000));
        } while(mp_header.contains(boundary) && YAPI._find_in_bytes(content,boundary.getBytes())>=0);
        //construct header parts
        String header_start = "Content-Type: multipart/form-data; boundary="+boundary+"\r\n\r\n--"+boundary+"\r\n"+mp_header;
        String header_stop="\r\n--"+boundary+"--\r\n";
        byte[] head_body = new byte[header_start.length()+ content.length+ header_stop.length()];
        int pos=0;int len=header_start.length();
        System.arraycopy(header_start.getBytes(), 0, head_body, pos, len);

        pos+=len;len=content.length;
        System.arraycopy(content, 0, head_body, pos, len);

        pos+=len;len=header_stop.length();
        System.arraycopy(header_stop.getBytes(), 0, head_body, pos, len);
        _request(request,head_body);
        return YAPI.SUCCESS;
    }

    protected int _upload(String pathname, String content) throws YAPI_Exception
    {
        try {
            return this._upload(pathname, content.getBytes(YAPI.DefaultEncoding));
        } catch (UnsupportedEncodingException e) {
            throw new YAPI_Exception(YAPI.INVALID_ARGUMENT, e.getLocalizedMessage());
        }
    }

    protected byte[] _download(String url) throws YAPI_Exception
    {
        String   request = "GET /"+url+" HTTP/1.1\r\n\r\n";
        return  _request(request,null);
    }


    protected String _json_get_key(byte[] json, String key) throws YAPI_Exception
    {
        JSONObject obj = null;
        try {
            obj =new JSONObject(new String(json,"ISO-8859-1"));
        } catch (JSONException ex) {
            throw new YAPI_Exception(YAPI.IO_ERROR,ex.getLocalizedMessage());
        } catch (UnsupportedEncodingException ex) {
            throw new YAPI_Exception(YAPI.IO_ERROR,ex.getLocalizedMessage());
        }
        if(obj.has(key)){
            String val= obj.optString(key);
            if(val==null)
                val = obj.toString();
            return val;
        }
        throw new YAPI_Exception(YAPI.INVALID_ARGUMENT,"No key "+key+"in JSON struct");
    }

    protected String _json_get_string(byte[] json) throws YAPI_Exception
    {
        JSONArray array = null;
        try {
            array = new JSONArray("["+new String(json,"ISO-8859-1")+"]");
        } catch (JSONException ex) {
            throw new YAPI_Exception(YAPI.IO_ERROR,ex.getLocalizedMessage());
        } catch (UnsupportedEncodingException ex) {
            throw new YAPI_Exception(YAPI.IO_ERROR,ex.getLocalizedMessage());
        }
        try {
            return array.getString(0);
        } catch (JSONException ex) {
            throw new YAPI_Exception(YAPI.IO_ERROR,ex.getLocalizedMessage());
        }
    }

    protected ArrayList<String> _json_get_array(byte[] json) throws YAPI_Exception
    {
        JSONArray array = null;
        try {
            array =new JSONArray(new String(json,"ISO-8859-1"));
        } catch (JSONException ex) {
            throw new YAPI_Exception(YAPI.IO_ERROR,ex.getLocalizedMessage());
        } catch (UnsupportedEncodingException ex) {
            throw new YAPI_Exception(YAPI.IO_ERROR,ex.getLocalizedMessage());
        }
        ArrayList<String> list = new ArrayList<String>();
        int len = array.length();
        for (int i=0;i<len;i++){
            try {
                list.add(array.get(i).toString());
            } catch (JSONException ex) {
                throw new YAPI_Exception(YAPI.IO_ERROR,ex.getLocalizedMessage());
            }
        }
        return list;
    }
    // Load and parse the REST API for a function given by class name and
    // identifier, possibly applying changes
    // Device cache will be preloaded when loading function "module" and
    // leveraged for other modules
    protected JSONObject _devRequest(String extra) throws YAPI_Exception
    {
        YDevice dev = SafeYAPI().funcGetDevice(_className, _func);
        YPEntry yp = SafeYAPI().resolveFunction(_className, _func);
        _hwId = yp.getHardwareId();
        _funId = yp.getFuncId();
        _serial = yp.getSerial();
        JSONObject loadval = null;
        if (extra.equals("")) {
            // use a cached API string, without reloading unless module is
            // requested
            String yreq = dev.requestAPI();
            try {
                JSONObject jsonval = new JSONObject(yreq);
                loadval = jsonval.getJSONObject(_funId);
            } catch (JSONException ex) {
                throw new YAPI_Exception(YAPI.IO_ERROR,
                        "Request failed, could not parse API result for " + dev);
            }
        } else {
            dev.dropCache();
        }
        if (loadval == null) {
            // request specified function only to minimize traffic
            if (extra.equals("")) {
                String httpreq = "GET /api/" + _funId + ".json";
                String yreq = new String(dev.requestHTTP(httpreq,null, false));
                try {
                    loadval = new JSONObject(yreq);
                } catch (JSONException ex) {
                    throw new YAPI_Exception(YAPI.IO_ERROR,
                            "Request failed, could not parse API value for "
                            + httpreq);
                }
            } else {
                String httpreq = "GET /api/" + _funId + extra;
                dev.requestHTTP(httpreq,null, true);
                return null;
            }
        }
        return loadval;
    }

    // Method used to cache DataStream objects (new DataLogger)
    YDataStream _findDataStream(YDataSet dataset, String def) throws YAPI_Exception
    {
        String key = dataset.get_functionId()+":"+def;
        if(_dataStreams.containsKey(key)) {
            return _dataStreams.get(key);
        }

        YDataStream newDataStream = new YDataStream(this, dataset, YAPI._decodeWords(def));
        _dataStreams.put(key, newDataStream);
        return newDataStream;
    }

    /**
     * Checks if the function is currently reachable, without raising any error.
     * If there is a cached value for the function in cache, that has not yet
     * expired, the device is considered reachable.
     * No exception is raised if there is an error while trying to contact the
     * device hosting the function.
     * 
     * @return true if the function can be reached, and false otherwise
     */
    public boolean isOnline()
    {
        // A valid value in cache means that the device is online
        if (_cacheExpiration > YAPI.GetTickCount()) {
            return true;
        }
        try {
            // Check that the function is available without throwing exceptions
            load(SafeYAPI().DefaultCacheValidity);
        } catch (YAPI_Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * Returns the numerical error code of the latest error with the function.
     * This method is mostly useful when using the Yoctopuce library with
     * exceptions disabled.
     * 
     * @return a number corresponding to the code of the latest error that occured while
     *         using the function object
     */
    public int get_errorType()
    {
        return _lastErrorType;
    }

    public int getErrorType()
    {
        return _lastErrorType;
    }

    public int errorType()
    {
        return _lastErrorType;
    }

    public int errType()
    {
        return _lastErrorType;
    }

    /**
     * Returns the error message of the latest error with the function.
     * This method is mostly useful when using the Yoctopuce library with
     * exceptions disabled.
     * 
     * @return a string corresponding to the latest error message that occured while
     *         using the function object
     */
    public String get_errorMessage()
    {
        return _lastErrorMsg;
    }

    public String getErrorMessage()
    {
        return _lastErrorMsg;
    }

    public String errorMessage()
    {
        return _lastErrorMsg;
    }

    public String errMessage()
    {
        return _lastErrorMsg;
    }

    /**
     * Preloads the function cache with a specified validity duration.
     * By default, whenever accessing a device, all function attributes
     * are kept in cache for the standard duration (5 ms). This method can be
     * used to temporarily mark the cache as valid for a longer period, in order
     * to reduce network trafic for instance.
     * 
     * @param msValidity : an integer corresponding to the validity attributed to the
     *         loaded function parameters, in milliseconds
     * 
     * @return YAPI.SUCCESS when the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int load(long msValidity) throws YAPI_Exception
    {
        JSONObject json_obj = _devRequest("");
        _parse(json_obj, msValidity);
        return YAPI.SUCCESS;
    }

    /**
     * Gets the YModule object for the device on which the function is located.
     * If the function cannot be located on any module, the returned instance of
     * YModule is not shown as on-line.
     * 
     * @return an instance of YModule
     */
    public YModule get_module()
    {
        YPEntry ypEntry;
        // try to resolve the function name to a device id without query
        if (_serial != null && !_serial.equals("")) {
            return YModule.FindModule(_serial + ".module");
        }
        if (_func.indexOf('.') == -1) {
            try {
                ypEntry = SafeYAPI().resolveFunction(_className, _func);
                return YModule.FindModule(ypEntry.getSerial() + ".module");
            } catch (YAPI_Exception ignored) {
            }
        }
        try {
            // device not resolved for now, force a communication for a last chance resolution
            if (load(SafeYAPI().DefaultCacheValidity) == YAPI.SUCCESS) {
                ypEntry = SafeYAPI().resolveFunction(_className, _func);
                return YModule.FindModule(ypEntry.getSerial() + ".module");
            }
        } catch (YAPI_Exception ignored) {
        }
        // return a true yFindModule object even if it is not a module valid for communicating
        return YModule.FindModule("module_of_" + _className + "_" + _func);
    }

    public YModule getModule()
    {
        return get_module();
    }

    public YModule module()
    {
        return get_module();
    }

    /**
     * Returns a unique identifier of type YFUN_DESCR corresponding to the function.
     * This identifier can be used to test if two instances of YFunction reference the same
     * physical function on the same physical device.
     * 
     * @return an identifier of type YFUN_DESCR.
     * 
     * If the function has never been contacted, the returned value is YFunction.FUNCTIONDESCRIPTOR_INVALID.
     */
    public String get_functionDescriptor()
    {
        // try to resolve the function name to a device id without query
        try {
            return SafeYAPI().resolveFunction(_className, _func).getHardwareId();
        } catch (YAPI_Exception ignored) {
            return FUNCTIONDESCRIPTOR_INVALID;
        }
    }

    public String getFunctionDescriptor()
    {
        return get_functionDescriptor();
    }

    public String functionDescriptor()
    {
        return get_functionDescriptor();
    }

    /**
     * Returns the value of the userData attribute, as previously stored using method
     * set_userData.
     * This attribute is never touched directly by the API, and is at disposal of the caller to
     * store a context.
     * 
     * @return the object stored previously by the caller.
     */
    public Object get_userData()
    {
        return _userData;
    }

    public Object getUserData()
    {
        return get_userData();
    }

    public Object userData()
    {
        return get_userData();
    }

    /**
     * Stores a user context provided as argument in the userData attribute of the function.
     * This attribute is never touched by the API, and is at disposal of the caller to store a context.
     * 
     * @param data : any kind of object to be stored
     * @noreturn
     */
    public void set_userData(Object data)
    {
        _userData = data;
    }

    public void setUserData(Object data)
    {
        set_userData(data);
    }

}
