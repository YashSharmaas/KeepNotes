package com.example.yrmultimediaco.keepnotes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mikepenz.fastadapter.IIdentifyable;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.List;

public class DBhelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "notesDB";
    public static final int DB_VERSION = 1;
    public static final String _ID = "id";
    public static final String TABLE_NAME = "notesTable";
    public static final String TITLE  = "notesTitle";
    public static final String SUB_TITLE = "subTitle";
    public static final String DESCRIPTION = "notesDescription";
    public static final String DATE_TIME = "dateTime";
    public static final String COLOR = "noteColor";
    public static final String IMAGE_PATH = "imagePath";
    public static final String URL = "url";
    public static final String STATUS = "status";
    public static final String ARCHIVE_STATUS = "archiveStatus";
    public static final String IS_FAVORITE = "isFavourite";
    public static final String ALARM_SET_COLUMN_NAME = "isAlarmSet";
    public static final String CATEGORIES_TABLE_NAME = "categoriesTable";
    public static final String CATEGORY_ID = "categoryId";
    public static final String CATEGORY_NAME = "categoryName";
    public static final String NOTES_LABELS_TABLE_NAME = "notesLabelsTable";
    public static final String NOTES_LABELS_ID = "notesLabelsId";
    public static final String NOTE_ID = "noteId";
    public static final String LABEL_ID = "labelId";
    public static final String LOCK_STATE = "lockState";
    public static final String LOCK_PIN = "lockPin";
    public static final String LABELS_HIERARCHY_TABLE = "labelHierarchyTable";
    public static final String PARENT_LABEL_ID = "labelId";
    public static final String positionColumn = "position";
    public static final String LABEL_RELATIONSHIPS_TABLE = "relationshipTable";
    public static final String RELATIONSHIP_ID = "relationshipId";
    public static final String CHILD_LABEL_ID = "childLabelId";



    public DBhelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = " CREATE TABLE " + TABLE_NAME + " ( "
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TITLE + " TEXT, "
                + SUB_TITLE + " TEXT, "
                + DESCRIPTION + " TEXT, "
                + DATE_TIME + " TEXT, "
                + COLOR + " TEXT, "
                + IMAGE_PATH + " TEXT, "
                + URL + " TEXT, "
                + STATUS + " INTEGER DEFAULT 0, "
                + ARCHIVE_STATUS + " INTEGER DEFAULT 0, "
                + IS_FAVORITE + " INTEGER DEFAULT 0, "
                + LOCK_PIN + " TEXT, "
                + LOCK_STATE + " INTEGER DEFAULT 0, "
                + positionColumn + " INTEGER , "
                + ALARM_SET_COLUMN_NAME + " INTEGER DEFAULT 0 )";

        db.execSQL(query);


        String categoriesTableQuery = "CREATE TABLE " + CATEGORIES_TABLE_NAME + " ("
                + CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CATEGORY_NAME + " TEXT, "
                + PARENT_LABEL_ID + " INTEGER)";
        db.execSQL(categoriesTableQuery);

        String notesLabelsTableQuery = "CREATE TABLE " + NOTES_LABELS_TABLE_NAME + " ("
                + NOTES_LABELS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOTE_ID + " INTEGER, "
                + LABEL_ID + " INTEGER)";
        db.execSQL(notesLabelsTableQuery);

        /*String notesLabelTableName = "CREATE TABLE " + LABELS_HIERARCHY_TABLE + " ("
                + LABELS_HIERARCHY_TABLE + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PARENT_LABEL_ID + " INTEGER, "
                + LABEL_ID + " INTEGER, "
                + NOTE_ID + " INTEGER)";
        db.execSQL(notesLabelTableName);*/

        /*String relationshipTable = "CREATE TABLE " + LABEL_RELATIONSHIPS_TABLE + " ("
                + RELATIONSHIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PARENT_LABEL_ID + " INTEGER, "
                + CHILD_LABEL_ID + " INTEGER, "
                + "FOREIGN KEY (" + PARENT_LABEL_ID + ") REFERENCES " + CATEGORIES_TABLE_NAME + "(" + CATEGORY_ID + "), "
                + "FOREIGN KEY (" + CHILD_LABEL_ID + ") REFERENCES " + CATEGORIES_TABLE_NAME + "(" + CATEGORY_ID + ")"
                + ")";
        db.execSQL(relationshipTable);
*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

   /* public void addLabelRelationship(long parentLabelId, long childLabelId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PARENT_LABEL_ID, parentLabelId);
        values.put(CHILD_LABEL_ID, childLabelId);
        db.insert(LABEL_RELATIONSHIPS_TABLE, null, values);
    }

    public long addLabelWithParent(String labelName) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Insert the label and get the ID of the inserted row
        ContentValues labelValues = new ContentValues();
        labelValues.put(CATEGORY_NAME, labelName);
        long labelId = db.insert(CATEGORIES_TABLE_NAME, null, labelValues);

        // Insert the label relationship with itself as the parent for now
        ContentValues relationshipValues = new ContentValues();
        relationshipValues.put(PARENT_LABEL_ID, labelId);
        relationshipValues.put(CHILD_LABEL_ID, labelId);
        db.insert(LABEL_RELATIONSHIPS_TABLE, null, relationshipValues);

        return labelId;
    }


    public List<Long> getChildLabels(long parentLabelId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Long> childLabelIds = new ArrayList<>();

        String selection = PARENT_LABEL_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(parentLabelId)};

        Cursor cursor = db.query(LABEL_RELATIONSHIPS_TABLE, new String[]{CHILD_LABEL_ID},
                selection, selectionArgs, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long childLabelId = cursor.getLong(cursor.getColumnIndex(CHILD_LABEL_ID));
                childLabelIds.add(childLabelId);
            }
            cursor.close();
        }

        return childLabelIds;
    }*/


    public long addSubLabel(String labelName, long parentLabelId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CATEGORY_NAME, labelName);
        values.put(PARENT_LABEL_ID, parentLabelId); // Set the parent label ID
        long labelId = db.insert(CATEGORIES_TABLE_NAME, null, values);
        return labelId;
    }

    public void associateNoteWithLabel(int noteId, int labelId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NOTE_ID, noteId);
        values.put(LABEL_ID, labelId);
        db.insert(NOTES_LABELS_TABLE_NAME, null, values);
    }
    public void disassociateNoteFromLabels(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NOTES_LABELS_TABLE_NAME, NOTE_ID + " = ?", new String[]{String.valueOf(noteId)});
    }


    public void deleteLabelAndAssociatedNotes(int labelId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete label from categories table
        db.delete(CATEGORIES_TABLE_NAME, CATEGORY_ID + " = ?", new String[]{String.valueOf(labelId)});

        // Delete associated notes from notes_labels table
        db.delete(NOTES_LABELS_TABLE_NAME, LABEL_ID + " = ?", new String[]{String.valueOf(labelId)});
    }


    public boolean isNoteAssociatedWithLabel(int noteId, int labelId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = NOTE_ID + " = ? AND " + LABEL_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(noteId), String.valueOf(labelId)};

        Cursor cursor = db.query(NOTES_LABELS_TABLE_NAME, null, selection, selectionArgs, null, null, null);

        boolean isAssociated = cursor != null && cursor.getCount() > 0;
        cursor.close();

        return isAssociated;
    }

    public void deleteNoteFromLabel(int noteId, int labelId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = NOTE_ID + " = ? AND " + LABEL_ID + " = ?";
        String[] whereArgs = {String.valueOf(noteId), String.valueOf(labelId)};
        db.delete(NOTES_LABELS_TABLE_NAME, whereClause, whereArgs);
    }

    public ArrayList<AbstractItem> fetchNotesForLabel(int labelId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query notes associated with the given label
        String selection = LABEL_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(labelId)};
        Cursor cursor = db.query(NOTES_LABELS_TABLE_NAME, null, selection, selectionArgs, null, null, null);

        ArrayList<AbstractItem> items = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Retrieve note ID from the cursor
                @SuppressLint("Range") int noteId = cursor.getInt(cursor.getColumnIndex(NOTE_ID));

                // Fetch the note data using noteId
                CreateNotesAdapter note = getNoteById(noteId);

                if (note != null) {
                    items.add(note);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public void associateNoteWithLabelWIthParent(int noteId, int labelId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues hierarchyValues = new ContentValues();
        hierarchyValues.put(LABEL_ID, labelId);
        hierarchyValues.put(NOTE_ID, noteId);
        db.insert(LABELS_HIERARCHY_TABLE, null, hierarchyValues);
    }


    /*public long addLabelWithParent(String labelName) {  //Folder == Label
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CATEGORY_NAME, labelName);
        long labelId = db.insert(CATEGORIES_TABLE_NAME, null, values);

        ContentValues hierarchyValues = new ContentValues();
        hierarchyValues.put(LABEL_ID, labelId);
        hierarchyValues.putNull(NOTE_ID);
        db.insert(LABELS_HIERARCHY_TABLE, null, hierarchyValues);

        return labelId;
    }*/

    public ArrayList<AbstractItem> fetchNotesForLabelHierarchy(int labelId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<AbstractItem> items = new ArrayList<>();

        String query = "SELECT " + NOTE_ID + " FROM " + LABELS_HIERARCHY_TABLE
                + " WHERE " + LABEL_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(labelId)};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int noteId = cursor.getInt(cursor.getColumnIndex(NOTE_ID));
                CreateNotesAdapter note = getNoteById(noteId);

                if (note != null) {
                    items.add(note);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public long addLabel(String labelName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CATEGORY_NAME, labelName);
        // Insert the label and get the ID of the inserted row
        long labelId = db.insert(CATEGORIES_TABLE_NAME, null, values);
        return labelId;
    }

    public void updateLabelName(long labelId, String newLabelName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CATEGORY_NAME, newLabelName);
        db.update(CATEGORIES_TABLE_NAME, values, CATEGORY_ID + " = ?", new String[]{String.valueOf(labelId)});
        db.close();
    }

    @SuppressLint("Range")
    public String getLabelNameById(long labelId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                CATEGORIES_TABLE_NAME,
                new String[]{CATEGORY_NAME},
                CATEGORY_ID + " = ?",
                new String[]{String.valueOf(labelId)},
                null, null, null
        );

        String labelName = "";
        if (cursor != null && cursor.moveToFirst()) {
            labelName = cursor.getString(cursor.getColumnIndex(CATEGORY_NAME));
            cursor.close();
        }

        return labelName;
    }

    public ArrayList<AbstractItem> getAllLabels() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<AbstractItem> labels = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + CATEGORIES_TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int labelId = cursor.getInt(cursor.getColumnIndex(CATEGORY_ID));
                    @SuppressLint("Range") String labelName = cursor.getString(cursor.getColumnIndex(CATEGORY_NAME));

                    AddLabelAdapter label = new AddLabelAdapter(labelId, labelName);
                    labels.add(label);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return labels;
    }

    public List<CreateNotesAdapter> getNotesWithAlarms() {
        List<CreateNotesAdapter> notesWithAlarms = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ALARM_SET_COLUMN_NAME + " = 1";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Create a CreateNotesAdapter object from cursor data
                @SuppressLint("Range") CreateNotesAdapter note = new CreateNotesAdapter(
                        (int) cursor.getLong(cursor.getColumnIndex(_ID)),
                        cursor.getString(cursor.getColumnIndex(TITLE)),
                        cursor.getString(cursor.getColumnIndex(SUB_TITLE)),
                        cursor.getString(cursor.getColumnIndex(DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(DATE_TIME)),
                        cursor.getString(cursor.getColumnIndex(COLOR)),
                        cursor.getString(cursor.getColumnIndex(IMAGE_PATH)),
                        cursor.getString(cursor.getColumnIndex(URL))
                );
                notesWithAlarms.add(note);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return notesWithAlarms;
    }

    public <T extends IIdentifyable & IItem> long addNotes(T data/*, boolean isAlarmSet*/){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        long newNoteId = -1;

        try {
            if (data instanceof CreateNotesAdapter) {
                CreateNotesAdapter createNotesAdapter = (CreateNotesAdapter) data;
                values.put(TITLE, createNotesAdapter.getTitle());
                values.put(SUB_TITLE, createNotesAdapter.getSubTitle());
                values.put(DESCRIPTION, createNotesAdapter.getDescription());
                values.put(DATE_TIME, createNotesAdapter.getDateTime());
                values.put(COLOR, createNotesAdapter.getColor());
                values.put(IMAGE_PATH, createNotesAdapter.getImagePath());
                values.put(URL, createNotesAdapter.getUrl());
                values.put(STATUS, 0); // Not in trash
                values.put(ARCHIVE_STATUS, 0); // Not Archived
                //values.put(IS_FAVORITE, createNotesAdapter.isSelected() ? 1 : 0);
                values.put(IS_FAVORITE, createNotesAdapter.isFavorite() ? 1 : 0);
                //values.put(ALARM_SET_COLUMN_NAME, isAlarmSet ? 1 : 0);

            }

            newNoteId = db.insert(TABLE_NAME, null, values);

        } catch (Exception e){
            db.close();
        }

        return newNoteId;
    }

/*public ArrayList<AbstractItem> fetchNotes(boolean includeTrash){
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = includeTrash ? null : STATUS + " = ?";
        String[] selectionArgs = includeTrash ? null : new String[]{String.valueOf(0)}; // Fetch notes not in trash
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<AbstractItem> items = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()){
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(_ID));
                String title = cursor.getString(1);
                String subTitle = cursor.getString(2);
                String description = cursor.getString(3);
                String time = cursor.getString(4);
                String color = cursor.getString(5);
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex(IMAGE_PATH));
                @SuppressLint("Range") String url = cursor.getString(cursor.getColumnIndex(URL));


                    CreateNotesAdapter createNotesAdapter = new CreateNotesAdapter(
                            id,title,subTitle,description,time,color, imagePath, url
                    );
                    items.add(createNotesAdapter);


            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
}*/


    public long duplicateNote(CreateNotesAdapter existingNote){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        long newNoteId = -1;

        try{

            values.put(TITLE, existingNote.getTitle());
            values.put(SUB_TITLE, existingNote.getSubTitle());
            values.put(DESCRIPTION, existingNote.getDescription());
            values.put(DATE_TIME, existingNote.getDateTime());
            values.put(COLOR, existingNote.getColor());
            values.put(IMAGE_PATH, existingNote.getImagePath());
            values.put(URL, existingNote.getUrl());
            values.put(STATUS, 0);
            values.put(ARCHIVE_STATUS, 0);
            values.put(IS_FAVORITE, existingNote.isFavorite() ? 1 : 0);

            newNoteId = db.insert(TABLE_NAME, null, values);

        } catch (Exception e){
            db.close();

        }
        return newNoteId;
    }
    public void updateNotePosition(int noteId, int newPosition) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(positionColumn, newPosition); // Replace with your actual position column name
        String selection = _ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(noteId)};
        db.update(TABLE_NAME, values, selection, selectionArgs);
    }

    public ArrayList<AbstractItem> fetchRegularNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = STATUS + " = ? AND " + ARCHIVE_STATUS + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(0),  String.valueOf(0)}; // Fetch notes not in trash and favourites also Archived
        String orderBy = positionColumn + " ASC";
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, orderBy);
        ArrayList<AbstractItem> items = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Retrieve data from the cursor
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(_ID));
                String title = cursor.getString(1);
                String subTitle = cursor.getString(2);
                String description = cursor.getString(3);
                String time = cursor.getString(4);
                String color = cursor.getString(5);
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex(IMAGE_PATH));
                @SuppressLint("Range") String url = cursor.getString(cursor.getColumnIndex(URL));

                @SuppressLint("Range") int isFavoriteValue = cursor.getInt(cursor.getColumnIndex(IS_FAVORITE));
                boolean isFavorite = (isFavoriteValue == 1);
                // Inside fetchRegularNotes() method
                @SuppressLint("Range") int lockState = cursor.getInt(cursor.getColumnIndex(LOCK_STATE));
                boolean isLockNote = (lockState == 1);

                // Create a CreateNotesAdapter instance and add to the list
                CreateNotesAdapter createNotesAdapter = new CreateNotesAdapter(
                        id, title, subTitle, description, time, color, imagePath, url
                );

                createNotesAdapter.setFavorite(isFavorite);
                createNotesAdapter.setNoteLock(isLockNote); // Set the lock state for the note

                items.add(createNotesAdapter);


            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

 public ArrayList<AbstractItem> fetchNotesByColor(String colorr) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = STATUS + " = ? AND " + COLOR + " = ? AND " + ARCHIVE_STATUS + " = ?"; //AND " + ARCHIVE_STATUS + " = ?  //, String.valueOf(0)
        String[] selectionArgs = new String[]{String.valueOf(0), colorr, String.valueOf(0)}; // Fetch notes not in trash an favourites also archived
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<AbstractItem> items = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Retrieve data from the cursor
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(_ID));
                String title = cursor.getString(1);
                String subTitle = cursor.getString(2);
                String description = cursor.getString(3);
                String time = cursor.getString(4);
                String color = cursor.getString(5);
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex(IMAGE_PATH));
                @SuppressLint("Range") String url = cursor.getString(cursor.getColumnIndex(URL));

                @SuppressLint("Range") int isFavoriteValue = cursor.getInt(cursor.getColumnIndex(IS_FAVORITE));
                boolean isFavorite = (isFavoriteValue == 1);

                // Create a CreateNotesAdapter instance and add to the list
                CreateNotesAdapter createNotesAdapter = new CreateNotesAdapter(
                        id, title, subTitle, description, time, color, imagePath, url
                );

                createNotesAdapter.setFavorite(isFavorite);

                items.add(createNotesAdapter);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public void lockNoteWithPIN(long noteId, String pin) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues lockValues = new ContentValues();
        lockValues.put(LOCK_STATE, 1);
        lockValues.put(LOCK_PIN, pin);

        db.update(TABLE_NAME, lockValues, _ID + " = ?", new String[]{String.valueOf(noteId)});
    }

    public String getNoteLockPIN(long noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {LOCK_PIN};
        String selection = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(noteId)};

        Cursor cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String storedPin = cursor.getString(cursor.getColumnIndex(LOCK_PIN));
            cursor.close();
            return storedPin;
        }

        return null;
    }



    public void lockNote(long noteId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues lockValues = new ContentValues();
        lockValues.put(LOCK_STATE, 1);

        db.update(TABLE_NAME, lockValues, _ID + " = ?", new String[]{String.valueOf(noteId)});
    }

    public void unlockNote(long noteId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues unlockValues = new ContentValues();
        unlockValues.put(LOCK_STATE, 0);

        db.update(TABLE_NAME, unlockValues, _ID + " = ?", new String[]{String.valueOf(noteId)});
    }


    public ArrayList<AbstractItem> fetchTrashedNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = STATUS + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(1)}; // Fetch trashed notes
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<AbstractItem> items = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Retrieve data from the cursor
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(_ID));
                String title = cursor.getString(1);
                String subTitle = cursor.getString(2);
                String description = cursor.getString(3);
                String time = cursor.getString(4);
                String color = cursor.getString(5);
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex(IMAGE_PATH));

                // Create a TrashAdapter instance and add to the list
                TrashAdapter trashAdapter = new TrashAdapter(
                        id, title, subTitle, description, color, time, imagePath
                );

               // trashAdapter.setButtonClickListner((TrashAdapter.ButtonClickListner) this);

                items.add(trashAdapter);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public ArrayList<AbstractItem> fetchArchivedNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = ARCHIVE_STATUS + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(1)}; // Fetch archived notes
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<AbstractItem> items = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Retrieve data from the cursor
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(_ID));
                String title = cursor.getString(1);
                String subTitle = cursor.getString(2);
                String description = cursor.getString(3);
                String time = cursor.getString(4);
                String color = cursor.getString(5);
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex(IMAGE_PATH));

                // Create a ArchivedAdapter instance and add to the list
                ArchivedAdapter archivedAdapter = new ArchivedAdapter(
                        id, title, subTitle, description, color, time, imagePath
                );

                //archivedAdapter.setButtonClickListner(this);

                items.add(archivedAdapter);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }



    public void updateFavoriteStatus(int noteId, boolean isFavourite){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(IS_FAVORITE, isFavourite ? 1 : 0);

        String selection = _ID + " = ? ";
        String [] selectionArgs = {String.valueOf(noteId)};

        db.update(TABLE_NAME, values, selection, selectionArgs);
        db.close();

    }
    public void updateAlarmSetting(long noteId, int alarmSetting) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALARM_SET_COLUMN_NAME, alarmSetting); // Replace ALARM_COLUMN with your actual column name

        String selection = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(noteId)};

        db.update(TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }

    public void updateNotes(CreateNotesAdapter notesAdapter /*, boolean updateAlarm*/){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(_ID, notesAdapter.getId());
        values.put(TITLE, notesAdapter.getTitle());
        values.put(SUB_TITLE, notesAdapter.getSubTitle());
        values.put(DESCRIPTION, notesAdapter.getDescription());
        values.put(COLOR, notesAdapter.getColor());
        values.put(IMAGE_PATH, notesAdapter.getImagePath());
        values.put(URL, notesAdapter.getUrl());

        String selection = _ID + " = ? ";
        String[] selectionArgs = {String.valueOf(notesAdapter.getId())};

        db.update(TABLE_NAME,values,selection, selectionArgs);
        db.close();

       /* if (updateAlarm) {
            // Update the ALARM_SET column in the database to 1 for the specific note
            updateAlarmSetting(notesAdapter.getId(), 1);
        } else {

            updateAlarmSetting(notesAdapter.getId(), 0);
        }*/

}

    public CreateNotesAdapter getNoteById(int noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(noteId)};

        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        CreateNotesAdapter note = null;

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(_ID);

            if (idIndex != -1) {
                int id = cursor.getInt(idIndex);
                String title = cursor.getString(1);
                String subTitle = cursor.getString(2);
                String description = cursor.getString(3);
                String dateTime = cursor.getString(4);
                String color = cursor.getString(5);
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex(IMAGE_PATH));
                @SuppressLint("Range") String url = cursor.getString(cursor.getColumnIndex(URL));

                note = new CreateNotesAdapter(id, title, subTitle, description, dateTime, color, imagePath, url);
            }
            cursor.close();
        }

        return note;
    }

    public void deleteNotes(int Id){
        SQLiteDatabase db = this.getWritableDatabase();

        String [] selectionArgs = {String.valueOf(Id)};

        String[] columnName = {TITLE};
        Cursor cursor = db.query(TABLE_NAME, columnName, _ID + "=?", selectionArgs, null, null, null, null);

        if (cursor.moveToFirst()){

        }
        cursor.close();

        db.delete(TABLE_NAME, _ID + "=?", selectionArgs);
        db.close();
    }

    public void moveNoteToTrash(int noteId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(STATUS, 1); // Move note to trash

        String selection = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(noteId)};

        Log.d("Database", "moveNoteToTrash: Executing query: " + selection);
        db.update(TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }


    public void restoreNoteFromTrash(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(STATUS, 0); // Restoring the note from trash

        String selection = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(noteId)};

        db.update(TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }

    public void moveNoteToArchive(int noteId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ARCHIVE_STATUS, 1); // Move note to Archive

        String selection = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(noteId)};

        try {
            db.update(TABLE_NAME, values, selection, selectionArgs);
            Log.d("Database", "moveNoteToArchive: Note moved to archive: " + noteId);
        } catch (SQLiteException e) {
            Log.e("Database", "moveNoteToArchive: Error moving note to archive: " + e.getMessage());
        } finally {
            db.close();
        }
    }


    public void restoreNoteFromArchive(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ARCHIVE_STATUS, 0); // Restoring the note from archive

        String selection = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(noteId)};

        db.update(TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }



}
