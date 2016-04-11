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

    GalleryItemAdapter mAdapter;
    GridView mGridView;
    ProgressBar mProgressBar;
    ArrayList<GalleryItem> mGalleryItems;
    ThumbnaiDownloader<ImageView> mThumbnaiThread;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        setHasOptionsMenu(true);
        Log.d(TAG,"saved items size is "+GalleryItemLab.get(getActivity()).getGalleryItems().size());
        mGalleryItems =GalleryItemLab.get(getActivity()).getGalleryItems();//获取文件中的items
        Log.d(TAG,"mGalleryItems size is "+ mGalleryItems.size());
        Log.d(TAG, "onCreate: page "+FlickrFetchr.page);
        if (mGalleryItems ==null){
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
        mGridView=(GridView)v.findViewById(R.id.gridView_photo_fragment);
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
                        if (getActivity()!=null){
                            updateItems();
                            mSRL.setRefreshing(false);
                        }
                    }
                },4000);
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GalleryItem item= mGalleryItems.get(position);
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
                        mProgressBar.setVisibility(View.VISIBLE);
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

        if (mGalleryItems !=null){
            mAdapter=new GalleryItemAdapter(mGalleryItems);
            mGridView.setAdapter(mAdapter);
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

            FlickrFetchr.fromWhere="interesting";
            return new FlickrFetchr().fetchItems();
        }

        //在主线程运行，在doinbackground之后执行
        @Override
        protected void onPostExecute(final ArrayList<GalleryItem> galleryItems) {
            mProgressBar.setVisibility(View.GONE);

            //判断返回的内容是否与旧的相同，防止重复添加
            int lastOldItem=mGalleryItems.size()-1;
            int lastNewItem=galleryItems.size()-1;
            if (!galleryItems.get(lastNewItem).getId().equals(mGalleryItems.get(lastOldItem).getId())){
                mGalleryItems.addAll(galleryItems);
                mAdapter.notifyDataSetChanged();
            }

            if (FlickrFetchr.page==1){
                if (galleryItems.size()!=0){
                    //添加新的图片前先删除原有的
                    GalleryItemLab.get(getActivity()).deleteGalleryItems();
                }
                Log.d(TAG, "onPostExecute: saved galleryItems");
                GalleryItemLab.get(getActivity()).addGalleryItems(galleryItems);//应设置保存第一页
                Log.d(TAG,"saved items size is "+GalleryItemLab.get(getActivity()).getGalleryItems().size());
            }

            Log.d(TAG,"mGalleryItems size is "+ mGalleryItems.size());
            //setupAdapter();
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
