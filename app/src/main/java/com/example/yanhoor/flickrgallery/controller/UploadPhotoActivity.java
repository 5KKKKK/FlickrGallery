package com.example.yanhoor.flickrgallery.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.example.yanhoor.flickrgallery.R;

/**
 * Created by yanhoor on 2016/3/31.
 */
public class UploadPhotoActivity extends SingleFragmentActivity{
    @Override
    protected Fragment createFragment() {
        return new UploadPhotoFragment();
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
