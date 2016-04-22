package com.example.yanhoor.flickrgallery.controller;

import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by yanhoor on 2016/3/20.
 */
public class UserProfileActivity extends SingleFragmentActivity {
    private static final String TAG="UserProfileActivity";

    @Override
    protected Fragment createFragment() {
        Log.d(TAG,this.toString());
        String mUserId=(String)getIntent().getSerializableExtra(UserProfileFragment.EXTRA_USER_ID);

        return UserProfileFragment.newUserProfileFragmentInstance(mUserId);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,this.toString());
        super.onDestroy();
    }
}
