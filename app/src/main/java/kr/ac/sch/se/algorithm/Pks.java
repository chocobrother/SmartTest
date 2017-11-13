package kr.ac.sch.se.algorithm;

import java.util.ArrayList;

// Peak value
public class Pks {

    private double[] pks;

    public Pks() {
    }

    public double[] getPks() {
        return pks;
    }

    public void setPks(double[] pks) {
        this.pks = pks;
    }

    public ArrayList<Double> getPksList() {
        ArrayList<Double> arrayList = new ArrayList<Double>(pks.length);
        for (double data : pks) {
            arrayList.add(data);
        }
        return arrayList;
    }

    public void setPks(ArrayList<Double> arrayList) {
        pks = new double[arrayList.size()];
        for (int i = 0; i < pks.length; ++i) {
            pks[i] = arrayList.get(i);
        }
    }

    public int getSize() {
        return pks.length;
    }

}
