package alf.stream.clockradio;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Alf on 7/7/2017.
 */
class DatabaseManager {

    static class DatabaseHelper extends SQLiteOpenHelper {
        private static DatabaseHelper instance = null;
        private static SQLiteDatabase db = null;
        private static final String TAG = "DatabaseHelper";
//        private Context context;
        private static final int DATABASE_VERSION = 3;
        private static final String DATABASE_NAME = "alarms.db";
        private static BroadcastReceiver broadcastReceiver;

        /*********************
         * Database Instance *
         *********************/

        static DatabaseHelper getInstance(Context context) {
            if (instance == null) {
                instance = new DatabaseHelper(context.getApplicationContext());
            }
            if(broadcastReceiver == null) {
                broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int flag = intent.getIntExtra(context.getString(R.string.alarm_changed_flag),-1);
                        if(flag == -1) Log.e(TAG,"Alarm changed broadcast without a flag!!");
                        else Log.d(TAG,"Alarm changed broadcast with flag "+flag);

                        int alarmId = intent.getIntExtra(context.getString(R.string.alarm_id_int),-1);
                        boolean changed = false;
                        switch(flag) {
                            case Alarm.FLAG_CREATE:
                                alarmId = addAlarmFromBroadcast(context,intent);
                                changed = true;
                                break;

                            case Alarm.FLAG_ACTIVE_CHANGE:
                                boolean active = intent.getBooleanExtra(context.getString(R.string.alarm_active_boolean), true);
                                updateAlarmActive(alarmId, active);
                                changed = true;
                                break;

                            case Alarm.FLAG_UPDATE:
                                updateAlarmFromBroadcast(context, intent, alarmId);
                                changed = true;
                                break;

                            case Alarm.FLAG_DELETE:
                                deleteAlarm(alarmId);
                                changed = true;
                                break;
                        }
                        if(changed) {
                            boolean showToast = intent.getBooleanExtra(context.getString(R.string.show_toast_boolean), false);
                            LocalBroadcastManager.getInstance(context)
                                    .sendBroadcast(new Intent(context.getString(R.string.database_changed_filter))
                                            .putExtra(context.getString(R.string.alarm_id_int), alarmId)
                                            .putExtra(context.getString(R.string.alarm_changed_flag), flag)
                                            .putExtra(context.getString(R.string.show_toast_boolean),showToast));
                        }
                    }
                };
                LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter(context.getString(R.string.alarm_changed_filter)));
            }
            return instance;
        }

        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
//            this.context = context;
        }

        void openDatabase(){
            if(db == null || !db.isOpen()){
                db = getWritableDatabase();
                Log.d(TAG, "Opened");
            }
        }

        void closeDatabase(){
            if(db != null && db.isOpen()){
                db.close();
                Log.d(TAG, "Closed");
            }
        }

        /********************
         * SQLite Overrides *
         ********************/

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            db.setForeignKeyConstraintsEnabled(true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "Created Database " + DATABASE_NAME + " version " + DATABASE_VERSION);
            db.execSQL(StationTable.SQL_CREATE_TABLE);
            db.execSQL(AlarmTable.SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            Log.i(TAG, "Upgrade database from version " + i + " to " + i1);
            db.execSQL(AlarmTable.SQL_DROP_TABLE);
            db.execSQL(StationTable.SQL_DROP_TABLE);
            onConfigure(db);
            onCreate(db);
        }


        /******************************
         * STATION TABLE AND HANDLERS *
         ******************************/
        final static class StationTable {
            static final String TABLE_NAME = "stations";

            static final String COLUMN_ID = "_id";
            static final String COLUMN_NAME = "_name";
            static final String COLUMN_LINK = "_link";

            static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_LINK + " TEXT NOT NULL " +
                    ");";

            static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        }

        void populateStationTable(InputStream inputStreamCSV) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStreamCSV, Charset.forName("UTF-8"))
            );

            String row = "";
            ContentValues contentValues = new ContentValues();

            try {
                while((row = br.readLine()) != null){
                    String[] values = row.split(",");
                    contentValues.put(StationTable.COLUMN_NAME, values[0]);
                    contentValues.put(StationTable.COLUMN_LINK, values[1]);
                    db.insert(StationTable.TABLE_NAME, null, contentValues);
                }
            } catch (IOException e) {
                Log.wtf(TAG,"Error reading station on line "+row, e);
                e.printStackTrace();
            }
        }

        List<RadioStation> getRadioStations() {
            Cursor c = db.rawQuery("SELECT * FROM "+StationTable.TABLE_NAME+";", null);
            c.moveToFirst();
            List<RadioStation> rs = new ArrayList<>();
            while (!c.isAfterLast()){
                rs.add(new RadioStation(
                        c.getInt(c.getColumnIndex(StationTable.COLUMN_ID)),
                        c.getString(c.getColumnIndex(StationTable.COLUMN_NAME)),
                        c.getString(c.getColumnIndex(StationTable.COLUMN_LINK))
                ));
                c.moveToNext();
            }
            c.close();
            return rs;
        }

        String getStationLink(int id) {
            Cursor c = db.rawQuery("SELECT "+StationTable.COLUMN_LINK+" FROM "+StationTable.TABLE_NAME+
                    " WHERE "+StationTable.COLUMN_ID+"="+id, null);
            String name = null;
            if(c.moveToFirst()) {
                name = c.getString(c.getColumnIndex(StationTable.COLUMN_LINK));
            }
            c.close();
            return name;
        }


        /****************************
         | ALARM TABLE AND HANDLERS |
         ****************************/
        final static class AlarmTable {
            static final String TABLE_NAME = "alarms";

            static final String COLUMN_ID = "_id";
            static final String COLUMN_ACTIVE = "_active";
            static final String COLUMN_HOUR = "_hour";
            static final String COLUMN_MINUTE = "_minute";
            static final String COLUMN_MON = "_mon";
            static final String COLUMN_TUE = "_tue";
            static final String COLUMN_WED = "_wed";
            static final String COLUMN_THU = "_thu";
            static final String COLUMN_FRI = "_fri";
            static final String COLUMN_SAT = "_sat";
            static final String COLUMN_SUN = "_sun";
            static final String COLUMN_STATION = "_station";
            static final String COLUMN_VOLUME = "_volume";

            static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
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
                    COLUMN_VOLUME + " INTEGER NOT NULL, " +
                    "FOREIGN KEY("+COLUMN_STATION+") REFERENCES "+StationTable.TABLE_NAME+"("+StationTable.COLUMN_ID+")" +
                    ");";

            static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        }

        private static int addAlarmFromBroadcast(Context context, Intent intent){
            ContentValues cv = contentValuesFromBroadcastIntent(context, intent);
            return (int) db.insert(AlarmTable.TABLE_NAME, null, cv);
        }

        private static void updateAlarmFromBroadcast(Context context, Intent intent, int id){
            ContentValues cv = contentValuesFromBroadcastIntent(context, intent);
            db.update(AlarmTable.TABLE_NAME, cv, AlarmTable.COLUMN_ID+"="+id, null);
        }

        private static ContentValues contentValuesFromBroadcastIntent(Context context, Intent intent){
            boolean active = intent.getBooleanExtra(context.getString(R.string.alarm_id_int),true);
            int[] time = intent.getIntArrayExtra(context.getString(R.string.alarm_time_int_array));
            boolean[] days = intent.getBooleanArrayExtra(context.getString(R.string.alarm_days_boolean_array));
            int station = intent.getIntExtra(context.getString(R.string.station_id_int),0);
            int volume = intent.getIntExtra(context.getString(R.string.alarm_volume_int),0);
            ContentValues cv = new ContentValues();
            cv.put(AlarmTable.COLUMN_ACTIVE, active ? 1:0);
            cv.put(AlarmTable.COLUMN_HOUR, time[0]);
            cv.put(AlarmTable.COLUMN_MINUTE, time[1]);
            cv.put(AlarmTable.COLUMN_MON, days[0] ? 1:0);
            cv.put(AlarmTable.COLUMN_TUE, days[1] ? 1:0);
            cv.put(AlarmTable.COLUMN_WED, days[2] ? 1:0);
            cv.put(AlarmTable.COLUMN_THU, days[3] ? 1:0);
            cv.put(AlarmTable.COLUMN_FRI, days[4] ? 1:0);
            cv.put(AlarmTable.COLUMN_SAT, days[5] ? 1:0);
            cv.put(AlarmTable.COLUMN_SUN, days[6] ? 1:0);
            cv.put(AlarmTable.COLUMN_STATION, station);
            cv.put(AlarmTable.COLUMN_VOLUME, volume);
            return cv;
        }

        private static void updateAlarmActive(int id, boolean active){
            ContentValues cv = new ContentValues();
            cv.put(AlarmTable.COLUMN_ACTIVE, active?1:0);
            db.update(AlarmTable.TABLE_NAME, cv, AlarmTable.COLUMN_ID+"="+id, null);
        }
//
//        private static void updateAlarm(Alarm alarm) {
//            db.update(AlarmTable.TABLE_NAME, getAlarmValues(alarm), AlarmTable.COLUMN_ID + "=" + alarm.get_id(), null);
//            Log.d(TAG, "Alarm " + alarm.get_id() + " updated in database");
//        }

        private static void deleteAlarm(int id) {
            db.execSQL("DELETE FROM " + AlarmTable.TABLE_NAME + " WHERE " + AlarmTable.COLUMN_ID + " = " + id + ";");
            Log.d(TAG, "Alarm " + id + " deleted from database");
        }

        Alarm getAlarm(int id) {
            Cursor c = db.rawQuery("SELECT * FROM " + AlarmTable.TABLE_NAME + " WHERE " + AlarmTable.COLUMN_ID + " = " + id + ";", null);
            Alarm alarm = null;
            if (c.moveToFirst()) {
                alarm = alarmFromCursor(c);
            }
            c.close();
            return alarm;
        }

        SparseArray<Alarm> getAllAlarms() {
            SparseArray<Alarm> alarms = new SparseArray<>();
            Cursor c = db.rawQuery("SELECT * FROM " + AlarmTable.TABLE_NAME + ";", null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                alarms.append(c.getInt(c.getColumnIndex(AlarmTable.COLUMN_ID)), alarmFromCursor(c));
                c.moveToNext();
            }
            c.close();
            return alarms;
        }

        Cursor getAlarmsCursor() {
            return db.rawQuery("SELECT "
                    +AlarmTable.TABLE_NAME+"."+AlarmTable.COLUMN_ID
                    +", "+AlarmTable.COLUMN_ACTIVE
                    +", "+AlarmTable.COLUMN_HOUR
                    +", "+AlarmTable.COLUMN_MINUTE
                    +", "+AlarmTable.COLUMN_MON
                    +", "+AlarmTable.COLUMN_TUE
                    +", "+AlarmTable.COLUMN_WED
                    +", "+AlarmTable.COLUMN_THU
                    +", "+AlarmTable.COLUMN_FRI
                    +", "+AlarmTable.COLUMN_SAT
                    +", "+AlarmTable.COLUMN_SUN
                    +", "+StationTable.COLUMN_NAME
                    +" FROM "+AlarmTable.TABLE_NAME+" JOIN "+StationTable.TABLE_NAME
                    +" ON "+ AlarmTable.TABLE_NAME+"."+AlarmTable.COLUMN_STATION+" = "+StationTable.TABLE_NAME+"."+StationTable.COLUMN_ID+";"
                    , null);
//            return rawQuery("SELECT alarms._id, _active, _hour, _minute, _mon, _tue, _wed, _thu, _fri, _sat, _sun, _name FROM alarms JOIN stations ON alarms._station = stations._id;");
//            return rawQuery("SELECT * FROM " + AlarmTable.TABLE_NAME + ";");
        }

        private static ContentValues getAlarmValues(Alarm alarm) {
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

        private static Alarm alarmFromCursor(Cursor c) {
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
}
