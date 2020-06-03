package com.example.pma.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RouteSQLiteHelper  extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "route.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ROUTE = "route";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CITY = "city";

    public static final String TABLE_BUSSTOP = "bus_stop";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";
    public static final String COLUMN_ROUTE_ID = "route_id"; // i za bus stop i za timetable

    public static final String TABLE_TIMETABLE = "timetable";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CONTENT = "content";


    public RouteSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String DB_CREATE_TABLE_ROUTE = "create table "
            + TABLE_ROUTE + "("
            + COLUMN_ID  + " integer primary key, "
            + COLUMN_NAME + " text, "
            + COLUMN_DESCRIPTION + " text, "
            + COLUMN_CITY + " text "
            + ")";

    private static final String DB_CREATE_TABLE_BUS_STOP = "create table "
            + TABLE_BUSSTOP + "("
            + COLUMN_ID  + " integer primary key, "
            + COLUMN_NAME + " text, "
            + COLUMN_LAT + " real, "
            + COLUMN_LNG + " real, "
            + COLUMN_ROUTE_ID + " integer "
            + ")";

    private static final String DB_CREATE_TABLE_TIMETABLE = "create table "
            + TABLE_TIMETABLE + "("
            + COLUMN_ID  + " integer primary key, "
            + COLUMN_TYPE + " text, "
            + COLUMN_CONTENT + " text, "
            + COLUMN_ROUTE_ID + " integer "
            + ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE_TABLE_ROUTE);
        db.execSQL(DB_CREATE_TABLE_BUS_STOP);
        db.execSQL(DB_CREATE_TABLE_TIMETABLE);

    }

    // kada zelimo da izmeninmo tabele, moramo pozvati drop table za sve tabele koje imamo
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUSSTOP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMETABLE);
        onCreate(db);

    }
}
