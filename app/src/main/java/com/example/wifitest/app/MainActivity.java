package com.example.wifitest.app;

import android.support.v4.app.Fragment;


public class MainActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment()
    {
        return new ValuesListFragment();
    }
}
