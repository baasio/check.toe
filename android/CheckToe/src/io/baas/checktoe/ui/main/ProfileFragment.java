
package io.baas.checktoe.ui.main;

import com.actionbarsherlock.view.MenuItem;
import com.kth.baasio.Baas;
import com.kth.baasio.callback.BaasioCallback;
import com.kth.baasio.callback.BaasioQueryCallback;
import com.kth.baasio.entity.BaasioBaseEntity;
import com.kth.baasio.entity.entity.BaasioEntity;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.query.BaasioQuery;
import com.kth.baasio.query.BaasioQuery.ORDER_BY;
import com.kth.baasio.utils.JsonUtils;
import com.kth.baasio.utils.ObjectUtils;
import com.kth.common.utils.LogUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.baas.checktoe.R;
import io.baas.checktoe.ui.BaseFragment;
import io.baas.checktoe.ui.dialog.DialogUtils;
import io.baas.checktoe.ui.view.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import io.baas.checktoe.ui.view.pulltorefresh.PullToRefreshListView;
import io.baas.checktoe.utils.EtcUtils;
import io.baas.checktoe.utils.actionmodecompat.ActionMode;
import io.baas.checktoe.utils.actionmodecompat.ActionMode.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProfileFragment extends BaseFragment implements OnRefreshListener, Callback {

    private static final String TAG = LogUtils.makeLogTag(ProfileFragment.class);

    public static final String INTENT_USER = "user";

    public static final String INTENT_USER_UUID = "user_uuid";

    public static final String ENTITY_PROPERTY_NAME_INTRODUCTION = "introduction";

    public static final String ENTITY_TYPE = "comment";

    public static final String ENTITY_PROPERTY_NAME_PROFILE_USER_UUID = "profile_user_uuid";

    public static final String ENTITY_PROPERTY_NAME_OWNER_UUID = "owner_uuid";

    public static final String ENTITY_PROPERTY_NAME_OWNER_NAME = "owner_name";

    public static final String ENTITY_PROPERTY_NAME_OWNER_PICTURE = "owner_picture";

    public static final String ENTITY_PROPERTY_NAME_OWNER_IS_FACEBOOK = "owner_isfacebook";

    public static final String ENTITY_PROPERTY_NAME_BODY = "body";

    private DisplayImageOptions options;

    private ViewGroup mvgRoot;

    private HeaderLayout mllRoot;

    private ImageView mivProfile;

    private ImageView mivFacebook;

    private TextView mtvName;

    private TextView mtvEmail;

    private TextView mtvCreated;

    private TextView mtvModified;

    private TextView mtvIntroduction;

    private PullToRefreshListView mPullToRefreshList;

    private ListView mList;

    private TextView mtvEmpty;

    private EditText metComment;

    private Button mbtWrite;

    private EntityListAdapter mListAdapter;

    private ArrayList<BaasioEntity> mEntityList;

    private BaasioQuery mQuery;

    private ActionMode mActionMode;

    private View mLongClickedView;

    private Integer mLongClickedPosition;

    private BaasioUser mUser;

    public static final int QUERY_INIT = 0;

    public static final int QUERY_REFRESH = 1;

    public static final int QUERY_NEXT = 2;

    private Boolean mIsFollowing;

    public ProfileFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = new DisplayImageOptions.Builder().showStubImage(R.drawable.person_image_empty)
                .showImageForEmptyUri(R.drawable.person_image_empty).cacheInMemory().cacheOnDisc()
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        mEntityList = new ArrayList<BaasioEntity>();
        mListAdapter = new EntityListAdapter(getActivity());

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            String userString = intent.getStringExtra(INTENT_USER);

            if (!ObjectUtils.isEmpty(userString)) {
                mUser = JsonUtils.parse(userString, BaasioUser.class);
            } else {
                String userUuid = intent.getStringExtra(INTENT_USER_UUID);

                mUser = new BaasioUser();
                mUser.setUuid(UUID.fromString(userUuid));

                mUser.getInBackground(new BaasioCallback<BaasioUser>() {

                    @Override
                    public void onResponse(BaasioUser response) {
                        mUser = response;

                        BaasioUser currentUser = Baas.io().getSignedInUser();
                        if (!ObjectUtils.isEmpty(currentUser)) {
                            if (currentUser.getUuid().equals(mUser)) {
                                Baas.io().setSignedInUser(mUser);
                            }
                        }

                        refreshView();
                    }

                    @Override
                    public void onException(BaasioException e) {
                        Toast.makeText(getActivity(), getString(R.string.error_get_profile),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        refresh(QUERY_INIT);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        mvgRoot = (ViewGroup)inflater.inflate(R.layout.fragment_profile, null);
        mtvEmpty = (TextView)mvgRoot.findViewById(R.id.tvEmpty);
        mtvEmpty.setText(getString(R.string.label_profile_empty_comment));

        mllRoot = (HeaderLayout)inflater.inflate(R.layout.listview_header_profile, null);
        mllRoot.setOnSizeChangedListener(new HeaderLayout.SizeChangedListener() {

            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                int height = mvgRoot.getHeight();

                ViewGroup.LayoutParams params = mtvEmpty.getLayoutParams();
                params.height = height - h;
                mtvEmpty.setLayoutParams(params);
            }
        });

        mivProfile = (ImageView)mllRoot.findViewById(R.id.ivProfile);
        mivFacebook = (ImageView)mllRoot.findViewById(R.id.ivFacebook);

        mtvName = (TextView)mllRoot.findViewById(R.id.tvName);
        mtvEmail = (TextView)mllRoot.findViewById(R.id.tvEmail);

        mtvCreated = (TextView)mllRoot.findViewById(R.id.tvCreated);
        mtvModified = (TextView)mllRoot.findViewById(R.id.tvModified);

        mtvIntroduction = (TextView)mllRoot.findViewById(R.id.tvBody);

        mPullToRefreshList = (PullToRefreshListView)mvgRoot.findViewById(R.id.lvPullToRefresh);
        mPullToRefreshList.setOnRefreshListener(this);

        mList = mPullToRefreshList.getRefreshableView();
        mList.addHeaderView(mllRoot);
        mList.setDivider(null);

        mList.setAdapter(mListAdapter);

        if (!ObjectUtils.isEmpty(mQuery)) {
            if (mQuery.hasNextEntities()) {
                if (mPullToRefreshList != null) {
                    mPullToRefreshList.setHasMoreData(true);
                    mPullToRefreshList.setFooterVisible();
                }

            } else {
                if (mPullToRefreshList != null) {
                    mPullToRefreshList.setHasMoreData(false);
                    mPullToRefreshList.setFooterGone();
                }

            }
        }

        metComment = (EditText)mvgRoot.findViewById(R.id.etComment);

        mbtWrite = (Button)mvgRoot.findViewById(R.id.btConfirm);
        mbtWrite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String comment = metComment.getText().toString();
                if (ObjectUtils.isEmpty(comment)) {
                    Toast.makeText(getActivity(), getString(R.string.error_comment_no_content),
                            Toast.LENGTH_LONG).show();
                } else {
                    BaasioUser user = Baas.io().getSignedInUser();

                    if (ObjectUtils.isEmpty(user)) {
                        Toast.makeText(getActivity(), getString(R.string.error_comment_need_login),
                                Toast.LENGTH_LONG).show();
                    } else {
                        BaasioEntity entity = new BaasioEntity(ENTITY_TYPE);
                        entity.setProperty(ENTITY_PROPERTY_NAME_PROFILE_USER_UUID, mUser.getUuid()
                                .toString());
                        entity.setProperty(ENTITY_PROPERTY_NAME_OWNER_UUID, user.getUuid()
                                .toString());
                        entity.setProperty(ENTITY_PROPERTY_NAME_OWNER_PICTURE, user.getPicture());
                        entity.setProperty(ENTITY_PROPERTY_NAME_OWNER_NAME, user.getName());

                        if (!ObjectUtils.isEmpty(user.getFacebook())) {
                            entity.setProperty(ENTITY_PROPERTY_NAME_OWNER_IS_FACEBOOK, true);
                        } else {
                            entity.setProperty(ENTITY_PROPERTY_NAME_OWNER_IS_FACEBOOK, false);
                        }

                        entity.setProperty(ENTITY_PROPERTY_NAME_BODY, comment);

                        DialogUtils.showProgressDialog(ProfileFragment.this, "writing_comment",
                                getString(R.string.progress_dialog_saving));

                        entity.saveInBackground(new BaasioCallback<BaasioEntity>() {

                            @Override
                            public void onResponse(BaasioEntity response) {
                                new Handler().post(new Runnable() {

                                    @Override
                                    public void run() {
                                        DialogUtils.dissmissProgressDialog(ProfileFragment.this,
                                                "writing_comment");
                                    }
                                });

                                if (!ObjectUtils.isEmpty(response)) {
                                    Toast.makeText(getActivity(),
                                            getString(R.string.success_write_comment),
                                            Toast.LENGTH_LONG).show();

                                    mEntityList.add(response);
                                    mListAdapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(
                                            getActivity(),
                                            getString(R.string.fail_write_comment,
                                                    getString(R.string.error_unknown)),
                                            Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onException(BaasioException e) {
                                new Handler().post(new Runnable() {

                                    @Override
                                    public void run() {
                                        DialogUtils.dissmissProgressDialog(ProfileFragment.this,
                                                "writing_comment");
                                    }
                                });

                                Toast.makeText(
                                        getActivity(),
                                        getString(R.string.fail_write_comment,
                                                e.getErrorDescription()), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });

        refreshView();

        return mvgRoot;
    }

    private void refreshView() {
        if (mUser != null) {
            String imageUrl = mUser.getPicture();
            if (imageUrl != null) {
                mImageLoader.displayImage(imageUrl, mivProfile, options);
            } else {
                mivProfile.setImageResource(R.drawable.person_image_empty);
            }

            if (!ObjectUtils.isEmpty(mUser.getFacebook())) {
                mivFacebook.setVisibility(View.VISIBLE);
            } else {
                mivFacebook.setVisibility(View.GONE);
            }

            String name = mUser.getName();
            if (!ObjectUtils.isEmpty(name)) {
                mtvName.setText(name);
            } else {
                mtvName.setText(mUser.getUsername());
            }

            String email = mUser.getEmail();
            if (!ObjectUtils.isEmpty(email)) {
                mtvEmail.setText(email);
            }

            String introduction = EtcUtils.getStringFromEntity(mUser,
                    ENTITY_PROPERTY_NAME_INTRODUCTION);
            if (!ObjectUtils.isEmpty(introduction)) {
                mtvIntroduction.setText(introduction);
            }

            if (mUser.getCreated() != null) {
                String createdTime = EtcUtils.getDateString(mUser.getCreated());
                if (!TextUtils.isEmpty(createdTime)) {
                    mtvCreated.setText(getString(R.string.label_created_user, createdTime));
                }
            }

            if (mUser.getModified() != null) {
                String modifiedTime = EtcUtils.getDateString(mUser.getModified());
                if (!TextUtils.isEmpty(modifiedTime)) {
                    mtvModified.setText(getString(R.string.label_modified_user, modifiedTime));
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
            com.actionbarsherlock.view.MenuInflater inflater) {

        inflater.inflate(R.menu.fragment_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private Boolean isFollowing() {
        return mIsFollowing;
    }

    @Override
    public void onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        BaasioUser user = Baas.io().getSignedInUser();
        if (!ObjectUtils.isEmpty(user)) {
            MenuItem follow = menu.findItem(R.id.menu_follow);
            MenuItem unfollow = menu.findItem(R.id.menu_unfollow);

            if (mUser.getUuid().equals(user.getUuid())) {
                follow.setVisible(false);
                unfollow.setVisible(false);
            } else {
                if (isFollowing() == null) {
                    follow.setVisible(false);
                    unfollow.setVisible(false);
                } else {
                    follow.setVisible(true);
                    unfollow.setVisible(true);

                    if (isFollowing()) {
                        follow.setVisible(false);
                        unfollow.setVisible(true);
                    } else {
                        follow.setVisible(true);
                        unfollow.setVisible(false);
                    }
                }
            }
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_follow: {
                BaasioUser user = Baas.io().getSignedInUser();

                DialogUtils.showProgressDialog(ProfileFragment.this, "following",
                        getString(R.string.progress_dialog_saving));
                user.connectInBackground("following", mUser, new BaasioCallback<BaasioUser>() {

                    @Override
                    public void onResponse(BaasioUser response) {
                        new Handler().post(new Runnable() {

                            @Override
                            public void run() {
                                DialogUtils.dissmissProgressDialog(ProfileFragment.this,
                                        "following");
                            }
                        });

                        if (!ObjectUtils.isEmpty(response)) {
                            Toast.makeText(getActivity(), getString(R.string.success_following),
                                    Toast.LENGTH_LONG).show();

                            mIsFollowing = Boolean.valueOf(true);

                            getSherlockActivity().invalidateOptionsMenu();
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    getString(R.string.fail_following,
                                            getString(R.string.error_unknown)), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onException(BaasioException e) {
                        new Handler().post(new Runnable() {

                            @Override
                            public void run() {
                                DialogUtils.dissmissProgressDialog(ProfileFragment.this,
                                        "following");
                            }
                        });

                        Toast.makeText(getActivity(),
                                getString(R.string.fail_following, e.getErrorDescription()),
                                Toast.LENGTH_LONG).show();
                    }
                });
                break;
            }
            case R.id.menu_unfollow: {
                BaasioUser user = Baas.io().getSignedInUser();

                DialogUtils.showProgressDialog(ProfileFragment.this, "unfollowing",
                        getString(R.string.progress_dialog_saving));
                user.disconnectInBackground("following", mUser, new BaasioCallback<BaasioUser>() {

                    @Override
                    public void onResponse(BaasioUser response) {
                        new Handler().post(new Runnable() {

                            @Override
                            public void run() {
                                DialogUtils.dissmissProgressDialog(ProfileFragment.this,
                                        "unfollowing");
                            }
                        });

                        if (!ObjectUtils.isEmpty(response)) {
                            Toast.makeText(getActivity(), getString(R.string.success_unfollowing),
                                    Toast.LENGTH_LONG).show();

                            mIsFollowing = Boolean.valueOf(false);

                            getSherlockActivity().invalidateOptionsMenu();
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    getString(R.string.fail_unfollowing,
                                            getString(R.string.error_unknown)), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onException(BaasioException e) {
                        new Handler().post(new Runnable() {

                            @Override
                            public void run() {
                                DialogUtils.dissmissProgressDialog(ProfileFragment.this,
                                        "unfollowing");
                            }
                        });

                        Toast.makeText(getActivity(),
                                getString(R.string.fail_unfollowing, e.getErrorDescription()),
                                Toast.LENGTH_LONG).show();
                    }
                });
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private BaasioQueryCallback mQueryCallback = new BaasioQueryCallback() {

        @Override
        public void onResponse(List<BaasioBaseEntity> entities, List<Object> list,
                BaasioQuery query, long timestamp) {
            new Handler().post(new Runnable() {

                @Override
                public void run() {
                    if (mPullToRefreshList != null) {
                        if (mPullToRefreshList.isRefreshing())
                            mPullToRefreshList.onRefreshComplete();
                    }

                    DialogUtils.dissmissProgressDialog(ProfileFragment.this, "loading");
                }
            });

            mEntityList.clear();

            mQuery = query;

            List<BaasioEntity> comments = BaasioBaseEntity.toType(entities, BaasioEntity.class);
            mEntityList.addAll(comments);

            mListAdapter.notifyDataSetChanged();

            if (mEntityList.isEmpty()) {
                mtvEmpty.setVisibility(View.VISIBLE);
            } else {
                mtvEmpty.setVisibility(View.GONE);
            }

            if (mQuery.hasNextEntities()) {
                if (mPullToRefreshList != null) {
                    mPullToRefreshList.setHasMoreData(true);
                    mPullToRefreshList.setFooterVisible();
                }

            } else {
                if (mPullToRefreshList != null) {
                    mPullToRefreshList.setHasMoreData(false);
                    mPullToRefreshList.setFooterGone();
                }

            }
        }

        @Override
        public void onException(BaasioException e) {
            Toast.makeText(getActivity(), "queryInBackground =>" + e.toString(), Toast.LENGTH_LONG)
                    .show();

            new Handler().post(new Runnable() {

                @Override
                public void run() {
                    if (mPullToRefreshList != null) {
                        if (mPullToRefreshList.isRefreshing())
                            mPullToRefreshList.onRefreshComplete();
                    }

                    DialogUtils.dissmissProgressDialog(ProfileFragment.this, "loading");
                }
            });

        }
    };

    private BaasioQueryCallback mQueryNextCallback = new BaasioQueryCallback() {

        @Override
        public void onResponse(List<BaasioBaseEntity> entities, List<Object> list,
                BaasioQuery query, long timestamp) {

            new Handler().post(new Runnable() {

                @Override
                public void run() {
                    if (mPullToRefreshList != null) {
                        mPullToRefreshList.setIsLoading(false);
                    }

                }
            });

            mQuery = query;

            List<BaasioEntity> comments = BaasioBaseEntity.toType(entities, BaasioEntity.class);
            mEntityList.addAll(comments);

            mListAdapter.notifyDataSetChanged();

            if (mEntityList.isEmpty()) {
                mtvEmpty.setVisibility(View.VISIBLE);
            } else {
                mtvEmpty.setVisibility(View.GONE);
            }

            if (mQuery.hasNextEntities()) {
                if (mPullToRefreshList != null) {
                    mPullToRefreshList.setHasMoreData(true);
                    mPullToRefreshList.setFooterVisible();
                }

            } else {
                if (mPullToRefreshList != null) {
                    mPullToRefreshList.setHasMoreData(false);
                    mPullToRefreshList.setFooterGone();
                }

            }
        }

        @Override
        public void onException(BaasioException e) {
            Toast.makeText(getActivity(), "nextInBackground =>" + e.toString(), Toast.LENGTH_LONG)
                    .show();

            new Handler().post(new Runnable() {

                @Override
                public void run() {
                    if (mPullToRefreshList != null) {
                        mPullToRefreshList.setIsLoading(false);
                    }
                }
            });

        }
    };

    private void refresh(final int mode) {
        if (mode == QUERY_INIT) {
            DialogUtils.showProgressDialog(this, "loading",
                    getString(R.string.progress_dialog_loading));
        }

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            String userString = intent.getStringExtra(INTENT_USER);

            if (!ObjectUtils.isEmpty(userString)) {
                mUser = JsonUtils.parse(userString, BaasioUser.class);
            } else {
                String userUuid = intent.getStringExtra(INTENT_USER_UUID);

                mUser = new BaasioUser();
                mUser.setUuid(UUID.fromString(userUuid));
            }
        }

        if (!ObjectUtils.isEmpty(mUser)) {
            mUser.getInBackground(new BaasioCallback<BaasioUser>() {

                @Override
                public void onResponse(BaasioUser response) {
                    mUser = response;

                    BaasioUser currentUser = Baas.io().getSignedInUser();
                    if (!ObjectUtils.isEmpty(currentUser)) {
                        if (currentUser.getUuid().equals(mUser)) {
                            Baas.io().setSignedInUser(mUser);
                        }

                        BaasioQuery query = new BaasioQuery();
                        query.setRawString("user/" + currentUser.getUuid().toString()
                                + "/following/" + mUser.getUuid().toString());

                        query.queryInBackground(new BaasioQueryCallback() {

                            @Override
                            public void onResponse(List<BaasioBaseEntity> entities,
                                    List<Object> list, BaasioQuery query, long timestamp) {
                                if (entities.size() > 0) {
                                    // FIXME: 임시로 처리해놓음
                                    // mIsFollowing = Boolean.valueOf(true);
                                    mIsFollowing = Boolean.valueOf(false);
                                } else {
                                    mIsFollowing = Boolean.valueOf(false);
                                }
                                getSherlockActivity().invalidateOptionsMenu();

                                getEntities(mode);
                            }

                            @Override
                            public void onException(BaasioException e) {
                                if (e.getErrorCode() == 101) {
                                    mIsFollowing = Boolean.valueOf(false);

                                    getSherlockActivity().invalidateOptionsMenu();
                                } else {
                                    LogUtils.LOGE(TAG, e.toString());
                                }

                                getEntities(mode);
                            }
                        });
                    }
                }

                @Override
                public void onException(BaasioException e) {
                    getEntities(mode);
                }
            });
        }

    }

    private void getEntities(int mode) {
        if (mode == QUERY_INIT || mode == QUERY_REFRESH) {
            mQuery = new BaasioQuery();
            mQuery.setType(ENTITY_TYPE);

            StringBuilder builder = new StringBuilder();

            builder.append(ENTITY_PROPERTY_NAME_PROFILE_USER_UUID + " = "
                    + mUser.getUuid().toString());

            mQuery.setWheres(builder.toString());
            mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED, ORDER_BY.DESCENDING);

            mQuery.queryInBackground(mQueryCallback);
        } else
            mQuery.nextInBackground(mQueryNextCallback);
    }

    public class EntityViewHolder {
        public ViewGroup mllRoot;

        public ImageView mivProfile;

        public ImageView mivFacebook;

        public TextView mtvName;

        public TextView mtvBody;

        public TextView mtvCreated;
    }

    private class EntityListAdapter extends BaseAdapter {
        private Context mContext;

        private LayoutInflater mInflater;

        public EntityListAdapter(Context context) {
            super();

            mContext = context;
            mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mEntityList.size();
        }

        @Override
        public BaasioEntity getItem(int position) {
            return mEntityList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /*
         * (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View,
         * android.view.ViewGroup)
         */
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            EntityViewHolder view = null;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listview_item_commentlist, parent, false);

                view = new EntityViewHolder();

                view.mllRoot = (ViewGroup)convertView.findViewById(R.id.llRoot);
                view.mivProfile = (ImageView)convertView.findViewById(R.id.ivProfile);
                view.mivFacebook = (ImageView)convertView.findViewById(R.id.ivFacebook);
                view.mtvName = (TextView)convertView.findViewById(R.id.tvName);
                view.mtvBody = (TextView)convertView.findViewById(R.id.tvBody);
                view.mtvCreated = (TextView)convertView.findViewById(R.id.tvCreated);

                if (view != null) {
                    convertView.setTag(view);
                }
            } else {
                view = (EntityViewHolder)convertView.getTag();
            }

            BaasioEntity entity = mEntityList.get(position);

            if (entity != null) {
                String imageUrl = EtcUtils.getStringFromEntity(entity,
                        ENTITY_PROPERTY_NAME_OWNER_PICTURE);
                if (imageUrl != null) {
                    mImageLoader.displayImage(imageUrl, view.mivProfile, options);
                } else {
                    view.mivProfile.setImageResource(R.drawable.person_image_empty);
                }

                boolean isFacebook = EtcUtils.getBooleanFromEntity(entity,
                        ENTITY_PROPERTY_NAME_OWNER_IS_FACEBOOK, false);
                if (isFacebook) {
                    view.mivFacebook.setVisibility(View.VISIBLE);
                } else {
                    view.mivFacebook.setVisibility(View.GONE);
                }

                setStringToView(entity, view.mtvName, ENTITY_PROPERTY_NAME_OWNER_NAME);
                setStringToView(entity, view.mtvBody, ENTITY_PROPERTY_NAME_BODY);

                if (entity.getModified() != null) {
                    String createdTime = EtcUtils.getAgoTimeString(getSherlockActivity(),
                            entity.getCreated());
                    if (!TextUtils.isEmpty(createdTime)) {
                        view.mtvCreated.setText(getString(R.string.label_profile_created_comment,
                                createdTime));
                    }
                }

                if (view.mllRoot != null) {
                    view.mllRoot.setOnLongClickListener(new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View view) {
                            if (mActionMode != null) {
                                // CAB already displayed, ignore
                                return true;
                            }

                            mLongClickedView = view;
                            mLongClickedPosition = position;

                            mActionMode = ActionMode.start(getActivity(), ProfileFragment.this);
                            EtcUtils.setActivatedCompat(mLongClickedView, true);
                            return true;
                        }
                    });
                }
            }
            return convertView;
        }

        private void setStringToView(BaasioEntity entity, TextView view, String value) {
            view.setText(EtcUtils.getStringFromEntity(entity, value));
        }

    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.view.pulltorefresh.PullToRefreshBase.
     * OnRefreshListener#onRefresh()
     */
    @Override
    public void onRefresh() {
        refresh(QUERY_REFRESH);
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.view.pulltorefresh.PullToRefreshBase.
     * OnRefreshListener#onUpdate()
     */
    @Override
    public void onUpdate() {
        refresh(QUERY_NEXT);
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.utils.actionmodecompat.ActionMode.Callback#
     * onCreateActionMode
     * (com.kth.kanu.baassample.utils.actionmodecompat.ActionMode,
     * android.view.Menu)
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (mLongClickedView == null) {
            return true;
        }

        MenuInflater inflater = mode.getMenuInflater();
        // inflater.inflate(R.menu.contextmenu_fragment_postdetail, menu);

        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.utils.actionmodecompat.ActionMode.Callback#
     * onPrepareActionMode
     * (com.kth.kanu.baassample.utils.actionmodecompat.ActionMode,
     * android.view.Menu)
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.utils.actionmodecompat.ActionMode.Callback#
     * onActionItemClicked
     * (com.kth.kanu.baassample.utils.actionmodecompat.ActionMode,
     * android.view.MenuItem)
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
        boolean handled = false;
        switch (item.getItemId()) {
        }

        LogUtils.LOGV(TAG, "onActionItemClicked: position=" + mLongClickedPosition + " title="
                + item.getTitle());
        mActionMode.finish();
        return handled;
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.utils.actionmodecompat.ActionMode.Callback#
     * onDestroyActionMode
     * (com.kth.kanu.baassample.utils.actionmodecompat.ActionMode)
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        if (mLongClickedView != null) {
            EtcUtils.setActivatedCompat(mLongClickedView, false);
            mLongClickedPosition = null;
            mLongClickedView = null;
        }
    }

}
