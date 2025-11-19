package src.comitton.dialog;

import static src.comitton.dialog.PageThumbnail.mThumView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import java.util.Arrays;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.common.ImageAccess;
import src.comitton.common.Logcat;
import src.comitton.imageview.PageSelectListener;

@SuppressLint("NewApi")
public class ToolbarDialog extends ImmersiveDialog implements
		OnClickListener, OnSeekBarChangeListener, DialogInterface.OnDismissListener {

	protected static PageSelectListener mListener = null;
	protected Activity mActivity;

	// パラメータ
	protected boolean mViewer;
	protected boolean mDirTree;
	protected int mShareType = DEF.SHARE_SINGLE;
	protected static int mPage;
	protected static int mMaxPage;
	protected static boolean mReverse;
	protected static boolean mAutoApply;

	protected boolean mShare;
	protected static SeekBar mSeekPage;

	// ツールバーを動的に表示する変更に伴いコメントアウトにした
	/*
	private AppCompatImageButton mBtnLeftMost;
	private AppCompatImageButton mBtnLeft100;
	private AppCompatImageButton mBtnLeft10;
	private AppCompatImageButton mBtnLeft1;
	private AppCompatImageButton mBtnRight1;
	private AppCompatImageButton mBtnRight10;
	private AppCompatImageButton mBtnRight100;
	private AppCompatImageButton mBtnRightMost;
	private AppCompatImageButton mBtnPageReset;

	private AppCompatImageButton mBtnBookLeft;
	private AppCompatImageButton mBtnBookRight;
	private AppCompatImageButton mBtnBookmarkLeft;
	private AppCompatImageButton mBtnBookmarkRight;
	private AppCompatImageButton mBtnThumbSlider;
	private AppCompatImageButton mBtnDirTree;
	private AppCompatImageButton mBtnTOC;
	private AppCompatImageButton mBtnFavorite;
	private AppCompatImageButton mBtnAddFavorite;
	private AppCompatImageButton mBtnSearch;
	private AppCompatImageButton mBtnShare;
	private AppCompatImageButton mBtnShareLeftPage;
	private AppCompatImageButton mBtnShareRightPage;
	private AppCompatImageButton mBtnRotate;
	private AppCompatImageButton mBtnRotateImage;
	private AppCompatImageButton mBtnSelectThum;
	private AppCompatImageButton mBtnTrimThumb;
	private AppCompatImageButton mBtnControl;
	private AppCompatImageButton mBtnMenu;
	private AppCompatImageButton mBtnConfig;
	private AppCompatImageButton mBtnEditButton;
	private AppCompatImageButton mBtnProfile1;
	private AppCompatImageButton mBtnProfile2;
	private AppCompatImageButton mBtnProfile3;
	private AppCompatImageButton mBtnProfile4;
	private AppCompatImageButton mBtnProfile5;
	private AppCompatImageButton mBtnProfile6;
	private AppCompatImageButton mBtnProfile7;
	private AppCompatImageButton mBtnProfile8;
	private AppCompatImageButton mBtnProfile9;
	private AppCompatImageButton mBtnProfile10;

	*/
	public ToolbarDialog(AppCompatActivity activity, @StyleRes int themeResId) {
		super(activity, themeResId);
		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を設定
		dlgWindow.setBackgroundDrawableResource(R.drawable.dialognoframe);

		// 画面下に表示
		WindowManager.LayoutParams wmlp = dlgWindow.getAttributes();
		wmlp.gravity = Gravity.BOTTOM;
		dlgWindow.setAttributes(wmlp);
		setCanceledOnTouchOutside(true);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mActivity = activity;

		// ダイアログ終了通知設定
		setOnDismissListener(this);
	}

	public void setParams(boolean viewer, int page, int maxpage, boolean reverse,boolean dirtree) {
		mViewer = viewer;
		mPage = page;
		mMaxPage = maxpage;
		mReverse = reverse;
		mDirTree = dirtree;
	}

	public void setShareType(int shareType) {
		// ツールバーを動的に表示する変更に伴いコメントアウトにした
		/*
		mShareType = shareType;
		if (mShare) {
			if (mShareType == DEF.SHARE_SINGLE) {
				mBtnShare.setVisibility(View.VISIBLE);
				mBtnShareLeftPage.setVisibility(View.GONE);
				mBtnShareRightPage.setVisibility(View.GONE);
			} else {
				mBtnShare.setVisibility(View.GONE);
				mBtnShareLeftPage.setVisibility(View.VISIBLE);
				mBtnShareRightPage.setVisibility(View.VISIBLE);
			}
		}
		*/
	}

	@SuppressLint("SuspiciousIndentation")
    protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		int logLevel = Logcat.LOG_LEVEL_WARN;

		// 一度ダイアログを表示すると画面回転時に呼び出される
        Window win = getWindow();
        WindowManager.LayoutParams lpCur = win.getAttributes();
        WindowManager.LayoutParams lpNew = new WindowManager.LayoutParams();
        lpNew.copyFrom(lpCur);
        lpNew.width = WindowManager.LayoutParams.MATCH_PARENT;
        lpNew.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lpNew);

		mSeekPage = (SeekBar) findViewById(R.id.seek_page);
		mSeekPage.setMax(mMaxPage - 1);
		setProgress(mPage, false);
		mSeekPage.setOnSeekBarChangeListener(this);

		// ツールバーを動的に表示する変更に伴いコメントアウトにした
		/*
		mBtnLeftMost = (AppCompatImageButton) this.findViewById(R.id.leftmost);
		mBtnLeft100 = (AppCompatImageButton) this.findViewById(R.id.left100);
		mBtnLeft10  = (AppCompatImageButton) this.findViewById(R.id.left10);
		mBtnLeft1   = (AppCompatImageButton) this.findViewById(R.id.left1);
		mBtnRightMost = (AppCompatImageButton) this.findViewById(R.id.rightmost);
		mBtnRight100 = (AppCompatImageButton) this.findViewById(R.id.right100);
		mBtnRight10  = (AppCompatImageButton) this.findViewById(R.id.right10);
		mBtnRight1   = (AppCompatImageButton) this.findViewById(R.id.right1);
		mBtnPageReset = (AppCompatImageButton) this.findViewById(R.id.page_reset);

		mBtnLeftMost.setOnClickListener(this);
		mBtnLeft100.setOnClickListener(this);
		mBtnLeft10.setOnClickListener(this);
		mBtnLeft1.setOnClickListener(this);
		mBtnRightMost.setOnClickListener(this);
		mBtnRight100.setOnClickListener(this);
		mBtnRight10.setOnClickListener(this);
		mBtnRight1.setOnClickListener(this);
		mBtnPageReset.setOnClickListener(this);

		mBtnBookLeft = (AppCompatImageButton) this.findViewById(R.id.book_left);
		mBtnBookRight = (AppCompatImageButton) this.findViewById(R.id.book_right);
		mBtnBookmarkLeft = (AppCompatImageButton) this.findViewById(R.id.bookmark_left);
		mBtnBookmarkRight = (AppCompatImageButton) this.findViewById(R.id.bookmark_right);
		mBtnThumbSlider = (AppCompatImageButton) this.findViewById(R.id.thumb_slider);
		mBtnDirTree = (AppCompatImageButton) this.findViewById(R.id.directory_tree);
		mBtnTOC = (AppCompatImageButton) this.findViewById(R.id.table_of_contents);
		mBtnFavorite = (AppCompatImageButton) this.findViewById(R.id.favorite);
		mBtnAddFavorite = (AppCompatImageButton) this.findViewById(R.id.add_favorite);
		mBtnSearch = (AppCompatImageButton) this.findViewById(R.id.search);
		mBtnShare = (AppCompatImageButton) this.findViewById(R.id.share);
		mBtnShareLeftPage = (AppCompatImageButton) this.findViewById(R.id.share_left_page);
		mBtnShareRightPage = (AppCompatImageButton) this.findViewById(R.id.share_right_page);
		mBtnRotate = (AppCompatImageButton) this.findViewById(R.id.rotate);
		mBtnRotateImage = (AppCompatImageButton) this.findViewById(R.id.rotate_image);
		mBtnSelectThum = (AppCompatImageButton) this.findViewById(R.id.select_thumb);
		mBtnTrimThumb = (AppCompatImageButton) this.findViewById(R.id.trimming_thumb);
		mBtnControl = (AppCompatImageButton) this.findViewById(R.id.control);
		mBtnMenu = (AppCompatImageButton) this.findViewById(R.id.menu);
		mBtnConfig = (AppCompatImageButton) this.findViewById(R.id.config);
		mBtnEditButton = (AppCompatImageButton) this.findViewById(R.id.edit_button);
		mBtnProfile1 = (AppCompatImageButton) this.findViewById(R.id.profile1);
		mBtnProfile2 = (AppCompatImageButton) this.findViewById(R.id.profile2);
		mBtnProfile3 = (AppCompatImageButton) this.findViewById(R.id.profile3);
		mBtnProfile4 = (AppCompatImageButton) this.findViewById(R.id.profile4);
		mBtnProfile5 = (AppCompatImageButton) this.findViewById(R.id.profile5);
		mBtnProfile6 = (AppCompatImageButton) this.findViewById(R.id.profile6);
		mBtnProfile7 = (AppCompatImageButton) this.findViewById(R.id.profile7);
		mBtnProfile8 = (AppCompatImageButton) this.findViewById(R.id.profile8);
		mBtnProfile9 = (AppCompatImageButton) this.findViewById(R.id.profile9);
		mBtnProfile10 = (AppCompatImageButton) this.findViewById(R.id.profile10);

		mBtnBookLeft.setOnClickListener(this);
		mBtnBookRight.setOnClickListener(this);
		mBtnBookmarkLeft.setOnClickListener(this);
		mBtnBookmarkRight.setOnClickListener(this);
		mBtnThumbSlider.setOnClickListener(this);
		mBtnDirTree.setOnClickListener(this);
		mBtnTOC.setOnClickListener(this);
		mBtnFavorite.setOnClickListener(this);
		mBtnAddFavorite.setOnClickListener(this);
		mBtnSearch.setOnClickListener(this);
		mBtnShare.setOnClickListener(this);
		mBtnShareLeftPage.setOnClickListener(this);
		mBtnShareRightPage.setOnClickListener(this);
		mBtnRotate.setOnClickListener(this);
		mBtnRotateImage.setOnClickListener(this);
		mBtnSelectThum.setOnClickListener(this);
		mBtnTrimThumb.setOnClickListener(this);
		mBtnControl.setOnClickListener(this);
		mBtnMenu.setOnClickListener(this);
		mBtnConfig.setOnClickListener(this);
		mBtnEditButton.setOnClickListener(this);
		mBtnProfile1.setOnClickListener(this);
		mBtnProfile2.setOnClickListener(this);
		mBtnProfile3.setOnClickListener(this);
		mBtnProfile4.setOnClickListener(this);
		mBtnProfile5.setOnClickListener(this);
		mBtnProfile6.setOnClickListener(this);
		mBtnProfile7.setOnClickListener(this);
		mBtnProfile8.setOnClickListener(this);
		mBtnProfile9.setOnClickListener(this);
		mBtnProfile10.setOnClickListener(this);

		boolean[] states = ToolbarEditDialog.loadToolbarState(mActivity);
		Logcat.d(logLevel, "states[]=" + Arrays.toString(states));

		for (int i = 0; i < states.length; ++i) {
			switch (ToolbarEditDialog.COMMAND_ID[i]) {
				case DEF.TOOLBAR_LEFTMOST: {
					if (!states[i]) {
						mBtnLeftMost.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_LEFT100: {
					if (!states[i]) {
						mBtnLeft100.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_LEFT10: {
					if (!states[i]) {
						mBtnLeft10.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_LEFT1: {
					if (!states[i]) {
						mBtnLeft1.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_RIGHT1: {
					if (!states[i]) {
						mBtnRight1.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_RIGHT10: {
					if (!states[i]) {
						mBtnRight10.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_RIGHT100: {
					if (!states[i]) {
						mBtnRight100.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_RIGHTMOST: {
					if (!states[i]) {
						mBtnRightMost.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_PAGE_RESET: {
					if (!states[i]) {
						mBtnPageReset.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_BOOK_LEFT: {
					if (!states[i]) {
						mBtnBookLeft.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_BOOK_RIGHT: {
					if (!states[i]) {
						mBtnBookRight.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_BOOKMARK_LEFT: {
					if (!states[i]) {
						mBtnBookmarkLeft.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_BOOKMARK_RIGHT: {
					if (!states[i]) {
						mBtnBookmarkRight.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_THUMB_SLIDER: {
					if (!states[i] || mViewer == DEF.TEXT_VIEWER) {
						mBtnThumbSlider.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_DIR_TREE: {
					if (!states[i] || mViewer == DEF.TEXT_VIEWER || !mDirTree) {
						mBtnDirTree.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_TOC: {
					if (!states[i] || mViewer == DEF.IMAGE_VIEWER) {
						mBtnTOC.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_FAVORITE: {
					if (!states[i]) {
						mBtnFavorite.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_ADD_FAVORITE: {
					if (!states[i]) {
						mBtnAddFavorite.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_SEARCH: {
					if (!states[i] || mViewer == DEF.IMAGE_VIEWER) {
						mBtnSearch.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_SHARE: {
					mShare = states[i];
					if (!states[i] || mViewer == DEF.TEXT_VIEWER) {
						mBtnShare.setVisibility(View.GONE);
						mBtnShareLeftPage.setVisibility(View.GONE);
						mBtnShareRightPage.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_ROTATE: {
					if (!states[i]) {
						mBtnRotate.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_ROTATE_IMAGE: {
					if (!states[i] || mViewer == DEF.TEXT_VIEWER) {
						mBtnRotateImage.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_SELECT_THUMB: {
					if (!states[i] || mViewer == DEF.TEXT_VIEWER) {
						mBtnSelectThum.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_TRIM_THUMB: {
					if (!states[i] || mViewer == DEF.TEXT_VIEWER) {
						mBtnTrimThumb.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_CONTROL: {
					if (!states[i]) {
						mBtnControl.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_MENU: {
					if (!states[i]) {
						mBtnMenu.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_CONFIG: {
					if (!states[i]) {
						mBtnConfig.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_EDIT_TOOLBAR: {
					if (!states[i]) {
						mBtnEditButton.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_PROFILE1: {
					if (!states[i]) {
						mBtnProfile1.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_PROFILE2: {
					if (!states[i]) {
						mBtnProfile2.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_PROFILE3: {
					if (!states[i]) {
						mBtnProfile3.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_PROFILE4: {
					if (!states[i]) {
						mBtnProfile4.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_PROFILE5: {
					if (!states[i]) {
						mBtnProfile5.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_PROFILE6: {
					if (!states[i]) {
						mBtnProfile6.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_PROFILE7: {
					if (!states[i]) {
						mBtnProfile7.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_PROFILE8: {
					if (!states[i]) {
						mBtnProfile8.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_PROFILE9: {
					if (!states[i]) {
						mBtnProfile9.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_PROFILE10: {
					if (!states[i]) {
						mBtnProfile10.setVisibility(View.GONE);
					}
					break;
				}
			}
		}
		*/
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		// ダイアログのサイズを設定する
		Rect size = new Rect();
		mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(size);
		int cx = size.width();
		getWindow().setLayout(cx, ViewGroup.LayoutParams.WRAP_CONTENT);

		Resources res = mActivity.getResources();
		float ratio = ToolbarEditDialog.getToolbarRatio(mActivity);

		// ツールバーを動的に表示する変更に伴いコメントアウトにした
		/*
		// ボタンのサイズを変更する
		zoom(res, mBtnLeftMost, ratio);
		zoom(res, mBtnLeft100, ratio);
		zoom(res, mBtnLeft10, ratio);
		zoom(res, mBtnLeft1, ratio);
		zoom(res, mBtnRightMost, ratio);
		zoom(res, mBtnRight100, ratio);
		zoom(res, mBtnRight10, ratio);
		zoom(res, mBtnRight1, ratio);
		zoom(res, mBtnPageReset, ratio);

		zoom(res, mBtnBookLeft, ratio);
		zoom(res, mBtnBookRight, ratio);
		zoom(res, mBtnBookmarkLeft, ratio);
		zoom(res, mBtnBookmarkRight, ratio);
		zoom(res, mBtnThumbSlider, ratio);
		zoom(res, mBtnDirTree, ratio);
		zoom(res, mBtnTOC, ratio);
		zoom(res, mBtnFavorite, ratio);
		zoom(res, mBtnAddFavorite, ratio);
		zoom(res, mBtnSearch, ratio);
		zoom(res, mBtnShare, ratio);
		zoom(res, mBtnShareLeftPage, ratio);
		zoom(res, mBtnShareRightPage, ratio);
		zoom(res, mBtnRotate, ratio);
		zoom(res, mBtnRotateImage, ratio);
		zoom(res, mBtnSelectThum, ratio);
		zoom(res, mBtnTrimThumb, ratio);
		zoom(res, mBtnControl, ratio);
		zoom(res, mBtnMenu, ratio);
		zoom(res, mBtnConfig, ratio);
		zoom(res, mBtnEditButton, ratio);
		zoom(res, mBtnProfile1, ratio);
		zoom(res, mBtnProfile2, ratio);
		zoom(res, mBtnProfile3, ratio);
		zoom(res, mBtnProfile4, ratio);
		zoom(res, mBtnProfile5, ratio);
		zoom(res, mBtnProfile6, ratio);
		zoom(res, mBtnProfile7, ratio);
		zoom(res, mBtnProfile8, ratio);
		zoom(res, mBtnProfile9, ratio);
		zoom(res, mBtnProfile10, ratio);
		*/
	}

	private void zoom(Resources resources, ImageButton imageButton, float ratio){
		// ツールバーを動的に表示する変更に伴いコメントアウトにした
		/*
		// ボタンのサイズを変更する
		imageButton.setImageDrawable(ImageAccess.zoom(resources, imageButton.getDrawable(), ratio));
		imageButton.setPadding((int)(imageButton.getPaddingLeft() * ratio), (int)(imageButton.getPaddingTop() * ratio), (int)(mBtnLeftMost.getPaddingRight() * ratio), (int)(mBtnLeftMost.getPaddingBottom() * ratio));
		imageButton.getLayoutParams().height = (int)(imageButton.getHeight() * ratio);
		imageButton.getLayoutParams().width = (int)(imageButton.getWidth() * ratio);
		imageButton.requestLayout();
		*/
	}

	public void setPageSelectListear(PageSelectListener listener) {
		mListener = listener;
	}

	// ツールバーのタッチイベントの実行
	public static void SetListner(int index) {
		switch (index) {
			case DEF.TOOLBAR_EVENT_BOOK_LEFT:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_BOOK_LEFT);
				break;
			case DEF.TOOLBAR_EVENT_BOOK_RIGHT:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_BOOK_RIGHT);
				break;
			case DEF.TOOLBAR_EVENT_BOOKMARK_LEFT:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_BOOKMARK_LEFT);
				break;
			case DEF.TOOLBAR_EVENT_BOOKMARK_RIGHT:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_BOOKMARK_RIGHT);
				break;
			case DEF.TOOLBAR_EVENT_THUMB_SLIDER:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_THUMB_SLIDER);
				break;
			case DEF.TOOLBAR_EVENT_DIR_TREE:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_DIR_TREE);
				break;
			case DEF.TOOLBAR_EVENT_TOC:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_TOC);
				break;
			case DEF.TOOLBAR_EVENT_FAVORITE:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_FAVORITE);
				break;
			case DEF.TOOLBAR_EVENT_ADD_FAVORITE:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_ADD_FAVORITE);
				break;
			case DEF.TOOLBAR_EVENT_SEARCH:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SEARCH);
				break;
			case DEF.TOOLBAR_EVENT_SHARE:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SHARE);
				break;
			case DEF.TOOLBAR_EVENT_ROTATE:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_ROTATE);
				break;
			case DEF.TOOLBAR_EVENT_ROTATE_IMAGE:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_ROTATE_IMAGE);
				break;
			case DEF.TOOLBAR_EVENT_SELECT_THUMB:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SELECT_THUMB);
				break;
			case DEF.TOOLBAR_EVENT_TRIM_THUMB:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_TRIM_THUMB);
				break;
			case DEF.TOOLBAR_EVENT_CONTROL:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_CONTROL);
				break;
			case DEF.TOOLBAR_EVENT_MENU:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_MENU);
				break;
			case DEF.TOOLBAR_EVENT_CONFIG:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_CONFIG);
				break;
			case DEF.TOOLBAR_EVENT_EDIT_TOOLBAR:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_EDIT_TOOLBAR);
				break;
			case DEF.TOOLBAR_EVENT_PROFILE1:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE1);
				break;
			case DEF.TOOLBAR_EVENT_PROFILE2:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE2);
				break;
			case DEF.TOOLBAR_EVENT_PROFILE3:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE3);
				break;
			case DEF.TOOLBAR_EVENT_PROFILE4:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE4);
				break;
			case DEF.TOOLBAR_EVENT_PROFILE5:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE5);
				break;
			case DEF.TOOLBAR_EVENT_PROFILE6:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE6);
				break;
			case DEF.TOOLBAR_EVENT_PROFILE7:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE7);
				break;
			case DEF.TOOLBAR_EVENT_PROFILE8:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE8);
				break;
			case DEF.TOOLBAR_EVENT_PROFILE9:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE9);
				break;
			case DEF.TOOLBAR_EVENT_PROFILE10:
				mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE10);
				break;
		}
		if (index > DEF.TOOLBAR_EVENT_PAGE_RESET) {
			return;
		}
		int page = calcProgress(mSeekPage.getProgress());
		switch (index) {
			case DEF.TOOLBAR_EVENT_LEFTMOST:
				if (mReverse) {
					page = mMaxPage - 1;
				} else {
					page = 0;
				}
				break;
			case DEF.TOOLBAR_EVENT_LEFT100:
				if (mReverse) {
					page += 100;
				} else {
					page -= 100;
				}
				break;
			case DEF.TOOLBAR_EVENT_LEFT10:
				if (mReverse) {
					page += 10;
				} else {
					page -= 10;
				}
				break;
			case DEF.TOOLBAR_EVENT_LEFT1:
				if (mReverse) {
					page += 1;
				} else {
					page -= 1;
				}
				break;
			case DEF.TOOLBAR_EVENT_RIGHT1:
				if (mReverse) {
					page -= 1;
				} else {
					page += 1;
				}
				break;
			case DEF.TOOLBAR_EVENT_RIGHT10:
				if (mReverse) {
					page -= 10;
				} else {
					page += 10;
				}
				break;
			case DEF.TOOLBAR_EVENT_RIGHT100:
				if (mReverse) {
					page -= 100;
				} else {
					page += 100;
				}
				break;
			case DEF.TOOLBAR_EVENT_RIGHTMOST:
				if (mReverse) {
					page = 0;
				} else {
					page = mMaxPage - 1;
				}
				break;
			case DEF.TOOLBAR_EVENT_PAGE_RESET:
				// ページ選択をリセット
				page = mPage;
				break;
		}
		// ページ選択の場合
		if (page < 0) {
			page = 0;
		} else if (page >= mMaxPage) {
			page = mMaxPage - 1;
		}

		// 設定と通知
		if (mAutoApply) {
			mListener.onSelectPage(page);
		}
		setProgress(page, false);
		// シークバーを移動させてもonProgressChangedへ飛ばないので直接操作する
		mThumView.setPosition(page);
	}

	@Override
	public void onClick(View v) {
		// ツールバーを動的に表示する変更に伴いコメントアウトにした
		/*
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// ボタンクリック

		if (mBtnBookLeft == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_BOOK_LEFT);
		}
		else if (mBtnBookRight == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_BOOK_RIGHT);
		}
		else if (mBtnBookmarkLeft == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_BOOKMARK_LEFT);
		}
		else if (mBtnBookmarkRight == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_BOOKMARK_RIGHT);
		}
		else if (mBtnThumbSlider == v) {
			dismiss();
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_THUMB_SLIDER);
		}
		else if (mBtnDirTree == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_DIR_TREE);
		}
		else if (mBtnTOC == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_TOC);
		}
		else if (mBtnFavorite == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_FAVORITE);
		}
		else if (mBtnAddFavorite == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_ADD_FAVORITE);
		}
		else if (mBtnSearch == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SEARCH);
		}
		else if (mBtnShare == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SHARE);
		}
		else if (mBtnShareLeftPage == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SHARE_LEFT_PAGE);
		}
		else if (mBtnShareRightPage == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SHARE_RIGHT_PAGE);
		}
		else if (mBtnRotate == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_ROTATE);
		}
		else if (mBtnRotateImage == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_ROTATE_IMAGE);
		}
		else if (mBtnSelectThum == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SELECT_THUMB);
		}
		else if (mBtnTrimThumb == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_TRIM_THUMB);
		}
		else if (mBtnControl == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_CONTROL);
		}
		else if (mBtnMenu == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_MENU);
		}
		else if (mBtnConfig == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_CONFIG);
		}
		else if (mBtnEditButton == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_EDIT_TOOLBAR);
		}
		else if (mBtnProfile1 == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE1);
		}
		else if (mBtnProfile2 == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE2);
		}
		else if (mBtnProfile3 == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE3);
		}
		else if (mBtnProfile4 == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE4);
		}
		else if (mBtnProfile5 == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE5);
		}
		else if (mBtnProfile6 == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE6);
		}
		else if (mBtnProfile7 == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE7);
		}
		else if (mBtnProfile8 == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE8);
		}
		else if (mBtnProfile9 == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE9);
		}
		else if (mBtnProfile10 == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_PROFILE10);
		}

		else {
			// ページ選択の場合
			int page = calcProgress(mSeekPage.getProgress());
			Logcat.d(logLevel, "現在ページ=" + page + ", mReverse=" + mReverse);

			if (mBtnLeftMost == v) {
				Logcat.d(logLevel, "v=mBtnLeftMost");
				if (mReverse) {
					page = mMaxPage - 1;
				} else {
					page = 0;
				}
			} else if (mBtnLeft100 == v) {
				Logcat.d(logLevel, "v=mBtnLeft100");
				if (mReverse) {
					page += 100;
				} else {
					page -= 100;
				}
			} else if (mBtnLeft10 == v) {
				Logcat.d(logLevel, "v=mBtnLeft10");
				if (mReverse) {
					page += 10;
				} else {
					page -= 10;
				}
			} else if (mBtnLeft1 == v) {
				Logcat.d(logLevel, "v=mBtnLeft1");
				if (mReverse) {
					page += 1;
				} else {
					page -= 1;
				}
			} else if (mBtnRight1 == v) {
				Logcat.d(logLevel, "v=mBtnRight1");
				if (mReverse) {
					page -= 1;
				} else {
					page += 1;
				}
			} else if (mBtnRight10 == v) {
				Logcat.d(logLevel, "v=mBtnRight10");
				if (mReverse) {
					page -= 10;
				} else {
					page += 10;
				}
			} else if (mBtnRight100 == v) {
				Logcat.d(logLevel, "v=mBtnRight100");
				if (mReverse) {
					page -= 100;
				} else {
					page += 100;
				}
			} else if (mBtnRightMost == v) {
				Logcat.d(logLevel, "v=mBtnRightMost");
				if (mReverse) {
					page = 0;
				} else {
					page = mMaxPage - 1;
				}
			} else if (mBtnPageReset == v) {
				Logcat.d(logLevel, "v=mBtnPageReset");
				// ページ選択をリセット
				page = mPage;
			}

			Logcat.d(logLevel, "変更後ページ=" + page);

			if (page < 0) {
				page = 0;
			} else if (page >= mMaxPage) {
				page = mMaxPage - 1;
			}

			// 設定と通知
			if (mAutoApply) {
				mListener.onSelectPage(page);
			}
			setProgress(page, false);
		}
		*/
	}

	protected static void setProgress(int pos, boolean fromThumb) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "pos=" + pos);
		//if (debug) {DEF.StackTrace("ToolbarDialog", "setProgress:");}
		int convpos;

		if (!mReverse) {
			convpos = pos;
		}
		else {
			convpos = mSeekPage.getMax() - pos;
		}
		Logcat.d(logLevel, "convpos=" + convpos);
		mSeekPage.setProgress(convpos);
	}

	protected static int calcProgress(int pos) {
		int convpos;

		if (!mReverse) {
			convpos = pos;
		}
		else {
			convpos = mSeekPage.getMax() - pos;
		}
		return convpos;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int page, boolean fromUser) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "page=" + page + ", fromUser=" + fromUser);
		// 変更
		if (fromUser) {
			int cnvpage = calcProgress(page);
			mListener.onSelectPage(cnvpage);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// 開始

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// 終了
		if (mAutoApply) {
			int cnvpage = calcProgress(seekBar.getProgress());
			mListener.onSelectPage(cnvpage);
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mListener.onSelectPageSelectDialog(DEF.TOOLBAR_DISMISS);
	}

}