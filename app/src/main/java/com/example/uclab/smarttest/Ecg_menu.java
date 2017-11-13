package com.example.uclab.smarttest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by UCLAB on 2016-08-01.
 */
public class Ecg_menu extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
    }

    @Override
      public void onBackPressed() {
        super.onBackPressed();
            finish();
        }

    public void btnClick(View v) {
        switch(v.getId()) {
            case R.id.measure:
                Intent j = new Intent(Ecg_menu.this, BleScan_Activity.class);
                startActivity(j);
                break;
            case R.id.record:
                Intent k = new Intent(Ecg_menu.this, Ecg_record.class);
                startActivity(k);
                break;
            case R.id.change:
                Intent f = new Intent(Ecg_menu.this, Ecg_change.class);
                startActivity(f);
                break;
            case R.id.testid:
//                Network_Http http = new Network_Http(SessionIDSharedPreference.getSessionIdPreference(getApplicationContext()));
//                http.HttpPost_send("201701101118", "80",  "23.4", "24", "25", "no");

//                                Network_Http http = new Network_Http(SessionIDSharedPreference.getSessionIdPreference(getApplicationContext()));
//                http.HttpGet_send("dataRecive");
                break;
        }
    }
}
