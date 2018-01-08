package com.kazvoeten.btcompass2;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewSwitcher;

import com.kazvoeten.btcompass2.data.Activity;
import com.kazvoeten.btcompass2.data.ActivitySettings;
import com.kazvoeten.btcompass2.data.ActivityType;
import com.kazvoeten.btcompass2.data.InstanceRetainer;

import java.sql.Time;

/**
 * Created by s147137 on 30/11/2017.
 */

public class PlannerView extends LinearLayout {
    private ImageSwitcher imageSwitch;
    private TextView activityName, selectedTime;
    private ImageView button_prev, button_next, button_edit;
    public static int selectedHour = 0, selectedMinute = 0;
    public static ActivityType selectedActivity = ActivityType.Bakker;
    public static ActivitySettings[] activitySettings = new ActivitySettings[Byte.MAX_VALUE];

    public PlannerView(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            inflater.inflate(R.layout.activity_planner, this, true);
        }

        this.imageSwitch = (ImageSwitcher) findViewById(R.id.planner_image_switcher);
        imageSwitch.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(context);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return imageView;
            }
        });

        imageSwitch.setInAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));
        imageSwitch.setOutAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right));
        imageSwitch.setImageResource(ActivityType.Bakker.getImage());

        this.selectedTime = (TextView) findViewById(R.id.planner_time_set);
        this.activityName = (TextView) findViewById(R.id.planner_activity_current);
        activityName.setText(selectedActivity.name());

        this.button_prev = (ImageView) findViewById(R.id.planer_image_prev_activity);
        this.button_next = (ImageView) findViewById(R.id.planner_image_next_activity);

        button_next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedActivity = ActivityType.getNext(selectedActivity);
                activityName.setText(selectedActivity.name());
                imageSwitch.setImageResource(selectedActivity.getImage());
            }
        });

        button_prev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedActivity = ActivityType.getPrevious(selectedActivity);
                activityName.setText(selectedActivity.name());
                imageSwitch.setImageResource(selectedActivity.getImage());
            }
        });

        selectedTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog form = new Dialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
                form.requestWindowFeature(Window.FEATURE_NO_TITLE);
                form.setContentView(R.layout.activity_set_time);
                form.setCancelable(false);
                form.show();

                final TimePicker picked = (TimePicker) form.findViewById(R.id.pick_time);
                Button ok = (Button) form.findViewById(R.id.button_set_time);

                ok.setOnClickListener(new OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View view) {
                        setSelectedHour(picked.getHour());
                        setSelectedMinute(picked.getMinute());
                        form.cancel();
                    }
                });

            }
        });

        this.button_edit = (ImageView) findViewById(R.id.planner_edit_activity);
        button_edit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final ActivitySettings settings = activitySettings[selectedActivity.getValue()];
                if (settings == null) return;
                final Dialog form = new Dialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
                form.requestWindowFeature(Window.FEATURE_NO_TITLE);
                form.setContentView(R.layout.activity_edit_activity);
                form.setCancelable(false);
                form.show();

                final TextView title = (TextView) form.findViewById(R.id.ea_title);
                final EditText dest = (EditText) form.findViewById(R.id.ea_edit_dest);
                final Button accept = (Button) form.findViewById(R.id.ea_accept);
                final Button remove = (Button) form.findViewById(R.id.ea_delete);

                remove.setVisibility(INVISIBLE);
                title.setText(selectedActivity.name());

                accept.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        form.cancel();
                    }
                });

                handleToggleView(form, settings);
            }
        });
    }

     static {
        for(ActivityType activity : ActivityType.values()) {
            activitySettings[activity.getValue()] = new ActivitySettings();
            for(int i = 0; i < activitySettings[activity.getValue()].days.length; ++i) {
                activitySettings[activity.getValue()].days[i] = true;
            }
            activitySettings[activity.getValue()].location = "";
            activitySettings[activity.getValue()].activityType = activity;
        }
    }

    private void handleToggleView(Dialog form, final ActivitySettings settings) {
        final ImageView toggle_ma = (ImageView) form.findViewById(R.id.ea_toggle_ma),
                toggle_di = (ImageView) form.findViewById(R.id.ea_toggle_di),
                toggle_wo = (ImageView) form.findViewById(R.id.ea_toggle_wo),
                toggle_do = (ImageView) form.findViewById(R.id.ea_toggle_do),
                toggle_vr = (ImageView) form.findViewById(R.id.ea_toggle_vr),
                toggle_za = (ImageView) form.findViewById(R.id.ea_toggle_za),
                toggle_zo = (ImageView) form.findViewById(R.id.ea_toggle_zo);

        if (settings.days[Week.MONDAY.getPos()] == false) {
            toggle_ma.setImageResource(R.drawable.ma_off);
        }

        if (settings.days[Week.TUESDAY.getPos()] == false) {
            toggle_di.setImageResource(R.drawable.di_off);
        }

        if (settings.days[Week.WEDNESDAY.getPos()] == false) {
            toggle_wo.setImageResource(R.drawable.wo_off);
        }

        if (settings.days[Week.THURSDAY.getPos()] == false) {
            toggle_do.setImageResource(R.drawable.do_off);
        }

        if (settings.days[Week.FRIDAY.getPos()] == false) {
            toggle_vr.setImageResource(R.drawable.vr_off);
        }

        if (settings.days[Week.SATURDAY.getPos()] == false) {
            toggle_za.setImageResource(R.drawable.za_off);
        }

        if (settings.days[Week.SUNDAY.getPos()] == false) {
            toggle_zo.setImageResource(R.drawable.zo_off);
        }

        toggle_ma.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settings.days[Week.MONDAY.getPos()] == false) {
                    settings.days[Week.MONDAY.getPos()] = true;
                    toggle_ma.setImageResource(R.drawable.ma_on);
                } else {
                    settings.days[Week.MONDAY.getPos()] = false;
                    toggle_ma.setImageResource(R.drawable.ma_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });

        toggle_di.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settings.days[Week.TUESDAY.getPos()] == false) {
                    settings.days[Week.TUESDAY.getPos()] = true;
                    toggle_di.setImageResource(R.drawable.di_on);
                } else {
                    settings.days[Week.TUESDAY.getPos()] = false;
                    toggle_di.setImageResource(R.drawable.di_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });

        toggle_wo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settings.days[Week.WEDNESDAY.getPos()] == false) {
                    settings.days[Week.WEDNESDAY.getPos()] = true;
                    toggle_wo.setImageResource(R.drawable.wo_on);
                } else {
                    settings.days[Week.WEDNESDAY.getPos()] = false;
                    toggle_wo.setImageResource(R.drawable.wo_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });

        toggle_do.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settings.days[Week.THURSDAY.getPos()] == false) {
                    settings.days[Week.THURSDAY.getPos()] = true;
                    toggle_do.setImageResource(R.drawable.do_on);
                } else {
                    settings.days[Week.THURSDAY.getPos()] = false;
                    toggle_do.setImageResource(R.drawable.do_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });

        toggle_vr.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settings.days[Week.FRIDAY.getPos()] == false) {
                    settings.days[Week.FRIDAY.getPos()] = true;
                    toggle_vr.setImageResource(R.drawable.vr_on);
                } else {
                    settings.days[Week.FRIDAY.getPos()] = false;
                    toggle_vr.setImageResource(R.drawable.vr_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });

        toggle_za.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settings.days[Week.SATURDAY.getPos()] == false) {
                    settings.days[Week.SATURDAY.getPos()] = true;
                    toggle_za.setImageResource(R.drawable.za_on);
                } else {
                    settings.days[Week.SATURDAY.getPos()] = false;
                    toggle_za.setImageResource(R.drawable.za_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });

        toggle_zo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settings.days[Week.SUNDAY.getPos()] == false) {
                    settings.days[Week.SUNDAY.getPos()] = true;
                    toggle_zo.setImageResource(R.drawable.zo_on);
                } else {
                    settings.days[Week.SUNDAY.getPos()] = false;
                    toggle_zo.setImageResource(R.drawable.zo_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });
    }

    public void setSelectedHour(int hour) {
        this.selectedHour = hour;
        this.selectedTime.setText(Activity.getFormattedTime(this.selectedHour, this.selectedMinute));
    }

    public void setSelectedMinute(int minute) {
        this.selectedMinute = minute;
        this.selectedTime.setText(Activity.getFormattedTime(this.selectedHour, this.selectedMinute));
    }

}
