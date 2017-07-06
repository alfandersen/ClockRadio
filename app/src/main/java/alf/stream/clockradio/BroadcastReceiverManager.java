package alf.stream.clockradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Alf on 7/6/2017.
 */

public class BroadcastReceiverManager {
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver playbackStartReceiver;
    private BroadcastReceiver loadingReceiver;

    public BroadcastReceiverManager(Context context, final MainActivity mainActivity){
        localBroadcastManager = LocalBroadcastManager.getInstance(context);


        playbackStartReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mainActivity.activatePlayButton();

            }
        };
        localBroadcastManager.registerReceiver(playbackStartReceiver, new IntentFilter(context.getString(R.string.play_started_filter)));


        loadingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mainActivity.showLoadingBar(intent.getBooleanExtra(context.getString(R.string.loading_station_boolean),false));
            }
        };
        localBroadcastManager.registerReceiver(loadingReceiver, new IntentFilter(context.getString(R.string.loading_station_filter)));

    }

    public void unregister() {
        localBroadcastManager.unregisterReceiver(playbackStartReceiver);
        localBroadcastManager.unregisterReceiver(loadingReceiver);
    }
}
