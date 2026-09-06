package src.comitton.fileaccess;

import android.annotation.TargetApi;
import android.app.Activity;
import android.util.Log;
import android.system.ErrnoException;
import android.system.OsConstants;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbRandomAccessFile;
import src.comitton.common.DEF;
import src.comitton.common.Logcat;

@TargetApi(26)
public class SambaProxyFileCallback extends StorageManagerCompat.ProxyFileDescriptorCallbackCompat {
    private static final String TAG = "SambaProxyFileCallback";

    private final Activity mActivity;
    private SmbRandomAccessFile mSmbRandomAccessFile = null;
    private final ExecutorService mSmbExecutor = Executors.newSingleThreadExecutor();

    public SambaProxyFileCallback(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass) {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        Logcat.d(logLevel, "uri=" + uri + ", user=" + user + ", pass=" + pass);
        mActivity = activity;
        // オープン処理をバックグラウンドスレッド(mSmbExecutor)のキューに投入
        // コンストラクタ自体はブロックせず即座に復帰するため、UIスレッドから呼ばれても安全になる
        mSmbExecutor.submit(() -> {
            try {
                mSmbRandomAccessFile = SmbFileAccess.openRandomAccessFile(uri, user, pass, "rw");
            } catch (IOException e) {
                Logcat.e(logLevel, "Async Open Error.", e);
            }
        });
    }
    // ファイルがまだオープンされていない場合(またはオープン中の場合)、mSmbExecutorのキュー内で順序が保証されるため、呼び出し時点では自動的にオープン完了後に実行される
    private SmbRandomAccessFile getFileOrThrow() throws IOException {
        if (mSmbRandomAccessFile == null) {
            throw new IOException("File not opened or connection failed.");
        }
        return mSmbRandomAccessFile;
    }

    @Override
    public long onGetSize() {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        try {
            Future<Long> future = mSmbExecutor.submit(() -> {
                SmbRandomAccessFile file = getFileOrThrow();
                return file.length();
            });
            return future.get();
        } catch (RejectedExecutionException e) {
            Logcat.e(logLevel, "Already shut down.", e);
            return 0;
        } catch (Exception e) {
            Logcat.e(logLevel, "Get File Size Error.", e);
            return 0;
        }
    }

    @Override
    public int onRead(long offset, int size, byte[] data) throws ErrnoException {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        try {
            Future<Integer> future = mSmbExecutor.submit(() -> {
                SmbRandomAccessFile file = getFileOrThrow();

                long fileSize = file.length();
                if (offset >= fileSize) {
                    return -1;
                }
                file.seek(offset);
                int rSize = (int) Math.min(size, fileSize - offset);
                if (rSize <= 0) return -1;
                return file.read(data, 0, rSize);
            });
            int read = future.get();
            if (read < 0) {
                // EOFの通知
                throw new ErrnoException("onRead EOF", OsConstants.ENODATA);
            }
            return read;
        } catch (RejectedExecutionException e) {
            // shutdown 後に呼ばれた場合は Bad File Descriptor エラーをシステムに返す
            Logcat.e(logLevel, "Task rejected (Already closed).", e);
            throw new ErrnoException("onRead Rejected", OsConstants.EBADF);
        } catch (ErrnoException e) {
            throw e;
        } catch (Exception e) {
            Logcat.e(logLevel, "File read error. ", e);
            throw new ErrnoException("onRead Error", OsConstants.EIO);
        }
    }

    @Override
    public int onWrite(long offset, int size, byte[] data) throws ErrnoException {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        try {
            Future<Integer> future = mSmbExecutor.submit(() -> {
                SmbRandomAccessFile file = getFileOrThrow();
                file.seek(offset);
                file.write(data, 0, size);
                return size;
            });
            return future.get();
        } catch (RejectedExecutionException e) {
            Logcat.e(logLevel, "Task rejected (Already closed).", e);
            throw new ErrnoException("onWrite Rejected", OsConstants.EBADF);
        } catch (Exception e) {
            Logcat.e(logLevel, "File write error. ", e);
            throw new ErrnoException("onWrite Error", OsConstants.EIO);
        }
    }

    @Override
    public void onFsync() {
        // Nothing to do
    }

    @Override
    public void onRelease() {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        try {
            mSmbExecutor.submit(() -> {
                try {
                    if (mSmbRandomAccessFile != null) {
                        mSmbRandomAccessFile.close();
                    }
                } catch (SmbException e) {
                    Logcat.e(logLevel, "File Release Error.", e);
                } finally {
                    mSmbExecutor.shutdown();
                }
            });
        } catch (RejectedExecutionException e) {
            // すでに shutdown されている場合は無視
            Logcat.w(logLevel, "onRelease called after shutdown.");
        }
    }
}
