package src.comitton.fileaccess;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbRandomAccessFile;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.fileview.data.FileData;
import src.comitton.fileview.FileSelectActivity;

public class SmbFileAccess {
	private static final String TAG = "SmbFileAccess";

	private static String[] parseUri(@NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		String url = uri;
		// [0]=host, [1]=share, [2]=path
		String[] urlData = new String[3];
		urlData[0] = "";
		urlData[1] = "";
		urlData[2] = "";

		String tmp = "";
		int idx;

		// URLをホスト、共有フォルダ、パスに分解する
		if (url.startsWith("smb://")) {
			tmp = url.substring("smb://".length());
			idx = tmp.indexOf("/");
			if (idx >= 0) {
				// 最初の "/" より前をhost、後をpathに代入
				urlData[0] = tmp.substring(0, idx);
				tmp = tmp.substring(idx + 1);
			} else {
				urlData[0] = tmp;
				tmp = "";

			}
			idx = tmp.indexOf("/", 1);
			if (idx >= 0) {
				// 2番目の "/" より前をshare、後をpathに代入
				urlData[1] = tmp.substring(0, idx);
				urlData[2] = tmp.substring(idx + 1);
			} else {
				urlData[1] = tmp;
				urlData[2] = "";
			}
		}

		Logcat.d(logLevel, "host=" + urlData[0] + ", share=" + urlData[1] + ", path=" + urlData[2]);
		return urlData;
	}

	private static String[] parseUser(@NonNull final String user) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. user=" + user);

		// [0]=domain, [1]=user
		String[] userData = new String[2];
		userData[0] = "";
		userData[1] = "";

		int idx;

		// URLをホスト、共有フォルダ、パスに分解する
		idx = user.indexOf(";");
		if (idx >= 0) {
			userData[0] = user.substring(0, idx);
			userData[1] = user.substring(idx + 1);
		}
		else {
			userData[0] = "";
			userData[1] = user;
		}

		Logcat.d(logLevel, "domain=" + userData[0] + ", user=" + userData[1]);
		return userData;
	}

	// SMB認証
	public static SmbFile smbFile(@NonNull final String uri, @NonNull final String user, @NonNull final String pass) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		SmbFile sfile = null;
		NtlmPasswordAuthenticator smbAuth;
		CIFSContext context;
		BaseContext baseContext;
		boolean smb_mode = FileSelectActivity.getSmbMode();

		// SMBの基本設定
		Properties prop = new Properties();

		// SMB1, SMB202, SMB210, SMB300, SMB302, SMB311
		if (smb_mode) {
			// ComittoN 互換モードはSMB1決め打ちなのでSMB1に特化する
			prop.setProperty("jcifs.smb.client.minVersion", "SMB1");
			prop.setProperty("jcifs.smb.client.maxVersion", "SMB1");
		}
		else {
			prop.setProperty("jcifs.smb.client.minVersion", "SMB1");
			prop.setProperty("jcifs.smb.client.maxVersion", "SMB311");
		}

		// https://github.com/AgNO3/jcifs-ng/issues/171
		prop.setProperty("jcifs.traceResources", "false");

		// JCIFSのログを出力しない
		prop.setProperty("jcifs.util.loglevel", "0");

		//prop.setProperty("jcifs.smb.lmCompatibility", "3");
		//prop.setProperty("jcifs.smb.client.useExtendedSecuruty", "true");
		//prop.setProperty("jcifs.smb.useRawNTLM", "true");
		//prop.setProperty("jcifs.smb.client.signingPreferred", "true");
		//prop.setProperty("jcifs.smb.client.useSMB2Negotiation", "true");
		//prop.setProperty("jcifs.smb.client.ipcSigningEnforced", "true");

		//prop.setProperty("jcifs.smb.client.signingEnforced", "true");
		//prop.setProperty("jcifs.smb.client.disableSpnegoIntegrity", "true");

		try {
			// BaseContextではコネクションが足りなくなるため、SingletonContextを使用する
			//Configuration config = new PropertyConfiguration(prop);
			//context = new BaseContext(config);
			SingletonContext.init(prop);
		} catch (CIFSException e) {
			// 既に認証している
			Logcat.d(logLevel, "", e);
		}

		String[] userData = parseUser(user);
		// [0]=domain, [1]=user
		String domain = userData[0];
		String real_user = userData[1];

		try {
			if (domain != null && !domain.isEmpty() && !smb_mode) {
				//接続コンテキストを作成する
				smbAuth = new NtlmPasswordAuthenticator(domain, real_user, pass);
				context = SingletonContext.getInstance().withCredentials(smbAuth);
			} else if (real_user != null && !real_user.isEmpty() && !(real_user.equalsIgnoreCase("guest") && pass.isEmpty())) {
				//接続コンテキストを作成する
				if (smb_mode) {
					// ComittoN 互換モードでアクセスする
					baseContext = new BaseContext(new PropertyConfiguration(prop));
					// 旧式の関数NtlmPasswordAuthenticationを使用する
					smbAuth = new NtlmPasswordAuthentication(baseContext, "", real_user, pass);
					context = baseContext.withCredentials(smbAuth);
				}
				else {
					smbAuth = new NtlmPasswordAuthenticator(real_user, pass);
					context = SingletonContext.getInstance().withCredentials(smbAuth);
				}
			} else if (real_user.equalsIgnoreCase("guest") && pass.isEmpty()) {
				// Guest認証を期待するWindows共有の接続向け
				//接続コンテキストを作成する
				context = SingletonContext.getInstance().withGuestCrendentials();
			} else {
				// Connect with anonymous mode
				//接続コンテキストを作成する
				context = SingletonContext.getInstance().withAnonymousCredentials();
			}
		    try {
		        sfile = new SmbFile(uri, context);
		    } catch (MalformedURLException e) {
				// 認証できない
				Logcat.e(logLevel, "", e);
		    }
		} catch (CIFSException e) {
			// 認証できない
			Logcat.e(logLevel, "", e);
        }
        return sfile;
	}

	public static String filename(@NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "uri=" + uri);

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

	public static long length(@NonNull final String uri, @NonNull final String user, @NonNull final String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "uri=" + uri);
		long length;
        try {
            length = smbFile(uri, user, pass).length();
        } catch (SmbException e) {
			throw new FileAccessException(TAG + ": length: " + e.getLocalizedMessage());
        }
        return length;
	}

	public static String parent(@NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		String result = uri.replaceFirst("([^/]+?)?/*$", "");
		Logcat.d(logLevel, "終了します. uri=" + uri + ", result=" + result);
		return result;
	}

	public static String relativePath(@NonNull final String base, @NonNull final String target) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. base=" + base + ", target=" + target);

		String result;
		String tmp;

		if (target.startsWith("smb://")) {
			// targetがsmb://で始まるならそのまま返す
			result = target;
			Logcat.d(logLevel, "target が smb:// で始まっています. result=" + result);
		} else {
			// targetがsmb://で始まらないならbaseとtargetを連結する
			result = base + target;
			Logcat.d(logLevel, "target が smb:// で始まっていません. result=" + result);
		}

		// 連続するスラッシュは1つにまとめる
		result = result.replaceAll("/+", "/");
		result = result.replace("smb:/", "smb://");
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

	public static ParcelFileDescriptor openParcelFileDescriptor(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass, @Nullable final Handler handler) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "uri=" + uri + ", user=" + user + ", pass=" + pass);

		ParcelFileDescriptor parcelFileDescriptor = null;

		StorageManagerCompat storageManager = StorageManagerCompat.from(activity);
		try {
			parcelFileDescriptor =
					storageManager.openProxyFileDescriptor(
							ParcelFileDescriptor.MODE_READ_ONLY,
							new SambaProxyFileCallback(activity, uri, user, pass),
							handler);
		} catch (IOException e) {
			throw new FileAccessException(TAG + ": openProxyFileDescriptor: SMB File not found.");
		}

		return parcelFileDescriptor;
	}

	// ユーザ認証付きSambaストリーム
	public static SmbRandomAccessFile openRandomAccessFile(@NonNull final String uri, @NonNull final String user, @NonNull final String pass, @NonNull final String mode) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "uri=" + uri + ", user=" + user + ", pass=" + pass);
		//if (debug) {DEF.StackTrace(TAG, "smbAccessFile: ");}
		SmbRandomAccessFile stream;
		try {
			if (!exists(uri, user, pass)) {
				throw new FileAccessException(TAG + ": smbAccessFile: File not found.");
			}
		} catch (FileAccessException e) {
			throw new FileAccessException(TAG + ": smbAccessFile: File not found.");
		}

		if (!DEF.isUiThread()) {
			// UIスレッドではない時はそのまま実行
			SmbFile sfile = smbFile(uri, user, pass);
            try {
                stream = new SmbRandomAccessFile(sfile, mode);
            } catch (SmbException e) {
				throw new FileAccessException(TAG + ": smbAccessFile: Can not get SmbRandomAccessFile.");
            }
        } else {
			// UIスレッドの時は新しいスレッド内で実行
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<SmbRandomAccessFile> future = executor.submit(new Callable<SmbRandomAccessFile>() {

				@Override
				public SmbRandomAccessFile call() throws SmbException, MalformedURLException, FileAccessException {
					SmbFile sfile = smbFile(uri, user, pass);
					return new SmbRandomAccessFile(sfile, "r");
				}
			});

			try {
				stream = future.get();
			} catch (Exception e) {
				Logcat.e(logLevel, "Can not get SmbRandomAccessFile.", e);
				throw new FileAccessException(TAG + ": smbAccessFile: Can not get SmbRandomAccessFile.");
			}
		}

		return stream;
	}

	public static SmbFileInputStream getInputStream(@NonNull final String uri, @NonNull final String user, @NonNull final String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "uri=" + uri);
		try {
			SmbFile orgfile = smbFile(uri, user, pass);
			return new SmbFileInputStream(orgfile);
		} catch (SmbException e) {
			Logcat.e(logLevel, "", e);
			throw new FileAccessException(TAG + ": getInputStream: " + e.getLocalizedMessage());
		}
	}

	public static SmbFileOutputStream getOutputStream(@NonNull final String uri, @NonNull final String user, @NonNull final String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "uri=" + uri);
		try {
			SmbFile orgfile = smbFile(uri, user, pass);
			if (!orgfile.exists()) {
				// ファイルがなければ作成する
				orgfile.createNewFile();
			}
			return new SmbFileOutputStream(orgfile);
		} catch (SmbException e) {
			Logcat.e(logLevel, "", e);
			throw new FileAccessException(TAG + ": getOutputStream: " + e.getLocalizedMessage());
		}
	}

	// ファイル存在チェック
	public static boolean exists(@NonNull final String uri, @NonNull final String user, @NonNull final String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "uri=" + uri + ", user=" + user + ", pass=" + pass);
		boolean result = false;

		if (!DEF.isUiThread()) {
			// UIスレッドではない時はそのまま実行
			SmbFile orgfile;
			orgfile = SmbFileAccess.smbFile(uri, user, pass);

			try {
				result = orgfile.exists();
			} catch (SmbException e) {
				throw new FileAccessException(TAG + ": exists: " + e.getLocalizedMessage());
			}
			return result;

		} else {
			// UIスレッドの時は新しいスレッド内で実行
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<Boolean> future = executor.submit(new Callable<Boolean>() {

				@Override
				public Boolean call() throws FileAccessException {
					SmbFile orgfile;
					boolean result = false;
					orgfile = SmbFileAccess.smbFile(uri, user, pass);
					try {
						result = orgfile.exists();
					} catch (SmbException e) {
						throw new FileAccessException(TAG + ": exists: " + e.getLocalizedMessage());
					}
					return result;
				}
			});

			try {
				result = future.get();
			} catch (Exception e) {
				Logcat.e(logLevel, "", e);
				throw new FileAccessException(TAG + ": exists: " + e.getLocalizedMessage());
			}
		}

		return result;
	}

	public static boolean isDirectory(@NonNull final String uri, @NonNull final String user, @NonNull final String pass) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		boolean result = false;

		// SMBの場合
		if (!DEF.isUiThread()) {
			// UIスレッドではない時はそのまま実行
			SmbFile orgfile;
			orgfile = SmbFileAccess.smbFile(uri, user, pass);
			try {
				result = orgfile.isDirectory();
			} catch (SmbException e) {
				result = false;
			}
		} else {
			// UIスレッドの時は新しいスレッド内で実行
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<Boolean> future = executor.submit(new Callable<Boolean>() {

				@Override
				public Boolean call() throws FileAccessException {
					SmbFile orgfile;
					boolean result = false;
					orgfile = SmbFileAccess.smbFile(uri, user, pass);
					try {
						result = orgfile.isDirectory();
					} catch (SmbException e) {
						result = false;
					}
					return result;
				}
			});

			try {
				result = future.get();
			} catch (Exception e) {
				Logcat.e(logLevel, "File not found.", e);
			}
		}

		Logcat.d(logLevel, "終了します. uri=" + uri + ", result=" + result);
		return result;
	}

	@SuppressLint("SuspiciousIndentation")
    public static ArrayList<FileData> listFiles(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass, @Nullable Handler handler) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", user=" + user + ", pass=" + pass);
		boolean isLocal;

		String[] urlData = parseUri(uri);
		// [0]=host, [1]=share, [2]=path
		String share = urlData[1];

		ArrayList<FileData> fileList = new ArrayList<FileData>();

		if (!DEF.isUiThread()) {
			// UIスレッドではない時はそのまま実行

			// ファイルリストを取得
			File[] lfiles = null;
			SmbFile smbFile;
			SmbFile[] smbFiles = null;
			String[] fnames = null;
			int length;

			// ファイル一覧取得
			smbFile = SmbFileAccess.smbFile(uri, user, pass);

			try {
				if (share.isEmpty()) {
					// ホスト名までしか指定されていない場合
					fnames = smbFile.list();
					if (fnames == null || fnames.length == 0) {
						return fileList;
					}
					length = fnames.length;
				} else {
					// 共有ポイントまで指定済みの場合
					smbFiles = smbFile.listFiles();
					if (smbFiles == null || smbFiles.length == 0) {
						return fileList;
					}
					length = smbFiles.length;
				}
			} catch (SmbException e) {
				Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
				if	(e.getLocalizedMessage().indexOf("directory") != -1)	{
					// カスタムURLスキームのfileopen時にエラーになるので暫定的にToast処理を中断して戻る
					return fileList;
				}
				DEF.sendMessage(activity.getString(R.string.SmbAccessError) + ":\n" + e.getLocalizedMessage(), Toast.LENGTH_LONG, handler);
				return fileList;
			}

			Logcat.d(logLevel, "length=" + length);

			// FileData型のリストを作成
			boolean isDir;
			String name;
			long size = 0;
			long date = 0;

			for (int i = 0; i < length; i++) {
				if (share.isEmpty()) {
					// ホスト名までしか指定されていない場合
					name = fnames[i];
					// 全部フォルダ扱い
					isDir = true;
				} else {
					// 共有ポイントまで指定済みの場合
					name = smbFiles[i].getName();
					isDir = name.endsWith("/");
					try {
						size = smbFiles[i].length();
						date = smbFiles[i].lastModified();
					} catch (SmbException e) {
						Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
						DEF.sendMessage(activity.getString(R.string.SmbAccessError) + ":\n" + e.getLocalizedMessage(), Toast.LENGTH_LONG, handler);
						return fileList;
					}
				}

				if (isDir) {
					// ディレクトリの場合
					if (!name.endsWith("/")) {
						name += "/";
					}
				}

				FileData fileData = new FileData(activity, name, size, date);
				fileList.add(fileData);

				Logcat.d(logLevel, "index=" + (fileList.size() - 1) + ", name=" + fileData.getName() + ", type=" + fileData.getType() + ", extType=" + fileData.getExtType());
			}

			if (!fileList.isEmpty()) {
				Collections.sort(fileList, new FileAccess.FileDataComparator());
			}
		}
		else {
			// UIスレッドの時は新しいスレッド内で実行
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<ArrayList<FileData>> future = executor.submit(new Callable<ArrayList<FileData>>() {

				@Override
				public ArrayList<FileData> call() throws FileAccessException {
					// ファイルリストを取得
					File[] lfiles = null;
					SmbFile smbFile;
					SmbFile[] smbFiles = null;
					String[] fnames = null;
					ArrayList<FileData> fileList = new ArrayList<FileData>();
					int length;

					// ファイル一覧取得
					smbFile = SmbFileAccess.smbFile(uri, user, pass);

					try {
						if (share.isEmpty()) {
							// ホスト名までしか指定されていない場合
							fnames = smbFile.list();
							if (fnames == null || fnames.length == 0) {
								return fileList;
							}
							length = fnames.length;
						} else {
							// 共有ポイントまで指定済みの場合
							smbFiles = smbFile.listFiles();
							if (smbFiles == null || smbFiles.length == 0) {
								return fileList;
							}
							length = smbFiles.length;
						}
					} catch (SmbException e) {
						Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
						DEF.sendMessage(activity.getString(R.string.SmbAccessError) + ":\n" + e.getLocalizedMessage(), Toast.LENGTH_LONG, handler);
						return fileList;
					}

					Logcat.d(logLevel, "length=" + length);

					// FileData型のリストを作成
					boolean isDir;
					String name;
					long size = 0;
					long date = 0;

					for (int i = 0; i < length; i++) {
						if (share.isEmpty()) {
							// ホスト名までしか指定されていない場合
							name = fnames[i];
							// 全部フォルダ扱い
							isDir = true;
						} else {
							// 共有ポイントまで指定済みの場合
							name = smbFiles[i].getName();
							isDir = name.endsWith("/");
							try {
								size = smbFiles[i].length();
								date = smbFiles[i].lastModified();
							} catch (SmbException e) {
								Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
								DEF.sendMessage(activity.getString(R.string.SmbAccessError) + ":\n" + e.getLocalizedMessage(), Toast.LENGTH_LONG, handler);
								return fileList;
							}
						}

						if (isDir) {
							// ディレクトリの場合
							if (!name.endsWith("/")) {
								name += "/";
							}
						}

						FileData fileData = new FileData(activity, name, size, date);
						fileList.add(fileData);

						Logcat.d(logLevel, "index=" + (fileList.size() - 1) + ", name=" + fileData.getName() + ", type=" + fileData.getType() + ", extType=" + fileData.getExtType());
					}

					if (!fileList.isEmpty()) {
						Collections.sort(fileList, new FileAccess.FileDataComparator());
					}
					return fileList;
				}
			});
			try {
				fileList = future.get();
			} catch (Exception e) {
				Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
				DEF.sendMessage(activity.getString(R.string.SmbAccessError) + ":\n" + e.getLocalizedMessage(), Toast.LENGTH_LONG, handler);
				return fileList;
			}
		}
		return fileList;
	}

	public static boolean renameTo(@NonNull final String uri, @NonNull final String fromfile, @NonNull final String tofile, @NonNull final String user, @NonNull final String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "uri=" + uri + ", fromfile=" + fromfile + ", tofile=" + tofile + ", user=" + user + ", pass=" + pass);
		if (tofile.indexOf('/') > 0) {
			throw new FileAccessException(TAG + ": renameTo: Invalid file name.");
		}

		if (!DEF.isUiThread()) {
			// UIスレッドではない時はそのまま実行
			SmbFile orgfile;
			try {
				orgfile = SmbFileAccess.smbFile(uri + fromfile, user, pass);
				if (!orgfile.exists()) {
					// 変更前ファイルが存在しなければエラー
					Logcat.e(logLevel, "File not found.");
					throw new FileAccessException(TAG + ": renameTo: File not found.");
				}
			} catch (SmbException e) {
				Logcat.e(logLevel, "", e);
				throw new FileAccessException(TAG + ": renameTo: " + e.getLocalizedMessage());
			}

			SmbFile dstfile;
			try {
				dstfile = SmbFileAccess.smbFile(uri + tofile, user, pass);
				if (dstfile.exists()) {
					// 変更後ファイルが存在すればエラー
					Logcat.e(logLevel, "File access error.");
					throw new FileAccessException(TAG + ": renameTo: File access error.");
				}
			} catch (SmbException e) {
				Logcat.e(logLevel, "", e);
				throw new FileAccessException(TAG + ": renameTo: " + e.getLocalizedMessage());
			}

			// ファイル名変更
			try {
				orgfile.renameTo(dstfile);
				return dstfile.exists();
			} catch (SmbException e) {
				Logcat.e(logLevel, "", e);
				throw new FileAccessException(TAG + ": renameTo: " + e.getLocalizedMessage());
			}
		}
		else {
			// UIスレッドの時は新しいスレッド内で実行
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<Boolean> future = executor.submit(new Callable<Boolean>() {

				@Override
				public Boolean call() throws FileAccessException {
					SmbFile orgfile;
					try {
						orgfile = SmbFileAccess.smbFile(uri + fromfile, user, pass);
						if (!orgfile.exists()) {
							// 変更前ファイルが存在しなければエラー
							Logcat.e(logLevel, "File not found.");
							throw new FileAccessException(TAG + ": renameTo: File not found.");
						}
					} catch (SmbException e) {
						Logcat.e(logLevel, "", e);
						throw new FileAccessException(TAG + ": renameTo: " + e.getLocalizedMessage());
					}

					SmbFile dstfile;
					try {
						dstfile = SmbFileAccess.smbFile(uri + tofile, user, pass);
						if (dstfile.exists()) {
							// 変更後ファイルが存在すればエラー
							Logcat.e(logLevel, "File access error.");
							throw new FileAccessException(TAG + ": renameTo: File access error.");
						}
					} catch (SmbException e) {
						Logcat.e(logLevel, "", e);
						throw new FileAccessException(TAG + ": renameTo: " + e.getLocalizedMessage());
					}

					// ファイル名変更
					try {
						orgfile.renameTo(dstfile);
						return dstfile.exists();
					} catch (SmbException e) {
						Logcat.e(logLevel, "", e);
						throw new FileAccessException(TAG + ": renameTo: " + e.getLocalizedMessage());
					}
				}
			});
			try {
				return future.get();
			} catch (Exception e) {
				throw new FileAccessException(TAG + ": renameTo: " + e.getLocalizedMessage());
			}
		}
	}

	// タイムスタンプ
	public static long date(@NonNull final String uri, @NonNull final String user, @NonNull final String pass) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		try {
			SmbFile smbFile = SmbFileAccess.smbFile(uri, user, pass);
			return smbFile.lastModified();
		} catch (SmbException e) {
			Logcat.e(logLevel, "", e);
		}
		return 0L;
	}

	// ファイル削除
	public static boolean delete(@NonNull final String uri, @NonNull final String user, @NonNull final String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", user=" + user + ", pass=" + pass );

		if (!DEF.isUiThread()) {
			// UIスレッドではない時はそのまま実行
			SmbFile orgfile;
			orgfile = SmbFileAccess.smbFile(uri, user, pass);
			try {
				orgfile.delete();
				return !orgfile.exists();
			} catch (SmbException e) {
				throw new FileAccessException(TAG + ": delete: " + e.getLocalizedMessage());
			}
		} else {
			// UIスレッドの時は新しいスレッド内で実行
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<Boolean> future = executor.submit(new Callable<Boolean>() {

				@Override
				public Boolean call() throws FileAccessException {
					SmbFile orgfile;
					orgfile = SmbFileAccess.smbFile(uri, user, pass);
					try {
						orgfile.delete();
						return !orgfile.exists();
					} catch (SmbException e) {
						throw new FileAccessException(TAG + ": delete: " + e.getLocalizedMessage());
					}
				}
			});
			try {
				return future.get();
			} catch (Exception e) {
				throw new FileAccessException(TAG + ": delete: " + e.getLocalizedMessage());
			}
		}
	}

	// ディレクトリ作成
	public static boolean mkdir(@NonNull final String uri, @NonNull final String user, @NonNull final String pass, @NonNull final String item) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", item=" + item);

		boolean result = false;
		if (!DEF.isUiThread()) {
			try {
				// UIスレッドではない時はそのまま実行
				SmbFile smbFile = smbFile(uri + item, user, pass);
				if (smbFile.exists()) {
					result = false;
				}
				else {
					smbFile.mkdir();
					result = smbFile.exists();
				}
			} catch (SmbException e) {
				result = false;
				Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
			}
		} else {
			// UIスレッドの時は新しいスレッド内で実行
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<Boolean> future = executor.submit(new Callable<Boolean>() {

				@Override
				public Boolean call() throws FileAccessException {
					try {
						SmbFile smbFile = smbFile(uri + item, user, pass);
						if (smbFile.exists()) {
							return false;
						}
						else {
							smbFile.mkdir();
							return smbFile.exists();
						}
					} catch (SmbException e) {
						Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
						return false;
					}
				}
			});
			try {
				result = future.get();
			} catch (Exception e) {
				result = false;
				Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
			}
		}
		return result;
	}

	// ファイル作成
	public static boolean createFile(@NonNull final String uri, @NonNull final String user, @NonNull final String pass, @NonNull final String item) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", item=" + item);

		boolean result = false;
		if (!DEF.isUiThread()) {
			try {
				// UIスレッドではない時はそのまま実行
				SmbFile smbFile = smbFile(uri + item, user, pass);
				if (smbFile.exists()) {
					result = false;
				}
				else {
					smbFile.createNewFile();
					result = smbFile.exists();
				}
			} catch (SmbException e) {
				result = false;
				Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
			}
		} else {
			// UIスレッドの時は新しいスレッド内で実行
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<Boolean> future = executor.submit(new Callable<Boolean>() {

				@Override
				public Boolean call() throws FileAccessException {
					try {
						SmbFile smbFile = smbFile(uri + item, user, pass);
						if (smbFile.exists()) {
							return false;
						}
						else {
							smbFile.createNewFile();
							return smbFile.exists();
						}
					} catch (SmbException e) {
						Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
						return false;
					}
				}
			});
			try {
				result = future.get();
			} catch (Exception e) {
				result = false;
				Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
			}
		}
		return result;
	}

}
