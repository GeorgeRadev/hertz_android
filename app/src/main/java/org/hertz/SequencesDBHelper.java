package org.hertz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SequencesDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Sequences.db";

    public static final String FAVORITE_TABLE = "FAVORITE";
    public static final String FAVORITE_NAME = "NAME";

    public static final String SEQUENCE_TABLE = "SEQUENCE";
    public static final String SEQUENCE_NAME = "NAME";
    public static final String SEQUENCE_DESCRIPTION = "DESCRIPTION";
    public static final String SEQUENCE_FREQUENCIES = "FREQUENCIES";

    public SequencesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FAVORITE_TABLE + "("
                + FAVORITE_NAME + " TEXT NOT NULL PRIMARY KEY ASC)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + SEQUENCE_TABLE + "("
                + SEQUENCE_NAME + " TEXT NOT NULL PRIMARY KEY ASC,"
                + SEQUENCE_DESCRIPTION + " TEXT,"
                + SEQUENCE_FREQUENCIES + " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
