package com.stdako.grtexter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Pattern;


public class MyActivity extends Activity {

    ListView listView;
    SimpleAdapter adapter;
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();

    final public static String name = "name";
    final public static String num = "num";
    final public static String dataSaved = "hasDataBeenSaved?";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // don't touch pls
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        final SmsManager smsManager = SmsManager.getDefault();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key),
                this.MODE_PRIVATE);
        Map<String,?> rawData = sharedPref.getAll();
        SharedPreferences.Editor editor = sharedPref.edit();

        // user hasn't done anything; here's some default data
        if (rawData.get(dataSaved) == null) {
            // add temp data to listview
            Map<String, String> datum1 = new HashMap<String, String>(2);
            datum1.put(name, getString(R.string.data_default_name1));
            datum1.put(num, getString(R.string.data_default_num1));
            data.add(datum1);
            Map<String, String> datum2 = new HashMap<String, String>(2);
            datum2.put(name, getString(R.string.data_default_name2));
            datum2.put(num, getString(R.string.data_default_num2));
            data.add(datum2);

            // remove/add temp data to storage
            editor.remove(getString(R.string.data_default_name1));
            editor.remove(getString(R.string.data_default_name2));
            editor.putString(getString(R.string.data_default_name1), getString(R.string.data_default_num1));
            editor.putString(getString(R.string.data_default_name2), getString(R.string.data_default_num2));
            editor.apply();
        } else {
            List<String> keys = new ArrayList<String>(rawData.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                if (!key.equals(dataSaved)) {
                    Map<String,String> datum = new HashMap<String, String>(2);
                    datum.put(name, key);
                    datum.put(num, (String) rawData.get(key));
                    data.add(datum);
                }
            }
        }

        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view.findViewById(R.id.stopNumber);
                String stopNumber = (String) tv.getText();
                smsManager.sendTextMessage(getString(R.string.data_default_phone_number),
                        null, stopNumber, null, null);
            }
        });

        adapter = new SimpleAdapter(this, data, R.layout.list_item_layout, new String[]{name, num},
                new int[]{R.id.stopName, R.id.stopNumber});

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

    public String[] checkInput(View dialogView, Boolean flag) {
        /*
        check inputs to see if they're legit.
          - stop name length is < 49 chars
          - stop number length is < 9 chars
          - stop number has non-numeric/whitespace chars
          - stop number isn't currently in use
        return empty string if all is good.
        return other message otherwise.
         */

        final String pattern = "[^0-9\\s]";
        final Pattern r = Pattern.compile(pattern);
        EditText etName = (EditText) dialogView.findViewById(R.id.itemStopName);
        EditText etStop = (EditText) dialogView.findViewById(R.id.itemStopNumber);
        String stopName = etName.getText().toString();
        String stopNumber = etStop.getText().toString();
        String msg = "";

        if (stopNumber.length() > 8)
            msg = getString(R.string.dialog_error_msg_num_toolong);
        else if (stopName.length() > 48)
            msg = getString(R.string.dialog_error_msg_name_toolong);
        else if (r.matcher(stopNumber).find())
            msg = getString(R.string.dialog_error_msg_num_invalidchars);
        else if (stopName.equals(dataSaved))
            msg = getString(R.string.dialog_error_msg_name_nope);

        // check to see if stop number is being used already
        // but only if we ask it to
        if (flag) {
            for (Map<String, String> datum : data) {
                String curStopNumber = datum.get(num);
                if (curStopNumber.equals(stopNumber)) {
                    msg = getString(R.string.dialog_error_msg_num_duplicate);
                    break;
                }
            }
        }
        return new String[] {msg, stopName, stopNumber};
    }

    public void editItem(View view) {
        // generate edit dialog
        final View dialogView = generateDialogView(getString(R.string.dialog_edit_title), true, view);
        final AlertDialog d = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_edit_posbtn, null)
                .setNeutralButton(R.string.dialog_edit_neubtn, null)
                .setNegativeButton(R.string.dialog_edit_negbtn, null)
                .create();

        view = (View) view.getParent();
        TextView tvName = (TextView) view.findViewById(R.id.stopName);
        TextView tvStop = (TextView) view.findViewById(R.id.stopNumber);
        final String stopName = tvName.getText().toString();
        final String stopNumber = tvStop.getText().toString();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnPos = d.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnNeg = d.getButton(AlertDialog.BUTTON_NEGATIVE);

                btnPos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // get new info...
                        EditText etStopNameNew = (EditText) dialogView.findViewById(R.id.itemStopName);
                        EditText etStopNumberNew = (EditText) dialogView.findViewById(R.id.itemStopNumber);
                        String newStopName = etStopNameNew.getText().toString();
                        String newStopNumber = etStopNumberNew.getText().toString();

                        String[] info = checkInput(dialogView, false);
                        String msg = info[0];

                        if (msg.equals("")) { // update info
                            for (Map<String, String> datum : data) {
                                if (datum.get(num).equals(stopNumber)) {
                                    // change old key-val pair in memory
                                    datum.put(name, newStopName);
                                    datum.put(num, newStopNumber);
                                    adapter.notifyDataSetChanged();

                                    // remove old key-val pair from storage, add new
                                    SharedPreferences sharedPref = view.getContext().getSharedPreferences(
                                            getString(R.string.preference_file_key),
                                            view.getContext().MODE_PRIVATE
                                    );
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.remove(stopName);
                                    editor.putString(newStopName, newStopNumber);
                                    if (sharedPref.getAll().get(dataSaved) == null)
                                        editor.putString(dataSaved, "true");
                                    editor.apply();
                                    d.dismiss();
                                    break;
                                }
                            }
                        } else {
                            TextView tvErrorMsg = (TextView) d.findViewById(R.id.errorMessage);
                            tvErrorMsg.setText(msg);
                            tvErrorMsg.setVisibility(View.VISIBLE);
                        }
                    }
                });

                btnNeg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (Map<String, String> datum : data) {
                            if (datum.get(num).equals(stopNumber)) {
                                // remove key-value pair from memory
                                data.remove(datum);
                                adapter.notifyDataSetChanged();

                                // remove from storage
                                SharedPreferences sharedPref = view.getContext().getSharedPreferences(
                                        getString(R.string.preference_file_key),
                                        view.getContext().MODE_PRIVATE
                                );
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.remove(stopName);
                                if (sharedPref.getAll().get(dataSaved).equals("false"))
                                    editor.putString(dataSaved, "true");
                                editor.apply();
                                d.dismiss();
                                break;
                            }
                        }
                    }
                });
            }
        });
        d.show();
    }

    public void addItem(final View view) {
        final View dialogView = generateDialogView( getString(R.string.dialog_add_title), false, view);
        final AlertDialog d = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_add_posbtn, null)
                .setNegativeButton(R.string.dialog_add_negbtn, null)
                .create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnPos = d.getButton(AlertDialog.BUTTON_POSITIVE);

                btnPos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String[] info = checkInput(dialogView, true);
                        String msg = info[0];
                        String stopName = info[1];
                        String stopNumber = info[2];

                        if (msg.equals("")) {
                            // add to memory (list view)
                            Map<String,String> datum = new HashMap<String, String>(2);
                            datum.put(name, stopName);
                            datum.put(num, stopNumber);
                            data.add(datum);
                            adapter.notifyDataSetChanged();

                            // add to storage
                            SharedPreferences sharedPref = view.getContext().getSharedPreferences(
                                    getString(R.string.preference_file_key),
                                    view.getContext().MODE_PRIVATE
                            );
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(stopName, stopNumber);
                            if (sharedPref.getAll().get(dataSaved) == null)
                                editor.putString(dataSaved, "true");
                            editor.apply();
                            d.dismiss();
                        } else {
                            TextView tvErrorMsg = (TextView) d.findViewById(R.id.errorMessage);
                            tvErrorMsg.setText(msg);
                            tvErrorMsg.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
        d.show();
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
