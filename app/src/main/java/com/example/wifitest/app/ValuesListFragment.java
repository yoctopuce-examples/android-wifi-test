package com.example.wifitest.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wifitest.app.SensorDatabaseHelper.SensorsValuesCursor;


public class ValuesListFragment extends ListFragment {

    private SensorsValuesCursor _sensorsValuesCursor;
    private final BroadcastReceiver _broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            reloadValues();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // query the list of sensorsValues
        _sensorsValuesCursor = SensorDatabaseHelper.get(getActivity().getApplicationContext()).querySensorsValues();
        // create an adapter to point at this cursor
        SensorsValuesAdapter adapter = new SensorsValuesAdapter(getActivity(), _sensorsValuesCursor);
        setListAdapter(adapter);
        reloadValues();
    }

    private void reloadValues()
    {
        _sensorsValuesCursor.requery();
        ((SensorsValuesAdapter) getListAdapter()).notifyDataSetChanged();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter(BgSensorsService.ACTION_NEW_SENSORS_VALUE);
        getActivity().registerReceiver(_broadcastReceiver, filter);
        reloadValues();
    }

    @Override
    public void onPause()
    {
        getActivity().unregisterReceiver(_broadcastReceiver);
        super.onPause();
    }


    @Override
    public void onDestroy()
    {
        _sensorsValuesCursor.close();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_service_toggle:
                boolean alarmOn = BgSensorsService.isServiceAlarmOn(getActivity());
                BgSensorsService.SetServiceAlarm(getActivity(), !alarmOn);
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.action_clear_values:
                SensorDatabaseHelper.get(getActivity().getApplicationContext()).deleteAllValues();
                reloadValues();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_service_toggle);
        if (item == null) {
            return;
        }
        boolean alarmOn = BgSensorsService.isServiceAlarmOn(getActivity());
        if (alarmOn) {
            item.setTitle(R.string.stop_background);
        } else {
            item.setTitle(R.string.start_background);
        }
    }

    private static class SensorsValuesAdapter extends CursorAdapter {

        private final SensorsValuesCursor _sensorsValuesCursor;

        public SensorsValuesAdapter(Context context, SensorsValuesCursor cursor)
        {
            super(context, cursor, 0);
            _sensorsValuesCursor = cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            // use a layout inflater to get a row view
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor)
        {
            // get the sensorValue for the current row
            SensorsValue sensorValue = _sensorsValuesCursor.getSensorValue();
            // set up the start date text view
            TextView textView = (TextView) view;
            textView.setText(sensorValue.toString());
        }

    }

}
