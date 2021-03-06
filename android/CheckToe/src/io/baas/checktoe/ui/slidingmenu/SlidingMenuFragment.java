
package io.baas.checktoe.ui.slidingmenu;

import com.kth.baasio.Baas;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.utils.ObjectUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.baas.checktoe.R;
import io.baas.checktoe.ui.BaseFragment;
import io.baas.checktoe.ui.auth.SignInActivity;
import io.baas.checktoe.ui.main.MainActivity;
import io.baas.checktoe.ui.main.ProfileActivity;
import io.baas.checktoe.ui.main.ProfileFragment;

public class SlidingMenuFragment extends BaseFragment {

    public static final int REQUEST_SIGNIN = 1;

    private ViewGroup mRootView;

    private LinearLayout mllProfile;

    private ImageView mivProfile;

    private ImageView mivFacebook;

    private TextView mtvName;

    private LinearLayout mllLogout;

    private TextView mtvLogout;

    private TextView mtvMyChecklist;

    private TextView mtvRecommendChecklist;

    private TextView mtvFavoriteChecklist;

    private TextView tvFriend;

    private DisplayImageOptions options;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = new DisplayImageOptions.Builder().showStubImage(R.drawable.person_image_empty)
                .showImageForEmptyUri(R.drawable.person_image_empty).cacheInMemory().cacheOnDisc()
                .bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_slidemenu, null);

        mllProfile = (LinearLayout)mRootView.findViewById(R.id.llProfile);
        mllProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!ObjectUtils.isEmpty(Baas.io().getSignedInUser())) {
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.putExtra(ProfileFragment.INTENT_USER, Baas.io().getSignedInUser()
                            .toString());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivityForResult(intent, REQUEST_SIGNIN);
                }

            }
        });

        mivProfile = (ImageView)mRootView.findViewById(R.id.ivProfile);
        mivFacebook = (ImageView)mRootView.findViewById(R.id.ivFacebook);

        mtvName = (TextView)mRootView.findViewById(R.id.tvName);

        mllLogout = (LinearLayout)mRootView.findViewById(R.id.llLogout);

        mtvLogout = (TextView)mRootView.findViewById(R.id.tvLogout);
        mtvLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                BaasioUser.signOut(getActivity());
                Toast.makeText(getActivity(), getString(R.string.msg_signout_success),
                        Toast.LENGTH_LONG).show();

                refreshViews();

                switchFragment(MainActivity.MODE_RECOMMEND_CHECKLIST);
            }
        });

        mtvMyChecklist = (TextView)mRootView.findViewById(R.id.tvMyChecklist);
        mtvMyChecklist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.MODE_MY_CHECKLIST);
            }
        });

        mtvRecommendChecklist = (TextView)mRootView.findViewById(R.id.tvRecommendChecklist);
        mtvRecommendChecklist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.MODE_RECOMMEND_CHECKLIST);
            }
        });

        mtvFavoriteChecklist = (TextView)mRootView.findViewById(R.id.tvFavoriteChecklist);
        mtvFavoriteChecklist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.MODE_FAVORITE_CHECKLIST);
            }
        });

        tvFriend = (TextView)mRootView.findViewById(R.id.tvFriend);
        tvFriend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.MODE_FRIEND);
            }
        });

        return mRootView;
    }

    public void refreshViews() {
        BaasioUser user = Baas.io().getSignedInUser();
        if (!ObjectUtils.isEmpty(user)) {
            String imageUrl = user.getPicture();

            if (imageUrl != null)
                mImageLoader.displayImage(imageUrl, mivProfile, options);
            else
                mivProfile.setImageResource(R.drawable.person_image_empty);

            if (!ObjectUtils.isEmpty(user.getFacebook())) {
                mivFacebook.setVisibility(View.VISIBLE);
            } else {
                mivFacebook.setVisibility(View.GONE);
            }

            mllLogout.setVisibility(View.VISIBLE);

            String name = user.getName();

            mtvName.setText(name);

            mtvMyChecklist.setEnabled(true);
            mtvFavoriteChecklist.setEnabled(true);
        } else {
            mivProfile.setImageResource(R.drawable.person_image_empty);
            mtvName.setText(R.string.label_login);
            mllLogout.setVisibility(View.GONE);

            mtvMyChecklist.setEnabled(false);
            mtvFavoriteChecklist.setEnabled(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SIGNIN) {
                switchFragment(MainActivity.MODE_MY_CHECKLIST);

                Toast.makeText(getActivity(), getString(R.string.msg_signin_success),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshViews();
    }

    // the meat of switching the above fragment
    private void switchFragment(int mode) {
        if (getActivity() == null)
            return;

        if (getActivity() instanceof MainActivity) {
            MainActivity fca = (MainActivity)getActivity();
            fca.switchFragment(mode);
        }
    }
}
