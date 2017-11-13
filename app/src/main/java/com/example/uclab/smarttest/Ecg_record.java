package com.example.uclab.smarttest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import kr.ac.sch.se.Common.SessionIDSharedPreference;
import kr.ac.sch.se.Data_Transmission.Network_Http;

/**
 * Created by UCLAB on 2016-08-04.
 */
public class Ecg_record extends Activity  {
    private final String TAG = "ECG_RECORD";
    private Calendar cal;
    private int direction;
    private final int LEFT_CALANDER = 1;
    private final int RIGHT_CALANDER = 2;
    private TextView firstDataTextView, secondDataTextView;
    private String firstTime = null, lastTime = null;

    LayoutInflater inflater;
    View header;
    TextView select_d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record);

        direction = 0;

        firstDataTextView = (TextView) findViewById(R.id.forText);
        secondDataTextView = (TextView) findViewById(R.id.backText);

        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        header = inflater.inflate(R.layout.calendar, null, false);
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.cal1:
                direction = 1;
                cal = new Calendar(this, okListener);
                cal.show();
                break;

            case R.id.cal2:
                direction = 2;
                cal = new Calendar(this, okListener);
                cal.show();
                break;

            case R.id.button:
                Network_Http network_http = new Network_Http(SessionIDSharedPreference.getSessionIdPreference(getApplicationContext()));

                Log.e(TAG, firstTime + "," + lastTime);

                if(firstTime != null || lastTime != null)
                    firstTime = dateStringFormatMake(firstTime);
                    lastTime = dateStringFormatMake(lastTime);

                //예외처리하는 부분을 추가하자
                //뒤시간크면 하도록

                if(Integer.parseInt(lastTime) - Integer.parseInt(firstTime) > 0) {
                    network_http.HttpGet_send(Network_Http.DATA_RECORD_GET, firstTime, lastTime);
                }else{
                    //
                    Toast.makeText(getApplicationContext(), R.string.record_view, Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    //2017-1-11 -> 20170111
    public String dateStringFormatMake(String raw_date){
        String str = "-";
        String tmp = raw_date;

        Log.e(TAG, "rawDate:"+raw_date);

        //year
        int index = tmp.indexOf(str);
        String year = tmp.substring(0, index);

        //month
        index = tmp.indexOf(str);
        String month = tmp.substring(index + 1);
        index = month.indexOf(str);
        month = month.substring(0, index);

        //day
        index = tmp.indexOf(str);
        String day = tmp.substring(index + 1);
        index = day.indexOf(str);
        day = day.substring(index+1, day.length());

        if(month.length() == 1){
            month = "0"+month;
        }

        if(day.length() == 1){
            day = "0"+day;
        }

        return year+month+day;
    }

    private View.OnClickListener okListener = new View.OnClickListener() {
        public void onClick(View v) {
            select_d = (TextView) ((Calendar)cal).findViewById(R.id.curText);

            if(direction == LEFT_CALANDER) {
                firstDataTextView.setText(select_d.getText().toString());
                firstTime = select_d.getText().toString();
            }

            else if (direction == RIGHT_CALANDER) {
                secondDataTextView.setText(select_d.getText().toString());
                lastTime = select_d.getText().toString();
            }

            cal.dismiss();
        }
    };
}
