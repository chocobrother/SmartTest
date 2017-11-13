package kr.ac.sch.se.Data_Transmission;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sun on 2016-11-06.
 */
public class Packet_Parsing implements PacketAnalysis, Runnable {
    private final String TAG = "PACKET_PARSING";
    // parsing
    // private TextFileWrite<Double> textFile;
    // private int id, id2;
    private int packetId;
    private double value;

    // respones
    public static final int RES_ECG_ID = 50;
    public static final int RES_BMI_ID = 55;
    public static final int RES_BODY_FAT_ID = 53;
    public static final int RES_BODY_FAT_PERCENTAGE_ID = 54;
    public static final int RES_WEIGHT_ID = 52;

    private static final int START_HIGH_VALUE1 = 98;
    private static final int START_HIGH_VALUE2 = 97;
    private static final int END_HIGH_VALUE1 = 96;
    private static final int END_HIGH_VALUE2 = 95;
    private static final int ZERO_VALUE = 99;

    public static final int RES_HEART_RATE = 101;

    private BlockingQueue<Byte> queue;
    private Handler mhandler;

    boolean Run = true;

    public Packet_Parsing(BlockingQueue<Byte> r, Handler mhandler) {
        queue = r;
        value = 0;
        this.mhandler = mhandler;
    }

    public void SetRunning(boolean state){
        Run = state;
    }


    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
//            byte[] packet;
            ArrayList<Byte> packet;
            byte data = 0, data1 = 0;
            int n = 0;
            boolean FIRST_SEQ = true;

            while (true) {
                packet = new ArrayList<>();
                while(true){
                    data = queue.take();
                    if (FIRST_SEQ && data == START_HIGH_VALUE1) {
                        data = queue.take();
                        if(data == START_HIGH_VALUE2){
                            packet.add((byte)START_HIGH_VALUE1);
                            packet.add((byte)START_HIGH_VALUE2);
                            break;
                        }
                    }
                }

                //payload length, message id
                data = queue.take();
                packet.add((byte)data);
                data = queue.take();
                packet.add((byte)data);

                ArrayList<Byte> tmp = new ArrayList<>();

                int tmpIdx = 0;
                byte tmpValue = 0;

                while (true) {
                    data = queue.take();
                    tmp.add((byte)data);

                    if(tmpValue == END_HIGH_VALUE1 && data == END_HIGH_VALUE2){
                        break;
                    }
                    tmpValue = data;
                }

                for(int n1 = 0; n1 < tmp.size(); n1++){
                    if(tmp.get(n1) == ZERO_VALUE && tmp.get(n1+1) == ZERO_VALUE){
                        packet.add((byte)0);
                        n1++;
                    }else{
                        packet.add((byte)tmp.get(n1));
                    }
                }

                n = 0;
                //패킷을 하나 만들면 파싱을 거쳐서 데이터 획득
                if(packet.size() > 5) {
                    parsing(packet);
                }
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    @Override
    public boolean parsing(ArrayList<Byte> packet) {
        if (packet != null) {
            if(packet.get(3) == RES_ECG_ID){
                value = (100 * packet.get(4)) + packet.get(5);

                Message msg = mhandler.obtainMessage(packet.get(3), (int)value, 0);
                mhandler.sendMessage(msg);
            }else if(packet.get(3) == RES_WEIGHT_ID){
                value = (100 * packet.get(4)) + packet.get(5);

                Message msg = mhandler.obtainMessage(packet.get(3), (int)value, 0);
                mhandler.sendMessage(msg);
            }else if(packet.get(3) == RES_BMI_ID){
                value = (100 * packet.get(4)) + packet.get(5);

                Message msg = mhandler.obtainMessage(packet.get(3), (int)value, 0);
                mhandler.sendMessage(msg);
            }
        }
        return true;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public byte[] make(int id, int body) {
        return null;
    }

    public int getId() {
        return packetId;
    }

    public double getValue() {
        return value;
    }


}