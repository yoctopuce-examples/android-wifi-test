package com.example.wifitest.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SensorDatabaseHelper extends SQLiteOpenHelper {


    private static final String DB_NAME = "sensorval.sqlite";
    private static final int VERSION = 2;
    private static final String SENSORSVALUES_TABLES = "sensorsvalues";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TEMPERATURE = "temperature";
    private static final String COLUMN_HUMIDITY = "humidity";
    private static final String COLUMN_PRESSURE = "pressure";
    private static final String COLUMN_LIGHT = "light";
    private static final String COLUMN_TIMESTAMP = "tstamp";
    private static SensorDatabaseHelper sSensorDatabaseHelper = null;

    public SensorDatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, VERSION);
    }

    public static SensorDatabaseHelper get(Context applicationContext)
    {
        if (sSensorDatabaseHelper == null) {
            sSensorDatabaseHelper = new SensorDatabaseHelper(applicationContext);
        }
        return sSensorDatabaseHelper;
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // create the "sensorvalue" table
        db.execSQL("create table " + SENSORSVALUES_TABLES + " (" + COLUMN_ID + " integer primary key autoincrement," +
                COLUMN_TIMESTAMP + " integer," + COLUMN_TEMPERATURE + " real, " +
                COLUMN_HUMIDITY + " real, " + COLUMN_PRESSURE + " real," +
                COLUMN_LIGHT + " real)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2)
    {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SENSORSVALUES_TABLES);
        // Create tables again
        onCreate(db);
    }


    public long insertSensorValue(SensorsValue sensorsValue)
    {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TIMESTAMP, sensorsValue.getTimestamp());
        cv.put(COLUMN_TEMPERATURE, sensorsValue.getTemperature());
        cv.put(COLUMN_HUMIDITY, sensorsValue.getHumidity());
        cv.put(COLUMN_PRESSURE, sensorsValue.getPressure());
        cv.put(COLUMN_LIGHT, sensorsValue.getLight());
        SQLiteDatabase writableDatabase = getWritableDatabase();
        if (writableDatabase == null) {
            return -1;
        }
        return writableDatabase.insert(SENSORSVALUES_TABLES, null, cv);
    }

    public void deleteAllValues()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            db.delete(SENSORSVALUES_TABLES, null, null);
            db.close();
        }
    }

    public SensorsValuesCursor querySensorsValues()
    {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor query = null;
        if (readableDatabase != null) {
            query = readableDatabase.query(SENSORSVALUES_TABLES, null, null, null, null, null, COLUMN_ID + " DESC");
        }
        return new SensorsValuesCursor(query);
    }


    public static class SensorsValuesCursor extends CursorWrapper {
        public SensorsValuesCursor(Cursor cursor)
        {
            super(cursor);
        }

        public SensorsValue getSensorValue()
        {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            SensorsValue sensorsValue = new SensorsValue();
            long longval = getLong(getColumnIndex(COLUMN_ID));
            sensorsValue.setId(longval);
            longval = getLong(getColumnIndex(COLUMN_TIMESTAMP));
            sensorsValue.setTimestamp(longval);
            double dblval = getDouble(getColumnIndex(COLUMN_TEMPERATURE));
            sensorsValue.setTemperature(dblval);
            dblval = getDouble(getColumnIndex(COLUMN_HUMIDITY));
            sensorsValue.setHumidity(dblval);
            dblval = getDouble(getColumnIndex(COLUMN_PRESSURE));
            sensorsValue.setPressure(dblval);
            dblval = getDouble(getColumnIndex(COLUMN_LIGHT));
            sensorsValue.setLight(dblval);
            return sensorsValue;
        }
    }

}
