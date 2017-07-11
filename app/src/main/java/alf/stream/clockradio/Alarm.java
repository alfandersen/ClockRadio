package alf.stream.clockradio;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseBooleanArray;

import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Alf on 7/7/2017.
 */

public class Alarm {
    private static final String TAG = "Alarm";
    private int _id;
    private boolean _active;
    private int _hour;
    private int _minute;
    private SparseBooleanArray activeDays;
    private int _station;
    private int _volume;


    // Constructor

    public Alarm(int _id, boolean _active, int _hour, int _minute, boolean _mon, boolean _tue, boolean _wed, boolean _thu, boolean _fri, boolean _sat, boolean _sun, int _station, int _volume) {
        this._id = _id;
        this._active = _active;
        this._hour = _hour;
        this._minute = _minute;
        activeDays = new SparseBooleanArray();
        activeDays.append(Calendar.MONDAY,_mon);
        activeDays.append(Calendar.TUESDAY,_tue);
        activeDays.append(Calendar.WEDNESDAY,_wed);
        activeDays.append(Calendar.THURSDAY,_thu);
        activeDays.append(Calendar.FRIDAY,_fri);
        activeDays.append(Calendar.SATURDAY,_sat);
        activeDays.append(Calendar.SUNDAY,_sun);
        this._station = _station;
        this._volume = _volume;
    }


    // Getters

    public int get_id() {
        return _id;
    }

    public boolean is_active() {
        return _active;
    }

    public int get_hour() {
        return _hour;
    }

    public int get_minute() {
        return _minute;
    }

    public boolean get_mon() {
        return activeDays.get(Calendar.MONDAY);
    }

    public boolean get_tue() {
        return activeDays.get(Calendar.TUESDAY);
    }

    public boolean get_wed() {
        return activeDays.get(Calendar.WEDNESDAY);
    }

    public boolean get_thu() {
        return activeDays.get(Calendar.THURSDAY);
    }

    public boolean get_fri() {
        return activeDays.get(Calendar.FRIDAY);
    }

    public boolean get_sat() {
        return activeDays.get(Calendar.SATURDAY);
    }

    public boolean get_sun() {
        return activeDays.get(Calendar.SUNDAY);
    }

    public int get_station() {
        return _station;
    }

    public int get_volume() {
        return _volume;
    }

    // Setters

    public void set_id(int _id) {
        this._id = _id;
    }

//    @Override
//    public String toString() {
//        return "Alarm{" +
//                "_id=" + _id +
//                ", _active=" + _active +
//                ", _hour=" + _hour +
//                ", _minute=" + _minute +
//                ", _mon=" + get_mon() +
//                ", _tue=" + get_tue() +
//                ", _wed=" + get_wed() +
//                ", _thu=" + get_thu() +
//                ", _fri=" + get_fri() +
//                ", _sat=" + get_sat() +
//                ", _sun=" + get_sun() +
//                ", _station=" + _station +
//                ", _volume=" + _volume +
//                '}';
//    }


    // Alarm Handling

    // If no days are checked, treat it as a one time event.
    public void resetAlarm(Context context) {
        if(get_mon() || get_tue() || get_wed() || get_thu() || get_fri() || get_sat() ||get_sun()){
            setAlarm(context);
        }
        else{
            _active = false;
        }
    }

    public void setAlarm(Context context) {
        _active = true;
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, _hour);
        alarmTime.set(Calendar.MINUTE, _minute);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);

        Calendar now = Calendar.getInstance();

        int activeInDays = activeInDays(now, 0);
        int activeInDaysAfterToday = activeInDays(now, 1);

        if(alarmTime.before(now))
            alarmTime.add(Calendar.DAY_OF_YEAR,Math.abs(activeInDaysAfterToday));
        else if(activeInDays > 0 && alarmTime.after(now))
            alarmTime.add(Calendar.DAY_OF_YEAR,activeInDays);

//        if(activeInDays == 0 && now.after(alarmTime))
//            activeInDays = activeInDays(now,1);
//
//        if(activeInDays == -1 && now.after(alarmTime)){
//            Log.d(TAG,"A: activeInDays="+activeInDays);
//            alarmTime.add(Calendar.DAY_OF_YEAR,1);
//        }
//        else if(!((activeInDays == 0 || activeInDays == -1) && now.before(alarmTime))){
//            Log.d(TAG,"B: activeInDays="+activeInDays);
//            activeInDays = activeInDays(now, 1);
//            Log.d(TAG,"C: activeInDays="+activeInDays);
//            alarmTime.add(Calendar.DAY_OF_YEAR, activeInDays);
//        }

//        // make sure that alarmTime is in the future
//        while(alarmTime.compareTo(now) <= 0){
//            alarmTime.add(Calendar.DAY_OF_YEAR,1);
//        }
//
//        // TODO: This should not be so many lines of code
//        switch(now.get(Calendar.DAY_OF_WEEK)){
//            case Calendar.MONDAY:
//                if(_mon) ;
//                else if(_tue) alarmTime.add(Calendar.DAY_OF_YEAR,1);
//                else if(_wed) alarmTime.add(Calendar.DAY_OF_YEAR,2);
//                else if(_thu) alarmTime.add(Calendar.DAY_OF_YEAR,3);
//                else if(_fri) alarmTime.add(Calendar.DAY_OF_YEAR,4);
//                else if(_sat) alarmTime.add(Calendar.DAY_OF_YEAR,5);
//                else if(_sun) alarmTime.add(Calendar.DAY_OF_YEAR,6);
//                break;
//
//            case Calendar.TUESDAY:
//                if(_tue) ;
//                else if(_wed) alarmTime.add(Calendar.DAY_OF_YEAR,1);
//                else if(_thu) alarmTime.add(Calendar.DAY_OF_YEAR,2);
//                else if(_fri) alarmTime.add(Calendar.DAY_OF_YEAR,3);
//                else if(_sat) alarmTime.add(Calendar.DAY_OF_YEAR,4);
//                else if(_sun) alarmTime.add(Calendar.DAY_OF_YEAR,5);
//                else if(_mon) alarmTime.add(Calendar.DAY_OF_YEAR,6);
//                break;
//
//            case Calendar.WEDNESDAY:
//                if(_wed) ;
//                else if(_thu) alarmTime.add(Calendar.DAY_OF_YEAR,1);
//                else if(_fri) alarmTime.add(Calendar.DAY_OF_YEAR,2);
//                else if(_sat) alarmTime.add(Calendar.DAY_OF_YEAR,3);
//                else if(_sun) alarmTime.add(Calendar.DAY_OF_YEAR,4);
//                else if(_mon) alarmTime.add(Calendar.DAY_OF_YEAR,5);
//                else if(_tue) alarmTime.add(Calendar.DAY_OF_YEAR,6);
//                break;
//
//            case Calendar.THURSDAY:
//                if(_thu) ;
//                else if(_fri) alarmTime.add(Calendar.DAY_OF_YEAR,1);
//                else if(_sat) alarmTime.add(Calendar.DAY_OF_YEAR,2);
//                else if(_sun) alarmTime.add(Calendar.DAY_OF_YEAR,3);
//                else if(_mon) alarmTime.add(Calendar.DAY_OF_YEAR,4);
//                else if(_tue) alarmTime.add(Calendar.DAY_OF_YEAR,5);
//                else if(_wed) alarmTime.add(Calendar.DAY_OF_YEAR,6);
//                break;
//
//            case Calendar.FRIDAY:
//                if(_fri) ;
//                else if(_sat) alarmTime.add(Calendar.DAY_OF_YEAR,1);
//                else if(_sun) alarmTime.add(Calendar.DAY_OF_YEAR,2);
//                else if(_mon) alarmTime.add(Calendar.DAY_OF_YEAR,3);
//                else if(_tue) alarmTime.add(Calendar.DAY_OF_YEAR,4);
//                else if(_wed) alarmTime.add(Calendar.DAY_OF_YEAR,5);
//                else if(_thu) alarmTime.add(Calendar.DAY_OF_YEAR,6);
//                break;
//
//
//            case Calendar.SATURDAY:
//                if(_sat) ;
//                else if(_sun) alarmTime.add(Calendar.DAY_OF_YEAR,1);
//                else if(_mon) alarmTime.add(Calendar.DAY_OF_YEAR,2);
//                else if(_tue) alarmTime.add(Calendar.DAY_OF_YEAR,3);
//                else if(_wed) alarmTime.add(Calendar.DAY_OF_YEAR,4);
//                else if(_thu) alarmTime.add(Calendar.DAY_OF_YEAR,5);
//                else if(_fri) alarmTime.add(Calendar.DAY_OF_YEAR,6);
//                break;
//
//
//            case Calendar.SUNDAY:
//                if(_sun) ;
//                else if(_mon) alarmTime.add(Calendar.DAY_OF_YEAR,1);
//                else if(_tue) alarmTime.add(Calendar.DAY_OF_YEAR,2);
//                else if(_wed) alarmTime.add(Calendar.DAY_OF_YEAR,3);
//                else if(_thu) alarmTime.add(Calendar.DAY_OF_YEAR,4);
//                else if(_fri) alarmTime.add(Calendar.DAY_OF_YEAR,5);
//                else if(_sat) alarmTime.add(Calendar.DAY_OF_YEAR,6);
//                break;
//        }
//

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra(context.getString(R.string.alarm_id_int),_id);
        AlarmManager alarmManager =  (AlarmManager) context.getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,_id,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP,alarmTime.getTimeInMillis(),pendingIntent);

        LocalBroadcastManager.getInstance(context).sendBroadcast(
                new Intent(context.getString(R.string.alarm_set_filter))
                        .putExtra(context.getString(R.string.station_id_int),_station)
                        .putExtra(context.getString(R.string.alarm_active_boolean),true)
                        .putExtra(context.getString(R.string.alarm_time_string),String.format(Locale.getDefault(),"%1$tA %1$tb %1$td %1$tY at %1$tH:%1$tM", alarmTime))
        );

        Log.i(TAG,"Set alarm "+_id+" to play " + String.format(Locale.getDefault(),"%1$tA %1$tb %1$td %1$tY at %1$tH:%1$tM", alarmTime));
    }

    public int activeInDays(Calendar now, int after){
        Calendar nextDays = Calendar.getInstance();
        nextDays.add(Calendar.DAY_OF_WEEK,after);
        for(int i = after; i <= 7; i++){
            int day = nextDays.get(Calendar.DAY_OF_WEEK);
            if(activeDays.get(day))
                return i;
            nextDays.add(Calendar.DAY_OF_WEEK,1);
        }
        return -1;
    }

    private String calendarString(Calendar c){
        return String.format(Locale.getDefault(),"%02d:%02d - %d / %d",
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.DAY_OF_YEAR),
                c.get(Calendar.YEAR));
    }

    public void cancelAlarm(Context context){
        _active = false;
        Log.i(TAG,"Canceled alarm " + _id + " with address "+this);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,_id,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager =  (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        LocalBroadcastManager.getInstance(context).sendBroadcast(
                new Intent(context.getString(R.string.alarm_set_filter))
                        .putExtra(context.getString(R.string.alarm_id_int),_id)
                        .putExtra(context.getString(R.string.alarm_active_boolean),false)
        );
    }
}
