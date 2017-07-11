package alf.stream.clockradio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
// TODO: getWritable and getReadable in seperate thread...?? Depends on speed - It's a very small database.
public class DatabaseManager {

    public static class DatabaseHelper extends SQLiteOpenHelper {
        private static DatabaseHelper instance = null;
        private static SQLiteDatabase db = null;
        private static final String TAG = "DatabaseHelper";
//        private Context context;
        private static final int DATABASE_VERSION = 3;
        private static final String DATABASE_NAME = "alarms.db";

        /*********************
         * Database Instance *
         *********************/

        public static DatabaseHelper getInstance(Context context) {
            if (instance == null) {
                instance = new DatabaseHelper(context.getApplicationContext());
            }
            return instance;
        }

        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
//            this.context = context;
        }

        public void open(){
            if(db == null || !db.isOpen()){
                db = getWritableDatabase();
                Log.d(TAG, "Opened");
            }
        }

        public void close(){
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

        /***********************
         * Convenience methods *
         ***********************/

        public void updateTableField(String tableName, int id, String column, boolean value) {
            ContentValues cv = new ContentValues();
            cv.put(column, value);
            update(tableName,cv,"_id=" + id);
        }

        public void updateTableField(String tableName, int id, String column, String value) {
            ContentValues cv = new ContentValues();
            cv.put(column, value);
            update(tableName,cv,"_id=" + id);
        }

        public void updateTableField(String tableName, int id, String column, int value) {
            ContentValues cv = new ContentValues();
            cv.put(column, value);
            update(tableName,cv,"_id=" + id);
        }

        private long insert(String table, ContentValues cv){
            boolean singleQuery = (db == null || !db.isOpen());
            if(singleQuery) open();
            long ret = db.insert(table, null, cv);
            if(singleQuery) close();
            return ret;
        }

        private int update(String table, ContentValues cv, String whereClause){
            boolean singleQuery = (db == null || !db.isOpen());
            if(singleQuery) open();
            int ret = db.update(table, cv, whereClause, null);
            if(singleQuery) close();
            return ret;
        }

        private void execSQL(String sql){
            boolean singleQuery = (db == null || !db.isOpen());
            if(singleQuery) open();
            db.execSQL(sql);
            if(singleQuery) close();
        }

        private Cursor rawQuery(String sql){
            boolean singleQuery = (db == null || !db.isOpen());
            if(singleQuery) open();
            Cursor c = db.rawQuery(sql, null);
            if(singleQuery) close();
            return c;
        }

        /******************************
         * STATION TABLE AND HANDLERS *
         ******************************/
        public final class StationTable {
            public static final String TABLE_NAME = "stations";

            public static final String COLUMN_ID = "_id";
            public static final String COLUMN_NAME = "_name";
            public static final String COLUMN_LINK = "_link";

            public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_LINK + " TEXT NOT NULL " +
                    ");";

            public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        }

        public void populateStationTable(InputStream inputStreamCSV) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStreamCSV, Charset.forName("UTF-8"))
            );

            String row = "";
            ContentValues contentValues = new ContentValues();
            boolean singleQuery = (db == null || !db.isOpen());
            if(singleQuery) open();
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
            if(singleQuery) close();
        }

        // TODO:
        public List<RadioStation> getRadioStations() {
            Cursor c = rawQuery("SELECT * FROM "+StationTable.TABLE_NAME+";");
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
            return rs;
        }
        public RadioStation getStation(String station) {
            Cursor c = rawQuery("SELECT * FROM " + StationTable.TABLE_NAME + " WHERE " + StationTable.COLUMN_NAME + "=" + station + ";");
            if (c.moveToFirst())
                return radioStationFromCursor(c);
            else
                return null;
        }

        public String getStationLink(int id) {
            Cursor c = rawQuery("SELECT "+StationTable.COLUMN_LINK+" FROM "+StationTable.TABLE_NAME+
                    " WHERE "+StationTable.COLUMN_ID+"="+id);
            c.moveToFirst();
            if(c.isAfterLast()) return null;
            else return c.getString(c.getColumnIndex(StationTable.COLUMN_LINK));
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
                    COLUMN_VOLUME + " INTEGER NOT NULL, " +
                    "FOREIGN KEY("+COLUMN_STATION+") REFERENCES "+StationTable.TABLE_NAME+"("+StationTable.COLUMN_ID+")" +
                    ");";

            public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        }

        public long addAlarm(Alarm alarm) {
            Log.d(TAG, "Alarm " + alarm.get_id() + " added to database");
            return insert(AlarmTable.TABLE_NAME, getAlarmValues(alarm));
        }

        public void updateAlarm(Alarm alarm) {
            update(AlarmTable.TABLE_NAME, getAlarmValues(alarm), AlarmTable.COLUMN_ID + "=" + alarm.get_id());
            Log.d(TAG, "Alarm " + alarm.get_id() + " updated in database");
        }


        public void deleteAlarm(int id) {
            execSQL("DELETE FROM " + AlarmTable.TABLE_NAME + " WHERE " + AlarmTable.COLUMN_ID + " = " + id + ";");
            Log.d(TAG, "Alarm " + id + " deleted from database");
        }

        public void deleteAlarm(Alarm alarm) {
            deleteAlarm(alarm.get_id());
        }

        public Alarm getAlarm(int id) {
            Cursor c = rawQuery("SELECT * FROM " + AlarmTable.TABLE_NAME + " WHERE " + AlarmTable.COLUMN_ID + " = " + id + ";");
            if (c.moveToFirst()) {
                return alarmFromCursor(c);
            }
            return null;
        }

        public SparseArray<Alarm> getAllAlarms() {
            SparseArray<Alarm> alarms = new SparseArray<>();
            Cursor c = rawQuery("SELECT * FROM " + AlarmTable.TABLE_NAME + ";");
            c.moveToFirst();
            while (!c.isAfterLast()) {
                alarms.append(c.getInt(c.getColumnIndex(AlarmTable.COLUMN_ID)), alarmFromCursor(c));
                c.moveToNext();
            }
            return alarms;
        }

        public Cursor getAlarmsCursor() {
            return rawQuery("SELECT alarms._id, _active, _hour, _minute, _mon, _tue, _wed, _thu, _fri, _sat, _sun, _name FROM alarms JOIN stations ON alarms._station = stations._id;");
//            return rawQuery("SELECT * FROM " + AlarmTable.TABLE_NAME + ";");
        }

        private ContentValues getAlarmValues(Alarm alarm) {
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
}
