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
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import static alf.stream.clockradio.R.color.on;

public class EditAlarmActivity extends AppCompatActivity {

    private static final String TAG = "EditAlarmActivity";
    private Alarm alarm;

    private DataBaseHandler dataBaseHandler;
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
        setContentView(R.layout.activity_edit_alarm);

        context = getApplicationContext();
        intent = getIntent();

        // Get alarm if it exists in the database
        dataBaseHandler = new DataBaseHandler(context, DataBaseHandler.DATABASE_NAME, null, DataBaseHandler.DATABASE_VERSION);
        int alarmId = intent.getIntExtra(getString(R.string.alarm_id_int), -1);
        alarm = dataBaseHandler.getAlarm(alarmId);

        // Setup UI Elements
        setupTimePicker();
        setupWeekDayCheckers();
        setupStationSpinners();
        setupVolumeBar();
        setupOkButton();
        setupCancelButton();
    }

    private void setupTimePicker(){
        timePicker = (TimePicker) findViewById(R.id.timePicker_EditAlarm);
        timePicker.setIs24HourView(true);
        if(alarm != null) {
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
        if(alarm != null){
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
        ArrayAdapter<CharSequence> stationAdapter = ArrayAdapter.createFromResource(context, R.array.station_names, android.R.layout.simple_spinner_item);
        stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stationSpinner.setAdapter(stationAdapter);
        stationSpinner.setOnItemSelectedListener(stationSelectedListener);

//        regionSpinner = (Spinner) findViewById(R.id.regionSpinner_EditAlarm);
//        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(context, R.array.region_names, android.R.layout.simple_spinner_item);
//        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        regionSpinner.setAdapter(regionAdapter);

        if(alarm != null) {
            stationSpinner.setSelection(alarm.get_station());
//            regionSpinner.setSelection(alarm.get_region());
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

        if(alarm != null){
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
                if(alarm == null)
                    dataBaseHandler.addAlarm(createAlarm());
                else
                    dataBaseHandler.updateAlarm(createAlarm());

                Intent overviewIntent = new Intent(context,OverviewActivity.class);
                context.startActivity(overviewIntent);
            }
        });
    }

    private void setupCancelButton() {
        cancelButton = (Button) findViewById(R.id.cancelButton_EditAlarm);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent overviewIntent = new Intent(context,OverviewActivity.class);
                context.startActivity(overviewIntent);
            }
        });
    }

    private Alarm createAlarm(){
        return new Alarm(
                alarm == null ? -1 : alarm.get_id(),
                alarm == null || alarm.is_active(),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? timePicker.getHour(): timePicker.getCurrentHour(),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? timePicker.getMinute(): timePicker.getCurrentMinute(),
                monTextView.isChecked(), tueTextView.isChecked(), wedTextView.isChecked(),
                thuTextView.isChecked(), friTextView.isChecked(), satTextView.isChecked(), sunTextView.isChecked(),
                stationSpinner.getSelectedItemPosition(), //TODO: Remember to change when I make distinct region spinner (should probably get from database)
                volumeBar.getProgress()
        );
    }

    private AdapterView.OnItemSelectedListener stationSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            ((TextView) stationSpinner.getChildAt(0)).setTextColor(ContextCompat.getColor(context,R.color.white));
            // TODO: Distinct region spinner
//            String selection = getResources().getStringArray(R.array.station_links)[i];
//            if(selection.startsWith("P4"))
//                regionSpinner.setVisibility(Spinner.VISIBLE);
//            else
//                regionSpinner.setVisibility(Spinner.INVISIBLE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    };
}
