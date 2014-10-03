package com.stdako.grtexter;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyActivity extends Activity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // don't touch pls
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // get list object from the xml
        listView = (ListView) findViewById(R.id.list);

        // adding click event listener to each item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view.findViewById(R.id.stopNumber);

                System.out.println(tv.getText());
            }
        });

        // temp data
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        Map<String, String> datum1 = new HashMap<String, String>(2);
        datum1.put("line1", "Home to UW");
        datum1.put("line2", "1161");
        data.add(datum1);
        Map<String, String> datum2 = new HashMap<String, String>(2);
        datum2.put("line1", "UW Engineering to Home");
        datum2.put("line2","1124 9");
        data.add(datum2);

        // define a new adapter
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.list_item_layout, new String[] {"line1", "line2"},
                new int[] {R.id.description, R.id.stopNumber});

        // assign adapter to listView
        listView.setAdapter(adapter);
    }

    public void editItem(View view) {
        System.out.println("edit button hit");
    }

    public void addItem(View view) {
        System.out.println("add button hit");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
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
