package alf.stream.clockradio;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TimePicker;

public class EditAlarmActivity extends AppCompatActivity {

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
        int alarmId = intent.getIntExtra(getString(R.string.alarm_id), -1);
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
        if(alarm != null){
            monTextView.setChecked(alarm.get_mon());
            tueTextView.setChecked(alarm.get_tue());
            wedTextView.setChecked(alarm.get_wed());
            thuTextView.setChecked(alarm.get_thu());
            friTextView.setChecked(alarm.get_fri());
            satTextView.setChecked(alarm.get_sat());
            sunTextView.setChecked(alarm.get_sun());
        }
    }

    private void setupStationSpinners() {
        stationSpinner = (Spinner) findViewById(R.id.stationSpinner_EditAlarm);
        ArrayAdapter<CharSequence> stationAdapter = ArrayAdapter.createFromResource(context, R.array.radio_stations, android.R.layout.simple_spinner_item);
        stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stationSpinner.setAdapter(stationAdapter);

        regionSpinner = (Spinner) findViewById(R.id.regionSpinner_EditAlarm);
        ArrayAdapter<CharSequence> regionalAdapter = ArrayAdapter.createFromResource(context, R.array.p4_array, android.R.layout.simple_spinner_item);
        regionalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionalAdapter);

        stationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = getResources().getStringArray(R.array.streams)[i];
                if(selection.equals(getString(R.string.P4_selection)))
                    regionSpinner.setVisibility(Spinner.VISIBLE);
                else
                    regionSpinner.setVisibility(Spinner.INVISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        if(alarm != null) {
            stationSpinner.setSelection(alarm.get_station());
            regionSpinner.setSelection(alarm.get_region());
        }
    }

    private void setupVolumeBar() {
        volumeBar = (SeekBar) findViewById(R.id.volumeSeekBar_EditAlarm);
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volumeBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        new Thread(new Runnable() {
            @Override
            public void run() {
                mediaPlayer = MediaPlayer.create(context, R.raw.volume_change);
            }
        });

        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.start();
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
        return new Alarm( alarm == null ? -1 : alarm.get_id(),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? timePicker.getHour(): timePicker.getCurrentHour(),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? timePicker.getMinute(): timePicker.getCurrentMinute(),
                monTextView.isChecked(), tueTextView.isChecked(), wedTextView.isChecked(),
                thuTextView.isChecked(), friTextView.isChecked(), satTextView.isChecked(), sunTextView.isChecked(),
                stationSpinner.getSelectedItemPosition(), regionSpinner.getSelectedItemPosition(),
                volumeBar.getProgress()
        );
    }
}
