
package com.kth.baasio.checktoe.ui.main;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.kth.baasio.checktoe.R;
import com.kth.baasio.checktoe.ui.SlidingSimpleSinglePaneActivity;
import com.kth.baasio.checktoe.ui.slidingmenu.SlidingMenuFragment;
import com.kth.baasio.helpcenter.ui.HelpCenterActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class MainActivity extends SlidingSimpleSinglePaneActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment onCreateMainFragment() {
        // TODO Auto-generated method stub
        return new SampleListFragment();
    }

    @Override
    protected Fragment onCreateLeftPaneFragment() {
        // TODO Auto-generated method stub
        return new SlidingMenuFragment();
    }

    @Override
    protected Fragment onCreateRightPaneFragment() {
        // TODO Auto-generated method stub
        return new SampleListFragment();
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

}
