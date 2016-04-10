package com.example.yanhoor.flickrgallery;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.yanhoor.flickrgallery.model.Group;
import com.example.yanhoor.flickrgallery.model.PhotoSet;
import com.example.yanhoor.flickrgallery.model.Topic;
import com.example.yanhoor.flickrgallery.model.User;

import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/3/26.
 */
public class ListActivity extends SingleFragmentActivity {
    private static final String TAG="ListActivity";

    public static String dataType="1";

    @Override
    protected Fragment createFragment() {
        Log.d(TAG,"dataType is "+dataType);

        switch (dataType){
            case "groups":
                ArrayList<Group> mGroups=(ArrayList<Group>) getIntent()
                        .getSerializableExtra(ListGroupsFragment.EXTRA_DATA_GROUPS);
                return ListGroupsFragment.newInstance(mGroups);

            case "followings":
                ArrayList<User>mFollowings=(ArrayList<User>)getIntent()
                        .getSerializableExtra(ListFollowingsFragment.EXTRA_DATA_FOLLOWINGS);
                return ListFollowingsFragment.newInstance(mFollowings);

            case "topics":
                ArrayList<Topic>mTopics=(ArrayList<Topic>)getIntent()
                        .getSerializableExtra(ListTopicsFragment.EXTRA_TOPICS_DATA);
                return ListTopicsFragment.newInstance(mTopics);

            case "photosets":
                ArrayList<PhotoSet>mPhotosets=(ArrayList<PhotoSet>)getIntent()
                        .getSerializableExtra(ListPhotosetFragment.EXTRA_PHOTOSET_DATA);
                return ListPhotosetFragment.newInstance(mPhotosets);

            default:
                return null;
        }

    }
}
