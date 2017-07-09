package alf.stream.clockradio;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.Locale;

import static alf.stream.clockradio.R.color.on;

/**
 * Created by Alf on 7/8/2017.
 */

class AlarmListAdapter extends CursorAdapter {

    public static final String TAG = "AlarmListAdapter";
    private String[] stationNames;

    public AlarmListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        stationNames = context.getResources().getStringArray(R.array.station_names);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.alarm_row, parent, false);
    }

    @Override
    public void bindView(View alarmView, final Context context, final Cursor cursor) {
        // Time
        TextView timeTextView = (TextView) alarmView.findViewById(R.id.timeTextView_AlarmRow);
        int hour = parseInt(cursor, DataBaseHandler.AlarmTable.COLUMN_HOUR);
        int minute = parseInt(cursor, DataBaseHandler.AlarmTable.COLUMN_MINUTE);
        timeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));

        // Station Name
        TextView stationNameTextView = (TextView) alarmView.findViewById(R.id.stationTextView_AlarmRow);
        int stationNo = parseInt(cursor, DataBaseHandler.AlarmTable.COLUMN_STATION);
        stationNameTextView.setText(stationNames[stationNo]);

        // Is Active
        CheckBox activationCheckBox = (CheckBox) alarmView.findViewById(R.id.activeCheckBox_AlarmRow);
        activationCheckBox.setChecked(parseBoolean(cursor, DataBaseHandler.AlarmTable.COLUMN_ACTIVE));

        // Days
        setDayActive(context, alarmView, R.id.mon_AlarmRow, parseBoolean(cursor, DataBaseHandler.AlarmTable.COLUMN_MON));
        setDayActive(context, alarmView, R.id.tue_AlarmRow, parseBoolean(cursor, DataBaseHandler.AlarmTable.COLUMN_TUE));
        setDayActive(context, alarmView, R.id.wed_AlarmRow, parseBoolean(cursor, DataBaseHandler.AlarmTable.COLUMN_WED));
        setDayActive(context, alarmView, R.id.thu_AlarmRow, parseBoolean(cursor, DataBaseHandler.AlarmTable.COLUMN_THU));
        setDayActive(context, alarmView, R.id.fri_AlarmRow, parseBoolean(cursor, DataBaseHandler.AlarmTable.COLUMN_FRI));
        setDayActive(context, alarmView, R.id.sat_AlarmRow, parseBoolean(cursor, DataBaseHandler.AlarmTable.COLUMN_SAT));
        setDayActive(context, alarmView, R.id.sun_AlarmRow, parseBoolean(cursor, DataBaseHandler.AlarmTable.COLUMN_SUN));

        // TODO: Delete button
//        ((ImageView) alarmView.findViewById(R.id.deleteImageView_AlarmRow)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                parent.removeView(alarmView);
//                new DataBaseHandler(getContext(), DataBaseHandler.DATABASE_NAME, null, DataBaseHandler.DATABASE_VERSION)
//                        .deleteAlarm(alarm);
//            }
//        });

//        // TODO: Layout
//        LinearLayout rowLayout = (LinearLayout) alarmView.findViewById(R.id.layout_AlarmRow);
//        rowLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent editIntent = new Intent(context, EditAlarmActivity.class);
//                int alarmId = parseInt(cursor, DataBaseHandler.AlarmTable.COLUMN_ID);
//                editIntent.putExtra(context.getString(R.string.alarm_id_int),alarmId);
//                context.startActivity(editIntent);
//            }
//        });
    }

    private boolean parseBoolean(Cursor cursor, String column){
        return parseInt(cursor, column) == 1;
    }

    private int parseInt(Cursor cursor, String column) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(column));
    }

    private void setDayActive(Context context, View v, int id, boolean active){
        ((TextView) v.findViewById(id)).setTextColor(
                ContextCompat.getColor(context, active ? on : R.color.off_bright));
    }
}
