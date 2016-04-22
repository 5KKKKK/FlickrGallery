package com.example.yanhoor.flickrgallery.controller;

import android.support.v4.app.Fragment;

import com.example.yanhoor.flickrgallery.model.Topic;

/**
 * Created by yanhoor on 2016/3/30.
 */
public class TopicDetailActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        Topic topic=(Topic)getIntent().getSerializableExtra(TopicDetailFragment.EXTRA_TOPIC);

        return TopicDetailFragment.newInstance(topic);
    }
}
