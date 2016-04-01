package com.example.yanhoor.flickrgallery.util;

import android.net.Uri;
import android.util.Log;

import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.example.yanhoor.flickrgallery.model.User;

import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.kymjs.kjframe.http.HttpConfig;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yanhoor on 2016/3/21.
 */
public class PhotoInfoUtil {
    private static final String TAG="PhotoInfoUtil";

    public  static final String ENDPOINT="https://api.flickr.com/services/rest/";
    public static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";


    listener mMainThreadListener;

    public interface listener {
        void onUpdateFinish(GalleryItem galleryItem);
    }

    public void setListener(listener mListener){
        mMainThreadListener=mListener;
    }

    public GalleryItem getPhotoInfo(final GalleryItem galleryItem){
        final User owner=new User();
        String photo_id=galleryItem.getId();
        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method","flickr.photos.getInfo")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("photo_id",photo_id)
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        KJHttp kjHttp=new KJHttp(config);
        kjHttp.get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                mMainThreadListener.onUpdateFinish(galleryItem);
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG,"Getting photo info from "+t);

                try{
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));
                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){

                        if (eventType==XmlPullParser.START_TAG&&"photo".equals(parser.getName())){
                            String secret=parser.getAttributeValue(null,"secret");
                            String server=parser.getAttributeValue(null,"server");
                            String farm=parser.getAttributeValue(null,"farm");

                            galleryItem.setSecret(secret);
                            galleryItem.setServer(server);
                            galleryItem.setFarm(farm);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"owner".equals(parser.getName())){
                            String userId=parser.getAttributeValue(null,"nsid");
                            String userName=parser.getAttributeValue(null,"username");
                            String realName=parser.getAttributeValue(null,"realname");
                            String location=parser.getAttributeValue(null,"location");
                            String iconServer=parser.getAttributeValue(null,"iconserver");
                            String iconFarm=parser.getAttributeValue(null,"iconfarm");

                            owner.setId(userId);
                            owner.setLocation(location);
                            owner.setUserName(userName);
                            owner.setRealName(realName);
                            owner.setIconFarm(iconFarm);
                            owner.setIconServer(iconServer);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"title".equals(parser.getName())){
                            String title=parser.nextText();
                            Log.d(TAG,"title is "+title);
                            galleryItem.setTitle(title);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"description".equals(parser.getName())){
                            String description=parser.nextText();
                            Log.d(TAG,"description is "+description);
                            galleryItem.setDescription(description);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"dates".equals(parser.getName())){
                            String postedTime=parser.getAttributeValue(null,"posted");
                            //unix timetamp转化为现在的ms要乘1000
                            Date mDate=new Date(Long.parseLong(postedTime)*1000);
                            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                            String dateString=simpleDateFormat.format(mDate);
                            galleryItem.setPostedTime(dateString);
                        }

                        eventType=parser.next();
                    }
                }catch (XmlPullParserException xppe){
                    xppe.printStackTrace();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
            }
        });
        galleryItem.setOwner(owner);
        return galleryItem;
    }

}
