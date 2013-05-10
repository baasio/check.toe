
package io.baas.checktoe.ui.auth;

import com.actionbarsherlock.app.SherlockFragment;
import com.kth.baasio.callback.BaasioSignUpCallback;
import io.baas.checktoe.R;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.utils.ObjectUtils;
import com.kth.common.sns.tools.facebook.FacebookAuthActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

public class SignUpFragment extends SherlockFragment {
    public static final int REQUEST_SIGNUP_VIA_FACEBOOK = 1;

    public static final int REQUEST_SHARE_VIA_FACEBOOK = 2;

    public static final int REQUEST_SIGNUP_VIA_TWITTER = 3;

    public static final int REQUEST_SHARE_VIA_TWITTER = 4;

    private Context mContext;

    private ViewGroup mRootView;

    private EditText mEmail;

    private EditText mName;

    private EditText mPassword;

    private EditText mPassword2;

    private Button mConfirm;

    private TextView mViaFacebook;

    private TextView mAlreadySignUped;

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();

        mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_signup, null);
        mEmail = (EditText)mRootView.findViewById(R.id.textEmail);
        mName = (EditText)mRootView.findViewById(R.id.textName);
        mPassword = (EditText)mRootView.findViewById(R.id.textPassword);
        mPassword2 = (EditText)mRootView.findViewById(R.id.textPassword2);

        mConfirm = (Button)mRootView.findViewById(R.id.buttonConfirm);
        mConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = mName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String passwordValidate = mPassword2.getText().toString().trim();

                Pattern pattern = Pattern.compile(EMAIL_PATTERN);
                if (!pattern.matcher(email).matches()) {
                    Toast.makeText(getActivity(),
                            getActivity().getResources().getString(R.string.error_invalid_email),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (ObjectUtils.isEmpty(name)) {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_name),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (password == null || password.length() < 4 || passwordValidate == null
                        || passwordValidate.length() < 4) {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_password),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (!password.equals(passwordValidate)) {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_password),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                BaasioUser.signUpInBackground(email, name, email, password,
                        new BaasioSignUpCallback() {

                            @Override
                            public void onException(BaasioException e) {
                                if (e.getStatusCode() != null) {
                                    if (e.getErrorCode() == 913) {
                                        Toast.makeText(getActivity(),
                                                getString(R.string.error_already_exist),
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                }

                                Toast.makeText(getActivity(),
                                        "signUpInBackground =>" + e.toString(), Toast.LENGTH_LONG)
                                        .show();
                            }

                            @Override
                            public void onResponse(BaasioUser response) {
                                if (response != null) {
                                    Intent intent = new Intent(mContext, SignInActivity.class);
                                    intent.putExtra(SignInFragment.INTENT_EMAIL_INFO,
                                            response.getEmail());
                                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                                    getActivity().setResult(Activity.RESULT_OK, intent);
                                    getActivity().finish();
                                }
                            }
                        });
            }
        });

        mViaFacebook = (TextView)mRootView.findViewById(R.id.textViaFacebook);
        mViaFacebook.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FacebookAuthActivity.class);
                intent.putExtra(FacebookAuthActivity.INTENT_REQUEST_CODE,
                        FacebookAuthActivity.REQUEST_MODE_SIGNUP_VIA_FACEBOOK);
                startActivityForResult(intent, REQUEST_SIGNUP_VIA_FACEBOOK);
            }
        });

        mAlreadySignUped = (TextView)mRootView.findViewById(R.id.textAlreadySignUped);
        mAlreadySignUped.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        });

        return mRootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SIGNUP_VIA_TWITTER
                    || requestCode == REQUEST_SIGNUP_VIA_FACEBOOK) {

                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();

            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == REQUEST_SIGNUP_VIA_TWITTER
                    || requestCode == REQUEST_SIGNUP_VIA_FACEBOOK) {
                if (!ObjectUtils.isEmpty(data)) {
                    String error = data.getStringExtra(FacebookAuthActivity.INTENT_RESULT_ERROR);
                    if (!ObjectUtils.isEmpty(error)) {
                        Toast.makeText(getActivity(),
                                getString(R.string.error_signup_facebook) + " : " + error,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                Toast.makeText(getActivity(), getString(R.string.error_signup_facebook),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
