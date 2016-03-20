package com.example.yanhoor.flickrgallery.util;

import android.net.Uri;
import android.util.Log;

import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.example.yanhoor.flickrgallery.model.User;

import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/3/20.
 */
public class GetUserProfileUtil {
    private static final String TAG="GetUserProfileUtil";

    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";

    private User mUser;
    private ArrayList<GalleryItem>mGalleryItems;

    public User getUserProfile(User user){
        mUser=user;
        getUserInfo();
        getFollowings();
        getUserPhoto();
        return mUser;
    }

    public void getUserInfo(){

        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method","flickr.people.getInfo")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("user_id",mUser.getId())
                .build().toString();

        new KJHttp.Builder().url(url).callback(new HttpCallBack() {
            @Override
            public void onSuccess(String t) {
                Log.d(TAG,"Getting user info from "+t);
                try{
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));//事件类型初始化为START_DOCUMENT

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"person".equals(parser.getName())){
                            String iconSever=parser.getAttributeValue(null,"iconserver");
                            String iconFarm=parser.getAttributeValue(null,"iconfarm");

                            mUser.setIconServer(iconSever);
                            mUser.setIconFarm(iconFarm);
                        }
                        if (eventType==XmlPullParser.START_TAG&&"username".equals(parser.getName())){
                            String userName=parser.nextText();
                            mUser.setUserName(userName);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"realname".equals(parser.getName())){
                            String realName=parser.nextText();
                            mUser.setRealName(realName);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"location".equals(parser.getName())){
                            String location=parser.nextText();
                            mUser.setLocation(location);
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

    }

    public void getFollowings(){
        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method","flickr.contacts.getPublicList")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("user_id",mUser.getId())
                .build().toString();

        new KJHttp.Builder().url(url).callback(new HttpCallBack() {
            @Override
            public void onSuccess(String t) {
                Log.d(TAG,"Getting following from "+t);
                ArrayList<User>mFollowings=new ArrayList<>();
                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"contact".equals(parser.getName())){
                            User following=new User();
                            String followingId=parser.getAttributeValue(null,"nsid");
                            String followingUserName=parser.getAttributeValue(null,"username");

                            following.setId(followingId);
                            following.setUserName(followingUserName);
                            mFollowings.add(following);
                        }
                        eventType=parser.next();
                    }
                }catch (XmlPullParserException xppe){
                    xppe.printStackTrace();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
                mUser.setFollowingUsers(mFollowings);
            }
        });

    }

    public void getUserPhoto(){
        mGalleryItems=new ArrayList<>();

        String photoUrl= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method","flickr.people.getPhotosOf")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("user_id",mUser.getId())
                .appendQueryParameter("extras","url_s")
                .build().toString();

        new KJHttp.Builder().url(photoUrl).callback(new HttpCallBack() {
            @Override
            public void onSuccess(String t) {
                Log.d(TAG,"Getting user photo from "+t);
                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"photo".equals(parser.getName())){
                            GalleryItem photo=new GalleryItem();
                            String photoId=parser.getAttributeValue(null,"id");
                            String smallUrl=parser.getAttributeValue(null,"url_s");

                            photo.setId(photoId);
                            photo.setUrl(smallUrl);
                            mGalleryItems.add(photo);
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

    }

}
