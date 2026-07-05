package src.comitton.common;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.database.ContentObserver;

// マルチプロセス対応のSharedPreferences環境を構築
public class MultiProcessPreferences implements SharedPreferences {
	private static volatile MultiProcessPreferences sInstance;
	private final String AUTHORITY;
	private final Uri PROVIDER_URI;
	private final Context context;
	private final ContentObserver contentObserver;
	private final SharedPreferences.OnSharedPreferenceChangeListener localPreferenceListener;
	private final java.util.List<OnSharedPreferenceChangeListener> listeners = new java.util.ArrayList<>();
	private final ConcurrentHashMap<String, Object> mCache = new ConcurrentHashMap<>();

	private MultiProcessPreferences(Context context) {
		this.context = context.getApplicationContext();
		this.AUTHORITY = this.context.getPackageName() + ".prefprovider";
		this.PROVIDER_URI = Uri.parse("content://" + this.AUTHORITY);
		this.contentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
			@Override
			public void onChange(boolean selfChange, @Nullable Uri uri) {
				super.onChange(selfChange, uri);
				if (uri == null) return;
				// URLの後ろについている「?key=xxxx」からキー名を引き抜く
				String key = uri.getQueryParameter("key");
				if (key == null) {
					key = "";
				}
				// 別プロセスで値が変わったキーをキャッシュから削除(次回読み込み時にProviderへ行く)
				if (!key.isEmpty()) {
					mCache.remove(key);
				}
				else {
					// キーが不明な場合は念のため全クリア
					mCache.clear();
				}
				// 取り出した本物のキー名をそのまま既存のリスナーに届ける
				synchronized (listeners) {
					for (OnSharedPreferenceChangeListener listener : listeners) {
						if (listener != null) {
							listener.onSharedPreferenceChanged(MultiProcessPreferences.this, key);
						}
					}
				}
			}
		};
		// 標準のSharedPreferencesを監視するリスナー
		this.localPreferenceListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key == null) return;
				// システムが書き換えた最新値を取得
				Object value = sharedPreferences.getAll().get(key);
				if (value == null) return;
				// 自分自身(MultiProcessPreferences)のEditorを使ってSAVE_BATCHを強制呼び出し
				// 自分が書き換えた値はそのままキャッシュに格納して高速化
				mCache.put(key, value);
				Editor editor = MultiProcessPreferences.this.edit();
				if (value instanceof String) editor.putString(key, (String) value);
				else if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
				else if (value instanceof Integer) editor.putInt(key, (Integer) value);
				else if (value instanceof Long) editor.putLong(key, (Long) value);
				else if (value instanceof Float) editor.putFloat(key, (Float) value);
				// ここで自動的にプロバイダへ送信される
				editor.apply();
			}
		};
		if (!isMainProcess(this.context)) {
			// 別プロセスの場合はプロバイダからの通知を監視し重い通信(GET_ALL)を走らせて初期化
			this.context.getContentResolver().registerContentObserver(PROVIDER_URI, true, contentObserver);
			preloadAllPreferences(); 
		}
		else {
			// メインプロセス自身の場合は標準SharedPreferencesの動きを監視
			// 通信は完全にスキップして手元の生のキャッシュから超高速でデータをコピー
			android.preference.PreferenceManager.getDefaultSharedPreferences(this.context).registerOnSharedPreferenceChangeListener(localPreferenceListener);
			preloadFromLocal(); 
		}
	}
	// インスタンスを1つに限定して取得するメソッド(シングルトンパターン)
	public static MultiProcessPreferences getInstance(Context context) {
		if (sInstance == null) {
			synchronized (MultiProcessPreferences.class) {
				if (sInstance == null) {
					sInstance = new MultiProcessPreferences(context);
				}
			}
		}
		return sInstance;
	}
	// メインプロセス専用：通信を100%スキップしてメモリ間コピーする爆速処理
	private void preloadFromLocal() {
		try {
			String prefName = context.getPackageName() + "_preferences";
			SharedPreferences sp = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
			Map<String, ?> all = sp.getAll();
			if (all != null) {
				mCache.clear();
				mCache.putAll(all);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// プロセス判別用の補助メソッド
	private boolean isMainProcess(Context context) {
		String currentProcessName = "";
		// Android 9(API28)以降向けの超高速API
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
			currentProcessName = android.app.Application.getProcessName();
		}
		else {
			// Android 8.1以前向けの高速代替処理(ファイルを直接読むためActivityManagerより圧倒的に速い)
			try {
				java.io.BufferedReader reader = new java.io.BufferedReader(
					new java.io.FileReader("/proc/self/cmdline"));
				currentProcessName = reader.readLine().trim();
				reader.close();
			}
			catch (Exception e) {
				// 万が一のフォールバック
				currentProcessName = context.getPackageName();
			}
		}
		// プロセス名がパッケージ名と一致すればメインプロセス
		return context.getPackageName().equals(currentProcessName);
	}

	private synchronized void preloadAllPreferences() {
		try {
			Bundle reply = context.getContentResolver().call(PROVIDER_URI, "GET_ALL", null, null);
			if (reply != null) {
				mCache.clear();
				for (String key : reply.keySet()) {
					Object value = reply.get(key);
					if (value != null) {
						mCache.put(key, value);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	// 読み出し(Get)系の実装
	@Override
	public String getString(String key, @Nullable String defValue) {
		// キャッシュにない場合のみ、String専用の最小限の処理でProviderを叩く
		try {
			if (mCache.containsKey(key)) return (String) mCache.get(key);
			Bundle extras = new Bundle();
			extras.putString("def", defValue);
			Bundle reply = context.getContentResolver().call(PROVIDER_URI, "GET_STRING", key, extras);
			if (reply != null && reply.containsKey("val")) {
				String val = reply.getString("val");
				mCache.put(key, val != null ? val : defValue);
				return val;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return defValue;
	}
	@Override
	public boolean getBoolean(String key, boolean defValue) {
		try {
			if (mCache.containsKey(key)) return (Boolean) mCache.get(key);
			Bundle extras = new Bundle();
			extras.putBoolean("def", defValue);
			Bundle reply = context.getContentResolver().call(PROVIDER_URI, "GET_BOOLEAN", key, extras);
			if (reply != null && reply.containsKey("val")) {
				boolean val = reply.getBoolean("val");
				mCache.put(key, val);
				return val;
			}
		}
		catch (Exception e) {
			e.printStackTrace(); 
		}
		return defValue;
	}
	@Override
	public int getInt(String key, int defValue) {
		try {
			if (mCache.containsKey(key)) return (Integer) mCache.get(key);
			Bundle extras = new Bundle();
			extras.putInt("def", defValue);
			Bundle reply = context.getContentResolver().call(PROVIDER_URI, "GET_INT", key, extras);
			if (reply != null && reply.containsKey("val")) {
				int val = reply.getInt("val");
				mCache.put(key, val);
				return val;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return defValue;
	}
	@Override
	public long getLong(String key, long defValue) {
		try {
			if (mCache.containsKey(key)) return (Long) mCache.get(key);
			Bundle extras = new Bundle();
			extras.putLong("def", defValue);
			Bundle reply = context.getContentResolver().call(PROVIDER_URI, "GET_LONG", key, extras);
			if (reply != null && reply.containsKey("val")) {
				long val = reply.getLong("val");
				mCache.put(key, val);
				return val;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return defValue;
	}
	@Override
	public float getFloat(String key, float defValue) {
		try {
			if (mCache.containsKey(key)) return (Float) mCache.get(key);
			Bundle extras = new Bundle();
			extras.putFloat("def", defValue);
			Bundle reply = context.getContentResolver().call(PROVIDER_URI, "GET_FLOAT", key, extras);
			if (reply != null && reply.containsKey("val")) {
				float val = reply.getFloat("val");
				mCache.put(key, val);
				return val;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return defValue;
	}
	@Nullable
	@Override
	public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
		return defValues;
	}
	// 書き込み(Editor)系の実装
	@Override
	public Editor edit() {
		return new Editor() {
			private final Bundle pendingChanges = new Bundle();
			@Override
			public Editor putString(String key, @Nullable String value) {
				pendingChanges.putString(key, value);
				return this;
			}
			@Override
			public Editor putBoolean(String key, boolean value) {
				pendingChanges.putBoolean(key, value);
				return this;
			}
			@Override
			public Editor putInt(String key, int value) {
				pendingChanges.putInt(key, value);
				return this;
			}
			@Override
			public Editor putLong(String key, long value) {
				pendingChanges.putLong(key, value);
				return this;
			}
			@Override
			public Editor putFloat(String key, float value) {
				pendingChanges.putFloat(key, value);
				return this;
			}
			@Override
			public Editor remove(String key) {
				pendingChanges.putBoolean(key, true);
				return this;
			}
			@Override
			public boolean commit() {
				try {
					// 念のためこれからプロバイダ経由で保存する予定のデータを一時退避
					Bundle changesCopy = new Bundle(pendingChanges);
					// プロバイダを叩いて物理ディスクに保存(SAVE_BATCH)
					Bundle reply = context.getContentResolver().call(PROVIDER_URI, "SAVE_BATCH", null, pendingChanges);
					pendingChanges.clear();
					if (changesCopy != null) {
						for (String key : changesCopy.keySet()) {
							Object value = changesCopy.get(key);
							if (value != null) {
								// 自身が持つ高速キャッシュマップを即座に最新データに更新
								mCache.put(key, value);
							}
						}
					}
					return true;
				}
				catch (Exception e) {
				    e.printStackTrace();
				    return false;
				}
			}
			@Override
			public void apply() {
				// マルチプロセス間の同期ズレを防ぐため即時コミット
				commit();
			}
			@Override public Editor clear() { return this; }
			@Override public Editor putStringSet(String key, @Nullable Set<String> values) { return this; }
		};
	}
	// キャッシュを返す
	@Override public Map<String, ?> getAll() { return mCache; }
	// 既存コードのビルドエラーを防ぐ空実装
	@Override public boolean contains(String key) { return false; }
	// リスナーを適切に登録・削除できるようにする
	@Override
	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l) {
		if (l == null) return;
		synchronized (listeners) {
			if (!listeners.contains(l)) {
				listeners.add(l);
			}
		}
	}
	@Override 
	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l) {
		if (l == null) return;
		synchronized (listeners) {
			listeners.remove(l);
		}
	}
}

