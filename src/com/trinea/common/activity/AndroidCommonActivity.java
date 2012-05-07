package com.trinea.common.activity;

import com.trinea.common.R;

import android.app.Activity;
import android.os.Bundle;

public class AndroidCommonActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}