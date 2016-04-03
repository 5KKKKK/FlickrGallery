package com.example.yanhoor.flickrgallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;

/**
 * Created by yanhoor on 2016/3/30.
 */
public class TopicReplyActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        String groupId=getIntent().getStringExtra(TopicReplyFragment.EXTRA_GROUP_ID);
        String topicId=getIntent().getStringExtra(TopicReplyFragment.EXTRA_TOPIC_ID);

        return TopicReplyFragment.newInstance(groupId,topicId);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setMessage(R.string.back_key_note)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create().show();
        }
        return false;
    }

}
