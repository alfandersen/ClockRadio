package alf.stream.clockradio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Alf on 7/5/2017.
 */

public class RadioService extends Service {
    private static MediaPlayer player;
    private String station;
    private Context context;

    public static boolean isPlaying(){
        return player != null && player.isPlaying();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    /**
     * IMPORTANT!!! Remember to:
     * intent.putExtra("stationPath", "blablabla.m3u8");
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO: Backup Alarm in case of null intent or no internet.
        if(intent == null) Log.e ("intent: ", "null");
        else {
            String newStation = intent.getStringExtra("stationPath");

            if (station == null || newStation != station || player == null || !player.isPlaying()) {
                station = newStation;
                stopPlayer();
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
                    player = MediaPlayer.create(context, Uri.parse(station));
                    player.start();
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
