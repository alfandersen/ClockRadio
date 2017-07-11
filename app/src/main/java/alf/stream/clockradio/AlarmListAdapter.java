package alf.stream.clockradio;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Alf on 7/8/2017.
 */

class AlarmListAdapter extends CursorAdapter {

    private static final String TAG = "AlarmListAdapter";
    private Context context;
    private String[] stationNames;
    private HashMap<CheckBox,Integer> checkBoxMap = new HashMap<>();

    public AlarmListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
//        Log.i(TAG, "Created "+this);
        this.context = context;
//        stationNames = context.getResources().getStringArray(R.array.station_names);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.alarm_row, parent, false);
    }

    @Override
    public void bindView(View alarmView, final Context context, final Cursor cursor) {
        // ID
        final int alarmId = parseInt(cursor, DatabaseManager.DatabaseHelper.AlarmTable.COLUMN_ID);

        // Time
        TextView timeTextView = (TextView) alarmView.findViewById(R.id.timeTextView_AlarmRow);
        int hour = parseInt(cursor, DatabaseManager.DatabaseHelper.AlarmTable.COLUMN_HOUR);
        int minute = parseInt(cursor, DatabaseManager.DatabaseHelper.AlarmTable.COLUMN_MINUTE);
        timeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));

        // Station Name
        TextView stationNameTextView = (TextView) alarmView.findViewById(R.id.stationTextView_AlarmRow);
//        int stationNo = parseInt(cursor, DatabaseManager.DatabaseHelper.AlarmTable.COLUMN_STATION);
        String stationName = parseString(cursor, DatabaseManager.DatabaseHelper.StationTable.COLUMN_NAME);
        stationNameTextView.setText(stationName);

        // Is Active
        CheckBox activationCheckBox = (CheckBox) alarmView.findViewById(R.id.activeCheckBox_AlarmRow);
        activationCheckBox.setChecked(parseBoolean(cursor, DatabaseManager.DatabaseHelper.AlarmTable.COLUMN_ACTIVE));
        activationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(new Intent(context.getString(R.string.alarm_active_filter))
                        .putExtra(context.getString(R.string.alarm_id_int),alarmId)
                        .putExtra(context.getString(R.string.alarm_active_boolean),b));
            }
        });
        checkBoxMap.put(activationCheckBox,alarmId);

        // Days
        setDayActive(context, alarmView, R.id.mon_AlarmRow, parseBoolean(cursor, DatabaseManager.DatabaseHelper.AlarmTable.COLUMN_MON));
        setDayActive(context, alarmView, R.id.tue_AlarmRow, parseBoolean(cursor, DatabaseManager.DatabaseHelper.AlarmTable.COLUMN_TUE));
        setDayActive(context, alarmView, R.id.wed_AlarmRow, parseBoolean(cursor, DatabaseManager.DatabaseHelper.AlarmTable.COLUMN_WED));
        setDayActive(context, alarmView, R.id.thu_AlarmRow, parseBoolean(cursor, DatabaseManager.DatabaseHelper.AlarmTable.COLUMN_THU));
        setDayActive(context, alarmView, R.id.fri_AlarmRow, parseBoolean(cursor, DatabaseManager.DatabaseHelper.AlarmTable.COLUMN_FRI));
        setDayActive(context, alarmView, R.id.sat_AlarmRow, parseBoolean(cursor, DatabaseManager.DatabaseHelper.AlarmTable.COLUMN_SAT));
        setDayActive(context, alarmView, R.id.sun_AlarmRow, parseBoolean(cursor, DatabaseManager.DatabaseHelper.AlarmTable.COLUMN_SUN));

        // TODO: Delete button
        ImageView deleteImage = (ImageView) alarmView.findViewById(R.id.deleteImageView_AlarmRow);
        deleteImage.setImageDrawable(context.getDrawable(R.drawable.ic_cross_circled));
        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"delete alarm with id " + alarmId + " from Adapter "+this);
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(new Intent(context.getString(R.string.alarm_delete_filter))
                        .putExtra(context.getString(R.string.alarm_id_int),alarmId));
            }
        });
    }

    private boolean parseBoolean(Cursor cursor, String column){
        return parseInt(cursor, column) == 1;
    }

    private int parseInt(Cursor cursor, String column) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(column));
    }

    private String parseString(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndexOrThrow(column));
    }

    private void setDayActive(Context context, View v, int id, boolean active){
        if(active) {
            TextView tv = (TextView) v.findViewById(id);
            tv.setTextColor(ContextCompat.getColor(context, R.color.on));
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        }
    }

//    CompoundButton.OnCheckedChangeListener activeChangedListener = new CompoundButton.OnCheckedChangeListener() {
//        @Override
//        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
//            lbm.sendBroadcast(new Intent(context.getString(R.string.alarm_active_filter))
//                    .putExtra(context.getString(R.string.alarm_id_int),checkBoxMap.get((CheckBox)compoundButton))
//                    .putExtra(context.getString(R.string.alarm_active_boolean),b));
//        }
//    };
}
