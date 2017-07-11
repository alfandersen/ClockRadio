package alf.stream.clockradio;

import android.util.SparseArray;

import java.util.ArrayList;

/**
 * Created by Alf on 7/11/2017.
 */

public class IdList<T> extends ArrayList<T> {
    private SparseArray<T> elements;

    public IdList() {
        elements = new SparseArray<>();
    }

    public void add(Integer id, T element){
        elements.append(id, element);
        super.add(element);
    }

    @Override
    public T get(int id){
        return elements.get(id);
    }

    public T getPos(int pos){
        return super.get(pos);
    }
}
