package kr.ac.sch.se.Common;

import java.util.ArrayList;

/**
 * Created by sun on 2017-01-11.
 */
public class RecordDatas_Info {
    private ArrayList<RecordData> recordDatas;

    public int getTotalCont() {
        return totalCont;
    }

    public void setTotalCont(int totalCont) {
        this.totalCont = totalCont;
    }

    private int totalCont;

    public RecordDatas_Info(){
        this.recordDatas = new ArrayList<>();
        this.totalCont = 0;
}

    public void put(String wDate,String weight,String bmi,String fatMass,String fatPer,String arrhythmia){
        RecordData data = new RecordData(wDate, weight, bmi, fatMass, fatPer, arrhythmia);
        recordDatas.add(data);
    }

    public RecordData get(int index){
        if(recordDatas.size() > index){
            return recordDatas.get(index);
        }else{
            return null;
        }
    }
}

