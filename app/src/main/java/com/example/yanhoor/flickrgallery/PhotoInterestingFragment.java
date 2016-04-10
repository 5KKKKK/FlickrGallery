package com.example.yanhoor.flickrgallery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.example.yanhoor.flickrgallery.model.GalleryItemLab;

import java.util.ArrayList;
/**
 * Created by yanhoor on 2016/3/3.
 */
public class PhotoInterestingFragment extends VisibleFragment {
    private static final String TAG="PhotoInteresting";

    public static int totalPages;//在FlickrFetcher获取照片时设置的总页数

    GridView mGridView;
    ProgressBar mProgressBar;
    ArrayList<GalleryItem> mPerPageItems;
    ArrayList<ArrayList<GalleryItem>>mAllItems=new ArrayList<>();
    ThumbnaiDownloader<ImageView> mThumbnaiThread;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mPerPageItems =GalleryItemLab.get(getActivity()).getGalleryItems();//获取文件中的items
        mAllItems.add(mPerPageItems);
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
        mGridView=(GridView)v.findViewById(R.id.gridView);
        mProgressBar=(ProgressBar)v.findViewById(R.id.load_more_progress);
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
                        //返回上一页
                        if (FlickrFetchr.page>1){
                            mSRL.setRefreshing(false);
                            int lastPage=--FlickrFetchr.page;
                            mPerPageItems=mAllItems.get(lastPage-1);
                            setupAdapter();

                            if (FlickrFetchr.page==1)
                                return;//防止进入下面的if刷新两次
                        }

                        if (getActivity()!=null&&FlickrFetchr.page==1){
                            updateItems();
                            mAllItems.clear();
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

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (view.getLastVisiblePosition()==view.getCount()-1){
                    if (scrollState==SCROLL_STATE_TOUCH_SCROLL&&FlickrFetchr.page<totalPages){
                        try {
                            mProgressBar.setVisibility(View.VISIBLE);
                            Thread.sleep(1000);//延迟加载
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        FlickrFetchr.page++;
                        updateItems();
                    }
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        return v;
    }

    void setupAdapter(){
        Log.d(TAG, "setupAdapter: current page "+FlickrFetchr.page);
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
            mProgressBar.setVisibility(View.GONE);
            mPerPageItems =galleryItems;

            if (FlickrFetchr.page==1){
                if (mPerPageItems.size()!=0){
                    //添加新的图片前先删除原有的
                    GalleryItemLab.get(getActivity()).deleteGalleryItems();
                }
                GalleryItemLab.get(getActivity()).addGalleryItems(mPerPageItems);//应设置保存第一页
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
