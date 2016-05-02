package com.example.yanhoor.flickrgallery.controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.Group;
import com.example.yanhoor.flickrgallery.model.User;
import com.example.yanhoor.flickrgallery.util.StaticMethodUtil;

import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by yanhoor on 2016/4/4.
 */
public class SearchActivity extends Activity implements CompoundButton.OnCheckedChangeListener{
    private static final String TAG="SearchActivity";
    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";

    private CheckBox photoCheckBox;
    private CheckBox groupCheckBox;
    private CheckBox emailCheckbox;
    private CheckBox userNameCheckBox;
    private SearchView searchView;
    private String searchType;
    private ProgressDialog progressDialog;
    private int checkCount=0;

    private String mFullToken;
    private ArrayList<Group>mGroups;
    private ArrayList<User>mUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mFullToken= PreferenceManager.getDefaultSharedPreferences(this)
                .getString(LogInFragment.PREF_FULL_TOKEN,null);

        searchView=(SearchView)findViewById(R.id.search_view);
        photoCheckBox=(CheckBox)findViewById(R.id.search_photo_checkBox);
        groupCheckBox=(CheckBox)findViewById(R.id.search_group_checkBox);
        emailCheckbox=(CheckBox)findViewById(R.id.search_user_by_Email_checkBox);
        userNameCheckBox=(CheckBox)findViewById(R.id.search_user_by_userName_checkBox);

        photoCheckBox.setOnCheckedChangeListener(this);
        groupCheckBox.setOnCheckedChangeListener(this);
        emailCheckbox.setOnCheckedChangeListener(this);
        userNameCheckBox.setOnCheckedChangeListener(this);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                handleSearchRequest(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    private void handleSearchRequest(String query){
        if (checkCount==0){
            Toast.makeText(this,R.string.no_select_search_activity,Toast.LENGTH_SHORT).show();
        }else if (checkCount>1){
            Toast.makeText(this,R.string.over_count_selected,Toast.LENGTH_SHORT).show();
        }else {
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage(getResources().getString(R.string.searching_message));
            progressDialog.setCancelable(true);
            progressDialog.show();
            if (searchType.equals("photo")){
                Intent searchIntent=new Intent(this,SearchProcessActivity.class);
                searchIntent.putExtra(SearchGalleryFragment.EXTRA_QUERY_GALLERY,query);
                startActivity(searchIntent);
                progressDialog.dismiss();
            }
            if (searchType.equals("group")){
                searchGroup(query);
            }
            if (searchType.equals("userEmail")){
                searchUserByEmail(query);
            }
            if (searchType.equals("userName")){
                searchUserByUserName(query);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.search_photo_checkBox:
                if (isChecked){
                    checkCount++;
                    searchType="photo";
                }else {
                    checkCount--;
                }
                break;

            case R.id.search_group_checkBox:
                if (isChecked){
                    checkCount++;
                    searchType="group";
                }else {
                    checkCount--;
                }
                break;

            case R.id.search_user_by_Email_checkBox:
                if (isChecked){
                    checkCount++;
                    searchType="userEmail";
                }else {
                    checkCount--;
                }
                break;

            case R.id.search_user_by_userName_checkBox:
                if (isChecked){
                    checkCount++;
                    searchType="userName";
                }else {
                    checkCount--;
                }
                break;

            default:
                break;
        }
    }

    private void searchGroup(String query){
        mGroups=new ArrayList<>();

        String[] mSignFullTokenStringArray = {"method" + "flickr.groups.search",
                "api_key" + LogInFragment.API_KEY, "auth_token" + mFullToken,
                LogInFragment.PUBLIC_CODE, "text"+query};

        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method","flickr.groups.search")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("text",query)
                .appendQueryParameter("auth_token", mFullToken)
                .appendQueryParameter("api_sig", apiSig)
                .build().toString();

        Log.d(TAG, "searchGroup: search group from "+url);

        new KJHttp().get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.dismiss();
                ListActivity.dataType="groups";
                Intent i=new Intent(getBaseContext(),ListActivity.class);
                i.putExtra(ListGroupsFragment.EXTRA_DATA_GROUPS,mGroups);
                startActivity(i);
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG, "onSuccess: search group result is "+t);

                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new StringReader(t));
                    int eventType = parser.getEventType();

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType==XmlPullParser.START_TAG&&"group".equals(parser.getName())){
                            Group group=new Group();
                            String id=parser.getAttributeValue(null,"nsid");
                            String name=parser.getAttributeValue(null,"name");
                            String iconServer=parser.getAttributeValue(null,"iconserver");
                            String iconFarm=parser.getAttributeValue(null,"iconfarm");

                            group.setId(id);
                            group.setGroupName(name);
                            group.setIconServer(iconServer);
                            group.setIconFarm(iconFarm);
                            mGroups.add(group);
                        }

                        eventType = parser.next();
                    }
                }catch (XmlPullParserException xppe){
                    xppe.printStackTrace();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
            }
        });
    }

    private void searchUserByEmail(String query){
        mUsers=new ArrayList<>();

        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method","flickr.people.findByEmail")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("find_email",query)
                .build().toString();

        new KJHttp().get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.dismiss();
                ListActivity.dataType="followings";
                Intent i=new Intent(getBaseContext(),ListActivity.class);
                i.putExtra(ListFollowingsFragment.EXTRA_DATA_FOLLOWINGS,mUsers);
                startActivity(i);
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG, "onSuccess: Getting users from "+t);

                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new StringReader(t));
                    int eventType = parser.getEventType();

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType==XmlPullParser.START_TAG&&"user".equals(parser.getName())){
                            User user=new User();
                            String nsid=parser.getAttributeValue(null,"nsid");
                            user.setId(nsid);

                            parser.next();
                            parser.next();
                            String userName=parser.nextText();
                            Log.d(TAG, "onSuccess: userNaem is "+userName);
                            user.setUserName(userName);

                            mUsers.add(user);
                        }

                        eventType = parser.next();
                    }
                }catch (XmlPullParserException xppe){
                    xppe.printStackTrace();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
            }
        });

    }

    private void searchUserByUserName(String query){
        mUsers=new ArrayList<>();

        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method","flickr.people.findByUsername")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("username",query)
                .build().toString();

        new KJHttp().get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.dismiss();
                ListActivity.dataType="followings";
                Intent i=new Intent(getBaseContext(),ListActivity.class);
                i.putExtra(ListFollowingsFragment.EXTRA_DATA_FOLLOWINGS,mUsers);
                startActivity(i);
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG, "onSuccess: Getting users from "+t);

                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new StringReader(t));
                    int eventType = parser.getEventType();

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType==XmlPullParser.START_TAG&&"user".equals(parser.getName())){
                            User user=new User();
                            String nsid=parser.getAttributeValue(null,"nsid");
                            user.setId(nsid);

                            parser.next();
                            parser.next();
                            String userName=parser.nextText();
                            Log.d(TAG, "onSuccess: userNaem is "+userName);
                            user.setUserName(userName);

                            mUsers.add(user);
                        }

                        eventType = parser.next();
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
