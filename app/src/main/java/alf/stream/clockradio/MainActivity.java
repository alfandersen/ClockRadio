package alf.stream.clockradio;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Context context;
//    private RadioStreamer radioStreamer;
    private AlarmManager alarmManager;
    private Intent alarmIntent;
    private PendingIntent pendingAlarm;

    private RadioHandler radioHandler;

    private ToggleButton alarmToggle;
    private TextView alarmTimeTextView;
    private Button editTimeButton;

    private Spinner stationSpinner;
    private ToggleButton playToggle;
    private ProgressBar loadingProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();

        radioHandler = new RadioHandler(context);

        // TODO: Loading Animation
        loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);


        // Select Radio Station Spinner
        stationSpinner = (Spinner) findViewById(R.id.stationSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.radio_stations, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stationSpinner.setAdapter(adapter);
        stationSpinner.setSelection(PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt("savedStation", 0));
        Log.e("selected: ", stationSpinner.getSelectedItemPosition()+"");

        stationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                radioHandler.setStation(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // Set Alarm Button
        alarmToggle = (ToggleButton) findViewById(R.id.alarmToggle);
        alarmToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    Calendar calendar = Calendar.getInstance();
                    // Assuming text is of format "Alarm: 03:14" -> {"Alarm", " 03", "14"}
                    int[] hourAndMinute = hourAndMinuteParser(alarmTimeTextView.getText().toString());
                    if(hourAndMinute != null) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourAndMinute[0]);
                        calendar.set(Calendar.MINUTE, hourAndMinute[1]);
                        calendar.set(Calendar.SECOND, 0);
                        if(calendar.compareTo(Calendar.getInstance()) <= 0){
                            calendar.add(Calendar.DAY_OF_YEAR,1);
                        }

                        pendingAlarm = PendingIntent.getBroadcast(context,0,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingAlarm);
                    }
                    else {
                        alarmToggle.setChecked(false);
                    }
                }
                else {
                    alarmManager.cancel(pendingAlarm);
                }
            }
        });

        // Play Button
        playToggle = (ToggleButton) findViewById(R.id.playButton);
        playToggle.setChecked(radioHandler.isPlaying());
        playToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) radioHandler.startPlayBack();
                else radioHandler.stopPlayBack();
            }
        });

        // Alarm Time Text
        alarmTimeTextView = (TextView) findViewById(R.id.alarmTimeTextView);
        alarmTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToTimePicker();
            }
        });
        updateAlarmTime();

        // Alarm Manager
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmIntent = new Intent(context, AlarmReceiver.class);
//        Bundle bundle = new Bundle();
    }

    /**
     *  Assuming text is of format "Alarm: 03:14" -> {"Alarm", " 03", "14"}
     */
    private int[] hourAndMinuteParser(String alarmTimeText){
        String[] timeText = alarmTimeText.split(":");
        try {
            int[] out = new int[2];
            out[0] = Integer.parseInt(timeText[1].trim());
            out[1] = Integer.parseInt(timeText[2].trim());
            return out;
        }
        catch (Exception e) {
            Log.e("Parser fail", alarmTimeText);
            Log.e("timeText", "{'"+timeText[0]+"' , '"+timeText[1]+"' , '"+timeText[2]+"'}");
            e.printStackTrace();
            return null;
        }
    }

    private boolean updateAlarmTime() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String timeText = getIntent().getStringExtra("timeString");

        if(timeText != null){
            Log.e("TimeText ", "not null");
            int[] hourAndMinute = hourAndMinuteParser(timeText);
            if(hourAndMinute!= null) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("alarmHour", hourAndMinute[0]);
                editor.putInt("alarmMinute", hourAndMinute[1]);
                editor.apply();
                alarmTimeTextView.setText(timeText);
                return true;
            }
        }
        int hour = pref.getInt("alarmHour", 0);
        int minute = pref.getInt("alarmMinute", 0);
        timeText = String.format(Locale.ENGLISH, "Alarm: %02d:%02d", hour, minute);
        alarmTimeTextView.setText(timeText);
        return false;
    }

    private void goToTimePicker(){
        // TODO: Add extra: time shown in app to set default time.
        Intent intent = new Intent(getApplicationContext(),TimePickerActivity.class);
        startActivity(intent);
    }

    public void updateSpinnerColor(final boolean isPlaying) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView playingStation = (TextView) stationSpinner.getChildAt(0);
                if (playingStation != null) {
                    if (isPlaying) {
                        playingStation.setTextColor(Color.BLACK);
                        stationSpinner.setBackgroundColor(ContextCompat.getColor(context, R.color.on));
                    } else {
                        playingStation.setTextColor(Color.WHITE);
                        stationSpinner.setBackgroundColor(ContextCompat.getColor(context, R.color.off));
                    }
                }
            }
        });
    }

    public void showLoadingBar(final boolean show){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(show) loadingProgressBar.setVisibility(loadingProgressBar.VISIBLE);
                else loadingProgressBar.setVisibility(loadingProgressBar.INVISIBLE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        radioHandler.unBind();
    }

    public boolean playToggleIsChecked() {
        return playToggle.isChecked();
    }


}
