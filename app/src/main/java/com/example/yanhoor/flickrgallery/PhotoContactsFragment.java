package com.example.yanhoor.flickrgallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";
    private static final String PARAM_EXTRAS="extras";
    private static final String EXTRA_SMALL_URL="url_s";

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
        mGridView=(GridView)v.findViewById(R.id.gridView);

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
                },5000);
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

        return v;
    }

    void setupAdapter(){
        if (getActivity()==null || mGridView==null) return;

        if (mGalleryItems!=null){
            mGridView.setAdapter(new GridViewAdapter(mGalleryItems));
        }else {
            mGridView.setAdapter(null);
        }
    }

    private void getContactsPhotos(){
        String[] mSignFullTokenStringArray = {"method" + "flickr.photos.getContactsPhotos",
                "api_key" + LogInFragment.API_KEY, "auth_token" + MainLayoutActivity.fullToken,
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
                .appendQueryParameter("auth_token", MainLayoutActivity.fullToken)
                .appendQueryParameter("api_sig",apiSig)
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                setupAdapter();
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);

                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    new FlickrFetchr().parseItems(mGalleryItems,parser);
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
