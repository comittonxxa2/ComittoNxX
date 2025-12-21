package src.comitton.imageview;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.config.SetCornerEndImageViewerActivity;
import src.comitton.config.SetCornerEndTextViewerActivity;
import src.comitton.textview.TextActivity;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Point;
import android.os.SystemClock;
import android.text.Editable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Bundle;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.Checkable;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.Display;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TouchPanelView extends View {

	private static int mWidth;
	private static int mHeight;
	private static int tapindex = -1;
	private static int tappattern = 0;
	private static int tapcornerendindex = 0;
	private static int tapcornerenddoubleindex = 0;
	private static boolean tapcornerendmode = false;
	private static String text;
	private static float x,y;
	private static Context mContext;
	private static AlertDialog.Builder builder;
	private static SharedPreferences mSharedPreferences;
	private static int mMode;
	private static int clickmode = 0;
	private static int customkeynumber = 0;
	private static Thread mCustomkeyThread = null;
	private static Handler mainHandler;
	private static boolean breakthread = false;
	private static AlertDialog.Builder custom_builder = null;
	private static AlertDialog dialog = null;
	private static int dialog_keycode = 0;
	private static int input_dialog_keycode = 0;
	private static boolean editcheck = false;
	private static CharSequence[] items = null;
	private static LinearLayout mMainLayout;
	private static boolean keyboardoff = false;
	private static InputMethodManager mInputMethodManager;
	private static String[] mProfileWord;

	// キーボード表示を制御するためのオブジェクト
	InputMethodManager inputMethodManager;
	// 背景のレイアウト
	private LinearLayout mainLayout;

	// タッチパネル設定の値の格納用
	private static int [] tapdata = {
		0,1,2,3,4,5,6,7,8,9,10
	};

	// タッチパネル設定に有効な項目をtrueにする(イメージビューア)
	public static final boolean[] ImgEnable =
	{
		true,	//	0
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,	//	10
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,	//	20
		false,
		false,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,	//	30
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,	//	40
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,	//	50
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		false,
		false,	//	60
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		true,	//	70
		true,
		true,
		false,
		true,
		true,
		true,
		true,
		true,
		true,
		true,	//	80
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,	//	90
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,	//	100
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,	//	110
		true,
		true,
		true,
		true,
	};

	// タッチパネル設定に有効な項目をtrueにする(テキストビューア)
	public static final boolean[] TxtEnable =
	{
		true,	//	0
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,	//	10
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		true,
		false,
		false,	//	20
		true,
		true,
		false,
		false,
		false,
		false,
		false,
		true,
		true,
		true,	//	30
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,	//	40
		false,
		false,
		false,
		false,
		false,
		true,
		true,
		true,
		true,
		false,	//	50
		false,
		false,
		false,
		false,
		true,
		true,
		true,
		false,
		false,
		false,	//	60
		false,
		false,
		false,
		false,
		false,
		false,
		true,
		true,
		true,
		false,	//	70
		false,
		false,
		true,
		true,
		true,
		true,
		false,
		true,
		true,
		false,	//	80
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,	//	90
		false,
		false,
		true,
		false,
		false,
		false,
		false,
		false,
		false,
		false,	//	100
		false,
		false,
		false,
		false,
		false,
		false,
		true,
		false,
		false,
		false,	//	110
		false,
		false,
		true,
		false,
	};

	// ラジオボタンのアラートダイアログに表示するリストの文字列のテーブル
	public static final int[] HardwareKeyName =
	{
		R.string.hardwarekey00,
		R.string.noselect,
		R.string.hardwarekey02,
		R.string.ToolbarLeftmost,		// 一番左のページ
		R.string.ToolbarLeft100,		// 左へ100ページ
		R.string.ToolbarLeft10,			// 左へ10ページ
		R.string.ToolbarLeft1,			// 左へ1ページ
		R.string.ToolbarRight1,			// 右へ1ページ
		R.string.ToolbarRight10,		// 右へ10ページ
		R.string.ToolbarRight100,		// 右へ100ページ
		R.string.ToolbarRightMost,		// 一番右のページ
		R.string.ToolbarNextScroll,		// 次のページへスクロール
		R.string.ToolbarPrevScroll,		// 前のページへスクロール
		R.string.ToolbarPinchScaleUp,	// ピンチスケールアップ
		R.string.ToolbarPinchScaleDown,	// ピンチスケールダウン
		R.string.ToolbarBookLeft,		// 前のファイル(最終ページ)/次のファイル(先頭ページ)
		R.string.ToolbarBookRight,		// 次のファイル(先頭ページ)/前のファイル(最終ページ)
		R.string.ToolbarBookmarkLeft,	// 前(次)のファイル(しおり位置)
		R.string.ToolbarBookmarkRight,	// 次(前)のファイル(しおり位置)
		R.string.TapThumbSlider,	// サムネイル/スライダー切り替え
		R.string.TapDirTree,		// サブディレクトリ選択
		R.string.TapTOC,			// 見出し選択
		R.string.TapSearch,			// 検索文字列設定
		R.string.TapShare,			// 共有
		R.string.ToolbarRotate,			// 画面方向切り替え
		R.string.TapRotateImage,	// 画像の回転
		R.string.TapSelectThum,		// サムネイルに設定
		R.string.TapTrimThumb,		// 範囲選択してサムネイルに設定
		R.string.ToolbarEditToolbar,			// メニューを開く
		R.string.ToolbarConfig,			// 設定画面を開く
		R.string.ToolbarPulldownMenu,	// プルダウンメニューを開く

		R.string.scrlway00,	// 横方向→縦方向
		R.string.scrlway01,	// 縦方向→横方向
		R.string.Toolbardisplayposition00,	// 中心へ移動
		R.string.Toolbardisplayposition01,	// 上へ移動
		R.string.Toolbardisplayposition02,	// 下へ移動
		R.string.Toolbardisplayposition03,	// 左へ移動
		R.string.Toolbardisplayposition04,	// 右へ移動
		R.string.Toolbardisplayposition05,	// 左上へ移動
		R.string.Toolbardisplayposition06,	// 右上へ移動
		R.string.Toolbardisplayposition07,	// 左下へ移動
		R.string.Toolbardisplayposition08,	// 右下へ移動
		R.string.selrota00,		// 回転無しで表示
		R.string.selrota01,		// 90°回転して表示
		R.string.selrota02,		// 180°回転して表示
		R.string.selrota03,		// 270°回転して表示
		R.string.selsize00,		// 元のサイズで表示
		R.string.selsize01,		// 幅に合わせて表示
		R.string.selsize02,		// 高さに合わせて表示
		R.string.selsize03,		// 全体を表示
		R.string.selsize04,		// 画面全体で表示
		R.string.selsize05,		// 画面全体で表示(見開き対応)
		R.string.selsize06,		// 幅に合わせて表示(見開き対応)
		R.string.selsize07,		// 全体を表示(見開き対応)
		R.string.selview00,		// そのまま表示
		R.string.selview01,		// 見開き表示
		R.string.selview02,		// 単ページ表示
		R.string.selview03,		// 連続表示
		R.string.selview04,		// 単ページ／見開き
		R.string.Toolbarpicsize00,		// 元画像サイズ
		R.string.Toolbarpicsize01,		// 元画像サイズの2倍
		R.string.Toolbarpicsize02,		// 元画像サイズの3倍
		R.string.Toolbarpicsize03,		// 元画像サイズの4倍
		R.string.Toolbarpicsize04,		// 画面サイズに拡大
		R.string.Toolbarpicsize05,		// 画面サイズに拡大(余白無視)
		R.string.Toolbarpicsize06,		// 画面サイズに拡大(見開き対応)
		R.string.Toolbarpicsize07,		// 画面サイズに拡大(見開き対応見開き対応:余白無視)
		R.string.Tapascmode00,		// 縦表示
		R.string.Tapascmode01,		// 横表示(回転)
		R.string.Tapascmode02,		// 縦/横併用
		R.string.mgnCutMenu,	// 余白削除
		R.string.mgnCutColorMenu,	// 余白削除の色
		R.string.imgConfMenu,	// 画像表示設定
		R.string.txtConfMenu,	// テキスト表示設定
		R.string.tguide02,		// 見開き設定
		R.string.tguide03,		// 画像サイズ
		R.string.noiseMenu,		// 音操作
		R.string.playMenu,		// オートプレイ開始
		R.string.addBookmarkMenu,// ブックマーク追加
		R.string.selBookmarkMenu,// ブックマーク選択
		R.string.sharpenMenu,	// シャープ化
		R.string.moireMenu,		// モアレ軽減
		R.string.brightMenu,	// 明るさ補正
		R.string.gammaMenu,		// ガンマ補正
		R.string.contrastMenu,	// コントラスト
		R.string.hueMenu,		// 色相
		R.string.saturationMenu,	// 彩度
		R.string.bklightMenu,	// バックライト
		R.string.invertMenu,	// 白黒反転
		R.string.grayMenu,		// グレースケール
		R.string.coloringMenu,	// 自動着色
		R.string.algoriMenu,	// 画像補間方式
		R.string.reverseMenu,	// ページ逆順
		R.string.chgOpeMenu,	// 操作入れ替え
		R.string.pageWayMenu,	// 表紙方向
		R.string.scrlWay2Menu,	// スクロール方向入れ替え
		R.string.setTopMenu,	// 上部メニュー設定
		R.string.cMargin,		// 中央余白表示
		R.string.cShadow,		// 中央影表示
		R.string.DisplayPositionMenu,		// 画面の表示位置
		R.string.SetProfileMenu,	// プロファイルの登録
		R.string.DelProfileMenu,	// プロファイルの削除
		R.string.Profile1,		// プロファイル1
		R.string.Profile2,		// プロファイル2
		R.string.Profile3,		// プロファイル3
		R.string.Profile4,		// プロファイル4
		R.string.Profile5,		// プロファイル5
		R.string.ToolbarExitViewer,
		R.string.Profile6,		// プロファイル6
		R.string.Profile7,		// プロファイル7
		R.string.Profile8,		// プロファイル8
		R.string.Profile9,		// プロファイル9
		R.string.Profile10,		// プロファイル10
		R.string.SwitchingScreenOrientation,	// 表示方向の切り替え
		R.string.AnimationPause,
	};

	// ラジオボタンのアラートダイアログに表示するリストの文字列のテーブル
	private static final int[] TapClickString =
	{
		R.string.tapClick00,
		R.string.tapClick01,
		R.string.tapClick02,
	};

	public TouchPanelView(Context context, int index) {
		super(context);
		mContext = context;
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		// 値を読み出す
		if (index == 1) {
			mMode = 1;
			// イメージビューア
			LoadTapPatternData();
		}
		else if (index == 2) {
			mMode = 2;
			// テキストビューア
			LoadTapPatternTxtData();
		}
		customkeynumber = 0;
	}

	// 編集可能かどうかを調べる
	public static boolean GetEditMode() {
		boolean edit = false;
		if (tappattern == 0) {
			// 設定なしの場合は不可能にする
			edit = false;
		}
		else {
			edit = true;
		}
		return edit;
	}

	// タップ操作のパターンのアラートダイアログを表示
	public static void SetAlertDialogTag(Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dialogView = inflater.inflate(R.layout.dialog_radio_with_image, null);
		// ラジオボタンのダイアログのリストを表示
		builder.setView(dialogView);

		RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group_items);
		RadioButton radioButton = null;
		// タップ操作のパターンでラジオボタンのIDを切り替える
		if (tappattern == 0) {
			radioButton = dialogView.findViewById(R.id.radio_option0);
		}
		else if (tappattern == 1) {
			radioButton = dialogView.findViewById(R.id.radio_option1);
		}
		else if (tappattern == 2) {
			radioButton = dialogView.findViewById(R.id.radio_option2);
		}
		else if (tappattern == 3) {
			radioButton = dialogView.findViewById(R.id.radio_option3);
		}
		else if (tappattern == 4) {
			radioButton = dialogView.findViewById(R.id.radio_option4);
		}
		else if (tappattern == 5) {
			radioButton = dialogView.findViewById(R.id.radio_option5);
		}
		else if (tappattern == 6) {
			radioButton = dialogView.findViewById(R.id.radio_option6);
		}
		else if (tappattern == 7) {
			radioButton = dialogView.findViewById(R.id.radio_option7);
		}
		else if (tappattern == 8) {
			radioButton = dialogView.findViewById(R.id.radio_option8);
		}
		else if (tappattern == 9) {
			radioButton = dialogView.findViewById(R.id.radio_option9);
		}

		// ラジオボタンにチェックを入れる
        radioButton.setChecked(true);
		RadioButton finalRadioButton = radioButton;
		// ラジオボタンの位置までスクロールさせる
        ScrollView scrollView = dialogView.findViewById(R.id.scroll_view_id);
		scrollView.post(() -> {
			int[] location = new int[2];
			// 画面上の座標を取得
			finalRadioButton.getLocationOnScreen(location);
			// スクロールさせる
			scrollView.smoothScrollTo(0, finalRadioButton.getTop());
		});

		builder.setTitle(R.string.tapPatternMenu)
			.setPositiveButton(R.string.btnOK, (dialog, which) -> {
				int selectedId = radioGroup.getCheckedRadioButtonId();
				RadioButton selectedRadioButton = dialogView.findViewById(selectedId);
				// 選択されたラジオボタンの処理
				if (selectedId == R.id.radio_option0) {
					// オプション1が選択された場合の処理
					tappattern = 0;
				} else if (selectedId == R.id.radio_option1) {
					// オプション2が選択された場合の処理
					tappattern = 1;
				} else if (selectedId == R.id.radio_option2) {
					// オプション3が選択された場合の処理
					tappattern = 2;
				} else if (selectedId == R.id.radio_option3) {
					// オプション4が選択された場合の処理
					tappattern = 3;
				} else if (selectedId == R.id.radio_option4) {
					// オプション5が選択された場合の処理
					tappattern = 4;
				} else if (selectedId == R.id.radio_option5) {
					// オプション6が選択された場合の処理
					tappattern = 5;
				} else if (selectedId == R.id.radio_option6) {
					// オプション2が選択された場合の処理
					tappattern = 6;
				} else if (selectedId == R.id.radio_option7) {
					// オプション2が選択された場合の処理
					tappattern = 7;
				} else if (selectedId == R.id.radio_option8) {
					// オプション2が選択された場合の処理
					tappattern = 8;
				} else if (selectedId == R.id.radio_option9) {
					// オプション2が選択された場合の処理
					tappattern = 9;
				}
				Editor ed = mSharedPreferences.edit();
				if (mMode == 1) {
					ed.putInt(DEF.KEY_TAP_I_PATTERN_NUMBER, tappattern);
				}
				else if (mMode == 2) {
					ed.putInt(DEF.KEY_TAP_T_PATTERN_NUMBER, tappattern);
				}
				ed.apply();
				// 値を読み出す
				if (mMode == 1) {
					// イメージビューア
					LoadTapPatternData();
				}
				else if (mMode == 2) {
					// テキストビューア
					LoadTapPatternTxtData();
				}
			})
			.setNegativeButton(R.string.btnCancel, null);
		// アラートダイアログを表示
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	// カスタムキーのコードを表示させるための文字列を取得
	private static String GetCodeString(int index) {
		// カスタムキーのコードを読み出す
		int codedata = TouchPanelView.LoadCustomkeyCode(mSharedPreferences, index);
		Resources res = mContext.getResources();
		String StrNum;
		if (codedata != 0) {
			// 見つかれば数値を文字列に変換
			StrNum = String.valueOf(codedata);
			for (int i = 0; i < DEF.KEYCODE_INDEX.length; i++) {
				if (DEF.KEYCODE_INDEX[i] == codedata) {
					// 準備されたコードに含まれていれば表示する
					StrNum = StrNum + "(" + res.getString(DEF.HardwareKeyTitleName[i]) + ")";
					break;
				}
			}
		}
		else {
			// 見つからなかった場合はメッセージを取得
			StrNum = res.getString(R.string.none);
		}
		// 文字列を組み立てる
		String ViewCodeData = res.getString(R.string.ViewCodeData) + " : " + StrNum;
		return ViewCodeData;
	}

	// カスタムキーのアラートダイアログを表示
	public static void SetAlertDialogCustom(Activity activity) {
		custom_builder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dialogView = inflater.inflate(R.layout.dialog_customkey, null);
		// プルダウン
		Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner);
		spinner.setSelection(customkeynumber);
		TextView codeview = (TextView)  dialogView.findViewById(R.id.codeview);
		codeview.setText(GetCodeString(customkeynumber));
		// タイトル入力
		EditText editText = (EditText) dialogView.findViewById(R.id.editTextDialogInput);
		editText.setHint(R.string.HintHardwareKey);
		editText.setText(LoadCustomkeyTitle(mSharedPreferences, customkeynumber));

		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// 入力前
				// コードの取得を無効にする
				keyboardoff = true;
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// 入力中
			}
			@Override
			public void afterTextChanged(Editable s) {
				// 入力後
			}
		});
		// フォーカスリスナーのセット.
		editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				// コードの取得の要綱/無効を切り替える
				keyboardoff = hasFocus;
			}
		});

		// アラートダイアログのレイアウトを取得
		mMainLayout = (LinearLayout)dialogView.findViewById(R.id.main_layout);
		// キーボードマネージャーを取得
		InputMethodManager mInputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		// タイトル入力決定ボタン
		Button button1 = dialogView.findViewById(R.id.positiveButton);
		button1.setText(R.string.BtnTitleEnter);
		button1.setOnClickListener(v -> {
			// キーボードの表示を消す
			mInputMethodManager.hideSoftInputFromWindow(mMainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			// 他のフォーカスを取得(あらかじめxmlでfocusableInTouchModeを有効にしておくこと)
			codeview.requestFocus();
			codeview.setFocusable(true);
			codeview.setFocusableInTouchMode(true);
			// コードの取得を有効にする
			keyboardoff = false;
		});
		// タイトル入力キャンセルボタン
		Button button2 = dialogView.findViewById(R.id.negaitiveButton);
		button2.setText(R.string.BtnTitleCansel);
		button2.setOnClickListener(v -> {
			// キーボードの表示を消す
			mInputMethodManager.hideSoftInputFromWindow(mMainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			// 他のフォーカスを取得(あらかじめxmlでfocusableInTouchModeを有効にしておくこと)
			codeview.requestFocus();
			codeview.setFocusable(true);
			codeview.setFocusableInTouchMode(true);
			// テキストの中身を元に戻す
			editText.setText(LoadCustomkeyTitle(mSharedPreferences, customkeynumber));
			// コードの取得を有効にする
			keyboardoff = false;
		});

		// メッセージ
		custom_builder.setView(dialogView);
		custom_builder.setMessage(R.string.WaitHardwareKey);
		custom_builder.setTitle(R.string.DetectHardwareKey);
		// OKボタンクリック
		custom_builder.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// OKがクリックされた際の処理
				// タイトル入力の内容を取り出す
				String inputText = editText.getText().toString();
				// プルダウンの文字を取り出す
				String item = (String) spinner.getSelectedItem();
				// プルダウンのリストを取り出す
				String itemlist[] = dialogView.getResources().getStringArray(R.array.customkeylist);
				for (int i = 0; i < itemlist.length; i++) {
					// プルダウンの文字とリストを比較する
					if (item.equals(itemlist[i])) {
						// プルダウンの位置を取得
						customkeynumber = i;
						break;
					}
				}
				// タイトル入力の内容を保存
				SaveCustomkeyTitle(mSharedPreferences, inputText, customkeynumber);
				// コードを保存
				SaveCustomkeyCode(mSharedPreferences, dialog_keycode, customkeynumber);
				// カスタムキーの検出用のスレッドを終了させる
				breakthread = true;
			}
		});
		// キャンセルボタンクリック
		custom_builder.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// キャンセルがクリックされた際の処理
				// ダイアログを閉じる
				dialog.dismiss();
				// カスタムキーの検出用のスレッドを終了させる
				breakthread = true;
			}
		});
		// クリアボタンクリック
		custom_builder.setNeutralButton(R.string.btnClear, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// リスナーで検出するのでここでは何もしない(ダイアログの表示を維持するため)
			}
		});
		// プルダウンが選択された
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				// 項目が選択された時の処理をここに記述
				String selectedItem = (String) parentView.getItemAtPosition(position);
				// プルダウンのリストを取り出す
				String itemlist[] = dialogView.getResources().getStringArray(R.array.customkeylist);
				for (int i = 0; i < itemlist.length; i++) {
					// プルダウンの文字とリストを比較する
					if (selectedItem.equals(itemlist[i])) {
						// プルダウンの位置を取得
						if (customkeynumber != i) {
							// 変更された場合
							customkeynumber = i;
							// タイトル入力を切り替える
							editText.setText(LoadCustomkeyTitle(mSharedPreferences, customkeynumber));
							GetCodeString(customkeynumber);
							codeview.setText(GetCodeString(customkeynumber));

							keyboardoff = false;
							break;
						}
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// 何も選択されなかった時の処理
			}
		});
		// アラートダイアログを作成
		dialog = custom_builder.create();
		// ハードウェアキー検出のリスナーを実装する
		dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// コードを取得
				if (keyboardoff == true) {
					// タイトル入力中は取得を保留
					// キー入力させるためそのまま返す
					return false;
				}
				else {
					dialog_keycode = keyCode;
				}
				// 他のアプリに制御を渡さない
				return true;
			}
		});
		// アラートダイアログを表示
		dialog.show();
		// クリアボタンの検出のリスナーを実装する
		Button button = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
		button.setOnClickListener(new View.OnClickListener() {
			// ダイアログを閉じさせないようにする
			@Override
			public void onClick(View v) {
				// クリアがクリックされた際の処理
				// コードをクリア
				dialog_keycode = 0;
			}
		});
		// カスタムキーの検出用のスレッドを実行
		mCustomkeyThread = new CustomkeyThread();
		mCustomkeyThread.start();
	}


	// カスタムキーの検出用のスレッド
	public static class CustomkeyThread extends Thread  {
		public void run() {
			String message_old = "";
			int wait_mes_count = 0;
			long start_time = SystemClock.uptimeMillis();
			boolean stop = false;
			while (!stop) {
				if (dialog != null) {
					try {
						if (breakthread) {
							// 終了
							breakthread = false;
							stop = true;
						}
						// 100ミリ秒単位で検出させる
						mCustomkeyThread.sleep(100);
						String message;
						Resources res = mContext.getResources();
						if (dialog_keycode != 0) {
							// コードを検出
							message = res.getString(R.string.FoundHardwareKey) + dialog_keycode;
							// 見つかれば数値を文字列に変換
							for (int i = 0; i < DEF.KEYCODE_INDEX.length; i++) {
								if (DEF.KEYCODE_INDEX[i] == dialog_keycode) {
									message = message + "(" + res.getString(DEF.HardwareKeyTitleName[i]) + ")";
									break;
								}
							}
						}
						else {
							// コードが検出されていない
							message = res.getString(R.string.WaitHardwareKey);
							// キーの検出を待っている間は末尾のドットを繰り返す
							long now_time = SystemClock.uptimeMillis();
							if ((now_time - start_time) > 300) {
								start_time = now_time;
								wait_mes_count++;
								if (wait_mes_count > 3) {
									wait_mes_count = 0;
								}
							}
							for (int i = 0; i < wait_mes_count; i++) {
								// 末尾にドットを付け足す
								message += ".";
							}
						}
						// メイン画面で表示させるためハンドラを得る
						mainHandler = new Handler(Looper.getMainLooper());
						// メイン画面で表示
						if (!message.equals(message_old)) {
							// 変化があった時だけメッセージを表示
							message_old = message;
							String finalMessage = message;
							mainHandler.post(() -> {
								dialog.setMessage(finalMessage);
							});
						}
					} catch  (Exception e) {
					}
				}
			}
			mResult();
		}
	}
	private static void mResult() {
		// スレッドを終了させて最初から始める
		mCustomkeyThread = null;
	}

	// タッチパネル設定のアラートダイアログを表示
	public static void SetAlertDialogClick(Activity activity) {

		// 初期選択したいラジオボタンのインデックス (0から始まる)
		int checkedItem = clickmode;
		// 最大数を合わせなおして格納する
		items = new String[TapClickString.length];
		for (int i = 0; i < TapClickString.length; i++) {
			items[i] = activity.getResources().getString(TapClickString[i]);
		}
		builder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
		builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ラジオボタンが選択された際の処理
				// whichは選択されたラジオボタンのインデックス
				// 今回は何もしない
			}
		});

		builder.setTitle(R.string.tapClickMenu);
		builder.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// OKがクリックされた際の処理
				// 選択されたラジオボタンの値をここから取得する
				ListView listView = ((AlertDialog) dialog).getListView();
				CharSequence selectedItem = items[listView.getCheckedItemPosition()];
				clickmode = listView.getCheckedItemPosition();
			}
		});

		builder.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// キャンセルがクリックされた際の処理
				dialog.dismiss(); // ダイアログを閉じる
			}
		});
		// アラートダイアログを表示
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private static void SetProfileWord() {
		mProfileWord = new String[10];
		// 初期値を読み出す
		mProfileWord[0] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_01, "");
		mProfileWord[1] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_02, "");
		mProfileWord[2] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_03, "");
		mProfileWord[3] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_04, "");
		mProfileWord[4] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_05, "");
		mProfileWord[5] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_06, "");
		mProfileWord[6] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_07, "");
		mProfileWord[7] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_08, "");
		mProfileWord[8] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_09, "");
		mProfileWord[9] = mSharedPreferences.getString(DEF.KEY_PROFILE_WORD_10, "");
	}

	// タッチパネル設定のアラートダイアログを表示
	public static void SetAlertDialog(Activity activity) {

		// 初期選択したいラジオボタンのインデックス (0から始まる)
		int checkedItem_temp;
		if (clickmode == 2) {
			checkedItem_temp = (tapdata[tapindex] >> 16) & 0xff;
		}
		else if (clickmode == 1) {
			checkedItem_temp = (tapdata[tapindex] >> 8) & 0xff;
		}
		else {
			checkedItem_temp = tapdata[tapindex] & 0xff;
		}
		int checkedItem = 0;
		final int[] loop = {0};
		SetProfileWord();
		String[] items_temp = new String[HardwareKeyName.length];
		// タッチパネル設定に有効な項目を取り出して格納する
		for (int i = 0; i < HardwareKeyName.length; i++) {
			if (mMode == 1) {
				// イメージビューア
				if (ImgEnable[i]) {
					// 有効な項目のみ格納する
					items_temp[loop[0]] = activity.getResources().getString(HardwareKeyName[i]);
					if (i >= DEF.TAP_PROFILE1 && i <= DEF.TAP_PROFILE5 && !mProfileWord[i - DEF.TAP_PROFILE1].equals("")) {
						items_temp[loop[0]] = mProfileWord[i - DEF.TAP_PROFILE1];
					}
					if (i >= DEF.TAP_PROFILE6 && i <= DEF.TAP_PROFILE10 && !mProfileWord[i - DEF.TAP_PROFILE6 + 5].equals("")) {
						items_temp[loop[0]] = mProfileWord[i - DEF.TAP_PROFILE6 + 5];
					}
					if (checkedItem_temp == i) {
						// 初期選択したいラジオボタンのインデックスが有効な項目と一致した場合は有効な通し番号に置き換える
						checkedItem = loop[0];
					}
					loop[0]++;
				}
			}
			else if (mMode == 2) {
				// テキストビューア
				if (TxtEnable[i]) {
					// 有効な項目のみ格納する
					items_temp[loop[0]] = activity.getResources().getString(HardwareKeyName[i]);
					if (checkedItem_temp == i) {
						// 初期選択したいラジオボタンのインデックスが有効な項目と一致した場合は有効な通し番号に置き換える
						checkedItem = loop[0];
					}
					loop[0]++;
				}
			}
		}
		// 最大数を合わせなおして格納する
		items = new String[loop[0]];
		for (int i = 0; i < loop[0]; i++) {
			items[i] = items_temp[i];
		}

		builder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
		builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ラジオボタンが選択された際の処理
				// whichは選択されたラジオボタンのインデックス
				// 今回は何もしない
			}
		});

		builder.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// OKがクリックされた際の処理
				// 選択されたラジオボタンの値をここから取得する
				ListView listView = ((AlertDialog) dialog).getListView();
				CharSequence selectedItem = items[listView.getCheckedItemPosition()];
				loop[0] = 0;
				// 選択されたラジオボタンの値から元の番号へ戻す
				for (int i = 0; i < HardwareKeyName.length; i++) {
					if (mMode == 1) {
						// イメージビューア
						if (ImgEnable[i]) {
							if (loop[0] == listView.getCheckedItemPosition()) {
								// 一致したら元の値で格納する
								if (clickmode == 2) {
									tapdata[tapindex] = (tapdata[tapindex] & 0xff00ffff) | (i << 16);
								}
								else if (clickmode == 1) {
									tapdata[tapindex] = (tapdata[tapindex] & 0xffff00ff) | (i << 8);
								}
								else {
									tapdata[tapindex] = (tapdata[tapindex] & 0xffffff00) | i;
								}
								// 格納したら終了
								break;
							}
							loop[0]++;
						}
					}
					else if (mMode == 2) {
						// テキストビューア
						if (TxtEnable[i]) {
							if (loop[0] == listView.getCheckedItemPosition()) {
								// 一致したら元の値で格納する
								if (clickmode == 2) {
									tapdata[tapindex] = (tapdata[tapindex] & 0xff00ffff) | (i << 16);
								}
								else if (clickmode == 1) {
									tapdata[tapindex] = (tapdata[tapindex] & 0xffff00ff) | (i << 8);
								}
								else {
									tapdata[tapindex] = (tapdata[tapindex] & 0xffffff00) | i;
								}
								// 格納したら終了
								break;
							}
							loop[0]++;
						}
					}
				}
				// 値を書き込む
				if (mMode == 1) {
					// イメージビューア
					SaveTapPatternData();
					// 表示更新
					ImageActivity.UpdateTouchPanelData();
				}
				else if (mMode == 2) {
					// テキストビューア
					SaveTapPatternTxtData();
					// 表示更新
					TextActivity.UpdateTouchPanelData();
				}
			}
		});

		builder.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// キャンセルがクリックされた際の処理
				dialog.dismiss(); // ダイアログを閉じる
			}
		});
		// アラートダイアログを表示
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	// タッチパネル設定を取り出す
	public static int GetTouchPositionData(int mode) {
		int data = -1;
		if (tappattern == 0) {
			// パターンが未設定の場合
			if (GetCornetEndParameter(Enable) != 0) {
				// 四隅と端のタップ操作が有効の場合
				if (tapcornerendmode) {
					// ダブルタップが有効の場合
					if (mode == 1) {
						// シングルタップ
						data = tapcornerendindex;
					}
					else if (mode == 2) {
						// ダブルタップ
						data = tapcornerenddoubleindex;
					}
				}
				else {
					if (mode == 1) {
						// シングルタップ
						data = tapcornerendindex;
					}
				}
			}
			if (data == 0) {
				// システム設定の場合は未設定にする
				data = -1;
			}
		}
		else if (tapindex == -1) {
			// 座標が未定の場合
			data = -1;
		}
		else {
			if (mode == 0) {
				// チェックの場合は0を返す
				data = 0;
			}
			else {
				// タッチパネル設定を取り出す
				int tempdata = tapdata[tapindex];
				switch (mode) {
					case 1:
						// シングルタップ
						data = tempdata & 0xff;
						break;
					case 2:
						// ダブルタップ
						data = (tempdata >> 8) & 0xff;
						break;
					case 3:
						// 長押し
						data = (tempdata >> 16) & 0xff;
						break;
					default:
						data = 0;
						break;
				}
			}
		}
		return data;
	}

	private static final int Enable = 0;
	private static final int Width = 1;
	private static final int Height = 2;
	private static final int TopLeftCorner = 3;
	private static final int TopRightCorner = 4;
	private static final int BottomLeftCorner = 5;
	private static final int BottomRightCorner = 6;
	private static final int LeftEnd = 7;
	private static final int RightEnd = 8;
	private static final int TopEnd = 9;
	private static final int BottomEnd = 10;
	private static final int DoubleTap = 11;
	private static final int TopLeft = 12;
	private static final int TopRight = 13;
	private static final int BottomLeft = 14;
	private static final int BottomRight = 15;
	private static final int SingleTap = 16;

	// 四隅と端のタップ操作のパラメータ読み出しでイメージビューアとテキストビューアで振り分ける
	private static int GetCornetEndParameter(int index) {
		int data = 0;
		switch (index) {
			case Enable:
				if (mMode == 1) {
					data = (SetCornerEndImageViewerActivity.getCornerEndEnable(mSharedPreferences)) ? 1 : 0;
				}
				else if (mMode == 2) {
					data = (SetCornerEndTextViewerActivity.getCornerEndEnable(mSharedPreferences)) ? 1 : 0;
				}
				break;
			case Width:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getCornerEndWidthLevel(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getCornerEndWidthLevel(mSharedPreferences);
				}
				break;
			case Height:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getCornerEndHeightLevel(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getCornerEndHeightLevel(mSharedPreferences);
				}
				break;
			case TopLeftCorner:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getTopLeftCornerTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getTopLeftCornerTap(mSharedPreferences);
				}
				break;
			case TopRightCorner:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getTopRightCornerTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getTopRightCornerTap(mSharedPreferences);
				}
				break;
			case BottomLeftCorner:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getBottomLeftCornerTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getBottomLeftCornerTap(mSharedPreferences);
				}
				break;
			case BottomRightCorner:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getBottomRightCornerTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getBottomRightCornerTap(mSharedPreferences);
				}
				break;
			case LeftEnd:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getLeftEndTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getLeftEndTap(mSharedPreferences);
				}
				break;
			case RightEnd:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getRightEndTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getRightEndTap(mSharedPreferences);
				}
				break;
			case TopEnd:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getTopEndTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getTopEndTap(mSharedPreferences);
				}
				break;
			case BottomEnd:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getBottomEndTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getBottomEndTap(mSharedPreferences);
				}
				break;
			case DoubleTap:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getDoubleTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getDoubleTap(mSharedPreferences);
				}
				break;
			case TopLeft:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getTopLeftTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getTopLeftTap(mSharedPreferences);
				}
				break;
			case TopRight:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getTopRightTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getTopRightTap(mSharedPreferences);
				}
				break;
			case BottomLeft:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getBottomLeftTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getBottomLeftTap(mSharedPreferences);
				}
				break;
			case BottomRight:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getBottomRightTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getBottomRightTap(mSharedPreferences);
				}
				break;
			case SingleTap:
				if (mMode == 1) {
					data = SetCornerEndImageViewerActivity.getSingleTap(mSharedPreferences);
				}
				else if (mMode == 2) {
					data = SetCornerEndTextViewerActivity.getSingleTap(mSharedPreferences);
				}
				break;
		}
		return data;
	}

	// 四隅と端のタップ操作のチェック
	private static void CheckCornetEnd(int x, int y, int width, int height) {
		if (GetCornetEndParameter(Enable) != 0) {
			// 四隅と端のタップ操作が有効の場合
			float cornerendwidthlevelmin = (float)GetCornetEndParameter(Width) / 100;
			float cornerendwidthlevelmax = (100 - (float)GetCornetEndParameter(Width)) / 100;
			float cornerendheightlevelmin = (float)GetCornetEndParameter(Height) / 100;
			float cornerendheightlevelmax = (100 - (float)GetCornetEndParameter(Height)) / 100;
			// あらかじめ設定なしに設定しておく
			tapcornerendindex = -1;
			tapcornerenddoubleindex = -1;
			tapcornerendmode = false;
			// 優先順位の高い順に範囲をチェックする
			if (GetCornetEndParameter(TopLeftCorner) != 1 && x < (width * cornerendwidthlevelmin) && y < (height * cornerendheightlevelmin)) {
				// 設定が無反応以外で範囲内なら
				// 左上隅
				tapcornerendindex = GetCornetEndParameter(TopLeftCorner);
			}
			if (GetCornetEndParameter(TopRightCorner) != 1 && x >= (width * cornerendwidthlevelmax) && y < (height * cornerendheightlevelmin) && tapcornerendindex == -1) {
				// 左上隅が設定なしで設定が無反応以外で範囲内なら
				// 右上隅
				tapcornerendindex = GetCornetEndParameter(TopRightCorner);
			}
			if (GetCornetEndParameter(BottomLeftCorner) != 1 && x < (width * cornerendwidthlevelmin) && y >= (height * cornerendheightlevelmax) && tapcornerendindex == -1) {
				// 右上隅が設定なしで設定が無反応以外で範囲内なら
				// 左下隅
				tapcornerendindex = GetCornetEndParameter(BottomLeftCorner);
			}
			if (GetCornetEndParameter(BottomRightCorner) != 1 && x >= (width * cornerendwidthlevelmax) && y >= (height * cornerendheightlevelmax) && tapcornerendindex == -1) {
				// 左下隅が設定なしで設定が無反応以外で範囲内なら
				// 右下隅
				tapcornerendindex = GetCornetEndParameter(BottomRightCorner);
			}
			if (GetCornetEndParameter(LeftEnd) != 1 && x < (width * cornerendwidthlevelmin) && tapcornerendindex == -1) {
				// 右下隅が設定なしで設定が無反応以外で範囲内なら
				// 左端
				tapcornerendindex = GetCornetEndParameter(LeftEnd);
			}
			if (GetCornetEndParameter(RightEnd) != 1 && x >= (width * cornerendwidthlevelmax) && tapcornerendindex == -1) {
				// 左端が設定なしで設定が無反応以外で範囲内なら
				// 右端
				tapcornerendindex = GetCornetEndParameter(RightEnd);
			}
			if (GetCornetEndParameter(TopEnd) != 1 && y < (height * cornerendheightlevelmin) && tapcornerendindex == -1) {
				// 右端が設定なしで設定が無反応以外で範囲内なら
				// 上端
				tapcornerendindex = GetCornetEndParameter(TopEnd);
			}
			if (GetCornetEndParameter(BottomEnd) != 1 && y >= (height * cornerendheightlevelmax) && tapcornerendindex == -1) {
				// 上端が設定なしで設定が無反応以外で範囲内なら
				// 下端
				tapcornerendindex = GetCornetEndParameter(BottomEnd);
			}
			if (GetCornetEndParameter(DoubleTap) != 1 && tapcornerendindex == - 1) {
				// 下端が設定なしで設定が無反応以外で範囲内なら
				// ダブルタップ
				tapcornerenddoubleindex = GetCornetEndParameter(DoubleTap);
				// シングルタップ動作は無反応に設定
				tapcornerendindex = -1;
				if (tapcornerenddoubleindex > 1) {
					// 設定が有効なら
					tapcornerendmode = true;
				}
			}
			if (GetCornetEndParameter(TopLeft) != 1 && x < (width / 2) && y < (height / 2) && tapcornerendindex == -1) {
				// ダブルタップが設定なしで設定が無反応以外で範囲内なら
				// 左上
				tapcornerendindex = GetCornetEndParameter(TopLeft);
			}
			if (GetCornetEndParameter(TopRight) != 1 && x >= (width / 2) && y < (height / 2)  && tapcornerendindex == -1) {
				// 左上が設定なしで設定が無反応以外で範囲内なら
				// 右上
				tapcornerendindex = GetCornetEndParameter(TopRight);
			}
			if (GetCornetEndParameter(BottomLeft) != 1 && x < (width / 2) && y >= (height / 2) && tapcornerendindex == -1) {
				// 右上が設定なしで設定が無反応以外で範囲内なら
				// 左下
				tapcornerendindex = GetCornetEndParameter(BottomLeft);
			}
			if (GetCornetEndParameter(BottomRight) != 1 && x >= (width / 2) && y >= (height / 2) && tapcornerendindex == -1) {
				// 左下が設定なしで設定が無反応以外で範囲内なら
				// 右下
				tapcornerendindex = GetCornetEndParameter(BottomRight);
			}
			if (tapcornerendindex == -1) {
				// 右下が設定なしなら
				// シングルタップ
				tapcornerendindex = GetCornetEndParameter(SingleTap);
			}
		}
		else {
			// 四隅と端のタップ操作のチェックが無効だった場合
			tapindex = -1;
		}
	}

	// タッチパネルの座標から範囲を計算して番号を取得
	public static void SetTouchPosition(int x, int y, int width, int height) {
		switch (tappattern) {
			case 0:
				// 四隅と端のタップ操作のチェック
				CheckCornetEnd(x, y, width, height);
				break;
			case 1:
				if (x >= 0 && x < (width / 2) && y >= 0 && y < (height / 2)) {
					tapindex = 0;
				}
				else if (x >= (width / 2) && x < width && y >= 0 && y < (height / 2)) {
					tapindex = 1;
				}
				else if (x >= 0 && x < (width / 2) && y >= (height / 2) && y < height) {
					tapindex = 2;
				}
				else if (x >= (width / 2) && x < width && y >= (height / 2) && y < height) {
					tapindex = 3;
				}
				else {
					tapindex = -1;
				}
				break;
			case 2:
				if (x >= 0 && x < (width / 2) && y >= 0 && y < (height / 5)) {
					tapindex = 0;
				}
				else if (x >= (width / 2) && x < width && y >= 0 && y < (height / 5)) {
					tapindex = 1;
				}
				else if (x >= 0 && x < (width / 2) && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 2;
				}
				else if (x >= (width / 2) && x < width && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 3;
				}
				else if (x >= 0 && x < (width / 2) && y >= (height / 5 * 4) && y < height) {
					tapindex = 4;
				}
				else if (x >= (width / 2) && x < width && y >= (height / 5 * 4) && y < height) {
					tapindex = 5;
				}
				else {
					tapindex = -1;
				}
				break;
			case 3:
				if (x >= 0 && x < (width / 10 * 3) && y >= 0 && y < (height / 2)) {
					tapindex = 0;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= 0 && y < (height / 2)) {
					tapindex = 1;
				}
				else if (x >= (width / 10 * 7) && x < width && y >= 0 && y < (height / 2)) {
					tapindex = 2;
				}
				else if (x >= 0 && x < (width / 10 * 3) && y >= (height / 2) && y < height) {
					tapindex = 3;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 2) && y < height) {
					tapindex = 4;
				}
				else if (x >= (width / 10 * 7) && x < width && y >= (height / 2) && y < height) {
					tapindex = 5;
				}
				else {
					tapindex = -1;
				}
				break;
			case 4:
				if (x >= 0 && x < (width / 10 * 3) && y >= 0 && y < (height / 5)) {
					tapindex = 0;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= 0 && y < (height / 5)) {
					tapindex = 1;
				}
				else if (x >= (width / 10 * 7) && x < width && y >= 0 && y < (height / 5)) {
					tapindex = 2;
				}
				else if (x >= 0 && x < (width / 10 * 3) && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 3;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 4;
				}
				else if (x >= (width / 10 * 7) && x < width && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 5;
				}
				else if (x >= 0 && x < (width / 10 * 3) && y >= (height / 5 * 4) && y < height) {
					tapindex = 6;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 5 * 4) && y < height) {
					tapindex = 7;
				}
				else if (x >= (width / 10 * 7) && x < width && y >= (height / 5 * 4) && y < height) {
					tapindex = 8;
				}
				else {
					tapindex = -1;
				}
				break;
			case 5:
				if (x >= 0 && x < (width / 10 * 3) && y >= 0 && y < (height / 5)) {
					tapindex = 0;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= 0 && y < (height / 5)) {
					tapindex = 1;
				}
				else if (x >= (width / 10 * 7) && x < width && y >= 0 && y < (height / 5)) {
					tapindex = 2;
				}
				else if (x >= 0 && x < (width / 10 * 3) && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 3;
				}

				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 5) && y < (height / 5 * 2)) {
					tapindex = 4;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 5 * 2) && y < (height / 5 * 3)) {
					tapindex = 5;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 5 * 3) && y < (height / 5 * 4)) {
					tapindex = 6;
				}

				else if (x >= (width / 10 * 7) && x < width && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 7;
				}

				else if (x >= 0 && x < (width / 10 * 3) && y >= (height / 5 * 4) && y < height) {
					tapindex = 8;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 5 * 4) && y < height) {
					tapindex = 9;
				}
				else if (x >= (width / 10 * 7) && x < width && y >= (height / 5 * 4) && y < height) {
					tapindex = 10;
				}
				else {
					tapindex = -1;
				}
				break;
			case 6:
				if (x >= 0 && x < width && y >= 0 && y < (height / 5)) {
					tapindex = 0;
				}
				else if (x >= 0 && x < (width / 10 * 3) && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 1;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 2;
				}
				else if (x >= (width / 10 * 7) && x < width && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 3;
				}
				else if (x >= 0 && x < width && y >= (height / 5 * 4) && y < height) {
					tapindex = 4;
				}
				else {
					tapindex = -1;
				}
				break;
			case 7:
				if (x >= 0 && x < (width / 2) && y >= 0 && y < (height / 5)) {
					tapindex = 0;
				}
				else if (x >= (width / 2) && x < width && y >= 0 && y < (height / 5)) {
					tapindex = 1;
				}

				else if (x >= 0 && x < (width / 10 * 3) && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 2;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 3;
				}
				else if (x >= (width / 10 * 7) && x < width && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 4;
				}

				else if (x >= 0 && x < (width / 2) && y >= (height / 5 * 4) && y < height) {
					tapindex = 5;
				}
				else if (x >= (width / 2) && x < width && y >= (height / 5 * 4) && y < height) {
					tapindex = 6;
				}
				else {
					tapindex = -1;
				}
				break;
			case 8:
				if (x >= 0 && x < (width / 10 * 3) && y >= 0 && y < height) {
					tapindex = 0;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= 0 && y < (height / 5)) {
					tapindex = 1;
				}
				else if (x >= (width / 10 * 7) && x < width && y >= 0 && y < height) {
					tapindex = 2;
				}

				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 3;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 5 * 4) && y < height) {
					tapindex = 4;
				}
				else {
					tapindex = -1;
				}
				break;
			case 9:
				if (x >= 0 && x < (width / 10 * 3) && y >= 0 && y < (height / 2)) {
					tapindex = 0;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= 0 && y < (height / 5)) {
					tapindex = 1;
				}
				else if (x >= (width / 10 * 7) && x < width && y >= 0 && y < (height / 2)) {
					tapindex = 2;
				}

				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 5) && y < (height / 5 * 4)) {
					tapindex = 3;
				}
				else if (x >= 0 && x < (width / 10 * 3) && y >= (height / 2) && y < height) {
					tapindex = 4;
				}
				else if (x >= (width / 10 * 3) && x < (width / 10 * 7) && y >= (height / 5 * 4) && y < height) {
					tapindex = 5;
				}
				else if (x >= (width / 10 * 7) && x < width && y >= (height / 2) && y < height) {
					tapindex = 6;
				}
				else {
					tapindex = -1;
				}
				break;
		}
	}

	// タッチパネル設定の座標を格納
	public static void SetViewArea(int width, int height) {
		mWidth = width;
		mHeight = height;
	}

	// タッチパネル設定を表示
	public static void Drawmain(Canvas canvas) {

		int disp_x = mWidth;
		int disp_y = mHeight;
		int margin = 1;

		Paint paint = new Paint();

		// 最少輝度
		paint.setColor(0xff000000);
		// 半透明にする
		paint.setAlpha(0xc0);

		// 背景の輝度を下げる
		canvas.drawRect(0, 0, disp_x, disp_y, paint);

		// 最大輝度
		paint.setColor(0xffffffff);
		paint.setAlpha(255);
		// 外枠を表示
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(margin);
		canvas.drawRect(0+margin, 0+margin, disp_x-margin, disp_y-margin, paint);

		int[] tapdatatemp = new int[11];
		for (int i = 0; i < 11; i++) {
			if (clickmode == 2) {
				tapdatatemp[i] = (tapdata[i] >> 16) & 0xff;
			}
			else if (clickmode == 1) {
				tapdatatemp[i] = (tapdata[i] >> 8) & 0xff;
			}
			else {
				tapdatatemp[i] = tapdata[i] & 0xff;
			}
		}

		switch (tappattern) {
			case 1:
				paint.setStyle(Paint.Style.FILL);
				// 縦線
				canvas.drawLine(disp_x / 2, 0+margin, disp_x / 2, disp_y-margin, paint);
				// 横線
				canvas.drawLine(0+margin, disp_y / 2, disp_x-margin, disp_y / 2, paint);

				paint.setStyle(Paint.Style.FILL);

				PutText(tapdatatemp[0], disp_x / 2, disp_y / 2, 0, 0, canvas);
				PutText(tapdatatemp[1], disp_x / 2, disp_y / 2, disp_x / 2, 0, canvas);
				PutText(tapdatatemp[2], disp_x / 2, disp_y / 2, 0, disp_y / 2, canvas);
				PutText(tapdatatemp[3], disp_x / 2, disp_y / 2, disp_x / 2, disp_y / 2, canvas);
				break;
			case 2:
				paint.setStyle(Paint.Style.FILL);
				// 縦線
				canvas.drawLine(disp_x / 2, 0+margin, disp_x / 2, disp_y-margin, paint);
				// 横線
				canvas.drawLine(0+margin, disp_y / 5, disp_x-margin, disp_y / 5, paint);
				canvas.drawLine(0+margin, disp_y / 5 * 4, disp_x-margin, disp_y / 5 * 4, paint);

				paint.setStyle(Paint.Style.FILL);

				PutText(tapdatatemp[0], disp_x / 2, disp_y / 5, 0, 0, canvas);
				PutText(tapdatatemp[1], disp_x / 2, disp_y / 5, disp_x / 2, 0, canvas);

				PutText(tapdatatemp[2], disp_x / 2, disp_y / 5 * 3, 0, disp_y / 5, canvas);
				PutText(tapdatatemp[3], disp_x / 2, disp_y / 5 * 3, disp_x / 2, disp_y / 5, canvas);

				PutText(tapdatatemp[4], disp_x / 2, disp_y / 5, 0, disp_y / 5 * 4, canvas);
				PutText(tapdatatemp[5], disp_x / 2, disp_y / 5, disp_x / 2, disp_y / 5 * 4, canvas);

				break;
			case 3:
				paint.setStyle(Paint.Style.FILL);
				// 縦線
				canvas.drawLine(disp_x / 10 * 3, 0+margin, disp_x / 10 * 3, disp_y-margin, paint);
				canvas.drawLine(disp_x / 10 * 7, 0+margin, disp_x / 10 * 7, disp_y-margin, paint);
				// 横線
				canvas.drawLine(0+margin, disp_y / 2, disp_x-margin, disp_y / 2, paint);

				PutText(tapdatatemp[0], disp_x / 10 * 3, disp_y / 2, 0, 0, canvas);
				PutText(tapdatatemp[1], disp_x / 10 * 4, disp_y / 2, disp_x / 10 * 3, 0, canvas);

				PutText(tapdatatemp[2], disp_x / 10 * 3, disp_y / 2, disp_x / 10 * 7, 0, canvas);

				PutText(tapdatatemp[3], disp_x / 10 * 3, disp_y / 2, 0, disp_y / 2, canvas);

				PutText(tapdatatemp[4], disp_x / 10 * 4, disp_y / 2, disp_x / 10 * 3, disp_y / 2, canvas);
				PutText(tapdatatemp[5], disp_x / 10 * 3, disp_y / 2, disp_x / 10 * 7, disp_y / 2, canvas);

				break;
			case 4:
				paint.setStyle(Paint.Style.FILL);
				// 縦線
				canvas.drawLine(disp_x / 10 * 3, 0+margin, disp_x / 10 * 3, disp_y-margin, paint);
				canvas.drawLine(disp_x / 10 * 7, 0+margin, disp_x / 10 * 7, disp_y-margin, paint);
				// 横線
				canvas.drawLine(0+margin, disp_y / 5, disp_x-margin, disp_y / 5, paint);
				canvas.drawLine(0+margin, disp_y / 5 * 4, disp_x-margin, disp_y / 5 * 4, paint);

				PutText(tapdatatemp[0], disp_x / 10 * 3, disp_y / 5, 0, 0, canvas);
				PutText(tapdatatemp[1], disp_x / 10 * 4, disp_y / 5, disp_x / 10 * 3, 0, canvas);

				PutText(tapdatatemp[2], disp_x / 10 * 3, disp_y / 5, disp_x / 10 * 7, 0, canvas);

				PutText(tapdatatemp[3], disp_x / 10 * 3, disp_y / 5 * 3, 0, disp_y / 5, canvas);
				PutText(tapdatatemp[4], disp_x / 10 * 4, disp_y / 5 * 3, disp_x / 10 * 3, disp_y / 5, canvas);

				PutText(tapdatatemp[5], disp_x / 10 * 3, disp_y / 5 * 3, disp_x / 10 * 7, disp_y / 5, canvas);

				PutText(tapdatatemp[6], disp_x / 10 * 3, disp_y / 5, 0, disp_y / 5 * 4, canvas);

				PutText(tapdatatemp[7], disp_x / 10 * 4, disp_y / 5, disp_x / 10 * 3, disp_y / 5 * 4, canvas);
				PutText(tapdatatemp[8], disp_x / 10 * 3, disp_y / 5, disp_x / 10 * 7, disp_y / 5 * 4, canvas);

				break;
			case 5:
				paint.setStyle(Paint.Style.FILL);
				// 縦線
				canvas.drawLine(disp_x / 10 * 3, 0+margin, disp_x / 10 * 3, disp_y-margin, paint);
				canvas.drawLine(disp_x / 10 * 7, 0+margin, disp_x / 10 * 7, disp_y-margin, paint);
				// 横線
				canvas.drawLine(disp_x / 10 * 3+margin, disp_y / 5 * 2, disp_x / 10 * 7-margin, disp_y / 5 * 2, paint);
				canvas.drawLine(disp_x / 10 * 3+margin, disp_y / 5 * 3, disp_x / 10 * 7-margin, disp_y / 5 * 3, paint);

				canvas.drawLine(0+margin, disp_y / 5, disp_x-margin, disp_y / 5, paint);
				canvas.drawLine(0+margin, disp_y / 5 * 4, disp_x-margin, disp_y / 5 * 4, paint);

				PutText(tapdatatemp[0], disp_x / 10 * 3, disp_y / 5, 0, 0, canvas);
				PutText(tapdatatemp[1], disp_x / 10 * 4, disp_y / 5, disp_x / 10 * 3, 0, canvas);

				PutText(tapdatatemp[2], disp_x / 10 * 3, disp_y / 5, disp_x / 10 * 7, 0, canvas);


				PutText(tapdatatemp[3], disp_x / 10 * 3, disp_y / 5 * 3, 0, disp_y / 5, canvas);

				PutText(tapdatatemp[4], disp_x / 10 * 4, disp_y / 5, disp_x / 10 * 3, disp_y / 5, canvas);
				PutText(tapdatatemp[5], disp_x / 10 * 4, disp_y / 5, disp_x / 10 * 3, disp_y / 5 * 2, canvas);
				PutText(tapdatatemp[6], disp_x / 10 * 4, disp_y / 5, disp_x / 10 * 3, disp_y / 5 * 3, canvas);

				PutText(tapdatatemp[7], disp_x / 10 * 3, disp_y / 5 * 3, disp_x / 10 * 7, disp_y / 5, canvas);

				PutText(tapdatatemp[8], disp_x / 10 * 3, disp_y / 5, 0, disp_y / 5 * 4, canvas);
				PutText(tapdatatemp[9], disp_x / 10 * 4, disp_y / 5, disp_x / 10 * 3, disp_y / 5 * 4, canvas);

				PutText(tapdatatemp[10], disp_x / 10 * 3, disp_y / 5, disp_x / 10 * 7, disp_y / 5 * 4, canvas);

				break;
			case 6:
				paint.setStyle(Paint.Style.FILL);
				// 縦線
				canvas.drawLine(disp_x / 10 * 3, disp_y / 5+margin, disp_x / 10 * 3, disp_y / 5 * 4-margin, paint);
				canvas.drawLine(disp_x / 10 * 7, disp_y / 5+margin, disp_x / 10 * 7, disp_y / 5 * 4-margin, paint);
				// 横線
				canvas.drawLine(0+margin, disp_y / 5, disp_x-margin, disp_y / 5, paint);
				canvas.drawLine(0+margin, disp_y / 5 * 4, disp_x-margin, disp_y / 5 * 4, paint);

				PutText(tapdatatemp[0], disp_x , disp_y / 5, 0, 0, canvas);

				PutText(tapdatatemp[1], disp_x / 10 * 3, disp_y / 5 * 3, 0, disp_y / 5, canvas);
				PutText(tapdatatemp[2], disp_x / 10 * 4, disp_y / 5 * 3, disp_x / 10 * 3, disp_y / 5, canvas);
				PutText(tapdatatemp[3], disp_x / 10 * 3, disp_y / 5 * 3, disp_x / 10 * 7, disp_y / 5, canvas);

				PutText(tapdatatemp[4], disp_x , disp_y / 5, 0, disp_y / 5 * 4, canvas);

				break;
			case 7:
				paint.setStyle(Paint.Style.FILL);
				// 縦線
				canvas.drawLine(disp_x / 10 * 3, disp_y / 5+margin, disp_x / 10 * 3, disp_y / 5 * 4-margin, paint);
				canvas.drawLine(disp_x / 10 * 7, disp_y / 5+margin, disp_x / 10 * 7, disp_y / 5 * 4-margin, paint);
				canvas.drawLine(disp_x / 2, 0+margin, disp_x / 2, disp_y / 5-margin, paint);
				canvas.drawLine(disp_x / 2, disp_y / 5 * 4+margin, disp_x / 2, disp_y-margin, paint);
				// 横線
				canvas.drawLine(0+margin, disp_y / 5, disp_x-margin, disp_y / 5, paint);
				canvas.drawLine(0+margin, disp_y / 5 * 4, disp_x-margin, disp_y / 5 * 4, paint);

				PutText(tapdatatemp[0], disp_x / 2, disp_y / 5, 0, 0, canvas);
				PutText(tapdatatemp[1], disp_x / 2, disp_y / 5, disp_x / 2, 0, canvas);

				PutText(tapdatatemp[2], disp_x / 10 * 3, disp_y / 5 * 3, 0, disp_y / 5, canvas);
				PutText(tapdatatemp[3], disp_x / 10 * 4, disp_y / 5 * 3, disp_x / 10 * 3, disp_y / 5, canvas);
				PutText(tapdatatemp[4], disp_x / 10 * 3, disp_y / 5 * 3, disp_x / 10 * 7, disp_y / 5, canvas);

				PutText(tapdatatemp[5], disp_x / 2, disp_y / 5, 0, disp_y / 5 * 4, canvas);
				PutText(tapdatatemp[6], disp_x / 2, disp_y / 5, disp_x / 2, disp_y / 5 * 4, canvas);

				break;
			case 8:
				paint.setStyle(Paint.Style.FILL);
				// 縦線
				canvas.drawLine(disp_x / 10 * 3, 0+margin, disp_x / 10 * 3, disp_y-margin, paint);
				canvas.drawLine(disp_x / 10 * 7, 0+margin, disp_x / 10 * 7, disp_y-margin, paint);
				// 横線
				canvas.drawLine(disp_x / 10 * 3+margin, disp_y / 5, disp_x / 10 * 7-margin, disp_y / 5, paint);
				canvas.drawLine(disp_x / 10 * 3+margin, disp_y / 5 * 4, disp_x / 10 * 7-margin, disp_y / 5 * 4, paint);

				PutText(tapdatatemp[0], disp_x / 10 * 3, disp_y, 0, 0, canvas);
				PutText(tapdatatemp[1], disp_x / 10 * 4, disp_y / 5, disp_x / 10 * 3, 0, canvas);
				PutText(tapdatatemp[2], disp_x / 10 * 3, disp_y, disp_x / 10 * 7, 0, canvas);

				PutText(tapdatatemp[3], disp_x / 10 * 4, disp_y / 5 * 3, disp_x / 10 * 3, disp_y / 5, canvas);
				PutText(tapdatatemp[4], disp_x / 10 * 4, disp_y / 5, disp_x / 10 * 3, disp_y / 5 * 4, canvas);

				break;
			case 9:
				paint.setStyle(Paint.Style.FILL);
				// 縦線
				canvas.drawLine(disp_x / 10 * 3, 0+margin, disp_x / 10 * 3, disp_y-margin, paint);
				canvas.drawLine(disp_x / 10 * 7, 0+margin, disp_x / 10 * 7, disp_y-margin, paint);
				// 横線
				canvas.drawLine(disp_x / 10 * 3+margin, disp_y / 5, disp_x / 10 * 7-margin, disp_y / 5, paint);
				canvas.drawLine(disp_x / 10 * 3+margin, disp_y / 5 * 4, disp_x / 10 * 7-margin, disp_y / 5 * 4, paint);
				canvas.drawLine(0+margin, disp_y / 2, disp_x / 10 * 3-margin, disp_y / 2, paint);
				canvas.drawLine(disp_x / 10 * 7+margin, disp_y / 2, disp_x-margin, disp_y / 2, paint);

				PutText(tapdatatemp[0], disp_x / 10 * 3, disp_y / 2, 0, 0, canvas);
				PutText(tapdatatemp[1], disp_x / 10 * 4, disp_y / 5, disp_x / 10 * 3, 0, canvas);
				PutText(tapdatatemp[2], disp_x / 10 * 3, disp_y / 2, disp_x / 10 * 7, 0, canvas);

				PutText(tapdatatemp[3], disp_x / 10 * 4, disp_y / 5 * 3, disp_x / 10 * 3, disp_y / 5, canvas);
				PutText(tapdatatemp[4], disp_x / 10 * 3, disp_y / 2, 0, disp_y / 2, canvas);
				PutText(tapdatatemp[5], disp_x / 10 * 4, disp_y / 5, disp_x / 10 * 3, disp_y / 5 * 4, canvas);
				PutText(tapdatatemp[6], disp_x / 10 * 3, disp_y / 2, disp_x / 10 * 7, disp_y / 2, canvas);

				break;
		}

		// 上部に操作説明を描画
		int pagecy = disp_y / 20;	// ページ数表示の高さ

		paint.setColor(0xff408040);
		paint.setAlpha(0x80);
		canvas.drawRect(0, 0, disp_x, pagecy, paint);

		TextPaint textpaint = new TextPaint();
		float fontsize = 36f;
		textpaint.setTextSize(fontsize);
		int textWidth;
		int text_x;
		int text_y;
		String mestext;

		textpaint.setColor(0xffffffff);
		textpaint.setAlpha(0xff);
		textpaint.setStyle(Paint.Style.FILL);

		Resources res = mContext.getResources();
		if (disp_x > disp_y) {
			// 横画面
			// テキストを取り出す
			mestext = res.getString(R.string.SettingTapClick);
			mestext += (" : " + res.getString(TapClickString[clickmode]));
			mestext += (" " + res.getString(R.string.MesTapHelpMes));
			// テキストの横幅を取得
			textWidth = (int)textpaint.measureText(mestext);
			// 中心を求める
			text_x = (disp_x - textWidth) / 2 + margin + 0;
			// 中心を求める
			text_y = (pagecy + (int)fontsize) / 2;
			canvas.drawText(mestext, text_x, text_y, textpaint);
		}
		else {
			// 縦画面
			// テキストを取り出す
			mestext = res.getString(R.string.SettingTapClick);
			mestext += (" : " + res.getString(TapClickString[clickmode]));
			// テキストの横幅を取得
			textWidth = (int)textpaint.measureText(mestext);
			// 中心を求める
			text_x = (disp_x - textWidth) / 2 + margin + 0;
			// 中心を求める
			text_y = (pagecy + (int)fontsize) / 2 - (int)fontsize / 2;
			canvas.drawText(mestext, text_x, text_y, textpaint);

			mestext = res.getString(R.string.MesTapHelpMes);
			// テキストの横幅を取得
			textWidth = (int)textpaint.measureText(mestext);
			// 中心を求める
			text_x = (disp_x - textWidth) / 2 + margin + 0;
			// 中心を求める
			text_y = (pagecy + (int)fontsize) / 2 + (int)fontsize / 2;
			canvas.drawText(mestext, text_x, text_y, textpaint);
		}

	}

	// テキストを表示
	private static void PutText(int index, int width, int height, int x_offset, int y_offset, Canvas canvas) {

		int text_x;
		int text_y;
		int textWidth;
		int margin = 10;
		float fontsize = 50f;
		width -= margin * 2;
		String text;

		TextPaint textpaint = new TextPaint();
		// 最大輝度
		textpaint.setColor(0xffffffff);
		// フォントサイズを設定
		textpaint.setTextSize(fontsize);
		textpaint.setStyle(Paint.Style.FILL);

		TextPaint textpaintstroke = new TextPaint();
		// 最小輝度
		textpaintstroke.setColor(0xff000000);
		// フォントサイズを設定
		textpaintstroke.setTextSize(fontsize);
		// 縁取り設定
		textpaintstroke.setStyle(Paint.Style.STROKE);
		textpaintstroke.setStrokeWidth(2);

		SetProfileWord();
		Resources res = mContext.getResources();
		// テキストを取り出す
		if (index >= DEF.TAP_PROFILE1 && index <= DEF.TAP_PROFILE5 && !mProfileWord[index - DEF.TAP_PROFILE1].equals("")) {
			text = mProfileWord[index - DEF.TAP_PROFILE1];
		}
		else if (index >= DEF.TAP_PROFILE6 && index <= DEF.TAP_PROFILE10 && !mProfileWord[index - DEF.TAP_PROFILE6 + 5].equals("")) {
			text = mProfileWord[index - DEF.TAP_PROFILE6 + 5];
		}
		else {
			text = res.getString(HardwareKeyName[index]);
		}

		// 最初にStaticLayoutを作る
		// widthは描画領域の幅
		// 1行の文字列がこの幅を超えると自動で折り返す
		StaticLayout staticLayout = new StaticLayout(text, textpaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
		// 行数を取得
		int lineCount = staticLayout.getLineCount();

		// テキストの横幅を取得
		textWidth = (int)textpaint.measureText(text);

		if (textWidth > width) {
			// 折り返す場合は先頭から表示
			text_x = 0 + margin + x_offset;
		}
		else {
			// 中心を求める
			text_x = (width - textWidth) / 2 + margin + x_offset;
		}
		// 中心を求める
		text_y = height / 2 - (int)fontsize * lineCount / 2 + y_offset;

		StaticLayout staticLayoutstroke = new StaticLayout(text, textpaintstroke, width, Layout.Alignment.ALIGN_NORMAL, 1, 0, true);

		// staticLayout.drawで描画する
		// 座標を指定できないので、まずCanvasの座標を動かしてから描画
		// 縁取りを先に描画
		canvas.save();
		canvas.translate(text_x, text_y);
		staticLayoutstroke.draw(canvas);
		canvas.restore();
		// その上に文字を描画
		canvas.save();
		canvas.translate(text_x, text_y);
		staticLayout.draw(canvas);
		canvas.restore();
	}

	// タップ操作のパターンの内容を読み出す(イメージビューア)
	public static void LoadTapPatternData() {
		tappattern = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_I_PATTERN_NUMBER, 0);
		switch (tappattern) {
			case 0:
				break;
			case 1:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_01_01, DEF.TAP_PATTERN_I01_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_01_02, DEF.TAP_PATTERN_I01_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_01_03, DEF.TAP_PATTERN_I01_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_01_04, DEF.TAP_PATTERN_I01_DEFAULT_04);
				
				break;
			case 2:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_02_01, DEF.TAP_PATTERN_I02_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_02_02, DEF.TAP_PATTERN_I02_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_02_03, DEF.TAP_PATTERN_I02_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_02_04, DEF.TAP_PATTERN_I02_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_02_05, DEF.TAP_PATTERN_I02_DEFAULT_05);
				tapdata[5] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_02_06, DEF.TAP_PATTERN_I02_DEFAULT_06);
				
				break;
			case 3:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_03_01, DEF.TAP_PATTERN_I03_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_03_02, DEF.TAP_PATTERN_I03_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_03_03, DEF.TAP_PATTERN_I03_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_03_04, DEF.TAP_PATTERN_I03_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_03_05, DEF.TAP_PATTERN_I03_DEFAULT_05);
				tapdata[5] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_03_06, DEF.TAP_PATTERN_I03_DEFAULT_06);
				
				break;
			case 4:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_04_01, DEF.TAP_PATTERN_I04_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_04_02, DEF.TAP_PATTERN_I04_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_04_03, DEF.TAP_PATTERN_I04_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_04_04, DEF.TAP_PATTERN_I04_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_04_05, DEF.TAP_PATTERN_I04_DEFAULT_05);
				tapdata[5] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_04_06, DEF.TAP_PATTERN_I04_DEFAULT_06);
				tapdata[6] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_04_07, DEF.TAP_PATTERN_I04_DEFAULT_07);
				tapdata[7] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_04_08, DEF.TAP_PATTERN_I04_DEFAULT_08);
				tapdata[8] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_04_09, DEF.TAP_PATTERN_I04_DEFAULT_09);
				
				break;
			case 5:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_05_01, DEF.TAP_PATTERN_I05_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_05_02, DEF.TAP_PATTERN_I05_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_05_03, DEF.TAP_PATTERN_I05_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_05_04, DEF.TAP_PATTERN_I05_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_05_05, DEF.TAP_PATTERN_I05_DEFAULT_05);
				tapdata[5] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_05_06, DEF.TAP_PATTERN_I05_DEFAULT_06);
				tapdata[6] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_05_07, DEF.TAP_PATTERN_I05_DEFAULT_07);
				tapdata[7] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_05_08, DEF.TAP_PATTERN_I05_DEFAULT_08);
				tapdata[8] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_05_09, DEF.TAP_PATTERN_I05_DEFAULT_09);
				tapdata[9] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_05_10, DEF.TAP_PATTERN_I05_DEFAULT_10);
				tapdata[10] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_05_11, DEF.TAP_PATTERN_I05_DEFAULT_11);
				
				break;
			case 6:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_06_01, DEF.TAP_PATTERN_I06_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_06_02, DEF.TAP_PATTERN_I06_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_06_03, DEF.TAP_PATTERN_I06_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_06_04, DEF.TAP_PATTERN_I06_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_06_05, DEF.TAP_PATTERN_I06_DEFAULT_05);
				
				break;
			case 7:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_07_01, DEF.TAP_PATTERN_I07_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_07_02, DEF.TAP_PATTERN_I07_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_07_03, DEF.TAP_PATTERN_I07_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_07_04, DEF.TAP_PATTERN_I07_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_07_05, DEF.TAP_PATTERN_I07_DEFAULT_05);
				tapdata[5] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_07_06, DEF.TAP_PATTERN_I07_DEFAULT_06);
				tapdata[6] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_07_07, DEF.TAP_PATTERN_I07_DEFAULT_07);
				
				break;
			case 8:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_08_01, DEF.TAP_PATTERN_I08_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_08_02, DEF.TAP_PATTERN_I08_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_08_03, DEF.TAP_PATTERN_I08_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_08_04, DEF.TAP_PATTERN_I08_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_08_05, DEF.TAP_PATTERN_I08_DEFAULT_05);
				
				break;
			case 9:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_09_01, DEF.TAP_PATTERN_I09_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_09_02, DEF.TAP_PATTERN_I09_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_09_03, DEF.TAP_PATTERN_I09_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_09_04, DEF.TAP_PATTERN_I09_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_09_05, DEF.TAP_PATTERN_I09_DEFAULT_05);
				tapdata[5] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_09_06, DEF.TAP_PATTERN_I09_DEFAULT_06);
				tapdata[6] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_09_07, DEF.TAP_PATTERN_I09_DEFAULT_07);
				
				break;
		}
	}

	// タップ操作のパターンの内容を読み出す(テキストビューア)
	public static void LoadTapPatternTxtData() {
		tappattern = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_T_PATTERN_NUMBER, 0);
		switch (tappattern) {
			case 0:
				break;
			case 1:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_01_01, DEF.TAP_PATTERN_T01_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_01_02, DEF.TAP_PATTERN_T01_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_01_03, DEF.TAP_PATTERN_T01_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_01_04, DEF.TAP_PATTERN_T01_DEFAULT_04);
				
				break;
			case 2:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_02_01, DEF.TAP_PATTERN_T02_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_02_02, DEF.TAP_PATTERN_T02_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_02_03, DEF.TAP_PATTERN_T02_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_02_04, DEF.TAP_PATTERN_T02_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_02_05, DEF.TAP_PATTERN_T02_DEFAULT_05);
				tapdata[5] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_02_06, DEF.TAP_PATTERN_T02_DEFAULT_06);
				
				break;
			case 3:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_03_01, DEF.TAP_PATTERN_T03_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_03_02, DEF.TAP_PATTERN_T03_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_03_03, DEF.TAP_PATTERN_T03_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_03_04, DEF.TAP_PATTERN_T03_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_03_05, DEF.TAP_PATTERN_T03_DEFAULT_05);
				tapdata[5] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_03_06, DEF.TAP_PATTERN_T03_DEFAULT_06);
				
				break;
			case 4:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_04_01, DEF.TAP_PATTERN_T04_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_04_02, DEF.TAP_PATTERN_T04_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_04_03, DEF.TAP_PATTERN_T04_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_04_04, DEF.TAP_PATTERN_T04_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_04_05, DEF.TAP_PATTERN_T04_DEFAULT_05);
				tapdata[5] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_04_06, DEF.TAP_PATTERN_T04_DEFAULT_06);
				tapdata[6] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_04_07, DEF.TAP_PATTERN_T04_DEFAULT_07);
				tapdata[7] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_04_08, DEF.TAP_PATTERN_T04_DEFAULT_08);
				tapdata[8] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_04_09, DEF.TAP_PATTERN_T04_DEFAULT_09);
				
				break;
			case 5:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_05_01, DEF.TAP_PATTERN_T05_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_05_02, DEF.TAP_PATTERN_T05_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_05_03, DEF.TAP_PATTERN_T05_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_05_04, DEF.TAP_PATTERN_T05_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_05_05, DEF.TAP_PATTERN_T05_DEFAULT_05);
				tapdata[5] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_05_06, DEF.TAP_PATTERN_T05_DEFAULT_06);
				tapdata[6] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_05_07, DEF.TAP_PATTERN_T05_DEFAULT_07);
				tapdata[7] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_05_08, DEF.TAP_PATTERN_T05_DEFAULT_08);
				tapdata[8] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_05_09, DEF.TAP_PATTERN_T05_DEFAULT_09);
				tapdata[9] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_05_10, DEF.TAP_PATTERN_T05_DEFAULT_10);
				tapdata[10] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_05_11, DEF.TAP_PATTERN_T05_DEFAULT_11);
				
				break;
			case 6:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_06_01, DEF.TAP_PATTERN_T06_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_06_02, DEF.TAP_PATTERN_T06_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_06_03, DEF.TAP_PATTERN_T06_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_06_04, DEF.TAP_PATTERN_T06_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_06_05, DEF.TAP_PATTERN_T06_DEFAULT_05);
				
				break;
			case 7:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_07_01, DEF.TAP_PATTERN_T07_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_07_02, DEF.TAP_PATTERN_T07_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_07_03, DEF.TAP_PATTERN_T07_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_07_04, DEF.TAP_PATTERN_T07_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_07_05, DEF.TAP_PATTERN_T07_DEFAULT_05);
				tapdata[5] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_07_06, DEF.TAP_PATTERN_T07_DEFAULT_06);
				tapdata[6] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_07_07, DEF.TAP_PATTERN_T07_DEFAULT_07);
				
				break;
			case 8:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_08_01, DEF.TAP_PATTERN_T08_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_08_02, DEF.TAP_PATTERN_T08_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_08_03, DEF.TAP_PATTERN_T08_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_08_04, DEF.TAP_PATTERN_T08_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_08_05, DEF.TAP_PATTERN_T08_DEFAULT_05);
				
				break;
			case 9:
				tapdata[0] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_09_01, DEF.TAP_PATTERN_T09_DEFAULT_01);
				tapdata[1] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_09_02, DEF.TAP_PATTERN_T09_DEFAULT_02);
				tapdata[2] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_09_03, DEF.TAP_PATTERN_T09_DEFAULT_03);
				tapdata[3] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_09_04, DEF.TAP_PATTERN_T09_DEFAULT_04);
				tapdata[4] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_09_05, DEF.TAP_PATTERN_T09_DEFAULT_05);
				tapdata[5] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_09_06, DEF.TAP_PATTERN_T09_DEFAULT_06);
				tapdata[6] = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_PATTERN_T_09_07, DEF.TAP_PATTERN_T09_DEFAULT_07);
				
				break;
		}
	}

	// タップ操作のパターンの内容を書きこむ(イメージビューア)
	private static void SaveTapPatternData() {
		tappattern = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_I_PATTERN_NUMBER, 0);
		Editor ed = mSharedPreferences.edit();

		switch (tappattern) {
			case 0:
				break;
			case 1:
				ed.putInt(DEF.KEY_TAP_PATTERN_01_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_01_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_01_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_01_04, tapdata[3]);
				ed.apply();
				break;
			case 2:
				ed.putInt(DEF.KEY_TAP_PATTERN_02_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_02_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_02_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_02_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_02_05, tapdata[4]);
				ed.putInt(DEF.KEY_TAP_PATTERN_02_06, tapdata[5]);
				ed.apply();
				break;
			case 3:
				ed.putInt(DEF.KEY_TAP_PATTERN_03_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_03_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_03_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_03_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_03_05, tapdata[4]);
				ed.putInt(DEF.KEY_TAP_PATTERN_03_06, tapdata[5]);
				ed.apply();
				break;
			case 4:
				ed.putInt(DEF.KEY_TAP_PATTERN_04_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_04_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_04_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_04_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_04_05, tapdata[4]);
				ed.putInt(DEF.KEY_TAP_PATTERN_04_06, tapdata[5]);
				ed.putInt(DEF.KEY_TAP_PATTERN_04_07, tapdata[6]);
				ed.putInt(DEF.KEY_TAP_PATTERN_04_08, tapdata[7]);
				ed.putInt(DEF.KEY_TAP_PATTERN_04_09, tapdata[8]);
				ed.apply();
				break;
			case 5:
				ed.putInt(DEF.KEY_TAP_PATTERN_05_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_05_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_05_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_05_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_05_05, tapdata[4]);
				ed.putInt(DEF.KEY_TAP_PATTERN_05_06, tapdata[5]);
				ed.putInt(DEF.KEY_TAP_PATTERN_05_07, tapdata[6]);
				ed.putInt(DEF.KEY_TAP_PATTERN_05_08, tapdata[7]);
				ed.putInt(DEF.KEY_TAP_PATTERN_05_09, tapdata[8]);
				ed.putInt(DEF.KEY_TAP_PATTERN_05_10, tapdata[9]);
				ed.putInt(DEF.KEY_TAP_PATTERN_05_11, tapdata[10]);
				ed.apply();
				break;
			case 6:
				ed.putInt(DEF.KEY_TAP_PATTERN_06_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_06_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_06_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_06_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_06_05, tapdata[4]);
				ed.apply();
				break;
			case 7:
				ed.putInt(DEF.KEY_TAP_PATTERN_07_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_07_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_07_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_07_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_07_05, tapdata[4]);
				ed.putInt(DEF.KEY_TAP_PATTERN_07_06, tapdata[5]);
				ed.putInt(DEF.KEY_TAP_PATTERN_07_07, tapdata[6]);
				ed.apply();
				break;
			case 8:
				ed.putInt(DEF.KEY_TAP_PATTERN_08_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_08_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_08_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_08_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_08_05, tapdata[4]);
				ed.apply();
				break;
			case 9:
				ed.putInt(DEF.KEY_TAP_PATTERN_09_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_09_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_09_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_09_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_09_05, tapdata[4]);
				ed.putInt(DEF.KEY_TAP_PATTERN_09_06, tapdata[5]);
				ed.putInt(DEF.KEY_TAP_PATTERN_09_07, tapdata[6]);
				ed.apply();
				break;
		}
	}

	// タップ操作のパターンの内容を書きこむ(テキストビューア)
	private static void SaveTapPatternTxtData() {
		tappattern = DEF.getInt(mSharedPreferences, DEF.KEY_TAP_T_PATTERN_NUMBER, 0);
		Editor ed = mSharedPreferences.edit();

		switch (tappattern) {
			case 0:
				break;
			case 1:
				ed.putInt(DEF.KEY_TAP_PATTERN_T_01_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_01_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_01_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_01_04, tapdata[3]);
				ed.apply();
				break;
			case 2:
				ed.putInt(DEF.KEY_TAP_PATTERN_T_02_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_02_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_02_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_02_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_02_05, tapdata[4]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_02_06, tapdata[5]);
				ed.apply();
				break;
			case 3:
				ed.putInt(DEF.KEY_TAP_PATTERN_T_03_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_03_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_03_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_03_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_03_05, tapdata[4]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_03_06, tapdata[5]);
				ed.apply();
				break;
			case 4:
				ed.putInt(DEF.KEY_TAP_PATTERN_T_04_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_04_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_04_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_04_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_04_05, tapdata[4]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_04_06, tapdata[5]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_04_07, tapdata[6]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_04_08, tapdata[7]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_04_09, tapdata[8]);
				ed.apply();
				break;
			case 5:
				ed.putInt(DEF.KEY_TAP_PATTERN_T_05_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_05_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_05_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_05_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_05_05, tapdata[4]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_05_06, tapdata[5]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_05_07, tapdata[6]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_05_08, tapdata[7]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_05_09, tapdata[8]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_05_10, tapdata[9]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_05_11, tapdata[10]);
				ed.apply();
				break;
			case 6:
				ed.putInt(DEF.KEY_TAP_PATTERN_T_06_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_06_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_06_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_06_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_06_05, tapdata[4]);
				ed.apply();
				break;
			case 7:
				ed.putInt(DEF.KEY_TAP_PATTERN_T_07_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_07_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_07_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_07_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_07_05, tapdata[4]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_07_06, tapdata[5]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_07_07, tapdata[6]);
				ed.apply();
				break;
			case 8:
				ed.putInt(DEF.KEY_TAP_PATTERN_T_08_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_08_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_08_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_08_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_08_05, tapdata[4]);
				ed.apply();
				break;
			case 9:
				ed.putInt(DEF.KEY_TAP_PATTERN_T_09_01, tapdata[0]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_09_02, tapdata[1]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_09_03, tapdata[2]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_09_04, tapdata[3]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_09_05, tapdata[4]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_09_06, tapdata[5]);
				ed.putInt(DEF.KEY_TAP_PATTERN_T_09_07, tapdata[6]);
				ed.apply();
				break;
		}
	}

	public static int LoadCustomkeyCode(SharedPreferences sharedpreferences, int index) {
		int data = 0;
		switch (index) {
			case 0:
				data = DEF.getInt(sharedpreferences, DEF.KEY_CUSTOMKEY_CODE_01, 0);
				break;
			case 1:
				data = DEF.getInt(sharedpreferences, DEF.KEY_CUSTOMKEY_CODE_02, 0);
				break;
			case 2:
				data = DEF.getInt(sharedpreferences, DEF.KEY_CUSTOMKEY_CODE_03, 0);
				break;
			case 3:
				data = DEF.getInt(sharedpreferences, DEF.KEY_CUSTOMKEY_CODE_04, 0);
				break;
			case 4:
				data = DEF.getInt(sharedpreferences, DEF.KEY_CUSTOMKEY_CODE_05, 0);
				break;
			case 5:
				data = DEF.getInt(sharedpreferences, DEF.KEY_CUSTOMKEY_CODE_06, 0);
				break;
			case 6:
				data = DEF.getInt(sharedpreferences, DEF.KEY_CUSTOMKEY_CODE_07, 0);
				break;
			case 7:
				data = DEF.getInt(sharedpreferences, DEF.KEY_CUSTOMKEY_CODE_08, 0);
				break;
			case 8:
				data = DEF.getInt(sharedpreferences, DEF.KEY_CUSTOMKEY_CODE_09, 0);
				break;
			case 9:
				data = DEF.getInt(sharedpreferences, DEF.KEY_CUSTOMKEY_CODE_10, 0);
				break;
		}
		return data;
	}

	public static void SaveCustomkeyCode(SharedPreferences sharedpreferences, int code, int index) {
		Editor ed = sharedpreferences.edit();

		switch (index) {
			case 0:
				ed.putInt(DEF.KEY_CUSTOMKEY_CODE_01, code);
				ed.apply();
				break;
			case 1:
				ed.putInt(DEF.KEY_CUSTOMKEY_CODE_02, code);
				ed.apply();
				break;
			case 2:
				ed.putInt(DEF.KEY_CUSTOMKEY_CODE_03, code);
				ed.apply();
				break;
			case 3:
				ed.putInt(DEF.KEY_CUSTOMKEY_CODE_04, code);
				ed.apply();
				break;
			case 4:
				ed.putInt(DEF.KEY_CUSTOMKEY_CODE_05, code);
				ed.apply();
				break;
			case 5:
				ed.putInt(DEF.KEY_CUSTOMKEY_CODE_06, code);
				ed.apply();
				break;
			case 6:
				ed.putInt(DEF.KEY_CUSTOMKEY_CODE_07, code);
				ed.apply();
				break;
			case 7:
				ed.putInt(DEF.KEY_CUSTOMKEY_CODE_08, code);
				ed.apply();
				break;
			case 8:
				ed.putInt(DEF.KEY_CUSTOMKEY_CODE_09, code);
				ed.apply();
				break;
			case 9:
				ed.putInt(DEF.KEY_CUSTOMKEY_CODE_10, code);
				ed.apply();
				break;
		}
	}

	public static String LoadCustomkeyTitle(SharedPreferences sharedpreferences, int index) {
		String data = "";
		switch (index) {
			case 0:
				data = sharedpreferences.getString(DEF.KEY_CUSTOMKEY_TITLE_01, "");
				break;
			case 1:
				data = sharedpreferences.getString(DEF.KEY_CUSTOMKEY_TITLE_02, "");
				break;
			case 2:
				data = sharedpreferences.getString(DEF.KEY_CUSTOMKEY_TITLE_03, "");
				break;
			case 3:
				data = sharedpreferences.getString(DEF.KEY_CUSTOMKEY_TITLE_04, "");
				break;
			case 4:
				data = sharedpreferences.getString(DEF.KEY_CUSTOMKEY_TITLE_05, "");
				break;
			case 5:
				data = sharedpreferences.getString(DEF.KEY_CUSTOMKEY_TITLE_06, "");
				break;
			case 6:
				data = sharedpreferences.getString(DEF.KEY_CUSTOMKEY_TITLE_07, "");
				break;
			case 7:
				data = sharedpreferences.getString(DEF.KEY_CUSTOMKEY_TITLE_08, "");
				break;
			case 8:
				data = sharedpreferences.getString(DEF.KEY_CUSTOMKEY_TITLE_09, "");
				break;
			case 9:
				data = sharedpreferences.getString(DEF.KEY_CUSTOMKEY_TITLE_10, "");
				break;
		}
		return data;
	}

	public static void SaveCustomkeyTitle(SharedPreferences sharedpreferences, String data, int index) {
		Editor ed = sharedpreferences.edit();

		switch (index) {
			case 0:
				ed.putString(DEF.KEY_CUSTOMKEY_TITLE_01, data);
				ed.apply();
				break;
			case 1:
				ed.putString(DEF.KEY_CUSTOMKEY_TITLE_02, data);
				ed.apply();
				break;
			case 2:
				ed.putString(DEF.KEY_CUSTOMKEY_TITLE_03, data);
				ed.apply();
				break;
			case 3:
				ed.putString(DEF.KEY_CUSTOMKEY_TITLE_04, data);
				ed.apply();
				break;
			case 4:
				ed.putString(DEF.KEY_CUSTOMKEY_TITLE_05, data);
				ed.apply();
				break;
			case 5:
				ed.putString(DEF.KEY_CUSTOMKEY_TITLE_06, data);
				ed.apply();
				break;
			case 6:
				ed.putString(DEF.KEY_CUSTOMKEY_TITLE_07, data);
				ed.apply();
				break;
			case 7:
				ed.putString(DEF.KEY_CUSTOMKEY_TITLE_08, data);
				ed.apply();
				break;
			case 8:
				ed.putString(DEF.KEY_CUSTOMKEY_TITLE_09, data);
				ed.apply();
				break;
			case 9:
				ed.putString(DEF.KEY_CUSTOMKEY_TITLE_10, data);
				ed.apply();
				break;
		}
	}

	// フローティングアイコンの十字アイコン設定を表示
	public static void FloatingIconDrawmain(Canvas canvas, int width, int height) {

		int disp_x = width;
		int disp_y = height;
		int margin = 1;

		Paint paint = new Paint();

		// 最少輝度
		paint.setColor(0xff000000);
		// 半透明にする
		paint.setAlpha(0xc0);

		// 背景の輝度を下げる
		canvas.drawRect(0, 0, disp_x, disp_y, paint);

		// 最大輝度
		paint.setColor(0xffffffff);
		paint.setAlpha(255);
		// 外枠を表示
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(margin);
		canvas.drawRect(0+margin, 0+margin, disp_x-margin, disp_y-margin, paint);

		// 上部に操作説明を描画
		int pagecy = disp_y / 20;	// ページ数表示の高さ

		paint.setColor(0xff408040);
		paint.setAlpha(0x80);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(0, 0, disp_x, pagecy, paint);

		TextPaint textpaint = new TextPaint();
		float fontsize = 36f;
		textpaint.setTextSize(fontsize);
		int textWidth;
		int text_x;
		int text_y;
		String mestext;

		textpaint.setColor(0xffffffff);
		textpaint.setAlpha(0xff);
		textpaint.setStyle(Paint.Style.FILL);

		Resources res = mContext.getResources();

		if (disp_x > disp_y) {
			// 横画面
			// テキストを取り出す
			mestext = res.getString(R.string.cursorfloatingicon);
			mestext += (" " + res.getString(R.string.SettingCursorClick));
			// テキストの横幅を取得
			textWidth = (int)textpaint.measureText(mestext);
			// 中心を求める
			text_x = (disp_x - textWidth) / 2 + margin + 0;
			// 中心を求める
			text_y = (pagecy + (int)fontsize) / 2;
			canvas.drawText(mestext, text_x, text_y, textpaint);
		}
		else {
			// 縦画面
			// テキストを取り出す
			mestext = res.getString(R.string.cursorfloatingicon);
			// テキストの横幅を取得
			textWidth = (int)textpaint.measureText(mestext);
			// 中心を求める
			text_x = (disp_x - textWidth) / 2 + margin + 0;
			// 中心を求める
			text_y = (pagecy + (int)fontsize) / 2 - (int)fontsize / 2;
			canvas.drawText(mestext, text_x, text_y, textpaint);

			mestext = res.getString(R.string.SettingCursorClick);
			// テキストの横幅を取得
			textWidth = (int)textpaint.measureText(mestext);
			// 中心を求める
			text_x = (disp_x - textWidth) / 2 + margin + 0;
			// 中心を求める
			text_y = (pagecy + (int)fontsize) / 2 + (int)fontsize / 2;
			canvas.drawText(mestext, text_x, text_y, textpaint);
		}
	}
}

