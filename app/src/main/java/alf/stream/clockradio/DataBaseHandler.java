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
// TODO: getWritable and getReadable in seperate thread...?? Depends on speed - It's a very small database.
public class DataBaseHandler extends SQLiteOpenHelper {
    Context context;
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "alarms.db";

    public DataBaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
//        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(StationTable.SQL_CREATE_TABLE);
        populateStationTable(db);
        db.execSQL(AlarmTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(AlarmTable.SQL_DROP_TABLE);
        db.execSQL(StationTable.SQL_DROP_TABLE);
        onCreate(db);
    }

    /******************************
     | STATION TABLE AND HANDLERS |
     ******************************/
    public final class StationTable {
        public static final String TABLE_NAME = "stations";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "_name";
        public static final String COLUMN_LINK = "_link";

        public static final String SQL_CREATE_TABLE =  "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_LINK + " TEXT NOT NULL " +
                ");";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    private void populateStationTable(SQLiteDatabase db){
        String[] stations = context.getResources().getStringArray(R.array.station_names);
        String[] stationLinks = context.getResources().getStringArray(R.array.station_links);
        ContentValues contentValues = new ContentValues();
        for(int i = 0; i < stations.length; i++) {
            contentValues.put(StationTable.COLUMN_ID, i);
            contentValues.put(StationTable.COLUMN_NAME, stations[i]);
            contentValues.put(StationTable.COLUMN_LINK, stationLinks[i]);
            db.insert(StationTable.TABLE_NAME, null, contentValues);
        }
    }

    public RadioStation getStation(String station) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+StationTable.TABLE_NAME+" WHERE "+StationTable.COLUMN_NAME +"="+station+";",null);

        if(c.moveToFirst())
            return radioStationFromCursor(c);
        else
            return null;
    }

    private RadioStation radioStationFromCursor(Cursor c) {
        return new RadioStation(
                c.getInt(c.getColumnIndex(StationTable.COLUMN_ID)),
                c.getString(c.getColumnIndex(StationTable.COLUMN_NAME)),
                c.getString(c.getColumnIndex(StationTable.COLUMN_LINK)));
    }


    /****************************
     | ALARM TABLE AND HANDLERS |
     ****************************/
    public final class AlarmTable {
        public static final String TABLE_NAME = "alarms";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_ACTIVE = "_active";
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
        public static final String COLUMN_VOLUME = "_volume";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ACTIVE + " INTEGER DEFAULT 1, " +
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
                COLUMN_VOLUME + " INTEGER NOT NULL " +
//                "FOREIGN KEY("+COLUMN_STATION+") REFERENCES "+StationTable.TABLE_NAME+"("+StationTable.COLUMN_ID+")" +
                ");";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

    public void addAlarm(Alarm alarm){
        getWritableDatabase().insert(AlarmTable.TABLE_NAME, null, getAlarmValues(alarm));
    }

    public void updateAlarm(Alarm alarm){
        getWritableDatabase().update(AlarmTable.TABLE_NAME, getAlarmValues(alarm), AlarmTable.COLUMN_ID+"="+alarm.get_id(), null);
    }

    public void deleteAlarm(int id){
        getWritableDatabase().execSQL("DELETE FROM " + AlarmTable.TABLE_NAME + " WHERE " + AlarmTable.COLUMN_ID + " = " + id + ";");
    }

    public void deleteAlarm(Alarm alarm) {
        deleteAlarm(alarm.get_id());
    }

    public Alarm getAlarm(int id){
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + AlarmTable.TABLE_NAME + " WHERE " + AlarmTable.COLUMN_ID + " = " + id + ";", null);
        if(c.moveToFirst()){
            return alarmFromCursor(c);
        }
        return null;
    }

//    public List<Alarm> getAllAlarms(){
//        ArrayList<Alarm> alarms = new ArrayList<>();
//        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + AlarmTable.TABLE_NAME + ";", null);
//        c.moveToFirst();
//        while (!c.isAfterLast()){
//            alarms.add(alarmFromCursor(c));
//            c.moveToNext();
//        }
//        return alarms;
//    }

    public Cursor getAlarmsCursor() {
        ArrayList<Alarm> alarms = new ArrayList<>();
        return getReadableDatabase().rawQuery("SELECT * FROM " + AlarmTable.TABLE_NAME + ";", null);
    }

    private ContentValues getAlarmValues(Alarm alarm){
        ContentValues values = new ContentValues();
        values.put(AlarmTable.COLUMN_HOUR, alarm.get_hour());
        values.put(AlarmTable.COLUMN_ACTIVE, alarm.is_active() ? 1 : 0);
        values.put(AlarmTable.COLUMN_MINUTE, alarm.get_minute());
        values.put(AlarmTable.COLUMN_MON, alarm.get_mon() ? 1 : 0);
        values.put(AlarmTable.COLUMN_TUE, alarm.get_tue() ? 1 : 0);
        values.put(AlarmTable.COLUMN_WED, alarm.get_wed() ? 1 : 0);
        values.put(AlarmTable.COLUMN_THU, alarm.get_thu() ? 1 : 0);
        values.put(AlarmTable.COLUMN_FRI, alarm.get_fri() ? 1 : 0);
        values.put(AlarmTable.COLUMN_SAT, alarm.get_sat() ? 1 : 0);
        values.put(AlarmTable.COLUMN_SUN, alarm.get_sun() ? 1 : 0);
        values.put(AlarmTable.COLUMN_STATION, alarm.get_station());
        values.put(AlarmTable.COLUMN_VOLUME, alarm.get_volume());
        return values;
    }

    private Alarm alarmFromCursor(Cursor c) {
        return new Alarm(
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_ID)),
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_ACTIVE)) == 1,
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_HOUR)),
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_MINUTE)),
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_MON)) == 1,
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_TUE)) == 1,
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_WED)) == 1,
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_THU)) == 1,
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_FRI)) == 1,
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_SAT)) == 1,
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_SUN)) == 1,
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_STATION)),
                c.getInt(c.getColumnIndex(AlarmTable.COLUMN_VOLUME))
        );
    }
}
