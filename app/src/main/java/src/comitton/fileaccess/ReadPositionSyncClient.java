package src.comitton.fileaccess;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
                String normalizedFile = DEF.stripArchiveExt(file);
                HttpURLConnection conn = null;
                try {
                    String url = baseUrl(sp)
                            + "?host=" + URLEncoder.encode(host, "UTF-8")
                            + "&path=" + URLEncoder.encode(path == null ? "" : path, "UTF-8")
                            + "&file=" + URLEncoder.encode(normalizedFile, "UTF-8");
                    conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(GET_CONNECT_TIMEOUT_MS);
                    conn.setReadTimeout(GET_READ_TIMEOUT_MS);
                    applyAuth(conn, sp);

                    int responseCode = conn.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        // 404(未登録)を含む。エラーではなく単に情報が無いだけ
                        return;
                    }
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    result[0] = new Gson().fromJson(response.toString(), Position.class);
                }
                catch (Exception e) {
                    Logcat.e(logLevel, "ReadPositionSync get error", e);
                }
                finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        });
        thread.start();
        try {
            // 接続+読み込みタイムアウトの合計時間より少し余裕を持たせて待つ
            thread.join(GET_CONNECT_TIMEOUT_MS + GET_READ_TIMEOUT_MS + 500);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
                }
                catch (Exception e) {
                    Logcat.e(logLevel, "ReadPositionSync pullAllForHost error", e);
                }
                finally {
                    if (conn != null) {
                        conn.disconnect();
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
        if (host == null || host.isEmpty() || file == null || file.isEmpty() || !isConfigured(sp)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int logLevel = Logcat.LOG_LEVEL_WARN;
                HttpURLConnection conn = null;
                try {
                    Position dto = new Position();
                    dto.host = host;
                    dto.path = path == null ? "" : path;
                    dto.file = DEF.stripArchiveExt(file);
                    dto.page = page;
                    dto.maxpage = maxpage;
                    dto.chapter = chapter;
                    dto.pagerate = pagerate;
                    dto.date = System.currentTimeMillis() / 1000;

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
                    }
                }
                catch (Exception e) {
                    Logcat.e(logLevel, "ReadPositionSync push error", e);
                }
                finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }
}
