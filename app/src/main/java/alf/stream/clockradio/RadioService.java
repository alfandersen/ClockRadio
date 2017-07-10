package alf.stream.clockradio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Alf on 7/5/2017.
 */

public class RadioService extends Service {
    private static MediaPlayer player;
    private static String station;
    private Context context;

    public static boolean isPlaying(){
        return player != null && player.isPlaying();
    }

    public static String getStation() {
        return station;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    /**
     * IMPORTANT!!! Remember to:
     * intent.putExtra(context.getString(R.string.station_path_string), "blablabla.m3u8");
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO: Backup Alarm in case of null intent or no internet.
        if(intent == null) Log.e ("intent: ", "null");
        else {
            String newStation = intent.getStringExtra(context.getString(R.string.station_path_string));

            if (station == null || !station.equals(newStation) || player == null || !player.isPlaying()) {
                station = newStation;
                startPlayer();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startPlayer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(station != null) {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(new Intent(context.getString(R.string.loading_station_filter))
                                    .putExtra(context.getString(R.string.loading_station_boolean),true));

                    stopPlayer();
                    player = MediaPlayer.create(context, Uri.parse(station));
                    player.start();

                    lbm.sendBroadcast(new Intent(getResources().getString(R.string.play_started_filter)));
                    lbm.sendBroadcast(new Intent(getResources().getString(R.string.loading_station_filter))
                                    .putExtra(getResources().getString(R.string.loading_station_boolean),false));

                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            startPlayer();
                        }
                    });
                }
            }
        }).start();
    }

    private void stopPlayer(){
        if(player != null && player.isPlaying()) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(new Intent(getResources().getString(R.string.play_stopped_filter)));
            player.stop();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayer();
    }
}
