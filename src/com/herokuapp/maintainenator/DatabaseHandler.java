package com.herokuapp.maintainenator;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TABLE_NAME = "history";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_DESC = "description";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_PHOTOS_PATH = "photos_path";
    private static final String COLUMN_AUDIO_PATH = "audio_path";

    private static final String DATABASE_NAME = "maintainenator.db";
    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_NAME + "(" + COLUMN_DATE
            + " text, " + COLUMN_DESC + " text, " + COLUMN_LOCATION
            + " text," + COLUMN_PHOTOS_PATH + " text, "
            + COLUMN_AUDIO_PATH + " text);";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void addReport(History history) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, history.getDate());
        values.put(COLUMN_DESC, history.getDescription());
        values.put(COLUMN_LOCATION, history.getLocation());
        values.put(COLUMN_PHOTOS_PATH, history.getPhotosPath());
        values.put(COLUMN_AUDIO_PATH, history.getAudioPath());

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<History> getAllReports() {
        List<History> reportList = new ArrayList<History>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        try {
            if (cursor.moveToFirst()) {
                do {
                    // Don't fetch photos because we are not using it in list view.
                    //TODO add id entry, if user delete some report
                    History history = new History();
                    history.setDate(cursor.getString(0));
                    history.setDescription(cursor.getString(1));
                    history.setLocation(cursor.getString(2));
                    history.setPhotosPath(cursor.getString(3));
                    reportList.add(history);
                } while (cursor.moveToNext());
            }
            return reportList;
        } finally {
            db.close();
            cursor.close();
        }
    }

    public History getReportById(int id) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE rowid=?";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] {""+id});
        try {
            if (cursor.moveToFirst()) {
                History history = new History();
                history.setDate(cursor.getString(0));
                history.setDescription(cursor.getString(1));
                history.setLocation(cursor.getString(2));
                history.setPhotosPath(cursor.getString(3));
                history.setAudioPath(cursor.getString(4));
                return history;
            }
        } finally {
            db.close();
            cursor.close();
        }
        return null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}