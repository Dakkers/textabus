package com.stdako.grtexter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class DisplayMessageActivity extends Activity {
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // don't touch pls
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // get list object from the xml
        listView = (ListView) findViewById(R.id.list);

        // defined values to show
        String[] values = new String[] {"1124", "1161", "1174"};

        // define a new adapter
        // 1st param: context
        // 2nd param: layout for row
        // 3rd param: ID of textview to which data is written
        // 4th param: array of data
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        // assign adapter to listView


//        Intent intent = getIntent();
//        String message = intent.getStringExtra(MyActivity.EXTRA_MESSAGE);
//
//        TextView textView = new TextView(this);
//        textView.setTextSize(40);
//        textView.setText(message);
//
//        setContentView(textView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
