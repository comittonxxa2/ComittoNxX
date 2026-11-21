package src.comitton.fileaccess;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.fileview.data.LibraryEntry;
import src.comitton.fileview.filelist.LibraryCache;
import src.comitton.fileview.filelist.ServerSelect;

/**
 * 端末のローカルにコピーした本が、サーバーのどの本かを書庫管理のキャッシュ(library.dat)から割り出す。
 * ファイル名(大文字小文字は区別しない)とサイズが両方一致する本が1つだけのときに限り、
 * 既読位置の同期に使うキー(接続先 + フォルダ + ファイル名)を返す。0件や複数件のときは null。
 * 書庫管理のキャッシュはオフラインでも使えるので、圏外でも判定できる。
 */
public class LocalBookOrigin {
	public static class Origin {
		public String host;
		public String path;
		public String file;
	}

	// 直前の判定結果(同じファイルを開き直すたびに全件を走査しない)
	private static String sLastKey;
	private static int sLastCacheVersion = -1;
	private static Origin sLastValue;

	public static synchronized Origin resolve(Context context, SharedPreferences sp, String localFilePath) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (localFilePath == null || localFilePath.isEmpty()) {
			return null;
		}
		File local = new File(localFilePath);
		if (!local.isFile()) {
			return null;
		}
		long size = local.length();
		String key = localFilePath + "|" + size + "|" + local.lastModified();
		// 書庫管理のキャッシュが更新されたら、前回「一致なし」だった本も判定し直す
		int cacheVersion = LibraryCache.getVersion();
		if (key.equals(sLastKey) && cacheVersion == sLastCacheVersion) {
			return sLastValue;
		}

		LibraryEntry matched = null;
		int count = 0;
		String name = local.getName();
		for (LibraryEntry e : LibraryCache.getEntries()) {
			if (e.getSize() == size && name.equalsIgnoreCase(e.getName())) {
				matched = e;
				count++;
			}
		}
		Origin result = null;
		if (count == 1) {
			result = toOrigin(context, sp, matched);
		}
		else {
			Logcat.d(logLevel, "書庫管理に一意に一致する本が無いため、既読位置は共有しません. name=" + name + ", size=" + size + ", 一致数=" + count);
		}
		sLastKey = key;
		sLastCacheVersion = cacheVersion;
		sLastValue = result;
		return result;
	}

	// 書庫管理エントリのUnixパスを、置換設定(EverythingReplaceFrom/To)と登録済みSMBサーバーのhostで
	// smb://パスにして、接続先・フォルダ・ファイル名に分解する。
	// FileSelectActivity.resolveLibraryFileLocation()と同じ変換。
	private static Origin toOrigin(Context context, SharedPreferences sp, LibraryEntry entry) {
		String replaceFrom = sp.getString(DEF.KEY_EVERYTHING_REPLACE_FROM, DEF.DEFAULT_EVERYTHING_REPLACE_FROM);
		String replaceTo = sp.getString(DEF.KEY_EVERYTHING_REPLACE_TO, DEF.DEFAULT_EVERYTHING_REPLACE_TO);
		String fullPath = entry.getFullPath();
		String smbPath = fullPath;
		if (!replaceFrom.isEmpty() && fullPath.startsWith(replaceFrom)) {
			smbPath = replaceTo + fullPath.substring(replaceFrom.length());
		}
		if (!smbPath.startsWith("smb://")) {
			if (smbPath.startsWith("//")) {
				smbPath = "smb:" + smbPath;
			}
			else if (smbPath.startsWith("/")) {
				smbPath = "smb:/" + smbPath;
			}
			else {
				smbPath = "smb://" + smbPath;
			}
		}

		ServerSelect servers = new ServerSelect(sp, context);
		String matchedHost = null;
		String matchedRoot = null;
		for (int s = 0; s < DEF.MAX_SERVER; s++) {
			if (servers.getAccessType(s) != DEF.ACCESS_TYPE_SMB) {
				continue;
			}
			String host = servers.getHost(s);
			if (host == null || host.isEmpty()) {
				continue;
			}
			String root = "smb://" + host;
			// ホスト名の前方一致(nas と nas-server など)で別のサーバーに誤って一致しないよう、ルートの直後は区切りか末尾に限る
			if (smbPath.regionMatches(true, 0, root, 0, root.length())
					&& (smbPath.length() == root.length() || smbPath.charAt(root.length()) == '/' || smbPath.charAt(root.length()) == ':')
					&& (matchedRoot == null || root.length() > matchedRoot.length())) {
				matchedHost = host;
				matchedRoot = root;
			}
		}
		if (matchedRoot == null) {
			return null;
		}
		String afterRoot = smbPath.substring(matchedRoot.length());
		if (!afterRoot.startsWith("/")) {
			afterRoot = "/" + afterRoot;
		}
		int lastSlash = afterRoot.lastIndexOf('/');
		String dirPath = afterRoot.substring(0, lastSlash + 1);
		String fileName = afterRoot.substring(lastSlash + 1);
		if (fileName.isEmpty()) {
			return null;
		}
		Origin o = new Origin();
		o.host = matchedHost;
		o.path = dirPath;
		o.file = fileName;
		return o;
	}
}
