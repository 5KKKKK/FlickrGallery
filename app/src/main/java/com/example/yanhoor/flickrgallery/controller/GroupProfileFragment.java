package com.example.yanhoor.flickrgallery.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.example.yanhoor.flickrgallery.model.Group;
import com.example.yanhoor.flickrgallery.util.GetGroupProfileUtil;
import com.example.yanhoor.flickrgallery.util.StaticMethodUtil;
import com.ms.square.android.expandabletextview.ExpandableTextView;
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
 * Created by yanhoor on 2016/3/27.
 */
public class GroupProfileFragment extends Fragment  implements View.OnClickListener{
    private static final String TAG="GroupProfileFragment ";

    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";

    public static String EXTRA_GROUP_ID="groupId";

    private Group mGroup;
    private GetGroupProfileUtil mGetGroupProfileUtil;

    private ImageView mGroupIcon;
    private TextView mGroupName;
    private Button joinButton;
    private TextView mMemberNumber;
    private RelativeLayout memberLayout;
    RelativeLayout topicLayout;
    private TextView mTopicNumber;
    RelativeLayout ruleLayout;
    ExpandableTextView ruleContent;
    private RelativeLayout descriptionLayout;
    private ExpandableTextView mGroupDescription;
    private ExpandableHeightGridView mGroupPhotoGridview;

    String mGroupId;

    public static GroupProfileFragment newInstance(String mGroupId){
        Bundle args=new Bundle();
        args.putSerializable(EXTRA_GROUP_ID,mGroupId);
        GroupProfileFragment fragment=new GroupProfileFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private void updateData(){
        mGetGroupProfileUtil.getGroupProfile(mGroupId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mGroupId=(String) getArguments().getSerializable(EXTRA_GROUP_ID);
        mGroup=new Group();
        mGroup.setId(mGroupId);

        mGetGroupProfileUtil=new GetGroupProfileUtil();
        mGetGroupProfileUtil.setListener(new GetGroupProfileUtil.GroupListener() {
            @Override
            public void onUpdateFinish(Group group) {
                mGroup = group;
                updateUI();
            }
        });
        updateData();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_group_profile,container,false);
        mGroupIcon=(ImageView)v.findViewById(R.id.group_icon_profile);
        mGroupName=(TextView)v.findViewById(R.id.group_name_profile);
        joinButton=(Button)v.findViewById(R.id.join_group_button_groupProfile);
        memberLayout=(RelativeLayout)v.findViewById(R.id.member_layout_profile);
        mMemberNumber=(TextView)v.findViewById(R.id.member_number_profile);
        topicLayout=(RelativeLayout)v.findViewById(R.id.topic_layout);
        mTopicNumber=(TextView)v.findViewById(R.id.topic_number_profile);
        ruleLayout=(RelativeLayout)v.findViewById(R.id.rule_layout_groupProfile);
        ruleContent=(ExpandableTextView)v.findViewById(R.id.rule_content_groupProfile);
        descriptionLayout=(RelativeLayout)v.findViewById(R.id.description_layout_groupProfile);
        mGroupDescription=(ExpandableTextView) v.findViewById(R.id.description_content_groupProfile);
        mGroupPhotoGridview=(ExpandableHeightGridView) v.findViewById(R.id.photo_gridView_groupProfile);
        mGroupPhotoGridview.setExpanded(true);

        mGroupPhotoGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i=new Intent(getActivity(),PhotoDetailActivity.class);
                i.putExtra(PhotoDetailFragment.EXTRA_GALLERYITEM_mId,mGroup.getGalleryItems().get(position).getId());
                startActivity(i);
            }
        });

        memberLayout.setOnClickListener(this);
        joinButton.setOnClickListener(this);
        topicLayout.setOnClickListener(this);

        updateUI();

        return v;
    }

    public void updateUI(){
        if (mGroup.getIsMember()!=null){
            if (mGroup.getIsMember().equals("1")){
                mGroupName.setTextColor(getResources().getColor(R.color.colorGreenDark));
                joinButton.setText(R.string.leave_group);
                joinButton.setBackgroundResource(R.color.colorRedLight);
            }else {
                mGroupName.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                joinButton.setText(R.string.join_group);
                joinButton.setBackgroundResource(R.color.colorGreenDark);
            }
        }

        new KJBitmap.Builder().view(mGroupIcon).imageUrl(mGroup.getGroupIconUrl()).display();

        if (mGroup.getGroupName()!=null){
            mGroupName.setText(mGroup.getGroupName());
        }

        if (mGroup.getMemberNumber()!=null){
            mMemberNumber.setText(mGroup.getMemberNumber());
        }

        if (mGroup.getTopicsSum()!=null){
            mTopicNumber.setText(mGroup.getTopicsSum());
        }
        if (mGroup.getDescription()!=null){
            descriptionLayout.setVisibility(View.VISIBLE);
            mGroupDescription.setText(mGroup.getDescription());
        }

        Log.d(TAG,"rule is "+mGroup.getRule());
        if (!TextUtils.isEmpty(mGroup.getRule())){
            ruleLayout.setVisibility(View.VISIBLE);
            ruleContent.setText(mGroup.getRule());
        }

        if (mGroup.getGalleryItems().size()>0&&getActivity()!=null){
            mGroupPhotoGridview.setAdapter(new GridViewAdapter(mGroup.getGalleryItems()));
        }else {
            mGroupPhotoGridview.setAdapter(null);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.member_layout_profile:
                if (mGroup.getMembers().size()>0){
                    Intent i=new Intent(getActivity(),ListActivity.class);
                    ListActivity.dataType="followings";//将members作为followings类型数据展示
                    i.putExtra(ListFollowingsFragment.EXTRA_DATA_FOLLOWINGS,mGroup.getMembers());
                    startActivity(i);
                }
                break;

            case R.id.topic_layout:
                if (mGroup.getTopics().size()>0){
                    Intent i=new Intent(getActivity(),ListActivity.class);
                    i.putExtra(ListTopicsFragment.EXTRA_TOPICS_DATA,mGroup.getTopics());
                    ListActivity.dataType="topics";
                    startActivity(i);
                }
                break;

            case R.id.join_group_button_groupProfile:
                if (mGroup.getIsMember()!=null){
                    if (mGroup.getIsMember().equals("1")){
                        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.leave_group_message_dialog)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        leaveGroup();
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }else {
                        joinGroup();
                    }
                }

            default:
                break;
        }
    }

    private void leaveGroup(){
        String[] mSignFullTokenStringArray = {"method" + "flickr.groups.leave",
                "api_key" + LogInFragment.API_KEY, "auth_token" + MainLayoutActivity.fullToken,
                LogInFragment.PUBLIC_CODE, "group_id" + mGroup.getId()};
        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.groups.leave")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("group_id",mGroup.getId())
                .appendQueryParameter("auth_token", MainLayoutActivity.fullToken)
                .appendQueryParameter("api_sig",apiSig)
                .build().toString();
        Log.d(TAG,"leave group with url "+ url);

        new KJHttp().post(url, null, new HttpCallBack() {
            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG,"Getting leave group reply from "+t);

                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"rsp".equals(parser.getName())){
                            String state=parser.getAttributeValue(null,"stat");
                            if (state.equals("ok")){
                                updateData();
                                Toast.makeText(getActivity(),R.string.leave_group_successfully,Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (eventType==XmlPullParser.START_TAG&&"err".equals(parser.getName())){
                            String errorMessage=parser.getAttributeValue(null,"msg");
                            Toast.makeText(getActivity(),errorMessage,Toast.LENGTH_SHORT).show();
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

    private void joinGroup(){
        String[] mSignFullTokenStringArray = {"method" + "flickr.groups.join",
                "api_key" + LogInFragment.API_KEY, "auth_token" + MainLayoutActivity.fullToken,
                LogInFragment.PUBLIC_CODE, "group_id" + mGroup.getId()};
        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.groups.join")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("group_id",mGroup.getId())
                .appendQueryParameter("auth_token", MainLayoutActivity.fullToken)
                .appendQueryParameter("api_sig",apiSig)
                .build().toString();

        new KJHttp().post(url, null, new HttpCallBack() {

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG,"Join group reply "+t);

                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"rsp".equals(parser.getName())){
                            String state=parser.getAttributeValue(null,"stat");
                            if (state.equals("ok")){
                                updateData();
                                Toast.makeText(getActivity(),R.string.join_group_successfully,Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (eventType==XmlPullParser.START_TAG&&"err".equals(parser.getName())){
                            String errorMessage=parser.getAttributeValue(null,"msg");
                            Toast.makeText(getActivity(),errorMessage,Toast.LENGTH_SHORT).show();
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

    @Override
    public void onResume() {
        super.onResume();
    }

    private class GridViewAdapter extends ArrayAdapter<GalleryItem>{
        public GridViewAdapter(ArrayList<GalleryItem>items){
            super(getActivity().getApplicationContext(),0,items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.item_image_view,parent,false);
            }
            final ImageView imageView=(ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.brain_up_close);

            Picasso.with(getActivity())
                    .load(mGroup.getGalleryItems().get(position).getUrl())
                    .resize(240,240)
                    .centerCrop()
                    .into(imageView);

            return convertView;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGroupPhotoGridview=null;
        System.gc();
    }
}
