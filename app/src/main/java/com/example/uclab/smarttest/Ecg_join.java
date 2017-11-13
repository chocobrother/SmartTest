package com.example.uclab.smarttest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.logging.Handler;

/**
 * Created by UCLAB on 2016-07-19.
 *///
public class Ecg_join extends Activity {
    EditText i, p, a, h;
    Button b;
    HttpPost hp;
    String db_url = "http://1.209.108.9:8080/scale_project/test2.jsp";
    ArrayList<NameValuePair> params;
    joinJSP lj;
    CheckBox ma, fma;
    String ge;

    private Handler handle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join);

        i = (EditText) findViewById(R.id.e_id);
        p = (EditText) findViewById(R.id.e_pw);
        a = (EditText) findViewById(R.id.e_age);
        h = (EditText) findViewById(R.id.e_height);
        b = (Button) findViewById(R.id.button);

        ma = (CheckBox) findViewById(R.id.male);
        fma = (CheckBox) findViewById(R.id.female);

        params = new ArrayList<NameValuePair>();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button:
                lj = new joinJSP();
                lj.execute();

                finish();
                break;
        }
    }

    class joinJSP extends AsyncTask<Void,String,Void> {
        @Override
        protected Void doInBackground(Void... param) {
            HttpClient client = new DefaultHttpClient();

            // 5초동안 응답이 없으면 끊어지도록 함
            HttpParams pa = client.getParams();
            HttpConnectionParams.setConnectionTimeout(pa, 5000);

            try {
                hp = new HttpPost(db_url);

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
                    String st = reader.readLine();

                    if(st == null) {
                        Log.e("null", "null");
                    }

                    else if (st == "Success!") {
                        Toast.makeText(getApplicationContext(), "가입되었습니다", Toast.LENGTH_LONG).show();
                    }

                    else {
                        Log.e("jsp", st + "");
                    }

                    if (st == null) {
                        break;
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
            } finally {
                // httpClient 닫음
                client.getConnectionManager().shutdown();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(ma.isChecked() == true && fma.isChecked() == false) {
                ge = "male";
            }

            else if (ma.isChecked() == false && fma.isChecked() == true) {
                ge = "female";
            }

            params.add(new BasicNameValuePair("ID", i.getText().toString()));
            params.add(new BasicNameValuePair("PW", p.getText().toString()));
            params.add(new BasicNameValuePair("AGE", a.getText().toString()));
            params.add(new BasicNameValuePair("GENDER", ge));
            params.add(new BasicNameValuePair("HEIGHT", h.getText().toString()));
        }
    }
}

