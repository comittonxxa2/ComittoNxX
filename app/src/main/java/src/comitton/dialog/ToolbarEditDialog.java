package src.comitton.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.widget.TextViewCompat;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.StyleRes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.common.Logcat;

@SuppressLint("NewApi")
public class ToolbarEditDialog extends ImmersiveDialog implements OnClickListener, SeekBar.OnSeekBarChangeListener {

	private TextView mTitleText;
	private Button mBtnOk;
	private Button mBtnCancel;
	private ListView mListView;
	private LinearLayout mFooter;

	private String mTitle;
	private boolean[] mStates;
	private String[] mItems;

	private String mDefaultStr;
	private int mToolbarSize;
	private TextView mTxtSize;
	private String mSizeStr;
	private Mlist[] mlists;
	private int[] mIndex;
	private SeekBar mSkbBkSize;

	private ItemArrayAdapter mItemArrayAdapter;
	private String[] mProfileWord;

	static final int[] COMMAND_DRAWABLE =
			{
					R.drawable.arrow_left_to_line,
					R.drawable.arrow_left_100,
					R.drawable.arrow_left_10,
					R.drawable.arrow_left,
					R.drawable.arrow_right,
					R.drawable.arrow_right_10,
					R.drawable.arrow_right_100,
					R.drawable.arrow_right_to_line,
					R.drawable.reset,
					R.drawable.book_arrow_left,
					R.drawable.book_arrow_right,
					R.drawable.book_arrow_left_bookmark,
					R.drawable.book_arrow_right_bookmark,
					R.drawable.thumb_slider,
					R.drawable.directory_tree,
					R.drawable.table_of_contents,
					R.drawable.list_favorite,
					R.drawable.add_favorite,
					R.drawable.toolbar_search,
					R.drawable.share,
					R.drawable.rotate,
					R.drawable.rotate_image,
					R.drawable.select_thumb,
					R.drawable.trimming_thumb,
					R.drawable.control,
					R.drawable.navi_menu,
					R.drawable.config,
					R.drawable.pen,
					R.drawable.profile1,
					R.drawable.profile2,
					R.drawable.profile3,
					R.drawable.profile4,
					R.drawable.profile5,
					R.drawable.profile6,
					R.drawable.profile7,
					R.drawable.profile8,
					R.drawable.profile9,
					R.drawable.profile10,
			};

	public static final int[] COMMAND_ID =
		{
			DEF.TOOLBAR_LEFTMOST,
			DEF.TOOLBAR_LEFT100,
			DEF.TOOLBAR_LEFT10,
			DEF.TOOLBAR_LEFT1,
			DEF.TOOLBAR_RIGHT1,
			DEF.TOOLBAR_RIGHT10,
			DEF.TOOLBAR_RIGHT100,
			DEF.TOOLBAR_RIGHTMOST,
			DEF.TOOLBAR_PAGE_RESET,
			DEF.TOOLBAR_BOOK_LEFT,
			DEF.TOOLBAR_BOOK_RIGHT,
			DEF.TOOLBAR_BOOKMARK_LEFT,
			DEF.TOOLBAR_BOOKMARK_RIGHT,
			DEF.TOOLBAR_THUMB_SLIDER,
			DEF.TOOLBAR_DIR_TREE,
			DEF.TOOLBAR_TOC,
			DEF.TOOLBAR_FAVORITE,
			DEF.TOOLBAR_ADD_FAVORITE,
			DEF.TOOLBAR_SEARCH,
			DEF.TOOLBAR_SHARE,
			DEF.TOOLBAR_ROTATE,
			DEF.TOOLBAR_ROTATE_IMAGE,
			DEF.TOOLBAR_SELECT_THUMB,
			DEF.TOOLBAR_TRIM_THUMB,
			DEF.TOOLBAR_CONTROL,
			DEF.TOOLBAR_MENU,
			DEF.TOOLBAR_CONFIG,
			DEF.TOOLBAR_EDIT_TOOLBAR,
			DEF.TOOLBAR_PROFILE1,
			DEF.TOOLBAR_PROFILE2,
			DEF.TOOLBAR_PROFILE3,
			DEF.TOOLBAR_PROFILE4,
			DEF.TOOLBAR_PROFILE5,
			DEF.TOOLBAR_PROFILE6,
			DEF.TOOLBAR_PROFILE7,
			DEF.TOOLBAR_PROFILE8,
			DEF.TOOLBAR_PROFILE9,
			DEF.TOOLBAR_PROFILE10,
		};

	private static final boolean[] DEFAULT_VALUES =
		{
			true,		// 一番左のページ
			false,		// 左へ100ページ
			false,			// 左へ10ページ
			true,			// 左へ1ページ
			true,			// 右へ1ページ
			false,		// 右へ10ページ
			false,		// 右へ100ページ
			true,		// 一番右のページ
			true,		// ページ選択をリセット
			false,		// 前のファイル(最終ページ)/次のファイル(先頭ページ)
			false,		// 次のファイル(先頭ページ)/前のファイル(最終ページ)
			true,	// 前(次)のファイル(しおり位置)
			true,	// 次(前)のファイル(しおり位置)
			false,	// サムネイル/スライダー切り替え(イメージビュワーのみ)
			false,		// サブディレクトリ選択(イメージビュワーのみ)
			false,			// 見出し選択(テキストビュワーのみ)
			false,		// ブックマーク選択
			false,	// ブックマーク追加
			false,			// 検索文字列設定(テキストビュワーのみ)
			false,			// 共有(イメージビュワーのみ)
			false,			// 画面方向切り替え(イメージビュワーのみ)
			false,	// 画像の回転(イメージビュワーのみ)
			false,		// サムネイルに設定(イメージビュワーのみ)
			false,		// 範囲選択してサムネイルに設定(イメージビュワーのみ)
			false,		// 画像/テキスト表示設定
			true,			// メニューを開く
			false,			// 設定画面を開く
			true,			// ツールバーを編集
			false,		// プロファイル1
			false,		// プロファイル2
			false,		// プロファイル3
			false,		// プロファイル4
			false,		// プロファイル5
			false,		// プロファイル6
			false,		// プロファイル7
			false,		// プロファイル8
			false,		// プロファイル9
			false,		// プロファイル10
		};

	private static final int[] COMMAND_RES =
			{
					R.string.ToolbarLeftmost,		// 一番左のページ
					R.string.ToolbarLeft100,		// 左へ100ページ
					R.string.ToolbarLeft10,			// 左へ10ページ
					R.string.ToolbarLeft1,			// 左へ1ページ
					R.string.ToolbarRight1,			// 右へ1ページ
					R.string.ToolbarRight10,		// 右へ10ページ
					R.string.ToolbarRight100,		// 右へ100ページ
					R.string.ToolbarRightMost,		// 一番右のページ
					R.string.ToolbarPageReset,		// ページ選択をリセット
					R.string.ToolbarBookLeft,		// 前のファイル(最終ページ)/次のファイル(先頭ページ)
					R.string.ToolbarBookRight,		// 次のファイル(先頭ページ)/前のファイル(最終ページ)
					R.string.ToolbarBookmarkLeft,	// 前(次)のファイル(しおり位置)
					R.string.ToolbarBookmarkRight,	// 次(前)のファイル(しおり位置)
					R.string.ToolbarThumbSlider,	// サムネイル/スライダー切り替え(イメージビュワーのみ)
					R.string.ToolbarDirTree,		// サブディレクトリ選択(イメージビュワーのみ)
					R.string.ToolbarTOC,			// 見出し選択(テキストビュワーのみ)
					R.string.ToolbarFavorite,		// ブックマーク選択
					R.string.ToolbarAddFavorite,	// ブックマーク追加
					R.string.ToolbarSearch,			// 検索文字列設定(テキストビュワーのみ)
					R.string.ToolbarShare,			// 共有(イメージビュワーのみ)
					R.string.ToolbarRotate,			// 画面方向切り替え(イメージビュワーのみ)
					R.string.ToolbarRotateImage,	// 画像の回転(イメージビュワーのみ)
					R.string.ToolbarSelectThum,		// サムネイルに設定(イメージビュワーのみ)
					R.string.ToolbarTrimThumb,		// 範囲選択してサムネイルに設定(イメージビュワーのみ)
					R.string.ToolbarControl,		// 画像/テキスト表示設定
					R.string.ToolbarMenu,			// メニューを開く
					R.string.ToolbarConfig,			// 設定画面を開く
					R.string.ToolbarEditToolbar,	// ツールバーを編集
					R.string.ToolbarProfile1,		// プロファイル1
					R.string.ToolbarProfile2,		// プロファイル2
					R.string.ToolbarProfile3,		// プロファイル3
					R.string.ToolbarProfile4,		// プロファイル4
					R.string.ToolbarProfile5,		// プロファイル5
					R.string.ToolbarProfile6,		// プロファイル6
					R.string.ToolbarProfile7,		// プロファイル7
					R.string.ToolbarProfile8,		// プロファイル8
					R.string.ToolbarProfile9,		// プロファイル9
					R.string.ToolbarProfile10,		// プロファイル10
			};

	private class Mlist {
		String mitems;
		int mCommndDrawables;
		int mCommandRes;
		boolean mChecked;
		int mindex;
		public void setItems(String items, int CommndDrawables, int CommandRes) {
			mitems = items;
			mCommndDrawables = CommndDrawables;
			mCommandRes = CommandRes;
		}
		public void setChecked(boolean checked) {
			mChecked = checked;
		}
		public void setIndex(int Index) {
			mindex = Index;
		}
		public String getItems() {
			return mitems;
		}
		public int getCommndDrawables() {
			return mCommndDrawables;
		}
		public boolean getChecked() {
			return mChecked;
		}
		public int getIndex() {
			return mindex;
		}
	}

	public ToolbarEditDialog(AppCompatActivity activity, @StyleRes int themeResId, int cx, int cy) {
		super(activity, themeResId);
		setCanceledOnTouchOutside(true);

		mTitle = activity.getString(R.string.ToolbarEditTitle);
		mStates = loadToolbarState(mActivity);

		SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(activity);
		mProfileWord = new String[10];

		// 初期値を読み出す
		mProfileWord[0] = sharedPreference.getString(DEF.KEY_PROFILE_WORD_01, "");
		mProfileWord[1] = sharedPreference.getString(DEF.KEY_PROFILE_WORD_02, "");
		mProfileWord[2] = sharedPreference.getString(DEF.KEY_PROFILE_WORD_03, "");
		mProfileWord[3] = sharedPreference.getString(DEF.KEY_PROFILE_WORD_04, "");
		mProfileWord[4] = sharedPreference.getString(DEF.KEY_PROFILE_WORD_05, "");
		mProfileWord[5] = sharedPreference.getString(DEF.KEY_PROFILE_WORD_06, "");
		mProfileWord[6] = sharedPreference.getString(DEF.KEY_PROFILE_WORD_07, "");
		mProfileWord[7] = sharedPreference.getString(DEF.KEY_PROFILE_WORD_08, "");
		mProfileWord[8] = sharedPreference.getString(DEF.KEY_PROFILE_WORD_09, "");
		mProfileWord[9] = sharedPreference.getString(DEF.KEY_PROFILE_WORD_10, "");

		String[] items = null;
		Mlist[] lists;
		items = new String[COMMAND_RES.length];
		lists = new Mlist[COMMAND_RES.length];
		mIndex = new int[COMMAND_RES.length];
		for (int i = 0; i < COMMAND_RES.length; i++) {
			lists[i] = new Mlist();
			mIndex[i] = sharedPreference.getInt(DEF.KEY_PAGE_SELECT_TOOLBAR_INDEX + COMMAND_ID[i], i);
		}
		for (int i = 0; i < COMMAND_RES.length; i++) {
			if (COMMAND_ID[i] >= DEF.TOOLBAR_PROFILE1 && COMMAND_ID[i] <= DEF.TOOLBAR_PROFILE10) {
				// プロファイル
				if (mProfileWord[COMMAND_ID[i] - DEF.TOOLBAR_PROFILE1].equals("")) {
					// 中身が未定義なら
					items[i] = activity.getResources().getString(COMMAND_RES[mIndex[i]]);
				}
				else {
					// 後半に中身を追加
					items[i] = activity.getResources().getString(COMMAND_RES[mIndex[i]]) + " : " + mProfileWord[COMMAND_ID[i] - DEF.TOOLBAR_PROFILE1];
				}
			}
			else {
				items[i] = activity.getResources().getString(COMMAND_RES[mIndex[i]]);
			}
			lists[i].setItems(items[i], COMMAND_DRAWABLE[mIndex[i]], COMMAND_RES[mIndex[i]]);
			lists[i].setChecked(mStates[mIndex[i]]);
			lists[i].setIndex(mIndex[i]);
		}
		mItems = items;
		mlists = lists;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.checkdialog);

		mTitleText = (TextView)this.findViewById(R.id.text_title);
		mBtnOk  = (Button)this.findViewById(R.id.btn_ok);
		mBtnCancel  = (Button)this.findViewById(R.id.btn_cancel);
		mListView = (ListView)this.findViewById(R.id.listview);
		mFooter = (LinearLayout)this.findViewById(R.id.footer);

		LayoutInflater inflater = LayoutInflater.from(mActivity);

		Resources res = mActivity.getResources();
		mDefaultStr = res.getString(R.string.auto);
		SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(mActivity);
		mToolbarSize = sharedPreference.getInt(DEF.KEY_TOOLBAR_SIZE, DEF.DEFAULT_TOOLBAR_SIZE);
		mFooter.addView(inflater.inflate(R.layout.toolbar_size, null, false), 0);
		mTxtSize = mFooter.findViewById(R.id.label_toolbar_size);
		mSizeStr  = mTxtSize.getText().toString();
		mTxtSize.setText(mSizeStr.replaceAll("%", getToolbarSize(mToolbarSize)));
		mSkbBkSize = mFooter.findViewById(R.id.seek_toolbar_size);
		mSkbBkSize.setMax(DEF.MAX_TOOLBAR_SIZE);
		mSkbBkSize.setProgress(mToolbarSize);
		mSkbBkSize.setOnSeekBarChangeListener(this);

		mTitleText.setText(mTitle);
		// リストの設定
		mListView.setScrollingCacheEnabled(false);
		mItemArrayAdapter = new ItemArrayAdapter(mActivity, -1, mlists);
		mListView.setAdapter(mItemArrayAdapter);

		// デフォルトはしおりを記録する
		mBtnOk.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// スクロールビューの最大サイズを設定する
		// 最大サイズ以下ならそのまま表示する
		int maxheight = mHeight - mTitleText.getHeight() - mFooter.getHeight();
		mListView.getLayoutParams().width = mWidth;
		mListView.getLayoutParams().height = Math.min(mListView.getHeight(), maxheight);
		mListView.requestLayout();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_MENU:
					dismiss();
					break;
			}
		}
		// 自動生成されたメソッド・スタブ
		return super.dispatchKeyEvent(event);
	}

	public static int[] loadToolbarIndex(Context context) {
		int[] states;
		states = new int[COMMAND_ID.length];
		try {
			SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(context);

			int count = 0;
			for (int i = 0; i < states.length; i++) {
				try {
					states[i] = sharedPreference.getInt(DEF.KEY_PAGE_SELECT_TOOLBAR_INDEX + COMMAND_ID[i], i);
				}
				catch (Exception e) {
				}
			}
		}
		catch (Exception e) {
		}
		return states;
	}

	// 設定を読み込み
	public static boolean[] loadToolbarState(Context context) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		boolean[] states = null;
		try {
			Resources res = context.getResources();
			SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(context);

			Logcat.d(logLevel, "保存された設定を取得します.");
			states = new boolean[COMMAND_ID.length];
			int count = 0;
			for (int i = 0; i < states.length; i++) {
				try {
					states[i] = sharedPreference.getBoolean(DEF.KEY_PAGE_SELECT_TOOLBAR + COMMAND_ID[i], DEFAULT_VALUES[i]);
					if (states[i]) {
						// 表示する個数
						count++;
					}
				}
				catch (Exception e) {
					Logcat.e(logLevel, "ループ1でエラーが発生しました.", e);
					Logcat.e(logLevel, "COMMAND_ID[" + i + "]=" + COMMAND_ID[i]);
				}
			}

			Logcat.d(logLevel, "表示するコマンドを設定します.");
			int[] commandId = new int[count];
			String[] commandStr = new String[count];
			count = 0;
			for (int i = 0; i < states.length; i++) {
				try {
					if (states[i]) {
						// 表示するコマンドを設定
						commandId[count] = COMMAND_ID[i];
						commandStr[count] = res.getString(COMMAND_RES[i]).replaceAll("\\(%\\)", "");
						count++;
					}
				}
				catch (Exception e) {
					Logcat.e(logLevel, "ループ2でエラーが発生しました.", e);
					Logcat.e(logLevel, "COMMAND_ID[" + i + "]=" + COMMAND_ID[i]);
				}
			}
			Logcat.d(logLevel, " 終了します.");
		}
		catch (Exception e) {
			Logcat.e(logLevel, "エラーが発生しました.", e);
		}
		Logcat.d(logLevel, "終了します.");
		return states;
	}

	// 設定を保存
	private void saveToolbarState(Context context, boolean[] states) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. states.length=" + states.length);

		SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor ed = sharedPreference.edit();
		for (int i = 0 ; i < states.length ; i ++) {
			try {
				ed.putBoolean(DEF.KEY_PAGE_SELECT_TOOLBAR + COMMAND_ID[i], states[i]);
				ed.putInt(DEF.KEY_PAGE_SELECT_TOOLBAR_INDEX + COMMAND_ID[i], mlists[i].getIndex());
			}
			catch (Exception e) {
				Logcat.e(logLevel, "エラーが発生しました.", e);
			}
		}
		ed.putInt(DEF.KEY_TOOLBAR_SIZE, mSkbBkSize.getProgress());
		ed.apply();
		Logcat.d(logLevel, "終了します.");
	}

	public static float getToolbarRatio(Context context) {
		SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
		return 0.25f * (sharedPreference.getInt(DEF.KEY_TOOLBAR_SIZE, DEF.DEFAULT_TOOLBAR_SIZE) + 2);
	}

	private String getToolbarSize(int progress) {
		String str;
		if (progress == DEF.DEFAULT_TOOLBAR_SIZE || progress >= DEF.MAX_TOOLBAR_SIZE) {
			str = mDefaultStr;
		}
		else {
			str = String.valueOf((progress + 2) * 25) + "%";
		}
		return str;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// 変更通知
		if (seekBar == mSkbBkSize) {
			String str = getToolbarSize(progress);
			mTxtSize.setText(mSizeStr.replaceAll("%", str));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	// dpをピクセルへ変換
	private static int dpToPx(Context context, int dpValue) {
		float density = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * density + 0.5f);
	}

	public class ItemArrayAdapter extends ArrayAdapter<Mlist>
	{
		List<HashMap<String, Object>> mMap;
		private Mlist[] mlists;

		// コンストラクタ
		public ItemArrayAdapter(Context context, int resId, Mlist[] lists)
		{
			super(context, resId, lists);
			mlists = lists;
		}

		// 一要素のビューの生成
		@Override
		public View getView(int index, View view, ViewGroup parent) {
			int logLevel = Logcat.LOG_LEVEL_WARN;
			Logcat.d(logLevel, "開始します. index=" + index);

			// レイアウトの生成
			CheckBox checkbox;
			ImageView imageview;
			TextView textview;
			AppCompatButton  button1;
			AppCompatButton  button2;
			Drawable drawable;

			if(view == null) {
				int marginW = (int)(4 * mScale);
				int marginH = (int)(0 * mScale);
				Context context = getContext();
				int marginInDp = 5;
				int marginInPx = dpToPx(context, marginInDp);
				int mButtonDp = 48;
				int mButtonPx = dpToPx(context, mButtonDp);
				// チェックボックスのレイアウトの色を持ってくる
				int colorInt = context.getColor(R.color.green2);
				// レイアウト
				LinearLayout layout = new LinearLayout(context);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				layout.setBackgroundColor(0);
				view = layout;

				LinearLayout inLayout = new LinearLayout(context);
				inLayout.setOrientation(LinearLayout.HORIZONTAL);
				inLayout.setBackgroundColor(0);

				checkbox = new CheckBox(context);
				checkbox.setId(0);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				layoutParams.gravity= Gravity.LEFT | Gravity.CENTER_VERTICAL;
				checkbox.setLayoutParams(layoutParams);
				inLayout.addView(checkbox);

				imageview = new ImageView(context);
				imageview.setId(1);
				int sp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 32, context.getResources().getDisplayMetrics());
				layoutParams = new LinearLayout.LayoutParams(sp, sp);
				layoutParams.gravity=Gravity.CENTER;
				layoutParams.setMargins(0, marginW, marginW, marginW);
				imageview.setLayoutParams(layoutParams);
				imageview.setScaleType(ImageView.ScaleType.CENTER);
				inLayout.addView(imageview);

				textview = new TextView(context);
				textview.setId(2);
				layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
				layoutParams.gravity= Gravity.LEFT | Gravity.CENTER_VERTICAL;
				textview.setLayoutParams(layoutParams);
				textview.setPadding(marginW, marginW, 0, marginW);
				inLayout.addView(textview);
				// 項目移動のボタンを追加
				button1 = new AppCompatButton(context);
				button1.setId(3);
				layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
				layoutParams.gravity =  Gravity.CENTER;
				layoutParams.width = mButtonPx;
				layoutParams.height = mButtonPx;
	            button1.setPadding(marginInPx, marginInPx, marginInPx, marginInPx);
				button1.setTextColor(colorInt);
				button1.setLayoutParams(layoutParams);
				TextViewCompat.setAutoSizeTextTypeWithDefaults(button1, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
				inLayout.addView(button1);
				// 項目移動のボタンを追加
				button2 = new AppCompatButton(context);
				button2.setId(4);
				layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
				layoutParams.gravity = Gravity.CENTER;
				layoutParams.width = mButtonPx;
				layoutParams.height = mButtonPx;
	            button2.setPadding(marginInPx, marginInPx, marginInPx, marginInPx);
				button2.setTextColor(colorInt);
				button2.setLayoutParams(layoutParams);
				TextViewCompat.setAutoSizeTextTypeWithDefaults(button2, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
				inLayout.addView(button2);

				layout.addView(inLayout);

				checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton view, boolean state) {
						Integer index = (Integer)view.getTag();
						if (index != null) {
    						if (0 <= index && index < mStates.length) {
    							mlists[index].setChecked(state);
    						}
						}
					}
				});
			}
			else {
				Logcat.d(logLevel, "設定済みのビューを呼び出します. index=" + index);
				checkbox = (CheckBox)view.findViewById(0);
				imageview = (ImageView)view.findViewById(1);
				textview = (TextView)view.findViewById(2);
				button1 = (AppCompatButton)view.findViewById(3);
				button2 = (AppCompatButton)view.findViewById(4);
			}

			// 値の指定
			checkbox.setTag(index);
			checkbox.setChecked(mlists[index].getChecked());
			button1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					List<Mlist> itemsList = Arrays.asList(mlists);
					if (index > 0) {
						// 項目の上下を入れ替える
						Collections.swap(itemsList, index, index - 1);
						// リストを更新
						notifyDataSetChanged();
					}
				}
			});
			button2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					List<Mlist> itemsList = Arrays.asList(mlists);
					if (index < COMMAND_RES.length - 1) {
						// 項目の上下を入れ替える
						Collections.swap(itemsList, index, index + 1);
						// リストを更新
						notifyDataSetChanged();
					}
				}
			});

			if (imageview != null) {
				if (0 <= index && index < COMMAND_DRAWABLE.length) {
					Logcat.d(logLevel, "アイコンをセットします. index=" + index);
					drawable = mActivity.getDrawable(mlists[index].getCommndDrawables());
					drawable.setTint(mActivity.getResources().getColor(R.color.white1));
					imageview.setImageDrawable(drawable);
				}
			}
			else {
				Logcat.d(logLevel, "ImageViewがnullです. index=" + index);
			}
			// ボタンを右端へ追いやるため空白で埋める(格好良いとは言えないがパラメータ調整では修正できなかったのでこの方法にした)
			int spaceCount = 100;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < spaceCount; i++) {
				sb.append(" ");
			}
			String combinedText = sb.toString();
			// テキストをセット
			textview.setText(mlists[index].getItems() + combinedText);
			// ボタンの矢印をセット
			Resources res = mActivity.getResources();
			button1.setText(res.getString(R.string.SettingArrowUp));
			button2.setText(res.getString(R.string.SettingArrowDown));
			return view;
		}
	}

	@Override
	public void onClick(View v) {
		// キャンセルクリック
		if (v.getId() == R.id.btn_ok) {
			// 選択完了
			for (int i = 0; i < mStates.length; i++) {
				mStates[mlists[i].getIndex()] = mlists[i].getChecked();
			}
			saveToolbarState(mActivity, mStates);
		}
		dismiss();
	}

}