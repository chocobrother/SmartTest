package kr.ac.sch.se.algorithm;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

import kr.ac.sch.se.Common.TextFileWrite;
import kr.ac.sch.se.Data_Transmission.Packet_Make;
import kr.ac.sch.se.Data_Transmission.Packet_Parsing;

/**
 * Created by sun on 2017-02-04.
 */
public class ECG_Signal implements Runnable {
    private final String TAG = "HEART_RATE_THREAD";

    private final int GoodSignal = 1, BadSignal = 2;

    public static final int GOOD_STATE = 40;
    public static final int BAD_STATE = 41;
    public static final int NONE_STATE = 42;
    public static final int START_TIME = 43;
    public static final int STOP_TIME = 44;

    private String Good, Bad, None;
    boolean Run = true;

    long startTime, currentTime, signaTestTime;
    long checkTime = 0;
    boolean signalClean = false;
    boolean cleanSignalFlag = false;

    boolean singalCheckFlag = true;

    String afDetection = "No";
    ArrayList<Double> full_signal, clean_signal;
    //원형큐처럼
    int[] heartRateArray;
    int hrArrayIdx = 0;
    int fs_min = 50;

    peakDetection peak;
    //    hrThread hr;//
//    ECG_Manage_Thread dataMange;

    ArrayList<Double> avgHr = new ArrayList<Double>();
    double[] a, b;
    ArrayList<Double> A = new ArrayList<Double>();
    ArrayList<Double> B = new ArrayList<Double>();
    Handler mHandler;

    TextFileWrite<Double> textFile;
    TextFileWrite<Double> textFile1;
    int fileNumber = 0, fileNumber1 = 0;

    public ECG_Signal(Handler mHandler) {
        full_signal = new ArrayList<Double>();
        clean_signal = new ArrayList<Double>();
        heartRateArray = new int[2];
        this.mHandler = mHandler;

        a = new double[7];
        b = new double[7];

        a[0] = 0.0614;
        a[1] = 0.0;
        a[2] = -0.1841;
        a[3] = 0.0;
        a[4] = 0.1841;
        a[5] = 0.0;
        a[6] = -0.0614;

        b[0] = 1.0;
        b[1] = -2.3648;
        b[2] = 2.9556;
        b[3] = -2.4505;
        b[4] = 1.4848;
        b[5] = -0.5514;
        b[6] = 0.1108;

        for (int n = 0; n < a.length; n++)
            A.add(a[n]);
        for (int n = 0; n < b.length; n++)
            B.add(b[n]);


        textFile = new TextFileWrite<Double>("ecg_section_data");
        textFile1 = new TextFileWrite<Double>("ecg_section_data1");
    }

    public void clearCleanSiganl() {
        this.clean_signal.clear();
    }

    public void setSignalAdd(double value) {
        full_signal.add(value);

        if (cleanSignalFlag) {
            clean_signal.add(value);
        }
    }

    public void start() {
        startTime = System.currentTimeMillis();
        signaTestTime = System.currentTimeMillis();
    }

    public double getAvgHr() {
        double tmp = 0;
        for (int n = 0; n < avgHr.size(); n++)
            tmp += avgHr.get(n);

        tmp /= avgHr.size();

        return tmp;
    }

    public void interrupt() {
//        hr.interrupt();
//        dataMange.interrupt();
    }

    public String afDetection() { // 심방 세동 감지 (부정맥?)
        double[] data = new double[65 * 10];

        if (clean_signal.size() < 65 * 10) {
            return "--";
        }
        for (int n = 0; n < (10 * 65); n++) {
            data[n] = clean_signal.get(n);
        }

        data = bandpassfilter.calculation(b, a, data, data.length);
        data = cus_math.max_division(data);

        peakDetection peak = new peakDetection(data, 60);

        peak.calculation();

        ArrayList<Integer> tmp = peak.getRpeak();

        double[] rr = new double[tmp.size() - 1];
        double[] rri = new double[rr.length];
        //diff
        for (int n = 0; n < rr.length; n++) {
            rr[n] = tmp.get(n + 1) - tmp.get(n);
        }

        for (int n = 0; n < rr.length; n++) {
            rri[n] = rr[n] / 60;
        }

        //rmssd
        double dd = 0;
        for (int n = 0; n < rri.length - 1; n++) {
            dd += Math.pow(rri[n + 1] - rri[n], 2);
        }

        double rmssd = Math.sqrt(dd / rri.length);

        Log.e(TAG, "rmssd: " + rmssd);

        if (rmssd > 0.13) {
            afDetection = "yes";
        } else {
            afDetection = "no";
        }

        return afDetection;
    }

    public double hearRate(ArrayList<Integer> data, int fs) {
        int[] rr = cus_math.RR_interval(data);
        int hr = 0;

        for (int n = 0; n < rr.length; n++) {
            hr += (fs * 60) / rr[n];
//                Log.e(TAG, "HR: " + (fs * 60) / rr[n] + ", RR size:" + rr.length + ", fs:" + fs);
        }
        hr /= rr.length;

        return hr;
    }

    public boolean isSignalClean() {// true 면 사람 올라온 이벤트
        if (full_signal.size() > (fs_min * 2)) {
            textFile.TextFileInit(textFile.getPath(), "ecg" + (fileNumber++));

            double[] data = new double[full_signal.size()];

            //이과정에서도 full_signal에 데이터가 들어가고 있으므로 data.length를 사용해야 함
            for (int n = 0; n < data.length; n++) {
                data[n] = full_signal.get(n);
                textFile.add(data[n]);
            }

            int fs = data.length / 2;

            double[] ecg_h = bandpassfilter.calculation(b, a, data, data.length);

            ecg_h = cus_math.max_division(ecg_h);

            peakDetection peak = new peakDetection(ecg_h, fs);

            peak.calculation();

            ArrayList<Integer> tmp = peak.getRpeak();

            double hr = 0;
            if (tmp.size() > 1) {
                hr = hearRate(tmp, fs);
                signalHeartRateySendMessage((int) hr);
//                Log.e(TAG, "Heart Rate " + hr);
                heartRateArray[hrArrayIdx++] = (int) hr;

                if (hrArrayIdx == 2) {
                    hrArrayIdx = 0;
                }

                double tmp1 = heartRateArray[0] - heartRateArray[1];
                Log.e(TAG, "Heart Rate " + hr + ", heartRateArray[0]:" + heartRateArray[0] + ",heartRateArray[1]" + heartRateArray[1]);
                if (hr > 150 || hr < 50) {
                    return false;
                }

                if (Math.abs(tmp1) > 10) {
                    //bad signal
                    return false;
                } else {
                    //good signal
                    avgHr.add(hr);
                    return true;
                }
            }
        }
        return false;
    }

    public void signalHeartRateySendMessage(int hr) {
        Message msg = new Message();
        msg.what = Packet_Parsing.RES_HEART_RATE;
        msg.arg1 = (int) hr; //good signal
        mHandler.sendMessage(msg);
    }

    public void signalQualitySendMessage(int signalQuality) {
        Message msg = mHandler.obtainMessage();
        msg.what = Packet_Make.SEND_SIGNAL_QUALITY_ID;
        msg.arg1 = signalQuality; //good signal
        mHandler.sendMessage(msg);
    }

    public void signalGoodQualitySendMessage() {
        Message msg = mHandler.obtainMessage();
        msg.what = ECG_Signal.GOOD_STATE;
        msg.obj = "Good"; //good signal
        mHandler.sendMessage(msg);
    }


    public void signalBadQualitySendMessage() {
        Message msg = mHandler.obtainMessage();
        msg.what = ECG_Signal.BAD_STATE;
        msg.obj = "Bad"; //good signal
        mHandler.sendMessage(msg);
    }

    public void startTimeSendMessage() {
        Message msg = mHandler.obtainMessage();
        msg.what = ECG_Signal.START_TIME;
        msg.obj = "Start"; //good signal
        mHandler.sendMessage(msg);
    }

    public void stopTimeSendMessage() {
        Message msg = mHandler.obtainMessage();
        msg.what = ECG_Signal.STOP_TIME;

        mHandler.sendMessage(msg);
    }


    @Override
    public void run() {
        boolean start = true;
        while (true) {
            currentTime = System.currentTimeMillis();
            if (currentTime - startTime >= 2000) {
                if (isSignalClean()) {

                    checkTime += 2000;
                    cleanSignalFlag = true;
                    signalQualitySendMessage(GoodSignal);

                    signalGoodQualitySendMessage();

                    startTimeSendMessage();

                } else {
                    cleanSignalFlag = false;
                    checkTime = 0;
                    signalQualitySendMessage(BadSignal);
                    signalBadQualitySendMessage();

                   stopTimeSendMessage();

                }
                full_signal.clear();
                startTime = currentTime;
//                  start = false;
            }

        }
    }
}





