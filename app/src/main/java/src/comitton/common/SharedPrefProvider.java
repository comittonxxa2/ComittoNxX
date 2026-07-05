package src.comitton.common;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// マルチプロセス対応のSharedPreferenceのコンテンツプロバイダー
public class SharedPrefProvider extends ContentProvider {
	@Override public boolean onCreate() { return true; }
	@Nullable
	@Override
	public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
		Context ctx = getContext();
		if (ctx == null) return null;

		String prefName = ctx.getPackageName() + "_preferences";
		SharedPreferences sp = ctx.getSharedPreferences(prefName, Context.MODE_PRIVATE);
		Bundle bundle = new Bundle();
		// 全データ一括取得コマンド
		if ("GET_ALL".equals(method)) {
			java.util.Map<String, ?> allEntries = sp.getAll();
			for (java.util.Map.Entry<String, ?> entry : allEntries.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				// Bundleに型を保持したまま詰め込む
				if (value instanceof String) bundle.putString(key, (String) value);
				else if (value instanceof Boolean) bundle.putBoolean(key, (Boolean) value);
				else if (value instanceof Integer) bundle.putInt(key, (Integer) value);
				else if (value instanceof Long) bundle.putLong(key, (Long) value);
				else if (value instanceof Float) bundle.putFloat(key, (Float) value);
			}
			return bundle;
		}
		if (method.startsWith("GET_") && arg != null) {
			if ("GET_STRING".equals(method)) bundle.putString("val", sp.getString(arg, extras != null ? extras.getString("def") : ""));
			else if ("GET_BOOLEAN".equals(method)) bundle.putBoolean("val", sp.getBoolean(arg, extras != null && extras.getBoolean("def")));
			else if ("GET_INT".equals(method)) bundle.putInt("val", sp.getInt(arg, extras != null ? extras.getInt("def") : 0));
			else if ("GET_LONG".equals(method)) bundle.putLong("val", sp.getLong(arg, extras != null ? extras.getLong("def") : 0L));
			else if ("GET_FLOAT".equals(method)) bundle.putFloat("val", sp.getFloat(arg, extras != null ? extras.getFloat("def") : 0.0f));
			return bundle;
		}
		if ("SAVE_BATCH".equals(method) && extras != null) {
			String prefNames = ctx.getPackageName() + "_preferences";
			// プロバイダプロセス用(ディスク書き込み用)
			SharedPreferences spe = ctx.getSharedPreferences(prefNames, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = spe.edit();
			// メインプロセスのキャッシュ同期用
			SharedPreferences mainSp = PreferenceManager.getDefaultSharedPreferences(ctx);
			SharedPreferences.Editor mainEditor = mainSp.edit();

			for (String key : extras.keySet()) {
				Object value = extras.get(key);
				if (value instanceof String) {
					editor.putString(key, (String) value);
					mainEditor.putString(key, (String) value);
				}
				else if (value instanceof Boolean) {
					editor.putBoolean(key, (Boolean) value);
					mainEditor.putBoolean(key, (Boolean) value);
				}
				else if (value instanceof Integer) {
					editor.putInt(key, (Integer) value);
					mainEditor.putInt(key, (Integer) value);
				}
				else if (value instanceof Long) {
					editor.putLong(key, (Long) value);
					mainEditor.putLong(key, (Long) value);
				}
				else if (value instanceof Float) {
					editor.putFloat(key, (Float) value);
					mainEditor.putFloat(key, (Float) value);
				}
			}
			// 両方とも確実にcommit()で即時永続化・同期させる
			boolean b1 = editor.commit();
			boolean b2 = mainEditor.commit();
			if (b1 || b2) {
				// 別プロセスへ変更を通知
				for (String key : extras.keySet()) {
					Uri changeUri = Uri.parse("content://" + ctx.getPackageName() + ".prefprovider/change?key=" + Uri.encode(key));
					ctx.getContentResolver().notifyChange(changeUri, null);
				}
			}
		}
		return bundle;
	}

	@Nullable @Override public Cursor query(@NonNull Uri u, String[] p, String s, String[] a, String so) { return null; }
	@Nullable @Override public String getType(@NonNull Uri u) { return null; }
	@Nullable @Override public Uri insert(@NonNull Uri u, ContentValues v) { return null; }
	@Override public int delete(@NonNull Uri u, String s, String[] a) { return 0; }
	@Override public int update(@NonNull Uri u, ContentValues v, String s, String[] a) { return 0; }
}
