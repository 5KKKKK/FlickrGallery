package com.example.yanhoor.flickrgallery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.kymjs.kjframe.KJBitmap;
import org.kymjs.kjframe.bitmap.BitmapCallBack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by yanhoor on 2016/4/6.
 */
public class PhotoViewFragment extends Fragment {
    private static final String TAG="PhotoViewFragment";

    public static final String EXTRA_PHOTO_URL="photo_url";

    private String photoUrl;
    private ImageView mImageView;
    private Bitmap mBitmap;
    private Activity mActivity;

    public static PhotoViewFragment newInstance(String url){
        Bundle args=new Bundle();
        args.putString(EXTRA_PHOTO_URL,url);
        PhotoViewFragment fragment=new PhotoViewFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity=(Activity)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        photoUrl=getArguments().getString(EXTRA_PHOTO_URL);
        Log.d(TAG, "onCreate: photoUrl is "+photoUrl);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photo_view,container,false);

        mImageView=(ImageView)v.findViewById(R.id.imageView_photo_fragment);

        final ProgressDialog progressDialog=new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.downloading_photo_progressdialog));;
        progressDialog.setCancelable(true);
        progressDialog.show();

        final KJBitmap kjBitmap=new KJBitmap();
        kjBitmap.doDisplay(mImageView, photoUrl, 0, 0, null, 0, null, 0, new BitmapCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.dismiss();
                new PhotoViewAttacher(mImageView);
            }

            @Override
            public void onSuccess(Bitmap bitmap) {
                super.onSuccess(bitmap);
                mBitmap=bitmap;
            }
        });

        return v;
    }

    private void saveImage(Bitmap bmp) {
        File appDir = new File(Environment.getExternalStorageDirectory(), "FlickrGallery");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(getActivity(),R.string.save_photo_success,Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.save_photo_context_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_photo_menu:
                if (mBitmap!=null){
                    saveImage(mBitmap);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
