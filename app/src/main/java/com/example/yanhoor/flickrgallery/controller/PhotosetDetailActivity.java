package com.example.yanhoor.flickrgallery.controller;

import android.support.v4.app.Fragment;

/**
 * Created by yanhoor on 2016/4/9.
 */
public class PhotosetDetailActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        String photoSetId=getIntent().getStringExtra(PhotosetDetailFragment.EXTRA_PHOTOSET_ID);
        String userId=getIntent().getStringExtra(PhotosetDetailFragment.EXTRA_USER_ID);

        return PhotosetDetailFragment.newInstance(photoSetId,userId);
    }
}
