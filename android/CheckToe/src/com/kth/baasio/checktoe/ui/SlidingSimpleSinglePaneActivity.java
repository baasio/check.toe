
package com.kth.baasio.checktoe.ui;

import com.actionbarsherlock.view.MenuItem;
import com.kth.baasio.checktoe.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class SlidingSimpleSinglePaneActivity extends SlidingBaseActivity {

    private Fragment mMainFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_singlepane_empty);

        if (getIntent().hasExtra(Intent.EXTRA_TITLE)) {
            setTitle(getIntent().getStringExtra(Intent.EXTRA_TITLE));
        }

        if (savedInstanceState == null) {
            mMainFragment = onCreateMainFragment();
            mMainFragment.setArguments(intentToFragmentArguments(getIntent()));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.root_container, mMainFragment, "single_pane").commit();
        } else {
            mMainFragment = (Fragment)getSupportFragmentManager().findFragmentByTag("single_pane");
        }
    }

    protected abstract Fragment onCreateMainFragment();

    public Fragment getMainFragment() {
        return mMainFragment;
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

}
