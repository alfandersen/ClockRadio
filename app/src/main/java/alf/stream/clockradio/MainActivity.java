package alf.stream.clockradio;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private RadioStreamer radioStreamer;
    private Spinner stationSpinner;
    private ToggleButton playToggle;
    private Context context;
    private ProgressBar loadingProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();

        // Loading Animation
        loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        // Radio Streamer
        radioStreamer = new RadioStreamer(this);


        // Select Radio Station Spinner
        stationSpinner = (Spinner) findViewById(R.id.stationSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.radio_stations, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stationSpinner.setAdapter(adapter);
        stationSpinner.setSelection(radioStreamer.getCurrentStation());
        Log.e("selected: ", stationSpinner.getSelectedItemPosition()+"");

        stationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                radioStreamer.setStation(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // Play Radio Button
        playToggle = (ToggleButton) findViewById(R.id.playToggle);
        playToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) radioStreamer.start();
                else radioStreamer.stop();
            }
        });

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
        radioStreamer.stop();
        radioStreamer.saveStation();
    }
}
