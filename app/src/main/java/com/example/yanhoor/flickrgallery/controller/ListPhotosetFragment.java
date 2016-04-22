package com.example.yanhoor.flickrgallery.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.PhotoSet;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/4/8.
 */
public class ListPhotosetFragment extends Fragment {
    private static final String TAG="ListPhotosetFragment";

    public static final String EXTRA_PHOTOSET_DATA="photoset";

    private GridView mGridView;
    private ArrayList<PhotoSet>mPhotoSets;
    private GridviewAdapter mAdapter;

    public static ListPhotosetFragment newInstance(ArrayList<PhotoSet> photoSets){
        Bundle args=new Bundle();
        args.putSerializable(EXTRA_PHOTOSET_DATA,photoSets);
        ListPhotosetFragment fragment=new ListPhotosetFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoSets=new ArrayList<>();
        mPhotoSets.addAll((ArrayList<PhotoSet>)getArguments().getSerializable(EXTRA_PHOTOSET_DATA));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photoset_list,container,false);

        mGridView=(GridView) v.findViewById(R.id.photoset_gridView);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: user id "+mPhotoSets.get(position).getOwnerId());
                Intent i=new Intent(getActivity(),PhotosetDetailActivity.class);
                i.putExtra(PhotosetDetailFragment.EXTRA_PHOTOSET_ID,mPhotoSets.get(position).getId());
                i.putExtra(PhotosetDetailFragment.EXTRA_USER_ID,mPhotoSets.get(position).getOwnerId());
                startActivity(i);
            }
        });

        Log.d(TAG, "onCreateView: mPhotosets size is "+mPhotoSets.size());
        if (mPhotoSets.size()>0){
            mAdapter=new GridviewAdapter(mPhotoSets);
            mGridView.setAdapter(mAdapter);
        }

        return v;
    }

    private class GridviewAdapter extends ArrayAdapter<PhotoSet>{
        private GridviewAdapter(ArrayList<PhotoSet> mPhotosets){
            super(getActivity(),0,mPhotosets);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.item_photoset_list,parent,false);
            }
            ImageView primaryPhoto=(ImageView)convertView.findViewById(R.id.imageView_photoset_primary);
            TextView titleView=(TextView)convertView.findViewById(R.id.photoset_title);

            PhotoSet photoSet=mPhotoSets.get(position);

            Picasso.with(getActivity())
                    .load(photoSet.getPrimaryPhotoUrl())
                    .resize(240,240)
                    .centerCrop()
                    .placeholder(R.drawable.brain_up_close)
                    .into(primaryPhoto);

            titleView.setText(photoSet.getTitle());

            return convertView;
        }

    }
}
