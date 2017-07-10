package alf.stream.clockradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class OverviewActivity extends AppCompatActivity {

    private static final String TAG = "OverviewActivity";

    private Context context;
    private DataBaseHandler dataBaseHandler;
    private RadioHandler radioHandler;
    private SparseArray<Alarm> alarms;

    // Broadcast Receivers
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver loadingReceiver;
    private BroadcastReceiver playStartedReceiver;
    private BroadcastReceiver playStoppedReceiver;
    private BroadcastReceiver alarmActiveReceiver;
    private BroadcastReceiver alarmDeleteReceiver;

    // UI Elements
    private MenuItem addAlarm;
    private ListView listView;
    private Spinner stationSpinner, regionSpinner;
    private ImageButton playButton;
    private ProgressBar loadingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        context = getApplicationContext();

        dataBaseHandler = new DataBaseHandler(context,DataBaseHandler.DATABASE_NAME,null,DataBaseHandler.DATABASE_VERSION);
        radioHandler = new RadioHandler(context);
        alarms = dataBaseHandler.getAllAlarms();

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
        setupAlarmActiveReceiver();
        setupAlarmDeleteReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        radioHandler.unBind();
        localBroadcastManager.unregisterReceiver(loadingReceiver);
        localBroadcastManager.unregisterReceiver(playStartedReceiver);
        localBroadcastManager.unregisterReceiver(playStoppedReceiver);
        localBroadcastManager.unregisterReceiver(alarmActiveReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_overview, menu);
        setupAddAlarm(menu);
        return true;
    }

    private void setupAddAlarm(Menu menu){
        addAlarm = (MenuItem) menu.findItem(R.id.addAlarm_Overview);
        addAlarm.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(getApplicationContext(), EditAlarmActivity.class);
//                intent.putExtra(alarm.get_id());
                context.startActivity(intent);
                return true;
            }
        });
    }

    private void setupListView() {
        listView = (ListView) findViewById(R.id.listView_Overview);
        final AlarmListAdapter alarmListAdapter = new AlarmListAdapter(context, dataBaseHandler.getAlarmsCursor(), false);
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
        ArrayAdapter<CharSequence> stationAdapter = ArrayAdapter.createFromResource(context, R.array.station_names, android.R.layout.simple_spinner_item);
        stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
                    setStationSpinnerTextColor(R.color.white);
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
                if(stationSpinner.getSelectedItemPosition() != radioHandler.getCurrentStation()){
                    stationSpinner.setSelection(stationIndex);
                }
                setStationSpinnerTextColor(R.color.on);
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
                setStationSpinnerTextColor(R.color.white);
            }
        };
        localBroadcastManager.registerReceiver(playStoppedReceiver, new IntentFilter(context.getString(R.string.play_stopped_filter)));
    }

    private void setupAlarmActiveReceiver() {
        alarmActiveReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int alarmId = intent.getIntExtra(context.getString(R.string.alarm_id_int),-1);
                boolean active = intent.getBooleanExtra(context.getString(R.string.alarm_active_boolean),true);
                Alarm alarm = alarms.get(alarmId);
                if(alarm != null){
                    dataBaseHandler.updateTableField(
                            DataBaseHandler.AlarmTable.TABLE_NAME, alarmId,
                            DataBaseHandler.AlarmTable.COLUMN_ACTIVE, active
                    );
                    if(active) alarm.setAlarm(context);
                    else alarm.cancelAlarm(context);
                }
            }
        };
        localBroadcastManager.registerReceiver(alarmActiveReceiver, new IntentFilter(context.getString(R.string.alarm_active_filter)));
    }

    private void setupAlarmDeleteReceiver() {
        alarmDeleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int alarmId = intent.getIntExtra(context.getString(R.string.alarm_id_int),-1);
                Alarm alarm = alarms.get(alarmId);
                if(alarm != null){
                    alarm.cancelAlarm(context);
                    dataBaseHandler.deleteAlarm(alarm);
                    setupListView();
                    alarms.remove(alarmId);
                }
            }
        };
        localBroadcastManager.registerReceiver(alarmDeleteReceiver, new IntentFilter(context.getString(R.string.alarm_delete_filter)));
    }

    /****************
     * AdapterViews *
     ****************/

    private void setStationSpinnerTextColor(int colorId){
        ((TextView) stationSpinner.getChildAt(0)).setTextColor(ContextCompat.getColor(context,colorId));
    }

    private AdapterView.OnItemSelectedListener stationSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(adapterView.equals(stationSpinner)) {
                radioHandler.setStation(i);
                if(radioHandler.isPlaying())
                    setStationSpinnerTextColor(R.color.on);
                else
                    setStationSpinnerTextColor(R.color.white);
            }

                // TODO: Distinct region spinner
//            if(adapterView.equals(stationSpinner)) {
//                String selection = getResources().getStringArray(R.array.streams)[i];
//                if (selection.equals(getString(R.string.P4_selection))) {
//                    regionSpinner.setVisibility(Spinner.VISIBLE);
//                    radioHandler.setStation(regionSpinner.getSelectedItemPosition());
//                } else {
//                    regionSpinner.setVisibility(Spinner.INVISIBLE);
//                    radioHandler.setStation(i);
//                }
//            }
//            else if (adapterView.equals(regionSpinner)){
//                radioHandler.setStation(i);
//            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    };
}
