package src.comitton.fileaccess;

import android.app.Activity;
import android.content.Context;
import android.os.ParcelFileDescriptor;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.SftpATTRS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.FutureTask;

import src.comitton.fileview.data.FileData;
import src.comitton.common.Logcat;

public class SftpFileAccess {
	// SFTPのURIからホスト名を抽出
	public static String parseHost(String uriString) {
		if (uriString == null || uriString.isEmpty()) {
			return "";
		}
		try {
			int schemeEnd = uriString.indexOf("://");
			if (schemeEnd == -1) {
				return "";
			}
			int authStart = schemeEnd + 3;
			// パスが始まる最初の「/」の位置を探す(パス内の @ や : を巻き込まないため)
			int slashIndex = uriString.indexOf('/', authStart);
			int authorityEnd = (slashIndex != -1) ? slashIndex : uriString.length();
			// ユーザー名@ があればスキップ(ただし、最初のスラッシュより手前にあるものに限定)
			int atIndex = uriString.indexOf('@', authStart);
			if (atIndex != -1 && atIndex > authorityEnd) {
				atIndex = -1; // パス内にある @ は無視する
			}
			int hostStart = (atIndex != -1) ? atIndex + 1 : authStart;
			// ホスト名の終わり(ポート番号の ':' か、パスの始まり '/')を探す
			int colonIndex = uriString.indexOf(':', hostStart);
			int hostEnd;
			if (colonIndex != -1 && colonIndex < authorityEnd) {
				hostEnd = colonIndex;
			}
			else {
				hostEnd = authorityEnd;
			}

			String host = uriString.substring(hostStart, hostEnd);
			return host != null ? host.trim() : "";
		}
		catch (Exception e) {
			return "";
		}
	}
	// SFTPのURIまたは引数からポート番号を抽出(デフォルト: 22)
	public static int parsePort(String uriString, String portnumber) {
		try {
			// 引数で明示的なポート指定があれば最優先
			if (portnumber != null && !portnumber.isEmpty()) {
				return Integer.parseInt(portnumber);
			}
			int schemeEnd = uriString.indexOf("://");
			if (schemeEnd != -1) {
				int authStart = schemeEnd + 3;
				int atIndex = uriString.indexOf('@', authStart);
				int hostStart = (atIndex != -1) ? atIndex + 1 : authStart;
				int colonIndex = uriString.indexOf(':', hostStart);
				int slashIndex = uriString.indexOf('/', hostStart);
				
				if (colonIndex != -1 && (slashIndex == -1 || colonIndex < slashIndex)) {
					int portEnd = (slashIndex != -1) ? slashIndex : uriString.length();
					String portStr = uriString.substring(colonIndex + 1, portEnd);
					return Integer.parseInt(portStr);
				}
			}
		}
		catch (Exception ignored) {}
		// デフォルトポート(SFTP = 22)
		return 22;
	}
	// SFTPのURIからリモートファイルの絶対パスを抽出し、UTF-8でデコード
	public static String parsePath(String uriString) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (uriString == null || uriString.isEmpty()) {
			return "/";
		}
		try {
			int schemeEnd = uriString.indexOf("://");
			if (schemeEnd != -1) {
				int firstSlash = uriString.indexOf('/', schemeEnd + 3);
				if (firstSlash != -1) {
					String rawPath = uriString.substring(firstSlash);
					// パーセントエンコードされた文字（%xx）があればデコード、なければそのまま返す
					return URLDecoder.decode(rawPath, StandardCharsets.UTF_8.name());
				}
			}
		}
		catch (Exception e) {
			Logcat.e(logLevel, "parsePath error for URI: " + uriString, e);
		}
		return "/";
	}
	// JSchを使用してSFTPセッションを確立し、SFTPチャネルを開いて接続
	public static Object[] connectAndOpenChannel(String uriString, String user, String pass, String portnumber) throws Exception {
		String host = parseHost(uriString);
		if (host == null || host.isEmpty()) {
			throw new IOException("SFTP URIのパースに失敗しました（ホスト名が空です）。URI: " + uriString);
		}
		int port = parsePort(uriString, portnumber);
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, host, port);
		if (pass != null && !pass.isEmpty()) {
			session.setPassword(pass);
		}
		// ホスト鍵の厳格なチェックを無効化(アプリ内接続用)
		session.setConfig("StrictHostKeyChecking", "no");
		// セッション接続(10秒タイムアウト)
		session.connect(10000);
		// SFTPチャネルオープン&接続
		ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
		channelSftp.connect(10000);
		return new Object[]{session, channelSftp};
	}
	// URIからファイル名のみを抽出
	public static String filename(String uri) {
		if (uri.isEmpty()) {
			return "";
		}
		if (uri.endsWith("/")) {
			return uri.replaceFirst("^.*?([^/]+)?/$", "$1/");
		}
		else {
			return uri.replaceFirst("^.*?([^/]+)?$", "$1");
		}
	}
	// URIから親ディレクトリのパス文字列を取得します。
	public static String parent(String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		String result = uri.replaceFirst("([^/]+?)?/*$", "");
		Logcat.d(logLevel, "終了します. uri=" + uri + ", result=" + result);
		return result;
	}
	// リモートファイルのサイズを取得
	public static long length(String uri, String user, String pass, String port) throws FileAccessException {
		try {
			Object[] sc = connectAndOpenChannel(uri, user, pass, port);
			Session session = (Session) sc[0];
			ChannelSftp sftp = (ChannelSftp) sc[1];
			try {
				SftpATTRS attrs = sftp.lstat(parsePath(uri));
				return attrs.getSize();
			}
			finally {
				sftp.exit();
				session.disconnect();
			}
		}
		catch (Exception e) {
			throw new FileAccessException("SFTP length error: " + e.getMessage());
		}
	}
	// パイプを用いた非同期ストリーミングにより、ParcelFileDescriptor を生成
	public static ParcelFileDescriptor openParcelFileDescriptor(Context context, String uri, String user, String pass, String port) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
			ParcelFileDescriptor readSide = pipe[0];
			ParcelFileDescriptor writeSide = pipe[1];
			// メインスレッドブロックを防ぐためバックグラウンドスレッドで読み込み処理を実行
			new Thread(() -> {
				Session session = null;
				ChannelSftp sftp = null;
				try {
					Object[] sc = connectAndOpenChannel(uri, user, pass, port);
					session = (Session) sc[0];
					sftp = (ChannelSftp) sc[1];
					String remotePath = parsePath(uri);
					try (InputStream in = sftp.get(remotePath);
						 OutputStream out = new ParcelFileDescriptor.AutoCloseOutputStream(writeSide)) {
						byte[] buffer = new byte[8192];
						int len;
						while ((len = in.read(buffer)) != -1) {
							out.write(buffer, 0, len);
						}
						out.flush();
					}
				}
				catch (Exception e) {
					Logcat.e(logLevel, "Error streaming SFTP data: ", e);
					try {
						writeSide.closeWithError(e.getMessage());
					}
					catch (IOException ignored) {}
				}
				finally {
					if (sftp != null) {
						try { sftp.exit(); }
						catch (Exception ignored) {}
					}
					if (session != null) {
						try { session.disconnect(); }
						catch (Exception ignored) {}
					}
				}
			}).start();
			return readSide;
		}
		catch (IOException e) {
			throw new FileAccessException("Failed to create ParcelFileDescriptor for SFTP : " + e.getMessage());
		}
	}
	// ファイルが存在するかどうかを判定
	public static boolean exists(String uri, String user, String pass, String port) {
		try {
			length(uri, user, pass, port);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	// 指定パスがディレクトリかどうかを判定
	public static boolean isDirectory(String uri, String user, String pass, String port) {
		try {
			Object[] sc = connectAndOpenChannel(uri, user, pass, port);
			Session session = (Session) sc[0];
			ChannelSftp sftp = (ChannelSftp) sc[1];
			try {
				SftpATTRS attrs = sftp.lstat(parsePath(uri));
				return attrs.isDir();
			}
			finally {
				sftp.exit();
				session.disconnect();
			}
		}
		catch (Exception e) {
			return false;
		}
	}
	// ディレクトリ内のファイル一覧を取得
	public static ArrayList<FileData> listFiles(Activity activity, String uri, String user, String pass, String port) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		ArrayList<FileData> result = new ArrayList<>();
		try {
			Object[] sc = connectAndOpenChannel(uri, user, pass, port);
			Session session = (Session) sc[0];
			ChannelSftp sftp = (ChannelSftp) sc[1];
			try {
				Vector<ChannelSftp.LsEntry> list = sftp.ls(parsePath(uri));
				for (ChannelSftp.LsEntry entry : list) {
					String name = entry.getFilename();
					if (name.equals(".") || name.equals("..")) continue;
					if (entry.getAttrs().isDir()) {
						// ディレクトリの場合
						if (!name.endsWith("/")) {
							name += "/";
						}
					}
					long size = entry.getAttrs().getSize();
					// JSchのMTimeは秒単位のためミリ秒に変換
					long mtime = entry.getAttrs().getMTime() * 1000L;
					result.add(new FileData(activity, name, size, mtime, true));
				}
			}
			finally {
				sftp.exit();
				session.disconnect();
			}
		}
		catch (Exception e) {
			Logcat.e(logLevel, "SFTP listFiles error: ", e);
		}
		return result;
	}
	// 読み込み用 InputStream を取得
	public static InputStream getInputStream(String uri, String user, String pass, String port) throws FileAccessException {
		try {
			Object[] sc = connectAndOpenChannel(uri, user, pass, port);
			Session session = (Session) sc[0];
			ChannelSftp sftp = (ChannelSftp) sc[1];
			return sftp.get(parsePath(uri));
		}
		catch (Exception e) {
			throw new FileAccessException("SFTP getInputStream error: " + e.getMessage());
		}
	}
	// 書き込み用 OutputStream を取得
	public static OutputStream getOutputStream(String uri, String user, String pass, String port) throws FileAccessException {
		try {
			Object[] sc = connectAndOpenChannel(uri, user, pass, port);
			Session session = (Session) sc[0];
			ChannelSftp sftp = (ChannelSftp) sc[1];
			return sftp.put(parsePath(uri));
		}
		catch (Exception e) {
			throw new FileAccessException("SFTP getOutputStream error: " + e.getMessage());
		}
	}
	// ディレクトリを作成
	public static boolean mkdir(String uri, String user, String pass, String item, String port) {
		try {
			Object[] sc = connectAndOpenChannel(uri, user, pass, port);
			Session session = (Session) sc[0];
			ChannelSftp sftp = (ChannelSftp) sc[1];
			try {
				String target = relativePath(uri, item);
				sftp.mkdir(target);
				return true;
			}
			finally {
				sftp.exit();
				session.disconnect();
			}
		}
		catch (Exception e) {
			return false;
		}
	}
	// ファイルまたはディレクトリを削除(メインスレッドガード付き)
	public static boolean delete(String uri, String user, String pass, String port) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (uri == null) {
			return false;
		}
		// メインスレッドから呼び出された場合は NetworkOnMainThreadException を防ぐため別スレッドで実行して待機
		if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
			FutureTask<Boolean> task = new FutureTask<>(() -> deleteInternal(uri, user, pass, port));
			new Thread(task).start();
			try {
				return task.get();
			}
			catch (Exception e) {
				Logcat.e(logLevel, "SFTP delete async task error: " + e.getMessage());
				return false;
			}
		}
		else {
			return deleteInternal(uri, user, pass, port);
		}
	}
	// delete の実処理本体
	private static boolean deleteInternal(String uri, String user, String pass, String port) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Session session = null;
		ChannelSftp sftp = null;
		try {
			Object[] sc = connectAndOpenChannel(uri, user, pass, port);
			session = (Session) sc[0];
			sftp = (ChannelSftp) sc[1];
			String remotePath = parsePath(uri);
			// ディレクトリかファイルかを判定して適切な削除コマンドを実行
			if (sftp.lstat(remotePath).isDir()) {
				sftp.rmdir(remotePath);
			}
			else {
				sftp.rm(remotePath);
			}
			return true;
		}
		catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append("SFTP delete exception: ").append(e.getClass().getName()).append(": ").append(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				sb.append("\n  at ").append(elem.toString());
			}
			Logcat.e(logLevel, sb.toString());
			return false;
		}
		finally {
			if (sftp != null) {
				try { sftp.exit(); } catch (Exception ignored) {}
			}
			if (session != null) {
				try { session.disconnect(); } catch (Exception ignored) {}
			}
		}
	}
	// ベースURIとターゲットを結合し、相対パス・絶対パスを正規化
	public static String relativePath(String base, String target) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. base=" + base + ", target=" + target);
		String result;
		String tmp;
		if (target.startsWith("sftp://")) {
			// targetがsftp://で始まるならそのまま返す
			result = target;
			Logcat.d(logLevel, "target が sftp:// で始まっています. result=" + result);
		}
		else {
			// targetがftp://で始まらないならbaseとtargetを連結する
			result = base + target;
			Logcat.d(logLevel, "target が sftp:// で始まっていません. result=" + result);
		}
		// 連続するスラッシュは1つにまとめる
		result = result.replaceAll("/+", "/");
		result = result.replace("sftp:/", "sftp://");
		// ../があれば親ディレクトリを削除
		while (true) {
			tmp = result;
			result = result.replaceFirst("[^/]+/\\.\\./", "");
			if (result.equals(tmp)) {
				break;
			}
		}
		// 末尾が..なら親ディレクトリを削除
		result = result.replaceFirst("[^/]+/\\.\\.$", "");
		if (!result.startsWith(base)) {
			// resultがbaseの子孫じゃなければ空文字列を返す
			result = "";
		}
		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}
	// ファイル名を変更(メインスレッドガード付き)
	public static boolean renameTo(String uri, String user, String pass, String fromFile, String toFile, String port) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (uri == null || fromFile == null || toFile == null) {
			Logcat.e(logLevel, "SFTP renameTo error: arguments are null");
			return false;
		}
		// メインスレッドから呼び出された場合は別スレッドで安全に実行し同期待機
		if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
			FutureTask<Boolean> task = new FutureTask<>(() -> renameToInternal(uri, user, pass, fromFile, toFile, port));
			new Thread(task).start();
			try {
				return task.get();
			}
			catch (Exception e) {
				Logcat.e(logLevel, "SFTP renameTo async task error: " + e.getMessage());
				return false;
			}
		}
		else {
			return renameToInternal(uri, user, pass, fromFile, toFile, port);
		}
	}
	// renameTo の実処理本体
	private static boolean renameToInternal(String uri, String user, String pass, String fromFile, String toFile, String port) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Session session = null;
		ChannelSftp sftp = null;
		try {
			Object[] sc = connectAndOpenChannel(uri, user, pass, port);
			session = (Session) sc[0];
			sftp = (ChannelSftp) sc[1];
			// URIから正確なリモート絶対パスを取得し、親ディレクトリのパスを構築する
			String remotePath = parsePath(uri);
			String parentPath;
			if (remotePath.endsWith("/")) {
				parentPath = remotePath;
			}
			else {
				int lastSlash = remotePath.lastIndexOf('/');
				parentPath = (lastSlash >= 0) ? remotePath.substring(0, lastSlash + 1) : "/";
			}
			String fromPath = parentPath + fromFile;
			String toPath = parentPath + toFile;
			// パス区切りのスラッシュを正規化
			fromPath = fromPath.replaceAll("/+", "/");
			toPath = toPath.replaceAll("/+", "/");
			Logcat.d(logLevel, "SFTP rename: from [" + fromPath + "] to [" + toPath + "]");
			sftp.rename(fromPath, toPath);
			return true;
		}
		catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append("SFTP renameTo exception: ").append(e.getClass().getName()).append(": ").append(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				sb.append("\n  at ").append(elem.toString());
			}
			Logcat.e(logLevel, sb.toString());
			return false;
		}
		finally {
			if (sftp != null) {
				try { sftp.exit(); } catch (Exception ignored) {}
			}
			if (session != null) {
				try { session.disconnect(); } catch (Exception ignored) {}
			}
		}
	}
	// ファイルの最終更新日時(タイムスタンプ)を取得
	public static long date(String uri, String user, String pass, String port) {
		try {
			Object[] sc = connectAndOpenChannel(uri, user, pass, port);
			Session session = (Session) sc[0];
			ChannelSftp sftp = (ChannelSftp) sc[1];
			try {
				SftpATTRS attrs = sftp.lstat(parsePath(uri));
				// JSchのMTimeは秒単位のためミリ秒に変換
				return attrs.getMTime() * 1000L;
			}
			finally {
				sftp.exit();
				session.disconnect();
			}
		}
		catch (Exception e) {
			return 0L;
		}
	}
	// 空のファイルを作成
	public static boolean createFile(String uri, String user, String pass, String item, String port) {
		try {
			Object[] sc = connectAndOpenChannel(uri, user, pass, port);
			Session session = (Session) sc[0];
			ChannelSftp sftp = (ChannelSftp) sc[1];
			try {
				String target = relativePath(uri, item);
				java.io.ByteArrayInputStream emptyInput = new java.io.ByteArrayInputStream(new byte[0]);
				sftp.put(emptyInput, target);
				return true;
			}
			finally {
				sftp.exit();
				session.disconnect();
			}
		}
		catch (Exception e) {
			return false;
		}
	}
}
