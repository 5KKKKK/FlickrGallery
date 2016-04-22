package com.example.yanhoor.flickrgallery.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.Topic;
import com.example.yanhoor.flickrgallery.model.TopicReply;
import com.example.yanhoor.flickrgallery.model.User;
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
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by yanhoor on 2016/3/30.
 */
public class TopicDetailFragment extends Fragment  implements View.OnClickListener{
    private static final String TAG="TopicDetailFragmen";

    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";

    public static final String EXTRA_TOPIC="topic";

    private Topic mTopic;

    ImageView authorIcon;
    TextView authorName;
    TextView topicTime;
    TextView topicSubject;
    ExpandableTextView topicMessage;
    TextView replyNum;
    RecyclerView replyList;
    Button replyButton;

    //需要Topic的GroupId, topicId
    public static TopicDetailFragment newInstance(Topic topic){
        Bundle args=new Bundle();
        args.putSerializable(EXTRA_TOPIC,topic);
        TopicDetailFragment fragment=new TopicDetailFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTopic=new Topic();
        mTopic=(Topic)getArguments().getSerializable(EXTRA_TOPIC);
        updateData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_topic_detail,container,false);

        authorIcon=(ImageView)v.findViewById(R.id.topic_author_icon);
        authorName=(TextView)v.findViewById(R.id.topic_author);
        topicTime=(TextView)v.findViewById(R.id.topic_time);
        topicSubject=(TextView)v.findViewById(R.id.topic_subject);
        topicMessage=(ExpandableTextView) v.findViewById(R.id.topic_message);
        replyNum=(TextView)v.findViewById(R.id.topic_reply_num);
        replyList=(RecyclerView)v.findViewById(R.id.topic_reply_list);
        replyButton=(Button)v.findViewById(R.id.post_reply);

        replyButton.setOnClickListener(this);

        new KJBitmap.Builder().imageUrl(mTopic.getAuthor().getUserIconUrl()).view(authorIcon).display();
        authorName.setText(mTopic.getAuthor().getUserName());
        topicTime.setText(mTopic.getDateCreate());

        authorName.setOnClickListener(this);
        authorIcon.setOnClickListener(this);

        updateUI();

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.post_reply:
                Intent i=new Intent(getActivity(),TopicReplyActivity.class);
                i.putExtra(TopicReplyFragment.EXTRA_GROUP_ID,mTopic.getGroupId());
                i.putExtra(TopicReplyFragment.EXTRA_TOPIC_ID,mTopic.getId());
                startActivity(i);
                break;
            case R.id.topic_author:
            case R.id.topic_author_icon:
                Intent intent=new Intent(getActivity(),UserProfileActivity.class);
                intent.putExtra(UserProfileFragment.EXTRA_USER_ID,mTopic.getAuthor().getId());
                startActivity(intent);
                break;

        }
    }

    private void updateData(){
        getTopicInfo();
        getReply();
    }

    private void updateUI(){
        topicSubject.setText(mTopic.getSubject());
        topicMessage.setText(mTopic.getMessage());

        if (mTopic.getCountReplies()!=null){
            replyNum.setText(mTopic.getCountReplies());
        }

        if (mTopic.getTopicReplies()!=null){
            replyList.setLayoutManager(new LinearLayoutManager(getActivity()));
            replyList.setItemAnimator(new DefaultItemAnimator());
            replyList.setAdapter(new RVAdapter());
        }
        Log.d(TAG,"reply sum is "+mTopic.getTopicReplies().size());
    }

    private void getTopicInfo(){
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.groups.discuss.topics.getInfo")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("group_id", mTopic.getGroupId())
                .appendQueryParameter("topic_id",mTopic.getId())
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                updateUI();
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG,"Getting topic info from "+t);

                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"topic".equals(parser.getName())){
                            String subject=parser.getAttributeValue(null,"subject");
                            String countReply=parser.getAttributeValue(null,"count_replies");

                            mTopic.setSubject(subject);
                            mTopic.setCountReplies(countReply);
                        }
                        if (eventType==XmlPullParser.START_TAG&&"message".equals(parser.getName())){
                            String msg=parser.nextText();
                            mTopic.setMessage(msg);
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

    private void getReply(){
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.groups.discuss.replies.getList")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("group_id", mTopic.getGroupId())
                .appendQueryParameter("topic_id",mTopic.getId())
                .build().toString();

        HttpConfig config=new HttpConfig();
        config.cacheTime=0;
        new KJHttp(config).get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                updateUI();
            }

            @Override
            public void onSuccess(String t) {
                mTopic.getTopicReplies().clear();
                ArrayList<TopicReply>mReplies=new ArrayList<>();
                super.onSuccess(t);
                Log.d(TAG,"Getting topic reply from "+t);
                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while(eventType!=XmlPullParser.END_DOCUMENT){

                        if (eventType==XmlPullParser.START_TAG&&"reply".equals(parser.getName())){
                            TopicReply reply=new TopicReply();
                            User author=new User();
                            String id=parser.getAttributeValue(null,"id");
                            String authorId=parser.getAttributeValue(null,"author");
                            String authorName=parser.getAttributeValue(null,"authorname");
                            String iconServer=parser.getAttributeValue(null,"iconserver");
                            String iconFarm=parser.getAttributeValue(null,"iconfarm");
                            String dateCreate=parser.getAttributeValue(null,"datecreate");

                            author.setId(authorId);
                            author.setUserName(authorName);
                            author.setIconServer(iconServer);
                            author.setIconFarm(iconFarm);

                            Date mDate=new Date(Long.parseLong(dateCreate)*1000);
                            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                            String dateString=simpleDateFormat.format(mDate);
                            reply.setDateCreate(dateString);
                            reply.setId(id);
                            reply.setAuthor(author);

                            parser.next();
                            parser.next();

                            if ("message".equals(parser.getName())){
                                String replyContent=parser.nextText();
                                reply.setMessage(replyContent);
                                Log.d(TAG,"reply msg is "+replyContent);
                            }

                            mReplies.add(reply);
                        }

                        eventType=parser.next();
                    }
                }catch (XmlPullParserException xppe) {
                    xppe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                ArrayList<TopicReply>temps=new ArrayList<>();
                //倒序
                for (int i=mReplies.size()-1;i>=0;i--){
                    temps.add(mReplies.get(i));
                }
                mTopic.setTopicReplies(temps);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
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

            if (mTopic.getTopicReplies().get(position).getAuthor()!=null){
                Picasso.with(getActivity())
                        .load(mTopic.getTopicReplies().get(position).getAuthor().getUserIconUrl())
                        .resize(30,30)
                        .centerCrop()
                        .placeholder(R.drawable.brain_up_close)
                        .into(holder.replyAuthorIcon);

                holder.author.setText(mTopic.getTopicReplies().get(position).getAuthor().getUserName());
            }

            holder.content.setText(mTopic.getTopicReplies().get(position).getMessage());
            holder.time.setText(mTopic.getTopicReplies().get(position).getDateCreate());

            holder.author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(getActivity(),UserProfileActivity.class);
                    i.putExtra(UserProfileFragment.EXTRA_USER_ID,mTopic.getTopicReplies().get(holder.getAdapterPosition()).getAuthor().getId());
                    startActivity(i);
                    Log.d(TAG,"Going to user profile");
                }
            });
            holder.replyAuthorIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(getActivity(),UserProfileActivity.class);
                    i.putExtra(UserProfileFragment.EXTRA_USER_ID,mTopic.getTopicReplies().get(holder.getAdapterPosition()).getAuthor().getId());
                    startActivity(i);
                    Log.d(TAG,"Going to user profile");
                }
            });

        }

        @Override
        public int getItemCount() {
            return mTopic.getTopicReplies().size();
        }

        class RVViewHolder extends RecyclerView.ViewHolder{
            CircleImageView replyAuthorIcon;
            TextView author;
            TextView content;
            TextView time;

            public RVViewHolder(View view){
                super(view);
                replyAuthorIcon =(CircleImageView) view.findViewById(R.id.topic_author_icon);
                author=(TextView)view.findViewById(R.id.topic_author);
                content=(TextView)view.findViewById(R.id.topic_message);
                time=(TextView)view.findViewById(R.id.topic_time);
            }
        }

    }

}
