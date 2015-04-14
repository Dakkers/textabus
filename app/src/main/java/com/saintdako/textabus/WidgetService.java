package com.saintdako.textabus;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private List<String> mWidgetItems = new ArrayList<>();
    private Context mContext;
    private int mAppWidgetId;

    private SharedPreferences sharedPref;
    private JSONObject rawData;
    final public static String keyUserData = "textabus.USER_DATA_KEY";

    public ListRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.preference_file_key),
                mContext.MODE_PRIVATE);

        try {
            rawData = new JSONObject(sharedPref.getString(keyUserData, "NULL"));
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        List<String> keys = getKeys(rawData);
        Collections.sort(keys);
        mWidgetItems = keys;
    }

    public void onDestroy() {
        mWidgetItems.clear();
    }

    public RemoteViews getViewAt(int position) {

        String stopName = mWidgetItems.get(position);
        String stopNumber = rawData.optString(stopName);

        // Construct a remote views item based on the app widget item XML file,
        // and set the text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        rv.setTextViewText(R.id.widget_item, stopName);

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(ListWidgetProvider.EXTRA_ITEM, position);
        extras.putString(ListWidgetProvider.STOP_NAME, stopName);
        extras.putString(ListWidgetProvider.STOP_NUMBER, stopNumber);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        // Return the remote views object.
        return rv;
    }

    @Override
    public void onDataSetChanged() {
        try {
            rawData = new JSONObject(sharedPref.getString(keyUserData, "NULL"));
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        List<String> keys = getKeys(rawData);
        Collections.sort(keys);
        mWidgetItems = keys;
        System.out.println(mWidgetItems);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mWidgetItems.size();
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public List<String> getKeys(JSONObject rawData) {
        return com.saintdako.textabus.MyActivity.getKeys(rawData);
    }
}