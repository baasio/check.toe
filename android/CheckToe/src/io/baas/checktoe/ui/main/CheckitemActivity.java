/**
 * 0. Project  : XXXX 프로젝트
 *
 * 1. FileName : UserForGroupActivity.java
 * 2. Package : com.kth.baasio.baassample.ui.main
 * 3. Comment : 
 * 4. 작성자  : Brad
 * 5. 작성일  : 2012. 12. 10. 오후 8:14:18
 * 6. 변경이력 : 
 *                    이름     : 일자          : 근거자료   : 변경내용
 *                   ------------------------------------------------------
 *                    Brad : 2012. 12. 10. :            : 신규 개발.
 */

package io.baas.checktoe.ui.main;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import io.baas.checktoe.ui.SimpleSinglePaneActivity;

public class CheckitemActivity extends SimpleSinglePaneActivity {

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
        CheckitemFragment fragment = new CheckitemFragment();
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
