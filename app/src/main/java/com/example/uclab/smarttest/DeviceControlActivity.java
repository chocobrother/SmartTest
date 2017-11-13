package com.example.uclab.smarttest;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import kr.ac.sch.se.Common.TextFileWrite;
import kr.ac.sch.se.Data_Transmission.PacketAnalysis;
import kr.ac.sch.se.Data_Transmission.Packet_Make;
import kr.ac.sch.se.Data_Transmission.Packet_Parsing;
import kr.ac.sch.se.algorithm.ECG_Signal;
import kr.ac.sch.se.bluetooth.BluetoothLeService;
import kr.ac.sch.se.bluetooth.SampleGattAttributes;

public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    //
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private static final int TIME_OUT = 2001;
    private static final int REWIND = 2002;
    private static final int SHUTDOWN = 2003;

    private TextView isSerial;
    private TextView elpaseTimeTextView, weightTextView, hrTextView, signalQualityTextView;

    private String mDeviceName;
    private String mDeviceAddress;
    private Packet_Parsing packet;

    //  private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;

    private Button sendBtn, stopBtn;
    private Thread sendPacket, elapseTimeThread, packetThread, ecg_signalThread;

    AlertDialog alert;

    private ECG_Signal ecg_signal;

    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;
    private TextFileWrite<Double> textFile;

    public final static UUID HM_RX_TX =
            UUID.fromString(SampleGattAttributes.HM_RX_TX);

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private String currentSignal = null;

    public boolean timeCountFlag = true;
    private int height = 173, gender = 1, age = 29;
    private int elaps_time = 50;
    private double weight_data, bmi_data, body_fat, body_fat_percentage;

    private PacketAnalysis packetMake = new Packet_Make();
    private BlockingQueue<Byte> packetQueue = new LinkedBlockingQueue<Byte>(1024);


    //
    private boolean goodStartFlag = false;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            final int value = msg.arg1;
            String state_temp = null;

            switch (msg.what) {
                case Packet_Parsing.RES_WEIGHT_ID:
                    weightTextView.setText(value+"kg");
                    break;
                case Packet_Parsing.RES_ECG_ID:
                    textFile.add((double)value);
                    series.appendData(new DataPoint(lastX++, value), true, 150);
                    if(ecg_signal != null)
                        ecg_signal.setSignalAdd(value);
                    break;
                case Packet_Make.SEND_SIGNAL_QUALITY_ID:
                    if(msg.arg1 == 1) {
                        signalQualityTextView.setText(getString(R.string.measure_signal_good_quality));
                        signalQualityTextView.setTextColor(Color.GREEN);
                        ble_send_no_thread(packetMake.make(Packet_Make.SEND_SIGNAL_QUALITY_ID, 1));

                        //graph color
                        series.setColor(Color.GREEN);
                    }else {
                        signalQualityTextView.setText(getString(R.string.measure_signal_bad_quality));
                        signalQualityTextView.setTextColor(Color.RED);
                        ble_send_no_thread(packetMake.make(Packet_Make.SEND_SIGNAL_QUALITY_ID, 2));

                        //graph color
                        series.setColor(Color.RED);
                    }
                    break;
                case Packet_Parsing.RES_HEART_RATE:
                    hrTextView.setText(value+"bpm");
//                    ble_send(packetMake.make(Packet_Make.SEND_HR_ID, value));
                    break;
                case Packet_Parsing.RES_BMI_ID:
                    bmi_data = value;
                    break;
//주환
                case ECG_Signal.GOOD_STATE:
                    currentSignal = msg.obj.toString();
                    break;

                case ECG_Signal.BAD_STATE:
                    currentSignal = msg.obj.toString();
                    break;

                case ECG_Signal.NONE_STATE:
                    break;

                case ECG_Signal.START_TIME:

                    if(goodStartFlag == false) {

                        ble_send(packetMake.make(Packet_Make.REQ_WEIGHT_ID, 0));

                        goodStartFlag = true;


                        elapseTimeThread = new Thread(new ElapseTimeThread());
                        elapseTimeThread.start();

                    }
                    ReStart();
                    break;

                case ECG_Signal.STOP_TIME:
                    TimeStop();
                    break;



                case TIME_OUT:
//                    String signal = currentSignal;
                    String signal = ecg_signal.afDetection();
                    if(signal.equals("no")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceControlActivity.this).setTitle("결과").setIcon(R.mipmap.ic_launcher).setMessage("정상 입니다.");

                        alert =  builder.create();
                        alert.show();

                        final Handler handler = new Handler();
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if(alert.isShowing()){
                                    alert.dismiss();
                                    elaps_time = 50;
                                    elpaseTimeTextView.setText(elaps_time+"s");

                                    ecg_signal.clearCleanSiganl();
                                }
                            }
                        };

                        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                handler.removeCallbacks(runnable);
                            }
                        });

                        handler.postDelayed(runnable,3000);

                    }
                    else if(signal.equals("yes")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceControlActivity.this).setTitle("결과").setIcon(R.mipmap.ic_launcher).setMessage("부정맥증상 존재합니다.");

                        alert =  builder.create();
                        alert.show();

                        final Handler handler = new Handler();
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if(alert.isShowing()){
                                    alert.dismiss();
                                    elaps_time = 50;
                                    elpaseTimeTextView.setText(elaps_time+"s");

                                    ecg_signal.clearCleanSiganl();
                                }
                            }
                        };

                        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                handler.removeCallbacks(runnable);
                            }
                        });

                        handler.postDelayed(runnable,3000);
                    }
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceControlActivity.this).setTitle("결과").setIcon(R.mipmap.ic_launcher).setMessage("데이터가 부족합니다.");

                        alert =  builder.create();
                        alert.show();

                        final Handler handler = new Handler();
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if(alert.isShowing()){
                                    alert.dismiss();
                                    elaps_time = 50;
                                    elpaseTimeTextView.setText(elaps_time+"s");
                                    ecg_signal.clearCleanSiganl();
                                }
                            }
                        };

                        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                handler.removeCallbacks(runnable);
                            }
                        });

                        handler.postDelayed(runnable,3000);
                    }
                    break;
            }
        }
    };

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            Log.e(TAG, "ddddddddd들어왔나 연결에");
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getByteArrayExtra(mBluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void clearUI() {
//        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        //graph view
        GraphView graph = (GraphView) findViewById(R.id.graph);
//        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
        //graph.getGridLabelRenderer().dmd
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE); //배경이 숨겨짐

        // data
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);

        // customize a little bit viewport
        Viewport viewport = graph.getViewport();
        viewport.setMinX(0);
        viewport.setMaxX(150);
        viewport.setMinY(0);
        viewport.setMaxY(5000);
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setScrollable(true);
        viewport.setScalable(true);

        textFile = new TextFileWrite<Double>(getString(R.string.textFolderName));
        textFile.TextFileInit(textFile.getPath(), getString(R.string.textFileName));
        elpaseTimeTextView = (TextView)findViewById(R.id.elpaseTimeTextView);
        elpaseTimeTextView.setText(elaps_time+"s");
        weightTextView = (TextView)findViewById(R.id.weightTextView);
        hrTextView = (TextView)findViewById(R.id.hrTextView);
        signalQualityTextView = (TextView)findViewById(R.id.signal_quality);

        sendBtn = (Button)findViewById(R.id.start);
        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startSet();
            }
        });

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Log.e(TAG, "디바이스 이름: "+ mDeviceName);
        Log.e(TAG, "디바이스 주소: " + mDeviceAddress);
        // Sets up UI references.
        // is serial present?
        isSerial = (TextView) findViewById(R.id.isSerial);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {//
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        if(sendPacket != null)
            sendPacket.interrupt();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }//
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mConnectionState.setText(resourceId);
            }
        });
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private boolean isReadableCharacteristic(BluetoothGattCharacteristic chr) {
        if(chr == null) return false;

        final int charaProp = chr.getProperties();
        if((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            //       Logs.d("# Found readable characteristic");
            return true;
        } else {
            //     Logs.d("# Not readable characteristic");
            return false;
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));

            // If the service exists for HM 10 Serial, say so.
            if(SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") { isSerial.setText("Yes, serial :-)"); } else {  isSerial.setText("No, serial :-("); }
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            // get characteristic when UUID matches RX/TX UUID
            characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
            characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void ble_send_no_thread(final byte[] buffer) {
        if(characteristicRX != null)
            characteristicTX.setValue(buffer);

        if(mBluetoothLeService != null)
            mBluetoothLeService.writeCharacteristic(characteristicTX);
    }

    private void ble_send(final byte[] buffer){
        characteristicTX.setValue(buffer);
        mBluetoothLeService.writeCharacteristic(characteristicTX);

        try {
            Thread.sleep(300);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
    public void startSet(){

        sendPacket = new Thread(new packetSendThread());
        sendPacket.start();

        //SQI 처리 및 심박계산
        ecg_signal = new ECG_Signal(mHandler);
        Thread ecg_signalThread = new Thread(ecg_signal);
        ecg_signalThread.start();

        //packet parsing thread
        packet = new Packet_Parsing(packetQueue, mHandler);
        Thread packetThread = new Thread(packet);
        packetThread.start();

//        packet = new Packet_Parsing(packetQueue, mHandler);
//        Thread packetThread = new Thread(packet);
//        packetThread.start();


    }
    private void displayData(final byte[] data) {
        //Thread 생성
        if (data != null) {
            //패킷저장
//            textFile.add(bytesToHex(data));

            for(int n =0 ; n < data.length; n++){
                try {
                    packetQueue.put(data[n]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class BleSendThread extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPostExecute(Void params) {
            return ;
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }



    class packetSendThread implements Runnable{
        private final String TAG = "PACKET_SEND_THREAD";
        private boolean start = true;

        @Override
        public void run() {
            while(true) {
                if (mConnected && start) {
                    mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
                    mBluetoothLeService.readCharacteristic(characteristicRX);

                    //키, 나이, 성별
                    ble_send(packetMake.make(Packet_Make.SEND_HEIGHT_ID, height));
                    ble_send(packetMake.make(Packet_Make.SEND_HEIGHT_ID, height));
                    ble_send(packetMake.make(Packet_Make.SEND_AGE_ID, age));
                    ble_send(packetMake.make(Packet_Make.SEND_GENDER_ID, gender));

                    ble_send(packetMake.make(Packet_Make.REQ_WEIGHT_ID, 0));
                    ble_send(packetMake.make(Packet_Make.REQ_HAND_ECG_ID, 0));
                    ble_send(packetMake.make(Packet_Make.REQ_ECG_NOISE, 0));
                    start = false;


                } else {

                }

            }

        }
    }
    public void TimeStop(){
        timeCountFlag = false;
    }
    public void ReStart(){
        timeCountFlag = true;
    }
    class ElapseTimeThread implements Runnable{

        private int e_time = elaps_time;
        private boolean elapseFlag = true;
       // public boolean timeCountFlag = true;

        @Override
        public void run() {

            if(mConnected) {
                while (elapseFlag) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (timeCountFlag)
                                elpaseTimeTextView.setText((e_time--) + "s");

                            if (e_time <= 0) {

                                e_time = 50;
                                goodStartFlag = false;
                                mHandler.sendEmptyMessage(TIME_OUT); //핸들러 메시지 호출후

                                elapseFlag = false;
                            }
                        }
                        // }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //end
                    if (e_time == 0) {
                        Log.e(TAG, "nnnnnnnnnnnnn");
                        goodStartFlag = false;

                        //stop ecg
                        ble_send(packetMake.make(Packet_Make.SEND_SIGNAL_QUALITY_ID, 3));
                        ble_send(packetMake.make(Packet_Make.REQ_ECG_END_ID, 0));
                        ble_send(packetMake.make(Packet_Make.REQ_BODY_FAT_ID, 0));
                        ble_send(packetMake.make(Packet_Make.REQ_BDOY_FAT_PERCENTAGE_ID, 0));
                        ble_send(packetMake.make(Packet_Make.REQ_BMI_ID, 0));

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //bluetooth
                                    unbindService(mServiceConnection);

                                } catch (Exception e) {
                                    Log.e("ioe", e.toString());
                                }
                            }
                        }).start();
                        // elapseFlag = false;

                    }
                }
            }
        }

    }

}
