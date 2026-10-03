package src.comitton.fileaccess;

import android.content.Context;
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
import src.comitton.fileview.data.RecordItem;

// 栞(ブックマーク)を複数端末間で共有するための同期用HTTPクライアント。
// EverythingClient.javaと同じ流儀(明示的なバックグラウンドスレッド、HttpURLConnection、
// Gsonでのシリアライズ、Basic認証、Handler経由のコールバック)で実装する。
// push系(追加/削除)はベストエフォートで、失敗してもローカルの栞データには影響しない。
public class BookmarkSyncClient {
    private static final String TAG = "BookmarkSyncClient";

    // サーバー側のJSONスキーマに対応するDTO
    private static class BookmarkDto {
        String host;
        String path;
        String file;
        int page;
        String image;
        int chapter;
        double pagerate;
        String dispname;
        int type;
        long date;
    }

    private static BookmarkDto toDto(RecordItem item, String host) {
        BookmarkDto dto = new BookmarkDto();
        dto.host = host;
        dto.path = item.getPath();
        dto.file = item.getFile();
        dto.page = item.getPage();
        dto.image = item.getImage();
        dto.chapter = item.getChapter();
        dto.pagerate = item.getPageRate();
        dto.dispname = item.getDispName();
        dto.type = item.getType();
        dto.date = item.getDate();
        return dto;
    }

    private static boolean isConfigured(SharedPreferences sp) {
        return !sp.getString(DEF.KEY_BOOKMARKSYNC_HOST, "").isEmpty();
    }

    private static String baseUrl(SharedPreferences sp) {
        String host = sp.getString(DEF.KEY_BOOKMARKSYNC_HOST, "");
        String port = sp.getString(DEF.KEY_BOOKMARKSYNC_PORT, "8081");
        return "http://" + host + ":" + port + "/bookmarks";
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
     * 栞の追加/更新をサーバーへ反映する(ベストエフォート、fire-and-forget)。
     * @param host このRecordItemが属するSMBサーバーのホスト名(ServerSelect.getHost(item.getServer())で解決したもの)
     */
    public static void pushUpsert(final Context context, final RecordItem item, final String host, final SharedPreferences sp) {
        if (host == null || host.isEmpty() || !isConfigured(sp)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int logLevel = Logcat.LOG_LEVEL_WARN;
                HttpURLConnection conn = null;
                try {
                    String json = new Gson().toJson(toDto(item, host));

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
                        Logcat.e(logLevel, "BookmarkSync push failed: HTTP " + responseCode);
                    }
                }
                catch (Exception e) {
                    // ベストエフォート: ローカルの栞は既に保存済みなので、同期失敗はログのみ
                    Logcat.e(logLevel, "BookmarkSync push error", e);
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
     * 栞の削除をサーバーへ反映する(ベストエフォート、fire-and-forget)。
     */
    public static void pushDelete(final Context context, final RecordItem item, final String host, final SharedPreferences sp) {
        if (host == null || host.isEmpty() || !isConfigured(sp)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int logLevel = Logcat.LOG_LEVEL_WARN;
                HttpURLConnection conn = null;
                try {
                    String json = new Gson().toJson(toDto(item, host));

                    conn = (HttpURLConnection) new URL(baseUrl(sp)).openConnection();
                    conn.setRequestMethod("DELETE");
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
                        Logcat.e(logLevel, "BookmarkSync delete failed: HTTP " + responseCode);
                    }
                }
                catch (Exception e) {
                    Logcat.e(logLevel, "BookmarkSync delete error", e);
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
     * 指定ホストの栞一覧をサーバーから取得する。結果はHandler経由で返す
     * (msg.what = DEF.HMSG_BOOKMARKSYNC_RESULT, msg.arg1 = server番号, msg.obj = ArrayList&lt;RecordItem&gt;)。
     * 通信に失敗した場合はメッセージを送らない(呼び出し側はローカルデータのみで表示を継続する)。
     */
    public static void pullAll(final Context context, final String host, final int server, final Handler handler, final SharedPreferences sp) {
        if (host == null || host.isEmpty() || handler == null || !isConfigured(sp)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int logLevel = Logcat.LOG_LEVEL_WARN;
                HttpURLConnection conn = null;
                try {
                    String encodedHost = URLEncoder.encode(host, "UTF-8");
                    conn = (HttpURLConnection) new URL(baseUrl(sp) + "?host=" + encodedHost).openConnection();
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
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    BookmarkDto[] dtos = new Gson().fromJson(response.toString(), BookmarkDto[].class);

                    ArrayList<RecordItem> list = new ArrayList<RecordItem>();
                    if (dtos != null) {
                        for (BookmarkDto dto : dtos) {
                            RecordItem item = new RecordItem();
                            item.setType(dto.type);
                            item.setServer(server);
                            item.setPath(dto.path);
                            item.setFile(dto.file);
                            item.setImage(dto.image);
                            item.setChapter(dto.chapter);
                            item.setPageRate((float) dto.pagerate);
                            item.setPage(dto.page);
                            item.setDispName(dto.dispname);
                            item.setDate(dto.date);
                            list.add(item);
                        }
                    }

                    Message msg = handler.obtainMessage(DEF.HMSG_BOOKMARKSYNC_RESULT, server, 0, list);
                    handler.sendMessage(msg);
                }
                catch (Exception e) {
                    Logcat.e(logLevel, "BookmarkSync pull error", e);
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
