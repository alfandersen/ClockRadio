package alf.stream.clockradio;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by Alf on 7/11/2017.
 */

public class StationAdapter extends ArrayAdapter<RadioStation> {
    List<RadioStation> stations;

    public StationAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<RadioStation> objects) {
        super(context, resource, textViewResourceId, objects);
        stations = objects;
    }

    @Override
    public long getItemId(int position) {
        return stations.get(position).get_id();
    }

    public int getIdPosition(int id){
        for(int i = 0; i < this.getCount(); i++){
            if(this.getItem(i).get_id() == id)
                return i;
        }
        return -1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}