package com.example.yanhoor.flickrgallery.controller;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by yanhoor on 2016/3/9.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG="NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"received result: "+getResultCode());

        //VisibleFragment在运行，总是比这里早接收广播（Notificationreceiver设置的优先级低），
        // 因而ResultCode总是Activity.RESULT_CANCELED
        if (getResultCode()!= Activity.RESULT_OK){
            return;
        }

        int requestCode= intent.getIntExtra("REQUEST_CODE",0);
        Notification notification=intent.getParcelableExtra("NOTIFICATION");
        NotificationManager notificationManager=(NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(requestCode,notification);
    }
}
