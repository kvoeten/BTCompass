package com.kazvoeten.btcompass2.data;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.kazvoeten.btcompass2.CompassActivity;
import com.kazvoeten.btcompass2.Week;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by s147137 on 26/11/2017.
 */

public class InstanceRetainer extends Fragment{
    private static final String FRAGMENT_TAG = "InstanceRetainer";
    private static InstanceRetainer fragment;
    public ArrayList<Activity> activities = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void addActivity(Activity activity) {
        this.activities.add(activity);
    }

    public void removeActivity(Activity activity) {
        this.activities.remove(activity);
    }

    public static InstanceRetainer getFragment(CompassActivity origin) {
        FragmentManager fm = origin.getFragmentManager();
        fragment = (InstanceRetainer) fm.findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new InstanceRetainer();
            fm.beginTransaction().add(fragment, FRAGMENT_TAG).commit();
        }
        return fragment;
    }

    public static InstanceRetainer getFragment() {
        return fragment;
    }
}
