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
    public static final int FLAG_LOADING = 1;
    public static final int FLAG_PLAYING = 2;
    public static final int FLAG_STOPPED = 3;
    public static final String BROADCAST_FILTER = "radio_service_filter";
    public static final String BROADCAST_FLAG = "radio_service_flag";

    private static final String TAG = "RadioService";
    private static MediaPlayer player;
    private static String stationUrl;
    private Context context;
    private LocalBroadcastManager lbm;

    public static boolean isPlaying(){
        return player != null && player.isPlaying();
    }

    public static String getStationUrl() {
        return stationUrl;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.i(TAG, "onCreate()");
        context = getApplicationContext();
        lbm = LocalBroadcastManager.getInstance(context);
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

            if (stationUrl == null || !stationUrl.equals(newStation) || player == null || !player.isPlaying()) {
                stationUrl = newStation;
                startPlayer();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startPlayer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(stationUrl != null) {
                    stopPlayer();

                    lbm.sendBroadcast(new Intent(BROADCAST_FILTER).putExtra(BROADCAST_FLAG,FLAG_LOADING));

                    player = MediaPlayer.create(context, Uri.parse(stationUrl));

                    if(player != null) {
                        Log.i(TAG, "startPlayer()");
                        player.start();

                        lbm.sendBroadcast(new Intent(BROADCAST_FILTER).putExtra(BROADCAST_FLAG,FLAG_PLAYING));

                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                startPlayer();
                            }
                        });
                    }
                    else{
                        Log.e(TAG, "Player couldn't start! Reason: Mediaplayer.create() returned null.");
                        lbm.sendBroadcast(new Intent(BROADCAST_FILTER).putExtra(BROADCAST_FLAG,FLAG_STOPPED));
                    }
                }
            }
        }).start();
    }

    private void stopPlayer(){
        if(player != null && player.isPlaying()) {
            lbm.sendBroadcast(new Intent(BROADCAST_FILTER).putExtra(BROADCAST_FLAG,FLAG_STOPPED));
            Log.i(TAG, "stopPlayer()");
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
