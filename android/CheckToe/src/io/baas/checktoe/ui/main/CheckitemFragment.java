
package io.baas.checktoe.ui.main;

import static com.kth.common.utils.LogUtils.LOGV;
import static com.kth.common.utils.LogUtils.makeLogTag;

import com.actionbarsherlock.view.MenuItem;
import com.kth.baasio.Baas;
import com.kth.baasio.callback.BaasioAsyncTask;
import com.kth.baasio.callback.BaasioCallback;
import com.kth.baasio.callback.BaasioQueryCallback;
import com.kth.baasio.entity.BaasioBaseEntity;
import com.kth.baasio.entity.entity.BaasioEntity;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.exception.BaasioError;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.query.BaasioQuery;
import com.kth.baasio.query.BaasioQuery.ORDER_BY;
import com.kth.baasio.response.BaasioResponse;
import com.kth.baasio.utils.JsonUtils;
import com.kth.baasio.utils.ObjectUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import org.springframework.http.HttpMethod;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

public class CheckitemFragment extends BaseFragment implements OnRefreshListener, Callback {

    private static final String TAG = makeLogTag(CheckitemFragment.class);

    public static final String ENTITY_TYPE = "citem";

    public static final String ENTITY_PROPERTY_NAME_ID_CLIST = "id_clist";

    public static final String ENTITY_PROPERTY_NAME_ID_ORIGIN = "id_origin";

    public static final String ENTITY_PROPERTY_NAME_ID_PARENT = "id_parent";

    public static final String ENTITY_PROPERTY_NAME_TITLE = "title";

    public static final String ENTITY_PROPERTY_NAME_DESCRIPTION = "description";

    public static final String ENTITY_PROPERTY_NAME_TAG = "tag";

    public static final String ENTITY_PROPERTY_NAME_DUE_DATE = "due_date";

    public static final String ENTITY_PROPERTY_NAME_NOTIFICATION = "notification";

    public static final String ENTITY_PROPERTY_NAME_PRIORITY = "priority";

    public static final String ENTITY_PROPERTY_NAME_OWNER_UUID = "owner_uuid";

    public static final String ENTITY_PROPERTY_NAME_OWNER_NAME = "owner_name";

    public static final String ENTITY_PROPERTY_NAME_OWNER_PICTURE = "owner_picture";

    public static final String ENTITY_PROPERTY_NAME_OWNER_IS_FACEBOOK = "owner_isfacebook";

    public static final String ENTITY_PROPERTY_NAME_ASSIGNOR_UUID = "assignor_uuid";

    public static final String ENTITY_PROPERTY_NAME_ASSIGNOR_NAME = "assignor_name";

    public static final String ENTITY_PROPERTY_NAME_ASSIGNOR_PICTURE = "assignor_picture";

    public static final String ENTITY_PROPERTY_NAME_CTYPE = "ctype";

    public static final String ENTITY_PROPERTY_NAME_STATUS = "status";

    public static final String ENTITY_PROPERTY_NAME_DELETED = "deleted";

    private DisplayImageOptions options;

    private ViewGroup mRootView;

    private ImageView mivProfile;

    private ImageView mivFacebook;

    private TextView mtvTitle;

    private TextView mtvName;

    private TextView mtvDescription;

    private PullToRefreshListView mlvPullToRefresh;

    private ListView mlvList;

    private TextView mtvEmpty;

    private EntityListAdapter mListAdapter;

    private ArrayList<BaasioEntity> mEntityList;

    private BaasioQuery mQuery;

    private ActionMode mActionMode;

    private View mLongClickedView;

    private Integer mLongClickedPosition;

    private BaasioEntity mChecklist;

    private boolean mIsAddedFavorite = false;

    public static final int QUERY_INIT = 0;

    public static final int QUERY_REFRESH = 1;

    public static final int QUERY_NEXT = 2;

    public CheckitemFragment() {
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
            String intentString = intent.getStringExtra("checklist");

            if (!ObjectUtils.isEmpty(intentString)) {
                mChecklist = JsonUtils.parse(intentString, BaasioEntity.class);
            }
        }

        getEntities(QUERY_INIT);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_checkitem, null);

        mivProfile = (ImageView)mRootView.findViewById(R.id.ivProfile);
        mivProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String ownerUuid = EtcUtils.getStringFromEntity(mChecklist,
                        ENTITY_PROPERTY_NAME_OWNER_UUID);

                if (!ObjectUtils.isEmpty(ownerUuid)) {
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.putExtra(ProfileFragment.INTENT_USER_UUID, ownerUuid);
                    startActivity(intent);
                }
            }
        });

        String imageUrl = EtcUtils.getStringFromEntity(mChecklist,
                ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_PICTURE);
        if (!ObjectUtils.isEmpty(imageUrl)) {
            mImageLoader.displayImage(imageUrl, mivProfile, options);
        }

        mivFacebook = (ImageView)mRootView.findViewById(R.id.ivFacebook);

        boolean isFacebook = EtcUtils.getBooleanFromEntity(mChecklist,
                ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_IS_FACEBOOK, false);
        if (isFacebook) {
            mivFacebook.setVisibility(View.VISIBLE);
        } else {
            mivFacebook.setVisibility(View.GONE);
        }

        mtvTitle = (TextView)mRootView.findViewById(R.id.tvTitle);
        mtvName = (TextView)mRootView.findViewById(R.id.tvName);
        mtvDescription = (TextView)mRootView.findViewById(R.id.tvDescription);

        String title = EtcUtils.getStringFromEntity(mChecklist,
                ChecklistFragment.ENTITY_PROPERTY_NAME_TITLE);
        if (!ObjectUtils.isEmpty(title)) {
            mtvTitle.setText(title);
        }

        String name = EtcUtils.getStringFromEntity(mChecklist,
                ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_NAME);
        if (!ObjectUtils.isEmpty(name)) {
            mtvName.setText(name);
        }

        String description = EtcUtils.getStringFromEntity(mChecklist,
                ChecklistFragment.ENTITY_PROPERTY_NAME_DESCRIPTION);
        if (!ObjectUtils.isEmpty(description)) {
            mtvDescription.setText(description);
        }

        mtvEmpty = (TextView)mRootView.findViewById(R.id.tvEmpty);
        mtvEmpty.setText(getString(R.string.empty_checkitem));

        mlvPullToRefresh = (PullToRefreshListView)mRootView.findViewById(R.id.lvPullToRefresh);
        mlvPullToRefresh.setOnRefreshListener(this);

        mlvList = mlvPullToRefresh.getRefreshableView();
        mlvList.setAdapter(mListAdapter);

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

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
            com.actionbarsherlock.view.MenuInflater inflater) {

        BaasioUser user = Baas.io().getSignedInUser();
        if (!ObjectUtils.isEmpty(user)) {
            inflater.inflate(R.menu.fragment_checkitem, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        BaasioUser user = Baas.io().getSignedInUser();
        if (ObjectUtils.isEmpty(user)) {
            return;
        }

        MenuItem addCheckitem = menu.findItem(R.id.menu_add_checkitem);
        MenuItem addFavorite = menu.findItem(R.id.menu_add_favorite);
        MenuItem removeFavorite = menu.findItem(R.id.menu_remove_favorite);
        MenuItem cloneChecklist = menu.findItem(R.id.menu_clone_checklist);

        if (!isSigninUserOwner()) {
            if (isAddedFavoriteChecklist()) {
                addFavorite.setVisible(false);
                removeFavorite.setVisible(true);
            } else {
                addFavorite.setVisible(true);
                removeFavorite.setVisible(false);
            }

            addCheckitem.setVisible(false);

            cloneChecklist.setVisible(true);
        } else {
            addFavorite.setVisible(false);
            removeFavorite.setVisible(false);

            addCheckitem.setVisible(true);

            cloneChecklist.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_favorite: {
                mIsAddedFavorite = !mIsAddedFavorite;
                getSherlockActivity().invalidateOptionsMenu();
                break;
            }
            case R.id.menu_remove_favorite: {
                mIsAddedFavorite = !mIsAddedFavorite;
                getSherlockActivity().invalidateOptionsMenu();
                break;
            }
            case R.id.menu_clone_checklist: {
                cloneChecklist();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void cloneChecklist() {
        BaasioUser user = Baas.io().getSignedInUser();
        BaasioEntity entity = new BaasioEntity(mChecklist);
        entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_UUID, user.getUuid()
                .toString());

        entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_NAME, user.getName());

        entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_PICTURE, user.getPicture());

        if (!ObjectUtils.isEmpty(user.getFacebook())) {
            entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_IS_FACEBOOK, true);
        } else {
            entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_IS_FACEBOOK, false);
        }

        DialogUtils.showProgressDialog(this, "cloning", getString(R.string.progress_dialog_saving));

        entity.saveInBackground(new BaasioCallback<BaasioEntity>() {

            @Override
            public void onResponse(BaasioEntity response) {
                if (!ObjectUtils.isEmpty(response)) {
                    cloneCheckitemsInBackground(response, mEntityList,
                            new BaasioCallback<List<BaasioEntity>>() {
                                @Override
                                public void onResponse(List<BaasioEntity> response) {
                                    new Handler().post(new Runnable() {

                                        @Override
                                        public void run() {
                                            DialogUtils.dissmissProgressDialog(
                                                    CheckitemFragment.this, "cloning");
                                        }
                                    });

                                    if (!ObjectUtils.isEmpty(response)) {
                                        Toast.makeText(getActivity(),
                                                getString(R.string.success_clone_checklist),
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(
                                                getActivity(),
                                                getString(R.string.fail_clone_checklist,
                                                        getString(R.string.error_unknown)),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onException(BaasioException e) {
                                    new Handler().post(new Runnable() {

                                        @Override
                                        public void run() {
                                            DialogUtils.dissmissProgressDialog(
                                                    CheckitemFragment.this, "cloning");
                                        }
                                    });

                                    Toast.makeText(
                                            getActivity(),
                                            getString(R.string.fail_clone_checklist,
                                                    e.getErrorDescription()), Toast.LENGTH_LONG)
                                            .show();
                                }
                            });

                } else {
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            DialogUtils.dissmissProgressDialog(CheckitemFragment.this, "cloning");
                        }
                    });

                    Toast.makeText(
                            getActivity(),
                            getString(R.string.fail_clone_checklist,
                                    getString(R.string.error_unknown)), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onException(BaasioException e) {
                new Handler().post(new Runnable() {

                    @Override
                    public void run() {
                        DialogUtils.dissmissProgressDialog(CheckitemFragment.this, "cloning");
                    }
                });

                Toast.makeText(getActivity(),
                        getString(R.string.fail_clone_checklist, e.getErrorDescription()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<BaasioEntity> cloneCheckitems(BaasioEntity checklist,
            ArrayList<BaasioEntity> checkitems) throws BaasioException {
        BaasioUser user = Baas.io().getSignedInUser();

        ArrayList<BaasioEntity> items = new ArrayList<BaasioEntity>();
        for (BaasioEntity entry : checkitems) {
            BaasioEntity clonedItem = new BaasioEntity(entry);
            clonedItem.setProperty(CheckitemFragment.ENTITY_PROPERTY_NAME_ID_CLIST, checklist
                    .getUuid().toString());
            clonedItem.setProperty(CheckitemFragment.ENTITY_PROPERTY_NAME_OWNER_UUID, user
                    .getUuid().toString());
            clonedItem.setProperty(CheckitemFragment.ENTITY_PROPERTY_NAME_OWNER_NAME,
                    user.getName());
            clonedItem.setProperty(CheckitemFragment.ENTITY_PROPERTY_NAME_OWNER_PICTURE,
                    user.getPicture());
            clonedItem.setProperty(CheckitemFragment.ENTITY_PROPERTY_NAME_ASSIGNOR_UUID, "");
            clonedItem.setProperty(CheckitemFragment.ENTITY_PROPERTY_NAME_ASSIGNOR_PICTURE, "");
            clonedItem.setProperty(CheckitemFragment.ENTITY_PROPERTY_NAME_ASSIGNOR_NAME, "");

            items.add(clonedItem);
        }

        BaasioResponse response = Baas.io().apiRequest(HttpMethod.POST, null, items,
                CheckitemFragment.ENTITY_TYPE);

        if (!ObjectUtils.isEmpty(response)) {
            return BaasioBaseEntity.toType(response.getEntities(), BaasioEntity.class);
        }

        throw new BaasioException(BaasioError.ERROR_UNKNOWN_NO_RESPONSE_DATA);
    }

    public void cloneCheckitemsInBackground(final BaasioEntity checklist,
            final ArrayList<BaasioEntity> checkitems,
            final BaasioCallback<List<BaasioEntity>> callback) {
        (new BaasioAsyncTask<List<BaasioEntity>>(callback) {
            @Override
            public List<BaasioEntity> doTask() throws BaasioException {
                return cloneCheckitems(checklist, checkitems);
            }
        }).execute();
    }

    private boolean isSigninUserOwner() {
        BaasioUser user = Baas.io().getSignedInUser();

        String ownerUuid = EtcUtils
                .getStringFromEntity(mChecklist, ENTITY_PROPERTY_NAME_OWNER_UUID);

        if (user.getUuid().toString().equals(ownerUuid)) {
            return true;
        }

        return false;
    }

    private boolean isAddedFavoriteChecklist() {
        return mIsAddedFavorite;
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

                    DialogUtils.dissmissProgressDialog(CheckitemFragment.this, "loading");
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

                    DialogUtils.dissmissProgressDialog(CheckitemFragment.this, "loading");
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
            mQuery.setWheres(ENTITY_PROPERTY_NAME_ID_CLIST + " = "
                    + mChecklist.getUuid().toString());
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
                convertView = mInflater.inflate(R.layout.listview_item_checkitem, parent, false);

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
                        ENTITY_PROPERTY_NAME_ASSIGNOR_PICTURE);
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
                            // BaasioEntity entity = mEntityList.get(position);
                            //
                            // Intent intent = new Intent(getActivity(),
                            // PostDetailActivity.class);
                            // intent.putExtra(Intent.EXTRA_TITLE,
                            // getString(R.string.title_activity_postdetail));
                            // intent.putExtra("post", entity.toString());
                            //
                            // startActivity(intent);
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

                            mActionMode = ActionMode.start(getActivity(), CheckitemFragment.this);
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

        // MenuInflater inflater = mode.getMenuInflater();
        // inflater.inflate(R.menu.contextmenu_fragment_post, menu);

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
            EtcUtils.setActivatedCompat(mLongClickedView, false);
            mLongClickedPosition = null;
            mLongClickedView = null;
        }
    }

}
