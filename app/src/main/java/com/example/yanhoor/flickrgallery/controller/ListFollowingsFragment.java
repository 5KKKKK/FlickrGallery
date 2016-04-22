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
import com.example.yanhoor.flickrgallery.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/3/26.
 */
public class ListFollowingsFragment extends Fragment {
    private static final String TAG="ListGroupsFragment ";

    public static final String EXTRA_DATA_FOLLOWINGS ="data";

    private RecyclerView mRV;

    private ArrayList<User>mFollowings;

    public static ListFollowingsFragment newInstance(ArrayList<User> mData){
        Bundle args=new Bundle();
        args.putSerializable(EXTRA_DATA_FOLLOWINGS,mData);
        ListFollowingsFragment fragment=new ListFollowingsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        mFollowings=new ArrayList<>();
        mFollowings.addAll((ArrayList<User>) getArguments().getSerializable(EXTRA_DATA_FOLLOWINGS));

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
                    .load(mFollowings.get(position).getUserIconUrl())
                    .placeholder(R.drawable.brain_up_close)
                    .resize(20,20)
                    .centerCrop()
                    .into(holder.mIcon);

            holder.mName.setText(mFollowings.get(position).getUserName());
            holder.mName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mUserId=mFollowings.get(position).getId();
                    Intent i=new Intent(getActivity(),UserProfileActivity.class);
                    i.putExtra(UserProfileFragment.EXTRA_USER_ID,mUserId);
                    startActivity(i);
                }
            });

        }

        @Override
        public int getItemCount() {
            return mFollowings.size();
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
