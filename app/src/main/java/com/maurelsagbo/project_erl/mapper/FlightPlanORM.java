package com.maurelsagbo.project_erl.mapper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.maurelsagbo.project_erl.models.FlightPlan;
import com.maurelsagbo.project_erl.wrapper.DatabaseWrapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FlightPlanORM {

    private static final String TAG = "FlighPlanORM";

    private static final String TABLE_NAME = "FlightPlan";

    private static final String COMMA_SEP = ", ";

    private static final String COLUMN_ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final String COLUMN_ID = "id";

    private static final String COLUMN_NAME_TYPE = "TEXT";
    private static final String COLUMN_NAME = "nom du lieu";

    private static final String COLUMN_DATE_TYPE = "TEXT";
    private static final String COLUMN_DATE = "date de création";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " " + COLUMN_ID_TYPE + COMMA_SEP +
                    COLUMN_NAME + " " + COLUMN_NAME_TYPE + COMMA_SEP +
                    COLUMN_DATE + " " + COLUMN_DATE_TYPE + COMMA_SEP + ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat();

    public static String getColumnIdType() {
        return COLUMN_ID_TYPE;
    }

    public static void postFlightPlan(Context context, FlightPlan flightPlan){
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getWritableDatabase();

        ContentValues values = flightPlanToContentValues(flightPlan);
        long postId = database.insert(FlightPlanORM.TABLE_NAME, "null", values);
        Log.i(TAG, "Inserted new Post with ID: " + postId);

        database.close();
    }

    public static FlightPlan getFlightPlanId(Context context, int id){
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + FlightPlanORM.TABLE_NAME + " WHERE fp_id = " + id, null);

        Log.i(TAG, "Loaded " + cursor.getCount() + " flight plan...");

        FlightPlan flightPlan = new FlightPlan();

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                flightPlan = cursorToFlightPlan(cursor);
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
                FlightPlan flightPlan = cursorToFlightPlan(cursor);
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

        values.put(FlightPlanORM.COLUMN_ID, flightPlan.getId());
        values.put(FlightPlanORM.COLUMN_NAME, flightPlan.getLocationName());
        values.put(FlightPlanORM.COLUMN_DATE, dateFormat.format(flightPlan.getDate()));

        return values;
    }

    private static FlightPlan cursorToFlightPlan(Cursor cursor){
        FlightPlan flightPlan = new FlightPlan();

        flightPlan.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
        flightPlan.setLocationName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));

        String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
        try {
            flightPlan.setDate(dateFormat.parse(date));
        } catch (ParseException ex) {
            Log.e(TAG, "Failed to parse date " + date + " for flight plan " + flightPlan.getId());
            flightPlan.setDate(null);
        }

        return flightPlan;
    }
}
