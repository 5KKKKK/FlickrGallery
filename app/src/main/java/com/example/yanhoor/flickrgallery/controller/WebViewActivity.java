package com.example.yanhoor.flickrgallery.controller;

import android.support.v4.app.Fragment;

/**
 * Created by yanhoor on 2016/3/10.
 */
public class WebViewActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        String url=getIntent().getStringExtra(WebViewFragment.EXTRA_URL);

        return WebViewFragment.newInstance(url);
    }
}
