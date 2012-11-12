package com.herokuapp.maintainenator;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "history";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DESC = "description";
    public static final String COLUMN_LOCATION = "location";
    
    private static final String DATABASE_NAME = "maintainenator.db";
    private static final int DATABASE_VERSION = 1;
    
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + "(" + COLUMN_DATE
            + " text, " + COLUMN_DESC + " text, " + COLUMN_LOCATION
            + " text);";
    
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void addReport(History history) {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, history.getDate()); // Report Date
        values.put(COLUMN_DESC, history.getDescription()); // Report Description
        values.put(COLUMN_LOCATION, history.getLocation()); // Report Location
     
        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }
    
    public List<History> getAllReports() {
        List<History> reportList = new ArrayList<History>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                History history = new History();
                history.setDate(cursor.getString(0));
                history.setDescription(cursor.getString(1));
                history.setLocation(cursor.getString(2));
                // Adding contact to list
                reportList.add(history);
            } while (cursor.moveToNext());
        }
     
        // return report list
        return reportList;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
