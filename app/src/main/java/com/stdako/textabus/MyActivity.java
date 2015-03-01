package com.stdako.textabus;

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
import java.util.regex.Pattern;


public class MyActivity extends Activity {

    ListView listView;
    SimpleAdapter adapter;
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    String SMSNumber;

    final public static String name = "name";
    final public static String num = "num";
    final public static String keyDataSaved = "textabus.DATA_SAVED_KEY";
    final public static String keySMSNumber = "textabus.SMS_NUMBER_KEY";
    final SmsManager smsManager = SmsManager.getDefault();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // don't touch pls
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key),
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
        listView.setOnItemClickListener(
                setListItemClickListener((String) rawData.get(keySMSNumber))
        );
        SMSNumber = (String) rawData.get(keySMSNumber);

        adapter = new SimpleAdapter(this, data, R.layout.list_item_layout, new String[]{name, num},
                new int[]{R.id.stopName, R.id.stopNumber});

        listView.setAdapter(adapter);
    }

    // I'm sorry.
    public AdapterView.OnItemClickListener setListItemClickListener(final String smsNumber) {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view.findViewById(R.id.stopNumber);
                String stopNumber = (String) tv.getText();
                smsManager.sendTextMessage(smsNumber, null, stopNumber, null, null);
            }
        };
    }

    public String getStringFromEditText(View dialogView, int Id) {
        return ((EditText) dialogView.findViewById(Id)).getText().toString();
    }

    public String getStringFromTextView(View view, int Id) {
        return ((TextView) view.findViewById(Id)).getText().toString();
    }

    public View generateDialogView(String title, Boolean editingStop, View view) {
        /*
        title: dialog title
        flag: true for editing, false for adding (changes EditText hints if true)
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

    public String[] checkInput(View dialogView, Boolean checkStopNumbers) {
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
        String stopName = getStringFromEditText(dialogView, R.id.itemStopName);
        String stopNumber = getStringFromEditText(dialogView, R.id.itemStopNumber);
        String msg = "";

        if (stopNumber.length() > 8)
            msg = getString(R.string.dialog_error_msg_num_toolong);
        else if (stopName.length() > 48)
            msg = getString(R.string.dialog_error_msg_name_toolong);
        else if (r.matcher(stopNumber).find())
            msg = getString(R.string.dialog_error_msg_num_invalidchars);
        else if (stopName.equals(keyDataSaved))
            msg = getString(R.string.dialog_error_msg_name_nope);

        // check to see if stop number is being used already
        // but only if we ask it to
        if (checkStopNumbers) {
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
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            LayoutInflater inflater = getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_settings_layout, null);

            final AlertDialog d = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setPositiveButton(R.string.dialog_settings_posbtn, null)
                    .setNegativeButton(R.string.dialog_settings_negbtn, null)
                    .create();

            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button btnPos = d.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button btnNeg = d.getButton(AlertDialog.BUTTON_NEGATIVE);

                    btnPos.setBackgroundColor(getResources().getColor(R.color.background_color));
                    btnNeg.setBackgroundColor(getResources().getColor(R.color.background_color));
                    EditText etSmsNumber = (EditText) dialogView.findViewById(R.id.smsNumber);
                    etSmsNumber.setText(SMSNumber);
                    etSmsNumber.setSelection(SMSNumber.length());

                    btnPos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String pattern = "^[0-9+]+$";
                            Pattern r = Pattern.compile(pattern);
                            final String smsNumber = getStringFromEditText(dialogView, R.id.smsNumber);

                            if (r.matcher(smsNumber).find()) {
                                // change number in memory
                                SMSNumber = smsNumber;

                                // change the listener on each list item so it sends texts to the new number
                                listView = (ListView) findViewById(R.id.list);
                                listView.setOnItemClickListener(setListItemClickListener(smsNumber));

                                // add to storage
                                SharedPreferences sharedPref = view.getContext().getSharedPreferences(
                                        getString(R.string.preference_file_key),
                                        view.getContext().MODE_PRIVATE
                                );
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.textabus_sms_key), smsNumber);
                                editor.apply();
                                d.dismiss();
                            } else {
                                TextView tvErrorMsg = (TextView) d.findViewById(R.id.errorMessage);
                                tvErrorMsg.setText(R.string.dialog_error_msg_settings_invalid);
                                tvErrorMsg.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            });
            d.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
