package com.example.yanhoor.flickrgallery.model;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/3/20.
 */
public class User implements Serializable{
    private static final String TAG="User";

    private String mUserName;
    private String mRealName;
    private String mId;
    private String mIconServer;
    private String mIconFarm;
    private String mLocation;
    private String mFollowingsNumber;
    private String mDescription;
    private String mContact;
    private String mPhotosetNum;
    private String mFavoritesNum;

    private ArrayList<User>mFollowingUsers=new ArrayList<>();
    private ArrayList<GalleryItem>mGalleryItems=new ArrayList<>();
    private ArrayList<Group>mGroups=new ArrayList<>();//从flickr.people.getGroups获得
    private ArrayList<PhotoSet>mPhotoSets=new ArrayList<>();
    private ArrayList<GalleryItem>mFavoritePhotos=new ArrayList<>();

    public ArrayList<GalleryItem> getGalleryItems() {
        return mGalleryItems;
    }

    public void setGalleryItems(ArrayList<GalleryItem> galleryItems) {
        mGalleryItems = galleryItems;
    }

    public String getIconFarm() {
        return mIconFarm;
    }

    public void setIconFarm(String iconFarm) {
        mIconFarm = iconFarm;
    }

    public String getIconServer() {
        return mIconServer;
    }

    public void setIconServer(String iconServer) {
        mIconServer = iconServer;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public String getRealName() {
        return mRealName;
    }

    public void setRealName(String realName) {
        mRealName = realName;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public ArrayList<User> getFollowingUsers() {
        return mFollowingUsers;
    }

    public void setFollowingUsers(ArrayList<User> followingUsers) {
        mFollowingUsers.addAll(followingUsers);
    }

    public String getFollowingsNumber() {
        return mFollowingsNumber;
    }

    public void setFollowingsNumber(String followingsNumber) {
        mFollowingsNumber = followingsNumber;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public ArrayList<Group> getGroups() {
        return mGroups;
    }

    public void setGroups(ArrayList<Group> groups) {
        mGroups.addAll(groups);
    }

    public String getContact() {
        return mContact;
    }

    public void setContact(String contact) {
        mContact = contact;
    }

    public ArrayList<PhotoSet> getPhotoSets() {
        return mPhotoSets;
    }

    public void setPhotoSets(ArrayList<PhotoSet> photoSets) {
        mPhotoSets.addAll(photoSets);
    }

    public String getPhotosetNum() {
        return mPhotosetNum;
    }

    public void setPhotosetNum(String photosetNum) {
        mPhotosetNum = photosetNum;
    }

    public String getFavoritesNum() {
        return mFavoritesNum;
    }

    public void setFavoritesNum(String favoritesNum) {
        mFavoritesNum = favoritesNum;
    }

    public ArrayList<GalleryItem> getFavoritePhotos() {
        return mFavoritePhotos;
    }

    public void setFavoritePhotos(ArrayList<GalleryItem> favoritePhotos) {
        mFavoritePhotos.addAll(favoritePhotos);
    }

    public String getUserPageUrl(){
        return "https://www.flickr.com/people/"+mId+"/";
    }

    public String getUserIconUrl(){
        String iconUrl;
        //http://farm{icon-farm}.staticflickr.com/{icon-server}/buddyicons/{nsid}.jpg
        if (mIconServer!=null){
            if (Integer.parseInt(mIconServer)>0){
                iconUrl= "http://farm"+mIconFarm+".staticflickr.com/"+mIconServer+"/buddyicons/"+mId+".jpg";
            }else {
                iconUrl= "https://www.flickr.com/images/buddyicon.gif";
            }
        }else {
            iconUrl= "https://www.flickr.com/images/buddyicon.gif";
        }
        Log.d(TAG,"User IconUrl is "+iconUrl);
        return iconUrl;
    }
}
