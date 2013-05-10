
package io.baas.checktoe.ui;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import io.baas.checktoe.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public abstract class SlidingBaseActivity extends SlidingFragmentActivity {
    protected ImageLoader mImageLoader = ImageLoader.getInstance();

    private Fragment mLeftPaneFragment;

    private Fragment mRightPaneFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

        // set the Behind View
        setBehindContentView(R.layout.menu_frame);
        if (savedInstanceState == null) {
            FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
            mLeftPaneFragment = onCreateLeftPaneFragment();
            t.replace(R.id.menu_frame, mLeftPaneFragment);
            t.commit();
        } else {
            mLeftPaneFragment = (Fragment)this.getSupportFragmentManager().findFragmentById(
                    R.id.menu_frame);
        }

        // set the Right Behind View
        if (savedInstanceState == null) {
            mRightPaneFragment = onCreateRightPaneFragment();
            if (mRightPaneFragment != null) {
                getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
                getSlidingMenu().setSecondaryMenu(R.layout.menu_frame_two);
                getSlidingMenu().setSecondaryShadowDrawable(R.drawable.shadowright);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.menu_frame_two, mRightPaneFragment).commit();
            } else {
                getSlidingMenu().setMode(SlidingMenu.LEFT);
            }
        } else {
            Fragment fragment = (Fragment)this.getSupportFragmentManager().findFragmentById(
                    R.id.menu_frame_two);
            if (fragment != null) {
                mRightPaneFragment = fragment;
            } else {
                getSlidingMenu().setMode(SlidingMenu.LEFT);
            }
        }

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected abstract Fragment onCreateLeftPaneFragment();

    protected abstract Fragment onCreateRightPaneFragment();

    public Fragment getLeftPaneFragment() {
        return mLeftPaneFragment;
    }

    public Fragment getRightPaneFragment() {
        return mRightPaneFragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /**
     * Converts an intent into a {@link Bundle} suitable for use as fragment
     * arguments.
     */
    public static Bundle intentToFragmentArguments(Intent intent) {
        Bundle arguments = new Bundle();
        if (intent == null) {
            return arguments;
        }

        final Uri data = intent.getData();
        if (data != null) {
            arguments.putParcelable("_uri", data);
        }

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            arguments.putAll(intent.getExtras());
        }

        return arguments;
    }

    /**
     * Converts a fragment arguments bundle into an intent.
     */
    public static Intent fragmentArgumentsToIntent(Bundle arguments) {
        Intent intent = new Intent();
        if (arguments == null) {
            return intent;
        }

        final Uri data = arguments.getParcelable("_uri");
        if (data != null) {
            intent.setData(data);
        }

        intent.putExtras(arguments);
        intent.removeExtra("_uri");
        return intent;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getTracker().trackActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getTracker().trackActivityStop(this);
    }
}
