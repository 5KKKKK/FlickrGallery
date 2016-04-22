package com.example.yanhoor.flickrgallery.controller;

import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.Group;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/3/26.
 */
public class ListGroupsFragment extends Fragment {
    private static final String TAG="ListGroupsFragment ";

    public static final String EXTRA_DATA_GROUPS ="data";

    private RecyclerView mRV;

    private ArrayList<Group>mGroups;

    public static ListGroupsFragment newInstance(ArrayList<Group> mData){
        Bundle args=new Bundle();
        args.putSerializable(EXTRA_DATA_GROUPS,mData);
        ListGroupsFragment fragment=new ListGroupsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        mGroups=new ArrayList<>();
        mGroups.addAll((ArrayList<Group>) getArguments().getSerializable(EXTRA_DATA_GROUPS));

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_following_list,container,false);

        mRV=(RecyclerView) v.findViewById(R.id.following_RecyclerView);
        mRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRV.setItemAnimator(new DefaultItemAnimator());
        updateUI();

        return v;
    }

    public void updateUI(){
        mRV.setAdapter(new ListRVAdapter());
    }

    private class ListRVAdapter extends RecyclerView.Adapter<ListRVAdapter.RVViewHolder>{
        @Override
        public RVViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RVViewHolder holder=new RVViewHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_following_group_list,parent,false));

            return holder;
        }

        @Override
        public void onBindViewHolder(final RVViewHolder holder, final int position) {
            Picasso.with(getActivity())
                    .load(mGroups.get(position).getGroupIconUrl())
                    .placeholder(R.drawable.brain_up_close)
                    .resize(20,20)
                    .centerCrop()
                    .into(holder.mIcon);

            holder.mName.setText(mGroups.get(position).getGroupName());
            holder.mName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(getActivity(),GroupProfileActivity.class);
                    i.putExtra(GroupProfileFragment.EXTRA_GROUP_ID,mGroups.get(position).getId());
                    startActivity(i);
                }
            });

        }

        @Override
        public int getItemCount() {
            return mGroups.size();
        }

        class RVViewHolder extends RecyclerView.ViewHolder{
            ImageView mIcon;
            TextView mName;

            public RVViewHolder(View view){
                super(view);
                mIcon=(ImageView)view.findViewById(R.id.following_icon);
                mName=(TextView)view.findViewById(R.id.following_user_name);
            }
        }

    }

}
