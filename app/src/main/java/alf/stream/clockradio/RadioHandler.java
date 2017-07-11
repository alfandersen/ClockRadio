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

    private final String TAG = "RadioHandler";
    private Context context;
    private Intent intent;

    private int currentStation;
//    private String[] stationLinks;
//    private int currentRegion;
//    private String[] regionUrls;
//    private String currentStationUrl;

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
//        Log.i(TAG, "Created "+this);
        context = c;
        intent = new Intent(context, RadioService.class);
        context.bindService(intent, serviceConnection, 0);

//        stationLinks = context.getResources().getStringArray(R.array.station_links);
        if(isPlaying()){
            updateCurrentStation();
        }
        else {
            currentStation = PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getInt(context.getString(R.string.saved_station_int), -1);
        }

        if(currentStation < 0 || currentStation >= OverviewActivity.getRadioStations().size())
            currentStation = 0;

//        updateCurrentStationUrl();
    }

    private void updateCurrentStation(){
        String stationLink = RadioService.getStationUrl();
        for(int i = 0; i < OverviewActivity.getRadioStations().size(); i++){
            if(OverviewActivity.getRadioStations().get(i).get_link().equals(stationLink)) {
                currentStation = i;
                break;
            }
        }
    }

//    private void updateCurrentStationUrl(){
//        currentStationUrl = stationLinks[currentStation];
//        intent.putExtra(context.getString(R.string.station_path_string),currentStationUrl);
//    }

    public int getCurrentStation() {
        if(isPlaying())
            updateCurrentStation();
        return currentStation;
    }

    public void setStation(int newStation) {
        if(newStation != currentStation && newStation >= 0 && newStation < OverviewActivity.getRadioStations().size()) {
            currentStation = newStation;
//            updateCurrentStationUrl();
            saveStation();
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
        intent.putExtra(context.getString(R.string.station_path_string),OverviewActivity.getRadioStations().get(currentStation).get_link());
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
