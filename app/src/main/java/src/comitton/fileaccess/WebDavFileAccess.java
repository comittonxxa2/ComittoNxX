package src.comitton.fileaccess;

import android.app.Activity;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.TrustManager;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.util.Log;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.fileview.FileSelectActivity;
import src.comitton.fileview.data.FileData;

public class WebDavFileAccess {
	private static final String TAG = "WebDavFileAccess";

	public static Sardine getSardine(String user, String pass) {
		OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
		// 自己署名証明書を許可
		try {
			// すべての証明書を信頼するTrustManagerの定義
			final TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType) {}
					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType) {}
					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[]{};
					}
				}
			};
			// SSLContextの初期化
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			clientBuilder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
			// ホスト名の検証をスキップする場合
			clientBuilder.hostnameVerifier((hostname, session) -> true);
		}
		catch (Exception e) {
			Logcat.e(Logcat.LOG_LEVEL_WARN, "Failed to configure trust all certs: ", e);
		}
		// Basic認証インタープターの追加
		if (user != null && !user.isEmpty()) {
			String userPass = user + ":" + (pass != null ? pass : "");
			String base64UserPass = Base64.encodeToString(userPass.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
			final String basicAuth = "Basic " + base64UserPass;
			clientBuilder.addInterceptor(new Interceptor() {
				@Override
				public Response intercept(Chain chain) throws IOException {
					Request originalRequest = chain.request();
					Request authenticatedRequest = originalRequest.newBuilder()
						.header("Authorization", basicAuth)
						.build();
					return chain.proceed(authenticatedRequest);
				}
			});
		}
		OkHttpClient client = clientBuilder.build();
		return new OkHttpSardine(client);
	}

	public static String filename(@NonNull String uri) {
		try {
			URI u = new URI(uri);
			String path = u.getPath();
			if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
			int idx = path.lastIndexOf('/');
			return idx >= 0 ? URLDecoder.decode(path.substring(idx + 1), "UTF-8") : path;
		}
		catch (Exception e) {
			return uri;
		}
	}

	public static String parent(@NonNull String uri) {
		try {
			if (uri.endsWith("/")) uri = uri.substring(0, uri.length() - 1);
			int idx = uri.lastIndexOf('/');
			return idx >= 0 ? uri.substring(0, idx + 1) : uri;
		}
		catch (Exception e) {
			return uri;
		}
	}

	public static String relativePath(@NonNull String base, @NonNull String target) {
		if (target.startsWith("http://")) {
			return target;
		}
		if (!base.endsWith("/")) base += "/";
		return base + target;
	}

	public static long length(@NonNull String uri, @NonNull String user, @NonNull String pass) throws FileAccessException {
		try {
			Sardine sardine = getSardine(user, pass);
			List<DavResource> resources = sardine.list(uri, 0);
			if (!resources.isEmpty()) {
				return resources.get(0).getContentLength();
			}
		}
		catch (IOException e) {
			throw new FileAccessException("WebDAV length error: " + e.getMessage());
		}
		return 0;
	}

	public static ParcelFileDescriptor openParcelFileDescriptor(Context context, String url, String user, String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
			ParcelFileDescriptor readSide = pipe[0];
			ParcelFileDescriptor writeSide = pipe[1];

			new Thread(() -> {
				// Pipe書き込み用スレッド
				try (InputStream in = WebDavFileAccess.getInputStream(url, user, pass);
					 OutputStream out = new ParcelFileDescriptor.AutoCloseOutputStream(writeSide)) {

					byte[] buffer = new byte[8192];
					int len;
					while ((len = in.read(buffer)) != -1) {
						out.write(buffer, 0, len);
					}
					out.flush();
				}
				catch (Exception e) {
					Logcat.e(logLevel, "Error streaming WebDAV data: ", e);
					// 異常終了時もパイプを確実に破完させて読み取り側のブロックを解除する
					try {
						writeSide.closeWithError(e.getMessage());
					} catch (IOException ignored) {}
				}
			}).start();

			return readSide;
		}
		catch (IOException e) {
			throw new FileAccessException("Failed to create ParcelFileDescriptor for WebDAV : " + e.getMessage());
		}
	}

	public static WebDavRandomAccessFile openRandomAccessFile(@NonNull String uri, @NonNull String user, @NonNull String pass, @NonNull String mode) throws FileAccessException {
		try {
			return new WebDavRandomAccessFile(uri, user, pass);
		}
		catch (IOException e) {
			throw new FileAccessException("WebDAV open error: " + e.getMessage());
		}
	}

	public static InputStream getInputStream(@NonNull String uri, @NonNull String user, @NonNull String pass) throws FileAccessException {
		try {
			String normalizedUri = WebDavUtil.normalizeUri(uri);
			Sardine sardine = getSardine(user, pass);
			return sardine.get(normalizedUri);
		}
		catch (IOException e) {
			throw new FileAccessException("WebDAV getInputStream error: " + e.getMessage());
		}
	}

	public static OutputStream getOutputStream(@NonNull String uri, @NonNull String user, @NonNull String pass) throws FileAccessException {
		throw new FileAccessException("WebDAV write is not supported directly via OutputStream");
	}

	public static boolean exists(@NonNull String uri, @NonNull String user, @NonNull String pass) {
		try {
			Sardine sardine = getSardine(user, pass);
			return sardine.exists(uri);
		}
		catch (Exception e) {
			return false;
		}
	}

	public static boolean isDirectory(@NonNull String uri, @NonNull String user, @NonNull String pass) {
		try {
			Sardine sardine = getSardine(user, pass);
			List<DavResource> resources = sardine.list(uri, 0);
			if (!resources.isEmpty()) {
				return resources.get(0).isDirectory();
			}
		}
		catch (Exception e) {
			// 無視
		}
		return false;
	}

	public static ArrayList<FileData> listFiles(@NonNull Activity activity, @NonNull String uri, @NonNull String user, @NonNull String pass) throws FileAccessException {
		// WebDAV用に特別に処理(ルートからの絶対パスを返すため)
		// WebDAVのレスポンスを他ストレージと同じ相対名に揃える
		try {
			Sardine sardine = getSardine(user, pass);
			// カレントURLの正規化
			String targetUrl = WebDavUtil.normalizeUri(uri);
			if (!targetUrl.endsWith("/")) {
				targetUrl += "/";
			}
			List<DavResource> resources = sardine.list(targetUrl);
			ArrayList<FileData> fileList = new ArrayList<>();
			// カレントディレクトリ自身のパスを取得
			String currentPath = URI.create(targetUrl).getPath(); 

			for (DavResource res : resources) {
				String resPath = res.getPath();
				if (resPath == null) continue;
				// URLデコード(日本語対策)
				try {
					resPath = URLDecoder.decode(resPath, StandardCharsets.UTF_8.name());
				}
				catch (Exception ignored) {
				}
				// 末尾のスラッシュ表記を揃える
				String normResPath = resPath.endsWith("/") ? resPath : resPath + "/";
				String normCurrentPath = currentPath.endsWith("/") ? currentPath : currentPath + "/";
				// 自分自身(親ディレクトリ)ならスキップ
				if (normResPath.equals(normCurrentPath)) {
					continue;
				}
				// 単体名(相対パス)の抽出
				String cleanPath = resPath.endsWith("/") ? resPath.substring(0, resPath.length() - 1) : resPath;
				String entryName = cleanPath.substring(cleanPath.lastIndexOf('/') + 1);
				boolean isDir = res.isDirectory() || resPath.endsWith("/");
				// 他のストレージ(SMB/File)と全く同じ形式のFileDataを作成
				if (isDir) {
					// ディレクトリの場合
					if (!entryName.endsWith("/")) {
						entryName += "/";
					}
				}
				long size = res.getContentLength();
				long date = (res.getModified() != null) ? res.getModified().getTime() : 0L;
				// リスト表示用に特別に用意(htmlを認識させるため)
				FileData fileData = new FileData(activity, entryName, size, date, true);
				fileList.add(fileData);
			}
			if (!fileList.isEmpty() && !FileSelectActivity.getSkipSortFilelist()) {
				Collections.sort(fileList, new FileAccess.FileDataComparator());
			}
			return fileList;
		}
		catch (IOException e) {
			throw new FileAccessException("WebDAV listFiles error: " + e.getMessage());
		}
	}

	// WebDAV URIの重複スラッシュを修正するヘルパーメソッド
	private static String cleanWebDavUri(String uri) {
		if (uri == null || uri.isEmpty()) {
			return "";
		}
		// スキーム(http:// や https://) 部分の維持処理
		String scheme = "";
		if (uri.startsWith("http://")) {
			scheme = "http://";
			uri = uri.substring(7);
		}
		else if (uri.startsWith("https://")) {
			scheme = "https://";
			uri = uri.substring(8);
		}
		// 2個以上連続するスラッシュを 1個に整形
		uri = uri.replaceAll("/{2,}", "/");
		return scheme + uri;
	}

	// パス文字列の比較用にデコード＆正規化を行うヘルパー
	private static String normalizePath(String uriOrPath) {
		try {
			String path = uriOrPath;
			if (path.startsWith("http://") || path.startsWith("https://")) {
				path = new URI(path).getPath();
			}
			path = URLDecoder.decode(path, "UTF-8");
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			return path;
		}
		catch (Exception e) {
			return uriOrPath;
		}
	}

	public static boolean renameTo(String oldUri, String newUri, String user, String pass) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			Sardine sardine = getSardine(user, pass);
			sardine.move(oldUri, newUri);
			return true;
		}
		catch (IOException e) {
			Logcat.e(logLevel, "renameTo error: ", e);
			return false;
		}
	}

	public static long date(@NonNull String uri, @NonNull String user, @NonNull String pass) {
		try {
			Sardine sardine = getSardine(user, pass);
			List<DavResource> resources = sardine.list(uri, 0);
			if (!resources.isEmpty() && resources.get(0).getModified() != null) {
				return resources.get(0).getModified().getTime();
			}
		}
		catch (Exception e) {
			// 無視
		}
		return 0L;
	}

	public static boolean delete(@NonNull String uri, @NonNull String user, @NonNull String pass) {
		try {
			Sardine sardine = getSardine(user, pass);
			sardine.delete(uri);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public static boolean mkdir(@NonNull String uri, @NonNull String user, @NonNull String pass, @NonNull String item) {
		try {
			Sardine sardine = getSardine(user, pass);
			if (!uri.endsWith("/")) uri += "/";
			sardine.createDirectory(uri + item);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public static boolean createFile(@NonNull String uri, @NonNull String user, @NonNull String pass, @NonNull String item) {
		try {
			Sardine sardine = getSardine(user, pass);
			if (!uri.endsWith("/")) uri += "/";
			byte[] emptyData = new byte[0];
			sardine.put(uri + item, emptyData);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}
