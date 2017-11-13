package kr.ac.sch.se.algorithm;

import java.util.ArrayList;

// Peak index
public class Locs {

    private int[] locs;

    public Locs() {
    }

    public int[] getLocs() {
        return locs;
    }

    public void setLocs(int[] locs) {
        this.locs = locs;
    }

    public ArrayList<Integer> getLocsList() {
        ArrayList<Integer> arrayList = new ArrayList<Integer>(locs.length);
        for (int data : locs) {
            arrayList.add(data);
        }
        return arrayList;
    }

    public void setLocs(ArrayList<Integer> arrayList) {
        locs = new int[arrayList.size()];
        for (int i = 0; i < locs.length; ++i) {
            locs[i] = arrayList.get(i);
        }
    }

}
