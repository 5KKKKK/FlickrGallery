package com.example.yanhoor.flickrgallery.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.example.yanhoor.flickrgallery.model.PhotoSet;
import com.example.yanhoor.flickrgallery.util.GetPhotoSetInfoUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/4/9.
 */
public class PhotosetDetailFragment extends Fragment {
    private static final String TAG="PhotosetDetailFragment";

    public static final String EXTRA_PHOTOSET_ID="photoset_id";
    public static final String EXTRA_USER_ID="user_id";

    private PhotoSet mPhotoSet;
    private GetPhotoSetInfoUtil mGetPhotoSetInfoUtil;
    private GridView mGridView;

    public static PhotosetDetailFragment newInstance(String photosetId, String userId){
        Bundle args=new Bundle();
        args.putSerializable(EXTRA_PHOTOSET_ID,photosetId);
        args.putSerializable(EXTRA_USER_ID,userId);
        PhotosetDetailFragment fragment=new PhotosetDetailFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoSet=new PhotoSet();
        String photosetId=getArguments().getString(EXTRA_PHOTOSET_ID);
        String userId=getArguments().getString(EXTRA_USER_ID);
        mPhotoSet.setId(photosetId);
        mPhotoSet.setOwnerId(userId);
        mGetPhotoSetInfoUtil=new GetPhotoSetInfoUtil();
        updateData();
        mGetPhotoSetInfoUtil.setPhotoSetListener(new GetPhotoSetInfoUtil.PhotoSetListener() {
            @Override
            public void onUpdateFinish(PhotoSet photoSet) {
                mPhotoSet=photoSet;
                setupAdapter();
            }
        });
    }

    private void updateData(){
        mGetPhotoSetInfoUtil.getPhotoSetInfo(mPhotoSet.getId(),mPhotoSet.getOwnerId());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mGridView=(GridView)v.findViewById(R.id.gridView_photo_fragment);
        final SwipeRefreshLayout mSRL=(SwipeRefreshLayout)v.findViewById(R.id.swipeLayout);

        setupAdapter();

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i=new Intent(getActivity(),PhotoDetailActivity.class);
                i.putExtra(PhotoDetailFragment.EXTRA_GALLERYITEM_mId,mPhotoSet.getGalleryItems().get(position).getId());
                startActivity(i);
            }
        });

        mSRL.setColorSchemeResources(R.color.colorPurple,R.color.colorOrangeLight,
                R.color.colorRedLight,R.color.colorPrimary);
        mSRL.setProgressBackgroundColorSchemeResource(R.color.colorWhite);

        mSRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSRL.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity()!=null){
                            updateData();
                            mSRL.setRefreshing(false);
                        }
                    }
                },4000);
            }
        });

        return v;
    }

    void setupAdapter(){
        Log.d(TAG, "setupAdapter: galleryItems size is "+mPhotoSet.getGalleryItems().size());
        if (getActivity()==null || mGridView==null) return;

        if (mPhotoSet.getGalleryItems()!=null){
            mGridView.setAdapter(new GalleryItemAdapter(mPhotoSet.getGalleryItems()));
        }else {
            mGridView.setAdapter(null);
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
        public GalleryItemAdapter(ArrayList<GalleryItem> items){
            super(getActivity(),0,items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.item_image_view,parent,false);
            }
            ImageView imageView=(ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.brain_up_close);
            GalleryItem item=getItem(position);
            Log.d(TAG, "getView: photo url is "+item.getUrl());
            Picasso.with(getActivity())
                    .load(item.getUrl())
                    .resize(240,240)
                    .centerCrop()
                    .into(imageView);

            return convertView;
        }
    }

}
