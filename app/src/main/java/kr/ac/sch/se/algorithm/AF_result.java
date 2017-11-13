package kr.ac.sch.se.algorithm;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by sun on 2016-11-17.
 */
public class AF_result extends AsyncTask<Void, Void, Void> {
        private String url = "http://1.209.108.9:8080/result.jsp";
        private  HttpClient http = new DefaultHttpClient();
        private ArrayList<NameValuePair> params;
        private HttpPost hp, hp2;

        @Override
        protected Void doInBackground(Void... param) {
            // TODO Auto-generated method stub
            try {

                ArrayList<NameValuePair> nameValuePairs =
                        new ArrayList<NameValuePair>();
//                nameValuePairs.add(new BasicNameValuePair("FilePath", "유재석"));
//                nameValuePairs.add(new BasicNameValuePair("DATE", "유재석"));
//                nameValuePairs.add(new BasicNameValuePair("TIME", "유재석"));

                HttpParams params = http.getParams();
                HttpConnectionParams.setConnectionTimeout(params, 5000);
                HttpConnectionParams.setSoTimeout(params, 5000);

                HttpPost httpPost = new HttpPost(url);
                UrlEncodedFormEntity entityRequest =
                        new UrlEncodedFormEntity(nameValuePairs, "EUC-KR");

                httpPost.setEntity(entityRequest);

                HttpResponse responsePost = http.execute(httpPost);
                HttpEntity resEntity = responsePost.getEntity();

                /*if(resEntity != null) {
                    Log.e("RESPONSE", EntityUtils.toString(resEntity));
                }*/

                InputStream inputstream = resEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputstream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String st = reader.readLine();
                while (st != null) {
                    Log.e("aftest", st);
                    st = reader.readLine();
                }

            }catch(IOException e){e.printStackTrace();}
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

        }

        @Override
        protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                return;
        }
}
