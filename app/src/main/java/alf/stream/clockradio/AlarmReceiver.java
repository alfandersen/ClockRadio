package alf.stream.clockradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.util.Log;

/**
 * Created by Alf on 7/5/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        int alarmId = intent.getIntExtra(context.getString(R.string.alarm_id_int), -1);
        DatabaseManager.DatabaseHelper databaseHelper = DatabaseManager.DatabaseHelper.getInstance(context);
        databaseHelper.openDatabase();
        Alarm alarm = databaseHelper.getAlarm(alarmId);

        // Should be redundant, but for good measure check if the alarm is suppose to sound.
        if(alarm != null && alarm.is_active()) {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
            context.startActivity(launchIntent);

            Log.i(TAG, "Start Alarm "+alarm.get_id());

            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, alarm.get_volume(), 0);

            String stationLink = databaseHelper.getStationLink(alarm.get_station());

            Intent radioIntent = new Intent(context, RadioService.class);
            radioIntent.putExtra(context.getString(R.string.station_path_string), stationLink);
            context.startService(radioIntent);

            alarm.resetAlarm(context);
        }
        else{
            Log.w(TAG, "Alarm "+alarmId+" not started because "+(alarm == null ? "alarm was not found in database." : "alarm is not active."));
        }
    }
}
