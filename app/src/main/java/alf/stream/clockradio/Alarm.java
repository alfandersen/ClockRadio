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

    public static final int FLAG_CREATE = 1;
    public static final int FLAG_ACTIVE_CHANGE = 2;
    public static final int FLAG_UPDATE = 3;
    public static final int FLAG_DELETE = 4;
    public static final String BROADCAST_FILTER = "alarm_change_filter";
    public static final String BROADCAST_FLAG = "alarm_change_flag";


    private static final String TAG = "Alarm";
    private int _id;
    private boolean _active;
    private int _hour;
    private int _minute;
    private SparseBooleanArray activeDays;
    private int _station;
    private int _volume;

    private Calendar alarmTime;

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
        updateAlarmTime();
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


    // Alarm Handling

    private void updateAlarmTime() {
        alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, _hour);
        alarmTime.set(Calendar.MINUTE, _minute);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);

        Calendar now = Calendar.getInstance();

        if(alarmTime.before(now))   // Alarm time is the next active day AFTER today
            alarmTime.add(Calendar.DAY_OF_YEAR,Math.abs(activeInDays(now, 1))); // abs because -1 is returned if no days are active, which means alarm is only active once.
        else {
            int activeInDays = activeInDays(now, 0);
            if(activeInDays > 0 && alarmTime.after(now))
                alarmTime.add(Calendar.DAY_OF_YEAR,activeInDays);
        }
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

    public void setAlarm(Context context, boolean showToast) {
        updateAlarmTime();
        Intent overviewIntent = new Intent(context,OverviewActivity.class);
        PendingIntent overviewPendingIntent = PendingIntent.getActivity(context,_id,overviewIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(alarmTime.getTimeInMillis(),overviewPendingIntent);
        ((AlarmManager) context.getSystemService(ALARM_SERVICE)).setAlarmClock(alarmClockInfo,pendingIntent(context));

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ((AlarmManager) context.getSystemService(ALARM_SERVICE)).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,alarmTime.getTimeInMillis(),pendingIntent(context));
//        }
//        else {
//            ((AlarmManager) context.getSystemService(ALARM_SERVICE)).setExact(AlarmManager.RTC_WAKEUP,alarmTime.getTimeInMillis(),pendingIntent(context));
//        }

        Log.i(TAG,"Set alarm "+_id+" to play " + getAlarmTimeString());

        if(!_active) {
            _active = true;
            sendChangedBroadcast(context, FLAG_ACTIVE_CHANGE, showToast);
        }
    }

    public void unsetAlarm(Context context, boolean showToast){
        ((AlarmManager) context.getSystemService(ALARM_SERVICE)).cancel(pendingIntent(context));

        Log.i(TAG,"Unset alarm " + _id);

        if(_active) {
            _active = false;
            sendChangedBroadcast(context, FLAG_ACTIVE_CHANGE, showToast);
        }
    }

    public void resetAlarm(Context context) {
        if(get_mon() || get_tue() || get_wed() || get_thu() || get_fri() || get_sat() ||get_sun()){
            setAlarm(context, false);
        }
        else { // If no days are checked, treat it as a one time event.
            unsetAlarm(context, false);
        }
    }

    public void delete(Context context) {
        ((AlarmManager) context.getSystemService(ALARM_SERVICE)).cancel(pendingIntent(context));
        Log.i(TAG, "Delete alarm "+ _id);
        sendChangedBroadcast(context, FLAG_DELETE, true);
    }

    private PendingIntent pendingIntent(Context context){
        Intent alarmIntent = new Intent(context, AlarmReceiver.class)
                .putExtra(context.getString(R.string.alarm_id_int),_id)
                .setPackage(context.getPackageName());
        return PendingIntent.getBroadcast(context,_id,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public String getAlarmTimeString(){
        Calendar now = Calendar.getInstance();
        switch(alarmTime.get(Calendar.DAY_OF_YEAR)-now.get(Calendar.DAY_OF_YEAR)){
            case 0: return String.format(Locale.getDefault(),"Today at %1$tH:%1$tM", alarmTime);
            case 1: return String.format(Locale.getDefault(),"Tomorrow at %1$tH:%1$tM", alarmTime);
            default: return String.format(Locale.getDefault(),"%1$tA %1$tb %1$td at %1$tH:%1$tM", alarmTime);
        }

    }

    // Broadcast

    private void sendChangedBroadcast(Context context, int flag, boolean showToast){
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(new Intent(BROADCAST_FILTER)
                        .putExtra(context.getString(R.string.alarm_id_int), _id)
                        .putExtra(BROADCAST_FLAG, flag)
                        .putExtra(context.getString(R.string.alarm_active_boolean), _active)
                        .putExtra(context.getString(R.string.show_toast_boolean), showToast)
                );
    }

}
