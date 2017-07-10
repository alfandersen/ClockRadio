package alf.stream.clockradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by Alf on 7/5/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        int alarmId = intent.getIntExtra(context.getString(R.string.alarm_id_int), -1);
        DataBaseHandler dbHandler = new DataBaseHandler(context,DataBaseHandler.DATABASE_NAME,null,DataBaseHandler.DATABASE_VERSION);
        Alarm alarm = dbHandler.getAlarm(alarmId);
        Log.e(TAG, "Start "+alarm);

        // Should be redundant, but for good measure check if the alarm is suppose to sound.
        if(alarm != null && alarm.is_active()) {
            int station = alarm.get_station();
            String[] stationUrls = context.getResources().getStringArray(R.array.station_links);
            Intent radioIntent = new Intent(context, RadioService.class);
            radioIntent.putExtra(context.getString(R.string.station_path_string), stationUrls[station]);
            context.startService(radioIntent);

            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
            context.startActivity(launchIntent);

            alarm.resetAlarm(context);
        }
    }
}
