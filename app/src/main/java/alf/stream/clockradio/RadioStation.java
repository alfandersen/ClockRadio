package alf.stream.clockradio;

/**
 * Created by Alf on 7/8/2017.
 */

class RadioStation {
    private int _id;
    private String _name;
    private String _link;

    public RadioStation(int _id, String _name, String _link) {
        this._id = _id;
        this._name = _name;
        this._link = _link;
    }

    public int get_id() {
        return _id;
    }

    public String get_name() {
        return _name;
    }

    public String get_link() {
        return _link;
    }
}
