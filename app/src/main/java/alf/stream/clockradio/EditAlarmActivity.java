package alf.stream.clockradio;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TimePicker;

import static alf.stream.clockradio.R.color.on;

public class EditAlarmActivity extends AppCompatActivity {

    private static final String TAG = "EditAlarmActivity";
    private static final int CREATING = 0;
    private static final int EDITING = 1;
    private int mode;
    private Alarm alarm;

    private DatabaseManager.DatabaseHelper databaseHelper;
    private MediaPlayer mediaPlayer;

    private Context context;
    private Intent intent;

    // UI Elements
    private TimePicker timePicker;
    private CheckedTextView monTextView, tueTextView, wedTextView, thuTextView, friTextView, satTextView, sunTextView;
    private Spinner stationSpinner, regionSpinner;
    private SeekBar volumeBar;
    private Button okButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i(TAG, "onCreate()");
        setContentView(R.layout.activity_edit_alarm);
        context = this;
        intent = getIntent();

        // Get alarm if it exists in the database
//        databaseHelper = DatabaseManager.DatabaseHelper.getInstance(context);
        int alarmId = intent.getIntExtra(getString(R.string.alarm_id_int), -1);
//        alarm = databaseHelper.getAlarm(alarmId);
        alarm = OverviewActivity.getAlarms().get(alarmId);
        mode = alarm == null ? CREATING : EDITING;

        // Setup UI Elements
        setupTimePicker();
        setupWeekDayCheckers();
        setupStationSpinners();
        setupVolumeBar();
        setupOkButton();
        setupCancelButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setupTimePicker(){
        timePicker = (TimePicker) findViewById(R.id.timePicker_EditAlarm);
        timePicker.setIs24HourView(true);
        if(mode == EDITING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.setHour(alarm.get_hour());
                timePicker.setMinute(alarm.get_minute());
            } else {
                timePicker.setCurrentHour(alarm.get_hour());
                timePicker.setCurrentMinute(alarm.get_minute());
            }
        }
    }

    private void setupWeekDayCheckers() {
        monTextView = (CheckedTextView) findViewById(R.id.monCheckedTextView_EditAlarm);
        tueTextView = (CheckedTextView) findViewById(R.id.tueCheckedTextView_EditAlarm);
        wedTextView = (CheckedTextView) findViewById(R.id.wedCheckedTextView_EditAlarm);
        thuTextView = (CheckedTextView) findViewById(R.id.thuCheckedTextView_EditAlarm);
        friTextView = (CheckedTextView) findViewById(R.id.friCheckedTextView_EditAlarm);
        satTextView = (CheckedTextView) findViewById(R.id.satCheckedTextView_EditAlarm);
        sunTextView = (CheckedTextView) findViewById(R.id.sunCheckedTextView_EditAlarm);
        monTextView.setOnClickListener(dayClickListener);
        tueTextView.setOnClickListener(dayClickListener);
        wedTextView.setOnClickListener(dayClickListener);
        thuTextView.setOnClickListener(dayClickListener);
        friTextView.setOnClickListener(dayClickListener);
        satTextView.setOnClickListener(dayClickListener);
        sunTextView.setOnClickListener(dayClickListener);
        if(mode == EDITING){
            monTextView.setChecked(alarm.get_mon());
            tueTextView.setChecked(alarm.get_tue());
            wedTextView.setChecked(alarm.get_wed());
            thuTextView.setChecked(alarm.get_thu());
            friTextView.setChecked(alarm.get_fri());
            satTextView.setChecked(alarm.get_sat());
            sunTextView.setChecked(alarm.get_sun());
        }
        updateDayTextView(monTextView);
        updateDayTextView(tueTextView);
        updateDayTextView(wedTextView);
        updateDayTextView(thuTextView);
        updateDayTextView(friTextView);
        updateDayTextView(satTextView);
        updateDayTextView(sunTextView);
    }

    private View.OnClickListener dayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CheckedTextView tv = (CheckedTextView) view;
            tv.setChecked(!tv.isChecked()); // Toggle checked
            updateDayTextView(tv);
        }
    };

    private void updateDayTextView(CheckedTextView tv){
        if(tv.isChecked()) {
            tv.setTextColor(ContextCompat.getColor(context, on));
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        }
        else {
            tv.setTextColor(ContextCompat.getColor(context, R.color.off_bright));
            tv.setTypeface(tv.getTypeface(), Typeface.NORMAL);
        }
    }

    private void setupStationSpinners() {
        stationSpinner = (Spinner) findViewById(R.id.stationSpinner_EditAlarm);
        StationAdapter stationAdapter = OverviewActivity.getStationAdapter();
        stationSpinner.setAdapter(stationAdapter);

        if(mode == EDITING) {
            stationSpinner.setSelection(stationAdapter.getIdPosition(alarm.get_station()));
        }
        else {
            stationSpinner.setSelection(PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getInt(context.getString(R.string.saved_station_int), -1));
        }
    }

    private void setupVolumeBar() {
        volumeBar = (SeekBar) findViewById(R.id.volumeSeekBar_EditAlarm);
        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volumeBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        new Thread(new Runnable() {
            @Override
            public void run() {
                mediaPlayer = MediaPlayer.create(context, R.raw.volume_change);
            }
        }).start();

        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mediaPlayer != null) {
                    final int temp = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, volumeBar.getProgress(), 0);
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, temp, 0);
                        }
                    });
                }
            }
        });

        if(mode == EDITING){
            volumeBar.setProgress(alarm.get_volume());
        }
        else
            volumeBar.setProgress((int)(0.7*volumeBar.getMax()));
    }

    private void setupOkButton(){
        okButton = (Button) findViewById(R.id.okButton_EditAlarm);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                if(mode == CREATING)
                    lbm.sendBroadcast(createAlarmIntent());
                else {
                    lbm.sendBroadcast(updateAlarmIntent());
                }
                finish();
            }
        });
    }

    private void setupCancelButton() {
        cancelButton = (Button) findViewById(R.id.cancelButton_EditAlarm);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private Intent createAlarmIntent(){
        return makeIntent()
                .putExtra(context.getString(R.string.alarm_changed_flag), Alarm.FLAG_CREATE);
    }

    private Intent updateAlarmIntent(){
        return makeIntent()
                .putExtra(context.getString(R.string.alarm_changed_flag), Alarm.FLAG_UPDATE)
                .putExtra(context.getString(R.string.alarm_id_int),alarm.get_id())
                .putExtra(context.getString(R.string.alarm_active_boolean), alarm.is_active());
    }

    private Intent makeIntent(){
        return new Intent(context.getString(R.string.alarm_changed_filter))
                .putExtra(context.getString(R.string.show_toast_boolean), true)
                .putExtra(context.getString(R.string.alarm_time_int_array),
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                                new int[] {timePicker.getHour(), timePicker.getMinute()} :
                                new int[] {timePicker.getCurrentHour(),timePicker.getCurrentMinute()}
                )
                .putExtra(context.getString(R.string.alarm_days_boolean_array),
                        new boolean[] {monTextView.isChecked(), tueTextView.isChecked(), wedTextView.isChecked(),
                                thuTextView.isChecked(), friTextView.isChecked(), satTextView.isChecked(), sunTextView.isChecked()}
                )
                .putExtra(context.getString(R.string.station_id_int), ((RadioStation)stationSpinner.getSelectedItem()).get_id())
                .putExtra(context.getString(R.string.alarm_volume_int), volumeBar.getProgress());
    }
}
