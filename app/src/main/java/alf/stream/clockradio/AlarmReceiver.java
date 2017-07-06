package alf.stream.clockradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Alf on 7/5/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Alarm is going off!!!", "ARHHH WAKEUPPPPPPP!!!");
        Intent radioIntent = new Intent(context, RadioService.class);

        String[] stationUrls = context.getResources().getStringArray(R.array.streams);
        int currentStation = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt("savedStation", 0);

        radioIntent.putExtra("stationPath",stationUrls[currentStation]);

        context.startService(radioIntent);
    }

}
