package com.maurelsagbo.project_erl.mapper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.maurelsagbo.project_erl.models.FlightPlan;
import com.maurelsagbo.project_erl.wrapper.DatabaseWrapper;

import java.util.ArrayList;
import java.util.List;

public class FlightPlanORM {

    private static final String TAG = "FlighPlanORM";

    private static final String TABLE_NAME = "flightplan";

    private static final String COMMA_SEP = ", ";

    private static final String COLUMN_ID_TYPE = "INTEGER PRIMARY KEY";
    private static final String COLUMN_ID = "id";

    private static final String COLUMN_NAME_TYPE = "TEXT UNIQUE";
    private static final String COLUMN_NAME = "nom";

    private static final String COLUMN_WAYPOINTCOUNT_TYPE = "INTEGER";
    private static final String COLUMN_WAYPOINTCOUNT = "nombre_wp";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " " + COLUMN_ID_TYPE + COMMA_SEP +
                    COLUMN_NAME + " " + COLUMN_NAME_TYPE + COMMA_SEP +
                    COLUMN_WAYPOINTCOUNT + " " + COLUMN_WAYPOINTCOUNT_TYPE + ");";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static String getColumnId() {
        return COLUMN_ID;
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static long postFlightPlan(Context context, FlightPlan flightPlan){
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getWritableDatabase();

        ContentValues values = flightPlanToContentValues(flightPlan);

        long postId = -1;

        try {
            postId = database.insertOrThrow(FlightPlanORM.TABLE_NAME, "null", values);
            Log.i(TAG, "Inserted new flight plan with ID: " + postId);
        } catch (SQLiteConstraintException e){
            Log.e(TAG, "Failed to insert flight plan");
        }

        database.close();

        return postId;
    }

    public static FlightPlan getFlightPlanId(Context context, long id){
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + FlightPlanORM.TABLE_NAME + " WHERE id = " + id, null);

        Log.i(TAG, "Loaded " + cursor.getCount() + " flight plan...");

        FlightPlan flightPlan = new FlightPlan();

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                flightPlan = cursorToFlightPlan(cursor, context);
                cursor.moveToNext();
            }
            Log.i(TAG, "Flight plan loaded successfully.");
        }

        database.close();

        return flightPlan;
    }

    public static List<FlightPlan> getFlightPlans(Context context){
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + FlightPlanORM.TABLE_NAME, null);

        Log.i(TAG, "Loaded " + cursor.getCount() + " flight plans...");
        List<FlightPlan> flightPlanList = new ArrayList<>();

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                FlightPlan flightPlan = cursorToFlightPlan(cursor, context);
                flightPlanList.add(flightPlan);
                cursor.moveToNext();
            }
            Log.i(TAG, "Flight Plans loaded successfully.");
        }

        database.close();

        return flightPlanList;
    }

    private static ContentValues flightPlanToContentValues(FlightPlan flightPlan){
        ContentValues values = new ContentValues();

        values.put(FlightPlanORM.COLUMN_NAME, flightPlan.getLocationName());
        values.put(FlightPlanORM.COLUMN_WAYPOINTCOUNT, flightPlan.getNumWaypoint());

        return values;
    }

    private static FlightPlan cursorToFlightPlan(Cursor cursor, Context context){
        FlightPlan flightPlan = new FlightPlan();

        flightPlan.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
        flightPlan.setLocationName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
        flightPlan.setNumWaypoint(cursor.getInt(cursor.getColumnIndex(COLUMN_WAYPOINTCOUNT)));
        flightPlan.setWayPoints((WayPointORM.getWayPoints(context, flightPlan.getId())));

        return flightPlan;
    }
}
