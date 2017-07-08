package alf.stream.clockradio;

/**
 * Created by Alf on 7/7/2017.
 */

public class Alarm {
    private int _id;
    private boolean _active;
    private int _hour;
    private int _minute;
    private boolean _mon, _tue, _wed, _thu, _fri, _sat, _sun;
    private int _station;
    private int _region;
    private int _volume;


    // Constructor

    public Alarm(int _id, boolean _active, int _hour, int _minute, boolean _mon, boolean _tue, boolean _wed, boolean _thu, boolean _fri, boolean _sat, boolean _sun, int _station, int _region, int _volume) {
        this._id = _id;
        this._active = _active;
        this._hour = _hour;
        this._minute = _minute;
        this._mon = _mon;
        this._tue = _tue;
        this._wed = _wed;
        this._thu = _thu;
        this._fri = _fri;
        this._sat = _sat;
        this._sun = _sun;
        this._station = _station;
        this._region = _region;
        this._volume = _volume;
    }


    // Getters

    public int get_id() {
        return _id;
    }

    public boolean is_active() {
        return _active;
    }

    public int get_hour() {
        return _hour;
    }

    public int get_minute() {
        return _minute;
    }

    public boolean get_mon() {
        return _mon;
    }

    public boolean get_tue() {
        return _tue;
    }

    public boolean get_wed() {
        return _wed;
    }

    public boolean get_thu() {
        return _thu;
    }

    public boolean get_fri() {
        return _fri;
    }

    public boolean get_sat() {
        return _sat;
    }

    public boolean get_sun() {
        return _sun;
    }

    public int get_station() {
        return _station;
    }

    public int get_region() {
        return _region;
    }

    public int get_volume() {
        return _volume;
    }
}
