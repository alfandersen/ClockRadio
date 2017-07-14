package alf.stream.clockradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

public class OverviewActivity extends AppCompatActivity {

    private static final String TAG = "OverviewActivity";

    private Context context;
    private DatabaseManager.DatabaseHelper databaseHelper;
    private RadioHandler radioHandler;
    private static SparseArray<Alarm> alarms;
    private static List<RadioStation> radioStations;
    private static StationAdapter stationAdapter;

    // Broadcast Receivers
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver loadingReceiver;
    private BroadcastReceiver playStartedReceiver;
    private BroadcastReceiver playStoppedReceiver;
    private BroadcastReceiver databaseChangedReceiver;

    // UI Elements
    private MenuItem addAlarm;
    private ListView listView;
    private Spinner stationSpinner, regionSpinner;
    private ImageButton playButton;
    private ProgressBar loadingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i(TAG, "onCreate()");
        setContentView(R.layout.activity_overview);
//        context = getApplicationContext();
        context = this;

        databaseHelper = DatabaseManager.DatabaseHelper.getInstance(context);
        databaseHelper.openDatabase();

        radioStations = databaseHelper.getRadioStations();
        if(radioStations.isEmpty()) {
            databaseHelper.populateStationTable(getResources().openRawResource(R.raw.radio_stations));
            radioStations = databaseHelper.getRadioStations();
        }
        stationAdapter = new StationAdapter(context, R.layout.station_spinner_item, R.id.statioSpinnerTextView, radioStations);

        radioHandler = new RadioHandler(context);

        // Setup UI Elements
        setupListView();
        setupStationSpinners();
        setupPlayButton();
        setupLoadingAnimation();

        // Setup Broadcast Receiver
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        setupLoadingReceiver();
        setupPlayStartedReceiver();
        setupPlayStoppedReceiver();
        setupDatabaseChangedReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.i(TAG, "onStart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.i(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.i(TAG, "onDestroy()");
        radioHandler.unBind();
        localBroadcastManager.unregisterReceiver(loadingReceiver);
        localBroadcastManager.unregisterReceiver(playStartedReceiver);
        localBroadcastManager.unregisterReceiver(playStoppedReceiver);
        localBroadcastManager.unregisterReceiver(databaseChangedReceiver);
        databaseHelper.closeDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_overview, menu);
        setupAddAlarm(menu);
        return true;
    }

    public static List<RadioStation> getRadioStations() {
        return radioStations;
    }

    public static StationAdapter getStationAdapter() {
        return stationAdapter;
    }

    public static SparseArray<Alarm> getAlarms() {
        return alarms;
    }

    public static int radioStationAdapterPos(int stationId){
        for(int i = 0; i < stationAdapter.getCount(); i++){
            if((int)stationAdapter.getItemId(i) == stationId)
                return i;
        }
        return -1;
    }

    private void setupAddAlarm(Menu menu){
        addAlarm = (MenuItem) menu.findItem(R.id.addAlarm_Overview);
        addAlarm.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(getApplicationContext(), EditAlarmActivity.class);
//                intent.putExtra(alarm.get_id());
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
        });
    }

    private void setupListView() {
        alarms = databaseHelper.getAllAlarms();
        listView = (ListView) findViewById(R.id.listView_Overview);
        final AlarmListAdapter alarmListAdapter = new AlarmListAdapter(context, databaseHelper.getAlarmsCursor(), false);
        listView.setAdapter(alarmListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent editIntent = new Intent(context, EditAlarmActivity.class);
                editIntent.putExtra(context.getString(R.string.alarm_id_int),(int)alarmListAdapter.getItemId(i));
                context.startActivity(editIntent);
            }
        });
    }

    private void setupStationSpinners() {
        stationSpinner = (Spinner) findViewById(R.id.stationSpinner_Overview);
//        ArrayAdapter<CharSequence> stationAdapter = ArrayAdapter.createFromResource(context, R.array.station_names, android.R.layout.simple_spinner_item);
//        stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stationSpinner.setAdapter(stationAdapter);
        stationSpinner.setSelection(radioHandler.getCurrentStation());
        stationSpinner.setOnItemSelectedListener(stationSelectedListener);


        //TODO: Distinct region spinner
//        regionSpinner = (Spinner) findViewById(R.id.regionSpinner_Overview);
//        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(context, R.array.region_names, android.R.layout.simple_spinner_item);
//        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        regionSpinner.setAdapter(regionAdapter);
//        regionSpinner.setOnItemSelectedListener(stationSelectedListener);
    }

    private void setupPlayButton() {
        playButton = (ImageButton) findViewById(R.id.playButton_Overview);
        playButton.setImageDrawable(getDrawable(radioHandler.isPlaying() ? R.drawable.ic_stop_circled : R.drawable.ic_play_circled));
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(radioHandler.isPlaying())
                    radioHandler.stopPlayBack();
                else
                    radioHandler.startPlayBack();
            }
        });
    }

    private void setupLoadingAnimation() {
        loadingAnimation = (ProgressBar) findViewById(R.id.loadingAnimation_Overview);
    }


    /**********************
     * BroadcastReceivers *
     **********************/

    private void setupLoadingReceiver() {
        loadingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getBooleanExtra(context.getString(R.string.loading_station_boolean),false)) {
                    playButton.setVisibility(ImageButton.GONE);
                    loadingAnimation.setVisibility(ProgressBar.VISIBLE);
                }
            }
        };
        localBroadcastManager.registerReceiver(loadingReceiver, new IntentFilter(context.getString(R.string.loading_station_filter)));
    }

    private void setupPlayStartedReceiver() {
        playStartedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadingAnimation.setVisibility(ProgressBar.GONE);
                playButton.setVisibility(ImageButton.VISIBLE);
                playButton.setImageDrawable(getDrawable(R.drawable.ic_stop_circled));
                int stationIndex = radioHandler.getCurrentStation();
                if(stationSpinner.getSelectedItemPosition() != stationIndex){
                    stationSpinner.setSelection(stationIndex);
                    radioHandler.setStation(stationIndex);
                }
            }
        };
        localBroadcastManager.registerReceiver(playStartedReceiver, new IntentFilter(context.getString(R.string.play_started_filter)));
    }

    private void setupPlayStoppedReceiver() {
        playStoppedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadingAnimation.setVisibility(ProgressBar.GONE);
                playButton.setVisibility(ImageButton.VISIBLE);
                playButton.setImageDrawable(getDrawable(R.drawable.ic_play_circled));
            }
        };
        localBroadcastManager.registerReceiver(playStoppedReceiver, new IntentFilter(context.getString(R.string.play_stopped_filter)));
    }

    private void setupDatabaseChangedReceiver() {
        databaseChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int flag = intent.getIntExtra(context.getString(R.string.alarm_changed_flag),-1);
                int alarmId = intent.getIntExtra(context.getString(R.string.alarm_id_int),-1);
                boolean showToast = intent.getBooleanExtra(context.getString(R.string.show_toast_boolean),false);

                if(flag == -1 || alarmId == -1) Log.e(TAG,"Database broadcast not recognized with flag = "+flag+" and id = "+alarmId);
                else {
                    Log.d(TAG, "Database broadcast with flag " + flag + ",  id " + alarmId + ",  showToast " + showToast);

                    switch (flag) {
                        case Alarm.FLAG_CREATE:
                            setupListView();
                            alarms.get(alarmId).setAlarm(context,false);
                            Toast.makeText(context, "Alarm created. " + alarmSetToast(alarms.get(alarmId)), Toast.LENGTH_LONG).show();
                            break;

                        case Alarm.FLAG_ACTIVE_CHANGE:
                            if (showToast) { // Assuming that reset do not show toasts and checkbox change does
                                if (alarms.get(alarmId).is_active())
                                    Toast.makeText(context, alarmSetToast(alarms.get(alarmId)), Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(context, "Alarm canceled.", Toast.LENGTH_SHORT).show();
                            } else
                                setupListView();
                            break;

                        case Alarm.FLAG_UPDATE:
                            setupListView();
                            if(alarms.get(alarmId).is_active())
                                alarms.get(alarmId).setAlarm(context,false);
                            Toast.makeText(context, "Alarm updated. " + alarmSetToast(alarms.get(alarmId)), Toast.LENGTH_LONG).show();
                            break;

                        case Alarm.FLAG_DELETE:
                            setupListView();
                            Toast.makeText(context, "Alarm deleted.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        };
        localBroadcastManager.registerReceiver(databaseChangedReceiver, new IntentFilter(context.getString(R.string.database_changed_filter)));
    }

    String alarmSetToast(Alarm alarm){
        return stationAdapter.getObjectWithId(alarm.get_station())+ " will play\n" +alarm.getAlarmTimeString();
    }

    /****************
     * AdapterViews *
     ****************/


    private AdapterView.OnItemSelectedListener stationSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(adapterView.equals(stationSpinner)) {
                radioHandler.setStation(i);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    };
}
