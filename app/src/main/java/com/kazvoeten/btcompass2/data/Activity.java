package com.kazvoeten.btcompass2.data;

import com.kazvoeten.btcompass2.Week;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by s147137 on 26/11/2017.
 */

public class Activity {
    private int hour, minute;
    private ActivityType type;
    private float longditude = 0, latitude = 0;
    private String destination = "";
    private boolean[] days = new boolean[7];

    public Activity(int hour, int minute, ActivityType type) {
        this.hour = hour;
        this.minute = minute;
        this.type = type;
    }

    public void enableDay(Week day) {
        days[day.getPos()] = true;
    }

    public void disableDay(Week day) {
        days[day.getPos()] = false;
    }

    public float getLongditude() {
        return this.longditude;
    }

    public void setLongditude(float longditude) {
        this.longditude = longditude;
    }

    public float getLatitude() {
        return this.latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public ActivityType getType() {
        return this.type;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestination() {
        return this.destination;
    }

    public int getHour() {
        return this.hour;
    }

    public int getMinute() {
        return this.minute;
    }

    public static String getFormattedTime(int hour, int minute) {
        String shour = "" + hour;
        if (shour.length() < 2) {
            shour = "0" + hour;
        }

        String sminute = "" + minute;
        if (sminute.length() < 2) {
            sminute = "0" + minute;
        }

        return shour + ":" + sminute;
    }

    public String getDayPlanAsString() {
        String plan = "";
        for (boolean enabled: days) {
            plan += enabled ? "1" : "0";
        }
        return plan;
    }

    public boolean hasDayEnabled(Week day) {
        return this.days[day.getPos()];
    }
}
