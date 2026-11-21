package src.comitton.fileaccess;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.fileview.data.LibraryEntry;

/**
 * 書庫管理タブ用、bookmark-sync-serverの /library エンドポイントに対するクライアント。
 * サーバー側でPVEのスキャンスクリプトが定期的に全件を丸ごと置き換えているため
 * (GET /libraryは常に最新の完全なカタログ)、このクライアントも全ページを取得したら
 * ローカルキャッシュを丸ごと置き換える前提で結果を返す(呼び出し元でLibraryCache.clearAll()する)。
 * BookmarkSyncClient/HistorySyncClientと同じホスト設定(KEY_BOOKMARKSYNC_*)を再利用する。
 */
public class LibrarySyncClient {
    private static final String TAG = "LibrarySyncClient";
    private static final int PAGE_LIMIT = 500;

    private static class Page {
        int total;
        int offset;
        int limit;
        ArrayList<Item> results;
    }

    private static class Item {
        String path;
        String name;
        long size;
        long date_modified;
        String work_title;
        // 手動補正された巻番号。補正のあるファイルにだけ付く(古いサーバーでは常にnull)。
        String volumes;
    }

    private static boolean isConfigured(SharedPreferences sp) {
        return !sp.getString(DEF.KEY_BOOKMARKSYNC_HOST, "").isEmpty();
    }

    private static String baseUrl(SharedPreferences sp) {
        String host = sp.getString(DEF.KEY_BOOKMARKSYNC_HOST, "");
        String port = sp.getString(DEF.KEY_BOOKMARKSYNC_PORT, "8081");
        return "http://" + host + ":" + port + "/library";
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

    private static Page fetchPage(String url, int offset, SharedPreferences sp) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url + "?offset=" + offset + "&limit=" + PAGE_LIMIT).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);
            applyAuth(conn, sp);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("HTTP " + responseCode);
            }
            return new Gson().fromJson(readBody(conn), Page.class);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String readBody(HttpURLConnection conn) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    private static ArrayList<LibraryEntry> fetchAllEntries(SharedPreferences sp) throws Exception {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        String url = baseUrl(sp);
        ArrayList<LibraryEntry> entries = new ArrayList<>();
        int offset = 0;
        int total = Integer.MAX_VALUE;
        while (offset < total) {
            Page page = fetchPage(url, offset, sp);
            if (page == null || page.results == null || page.results.isEmpty()) {
                break;
            }
            for (Item item : page.results) {
                if (item.name == null || item.name.isEmpty()) {
                    continue;
                }
                LibraryEntry entry = new LibraryEntry(item.path != null ? item.path : "", item.name, item.size, item.date_modified, item.work_title);
                entry.setVolumes(item.volumes);
                entries.add(entry);
            }
            total = page.total;
            offset += page.results.size();
            Logcat.w(logLevel, "書庫管理: /library取得中. offset=" + offset + "/" + total);
        }
        if (total != Integer.MAX_VALUE && offset < total) {
            // 途中のページが空/不正のまま打ち切られた。部分的な一覧でキャッシュを置き換えないよう失敗扱いにする
            throw new java.io.IOException("Incomplete library response: " + offset + "/" + total);
        }
        return entries;
    }

    /**
     * サーバー側の書庫カタログを全ページ取得する。結果はHandler経由で返す
     * (msg.what = DEF.HMSG_LIBRARYSYNC_RESULT, msg.obj = ArrayList&lt;LibraryEntry&gt;)。
     * サーバー未設定または通信失敗時はHMSG_TOASTでエラーを通知するのみで、ローカルキャッシュには触れない。
     */
    public static void pullAll(final Context context, final Handler handler, final SharedPreferences sp) {
        if (handler == null) {
            return;
        }
        if (!isConfigured(sp)) {
            sendError(handler, "Bookmark sync server is not configured.");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<LibraryEntry> entries;
                try {
                    entries = fetchAllEntries(sp);
                }
                catch (Exception e) {
                    Logcat.e(Logcat.LOG_LEVEL_WARN, "書庫管理: /library取得エラー", e);
                    sendError(handler, "Library sync error: " + e.getMessage());
                    return;
                }
                Logcat.w(Logcat.LOG_LEVEL_WARN, "書庫管理: /library全件取得完了. " + entries.size() + "件");
                Message msg = handler.obtainMessage(DEF.HMSG_LIBRARYSYNC_RESULT, entries);
                handler.sendMessage(msg);
            }
        }).start();
    }

    private static void sendError(Handler handler, String error) {
        Message msg = handler.obtainMessage(DEF.HMSG_TOAST, error);
        handler.sendMessage(msg);
    }
}
