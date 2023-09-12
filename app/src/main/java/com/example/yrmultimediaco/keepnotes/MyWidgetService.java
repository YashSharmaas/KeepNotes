package com.example.yrmultimediaco.keepnotes;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MyWidgetFactory(getApplicationContext());
    }
}

class MyWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<AbstractItem> itemList; // Your list of CreateNotesAdapter items
    private Context context;
    DBhelper mDBhelper;

    public MyWidgetFactory(Context context) {
        this.context = context;
        mDBhelper = new DBhelper(context);
    }

    @Override
    public void onCreate() {
        itemList = new ArrayList<>(); // Initialize your list of CreateNotesAdapter items here

        // Fetch data from your database or other source and populate itemList
        ArrayList<AbstractItem> notesList = mDBhelper.fetchRegularNotes();

        //itemList.clear();

        Log.d("WidgetDebug", "Size of notesList: " + notesList.size());

        ArrayList<AbstractItem> updatedItemList = new ArrayList<>();

        // Add individual items to itemList
        for (AbstractItem note : notesList) {
            if (note instanceof CreateNotesAdapter) { // Ensure it's of the correct type
                CreateNotesAdapter createNotesAdapter = (CreateNotesAdapter) note;
                itemList.add(createNotesAdapter);
            }
        }

        // Log the data to check if it's correctly loaded
        for (int i = 0; i < itemList.size(); i++) {
            CreateNotesAdapter item = (CreateNotesAdapter) itemList.get(i);
            Log.d("WidgetDebug", "Item at position " + i + ": " + item.getTitle());
        }

        Log.d("WidgetDebug", "Size of itemList after adding: " + itemList.size());

    }


    @Override
    public void onDataSetChanged() {
        // Update your notes list (newNote) when new data is available
        ArrayList<AbstractItem> newNote = mDBhelper.fetchRegularNotes();

        // Check if there's a significant data change
        if (hasSignificantDataChange(newNote)) {
            // Assuming that updating the data is fast and efficient, you can proceed to update itemList
            itemList.clear();
            itemList.addAll(newNote);

            // Notify the widget that data has changed
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, MyWidgetProvider.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widgetListView);
        }
    }

    private boolean hasSignificantDataChange(ArrayList<AbstractItem> newNotes) {
        // If the sizes are different, consider it a significant change
        if (newNotes.size() != itemList.size()) {
            return true;
        }

        // Compare each item in the newNotes list with the corresponding item in itemList
        for (int i = 0; i < newNotes.size(); i++) {
            CreateNotesAdapter newNote = (CreateNotesAdapter) newNotes.get(i);
            CreateNotesAdapter currentNote = (CreateNotesAdapter) itemList.get(i);

            // Compare the contents of the newNote and currentNote
            if (!areNotesEqual(newNote, currentNote)) {
                return true;
            }
        }

        // If no significant changes were found, return false
        return false;
    }

    private boolean areNotesEqual(CreateNotesAdapter note1, CreateNotesAdapter note2) {
        // Implement your logic to compare the contents of the notes.
        // You may compare title, content, timestamps, or any relevant fields.
        // Return true if they are equal; otherwise, return false.
        return Objects.equals(note1.getTitle(), note2.getTitle());
    }





    @Override
    public void onDestroy() {
        // Clean up resources here
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        Log.d("WidgetDebug", "Item list size in getViewAt: " + itemList.size());

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        //try {
            CreateNotesAdapter item = (CreateNotesAdapter) itemList.get(position);
            String itemText = item.getTitle();
            remoteViews.setTextViewText(R.id.widgetListItem, itemText);


            // Create an intent to open MainActivity
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra("ITEM_ID", item.getId()); // Pass some data to identify the clicked item
            remoteViews.setOnClickFillInIntent(R.id.widgetListItem, fillInIntent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return remoteViews;
    }



    @Override
    public RemoteViews getLoadingView() {
        return null;
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
    public boolean hasStableIds() {
        return true;
    }
}

