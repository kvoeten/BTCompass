package com.kazvoeten.btcompass2.data;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.kazvoeten.btcompass2.CompassActivity;
import com.kazvoeten.btcompass2.R;
import com.kazvoeten.btcompass2.Week;

import java.util.ArrayList;

/**
 * Created by s147137 on 26/11/2017.
 */

public class EntryAdapter extends ArrayAdapter<Activity> {

    public EntryAdapter(Context context, ArrayList<Activity> activities) {
        super(context, 0, activities);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Activity entry = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_entry, parent, false);
        }

        TextView time = (TextView) convertView.findViewById(R.id.text_time);
        final TextView activity = (TextView) convertView.findViewById(R.id.text_activity);

        time.setText("     " + Activity.getFormattedTime(entry.getHour(), entry.getMinute()));
        activity.setText(entry.getType().name() + "     ");

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog form = new Dialog(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
                form.requestWindowFeature(Window.FEATURE_NO_TITLE);
                form.setContentView(R.layout.activity_edit_activity);
                form.setCancelable(true);
                form.show();

                final TextView title = (TextView) form.findViewById(R.id.ea_title);
                final EditText dest = (EditText) form.findViewById(R.id.ea_edit_dest);
                final Button accept = (Button) form.findViewById(R.id.ea_accept);
                final Button remove = (Button) form.findViewById(R.id.ea_delete);

                title.setText(entry.getType().name());

                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        form.cancel();
                        CompassActivity.getInstance().removeActivity(entry);
                    }
                });

                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        form.cancel();
                        CompassActivity.getInstance().updateActivity(entry);
                    }
                });

                handleToggleView(form, entry);
            }
        });

        return convertView;
    }

    private void handleToggleView(Dialog form, final Activity activity) {
        final ImageView toggle_ma = (ImageView) form.findViewById(R.id.ea_toggle_ma),
                toggle_di = (ImageView) form.findViewById(R.id.ea_toggle_di),
                toggle_wo = (ImageView) form.findViewById(R.id.ea_toggle_wo),
                toggle_do = (ImageView) form.findViewById(R.id.ea_toggle_do),
                toggle_vr = (ImageView) form.findViewById(R.id.ea_toggle_vr),
                toggle_za = (ImageView) form.findViewById(R.id.ea_toggle_za),
                toggle_zo = (ImageView) form.findViewById(R.id.ea_toggle_zo);

        if (!activity.hasDayEnabled(Week.MONDAY)) {
            toggle_ma.setImageResource(R.drawable.ma_off);
        }

        if (!activity.hasDayEnabled(Week.TUESDAY)) {
            toggle_di.setImageResource(R.drawable.di_off);
        }

        if (!activity.hasDayEnabled(Week.WEDNESDAY)) {
            toggle_wo.setImageResource(R.drawable.wo_off);
        }

        if (!activity.hasDayEnabled(Week.THURSDAY)) {
            toggle_do.setImageResource(R.drawable.do_off);
        }

        if (!activity.hasDayEnabled(Week.FRIDAY)) {
            toggle_vr.setImageResource(R.drawable.vr_off);
        }

        if (!activity.hasDayEnabled(Week.SATURDAY)) {
            toggle_za.setImageResource(R.drawable.za_off);
        }

        if (!activity.hasDayEnabled(Week.SUNDAY)) {
            toggle_zo.setImageResource(R.drawable.zo_off);
        }

        toggle_ma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!activity.hasDayEnabled(Week.MONDAY)) {
                    activity.enableDay(Week.MONDAY);
                    toggle_ma.setImageResource(R.drawable.ma_on);
                } else {
                    activity.disableDay(Week.MONDAY);
                    toggle_ma.setImageResource(R.drawable.ma_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });

        toggle_di.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!activity.hasDayEnabled(Week.TUESDAY)) {
                    activity.enableDay(Week.TUESDAY);
                    toggle_di.setImageResource(R.drawable.di_on);
                } else {
                    activity.disableDay(Week.TUESDAY);
                    toggle_di.setImageResource(R.drawable.di_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });

        toggle_wo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!activity.hasDayEnabled(Week.WEDNESDAY)) {
                    activity.enableDay(Week.WEDNESDAY);
                    toggle_wo.setImageResource(R.drawable.wo_on);
                } else {
                    activity.disableDay(Week.WEDNESDAY);
                    toggle_wo.setImageResource(R.drawable.wo_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });

        toggle_do.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!activity.hasDayEnabled(Week.THURSDAY)) {
                    activity.enableDay(Week.THURSDAY);
                    toggle_do.setImageResource(R.drawable.do_on);
                } else {
                    activity.disableDay(Week.THURSDAY);
                    toggle_do.setImageResource(R.drawable.do_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });

        toggle_vr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!activity.hasDayEnabled(Week.FRIDAY)) {
                    activity.enableDay(Week.FRIDAY);
                    toggle_vr.setImageResource(R.drawable.vr_on);
                } else {
                    activity.disableDay(Week.FRIDAY);
                    toggle_vr.setImageResource(R.drawable.vr_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });

        toggle_za.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!activity.hasDayEnabled(Week.SATURDAY)) {
                    activity.enableDay(Week.SATURDAY);
                    toggle_za.setImageResource(R.drawable.za_on);
                } else {
                    activity.disableDay(Week.SATURDAY);
                    toggle_za.setImageResource(R.drawable.za_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });

        toggle_zo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!activity.hasDayEnabled(Week.SUNDAY)) {
                    activity.enableDay(Week.SUNDAY);
                    toggle_zo.setImageResource(R.drawable.zo_on);
                } else {
                    activity.disableDay(Week.SUNDAY);
                    toggle_zo.setImageResource(R.drawable.zo_off);
                }
                CompassActivity.getInstance().saveActivitySettings();
            }
        });
    }
}
