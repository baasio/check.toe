
package io.baas.checktoe.ui.dialog;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class DialogUtils {

    public static ProgressDialogFragment showProgressDialog(Fragment fragment, String tag,
            String body, int style) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
        Fragment prev = fragment.getFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        ProgressDialogFragment progress = ProgressDialogFragment.newInstance();
        progress.setBody(body);
        progress.setStyle(style);
        progress.show(ft, tag);

        return progress;
    }

    public static ProgressDialogFragment showProgressDialog(Fragment fragment, String tag,
            String body) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
        Fragment prev = fragment.getFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        ProgressDialogFragment progress = ProgressDialogFragment.newInstance();
        progress.setBody(body);
        progress.show(ft, tag);

        return progress;
    }

    public static void setProgress(Fragment fragment, String tag, int progressValue) {
        ProgressDialogFragment progress = (ProgressDialogFragment)fragment.getFragmentManager()
                .findFragmentByTag(tag);

        if (progress != null) {
            progress.setProgress(progressValue);
        }
    }

    public static void dissmissProgressDialog(Fragment fragment, String tag) {
        ProgressDialogFragment progress = (ProgressDialogFragment)fragment.getFragmentManager()
                .findFragmentByTag(tag);

        if (progress != null) {
            progress.dismiss();
        }
    }

    public static ProgressDialogFragment showProgressDialog(FragmentActivity fragmentActivity,
            String tag, String body) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = fragmentActivity.getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        ProgressDialogFragment progress = ProgressDialogFragment.newInstance();
        progress.setBody(body);
        progress.setCancelable(false);
        progress.show(ft, tag);

        return progress;
    }

    public static ProgressDialogFragment showProgressDialog(FragmentActivity fragmentActivity,
            String tag, String body, int style) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = fragmentActivity.getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        ProgressDialogFragment progress = ProgressDialogFragment.newInstance();
        progress.setBody(body);
        progress.setStyle(style);
        progress.show(ft, tag);

        return progress;
    }

    public static void setProgress(FragmentActivity fragmentActivity, String tag, int progressValue) {
        ProgressDialogFragment progress = (ProgressDialogFragment)fragmentActivity
                .getSupportFragmentManager().findFragmentByTag(tag);

        if (progress != null) {
            progress.setProgress(progressValue);
        }
    }

    public static void dissmissProgressDialog(FragmentActivity fragmentActivity, String tag) {
        ProgressDialogFragment progress = (ProgressDialogFragment)fragmentActivity
                .getSupportFragmentManager().findFragmentByTag(tag);

        if (progress != null) {
            progress.dismiss();
        }
    }
}
