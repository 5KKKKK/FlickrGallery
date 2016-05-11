package com.example.yanhoor.flickrgallery.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.squareup.picasso.Picasso;

import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.kymjs.kjframe.http.HttpConfig;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/4/5.
 */
public class PhotoLatestFragment extends Fragment{
    private static final String TAG="PhotoLatestFragment";

    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";
    private static final String METHOD_GET_RECENT="flickr.photos.getRecent";
    private static final String PARAM_EXTRAS="extras";
    private static final String EXTRA_SMALL_URL="url_s";

    public static int totalPages;//在FlickrFetcher获取照片时设置的总页数
    private int page=1;
    private String per_page="25";
    private GridViewAdapter mAdapter;
    private GridView mGridView;
    private ArrayList<GalleryItem>mGalleryItems;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGalleryItems=new ArrayList<>();
        getRecentPhoto();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mGridView=(GridView)v.findViewById(R.id.gridView_photo_fragment);

        setupAdapter();

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
                            page=1;
                            mGalleryItems.clear();
                            getRecentPhoto();
                            mSRL.setRefreshing(false);
                        }
                    }
                },4000);
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GalleryItem item=mGalleryItems.get(position);
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
                        page++;
                        getRecentPhoto();
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
        if (getActivity()==null || mGridView==null) return;

        if (mGalleryItems!=null){
            mAdapter=new GridViewAdapter(mGalleryItems);
            mGridView.setAdapter(mAdapter);
        }else {
            mGridView.setAdapter(null);
        }
    }

    private void getRecentPhoto(){
        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method",METHOD_GET_RECENT)//自动转义查询字符串
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("page",String.valueOf(page))
                .appendQueryParameter("per_page",per_page)
                .appendQueryParameter(PARAM_EXTRAS,EXTRA_SMALL_URL)
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG, "onSuccess: Getting recent photos from "+t);
                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    ArrayList<GalleryItem>newGalleryItems=new ArrayList<>();
                    FlickrFetchr.fromWhere="latest";
                    new FlickrFetchr().parseItems(newGalleryItems,parser);

                    if (mGalleryItems.size()==0){
                        mGalleryItems.addAll(newGalleryItems);
                        mAdapter.notifyDataSetChanged();
                    }else {
                        String lastOldItem=mGalleryItems.get(mGalleryItems.size()-1).getId();
                        String lastNewItem=newGalleryItems.get(newGalleryItems.size()-1).getId();
                        if (!lastOldItem.equals(lastNewItem)){
                            mGalleryItems.addAll(newGalleryItems);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                }catch (XmlPullParserException xppe) {
                    xppe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

    }

    private class GridViewAdapter extends ArrayAdapter<GalleryItem>{
        public GridViewAdapter(ArrayList<GalleryItem> galleryItems){
            super(getActivity(),0,galleryItems);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.item_image_view,parent,false);
            }
            ImageView imageView=(ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.brain_up_close);
            GalleryItem item=getItem(position);
            Picasso.with(getActivity())
                    .load(item.getUrl())
                    .resize(300,300)
                    .centerCrop()
                    .into(imageView);

            return convertView;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGridView=null;
        System.gc();
    }

}
