package com.example.yanhoor.flickrgallery.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by yanhoor on 2016/3/9.
 */
public class VisibleFragment extends Fragment {
    public static final String TAG="VisibleFragment";

    private BroadcastReceiver mOnShowNotification=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"canceling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter=new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification,filter,PollService.PERM_PRIVATE,null);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mOnShowNotification);
    }
}
