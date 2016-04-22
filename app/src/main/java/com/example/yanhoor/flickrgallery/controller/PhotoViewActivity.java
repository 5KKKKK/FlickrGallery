package com.example.yanhoor.flickrgallery.controller;

import android.support.v4.app.Fragment;

import com.example.yanhoor.flickrgallery.model.GalleryItem;

/**
 * Created by yanhoor on 2016/4/6.
 */
public class PhotoViewActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        GalleryItem photo=(GalleryItem) getIntent().getSerializableExtra(PhotoViewFragment.EXTRA_GALLERY_ITEM);

        return PhotoViewFragment.newInstance(photo);
    }
}
