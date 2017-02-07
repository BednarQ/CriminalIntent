package com.example.root.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by root on 05.02.17.
 */

public class TimePickerFragment extends DialogFragment{

    public static final String ARG_TIME = "time";
    public static final String EXTRA_TIME = "com.example.root.criminalintent";

    private TimePicker mTimePicker;

    public static TimePickerFragment newInstance(Date date){
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, date);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time,null);
        mTimePicker = (TimePicker)v.findViewById(R.id.time_picker_dialog_fragment);
        final Date date = (Date) getArguments().getSerializable(ARG_TIME);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.time_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int hour = mTimePicker.getCurrentHour();
                        int minute = mTimePicker.getCurrentMinute();
                        date.setHours(hour);
                        date.setMinutes(minute);
                        sendResult(Activity.RESULT_OK,date);
                    }
                })
                .create();
    }
    private void sendResult(int resultCode, Date time){
        if(getFragmentManager() == null){
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME,time);

        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }
}
