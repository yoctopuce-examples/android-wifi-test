package com.example.wifitest.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SensorDatabaseHelper extends SQLiteOpenHelper {


    private static final String DB_NAME = "sensorval.sqlite";
    private static final int VERSION = 1;
    public static final String SENSORSVALUES_TABLES = "sensorsvalues";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TEMPERATURE = "temperature";
    private static final String COLUMN_HUMIDITY = "humidity";
    private static final String COLUMN_PRESSURE = "pressure";
    private static final String COLUMN_LIGHT = "light";
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
                COLUMN_TEMPERATURE + " real, " + COLUMN_HUMIDITY + " real, " + COLUMN_PRESSURE + " real," +
                COLUMN_LIGHT + " real)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2)
    {
        // implement schema changes and data massage here when upgrading
    }


    public long insertSensorValue(SensorsValue sensorsValue) {
        ContentValues cv = new ContentValues();
        cv.put("temperature", sensorsValue.getTemperature());
        cv.put("humidity", sensorsValue.getHumidity());
        cv.put("pressure", sensorsValue.getPressure());
        cv.put("light", sensorsValue.getIlumination());
        SQLiteDatabase writableDatabase = getWritableDatabase();
        if (writableDatabase == null) {
            return -1;
        }
        return writableDatabase.insert(SENSORSVALUES_TABLES, null, cv);
    }

    public SensorsValuesCursor querySensorsValues()
    {
        Cursor query = getReadableDatabase().query(SENSORSVALUES_TABLES, null, null, null, null, null, COLUMN_ID + " ASC");
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
            SensorsValue sensorsValue= new SensorsValue();
            long id = getLong(getColumnIndex(COLUMN_ID));
            sensorsValue.setId(id);
            double dblval = getDouble(getColumnIndex(COLUMN_TEMPERATURE));
            sensorsValue.setTemperature(dblval);
            dblval = getDouble(getColumnIndex(COLUMN_HUMIDITY));
            sensorsValue.setHumidity(dblval);
            dblval = getDouble(getColumnIndex(COLUMN_PRESSURE));
            sensorsValue.setPressure(dblval);
            dblval = getDouble(getColumnIndex(COLUMN_LIGHT));
            sensorsValue.setIlumination(dblval);
            return sensorsValue;
        }
    }

}
