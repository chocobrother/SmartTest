package kr.ac.sch.se.Data_Transmission;


import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

/**
 * Created by sun on 2017-01-10.
 */
public class HttpDataParsing {
    private String TAG = "HTTP_DATA_PARSING";
    private String url;
    private String sessionId;

    public HttpDataParsing(String url, String sessionId){
        this.url = url;
        this.sessionId = sessionId;
    }

    public String dataGetParsing(){
        SendJspNetworkRunnable asyncTask = new SendJspNetworkRunnable();
        String str = null;
        try {
            str = asyncTask.execute().get();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        catch (ExecutionException e){
            e.printStackTrace();
        }
        return str;
    }

    class SendJspNetworkRunnable extends AsyncTask<String, String, String> {
        private String resultString;

        public String sendHttpWithMsg(String addr){
            String resultString = null;
            HttpClient client = new DefaultHttpClient();

            HttpParams pa = client.getParams();
            HttpConnectionParams.setConnectionTimeout(pa, 5000);

            try {
                HttpGet hg = new HttpGet(url);
                hg.setHeader("session-id", sessionId);

                HttpResponse responsePOST = client.execute(hg);
                HttpEntity resEntity = responsePOST.getEntity();

                InputStream inputstream = resEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputstream, "UTF-8"), 8);

                while (true) {
                    String str = null;
                    if((str = reader.readLine()) != null){
                        resultString = str;
                    }else{
                        break;
                    }
                }

                inputstream.close();
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                // httpClient 닫음
                client.getConnectionManager().shutdown();
            }
            return resultString;
        }

        @Override
        protected String doInBackground(String... params) {
            resultString = sendHttpWithMsg(url);

            return resultString;
        }
    }
}
