package com.example.yanhoor.flickrgallery.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/4/11.
 */
public class FavoritePhotosFragment extends Fragment {
    public static final String EXTRA_FAVORITE_PHOTOS="favorites";

    private ArrayList<GalleryItem>mGalleryItems;
    private GridView mGridView;

    public static FavoritePhotosFragment newInstance(ArrayList<GalleryItem>galleryItems){
        Bundle args=new Bundle();
        args.putSerializable(EXTRA_FAVORITE_PHOTOS,galleryItems);
        FavoritePhotosFragment fragment=new FavoritePhotosFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGalleryItems=new ArrayList<>();
        mGalleryItems=(ArrayList<GalleryItem>)getArguments().getSerializable(EXTRA_FAVORITE_PHOTOS);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.gridview,container,false);
        mGridView=(GridView)v.findViewById(R.id.gridView);
        if (getActivity()!=null&&mGalleryItems.size()>0){
            mGridView.setAdapter(new GalleryItemAdapter(mGalleryItems));
        }

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i=new Intent(getActivity(),PhotoDetailActivity.class);
                i.putExtra(PhotoDetailFragment.EXTRA_GALLERYITEM_mId,mGalleryItems.get(position).getId());
                startActivity(i);
            }
        });

        return v;
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
        public GalleryItemAdapter(ArrayList<GalleryItem> items){
            super(getActivity(),0,items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView==null){
                viewHolder=new ViewHolder();
                convertView=getActivity().getLayoutInflater().inflate(R.layout.item_image_view,parent,false);
                viewHolder.mImageView=(ImageView) convertView.findViewById(R.id.gallery_item_imageView);
                convertView.setTag(viewHolder);
            }else {
                viewHolder=(ViewHolder)convertView.getTag();
            }
            viewHolder.mImageView.setImageResource(R.drawable.brain_up_close);
            GalleryItem item=getItem(position);
            Picasso.with(getActivity())
                    .load(item.getUrl())
                    .resize(240,240)
                    .centerCrop()
                    .into(viewHolder.mImageView);

            return convertView;
        }

        private class ViewHolder{
            ImageView mImageView;
        }
    }
}
