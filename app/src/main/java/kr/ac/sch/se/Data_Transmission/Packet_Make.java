package kr.ac.sch.se.Data_Transmission;

import java.util.ArrayList;

/**
 * Created by sun on 2016-11-06.
 */
public class Packet_Make implements PacketAnalysis {

    //request and send message id
    public static final int REQ_HAND_ECG_ID = 10;
    public static final int REQ_FOOT_ECG_ID = 11;
    public static final int REQ_WEIGHT_ID = 12;
    public static final int REQ_BODY_FAT_ID = 13;
    public static final int REQ_BDOY_FAT_PERCENTAGE_ID = 14;
    public static final int REQ_BMI_ID = 15;
    public static final int REQ_ECG_END_ID = 16;
    public static final int REQ_ECG_REVERSAL = 17;
    public static final int REQ_ECG_NOISE = 18;

    //send
    public static final int SEND_AGE_ID = 31;
    public static final int SEND_HEIGHT_ID = 30;
    public static final int SEND_GENDER_ID = 32;

    public static final int SEND_SIGNAL_QUALITY_ID = 33;
    public static final int SEND_ARRHTHMIA_ID = 34;
    public static final int SEND_HR_ID = 35;

    public static final int PAYLOADLENGTH = 3;

    @Override
    public byte[] make(int id, int body) {
        ArrayList<Byte> bufferList = new ArrayList<Byte>();;

        bufferList.add( ( new Byte((byte)(98))));
        bufferList.add( ( new Byte((byte)(97))));
        bufferList.add(( new Byte((byte)(PAYLOADLENGTH))));

        //message id
        switch(id){
            case SEND_AGE_ID:
                bufferList.add(( new Byte((byte)(SEND_AGE_ID))));
                break;
            case SEND_HEIGHT_ID :
                bufferList.add(( new Byte((byte)(SEND_HEIGHT_ID))));
                break;
            case SEND_GENDER_ID :
                bufferList.add(( new Byte((byte)(SEND_GENDER_ID))));
                break;
            case REQ_BODY_FAT_ID:
                bufferList.add(( new Byte((byte)(REQ_BODY_FAT_ID))));
                break;
            case REQ_BMI_ID:
                bufferList.add(( new Byte((byte)(REQ_BMI_ID))));
                break;
            case REQ_BDOY_FAT_PERCENTAGE_ID:
                bufferList.add(( new Byte((byte)(REQ_BDOY_FAT_PERCENTAGE_ID))));
                break;
            case REQ_ECG_END_ID:
                bufferList.add(( new Byte((byte)(REQ_ECG_END_ID))));
                break;
            case REQ_HAND_ECG_ID:
                bufferList.add(( new Byte((byte)(REQ_HAND_ECG_ID))));
                break;
            case REQ_FOOT_ECG_ID:
                bufferList.add(( new Byte((byte)(REQ_FOOT_ECG_ID))));
                break;
            case REQ_WEIGHT_ID:
                bufferList.add(( new Byte((byte)(REQ_WEIGHT_ID))));
                break;
            case REQ_ECG_REVERSAL:
                bufferList.add(( new Byte((byte)(REQ_ECG_REVERSAL))));
                break;
            case REQ_ECG_NOISE:
                bufferList.add(( new Byte((byte)(REQ_ECG_NOISE))));
                break;
            case SEND_SIGNAL_QUALITY_ID:
                bufferList.add(( new Byte((byte)(SEND_SIGNAL_QUALITY_ID))));
                break;
            case SEND_HR_ID:
                bufferList.add(( new Byte((byte)(SEND_HR_ID))));
                break;
            case SEND_ARRHTHMIA_ID:
                bufferList.add(( new Byte((byte)(SEND_ARRHTHMIA_ID))));
                break;
        }
        //message body
        if(body/100 == 0){
            bufferList.add(( new Byte((byte)(99))));
            bufferList.add(( new Byte((byte)(99))));
        }else{
            bufferList.add( ( new Byte((byte)(body/100))));
        }
        if(body%100 == 0){
            bufferList.add(( new Byte((byte)(99))));
            bufferList.add(( new Byte((byte)(99))));
        }else{
            bufferList.add( ( new Byte((byte)(body%100))));
        }

        int checksum = 0;
        for(int n =3 ; n < bufferList.size(); n++){
            checksum ^= bufferList.get(n);
        }

        if(checksum/100 == 0){
            bufferList.add(( new Byte((byte)(99))));
            bufferList.add(( new Byte((byte)(99))));
        }else{
            bufferList.add( ( new Byte((byte)(checksum/100))));
        }
        if(checksum%100 == 0){
            bufferList.add(( new Byte((byte)(99))));
            bufferList.add(( new Byte((byte)(99))));
        }else{
            bufferList.add( ( new Byte((byte)(checksum%100))));
        }

        //end sequence
        bufferList.add( ( new Byte((byte)(96))));
        bufferList.add( ( new Byte((byte)(95))));

        byte[] sendBuffer = new byte[bufferList.size()];
        for(int n =0 ; n< bufferList.size(); n++){
            sendBuffer[n] = bufferList.get(n);
        }
        return sendBuffer;
    }

    @Override
    public boolean parsing(ArrayList<Byte> packet) {
        return false;
    }
}
