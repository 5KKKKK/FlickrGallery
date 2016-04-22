package com.example.yanhoor.flickrgallery.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.example.yanhoor.flickrgallery.R;

/**
 * Created by yanhoor on 2015/12/6.
 */
public abstract class SingleFragmentActivity extends FragmentActivity {
    private static final String TAG="SingleFragmentActivity";
    protected abstract Fragment createFragment();

    protected int getLayoutResId(){
        return R.layout.activity_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        FragmentManager fm=getSupportFragmentManager();
        Fragment fragment=fm.findFragmentById(R.id.fragmentContainer);
        if(fragment==null){
            fragment=createFragment();
            fm.beginTransaction().add(R.id.fragmentContainer,fragment).commit();
        }
        Log.d(TAG,"onCreate");
    }
}
