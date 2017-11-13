package kr.ac.sch.se.Common;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sun on 2017-01-10.
 */
public class SessionIDSharedPreference {

    public static String getSessionIdPreference(Context context){
        SharedPreferences sessionIDSH = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        return sessionIDSH.getString("sessionID", "no_Session");
    }

    public static void saveSessionIdPreference(Context context, String sid){
        SharedPreferences sessionIDSH = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sessionIDSH.edit();
        editor.putString("sessionID", sid);
        editor.commit();
    }
}
