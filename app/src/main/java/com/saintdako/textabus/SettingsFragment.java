package com.saintdako.textabus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.saintdako.textabus.MyActivity.*;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    final public static String keySMSNumber    = "textabus.SMS_NUMBER_KEY";
    final public static String keyDataSaved    = "textabus.DATA_SAVED_KEY";
    final public static String keyDataImported = "textabus.DATA_IMPORTED_KEY";
    final public static String name = "name";
    final public static String num = "num";
    final public Integer LENGTH_LONG  = Toast.LENGTH_LONG;
    final public Integer LENGTH_SHORT = Toast.LENGTH_SHORT;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    String smsNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        sharedPref = this.getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
        );
        editor = sharedPref.edit();

        // get the current number (should be valid...)
        smsNumber = sharedPref.getString(keySMSNumber, "57555");

        Preference exportDataPref = findPreference(getString(R.string.textabus_export_key));
        exportDataPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (isExternalStorageWritable())
                    exportData(preference);
                else
                    Toast.makeText(preference.getContext(), getString(R.string.toast_not_writable), LENGTH_LONG).show();
                return true;
            }
        });

        Preference importDataPref = findPreference(getString(R.string.textabus_import_key));
        importDataPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (isExternalStorageReadable())
                    importData(preference.getContext());
                else
                    Toast.makeText(preference.getContext(), R.string.toast_not_readable, LENGTH_LONG).show();
                return true;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // this is the shared preference listener - listens for changes.
        if (key.equals(keySMSNumber)) {
            String newSMSNumber = sharedPreferences.getString(key, "57555");

            if (checkValidPhoneNumber(newSMSNumber)) {
                // phone number is good to go; put it in memory
                smsNumber = newSMSNumber;

                // and put it in storage
                editor.putString(key, newSMSNumber);
                editor.apply();
            } else {
                // phone number is bad; replace the ETP's text with the old number
                EditTextPreference etp = (EditTextPreference) findPreference(key);
                etp.setText(smsNumber);

                // alert user with TOAST yum
                Context ctx = etp.getContext();
                String msg = "Invalid phone number. Old phone number (" + smsNumber + ") will be used instead.";
                // use a for-loop to make the toast message 7 seconds instead of 3.5 seconds.
                for (int i=0; i<2; i++)
                    Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    public boolean checkValidPhoneNumber(String num) {
        // determine if a string 'num' is a valid phone number
        // regex: any characters that are not numbers.
        String pattern = "[^0-9]";
        Pattern r = Pattern.compile(pattern);

        if (r.matcher(num).find())
            return false;

        return true;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public void exportData(Preference preference) {
        /*
        Export's the user's data into a file called 'TextABus-data.txt'.
        Each line consists of a stop name and its corresponding stop
        number, separated by a tab.
        */

        // get shared pref & editor
        Map<String,?> rawData = sharedPref.getAll();
        SharedPreferences.Editor editor = sharedPref.edit();

        // string to be written to file
        String content = "";

        // loop over data
        List<String> keys = new ArrayList<String>(rawData.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            // key == stop name
            if (!key.equals(keyDataSaved) && !key.equals(keySMSNumber) && !key.equals(keyDataImported))
                content += key + "\t" + rawData.get(key) + "\n";
        }

        // set up file to write to
        File file;
        FileOutputStream outputStream;
        try {
            file = new File(Environment.getExternalStorageDirectory(), "TextABus-Data.txt");
            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
            Toast.makeText(preference.getContext(), R.string.toast_write_success, LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(preference.getContext(), R.string.toast_write_fail, LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void importData(Context ctx) {
        File file = new File(Environment.getExternalStorageDirectory(), "TextABus-Data.txt");

        if (!file.exists()) {
            Toast.makeText(ctx, R.string.toast_no_file, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line, name, num, msg;
            String[] splitLine;
            Boolean emptyFile = true, allLinesImported = true;

            while ((line = br.readLine()) != null) {
                // read each line, split through tab, check name & num
                splitLine = line.split("\t");
                if (splitLine.length != 2) {
                    Toast.makeText(ctx, R.string.toast_read_fail, LENGTH_SHORT).show();
                    return;
                }

                name = splitLine[0];
                num = splitLine[1];
                msg = checkInput(ctx, name, num);

                // if input is good, put into storage
                if (msg.equals(""))
                    editor.putString(name, num);
                // otherwise, let the user know later that a line failed.
                else
                    allLinesImported = false;

                emptyFile = false;
            }

            // if we have an empty file, show toast, do nothing.
            if (emptyFile) {
                Toast.makeText(ctx, R.string.toast_empty_file, LENGTH_LONG).show();
                return;
            }

            // close reader, update sharedpreferences
            br.close();
            editor.putString(keyDataImported, "true");
            editor.apply();

            if (allLinesImported)
                Toast.makeText(ctx, R.string.toast_read_success, LENGTH_SHORT).show();
            else
                Toast.makeText(ctx, R.string.toast_read_neutral, LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Toast.makeText(ctx, R.string.toast_read_fail, LENGTH_SHORT).show();
        }
    }

    public static String checkInput(Context ctx, String stopName, String stopNumber) {
        return com.saintdako.textabus.MyActivity.checkInputHelper(ctx, stopName, stopNumber);
    }
}
