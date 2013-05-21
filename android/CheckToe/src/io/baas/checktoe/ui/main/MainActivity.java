
package io.baas.checktoe.ui.main;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.kth.baasio.Baas;
import com.kth.baasio.callback.BaasioAsyncTask;
import com.kth.baasio.callback.BaasioCallback;
import com.kth.baasio.callback.BaasioSignInAsyncTask;
import com.kth.baasio.callback.BaasioSignInCallback;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.exception.BaasioError;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.helpcenter.ui.HelpCenterActivity;
import com.kth.baasio.response.BaasioResponse;
import com.kth.baasio.utils.ObjectUtils;
import com.kth.common.utils.LogUtils;

import org.springframework.http.HttpMethod;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import io.baas.checktoe.R;
import io.baas.checktoe.ui.SlidingSimpleSinglePaneActivity;
import io.baas.checktoe.ui.slidingmenu.SlidingMenuFragment;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends SlidingSimpleSinglePaneActivity {

    public static final int MODE_MY_CHECKLIST = 0;

    public static final int MODE_RECOMMEND_CHECKLIST = 1;

    public static final int MODE_FAVORITE_CHECKLIST = 2;

    public static final int MODE_FRIEND = 3;

    private boolean mQuit = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mQuit = false;
    }

    @Override
    public void onBackPressed() {
        if (mQuit) {
            super.onBackPressed();
            return;
        }

        this.mQuit = true;
        Toast.makeText(this, getString(R.string.label_back_to_finish), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mQuit = false;
            }
        }, 2000);
    }

    // the meat of switching the above fragment
    public void switchFragment(int mode) {
        if (MainActivity.this == null)
            return;

        if (mode != MODE_FRIEND) {
            if (getMainFragment() instanceof ChecklistFragment) {
                ChecklistFragment main = (ChecklistFragment)getMainFragment();

                Bundle arguments = new Bundle();
                arguments.putInt("checklist_mode", mode);
                main.reloadFromArguments(arguments);

                getSlidingMenu().showContent();
            }
        } else {
            getSlidingMenu().showContent();

            Intent intent = new Intent(MainActivity.this, FriendActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected Fragment onCreateMainFragment() {
        // TODO Auto-generated method stub
        return new ChecklistFragment();
    }

    @Override
    protected Fragment onCreateLeftPaneFragment() {
        // TODO Auto-generated method stub
        return new SlidingMenuFragment();
    }

    @Override
    protected Fragment onCreateRightPaneFragment() {
        FriendlistFragment fragment = new FriendlistFragment();
        fragment.setMode(FriendlistFragment.FRIENDLIST_MODE_SLIDE_SEARCH_FRIEND);

        return fragment;

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
            case R.id.menu_changepassword: {
                changePasswordInBackground("rurururu", "11111111", new BaasioCallback<Boolean>() {

                    @Override
                    public void onResponse(Boolean response) {
                        if (response) {
                            Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onException(BaasioException e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
                break;
            }

            case R.id.menu_resetpassword: {
                resetPasswordInBackground("breadval@hotmail.com", new BaasioCallback<Boolean>() {

                    @Override
                    public void onResponse(Boolean response) {
                        if (response) {
                            Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onException(BaasioException e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
                break;
            }
            case R.id.menu_signin_with_revoke: {
                signInWithRevokeInBackground(MainActivity.this, "breadval@hotmail.com", "1111",
                        new BaasioSignInCallback() {

                            @Override
                            public void onException(BaasioException e) {
                                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG)
                                        .show();
                            }

                            @Override
                            public void onResponse(BaasioUser response) {
                                if (response != null) {
                                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG)
                                            .show();

                                    LogUtils.LOGV("revoke", response.toString());
                                } else {
                                    Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        });
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean changePassword(String oldPassword, String newPassword) throws BaasioException {
        BaasioUser user = Baas.io().getSignedInUser();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("oldpassword", oldPassword);
        params.put("newpassword", newPassword);
        BaasioResponse response = Baas.io().apiRequest(HttpMethod.POST, null, params,
                BaasioUser.ENTITY_TYPE, user.getUuid().toString(), "password");

        if (response != null) {
            return true;
        }

        throw new BaasioException(BaasioError.ERROR_UNKNOWN_NO_RESPONSE_DATA);
    }

    public void changePasswordInBackground(final String oldPassword, final String newPassword,
            final BaasioCallback<Boolean> callback) {
        (new BaasioAsyncTask<Boolean>(callback) {
            @Override
            public Boolean doTask() throws BaasioException {
                return changePassword(oldPassword, newPassword);
            }
        }).execute();
    }

    public boolean resetPassword(String email) throws BaasioException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("key", "value");

        BaasioResponse response = Baas.io().apiRequest(HttpMethod.POST, params, null,
                BaasioUser.ENTITY_TYPE, email, "resetpw");

        if (!ObjectUtils.isEmpty(response)) {
            return true;
        }

        throw new BaasioException(BaasioError.ERROR_UNKNOWN_NO_RESPONSE_DATA);
    }

    public void resetPasswordInBackground(final String email, final BaasioCallback<Boolean> callback) {
        (new BaasioAsyncTask<Boolean>(callback) {
            @Override
            public Boolean doTask() throws BaasioException {
                return resetPassword(email);
            }
        }).execute();
    }

    void signInWithRevokeInBackground(final Context context, final String username,
            final String password, final BaasioSignInCallback callback) {
        (new BaasioSignInAsyncTask(callback) {
            @Override
            public BaasioUser doTask() throws BaasioException {
                return signInWithRevoke(context, username, password);
            }
        }).execute();
    }

    BaasioUser signInWithRevoke(Context context, String username, String password)
            throws BaasioException {
        String accesstoken = Baas.io().getAccessToken();

        if (!ObjectUtils.isEmpty(accesstoken)) {
            BaasioUser.signOut(context);
        }

        BaasioUser user = BaasioUser.signIn(context, username, password);

        BaasioResponse response = Baas.io().apiRequest(HttpMethod.POST, null, null, "users",
                user.getUniqueKey(), "revoketokens");
        if (response != null) {
            BaasioUser.signOut(context);

            return BaasioUser.signIn(context, username, password);
        }

        throw new BaasioException(BaasioError.ERROR_UNKNOWN_NO_RESPONSE_DATA);
    }
}
