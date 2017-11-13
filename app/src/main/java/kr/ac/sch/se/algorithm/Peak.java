package kr.ac.sch.se.algorithm;

// Peak information
public class Peak {

    private double[] X;
    private double Ph;
    private long Pd;
    private double Th;
    private int Np;
    private String Str;
    private boolean[] infldx;

    public Peak() {
    }

    public void setX(double[] X) {
        this.X = X;
    }

    public void setX(int index, double data) {
        X[index] = data;
    }

    public void setPh(double Ph) {
        this.Ph = Ph;
    }

    public void setPd(long Pd) {
        this.Pd = Pd;
    }

    public void setTh(double Th) {
        this.Th = Th;
    }

    public void setNp(int Np) {
        this.Np = Np;
    }

    public void setStr(String Str) {
        this.Str = Str;
    }

    public void setinfldx(boolean[] infldx) {
        this.infldx = infldx;
    }

    public double[] getX() {
        return X;
    }

    public double getX(int index) {
        return X[index];
    }

    public double getPh() {
        return Ph;
    }

    public long getPd() {
        return Pd;
    }

    public double getTh() {
        return Th;
    }

    public int getNp() {
        return Np;
    }

    public String getStr() {
        return Str;
    }

    public boolean[] getinfldx() {
        return infldx;
    }

}
