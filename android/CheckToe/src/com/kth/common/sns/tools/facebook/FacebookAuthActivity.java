
package com.kth.common.sns.tools.facebook;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.facebook.Session;
import com.facebook.SessionState;
import com.kth.baasio.callback.BaasioSignInCallback;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.helpcenter.ui.dialog.DialogUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class FacebookAuthActivity extends SherlockFragmentActivity {
    public static final String _TAG = "FacebookAuthActivity";

    private Context mContext;

    public static final String INTENT_REQUEST_CODE = "request_code";

    public static final int REQUEST_MODE_SIGNIN_VIA_FACEBOOK = 1;

    public static final int REQUEST_MODE_SIGNUP_VIA_FACEBOOK = 2;

    public static final int REQUEST_MODE_SHARE_VIA_FACEBOOK = 3;

    public static final String INTENT_RESULT_ERROR = "error";

    public static final String INTENT_RESULT_ERROR_BODY = "error_body";

    public static final String INTENT_RESULT_TOKEN = "token";

    public static final String INTENT_RESULT_USER = "user";

    private int mRequestMode = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        mRequestMode = getIntent().getIntExtra(INTENT_REQUEST_CODE, -1);

        // start Facebook Login
        Session.openActiveSession(this, true, new Session.StatusCallback() {

            // callback when session changes state
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {
                    String token = session.getAccessToken();

                    DialogUtils.showProgressDialog(FacebookAuthActivity.this, "facebook_login",
                            "로그인 중입니다.");

                    // showProgressDialog("로그인 중입니다.");
                    if (mRequestMode == REQUEST_MODE_SIGNIN_VIA_FACEBOOK
                            || mRequestMode == REQUEST_MODE_SIGNUP_VIA_FACEBOOK) {
                        BaasioUser.signInViaFacebookInBackground(mContext, token,
                                new BaasioSignInCallback() {

                                    @Override
                                    public void onException(BaasioException e) {
                                        DialogUtils.dissmissProgressDialog(
                                                FacebookAuthActivity.this, "facebook_login");

                                        setResult(RESULT_CANCELED);
                                        finish();
                                    }

                                    @Override
                                    public void onResponse(BaasioUser response) {
                                        DialogUtils.dissmissProgressDialog(
                                                FacebookAuthActivity.this, "facebook_login");

                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                });
                    } else {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
