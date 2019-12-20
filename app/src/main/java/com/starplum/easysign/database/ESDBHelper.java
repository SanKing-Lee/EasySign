package com.starplum.easysign.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ESDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "esDb.db";
    private static final int DATABASE_VERSION = 1;

    public ESDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
            create table sign_info (
                _id integer primary key autoincrement,
                uuid,
                date_stamp,
                is_signed_in,
                is_signed_out,
                time_signed_in,
                time_signed_out,
                time_work,
                time_work_fulfill;
                is_work_time_fulfill
            );
        */
        db.execSQL(
                "create table " + ESDBSchema.SignTable.NAME + "(" +
                        " id integer primary key autoincrement, " +
                        ESDBSchema.SignTable.SignCols.UUID + ", " +
                        ESDBSchema.SignTable.SignCols.DATE_STAMP + "," +
                        ESDBSchema.SignTable.SignCols.IS_SIGNED_IN + ", " +
                        ESDBSchema.SignTable.SignCols.IS_SIGNED_OUT + ", " +
                        ESDBSchema.SignTable.SignCols.TIME_SIGNED_IN + ", " +
                        ESDBSchema.SignTable.SignCols.TIME_SIGNED_OUT + ", " +
                        ESDBSchema.SignTable.SignCols.TIME_WORK + ", " +
                        ESDBSchema.SignTable.SignCols.TIME_WORK_FULFILL + ", " +
                        ESDBSchema.SignTable.SignCols.IS_WORK_TIME_FULFILL +
                        ")"
        );

        /*
            create table config (
                _id integer primary key autoincrement,
                work_fulfill_ms
                )
         */
        db.execSQL(
                "create table " + ESDBSchema.ConfigTable.NAME + "(" +
                        " id integer primary key autoincrement, " +
                        ESDBSchema.ConfigTable.ConfigCols.WORK_FULFILL_MS +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
