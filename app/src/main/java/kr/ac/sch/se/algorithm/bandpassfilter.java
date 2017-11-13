package kr.ac.sch.se.algorithm;

public class bandpassfilter{
    static{
        System.loadLibrary("filtfilt");
    }

    public static native void printTest();

    public static native double[] calculation(double[] b, double[] a, double[] signal, int signal_length);
}
