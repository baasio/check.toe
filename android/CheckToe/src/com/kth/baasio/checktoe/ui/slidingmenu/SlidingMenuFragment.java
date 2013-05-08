
package com.kth.baasio.checktoe.ui.slidingmenu;

import com.kth.baasio.Baas;
import com.kth.baasio.checktoe.R;
import com.kth.baasio.checktoe.ui.BaseFragment;
import com.kth.baasio.checktoe.ui.auth.SignInActivity;
import com.kth.baasio.utils.ObjectUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SlidingMenuFragment extends BaseFragment {

    private ViewGroup mRootView;

    private LinearLayout mllProfile;

    private ImageView mivProfile;

    private TextView mtvName;

    private LinearLayout mllLogout;

    private TextView mtvLogout;

    DisplayImageOptions options;

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

                } else {
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    startActivity(intent);
                }

            }
        });

        mivProfile = (ImageView)mRootView.findViewById(R.id.ivProfile);
        mtvName = (TextView)mRootView.findViewById(R.id.tvName);

        mllLogout = (LinearLayout)mRootView.findViewById(R.id.llLogout);

        mtvLogout = (TextView)mRootView.findViewById(R.id.tvLogout);
        mtvLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }
        });
        return mRootView;
    }

    public void refreshViews() {
        if (!ObjectUtils.isEmpty(Baas.io().getSignedInUser())) {
            String imageUrl = Baas.io().getSignedInUser().getPicture();

            if (imageUrl != null)
                mImageLoader.displayImage(imageUrl, mivProfile, options);

            mllLogout.setVisibility(View.VISIBLE);

            String name = Baas.io().getSignedInUser().getUsername();

            mtvName.setText(name);
        } else {
            mivProfile.setImageResource(R.drawable.person_image_empty);

            mllLogout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshViews();
    }
}
