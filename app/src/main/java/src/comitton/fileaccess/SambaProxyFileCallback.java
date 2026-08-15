package src.comitton.fileaccess;

import android.content.Context;
import android.system.ErrnoException;
import android.system.OsConstants;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// JCIFS-NGとSMBJの両対応させるため書き直した
public class SambaProxyFileCallback extends StorageManagerCompat.ProxyFileDescriptorCallbackCompat {
	private static final String TAG = "SambaProxyFileCallback";

	// ネットワークI/Oを非同期処理するためのバックグラウンドスレッドプール
	private static final ExecutorService sExecutor = Executors.newCachedThreadPool();

	private final String mUri;
	private final String mUser;
	private final String mPass;
	private final String mMode;

	private SmbRandomAccessFileCompat mRaf;

	public SambaProxyFileCallback(Context context, String uri, String user, String pass, String mode) {
		this.mUri = uri;
		this.mUser = user;
		this.mPass = pass;
		this.mMode = mode != null ? mode : "r";
	}

	public SambaProxyFileCallback(Context context, String uri, String user, String pass) {
		this(context, uri, user, pass, "r");
	}

	public SambaProxyFileCallback(String uri, String user, String pass, String mode) {
		this(null, uri, user, pass, mode);
	}

	public SambaProxyFileCallback(String uri, String user, String pass) {
		this(null, uri, user, pass, "r");
	}

	private synchronized void ensureOpen() throws IOException {
		if (mRaf != null) {
			return;
		}
		Future<SmbRandomAccessFileCompat> future = sExecutor.submit(() -> 
			SmbFileAccess.openRandomAccessFile(mUri, mUser, mPass, mMode)
		);
		try {
			mRaf = future.get();
		}
		catch (Exception e) {
			Throwable cause = e.getCause() != null ? e.getCause() : e;
			throw new IOException("Failed to open SMB file: " + cause.getMessage(), cause);
		}
	}

	@Override
	public long onGetSize() throws ErrnoException {
		Future<Long> future = sExecutor.submit(() -> {
			ensureOpen();
			return mRaf.length();
		});
		try {
			return future.get();
		}
		catch (Exception e) {
			android.util.Log.e(TAG, "onGetSize error", e);
			throw new ErrnoException("onGetSize", OsConstants.EIO);
		}
	}

	@Override
	public int onRead(long offset, int size, byte[] data) throws ErrnoException {
		Future<Integer> future = sExecutor.submit(() -> {
			ensureOpen();
			mRaf.seek(offset);
			int read = mRaf.read(data, 0, size);
			return read < 0 ? 0 : read;
		});
		try {
			return future.get();
		}
		catch (Exception e) {
			android.util.Log.e(TAG, "onRead error", e);
			throw new ErrnoException("onRead", OsConstants.EIO);
		}
	}

	@Override
	public void onRelease() {
		if (mRaf != null) {
			sExecutor.submit(() -> {
				try {
					mRaf.close();
				}
				catch (Exception e) {
					android.util.Log.e(TAG, "onRelease error", e);
				}
				finally {
					mRaf = null;
				}
			});
		}
	}
}
