package com.example.yanhoor.flickrgallery.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.Topic;

import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/3/27.
 */
public class ListTopicsFragment extends Fragment {
    private static final String TAG="ListTopicsFragment";

    public static String EXTRA_TOPICS_DATA="topics";

    private ArrayList<Topic>mTopics;

    public static ListTopicsFragment newInstance(ArrayList<Topic>topics){
        Bundle args=new Bundle();
        args.putSerializable(EXTRA_TOPICS_DATA,topics);
        ListTopicsFragment fragment=new ListTopicsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTopics=new ArrayList<>();
        mTopics.addAll((ArrayList<Topic>)getArguments().getSerializable(EXTRA_TOPICS_DATA));
        Log.d(TAG,"mTopics size is "+mTopics.size());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_topics_list,container,false);

        ListView listView=(ListView)v.findViewById(R.id.topics_list_view);
        listView.setAdapter(new ListViewAdapter(getActivity(),R.layout.item_text_view,mTopics));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i=new Intent(getActivity(),TopicDetailActivity.class);
                i.putExtra(TopicDetailFragment.EXTRA_TOPIC,mTopics.get(position));
                startActivity(i);
            }
        });

        return v;
    }

    private class ListViewAdapter extends ArrayAdapter<Topic>{
        private int resourceId;

        public ListViewAdapter(Context context,int textViewResourceId,ArrayList<Topic> objects){
            super(context,textViewResourceId,objects);
            resourceId=textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            ViewHolder viewHolder;
            if (convertView==null){
                v=LayoutInflater.from(getActivity()).inflate(resourceId,null);
                viewHolder=new ViewHolder();
                viewHolder.subject=(TextView)v.findViewById(R.id.textView_list);
                v.setTag(viewHolder);
            }else {
                v=convertView;
                viewHolder=(ViewHolder) v.getTag();
            }
            viewHolder.subject.setText(getItem(position).getSubject());
            return v;
        }

        private class ViewHolder{
            TextView subject;
        }
    }

}
