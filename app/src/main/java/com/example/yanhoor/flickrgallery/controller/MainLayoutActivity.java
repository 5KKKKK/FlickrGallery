package com.example.yanhoor.flickrgallery.controller;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.model.User;
import com.example.yanhoor.flickrgallery.util.GetUserProfileUtil;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by yanhoor on 2016/3/10.
 */
public class MainLayoutActivity extends FragmentActivity {
    private static final String TAG="MainLayoutActivity";

    public static String fullToken;
    public static String administratorId;

    private int[] stringId;
    GetUserProfileUtil mGetUserProfileUtil;
    private User administrator;

    private DrawerLayout mDrawerLayout;
    private LinearLayout drawerMenuList;
    private LinearLayout administratorLayout;
    private CircleImageView administratorIcon;
    private TextView administratorName;
    private RecyclerView menuList;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"on create");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_layout);

        fullToken=PreferenceManager.getDefaultSharedPreferences(this)
                .getString(LogInFragment.PREF_FULL_TOKEN,null);
        administratorId=PreferenceManager.getDefaultSharedPreferences(this)
                .getString(LogInFragment.PREF_USER_ID,null);

        if (TextUtils.isEmpty(fullToken)){
            Toast.makeText(this,R.string.fullToken_unavailable,Toast.LENGTH_SHORT).show();
            Intent i=new Intent(this,LogInActivity.class);
            startActivity(i);
        }

        administrator=new User();
        administrator.setId(administratorId);
        mGetUserProfileUtil=new GetUserProfileUtil();
        updateData();
        mGetUserProfileUtil.setPersonalProfileListener(new GetUserProfileUtil.listener() {
            @Override
            public void onUpdateFinish(User user) {
                administrator=user;
                updateUI();
            }
        });

        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        administratorLayout=(LinearLayout)findViewById(R.id.administrator_layout);
        administratorIcon=(CircleImageView)findViewById(R.id.administrator_icon_profile);
        administratorName=(TextView)findViewById(R.id.administrator_name);
        menuList=(RecyclerView)findViewById(R.id.menu_list_drawerLayout);
        mTabLayout=(TabLayout) findViewById(R.id.tabLayout);
        mViewPager=(ViewPager)findViewById(R.id.viewPager);
        mToolbar=(Toolbar)findViewById(R.id.toolbar);

        administratorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getBaseContext(),AdministratorProfileActivity.class);
                startActivity(i);
            }
        });

        mToolbar.setTitle("");
        setActionBar(mToolbar);

        FragmentManager fm=getSupportFragmentManager();
        pagerAdapter=new PagerAdapter(fm);
        mViewPager.setAdapter(pagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);

    }

    private void updateData(){
        mGetUserProfileUtil.getUserProfile(administratorId);
    }

    private void updateUI(){
        Picasso.with(this)
                .load(administrator.getUserIconUrl())
                .resize(100,100)
                .centerCrop()
                .into(administratorIcon);
        if (administrator.getUserName()!=null){
            administratorName.setText(administrator.getUserName());
        }

    }

    public class PagerAdapter extends FragmentStatePagerAdapter{

        public PagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new PhotoInterestingFragment();
                case 1:
                    return new PhotoLatestFragment();
                case 2:
                    return new PhotoContactsFragment();
                default:
                    return new PhotoInterestingFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "Interesting";
                case 1:
                    return "Latest";
                case 2:
                    return "Contacts";
                default:
                    return "Interesting";
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_photo_gallery,menu);

        MenuItem searchItem=menu.findItem(R.id.menu_item_search);
        SearchView searchView=(SearchView)searchItem.getActionView();

        SearchManager searchManager=(SearchManager)getSystemService(Context.SEARCH_SERVICE);
        ComponentName name=getComponentName();
        SearchableInfo searchableInfo=searchManager.getSearchableInfo(name);

        searchView.setSearchableInfo(searchableInfo);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_search:
                Intent searchIntent=new Intent(this,SearchActivity.class);
                startActivity(searchIntent);
                return true;

            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm=!PollService.isServiceAlarmOn(this);
                PollService.setServiceAlarm(this,shouldStartAlarm);
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
                    invalidateOptionsMenu();//刷新菜单项
                return true;

            case R.id.menu_item_login:
                Intent i=new Intent(this,LogInActivity.class);
                startActivity(i);
                return true;

            case R.id.menu_item_upload_photo:
                Intent uploadIntent=new Intent(this,UploadPhotoActivity.class);
                startActivity(uploadIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //更新选项菜单，除了菜单的首次创建外，每次菜单需要配置都会调用
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem toggleItem=menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(this)){
            toggleItem.setTitle(R.string.stop_polling);
        }else {
            toggleItem.setTitle(R.string.start_polling);
        }

        return true;
    }

}
