package kr.ac.sch.se.Data_Transmission;

import android.util.Log;

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
 * Created by sun on 2017-01-10.
 */
public class HttpDataSend {
    private String TAG = "HTTP_DATA_SEND";
    private String weight_url;
    private String sessionId;

    public HttpDataSend(String url, String sessionId){
        this.weight_url = url;
        this.sessionId = sessionId;
    }

    public void dataPostSend(String wDate,String weight,String bmi,String fatMass,String fatPer,String arrhythmia){
        Thread thread = new Thread(new SendJspNetworkRunnable(wDate, weight, bmi, fatMass, fatPer, arrhythmia));
        thread.start();

        return ;
    }


    class SendJspNetworkRunnable implements Runnable {
        private String wDate, weight, bmi, fatMass, fatPer, arrhythmia;

        public SendJspNetworkRunnable(String wDate,String weight,String bmi,String fatMass,String fatPer,String arrhythmia){
            this.wDate = wDate;
            this.weight = weight;
            this.bmi = bmi;
            this.fatMass = fatMass;
            this.fatPer = fatPer;
            this.arrhythmia = arrhythmia;
        }

        public void sendHttpWithMsg(String addr){
            HttpClient client = new DefaultHttpClient();

            HttpParams pa = client.getParams();
            HttpConnectionParams.setConnectionTimeout(pa, 5000);

            try {
                HttpPost hp = new HttpPost(weight_url);
                hp.setHeader("session-id", sessionId);

                ArrayList<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("wDate", wDate));
                params.add(new BasicNameValuePair("weight", weight));
                params.add(new BasicNameValuePair("bmi", bmi));
                params.add(new BasicNameValuePair("fatMass", fatMass));
                params.add(new BasicNameValuePair("fatPer",fatPer));
                params.add(new BasicNameValuePair("arrhythmia", arrhythmia));

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                hp.setEntity(ent);

                HttpResponse responsePOST = client.execute(hp);
                HttpEntity resEntity = responsePOST.getEntity();

                InputStream inputstream = resEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputstream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                while (true) {
                    String st = reader.readLine();

                    Log.e(TAG, "result:" + st);
//                    if(st == null) {
//                        Log.e("null", "null");
//                    }
//                    else {
//                        Log.e("jsp", st + "");
//                    }

                    if (st == null) {
                        break;
                    }

                    sb.append(st);
                }

                inputstream.close();
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                // httpClient 닫음
                client.getConnectionManager().shutdown();
            }
            return ;
        }

        @Override
        public void run() {
                sendHttpWithMsg(weight_url);
        }
    }
}
