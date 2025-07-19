package org.hertz;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class SequencesManager {
    private SequencesManager() {
        // no instance required
        // all static
    }

    private static SequencesDBHelper dbHelper;

    static final List<String> favorites = new ArrayList<>();
    static final List<Sequence> sequences = new ArrayList<>();
    static final Map<Character, IntCell> sequencesIndex = new TreeMap<>();

    static Sequence sequenceView;

    public static synchronized void init(Context context) {
        if (dbHelper == null) {
            dbHelper = new SequencesDBHelper(context);

            favoritesReload();
            sequencesReload();
        }
    }

//    private static void addTestData() {
//        Sequence s = new Sequence("Heliobacterium pylori", "", "2167 728 880 2950");
//        sequenceAdd(s);
//        favoriteAdd(s.name);
//
//        s = new Sequence("E. coli",
//                "escherichia colil cause infection in woonds and the urinary tract.",
//                "282 289 7849 327 548 333 413 642 799 802 804 832 957 1320 1550 1722");
//        sequenceAdd(s);
//    }

    public static void favoriteAdd(Sequence sequence) {
        dbFavoriteUpsert(sequence.name);
        favoritesReload();
    }

    public static void favoriteRemove(Sequence sequence) {
        dbFavoriteDelete(sequence.name);
        favoritesReload();
    }

    public static boolean isFavorite(Sequence sequence) {
        return Collections.binarySearch(favorites, sequence.name) >= 0;
    }

    protected static void dbFavoriteDelete(String favoriteName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(SequencesDBHelper.FAVORITE_TABLE,
                SequencesDBHelper.FAVORITE_NAME + " = ?",
                new String[]{favoriteName});
    }

    protected static void dbFavoriteUpsert(String favoriteName) {
        dbFavoriteDelete(favoriteName);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SequencesDBHelper.FAVORITE_NAME, favoriteName);

        db.insert(SequencesDBHelper.FAVORITE_TABLE, null, values);
    }

    protected static void favoritesReload() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(SequencesDBHelper.FAVORITE_TABLE,
                new String[]{SequencesDBHelper.FAVORITE_NAME},
                null, null, null, null,
                SequencesDBHelper.FAVORITE_NAME + " ASC",
                null
        );

        favorites.clear();
        int nameIx = cursor.getColumnIndexOrThrow(SequencesDBHelper.FAVORITE_NAME);

        while (cursor.moveToNext()) {
            String name = cursor.getString(nameIx);
            favorites.add(name);
        }
        cursor.close();
    }

    public static void sequenceAdd(Sequence sequence) {
        dbSequenceUpsert(sequence);
        sequencesReload();
    }

    public static void sequenceDelete(Sequence sequence) {
        dbSequenceDelete(sequence);
        sequencesReload();
    }

    protected static void dbSequenceDelete(Sequence sequence) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(SequencesDBHelper.SEQUENCE_TABLE,
                SequencesDBHelper.SEQUENCE_NAME + " = ?",
                new String[]{sequence.name});
    }

    protected static void dbSequenceUpsert(Sequence sequence) {
        sequence.validate();
        dbSequenceDelete(sequence);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SequencesDBHelper.SEQUENCE_NAME, sequence.name);
        values.put(SequencesDBHelper.SEQUENCE_DESCRIPTION, sequence.description);
        values.put(SequencesDBHelper.SEQUENCE_FREQUENCIES, sequence.frequencies);

        db.insert(SequencesDBHelper.SEQUENCE_TABLE, null, values);
    }

    protected static void sequencesReload() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                SequencesDBHelper.SEQUENCE_TABLE,
                new String[]{SequencesDBHelper.SEQUENCE_NAME, SequencesDBHelper.SEQUENCE_DESCRIPTION, SequencesDBHelper.SEQUENCE_FREQUENCIES},
                null, null,
                null, null,
                SequencesDBHelper.SEQUENCE_NAME + " ASC"
        );

        sequences.clear();
        int nameIx = cursor.getColumnIndexOrThrow(SequencesDBHelper.SEQUENCE_NAME);
        int descriptionIx = cursor.getColumnIndexOrThrow(SequencesDBHelper.SEQUENCE_DESCRIPTION);
        int frequenciesIx = cursor.getColumnIndexOrThrow(SequencesDBHelper.SEQUENCE_FREQUENCIES);

        while (cursor.moveToNext()) {
            String name = cursor.getString(nameIx);
            String description = cursor.getString(descriptionIx);
            String frequencies = cursor.getString(frequenciesIx);
            sequences.add(new Sequence(name, description, frequencies));
        }
        cursor.close();

        // rebuild index
        sequencesIndex.clear();
        for (Sequence sequence : sequences) {
            Character c = sequence.name.charAt(0);
            IntCell count = sequencesIndex.get(c);
            if (count == null) {
                count = new IntCell();
                count.value = 0;
                sequencesIndex.put(c, count);
            }
            count.value++;
        }
    }

    static Comparator<Sequence> sequenceComparator = new Comparator<Sequence>() {
        @Override
        public int compare(Sequence o1, Sequence o2) {
            return o1.name.compareTo(o2.name);
        }
    };

    public static Sequence getFavoriteSequence(String sequenceName) {
        Sequence sequence = new Sequence(sequenceName, "-", "0");
        int pos = Collections.binarySearch(sequences, sequence, sequenceComparator);
        if (pos >= 0) {
            return sequences.get(pos);
        } else {
            return null;
        }
    }

    public static void sequencesFilter(String filter, List<Integer> filtered) {
        filtered.clear();
        for (int i = 0, len = sequences.size(); i < len; i++) {
            Sequence s = sequences.get(i);
            if (s.name.contains(filter)) {
                filtered.add(i);
                continue;
            }
            if (s.description != null && s.description.contains(filter)) {
                filtered.add(i);
                continue;
            }
            if (s.frequencies.contains(filter)) {
                filtered.add(i);
                continue;
            }
        }
    }

    private static String FREQUENCY_DURATION = "FrequencyDuration";
    private static int FREQUENCY_DURATION_DEFAULT = 180;

    public static void setFrequencyDuration(Activity activity, int duration) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(FREQUENCY_DURATION, duration);
        editor.apply();
    }

    public static int getFrequencyDuration(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(FREQUENCY_DURATION, FREQUENCY_DURATION_DEFAULT);
    }

    public static void setSequenceView(Sequence sequence) {
        sequenceView = sequence;
    }

    public static Sequence getSequenceView() {
        return sequenceView;
    }

}
