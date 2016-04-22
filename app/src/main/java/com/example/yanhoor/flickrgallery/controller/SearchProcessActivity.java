package com.example.yanhoor.flickrgallery.controller;

import android.support.v4.app.Fragment;

/**
 * Created by yanhoor on 2016/4/4.
 */
public class SearchProcessActivity extends SingleFragmentActivity {
    private static final String TAG="SearchProcessActivity";

    @Override
    protected Fragment createFragment() {
        String query=(String) getIntent().getSerializableExtra(SearchGalleryFragment.EXTRA_QUERY_GALLERY);

        return SearchGalleryFragment.newInstance(query);
    }
}
