
package io.baas.checktoe.ui.main;

import static com.kth.common.utils.LogUtils.makeLogTag;

import com.actionbarsherlock.view.MenuItem;
import com.kth.baasio.Baas;
import com.kth.baasio.callback.BaasioCallback;
import com.kth.baasio.entity.entity.BaasioEntity;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.utils.JsonUtils;
import com.kth.baasio.utils.ObjectUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import io.baas.checktoe.R;
import io.baas.checktoe.ui.BaseFragment;
import io.baas.checktoe.ui.dialog.DialogUtils;
import io.baas.checktoe.utils.EtcUtils;

public class EditChecklistFragment extends BaseFragment {
    private static final String TAG = makeLogTag(EditChecklistFragment.class);

    private DisplayImageOptions options;

    private ViewGroup mRootView;

    private EditText metTitle;

    private EditText metDescription;

    private EditText metTag;

    private BaasioEntity mChecklist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = new DisplayImageOptions.Builder().showStubImage(R.drawable.person_image_empty)
                .showImageForEmptyUri(R.drawable.person_image_empty).cacheInMemory().cacheOnDisc()
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            String intentString = intent.getStringExtra("checklist");

            if (!ObjectUtils.isEmpty(intentString)) {
                mChecklist = JsonUtils.parse(intentString, BaasioEntity.class);
            }
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_edit_checklist, null);

        metTitle = (EditText)mRootView.findViewById(R.id.etTitle);
        metDescription = (EditText)mRootView.findViewById(R.id.etDescription);
        metTag = (EditText)mRootView.findViewById(R.id.etTag);

        refreshViews();
        return mRootView;
    }

    private void refreshViews() {
        if (!ObjectUtils.isEmpty(mChecklist)) {

            String title = EtcUtils.getStringFromEntity(mChecklist,
                    ChecklistFragment.ENTITY_PROPERTY_NAME_TITLE);
            metTitle.setText(title);

            getSherlockActivity().getActionBar().setTitle(
                    getString(R.string.title_activity_edit_checklist) + "> " + title);

            String description = EtcUtils.getStringFromEntity(mChecklist,
                    ChecklistFragment.ENTITY_PROPERTY_NAME_DESCRIPTION);
            metDescription.setText(description);

            String tag = EtcUtils.getStringFromEntity(mChecklist,
                    ChecklistFragment.ENTITY_PROPERTY_NAME_TAG);
            metTag.setText(tag);
        }
    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
            com.actionbarsherlock.view.MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_edit_checklist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_finish: {
                String title = metTitle.getText().toString();
                if (ObjectUtils.isEmpty(title)) {
                    Toast.makeText(getActivity(), getString(R.string.error_no_title),
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                String description = metDescription.getText().toString();
                if (ObjectUtils.isEmpty(description)) {
                    Toast.makeText(getActivity(), getString(R.string.error_no_description),
                            Toast.LENGTH_LONG).show();
                    return false;
                }

                String tag = metTag.getText().toString();
                if (ObjectUtils.isEmpty(tag)) {
                    Toast.makeText(getActivity(), getString(R.string.error_no_tag),
                            Toast.LENGTH_LONG).show();
                    return false;
                }

                BaasioUser user = Baas.io().getSignedInUser();
                BaasioEntity entity = new BaasioEntity(ChecklistFragment.ENTITY_TYPE);
                entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_UUID, user
                        .getUuid().toString());
                entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_NAME,
                        user.getName());
                entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_PICTURE,
                        user.getPicture());
                if (!ObjectUtils.isEmpty(user.getFacebook())) {
                    entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_IS_FACEBOOK,
                            true);
                } else {
                    entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_OWNER_IS_FACEBOOK,
                            false);
                }

                entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_TITLE, title.trim());
                entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_DESCRIPTION,
                        description.trim());
                entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_TAG, tag.trim());
                entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_DELETED, false);
                entity.setProperty(ChecklistFragment.ENTITY_PROPERTY_NAME_SHARED, true);

                DialogUtils.showProgressDialog(this, "saving",
                        getString(R.string.progress_dialog_saving));

                entity.saveInBackground(new BaasioCallback<BaasioEntity>() {

                    @Override
                    public void onResponse(BaasioEntity response) {
                        new Handler().post(new Runnable() {

                            @Override
                            public void run() {
                                DialogUtils.dissmissProgressDialog(EditChecklistFragment.this,
                                        "saving");
                            }
                        });

                        if (!ObjectUtils.isEmpty(response)) {
                            Toast.makeText(getActivity(),
                                    getString(R.string.success_add_checklist), Toast.LENGTH_LONG)
                                    .show();

                            getActivity().finish();
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    getString(R.string.fail_add_checklist,
                                            getString(R.string.error_unknown)), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onException(BaasioException e) {
                        new Handler().post(new Runnable() {

                            @Override
                            public void run() {
                                DialogUtils.dissmissProgressDialog(EditChecklistFragment.this,
                                        "saving");
                            }
                        });

                        Toast.makeText(getActivity(),
                                getString(R.string.fail_add_checklist, e.getErrorDescription()),
                                Toast.LENGTH_LONG).show();
                    }
                });
                break;
            }
            case R.id.menu_cancel: {
                getActivity().finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
