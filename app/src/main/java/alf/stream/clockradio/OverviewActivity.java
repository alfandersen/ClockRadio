package alf.stream.clockradio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

public class OverviewActivity extends AppCompatActivity {

    private Context context;

    private RadioHandler radioHandler;

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

        radioHandler = new RadioHandler(context);

        // Setup UI Elements
        setupAddAlarm();
        setupListView();
        setupStationSpinners();
        setupPlayButton();
        setupLoadingAnimation();
    }

    private void setupAddAlarm(){
        addAlarm = (MenuItem) findViewById(R.id.addAlarm_Overview);
        addAlarm.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //TODO: intent.putExtra(alarm.get_id());
                context.startActivity(intent);
                return true;
            }
        });
    }

    private void setupListView() {
        listView = (ListView) findViewById(R.id.listView_Overview);

    }

    private void setupStationSpinners() {
        stationSpinner = (Spinner) findViewById(R.id.stationSpinner_Overview);
        ArrayAdapter<CharSequence> stationAdapter = ArrayAdapter.createFromResource(context, R.array.radio_stations, android.R.layout.simple_spinner_item);
        stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stationSpinner.setAdapter(stationAdapter);
        stationSpinner.setOnItemSelectedListener(stationSelectedListener);

        regionSpinner = (Spinner) findViewById(R.id.regionSpinner_Overview);
        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(context, R.array.region_names, android.R.layout.simple_spinner_item);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);
        regionSpinner.setOnItemSelectedListener(stationSelectedListener);
    }


    private void setupPlayButton() {
        playButton = (ImageButton) findViewById(R.id.playButton_Overview);
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

    private void setLoadingAnimation(boolean isLoading){
        if(isLoading){
            playButton.setVisibility(ImageButton.GONE);
            loadingAnimation.setVisibility(ProgressBar.VISIBLE);
        }
        else{
            playButton.setVisibility(ImageButton.VISIBLE);
            loadingAnimation.setVisibility(ProgressBar.GONE);
            if(radioHandler.isPlaying())
                playButton.setImageDrawable(getDrawable(R.drawable.ic_action_stop));
            else
                playButton.setImageDrawable(getDrawable(R.drawable.ic_action_play));
        }
    }

    private AdapterView.OnItemSelectedListener stationSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(adapterView.equals(stationSpinner)) {
                String selection = getResources().getStringArray(R.array.streams)[i];
                if (selection.equals(getString(R.string.P4_selection))) {
                    regionSpinner.setVisibility(Spinner.VISIBLE);
                    radioHandler.setStation(regionSpinner.getSelectedItemPosition());
                } else {
                    regionSpinner.setVisibility(Spinner.INVISIBLE);
                    radioHandler.setStation(i);
                }
            }
            else if (adapterView.equals(regionSpinner)){
                radioHandler.setStation(i);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    };
}
