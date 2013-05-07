
package com.kth.baasio.checktoe.ui;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.kth.baasio.checktoe.R;
import com.kth.baasio.helpcenter.ui.HelpCenterActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends SlidingBaseActivity {

    private TextView tvBaasio;

    private UUID savedUuid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager vp = (ViewPager)findViewById(R.id.pager);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), vp,
                getSupportActionBar());
        for (int i = 0; i < 3; i++) {
            adapter.addTab(new SampleListFragment());
        }

        // set the Behind View
        setBehindContentView(R.layout.frame);
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.frame, new SampleListFragment());
        t.commit();

        // customize the SlidingMenu
        this.setSlidingActionBarEnabled(true);
        getSlidingMenu().setShadowWidthRes(R.dimen.shadow_width);
        getSlidingMenu().setShadowDrawable(R.drawable.shadow);
        getSlidingMenu().setBehindOffsetRes(R.dimen.actionbar_home_width);
        getSlidingMenu().setBehindScrollScale(0.25f);

        // customize the ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_helpcenter:
                Intent intent = new Intent(MainActivity.this, HelpCenterActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class PagerAdapter extends FragmentPagerAdapter implements
            ViewPager.OnPageChangeListener, TabListener {

        private List<Fragment> mFragments = new ArrayList<Fragment>();

        private ViewPager mPager;

        private ActionBar mActionBar;

        public PagerAdapter(FragmentManager fm, ViewPager vp, ActionBar ab) {
            super(fm);
            mPager = vp;
            mPager.setAdapter(this);
            mPager.setOnPageChangeListener(this);
            mActionBar = ab;
        }

        public void addTab(Fragment frag) {
            mFragments.add(frag);
            mActionBar.addTab(mActionBar.newTab().setTabListener(this)
                    .setText("Tab " + mFragments.size()));
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            mPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }
    }
}
