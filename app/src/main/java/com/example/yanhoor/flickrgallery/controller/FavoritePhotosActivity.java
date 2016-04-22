package com.example.yanhoor.flickrgallery.controller;

import android.support.v4.app.Fragment;

import com.example.yanhoor.flickrgallery.model.GalleryItem;

import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/4/11.
 */
public class FavoritePhotosActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        ArrayList<GalleryItem>items=(ArrayList<GalleryItem>) getIntent().getSerializableExtra(FavoritePhotosFragment.EXTRA_FAVORITE_PHOTOS);

        return FavoritePhotosFragment.newInstance(items);
    }
}
