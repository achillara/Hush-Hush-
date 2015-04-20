package com.example.anusha.place;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anusha on 3/21/2015.
 */
public  class DatabaseHandler extends SQLiteOpenHelper {


    // All Static variables
    private static final String TAG = "MyActivity";
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "contactsManager";

    // Contacts table name
    private static final String TABLE_CONTACTS = "contacts";

    // Contacts Table Columns names
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = " CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT)";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    // Adding new contact
    void addAddress(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Contact Name


        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }
    public void deleteAddress(String rowId) {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. delete
        db.delete(TABLE_CONTACTS, //table name
                "_id"+"=?",  // selections
                new String[] {rowId}); //selections args
        // 3. close
        db.close();




    }

    public boolean deleteAllAddresses() {
        SQLiteDatabase db = this.getWritableDatabase();
        int doneDelete = 0;
        doneDelete = db.delete(TABLE_CONTACTS, null, null);
        Log.w(TAG, Integer.toString(doneDelete));
        return doneDelete > 0;

    }

    public Cursor fetchAddressesbyName(String inputText) throws SQLException {

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w(TAG, inputText);
        Cursor mCursor = null;
        if (inputText == null || inputText.length() == 0) {
            mCursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID,
                            KEY_NAME,},
                    null, null, null, null, null);

        } else {
            mCursor = db.query(true, TABLE_CONTACTS, new String[]{KEY_ID,
                            KEY_NAME,},
                    KEY_NAME + " like '%" + inputText + "%'", null,
                    null, null, null, null);
        }
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public Cursor fetchAllAddresses() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCursor = db.query(TABLE_CONTACTS, new String[]{ KEY_ID, KEY_NAME,},KEY_ID , null,null,null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}
