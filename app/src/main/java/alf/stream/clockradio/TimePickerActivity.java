package alf.stream.clockradio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Locale;

import static java.lang.String.format;

public class TimePickerActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private Button setTimeOKButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_picker);

        // Time Picker
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int hour = pref.getInt("alarmHour", -1);
        int minute = pref.getInt("alarmMinute", -1);
        if(hour >= 0 && hour < 24 && minute >= 0 && minute < 60 ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.setHour(hour);
                timePicker.setMinute(minute);
            }
            else {
                timePicker.setCurrentHour(hour);
                timePicker.setCurrentMinute(minute);
            }
        }

        // OK Button
        setTimeOKButton = (Button) findViewById(R.id.setTimeOKButton);
        setTimeOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("timeString", timeString());
                startActivity(intent);
            }
        });
    }

    private String timeString() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return String.format(Locale.ENGLISH, "Alarm: %02d:%02d", timePicker.getHour(), timePicker.getMinute());
        }
        else {
            return format(Locale.ENGLISH, "Alarm: %02d:%02d", timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        }
    }
}
