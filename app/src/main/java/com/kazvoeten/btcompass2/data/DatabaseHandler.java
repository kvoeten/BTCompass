package com.kazvoeten.btcompass2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kazvoeten.btcompass2.Week;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by s147137 on 26/11/2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int VERSION = 2;
    private static final String DATABASE_NAME = "BTCompassActivities.db";
    private static final String TABLE_ACTIVITIES = "activities";
    private static final String TABLE_SETTINGS = "settings";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_ACTIVITIES + "(type INTEGER, hour INTEGER, minute INTEGER, longditude REAL, latitude REAL, days TEXT, destination TEXT)");
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_SETTINGS + "(type INTEGER, days TEXT, destination TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES); //Data is not retained on upgrade. (currently)
        onCreate(sqLiteDatabase);
    }

    public void addActivity(Activity activity) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("type", activity.getType().getValue());
        values.put("hour", activity.getHour());
        values.put("minute", activity.getMinute());
        values.put("longditude", activity.getLongditude());
        values.put("latitude", activity.getLatitude());
        values.put("days", activity.getDayPlanAsString());
        values.put("destination", activity.getDayPlanAsString());
        sqLiteDatabase.insert(TABLE_ACTIVITIES, null, values);
        sqLiteDatabase.close();
    }

    public void removeActivity(Activity activity) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        String whereClause = "type = ? and hour = ? and minute = ? and days = ?";
        String[] whereArgs = new String[]{
                String.valueOf(activity.getType().getValue()),
                String.valueOf(activity.getHour()),
                String.valueOf(activity.getMinute()),
                activity.getDayPlanAsString()
        };

        sqLiteDatabase.delete(TABLE_ACTIVITIES, whereClause, whereArgs);
    }

    public void fillPlanner(ArrayList<Activity> planner) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_ACTIVITIES, null);

        if(cursor.moveToFirst()) {
            do {
                ActivityType type = ActivityType.getByValue(cursor.getInt(0));
                Activity activity = new Activity(cursor.getInt(1), cursor.getInt(2), type);
                activity.setLongditude(cursor.getFloat(3));
                activity.setLatitude(cursor.getFloat(4));

                String dayplan = cursor.getString(5);
                for(int i = 0; i < 7; i++) {
                    if (dayplan.charAt(i) == '1') {
                        activity.enableDay(Week.getByPos(i));
                    }
                }

                activity.setDestination(cursor.getString(6));
                planner.add(activity);
            } while (cursor.moveToNext());
        }
    }

    public void getActivitySettings(ActivitySettings[] settings) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_SETTINGS, null);

        if(cursor.moveToFirst()) {
            do {
                ActivitySettings setting = new ActivitySettings();
                int type = cursor.getInt(0);
                setting.activityType = ActivityType.getByValue(type);
                String dayplan = cursor.getString(1);
                for(int i = 0; i < 7; i++) {
                    if (dayplan.charAt(i) == '1') {
                        setting.days[i] = true;
                    }
                }
                setting.location = cursor.getString(2);
                settings[type] = setting;
            } while (cursor.moveToNext());
        }
    }

    public void saveActivitySettings(ActivitySettings[] settings) {
        for (int i = 0; i < settings.length; ++i) {
            if (settings[i] == null) return;
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("type", i);

            String dayplan = "";
            for (int j = 0; j < settings[i].days.length; ++j) {
                dayplan += settings[i].days[j] == false ? "0" : "1";
            }

            values.put("days", dayplan);
            values.put("destination", settings[i].location);
            sqLiteDatabase.insert(TABLE_SETTINGS, null, values);
            sqLiteDatabase.close();
        }
    }

}
