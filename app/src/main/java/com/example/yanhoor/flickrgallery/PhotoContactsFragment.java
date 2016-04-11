package com.example.yanhoor.flickrgallery;

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

import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.example.yanhoor.flickrgallery.util.StaticMethodUtil;
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
import java.util.Arrays;

/**
 * Created by yanhoor on 2016/4/5.
 */
public class PhotoContactsFragment extends Fragment {
    private static final String TAG="PhotoContactsFragment";

    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";
    private static final String PARAM_EXTRAS="extras";
    private static final String EXTRA_SMALL_URL="url_s";

    public static int totalPages;//在FlickrFetcher获取照片时设置的总页数
    private int page=1;
    private String per_page="25";
    private GridViewAdapter mAdapter;
    private GridView mGridView;
    private ArrayList<GalleryItem> mGalleryItems;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGalleryItems=new ArrayList<>();
        getContactsPhotos();
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
                            getContactsPhotos();
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
                        getContactsPhotos();
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

    private void getContactsPhotos(){
        String[] mSignFullTokenStringArray = {"method" + "flickr.photos.getContactsPhotos",
                "api_key" + LogInFragment.API_KEY, "auth_token" + MainLayoutActivity.fullToken,
                "page"+page,"per_page"+per_page,
                LogInFragment.PUBLIC_CODE,PARAM_EXTRAS+EXTRA_SMALL_URL};
        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.photos.getContactsPhotos")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PARAM_EXTRAS,EXTRA_SMALL_URL)
                .appendQueryParameter("per_page",per_page)
                .appendQueryParameter("page",String.valueOf(page))
                .appendQueryParameter("auth_token", MainLayoutActivity.fullToken)
                .appendQueryParameter("api_sig",apiSig)
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
                Log.d(TAG, "onSuccess: Getting contacts photos from "+t);

                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    ArrayList<GalleryItem>newGalleryItems=new ArrayList<>();
                    FlickrFetchr.fromWhere="contacts";
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

    private class GridViewAdapter extends ArrayAdapter<GalleryItem> {
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

}
