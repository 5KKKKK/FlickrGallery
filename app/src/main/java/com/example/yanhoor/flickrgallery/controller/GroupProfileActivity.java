package com.example.yanhoor.flickrgallery.controller;

import android.support.v4.app.Fragment;

/**
 * Created by yanhoor on 2016/3/27.
 */
public class GroupProfileActivity extends SingleFragmentActivity {
    private static final String TAG="GroupProfileActivity";

    @Override
    protected Fragment createFragment() {
        String groupId=(String)getIntent().getSerializableExtra(GroupProfileFragment.EXTRA_GROUP_ID);

        return GroupProfileFragment.newInstance(groupId);
    }
}
