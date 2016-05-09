package com.example.yanhoor.flickrgallery.util;

import android.net.Uri;
import android.util.Log;

import com.example.yanhoor.flickrgallery.controller.LogInFragment;
import com.example.yanhoor.flickrgallery.controller.MainLayoutActivity;
import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.example.yanhoor.flickrgallery.model.Group;
import com.example.yanhoor.flickrgallery.model.Topic;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yanhoor on 2016/3/27.
 */
public class GetGroupProfileUtil {
    private static final String TAG="GetGroupProfileUtil";

    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";

    private GroupListener mMainThreadListener;
    private Group mGroup;
    private ArrayList<Topic>mTopics;
    private ArrayList<GalleryItem>mGalleryItems;

    public interface GroupListener{
        void onUpdateFinish(Group group);
    }

    public void setListener(GroupListener listener){
        mMainThreadListener=listener;
    }

    public void getGroupProfile(String mId){
        mGroup=new Group();
        mGroup.setId(mId);
        getGroupInfo();
        getGroupTopic();
        getGroupPhotos();
        getGroupMembers();
    }

    public void getGroupInfo(){

        String[] mSignFullTokenStringArray = {"method" + "flickr.groups.getInfo",
                "api_key" + LogInFragment.API_KEY, "auth_token" + MainLayoutActivity.fullToken,
                LogInFragment.PUBLIC_CODE, "group_id" + mGroup.getId()};
        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.groups.getInfo")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("group_id", mGroup.getId())
                .appendQueryParameter("auth_token", MainLayoutActivity.fullToken)
                .appendQueryParameter("api_sig",apiSig)
                .build().toString();

        Log.d(TAG,"Getting group info from "+url);

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                mMainThreadListener.onUpdateFinish(mGroup);
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG,"Getting group info from "+t);

                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"group".equals(parser.getName())){
                            String iconServer=parser.getAttributeValue(null,"iconserver");
                            String iconFarm=parser.getAttributeValue(null,"iconfarm");
                            String isMember=parser.getAttributeValue(null,"is_member");

                            Log.d(TAG,"is member "+isMember);
                            mGroup.setIsMember(isMember);
                            mGroup.setIconServer(iconServer);
                            mGroup.setIconFarm(iconFarm);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"name".equals(parser.getName())){
                            String name=parser.nextText();
                            mGroup.setGroupName(name);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"description".equals(parser.getName())){
                            String description=parser.nextText();
                            mGroup.setDescription(description);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"rules".equals(parser.getName())){
                            String rule=parser.nextText();
                            mGroup.setRule(rule);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"members".equals(parser.getName())){
                            String memberNum=parser.nextText();
                            mGroup.setMemberNumber(memberNum);
                        }

                        eventType=parser.next();
                    }
                }catch (XmlPullParserException xppe) {
                    xppe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
    }

    public void getGroupTopic(){
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.groups.discuss.topics.getList")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("group_id", mGroup.getId())
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                mMainThreadListener.onUpdateFinish(mGroup);
            }

            @Override
            public void onSuccess(String t) {
                mTopics=new ArrayList<>();
                super.onSuccess(t);
                Log.d(TAG,"Getting topics from "+t);

                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"topics".equals(parser.getName())){
                            String total=parser.getAttributeValue(null,"total");
                            mGroup.setTopicsSum(total);
                        }

                        if (eventType==XmlPullParser.START_TAG&&"topic".equals(parser.getName())){
                            Topic topic=new Topic();

                            User author=new User();
                            String id=parser.getAttributeValue(null,"id");
                            String subject=parser.getAttributeValue(null,"subject");
                            String authorId=parser.getAttributeValue(null,"author");
                            String authorName=parser.getAttributeValue(null,"authorname");
                            String iconServer=parser.getAttributeValue(null,"iconserver");
                            String iconFarm=parser.getAttributeValue(null,"iconfarm");

                            String countReply=parser.getAttributeValue(null,"count_replies");
                            String dateCreate=parser.getAttributeValue(null,"datecreate");
                            String message=parser.getAttributeValue(null,"message");
                            //unix timetamp转化为现在的ms要乘1000
                            Date mDate=new Date(Long.parseLong(dateCreate)*1000);
                            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                            String dateString=simpleDateFormat.format(mDate);

                            author.setId(authorId);
                            author.setIconServer(iconServer);
                            author.setUserName(authorName);
                            author.setIconFarm(iconFarm);
                            topic.setAuthor(author);
                            topic.setId(id);
                            topic.setSubject(subject);
                            topic.setCountReplies(countReply);
                            topic.setMessage(message);
                            topic.setDateCreate(dateString);
                            mTopics.add(topic);
                        }
                        eventType=parser.next();
                    }
                }catch (XmlPullParserException xppe) {
                    xppe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                mGroup.getTopics().clear();
                mGroup.setTopics(mTopics);
            }
        });

    }

    public void getGroupPhotos(){
        mGalleryItems=new ArrayList<>();
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.groups.pools.getPhotos")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("group_id", mGroup.getId())
                .appendQueryParameter("extras","url_s")
                .appendQueryParameter("per_page","6")
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                mMainThreadListener.onUpdateFinish(mGroup);
            }

            @Override
            public void onSuccess(String t) {
                mGalleryItems=new ArrayList<>();
                super.onSuccess(t);
                Log.d(TAG,"Getting group photo from "+t);

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
                }catch (XmlPullParserException xppe) {
                    xppe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                mGroup.getGalleryItems().clear();
                mGroup.setGalleryItems(mGalleryItems);
            }
        });

    }

    public void getGroupMembers(){
        String[] mSignFullTokenStringArray = {"method" + "flickr.groups.members.getList",
                "api_key" + LogInFragment.API_KEY, "auth_token" + MainLayoutActivity.fullToken,
                LogInFragment.PUBLIC_CODE, "group_id" + mGroup.getId()};
        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.groups.members.getList")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("group_id", mGroup.getId())
                .appendQueryParameter("auth_token", MainLayoutActivity.fullToken)
                .appendQueryParameter("api_sig",apiSig)
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                mMainThreadListener.onUpdateFinish(mGroup);
            }

            @Override
            public void onSuccess(String t) {
                ArrayList<User>members=new ArrayList<>();
                super.onSuccess(t);
                Log.d(TAG,"Getting group members from "+t);
                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"member".equals(parser.getName())){
                            User member=new User();
                            String id=parser.getAttributeValue(null,"nsid");
                            String userName=parser.getAttributeValue(null,"username");
                            String iconServer=parser.getAttributeValue(null,"iconserver");
                            String iconFarm=parser.getAttributeValue(null,"iconfarm");

                            member.setId(id);
                            member.setUserName(userName);
                            member.setIconServer(iconServer);
                            member.setIconFarm(iconFarm);
                            members.add(member);
                        }
                        eventType=parser.next();
                    }
                }catch (XmlPullParserException xppe) {
                    xppe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                mGroup.getMembers().clear();
                mGroup.setMembers(members);
            }
        });
    }

}
