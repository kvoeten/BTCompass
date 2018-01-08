package com.kazvoeten.btcompass2;

/**
 * Created by s147137 on 24/11/2017.
 */

public enum Week {
    MONDAY(2, 0),
    TUESDAY(3, 1),
    WEDNESDAY(4, 2),
    THURSDAY(5, 3),
    FRIDAY(6, 4),
    SATURDAY(7, 5),
    SUNDAY(1, 6);

    private int day, position;

    private Week(int day, int position) {
        this.day = day;
        this.position = position;
    }

    public int getValue() {
        return this.day;
    }

    public int getPos() {
        return this.position;
    }

    public static Week getByValue(int value) {
        for (Week week: Week.values()) {
            if (week.getValue() == value) {
                return week;
            }
        }
        return Week.MONDAY;
    }

    public static Week getByPos(int pos) {
        for (Week week: Week.values()) {
            if (week.getPos() == pos) {
                return week;
            }
        }
        return Week.MONDAY;
    }

    public static String getDutchName(Week week) {
        switch (week) {
            case MONDAY:
                return "MAANDAG";
            case TUESDAY:
                return "DINSDAG";
            case WEDNESDAY:
                return "WOENSDAG";
            case THURSDAY:
                return "DONDERDAG";
            case FRIDAY:
                return "VRIJDAG";
            case SATURDAY:
                return "ZATERDAG";
            case SUNDAY:
                return "ZONDAG";
            default:
                return "";
        }
    }
}
