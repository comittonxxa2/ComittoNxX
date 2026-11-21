package src.comitton.fileaccess;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;

// 既読位置(自動保存される「どこまで読んだか」)を複数端末間で共有するための同期クライアント。
// 栞(BookmarkSyncClient)とは別概念で、同じサーバー(bookmark-sync-server)の別エンドポイントを使う。
// ファイルを開く瞬間に同期的に取得し(サーバーを正として反映してから開く)、
// 閉じる瞬間にベストエフォートで反映する。
public class ReadPositionSyncClient {
    private static final String TAG = "ReadPositionSyncClient";
    // 開く瞬間に問い合わせるため、待たされすぎないよう短めに設定
    private static final int GET_CONNECT_TIMEOUT_MS = 1500;
    private static final int GET_READ_TIMEOUT_MS = 1500;

    public static class Position {
        public String host;
        public String path;
        public String file;
        public int page = DEF.PAGENUMBER_UNREAD;
        public int maxpage = DEF.PAGENUMBER_NONE;
        public int chapter = -1;
        public double pagerate = -1;
        public long date = 0;
    }

    public static boolean isConfigured(SharedPreferences sp) {
        return !sp.getString(DEF.KEY_BOOKMARKSYNC_HOST, "").isEmpty();
    }

    private static String baseUrl(SharedPreferences sp) {
        String host = sp.getString(DEF.KEY_BOOKMARKSYNC_HOST, "");
        String port = sp.getString(DEF.KEY_BOOKMARKSYNC_PORT, "8081");
        return "http://" + host + ":" + port + "/read_position";
    }

    private static void applyAuth(HttpURLConnection conn, SharedPreferences sp) {
        String user = sp.getString(DEF.KEY_BOOKMARKSYNC_USER, "");
        String pass = sp.getString(DEF.KEY_BOOKMARKSYNC_PASS, "");
        if (!user.isEmpty() && !pass.isEmpty()) {
            String auth = user + ":" + pass;
            String basicAuth = "Basic " + android.util.Base64.encodeToString(auth.getBytes(), android.util.Base64.NO_WRAP);
            conn.setRequestProperty("Authorization", basicAuth);
        }
    }

    /**
     * サーバー上の既読位置を同期的に取得する(短いタイムアウト付き)。
     * 未設定・未登録・通信失敗の場合はnullを返す。呼び出し側はローカルの値を使い続けること。
     * 短いタイムアウトを設定しているが、呼び出し元スレッドをその時間だけブロックする点に注意
     * (ファイルを開く前に同期してから開く、という仕様上の意図的な挙動)。
     * Androidではメインスレッドから直接ネットワークアクセスするとNetworkOnMainThreadExceptionが
     * 発生するため、内部でバックグラウンドスレッドを起動してjoin()で待ち合わせる。
     */
    public static Position getRemotePosition(final SharedPreferences sp, final String host, final String path, final String file) {
        if (host == null || host.isEmpty() || file == null || file.isEmpty() || !isConfigured(sp)) {
            return null;
        }
        final int logLevel = Logcat.LOG_LEVEL_WARN;
        final Position[] result = new Position[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 圏外などで送れていない既読位置があれば、先にサーバーへ反映してから問い合わせる
                flushPendingFor(sp, host, path, file);
                try {
                    result[0] = fetchPosition(sp, host, path, DEF.stripArchiveExt(file), GET_CONNECT_TIMEOUT_MS, GET_READ_TIMEOUT_MS, null);
                }
                catch (Exception e) {
                    Logcat.e(logLevel, "ReadPositionSync get error", e);
                }
            }
        });
        long perfJoinStart = System.currentTimeMillis();
        thread.start();
        try {
            // 接続+読み込みタイムアウトの合計時間より少し余裕を持たせて待つ
            thread.join(GET_CONNECT_TIMEOUT_MS + GET_READ_TIMEOUT_MS + 500);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // ビューア起動のたびにこの呼び出しをブロッキングで待つため、遅延が疑われた際に
        // 再現させず切り分けられるよう、閾値を超えた場合のみ恒常的に記録する
        long perfJoinElapsed = System.currentTimeMillis() - perfJoinStart;
        if (perfJoinElapsed > 500) {
            Logcat.w(logLevel, "既読位置のサーバー問い合わせに時間がかかりました. elapsed=" + perfJoinElapsed + "ms, host=" + host + ", file=" + file);
        }
        return result[0];
    }

    /**
     * 指定ホストの既読位置を全件取得する(ファイル一覧画面の「同期」ボタン用)。
     * 結果はHandler経由で返す(msg.what = what, msg.obj = ArrayList&lt;Position&gt;)。
     * 通信に失敗した場合はメッセージを送らない。
     */
    public static void pullAllForHost(final SharedPreferences sp, final String host, final Handler handler, final int what) {
        if (host == null || host.isEmpty() || handler == null || !isConfigured(sp)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int logLevel = Logcat.LOG_LEVEL_WARN;
                HttpURLConnection conn = null;
                boolean sent = false;
                try {
                    String url = baseUrl(sp) + "?host=" + URLEncoder.encode(host, "UTF-8");
                    conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(10000);
                    applyAuth(conn, sp);

                    int responseCode = conn.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        return;
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    Position[] positions = new Gson().fromJson(response.toString(), Position[].class);
                    ArrayList<Position> list = new ArrayList<Position>();
                    if (positions != null) {
                        for (Position p : positions) {
                            list.add(p);
                        }
                    }

                    Message msg = handler.obtainMessage(what, list);
                    handler.sendMessage(msg);
                    sent = true;
                }
                catch (Exception e) {
                    Logcat.e(logLevel, "ReadPositionSync pullAllForHost error", e);
                }
                finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (!sent) {
                        // 取得できなかったことも知らせる(obj が null)。複数のホストに問い合わせる側が、
                        // すべての返事がそろったことを数えられるようにするため。
                        handler.sendMessage(handler.obtainMessage(what, null));
                    }
                }
            }
        }).start();
    }

    /**
     * 既読位置をサーバーへ反映する(ベストエフォート、バックグラウンドスレッドで実行、fire-and-forget)。
     */
    public static void pushPosition(final SharedPreferences sp, final String host, final String path, final String file,
                                     final int page, final int maxpage, final int chapter, final float pagerate) {
        startPush(sp, host, path, file, page, maxpage, chapter, pagerate);
    }

    private static Thread startPush(final SharedPreferences sp, final String host, final String path, final String file,
                                     final int page, final int maxpage, final int chapter, final float pagerate) {
        if (host == null || host.isEmpty() || file == null || file.isEmpty() || !isConfigured(sp)) {
            return null;
        }
        final Position dto = new Position();
        dto.host = host;
        dto.path = path == null ? "" : path;
        dto.file = DEF.stripArchiveExt(file);
        dto.page = page;
        dto.maxpage = maxpage;
        dto.chapter = chapter;
        dto.pagerate = pagerate;
        dto.date = System.currentTimeMillis() / 1000;
        // 送信より先に保留として控えておく(圏外やプロセス終了で送れなくても、あとで送れるように)
        addPending(sp, dto);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int logLevel = Logcat.LOG_LEVEL_WARN;
                try {
                    if (postPosition(sp, dto)) {
                        removePending(sp, dto.host, dto.path, dto.file);
                    }
                }
                catch (Exception e) {
                    Logcat.e(logLevel, "ReadPositionSync push error", e);
                }
            }
        });
        thread.start();
        return thread;
    }

    // ---- 保留(圏外などでサーバーへ送れなかった既読位置) ----
    private static final String KEY_PENDING = "ReadPositionPending";

    private static synchronized List<Position> loadPending(SharedPreferences sp) {
        List<Position> list = null;
        try {
            list = new Gson().fromJson(sp.getString(KEY_PENDING, "[]"), new TypeToken<ArrayList<Position>>() {}.getType());
        }
        catch (Exception e) {
            Logcat.e(Logcat.LOG_LEVEL_WARN, "ReadPositionSync pending load error", e);
        }
        return list != null ? list : new ArrayList<Position>();
    }

    private static boolean samePosition(Position p, String host, String path, String file) {
        return p.host != null && p.host.equals(host) && (p.path == null ? "" : p.path).equals(path == null ? "" : path)
                && p.file != null && p.file.equals(file);
    }

    private static synchronized void addPending(SharedPreferences sp, Position dto) {
        List<Position> list = loadPending(sp);
        for (int i = list.size() - 1; i >= 0; i--) {
            if (samePosition(list.get(i), dto.host, dto.path, dto.file)) {
                list.remove(i);
            }
        }
        list.add(dto);
        sp.edit().putString(KEY_PENDING, new Gson().toJson(list)).apply();
    }

    private static synchronized void removePending(SharedPreferences sp, String host, String path, String file) {
        List<Position> list = loadPending(sp);
        boolean changed = false;
        for (int i = list.size() - 1; i >= 0; i--) {
            if (samePosition(list.get(i), host, path, file)) {
                list.remove(i);
                changed = true;
            }
        }
        if (changed) {
            sp.edit().putString(KEY_PENDING, new Gson().toJson(list)).apply();
        }
    }

    /**
     * 保留中の既読位置のうち、同じ本のものをサーバーへ反映する(呼び出し元スレッドで同期的に実行)。
     * サーバー側の既読位置のほうが新しければ、保留は捨ててサーバーの値を優先する
     * (別の端末で続きを読んでいた場合に、古い位置で上書きしないため)。
     */
    private static void flushPendingFor(SharedPreferences sp, String host, String path, String file) {
        String normalizedFile = DEF.stripArchiveExt(file);
        for (Position p : loadPending(sp)) {
            if (samePosition(p, host, path, normalizedFile)) {
                flushOne(sp, p);
            }
        }
    }

    private static void flushOne(SharedPreferences sp, Position p) {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        try {
            int[] code = new int[1];
            Position remote = fetchPosition(sp, p.host, p.path, p.file, GET_CONNECT_TIMEOUT_MS, GET_READ_TIMEOUT_MS, code);
            if (code[0] != HttpURLConnection.HTTP_OK && code[0] != HttpURLConnection.HTTP_NOT_FOUND) {
                return;	// 通信できなかった(またはエラー)ので、保留のままあとで再試行する
            }
            if (remote != null && remote.date > p.date) {
                Logcat.d(logLevel, "サーバー側の既読位置のほうが新しいため、保留を破棄します. file=" + p.file);
                removePending(sp, p.host, p.path, p.file);
                return;
            }
            if (postPosition(sp, p)) {
                removePending(sp, p.host, p.path, p.file);
            }
        }
        catch (Exception e) {
            Logcat.e(logLevel, "ReadPositionSync flush error", e);
        }
    }

    /** 保留中の既読位置をすべてサーバーへ反映する(バックグラウンド、通信できないときは何もしない) */
    public static void flushAllPendingAsync(final Context context, final SharedPreferences sp) {
        if (!isConfigured(sp) || loadPending(sp).isEmpty() || !isNetworkAvailable(context)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Position p : loadPending(sp)) {
                    flushOne(sp, p);
                }
            }
        }).start();
    }

    /** 通信できるネットワークがあるか(圏外のとき、サーバーへの問い合わせで待たされないための判定) */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return false;
            }
            Network network = cm.getActiveNetwork();
            if (network == null) {
                return false;
            }
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        catch (Exception e) {
            return true;
        }
    }

    // ---- HTTP ----

    // 1件の既読位置を取得する。未登録(404)や通信失敗はnull。codeOutにHTTPステータス(通信失敗は0)を返す
    private static Position fetchPosition(SharedPreferences sp, String host, String path, String normalizedFile,
                                           int connectTimeout, int readTimeout, int[] codeOut) throws Exception {
        HttpURLConnection conn = null;
        try {
            String url = baseUrl(sp)
                    + "?host=" + URLEncoder.encode(host, "UTF-8")
                    + "&path=" + URLEncoder.encode(path == null ? "" : path, "UTF-8")
                    + "&file=" + URLEncoder.encode(normalizedFile, "UTF-8");
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            applyAuth(conn, sp);

            int responseCode = conn.getResponseCode();
            if (codeOut != null) {
                codeOut[0] = responseCode;
            }
            if (responseCode != HttpURLConnection.HTTP_OK) {
                // 404(未登録)を含む。エラーではなく単に情報が無いだけ
                return null;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return new Gson().fromJson(response.toString(), Position.class);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // 既読位置をサーバーへ送る。成功(HTTP 200)ならtrue
    private static boolean postPosition(SharedPreferences sp, Position dto) {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        HttpURLConnection conn = null;
        try {
            String json = new Gson().toJson(dto);

            conn = (HttpURLConnection) new URL(baseUrl(sp)).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            applyAuth(conn, sp);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Logcat.e(logLevel, "ReadPositionSync push failed: HTTP " + responseCode);
                return false;
            }
            return true;
        }
        catch (Exception e) {
            Logcat.e(logLevel, "ReadPositionSync push error", e);
            return false;
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
