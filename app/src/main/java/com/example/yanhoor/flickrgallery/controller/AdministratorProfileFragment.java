package com.example.yanhoor.flickrgallery.controller;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.example.yanhoor.flickrgallery.model.Group;
import com.example.yanhoor.flickrgallery.model.User;
import com.example.yanhoor.flickrgallery.util.GetUserProfileUtil;
import com.example.yanhoor.flickrgallery.util.StaticMethodUtil;
import com.squareup.picasso.Picasso;

import org.kymjs.kjframe.KJBitmap;
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
 * Created by yanhoor on 2016/3/29.
 */
public class AdministratorProfileFragment extends Fragment implements View.OnClickListener{
    private static final String TAG="AdministratorProfile";

    private static final String ENDPOINT="https://api.flickr.com/services/rest/";

    private User mUser;
    private String mId;
    private ArrayList<User> mFollowings;
    private ArrayList<Group>mGroups;
    private int count;//用于记录成功删除的照片数
    private ProgressDialog progressDialog;

    ExpandableHeightGridView userPhotoGridView;
    TextView userName;
    RelativeLayout descriptionLayout;
    TextView userDescription;
    TextView followingNumber;
    RelativeLayout followingLayout;
    TextView groupNumber;
    RelativeLayout groupLayout;
    RelativeLayout photosetLayout;
    TextView photosetNumber;
    RelativeLayout favoritesLayout;
    TextView favoritesNum;
    RelativeLayout locationLayout;
    TextView locationTextView;
    ImageView buddyIconImageView;
    GetUserProfileUtil mGetUserProfileUtil;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mId= PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(LogInFragment.PREF_USER_ID,null);
        Log.d(TAG,"mId is "+mId);
        if (mId==null){
            Toast.makeText(getActivity(), R.string.fullToken_unavailable,Toast.LENGTH_SHORT).show();
            onDestroy();
            Intent i=new Intent(getActivity(),LogInActivity.class);
            startActivity(i);
        }
        mUser=new User();
        mUser.setId(mId);
        mGetUserProfileUtil=new GetUserProfileUtil();
        updateData();
        mGetUserProfileUtil.setPersonalProfileListener(new GetUserProfileUtil.listener() {
            @Override
            public void onUpdateFinish(User user) {
                mUser =user;
                updateUI();
            }
        });
    }

    private void updateData(){
        mGetUserProfileUtil.getUserProfile(mId);
        mGetUserProfileUtil.getGroups(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_administrator_profile,container,false);

        buddyIconImageView=(ImageView)v.findViewById(R.id.buddy_icon_administratorProfile);
        userName=(TextView)v.findViewById(R.id.user_name_administratorProfile);
        descriptionLayout=(RelativeLayout)v.findViewById(R.id.description_layout_administratorProfile);
        userDescription=(TextView)v.findViewById(R.id.user_description_administratorProfile);
        followingLayout=(RelativeLayout)v.findViewById(R.id.following_layout_administratorProfile);
        followingLayout.setOnClickListener(this);

        followingNumber=(TextView)v.findViewById(R.id.following_number_administratorProfile);
        groupNumber=(TextView)v.findViewById(R.id.group_number_administratorProfile);
        groupLayout=(RelativeLayout)v.findViewById(R.id.groupLayout_administratorProfile);
        groupLayout.setOnClickListener(this);

        photosetLayout=(RelativeLayout)v.findViewById(R.id.photoset_layout_administratorProfile);
        photosetNumber=(TextView)v.findViewById(R.id.photoset_num_administratorProfile);
        photosetLayout.setOnClickListener(this);

        favoritesLayout=(RelativeLayout)v.findViewById(R.id.favorites_layout_administratorProfile);
        favoritesNum=(TextView)v.findViewById(R.id.favorites_num_administratorProfile);
        favoritesLayout.setOnClickListener(this);

        locationLayout=(RelativeLayout)v.findViewById(R.id.location_layout_administratorProfile);
        locationTextView=(TextView)v.findViewById(R.id.location_administratorProfile);
        //使用自定义ExpandableHeightGridView防止与scrollview冲突
        userPhotoGridView=(ExpandableHeightGridView)v.findViewById(R.id.photo_gridView_administratorProfile);
        userPhotoGridView.setExpanded(true);
        userPhotoGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);//为上下文菜单登记视图
        userPhotoGridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater=mode.getMenuInflater();
                inflater.inflate(R.menu.delete_photo_context_menu,menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_delete_photo:
                        GalleryItemAdapter adapter=(GalleryItemAdapter)userPhotoGridView.getAdapter();
                        final ArrayList<String>mSelectedPhotoIds=new ArrayList<>();
                        for (int i=adapter.getCount()-1;i>=0;i--){
                            if (userPhotoGridView.isItemChecked(i)){
                                mSelectedPhotoIds.add(adapter.getItem(i).getId());
                            }
                        }

                        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.delete_photo_confirmation)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        progressDialog=new ProgressDialog(getActivity());
                                        progressDialog.setMessage(getResources().getString(R.string.deleting_message));;
                                        progressDialog.setCancelable(true);
                                        progressDialog.show();
                                        for (String id:mSelectedPhotoIds){
                                            deletePhoto(mSelectedPhotoIds.size(),id);
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create().show();

                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        updateUI();

        if (mUser.getGalleryItems().size()!=0){
            userPhotoGridView.setAdapter(new GalleryItemAdapter(mUser.getGalleryItems()));
        }

        userPhotoGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                GalleryItem item=mUser.getGalleryItems().get(position);
                Intent i=new Intent(getActivity(),PhotoDetailActivity.class);
                i.putExtra(PhotoDetailFragment.EXTRA_GALLERYITEM_mId,item.getId());
                startActivity(i);
            }
        });

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.following_layout_administratorProfile:
                mFollowings=new ArrayList<>();
                mFollowings.addAll(mUser.getFollowingUsers());
                if (mFollowings.size()>0){
                    Intent i=new Intent(getActivity(),ListActivity.class);
                    ListActivity.dataType="followings";
                    i.putExtra(ListFollowingsFragment.EXTRA_DATA_FOLLOWINGS,mFollowings);
                    startActivity(i);
                }
                break;

            case R.id.groupLayout_administratorProfile:
                mGroups=new ArrayList<>();
                mGroups.addAll(mUser.getGroups());
                if (mGroups.size()>0){
                    Intent i=new Intent(getActivity(),ListActivity.class);
                    ListActivity.dataType="groups";
                    i.putExtra(ListGroupsFragment.EXTRA_DATA_GROUPS,mGroups);
                    startActivity(i);
                }
                break;

            case R.id.photoset_layout_administratorProfile:
                if (mUser.getPhotosetNum()!=null&&!mUser.getPhotosetNum().equals("0")){
                    Intent photosetIntent=new Intent(getActivity(),ListActivity.class);
                    photosetIntent.putExtra(ListPhotosetFragment.EXTRA_PHOTOSET_DATA,mUser.getPhotoSets());
                    ListActivity.dataType="photosets";
                    startActivity(photosetIntent);
                }
                break;

            case R.id.favorites_layout_administratorProfile:
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
        //加载icon
        new KJBitmap.Builder().view(buddyIconImageView).imageUrl(mUser.getUserIconUrl()).display();
        if (mUser.getUserName()!=null){
            userName.setText(mUser.getUserName());
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

        //Log.d(TAG,"location length is "+mUser.getLocation().length());
        if (mUser.getLocation()!=null&&mUser.getLocation().length()!=0){
            locationLayout.setVisibility(View.VISIBLE);
            locationTextView.setText(mUser.getLocation());
        }

        if (mUser.getGalleryItems().size()>0&&getActivity()!=null){
            userPhotoGridView.setAdapter(new GalleryItemAdapter(mUser.getGalleryItems()));
        }else {
            userPhotoGridView.setAdapter(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: onResume");
        updateData();
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

    private void deletePhoto(final int sum,String photoId){
        String[] mSignFullTokenStringArray = {"method" + "flickr.photos.delete",
                "api_key" + LogInFragment.API_KEY, "auth_token" + MainLayoutActivity.fullToken,
                LogInFragment.PUBLIC_CODE, "photo_id" +photoId};

        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.photos.delete")
                .appendQueryParameter("api_key", LogInFragment.API_KEY)
                .appendQueryParameter("photo_id", photoId)
                .appendQueryParameter("auth_token", MainLayoutActivity.fullToken)
                .appendQueryParameter("api_sig", apiSig)
                .build().toString();

        new KJHttp().post(url, null, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                updateData();
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG, "onSuccess: delete photo response is "+t);

                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"rsp".equals(parser.getName())){
                            String state=parser.getAttributeValue(null,"stat");
                            if (state.equals("ok")){
                                count++;
                                if (count==sum){
                                    count=0;//清零用于继续删除
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(),R.string.delete_photo_successfully,Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        if (eventType==XmlPullParser.START_TAG&&"err".equals(parser.getName())){
                            String errorMessage=parser.getAttributeValue(null,"msg");
                            Toast.makeText(getActivity(),"Error occur uploading No"+(count+1)+"photo"+errorMessage,Toast.LENGTH_SHORT).show();
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

}
