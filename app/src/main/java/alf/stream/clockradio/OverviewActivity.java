package alf.stream.clockradio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class OverviewActivity extends AppCompatActivity {

    Context context;
    MenuItem addAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        context = getApplicationContext();
        addAlarm = (MenuItem) findViewById(R.id.action_add_alarm);
        addAlarm.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                context.startActivity(intent);
                return true;
            }
        });
    }
}
