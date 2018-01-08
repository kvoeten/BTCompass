package com.kazvoeten.btcompass2.data;

import com.kazvoeten.btcompass2.R;

/**
 * Created by s147137 on 26/11/2017.
 */

public enum ActivityType {
    Bakker(0, R.drawable.bakker),
    Gezondheidszorg(1, R.drawable.gezondheidszorg),
    Gym(2, R.drawable.sportschool),
    Markt(3, R.drawable.markt),
    Bezoek(4, R.drawable.bezoek),
    Supermarkt(5, R.drawable.supermarkt),
    Verjaardag(6, R.drawable.verjaardag),
    Wandelen(7, R.drawable.wandelen);

    private int value;
    private Integer image;

    ActivityType(int value, Integer image) {
        this.value = value;
        this.image = image;
    }

    public int getValue() {
        return this.value;
    }

    public Integer getImage() {
        return this.image;
    }

    public static ActivityType getByValue(int value) {
        for (ActivityType type: ActivityType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return ActivityType.Bakker;
    }

    public static ActivityType getNext(ActivityType activity) {
        if (activity.getValue() == 7) {
            return ActivityType.Bakker;
        } else {
            return getByValue(activity.getValue() + 1);
        }
    }

    public static ActivityType getPrevious(ActivityType activity) {
        if (activity.getValue() == 0) {
            return ActivityType.Wandelen;
        } else {
            return getByValue(activity.getValue() - 1);
        }
    }
}
