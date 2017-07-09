package alf.stream.clockradio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Alf on 7/5/2017.
 */

public class RadioHandler {

    private final String TAG = "RadioHandler";
    private Context context;
    private Intent intent;

    private int currentStation;
    private String[] stationLinks;
//    private int currentRegion;
//    private String[] regionUrls;
    private String currentStationUrl;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    // TODO: implement region and put saved instance strings in string.xml

    public RadioHandler(Context c){
        context = c;
        intent = new Intent(context, RadioService.class);
        context.bindService(intent, serviceConnection, 0);

        stationLinks = context.getResources().getStringArray(R.array.station_links);
        currentStation = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.saved_station_int), -1);

        Log.e(TAG,"currentStation = "+currentStation);

        if(currentStation < 0 || currentStation >= stationLinks.length)
            currentStation = 0;

        updateCurrentStationUrl();
    }

    private void updateCurrentStationUrl(){
        currentStationUrl = stationLinks[currentStation];
        intent.putExtra(context.getString(R.string.station_path_string),currentStationUrl);
    }

    public int getCurrentStation() {
        return currentStation;
    }

    public void setStation(int newStation) {
        if(newStation != currentStation && newStation >= 0 && newStation < stationLinks.length) {
            currentStation = newStation;
            updateCurrentStationUrl();
            saveStation();
            if(isPlaying())
                startPlayBack();
        }
    }

    public void saveStation() {
        Log.e(TAG,"Saving station.");
        SharedPreferences.Editor editor = PreferenceManager.
                getDefaultSharedPreferences(context).
                edit();
        editor.putInt(context.getString(R.string.saved_station_int), currentStation);
        editor.apply();
    }

    public void startPlayBack() {
        context.startService(intent);
    }

    public void stopPlayBack() {
        context.stopService(intent);
    }

    public void unBind(){
        context.unbindService(serviceConnection);
    }

    public boolean isPlaying() {
        return RadioService.isPlaying();
    }
}
