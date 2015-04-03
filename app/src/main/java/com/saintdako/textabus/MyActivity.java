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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class MyActivity extends Activity {

    ListView listView;
    SimpleAdapter adapter;
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    String SMSNumber;
    SharedPreferences sharedPref;

    final public static String name = "name";
    final public static String num = "num";
    final public static String keyDataSaved  = "textabus.DATA_SAVED_KEY";
    final public static String keySMSNumber  = "textabus.SMS_NUMBER_KEY";
    final public static String keyDataExport = "textabus.DATA_EXPORT_KEY";
    final SmsManager smsManager = SmsManager.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // don't touch pls
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key),
                this.MODE_PRIVATE);
        Map<String,?> rawData = sharedPref.getAll();
        SharedPreferences.Editor editor = sharedPref.edit();

        // user hasn't done anything; here's some default data
        if (rawData.get(keyDataSaved) == null) {
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
            editor.putString(getString(R.string.data_default_name1), getString(R.string.data_default_num1));
            editor.putString(getString(R.string.data_default_name2), getString(R.string.data_default_num2));
            editor.putString(keySMSNumber, getString(R.string.data_default_phone_number));
            editor.putString(keyDataSaved, "true");
            editor.apply();

            // reinitialize rawData, as sharedPreferences were populated with default values.
            rawData = sharedPref.getAll();
        } else {
            List<String> keys = new ArrayList<String>(rawData.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                if (!key.equals(keyDataSaved) && !key.equals(keySMSNumber)) {
                    Map<String,String> datum = new HashMap<String, String>(2);
                    datum.put(name, key);
                    datum.put(num, (String) rawData.get(key));
                    data.add(datum);
                }
            }
        }

        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(createListItemClickListener());
        listView.setOnItemLongClickListener(createListItemLongClickListener());

        SMSNumber = (String) rawData.get(keySMSNumber);

        adapter = new SimpleAdapter(this, data, R.layout.list_item_layout, new String[]{name, num},
                new int[]{R.id.stopName, R.id.stopNumber});

        listView.setAdapter(adapter);
    }

    public AdapterView.OnItemClickListener createListItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), R.string.toast_click, Toast.LENGTH_LONG).show();
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

    public String[] checkInput(View dialogView, Boolean checkStopNames) {
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

        // regex: check for anything that isn't numbers or whitespace
        final String pattern1 = "[^0-9\\s]";
        final Pattern r1 = Pattern.compile(pattern1);
        // regex: check for strings that are only whitespace or empty
        final String pattern2 = "^\\s+$";
        final Pattern r2 = Pattern.compile(pattern2);

        String stopName = getStringFromEditText(dialogView, R.id.itemStopName);
        String stopNumber = getStringFromEditText(dialogView, R.id.itemStopNumber);
        String msg = "";

        if (stopNumber.length() > 48)
            msg = getString(R.string.dialog_error_msg_num_toolong);
        else if (stopNumber.length() == 0)
            msg = getString(R.string.dialog_error_msg_num_empty);
        else if (stopName.length() > 48)
            msg = getString(R.string.dialog_error_msg_name_toolong);
        else if (r1.matcher(stopNumber).find())
            msg = getString(R.string.dialog_error_msg_num_invalidchars);
        else if (r2.matcher(stopNumber).find())
            msg = getString(R.string.dialog_error_msg_num_whitespace);
        else if (stopName.indexOf('\t') != -1)
            msg = getString(R.string.dialog_error_msg_name_tab);
        else if (stopName.equals(keyDataSaved))
            msg = getString(R.string.dialog_error_msg_name_nope);

        // check to see if stop name is being used already
        // but only if we ask it to
        if (checkStopNames) {
            for (Map<String, String> datum : data) {
                String currentStopName = datum.get(name);
                if (currentStopName.equals(stopName)) {
                    msg = getString(R.string.dialog_error_msg_name_duplicate);
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
                    @Override
                    public void onClick(View view) {
                        // get new info...
                        String newStopName = getStringFromEditText(dialogView, R.id.itemStopName);
                        String newStopNumber = getStringFromEditText(dialogView, R.id.itemStopNumber);
                        String[] info = checkInput(dialogView, false);
                        String msg = info[0];

                        if (msg.equals("")) {
                            // update info
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
                                    editor.apply();
                                    d.dismiss();
                                    break;
                                }
                            }
                        } else {
                            veryLongToast(d.getContext(), msg);
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

}
