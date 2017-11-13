package kr.ac.sch.se.algorithm;

import java.util.ArrayList;
import java.util.LinkedList;

public class cus_math{
    static public double max(double[] v) {
        int length = v.length;
        double tmp = v[0];

        for (int n = 1; n < length; n++) {
            if (v[n] > tmp)
                tmp = v[n];
        }

        return tmp;
    }

    public static double max(double[] v, int s, int e){
        double tmp = v[s];

        for (int n = s; n < e; n++) {
            if (v[n] > tmp)
                tmp = v[n];
        }

        return tmp;
    }

    public static double[] max_division(double[] v){
        double tmp = max(v);
        double[] result = new double[v.length];

        for(int n =0; n < v.length; n++){
            result[n] = v[n] / tmp;
        }

        return result;
    }

    static public double[] array_get(double[] a, int sIndex, int lIndex) {

//        System.out.println(sIndex + "," + lIndex + "," +  (lIndex - sIndex));
        double[] result = new double[lIndex - sIndex];

        for (int n = 0, i = sIndex; n <= ((lIndex - 1) - sIndex); n++, i++) {

            result[n] = a[i];
        }

        return result;
    }

    public static double mean(double[] v, int s, int e){
        double tmp = 0;

        for (int n = s; n < e; n++) {
            tmp += v[n];
        }
        tmp /= e-s;

        return tmp;
    }

    public static double mean(double[] v){
        double tmp = 0;

        for(int n = 0; n < v.length; n++){
            tmp += v[n];
        }

        tmp /= v.length;

        return tmp;
    }

//    public static <T> void writeData(T[] data, String name){
//        File textFile = new File(name+".txt");
//        try {
//            BufferedWriter out = new BufferedWriter(new FileWriter(textFile));
//
//            for(int n = 0; n < data.length; n++){
//                out.append(data[n] + "\r\n");
//                out.flush();
//            }
//            out.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }

    public static Double[] getDoubleArray(double[] data){
        Double[] result = new Double[data.length];
        
        for(int n = 0; n < result.length; n++){
            result[n] = data[n];
        }

        return result;
    }

    public static Integer[] getIntegerArray(int[] data){
        Integer[] result = new Integer[data.length];

        for(int n = 0; n < result.length; n++){
            result[n] = data[n];
        }

        return result;
    }

    public static double[] maFilter(double[] data, int window_size) {
        MovingAverage_Filter ma_Filter = new MovingAverage_Filter(
                window_size);
        LinkedList<Double> filteredList = new LinkedList<Double>();

        for (int n = 0; n < data.length; n++) {
            ma_Filter.newNum(data[n]);
            filteredList.add(ma_Filter.getAvg());
        }

        double[] result = new double[filteredList.size()];

        for(int n = 0; n < result.length; n++){
            result[n] = filteredList.get(n);
        }

        return result;
    }

    //derivative filter h_d * ecg_d
    public static double[] conv(double[] a, double[] b){
        int length = b.length+a.length-1;
        double[] result = new double[length];
        int i = 0, j = 0, il = 0;
        double tmp = 0;

        for(i = 0; i < length; i++){
            il = i;
            tmp = 0.0;
            for(j=0; j < b.length; j++){
                if(il >= 0 && il < a.length)
                    tmp = tmp+(a[il] * b[j]);
                il = il-1;
                result[i] = tmp;
            }
        }
        
        return result;
    }

    public static double[] squaring(double[] data){
        double[] result = new double[data.length];

        for(int n = 0; n < data.length; n++){
            result[n] = Math.pow(data[n], 2);
        }

        return result;
    }

    public static int[] RR_interval(ArrayList<Integer> data){

        int[] rr = new int[data.size()-1];

        for(int n = 0; n < data.size()-1; n++){
            rr[n] = data.get(n+1) - data.get(n);
        }

        return rr;
    }

    public static double getStd(ArrayList<Double> data) {
        double mean = 0;
        double std = 0;

        for (int n = 0; n < data.size(); n++) {
            mean += data.get(n);
        }

        for (int n = 0; n < data.size(); n++) {
            std += Math.pow(data.get(n) - mean, 2);
        }
        std = Math.sqrt(std / data.size());

        return std;
    }

    public static double getStd(double[] data) {
        double meanValue = mean(data);
        double std=0;
        for(int n=0; n < data.length; n++){
            std += Math.pow(data[n] - meanValue, 2);
        }

        std = Math.sqrt(std / data.length);

        return std;
    }
}

