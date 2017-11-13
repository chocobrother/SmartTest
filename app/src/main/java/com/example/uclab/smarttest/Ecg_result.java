package com.example.uclab.smarttest;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Created by UCLAB on 2016-09-06.
 */
public class Ecg_result extends Activity {
    String userID, filepath, date, time;
    double weight_data, bmi_data, body_fat, body_fat_percentage;
    int hr = 0;
    private String af_yn;
    String db_url = "http://1.209.108.9:8080/scale_project/result.jsp";
    String db_url2 = "http://1.209.108.9:8080/scale_project/insertResult.jsp";
    ArrayList<NameValuePair> params;
    HttpPost hp, hp2;
    resultJSP rj;
    private final String TAG = "ECG_RESULT";

    private TextView averageHR, bmiTextView ,body_fatTextView, body_percentageTextView, afTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);

        Intent i = getIntent();

        userID = i.getStringExtra("ID");
        filepath = i.getStringExtra("FilePath");
        date = i.getStringExtra("DATE");
        time = i.getStringExtra("TIME");
        weight_data = i.getDoubleExtra("WEIGHT", 0);
        bmi_data = i.getDoubleExtra("BMI", 0);
        body_fat = i.getDoubleExtra("BODY_FAT", 0);
        body_fat_percentage = i.getDoubleExtra("BODY_PERCENTAGE", 0);
        hr = (int)i.getDoubleExtra("HR", 0);
        af_yn = i.getStringExtra("AF_DETECTION");

        Log.e(TAG, filepath);
//        private TextView ageTextView,bmiTextView ,body_fatTextView, body_percentageTextView;
        averageHR = (TextView)findViewById(R.id.avg);
        bmiTextView = (TextView)findViewById(R.id.bmi_textView);
        body_fatTextView = (TextView)findViewById(R.id.body_fat_textView);
        body_percentageTextView = (TextView)findViewById(R.id.body_percentage_textView);
        afTextView = (TextView)findViewById(R.id.af);

        averageHR.setText((int)hr+"");
        bmiTextView.setText(bmi_data+"");
        body_fatTextView.setText(body_fat+"");
        body_percentageTextView.setText(body_fat_percentage+"");
        afTextView.setText(af_yn);

        Log.e("Ecg_result", userID + "," + weight_data + "," + bmi_data + "," + body_fat + "," + body_fat_percentage+","+af_yn+","+hr);

        params = new ArrayList<NameValuePair>();

        //fix
//        rj = new resultJSP();
//        rj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, DeviceControlActivity.class);
        //주환 메뉴 화면으로 안가고 바로 디바이스 컨트롤 액티비티로

        startActivity(intent);

        finish();
    }

    class resultJSP extends AsyncTask<Void, Void, Void> {
        String gender;
        int height, avg;
        double wei, fm, bf;

        @Override
        protected Void doInBackground(Void... param) {
            if(isCancelled()) {
                return null;
            }

            Log.e("resultJSP", "Start");
            HttpClient client = new DefaultHttpClient();
            HttpClient client2 = new DefaultHttpClient();

            // 5초동안 응답이 없으면 끊어지도록 함
            HttpParams pa = client.getParams();
            HttpConnectionParams.setConnectionTimeout(pa, 5000);

            try {
                Log.e("resultJSP", "IN");

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
                        break;
                    }

                    else {
                        if(st.contains("HEIGHT")) {
                            String[] dist = st.split(":");
                            height = Integer.parseInt(dist[1]);
                        }

                        else if (st.contains("GENDER")) {
                            String[] dist = st.split(":");
                            gender = dist[1];
                        }

                        else {
                            Log.e("m_jsp", st);
                        }
                    }

                    sb.append(st);
                }

                inputstream.close();

            } catch(IOException e) {
                Log.e("Ecg_result IOE", e.toString());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PrintStream pt = new PrintStream(out);

                e.printStackTrace(pt);
                Log.e("PST", out.toString());

            } finally {
                Log.e("httpClient", "Close");
                client.getConnectionManager().shutdown();
            }

            params = new ArrayList<NameValuePair>();

            //태양이형 여기 수정하셔야 돼요!!!!!!!!!!!!!!!!
//            Log.e("Ecg_result", userID + "," + weight_data + "," + bmi_data + "," + body_fat + "," + body_fat_percentage+","+af_yn);
//            averageHR.setText((int)hr+"");
//            bmiTextView.setText(bmi_data+"");
//            body_fatTextView.setText(body_fat+"");
//            body_percentageTextView.setText(body_fat_percentage+"");
//            afTextView.setText(af_yn);
            String s_avg = String.valueOf(hr);
            String s_fm = String.format("%.2f", body_fat);
            String s_bf = String.format("%.2f", body_fat_percentage);
            String s_weight = String.format("%.2f", weight_data);
            String s_bmi = String.format("%.2f", bmi_data);

            if(af_yn.equals("Yes")){
                af_yn = "Y";
            }else{
                af_yn = "N";
            }

            params.add(new BasicNameValuePair("ID", userID));
            params.add(new BasicNameValuePair("DATE", date));
            params.add(new BasicNameValuePair("TIME", time));
            params.add(new BasicNameValuePair("WEIGHT", s_weight));
            params.add(new BasicNameValuePair("AVG", s_avg));
            params.add(new BasicNameValuePair("FM", s_fm));
            params.add(new BasicNameValuePair("BF", s_bf));
            params.add(new BasicNameValuePair("BMI", s_bmi));
            params.add(new BasicNameValuePair("AF", af_yn));

            // 5초동안 응답이 없으면 끊어지도록 함
            pa = client2.getParams();
            HttpConnectionParams.setConnectionTimeout(pa, 5000);

            try {
                hp2 = new HttpPost(db_url2);

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                hp2.setEntity(ent);

                HttpResponse responsePOST = client2.execute(hp2);
                HttpEntity resEntity = responsePOST.getEntity();

                InputStream inputstream = resEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputstream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                while (true) {
                    String st = reader.readLine();

                    if(st == null) {
                        break;
                    }

                    else {
                        Log.e("insertResult", st);
                    }

                    sb.append(st);
                }

                inputstream.close();
            }  catch(IOException e) {
                Log.e("IOE", "IOE");
                e.printStackTrace();
            } finally {
                Log.e("httpClient", "Close");
                client2.getConnectionManager().shutdown();
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

            //params.add(new BasicNameValuePair("ID", userID));
            params.add(new BasicNameValuePair("FilePath", filepath));
            params.add(new BasicNameValuePair("DATE", date));
            params.add(new BasicNameValuePair("TIME", time));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            params.clear();

            return;
        }
    }
}
