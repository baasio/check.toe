
package io.baas.checktoe.ui.main;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import io.baas.checktoe.ui.SimpleSinglePaneActivity;

public class EditChecklistActivity extends SimpleSinglePaneActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /*
     * (non-Javadoc)
     * @see com.kth.baasio.baassample.ui.SimpleSinglePaneActivity#onCreatePane()
     */
    @Override
    protected Fragment onCreatePane() {
        EditChecklistFragment fragment = new EditChecklistFragment();
        return fragment;
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
}
