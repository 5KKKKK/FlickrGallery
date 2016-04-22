package com.example.yanhoor.flickrgallery.controller;

import android.content.Intent;
import android.net.Uri;
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

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.squareup.picasso.Picasso;

import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/4/4.
 */
public class SearchGalleryFragment  extends Fragment{
    private static final String TAG="SearchGalleryFragment";

    public static final String EXTRA_QUERY_GALLERY="gallery";

    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";
    private static final String METHOD_SEARCH="flickr.photos.search";
    private static final String PARAM_EXTRAS="extras";
    private static final String PARAM_TEXT="text";
    private static final String EXTRA_SMALL_URL="url_s";

    private ArrayList<GalleryItem>mGalleryItems;

    private GridView mGridView;

    public static SearchGalleryFragment newInstance(String query){
        Bundle args=new Bundle();
        args.putString(EXTRA_QUERY_GALLERY,query);
        SearchGalleryFragment fragment=new SearchGalleryFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGalleryItems=new ArrayList<>();
        String query=getArguments().getString(EXTRA_QUERY_GALLERY);
        searchPhoto(query);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_search_gallery,container,false);

        mGridView=(GridView)v.findViewById(R.id.gridView_search_gallery);
        setupAdapter();

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

    private void setupAdapter(){
        if (mGalleryItems!=null&&getActivity()!=null){
            mGridView.setAdapter(new GalleryItemAdapter(mGalleryItems));
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
            Log.d(TAG,"arrayAdapter, getView");
            if (convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.item_image_view,parent,false);
            }
            ImageView imageView=(ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.brain_up_close);
            GalleryItem item=getItem(position);

            Picasso.with(getActivity())
                    .load(item.getUrl())
                    .resize(360,360)
                    .centerCrop()
                    .placeholder(R.drawable.brain_up_close)
                    .into(imageView);

            return convertView;
        }
    }

    private void searchPhoto(String query){
        Log.d(TAG, "search: search word is "+query);
        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method",METHOD_SEARCH)
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter(PARAM_EXTRAS,EXTRA_SMALL_URL)
                .appendQueryParameter(PARAM_TEXT,query)
                .build().toString();

        new KJHttp().get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                setupAdapter();
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG,"Getting search result from "+t);

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

}
