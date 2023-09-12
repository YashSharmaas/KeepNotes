package com.example.yrmultimediaco.keepnotes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        try {
            // Update your widget here
            // You can use RemoteViews to update the widget's layout
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            // Get the ListView reference
            Intent intent = new Intent(context, MyWidgetService.class);
            remoteViews.setRemoteAdapter(R.id.widgetListView, intent);

            remoteViews.setTextViewText(R.id.textViewWidgets, "Keep Notes");
            // Handle widget interactions here, e.g., set click listeners


            Intent openMainActivityIntent = new Intent(context, MainActivity.class);
            openMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openMainActivityIntent, PendingIntent.FLAG_IMMUTABLE);


            // Set click listener to open MainActivity
            remoteViews.setOnClickPendingIntent(R.id.textViewWidgets, pendingIntent);

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
