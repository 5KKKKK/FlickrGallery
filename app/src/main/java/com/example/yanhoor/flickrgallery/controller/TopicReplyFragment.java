package com.example.yanhoor.flickrgallery.controller;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.Topic;
import com.example.yanhoor.flickrgallery.util.StaticMethodUtil;

import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

/**
 * Created by yanhoor on 2016/3/30.
 */
public class TopicReplyFragment extends Fragment {
    private static final String TAG="TopicReplyFragment";

    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";
    private static final String PUBLIC_CODE="a0e8c8d18675b5e2";
    public static final String EXTRA_GROUP_ID="group_id";
    public static final String EXTRA_TOPIC_ID="topic_id";

    private Topic mTopic;
    private String replyContent;
    private ProgressDialog progressDialog;
    EditText editReply;
    Button sendReply;

    public static TopicReplyFragment newInstance(String groupId,String topicId){
        Bundle args=new Bundle();
        args.putString(EXTRA_GROUP_ID,groupId);
        args.putString(EXTRA_TOPIC_ID,topicId);
        TopicReplyFragment fragment=new TopicReplyFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String groupId=getArguments().getString(EXTRA_GROUP_ID);
        String topicId=getArguments().getString(EXTRA_TOPIC_ID);
        mTopic=new Topic();
        mTopic.setGroupId(groupId);
        mTopic.setId(topicId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_topic_reply,container,false);

        editReply=(EditText)v.findViewById(R.id.edit_reply_topic);
        sendReply=(Button)v.findViewById(R.id.send_button_reply_topic);
        sendReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyContent=editReply.getText().toString().trim();
                if (replyContent!=null&&replyContent.length()>0){
                    progressDialog=new ProgressDialog(getActivity());
                    progressDialog.setMessage(getResources().getString(R.string.replying_message));
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                    postReply();
                }else {
                    Toast.makeText(getActivity(),R.string.content_empty,Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    private void postReply(){
        String[] mSignFullTokenStringArray = {"method" + "flickr.groups.discuss.replies.add",
                "api_key" +API_KEY, "auth_token" + MainLayoutActivity.fullToken,
                PUBLIC_CODE, "group_id" + mTopic.getGroupId(),
                "topic_id"+mTopic.getId(),"message"+replyContent};
        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", "flickr.groups.discuss.replies.add")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("group_id", mTopic.getGroupId())
                .appendQueryParameter("topic_id",mTopic.getId())
                .appendQueryParameter("message",replyContent)
                .appendQueryParameter("auth_token", MainLayoutActivity.fullToken)
                .appendQueryParameter("api_sig",apiSig)
                .build().toString();

        new KJHttp().post(url, null, new HttpCallBack() {
            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                progressDialog.dismiss();
                Log.d(TAG,"Getting reply topic msg from "+t);
                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){
                        if (eventType==XmlPullParser.START_TAG&&"rsp".equals(parser.getName())){
                            String state=parser.getAttributeValue(null,"stat");
                            if (state.equals("ok")){
                                Toast.makeText(getActivity(),R.string.reply_successfully,Toast.LENGTH_SHORT).show();
                                getActivity().finish();
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

}
