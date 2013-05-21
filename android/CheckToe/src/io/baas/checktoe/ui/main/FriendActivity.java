
package io.baas.checktoe.ui.main;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import io.baas.checktoe.R;
import io.baas.checktoe.ui.BaseActivity;

public class FriendActivity extends BaseActivity implements OnPageChangeListener, TabListener {

    private ViewPager mViewPager;

    FriendlistFragment mFollowingFragment;

    FriendlistFragment mFollowerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPager = (ViewPager)findViewById(R.id.pager);
        if (mViewPager != null) {
            // Phone setup
            mViewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
            mViewPager.setOnPageChangeListener(this);
            mViewPager.setPageMarginDrawable(R.drawable.grey_border_inset_lr);
            mViewPager.setPageMargin(getResources()
                    .getDimensionPixelSize(R.dimen.page_margin_width));

            final ActionBar actionBar = getSupportActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            // actionBar.addTab(actionBar.newTab().setText(R.string.auth_title).setTabListener(this));
            actionBar.addTab(actionBar.newTab().setText(R.string.tab_following)
                    .setTabListener(this));
            actionBar
                    .addTab(actionBar.newTab().setText(R.string.tab_follower).setTabListener(this));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onNewIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    private class MainPagerAdapter extends FragmentPagerAdapter {
        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mFollowingFragment = new FriendlistFragment();
                    mFollowingFragment.setMode(FriendlistFragment.FRIENDLIST_MODE_FOLLOWING);
                    return mFollowingFragment;
                case 1:
                    mFollowerFragment = new FriendlistFragment();
                    mFollowerFragment.setMode(FriendlistFragment.FRIENDLIST_MODE_FOLLOWER);
                    return mFollowerFragment;
            }

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageSelected(int position) {
        getSupportActionBar().setSelectedNavigationItem(position);
    }
}
