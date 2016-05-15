package com.example.yanhoor.flickrgallery;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by yanhoor on 2016/5/14.
 */
public class MyApplication extends Application {
    private RefWatcher mRefWatcher;
    private String mId;
    private String mUserName;
    private String mFullToken;

    public static final String PREF_FULL_TOKEN="fullToken";
    public static final String PREF_USER_ID ="id";
    public static final String PREF_USER_NAME="username";

    public static RefWatcher getRefWatcher(Context context) {
        MyApplication application = (MyApplication) context.getApplicationContext();
        return application.mRefWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setId(preferences.getString(PREF_USER_ID,null));
        setUserName(preferences.getString(PREF_USER_NAME,null));
        setFullToken(preferences.getString(PREF_FULL_TOKEN,null));
        mRefWatcher=LeakCanary.install(this);
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getFullToken() {
        return mFullToken;
    }

    public void setFullToken(String fullToken) {
        mFullToken = fullToken;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }
}
