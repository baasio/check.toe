
package io.baas.checktoe.ui.auth;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import io.baas.checktoe.ui.SimpleSinglePaneActivity;

public class SignUpActivity extends SimpleSinglePaneActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }

    /*
     * (non-Javadoc)
     * @see com.kth.baasio.baassample.ui.SimpleSinglePaneActivity#onCreatePane()
     */
    @Override
    protected Fragment onCreatePane() {
        // TODO Auto-generated method stub
        return new SignUpFragment();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onNewIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);

        // String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        //
        // if (!ObjectUtils.isEmpty(title)) {
        // getSupportActionBar().setTitle(title);
        // }
    }
}
