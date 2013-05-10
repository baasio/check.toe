
package io.baas.checktoe.ui.main;

import static com.kth.common.utils.LogUtils.makeLogTag;

import io.baas.checktoe.R;
import io.baas.checktoe.ui.BaseActivity;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

public class SearchableActivity extends BaseActivity {
    private static final String TAG = makeLogTag(SearchableActivity.class);

    private ChecklistFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_singlepane_empty);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        mFragment = (ChecklistFragment)fm.findFragmentById(R.id.root_container);
        if (mFragment == null) {
            mFragment = new ChecklistFragment();
            fm.beginTransaction().add(R.id.root_container, mFragment).commit();
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
        String query = intent.getStringExtra(SearchManager.QUERY);

        Uri uri = intent.getData();

        if (uri != null) {
            final Intent intent2 = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent2);

            finish();
        } else {
            getSupportActionBar().setTitle(getResources().getString(R.string.search_title, query));

            mFragment.reloadFromArguments(intentToFragmentArguments(intent));
        }
    }
}
