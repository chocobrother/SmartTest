package kr.ac.sch.se.algorithm;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

public class peakDetection{
    private double[] ecg_h;
    private double THR_SIG;
    private double THR_NOISE;
    private double SIG_LEV;
    private double NOISE_LEV;
    private double THR_SIG1;
    private double THR_NOISE1;
    private double SIG_LEV1;
    private double NOISE_LEV1;
 
    private int fs;
    private LinkedList<Double> rPeaks;
    private Locs locs;

    private ArrayList<Integer> qrs_i_raw = new ArrayList<Integer>();
    private ArrayList<Double> qrs_amp_raw = new ArrayList<Double>();

    public peakDetection(double[] signal, int fs){
        this.ecg_h = signal;
        this.fs = fs;
    }

    public ArrayList<Integer> getRpeak(){
        return qrs_i_raw;
    }

    public void findPeaks(double[] data, int min_heigth, int min_peak_distance){
         //findpeak
        Peak peak = new Peak();
        locs = new Locs();
        Pks pks = new Pks();
        rPeaks = new LinkedList<Double>();

        //200msec distance
        FindPeaks.parse_inputs(peak, data, min_heigth, min_peak_distance);
        FindPeaks.getPeaksAboveMinPeakHeight(peak, pks, locs);
        FindPeaks.removePeaksBelowThreshold(peak, pks, locs);
        FindPeaks.removePeaksSeparatedByLessThanMinPeakDistance(peak, pks, locs);
        FindPeaks.orderPeaks(peak, pks, locs);
        FindPeaks.keepAtMostNpPeaks(peak, pks, locs);

        rPeaks.addAll(pks.getPksList());
    }

    public void calculation(){
        //derivative filter
        double[] ecg_d = cus_math.conv(new double[]{-0.1250, -0.2500, 0, 0.2500, 0.1250}, ecg_h);
        ecg_d = cus_math.max_division(ecg_d);

        //Squaring
        double[] ecg_s = cus_math.squaring(ecg_d);
        
        //moving average filter
        double[] ecg_m = cus_math.maFilter(ecg_s, (int)(0.15*fs));
   
        //findpeaks
        findPeaks(ecg_m, Constants.MIN_PEAK_HEIGHT, (int)Math.round(0.2*fs));

        THR_SIG = cus_math.max(ecg_m, 0, 2*fs) * 1/3;
        THR_NOISE = cus_math.mean(ecg_m, 0, 2*fs) * 1/2;
        SIG_LEV = THR_SIG;
        NOISE_LEV = THR_NOISE;

        THR_SIG1 = cus_math.max(ecg_h, 0, 2*fs) * 1/3;
        THR_NOISE1 = cus_math.mean(ecg_h, 0, 2*fs) * 1/2;
        SIG_LEV1 = THR_SIG;
        NOISE_LEV1 = THR_NOISE;

        //fix

        ArrayList<Integer> locsList = locs.getLocsList();
        rPeaks.remove(0);
        locsList.remove(0);

//        cus_math.<Double>writeData(cus_math.getDoubleArray(ecg_h), "ecg_h");
//        cus_math.<Double>writeData(rPeaks.toArray(new Double[rPeaks.size()]), "peak");
//        cus_math.<Integer>writeData(locsList.toArray(new Integer[locsList.size()]), "locs");
//        cus_math.<Double>writeData(cus_math.getDoubleArray(ecg_m), "ecg_m");
//        cus_math.<Double>writeData(cus_math.getDoubleArray(ecg_s), "ecg_s");

        int x_i = 0;
        double y_i = 0;
        boolean ser_back = false;
        int skip = 0;
        //rpeak Amplitude
        ArrayList<Double> qrs_c = new ArrayList<Double>();
        ArrayList<Integer> qrs_i = new ArrayList<Integer>();

//        Log.e("SDADAS", "peakDetection" + rPeaks.size());

        for(int i = 0; i < rPeaks.size(); i++){
            //locate the corresponding peak in the filtered signal
            if(locsList.get(i) - Math.round(0.15*fs) >= 0 && locsList.get(i) <= ecg_h.length){
                double[] tmp = cus_math.array_get(ecg_h, locsList.get(i)-(int)Math.round(0.15*fs), locsList.get(i)); 
                double dd = tmp[0];

                for(int n = 0; n < tmp.length; n++){
                   if( tmp[n] > dd){
                        x_i = n;
                        dd = tmp[n];
                   }
                }
                y_i = dd;
                //System.out.println(y_i+","+x_i);
            }else{
                if( i == 0 ){
                    double[] tmp = cus_math.array_get(ecg_h, 0, locsList.get(i));

                    double dd = tmp[0];
                    for(int n = 0; n < tmp.length; n++){
                        if( tmp[n] > dd){
                            x_i = n;
                            dd = tmp[n];
                        }
                    }
                    y_i = dd;
                    ser_back = true;
                }else if(locsList.get(i) >= ecg_h.length){
                    double[] tmp = cus_math.array_get(ecg_h, locsList.get(i)-(int)Math.round(0.15*fs), ecg_h.length );

                    double dd = tmp[0];
                    for(int n = 0; n < tmp.length; n++){
                        if( tmp[n] > dd){
                            x_i = n;
                            dd = tmp[n];
                        }
                    }
                    y_i = dd;

                }
            }

            //find noise and QRS peaks
            if(rPeaks.get(i) >= THR_SIG){
                if(qrs_c.size() >= 3){
                    if(locsList.get(i) - qrs_i.get(qrs_i.size()-1) <= (int)Math.round(0.3600*fs)){
                        
                    }
                }
                // when T wave detected
                if(skip == 0){
                    qrs_c.add(rPeaks.get(i));
                    qrs_i.add(locsList.get(i));

                    //bandpass filter check threshold
                    if(y_i >= THR_SIG1){
                        if(ser_back){
                            qrs_i_raw.add(x_i);
                        }else{
                            qrs_i_raw.add(locsList.get(i)-(int)Math.round(0.15*fs)+(x_i-1)); // save index of bandpass
                        }
                        qrs_amp_raw.add(y_i); 

                        //adjust threshold for bandpass filtered sig
                        SIG_LEV1 = 0.125*y_i + 0.875*SIG_LEV1;
                    }

                    SIG_LEV = 0.125*rPeaks.get(i) + 0.875 * SIG_LEV;
                }
            }else if(THR_NOISE <= rPeaks.get(i) && rPeaks.get(i) < THR_SIG){
                NOISE_LEV1 = 0.125*y_i + 0.875*NOISE_LEV1;
                NOISE_LEV = 0.125*rPeaks.get(i) + 0.875*NOISE_LEV;
            }
            else if(rPeaks.get(i) < THR_NOISE){
                //noise level in filtered signal
                NOISE_LEV1 = 0.125*y_i + 0.875*NOISE_LEV1;
                NOISE_LEV = 0.125*rPeaks.get(i) + 0.875*NOISE_LEV;
            }

            //adjust the threshold with SNR
            if( (NOISE_LEV != 0) || (SIG_LEV != 0) ){
                THR_SIG = NOISE_LEV + 0.25*(Math.abs(SIG_LEV - NOISE_LEV));
                THR_NOISE = 0.5 * (THR_SIG);
            }

            //adjust the threshold with SNR for bandpassed signal
            if( (NOISE_LEV1 != 0) || (SIG_LEV1 != 0) ){
                THR_SIG1 = NOISE_LEV1 + 0.25*(Math.abs(SIG_LEV1 - NOISE_LEV1));
                THR_NOISE1 = 0.5*(THR_SIG1);
            }
        }

        return ;
    }
}
