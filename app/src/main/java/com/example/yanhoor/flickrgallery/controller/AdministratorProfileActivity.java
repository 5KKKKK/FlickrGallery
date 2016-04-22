package com.example.yanhoor.flickrgallery.controller;

import android.support.v4.app.Fragment;

/**
 * Created by yanhoor on 2016/4/5.
 */
public class AdministratorProfileActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new AdministratorProfileFragment();
    }
}
