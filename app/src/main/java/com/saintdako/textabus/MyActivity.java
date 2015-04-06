package com.saintdako.textabus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class MyActivity extends Activity {

    ListView listView;
    SimpleAdapter adapter;
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    SharedPreferences sharedPref;

    final public static String name = "name";
    final public static String num = "num";
    final public static String keySMSNumber    = "textabus.SMS_NUMBER_KEY";
    final public static String keyUserData     = "textabus.USER_DATA_KEY";
    final public static String keyDataImported = "textabus.DATA_IMPORTED_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // don't touch pls
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key),
                this.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // get user data
        JSONObject rawData = new JSONObject();
        String stringifiedData = sharedPref.getString(keyUserData, "NULL");

        // user hasn't done anything; here's some default data
        if (stringifiedData.equals("NULL")) {
            // add temp data to listview
            Map<String, String> datum1 = new HashMap<String, String>(2);
            datum1.put(name, getString(R.string.data_default_name1));
            datum1.put(num, getString(R.string.data_default_num1));
            data.add(datum1);
            Map<String, String> datum2 = new HashMap<String, String>(2);
            datum2.put(name, getString(R.string.data_default_name2));
            datum2.put(num, getString(R.string.data_default_num2));
            data.add(datum2);

            // add default data data to storage
            try {
                rawData.put(getString(R.string.data_default_name1), getString(R.string.data_default_num1));
                rawData.put(getString(R.string.data_default_name2), getString(R.string.data_default_num2));
            } catch (JSONException e) { e.printStackTrace(); }
            editor.putString(keySMSNumber, getString(R.string.data_default_phone_number));
            editor.putString(keyUserData, rawData.toString());
            editor.apply();
        } else {
            // get user data (rawData) and all stop names (keys)
            try {
                rawData = new JSONObject(sharedPref.getString(keyUserData, "NULL"));
            } catch (JSONException e) { }
            List<String> keys = getKeys(rawData);

            // put user data in proper format for ListView's adapter
            Collections.sort(keys);
            for (String key : keys) {
                Map<String,String> datum = new HashMap<String, String>(2);
                datum.put(name, key);
                try {
                    datum.put(num, (String) rawData.get(key));
                } catch (JSONException e) { e.printStackTrace(); }
                data.add(datum);
            }
        }

        System.out.println(stringifiedData);

        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(createListItemClickListener());
        listView.setOnItemLongClickListener(createListItemLongClickListener());

        adapter = new SimpleAdapter(this, data, R.layout.list_item_layout, new String[]{name, num},
                new int[]{R.id.stopName, R.id.stopNumber});

        listView.setAdapter(adapter);
    }

    public AdapterView.OnItemClickListener createListItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), R.string.toast_click, Toast.LENGTH_SHORT).show();
            }
        };
    }

    public AdapterView.OnItemLongClickListener createListItemLongClickListener() {
        // creates and returns the list-item click listener so that I don't have
        // to define it inside the "OnCreate" function.
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view.findViewById(R.id.stopNumber);
                String stopNumber = (String) tv.getText();
                // get SMS number; if key lookup fails, return "NULL"
                String smsNumber  = sharedPref.getString(keySMSNumber, "NULL");

                if (!(smsNumber.equals("NULL"))) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(smsNumber, null, stopNumber, null, null);
                    Toast.makeText(getApplicationContext(), R.string.toast_send_sms, Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        };
    }

    public String getStringFromEditText(View dialogView, int Id) {
        // given a view and the id of an EditText, extract the text as a string
        return ((EditText) dialogView.findViewById(Id)).getText().toString();
    }

    public String getStringFromTextView(View view, int Id) {
        // given a view and the id of a TextView, extract the text as a string
        return ((TextView) view.findViewById(Id)).getText().toString();
    }

    public View generateDialogView(String title, Boolean editingStop, View view) {
        /*
        create the dialog view for adding and editing bus stops.

        title: dialog title
        editingStop: true for editing, false for adding (changes EditText hints if true)
        view: parent view (add or edit image)
         */

        // black magic
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_addedit_layout, null);

        TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText(title);

        // parent of the image is the list item
        view = (View) view.getParent();

        if (editingStop) {
            EditText etName = (EditText) dialogView.findViewById(R.id.itemStopName);
            EditText etNumber = (EditText) dialogView.findViewById(R.id.itemStopNumber);
            String stopName = getStringFromTextView(view, R.id.stopName);
            String stopNumber = getStringFromTextView(view, R.id.stopNumber);
            etName.setText(stopName);
            etNumber.setText(stopNumber);
        }
        return dialogView;
    }

    public String checkInput(Context ctx, String oldStopName, String stopName, String stopNumber) {
        /*
        For the dialogs, checks to see if the stop name and stop number are
        good, and then checks to see if the stop name we are trying to
        add is a duplicate, unless the oldStopName == stopName.
        */
        String msg = checkInputHelper(ctx, stopName, stopNumber);

        if (!oldStopName.equals(stopName)) {
            for (Map<String, String> datum : data) {
                // check to see if stop name is being used already
                String currentStopName = datum.get(name);
                if (currentStopName.equals(stopName)) {
                    msg = getString(R.string.dialog_error_msg_name_duplicate);
                    break;
                }
            }
        }

        return msg;
    }

    public static String checkInputHelper(Context ctx, String stopName, String stopNumber) {
        /*
        check inputs to see if they're legit. following conditions must hold:
          - stop number length is < 49 chars
          - stop number is not empty
          - stop name length is < 49 chars
          - stop number has only numeric/whitespace chars
          - stop number is not only whitespace
          - stop number isn't currently in use
        return empty string if all is good.
        return other message otherwise.
         */
        String msg = "";

        // regex: check for anything that isn't numbers or whitespace
        final String pattern1 = "[^0-9\\s]";
        final Pattern r1 = Pattern.compile(pattern1);
        // regex: check for strings that are only whitespace or empty
        final String pattern2 = "^\\s+$";
        final Pattern r2 = Pattern.compile(pattern2);

        if (stopNumber.length() > 48)
            msg = ctx.getString(R.string.dialog_error_msg_num_toolong);
        else if (stopNumber.length() == 0)
            msg = ctx.getString(R.string.dialog_error_msg_num_empty);
        else if (stopName.length() > 48)
            msg = ctx.getString(R.string.dialog_error_msg_name_toolong);
        else if (r1.matcher(stopNumber).find())
            msg = ctx.getString(R.string.dialog_error_msg_num_invalidchars);
        else if (r2.matcher(stopNumber).find())
            msg = ctx.getString(R.string.dialog_error_msg_num_whitespace);
        else if (stopName.indexOf('\t') != -1)
            msg = ctx.getString(R.string.dialog_error_msg_name_tab);
        else if (stopName.equals(keyUserData) || stopName.equals(keySMSNumber) ||stopName.equals(keyDataImported))
            msg = ctx.getString(R.string.dialog_error_msg_name_nope);

        return msg;
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
        // these are the current stopName & stopNumber
        final String stopName = getStringFromTextView(view, R.id.stopName);
        final String stopNumber = getStringFromTextView(view, R.id.stopNumber);

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnPos = d.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnNeu = d.getButton(AlertDialog.BUTTON_NEUTRAL);
                Button btnNeg = d.getButton(AlertDialog.BUTTON_NEGATIVE);

                btnPos.setBackgroundColor(getResources().getColor(R.color.background_color));
                btnNeu.setBackgroundColor(getResources().getColor(R.color.background_color));
                btnNeg.setBackgroundColor(getResources().getColor(R.color.background_color));

                btnPos.setOnClickListener(new View.OnClickListener() {
                    // changing a stop's information
                    @Override
                    public void onClick(View view) {
                        // get new info...
                        String newStopName = getStringFromEditText(dialogView, R.id.itemStopName);
                        String newStopNumber = getStringFromEditText(dialogView, R.id.itemStopNumber);
                        String msg = checkInput(dialogView.getContext(), stopName, newStopName, newStopNumber);

                        if (msg.equals("")) {
                            // find out where old datum is in data, replace it with new datum
                            Map<String, String> newDatum, oldDatum = new HashMap<String, String>(2);
                            oldDatum.put(name, stopName);
                            oldDatum.put(num, stopNumber);
                            newDatum = data.get(data.indexOf(oldDatum));
                            newDatum.put(name, newStopName);
                            newDatum.put(num, newStopNumber);
                            adapter.notifyDataSetChanged();

                            // update datum in storage
                            SharedPreferences sharedPref = view.getContext().getSharedPreferences(
                                    getString(R.string.preference_file_key),
                                    view.getContext().MODE_PRIVATE
                            );

                            JSONObject rawData;
                            try {
                                rawData = new JSONObject(sharedPref.getString(keyUserData, "NULL"));
                                rawData.remove(stopName);
                                rawData.put(newStopName, newStopNumber);
                            } catch (JSONException e) {
                                Toast.makeText(d.getContext(), R.string.toast_save_fail, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                                return;
                            }

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(keyUserData, rawData.toString());
                            editor.apply();
                            d.dismiss();
                        } else {
                            veryLongToast(d.getContext(), msg);
                        }
                    }
                });

                btnNeg.setOnClickListener(new View.OnClickListener() {
                    // removing a stop
                    @Override
                    public void onClick(View view) {
                        Map<String, String> datum = new HashMap<String, String>(2);
                        datum.put(name, stopName);
                        datum.put(num, stopNumber);
                        data.remove(datum);
                        adapter.notifyDataSetChanged();

                        // remove from storage
                        JSONObject rawData;
                        try {
                            rawData = new JSONObject(sharedPref.getString(keyUserData, "NULL"));
                        } catch (JSONException e) {
                            Toast.makeText(d.getContext(), R.string.toast_general_error, Toast.LENGTH_LONG).show();
                            return;
                        }
                        rawData.remove(stopName);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(keyUserData, rawData.toString());
                        editor.apply();
                        d.dismiss();
                    }
                });
            }
        });
        d.show();
    }

    public void addItem(final View view) {
        final View dialogView = generateDialogView(getString(R.string.dialog_add_title), false, view);
        final AlertDialog d = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_add_posbtn, null)
                .setNegativeButton(R.string.dialog_add_negbtn, null)
                .create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnPos = d.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnNeg = d.getButton(AlertDialog.BUTTON_NEGATIVE);

                btnPos.setBackgroundColor(getResources().getColor(R.color.background_color));
                btnNeg.setBackgroundColor(getResources().getColor(R.color.background_color));

                btnPos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String stopName = getStringFromEditText(dialogView, R.id.itemStopName);
                        String stopNumber = getStringFromEditText(dialogView, R.id.itemStopNumber);
                        String msg = checkInput(dialogView.getContext(), stopName, stopName, stopNumber);

                        if (msg.equals("")) {
                            // add to memory (list view)
                            Map<String,String> datum = new HashMap<String, String>(2);
                            datum.put(name, stopName);
                            datum.put(num, stopNumber);
                            data.add(datum);
                            adapter.notifyDataSetChanged();

                            // add to storage
                            SharedPreferences.Editor editor = sharedPref.edit();
                            JSONObject rawData;
                            try {
                                rawData = new JSONObject(sharedPref.getString(keyUserData, "NULL"));
                                rawData.put(stopName, stopNumber);
                            } catch (JSONException e) {
                                Toast.makeText(d.getContext(), R.string.toast_save_fail, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                                return;
                            }
                            editor.putString(keyUserData, rawData.toString());
                            editor.apply();
                            d.dismiss();
                        } else {
                            veryLongToast(d.getContext(), msg);
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
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(MyActivity.this, SettingsActivity.class);
            startActivityForResult(intent, 0);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void veryLongToast(Context ctx, String msg) {
        // Makes a toast appear for 5.5 seconds (short + long)
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        /* Contains logic for adding imported data to view. */
        super.onResume();

        // check to see if any data was imported
        if (sharedPref.getString(keyDataImported, "false").equals("true")) {
            SharedPreferences.Editor editor = sharedPref.edit();

            String stopName, stopNumber, newStopNumber;
            Map<String, String> datum, newDatum;
            Boolean dataHasChanged = false;

            // loop over data, get the location (index) of each stop
            // use a map to hold the data (stopName --> index)
            Map<String, Integer> stopIndices = new HashMap<String, Integer>(data.size());
            for (int i = 0; i < data.size(); i++) {
                stopName = data.get(i).get(name);
                stopIndices.put(stopName, i);
            }

            // loop over shared preferences
            JSONObject rawData;
            try {
                rawData = new JSONObject(sharedPref.getString(keyUserData, "NULL"));
            } catch (JSONException e) {
                Toast.makeText(this.getApplicationContext(), R.string.toast_read_fail, Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return;
            }
            List<String> keys = getKeys(rawData);
            Integer stopIndex;
            for (String key : keys) {
                // get index of stopName; if it is null, then it hasn't been added to
                // the data in memory. if it is not null, check to see if the stop
                // number has changed.
                stopIndex = stopIndices.get(key);
                if (stopIndex == null) {
                    newDatum = new HashMap<String, String>(2);
                    newDatum.put(name, key);
                    newDatum.put(num, rawData.optString(key));
                    data.add(newDatum);
                    dataHasChanged = true;
                } else {
                    // get the stop corresponding to the index, then get its stop number
                    datum = data.get(stopIndex);
                    stopNumber = datum.get(num);

                    // get the version saved in storage then check if they're not equal
                    newStopNumber = rawData.optString(key);
                    if (!stopNumber.equals(newStopNumber)) {
                        // they're equal; replace old value.
                        newDatum = new HashMap<String, String>(2);
                        newDatum.put(name, key);
                        newDatum.put(num, newStopNumber);
                        data.set(stopIndex, newDatum);
                        dataHasChanged = true;
                    }
                }
            }

            if (dataHasChanged) {
                Collections.sort(data, stopNameComparator);
                adapter.notifyDataSetChanged();
            }

            editor.putString(keyDataImported, "false");
            editor.apply();
        }
    }

    public Comparator<Map<String, String>> stopNameComparator = new Comparator<Map<String, String>>() {
        // Comparator for comparing a Map<String,String> to another
        // Used for alphabetizing stops by stop name
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get(name).compareTo(m2.get(name));
        }
    };

    public static List<String> getKeys(JSONObject rawData) {
        /* Given the rawData (user's data), returns a list of the stop names (keys) */
        Iterator keysIterator = rawData.keys();
        List<String> keys = new ArrayList();
        while (keysIterator.hasNext())
            keys.add((String) keysIterator.next());
        return keys;
    }
}
