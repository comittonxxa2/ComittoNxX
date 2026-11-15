package src.comitton.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.common.Logcat;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.DialogPreference;
import androidx.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ImportSettingPreference extends DialogPreference implements OnItemClickListener, OnClickListener {
	private Context mContext;
	private SharedPreferences mSp;

	private String mNoName = "(No Name)";

	private Button mButtonOk;
	private EditText mEditView;
	private TextView mMsgView;
	private ListView mListView;
	private CheckBox mCheckbox;
	private ItemArrayAdapter mItemArrayAdapter;
	private static byte[] mAesKey = null;
	private static boolean mIgnoreReadInfo;

	private static final int LAYOUT_PADDING = 10;

	public ImportSettingPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mSp = PreferenceManager.getDefaultSharedPreferences(context);
		setSummary(getSummary().toString().replace("[sdcard]", "[" + Environment.getExternalStorageDirectory().getAbsolutePath() + "]"));

		// OKボタンを非表示にする
		//setPositiveButtonText(null);
	}

	@Override
	protected View onCreateDialogView() {
		mIgnoreReadInfo = DEF.getBoolean(mSp, DEF.KEY_IGNOREREADINFO, false);
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);

		layout.setPadding(LAYOUT_PADDING, LAYOUT_PADDING, LAYOUT_PADDING, LAYOUT_PADDING);
		mMsgView = new TextView(mContext);
		mEditView = new EditText(mContext);
		mEditView.setMaxLines(1);
		mEditView.setHint(R.string.selectFile);
		mEditView.setFocusable(false);
		mListView = new ListView(mContext);
		mListView.setScrollingCacheEnabled(false);
		mListView.setOnItemClickListener(this);
		mCheckbox = new CheckBox(mContext);
		mCheckbox.setText(R.string.selectIgnoreReadInfo);
		mCheckbox.setChecked(mIgnoreReadInfo);
		updateImportList();

		layout.addView(mMsgView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		layout.addView(mEditView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		layout.addView(mListView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		layout.addView(mCheckbox, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		String str = (String) getDialogMessage();
		mMsgView.setText(str);

		mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				mIgnoreReadInfo = isChecked;
				SharedPreferences.Editor editor = mSp.edit();
				editor.putBoolean(DEF.KEY_IGNOREREADINFO, isChecked);
				editor.apply();
			}
		});

		return layout;
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);

		Dialog dialog = getDialog();
		if (dialog != null) {
			mButtonOk = (Button)dialog.findViewById(android.R.id.button1);
			if (mButtonOk != null) {
				mButtonOk.setOnClickListener(this);
			}
		}
	}

	@Override
	protected void onDialogClosed (boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
		}
	}

	private void updateImportList() {
		String fontpath = DEF.getConfigDirectory();
		List<String> items = new ArrayList<String>();

		File[] files = new File(fontpath).listFiles(getFileExtensionFilter(DEF.EXTENSION_SETTING));
		if (files != null) {
			// 設定
			for (File file : files) {
				if (file != null && file.isFile()) {
					String filename = file.getName();
					filename = filename.substring(0, filename.length()-4);
					if (filename.length() == 0){
						filename = mNoName;
					}
					items.add(filename);
				}
			}
			Collections.sort(items);
		}

		// リストの設定
		mItemArrayAdapter = new ItemArrayAdapter(mContext, -1, items);
		mListView.setAdapter(mItemArrayAdapter);
	}


    public FilenameFilter getFileExtensionFilter(String ext) {
        final String mExt = ext;
        return new FilenameFilter() {
            public boolean accept(File file, String name) {
                boolean ret = name.endsWith(mExt);
                return ret;
            }
        };
    }

	// 設定ファイルの先頭の文字列をデコード
	private static boolean DecAesKey(String encstr) {
		try {
			// BASE64デコードして暗号鍵を取得する
			mAesKey = Base64.getDecoder().decode(encstr);
		} catch (Exception e) {
			// 先頭の文字列が識別できなかった場合はエラーを返す
			return false;
		}
		return true;
	}
	// 復号化
	private static String DecAes(String decstr) {
		String dec = null;
		try {
			// BASE64デコード
			byte[] byteArray = Base64.getDecoder().decode(decstr);
			// 復号化
			SecretKeySpec sks = new SecretKeySpec(mAesKey,0,16,"AES");
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			c.init(Cipher.DECRYPT_MODE, sks);
			byte output[] = c.doFinal(byteArray);
			// 文字列を取り出す
			Charset charset = StandardCharsets.UTF_8;
			dec = new String(output,charset);
		} catch (Exception e) {
			// 復号化に失敗した場合はそのまま返す(暗号化されていない場合はここへ飛んでくる)
			dec = decstr;
		}
		return dec;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		if (parent == mListView) {
			// ファイルリスト選択
			String filename = mItemArrayAdapter.mItems.get(position);
			mEditView.setText(filename, TextView.BufferType.NORMAL);
		}

	}

	//	@Override
	public void onClick(View v) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (v == mButtonOk) {
			// OKボタン処理
			String filename = mEditView.getText().toString();
			if (filename.length() == 0) {
				Logcat.d(logLevel, "ファイルを選択してください.");
				Toast.makeText(mContext, R.string.selectFile, Toast.LENGTH_SHORT).show();
				return;
			} else if (filename.equals(mNoName)) {
				Logcat.d(logLevel, mNoName + " を選択しました.");
				filename = "";
			}

			String filepath = DEF.getConfigDirectory() + filename + DEF.EXTENSION_SETTING;

			try {
				File file = new File(filepath);
				if (!file.exists()) {
					// ファイルが存在しない
					Toast.makeText(mContext, mContext.getResources().getString(R.string.FileNotExist) + "\n" + filepath, Toast.LENGTH_SHORT).show();
					return;
				}
			} catch (Exception ex) {
				Logcat.e(logLevel, "", ex);
				return;
			}


			FileInputStream is = null;
			InputStreamReader sr = null;
			BufferedReader br = null;
			// 読み込んだ設定はいったんここにためる。ファイルを最後まで読めて1件以上あったときだけ
			// 既存の設定を消して書き込む(途中で失敗したときに、既存の設定だけが消えないようにする)
			LinkedHashMap<String, Object> imported = new LinkedHashMap<>();
			try {
				is = new FileInputStream(filepath);
				sr = new InputStreamReader(is, "UTF-8");
				br = new BufferedReader(sr, 8192);

				// ファイル読み込み
				String line;
				boolean isFirstLine = true;
				boolean isEncrypted = false;
				List<String> contentLines = new ArrayList<>();

				while ((line = br.readLine()) != null) {
					// 復号化
					if (isFirstLine) {
						isFirstLine = false;
						// 1行目がAESキーのBase64エンコードとして復元できるか確認
						if (DecAesKey(line)) {
							isEncrypted = true;
							continue; // 2行目以降のボディへ
						}
					}

					if (isEncrypted) {
						// 先頭以外の場合
						line = DecAes(line);
						if (line == null) {
							throw new IllegalStateException("復号に失敗しました.");
						}
					}
					contentLines.add(line);
				}

				// 読み込んだ中身を結合
				StringBuilder sb = new StringBuilder();
				for (String l : contentLines) {
					sb.append(l);
				}
				String fileContent = sb.toString().trim();

				// 自動判別パース処理
				if (fileContent.startsWith("{") && fileContent.endsWith("}")) {
					// JSON形式のパース処理
					JSONObject jsonRoot = new JSONObject(fileContent);
					Iterator<String> jsonKeys = jsonRoot.keys();

					while (jsonKeys.hasNext()) {
						// キーと値を取得
						String key = jsonKeys.next();
						if (mIgnoreReadInfo && isReadInfoKey(key)) {
							// 既読情報を読み込まない場合は何もしない
							continue;
						}
						Object value = jsonRoot.get(key);
						if (value instanceof String || value instanceof Boolean || value instanceof Integer || value instanceof Long || value instanceof Float) {
							imported.put(key, value);
						}
						else if (value instanceof Double) {
							imported.put(key, ((Double) value).floatValue());
						}
					}
				}
				else {
					// 従来形式(行ごとの key=value)のパース処理
					for (String l : contentLines) {
						if (l.length() < 2) {
							continue;
						}
						// 種別取得
						char valuetype = l.charAt(0);
						int index = l.indexOf('=', 2);
						if (index <= 0) {
							continue;
						}
						// キーと値を取得
						String key = l.substring(2, index);
						String value = l.substring(index + 1);
						switch (valuetype) {
							case 'S':
							case 's': // NxT専用キー
								imported.put(key, value);
								break;
							case 'B':
							case 'b': // NxT専用キー
								imported.put(key, Boolean.parseBoolean(value));
								break;
							case 'F':
							case 'f': // NxT専用キー
								imported.put(key, Float.parseFloat(value));
								break;
							case 'I':
							case 'i':
								int workInt = Integer.parseInt(value);
								if (mIgnoreReadInfo && isReadInfoKey(key)) {
									// 既読情報を読み込まない場合は何もしない
								} else {
									imported.put(key, workInt);
								}
								break;
							case 'L':
							case 'l': // NxT専用キー
								imported.put(key, Long.parseLong(value));
								break;
						}
					}
				}
			} catch (Exception ex) {
				Logcat.e(logLevel, "設定ファイルを読み込めませんでした. 設定は変更しません. " + filepath, ex);
				imported.clear();
			} finally {
				try {
					if (br != null) {
						br.close();
					}
					if (sr != null) {
						sr.close();
					}
					if (is != null) {
						is.close();
					}
				} catch (Exception e) {
					;
				}
			}
			if (imported.isEmpty()) {
				// 読み込みに失敗したか、設定が1件も無いファイルだった
				Toast.makeText(mContext, mContext.getResources().getString(R.string.LoadConfigError) + "\n" + filepath, Toast.LENGTH_LONG).show();
				return;
			}

			Editor ed = mSp.edit();
			// 既存の設定を削除
			Map<String, ?> keys = mSp.getAll();
			if (keys != null) {
				for (String key : keys.keySet()) {
					if (mIgnoreReadInfo && isReadInfoKey(key)) {
						// 既読情報を読み込まない場合は削除しない
					}
					else if (DEF.checkExportKey(key)) {
						// 出力対象
						ed.remove(key);
					}
				}
			}
			// 読み込んだ設定を書き込む(同じキーはremoveより後のputが優先される)
			for (Map.Entry<String, Object> e : imported.entrySet()) {
				Object value = e.getValue();
				if (value instanceof String) {
					ed.putString(e.getKey(), (String) value);
				}
				else if (value instanceof Boolean) {
					ed.putBoolean(e.getKey(), (Boolean) value);
				}
				else if (value instanceof Integer) {
					ed.putInt(e.getKey(), (Integer) value);
				}
				else if (value instanceof Long) {
					ed.putLong(e.getKey(), (Long) value);
				}
				else if (value instanceof Float) {
					ed.putFloat(e.getKey(), (Float) value);
				}
			}
			ed.apply();
			Logcat.d(logLevel, " 設定を読み込みました. " + filepath);
			Toast.makeText(mContext, mContext.getResources().getString(R.string.LoadConfig) + "\n" + filepath, Toast.LENGTH_SHORT).show();
			Dialog dialog = getDialog();
			if (dialog != null) {
				dialog.dismiss();
			}
		}
	}

	// 既読情報(ファイルのURLをキーにした値)か
	private static boolean isReadInfoKey(String key) {
		return key.startsWith("/") || key.startsWith("smb://") || key.startsWith("http://") || key.startsWith("ftp://") || key.startsWith("ftps://") || key.startsWith("sftp://");
	}

	public class ItemArrayAdapter extends ArrayAdapter<String>
	{
		private List<String> mItems; // ファイル情報リスト

		// コンストラクタ
		public ItemArrayAdapter(Context context, int resId, List<String> items)
		{
			super(context, resId, items);
			mItems = items;
		}

		// 一要素のビューの生成
		@Override
		public View getView(int index, View view, ViewGroup parent)
		{
			// レイアウトの生成
			if(view == null) {
				Context context = getContext();
				// レイアウト
				LinearLayout layout = new LinearLayout( context );
				layout.setPadding( 10, 10, 10, 10 );
//				layout.setBackgroundColor(Color.WHITE);
				view = layout;
				// テキスト
				TextView textview = new TextView(context);
				textview.setTag("text");
//				textview.setTextColor(Color.BLACK);
				textview.setPadding(10, 10, 10, 10);
				textview.setTextSize(18);
				layout.addView(textview);
			}

			// 値の指定
			String item = mItems.get(index);
			TextView textview = (TextView)view.findViewWithTag("text");
			textview.setText(item);
			return view;
		}
	}
}
