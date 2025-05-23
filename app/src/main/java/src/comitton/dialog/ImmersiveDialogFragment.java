package src.comitton.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * Workaround for immersive mode breaking when dialogs are shown
 *
 * See https://stackoverflow.com/questions/22794049/how-do-i-maintain-the-immersive-mode-in-dialogs/38469972#38469972
 *
 */
public abstract class ImmersiveDialogFragment extends DialogFragment {

    private static final String DEBUG_TAG = ImmersiveDialogFragment.class.getSimpleName().substring(0, Math.min(23, ImmersiveDialogFragment.class.getSimpleName().length()));

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        // Make the dialog non-focusable before showing it
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        showImmersive(manager);
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        int result = super.show(transaction, tag);
        showImmersive(getParentFragmentManager());
        return result;
    }

    /**
     * Show a dialog in immersive mode
     *
     * @param manager our FragmentManager
     */
    @SuppressLint("NewApi")
    private void showImmersive(@NonNull FragmentManager manager) {
        final Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            // It is necessary to call executePendingTransactions() on the FragmentManager
            // before hiding the navigation bar, because otherwise getWindow() would raise a
            // NullPointerException since the window was not yet created.
            if (!manager.isDestroyed()) {
                manager.executePendingTransactions();

                Dialog dialog = getDialog();

                if (dialog != null && dialog.getWindow() != null) { // seems to be an issue on some systems
                    Window dialogWindow = dialog.getWindow();
                    // Copy flags from the activity, assuming it's fullscreen.
                    // It is important to do this after show() was called. If we would do this in onCreateDialog(),
                    // we would get a requestFeature() error.
                    dialogWindow.getDecorView().setSystemUiVisibility(getActivity().getWindow().getDecorView().getSystemUiVisibility());

                    // Make the dialogs window focusable again
                    dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                }
            } else {
                Log.e(DEBUG_TAG, "FragmentManager is detroyed");
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }
}