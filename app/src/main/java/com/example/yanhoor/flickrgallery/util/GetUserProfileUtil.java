package com.example.yanhoor.flickrgallery.util;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.yanhoor.flickrgallery.controller.LogInFragment;
import com.example.yanhoor.flickrgallery.controller.MainLayoutActivity;
import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.example.yanhoor.flickrgallery.model.Group;
import com.example.yanhoor.flickrgallery.model.PhotoSet;
import com.example.yanhoor.flickrgallery.model.User;

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
 * Created by yanhoor on 2016/3/20.
 */
//在需要获取信息的地方调用getUserProfile(String userId)，最后设置listener获取更新后的user
public class GetUserProfileUtil {
    private static final String TAG="GetUserProfileUtil";

    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";

    private User mUser;
    private ArrayList<GalleryItem>mGalleryItems;
    private ArrayList<GalleryItem>mFavoritePhotos;
    private ArrayList<Group>mGroups;
    private ArrayList<PhotoSet>mPhotoSets;

    listener mPersonalProfileListener;

    public interface listener {
        void onUpdateFinish(User user);
    }

    public void setPersonalProfileListener(listener mListener){
        mPersonalProfileListener =mListener;
    }

    public User getUserProfile(String userId){
        Log.d(TAG,"getUserProfile");
        mUser=new User();
        mUser.setId(userId);
        getUserInfo();
        getFollowings();
        getUserPhoto();
        getPhotoSetList();
        getFavoritePhotos();
        Log.d(TAG,"user name is "+mUser.getUserName());
        return mUser;
    }

    public void getUserInfo() {
        HttpConfig config=new HttpConfig();
        config.cacheTime=0;

        Log.d(TAG,"fulltoken is "+MainLayoutActivity.fullToken);
        String[] mSignFullTokenStringArray = {"method" + "flickr.people.getInfo",
                "api_key" + LogInFragment.API_KEY, "auth_token" + MainLayoutActivity.fullToken,
                LogInFragment.PUBLIC_CODE, "user_id" + mUser.getId()};
        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.people.getInfo")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("user_id", mUser.getId())
                .appendQueryParameter("auth_token", MainLayoutActivity.fullToken)
                .appendQueryParameter("api_sig",apiSig)
                .build().toString();

        Log.d(TAG,"url is "+url);

        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                mPersonalProfileListener.onUpdateFinish(mUser);
                Log.d(TAG,"location is0"+mUser.getLocation()+"0");
            }

            @Override
            public void onSuccess(String t) {
                Log.d(TAG, "Getting user info from " + t);
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new StringReader(t));//事件类型初始化为START_DOCUMENT

                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && "person".equals(parser.getName())) {
                            String iconSever = parser.getAttributeValue(null, "iconserver");
                            String iconFarm = parser.getAttributeValue(null, "iconfarm");
                            String contact=parser.getAttributeValue(null,"contact");

                            mUser.setIconServer(iconSever);
                            mUser.setIconFarm(iconFarm);
                            mUser.setContact(contact);
                        }
                        if (eventType == XmlPullParser.START_TAG && "username".equals(parser.getName())) {
                            String userName = parser.nextText();
                            Log.d(TAG,"userName is "+userName);
                            mUser.setUserName(userName);
                        }

                        if (eventType == XmlPullParser.START_TAG && "realname".equals(parser.getName())) {
                            String realName = parser.nextText();
                            mUser.setRealName(realName);
                        }

                        if (eventType == XmlPullParser.START_TAG && "location".equals(parser.getName())) {
                            String location = parser.nextText().trim();
                            mUser.setLocation(location);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"description".equals(parser.getName())){
                            String description=parser.nextText();
                            mUser.setDescription(description);
                        }
                        eventType = parser.next();
                    }
                    Log.d(TAG,"userName is "+mUser.getUserName());

                } catch (XmlPullParserException xppe) {
                    xppe.printStackTrace();
                } catch (IOException ioe) {
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
        HttpConfig config=new HttpConfig();
        config.cacheTime=0;

        new KJHttp(config).get(url,new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                mPersonalProfileListener.onUpdateFinish(mUser);
            }

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
                        if (eventType==XmlPullParser.START_TAG&&"contacts".equals(parser.getName())){
                            String total=parser.getAttributeValue(null,"total");
                            mUser.setFollowingsNumber(total);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"contact".equals(parser.getName())){
                            User following=new User();
                            String followingId=parser.getAttributeValue(null,"nsid");
                            String followingUserName=parser.getAttributeValue(null,"username");
                            String iconServer=parser.getAttributeValue(null,"iconserver");
                            String iconFarm=parser.getAttributeValue(null,"iconfarm");

                            following.setId(followingId);
                            following.setUserName(followingUserName);
                            following.setIconServer(iconServer);
                            following.setIconFarm(iconFarm);
                            mFollowings.add(following);
                        }
                        eventType=parser.next();
                    }
                }catch (XmlPullParserException xppe){
                    xppe.printStackTrace();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
                mUser.getFollowingUsers().clear();
                mUser.setFollowingUsers(mFollowings);
            }
        });

    }

    public void getUserPhoto(){
        mGalleryItems=new ArrayList<>();

        String photoUrl= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method","flickr.people.getPhotos")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("user_id",mUser.getId())
                .appendQueryParameter("extras","url_s")
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(photoUrl,new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                mPersonalProfileListener.onUpdateFinish(mUser);
            }

            @Override
            public void onSuccess(String t) {
                mGalleryItems.clear();

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
                Log.d(TAG,"mGalleryItems size is "+mGalleryItems.size());
                mUser.setGalleryItems(mGalleryItems);
                Log.d(TAG,"Galleryitems size is "+mUser.getGalleryItems().size());
            }
        });
    }

    public void getGroups(Context context){
        mGroups=new ArrayList<>();

        String mFullToken= PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LogInFragment.PREF_FULL_TOKEN,null);

        //为flickr.stats.getPhotoStats方法获取api_sig
        String[] mSignFullTokenStringArray = {"method" + "flickr.people.getGroups","user_id"+mUser.getId(),
                "api_key" + LogInFragment.API_KEY, "auth_token" + mFullToken,
                LogInFragment.PUBLIC_CODE};
        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method","flickr.people.getGroups")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("user_id",mUser.getId())
                .appendQueryParameter("auth_token", mFullToken)
                .appendQueryParameter("api_sig", apiSig)
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                mPersonalProfileListener.onUpdateFinish(mUser);
            }

            @Override
            public void onSuccess(String t) {
                mGroups=new ArrayList<>();
                super.onSuccess(t);
                Log.d(TAG,"Getting group info from "+t);
                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"group".equals(parser.getName())){
                            Group group=new Group();
                            String id=parser.getAttributeValue(null,"nsid");
                            String name=parser.getAttributeValue(null,"name");
                            String iconFarm=parser.getAttributeValue(null,"iconfarm");
                            String iconServer=parser.getAttributeValue(null,"iconserver");
                            String members=parser.getAttributeValue(null,"members");
                            String poolCount=parser.getAttributeValue(null,"pool_count");

                            group.setId(id);
                            group.setGroupName(name);
                            group.setIconFarm(iconFarm);
                            group.setIconServer(iconServer);
                            group.setMemberNumber(members);
                            group.setPool_count(poolCount);
                            mGroups.add(group);
                        }
                        eventType=parser.next();
                    }
                }catch (XmlPullParserException xppe){
                    xppe.printStackTrace();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
                mUser.getGroups().clear();
                mUser.setGroups(mGroups);
            }
        });

    }

    public void getPhotoSetList(){
        mPhotoSets=new ArrayList<>();

        String[] mSignFullTokenStringArray = {"method" + "flickr.photosets.getList",
                "api_key" + LogInFragment.API_KEY, "auth_token" + MainLayoutActivity.fullToken,
                LogInFragment.PUBLIC_CODE, "user_id" + mUser.getId(),
        "primary_photo_extras"+"url_s"};
        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method","flickr.photosets.getList")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("user_id",mUser.getId())
                .appendQueryParameter("auth_token", MainLayoutActivity.fullToken)
                .appendQueryParameter( "primary_photo_extras","url_s")
                .appendQueryParameter("api_sig", apiSig)
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                mPersonalProfileListener.onUpdateFinish(mUser);
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG, "onSuccess: Getting photosets from "+t);
                mPhotoSets.clear();

                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){

                        if (eventType==XmlPullParser.START_TAG&&"photosets".equals(parser.getName())){
                            String total=parser.getAttributeValue(null,"total");
                            mUser.setPhotosetNum(total);
                        }

                        if ("photoset".equals(parser.getName())){
                            PhotoSet photoSet=new PhotoSet();
                            photoSet.setOwnerId(mUser.getId());
                            String id=parser.getAttributeValue(null,"id");
                            Log.d(TAG, "onSuccess: photoset id "+id);
                            photoSet.setId(id);

                            while(true){
                                eventType=parser.next();

                                if ("title".equals(parser.getName())){
                                    String title=parser.nextText();
                                    photoSet.setTitle(title);
                                }

                                if ("primary_photo_extras".equals(parser.getName())){
                                    String url=parser.getAttributeValue(null,"url_s");
                                    photoSet.setPrimaryPhotoUrl(url);
                                }

                                if (eventType==XmlPullParser.END_TAG&&"photoset".equals(parser.getName())){
                                    mPhotoSets.add(photoSet);
                                    break;
                                }
                            }

                        }

                        eventType=parser.next();
                    }
                }catch (XmlPullParserException xppe){
                    xppe.printStackTrace();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
                mUser.getPhotoSets().clear();
                Log.d(TAG, "onSuccess: mPhotosets size is "+mUser.getPhotoSets().size());
                mUser.setPhotoSets(mPhotoSets);
                Log.d(TAG, "onSuccess: mPhotosets size is "+mUser.getPhotoSets().size());
            }
        });
    }

    public void getFavoritePhotos(){
        mFavoritePhotos=new ArrayList<>();

        String[] mSignFullTokenStringArray = {"method" + "flickr.favorites.getList",
                "api_key" + LogInFragment.API_KEY, "auth_token" + MainLayoutActivity.fullToken,
                LogInFragment.PUBLIC_CODE, "user_id" + mUser.getId(),"format"+"rest",
                "extras"+"url_s"};
        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method","flickr.favorites.getList")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("user_id",mUser.getId())
                .appendQueryParameter("auth_token", MainLayoutActivity.fullToken)
                .appendQueryParameter("format","rest")
                .appendQueryParameter( "extras","url_s")
                .appendQueryParameter("api_sig", apiSig)
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                mPersonalProfileListener.onUpdateFinish(mUser);
            }

            @Override
            public void onSuccess(String t) {
                mFavoritePhotos.clear();
                super.onSuccess(t);
                Log.d(TAG, "onSuccess: favorite photo "+t);

                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while(eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"photos".equals(parser.getName())){
                            String total=parser.getAttributeValue(null,"total");
                            mUser.setFavoritesNum(total);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"photo".equals(parser.getName())){
                            GalleryItem photo=new GalleryItem();
                            String id=parser.getAttributeValue(null,"id");
                            String url=parser.getAttributeValue(null,"url_s");
                            photo.setId(id);
                            photo.setUrl(url);
                            mFavoritePhotos.add(photo);
                        }

                        eventType=parser.next();
                    }
                    mUser.getFavoritePhotos().clear();
                    mUser.setFavoritePhotos(mFavoritePhotos);
                }catch (XmlPullParserException xppe) {
                    xppe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
    }

}
