package com.example.yanhoor.flickrgallery;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yanhoor.flickrgallery.model.Comment;
import com.example.yanhoor.flickrgallery.model.GalleryItem;
import com.example.yanhoor.flickrgallery.model.User;
import com.example.yanhoor.flickrgallery.util.PhotoInfoUtil;
import com.example.yanhoor.flickrgallery.util.StaticMethodUtil;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

import org.kymjs.kjframe.KJBitmap;
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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by yanhoor on 2016/3/15.
 */
public class PhotoDetailFragment extends Fragment  implements View.OnClickListener{
    private static final String TAG="PhotoDetailFragment";
    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";

    private String mFullToken;

    public static final String EXTRA_GALLERYITEM_mId ="com.example.yanhoor.photogallery.galleryItem_id";

    private GalleryItem mGalleryItem;

    RelativeLayout ownerLayout;
    ImageView ownerIcon;
    TextView userName;
    TextView title;
    ExpandableTextView description;
    TextView location;
    TextView checkPhotoset;
    TextView postedTime;
    ImageView mImageView;
    TextView commentNumber;
    RelativeLayout favoritesLayout;
    TextView favoritesText;
    TextView favoritesNumber;
    TextView viewsNumber;
    EditText editComment;
    Button sendComment;
    RecyclerView mRV;

    PhotoInfoUtil mPhotoInfoUtil;
    ArrayList<Comment>mComments;
    User mOwner;
    String mGalleryId;

    public static PhotoDetailFragment newPhotoDetailFragmentInstance(String mId){
        Bundle args=new Bundle();
        args.putSerializable(EXTRA_GALLERYITEM_mId,mId);

        PhotoDetailFragment fragment=new PhotoDetailFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private void updateData(){
        //获取照片信息，userName,description,location等
        mGalleryItem=mPhotoInfoUtil.getPhotoInfo(mGalleryItem);
        mGalleryItem=mPhotoInfoUtil.getFavorites(mGalleryItem);
        mGalleryItem=mPhotoInfoUtil.getAllContexts(mGalleryItem);
        getComments(mComments,mGalleryId);//获取评论
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG,"on Create");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mGalleryId=(String) getArguments().getSerializable(EXTRA_GALLERYITEM_mId);
        Log.d(TAG,"mGalleryId is "+mGalleryId);

        //获取评论
        mComments=new ArrayList<>();

        //判断是否登录
        mFullToken= PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(LogInFragment.PREF_FULL_TOKEN,null);
        String administratorId=PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(LogInFragment.PREF_USER_ID,null);
        if (administratorId==null){
            Toast.makeText(getActivity(),R.string.fullToken_unavailable,Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

        mGalleryItem= new GalleryItem();
        mGalleryItem.setId(mGalleryId);

        //用于获取照片信息后调用更新UI
        mPhotoInfoUtil=new PhotoInfoUtil();
        mPhotoInfoUtil.setListener(new PhotoInfoUtil.listener() {
            @Override
            public void onUpdateFinish(GalleryItem galleryItem) {
                mGalleryItem=galleryItem;
                updateUI();
            }
        });

        updateData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photo_detail,container,false);

        ownerLayout=(RelativeLayout)v.findViewById(R.id.owner_layout);
        ownerIcon=(ImageView)v.findViewById(R.id.owner_icon);
        userName=(TextView)v.findViewById(R.id.user_name);
        title=(TextView)v.findViewById(R.id.photo_title);
        description=(ExpandableTextView) v.findViewById(R.id.Photo_description);
        location=(TextView)v.findViewById(R.id.location_text);
        checkPhotoset=(TextView)v.findViewById(R.id.check_photoSet);
        postedTime=(TextView)v.findViewById(R.id.posted_time_text);
        mImageView=(ImageView) v.findViewById(R.id.photo_imageView);
        commentNumber=(TextView)v.findViewById(R.id.comment_number);
        favoritesLayout=(RelativeLayout)v.findViewById(R.id.favorites_layout);
        favoritesText=(TextView)v.findViewById(R.id.favorites_text);
        favoritesNumber=(TextView)v.findViewById(R.id.favorites_number);
        viewsNumber=(TextView)v.findViewById(R.id.views_number);
        editComment=(EditText)v.findViewById(R.id.comment_editText);
        sendComment=(Button)v.findViewById(R.id.send_comment_button);
        mRV=(RecyclerView)v.findViewById(R.id.comment_list_view_RV);

        favoritesLayout.setOnClickListener(this);
        ownerLayout.setOnClickListener(this);
        sendComment.setOnClickListener(this);
        mImageView.setOnClickListener(this);
        checkPhotoset.setOnClickListener(this);
        updateUI();

        return v;
    }

    void updateUI(){
        if (getActivity()==null){
            return;
        }

        mOwner=mGalleryItem.getOwner();

        Log.d(TAG,"icon url is "+mOwner.getUserIconUrl());
        new KJBitmap.Builder()
                .view(ownerIcon)
                .size(ownerIcon.getWidth(),ownerIcon.getHeight())
                .imageUrl(mOwner.getUserIconUrl())
                .display();

        userName.setText(mOwner.getUserName());

        title.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
        Log.d(TAG,"title is "+mGalleryItem.getTitle());
        title.setText(mGalleryItem.getTitle());

        Log.d(TAG,"description is "+mGalleryItem.getDescription());
        description.setText(mGalleryItem.getDescription());

        location.setText(mGalleryItem.getLocation());

        if (mGalleryItem.getPhotoSetId()!=null){
            checkPhotoset.setVisibility(View.VISIBLE);
        }

        postedTime.setText(mGalleryItem.getPostedTime());

        Log.d(TAG,"Getting detail photo from "+mGalleryItem.getLargePhotoUrl());
        //使用kjbitmap
        new KJBitmap.Builder()
                .view(mImageView)
                .imageUrl(mGalleryItem.getLargePhotoUrl())
                .display();

        Log.d(TAG,"mComments size is "+mComments.size());
        commentNumber.setText(String.valueOf(mComments.size()));

        if (mGalleryItem.getIsFavorite()!=null&&mGalleryItem.getIsFavorite().equals("1")){
            favoritesText.setTextColor(getResources().getColor(R.color.colorAccent));
        }else {
            favoritesText.setTextColor(getResources().getColor(R.color.colorBlackGray));
        }
        if (mGalleryItem.getTotalFavoritesNum()!=null){
            favoritesNumber.setText(mGalleryItem.getTotalFavoritesNum());
        }

        if (mGalleryItem.getViews()!=null){
            viewsNumber.setText(mGalleryItem.getViews());
        }

        mRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRV.setItemAnimator(new DefaultItemAnimator());
        mRV.setAdapter(new RVAdapter());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_comment_button:
                String commentContent=editComment.getText().toString().trim();//防止只输入空格
                if (commentContent.length()>0){
                    postComment(commentContent);
                }else {
                    Toast.makeText(getActivity(),R.string.content_empty,Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.owner_layout:
                Intent i=new Intent(getActivity(),UserProfileActivity.class);
                i.putExtra(UserProfileFragment.EXTRA_USER_ID,mGalleryItem.getUserId());
                startActivity(i);
                Log.d(TAG,"Going to user profile");
                break;

            case R.id.favorites_layout:
                if (mGalleryItem.getIsFavorite().equals("0")){
                    addAsFavorite();
                }else {
                    removeFavorite();
                }
                break;

            case R.id.photo_imageView:
                if (mGalleryItem!=null){
                    Intent viewPhotoIntent=new Intent(getActivity(),PhotoViewActivity.class);
                    viewPhotoIntent.putExtra(PhotoViewFragment.EXTRA_GALLERY_ITEM,mGalleryItem);
                    startActivity(viewPhotoIntent);
                }
                break;

            case R.id.check_photoSet:
                Intent checkPhotosetIntent=new Intent(getActivity(),PhotosetDetailActivity.class);
                checkPhotosetIntent.putExtra(PhotosetDetailFragment.EXTRA_PHOTOSET_ID,mGalleryItem.getPhotoSetId());
                checkPhotosetIntent.putExtra(PhotosetDetailFragment.EXTRA_USER_ID,mGalleryItem.getUserId());
                startActivity(checkPhotosetIntent);
                break;

            default:
                break;
        }
    }

    //获取评论
    public void getComments(final ArrayList<Comment>requestComments,String id){
        Log.d(TAG,"requestComments size in getComments is "+requestComments.size());
        final ArrayList<Comment> comments=new ArrayList<>();

        String url=Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.photos.comments.getList")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("photo_id",id)
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                ArrayList<Comment>newComments=new ArrayList<>();
                for (int i=comments.size()-1;i>=0;i--){
                    newComments.add(comments.get(i));
                }
                requestComments.clear();//先清除原来的，防止重复
                requestComments.addAll(newComments);
                Log.d(TAG,"requestComments size in getComments is "+requestComments.size());
                Log.d(TAG, "onSuccess: comments size is "+comments.size());
                updateUI();
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG,"Getting comments from "+t);
                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"comment".equals(parser.getName())){
                            User author=new User();
                            Comment comment=new Comment();
                            String id=parser.getAttributeValue(null,"id");
                            String authorId=parser.getAttributeValue(null,"author");
                            String authorName=parser.getAttributeValue(null,"authorname");
                            String iconServer=parser.getAttributeValue(null,"iconserver");
                            String iconFarm=parser.getAttributeValue(null,"iconfarm");
                            String dateCreate=parser.getAttributeValue(null,"datecreate");
                            String content=parser.nextText();

                            //unix timetamp转化为现在的ms要乘1000
                            Date mDate=new Date(Long.parseLong(dateCreate)*1000);
                            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MM-dd HH:mm", Locale.US);
                            String dateString=simpleDateFormat.format(mDate);
                            comment.setId(id);
                            author.setId(authorId);
                            author.setUserName(authorName);
                            author.setIconServer(iconServer);
                            author.setIconFarm(iconFarm);
                            comment.setAuthor(author);
                            comment.setDateCreate(dateString);
                            comment.setContent(content);
                            comments.add(comment);
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

    public void postComment(String commentText){
        String[] mSignFullTokenStringArray = {"method" + "flickr.photos.comments.addComment",
                "api_key" + LogInFragment.API_KEY, "auth_token" + mFullToken,
                LogInFragment.PUBLIC_CODE, "photo_id" + mGalleryItem.getId(),
                "comment_text"+commentText};

        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.photos.comments.addComment")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("photo_id", mGalleryItem.getId())
                .appendQueryParameter("auth_token", mFullToken)
                .appendQueryParameter("comment_text",commentText)
                .appendQueryParameter("api_sig", apiSig)
                .build().toString();

        Log.d(TAG, "postComment: url is "+url);
        new KJHttp().post(url, null, new HttpCallBack() {
            String commentId;

            @Override
            public void onFinish() {
                super.onFinish();
                if (commentId!=null){
                    updateData();
                    editComment.setText("");
                    Toast.makeText(getActivity(),R.string.comment_successfully,Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG,"Getting comment id from "+t);

                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"comment".equals(parser.getName())){
                            commentId=parser.getAttributeValue(null,"id");
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

    private void addAsFavorite(){
        String[] mSignFullTokenStringArray = {"method" + "flickr.favorites.add",
                "api_key" + LogInFragment.API_KEY, "auth_token" + mFullToken,
                LogInFragment.PUBLIC_CODE, "photo_id" + mGalleryItem.getId()};

        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.favorites.add")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("photo_id", mGalleryItem.getId())
                .appendQueryParameter("auth_token", mFullToken)
                .appendQueryParameter("api_sig", apiSig)
                .build().toString();

        new KJHttp().post(url, null, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG, "onSuccess: response is "+t);

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
                                Toast.makeText(getActivity(),R.string.add_as_fav_success,Toast.LENGTH_SHORT).show();
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

    private void removeFavorite(){
        String[] mSignFullTokenStringArray = {"method" + "flickr.favorites.remove",
                "api_key" + LogInFragment.API_KEY, "auth_token" + mFullToken,
                LogInFragment.PUBLIC_CODE, "photo_id" + mGalleryItem.getId()};

        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.favorites.remove")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("photo_id", mGalleryItem.getId())
                .appendQueryParameter("auth_token", mFullToken)
                .appendQueryParameter("api_sig", apiSig)
                .build().toString();

        new KJHttp().post(url, null, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);

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
                                Toast.makeText(getActivity(),R.string.remove_from_fav_success,Toast.LENGTH_SHORT).show();
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

    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.RVViewHolder>{
        @Override
        public RVViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RVViewHolder holder=new RVViewHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_comment,parent,false));

            return holder;
        }

        @Override
        public void onBindViewHolder(final RVViewHolder holder, final int position) {

            Log.d(TAG,"comment author icon url is "+mComments.get(position).getAuthor().getUserIconUrl());
            Picasso.with(getActivity())
                    .load(mComments.get(position).getAuthor().getUserIconUrl())
                    .resize(30,30)
                    .centerCrop()
                    .placeholder(R.drawable.brain_up_close)
                    .into(holder.authorIcon);

            holder.author.setText(mComments.get(position).getAuthor().getUserName());
            holder.content.setText(mComments.get(position).getContent());
            holder.time.setText(mComments.get(position).getDateCreate());

            holder.author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(getActivity(),UserProfileActivity.class);
                    i.putExtra(UserProfileFragment.EXTRA_USER_ID,mComments.get(holder.getAdapterPosition()).getAuthor().getId());
                    startActivity(i);
                    Log.d(TAG,"Going to user profile");
                }
            });
            holder.authorIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(getActivity(),UserProfileActivity.class);
                    i.putExtra(UserProfileFragment.EXTRA_USER_ID,mComments.get(holder.getAdapterPosition()).getAuthor().getId());
                    startActivity(i);
                    Log.d(TAG,"Going to user profile");
                }
            });

        }

            @Override
        public int getItemCount() {
            return mComments.size();
        }

        class RVViewHolder extends RecyclerView.ViewHolder{
            CircleImageView authorIcon;
            TextView author;
            TextView content;
            TextView time;

            public RVViewHolder(View view){
                super(view);
                authorIcon=(CircleImageView) view.findViewById(R.id.topic_author_icon);
                author=(TextView)view.findViewById(R.id.topic_author);
                content=(TextView)view.findViewById(R.id.topic_message);
                time=(TextView)view.findViewById(R.id.topic_time);

            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //StaticMethodUtil.cleanImageView(mImageView);
    }
}
