package src.comitton.WebSharing;

import static src.comitton.WebSharing.NetworkUtils.getWiFiIpAddress;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import androidx.preference.PreferenceManager;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLServerSocketFactory;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;

import fi.iki.elonen.NanoHTTPD;
import src.comitton.config.SetWebServerActivity;

public class WebFilerServer extends NanoHTTPD {
	private final Context mContext;
	private final String mRootPath;
	private final String mDefaultTheme;
	private final String mBulkdownload;
	private final boolean mPopupImage;
	private final boolean mReadonly;
	private int mScrolly = 0;
	private final SharedPreferences sharedPreferences;
	// SecureRandom と nonce管理用の変数
	private static final SecureRandom secureRandom = new SecureRandom();
	private final ConcurrentHashMap<String, Long> activeNonces = new ConcurrentHashMap<>();
	// nonceの有効期限(例: 5分 = 300,000ミリ秒)
	private static final long NONCE_EXPIRE_TIME = 5 * 60 * 1000;
	// strings.xmlで日本語と英語を別々に定義するのが面倒なので作成した
	private Map<String, String> getMessages(boolean isEnglish) {
		Map<String, String> m = new HashMap<>();
		if (isEnglish) {
			m.put("title", "Web Share");
			m.put("selectAll", "Select All");
			m.put("size", "Size");
			m.put("modified", "Modified");
			m.put("directory", "Directory");
			m.put("download", "📥 Download");
			m.put("startDownload", "📥 Start download...");
			m.put("delete", "🗑️ Delete");
			m.put("rename", "✏️ Rename");
			m.put("darkDisplay", "🌙 Dark display");
			m.put("lightDisplay", "☀️ Light display");
			m.put("zipping", "⏳ Zipping & Downloading...");
			m.put("error", "Error loading list");
			m.put("dirNotSupported", "Direct folder download is not supported.");
			m.put("busyWait", "⏳ Processing... Please wait.");
			m.put("wanttoDelete", "Are you sure you want to delete this?");
			m.put("newName", "Please enter a new name");
			m.put("switchGridview", "🔲 Switch to grid view");
			m.put("switchListview", "📋 Switch to list view");
			m.put("compressDownload", "⏳ Compressing and downloading...");
			m.put("communicationError", "A communication error has occurred");
			m.put("failDelete", "Deletion failed.");
			m.put("failDownload", "Download failed");
			m.put("failUpload", "Upload failed.");
			m.put("deletion", "🗑️ Deletion in progress...");
			m.put("saving", "Saving (Writing to SD card)...");
			m.put("selected", "Selected");
			m.put("wanttoDeleteItem", "Do you want to delete the item in question?");
			m.put("Upload", "Upload");
			m.put("Uploading", "Uploading...");
			m.put("UploadingFile", "📥 Uploading files...");
			m.put("noFileSelect", "No file selected.");
			m.put("Select", "Select");
			m.put("Name", "Name");
			m.put("removeSelect", "🗑️ Remove selection");
			m.put("downloadSelect", "📥 Download selected files compressed into a ZIP archive.");
			m.put("descending", "Descending order");
			m.put("ascending", "Ascending order");
			m.put("extension", "Extension");
			m.put("dropFile", "📥 Drop file here");
			m.put("folderName", "Folder name");
			m.put("fileName", "File name");
			m.put("returnLevel", "📁 [..] Return to the previous level");
			m.put("treeError", "Tree error");
			m.put("Folder", "📁 Folder");
			m.put("Sort", "Sort");
			m.put("FileOnly", "Folder drop-and-drop is not supported. Please select files only.");
			m.put("reload", "Reload");
			m.put("Updating", "⏳ Updating...");
			m.put("Loading", "Loading...");
			m.put("failLoadimage", "Image loading failed.");
			m.put("failRetrievefile", "Failed to retrieve file.");
			m.put("LoadError", "Error: ");
			m.put("ArchiveViewer", "Archive Viewer: ");
			m.put("LoadArchiveError", "Archive file loading error: ");
			m.put("ReturnFilelist", "← Return to file list");
			m.put("ArchiveFileNotExist", "The Archive file does not exist.");
			m.put("EntryNotFound", "Entry not found");
			m.put("ArchivePathInvalid", "The Archive file path is invalid.");
		}
		else {
			m.put("title", "Web共有");
			m.put("selectAll", "すべて選択");
			m.put("size", "サイズ");
			m.put("modified", "更新日時");
			m.put("directory", "ディレクトリ");
			m.put("download", "📥 ダウンロード");
			m.put("startDownload", "📥 ダウンロードを開始します...");
			m.put("delete", "🗑️ 削除");
			m.put("rename", "✏️ 名称変更");
			m.put("darkDisplay", "🌙 ダーク表示");
			m.put("lightDisplay", "☀️ ライト表示");
			m.put("zipping", "⏳ ZIP圧縮・ダウンロード中...");
			m.put("error", "一覧取得エラー");
			m.put("dirNotSupported", "フォルダの直接ダウンロードはサポートされていません");
			m.put("busyWait", "⏳ 処理中... しばらくお待ちください");
			m.put("wanttoDelete", "を削除してもよろしいですか？");
			m.put("newName", "新しい名前を入力してください");
			m.put("switchGridview", "🔲 グリッド表示へ切り替え");
			m.put("switchListview", "📋 リスト表示へ切り替え");
			m.put("compressDownload", "⏳ 圧縮・ダウンロード中...");
			m.put("communicationError", "通信エラーが発生しました");
			m.put("failDelete", "削除に失敗しました");
			m.put("failDownload", "ダウンロードに失敗しました");
			m.put("failUpload", "アップロードに失敗しました");
			m.put("deletion", "🗑️ 削除処理中...");
			m.put("saving", "保存中 (SDカードへ書き込み中)...");
			m.put("selected", "選択された");
			m.put("wanttoDeleteItem", "件のアイテムを削除しますか？");
			m.put("Upload", "アップロード");
			m.put("Uploading", "アップロード中...");
			m.put("UploadingFile", "📥 ファイルをアップロード中...");
			m.put("noFileSelect", "ファイルが選択されていません。");
			m.put("Select", "選択");
			m.put("Name", "名前");
			m.put("removeSelect", "🗑️ 選択を削除");
			m.put("downloadSelect", "📥 選択をZIP圧縮してダウンロード");
			m.put("descending", "降順");
			m.put("ascending", "昇順");
			m.put("extension", "拡張子");
			m.put("dropFile", "📥 ここにファイルをドロップ");
			m.put("folderName", "フォルダ名");
			m.put("fileName", "ファイル名");
			m.put("returnLevel", "📁 [..] 上の階層へ戻る");
			m.put("treeError", "ツリーエラー");
			m.put("Folder", "📁 フォルダ");
			m.put("Sort", "並び替え");
			m.put("FileOnly", "フォルダのドロップはサポートされていません。ファイルのみを選択してください。");
			m.put("reload", "更新");
			m.put("Updating", "⏳ 更新中...");
			m.put("Loading", "読み込み中...");
			m.put("failLoadimage", "画像の読み込みに失敗しました");
			m.put("failRetrievefile", "ファイルの取得に失敗しました");
			m.put("LoadError", "エラー: ");
			m.put("ArchiveViewer", "書庫ビューア: ");
			m.put("LoadArchiveError", "書庫ファイルの読み込みエラー: ");
			m.put("ReturnFilelist", "← ファイル一覧に戻る");
			m.put("ArchiveFileNotExist", "書庫ファイルが存在しません");
			m.put("EntryNotFound", "エントリが見つかりません");
			m.put("ArchivePathInvalid", "書庫ファイルのパスが不正です");
		}
		return m;
	}
	// 英語環境かどうかの判定
	private boolean isEnglish(IHTTPSession session) {
		String acceptLang = session.getHeaders().get("accept-language");
		if (acceptLang != null) {
			String lower = acceptLang.toLowerCase();
			// 英語が含まれていて、かつ日本語より優先されている場合
			if (lower.contains("en") && !lower.startsWith("ja")) {
				return true;
			}
		}
		// デフォルトは日本語
		return false;
	}
	// コンストラクタ
	public WebFilerServer(int port, Context context, String rootPath, String defaultTheme, String bulkdownload, boolean popupimage) {
		super(port);
		int logLevel = Logcat.LOG_LEVEL_WARN;
		mContext = context;
		mRootPath = rootPath;
		mDefaultTheme = (defaultTheme != null && !defaultTheme.isEmpty()) ? defaultTheme : "light";
		mBulkdownload = bulkdownload + ".zip";
		mPopupImage = popupimage;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		mReadonly = SetWebServerActivity.getWebServerReadonly(sharedPreferences);

		Logcat.v(logLevel, "IpAddress=" + getWiFiIpAddress(mContext) + ", port=" + port + ", rootPath=" + rootPath + ", defaultTheme=" + mDefaultTheme);
		if (SetWebServerActivity.getWebServerTls(sharedPreferences)) {
			// ここで動的自己署名証明書を生成してHTTPS化を有効化する
			try {
				initSecureServer();
			}
			catch (Exception e) {
				Logcat.e(logLevel, "Failed to initialize HTTPS (SSL/TLS)", e);
			}
		}
	}
	// コンストラクタ
	public WebFilerServer(int port, Context context, String rootPath) {
		this(port, context, rootPath, "light", "archive", false);
	}
	// Bouncy Castleを使ってメモリ上で動的に自己署名証明書を生成し、NanoHTTPDに適用する
	private void initSecureServer() throws Exception {
		char[] password = "dynamic_secure_password".toCharArray();
		// 空のKeyStoreをメモリ上に作成
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(null, null);
		// RSA鍵ペア(公開鍵・秘密鍵)を動的生成
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(2048);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		// X.509 自己署名証明書のパラメータ設定
		X500Name issuer = new X500Name("CN=localhost, O=ComittonAndroid, C=JP");
		BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
		 // 1時間前
		Date notBefore = new Date(System.currentTimeMillis() - 1000L * 60L * 60L);
		// 1年後
		Date notAfter = new Date(System.currentTimeMillis() + 365L * 24L * 60L * 60L * 1000L);
		// Bouncy Castleを使って証明書をビルド
		JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuer, serialNumber, notBefore, notAfter, issuer, keyPair.getPublic());
		ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(keyPair.getPrivate());
		X509CertificateHolder certHolder = certBuilder.build(contentSigner);
		X509Certificate certificate = new JcaX509CertificateConverter()
				.getCertificate(certHolder);
		// 生成した秘密鍵と証明書をKeyStoreに登録
		keyStore.setKeyEntry("myserver", keyPair.getPrivate(), password, new Certificate[]{certificate});
		// KeyManagerFactory を経由して KeyManager[] を生成
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, password);
		KeyManager[] keyManagers = kmf.getKeyManagers();
		// NanoHTTPD の SSLソケットファクトリへ適用
		SSLServerSocketFactory sslFactory = NanoHTTPD.makeSSLSocketFactory(keyStore, keyManagers);
		makeSecure(sslFactory, null);
	}
	// ファイルアップロードや大容量ファイルのダウンロード時に通信が途中でタイムアウト(切断)してしまわないように、ソケットの制限時間を5分(300秒)に延長
	@Override
	public NanoHTTPD.ClientHandler createClientHandler(final java.net.Socket finalSocket, final InputStream inputStream) {
		// 通信のタイムアウト時間を延長する処理
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			// ルーター経由での大容量ファイル転送でタイムアウトしないように5分(300秒)に設定
			finalSocket.setSoTimeout(300 * 1000);
		}
		catch (Exception e) {
			Logcat.e(logLevel, "Failed to set socket timeout", e);
		}
		// 通常の処理へ引き継ぐ
		return super.createClientHandler(finalSocket, inputStream);
	}
	// 一時ファイルが勝手に削除されるのを防ぐファクトリー
	@Override
	public NanoHTTPD.TempFileManagerFactory getTempFileManagerFactory() {
		final File cacheDir = mContext.getCacheDir();
		return new NanoHTTPD.TempFileManagerFactory() {
			@Override
			public NanoHTTPD.TempFileManager create() {
				return new PersistentTempFileManager(cacheDir);
			}
		};
	}
	// HTTP通信(ファイルのアップロードなど)の際に一時的に作成されるファイル(テンポラリファイル)の保存場所と管理方法を独自にカスタマイズ(永続化)するためのクラス
	private static class PersistentTempFileManager implements NanoHTTPD.TempFileManager {
		private final File tmpDir;
		private final List<NanoHTTPD.TempFile> tempFiles;
		// コンストラクタ
		public PersistentTempFileManager(File contextCacheDir) {
			tmpDir = new File(contextCacheDir, "NanoHTTPD-tmp");
			if (!tmpDir.exists()) {
				tmpDir.mkdirs();
			}
			tempFiles = new ArrayList<>();
		}
		// 一時ファイルの削除・掃除
		@Override
		public void clear() {
			for (NanoHTTPD.TempFile file : tempFiles) {
				try {
					file.delete();
				}
				catch (Exception ignored) {}
			}
			tempFiles.clear();
		}
		// 一時ファイルの作成
		@Override
		public NanoHTTPD.TempFile createTempFile(String filename_hint) throws Exception {
			NanoHTTPD.DefaultTempFile tempFile = new NanoHTTPD.DefaultTempFile(tmpDir);
			tempFiles.add(tempFile);
			return tempFile;
		}
	}
	// スマホやPCのブラウザからリクエスト(アクセス)が送られてきたときに、どの画面を表示するか、どんなデータを返すかを判定して応答(Response)を生成する処理
	@Override
	public Response serve(IHTTPSession session) {
		// アクセス制御のタイプが入る
		int authType = SetWebServerActivity.getWebServerAuth(sharedPreferences);
		if (authType == DEF.WEBSERVER_AUTH_BASIC) {
			// Basic認証のチェック
			if (!checkBasicAuthentication(session)) {
				// 認証に失敗したらUnauthorizedを返す
				Response response = newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain; charset=UTF-8", "Unauthorized");
				response.addHeader("WWW-Authenticate", "Basic realm=\"WebFilerServer\"");
				return response;
			}
		}
		else if (authType == DEF.WEBSERVER_AUTH_DIGEST) {
			// Digest認証のチェック
			if (!checkDigestAuthentication(session)) {
				// 認証に失敗したらUnauthorizedを返す
				Response response = newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain; charset=UTF-8", "Unauthorized");
				String nonce = generateNonce();
				String realm = "WebFilerServer";
				String authHeaderValue = String.format("Digest realm=\"%s\", qop=\"auth\", nonce=\"%s\", opaque=\"%s\"", realm, nonce, generateOpaque());
				response.addHeader("WWW-Authenticate", authHeaderValue);
				return response;
			}
		}
		// 英語環境かどうかを判定
		boolean isEn = isEnglish(session);
		Map<String, String> lang = getMessages(isEn);
		// メソッドを得る
		Method method = session.getMethod();
		// URLを得る
		String uri = session.getUri();
		// パラメータを得る
		Map<String, String> parms = session.getParms();
		// カレントパスを得る
		String currentPath = parms.containsKey("path") ? parms.get("path") : mRootPath;
		if (currentPath == null || currentPath.isEmpty()) {
			currentPath = mRootPath;
		}
		// URLをデコード
		try {
			currentPath = URLDecoder.decode(currentPath, "UTF-8");
		}
		catch (Exception e) {
			currentPath = mRootPath;
		}
		// パスをサニタイズ
		currentPath = sanitizePath(currentPath);
		// 引数で処理を分岐
		if ("/get-scroll".equals(uri)) {
			// スクロール位置をブラウザに返す
			return newFixedLengthResponse(Response.Status.OK, "text/plain", String.valueOf(mScrolly));
		}
		if ("/save-scroll".equals(uri)) {
			// クエリパラメータからスクロール位置を取得
			String yValue = "0";
			String queryString = session.getQueryParameterString();
			int idx = queryString.indexOf("y=");
			if (idx != -1) {
				yValue = queryString.substring(idx + 2);
				// もし後ろに他のパラメータが続く場合の対策
				int ampIdx = yValue.indexOf("&");
				if (ampIdx != -1) {
					yValue = yValue.substring(0, ampIdx);
				}
				// スクロール位置を保存
				mScrolly = Integer.parseInt(yValue);
			}
			return newFixedLengthResponse(Response.Status.OK, "text/plain", "");		}
		if ("/bulk-download".equals(uri)) {
			return handleBulkDownload(session, lang);
		}
		if ("/bulk-delete".equals(uri)) {
			return handleBulkDelete(session, lang, currentPath);
		}
		if ("/archiveview".equals(uri)) {
			String archivePath = parms.get("path");
			String sortField = parms.get("sort");
			String sortOrder = parms.get("order");
			if (archivePath == null || archivePath.isEmpty()) {
				return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain; charset=UTF-8", lang.get("ArchivePathInvalid"));
			}
			return generateArchiveViewResponse(lang, archivePath, sortField, sortOrder);
		}
		if ("/archivefile".equals(uri)) {
			String archivePath = parms.get("archive");
			String entryName = parms.get("entry");
			return serveArchiveEntryResponse(lang, archivePath, entryName);
		}
		if (Method.POST.equals(method)) {
			if ("/upload".equals(uri)) {
				String sortField = parms.get("sort");
				String sortOrder = parms.get("order");
				return handleUpload(session, lang, currentPath, parms, sortField, sortOrder);
			}
			else if ("/mkdir".equals(uri)) {
				return handleMkdir(session, lang, currentPath, parms);
			}
			else if ("/delete".equals(uri)) {
				return handleDelete(session, lang, currentPath, parms);
			}
			else if ("/rename".equals(uri)) {
				return handleRename(session, lang, currentPath, parms);
			}
		}
		else if (Method.GET.equals(method)) {
			if ("/download".equals(uri)) {
				return handleDownload(lang, parms);
			}
			else {
				return handleBrowse(lang, currentPath, parms);
			}
		}
		// Not Foundを返す
		return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
	}
	// Basic認証の検証
	private boolean checkBasicAuthentication(IHTTPSession session) {
		Map<String, String> headers = session.getHeaders();
		String authHeader = headers.get("authorization");
		if (authHeader != null && authHeader.startsWith("Basic ")) {
			try {
				// 入力されたユーザー名とパスワードを取り出す
				String base64Credentials = authHeader.substring("Basic ".length()).trim();
				byte[] credBytes = Base64.decode(base64Credentials, Base64.DEFAULT);
				String credentials = new String(credBytes, StandardCharsets.UTF_8);
				String[] values = credentials.split(":", 2);
				if (values.length == 2) {
					// ユーザー名とパスワードの突き合わせ
					String inputUsername = values[0];
					String inputPassword = values[1];
					String validUsername = SetWebServerActivity.getWebServerUser(sharedPreferences);
					String validPassword = SetWebServerActivity.getWebServerPass(sharedPreferences);
					if (validUsername == null || validUsername.trim().isEmpty() ||
						validPassword == null || validPassword.trim().isEmpty()) {
						// もしもサーバーのユーザー名とパスワードが空っぽだった場合は強制的に失敗にする
						return false;
					}
					// 比較結果を返す
					return validUsername.equals(inputUsername) && validPassword.equals(inputPassword);
				}
			}
			catch (Exception e) {
				// エラー時は弾く
			}
		}
		return false;
	}
	// SecureRandom を使った安全な nonce 生成とサーバー側への保存
	private String generateNonce() {
		byte[] bytes = new byte[16];
		secureRandom.nextBytes(bytes);
		String nonce = Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP);
		// 発行時刻を記録(古いものの掃除も兼ねる)
		cleanExpiredNonces();
		activeNonces.put(nonce, System.currentTimeMillis());
		return nonce;
	}
	// SecureRandom を使った opaque 生成
	private String generateOpaque() {
		byte[] bytes = new byte[16];
		secureRandom.nextBytes(bytes);
		return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP);
	}
	// 期限切れの nonce を自動で掃除するメソッド
	private void cleanExpiredNonces() {
		long currentTime = System.currentTimeMillis();
		activeNonces.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > NONCE_EXPIRE_TIME);
	}
	// Digest認証の検証
	private boolean checkDigestAuthentication(IHTTPSession session) {
		Map<String, String> headers = session.getHeaders();
		String authHeader = headers.get("authorization");
		if (authHeader != null && authHeader.startsWith("Digest ")) {
			try {
				// Digestパラメータのパース(分解)
				Map<String, String> authParams = parseDigestHeader(authHeader.substring("Digest ".length()));
				String username = authParams.get("username");
				String realm = authParams.get("realm");
				String nonce = authParams.get("nonce");
				String uri = authParams.get("uri");
				String responseHash = authParams.get("response");
				String qop = authParams.get("qop");
				String nc = authParams.get("nc");
				String cnonce = authParams.get("cnonce");
				if (username == null || responseHash == null) {
					return false;
				}
				// この nonce は本当にこのサーバーが発行したものか?期限内か?をチェック
				if (!activeNonces.containsKey(nonce)) {
					// 発行した覚えのない、または不正な nonce
					return false;
				}
				long issuedTime = activeNonces.get(nonce);
				if ((System.currentTimeMillis() - issuedTime) > NONCE_EXPIRE_TIME) {
					// 期限切れの nonce
					activeNonces.remove(nonce);
					return false;
				}
				// ユーザー名の突き合わせ
				String validUsername = SetWebServerActivity.getWebServerUser(sharedPreferences);
				String validPassword = SetWebServerActivity.getWebServerPass(sharedPreferences);
				if (validUsername == null || validUsername.trim().isEmpty() ||
					validPassword == null || validPassword.trim().isEmpty()) {
					// もしもサーバーのユーザー名とパスワードが空っぽだった場合は強制的に失敗にする
					return false;
				}
				if (!validUsername.equals(username)) {
					return false;
				}
				// ハッシュ値(HA1とHA2)の計算
				String ha1 = md5(username + ":" + realm + ":" + validPassword);
				String method = session.getMethod().name();
				String ha2 = md5(method + ":" + uri);
				String expectedResponse;
				// 最終的な応答ハッシュの計算と比較
				if ("auth".equals(qop)) {
					expectedResponse = md5(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2);
				}
				else {
					expectedResponse = md5(ha1 + ":" + nonce + ":" + ha2);
				}
				// 比較結果を返す
				return expectedResponse.equalsIgnoreCase(responseHash);
			}
			catch (Exception e) {
				// パースエラー等
			}
		}
		return false;
	}
	// Digestパラメータのパース(分解)
	private Map<String, String> parseDigestHeader(String headerValue) {
		Map<String, String> map = new HashMap<>();
		String[] pairs = headerValue.split(",\\s*");
		for (String pair : pairs) {
			String[] keyValue = pair.split("=", 2);
			if (keyValue.length == 2) {
				String key = keyValue[0].trim();
				String value = keyValue[1].trim();
				if (value.startsWith("\"") && value.endsWith("\"")) {
					value = value.substring(1, value.length() - 1);
				}
				map.put(key, value);
			}
		}
		return map;
	}
	// MD5ハッシュを生成
	private String md5(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		}
		catch (Exception e) {
			return "";
		}
	}
	// パスをサニタイズする
	private String sanitizePath(String path) {
		try {
			File targetFile = new File(path);
			String canonicalTarget = targetFile.getCanonicalPath();
			String canonicalRoot = new File(mRootPath).getCanonicalPath();
			if (!canonicalTarget.startsWith(canonicalRoot)) {
				return mRootPath;
			}
			return canonicalTarget;
		}
		catch (Exception e) {
			return mRootPath;
		}
	}
	// コードを作成してWebブラウザへレスポンスを返す
	private Response handleBrowse(Map<String, String> lang, String currentPath, Map<String, String> parms) {
		// ベタでHTML/CSS/JavaScriptを記述すると長いコードになった
		String sortField = parms.containsKey("sort") ? parms.get("sort") : "name";
		String sortOrder = parms.containsKey("order") ? parms.get("order") : "asc";
		StringBuilder html = new StringBuilder();
		// Quirks Modeを回避するため必ず DOCTYPE を先頭につける
		html.append("<!DOCTYPE html>");
		html.append("<html><head><meta charset='UTF-8'><title>ComittoNxX ").append(lang.get("title")).append("</title>");
		// ここからJavaScript
		html.append("<script>");
		html.append("  const serverDefaultTheme = '").append(mDefaultTheme).append("';");
		html.append("  let currentTheme = localStorage.getItem('comitton_theme_mode') || serverDefaultTheme;");
		html.append("  if (currentTheme === 'dark') {");
		html.append("	document.documentElement.classList.add('dark-theme');");		// 読み込み瞬時のチラツキ防止
		html.append("  }");
		html.append("</script>");
		// ここまでJavaScript
		// ここからStyle
		// CSS変数(カスタムプロパティ)の定義
		html.append("<style>");
		html.append(":root {");
		html.append("  --bg-color: #f4f6f9;");
		html.append("  --header-bg: #343a40;");
		html.append("  --header-color: #ffffff;");
		html.append("  --sidebar-bg: #ffffff;");
		html.append("  --sidebar-border: #dee2e6;");
		html.append("  --text-color: #333333;");
		html.append("  --text-muted: #777777;");
		html.append("  --path-color: #555555;");
		html.append("  --panel-bg: #ffffff;");
		html.append("  --panel-border: #ced4da;");
		html.append("  --item-bg: #ffffff;");
		html.append("  --item-border: #e0e0e0;");
		html.append("  --item-hover-bg: #f1f7ff;");
		html.append("  --item-hover-border: #007bff;");
		html.append("  --table-th-bg: #f8f9fa;");
		html.append("  --table-border: #eee;");
		html.append("}");
		// ダークテーマ用のカラー上書き定義
		html.append("html.dark-theme {");
		html.append("  --bg-color: #121212;");
		html.append("  --header-bg: #1f1f1f;");
		html.append("  --header-color: #e0e0e0;");
		html.append("  --sidebar-bg: #1e1e1e;");
		html.append("  --sidebar-border: #333333;");
		html.append("  --text-color: #e0e0e0;");
		html.append("  --text-muted: #aaaaaa;");
		html.append("  --path-color: #cccccc;");
		html.append("  --panel-bg: #1e1e1e;");
		html.append("  --panel-border: #444444;");
		html.append("  --item-bg: #1e1e1e;");
		html.append("  --item-border: #333333;");
		html.append("  --item-hover-bg: #2a2a2a;");
		html.append("  --item-hover-border: #007bff;");
		html.append("  --table-th-bg: #252525;");
		html.append("  --table-border: #333333;");
		html.append("}");
		// 基本レイアウトとグローバルスタイル
		html.append("body { font-family: sans-serif; margin: 0; padding: 0; background: var(--bg-color); color: var(--text-color); display: flex; flex-direction: column; height: 100vh; box-sizing: border-box; }");
		html.append("header { background: var(--header-bg); color: var(--header-color); padding: 8px 15px; font-size: 16px; font-weight: bold; flex-shrink: 0; display: flex; justify-content: space-between; align-items: center; }");
		html.append(".container { display: flex; flex: 1; overflow: hidden; }");		// サイドバーとツリーリストのスタイル
		html.append(".sidebar { width: 260px; background: var(--sidebar-bg); border-right: 1px solid var(--sidebar-border); overflow-y: auto; padding: 10px; box-sizing: border-box; flex-shrink: 0; }");
		html.append(".sidebar h3 { font-size: 14px; margin: 0 0 8px 0; color: var(--text-color); border-bottom: 2px solid var(--sidebar-border); padding-bottom: 4px; }");
		html.append(".tree-list { list-style: none; padding-left: 0; margin: 0; }");
		html.append(".tree-list li { margin: 2px 0; }");
		html.append(".tree-list a { text-decoration: none; color: #007bff; display: block; padding: 3px 6px; border-radius: 3px; font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }");
		html.append(".tree-list a:hover { background-color: var(--item-hover-bg); }");
		html.append(".tree-list a.active { background-color: #007bff; color: white; font-weight: bold; }");
		html.append(".sub-tree { padding-left: 12px; border-left: 1px dashed var(--sidebar-border); margin-left: 4px; }");
		// メインコンテンツエリアのスタイル
		html.append(".main-content { flex: 1; padding: 10px 15px; overflow-y: auto; box-sizing: border-box; display: flex; flex-direction: column; }");
		html.append(".path-info { margin: 0 0 6px 0; font-size: 13px; color: var(--path-color); }");
		html.append(".back-link { margin: 0 0 6px 0; font-size: 13px; }");
		html.append(".back-link a { color: #007bff; text-decoration: none; }");
		html.append(".back-link a:hover { text-decoration: underline; }");
		// アクションボタン・フォーム関連
		html.append(".top-actions { display: flex; gap: 8px; margin-bottom: 8px; flex-wrap: wrap; align-items: center; justify-content: space-between; }");
		html.append(".action-group { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }");
		html.append(".action-box, .upload-box, .sort-box { border: 1px solid var(--panel-border); padding: 5px 8px; background: var(--panel-bg); border-radius: 4px; display: flex; align-items: center; gap: 6px; font-size: 13px; margin: 0; }");
		html.append(".action-box input[type='text'], .sort-box select { padding: 3px 6px; font-size: 13px; border: 1px solid var(--panel-border); border-radius: 3px; background: var(--panel-bg); color: var(--text-color); }");
		html.append(".action-box input[type='submit'], .upload-box button, .view-btn, .sort-box button { padding: 3px 8px; font-size: 13px; cursor: pointer; background: #007bff; color: white; border: none; border-radius: 3px; }");
		html.append(".action-box input[type='submit']:hover, .upload-box button:hover, .view-btn:hover, .sort-box button:hover { background: #0056b3; }");
		html.append(".upload-box input[type='file'] { font-size: 12px; color: var(--text-color); }");
		html.append(".upload-submit-btn:disabled { background: #cccccc !important; cursor: not-allowed !important; }");
		// ドラッグアンドドロップエリアのスタイル
		html.append("#dropZone { border: 2px dashed #007bff; border-radius: 6px; padding: 12px; text-align: center; margin-bottom: 8px; background: var(--panel-bg); color: var(--text-color); transition: background 0.2s; }");
		html.append("#dropZone.dragover { background: var(--item-hover-bg); border-color: #28a745; }");
		html.append(".view-toggle, .theme-toggle { background: #6c757d; }");
		html.append(".view-toggle:hover, .theme-toggle:hover { background: #5a6268; }");
		// グリッドビューおよびテーブルビュー
		html.append(".file-container.grid-view { display: grid; grid-template-columns: repeat(auto-fill, minmax(130px, 1fr)); gap: 8px; margin-top: 5px; padding-bottom: 20px; }");
		html.append(".file-container.grid-view .file-item { background: var(--item-bg); border: 1px solid var(--item-border); border-radius: 4px; padding: 8px; text-align: center; cursor: pointer; user-select: none; display: flex; flex-direction: column; justify-content: space-between; transition: all 0.1s ease; }");
		html.append(".file-container.grid-view .file-item:hover { background-color: var(--item-hover-bg); border-color: var(--item-hover-border); box-shadow: 0 1px 4px rgba(0,123,255,0.2); }");
		html.append(".file-container.grid-view .file-icon { font-size: 28px; margin-bottom: 4px; }");
		html.append(".file-container.grid-view .file-name { font-size: 12px; word-break: break-all; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; margin-bottom: 4px; color: var(--text-color); text-decoration: none; }");
		html.append(".file-container.grid-view .file-item a.file-name:hover { color: #007bff; text-decoration: underline; }");
		html.append(".file-container.grid-view .file-size { font-size: 10px; color: var(--text-muted); }");
		html.append(".file-container.table-view { display: block; background: var(--panel-bg); border: 1px solid var(--sidebar-border); border-radius: 4px; overflow-x: auto; margin-top: 5px; padding-bottom: 20px; }");
		html.append(".file-container.table-view table { width: 100%; border-collapse: collapse; text-align: left; font-size: 13px; }");
		html.append(".file-container.table-view th, .file-container.table-view td { padding: 6px 10px; border-bottom: 1px solid var(--table-border); color: var(--text-color); }");
		html.append(".file-container.table-view th { background: var(--table-th-bg); font-weight: bold; color: var(--text-color); }");
		html.append(".file-container.table-view tr.file-item { cursor: pointer; }");
		html.append(".file-container.table-view tr.file-item:hover { background-color: var(--item-hover-bg); }");
		html.append(".file-container.table-view .file-icon { font-size: 16px; margin-right: 6px; vertical-align: middle; }");
		html.append(".file-container.table-view .file-name { color: var(--text-color); text-decoration: none; }");
		html.append(".file-container.table-view .file-item a.file-name:hover { color: #007bff; text-decoration: underline; }");
		html.append(".file-container.table-view .file-size { color: var(--text-muted); font-size: 12px; }");
		// コンテキストメニューとメディアクエリ・ポップアップ
		html.append(".context-menu { display: none; position: absolute; background: var(--panel-bg); border: 1px solid var(--panel-border); box-shadow: 0 4px 12px rgba(0,0,0,0.15); z-index: 1000; border-radius: 4px; overflow: hidden; min-width: 150px; }");
		html.append(".context-menu ul { list-style: none; margin: 0; padding: 0; }");
		html.append(".context-menu li { padding: 8px 12px; cursor: pointer; font-size: 13px; border-bottom: 1px solid var(--table-border); color: var(--text-color); }");
		html.append(".context-menu li:last-child { border-bottom: none; }");
		html.append(".context-menu li:hover { background-color: #007bff; color: white; }");
		html.append("@media (max-width: 768px) {");
		html.append("  .container { flex-direction: column; }");
		html.append("  .sidebar { width: 100%; max-height: 120px; border-right: none; border-bottom: 1px solid var(--sidebar-border); }");
		html.append("}");
		// ポップアップ用CSS
		html.append(".modal { display: none; position: fixed; z-index: 9999; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.8); justify-content: center; align-items: center; }");
		html.append(".modal-content { max-width: 90%; max-height: 90%; object-fit: contain; }");
		// テキストモーダル用のスタイル
		html.append(".text-modal-content { background-color: var(--panel-bg, #fff); color: var(--text-color, #000); padding: 20px; border-radius: 8px; width: 80%; max-width: 800px; max-height: 80vh; overflow-y: auto; box-shadow: 0 4px 20px rgba(0,0,0,0.3); position: relative; }");
		html.append("#modalTextContent { white-space: pre-wrap; font-family: monospace; margin: 0; font-size: 14px; }");
		html.append(".close-btn { position: absolute; top: 20px; right: 30px; color: #fff; font-size: 40px; font-weight: bold; cursor: pointer; z-index: 10000; }");
		html.append(".text-close-btn { position: absolute; top: 10px; right: 15px; font-size: 24px; cursor: pointer; color: var(--text-color, #333); }");
		// PDF・動画・音声・HTML用モーダル用のスタイル
		html.append(".media-modal-content { background-color: var(--panel-bg, #fff); color: var(--text-color, #000); padding: 20px; border-radius: 8px; width: 85%; max-width: 900px; height: 80vh; display: flex; flex-direction: column; justify-content: center; align-items: center; position: relative; box-shadow: 0 4px 20px rgba(0,0,0,0.3); }");
		html.append(".media-modal-content iframe, .media-modal-content video { width: 100%; height: 100%; border: none; border-radius: 4px; }");
		html.append(".media-modal-content audio { width: 80%; margin-top: 20px; }");
		html.append("</style></head>");
		// ここまでStyle
		// ここからHTML
		// ヘッダーとテーマ切り替えボタンの構築
		html.append("<header>");
		html.append("<span>ComittoNxX ").append(lang.get("title")).append("</span>");
		html.append("<button id='themeToggleBtn' class='view-btn theme-toggle' onclick='toggleThemeMode()'>").append(lang.get("lightDisplay")).append("</button>");
		html.append("</header>");
		html.append("<body>");
		html.append("<div class='container'>");
		// サイドバー(フォルダツリー)の構築
		html.append("<div class='sidebar'>");
		html.append("<h3>").append(lang.get("Folder")).append("</h3>");
		html.append("<ul class='tree-list'>");

		try {
			File rootDir = new File(mRootPath);
			String rootName = rootDir.getName().isEmpty() ? "Root" : rootDir.getName();
			boolean isRootActive = rootDir.getCanonicalPath().equals(currentPath);
			
			String treeQuery = "?path=" + URLEncoder.encode(rootDir.getAbsolutePath(), "UTF-8") + "&sort=" + sortField + "&order=" + sortOrder;
			html.append("<li><a href='").append(treeQuery).append("' class='").append(isRootActive ? "active" : "").append("'>🏠 ").append(rootName).append("</a>");
			html.append("<ul class='tree-list sub-tree'>");
			buildFolderTreeHtml(rootDir, currentPath, sortField, sortOrder, html);
			html.append("</ul></li>");
		}
		catch (Exception e) {
			html.append("<li>").append(lang.get("treeError")).append("</li>");
		}

		html.append("</ul>");
		html.append("</div>");
		// メインコンテンツエリアとパンくず・戻るリンク
		html.append("<div class='main-content'>");
		html.append("<div class='path-info'>Current: <b>").append(currentPath).append("</b></div>");

		File fileObj = new File(currentPath);
		String parentPath = fileObj.getParent();

		try {
			String canonicalCurrent = fileObj.getCanonicalPath();
			String canonicalRoot = new File(mRootPath).getCanonicalPath();
			if (parentPath != null && canonicalCurrent.length() > canonicalRoot.length() && canonicalCurrent.startsWith(canonicalRoot)) {
				String parentQuery = "/?path=" + URLEncoder.encode(parentPath, "UTF-8") + "&sort=" + sortField + "&order=" + sortOrder;
				html.append("<div class='back-link'><a href='").append(parentQuery).append("'>").append(lang.get("returnLevel")).append("</a></div>");
			}
		}
		catch (Exception e) {
			if (parentPath != null && !currentPath.equals(mRootPath)) {
				try {
					String parentQuery = "/?path=" + URLEncoder.encode(parentPath, "UTF-8") + "&sort=" + sortField + "&order=" + sortOrder;
					html.append("<div class='back-link'><a href='").append(parentQuery).append("'>").append(lang.get("returnLevel")).append("</a></div>");
				}
				catch (Exception ignored) {}
			}
		}
		// 上部アクションボタン(フォルダ作成・アップロード・ソートUI)
		html.append("<div class='top-actions'>");
		html.append("<div class='action-group'>");
		if (!mReadonly) {
			// フォルダ作成フォーム
			html.append("<div class='action-box'>");
			html.append("<form action='/mkdir?path=").append(URLEncoder.encode(currentPath)).append("&sort=").append(sortField).append("&order=").append(sortOrder).append("' method='POST' style='margin:0; display:flex; gap:4px;'>");
			html.append("<input type='text' name='dirname' placeholder='").append(lang.get("folderName")).append("' required>");
			html.append("<input type='submit' value='作成'>");
			html.append("</form></div>");
			// ドラッグアンドドロップエリア & アップロードUI
			html.append("<div id='dropZone'>");
			html.append("  <span>").append(lang.get("dropFile")).append("</span>");
			html.append("  <div class='upload-box' style='margin:0; border:none; background:transparent; padding:0;'>");
			html.append("	<div style='margin:0; display:flex; gap:6px; align-items:center;'>");
			html.append("	  <input type='file' name='file' id='fileInput' multiple>");
			html.append("	  <button type='button' id='uploadSubmitBtn' class='upload-submit-btn' disabled onclick='uploadFiles()' style='background:#28a745; padding:4px 10px; font-size:12px;'>").append(lang.get("Upload")).append("</button>");
			html.append("	  <span id='fileSelectInfo' style='font-size:11px; color:#28a745; font-weight:bold;'></span>");
			html.append("	  <span id='uploadStatus' style='font-size:11px; color:#007bff; display:none;'>").append(lang.get("Uploading")).append("</span>");
			html.append("	</div>");
			html.append("  </div>");
			html.append("</div>");
		}
		// ソート選択UI
		html.append("<div class='sort-box'>");
		html.append("<select id='sortFieldSelect'>");
		html.append("<option value='name' ").append("name".equals(sortField) ? "selected" : "").append(">").append(lang.get("Name")).append("</option>");
		html.append("<option value='size' ").append("size".equals(sortField) ? "selected" : "").append(">").append(lang.get("size")).append("</option>");
		html.append("<option value='date' ").append("date".equals(sortField) ? "selected" : "").append(">").append(lang.get("modified")).append("</option>");
		html.append("<option value='ext' ").append("ext".equals(sortField) ? "selected" : "").append(">").append(lang.get("extension")).append("</option>");
		html.append("</select>");
		html.append("<select id='sortOrderSelect'>");
		html.append("<option value='asc' ").append("asc".equals(sortOrder) ? "selected" : "").append(">").append(lang.get("ascending")).append("</option>");
		html.append("<option value='desc' ").append("desc".equals(sortOrder) ? "selected" : "").append(">").append(lang.get("descending")).append("</option>");
		html.append("</select>");
		html.append("<button type='button' onclick='applySort()'>").append(lang.get("Sort")).append("</button>");
		html.append("</div>");
		// 通知・トースト、ビジーオーバーレイ、一括操作用ボタン群
		html.append("</div>");
		// ダウンロード開始時のトースト表示
		html.append("<div id='toastMessage' style='display:none; position:fixed; bottom:20px; right:20px; background:rgba(0,0,0,0.85); color:white; padding:10px 16px; border-radius:6px; font-size:13px; z-index:9999; box-shadow: 0 4px 12px rgba(0,0,0,0.3);'>").append(lang.get("startDownload")).append("</div>");
		// ビジー時のオーバーレイ表示
		html.append("<div id='busyOverlay' style='display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.4); z-index:99999; justify-content:center; align-items:center; color:white; font-size:18px; font-weight:bold; backdrop-filter:blur(2px);'>");
		html.append("<div style='background:rgba(0,0,0,0.8); padding:20px 30px; border-radius:8px; box-shadow:0 4px 15px rgba(0,0,0,0.3);'>").append(lang.get("busyWait")).append("</div>");
		html.append("</div>");
		// 表示切り替えボタン
		html.append("<button id='viewToggleBtn' class='view-btn view-toggle' onclick='toggleViewMode()'>").append(lang.get("switchListview")).append("</button>");
		// すべて選択用のチェックボックス
		html.append("<label style='cursor:pointer; display:flex; align-items:center; gap:4px; font-size:13px;'><input type='checkbox' id='selectAllCheckbox' onclick='toggleSelectAll(this)'> ").append(lang.get("selectAll")).append("</label>");
		// 一括ダウンロードボタン
		html.append("  <button type='button' id='bulkDownloadBtn' class='view-btn' style='background:#17a2b8;' disabled onclick='bulkDownload()'>").append(lang.get("downloadSelect")).append("</button>");
		if (!mReadonly) {
			// 一括削除ボタン
			html.append("  <button type='button' id='bulkDeleteBtn' class='view-btn' style='background:#dc3545;' disabled onclick='bulkDelete()'>").append(lang.get("removeSelect")).append("</button>");
		}
		// 更新ボタン
		html.append("  <button type='button' id='reloadBtn' class='view-btn' style='background:#6c757d;' onclick='reloadPage()'>🔄 ").append(lang.get("reload")).append("</button>");
		html.append("</div>");
		// ファイル一覧表示エリア(グリッドビューおよびテーブルビュー)
		html.append("<div id='fileContainer' class='file-container grid-view'>");
		// ファイル一覧表示
		try {
			File dir = new File(currentPath);
			File[] files = dir.listFiles();

			if (files != null) {
				sortFiles(files, sortField, sortOrder);

				html.append("<div id='gridItems' class='sub-view-grid' style='display:contents;'>");
				for (File file : files) {
					String name = file.getName();
					String fullPath = file.getAbsolutePath();
					boolean isDir = file.isDirectory();
					String subQuery = URLEncoder.encode(fullPath, "UTF-8") + "&sort=" + sortField + "&order=" + sortOrder;
					// 判定とダウンロードURLの作成
					boolean isImage = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".gif") || name.toLowerCase().endsWith(".webp") || name.toLowerCase().endsWith(".avif") || name.toLowerCase().endsWith(".bmp") || name.toLowerCase().endsWith(".svg"));
					boolean isText = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".txt") || name.toLowerCase().endsWith(".log") || name.toLowerCase().endsWith(".json") || name.toLowerCase().endsWith(".csv") || name.toLowerCase().endsWith(".md") || name.toLowerCase().endsWith(".js") || name.toLowerCase().endsWith(".java") || name.toLowerCase().endsWith(".py"));
					boolean isPdf = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".pdf"));
					boolean isVideo = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".mp4") || name.toLowerCase().endsWith(".webm") || name.toLowerCase().endsWith(".mov") || name.toLowerCase().endsWith(".ogv") || name.toLowerCase().endsWith(".m4v"));
					boolean isAudio = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".mp3") || name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".m4a") || name.toLowerCase().endsWith(".aac") || name.toLowerCase().endsWith(".ogg") || name.toLowerCase().endsWith(".flac") || name.toLowerCase().endsWith(".opus") || name.toLowerCase().endsWith(".weba"));
					boolean isHtml = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".htm") || name.toLowerCase().endsWith(".xml"));
					boolean isArchive = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".rar") || name.toLowerCase().endsWith(".7z") || name.toLowerCase().endsWith(".tar") || name.toLowerCase().endsWith(".cab") || name.toLowerCase().endsWith(".lzh"));
					// 判定のいずれかに引っかかった場合は fileUrl を付与
					boolean isPopupable = isImage || isText || isPdf || isVideo || isAudio || isHtml;
					String fileUrl = "/download?path=" + URLEncoder.encode(fullPath, "UTF-8");
					String archiveViewUrl = "/archiveview?path=" + URLEncoder.encode(fullPath, "UTF-8") + "&sort=" + sortField + "&order=" + sortOrder;
					// アイコンを自動判定
					String icon = getFileIcon(file);
					// グリッドのカード
					html.append("<div class='file-item'")
						.append(" data-path='").append(fullPath).append("'")
						.append(" data-name='").append(name).append("'")
						.append(" data-isdir='").append(isDir ? "true" : "false").append("'")
						.append(" data-isimage='").append(isImage ? "true" : "false").append("'")
						.append(" data-istext='").append(isText ? "true" : "false").append("'")
						.append(" data-ispdf='").append(isPdf ? "true" : "false").append("'")
						.append(" data-isvideo='").append(isVideo ? "true" : "false").append("'")
						.append(" data-isaudio='").append(isAudio ? "true" : "false").append("'")
						.append(" data-ishtml='").append(isHtml ? "true" : "false").append("'")
						.append(isPopupable ? " data-url='" + fileUrl + "'" : "")
						.append(" style='position:relative; cursor:pointer;'>");
					// チェックボックス
					html.append("<input type='checkbox' class='file-checkbox' data-path='").append(fullPath).append("' data-isdir='").append(isDir ? "true" : "false").append("' onclick='event.stopPropagation(); updateBulkButtons();' style='position:absolute; top:6px; left:6px; z-index:2; cursor:pointer;'>");

					if (isDir) {
						html.append("<div>");
						html.append("<div class='file-icon'>").append(icon).append("</div>");
						html.append("<a href='/?path=").append(subQuery).append("' class='file-name' onclick='event.stopPropagation();'>").append(name).append("</a>");
						html.append("</div>");
						// ディレクトリのタイムスタンプ表示
						html.append("<div class='file-date' style='font-size:11px; color:#666;'>").append(formatTimestamp(file.lastModified())).append("</div>");
					}
					else if (isArchive) {
						html.append("<div>");
						html.append("<div class='file-icon'>").append(icon).append("</div>");
						// クリックすると /archiveViewUrl に飛ぶようにする
						html.append("<a href='").append(archiveViewUrl).append("' class='file-name' onclick='event.stopPropagation();'>").append(name).append("</a>");
						html.append("</div>");
						html.append("<div style='display:flex; justify-content:space-between; font-size:11px; color:#666;'>");
						html.append("<div class='file-size'>").append(formatFileSize(file.length())).append("</div>");
						html.append("<span class='file-date'>").append(formatTimestamp(file.lastModified())).append("</span>");
						html.append("</div>");
					}
					else {
						html.append("<div>");
						html.append("<div class='file-icon'>").append(icon).append("</div>");
						html.append("<div class='file-name'>").append(name).append("</div>");
						html.append("</div>");
						html.append("<div style='display:flex; justify-content:space-between; font-size:11px; color:#666;'>");
						html.append("<div class='file-size'>").append(formatFileSize(file.length())).append("</div>");
						html.append("<span class='file-date'>").append(formatTimestamp(file.lastModified())).append("</span>");
						html.append("</div>");
					}
					html.append("</div>");
				}
				html.append("</div>");

				html.append("<div id='tableItems' class='sub-view-table' style='display:none; width:100%;'>");
				html.append("<table><thead><tr><th style='width: 35px; text-align: center;'>")
					.append(lang.get("Select"))
					.append("</th><th>")
					.append(lang.get("Name"))
					.append("</th><th style='width: 120px;'>")
					.append(lang.get("size"))
					.append("</th><th style='width: 140px;'>")
					.append(lang.get("modified"))
					.append("</th></tr></thead><tbody>");

				for (File file : files) {
					String name = file.getName();
					String fullPath = file.getAbsolutePath();
					boolean isDir = file.isDirectory();
					String subQuery = URLEncoder.encode(fullPath, "UTF-8") + "&sort=" + sortField + "&order=" + sortOrder;
					// テーブル用の判定とダウンロードURLの作成
					boolean isImage = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".gif") || name.toLowerCase().endsWith(".webp") || name.toLowerCase().endsWith(".avif") || name.toLowerCase().endsWith(".bmp") || name.toLowerCase().endsWith(".svg"));
					boolean isText = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".txt") || name.toLowerCase().endsWith(".log") || name.toLowerCase().endsWith(".json") || name.toLowerCase().endsWith(".csv") || name.toLowerCase().endsWith(".md") || name.toLowerCase().endsWith(".js") || name.toLowerCase().endsWith(".java") || name.toLowerCase().endsWith(".py"));
					boolean isPdf = !isDir && (name.toLowerCase().endsWith(".pdf"));
					boolean isVideo = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".mp4") || name.toLowerCase().endsWith(".webm") || name.toLowerCase().endsWith(".mov") || name.toLowerCase().endsWith(".ogv") || name.toLowerCase().endsWith(".m4v"));
					boolean isAudio = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".mp3") || name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".m4a") || name.toLowerCase().endsWith(".aac") || name.toLowerCase().endsWith(".ogg") || name.toLowerCase().endsWith(".flac") || name.toLowerCase().endsWith(".opus") || name.toLowerCase().endsWith(".weba"));
					boolean isHtml = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".htm") || name.toLowerCase().endsWith(".xml"));
					boolean isArchive = !isDir && (mPopupImage) && (name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".rar") || name.toLowerCase().endsWith(".7z") || name.toLowerCase().endsWith(".tar") || name.toLowerCase().endsWith(".cab") || name.toLowerCase().endsWith(".lzh"));
					// 判定のいずれかに引っかかった場合は fileUrl を付与
					boolean isPopupable = isImage || isText || isPdf || isVideo || isAudio || isHtml;
					String fileUrl = "/download?path=" + URLEncoder.encode(fullPath, "UTF-8");
					String archiveViewUrl = "/archiveview?path=" + URLEncoder.encode(fullPath, "UTF-8") + "&sort=" + sortField + "&order=" + sortOrder;
					// アイコンを自動判定(テーブル用)
					String icon = getFileIcon(file);
					String timeStr = formatTimestamp(file.lastModified());
					// テーブルの行
					html.append("<tr class='file-item'")
						.append(" data-path='").append(fullPath).append("'")
						.append(" data-name='").append(name).append("'")
						.append(" data-isdir='").append(isDir ? "true" : "false").append("'")
						.append(" data-isimage='").append(isImage ? "true" : "false").append("'")
						.append(" data-istext='").append(isText ? "true" : "false").append("'")
						.append(" data-ispdf='").append(isPdf ? "true" : "false").append("'")
						.append(" data-isvideo='").append(isVideo ? "true" : "false").append("'")
						.append(" data-isaudio='").append(isAudio ? "true" : "false").append("'")
						.append(" data-ishtml='").append(isHtml ? "true" : "false").append("'")
						.append(isPopupable ? " data-url='" + fileUrl + "'" : "")
						.append(" style='cursor:pointer;'>");

					html.append("<td style='text-align: center;'><input type='checkbox' class='file-checkbox' data-path='").append(fullPath).append("' data-isdir='").append(isDir ? "true" : "false").append("' onclick='event.stopPropagation(); updateBulkButtons();'></td>");

					if (isDir) {
						html.append("<td><span class='file-icon'>").append(icon).append("</span><a href='/?path=").append(subQuery).append("' class='file-name' onclick='event.stopPropagation();'>").append(name).append("</a></td>");
						html.append("<td class='file-size'>").append(lang.get("directory")).append("</td>");
						html.append("<td class='file-date' style='font-size:12px; color:#666;'>").append(timeStr).append("</td>");
					}
					else if (isArchive) {
						html.append("<td><span class='file-icon'>").append(icon).append("</span><a href='").append(archiveViewUrl).append("' class='file-name' onclick='event.stopPropagation();'>").append(name).append("</a></td>");
						html.append("<td class='file-size'>").append(formatFileSize(file.length())).append("</td>");
						html.append("<td class='file-date' style='font-size:12px; color:#666;'>").append(timeStr).append("</td>");
					}
					else {
						html.append("<td><span class='file-icon'>").append(icon).append("</span><span class='file-name'>").append(name).append("</span></td>");
						html.append("<td class='file-size'>").append(formatFileSize(file.length())).append("</td>");
						html.append("<td class='file-date' style='font-size:12px; color:#666;'>").append(timeStr).append("</td>");
					}
					html.append("</tr>");
				}
				html.append("</tbody></table>");
				html.append("</div>");
			}
		}
		catch (Exception e) {
			html.append("<div style='color: red;'>").append(lang.get("error")).append(": ").append(e.getMessage()).append("</div>");
		}

		html.append("</div>");
		html.append("</div>");
		html.append("</div>");
		// コンテキストメニューおよび画像モーダル枠の構築
		html.append("<div id='contextMenu' class='context-menu'>");
		html.append("<ul>");
		html.append("<li id='menuDownload'>").append(lang.get("download")).append("</li>");
		if (!mReadonly) {
			html.append("<li id='menuRename'>").append(lang.get("rename")).append("</li>");
			html.append("<li id='menuDelete' style='color: #dc3545;'>").append(lang.get("delete")).append("</li>");
		}
		html.append("</ul>");
		html.append("</div>");
		// ポップアップ用のHTML枠(画像)
		html.append("<div id='imageModal' class='modal' onclick=\"closeModal('imageModal')\">");
		html.append("<span class='close-btn'>&times;</span>");
		html.append("<img class='modal-content' id='modalImg'>");
		html.append("</div>");
		// テキストモーダル枠
		html.append("<div id='textModal' class='modal' onclick=\"closeModal('textModal')\">");
		html.append("<div class='text-modal-content' onclick='event.stopPropagation()'>");
		html.append("<span class='text-close-btn' onclick=\"closeModal('textModal')\">&times;</span>");
		html.append("<h3 id='textModalTitle' style='margin-top:0;'>テキストプレビュー</h3>");
		html.append("<pre id='modalTextContent'>読み込み中...</pre>");
		html.append("</div></div>");
		// PDF用モーダル
		html.append("<div id='pdfModal' class='modal' onclick=\"closeModal('pdfModal')\">");
		html.append("<div class='media-modal-content' onclick='event.stopPropagation();'>");
		html.append("<span class='text-close-btn' onclick=\"closeModal('pdfModal')\">&times;</span>");
		html.append("<h3 id='pdfModalTitle' style='margin-top:0;'></h3>");
		html.append("<iframe id='modalPdfFrame'></iframe>");
		html.append("</div></div>");
		// 動画用モーダル
		html.append("<div id='videoModal' class='modal' onclick=\"closeModal('videoModal')\">");
		html.append("<div class='media-modal-content' onclick='event.stopPropagation();'>");
		html.append("<span class='text-close-btn' onclick=\"closeModal('videoModal')\">&times;</span>");
		html.append("<h3 id='videoModalTitle' style='margin-top:0;'></h3>");
		html.append("<video id='modalVideo' controls autoplay></video>");
		html.append("</div></div>");
		// 音声用モーダル
		html.append("<div id='audioModal' class='modal' onclick=\"closeModal('audioModal')\">");
		html.append("<div class='media-modal-content' style='height: auto; min-height: 200px;' onclick='event.stopPropagation();'>");
		html.append("<span class='text-close-btn' onclick=\"closeModal('audioModal')\">&times;</span>");
		html.append("<h3 id='modalAudioTitle' style='margin-top:0;'></h3>");
		html.append("<audio id='modalAudio' controls autoplay></audio>");
		html.append("</div></div>");
		// HTML / XML用モーダル
		html.append("<div id='htmlModal' class='modal' onclick=\"closeModal('htmlModal')\">");
		html.append("<div class='media-modal-content' onclick='event.stopPropagation();'>");
		html.append("<span class='text-close-btn' onclick=\"closeModal('htmlModal')\">&times;</span>");
		html.append("<h3 id='htmlModalTitle' style='margin-top:0;'></h3>");
		html.append("<iframe id='modalHtmlFrame' style='background-color: #fff;'></iframe>");
		html.append("</div></div>");
		// ここからJavaScript
		html.append("<script>");
		// テーマ切り替え関連
		html.append("const themeToggleBtn = document.getElementById('themeToggleBtn');");
		html.append("function updateThemeButtonText() {");
		html.append("  if (!themeToggleBtn) return;");
		html.append("  if (currentTheme === 'dark') {");
		html.append("	themeToggleBtn.innerText = '").append(lang.get("darkDisplay")).append("';");
		html.append("  } else {");
		html.append("	themeToggleBtn.innerText = '").append(lang.get("lightDisplay")).append("';");
		html.append("  }");
		html.append("}");
		html.append("updateThemeButtonText();");
		html.append("");
		html.append("function toggleThemeMode() {");
		html.append("  currentTheme = (currentTheme === 'light') ? 'dark' : 'light';");
		html.append("  localStorage.setItem('comitton_theme_mode', currentTheme);");
		html.append("  if (currentTheme === 'dark') {");
		html.append("	document.documentElement.classList.add('dark-theme');");
		html.append("  } else {");
		html.append("	document.documentElement.classList.remove('dark-theme');");
		html.append("  }");
		html.append("  updateThemeButtonText();");
		html.append("}");
		html.append("");
		// 共通のビジー状態(画面ロック)切り替え関数
		html.append("function setBusy(isBusy, message = '").append(lang.get("busyWait")).append("') {");
		html.append("  const overlay = document.getElementById('busyOverlay');");
		html.append("  if (!overlay) return;");
		html.append("  if (isBusy) {");
		html.append("	overlay.querySelector('div').innerText = message;");
		html.append("	overlay.style.display = 'flex';");
		html.append("  } else {");
		html.append("	overlay.style.display = 'none';");
		html.append("  }");
		html.append("}");
		// ファイル選択時の処理
		html.append("function handleFileSelect(input) {");
		html.append("  const submitBtn = document.getElementById('uploadSubmitBtn');");
		html.append("  const infoSpan = document.getElementById('fileSelectInfo');");
		html.append("  if (!submitBtn) return;");
		html.append("  if (input.files && input.files.length > 0) {");
		html.append("	submitBtn.disabled = false;");
		html.append("	submitBtn.removeAttribute('disabled');");
		html.append("	submitBtn.style.display = 'inline-block';");
		html.append("	let names = [];");
		html.append("	for(let i=0; i<input.files.length; i++) { names.push(input.files[i].name); }");
		html.append("	if (infoSpan) infoSpan.innerText = '✔ ' + input.files.length + '件選択 (' + names.join(', ') + ')';");
		html.append("  } else {");
		html.append("	submitBtn.disabled = true;");
		html.append("	if (infoSpan) infoSpan.innerText = '';");
		html.append("  }");
		html.append("}");
		html.append("");
		// DOMContentLoaded イベント(ドラッグアンドドロップの設定など)
		html.append("document.addEventListener('DOMContentLoaded', function() {");
		html.append("  var fileInput = document.getElementById('fileInput');");
		html.append("  var dropZone = document.getElementById('dropZone');");
		html.append("");
		html.append("  if (fileInput) {");
		html.append("	fileInput.addEventListener('change', function() {");
		html.append("	  handleFileSelect(this);");
		html.append("	});");
		html.append("  }");
		html.append("");
		html.append("  if (dropZone) {");
		html.append("	['dragenter', 'dragover', 'dragleave', 'drop'].forEach(function(eventName) {");
		html.append("	  dropZone.addEventListener(eventName, function(e) { e.preventDefault(); e.stopPropagation(); }, false);");
		html.append("	});");
		html.append("	['dragenter', 'dragover'].forEach(function(eventName) {");
		html.append("	  dropZone.addEventListener(eventName, function() { dropZone.classList.add('dragover'); }, false);");
		html.append("	});");
		html.append("	['dragleave', 'drop'].forEach(function(eventName) {");
		html.append("	  dropZone.addEventListener(eventName, function() { dropZone.classList.remove('dragover'); }, false);");
		html.append("	});");
		html.append("	dropZone.addEventListener('drop', function(e) {");
		html.append("	  var dt = e.dataTransfer;");
		html.append("	  if (dt && dt.items) {");
		html.append("		for (var i = 0; i < dt.items.length; i++) {");
		html.append("		  var entry = dt.items[i].webkitGetAsEntry ? dt.items[i].webkitGetAsEntry() : null;");
		html.append("		  if (entry && entry.isDirectory) {");
		html.append("			alert('").append(lang.get("FileOnly")).append("');");
		html.append("			return;");
		html.append("		  }");
		html.append("		}");
		html.append("	  }");
		html.append("	  var files = dt.files;");
		html.append("	  if (files && files.length > 0 && fileInput) {");
		html.append("		try {");
		html.append("		  fileInput.files = files;");
		html.append("		} catch (err) {");
		html.append("		}");
		html.append("		handleFileSelect(fileInput);");
		html.append("	  }");
		html.append("	}, false);");
		html.append("  ");
		html.append("}");
		html.append("});");
		html.append("");
		// Ajaxアップロード処理
		html.append("async function uploadFiles() {");
		html.append("  const fileInput = document.getElementById('fileInput');");
		html.append("  if (!fileInput.files || fileInput.files.length === 0) {");
		html.append("	alert('").append(lang.get("noFileSelect")).append("');");
		html.append("	return;");
		html.append("  }");
		html.append("");
		html.append("  setBusy(true, '").append(lang.get("UploadingFile")).append("');");
		html.append("  const submitBtn = document.getElementById('uploadSubmitBtn');");
		html.append("  const statusSpan = document.getElementById('uploadStatus');");
		html.append("  submitBtn.style.display = 'none';");
		html.append("  statusSpan.style.display = 'inline';");
		html.append("  statusSpan.innerText = '").append(lang.get("Uploading")).append("';");
		html.append("");
		html.append("  const formData = new FormData();");
		html.append("  let fileCount = fileInput.files.length;");
		html.append("  for (let i = 0; i < fileCount; i++) {");
		html.append("	let file = fileInput.files[i];");
		html.append("	let fieldName = (i === 0) ? 'file' : ('file' + i);");
		html.append("	formData.append(fieldName, file);");
		html.append("  }");
		html.append("  formData.append('fileCount', fileCount);");
		html.append("");
		html.append("  try {");
		html.append("	let uploadUrl = '/upload?path=' + encodeURIComponent('").append(currentPath).append("') + '&fileCount=' + fileCount;");
		html.append("	for (let i = 0; i < fileCount; i++) {");
		html.append("	  uploadUrl += '&filename_' + i + '=' + encodeURIComponent(fileInput.files[i].name);");
		html.append("	}");
		html.append("");
		html.append("	const response = await fetch(uploadUrl, {");
		html.append("	  method: 'POST',");
		html.append("	  body: formData");
		html.append("	});");
		html.append("");
		html.append("	statusSpan.innerText = '").append(lang.get("saving")).append("';");
		html.append("	await new Promise(resolve => setTimeout(resolve, 50));");
		html.append("");
		html.append("	const resultText = await response.text();");
		html.append("	if (response.ok) {");
		html.append("      window.location.href = '/?path=' + encodeURIComponent('").append(currentPath).append("')");
		html.append(" + '&sort=' + encodeURIComponent('").append(sortField != null ? sortField : "").append("')");
		html.append(" + '&order=' + encodeURIComponent('").append(sortOrder != null ? sortOrder : "").append("')");
		html.append(" + '&_=' + new Date().getTime();");
		html.append("	} else {");
		html.append("	  alert('").append(lang.get("failUpload")).append(": ' + resultText);");
		html.append("	  submitBtn.style.display = 'inline';");
		html.append("	  statusSpan.style.display = 'none';");
		html.append("	}");
		html.append("  } catch (err) {");
		html.append("	alert('").append(lang.get("communicationError")).append(": ' + err);");
		html.append("	submitBtn.style.display = 'inline';");
		html.append("	statusSpan.style.display = 'none';");
		html.append("  }");
		html.append("}");
		html.append("");
		// ビュー切り替え & ソート関連
		html.append("let currentView = localStorage.getItem('comitton_view_mode') || 'grid';");
		html.append("const container = document.getElementById('fileContainer');");
		html.append("const gridSub = document.getElementById('gridItems');");
		html.append("const tableSub = document.getElementById('tableItems');");
		html.append("const toggleBtn = document.getElementById('viewToggleBtn');");
		html.append("");
		html.append("function applyViewMode() {");
		html.append("  if (currentView === 'table') {");
		html.append("	container.className = 'file-container table-view';");
		html.append("	gridSub.style.display = 'none';");
		html.append("	tableSub.style.display = 'block';");
		html.append("	toggleBtn.innerText = '").append(lang.get("switchGridview")).append("';");
		html.append("  } else {");
		html.append("	container.className = 'file-container grid-view';");
		html.append("	gridSub.style.display = 'contents';");
		html.append("	tableSub.style.display = 'none';");
		html.append("	toggleBtn.innerText = '").append(lang.get("switchListview")).append("';");
		html.append("  }");
		html.append("}");
		html.append("applyViewMode();");
		html.append("");
		html.append("function toggleViewMode() {");
		html.append("  currentView = (currentView === 'grid') ? 'table' : 'grid';");
		html.append("  localStorage.setItem('comitton_view_mode', currentView);");
		html.append("  applyViewMode();");
		html.append("}");
		html.append("");
		html.append("function applySort() {");
		html.append("  const field = document.getElementById('sortFieldSelect').value;");
		html.append("  const order = document.getElementById('sortOrderSelect').value;");
		html.append("  window.location.href = '/?path=' + encodeURIComponent('").append(currentPath).append("') + '&sort=' + field + '&order=' + order;");
		html.append("}");
		html.append("");
		// 全選択・解除のマスターチェックボックス制御
		html.append("function toggleSelectAll(master) {\n");
		html.append("  const checkboxes = document.querySelectorAll('.file-checkbox');\n");
		html.append("  checkboxes.forEach(cb => { cb.checked = master.checked; });\n");
		html.append("  updateBulkButtons();\n");
		html.append("}\n");
		// 選択状態の管理と一括操作用JS
		html.append("function getSelectedFiles() {");
		html.append("  const checkboxes = document.querySelectorAll('.file-checkbox:checked');");
		html.append("  let paths = [];");
		html.append("  checkboxes.forEach(cb => paths.push(cb.getAttribute('data-path')));");
		html.append("  return paths;");
		html.append("}");
		html.append("");
		// 選択状態の更新とマスターチェックボックスの同期
		html.append("function updateBulkButtons() {");
		html.append("  const checkboxes = document.querySelectorAll('.file-checkbox');");
		html.append("  const selected = getSelectedFiles();");
		html.append("  const downBtn = document.getElementById('bulkDownloadBtn');");
		html.append("  const delBtn = document.getElementById('bulkDeleteBtn');");
		html.append("  const master = document.getElementById('selectAllCheckbox');");
		html.append("  if (downBtn) downBtn.disabled = (selected.length === 0);");
		html.append("  if (delBtn) delBtn.disabled = (selected.length === 0);");
		html.append("  if (master) {");
		html.append("	if (selected.length === 0) {");
		html.append("	  master.checked = false; master.indeterminate = false;");
		html.append("	} else if (selected.length === checkboxes.length) {");
		html.append("	  master.checked = true; master.indeterminate = false;");
		html.append("	} else {");
		html.append("	  master.checked = false; master.indeterminate = true;");
		html.append("	}");
		html.append("  }");
		html.append("}");
		html.append("");
		// 一括ダウンロード処理
		html.append("async function bulkDownload() {");
		html.append("  const selected = getSelectedFiles();");
		html.append("  if (selected.length === 0) return;");
		html.append("");
		html.append("  setBusy(true, '").append(lang.get("zipping")).append("');");
		html.append("  const downBtn = document.getElementById('bulkDownloadBtn');");
		html.append("  const originalText = downBtn.innerHTML;");
		html.append("  downBtn.disabled = true;");
		html.append("  downBtn.innerHTML = '").append(lang.get("compressDownload")).append("';");
		html.append("");
		html.append("  const params = new URLSearchParams();");
		html.append("  selected.forEach(path => params.append('paths', path));");
		html.append("");
		html.append("  try {");
		html.append("	const response = await fetch('/bulk-download', {");
		html.append("	  method: 'POST',");
		html.append("	  headers: {");
		html.append("		'Content-Type': 'application/x-www-form-urlencoded'");
		html.append("	  },");
		html.append("	  body: params.toString()");
		html.append("	});");
		html.append("");
		html.append("	if (response.ok) {");
		html.append("	  const blob = await response.blob();");
		html.append("	  const url = window.URL.createObjectURL(blob);");
		html.append("	  const a = document.createElement('a');");
		html.append("	  a.href = url;");
		html.append("	  a.download = '").append(mBulkdownload).append("';");
		html.append("	  document.body.appendChild(a);");
		html.append("	  a.click();");
		html.append("	  a.remove();");
		html.append("	  window.URL.revokeObjectURL(url);");
		html.append("	} else {");
		html.append("	  const errText = await response.text();");
		html.append("	  alert('").append(lang.get("failDownload")).append(": ' + errText);");
		html.append("	}");
		html.append("  } catch (err) {");
		html.append("	alert('").append(lang.get("communicationError")).append(": ' + err);");
		html.append("  } finally {");
		html.append("	setBusy(false);");
		html.append("	downBtn.disabled = false;");
		html.append("	downBtn.innerHTML = originalText;");
		html.append("	updateBulkButtons();");
		html.append("  }");
		html.append("}");
		html.append("");
		// ページ更新処理
		html.append("function reloadPage() {");
		html.append("  if (typeof setBusy === 'function') { setBusy(true, '").append(lang.get("Updating")).append("'); }");
		html.append("  window.location.reload();");
		html.append("}");
		// 一括削除処理(非同期通信)
		html.append("async function bulkDelete() {");
		html.append("  const selected = getSelectedFiles();");
		html.append("  if (selected.length === 0) return;");
		html.append("  if (!confirm('").append(lang.get("selected")).append(" ' + selected.length + ' ").append(lang.get("wanttoDeleteItem")).append("')) return;");
		html.append("  setBusy(true, '").append(lang.get("deletion")).append("');");
		html.append("  try {");
		html.append("	const params = new URLSearchParams();");
		html.append("	selected.forEach(path => params.append('paths', path));");
		html.append("	const response = await fetch('/bulk-delete', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: params.toString() });");
		html.append("	if (response.ok) {");
		html.append("	  window.location.reload();");
		html.append("	} else {");
		html.append("	  const errText = await response.text();");
		html.append("	  alert('").append(lang.get("failDelete")).append(": ' + errText);");
		html.append("	  setBusy(false);");
		html.append("	}");
		html.append("  } catch (err) {");
		html.append("	alert('").append(lang.get("communicationError")).append(": ' + err);");
		html.append("	setBusy(false);");
		html.append("  }");
		html.append("}");
		// コンテキストメニュー(長押し・右クリックメニュー)の制御
		html.append("const menu = document.getElementById('contextMenu');");
		html.append("let currentTargetPath = '';");
		html.append("let currentTargetName = '';");
		html.append("let currentIsDirs = false;");
		html.append("document.addEventListener('click', () => { if (menu) menu.style.display = 'none'; });");
		html.append("");
		// 各ファイルアイテムに対する長押し(タッチデバイス)および右クリック(PC)のイベントリスナー登録
		html.append("document.querySelectorAll('.file-item').forEach(item => {");
		html.append("  let pressTimer;");
		html.append("  const showMenu = (e, targetItem) => {");
		html.append("	e.preventDefault();");
		html.append("	currentTargetPath = targetItem.getAttribute('data-path');");
		html.append("	currentTargetName = targetItem.getAttribute('data-name');");
		html.append("	currentIsDirs = targetItem.getAttribute('data-isdir') === 'true';");
		html.append("	if (menu) {");
		html.append("	  menu.style.display = 'block';");
		html.append("	  menu.style.left = Math.min(e.pageX, window.innerWidth - 160) + 'px';");
		html.append("	  menu.style.top = Math.min(e.pageY, window.innerHeight - 140) + 'px';");
		html.append("	}");
		html.append("  };");
		html.append("  item.addEventListener('touchstart', (e) => {");
		html.append("	pressTimer = setTimeout(() => { showMenu(e.touches[0], item); }, 500);");
		html.append("  });");
		html.append("  item.addEventListener('touchend', () => { clearTimeout(pressTimer); });");
		html.append("  item.addEventListener('touchmove', () => { clearTimeout(pressTimer); });");
		html.append("  item.addEventListener('contextmenu', (e) => { showMenu(e, item); });");
		html.append("});");
		html.append("");
		// コンテキストメニュー項目(ダウンロード・名前変更・削除)の動作
		// 個別ダウンロード処理
		html.append("const menuDownload = document.getElementById('menuDownload');");
		html.append("if (menuDownload) {");
		html.append("  menuDownload.addEventListener('click', function() {");
		html.append("	if (currentIsDirs) { alert('").append(lang.get("dirNotSupported")).append("'); return; }");
		html.append("	if (menuDownload.dataset.downloading === 'true') return;");
		html.append("	menuDownload.dataset.downloading = 'true';");
		html.append("	menuDownload.style.opacity = '0.5';");
		html.append("	menuDownload.style.pointerEvents = 'none';");
		html.append("	const toast = document.getElementById('toastMessage');");
		html.append("	if (toast) {");
		html.append("	  toast.style.display = 'block';");
		html.append("	  setTimeout(function() { toast.style.display = 'none'; }, 3000);");
		html.append("	}");
		html.append("	window.location.href = '/download?path=' + encodeURIComponent(currentTargetPath);");
		html.append("	setTimeout(function() {");
		html.append("	  menuDownload.dataset.downloading = 'false';");
		html.append("	  menuDownload.style.opacity = '1';");
		html.append("	  menuDownload.style.pointerEvents = 'auto';");
		html.append("	}, 3000);");
		html.append("  });");
		html.append("}");
		html.append("");
		// 個別リネーム(名前変更)処理
		html.append("const menuRename = document.getElementById('menuRename');");
		html.append("if (menuRename) {");
		html.append("  menuRename.addEventListener('click', () => {");
		html.append("	const newName = prompt('").append(lang.get("newName")).append(":', currentTargetName);");
		html.append("	if (newName && newName !== currentTargetName) {");
		html.append("	  const form = document.createElement('form');");
		html.append("	  form.method = 'POST';");
		html.append("	  form.action = '/rename?path=' + encodeURIComponent('").append(currentPath).append("') + '&sort=").append(sortField).append("&order=").append(sortOrder).append("';");
		html.append("	  const input1 = document.createElement('input'); input1.type = 'hidden'; input1.name = 'oldPath'; input1.value = currentTargetPath;");
		html.append("	  const input2 = document.createElement('input'); input2.type = 'hidden'; input2.name = 'newName'; input2.value = newName;");
		html.append("	  form.appendChild(input1); form.appendChild(input2); document.body.appendChild(form); form.submit();");
		html.append("	}");
		html.append("  });");
		html.append("}");
		html.append("");
		// 個別削除処理
		html.append("const menuDelete = document.getElementById('menuDelete');");
		html.append("if (menuDelete) {");
		html.append("  menuDelete.addEventListener('click', () => {");
		html.append("	if (confirm('「' + currentTargetName + '」").append(lang.get("wanttoDelete")).append("')) {");
		html.append("	  const form = document.createElement('form');");
		html.append("	  form.method = 'POST';");
		html.append("	  form.action = '/delete?path=' + encodeURIComponent('").append(currentPath).append("') + '&sort=").append(sortField).append("&order=").append(sortOrder).append("';");
		html.append("	  const inputElem = document.createElement('input'); inputElem.type = 'hidden'; inputElem.name = 'targetPath'; inputElem.value = currentTargetPath;");
		html.append("	  form.appendChild(inputElem); document.body.appendChild(form); form.submit();");
		html.append("	}");
		html.append("  });");
		html.append("}");
		// 画像プレビュー(モーダル表示)機能
		// 画像をクリックしたときにポップアップを開く処理
		html.append("document.querySelectorAll('.file-item').forEach(item => {");
		html.append("	item.addEventListener('click', async function(e) {");
		html.append("		if (e.target.tagName === 'INPUT' || e.target.tagName === 'A') return;");
		html.append("		const url = this.getAttribute('data-url');");
		html.append("		const fileName = this.getAttribute('data-name');");
		// 画像
		html.append("		if (this.getAttribute('data-isimage') === 'true') {");
		html.append("			const url = this.getAttribute('data-url');");
		html.append("			const modalImg = document.getElementById('modalImg');");
		html.append("			const imageModal = document.getElementById('imageModal');");
		html.append("			modalImg.src = '';");
		html.append("			imageModal.style.display = 'flex';");
		html.append("			const img = new Image();");
		html.append("			img.src = url;");
		html.append("			img.onload = function() {");
		html.append("				modalImg.src = url;");
		html.append("			};");
		html.append("			img.onerror = function() {");
		html.append("				imageModal.style.display = 'none';");
		html.append("				alert('").append(lang.get("failLoadimage")).append("');");
		html.append("			};");
		html.append("		}");
		// テキスト
		html.append("		else if (this.getAttribute('data-istext') === 'true') {");
		html.append("			const url = this.getAttribute('data-url');");
		html.append("			const fileName = this.getAttribute('data-name');");
		html.append("			const textModal = document.getElementById('textModal');");
		html.append("			const titleEl = document.getElementById('textModalTitle');");
		html.append("			const contentEl = document.getElementById('modalTextContent');");
		html.append("			titleEl.textContent = fileName;");
		html.append("			contentEl.textContent = '").append(lang.get("Loading")).append("';");
		html.append("			textModal.style.display = 'flex';");
		html.append("			try {");
		html.append("				const response = await fetch(url);");
		html.append("				if (!response.ok) throw new Error('").append(lang.get("failRetrievefile")).append("');");
		html.append("				const textData = await response.text();");
		html.append("				contentEl.textContent = textData;");
		html.append("			} catch (error) {");
		html.append("				contentEl.textContent = '").append(lang.get("LoadError")).append("' + error.message;");
		html.append("			}");
		html.append("		}");
		// PDF
		html.append("		else if (this.getAttribute('data-ispdf') === 'true') {");
		html.append("			const url = this.getAttribute('data-url');");
		html.append("			const fileName = this.getAttribute('data-name');");
		html.append("			document.getElementById('pdfModalTitle').textContent = fileName;");
		html.append("			const pdfFrame = document.getElementById('modalPdfFrame');");
		html.append("			pdfFrame.src = 'about:blank';");
		html.append("			document.getElementById('pdfModal').style.display = 'flex';");
		html.append("			try {");
		html.append("				const response = await fetch(url);");
		html.append("				if (!response.ok) throw new Error('").append(lang.get("failRetrievefile")).append("');");
		html.append("				const blob = await response.blob();");
		html.append("				pdfFrame.src = URL.createObjectURL(blob);");
		html.append("			} catch (error) {");
		html.append("				alert('").append(lang.get("LoadError")).append("' + error.message);");
		html.append("				closePdfModal();");
		html.append("			}");
		html.append("		}");
		// 動画
		html.append("		else if (this.getAttribute('data-isvideo') === 'true') {");
		html.append("			document.getElementById('videoModalTitle').textContent = fileName;");
		html.append("			const videoEl = document.getElementById('modalVideo');");
		html.append("			videoEl.src = url;");
		html.append("			document.getElementById('videoModal').style.display = 'flex';");
		html.append("		}");
		// 音声
		html.append("		else if (this.getAttribute('data-isaudio') === 'true') {");
		html.append("			document.getElementById('modalAudioTitle').textContent = fileName;");
		html.append("			const audioEl = document.getElementById('modalAudio');");
		html.append("			audioEl.src = url;");
		html.append("			document.getElementById('audioModal').style.display = 'flex';");
		html.append("		}");
		// HTML / XML(通常のページとして表示)
		html.append("		else if (this.getAttribute('data-ishtml') === 'true') {");
		html.append("			const url = this.getAttribute('data-url');");
		html.append("			const fileName = this.getAttribute('data-name');");
		html.append("			document.getElementById('htmlModalTitle').textContent = fileName;");
		html.append("			const htmlFrame = document.getElementById('modalHtmlFrame');");
		html.append("			htmlFrame.src = 'about:blank';");
		html.append("			document.getElementById('htmlModal').style.display = 'flex';");
		html.append("			try {");
		html.append("				const response = await fetch(url);");
		html.append("				if (!response.ok) throw new Error('").append(lang.get("failRetrievefile")).append("');");
		html.append("				const blob = await response.blob();");
		html.append("				htmlFrame.src = URL.createObjectURL(blob);");
		html.append("			} catch (error) {");
		html.append("				alert('").append(lang.get("LoadError")).append("' + error.message);");
		html.append("				closeHtmlModal();");
		html.append("			}");
		html.append("		}");
		html.append("	});");
		html.append("});");
		// モーダルを閉じる処理群
		html.append("function closeModal(modalId) {");
		html.append("	const modal = document.getElementById(modalId);");
		html.append("	if (!modal) return;");
		html.append("	modal.style.display = 'none';");
		html.append("	if (modalId === 'imageModal') {");
		html.append("		const img = document.getElementById('modalImg');");
		html.append("		if (img) img.src = '';");
		html.append("	} else if (modalId === 'textModal') {");
		html.append("		const contentEl = document.getElementById('modalTextContent');");
		html.append("		if (contentEl) contentEl.textContent = '';");
		html.append("	} else if (modalId === 'pdfModal') {");
		html.append("		const f = document.getElementById('modalPdfFrame');");
		html.append("		if (f) {");
		html.append("			if (f.dataset.blobUrl) {");
		html.append("				URL.revokeObjectURL(f.dataset.blobUrl);");
		html.append("				delete f.dataset.blobUrl;");
		html.append("			}");
		html.append("			f.src = 'about:blank';");
		html.append("		}");
		html.append("	} else if (modalId === 'videoModal') {");
		html.append("		const v = document.getElementById('modalVideo');");
		html.append("		if (v) {");
		html.append("			v.pause();");
		html.append("			v.removeAttribute('src');");
		html.append("			v.load();");
		html.append("		}");
		html.append("	} else if (modalId === 'audioModal') {");
		html.append("		const a = document.getElementById('modalAudio');");
		html.append("		if (a) {");
		html.append("			a.pause();");
		html.append("			a.removeAttribute('src');");
		html.append("			a.load();");
		html.append("		}");
		html.append("	} else if (modalId === 'htmlModal') {");
		html.append("		const f = document.getElementById('modalHtmlFrame');");
		html.append("		if (f) {");
		html.append("			if (f.dataset.blobUrl) {");
		html.append("				URL.revokeObjectURL(f.dataset.blobUrl);");
		html.append("				delete f.dataset.blobUrl;");
		html.append("			}");
		html.append("			f.src = 'about:blank';");
		html.append("		}");
		html.append("	}");
		html.append("}");
		// スクロール位置の保存と復元
		html.append("  function restoreScroll() {");
		html.append("	  fetch('/get-scroll' + window.location.search, { method: 'GET' })");
		html.append("		  .then(function(res) { return res.text(); })");
		html.append("		  .then(function(yStr) {");
		html.append("			  var y = parseInt(yStr, 10);");
		html.append("			  if (!isNaN(y) && y > 0) {");
		html.append("				  setTimeout(function() {");
		html.append("					  var mc = document.querySelector('.main-content');");
		html.append("					  var fc = document.getElementById('fileContainer');");
		html.append("					  if (fc && fc.scrollHeight > fc.clientHeight) { fc.scrollTop = y; }");
		html.append("					  if (mc) { mc.scrollTop = y; }");
		html.append("					  window.scrollTo(0, y);");
		html.append("				  }, 50);");
		html.append("			  }");
		html.append("		  }).catch(function(err) {});");
		html.append("  }");
		html.append("  window.addEventListener('load', restoreScroll);");
		html.append("  window.addEventListener('pageshow', function(e) { restoreScroll(); });");
		html.append("  var timer = null;");
		html.append("  window.addEventListener('scroll', function(e) {");
		html.append("	  var t = e.target;");
		html.append("	  var winY = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;");
		html.append("	  var mc = document.querySelector('.main-content');");
		html.append("	  var mcY = (mc && mc.scrollTop) ? mc.scrollTop : 0;");
		html.append("	  var targetY = (t && typeof t.scrollTop === 'number') ? t.scrollTop : 0;");
		html.append("	  var yVal = Math.floor(Math.max(winY, mcY, targetY));");
		html.append("	  if (yVal > 0) {");
		html.append("		  clearTimeout(timer);");
		html.append("		  timer = setTimeout(function() {");
		html.append("			  fetch('/save-scroll?y=' + yVal, { method: 'GET' }).catch(function(err) {});");
		html.append("		  }, 300);");
		html.append("	  }");
		html.append("  }, true);");
		html.append("</script>");
		// ここまでJavaScript
		html.append("</body>");
		html.append("</html>");
		// レスポンスを返す
		try {
			return newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8", html.toString());
		}
		catch (Exception e) {
			return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error generating view");
		}
	}
	// ファイルををソート
	private void sortFiles(File[] files, String sortField, String sortOrder) {
		final boolean isAsc = "asc".equals(sortOrder);

		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				boolean d1 = f1.isDirectory();
				boolean d2 = f2.isDirectory();
				if (d1 && !d2) return -1;
				if (!d1 && d2) return 1;

				int result = 0;
				switch (sortField) {
					case "size":
						long s1 = f1.isDirectory() ? 0 : f1.length();
						long s2 = f2.isDirectory() ? 0 : f2.length();
						result = Long.compare(s1, s2);
						break;
					case "date":
						result = Long.compare(f1.lastModified(), f2.lastModified());
						break;
					case "ext":
						String ext1 = getFileExtension(f1);
						String ext2 = getFileExtension(f2);
						result = ext1.compareToIgnoreCase(ext2);
						break;
					case "name":
					default:
						result = f1.getName().compareToIgnoreCase(f2.getName());
						break;
				}
				return isAsc ? result : -result;
			}
		});
	}
	// 拡張子を得る
	private String getFileExtension(File file) {
		if (file.isDirectory()) return "";
		String name = file.getName();
		int lastIndex = name.lastIndexOf('.');
		if (lastIndex > 0 && lastIndex < name.length() - 1) {
			return name.substring(lastIndex + 1);
		}
		return "";
	}
	// Mimeタイプを得る
	private String getMimeType(File file) {
		String ext = getFileExtension(file).toLowerCase();
		if (ext.isEmpty()) {
			return "application/octet-stream";
		}
		// Android標準の MimeTypeMap から取得を試みる
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mime = mimeTypeMap.getMimeTypeFromExtension(ext);
		if (mime != null) {
			return mime;
		}
		// 標準で取れない、または補強したい拡張子を switch でカバー
		switch (ext) {
			// 画像
			case "jpg":
			case "jpeg":
				return "image/jpeg";
			case "png":
				return "image/png";
			case "gif":
				return "image/gif";
			case "webp":
				return "image/webp";
			case "heic":
			case "heif":
				return "image/heic";
			case "svg":
				return "image/svg+xml";
			case "avif":
				return "image/avif";
			case "jxl":
				return "image/jxl";
			// PDF
			case "pdf":
				return "application/pdf";
			// テキスト・コード・マークダウン
			case "txt":
			case "log":
			case "md":
			case "java":
			case "py":
			case "js":
			case "css":
				return "text/plain; charset=UTF-8";
			case "html":
			case "htm":
				return "text/html; charset=UTF-8";
			case "doc":
				return "application/msword";
			case "docx":
				return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
			case "xls":
				return "application/vnd.ms-excel";
			case "xlsx":
				return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			case "ppt":
				return "application/vnd.ms-powerpoint";
			case "pptx":
				return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
			case "csv":
				return "text/csv";
			case "json":
				return "application/json";
			case "xml":
				return "application/xml";
			// メディア
			case "mp3":
				return "audio/mpeg";
			case "m4a":
				return "audio/mp4";
			case "wav":
				return "audio/wav";
			case "aac":
				return "audio/aac";
			case "flac":
				return "audio/flac";
			case "weba":
				return "audio/weba";
			case "ogg":
			case "opus":
				return "audio/ogg";
			case "mp4":
				return "video/mp4";
			case "mkv":
				return "video/x-matroska";
			case "mov":
				return "video/quicktime";
			case "webm":
				return "video/webm";
			case "ogv":
				return "video/ogg";
			// 書庫
			case "zip":
				return "application/zip";
			case "rar":
				return "application/vnd.rar";
			case "7z":
				return "application/x-7z-compressed";
			case "tar":
				return "application/x-tar";
			case "gz":
				return "application/gzip";
			case "cab":
				return "application/vnd.ms-cab-compressed";
			case "lzh":
			case "lha":
				return "application/x-lzh-compressed";
			case "epub":
			    return "application/epub+zip";
			    // その他
			default:
				return "application/octet-stream";
		}
	}
	// アイコンの処理
	private String getFileIcon(File file) {
		if (file.isDirectory()) {
			return "📁";
		}
		String name = file.getName().toLowerCase();
		// PDFファイル
		if (name.endsWith(".pdf")) {
			return "📑";
		}
		// 画像ファイル
		if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
			name.endsWith(".gif") || name.endsWith(".webp") || name.endsWith(".avif") ||
			name.endsWith(".bmp") || name.endsWith(".jxl") || name.endsWith(".svg")) {
			return "🖼️";
		}
		// テキスト・Web・コード系ファイル
		if (name.endsWith(".txt") || name.endsWith(".log") || name.endsWith(".md") || 
			name.endsWith(".csv") || name.endsWith(".json") || name.endsWith(".xml") || 
			name.endsWith(".html") || name.endsWith(".htm") || name.endsWith(".js") ||
			name.endsWith(".css") || name.endsWith(".java") || name.endsWith(".py")) {
			return "📝";
		}
		// 動画ファイル
		if (name.endsWith(".mp4") || name.endsWith(".webm") || name.endsWith(".ogv") ||
			name.endsWith(".mov") || name.endsWith(".m4v") || name.endsWith(".avi") ||
			name.endsWith(".mkv")) {
			return "🎬";
		}
		// 音声ファイル
		if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a") ||
			name.endsWith(".aac") || name.endsWith(".ogg") || name.endsWith(".flac") ||
			name.endsWith(".wma") || name.endsWith(".opus") || name.endsWith(".weba")) {
			return "🎵";
		}
		// 書庫ファイル
		if (name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".7z") || 
			name.endsWith(".tar") || name.endsWith(".gz") || name.endsWith(".cab") || 
			name.endsWith(".lzh")) {
			return "📦";
		}
		if (name.endsWith(".epub")) {
			return "📖";
		}
		// その他
		return "📄";
	}
	// タイムスタンプ
	private String formatTimestamp(long timeMillis) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		return sdf.format(new Date(timeMillis));
	}
	// ファイルサイズ
	private String formatFileSize(long size) {
		if (size <= 0) return "0 B";
		String[] units = { "B", "KB", "MB", "GB", "TB" };
		// 1024で割り算を行い単位を得る
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
	}
	// フォルダツリーの構築
	private void buildFolderTreeHtml(File parentDir, String currentPath, String sortField, String sortOrder, StringBuilder html) {
		File[] files = parentDir.listFiles();
		if (files == null) return;
		// 名前でソート
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return f1.getName().compareToIgnoreCase(f2.getName());
			}
		});
		// フォルダが見つからなくなるまでツリーを作成
		for (File file : files) {
			if (file.isDirectory()) {
				try {
					String fullPath = file.getCanonicalPath();
					String name = file.getName();
					boolean isActive = fullPath.equals(currentPath);
					String subQuery = "?path=" + URLEncoder.encode(fullPath, "UTF-8") + "&sort=" + sortField + "&order=" + sortOrder;
					html.append("<li><a href='").append(subQuery).append("' class='").append(isActive ? "active" : "").append("'>📁 ").append(name).append("</a>");
					File[] subFiles = file.listFiles();
					boolean hasSubDir = false;
					if (subFiles != null) {
						// サブフォルダを探す
						for (File sf : subFiles) {
							if (sf.isDirectory()) { hasSubDir = true; break; }
						}
					}
					if (hasSubDir) {
						html.append("<ul class='tree-list sub-tree'>");
						buildFolderTreeHtml(file, currentPath, sortField, sortOrder, html);
						html.append("</ul>");
					}
					html.append("</li>");
				}
				catch (Exception ignored) {}
			}
		}
	}
	// フォルダの作成のコードを作成してWebブラウザへレスポンスを返す
	private Response handleMkdir(IHTTPSession session, Map<String, String> lang, String currentPath, Map<String, String> parms) {
		try {
			Map<String, String> files = new HashMap<>();
			session.parseBody(files);
			Map<String, String> params = session.getParms();
			String dirname = params.get("dirname");
			// フォルダ名があれば作成
			if (dirname != null && !dirname.isEmpty()) {
				File newDir = new File(currentPath, dirname);
				if (!newDir.exists()) {
					newDir.mkdirs();
				}
			}
			// Webブラウザへレスポンスを返す
			return redirectTo(currentPath, parms);
		}
		catch (Exception e) {
			return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Mkdir Error: " + e.getMessage());
		}
	}
	// ファイルの削除のコードを作成してWebブラウザへレスポンスを返す
	private Response handleDelete(IHTTPSession session, Map<String, String> lang, String currentPath, Map<String, String> parms) {
		try {
			Map<String, String> files = new HashMap<>();
			session.parseBody(files);
			Map<String, String> params = session.getParms();
			String targetPath = params.get("targetPath");
			// パスが存在すればフォルダ以下のファイルを一括で削除
			if (targetPath != null) {
				// パスをサニタイズ
				targetPath = sanitizePath(targetPath);
				deleteRecursive(new File(targetPath));
			}
			// Webブラウザへレスポンスを返す
			return redirectTo(currentPath, parms);
		}
		catch (Exception e) {
			return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Delete Error: " + e.getMessage());
		}
	}
	// フォルダ以下のファイルを一括で削除
	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			File[] children = fileOrDirectory.listFiles();
			if (children != null) {
				for (File child : children) {
					deleteRecursive(child);
				}
			}
		}
		fileOrDirectory.delete();
	}
	// 名称変更のコードを作成してWebブラウザへレスポンスを返す
	private Response handleRename(IHTTPSession session, Map<String, String> lang, String currentPath, Map<String, String> parms) {
		try {
			Map<String, String> files = new HashMap<>();
			session.parseBody(files);
			Map<String, String> params = session.getParms();
			String oldPath = params.get("oldPath");
			String newName = params.get("newName");
			// パスが存在すれば名称変更
			if (oldPath != null && newName != null && !newName.isEmpty()) {
				// パスをサニタイズ
				oldPath = sanitizePath(oldPath);
				File oldFile = new File(oldPath);
				File newFile = new File(oldFile.getParent(), newName);
				oldFile.renameTo(newFile);
			}
			// Webブラウザへレスポンスを返す
			return redirectTo(currentPath, parms);
		}
		catch (Exception e) {
			return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Rename Error: " + e.getMessage());
		}
	}
	// ダウンロードのコードを作成してWebブラウザへレスポンスを返す
	private Response handleDownload(Map<String, String> lang, Map<String, String> parms) {
		try {
			String filePath = parms.get("path");
			if (filePath == null) {
				// File not foundを返す
				return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found");
			}
			// パスをサニタイズ
			filePath = sanitizePath(filePath);
			File file = new File(filePath);
			if (!file.exists() || file.isDirectory()) {
				// File not foundを返す
				return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found");
			}
			// Mimeタイプを得る
			String mimeType = getMimeType(file);
			// ファイルのストリームを開く
			FileInputStream fis = new FileInputStream(file);
			Response response = newChunkedResponse(Response.Status.OK, mimeType, fis);
			// ファイル名をURLエンコード
			String encodedName = URLEncoder.encode(file.getName(), "UTF-8").replaceAll("\\+", "%20");
			String contentDisposition = "attachment; filename=\"" + file.getName() + "\"; filename*=UTF-8''" + encodedName;
			// Webブラウザへレスポンスを返す(ストリーム送信)
			response.addHeader("Content-Disposition", contentDisposition);
			return response;
		}
		catch (Exception e) {
			return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Download Error: " + e.getMessage());
		}
	}
	// アップロードのコードを作成してWebブラウザへレスポンスを返す
	public NanoHTTPD.Response handleUpload(NanoHTTPD.IHTTPSession session, Map<String, String> lang, String currentPath, Map<String, String> parms, String sortField, String sortOrder) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			Map<String, String> files = new HashMap<>();
			session.parseBody(files);

			Logcat.v(logLevel, "=== HANDLE UPLOAD START ===");
			if (parms != null) {
				for (Map.Entry<String, String> entry : parms.entrySet()) {
					Logcat.v(logLevel, "Param Key: " + entry.getKey() + ", Value: " + entry.getValue());
				}
			}
			if (files != null) {
				for (Map.Entry<String, String> entry : files.entrySet()) {
					Logcat.v(logLevel, "File Key: " + entry.getKey() + ", TempPath: " + entry.getValue());
				}
			}
			// 送信されてきたファイルの総数(fileCount)を取得
			int fileCount = 1;
			if (parms != null && parms.containsKey("fileCount")) {
				try {
					fileCount = Integer.parseInt(parms.get("fileCount"));
				}
				catch (Exception ignored) {}
			}
			// 万が一 fileCount が 1 以下であっても、files マップのサイズを考慮する
			if (fileCount < files.size()) {
				fileCount = files.size();
			}

			boolean success = false;
			File destDir = new File(currentPath);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			// 0番目から fileCount(または files の数)だけ確実にループする
			for (int i = 0; i < fileCount; i++) {
				// キー名の候補(1つ目は "file", 2つ目以降は "file1", "file2"...)
				String fieldName = (i == 0) ? "file" : ("file" + i);
				String tempFilePath = files.get(fieldName);

				// キー名で直接見つからない場合のフォールバック(files のキーを順番に取得)
				if (tempFilePath == null && files.containsKey("file" + i)) {
					tempFilePath = files.get("file" + i);
				}
				// それでも見つからない場合で、filesマップに要素があれば、インデックス順にフォールバック取得
				if (tempFilePath == null && !files.isEmpty()) {
					int idx = 0;
					for (Map.Entry<String, String> entry : files.entrySet()) {
						if (idx == i) {
							fieldName = entry.getKey();
							tempFilePath = entry.getValue();
							break;
						}
						idx++;
					}
				}
				if (tempFilePath == null) {
					// これ以上対応する一時ファイルがなければループ終了
					break;
				}
				File tempFile = new File(tempFilePath);
				if (!tempFile.exists()) {
					continue;
				}
				// URLクエリ(parms)から各ファイルの元名を取得
				String originalFileName = null;
				if (parms != null) {
					originalFileName = parms.get("filename_" + i);
					if (originalFileName == null && i == 0) {
						originalFileName = parms.get("filename");
					}
				}
				// URLデコードとサニタイズ
				if (originalFileName != null && !originalFileName.isEmpty()) {
					try {
						originalFileName = URLDecoder.decode(originalFileName, "UTF-8");
						originalFileName = originalFileName.replace("\"", "").trim();
					}
					catch (Exception e) {
						Logcat.e(logLevel, "Filename decode error", e);
					}
				}
				// 最終的にも空の場合はタイムスタンプ名
				if (originalFileName == null || originalFileName.isEmpty()) {
					originalFileName = "uploaded_" + System.currentTimeMillis() + "_" + i + ".zip";
				}
				File destFile = new File(destDir, originalFileName);
				// 64KBバッファを使って効率的にSDカードへ書き込み
				try (InputStream in = new BufferedInputStream(new FileInputStream(tempFile), 65536);
					 OutputStream out = new BufferedOutputStream(new FileOutputStream(destFile), 65536)) {
					
					byte[] buffer = new byte[65536];
					int read;
					while ((read = in.read(buffer)) != -1) {
						out.write(buffer, 0, read);
					}
					out.flush();
					success = true;
					Logcat.v(logLevel, "Successfully saved: " + originalFileName);
				}
				catch (Exception e) {
					Logcat.e(logLevel, "Failed to save file: " + originalFileName, e);
				}
			}
			// リダイレクト先のURLをソート情報も含めて組み立てる
			StringBuilder redirectUrl = new StringBuilder("/?path=");
			redirectUrl.append(URLEncoder.encode(currentPath != null ? currentPath : "", StandardCharsets.UTF_8));
			if (sortField != null && !sortField.isEmpty()) {
				redirectUrl.append("&sort=").append(URLEncoder.encode(sortField, StandardCharsets.UTF_8));
			}
			if (sortOrder != null && !sortOrder.isEmpty()) {
				redirectUrl.append("&order=").append(URLEncoder.encode(sortOrder, StandardCharsets.UTF_8));
			}
			// Webブラウザへレスポンスを返す
			if (success) {
				NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", "OK");
				response.addHeader("Location", redirectUrl.toString());
				return response;
			}
			else {
				NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Error: No files saved");
				response.addHeader("Location", redirectUrl.toString());
				return response;
			}
		}
		catch (Exception e) {
			Logcat.e(logLevel, "Upload Exception", e);
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/html", "Error: " + e.getMessage());
		}
	}
	// 一括削除のコードを作成してWebブラウザへレスポンスを返す
	private NanoHTTPD.Response handleBulkDelete(NanoHTTPD.IHTTPSession session, Map<String, String> lang, String currentPath) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			List<String> pathsToDel = new ArrayList<>();
			// POSTボディの生データをInputStreamから直接読み込む
			String postBody = null;
			try {
				String contentLengthHeader = session.getHeaders().get("content-length");
				if (contentLengthHeader != null) {
					int contentLength = Integer.parseInt(contentLengthHeader);
					byte[] buf = new byte[contentLength];
					int read = 0;
					while (read < contentLength) {
						int n = session.getInputStream().read(buf, read, contentLength - read);
						if (n < 0) break;
						read += n;
					}
					postBody = new String(buf, 0, read, "UTF-8");
				}
			}
			catch (Exception e) {
				Logcat.e(logLevel, "Failed to read input stream for bulk delete", e);
			}
			if (postBody == null || postBody.isEmpty()) {
				Map<String, String> files = new HashMap<>();
				try {
					session.parseBody(files);
					postBody = files.get("postData");
				}
				catch (Exception ignored) {}
			}
			Logcat.v(logLevel, "Raw postBody for bulk delete: " + postBody);
			// パラメータの抽出とURLデコード
			if (postBody != null && !postBody.isEmpty()) {
				String[] pairs = postBody.split("&");
				for (String pair : pairs) {
					String[] kv = pair.split("=", 2);
					if (kv.length == 2 && "paths".equals(kv[0])) {
						String decodedPath = URLDecoder.decode(kv[1], "UTF-8");
						pathsToDel.add(decodedPath);
					}
				}
			}
			Logcat.v(logLevel, "Bulk Delete total paths to delete: " + pathsToDel.size());
			// 取得したパスのファイル/フォルダを再帰的に削除
			for (String pathStr : pathsToDel) {
				File target = new File(pathStr);
				if (target.exists()) {
					Logcat.v(logLevel, "Deleting target: " + target.getAbsolutePath());
					deleteRecursive(target);
				}
				else {
					Logcat.w(logLevel, "Delete target not found: " + pathStr);
				}
			}
			// 削除完了後に元のフォルダへリダイレクト
			String redirectUrl = "/?path=" + URLEncoder.encode(currentPath, "UTF-8");
			// Webブラウザへレスポンスを返す
			NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.REDIRECT, "text/html", "");
			response.addHeader("Location", redirectUrl);
			return response;
		}
		catch (Exception e) {
			Logcat.e(logLevel, "Bulk delete error", e);
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", "Delete Error: " + e.getMessage());
		}
	}
	// 一括ダウンロードのコードを作成してWebブラウザへレスポンスを返す
	private NanoHTTPD.Response handleBulkDownload(NanoHTTPD.IHTTPSession session ,Map<String, String> lang) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			List<String> pathsToZip = new ArrayList<>();
			// POSTボディの生データをInputStreamから直接読み込む
			String postBody = null;
			try {
				String contentLengthHeader = session.getHeaders().get("content-length");
				if (contentLengthHeader != null) {
					int contentLength = Integer.parseInt(contentLengthHeader);
					byte[] buf = new byte[contentLength];
					int read = 0;
					while (read < contentLength) {
						int n = session.getInputStream().read(buf, read, contentLength - read);
						if (n < 0) break;
						read += n;
					}
					postBody = new String(buf, 0, read, "UTF-8");
				}
			}
			catch (Exception e) {
				Logcat.e(logLevel, "Failed to read input stream for bulk download", e);
			}
			if (postBody == null || postBody.isEmpty()) {
				Map<String, String> files = new HashMap<>();
				try {
					session.parseBody(files);
					postBody = files.get("postData");
				}
				catch (Exception ignored) {}
			}
			// パラメータの抽出とURLデコード
			if (postBody != null && !postBody.isEmpty()) {
				String[] pairs = postBody.split("&");
				for (String pair : pairs) {
					String[] kv = pair.split("=", 2);
					if (kv.length == 2 && "paths".equals(kv[0])) {
						String decodedPath = URLDecoder.decode(kv[1], "UTF-8");
						pathsToZip.add(decodedPath);
					}
				}
			}
			Logcat.v(logLevel, "Bulk Download total paths to zip: " + pathsToZip.size());
			if (pathsToZip.isEmpty()) {
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "No files selected");
			}
			PipedOutputStream pipedOut = new PipedOutputStream();
			PipedInputStream pipedIn = new PipedInputStream(pipedOut, 65536);
			// 別スレッドでZIP圧縮ストリーミング書き込み(フォルダの再帰処理 & UTF-8エンコーディング対応)
			new Thread(() -> {
				// MS932 (Shift_JIS) を指定するとWindows標準の解凍でも文字化けしにくくなる
				// UTF-8にしたい場合は Charset.forName("UTF-8") に変更
				ZipOutputStream zos = null;
				try {
					zos = new ZipOutputStream(new BufferedOutputStream(pipedOut), java.nio.charset.Charset.forName("MS932"));

					for (String pStr : pathsToZip) {
						File target = new File(pStr);
						if (target.exists()) {
							// ファイルまたはディレクトリを再帰的に追加
							addFileToZip(zos, target, target.getParentFile(), "");
						}
						else {
							Logcat.w(logLevel, "Target not found: " + pStr);
						}
					}
					zos.finish();
				}
				catch (Exception e) {
					Logcat.e(logLevel, "Zip stream error", e);
				}
				finally {
					try {
						if (zos != null) zos.close();
					}
					catch (IOException ignored) {}
				}
			}).start();
			// Webブラウザへレスポンスを返す(ストリーム送信)
			NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/zip", pipedIn, -1);
			response.addHeader("Content-Disposition", "attachment; filename=\"" + mBulkdownload + "\"");
			return response;
		}
		catch (Exception e) {
			Logcat.e(logLevel, "Bulk download error", e);
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", "Zip Error: " + e.getMessage());
		}
	}
	// 補助メソッド：ファイルおよびディレクトリを再帰的にZIPに追加する
	private void addFileToZip(ZipOutputStream zos, File file, File rootDir, String parentDirName) throws IOException {
		String entryName;
		if (parentDirName.isEmpty()) {
			entryName = file.getName();
		}
		else {
			entryName = parentDirName + "/" + file.getName();
		}
		if (file.isDirectory()) {
			// ディレクトリの場合は末尾に "/" をつけてエントリを追加し、中のファイルを再帰処理
			if (!entryName.endsWith("/")) {
				entryName += "/";
			}
			zos.putNextEntry(new ZipEntry(entryName));
			zos.closeEntry();

			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					addFileToZip(zos, child, rootDir, entryName.substring(0, entryName.length() - 1));
				}
			}
		}
		else {
			// 通常のファイルの場合
			ZipEntry entry = new ZipEntry(entryName);
			zos.putNextEntry(entry);

			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), 32768)) {
				byte[] buffer = new byte[32768];
				int len;
				while ((len = bis.read(buffer)) != -1) {
					zos.write(buffer, 0, len);
				}
			}
			zos.closeEntry();
		}
	}
	// リダイレクト(フォルダの作成/ファイルの削除/名称変更時に呼ばれる)
	private Response redirectTo(String currentPath, Map<String, String> parms) {
		try {
			String sortField = parms.containsKey("sort") ? parms.get("sort") : "name";
			String sortOrder = parms.containsKey("order") ? parms.get("order") : "asc";

			Response response = newFixedLengthResponse(Response.Status.REDIRECT, "text/html", "");
			response.addHeader("Location", "/?path=" + URLEncoder.encode(currentPath, "UTF-8") + "&sort=" + sortField + "&order=" + sortOrder);
			response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.addHeader("Pragma", "no-cache");
			response.addHeader("Expires", "0");
			return response;
		}
		catch (Exception e) {
			return newFixedLengthResponse(Response.Status.OK, "text/plain", "Done");
		}
	}
	// ZIP / 7-Zip / RAR / TAR / CAB / LZH 共通の書庫ファイルビューア生成
	private Response generateArchiveViewResponse(Map<String, String> lang, String archivePath, String sortField, String sortOrder) {
		File archiveFile = new File(archivePath);
		if (!archiveFile.exists()) {
			return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain; charset=UTF-8", lang.get("ArchiveFileNotExist"));
		}
		// 書庫ファイルが置かれていた「親ディレクトリ」のパスを取得する
		File parentDir = archiveFile.getParentFile();
		String parentPath = (parentDir != null) ? parentDir.getAbsolutePath() : "";
		String encodedParentPath = URLEncoder.encode(parentPath, StandardCharsets.UTF_8);
		// 戻り先URLに持たせるクエリ(ソート設定があれば付与する)
		StringBuilder backUrl = new StringBuilder("/?path=").append(encodedParentPath);
		if (sortField != null && !sortField.isEmpty()) {
			backUrl.append("&sort=").append(URLEncoder.encode(sortField, StandardCharsets.UTF_8));
		}
		if (sortOrder != null && !sortOrder.isEmpty()) {
			backUrl.append("&order=").append(URLEncoder.encode(sortOrder, StandardCharsets.UTF_8));
		}

		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
		html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
		html.append("<title>").append(lang.get("ArchiveViewer")).append(archiveFile.getName()).append("</title>");

		html.append("<script>");
		html.append("  const serverDefaultTheme = '").append(mDefaultTheme).append("';");
		html.append("  let currentTheme = localStorage.getItem('comitton_theme_mode') || serverDefaultTheme;");
		html.append("  if (currentTheme === 'dark') {");
		html.append("	document.documentElement.classList.add('dark-theme');");
		html.append("  }");
		html.append("</script>");
		// CSSスタイル
		html.append("<style>");
		// デフォルトのスタイル
		html.append("body{font-family:sans-serif;margin:0;padding:20px;background:#f4f6f9;color:#333;transition:background 0.2s, color 0.2s;}");
		html.append(".container{max-width:800px;margin:0 auto;background:#fff;padding:20px;border-radius:12px;box-shadow:0 4px 12px rgba(0,0,0,0.1);}");
		html.append("table{width:100%;border-collapse:collapse;margin-top:10px;}");
		html.append("th,td{padding:12px;border-bottom:1px solid #eee;text-align:left;font-size:14px;}");
		html.append("th{background:#f8f9fa;color:#495057;position:sticky;top:0;}");
		html.append(".archive-row{cursor:pointer;transition:background 0.15s;}");
		html.append(".archive-row:hover{background:#f0f8ff;}");
		// ダークモード時のスタイル
		html.append("html.dark-theme body { background: #121212; color: #e0e0e0; }");
		html.append("html.dark-theme .container { background: #1e1e1e; box-shadow: 0 4px 12px rgba(0,0,0,0.4); }");
		html.append("html.dark-theme th { background: #2d2d2d; color: #b0b0b0; border-bottom: 1px solid #333; }");
		html.append("html.dark-theme td { border-bottom: 1px solid #333; }");
		html.append("html.dark-theme .archive-row:hover { background: #2a2d32; }");
		html.append("html.dark-theme a { color: #58a6ff; }");
		html.append("html.dark-theme .media-modal-content { background: #1e1e1e; color: #e0e0e0; }");
		html.append("html.dark-theme pre { background: #121212 !important; color: #e0e0e0; }");
		html.append("html.dark-theme .close-btn { color: #aaa; }");
		html.append("html.dark-theme .close-btn:hover { color: #fff; }");
		// モーダル共通スタイル
		html.append(".modal{display:none;position:fixed;z-index:1000;left:0;top:0;width:100%;height:100%;background:rgba(0,0,0,0.6);justify-content:center;align-items:center;backdrop-filter:blur(2px);}");
		html.append(".media-modal-content{background:#fff;width:90%;max-width:600px;max-height:85vh;border-radius:12px;box-shadow:0 8px 24px rgba(0,0,0,0.2);display:flex;flex-direction:column;overflow:hidden;position:relative;}");
		html.append(".close-btn{position:absolute;top:12px;right:16px;font-size:26px;font-weight:bold;color:#666;cursor:pointer;z-index:10;line-height:1;}");
		html.append(".close-btn:hover{color:#000;}");
		html.append("</style></head><body>");

		html.append("<div class=\"container\">");
		html.append("<h3>📦 ").append(archiveFile.getName()).append("</h3>");
		html.append("<p><a href=\"").append(backUrl.toString()).append("\">").append(lang.get("ReturnFilelist")).append("</a></p>");
		html.append("<div style=\"max-height:400px;overflow-y:auto;\"><table>");
		html.append("<thead><tr><th>").append(lang.get("fileName")).append("</th><th style=\"width:100px;\">").append(lang.get("size")).append("</th><th style=\"width:140px;\">").append(lang.get("modified")).append("</th></tr></thead><tbody>");
		// 書庫ファイル内エントリの走査とテーブル行の生成
		try (RandomAccessFileInStream fileStream = new RandomAccessFileInStream(new java.io.RandomAccessFile(archiveFile, "r"));
             IInArchive inArchive = SevenZip.openInArchive(null, fileStream)) {
			int itemCount = inArchive.getNumberOfItems();
			for (int i = 0; i < itemCount; i++) {

				String rawEntryName = (String) inArchive.getProperty(i, PropID.PATH);
				String entryName = rawEntryName;
				// アーカイブの拡張子を取得
				String lowerArchive = archivePath.toLowerCase();
				// LZHやCABの場合はCP932へ再デコードして比較する
				if (lowerArchive.endsWith(".lzh") || lowerArchive.endsWith(".cab")) {
					try {
						byte[] rawBytes = rawEntryName.getBytes("ISO-8859-1");
						entryName = new String(rawBytes, "CP932");
					}
					catch (Exception e) {
						entryName = rawEntryName;
					}
				}
				// フォルダ階層がある場合
				Boolean isFolder = (Boolean) inArchive.getProperty(i, PropID.IS_FOLDER);
				if (isFolder == null) isFolder = false;
				Long size = (Long) inArchive.getProperty(i, PropID.SIZE);
				if (size == null) size = 0L;
				// ファイルの種類に応じたアイコンを取得する
				File file = new File(entryName);
				String icon = getFileIcon(file);
				// 書庫ファイルのエントリから日時を取得してフォーマットする
				Date lastModified = (Date) inArchive.getProperty(i, PropID.LAST_MODIFICATION_TIME);
				String timeStr = (lastModified != null) ? formatTimestamp(lastModified.getTime()) : "-";
				// ディレクトリの場合はクリック対象から除外
				if (isFolder) {
					html.append("<tr>");
					html.append("<td>").append(icon).append(" ").append(entryName).append("</td>");
					html.append("<td style=\"color:#666;\">-</td>");
					html.append("</tr>");
					continue;
				}
				String encodedArchive = URLEncoder.encode(archivePath, StandardCharsets.UTF_8);
				String encodedEntry = URLEncoder.encode(entryName, StandardCharsets.UTF_8.toString());
				// クリック時に /archivefile を叩く URL を生成
				String fileUrl = "/archivefile?archive=" + encodedArchive + "&entry=" + encodedEntry;
				html.append("<tr class=\"archive-row\" onclick=\"openArchiveItem('").append(fileUrl).append("', '").append(entryName.replace("'", "\\'")).append("')\">");
				// 取得したアイコンを動的に埋め込む
				html.append("<td>").append(icon).append(" ").append(entryName).append("</td>");
				html.append("<td style=\"color:#666;\">").append(formatFileSize(size)).append("</td>");
				html.append("<td style=\"font-size:12px; color:#666;\">").append(timeStr).append("</td>");
				html.append("</tr>");
			}
		}
		catch (Exception e) {
			html.append("<tr><td colspan=\"2\" style=\"color:red;\">").append(lang.get("LoadArchiveError")).append(e.getMessage()).append("</td></tr>");
		}

		html.append("</tbody></table></div></div>");
		// モーダルHTML要素の配置
		// 画像モーダル
		html.append("<div id='imageModal' class='modal' onclick=\"closeModal('imageModal')\">");
		html.append("<div class='media-modal-content' onclick='event.stopPropagation();'><span class='close-btn' onclick=\"closeModal('imageModal')\">&times;</span>");
		html.append("<div style='padding:20px;text-align:center;overflow:auto;'><img id='modalImg' style='max-width:100%;max-height:75vh;'/></div></div></div>");
		// 動画モーダル
		html.append("<div id='videoModal' class='modal' onclick=\"closeModal('videoModal')\">");
		html.append("<div class='media-modal-content' onclick='event.stopPropagation();' style='max-width:800px;'><span class='close-btn' onclick=\"closeModal('videoModal')\">&times;</span>");
		html.append("<div style='padding:15px;background:#000;text-align:center;'><video id='modalVideo' controls style='max-width:100%;max-height:70vh;'></video></div></div></div>");
		// 音声モーダル
		html.append("<div id='audioModal' class='modal' onclick=\"closeModal('audioModal')\">");
		html.append("<div class='media-modal-content' onclick='event.stopPropagation();' style='max-width:400px;'><span class='close-btn' onclick=\"closeModal('audioModal')\">&times;</span>");
		html.append("<div style='padding:30px 20px;text-align:center;'><audio id='modalAudio' controls style='width:100%;'></audio></div></div></div>");
		// テキストモーダル
		html.append("<div id='textModal' class='modal' onclick=\"closeModal('textModal')\">");
		html.append("<div class='media-modal-content' onclick='event.stopPropagation();' style='max-width:700px;'><span class='close-btn' onclick=\"closeModal('textModal')\">&times;</span>");
		html.append("<div style='padding:20px;display:flex;flex-direction:column;height:80vh;'><h3 id='textTitle' style='margin-top:0;font-size:15px;'></h3>");
		html.append("<pre id='modalText' style='background:rgba(0,0,0,0.05);padding:12px;border-radius:6px;overflow:auto;flex:1;font-size:13px;margin:0;white-space:pre-wrap;word-break:break-all;'></pre></div></div></div>");
		// PDF / HTMLモーダル(共通のiframe用)
		html.append("<div id='iframeModal' class='modal' onclick=\"closeModal('iframeModal')\">");
		html.append("<div class='media-modal-content' onclick='event.stopPropagation();' style='width:90%;max-width:900px;height:85vh;'><span class='close-btn' onclick=\"closeModal('iframeModal')\">&times;</span>");
		html.append("<div style='padding:12px 20px;font-weight:bold;font-size:14px;border-bottom:1px solid rgba(0,0,0,0.1);' id='iframeTitle'>Preview</div>");
		html.append("<div style='flex:1;height:100%;'><iframe id='modalIframe' style='width:100%;height:100%;border:none;'></iframe></div></div></div>");
		// フロントエンドJavaScript(openArchiveItem & closeModal & メモリ管理)
		html.append("<script>");
		html.append("async function openArchiveItem(url, name) {");
		html.append("	const lower = name.toLowerCase();");
		html.append("	");
		html.append("	if (lower.match(/\\.(png|jpg|jpeg|gif|webp|avif|bmp|svg)$/)) {");
		html.append("		const img = document.getElementById('modalImg');");
		html.append("		img.src = '';");
		html.append("		img.src = url;");
		html.append("		document.getElementById('imageModal').style.display = 'flex';");
		html.append("	} ");
		html.append("	else if (lower.match(/\\.(mp4|webm|mov|ogv|m4v)$/)) {");
		html.append("		const vid = document.getElementById('modalVideo');");
		html.append("		vid.pause(); vid.removeAttribute('src'); vid.load();");
		html.append("		document.getElementById('videoModal').style.display = 'flex';");
		html.append("		const res = await fetch(url);");
		html.append("		const blob = await res.blob();");
		html.append("		const blobUrl = URL.createObjectURL(blob);");
		html.append("		vid.dataset.blobUrl = blobUrl;");
		html.append("		vid.src = blobUrl;");
		html.append("		vid.play();");
		html.append("	} ");
		html.append("	else if (lower.match(/\\.(mp3|wav|m4a|aac|ogg|flac|opus|weba)$/)) {");
		html.append("		const aud = document.getElementById('modalAudio');");
		html.append("		aud.pause(); aud.removeAttribute('src'); aud.load();");
		html.append("		document.getElementById('audioModal').style.display = 'flex';");
		html.append("		const res = await fetch(url);");
		html.append("		const blob = await res.blob();");
		html.append("		const blobUrl = URL.createObjectURL(blob);");
		html.append("		aud.dataset.blobUrl = blobUrl;");
		html.append("		aud.src = blobUrl;");
		html.append("		aud.play();");
		html.append("	} ");
		html.append("	else if (lower.match(/\\.(txt|log|json|csv|md|js|java|py)$/)) {");
		html.append("		document.getElementById('textTitle').innerText = name;");
		html.append("		document.getElementById('modalText').innerText = '読み込み中...';");
		html.append("		document.getElementById('textModal').style.display = 'flex';");
		html.append("		const res = await fetch(url);");
		html.append("		const text = await res.text();");
		html.append("		document.getElementById('modalText').innerText = text;");
		html.append("	} ");
		html.append("	else if (lower.match(/\\.(pdf|html|htm|xml)$/)) {");
		html.append("		document.getElementById('iframeTitle').innerText = name;");
		html.append("		document.getElementById('iframeModal').style.display = 'flex';");
		html.append("		const res = await fetch(url);");
		html.append("		const blob = await res.blob();");
		html.append("		const blobUrl = URL.createObjectURL(blob);");
		html.append("		const iframe = document.getElementById('modalIframe');");
		html.append("		iframe.dataset.blobUrl = blobUrl;");
		html.append("		iframe.src = blobUrl;");
		html.append("	}");
		html.append("}");
		// 終了・クリーンアップ関数(メモリリーク防止)
		html.append("function closeModal(id) {");
		html.append("	document.getElementById(id).style.display = 'none';");
		html.append("	if (id === 'imageModal') {");
		html.append("		document.getElementById('modalImg').src = '';");
		html.append("	}");
		html.append("	else if (id === 'videoModal') {");
		html.append("		const v = document.getElementById('modalVideo');");
		html.append("		v.pause();");
		html.append("		if (v.dataset.blobUrl) { URL.revokeObjectURL(v.dataset.blobUrl); delete v.dataset.blobUrl; }");
		html.append("		v.removeAttribute('src'); v.load();");
		html.append("	} else if (id === 'audioModal') {");
		html.append("		const a = document.getElementById('modalAudio');");
		html.append("		a.pause();");
		html.append("		if (a.dataset.blobUrl) { URL.revokeObjectURL(a.dataset.blobUrl); delete a.dataset.blobUrl; }");
		html.append("		a.removeAttribute('src'); a.load();");
		html.append("	} else if (id === 'iframeModal') {");
		html.append("		const iframe = document.getElementById('modalIframe');");
		html.append("		if (iframe.dataset.blobUrl) { URL.revokeObjectURL(iframe.dataset.blobUrl); delete iframe.dataset.blobUrl; }");
		html.append("		iframe.src = 'about:blank';");
		html.append("	}");
		html.append("}");
		html.append("</script>");

		html.append("</body></html>");
		return newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8", html.toString());
	}
	// 書庫ファイル（ZIP / 7z / RAR / TAR等）内の指定ファイルを抽出してバイナリ(ストリーム)で配信
	private Response serveArchiveEntryResponse(Map<String, String> lang, String archivePath, String entryName) {
		try {
			File fileObj = new File(archivePath);
			if (!fileObj.exists()) {
				return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", lang.get("EntryNotFound"));
			}
			// 7-Zipライブラリでアーカイブを開く
			RandomAccessFileInStream rafStream = new RandomAccessFileInStream(new java.io.RandomAccessFile(fileObj, "r"));
			IInArchive inArchive = SevenZip.openInArchive(null, rafStream);
			ISimpleInArchive simpleArchive = inArchive.getSimpleInterface();
			// 一致するエントリ(ファイル)を探す
			ISimpleInArchiveItem targetItem = null;
			String lowerArchive = archivePath.toLowerCase();
			boolean needsCp932 = lowerArchive.endsWith(".lzh") || lowerArchive.endsWith(".cab");
			for (ISimpleInArchiveItem item : simpleArchive.getArchiveItems()) {
				String rawPath = item.getPath();
				String currentEntryName = rawPath;
				// LZHやCABの場合は、一覧側と一致させるためにCP932へ再デコードして比較する
				if (needsCp932) {
					try {
						byte[] rawBytes = rawPath.getBytes("ISO-8859-1");
						currentEntryName = new String(rawBytes, "CP932");
					}
					catch (Exception ignored) {}
				}
				// パスの一致を確認
				if (entryName.equals(currentEntryName)) {
					targetItem = item;
					break;
				}
			}
			if (targetItem == null || targetItem.isFolder()) {
				inArchive.close();
				rafStream.close();
				return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", lang.get("EntryNotFound"));
			}
			long targetSize = targetItem.getSize();
			// パイプストリームを作成(書き込み側を別スレッドで動かし、読み込み側をNanoHTTPDに渡す)
			PipedOutputStream pout = new PipedOutputStream();
			// バッファサイズ(64KB)
			PipedInputStream pin = new PipedInputStream(pout, 65536);
			final ISimpleInArchiveItem itemToExtract = targetItem;
			// 別スレッドでアーカイブからデータを抽出し、パイプへ書き込む
			new Thread(() -> {
				try (pout; inArchive; rafStream) {
					itemToExtract.extractSlow(new ISequentialOutStream() {
						@Override
						public int write(byte[] data) throws SevenZipException {
							try {
								pout.write(data);
							}
catch (IOException e) {
								throw new SevenZipException("Stream closed or error", e);
							}
							return data.length;
						}
					});
				}
				catch (Exception e) {
					try {
						pout.close();
					} catch (IOException ignored) {}
				}
			}).start();
			// ファイルのMIMEタイプを判定
			File file = new File(entryName);
			String mimeType = getMimeType(file);
			// NanoHTTPDでストリームを返却(サイズが分かっている場合はサイズを指定)
			return newFixedLengthResponse(Response.Status.OK, mimeType, pin, targetSize);
		}
		catch (Exception e) {
			return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.getMessage());
		}
	}
}
