package com.kazvoeten.btcompass2;

import com.kazvoeten.btcompass2.data.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.kazvoeten.btcompass2.data.ActivitySettings;
import com.kazvoeten.btcompass2.data.ActivityType;
import com.kazvoeten.btcompass2.data.DatabaseHandler;
import com.kazvoeten.btcompass2.data.EntryAdapter;
import com.kazvoeten.btcompass2.data.InstanceRetainer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

public class CompassActivity extends AppCompatActivity {
    private ImageView button_prevday, button_nextday, button_calendar;
    private TextView text_day, text_time;
    private ListView day_activities;
    private int selectedDay, currentDay;
    private static CompassActivity instance = null;
    private ArrayList<Activity> dayView = new ArrayList<>();
    private EntryAdapter adapter;
    private ViewSwitcher vSwitch;

    private Animation slide_in_left, slide_out_right; //Menu switching animators
    private FloatingActionButton fab;
    private LinearLayout bottom_space;
    public static boolean isInPlanner = false;

    DatabaseHandler database = new DatabaseHandler(this);
    InstanceRetainer fragment;

    public static CompassActivity getInstance() {
        if (instance == null) {
            return new CompassActivity();
        }
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        button_prevday = (ImageView) findViewById(R.id.image_prev_day);
        button_nextday = (ImageView) findViewById(R.id.image_next_day);
        button_calendar = (ImageView) findViewById(R.id.image_title_calendar);
        text_day = (TextView) findViewById(R.id.text_title_day_current);
        text_time = (TextView) findViewById(R.id.text_title_date_current);
        day_activities = (ListView) findViewById(R.id.list_activities);
        vSwitch = (ViewSwitcher) findViewById(R.id.view_switch);
        fab = (FloatingActionButton) findViewById(R.id.fab_main_actions);
        bottom_space = (LinearLayout) findViewById(R.id.bottom_space);

        Calendar today = Calendar.getInstance();
        String time = today.getTime().toLocaleString();
        text_time.setText(time.substring(0, time.indexOf(":") - 2));

        currentDay = today.get(Calendar.DAY_OF_WEEK);

        fragment = InstanceRetainer.getFragment(this);
        database.fillPlanner(fragment.activities);

        adapter = new EntryAdapter(this, dayView);
        day_activities.setAdapter(adapter);
        reloadDayPlanView(currentDay);

        slide_in_left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slide_out_right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        vSwitch.setInAnimation(slide_in_left);
        vSwitch.setOutAnimation(slide_out_right);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInPlanner) {
                    addActivity();
                    vSwitch.showPrevious();
                    fab.setImageResource(R.drawable.ic_add_activity);
                    isInPlanner = false;
                } else {
                    vSwitch.showNext();
                    fab.setImageResource(R.drawable.ic_confirm_activity);
                    isInPlanner = true;
                }
            }
        });

        database.getActivitySettings(PlannerView.activitySettings);

        final BluetoothHandler mBlue = BluetoothHandler.getInstance("Adafruit EZ-Link 6567");
        try {
            mBlue.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        final Handler handler = new Handler();
        final int delay = 10000 * 60; //1 minute

        handler.postDelayed(new Runnable(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run(){
                //send current activity info

                //sort activities
                ArrayList<Activity> activities = fragment.activities;
                activities.sort(new Comparator<Activity>() {
                    @Override
                    public int compare(Activity activity, Activity t1) {
                        return activity.getHour() - t1.getHour();
                    }
                });
                activities.sort(new Comparator<Activity>() {
                    @Override
                    public int compare(Activity activity, Activity t1) {
                        return activity.getMinute() - t1.getMinute();
                    }
                });

                //find earliest activity that still has to be excecuted today
                for (Activity activity : fragment.activities) {
                    if (activity.hasDayEnabled(Week.getByValue(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)))) {
                        int time = Integer.valueOf("" + Calendar.getInstance().get(Calendar.HOUR) + Calendar.getInstance().get(Calendar.MINUTE));
                        int scheduled = Integer.valueOf("" + activity.getHour() + activity.getMinute());
                        if (time < scheduled && scheduled - time <= 30) {
                            mBlue.sendMessage(activity.getType().getValue()
                                    + "," + activity.getLongditude()
                                    + "," + activity.getLatitude());
                            break;
                        } else {
                            continue;
                        }
                    }
                }

                //reschedule function
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (isInPlanner) {
                vSwitch.showPrevious();
                fab.setImageResource(R.drawable.ic_add_activity);
                isInPlanner = false;
                return true;
            } else {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
                return true;
            }
        }
        return false;
    }

    private void reloadDayPlanView(int day) {
        selectedDay = day;
        System.out.println(day);
        button_prevday.setOnClickListener(null);
        button_prevday.setVisibility(View.INVISIBLE);

        button_nextday.setOnClickListener(null);
        button_nextday.setVisibility(View.INVISIBLE);

        Week selection = Week.getByValue(day);
        text_day.setText(Week.getDutchName(selection));

        switch (selection) {
            case MONDAY:
                button_nextday.setVisibility(View.VISIBLE);
                button_nextday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedDay++;
                        reloadDayPlanView(selectedDay);
                    }
                });
                break;
            case SUNDAY:
                button_prevday.setVisibility(View.VISIBLE);
                button_prevday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedDay = Week.SATURDAY.getValue();
                        reloadDayPlanView(selectedDay);
                    }
                });
                break;
            case SATURDAY:
                button_nextday.setVisibility(View.VISIBLE);
                button_nextday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedDay = 1;
                        reloadDayPlanView(selectedDay);
                    }
                });
                button_prevday.setVisibility(View.VISIBLE);
                button_prevday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedDay--;
                        reloadDayPlanView(selectedDay);
                    }
                });
                break;
            default:
                button_nextday.setVisibility(View.VISIBLE);
                button_nextday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedDay++;
                        reloadDayPlanView(selectedDay);
                    }
                });
                button_prevday.setVisibility(View.VISIBLE);
                button_prevday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedDay--;
                        reloadDayPlanView(selectedDay);
                    }
                });
                break;
        }

        dayView.clear();
        for(Activity activity : fragment.activities) {
            if (activity.hasDayEnabled(Week.getByValue(this.selectedDay))) {
                dayView.add(activity);
            }
        }
        adapter.notifyDataSetChanged();

    }

    public void showError(final String message, final boolean fatal) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(CompassActivity.getInstance());
                builder1.setMessage(message);
                System.out.println(message);
                builder1.setCancelable(false);

                builder1.setPositiveButton(
                        "ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                if (fatal) {
                                    System.exit(0);
                                }
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
    }

    public void saveActivitySettings() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                database.saveActivitySettings(PlannerView.activitySettings);
            }
        });
    }

    public void addActivity() {
        final ActivitySettings settings = PlannerView.activitySettings[PlannerView.selectedActivity.getValue()];
        Activity activity = new Activity(PlannerView.selectedHour, PlannerView.selectedMinute, PlannerView.selectedActivity);
        for (int i = 0; i < settings.days.length; ++i) {
            if (settings.days[i]) {
                activity.enableDay(Week.getByValue(i));
            }
        }
        activity.setDestination(settings.location);
        fragment.addActivity(activity);
        database.addActivity(activity);
        reloadDayPlanView(selectedDay);
    }

    public void updateActivity(Activity activity) {
        fragment.removeActivity(activity);
        database.removeActivity(activity);

        fragment.addActivity(activity);
        database.addActivity(activity);

        reloadDayPlanView(selectedDay);
    }

    public void removeActivity(Activity activity) {
        fragment.removeActivity(activity);
        database.removeActivity(activity);
        reloadDayPlanView(selectedDay);
    }
}
