package alf.stream.clockradio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Alf on 7/7/2017.
 */

public class DataBaseHandler extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "alarms.db";
    public static final String TABLE_ALARMS = "alarms";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_HOUR = "_hour";
    public static final String COLUMN_MINUTE = "_minute";
    public static final String COLUMN_MON = "_mon";
    public static final String COLUMN_TUE = "_tue";
    public static final String COLUMN_WED = "_wed";
    public static final String COLUMN_THU = "_thu";
    public static final String COLUMN_FRI = "_fri";
    public static final String COLUMN_SAT = "_sat";
    public static final String COLUMN_SUN = "_sun";
    public static final String COLUMN_STATION = "_station";
    public static final String COLUMN_REGION = "_region";
    public static final String COLUMN_VOLUME = "_volume";

    public DataBaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ALARMS + " ( " +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HOUR + " INTEGER NOT NULL, " +
                COLUMN_MINUTE + " INTEGER NOT NULL, " +
                COLUMN_MON + " INTEGER DEFAULT 1, " +
                COLUMN_TUE + " INTEGER DEFAULT 1, " +
                COLUMN_WED + " INTEGER DEFAULT 1, " +
                COLUMN_THU + " INTEGER DEFAULT 1, " +
                COLUMN_FRI + " INTEGER DEFAULT 1, " +
                COLUMN_SAT + " INTEGER DEFAULT 1, " +
                COLUMN_SUN + " INTEGER DEFAULT 1, " +
                COLUMN_STATION + " INTEGER DEFAULT 0, " +
                COLUMN_REGION + " INTEGER DEFAULT 0, " +
                COLUMN_VOLUME + " INTEGER NOT NULL " +
                ");";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String dropTable = "DROP TABLE IF EXISTS " + TABLE_ALARMS;
        db.execSQL(dropTable);
        onCreate(db);
    }

    public void addAlarm(Alarm alarm){
        getWritableDatabase().insert(TABLE_ALARMS, null, getValues(alarm));
    }

    public void updateAlarm(Alarm alarm){
        getWritableDatabase().update(TABLE_ALARMS, getValues(alarm), COLUMN_ID+"="+alarm.get_id(), null);
    }

    public void deleteAlarm(int id){
        getWritableDatabase().execSQL("DELETE FROM " + TABLE_ALARMS + " WHERE " + COLUMN_ID + " = " + id + ";");
    }

    public void deleteAlarm(Alarm alarm) {
        deleteAlarm(alarm.get_id());
    }

    public Alarm getAlarm(int id){
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_ALARMS + " WHERE " + COLUMN_ID + " = " + id + ";", null);
        if(c.moveToFirst()){
            return alarmFromCursor(c);
        }
        return null;
    }

    public ArrayList<Alarm> getAllAlarms(){
        ArrayList<Alarm> alarms = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_ALARMS + ";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            alarms.add(alarmFromCursor(c));
            c.moveToNext();
        }
        return alarms;
    }

    private ContentValues getValues(Alarm alarm){
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUR, alarm.get_hour());
        values.put(COLUMN_MINUTE, alarm.get_minute());
        values.put(COLUMN_MON, alarm.get_mon() ? 1 : 0);
        values.put(COLUMN_TUE, alarm.get_tue() ? 1 : 0);
        values.put(COLUMN_WED, alarm.get_wed() ? 1 : 0);
        values.put(COLUMN_THU, alarm.get_thu() ? 1 : 0);
        values.put(COLUMN_FRI, alarm.get_fri() ? 1 : 0);
        values.put(COLUMN_SAT, alarm.get_sat() ? 1 : 0);
        values.put(COLUMN_SUN, alarm.get_sun() ? 1 : 0);
        values.put(COLUMN_STATION, alarm.get_station());
        values.put(COLUMN_REGION, alarm.get_region());
        values.put(COLUMN_VOLUME, alarm.get_volume());
        return values;
    }

    private Alarm alarmFromCursor(Cursor c) {
        return new Alarm(
                c.getInt(c.getColumnIndex(COLUMN_ID)),
                c.getInt(c.getColumnIndex(COLUMN_HOUR)),
                c.getInt(c.getColumnIndex(COLUMN_MINUTE)),
                c.getInt(c.getColumnIndex(COLUMN_MON)) == 1,
                c.getInt(c.getColumnIndex(COLUMN_TUE)) == 1,
                c.getInt(c.getColumnIndex(COLUMN_WED)) == 1,
                c.getInt(c.getColumnIndex(COLUMN_THU)) == 1,
                c.getInt(c.getColumnIndex(COLUMN_FRI)) == 1,
                c.getInt(c.getColumnIndex(COLUMN_SAT)) == 1,
                c.getInt(c.getColumnIndex(COLUMN_SUN)) == 1,
                c.getInt(c.getColumnIndex(COLUMN_STATION)),
                c.getInt(c.getColumnIndex(COLUMN_REGION)),
                c.getInt(c.getColumnIndex(COLUMN_VOLUME))
        );
    }
}
