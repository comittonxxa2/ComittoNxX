package src.comitton.fileaccess;

import android.app.Activity;
import android.content.Context;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import src.comitton.fileview.data.FileData;
import src.comitton.common.Logcat;

public class FtpFileAccess {
	// URIからホスト名を抽出
	public static String parseHost(String uriString) {
		if (uriString == null || uriString.isEmpty()) {
			return null;
		}
		try {
			// パス（最初の「/」以降）を巻き込まないよう、権限部（Authority）または最初の「/」手前までに限定する
			// スキームを除いた後、最初の「/」またはポート「:」までの範囲でホストを特定する
			Pattern pattern = Pattern.compile("^[a-zA-Z0-9+-.]+://(?:[^/@]+@)?([^:/]+)(?::[0-9]+)?(?:/.*)?$");
			Matcher matcher = pattern.matcher(uriString);
			if (matcher.matches()) {
				String host = matcher.group(1);
				if (host != null && !host.isEmpty()) {
					return host;
				}
			}
			// フォールバックとして標準の URI パースを試みる
			URI uri = URI.create(uriString);
			return uri.getHost();
		}
		catch (Exception e) {
			return null;
		}
	}
	// URIからポート番号を抽出(デフォルト: FTP=21, FTPS=990等)
	public static int parsePort(String uriString, boolean isFtps, String defaultPortStr) {
		try {
			// 引数で明示的なポート指定があればそれを最優先
			if (defaultPortStr != null && !defaultPortStr.isEmpty()) {
				return Integer.parseInt(defaultPortStr);
			}
			// パスの「/」をまたいで `@` を巻き込まないよう `[^/@]+@` に修正
			Pattern pattern = Pattern.compile("^[a-zA-Z0-9+-.]+://(?:[^/@]+@)?[^:/]+:([0-9]+)(?:/.*)?$");
			Matcher matcher = pattern.matcher(uriString);
			if (matcher.matches()) {
				String portStr = matcher.group(1);
				if (portStr != null && !portStr.isEmpty()) {
					return Integer.parseInt(portStr);
				}
			}
			// 標準 URI クラスでの試行
			URI uri = URI.create(uriString);
			int port = uri.getPort();
			if (port != -1) {
				return port;
			}
		}
		catch (Exception e) {
			// 無視してデフォルト値へフォールバック
		}
		// デフォルトポート(FTPSなら990、FTPなら21)
		return isFtps ? 990 : 21;
	}
	// URIからリモートファイルパスを抽出
	public static String parsePath(String uriString) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (uriString == null || uriString.isEmpty()) {
			return "/";
		}
		try {
			// スキーム以降の最初のスラッシュ "/" を探し、そこから後ろをすべてパスとみなす
			// これにより、ホスト名やユーザー名に含まれる特殊文字や、未エンコードの日本語パスの例外を防ぐ
			int schemeEnd = uriString.indexOf("://");
			if (schemeEnd != -1) {
				int firstSlash = uriString.indexOf('/', schemeEnd + 3);
				if (firstSlash != -1) {
					String rawPath = uriString.substring(firstSlash);
					// パーセントエンコードされている部分（%E6%97...など）をUTF-8でデコードして日本語に戻す
					return URLDecoder.decode(rawPath, StandardCharsets.UTF_8.name());
				}
				else {
					// スラッシュがない場合はルート
					return "/";
				}
			}
			// フォールバックとして従来の URI クラスを使用
			URI uri = URI.create(uriString);
			String path = uri.getPath();
			if (path != null && !path.isEmpty()) {
				return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
			}
		}
		catch (Exception e) {
			Logcat.e(logLevel, "parsePath failed for URI: " + uriString + ", error: " + e.getMessage());
		}
		// 万が一パースできなかった場合
		return "/";
	}
	// 接続とログインを行うヘルパー
	public static FTPClient connectAndLogin(String uriString, String user, String pass, boolean isFtps, String portnumber, boolean isPassive) throws IOException {
		String host = parseHost(uriString);
		int port = parsePort(uriString, isFtps, portnumber);

		FTPClient ftpClient;
		if (isFtps) {
			ftpClient = new FTPSClient(false);
		}
		else {
			ftpClient = new FTPClient();
		}
		// connect() を呼ぶ「前」にコントロールエンコーディングを設定する
		ftpClient.setControlEncoding("UTF-8");
		ftpClient.setConnectTimeout(10000);
		ftpClient.connect(host, port);
		if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
			ftpClient.disconnect();
			throw new IOException("FTP server refused connection.");
		}
		// ログイン処理
		if (user != null && !user.isEmpty()) {
			if (!ftpClient.login(user, pass)) {
				ftpClient.disconnect();
				throw new IOException("FTP login failed.");
			}
		}
		else {
			if (!ftpClient.login("anonymous", "")) {
				ftpClient.disconnect();
				throw new IOException("FTP anonymous login failed.");
			}
		}
		// FTPSの場合のデータチャネル保護設定（PBSZ と PROT）
		if (isFtps && ftpClient instanceof FTPSClient) {
			try {
				FTPSClient ftpsClient = (FTPSClient) ftpClient;
				// PROTの前に必ず PBSZ 0 を送る必要がある
				ftpsClient.execPBSZ(0);
				ftpsClient.execPROT("P");
			}
			catch (Exception e) {
				throw new IOException("Failed to set FTPS data channel protection: " + e.getMessage(), e);
			}
		}
		// パッシブモード / アクティブモードの切り替え
		if (isPassive) {
			ftpClient.enterLocalPassiveMode();
		}
		else {
			ftpClient.enterLocalActiveMode();
		}
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		return ftpClient;
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
	// URIから親ディレクトリのパス文字列を取得
	public static String parent(String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		String result = uri.replaceFirst("([^/]+?)?/*$", "");
		Logcat.d(logLevel, "終了します. uri=" + uri + ", result=" + result);
		return result;
	}
	// ベースURIとターゲットを結合し、相対パス・絶対パスを正規化
	public static String relativePath(String base, String target) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. base=" + base + ", target=" + target);
		String result;
		String tmp;
		if (target.startsWith("ftp://") || target.startsWith("ftps://")) {
			// targetがftp://で始まるならそのまま返す
			result = target;
			Logcat.d(logLevel, "target が ftp:// で始まっています. result=" + result);
		}
		else {
			// targetがftp://で始まらないならbaseとtargetを連結する
			result = base + target;
			Logcat.d(logLevel, "target が ftp:// で始まっていません. result=" + result);
		}
		// 連続するスラッシュは1つにまとめる
		result = result.replaceAll("/+", "/");
		result = result.replace("ftp:/", "ftp://");
		result = result.replace("ftps:/", "ftps://");
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
	// リモートファイルのサイズを取得
	public static long length(String uri, String user, String pass, boolean isFtps, String port, boolean isPassive) throws FileAccessException {
		FTPClient client = null;
		try {
			client = connectAndLogin(uri, user, pass, isFtps, port, isPassive);
			return getFileSize(client, parsePath(uri));
		}
		catch (Exception e) {
			throw new FileAccessException("FTP length error: " + e.getMessage());
		}
		finally {
			if (client != null && client.isConnected()) {
				try {
					client.logout();
					client.disconnect();
				}
				catch (IOException ignored) {}
			}
		}
	}
	// パイプを用いた非同期ストリーミングにより、ParcelFileDescriptor を生成
	public static ParcelFileDescriptor openParcelFileDescriptor(Context context, String uri, String user, String pass, boolean isFtps, String port, boolean isPassive) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
			ParcelFileDescriptor readSide = pipe[0];
			ParcelFileDescriptor writeSide = pipe[1];
			// メインスレッドブロックを防ぐためバックグラウンドスレッドでダウンロード処理を実行
			new Thread(() -> {
				FTPClient client = null;
				InputStream in = null;
				try {
					client = connectAndLogin(uri, user, pass, isFtps, port, isPassive);
					String remotePath = parsePath(uri);
					in = client.retrieveFileStream(remotePath);
					if (in == null) {
						throw new IOException("Failed to retrieve FTP file stream for: " + remotePath);
					}
					try (OutputStream out = new ParcelFileDescriptor.AutoCloseOutputStream(writeSide)) {
						byte[] buffer = new byte[8192];
						int len;
						while ((len = in.read(buffer)) != -1) {
							out.write(buffer, 0, len);
						}
						out.flush();
					}
					if (in != null) {
						in.close();
					}
					if (client != null) {
						client.completePendingCommand();
					}
				}
				catch (Exception e) {
					Logcat.e(logLevel, "Error streaming FTP data: ", e);
					try {
						writeSide.closeWithError(e.getMessage());
					}
					catch (IOException ignored) {}
				}
				finally {
					if (client != null && client.isConnected()) {
						try {
							client.logout();
							client.disconnect();
						}
						catch (IOException ignored) {}
					}
				}
			}).start();

			return readSide;
		}
		catch (IOException e) {
			throw new FileAccessException("Failed to create ParcelFileDescriptor for FTP : " + e.getMessage());
		}
	}
	// サーバーから正確なファイルサイズを取得するためのヘルパー(MLSD失敗時のフォールバック付き)
	public static long getFileSize(FTPClient client, String remotePath) throws IOException {
		FTPFile[] files = client.listFiles(remotePath);
		if (files != null && files.length > 0) {
			return files[0].getSize();
		}
		// mlsdが使えない場合のフォールバック
		int lastSlash = remotePath.lastIndexOf('/');
		String parentDir = (lastSlash >= 0) ? remotePath.substring(0, lastSlash + 1) : "/";
		String fileName = (lastSlash >= 0) ? remotePath.substring(lastSlash + 1) : remotePath;
		FTPFile[] list = client.listFiles(parentDir);
		if (list != null) {
			for (FTPFile file : list) {
				if (file != null && file.getName().equals(fileName)) {
					return file.getSize();
				}
			}
		}
		return -1;
	}
	// ファイルが存在するかどうかを判定
	public static boolean exists(String uri, String user, String pass, boolean isFtps, String port, boolean isPassive) {
		try {
			long len = length(uri, user, pass, isFtps, port, isPassive);
			return len >= 0;
		}
		catch (Exception e) {
			return false;
		}
	}
	// 指定パスがディレクトリかどうかを判定
	public static boolean isDirectory(String uri, String user, String pass, boolean isFtps, String port, boolean isPassive) {
		FTPClient client = null;
		try {
			client = connectAndLogin(uri, user, pass, isFtps, port, isPassive);
			String remotePath = parsePath(uri);
			FTPFile[] files = client.listFiles(remotePath);
			if (files != null && files.length > 0) {
				return files[0].isDirectory();
			}
			return client.changeWorkingDirectory(remotePath);
		}
		catch (Exception e) {
			return false;
		}
		finally {
			if (client != null && client.isConnected()) {
				try {
					client.logout();
					client.disconnect();
				}
				catch (IOException ignored) {}
			}
		}
	}
	// ディレクトリ内のファイル一覧を取得
	public static ArrayList<FileData> listFiles(Activity activity, String uri, String user, String pass, boolean isFtps, String port, boolean isPassive) {
		ArrayList<FileData> result = new ArrayList<>();
		FTPClient client = null;
		try {
			client = connectAndLogin(uri, user, pass, isFtps, port, isPassive);
			org.apache.commons.net.ftp.FTPClientConfig config = new org.apache.commons.net.ftp.FTPClientConfig();
			// サーバーがUTCでログを出力している場合は "UTC" を指定（必要に応じて調整）
			config.setServerTimeZoneId("UTC"); 
			client.configure(config);
			String remotePath = parsePath(uri);
			FTPFile[] files = client.listFiles(remotePath);
			if (files != null) {
				for (FTPFile f : files) {
					if (f == null) continue;
					String name = f.getName();
					if (name.equals(".") || name.equals("..")) continue;
					if (f.isDirectory()) {
						// ディレクトリの場合
						if (!name.endsWith("/")) {
							name += "/";
						}
					}
					long mtime = (f.getTimestamp() != null) ? f.getTimestamp().getTimeInMillis() : 0L;
					result.add(new FileData(activity, name, f.getSize(), mtime, true));
				}
			}
		}
		catch (Exception ignored) {
		}
		finally {
			if (client != null && client.isConnected()) {
				try {
					client.logout();
					client.disconnect();
				}
				catch (IOException ignored) {}
			}
		}
		return result;
	}
	// 読み込み用 InputStream を取得
	public static InputStream getInputStream(String uri, String user, String pass, boolean isFtps, String port, boolean isPassive) throws FileAccessException {
		try {
			FTPClient client = connectAndLogin(uri, user, pass, isFtps, port, isPassive);
			String remotePath = parsePath(uri);
			InputStream in = client.retrieveFileStream(remotePath);
			if (in == null) {
				client.disconnect();
				throw new IOException("Failed to retrieve file stream.");
			}
			return in;
		}
		catch (Exception e) {
			throw new FileAccessException("FTP getInputStream error: " + e.getMessage());
		}
	}
	// 書き込み用 OutputStream を取得
	public static OutputStream getOutputStream(String uri, String user, String pass, boolean isFtps, String port, boolean isPassive) throws FileAccessException {
		try {
			FTPClient client = connectAndLogin(uri, user, pass, isFtps, port, isPassive);
			String remotePath = parsePath(uri);
			OutputStream out = client.storeFileStream(remotePath);
			if (out == null) {
				client.disconnect();
				throw new IOException("Failed to store file stream.");
			}
			return out;
		}
		catch (Exception e) {
			throw new FileAccessException("FTP getOutputStream error: " + e.getMessage());
		}
	}
	// ディレクトリを作成
	public static boolean mkdir(String uri, String user, String pass, String item, boolean isFtps, String port, boolean isPassive) {
		FTPClient client = null;
		try {
			client = connectAndLogin(uri, user, pass, isFtps, port, isPassive);
			String targetPath = relativePath(uri, item);
			return client.makeDirectory(targetPath);
		}
		catch (Exception e) {
			return false;
		}
		finally {
			if (client != null && client.isConnected()) {
				try {
					client.logout();
					client.disconnect();
				}
				catch (IOException ignored) {}
			}
		}
	}
	// ファイルまたはディレクトリを削除(メインスレッドガード付き)
	public static boolean delete(String uri, String user, String pass, boolean isFtps, String port, boolean isPassive) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (uri == null) {
			return false;
		}
		// メインスレッドから呼ばれた場合は別スレッドで安全に実行し、結果を同期待機する
		if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
			java.util.concurrent.FutureTask<Boolean> task = new java.util.concurrent.FutureTask<>(() -> 
				deleteInternal(uri, user, pass, isFtps, port, isPassive)
			);
			new Thread(task).start();
			try {
				return task.get();
			}
			catch (Exception e) {
				Logcat.e(logLevel, "FTP delete async task error: " + e.getMessage());
				return false;
			}
		}
		else {
			return deleteInternal(uri, user, pass, isFtps, port, isPassive);
		}
	}
	// delete の実処理本体
	private static boolean deleteInternal(String uri, String user, String pass, boolean isFtps, String port, boolean isPassive) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		FTPClient client = null;
		try {
			client = connectAndLogin(uri, user, pass, isFtps, port, isPassive);
			String remotePath = parsePath(uri);
			// ファイルかディレクトリかを判定して適切な削除コマンドを実行
			FTPFile[] files = client.listFiles(remotePath);
			boolean isDir = false;
			if (files != null && files.length > 0) {
				isDir = files[0].isDirectory();
			}
			if (isDir) {
				return client.removeDirectory(remotePath);
			}
			else {
				boolean success = client.deleteFile(remotePath);
				if (!success) {
					success = client.removeDirectory(remotePath);
				}
				return success;
			}
		}
		catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append("FTP delete exception: ").append(e.getClass().getName()).append(": ").append(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				sb.append("\n  at ").append(elem.toString());
			}
			Logcat.e(logLevel, sb.toString());
			return false;
		}
		finally {
			if (client != null && client.isConnected()) {
				try {
					client.logout();
					client.disconnect();
				}
				catch (IOException ignored) {}
			}
		}
	}
	// ファイル名を変更(メインスレッドガード付き)
	public static boolean renameTo(String uri, String user, String pass, String fromFile, String toFile, boolean isFtps, String port, boolean isPassive) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		if (uri == null || fromFile == null || toFile == null) {
			Logcat.e(logLevel, "FTP renameTo error: arguments are null (uri=" + uri + ", from=" + fromFile + ", to=" + toFile + ")");
			return false;
		}
		// メインスレッドから呼ばれた場合は別スレッドで安全に実行し、結果を同期待機する
		if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
			java.util.concurrent.FutureTask<Boolean> task = new java.util.concurrent.FutureTask<>(() -> 
				renameToInternal(uri, user, pass, fromFile, toFile, isFtps, port, isPassive)
			);
			new Thread(task).start();
			try {
				return task.get();
			}
			catch (Exception e) {
				Logcat.e(logLevel, "FTP renameTo async task error: " + e.getMessage());
				return false;
			}
		}
		else {
			return renameToInternal(uri, user, pass, fromFile, toFile, isFtps, port, isPassive);
		}
	}
	// renameTo の実処理本体
	private static boolean renameToInternal(String uri, String user, String pass, String fromFile, String toFile, boolean isFtps, String port, boolean isPassive) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		FTPClient client = null;
		try {
			client = connectAndLogin(uri, user, pass, isFtps, port, isPassive);
			// リモート絶対パスから親ディレクトリのパスを正しく構築する
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

			Logcat.d(logLevel, "FTP rename: from [" + fromPath + "] to [" + toPath + "]");
			boolean success = client.rename(fromPath, toPath);
			if (!success) {
				Logcat.e(logLevel, "FTP rename failed on server. Reply: " + client.getReplyString());
			}
			return success;
		}
		catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append("FTP renameTo exception: ").append(e.getClass().getName()).append(": ").append(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				sb.append("\n  at ").append(elem.toString());
			}
			Logcat.e(logLevel, sb.toString());
			return false;
		}
		finally {
			if (client != null && client.isConnected()) {
				try {
					client.logout();
					client.disconnect();
				}
				catch (IOException ignored) {}
			}
		}
	}
	// ファイルの最終更新日時(タイムスタンプ)を取得
	public static long date(String uri, String user, String pass, boolean isFtps, String port, boolean isPassive) {
		FTPClient client = null;
		try {
			client = connectAndLogin(uri, user, pass, isFtps, port, isPassive);
			String remotePath = parsePath(uri);
			// MDTMコマンドを使用してサーバーから正確な最終更新日時文字列を取得する
			String timeStr = client.getModificationTime(remotePath);
			if (timeStr != null && !timeStr.isEmpty()) {
				// サーバーが返すMDTM文字列をデバイスのローカルタイムゾーンで正しくパースする(9時間ズレを防止)
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
				// サーバー基準のUTC
				sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
				java.util.Date date = sdf.parse(timeStr);
				if (date != null) {
					return date.getTime();
				}
			}
			// MDTMが使えない場合のフォールバック
			org.apache.commons.net.ftp.FTPClientConfig config = new org.apache.commons.net.ftp.FTPClientConfig();
			// listFiles()ととタイムゾーンを完全一致させる
			config.setServerTimeZoneId("UTC");
			client.configure(config);
			int lastSlash = remotePath.lastIndexOf('/');
			String parentDir = lastSlash >= 0 ? remotePath.substring(0, lastSlash) : "";
			String fileName = lastSlash >= 0 ? remotePath.substring(lastSlash + 1) : remotePath;
			FTPFile[] files = client.listFiles(parentDir);
			if (files != null) {
				for (FTPFile file : files) {
					if (file != null && file.getName().equals(fileName) && file.getTimestamp() != null) {
						return file.getTimestamp().getTimeInMillis();
					}
				}
			}
		}
		catch (Exception ignored) {}
		finally {
			if (client != null && client.isConnected()) {
				try {
					client.logout();
					client.disconnect();
				}
				catch (IOException ignored) {}
			}
		}
		return 0L;
	}
	// 空のファイルを作成
	public static boolean createFile(String uri, String user, String pass, String item, boolean isFtps, String port, boolean isPassive) {
		FTPClient client = null;
		try {
			client = connectAndLogin(uri, user, pass, isFtps, port, isPassive);
			String targetPath = relativePath(uri, item);
			// 空のストリームを書き込むことでファイルを作成
			java.io.ByteArrayInputStream emptyInput = new java.io.ByteArrayInputStream(new byte[0]);
			return client.storeFile(targetPath, emptyInput);
		}
		catch (Exception e) {
			return false;
		}
		finally {
			if (client != null && client.isConnected()) {
				try {
					client.logout();
					client.disconnect();
				}
				catch (IOException ignored) {}
			}
		}
	}
	// 指定したリモートファイルをローカルの宛先ファイルへ一括で安全にダウンロードする
	public static boolean downloadFile(String uri, String user, String pass, boolean isFtps, String portNumber, boolean isPassive, java.io.File destFile) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		FTPClient client = null;
		try {
			String remotePath = parsePath(uri);
			Logcat.d(logLevel, "FTP Download Target URI: " + uri);
			Logcat.d(logLevel, "FTP Download Parsed RemotePath: " + remotePath);
			client = connectAndLogin(uri, user, pass, isFtps, portNumber, isPassive);
			try (OutputStream out = new java.io.FileOutputStream(destFile)) {
				boolean success = client.retrieveFile(remotePath, out);
				out.flush();
				int replyCode = client.getReplyCode();
				String replyString = client.getReplyString();
				Logcat.d(logLevel, "FTP retrieveFile result: " + success + ", ReplyCode: " + replyCode + ", Reply: " + replyString.trim());
				return success;
			}
		}
		catch (Exception e) {
			Logcat.e(logLevel, "FTP downloadFile error: " + e.getMessage(), e);
			return false;
		}
		finally {
			if (client != null && client.isConnected()) {
				try {
					client.logout();
					client.disconnect();
				}
				catch (IOException ignored) {}
			}
		}
	}
}
