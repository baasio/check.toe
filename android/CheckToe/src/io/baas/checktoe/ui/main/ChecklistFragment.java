
package io.baas.checktoe.ui.main;

import static com.kth.common.utils.LogUtils.LOGV;
import static com.kth.common.utils.LogUtils.makeLogTag;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.baas.checktoe.R;
import io.baas.checktoe.ui.SearchableBaseFragment;
import io.baas.checktoe.ui.dialog.DialogUtils;
import io.baas.checktoe.ui.view.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import io.baas.checktoe.ui.view.pulltorefresh.PullToRefreshListView;
import io.baas.checktoe.utils.EtcUtils;
import io.baas.checktoe.utils.actionmodecompat.ActionMode;
import io.baas.checktoe.utils.actionmodecompat.ActionMode.Callback;

import java.util.ArrayList;
import java.util.List;

public class ChecklistFragment extends SearchableBaseFragment implements OnRefreshListener,
        Callback {

    private static final String TAG = makeLogTag(ChecklistFragment.class);

    public static final String ENTITY_TYPE = "clist";

    public static final String ENTITY_PROPERTY_NAME_ID_ORIGIN = "id_origin";

    public static final String ENTITY_PROPERTY_NAME_ID_PARENT = "id_parent";

    public static final String ENTITY_PROPERTY_NAME_OWNER_UUID = "owner_uuid";

    public static final String ENTITY_PROPERTY_NAME_OWNER_NAME = "owner_name";

    public static final String ENTITY_PROPERTY_NAME_OWNER_PICTURE = "owner_picture";

    public static final String ENTITY_PROPERTY_NAME_OWNER_IS_FACEBOOK = "owner_isfacebook";

    public static final String ENTITY_PROPERTY_NAME_TITLE = "title";

    public static final String ENTITY_PROPERTY_NAME_DESCRIPTION = "description";

    public static final String ENTITY_PROPERTY_NAME_TAG = "tag";

    public static final String ENTITY_PROPERTY_NAME_DELETED = "deleted";

    public static final String ENTITY_PROPERTY_NAME_SHARED = "shared";

    private DisplayImageOptions options;

    private ViewGroup mRootView;

    private PullToRefreshListView mlvPullToRefresh;

    private ListView mlvList;

    private TextView mtvEmpty;

    private EntityListAdapter mListAdapter;

    private ArrayList<BaasioEntity> mEntityList;

    private BaasioQuery mQuery;

    private ActionMode mActionMode;

    private View mLongClickedView;

    private Integer mLongClickedPosition;

    private int mViewMode = MainActivity.MODE_MY_CHECKLIST;

    private String mSearchKeyword = "";

    public static final int QUERY_INIT = 0;

    public static final int QUERY_REFRESH = 1;

    public static final int QUERY_NEXT = 2;

    public ChecklistFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = new DisplayImageOptions.Builder().showStubImage(R.drawable.person_image_empty)
                .showImageForEmptyUri(R.drawable.person_image_empty).cacheInMemory().cacheOnDisc()
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        mEntityList = new ArrayList<BaasioEntity>();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_pulltorefresh_list, null);

        mtvEmpty = (TextView)mRootView.findViewById(R.id.tvEmpty);
        mtvEmpty.setText(getString(R.string.empty_checklist));

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

        if (!ObjectUtils.isEmpty(Baas.io().getSignedInUser())) {
            if (arguments != null)
                mViewMode = arguments.getInt("checklist_mode", MainActivity.MODE_MY_CHECKLIST);
            else
                mViewMode = MainActivity.MODE_MY_CHECKLIST;
        } else {
            mViewMode = MainActivity.MODE_RECOMMEND_CHECKLIST;
        }

        setTitle();

        mListAdapter = new EntityListAdapter(getActivity());
        mlvList.setAdapter(mListAdapter);

        getEntities(QUERY_INIT);

        getSherlockActivity().invalidateOptionsMenu();
    }

    private void setTitle() {
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            String searchKeyword = intent.getStringExtra(SearchManager.QUERY);

            if (!ObjectUtils.isEmpty(searchKeyword)) {
                mSearchKeyword = searchKeyword.trim();

                getSherlockActivity().getSupportActionBar().setTitle(
                        getResources().getString(R.string.search_title, mSearchKeyword));
            }
        }

        if (ObjectUtils.isEmpty(mSearchKeyword)) {
            switch (mViewMode) {
                case MainActivity.MODE_MY_CHECKLIST: {
                    getSherlockActivity().getSupportActionBar().setTitle(
                            getString(R.string.title_activity_my_checklist));
                    break;
                }
                case MainActivity.MODE_RECOMMEND_CHECKLIST: {
                    getSherlockActivity().getSupportActionBar().setTitle(
                            getString(R.string.title_activity_recomment_checklist));
                    break;
                }
                case MainActivity.MODE_FAVORITE_CHECKLIST: {
                    getSherlockActivity().getSupportActionBar().setTitle(
                            getString(R.string.title_activity_favorite_checklist));
                    break;
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
        if (ObjectUtils.isEmpty(mSearchKeyword)) {
            if (mViewMode == MainActivity.MODE_MY_CHECKLIST) {
                inflater.inflate(R.menu.fragment_my_checklist, menu);
            } else if (mViewMode == MainActivity.MODE_RECOMMEND_CHECKLIST) {
                inflater.inflate(R.menu.fragment_recommend_checklist, menu);
            } else if (mViewMode == MainActivity.MODE_FAVORITE_CHECKLIST) {
                inflater.inflate(R.menu.fragment_favorite_checklist, menu);
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        // TODO Auto-generated method stub
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                Bundle bundle = new Bundle();
                bundle.putInt(SearchableActivity.INTENT_BUNDLE_SEARCHMODE,
                        SearchableActivity.SEARCHMODE_CHECKLIST);
                getSherlockActivity().startSearch(null, false, bundle, false);
                break;
            case R.id.menu_new_checklist: {
                Intent intent = new Intent(getActivity(), EditChecklistActivity.class);
                intent.putExtra(Intent.EXTRA_TITLE,
                        getString(R.string.title_activity_new_checklist));
                startActivity(intent);
                break;
            }

            case R.id.menu_edit_checklist: {
                Intent intent = new Intent(getActivity(), EditChecklistActivity.class);
                intent.putExtra(Intent.EXTRA_TITLE,
                        getString(R.string.title_activity_edit_checklist));
                startActivity(intent);
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
                    if (mlvPullToRefresh != null) {
                        if (mlvPullToRefresh.isRefreshing())
                            mlvPullToRefresh.onRefreshComplete();
                    }

                    DialogUtils.dissmissProgressDialog(ChecklistFragment.this, "loading");
                }
            });

            mEntityList.clear();

            mQuery = query;

            List<BaasioEntity> posts = BaasioBaseEntity.toType(entities, BaasioEntity.class);
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

                    DialogUtils.dissmissProgressDialog(ChecklistFragment.this, "loading");
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

            List<BaasioEntity> posts = BaasioBaseEntity.toType(entities, BaasioEntity.class);
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
            mQuery.setType(ENTITY_TYPE);

            StringBuilder builder = new StringBuilder();

            if (!ObjectUtils.isEmpty(mSearchKeyword)) {
                builder.append(ENTITY_PROPERTY_NAME_TAG + " contains " + "'" + mSearchKeyword
                        + "*'");
            } else {
                if (mViewMode == MainActivity.MODE_MY_CHECKLIST) {
                    BaasioUser user = Baas.io().getSignedInUser();
                    builder.append(ENTITY_PROPERTY_NAME_OWNER_UUID + " = "
                            + user.getUuid().toString());
                }
            }

            if (builder.length() != 0) {
                builder.append(" and ");
            }

            builder.append(ENTITY_PROPERTY_NAME_DELETED + " = false");

            mQuery.setWheres(builder.toString());
            mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED, ORDER_BY.DESCENDING);
        }

        if (mode == QUERY_INIT) {
            DialogUtils.showProgressDialog(this, "loading",
                    getString(R.string.progress_dialog_loading));

            mQuery.queryInBackground(mQueryCallback);

        } else if (mode == QUERY_REFRESH) {
            mQuery.queryInBackground(mQueryCallback);
        } else
            mQuery.nextInBackground(mQueryNextCallback);

    }

    public class EntityViewHolder {
        public ViewGroup mllRoot;

        public ImageView mivProfile;

        public ImageView mivFacebook;

        public TextView mtvTitle;

        public TextView mtvDescription;

        public TextView mtvCreatedTime;

        public TextView mtvModifiedTime;
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
                convertView = mInflater.inflate(R.layout.listview_item_checklist, parent, false);

                view = new EntityViewHolder();

                view.mllRoot = (ViewGroup)convertView.findViewById(R.id.llRoot);
                view.mivProfile = (ImageView)convertView.findViewById(R.id.ivProfile);
                view.mivFacebook = (ImageView)convertView.findViewById(R.id.ivFacebook);
                view.mtvTitle = (TextView)convertView.findViewById(R.id.tvTitle);
                view.mtvDescription = (TextView)convertView.findViewById(R.id.tvDescription);
                view.mtvCreatedTime = (TextView)convertView.findViewById(R.id.tvCreatedTime);
                view.mtvModifiedTime = (TextView)convertView.findViewById(R.id.tvModifiedTime);

                if (view != null) {
                    convertView.setTag(view);
                }
            } else {
                view = (EntityViewHolder)convertView.getTag();
            }

            BaasioEntity entity = mEntityList.get(position);

            if (entity != null) {
                setStringToView(entity, view.mtvTitle, ENTITY_PROPERTY_NAME_TITLE);
                setStringToView(entity, view.mtvDescription, ENTITY_PROPERTY_NAME_DESCRIPTION);

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

                if (entity.getCreated() != null) {
                    String createdTime = EtcUtils.getDateString(entity.getCreated());
                    if (!TextUtils.isEmpty(createdTime)) {
                        view.mtvCreatedTime.setText(getString(R.string.created_time, createdTime));
                    }
                }

                if (entity.getModified() != null) {
                    String modifiedTime = EtcUtils.getDateString(entity.getModified());
                    if (!TextUtils.isEmpty(modifiedTime)) {
                        view.mtvModifiedTime
                                .setText(getString(R.string.modified_time, modifiedTime));
                    }
                }

                if (view.mllRoot != null) {
                    view.mllRoot.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            BaasioEntity entity = mEntityList.get(position);

                            String title = EtcUtils.getStringFromEntity(entity,
                                    ENTITY_PROPERTY_NAME_TITLE);

                            Intent intent = new Intent(getActivity(), CheckitemActivity.class);
                            intent.putExtra(Intent.EXTRA_TITLE, title);
                            intent.putExtra("checklist", entity.toString());

                            startActivity(intent);
                        }
                    });
                    view.mllRoot.setOnLongClickListener(new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View view) {
                            if (mActionMode != null) {
                                // CAB already displayed, ignore
                                return true;
                            }

                            mLongClickedView = view;
                            mLongClickedPosition = position;

                            mActionMode = ActionMode.start(getActivity(), ChecklistFragment.this);
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

        BaasioEntity entity = mEntityList.get(mLongClickedPosition);
        String title = EtcUtils.getStringFromEntity(entity, ENTITY_PROPERTY_NAME_TITLE);
        mode.setTitle(title);

        mLongClickedView.setBackgroundColor(getResources().getColor(R.color.abs__holo_blue_light));

        MenuInflater inflater = mode.getMenuInflater();
        if (ObjectUtils.isEmpty(mSearchKeyword)) {
            if (mViewMode == MainActivity.MODE_MY_CHECKLIST) {
                inflater.inflate(R.menu.contextmenu_my_checklist, menu);
            } else if (mViewMode == MainActivity.MODE_RECOMMEND_CHECKLIST) {
                inflater.inflate(R.menu.contextmenu_recommend_checklist, menu);
            } else if (mViewMode == MainActivity.MODE_FAVORITE_CHECKLIST) {
                inflater.inflate(R.menu.contextmenu_favorite_checklist, menu);
            }
        } else {
            inflater.inflate(R.menu.contextmenu_search_checklist, menu);
        }

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
            case R.id.menu_edit_checklist: {
                BaasioEntity entity = mEntityList.get(mLongClickedPosition);
                if (!ObjectUtils.isEmpty(entity)) {
                    Intent intent = new Intent(getActivity(), EditChecklistActivity.class);
                    intent.putExtra("checklist", entity.toString());
                    startActivity(intent);
                }
                break;
            }

            case R.id.menu_remove_checklist: {
                BaasioEntity entity = mEntityList.get(mLongClickedPosition);

                final int removePosition = mLongClickedPosition;
                if (!ObjectUtils.isEmpty(entity)) {
                    DialogUtils.showProgressDialog(this, "deleting",
                            getString(R.string.progress_dialog_removing));

                    entity.deleteInBackground(new BaasioCallback<BaasioEntity>() {

                        @Override
                        public void onResponse(BaasioEntity response) {
                            new Handler().post(new Runnable() {

                                @Override
                                public void run() {
                                    DialogUtils.dissmissProgressDialog(ChecklistFragment.this,
                                            "deleting");
                                }
                            });

                            if (!ObjectUtils.isEmpty(response)) {
                                Toast.makeText(getActivity(),
                                        getString(R.string.success_remove_checklist),
                                        Toast.LENGTH_LONG).show();

                                mEntityList.remove(removePosition);
                                mListAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(
                                        getActivity(),
                                        getString(R.string.fail_remove_checklist,
                                                getString(R.string.error_unknown)),
                                        Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onException(BaasioException e) {
                            new Handler().post(new Runnable() {

                                @Override
                                public void run() {
                                    DialogUtils.dissmissProgressDialog(ChecklistFragment.this,
                                            "deleting");
                                }
                            });

                            Toast.makeText(
                                    getActivity(),
                                    getString(R.string.fail_remove_checklist,
                                            e.getErrorDescription()), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                break;
            }
        }

        LOGV(TAG,
                "onActionItemClicked: position=" + mLongClickedPosition + " title="
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
            mLongClickedView.setBackgroundColor(getResources()
                    .getColor(android.R.color.transparent));

            EtcUtils.setActivatedCompat(mLongClickedView, false);
            mLongClickedPosition = null;
            mLongClickedView = null;
        }
    }

}
