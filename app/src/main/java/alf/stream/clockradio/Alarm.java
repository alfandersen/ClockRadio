package alf.stream.clockradio;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Alf on 7/7/2017.
 */

public class Alarm {
    private int _id;
    private boolean _active;
    private int _hour;
    private int _minute;
    private boolean _mon, _tue, _wed, _thu, _fri, _sat, _sun;
    private int _station;
    private int _volume;


    // Constructor

    public Alarm(int _id, boolean _active, int _hour, int _minute, boolean _mon, boolean _tue, boolean _wed, boolean _thu, boolean _fri, boolean _sat, boolean _sun, int _station, int _volume) {
        this._id = _id;
        this._active = _active;
        this._hour = _hour;
        this._minute = _minute;
        this._mon = _mon;
        this._tue = _tue;
        this._wed = _wed;
        this._thu = _thu;
        this._fri = _fri;
        this._sat = _sat;
        this._sun = _sun;
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
        return _mon;
    }

    public boolean get_tue() {
        return _tue;
    }

    public boolean get_wed() {
        return _wed;
    }

    public boolean get_thu() {
        return _thu;
    }

    public boolean get_fri() {
        return _fri;
    }

    public boolean get_sat() {
        return _sat;
    }

    public boolean get_sun() {
        return _sun;
    }

    public int get_station() {
        return _station;
    }

    public int get_volume() {
        return _volume;
    }

    // If no days are checked, treat it as a one time event.
    public void resetAlarm(Context context) {
        if(_mon || _tue || _wed || _thu || _fri || _sat ||_sun){
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

        // make sure that alarmTime is in the future
        while(alarmTime.compareTo(now) <= 0){
            alarmTime.add(Calendar.DAY_OF_YEAR,1);
        }

        // TODO: This should not be so many lines of code
        switch(now.get(Calendar.DAY_OF_WEEK)){
            case Calendar.MONDAY:
                if(_mon) ;
                else if(_tue) alarmTime.add(Calendar.DAY_OF_YEAR,1);
                else if(_wed) alarmTime.add(Calendar.DAY_OF_YEAR,2);
                else if(_thu) alarmTime.add(Calendar.DAY_OF_YEAR,3);
                else if(_fri) alarmTime.add(Calendar.DAY_OF_YEAR,4);
                else if(_sat) alarmTime.add(Calendar.DAY_OF_YEAR,5);
                else if(_sun) alarmTime.add(Calendar.DAY_OF_YEAR,6);
                break;

            case Calendar.TUESDAY:
                if(_tue) ;
                else if(_wed) alarmTime.add(Calendar.DAY_OF_YEAR,1);
                else if(_thu) alarmTime.add(Calendar.DAY_OF_YEAR,2);
                else if(_fri) alarmTime.add(Calendar.DAY_OF_YEAR,3);
                else if(_sat) alarmTime.add(Calendar.DAY_OF_YEAR,4);
                else if(_sun) alarmTime.add(Calendar.DAY_OF_YEAR,5);
                else if(_mon) alarmTime.add(Calendar.DAY_OF_YEAR,6);
                break;

            case Calendar.WEDNESDAY:
                if(_wed) ;
                else if(_thu) alarmTime.add(Calendar.DAY_OF_YEAR,1);
                else if(_fri) alarmTime.add(Calendar.DAY_OF_YEAR,2);
                else if(_sat) alarmTime.add(Calendar.DAY_OF_YEAR,3);
                else if(_sun) alarmTime.add(Calendar.DAY_OF_YEAR,4);
                else if(_mon) alarmTime.add(Calendar.DAY_OF_YEAR,5);
                else if(_tue) alarmTime.add(Calendar.DAY_OF_YEAR,6);
                break;

            case Calendar.THURSDAY:
                if(_thu) ;
                else if(_fri) alarmTime.add(Calendar.DAY_OF_YEAR,1);
                else if(_sat) alarmTime.add(Calendar.DAY_OF_YEAR,2);
                else if(_sun) alarmTime.add(Calendar.DAY_OF_YEAR,3);
                else if(_mon) alarmTime.add(Calendar.DAY_OF_YEAR,4);
                else if(_tue) alarmTime.add(Calendar.DAY_OF_YEAR,5);
                else if(_wed) alarmTime.add(Calendar.DAY_OF_YEAR,6);
                break;

            case Calendar.FRIDAY:
                if(_fri) ;
                else if(_sat) alarmTime.add(Calendar.DAY_OF_YEAR,1);
                else if(_sun) alarmTime.add(Calendar.DAY_OF_YEAR,2);
                else if(_mon) alarmTime.add(Calendar.DAY_OF_YEAR,3);
                else if(_tue) alarmTime.add(Calendar.DAY_OF_YEAR,4);
                else if(_wed) alarmTime.add(Calendar.DAY_OF_YEAR,5);
                else if(_thu) alarmTime.add(Calendar.DAY_OF_YEAR,6);
                break;


            case Calendar.SATURDAY:
                if(_sat) ;
                else if(_sun) alarmTime.add(Calendar.DAY_OF_YEAR,1);
                else if(_mon) alarmTime.add(Calendar.DAY_OF_YEAR,2);
                else if(_tue) alarmTime.add(Calendar.DAY_OF_YEAR,3);
                else if(_wed) alarmTime.add(Calendar.DAY_OF_YEAR,4);
                else if(_thu) alarmTime.add(Calendar.DAY_OF_YEAR,5);
                else if(_fri) alarmTime.add(Calendar.DAY_OF_YEAR,6);
                break;


            case Calendar.SUNDAY:
                if(_sun) ;
                else if(_mon) alarmTime.add(Calendar.DAY_OF_YEAR,1);
                else if(_tue) alarmTime.add(Calendar.DAY_OF_YEAR,2);
                else if(_wed) alarmTime.add(Calendar.DAY_OF_YEAR,3);
                else if(_thu) alarmTime.add(Calendar.DAY_OF_YEAR,4);
                else if(_fri) alarmTime.add(Calendar.DAY_OF_YEAR,5);
                else if(_sat) alarmTime.add(Calendar.DAY_OF_YEAR,6);
                break;
        }

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra(context.getString(R.string.alarm_id_int),_id);
        AlarmManager alarmManager =  (AlarmManager) context.getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,_id,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP,alarmTime.getTimeInMillis(),pendingIntent);
    }

    public void cancelAlarm(Context context){
        _active = false;
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,_id,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager =  (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
