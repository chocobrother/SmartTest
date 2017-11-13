package com.example.uclab.smarttest;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by UCLAB on 2016-10-23.
 */
public class Ecg_record_show extends Dialog {
    private View.OnClickListener cli;
    private String date, time, id;
    private Button bt;
    private TextView text_avg, text_fm, text_bf, text_wei, text_bmi, text_af;
    private showDisplay sd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.record_show);

        text_avg = (TextView) findViewById(R.id.avg);
        text_fm = (TextView) findViewById(R.id.fm);
        text_bf = (TextView) findViewById(R.id.bf);
        text_wei = (TextView) findViewById(R.id.weight);
        text_bmi = (TextView) findViewById(R.id.bmi);
        text_af = (TextView) findViewById(R.id.af);

        bt = (Button) findViewById(R.id.button);

        bt.setOnClickListener(cli);
    }

    public Ecg_record_show(Context context, String date, String time, String id, View.OnClickListener single) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        cli = single;
        this.date = date;
        this.time = time;
        this.id = id;

        sd = new showDisplay();
        sd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class showDisplay extends AsyncTask<Void,Void,Void> {
        HttpPost hp;
        ArrayList<NameValuePair> params;
        String url = "http://1.209.108.9:8080/scale_project/recordshow.jsp";
        String avg, fm, bf, wei, bmi, af;

        @Override
        protected Void doInBackground(Void... param) {
            HttpClient client = new DefaultHttpClient();
            String[] data = null;

            // 5초동안 응답이 없으면 끊어지도록 함
            HttpParams pa = client.getParams();
            HttpConnectionParams.setConnectionTimeout(pa, 5000);

            try {
                hp = new HttpPost(url);

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                hp.setEntity(ent);

                HttpResponse responsePOST = client.execute(hp);
                HttpEntity resEntity = responsePOST.getEntity();

                /*if(resEntity != null) {
                    Log.e("RESPONSE", EntityUtils.toString(resEntity));
                }*/

                InputStream inputstream = resEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputstream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                while (true) {
                    if(isCancelled()) {
                        return null;
                    }

                    String st = reader.readLine();

                    if(st == null) {
                        Log.e("m_null", "null");
                        break;
                    }

                    else {
                        if(st.contains("AVG")) {
                            data = st.split(":");
                            avg = data[1];
                        }

                        else if(st.contains("FM")) {
                            data = st.split(":");
                            fm = data[1];
                        }

                        else if(st.contains("BF")) {
                            data = st.split(":");
                            bf = data[1];
                        }

                        else if(st.contains("WEIGHT")) {
                            data = st.split(":");
                            wei = data[1];
                        }

                        else if(st.contains("BMI")) {
                            data = st.split(":");
                            bmi = data[1];
                        }

                        else if(st.contains("AF")) {
                            data = st.split(":");
                            af = data[1];
                        }
                    }

                    sb.append(st);
                }

                inputstream.close();

                // 결과를 저장
                //Message msg = handle.obtainMessage();
                //msg.obj = sb.toString();

                //handle.sendMessage(msg);
            } catch(IOException e) {
                e.printStackTrace();
                Log.e("IOE", e.toString());
            } finally {
                // httpClient 닫음
                Log.e("httpClient", "Close");
                client.getConnectionManager().shutdown();
            }

            return null;
        }

        @Override
        protected void onCancelled(Void result) {
            // TODO 취소할때 지정한 결과값으로 취소를 한다.
            super.onCancelled(result);

            Log.e("onCancelled", "IN");

            return;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(isCancelled()) {
                return ;
            }

            params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("ID", id));
            params.add(new BasicNameValuePair("DATE", date));
            params.add(new BasicNameValuePair("TIME", time));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            text_avg.setText(avg);
            text_fm.setText(fm);
            text_bf.setText(bf);
            text_wei.setText(wei);
            text_bmi.setText(bmi);
            text_af.setText(af);

            return;
        }
    }
}
