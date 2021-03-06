package com.example.yanhoor.flickrgallery.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by yanhoor on 2016/3/9.
 */
public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG="StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent){
        Log.d(TAG,"Received broadcast intent: "+intent.getAction());

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(context);
        boolean isOn=prefs.getBoolean(PollService.PREF_IS_ALARM_ON,false);
        PollService.setServiceAlarm(context,isOn);
    }

}
