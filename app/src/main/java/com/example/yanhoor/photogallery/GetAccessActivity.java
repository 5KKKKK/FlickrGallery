package com.example.yanhoor.photogallery;

import android.support.v4.app.Fragment;

/**
 * Created by yanhoor on 2016/3/10.
 */
public class GetAccessActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new GetAccessFragment();
    }
}