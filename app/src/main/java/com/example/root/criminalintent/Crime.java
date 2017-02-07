package com.example.root.criminalintent;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by root on 29.01.17.
 */

public class Crime {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private SimpleDateFormat mDateListFormat = new SimpleDateFormat("EEE dd/MMM/yyyy hh:mm");
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("EEE dd/MM/yyy");
    private SimpleDateFormat mTimeFormat = new SimpleDateFormat("hh:mm");
    private String mDateFormatted;
    private boolean mSolved;
    private String mSuspect;
    private int mSuspectID;




    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public String getDateListFormatted() {
        mDateFormatted = mDateListFormat.format(getDate());
        return mDateFormatted;
    }
    public String getDateFormatted() {
        mDateFormatted = mDateFormat.format(getDate());
        return mDateFormatted;
    }
    public String getTimeFormatted() {
        mDateFormatted = mTimeFormat.format(getDate());
        return mDateFormatted;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getPhotoFilename(){
        return "IMG_"+getId().toString()+".jpg";
    }

    public int getSuspectID() {
        return mSuspectID;
    }

    public void setSuspectID(int suspectID) {
        mSuspectID = suspectID;
    }

    public Crime(UUID id){
        mId = id;
        mDate = new Date();
    }
    public Crime( ){
        this(UUID.randomUUID());
    }

}
