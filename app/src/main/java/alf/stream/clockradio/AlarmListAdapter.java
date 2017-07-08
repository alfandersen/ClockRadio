package alf.stream.clockradio;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Alf on 7/8/2017.
 */

class AlarmListAdapter extends ArrayAdapter<Alarm> {

    private String[] stationNames, regionNames;

    public AlarmListAdapter(@NonNull Context context, Alarm[] alarms) {
        super(context, R.layout.alarm_row, alarms);
        stationNames = context.getResources().getStringArray(R.array.station_names);
        regionNames = context.getResources().getStringArray(R.array.region_names);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View alarmView = inflater.inflate(R.layout.alarm_row, parent, false);

        final Alarm alarm = getItem(position);
        if(alarm != null) {
            // Time
            ((TextView) alarmView.findViewById(R.id.timeTextView_AlarmRow))
                    .setText(alarm.get_hour() + ":" + alarm.get_minute());

            // Station
            String station = stationNames[alarm.get_station()];
            // TODO: Fix hardcoded 4 (P4)
            if(alarm.get_station() == 4)
                station = station+" "+regionNames[alarm.get_region()];

            ((TextView) alarmView.findViewById(R.id.stationTextView_AlarmRow)).setText(station);

            // Is Active
            ((CheckBox) alarmView.findViewById(R.id.activeCheckBox_AlarmRow))
                .setChecked(alarm.is_active());

            // Days
            setDayActive(alarmView, R.id.mon_AlarmRow, alarm.get_mon());
            setDayActive(alarmView, R.id.tue_AlarmRow, alarm.get_tue());
            setDayActive(alarmView, R.id.wed_AlarmRow, alarm.get_wed());
            setDayActive(alarmView, R.id.thu_AlarmRow, alarm.get_thu());
            setDayActive(alarmView, R.id.fri_AlarmRow, alarm.get_fri());
            setDayActive(alarmView, R.id.sat_AlarmRow, alarm.get_sat());
            setDayActive(alarmView, R.id.sun_AlarmRow, alarm.get_sun());

            // Delete button
            ((ImageView) alarmView.findViewById(R.id.deleteImageView_AlarmRow)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parent.removeView(alarmView);
                    new DataBaseHandler(getContext(), DataBaseHandler.DATABASE_NAME, null, DataBaseHandler.DATABASE_VERSION)
                            .deleteAlarm(alarm);
                }
            });

            // Layout
            alarmView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent editIntent = new Intent(getContext(),EditAlarmActivity.class);
                    editIntent.putExtra(getContext().getString(R.string.alarm_id),alarm.get_id());
                    getContext().startActivity(editIntent);
                }
            });
        }
        return super.getView(position,convertView,parent);
    }

    private void setDayActive(View v, int id, boolean active){
        ((TextView) v.findViewById(id)).setTextColor(
                getContext().getResources().getColor(active ? R.color.colorAccent : R.color.off_bright));
    }
}
