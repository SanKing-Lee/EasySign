package com.starplum.easysign.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

public class SignInfo {
    public static final SimpleDateFormat DATE_STAMP_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);

    private UUID mId;
    private long mWorkFulfillMs;
    private boolean mSignedIn;
    private long mSignInMs;

    private boolean mSignedOut;
    private long mSignOutMs;

    private boolean mWorkFulfill;
    private long mWorkMs;

    private String mDateStamp;

    public SignInfo() {
        mId = UUID.randomUUID();
    }

    public SignInfo(String dateStamp, boolean signedIn, long signInMs, boolean signedOut, long signOutMs, boolean workFulfill) {
        mId = UUID.randomUUID();
        mSignedIn = signedIn;
        mSignInMs = signInMs;
        mSignedOut = signedOut;
        mSignOutMs = signOutMs;
        mWorkFulfill = workFulfill;
        mDateStamp = dateStamp;
    }

    @Override
    public String toString() {
        return "SignInfo{" +
                "mWorkFulfillMs=" + mWorkFulfillMs +
                ", mSignedIn=" + mSignedIn +
                ", mSignInMs=" + mSignInMs +
                "(" + DateFormat.getTimeInstance().format(mSignInMs) + ")" +
                ", mSignedOut=" + mSignedOut +
                ", mSignOutMs=" + mSignOutMs +
                "(" + DateFormat.getTimeInstance().format(mSignOutMs) + ")" +
                ", mWorkFulfill=" + mWorkFulfill +
                ", mWorkMs=" + mWorkMs +
                "(" + buildWorkTime() + ")" +
                ", mDateStamp='" + mDateStamp + '\'' +
                '}';
    }

    public void calWorkMs() {
        mWorkMs = mSignOutMs - mSignInMs;
        if (mWorkMs < 0) {
            mWorkMs = 0;
        }
    }

    public String buildWorkTime() {
        StringBuilder sb = new StringBuilder();
        long second = mWorkMs / 1000;
        long minute = second / 60;
        long hour = minute / 60;
        second %= 60;
        minute %= 60;
        if (hour < 10) {
            sb.append("0");
        }
        sb.append(hour);
        sb.append(":");
        if (minute < 10) {
            sb.append("0");
        }
        sb.append(minute);
//        sb.append(":");
//        if(second < 10) {
//            sb.append("0");
//        }
//        sb.append(second);
        return sb.toString();
    }

    public long getWorkFulfillMs() {
        return mWorkFulfillMs;
    }

    public void setWorkFulfillMs(long workFulfillMs) {
        mWorkFulfillMs = workFulfillMs;
    }

    public void setWorkFulfill() {
        mWorkFulfill = (mWorkMs >= mWorkFulfillMs);
    }

    public boolean isSignedIn() {
        return mSignedIn;
    }

    public void setSignedIn(boolean signedIn) {
        mSignedIn = signedIn;
    }

    public long getSignInMs() {
        return mSignInMs;
    }

    public void setSignInMs(long signInMs) {
        mSignInMs = signInMs;
    }

    public boolean isSignedOut() {
        return mSignedOut;
    }

    public void setSignedOut(boolean signedOut) {
        mSignedOut = signedOut;
    }

    public long getSignOutMs() {
        return mSignOutMs;
    }

    public void setSignOutMs(long signOutMs) {
        mSignOutMs = signOutMs;
    }

    public boolean isWorkFulfill() {
        return mWorkFulfill;
    }

    public void setWorkFulfill(boolean workFulfill) {
        mWorkFulfill = workFulfill;
    }

    public long getWorkMs() {
        return mWorkMs;
    }

    public void setWorkMs(long workMs) {
        mWorkMs = workMs;
    }

    public String getDateStamp() {
        return mDateStamp;
    }

    public void setDateStamp(String dateStamp) {
        mDateStamp = dateStamp;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }
}
