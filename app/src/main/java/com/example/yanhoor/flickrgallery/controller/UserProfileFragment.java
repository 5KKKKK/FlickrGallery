package com.example.yanhoor.flickrgallery.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.example.yanhoor.flickrgallery.model.Group;
import com.example.yanhoor.flickrgallery.model.User;
import com.example.yanhoor.flickrgallery.util.GetUserProfileUtil;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

import org.kymjs.kjframe.KJBitmap;

import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/3/20.
 */
public class UserProfileFragment extends Fragment  implements View.OnClickListener{
    private static  final String TAG="UserProfileFragment";

    private User mUser;
    private String mUserId;
    private ArrayList<User>mFollowings;
    private ArrayList<Group>mGroups;
    ExpandableHeightGridView userPhotoGridView;
    TextView userName;
    TextView personalPage;
    RelativeLayout descriptionLayout;
    ExpandableTextView userDescription;
    TextView followingNumber;
    RelativeLayout followingLayout;
    TextView groupNumber;
    RelativeLayout groupLayout;
    RelativeLayout photosetLayout;
    TextView photosetNumber;
    RelativeLayout favoritesLayout;
    TextView favoritesNum;
    TextView locationTextView;
    RelativeLayout locationLayout;
    TextView location;
    ImageView buddyIconImageView;
    GetUserProfileUtil mGetUserProfileUtil;

    String contact;

    public static final String EXTRA_USER_ID="com.example.yanhoor.flickrgallery.controller.UserProfileFragment.user_Id";

    public static Fragment newUserProfileFragmentInstance(String userId){
        Bundle args=new Bundle();
        args.putSerializable(EXTRA_USER_ID,userId);

        UserProfileFragment fragment=new UserProfileFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private void updateData(){
        mGetUserProfileUtil.getUserProfile(mUserId);
        mGetUserProfileUtil.getGroups(getActivity());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId=(String)getArguments().getSerializable(EXTRA_USER_ID);
        mUser=new User();
        mUser.setId(mUserId);
        mGetUserProfileUtil=new GetUserProfileUtil();
        updateData();
        mGetUserProfileUtil.setPersonalProfileListener(new GetUserProfileUtil.listener() {
            @Override
            public void onUpdateFinish(User user) {
                mUser=user;
                Log.d(TAG,"userName is "+mUser.getUserName());
                updateUI();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_user_profile,container,false);

        buddyIconImageView=(ImageView)v.findViewById(R.id.buddy_icon_profile);
        userName=(TextView)v.findViewById(R.id.user_name_profile);
        personalPage =(TextView) v.findViewById(R.id.personal_page_text_profile);
        personalPage.setOnClickListener(this);
        descriptionLayout=(RelativeLayout)v.findViewById(R.id.description_layout_profile);
        userDescription=(ExpandableTextView) v.findViewById(R.id.user_description_profile);
        followingLayout=(RelativeLayout)v.findViewById(R.id.following_layout);
        followingLayout.setOnClickListener(this);

        followingNumber=(TextView)v.findViewById(R.id.following_number_profile);
        groupNumber=(TextView)v.findViewById(R.id.group_number_profile);
        groupLayout=(RelativeLayout)v.findViewById(R.id.groupLayout_profile);
        groupLayout.setOnClickListener(this);

        photosetLayout=(RelativeLayout)v.findViewById(R.id.photoset_layout_profile);
        photosetNumber=(TextView)v.findViewById(R.id.photoset_num_profile);
        photosetLayout.setOnClickListener(this);

        favoritesLayout=(RelativeLayout)v.findViewById(R.id.favorites_layout_profile);
        favoritesNum=(TextView)v.findViewById(R.id.favorites_num_profile);
        favoritesLayout.setOnClickListener(this);

        locationLayout=(RelativeLayout)v.findViewById(R.id.location_layout);
        locationTextView=(TextView)v.findViewById(R.id.location_profile);
        location=(TextView)v.findViewById(R.id.location_text_profile);
        //使用自定义ExpandableHeightGridView防止与scrollview冲突
        userPhotoGridView=(ExpandableHeightGridView)v.findViewById(R.id.photo_gridView_profile);
        userPhotoGridView.setExpanded(true);

        updateUI();

        if (mUser.getGalleryItems().size()!=0){
            userPhotoGridView.setAdapter(new GalleryItemAdapter(mUser.getGalleryItems()));
        }

        userPhotoGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                GalleryItem item=mUser.getGalleryItems().get(position);
                Intent i=new Intent(getActivity(),PhotoDetailActivity.class);
                //用于代替PhotoDetailActivity实现滑动查看图片详情
                //Intent i=new Intent(getActivity(),PhotoPageActivity.class);
                i.putExtra(PhotoDetailFragment.EXTRA_GALLERYITEM_mId,item.getId());
                startActivity(i);
            }
        });

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.following_layout:
                mFollowings=new ArrayList<>();
                mFollowings.addAll(mUser.getFollowingUsers());
                if (mFollowings.size()>0){
                    Intent i=new Intent(getActivity(),ListActivity.class);
                    ListActivity.dataType="followings";
                    i.putExtra(ListFollowingsFragment.EXTRA_DATA_FOLLOWINGS,mFollowings);
                    startActivity(i);
                }
                break;

            case R.id.groupLayout_profile:
                mGroups=new ArrayList<>();
                mGroups.addAll(mUser.getGroups());
                if (mGroups.size()>0){
                    Intent i=new Intent(getActivity(),ListActivity.class);
                    ListActivity.dataType="groups";
                    i.putExtra(ListGroupsFragment.EXTRA_DATA_GROUPS,mGroups);
                    startActivity(i);
                }
                break;

            case R.id.personal_page_text_profile:
                Intent i=new Intent(getActivity(),WebViewActivity.class);
                i.putExtra(WebViewFragment.EXTRA_URL,mUser.getUserPageUrl());
                startActivity(i);
                break;

            case R.id.photoset_layout_profile:
                if (mUser.getPhotosetNum()!=null&&!mUser.getPhotosetNum().equals("0")){
                    Intent photosetIntent=new Intent(getActivity(),ListActivity.class);
                    Log.d(TAG, "onClick: mPhotosets size is "+mUser.getPhotoSets().size());
                    photosetIntent.putExtra(ListPhotosetFragment.EXTRA_PHOTOSET_DATA,mUser.getPhotoSets());
                    ListActivity.dataType="photosets";
                    startActivity(photosetIntent);
                }
                break;

            case R.id.favorites_layout_profile:
                if (mUser.getFavoritePhotos().size()>0){
                    Intent favoritesIntent=new Intent(getActivity(),FavoritePhotosActivity.class);
                    favoritesIntent.putExtra(FavoritePhotosFragment.EXTRA_FAVORITE_PHOTOS,mUser.getFavoritePhotos());
                    startActivity(favoritesIntent);
                }
                break;

            default:
                break;
        }
    }

    void updateUI(){
        contact=mUser.getIsContact();

        //加载icon
        new KJBitmap.Builder().view(buddyIconImageView).imageUrl(mUser.getUserIconUrl()).display();
        if (mUser.getUserName()!=null){
            userName.setText(mUser.getUserName());
            if (mUser.getIsContact()!=null){
                if (mUser.getIsContact().equals("1")){
                    userName.setTextColor(getResources().getColor(R.color.colorGreenDark));
                }
            }
        }

        if (mUser.getDescription()!=null){
            descriptionLayout.setVisibility(View.VISIBLE);
            userDescription.setText(mUser.getDescription());
        }

        if (mUser.getFollowingsNumber()!=null){
            followingNumber.setText(mUser.getFollowingsNumber());
        }

        if (mUser.getGroups().size()>0){
            groupNumber.setText(String.valueOf(mUser.getGroups().size()));
        }

        if (mUser.getPhotosetNum()!=null){
            photosetNumber.setText(mUser.getPhotosetNum());
        }

        if (mUser.getFavoritesNum()!=null){
            favoritesLayout.setVisibility(View.VISIBLE);
            favoritesNum.setText(mUser.getFavoritesNum());
        }

        Log.d(TAG,"location is "+mUser.getLocation());
        if (!TextUtils.isEmpty(mUser.getLocation())){
            locationLayout.setVisibility(View.VISIBLE);
            locationTextView.setText(mUser.getLocation());
        }

        if (mUser.getGalleryItems().size()>0&&getActivity()!=null){
            userPhotoGridView.setAdapter(new GalleryItemAdapter(mUser.getGalleryItems()));
        }else {
            userPhotoGridView.setAdapter(null);
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
        public GalleryItemAdapter(ArrayList<GalleryItem> items){
            super(getActivity().getApplicationContext(),0,items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG,"arrayAdapter, getView");
            if (convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.item_image_view,parent,false);
            }
            final ImageView imageView=(ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.brain_up_close);

            Picasso.with(getActivity())
                    .load(mUser.getGalleryItems().get(position).getUrl())
                    .resize(240,240)
                    .centerCrop()
                    .into(imageView);

            return convertView;
        }
    }

}
