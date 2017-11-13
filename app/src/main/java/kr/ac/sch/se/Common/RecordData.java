package kr.ac.sch.se.Common;

/**
 * Created by sun on 2017-01-11.
 */
public class RecordData{
    private String wDate;
    private String weight;
    private String bmi;
    private String fatMass;
    private String fatPer;
    private String arrhythmia;

    public RecordData(String wDate,String weight,String bmi,String fatMass,String fatPer,String arrhythmia){
        this.wDate = wDate;
        this.weight = weight;
        this.bmi = bmi;
        this.fatMass = fatMass;
        this.fatPer = fatPer;
        this.arrhythmia = arrhythmia;
    }

    public String getwDate() {
        return wDate;
    }

    public void setwDate(String wDate) {
        this.wDate = wDate;
    }

    public String getBmi() {
        return bmi;
    }

    public void setBmi(String bmi) {
        this.bmi = bmi;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getFatMass() {
        return fatMass;
    }

    public void setFatMass(String fatMass) {
        this.fatMass = fatMass;
    }

    public String getFatPer() {
        return fatPer;
    }

    public void setFatPer(String fatPer) {
        this.fatPer = fatPer;
    }

    public String getArrhythmia() {
        return arrhythmia;
    }

    public void setArrhythmia(String arrhythmia) {
        this.arrhythmia = arrhythmia;
    }
}
