package com.starplum.easysign.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.starplum.easysign.database.ESDBHelper;

public class ConfigDao {
    private SQLiteDatabase mDatabase;

    public ConfigDao(Context context) {
        mDatabase = new ESDBHelper(context).getWritableDatabase();
    }


}
