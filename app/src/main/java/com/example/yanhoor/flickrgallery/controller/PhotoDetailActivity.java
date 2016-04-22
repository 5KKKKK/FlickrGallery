package com.example.yanhoor.flickrgallery.controller;

import android.support.v4.app.Fragment;

/**
 * Created by yanhoor on 2016/3/15.
 */
public class PhotoDetailActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        String mId=(String) getIntent().getSerializableExtra(PhotoDetailFragment.EXTRA_GALLERYITEM_mId);

        return PhotoDetailFragment.newPhotoDetailFragmentInstance(mId);
    }
}
