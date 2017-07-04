package alf.stream.clockradio;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;

/**
 * Created by Alf on 7/4/2017.
 */

public class RadioStreamer {

    private MainActivity mainActivity;
    private MediaPlayer player;
    private String[] stations;
    private int currentStation;


    public RadioStreamer(MainActivity mainActivity){
        this.mainActivity = mainActivity;

        stations = mainActivity.getResources().getStringArray(R.array.streams);

        currentStation = PreferenceManager
                .getDefaultSharedPreferences(mainActivity)
                .getInt("savedStation", 0);

    }

    private void updatePlayer(){
        player = MediaPlayer.create(mainActivity, Uri.parse(stations[currentStation]));
    }

    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mainActivity.showLoadingBar(true);
                updatePlayer();
                player.start();
                mainActivity.showLoadingBar(false);
                mainActivity.updateSpinnerColor(true);
            }
        }).start();
    }

    public void stop(){
        if(player != null && player.isPlaying()) {
            player.stop();
            mainActivity.updateSpinnerColor(false);
        }
    }

    /**
     * 0 = NEWS
     * 1 = P1
     * 2 = P2
     * 3 = P3
     * 4 = p4
     * 5 = P5
     * 6 = P6 BEAT
     * 7 = P7 MIX
     * 8 = P8 JAZZ
     * @param stationNo
     */
    public void setStation(int stationNo){
        if(stationNo != currentStation && stationNo >= 0 && stationNo < stations.length) {
            currentStation = stationNo;
            if (isPlaying()) {
                stop();
                start();
            }
            saveStation();
        }
    }

    public int getCurrentStation() {
        return currentStation;
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public void saveStation() {
        SharedPreferences.Editor editor = PreferenceManager.
                getDefaultSharedPreferences(mainActivity).
                edit();
        editor.putInt("savedStation", currentStation);
        editor.apply();
    }
}
