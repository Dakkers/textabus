package com.saintdako.textabus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class ListWidgetProvider extends AppWidgetProvider {
    final public static String keySMSNumber = "textabus.SMS_NUMBER_KEY";
    public static final String EXTRA_ITEM   = "com.example.android.stackwidget.EXTRA_ITEM";
    public static final String STOP_NAME    = "com.saintdako.textabus.WIDGET_STOPNAME";
    public static final String STOP_NUMBER  = "com.saintdako.textabus.WIDGET_STOPNUMBER";
    public static final String SEND_SMS     = "com.saintdako.textabus.SEND_SMS";
    public static final String ACTION_NOTIFY_DATASET_CHANGED = "com.saintdako.textabus.ACTION_NOTIFY_DATASET_CHANGED";

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        System.out.println(action);

        if (action.equals(SEND_SMS)) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                    context.MODE_PRIVATE);
            String stopName   = intent.getStringExtra(STOP_NAME);
            String stopNumber = intent.getStringExtra(STOP_NUMBER);
            String smsNumber  = sharedPref.getString(keySMSNumber, "NULL");

            if (!(smsNumber.equals("NULL"))) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(smsNumber, null, stopNumber, null, null);
                Toast.makeText(context,
                        "Message sent to stop \"" + stopName + "\"!",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
        super.onReceive(context ,intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);

            // setup intent to provide views ... yep
            Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            rv.setRemoteAdapter(appWidgetIds[i], R.id.widget_lv, intent);

            // The empty view is displayed when the collection has no items. It should be a sibling
            // of the collection view.
            rv.setEmptyView(R.id.widget_lv, R.id.widget_item);
            // System.out.println(appWidgetIds[i]);

            // create intent to send an SMS or something
            Intent smsIntent = new Intent(context, ListWidgetProvider.class);
            smsIntent.setAction(ListWidgetProvider.SEND_SMS);
            smsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            PendingIntent smsPendingIntent = PendingIntent.getBroadcast(context, 0, smsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_lv, smsPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void notifyDatasetChanged(Context context) {
        // System.out.println("broadcasting intent...");
        final Intent intent = new Intent(ACTION_NOTIFY_DATASET_CHANGED);
        context.sendBroadcast(intent);
    }
}