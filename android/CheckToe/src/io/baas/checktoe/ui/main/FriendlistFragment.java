
package io.baas.checktoe.ui.main;

import static com.kth.common.utils.LogUtils.makeLogTag;

import com.actionbarsherlock.view.MenuItem;
import com.kth.baasio.Baas;
import com.kth.baasio.callback.BaasioQueryCallback;
import com.kth.baasio.entity.BaasioBaseEntity;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.query.BaasioQuery;
import com.kth.baasio.query.BaasioQuery.ORDER_BY;
import com.kth.baasio.utils.ObjectUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.baas.checktoe.R;
import io.baas.checktoe.ui.SearchableBaseFragment;
import io.baas.checktoe.ui.view.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import io.baas.checktoe.ui.view.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class FriendlistFragment extends SearchableBaseFragment implements OnRefreshListener {

    private static final String TAG = makeLogTag(FriendlistFragment.class);

    public static final String ENTITY_TYPE = "user";

    public static final int FRIENDLIST_MODE_FOLLOWING = 0;

    public static final int FRIENDLIST_MODE_FOLLOWER = 1;

    public static final int FRIENDLIST_MODE_SEARCH_FRIEND = 2;

    public static final int FRIENDLIST_MODE_SLIDE_SEARCH_FRIEND = 3;

    private DisplayImageOptions options;

    private ViewGroup mRootView;

    private EditText metSearchFriends;

    private PullToRefreshListView mlvPullToRefresh;

    private ListView mlvList;

    private TextView mtvEmpty;

    private EntityListAdapter mListAdapter;

    private ArrayList<BaasioUser> mEntityList;

    private BaasioQuery mQuery;

    public static final int QUERY_INIT = 0;

    public static final int QUERY_REFRESH = 1;

    public static final int QUERY_NEXT = 2;

    private int mMode = FRIENDLIST_MODE_FOLLOWING;

    private String mSearchKeyword = "";

    public FriendlistFragment() {
        super();
    }

    public void setMode(int mode) {
        mMode = mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = new DisplayImageOptions.Builder().showStubImage(R.drawable.person_image_empty)
                .showImageForEmptyUri(R.drawable.person_image_empty).cacheInMemory().cacheOnDisc()
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        mEntityList = new ArrayList<BaasioUser>();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_friendlist, null);

        metSearchFriends = (EditText)mRootView.findViewById(R.id.etSearchFriends);

        mlvPullToRefresh = (PullToRefreshListView)mRootView.findViewById(R.id.lvPullToRefresh);
        mlvPullToRefresh.setOnRefreshListener(this);

        mlvList = mlvPullToRefresh.getRefreshableView();

        if (!ObjectUtils.isEmpty(mQuery)) {
            if (mQuery.hasNextEntities()) {
                if (mlvPullToRefresh != null) {
                    mlvPullToRefresh.setHasMoreData(true);
                    mlvPullToRefresh.setFooterVisible();
                }

            } else {
                if (mlvPullToRefresh != null) {
                    mlvPullToRefresh.setHasMoreData(false);
                    mlvPullToRefresh.setFooterGone();
                }

            }
        }

        reloadFromArguments(getArguments());
        return mRootView;
    }

    @Override
    public void reloadFromArguments(Bundle arguments) {
        mlvList.setAdapter(null);

        mListAdapter = new EntityListAdapter(getActivity());
        mlvList.setAdapter(mListAdapter);

        mtvEmpty = (TextView)mRootView.findViewById(R.id.tvEmpty);
        if (mMode == FRIENDLIST_MODE_FOLLOWER) {
            mtvEmpty.setText(getString(R.string.empty_follower));
            metSearchFriends.setVisibility(View.GONE);

        } else if (mMode == FRIENDLIST_MODE_FOLLOWING) {
            mtvEmpty.setText(getString(R.string.empty_following));
            metSearchFriends.setVisibility(View.GONE);

        } else if (mMode == FRIENDLIST_MODE_SEARCH_FRIEND) {
            mtvEmpty.setText(getString(R.string.empty_user));
            metSearchFriends.setVisibility(View.GONE);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                String searchKeyword = intent.getStringExtra(SearchManager.QUERY);

                if (!ObjectUtils.isEmpty(searchKeyword)) {
                    mSearchKeyword = searchKeyword.trim();

                    getSherlockActivity().getSupportActionBar().setTitle(
                            getResources().getString(R.string.search_title, mSearchKeyword));
                }
            }
        } else if (mMode == FRIENDLIST_MODE_SLIDE_SEARCH_FRIEND) {
            mtvEmpty.setText(getString(R.string.empty_user));
            metSearchFriends.setVisibility(View.VISIBLE);
        }

        getEntities(QUERY_INIT);

        getSherlockActivity().invalidateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
            com.actionbarsherlock.view.MenuInflater inflater) {

        inflater.inflate(R.menu.fragment_friendlist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                Bundle bundle = new Bundle();
                bundle.putInt(SearchableActivity.INTENT_BUNDLE_SEARCHMODE,
                        SearchableActivity.SEARCHMODE_FRIEND);
                getSherlockActivity().startSearch(null, false, bundle, false);
                break;
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
                    if (mlvPullToRefresh != null) {
                        if (mlvPullToRefresh.isRefreshing())
                            mlvPullToRefresh.onRefreshComplete();
                    }
                }
            });

            mEntityList.clear();

            mQuery = query;

            List<BaasioUser> posts = BaasioBaseEntity.toType(entities, BaasioUser.class);
            mEntityList.addAll(posts);

            mListAdapter.notifyDataSetChanged();

            if (mEntityList.isEmpty()) {
                mtvEmpty.setVisibility(View.VISIBLE);
            } else {
                mtvEmpty.setVisibility(View.GONE);
            }

            if (mQuery.hasNextEntities()) {
                if (mlvPullToRefresh != null) {
                    mlvPullToRefresh.setHasMoreData(true);
                    mlvPullToRefresh.setFooterVisible();
                }

            } else {
                if (mlvPullToRefresh != null) {
                    mlvPullToRefresh.setHasMoreData(false);
                    mlvPullToRefresh.setFooterGone();
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
                    if (mlvPullToRefresh != null) {
                        if (mlvPullToRefresh.isRefreshing())
                            mlvPullToRefresh.onRefreshComplete();
                    }
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
                    if (mlvPullToRefresh != null) {
                        mlvPullToRefresh.setIsLoading(false);
                    }
                }
            });

            mQuery = query;

            List<BaasioUser> posts = BaasioBaseEntity.toType(entities, BaasioUser.class);
            mEntityList.addAll(posts);

            mListAdapter.notifyDataSetChanged();

            if (mEntityList.isEmpty()) {
                mtvEmpty.setVisibility(View.VISIBLE);
            } else {
                mtvEmpty.setVisibility(View.GONE);
            }

            if (mQuery.hasNextEntities()) {
                if (mlvPullToRefresh != null) {
                    mlvPullToRefresh.setHasMoreData(true);
                    mlvPullToRefresh.setFooterVisible();
                }

            } else {
                if (mlvPullToRefresh != null) {
                    mlvPullToRefresh.setHasMoreData(false);
                    mlvPullToRefresh.setFooterGone();
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
                    if (mlvPullToRefresh != null) {
                        mlvPullToRefresh.setIsLoading(false);
                    }
                }
            });

        }
    };

    private void getEntities(int mode) {
        if (mode == QUERY_INIT || mode == QUERY_REFRESH) {
            mQuery = new BaasioQuery();

            BaasioUser user = Baas.io().getSignedInUser();
            if (mMode == FRIENDLIST_MODE_FOLLOWING) {
                if (!ObjectUtils.isEmpty(user)) {
                    mQuery.setRelation(user, "following");
                } else {
                    return;
                }

                // FIXME: sort order 안됨
                // mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED,
                // ORDER_BY.DESCENDING);
            } else if (mMode == FRIENDLIST_MODE_FOLLOWER) {
                if (!ObjectUtils.isEmpty(user)) {
                    mQuery.setRelation(user, "followers");
                } else {
                    return;
                }

                // FIXME: sort order 안됨
                // mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED,
                // ORDER_BY.DESCENDING);
            } else if (mMode == FRIENDLIST_MODE_SEARCH_FRIEND) {
                mQuery.setType(ENTITY_TYPE);

                if (!ObjectUtils.isEmpty(mSearchKeyword)) {
                    StringBuilder builder = new StringBuilder();

                    builder.append(BaasioUser.PROPERTY_NAME + " contains " + "'" + mSearchKeyword
                            + "*'");
                    builder.append(" OR ");
                    builder.append(BaasioUser.PROPERTY_EMAIL + " = " + "'" + mSearchKeyword + "*'");
                    builder.append(" OR ");
                    builder.append(BaasioUser.PROPERTY_USERNAME + " = " + "'" + mSearchKeyword
                            + "*'");

                    mQuery.setWheres(builder.toString());
                } else {
                    return;
                }

                mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED, ORDER_BY.DESCENDING);
            } else if (mMode == FRIENDLIST_MODE_SLIDE_SEARCH_FRIEND) {
                if (!ObjectUtils.isEmpty(user)) {
                    mQuery.setRelation(user, "following");
                } else {
                    return;
                }

                // FIXME: sort order 안됨
                // mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED,
                // ORDER_BY.DESCENDING);
            }
        }

        if (mode == QUERY_INIT) {
            if (mlvPullToRefresh != null) {
                mlvPullToRefresh.setRefreshing();
            }

            mQuery.queryInBackground(mQueryCallback);

        } else if (mode == QUERY_REFRESH) {
            mQuery.queryInBackground(mQueryCallback);
        } else
            mQuery.nextInBackground(mQueryNextCallback);
    }

    public boolean processEntity(int mode, String title, String body, final int position) {
        if (TextUtils.isEmpty(title)) {
            return false;
        }

        // if (mode == EntityDialogFragment.CREATE_ENTITY) {
        // BaasioEntity entity = new BaasioEntity(ENTITY_TYPE);
        // entity.setProperty(ENTITY_PROPERTY_NAME_WRITER_USERNAME,
        // Baas.io().getSignedInUser()
        // .getUsername());
        // entity.setProperty(ENTITY_PROPERTY_NAME_WRITER_PICTURE,
        // Baas.io().getSignedInUser()
        // .getPicture());
        // entity.setProperty(ENTITY_PROPERTY_NAME_WRITER_UUID,
        // Baas.io().getSignedInUser()
        // .getUuid().toString());
        // entity.setProperty(ENTITY_PROPERTY_NAME_TITLE, title);
        // if (!ObjectUtils.isEmpty(body)) {
        // entity.setProperty(ENTITY_PROPERTY_NAME_BODY, body);
        // }
        //
        // entity.saveInBackground(new BaasioCallback<BaasioEntity>() {
        //
        // @Override
        // public void onException(BaasioException e) {
        // Toast.makeText(getActivity(), "saveInBackground =>" + e.toString(),
        // Toast.LENGTH_LONG).show();
        // }
        //
        // @Override
        // public void onResponse(BaasioEntity response) {
        // if (response != null) {
        // mEntityList.add(0, response);
        //
        // mListAdapter.notifyDataSetChanged();
        //
        // if (mEntityList.isEmpty()) {
        // mEmptyList.setVisibility(View.VISIBLE);
        // } else {
        // mEmptyList.setVisibility(View.GONE);
        // }
        // }
        // }
        // });
        // } else if (mode == EntityDialogFragment.MODIFY_ENTITY) {
        // BaasioEntity entity = new BaasioEntity(mEntityList.get(position));
        // entity.setProperty(ENTITY_PROPERTY_NAME_TITLE, title);
        // entity.setProperty(ENTITY_PROPERTY_NAME_BODY, body);
        //
        // entity.updateInBackground(new BaasioCallback<BaasioEntity>() {
        //
        // @Override
        // public void onException(BaasioException e) {
        // Toast.makeText(getActivity(), "updateInBackground =>" + e.toString(),
        // Toast.LENGTH_LONG).show();
        // }
        //
        // @Override
        // public void onResponse(BaasioEntity response) {
        // if (response != null) {
        // mEntityList.remove(position);
        // mEntityList.add(0, response);
        //
        // mListAdapter.notifyDataSetChanged();
        //
        // if (mEntityList.isEmpty()) {
        // mEmptyList.setVisibility(View.VISIBLE);
        // } else {
        // mEmptyList.setVisibility(View.GONE);
        // }
        // }
        // }
        // });
        // }

        return false;
    }

    public class EntityViewHolder {
        public ViewGroup mllRoot;

        public ImageView mivProfile;

        public ImageView mivFacebook;

        public TextView mtvName;

        public TextView mtvEmail;
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
        public BaasioUser getItem(int position) {
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
                convertView = mInflater.inflate(R.layout.listview_item_friend, parent, false);

                view = new EntityViewHolder();

                view.mllRoot = (ViewGroup)convertView.findViewById(R.id.llRoot);
                view.mivProfile = (ImageView)convertView.findViewById(R.id.ivProfile);
                view.mivFacebook = (ImageView)convertView.findViewById(R.id.ivFacebook);
                view.mtvName = (TextView)convertView.findViewById(R.id.tvName);
                view.mtvEmail = (TextView)convertView.findViewById(R.id.tvEmail);

                if (view != null) {
                    convertView.setTag(view);
                }
            } else {
                view = (EntityViewHolder)convertView.getTag();
            }

            BaasioUser entity = mEntityList.get(position);

            if (entity != null) {
                String name = entity.getName();
                if (!ObjectUtils.isEmpty(name)) {
                    view.mtvName.setText(name);
                } else {
                    view.mtvName.setText(entity.getUsername());
                }

                String email = entity.getEmail();
                if (!ObjectUtils.isEmpty(email)) {
                    view.mtvEmail.setText(email);
                }

                String imageUrl = entity.getPicture();
                if (imageUrl != null) {
                    mImageLoader.displayImage(imageUrl, view.mivProfile, options);
                } else {
                    view.mivProfile.setImageResource(R.drawable.person_image_empty);
                }

                if (!ObjectUtils.isEmpty(entity.getFacebook())) {
                    view.mivFacebook.setVisibility(View.VISIBLE);
                } else {
                    view.mivFacebook.setVisibility(View.GONE);
                }

                if (view.mllRoot != null) {
                    view.mllRoot.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            BaasioUser user = mEntityList.get(position);
                            Intent intent = new Intent(getActivity(), ProfileActivity.class);
                            intent.putExtra(ProfileFragment.INTENT_USER, user.toString());
                            startActivity(intent);
                        }
                    });
                }
            }
            return convertView;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.view.pulltorefresh.PullToRefreshBase.
     * OnRefreshListener#onRefresh()
     */
    @Override
    public void onRefresh() {
        getEntities(QUERY_REFRESH);
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.view.pulltorefresh.PullToRefreshBase.
     * OnRefreshListener#onUpdate()
     */
    @Override
    public void onUpdate() {
        getEntities(QUERY_NEXT);
    }
}
