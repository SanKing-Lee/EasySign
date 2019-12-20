package com.starplum.easysign.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.starplum.easysign.database.ESDBHelper;
import com.starplum.easysign.database.ESDBSchema;
import com.starplum.easysign.model.SignInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SignInfoDao {

    private class SICursorWrapper extends CursorWrapper {
        public SICursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public SignInfo getSignInfo() {
            String uuid = getString(getColumnIndex(ESDBSchema.SignTable.SignCols.UUID));
            String dateStamp = getString(getColumnIndex(ESDBSchema.SignTable.SignCols.DATE_STAMP));
            String sSignedIn = getString(getColumnIndex(ESDBSchema.SignTable.SignCols.IS_SIGNED_IN));
            String sSignInMs = getString(getColumnIndex(ESDBSchema.SignTable.SignCols.TIME_SIGNED_IN));
            String sSignedOut = getString(getColumnIndex(ESDBSchema.SignTable.SignCols.IS_SIGNED_OUT));
            String sSignOutMs = getString(getColumnIndex(ESDBSchema.SignTable.SignCols.TIME_SIGNED_OUT));
            String sWorkMs = getString(getColumnIndex(ESDBSchema.SignTable.SignCols.TIME_WORK));
            String sWorkFulFillMs = getString(getColumnIndex(ESDBSchema.SignTable.SignCols.TIME_WORK_FULFILL));
            String sWorkFulfill = getString(getColumnIndex(ESDBSchema.SignTable.SignCols.IS_WORK_TIME_FULFILL));

            SignInfo signInfo = new SignInfo();
            if (uuid != null) signInfo.setId(UUID.fromString(uuid));
            if (dateStamp != null) signInfo.setDateStamp(dateStamp);
            if (sSignedIn != null) signInfo.setSignedIn(Integer.parseInt(sSignedIn) != 0);
            if (sSignInMs != null) signInfo.setSignInMs(Long.parseLong(sSignInMs));
            if (sSignedOut != null) signInfo.setSignedOut(Integer.parseInt(sSignedOut) != 0);
            if (sSignOutMs != null) signInfo.setSignOutMs(Long.parseLong(sSignOutMs));
            if(sWorkMs != null) signInfo.setWorkMs(Long.parseLong(sWorkMs));
            if(sWorkFulFillMs != null) signInfo.setWorkFulfillMs(Long.parseLong(sWorkFulFillMs));
            if (sWorkFulfill != null) signInfo.setWorkFulfill(Integer.parseInt(sWorkFulfill) != 0);
            return signInfo;
        }
    }

    private static final String LOG_TAG = "SignInfoDao";
    private static final String SIGN_INFO_DATE_STAMP_WHERE_CLAUSE = ESDBSchema.SignTable.SignCols.DATE_STAMP + " = ?";
    private static final String SIGN_INFO_UUID_WHERE_CLAUSE = ESDBSchema.SignTable.SignCols.UUID + " = ?";
    private static final String SIGN_INFO_DATE_STAMP_LIKE_CLAUSE = ESDBSchema.SignTable.SignCols.DATE_STAMP + " LIKE ?";

    private SQLiteDatabase mDatabase;

    public SignInfoDao(Context context) {
        mDatabase = new ESDBHelper(context).getWritableDatabase();
    }

    private ContentValues getContentValues(SignInfo signInfo) {
        ContentValues values = new ContentValues();
        values.put(ESDBSchema.SignTable.SignCols.UUID, signInfo.getId().toString());
        values.put(ESDBSchema.SignTable.SignCols.DATE_STAMP, signInfo.getDateStamp());
        values.put(ESDBSchema.SignTable.SignCols.IS_SIGNED_IN, signInfo.isSignedIn());
        values.put(ESDBSchema.SignTable.SignCols.IS_SIGNED_OUT, signInfo.isSignedOut());
        values.put(ESDBSchema.SignTable.SignCols.TIME_SIGNED_IN, signInfo.getSignInMs());
        values.put(ESDBSchema.SignTable.SignCols.TIME_SIGNED_OUT, signInfo.getSignOutMs());
        values.put(ESDBSchema.SignTable.SignCols.TIME_WORK, signInfo.getWorkMs());
        values.put(ESDBSchema.SignTable.SignCols.TIME_WORK_FULFILL, signInfo.getWorkFulfillMs());
        values.put(ESDBSchema.SignTable.SignCols.IS_WORK_TIME_FULFILL, signInfo.isWorkFulfill());
        return values;
    }

    private SICursorWrapper querySignInfo(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                ESDBSchema.SignTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new SICursorWrapper(cursor);
    }

    public SignInfo loadSignInfo(String dateStamp) {
        SICursorWrapper cursor = querySignInfo(SIGN_INFO_DATE_STAMP_WHERE_CLAUSE,
                new String[]{dateStamp});
        SignInfo signInfo = new SignInfo();
        signInfo.setDateStamp(dateStamp);
        if (cursor.isAfterLast()) {
            Log.i(LOG_TAG, "New sign info: " + signInfo.toString());
            insertSignInfo(signInfo);
            return signInfo;
        }
        cursor.moveToLast();
        signInfo = cursor.getSignInfo();
        Log.i(LOG_TAG, "Load sign info: " + signInfo.toString());
        cursor.close();
        return signInfo;
    }

    public SignInfo findSignInfoByUUID(UUID id) {
        try (SICursorWrapper cursorWrapper = querySignInfo(SIGN_INFO_UUID_WHERE_CLAUSE, new String[]{id.toString()})) {
            if (cursorWrapper.getCount() == 0) {
                return null;
            }
            cursorWrapper.moveToFirst();
            return cursorWrapper.getSignInfo();
        }
    }

    public void insertSignInfo(SignInfo signInfo) {
        ContentValues values = getContentValues(signInfo);
        mDatabase.insert(ESDBSchema.SignTable.NAME, null, values);
        Log.i(LOG_TAG, "insert: " + signInfo.toString());
    }

    public void updateSignInfo(SignInfo signInfo) {
        ContentValues values = getContentValues(signInfo);
        mDatabase.update(ESDBSchema.SignTable.NAME, values,
                SIGN_INFO_DATE_STAMP_WHERE_CLAUSE,
                new String[]{signInfo.getDateStamp()});
        Log.i(LOG_TAG, "update: " + signInfo.toString());
    }

    public List<SignInfo> getSignInfos() {
        List<SignInfo> signInfos = new ArrayList<>();

        try (SICursorWrapper cursorWrapper = querySignInfo(null, null)) {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                signInfos.add(cursorWrapper.getSignInfo());
                cursorWrapper.moveToNext();
            }
            return signInfos;
        }
    }

    public List<SignInfo> getFinishedSignInfos() {
        List<SignInfo> signInfos = new ArrayList<>();

        try (SICursorWrapper cursorWrapper = querySignInfo(null, null)) {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                SignInfo signInfo = cursorWrapper.getSignInfo();
                if (signInfo.isSignedOut() && signInfo.isSignedIn()) {
                    signInfos.add(cursorWrapper.getSignInfo());
                }
                cursorWrapper.moveToNext();
            }
            return signInfos;
        }
    }

    public List<SignInfo> getFinishedSignInfosByYear(int year)
    {
        List<SignInfo> yearSignInfos = new ArrayList<>();

        try(SICursorWrapper cursorWrapper = querySignInfo(SIGN_INFO_DATE_STAMP_LIKE_CLAUSE, new String[] {year+"%"})) {
            cursorWrapper.moveToFirst();
            while(!cursorWrapper.isAfterLast()) {
                SignInfo signInfo = cursorWrapper.getSignInfo();
                if (signInfo.isSignedOut() && signInfo.isSignedIn()) {
                    Log.i(LOG_TAG, "getFinishedSignInfoByYear: " + signInfo.toString());
                    yearSignInfos.add(cursorWrapper.getSignInfo());
                }
                cursorWrapper.moveToNext();
            }
        }
        return yearSignInfos;
    }
}