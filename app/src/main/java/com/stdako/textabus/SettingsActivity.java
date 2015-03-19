package com.stdako.textabus;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();

        // change background color of settings menu
        findViewById(android.R.id.content).setBackgroundColor(
                this.getResources().getColor(R.color.background_color)
        );
    }
}
