package com.starplum.easysign.database;

public class ESDBSchema {
    public static final class SignTable {
        public static final String NAME = "sign_info";

        public static final class SignCols {
            public static final String UUID = "uuid";
            public static final String DATE_STAMP = "date_stamp";
            public static final String IS_SIGNED_IN = "is_signed_in";
            public static final String IS_SIGNED_OUT = "is_signed_out";
            public static final String TIME_SIGNED_IN = "time_signed_in";
            public static final String TIME_SIGNED_OUT = "time_signed_out";
            public static final String IS_WORK_TIME_FULFILL = "is_work_time_fulfill";
        }
    }

    public static final class ConfigTable {
        public static final String NAME = "config";

        public static final class ConfigCols {
            public static final String WORK_FULFILL_MS = "work_fulfill_ms";
        }
    }
}
