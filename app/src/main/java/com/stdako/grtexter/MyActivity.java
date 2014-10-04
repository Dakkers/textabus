package com.stdako.grtexter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyActivity extends Activity {

    ListView listView;

    SimpleAdapter adapter;
    List<Map<String, String>> data;

    final public static String name = "name";
    final public static String num = "num";

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
        data = new ArrayList<Map<String, String>>();
        Map<String, String> datum1 = new HashMap<String, String>(2);
        datum1.put(name, "Home to UW");
        datum1.put(num, "1161");
        data.add(datum1);
        Map<String, String> datum2 = new HashMap<String, String>(2);
        datum2.put(name, "UW Engineering to Home");
        datum2.put(num, "1124 9");
        data.add(datum2);

        // define a new adapter
        adapter = new SimpleAdapter(this, data, R.layout.list_item_layout, new String[]{name, num},
                new int[]{R.id.stopName, R.id.stopNumber});

        // assign adapter to listView
        listView.setAdapter(adapter);
    }

    public View generateDialogView(String title, Boolean flag, View view) {
        /*
        title: dialog title
        flag: true for editing, false for adding (changes EditText hints if true)
        view: parent view (add or edit image)
         */

        // black magic
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_layout, null);

        TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText(title);

        // parent of the image is the list item
        view = (View) view.getParent();

        if (flag) {
            TextView tvName = (TextView) view.findViewById(R.id.stopName);
            TextView tvNumber = (TextView) view.findViewById(R.id.stopNumber);
            EditText etName = (EditText) dialogView.findViewById(R.id.itemStopName);
            EditText etNumber = (EditText) dialogView.findViewById(R.id.itemStopNumber);
            String stopName = tvName.getText().toString();
            String stopNumber = tvNumber.getText().toString();
            etName.setText(stopName);
            etNumber.setText(stopNumber);
        }

        return dialogView;
    }

    public void editItem(View view) {
        // generate edit dialog
        final View dialogView = generateDialogView(getString(R.string.dialog_edit_title), true, view);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        view = (View) view.getParent();
        TextView tvName = (TextView) view.findViewById(R.id.stopName);
        TextView tvStop = (TextView) view.findViewById(R.id.stopNumber);
        final String stopNumber = tvStop.getText().toString();

        builder.setPositiveButton( getString(R.string.dialog_edit_posbtn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (Map<String, String> datum : data) {
                    if (datum.get(num).equals(stopNumber)) {
                        EditText etStopNameNew = (EditText) dialogView.findViewById(R.id.itemStopName);
                        EditText etStopNumberNew = (EditText) dialogView.findViewById(R.id.itemStopNumber);
                        String newStopName = etStopNameNew.getText().toString();
                        String newStopNumber = etStopNumberNew.getText().toString();
                        datum.put(name, newStopName);
                        datum.put(num, newStopNumber);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });

        builder.setNeutralButton( getString(R.string.dialog_edit_neubtn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing
            }
        });

        builder.setNegativeButton( getString(R.string.dialog_edit_negbtn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (Map<String, String> datum : data) {
                    if (datum.get(num).equals(stopNumber)) {
                        data.remove(datum);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
        builder.setView(dialogView);
        builder.show();
    }

    public void addItem(final View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = generateDialogView( getString(R.string.dialog_add_title), false, view);

        builder.setPositiveButton(getString(R.string.dialog_add_posbtn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText etName = (EditText) dialogView.findViewById(R.id.itemStopName);
                EditText etStop = (EditText) dialogView.findViewById(R.id.itemStopNumber);
                String stopName = etName.getText().toString();
                String stopNumber = etStop.getText().toString();

                // check to see if stop number is being used already
                for (Map<String,String> datum : data) {
                    String curStopName = datum.get(name);
                    String curStopNumber = datum.get(num);
                    String msg = "";
                    Boolean flag = false;
//                    if (curStopName.equals(stopName)) {
//                        flag = true;
//                        msg = getString(R.string.dialog_error_msg_name);
//                    }
                    if (curStopNumber.equals(stopNumber)) {
                        flag = true;
                        msg = getString(R.string.dialog_error_msg_num);
                    }

                    if (flag) {
                        AlertDialog.Builder builderError = new AlertDialog.Builder(view.getContext());
                        builderError.setTitle(R.string.dialog_error_title);
                        builderError.setMessage(msg);
                        builderError.setPositiveButton(R.string.dialog_error_posbtn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // do nothing
                            }
                        });

                        builderError.show();
                        return;
                    }
                }

                Map<String,String> datum = new HashMap<String, String>(2);
                datum.put(name, stopName);
                datum.put(num, stopNumber);
                data.add(datum);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_add_negbtn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // narp
            }
        });
        builder.setView(dialogView);
        builder.show();
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
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
