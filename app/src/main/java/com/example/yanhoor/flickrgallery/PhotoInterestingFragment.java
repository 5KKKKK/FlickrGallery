package com.example.yanhoor.flickrgallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.example.yanhoor.flickrgallery.model.GalleryItemLab;
import com.paging.gridview.PagingGridView;

import java.util.ArrayList;
/**
 * Created by yanhoor on 2016/3/3.
 */
public class PhotoInterestingFragment extends VisibleFragment {
    private static final String TAG="PhotoInteresting";

    public static int totalPages;//获取照片时设置的总页数

    PagingGridView mGridView;
    ArrayList<GalleryItem> mPerPageItems;
    ArrayList<ArrayList<GalleryItem>>mAllItems=new ArrayList<>();
    ThumbnaiDownloader<ImageView> mThumbnaiThread;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mPerPageItems =GalleryItemLab.get(getActivity()).getGalleryItems();//获取文件中的items
        Log.d(TAG,"mPerPageItems is "+ mPerPageItems);
        if (mPerPageItems ==null){
            updateItems();
        }
        mThumbnaiThread=new ThumbnaiDownloader<>(getActivity(),new Handler());//创建的handler默认与当前线程相关联
        mThumbnaiThread.setListener( new ThumbnaiDownloader.Listener<ImageView>(){
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if (isVisible()){
                    Log.d(TAG,"setListener");
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnaiThread.start();
        mThumbnaiThread.getLooper();
        Log.d(TAG,"Background thread started");
    }

    public void updateItems(){
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mGridView=(PagingGridView)v.findViewById(R.id.gridView);
        setupAdapter();

        //下拉刷新颜色
        final SwipeRefreshLayout mSRL=(SwipeRefreshLayout)v.findViewById(R.id.swipeLayout);
        mSRL.setColorSchemeResources(R.color.colorPurple,R.color.colorOrangeLight,
                R.color.colorRedLight,R.color.colorPrimary);
        mSRL.setProgressBackgroundColorSchemeResource(R.color.colorWhite);

        mSRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSRL.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (FlickrFetchr.page>1){
                            mSRL.setRefreshing(false);
                            FlickrFetchr.page--;
                            mPerPageItems=mAllItems.get(FlickrFetchr.page);
                            setupAdapter();
                        }

                        if (getActivity()!=null&&FlickrFetchr.page==1){
                            ConnectivityManager connectivityManager=(ConnectivityManager)getActivity()
                                    .getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
                            if (networkInfo!=null&&networkInfo.isAvailable()){
                                updateItems();
                            }else{
                                Toast.makeText(getActivity(),R.string.networt_unavailable,Toast.LENGTH_SHORT).show();
                            }
                            mSRL.setRefreshing(false);
                        }
                    }
                },4000);
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GalleryItem item= mPerPageItems.get(position);
                    Intent i=new Intent(getActivity(),PhotoDetailActivity.class);
                    i.putExtra(PhotoDetailFragment.EXTRA_GALLERYITEM_mId,item.getId());
                    startActivity(i);
            }
        });

        return v;
    }

    void setupAdapter(){
        if (getActivity()==null || mGridView==null) return;

        if (mPerPageItems !=null){
            mGridView.setAdapter(new GalleryItemAdapter(mPerPageItems));
        }else {
            mGridView.setAdapter(null);
        }
    }

    //获取GalleryItem项
    private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>>{
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
            Activity activity=getActivity();

            Log.d(TAG, "doInBackground: activity is "+activity);
            if (activity==null)
                return new ArrayList<>();

            return new FlickrFetchr().fetchItems();
        }

        //在主线程运行，在doinbackground之后执行
        @Override
        protected void onPostExecute(final ArrayList<GalleryItem> galleryItems) {
            mPerPageItems =galleryItems;

            if (FlickrFetchr.page==1){
                if (mPerPageItems.size()!=0){
                    //添加新的图片前先删除原有的
                    GalleryItemLab.get(getActivity()).deleteGalleryItems();
                }
                GalleryItemLab.get(getActivity()).addGalleryItems(mPerPageItems);//应设置保存第一页
            }

            if (totalPages>FlickrFetchr.page){
                mGridView.setHasMoreItems(true);
                mGridView.setPagingableListener(new PagingGridView.Pagingable() {
                    @Override
                    public void onLoadMoreItems() {
                        FlickrFetchr.page++;
                        updateItems();
                    }
                });
                mGridView.onFinishLoading(true,galleryItems);
            }else {
                mGridView.onFinishLoading(false,null);
            }

            mAllItems.add(mPerPageItems);
            Log.d(TAG,"mPerPageItems size is "+ mPerPageItems.size());
            setupAdapter();
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>{
        public GalleryItemAdapter(ArrayList<GalleryItem> items){
            super(getActivity(),0,items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG,"arrayAdapter, getView");
            if (convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.item_image_view,parent,false);
            }
            ImageView imageView=(ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.brain_up_close);
            GalleryItem item=getItem(position);
            mThumbnaiThread.queueThumbnail(imageView,item.getUrl());
            return convertView;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnaiThread.clearQueue();
    }

    @Override
    public void onPause() {
        super.onPause();
        GalleryItemLab.get(getActivity()).saveGalleryItems();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnaiThread.quit();//结束线程
        Log.d(TAG,"Background thread destroyed");
    }
}
