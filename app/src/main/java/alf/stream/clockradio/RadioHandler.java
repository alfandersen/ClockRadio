package alf.stream.clockradio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

/**
 * Created by Alf on 7/5/2017.
 */

public class RadioHandler {
    private int currentStation;
    private String[] stationUrls;
    private String currentStationUrl;
    private Intent intent;
    private Context context;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public RadioHandler(Context c){
        context = c;
        intent = new Intent(context, RadioService.class);
        context.bindService(intent, serviceConnection, 0);

        stationUrls = context.getResources().getStringArray(R.array.streams);
        currentStation = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.saved_station_int), 0);
        if(currentStation < 0 || currentStation >= stationUrls.length)
            currentStation = 0;

        updateCurrentStationUrl();
    }

    private void updateCurrentStationUrl(){
        currentStationUrl = stationUrls[currentStation];
        intent.putExtra(context.getString(R.string.station_path_string),currentStationUrl);
        saveStation();
    }

    public int getCurrentStation() {
        return currentStation;
    }

    public void setStation(int newStation) {
        if(newStation != currentStation && newStation >= 0 && newStation < stationUrls.length) {
            currentStation = newStation;
            updateCurrentStationUrl();
            if(isPlaying())
                startPlayBack();
        }
    }

    public void saveStation() {
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
