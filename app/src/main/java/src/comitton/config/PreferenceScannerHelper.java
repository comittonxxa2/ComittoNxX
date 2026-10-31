package src.comitton.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.dip.muracoro.comittonx.R;

// 設定XMLファイルをスキャンして検索・一覧表示し、選択された項目のキー、XML名、対応するアクティビティ、および選択履歴を管理するヘルパークラス
public class PreferenceScannerHelper {

	private static final String PREF_NAME = "preference_scan_history";
	private static final String KEY_HISTORY = "history_list";
	private static final String KEY_SEARCH_SUMMARY = "search_summary_enabled";
	private static final String HISTORY_HEADER = "history_header";
	private static final String NAMESPACE = "http://schemas.android.com/apk/res/android";
	private static final String TAG_PREFERENCE = "Preference";
	private static final String TAG_PREFERENCECATEGORY = "PreferenceCategory";
	private static final String TAG_SRC = "src.comitton";
	private static final String NAME_TITLE = "title";
	private static final String NAME_SUMMARY = "summary";
	private static final String NAME_KEY = "key";
	private static final String NAME_DUMMY = "dummy";
	private static final String NAME_AT = "@";
	private static final String NAME_SPLIT = "---SEP---";

	// 履歴の最大保存件数
	private static final int MAX_HISTORY_SIZE = 10;
	// 設定項目が選択された際のコールバックインターフェース
	public interface OnPreferenceSelectedListener {
		void onPreferenceSelected(String key, Class<?> activityClass);
	}
	// 設定項目の情報(またはヘッダー行)を保持するクラス
	public static class PreferenceItemInfo {
		String tagName;
		String title;
		String summary;
		String key;
		String xmlFileName;
		String displayText;
		boolean isHeader;
		boolean isCategory;
		// 通常の設定項目用コンストラクタ
		public PreferenceItemInfo(String tagName, String title, String summary, String key, String xmlFileName, String displayText, boolean isCategory) {
			this.tagName = tagName != null ? tagName : "";
			this.title = title != null ? title : "";
			this.summary = summary != null ? summary : "";
			this.key = key != null ? key : "";
			this.xmlFileName = xmlFileName != null ? xmlFileName : "";
			this.displayText = displayText;
			this.isHeader = false;
			this.isCategory = isCategory;
		}
		// 履歴の読み込みなどで使用するコンストラクタ
		public PreferenceItemInfo(String tagName, String title, String summary, String key, String xmlFileName, String displayText) {
			this.tagName = tagName != null ? tagName : "";
			this.title = title != null ? title : "";
			this.summary = summary != null ? summary : "";
			this.key = key != null ? key : "";
			this.xmlFileName = xmlFileName != null ? xmlFileName : "";
			this.displayText = displayText;
			this.isHeader = false;
			this.isCategory = false;
		}
		// セクションヘッダー行用コンストラクタ
		public PreferenceItemInfo(String headerText, String xmlFileName) {
			this.tagName = "";
			this.title = "";
			this.summary = "";
			this.key = "";
			this.xmlFileName = xmlFileName;
			this.displayText = headerText;
			this.isHeader = true;
			this.isCategory = false;
		}

		@Override
		public String toString() {
			return displayText;
		}
	}

	// 検索キーワードに一致した部分にハイライトを適用するヘルパーメソッド
	private static void applyHighlight(SpannableString spannable, String fullText, String query) {
		if (query == null || query.isEmpty()) {
			return;
		}
		String lowerFullText = fullText.toLowerCase();
		String lowerQuery = query.toLowerCase();
		int index = lowerFullText.indexOf(lowerQuery);
		while (index >= 0) {
			int end = index + query.length();
			// 背景色(半透明の黄色)と文字色(黒)を設定
			spannable.setSpan(new BackgroundColorSpan(Color.parseColor("#88FFEE00")), index, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			spannable.setSpan(new ForegroundColorSpan(Color.BLACK), index, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			index = lowerFullText.indexOf(lowerQuery, end);
		}
	}

	// 設定検索・一覧ダイアログを表示するメインメソッド
	public static void showPreferenceScanDialog(Context context, OnPreferenceSelectedListener listener) {
		// すべてのパース済み設定アイテムを保持するリスト
		List<PreferenceItemInfo> allItems = new ArrayList<>();
		// 対象となる設定用XMLファイル名のリスト
		String[] preferenceXmlFileNames = {
			"config",
			"filelist",
			"sort",
			"recorder",
			"filecolor",
			"bookshelf",
			"themecolor",
			"image",
			"margincut",
			"cornerendimage",
			"imagedetail",
			"text",
			"cornerendtext",
			"epub",
			"cornerendepub",
			"imagetextdetail",
			"imagetextcolor",
			"common",
			"noise",
			"cache",
			"customurlscheme",
			"setservermessageblock",
			"seteverything",
			"setbookmarksync",
			"sethardwareselectkey",
			"webview",
			"webserver",
		};
		// 各XMLファイルに対応するタイトル文字列リソースのリスト
		int[] preferenceTitles = {
			R.string.config,
			R.string.filelist,
			R.string.sort,
			R.string.recorder,
			R.string.filecolor,
			R.string.bookshelf,
			R.string.themecolor,
			R.string.image,
			R.string.margincut,
			R.string.cornerendimage,
			R.string.imagedetail,
			R.string.text,
			R.string.cornerendtext,
			R.string.epub,
			R.string.cornerendepub,
			R.string.imagetextdetail,
			R.string.imagetextcolor,
			R.string.common,
			R.string.noise,
			R.string.cache,
			R.string.customurlscheme,
			R.string.setservermessageblock,
			R.string.seteverything,
			R.string.setbookmarksync,
			R.string.sethardwareselectkey,
			R.string.webview,
			R.string.webserver,
		};
		// 各XMLファイルに対応する設定画面アクティビティクラスのリスト(タップした項目へスクロール移動する)
		Class<?>[] preferenceActivityClasses = {
			SetConfigActivity.class,
			SetFileListActivity.class,
			SetSortActivity.class,
			SetRecorderActivity.class,
			SetFileColorActivity.class,
			SetBookShelfActivity.class,
			SetThemeActivity.class,
			SetImageActivity.class,
			SetMarginCutActivity.class,
			SetCornerEndImageViewerActivity.class,
			SetImageDetailActivity.class,
			SetTextActivity.class,
			SetCornerEndTextViewerActivity.class,
			SetEpubActivity.class,
			SetCornerEndEpubWebViewActivity.class,
			SetImageTextDetailActivity.class,
			SetImageTextColorActivity.class,
			SetCommonActivity.class,
			SetNoiseActivity.class,
			SetCacheActivity.class,
			SetCustomUrlSchemeActivity.class,
			SetServerMessageBlockActivity.class,
			SetEverythingActivity.class,
			SetBookmarkSyncActivity.class,
			SetHardwareSelectKeyActivity.class,
			SetWebViewActivity.class,
			SetWebServerActivity.class,
		};
		// 効率的なルックアップのためのマップを作成(XML名 -> アクティビティ / タイトル)
		Map<String, Class<?>> xmlToActivityMap = new HashMap<>();
		Map<String, String> xmlToTitleMap = new HashMap<>();

		for (int i = 0; i < preferenceXmlFileNames.length; i++) {
			String fileName = preferenceXmlFileNames[i];
			if (i < preferenceActivityClasses.length) {
				xmlToActivityMap.put(fileName, preferenceActivityClasses[i]);
			}
			if (i < preferenceTitles.length) {
				xmlToTitleMap.put(fileName, context.getString(preferenceTitles[i]));
			}
		}
		// 初期スキャン：各設定XMLファイルをパースして項目を全件取得する
		try {
			for (String fileName : preferenceXmlFileNames) {
				int resId = context.getResources().getIdentifier(fileName, "xml", context.getPackageName());
				if (resId != 0) {
					parseXmlPreferences(context, resId, fileName, allItems);
				}
				else {
					String titleName = xmlToTitleMap.containsKey(fileName) ? xmlToTitleMap.get(fileName) : fileName;
					allItems.add(new PreferenceItemInfo("--- " + titleName + " (" + context.getString(R.string.notfound) + ") ---", fileName));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		float scale = context.getResources().getDisplayMetrics().density;
		// カラー設定
		int textColor = Color.WHITE;
		int hintColor = Color.LTGRAY;
		// XMLレイアウトファイルを展開する
		View layout = LayoutInflater.from(context).inflate(R.layout.dialog_preference_scanner, null);
		// 各ビューをIDから取得
		EditText searchBox = layout.findViewById(R.id.searchBox);
		Button clearButton = layout.findViewById(R.id.clearButton);
		ListView listView = layout.findViewById(R.id.listView);
		CheckBox searchSummaryCheckBox = layout.findViewById(R.id.searchSummaryCheckBox);
		Button closeButton = layout.findViewById(R.id.closeButton);
		// 検索ボックスの文字色等の設定
		searchBox.setTextColor(textColor);
		searchBox.setHintTextColor(hintColor);
		// SharedPreferencesからチェック状態を復元(デフォルトは true)
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		boolean searchSummaryEnabled = prefs.getBoolean(KEY_SEARCH_SUMMARY, true);
		searchSummaryCheckBox.setChecked(searchSummaryEnabled);
		// ソフトウェアキーボードの「検索」または「完了」アクション時のリスナー
		searchBox.setOnEditorActionListener((v, actionId, event) -> {
			// 検索ボタン、完了ボタン、またはエンターキーが押されたか判定
			if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
				// ソフトウェアキーボードを隠す
				InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
				}
				// EditTextのフォーカスを外す
				searchBox.clearFocus();
				// イベントを消費した(処理完了)ことを伝える
				return true;
			}
			return false;
		});
		// 背景タッチ時にフォーカスを外しキーボードを非表示にする
		layout.setOnTouchListener((v, event) -> {
			// 検索ボックスからフォーカスを外し、キーボードを非表示にする
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
			}
			searchBox.clearFocus();
			// falseを返すことで、リストのスクロールやタップなどの通常のタッチイベントを阻害しないようにする
			return false;
		});

		List<PreferenceItemInfo> displayList = new ArrayList<>();

		ArrayAdapter<PreferenceItemInfo> adapter = new ArrayAdapter<PreferenceItemInfo>(context, android.R.layout.simple_list_item_1, displayList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textView = view.findViewById(android.R.id.text1);
				PreferenceItemInfo item = getItem(position);

				if (item != null) {
					if (item.isHeader) {
						// 項目の見出しと履歴は色を変える
						if (HISTORY_HEADER.equals(item.xmlFileName)) {
							textView.setTextColor(Color.MAGENTA);
						}
						else {
							textView.setTextColor(Color.CYAN);
						}
						textView.setText(item.displayText);
					}
					else if (item.isCategory) {
						// カテゴリーの場合は色を変える
						textView.setTextColor(Color.YELLOW);
						textView.setText(item.displayText);
					}
					else {
						// 検索クエリを取り出す
						String currentQuery = searchBox.getText().toString().trim();
						// 通常項目：title と summary がある場合は色を分ける
						boolean showSummary = searchSummaryCheckBox.isChecked() && item.summary != null && !item.summary.isEmpty();

						if (showSummary) {
							// 改行で区切る
							String fullText = item.title + "\n" + item.summary;
							SpannableString spannable = new SpannableString(fullText);
							// summary の開始位置と終了位置を計算
							// 改行文字を含める
							int start = item.title.length() + 1;
							int end = fullText.length();
							// summary部分をグレーにする
							spannable.setSpan(new ForegroundColorSpan(Color.GRAY), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
							// 検索キーワードに一致した部分をハイライト
							applyHighlight(spannable, fullText, currentQuery);
							// タイトルは通常の文字色
							textView.setTextColor(textColor);
							textView.setText(spannable);
						} 
						else {
							SpannableString spannable = new SpannableString(item.title);
							// 検索キーワードに一致した部分をハイライト
							applyHighlight(spannable, item.title, currentQuery);
							// タイトルは通常の文字色
							textView.setTextColor(textColor);
							textView.setText(spannable);
						}
					}
				}
				return view;
			}
		};
		listView.setAdapter(adapter);

		// 検索クエリおよびチェック状態に応じて表示リストを再構築し、アダプターを更新するメソッド
		Runnable updateList = () -> {
			String query = searchBox.getText().toString().toLowerCase().trim();
			boolean includeSummary = searchSummaryCheckBox.isChecked();
			List<PreferenceItemInfo> newList = new ArrayList<>();

			// 選択履歴の読み込みとフィルタリング
			List<PreferenceItemInfo> rawHistory = loadHistory(context);
			List<PreferenceItemInfo> matchedHistory = new ArrayList<>();
			for (PreferenceItemInfo item : rawHistory) {
				boolean match = false;
				if (query.isEmpty()) {
					// クエリが空白の場合は無条件でヒットさせる
					match = true;
				}
				else {
					// タイトルにクエリが含まれていればヒットさせる
					match = item.title.toLowerCase().contains(query);
					if (!match && includeSummary && !item.summary.isEmpty()) {
						// summaryにクエリが含まれていればヒットさせる
						match = item.summary.toLowerCase().contains(query);
					}
				}
				// ヒットした項目を追加
				if (match) {
					matchedHistory.add(item);
				}
			}
			// 履歴に一致する項目がある場合、最上部に「履歴」セクションを追加
			if (!matchedHistory.isEmpty()) {
				newList.add(new PreferenceItemInfo("--- " + context.getString(R.string.history) + " ---", HISTORY_HEADER));
				newList.addAll(matchedHistory);
			}
			// 各XMLファイルごとのグループ分けとフィルタリング
			Map<String, List<PreferenceItemInfo>> fileGroups = new LinkedHashMap<>();
			for (String fileName : preferenceXmlFileNames) {
				fileGroups.put(fileName, new ArrayList<>());
			}
			// 見つからなかったファイルなどのためのフォールバック用
			List<PreferenceItemInfo> otherItems = new ArrayList<>();

			for (PreferenceItemInfo item : allItems) {
				if (item.xmlFileName != null && fileGroups.containsKey(item.xmlFileName)) {
					fileGroups.get(item.xmlFileName).add(item);
				}
				else {
					otherItems.add(item);
				}
			}
			// 各ファイルごとにクエリにヒットする項目を抽出
			for (Map.Entry<String, List<PreferenceItemInfo>> entry : fileGroups.entrySet()) {
				String fileName = entry.getKey();
				List<PreferenceItemInfo> items = entry.getValue();

				List<PreferenceItemInfo> matchedItems = new ArrayList<>();
				for (PreferenceItemInfo item : items) {
					// クエリに一致するものを判定
					boolean match = false;
					if (query.isEmpty()) {
						// クエリが空白の場合は無条件でヒットさせる
						match = true;
					}
					else {
						if (item.isHeader) {
							// 項目の見出しと履歴は無条件でヒットさせる
							match = true;
						}
						else {
							// タイトルにクエリが含まれていればヒットさせる
							match = item.title.toLowerCase().contains(query);
							if (!match && includeSummary && !item.summary.isEmpty()) {
								// summaryにクエリが含まれていればヒットさせる
								match = item.summary.toLowerCase().contains(query);
							}
						}
					}
					// ヒットした項目を追加
					if (match) {
						matchedItems.add(item);
					}
				}
				// ヒットした項目が存在する場合のみヘッダーとアイテムを追加
				if (!matchedItems.isEmpty()) {
					String headerTitle = xmlToTitleMap.containsKey(fileName) ? xmlToTitleMap.get(fileName) : fileName;
					newList.add(new PreferenceItemInfo("--- " + headerTitle + " ---", fileName));
					newList.addAll(matchedItems);
				}
			}
			// その他のフォールバック項目を追加
			for (PreferenceItemInfo item : otherItems) {
				newList.add(item);
			}
			// アダプターのデータを一括更新
			adapter.clear();
			adapter.addAll(newList);
			adapter.notifyDataSetChanged();
		};
		// 初回表示用リストの構築を実行
		updateList.run();

		// チェックボックスの状態が変更されたときの処理
		searchSummaryCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefs.edit().putBoolean(KEY_SEARCH_SUMMARY, isChecked).apply();
			// 表示リストを再構築
			updateList.run();
		});

		// アラートダイアログの生成と設定
		AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
		// タイトル
		builder.setTitle(R.string.listPreferenceScanner);
		builder.setView(layout);
		// アラートダイアログを作成
		AlertDialog dialog = builder.create();
		// カスタムレイアウト内の閉じるボタンが押されたときの処理
		closeButton.setOnClickListener(v -> dialog.dismiss());
		// 履歴クリアボタンが押された時の処理
		clearButton.setOnClickListener(v -> {
			// 保存されている選択履歴をすべてクリア
			clearHistory(context);
			// 表示リストを再構築
			updateList.run();
		});
		// リスト項目タップ時の処理：履歴に保存し、リスナー経由でアクティビティ情報等を通知
		listView.setOnItemClickListener((parent, view, position, id) -> {
			PreferenceItemInfo selectedItem = adapter.getItem(position);
			// 項目の見出しとカテゴリー以外のみ通知(キーが無いと項目へスクロール移動できないため除外)
			if (selectedItem != null && !selectedItem.isHeader && !selectedItem.isCategory) {
				// 履歴に保存
				saveHistory(context, selectedItem.xmlFileName, selectedItem.key, selectedItem.title, selectedItem.summary, selectedItem.tagName);

				if (listener != null) {
					// 通知(通知先でアクティビティを呼び出して項目へスクロール移動させる)
					Class<?> matchedActivity = xmlToActivityMap.get(selectedItem.xmlFileName);
					listener.onPreferenceSelected(selectedItem.key, matchedActivity);
				}
				dialog.dismiss();
			}
		});
		// 検索ボックスの文字変更時にリアルタイムでリストを絞り込み
		searchBox.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// 表示リストを再構築
				updateList.run();
			}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		// アラートダイアログを表示
		dialog.show();
	}

	// SharedPreferencesから選択履歴を読み込むヘルパーメソッド
	private static List<PreferenceItemInfo> loadHistory(Context context) {
		List<PreferenceItemInfo> historyList = new ArrayList<>();
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		String savedStr = prefs.getString(KEY_HISTORY, "");
		
		if (!savedStr.isEmpty()) {
			String[] items = savedStr.split(NAME_SPLIT);
			for (String itemStr : items) {
				String[] parts = itemStr.split("\\|");
				if (parts.length >= 5) {
					String xmlFileName = parts[0];
					String key = parts[1];
					String title = parts[2];
					String summary = parts[3];
					String tagName = parts[4];
					// 読み込んだsummaryが "dummy" または "@" で始まるなら空文字にする
					if (summary != null) {
						String trimmed = summary.trim();
						if (trimmed.equalsIgnoreCase(NAME_DUMMY) || trimmed.startsWith(NAME_AT)) {
							summary = "";
						}
					}
					String displayText = summary.isEmpty() ? title : title + "\n" + summary;
					historyList.add(new PreferenceItemInfo(tagName, title, summary, key, xmlFileName, displayText));
				}
			}
		}
		return historyList;
	}

	// 選択された設定項目を履歴としてSharedPreferencesに保存するヘルパーメソッド
	private static void saveHistory(Context context, String xmlFileName, String key, String title, String summary, String tagName) {
		List<PreferenceItemInfo> historyList = loadHistory(context);
		// 既に存在する同一項目は一度削除して最上部に押し上げる
		historyList.removeIf(item -> item.xmlFileName.equals(xmlFileName) && item.key.equals(key));
		// title と summary がある場合は改行で区切る
		String displayText = summary.isEmpty() ? title : title + "\n" + summary;
		historyList.add(0, new PreferenceItemInfo(tagName, title, summary, key, xmlFileName, displayText));
		// 最大保存件数を超える場合は古いものを切り捨てる
		if (historyList.size() > MAX_HISTORY_SIZE) {
			historyList = historyList.subList(0, MAX_HISTORY_SIZE);
		}
		// 文字列形式にシリアライズして保存
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < historyList.size(); i++) {
			PreferenceItemInfo item = historyList.get(i);
			if (i > 0) sb.append(NAME_SPLIT);
			sb.append(item.xmlFileName).append("|").append(item.key).append("|").append(item.title).append("|").append(item.summary).append("|").append(item.tagName);
		}
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(KEY_HISTORY, sb.toString()).apply();
	}

	// 保存されている選択履歴をすべてクリアするヘルパーメソッド
	private static void clearHistory(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().remove(KEY_HISTORY).apply();
	}

	// 指定されたXMLリソースを解析し、Preference項目を抽出してリストに追加するヘルパーメソッド
	private static void parseXmlPreferences(Context context, int resId, String xmlFileName, List<PreferenceItemInfo> outList) {
		try (XmlResourceParser parser = context.getResources().getXml(resId)) {
			int eventType = parser.getEventType();
			String namespace = NAMESPACE;

			while (eventType != XmlResourceParser.END_DOCUMENT) {
				if (eventType == XmlResourceParser.START_TAG) {
					String tagName = parser.getName();
					if (tagName != null) {
						// Preference関連のタグであるかを判定
						boolean isPreferenceTag = tagName.contains(TAG_PREFERENCE) || tagName.startsWith(TAG_SRC);
						if (isPreferenceTag) {
							boolean isCategory = tagName.contains(TAG_PREFERENCECATEGORY);
							// Titleの取得
							int titleResId = parser.getAttributeResourceValue(namespace, NAME_TITLE, 0);
							String title;
							if (titleResId != 0) {
								try {
									title = context.getString(titleResId);
								}
								catch (Exception e) {
									title = parser.getAttributeValue(namespace, NAME_TITLE);
								}
							}
							else {
								title = parser.getAttributeValue(namespace, NAME_TITLE);
							}
							// Summaryの取得
							int summaryResId = parser.getAttributeResourceValue(namespace, NAME_SUMMARY, 0);
							String summary = null;
							if (summaryResId != 0) {
								try { summary = context.getString(summaryResId); } catch (Exception e) {}
							}
							if (summary == null) {
								summary = parser.getAttributeValue(namespace, NAME_SUMMARY);
							}
							// summaryが "dummy" または "@" の場合は無視して空文字扱いにする
							if (summary != null) {
								String trimmed = summary.trim();
								if (trimmed.equalsIgnoreCase(NAME_DUMMY) || trimmed.startsWith(NAME_AT)) {
									summary = "";
								}
							}

							String key = parser.getAttributeValue(namespace, NAME_KEY);
							String simpleTagName = tagName.substring(tagName.lastIndexOf('.') + 1);

							if (title != null || key != null || isCategory) {
								String cleanTitle = title != null ? title : context.getString(R.string.untitle);
								String cleanSummary = summary != null ? summary : "";
								String cleanKey = key != null ? key : context.getString(R.string.none);

								// 一覧表示用のテキスト(title と summary をペアにして表示)
								String displayText;
								if (!cleanSummary.isEmpty()) {
									// 改行で区切る
									displayText = cleanTitle + "\n" + cleanSummary;
								}
								else {
									displayText = cleanTitle;
								}

								outList.add(new PreferenceItemInfo(simpleTagName, cleanTitle, cleanSummary, cleanKey, xmlFileName, displayText, isCategory));
							}
						}
					}
				}
				eventType = parser.next();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
