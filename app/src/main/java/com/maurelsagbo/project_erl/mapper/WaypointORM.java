package com.maurelsagbo.project_erl.mapper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.maurelsagbo.project_erl.models.FlightPlan;
import com.maurelsagbo.project_erl.models.WayPoint;
import com.maurelsagbo.project_erl.wrapper.DatabaseWrapper;

import java.util.ArrayList;
import java.util.List;

public class WayPointORM {

    private static final String TAG = "WayPointORM";

    private static final String TABLE_NAME = "waypoint";

    private static final String COMMA_SEP = ", ";

    private static final String COLUMN_ID_TYPE = "INTEGER PRIMARY KEY";
    private static final String COLUMN_ID = "id";

    private static final String COLUMN_FLIGHTPLAN_ID_TYPE = "INTEGER";
    private static final String COLUMN_FLIGHTPLAN_ID = "fp_id";

    private static final String COLUMN_POSITION_TYPE = "INTEGER";
    private static final String COLUMN_POSITION= "position";

    private static final String COLUMN_LATITUDE_TYPE = "REAL";
    private static final String COLUMN_LATITUDE = "latitude";

    private static final String COLUMN_LONGITUDE_TYPE = "REAL";
    private static final String COLUMN_LONGITUDE = "longitude";

    private static final String COLUMN_ALTITUDE_TYPE = "REAL";
    private static final String COLUMN_ALTITUDE = "altitude";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " " + COLUMN_ID_TYPE + COMMA_SEP +
                    COLUMN_FLIGHTPLAN_ID + " " + COLUMN_FLIGHTPLAN_ID_TYPE + COMMA_SEP +
                    COLUMN_POSITION + " " + COLUMN_POSITION_TYPE + COMMA_SEP +
                    COLUMN_LATITUDE + " " + COLUMN_LATITUDE_TYPE + COMMA_SEP +
                    COLUMN_LONGITUDE + " " + COLUMN_LONGITUDE_TYPE + COMMA_SEP +
                    COLUMN_ALTITUDE + " " + COLUMN_ALTITUDE_TYPE + COMMA_SEP +
                    "FOREIGN KEY(" + COLUMN_FLIGHTPLAN_ID + ") REFERENCES " + FlightPlanORM.getTableName() + "(" + FlightPlanORM.getColumnId()+ "));";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    /**
     * Method that insert a waypoint in the database
     * @param context
     * @param wayPoint
     */
    public static void postWaypoint(Context context, WayPoint wayPoint, FlightPlan flightPlan){
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getWritableDatabase();

        ContentValues values = wayPointToContentValues(wayPoint, flightPlan);
        long postId = database.insert(WayPointORM.TABLE_NAME, "null", values);
        Log.i(TAG, "Inserted new waypoint with ID: " + postId);

        database.close();
    }

    /**
     * Method that get the waypoints from the database
     * @param context
     * @param flightPlan
     * @return
     */
    public static List<WayPoint> getWayPoints(Context context, FlightPlan flightPlan){
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + WayPointORM.TABLE_NAME + " WHERE fp_id = " + flightPlan.getId(), null);

        Log.i(TAG, "Loaded " + cursor.getCount() + " waypoints...");
        List<WayPoint> wayPointList = new ArrayList<>();

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                WayPoint wayPoint = cursorToWayPoint(cursor);
                wayPointList.add(wayPoint);
                cursor.moveToNext();
            }
            Log.i(TAG, "Waypoints loaded successfully.");
        }

        database.close();

        return wayPointList;
    }

    /**
     * Convert a WayPoint in ContentValues before inserting in the database
     * @param wayPoint
     * @param flightPlan
     * @return
     */
    private static ContentValues wayPointToContentValues(WayPoint wayPoint, FlightPlan flightPlan){
        ContentValues values = new ContentValues();

        values.put(WayPointORM.COLUMN_POSITION, wayPoint.getPosition());
        values.put(WayPointORM.COLUMN_ALTITUDE, wayPoint.getAltitude());
        values.put(WayPointORM.COLUMN_LATITUDE, wayPoint.getLatitude());
        values.put(WayPointORM.COLUMN_LONGITUDE, wayPoint.getLongitude());
        values.put(WayPointORM.COLUMN_FLIGHTPLAN_ID, flightPlan.getId());

        return values;
    }

    /**
     * Populates a WayPoint object with data from a Cursor
     * @param cursor
     * @return
     */
    private static WayPoint cursorToWayPoint(Cursor cursor){
        WayPoint wayPoint = new WayPoint();
        wayPoint.setPosition(cursor.getInt(cursor.getColumnIndex(COLUMN_POSITION)));
        wayPoint.setAltitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_ALTITUDE)));
        wayPoint.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)));
        wayPoint.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)));

        return wayPoint;
    }
}
