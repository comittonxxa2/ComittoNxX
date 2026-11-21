package src.comitton.fileview.filelist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.fileview.data.LibraryEntry;

/**
 * 書庫管理タブ(TYPE_LIBRARY)用のフラットなアーカイブ一覧のキャッシュ/永続化/グルーピングを担当する。
 *
 * RecordList汎用の.dat機構(FILENAME[]索引によるper-type単一ファイル)は使わず、
 * ここで独自に <conf>/library.dat (TSV: path\tname\tsize\tdateModified\ttitle) に永続化する。
 * 個々のアーカイブエントリをフラットなまま保存し、作品単位のグルーピングは表示のたびに
 * buildWorkList()で計算する。タイトル抽出(表記ゆれ吸収・手動補正含む)はサーバー側
 * (bookmark-sync-server /library の work_title)で行っており、ここでは受け取った値を
 * そのままグルーピングキーとして使うだけ(以前ここにあった正規表現ベースの抽出ロジックは
 * サーバー側のauto_title()へ移植済み)。
 */
public class LibraryCache {
    private static final String TAG = "LibraryCache";
    private static final String FILENAME = "library.dat";
    private static final String SEPARATOR = "\t";
    private static final String VOLUMES_PREFIX = "v:";

    private static ArrayList<LibraryEntry> sCache;
    // sCacheの中身が変わるたびに増やす。LocalBookOrigin等が、判定結果を覚えておいてよいか調べるのに使う。
    private static int sVersion = 0;
    // buildWorkList()の結果キャッシュ。作品数×全体正規表現グルーピングは全1万件超だと
    // メインスレッドで数秒かかりうる重い処理のため、タブ再表示/巻オープンのたびに
    // 再計算しない(merge()でデータが変わった時だけ無効化する)。
    private static ArrayList<LibraryWork> sWorkListCache;
    private static boolean sWorkListDirty = true;

    private LibraryCache() {
    }

    private static String getFilePath() {
        return DEF.getConfigDirectory() + FILENAME;
    }

    /**
     * メモリキャッシュ(初回呼び出し時にディスクから遅延ロード)を返す。
     * 呼び出し元はこのリストを直接書き換えないこと(merge()経由で更新する)。
     * merge()/removeEntries()/clearAll()は、このリストを書き換えず新しいリストに差し替えるため、
     * 取得したリストを別スレッドで走査していても、走査中に中身が変わることはない。
     */
    public static synchronized ArrayList<LibraryEntry> getEntries() {
        if (sCache == null) {
            sCache = loadFromDisk();
        }
        return sCache;
    }

    /** キャッシュの中身が変わるたびに増える番号 */
    public static synchronized int getVersion() {
        return sVersion;
    }

    /**
     * 指定エントリ((path, name)一致)をキャッシュから削除して保存する。
     * @return 実際に削除した件数
     */
    public static synchronized int removeEntries(List<LibraryEntry> targets) {
        if (targets == null || targets.isEmpty()) {
            return 0;
        }
        java.util.HashSet<String> keys = new java.util.HashSet<>();
        for (LibraryEntry t : targets) {
            keys.add(t.key());
        }
        ArrayList<LibraryEntry> current = new ArrayList<>(getEntries());
        int before = current.size();
        for (int i = current.size() - 1; i >= 0; i--) {
            if (keys.contains(current.get(i).key())) {
                current.remove(i);
            }
        }
        int removed = before - current.size();
        if (removed > 0) {
            sCache = current;
            sVersion++;
            saveToDisk(current);
            sWorkListDirty = true;
        }
        Logcat.w(Logcat.LOG_LEVEL_WARN, "removeEntries: 対象" + targets.size() + "件中" + removed + "件を削除");
        return removed;
    }

    /**
     * キャッシュを空にする(メモリ・ディスク両方)。
     * サーバー側で大量のファイル名変更/整理が行われた場合、merge()の
     * 「追加・更新のみで削除しない」仕様では対応できない(旧ファイル名の
     * エントリが永久に残る)ため、この場合は一度空にしてから
     * importFromJsonFile()で全件を入れ直す。
     */
    public static synchronized void clearAll() {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        sCache = new ArrayList<>();
        sVersion++;
        sWorkListCache = null;
        sWorkListDirty = true;
        File file = new File(getFilePath());
        if (file.exists() && !file.delete()) {
            Logcat.e(logLevel, "clearAll: " + file.getAbsolutePath() + " の削除に失敗しました");
        }
        Logcat.w(logLevel, "clearAll: 書庫管理キャッシュを空にしました");
    }

    private static ArrayList<LibraryEntry> loadFromDisk() {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        ArrayList<LibraryEntry> list = new ArrayList<>();
        String filepath = getFilePath();
        File file = new File(filepath);
        if (!file.exists()) {
            return list;
        }
        int skipped = 0;
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF-8"), 8192)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(SEPARATOR, -1);
                if (parts.length < 4) {
                    skipped++;
                    continue;
                }
                try {
                    String path = parts[0];
                    String name = parts[1];
                    long size = Long.parseLong(parts[2]);
                    long date = Long.parseLong(parts[3]);
                    // titleは後から追加した列。旧形式(4列のみ)のキャッシュファイルから読んだ場合は
                    // 空になるが、次回の「端末側を更新」でサーバー提供のtitleを含めて丸ごと
                    // 書き直されるため一時的なもの(buildWorkList()側でも空ならファイル名にフォールバックする)。
                    String title = parts.length >= 5 ? parts[4] : "";
                    LibraryEntry entry = new LibraryEntry(path, name, size, date, title);
                    // volumesは後から追加した列。補正ありは"v:"+値(空の補正は"v:"のみ)、補正なしは空。
                    if (parts.length >= 6 && parts[5].startsWith(VOLUMES_PREFIX)) {
                        entry.setVolumes(parts[5].substring(VOLUMES_PREFIX.length()));
                    }
                    list.add(entry);
                }
                catch (NumberFormatException ex) {
                    skipped++;
                    continue;
                }
            }
            }
            Logcat.w(logLevel, "loadFromDisk: " + filepath + " から" + list.size() + "件読み込み(不正行スキップ" + skipped + "件)");
        }
        catch (Exception ex) {
            Logcat.e(logLevel, "loadFromDisk失敗: " + filepath, ex);
        }
        return list;
    }

    private static void saveToDisk(ArrayList<LibraryEntry> list) {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        String filepath = getFilePath();
        File dir = new File(DEF.getConfigDirectory());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            // 書き込み途中の中断で既存のキャッシュが壊れないよう、一時ファイルに書いてから置き換える
            File tmp = new File(filepath + ".tmp");
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmp, false), "UTF-8"), 8192)) {
                for (LibraryEntry e : list) {
                    bw.write(sanitize(e.getPath()) + SEPARATOR + sanitize(e.getName()) + SEPARATOR + e.getSize() + SEPARATOR + e.getDateModified()
                            + SEPARATOR + sanitize(e.getTitle())
                            + SEPARATOR + (e.getVolumes() != null ? VOLUMES_PREFIX + sanitize(e.getVolumes()) : ""));
                    bw.newLine();
                }
            }
            File dest = new File(filepath);
            if (!tmp.renameTo(dest)) {
                if (dest.exists() && !dest.delete()) {
                    throw new java.io.IOException("cannot replace " + filepath);
                }
                if (!tmp.renameTo(dest)) {
                    throw new java.io.IOException("cannot rename " + tmp.getAbsolutePath());
                }
            }
            Logcat.w(logLevel, "saveToDisk: " + filepath + " へ" + list.size() + "件保存");
        }
        catch (Exception ex) {
            Logcat.e(logLevel, "saveToDisk失敗: " + filepath, ex);
        }
    }

    // TSVを壊さないよう、ファイル名/パスに万一タブや改行が含まれていても除去しておく
    private static String sanitize(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\t", " ").replace("\n", " ").replace("\r", " ");
    }

    /**
     * 新規/更新エントリをキャッシュへマージする。(path, name)が一致する既存エントリは上書き、
     * 無ければ追加する。既存エントリを削除することは無い(リフレッシュのlimit=200が最新分のみ
     * 返してきても、それ以前に取り込んだ分は保持され続ける)。
     * @return 新規追加件数
     */
    public static synchronized int merge(List<LibraryEntry> newEntries) {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        if (newEntries == null || newEntries.isEmpty()) {
            Logcat.w(logLevel, "merge: newEntriesが空のため何もしない");
            return 0;
        }
        ArrayList<LibraryEntry> current = new ArrayList<>(getEntries());
        HashMap<String, Integer> indexByKey = new HashMap<>();
        for (int i = 0; i < current.size(); i++) {
            indexByKey.put(current.get(i).key(), i);
        }
        int added = 0;
        int updated = 0;
        for (LibraryEntry e : newEntries) {
            Integer idx = indexByKey.get(e.key());
            if (idx != null) {
                current.set(idx, e);
                updated++;
            }
            else {
                indexByKey.put(e.key(), current.size());
                current.add(e);
                added++;
            }
        }
        sCache = current;
        sVersion++;
        saveToDisk(current);
        sWorkListDirty = true;
        Logcat.w(logLevel, "merge: 受信" + newEntries.size() + "件 → 新規" + added + "件、更新" + updated + "件、マージ後合計" + current.size() + "件");
        return added;
    }

    /**
     * 作品(タイトル)単位でグルーピングした結果。
     */
    public static class LibraryWork {
        public String title;
        public ArrayList<LibraryEntry> volumes;
        public long latestDate;
    }

    /**
     * フラットなエントリ一覧を作品単位でグルーピングし、最新巻の更新日時降順で返す。
     * グルーピングロジックは実データ(10,769件)で検証済みのPythonスクリプトをそのまま移植したもの。
     */
    public static synchronized ArrayList<LibraryWork> buildWorkList(ArrayList<LibraryEntry> flatList) {
        if (!sWorkListDirty && sWorkListCache != null) {
            return sWorkListCache;
        }
        int logLevel = Logcat.LOG_LEVEL_WARN;
        int fallbackCount = 0;
        LinkedHashMap<String, ArrayList<LibraryEntry>> groups = new LinkedHashMap<>();
        for (LibraryEntry e : flatList) {
            // titleはサーバー側(bookmark-sync-server /library のwork_title)で計算済み。
            // 空の場合(旧形式ローカルキャッシュ等)はファイル名を単体作品として扱うフォールバック。
            String title = e.getTitle();
            if (title == null || title.isEmpty()) {
                title = e.getName() != null ? e.getName() : "";
                fallbackCount++;
            }
            ArrayList<LibraryEntry> g = groups.get(title);
            if (g == null) {
                g = new ArrayList<>();
                groups.put(title, g);
            }
            g.add(e);
        }

        ArrayList<LibraryWork> works = new ArrayList<>();
        for (Map.Entry<String, ArrayList<LibraryEntry>> ent : groups.entrySet()) {
            LibraryWork w = new LibraryWork();
            w.title = ent.getKey();
            w.volumes = ent.getValue();
            long latest = 0;
            for (LibraryEntry v : w.volumes) {
                if (v.getDateModified() > latest) {
                    latest = v.getDateModified();
                }
            }
            w.latestDate = latest;
            works.add(w);
        }

        Collections.sort(works, new Comparator<LibraryWork>() {
            @Override
            public int compare(LibraryWork a, LibraryWork b) {
                return Long.compare(b.latestDate, a.latestDate);
            }
        });
        Logcat.w(logLevel, "buildWorkList: " + flatList.size() + "件 → " + works.size() + "作品にグルーピング"
                + "(title未設定によるファイル名フォールバック=" + fallbackCount + "件)");
        sWorkListCache = works;
        sWorkListDirty = false;
        return works;
    }

    // ファイル名の表記ゆれ(連続する半角/全角スペースの個数違いなど)を軽く吸収するための正規化。
    // タイトルの意味自体が異なるもの(表記の大幅な違い)までは吸収しない ―― あくまで
    // 前後の空白除去+連続する空白を半角スペース1個にまとめる程度に留める。
    private static String normalizeWhitespace(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().replaceAll("[\\s　]+", " ");
    }

    // ---- 巻番号の読み取り(規則: comic-library-tools の docs/volume-rules.md と同一) ----
    private static final java.util.regex.Pattern EXT_PATTERN =
            java.util.regex.Pattern.compile("\\.(zip|rar|cbz)$", java.util.regex.Pattern.CASE_INSENSITIVE);
    private static final java.util.regex.Pattern VOL_A =
            java.util.regex.Pattern.compile("(?<![A-Za-z0-9])[Vv](\\d{1,4})(?:-(\\d{1,4}))?s?(?:\\s+DL)?$");
    private static final java.util.regex.Pattern VOL_B =
            java.util.regex.Pattern.compile("第(\\d{1,4})(?:-(\\d{1,4}))?巻s?(?:\\s+DL)?$");
    private static final java.util.regex.Pattern VOL_C =
            java.util.regex.Pattern.compile("(?:^|[\\s_])([上中下])巻(?:\\s|$)");

    // 巻番号の読み取り結果。start<0 は巻数なし。
    public static final class VolumeNumber {
        public final int start;
        public final int end;
        private final String label;

        VolumeNumber(int start, int end, String label) {
            this.start = start;
            this.end = end;
            this.label = label;
        }

        public boolean isValid() {
            return start >= 0;
        }

        // 表示用: 「3巻」「1〜3巻」「1・3巻」「1〜3・5巻」「上巻」。読めないときは空文字。
        public String label() {
            return start < 0 ? "" : label;
        }
    }

    private static final java.util.regex.Pattern VOLUMES_TOKEN =
            java.util.regex.Pattern.compile("(\\d{1,5})(?:-(\\d{1,5}))?");

    private static final VolumeNumber NO_VOLUME = new VolumeNumber(-1, -1, null);

    public static VolumeNumber parseVolumeNumber(String fileName) {
        if (fileName == null) {
            return NO_VOLUME;
        }
        String s = EXT_PATTERN.matcher(fileName).replaceFirst("");
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC).trim();
        java.util.regex.Matcher m = VOL_A.matcher(s);
        if (!m.find()) {
            m = VOL_B.matcher(s);
            if (!m.find()) {
                m = null;
            }
        }
        if (m != null) {
            int a = Integer.parseInt(m.group(1));
            int b = (m.group(2) != null) ? Integer.parseInt(m.group(2)) : a;
            int lo = Math.min(a, b);
            int hi = Math.max(a, b);
            return new VolumeNumber(lo, hi, (lo == hi) ? (lo + "巻") : (lo + "〜" + hi + "巻"));
        }
        m = VOL_C.matcher(s);
        if (m.find()) {
            String k = m.group(1);
            int n = k.equals("上") ? 1 : (k.equals("中") ? 2 : 3);
            return new VolumeNumber(n, n, k + "巻");
        }
        return NO_VOLUME;
    }

    // サーバーの手動補正(volumes)を読む。「3」「1-3」「1,3」「1-3,5」。空文字は巻数なし。
    // 形式が読めないときはnull(=補正なしとして、ファイル名から読む)。
    public static VolumeNumber parseVolumes(String volumes) {
        if (volumes == null) {
            return null;
        }
        String s = java.text.Normalizer.normalize(volumes, java.text.Normalizer.Form.NFKC).trim();
        if (s.isEmpty()) {
            return NO_VOLUME;
        }
        java.util.TreeSet<Integer> set = new java.util.TreeSet<Integer>();
        for (String token : s.split(",")) {
            java.util.regex.Matcher m = VOLUMES_TOKEN.matcher(token.trim());
            if (!m.matches()) {
                return null;
            }
            int a = Integer.parseInt(m.group(1));
            int b = (m.group(2) != null) ? Integer.parseInt(m.group(2)) : a;
            int lo = Math.min(a, b);
            int hi = Math.max(a, b);
            if (hi - lo > 10000) {
                return null;
            }
            for (int n = lo; n <= hi; n++) {
                set.add(n);
            }
        }
        // 続いている巻は「〜」、切れ目は「・」でつなぐ
        StringBuilder sb = new StringBuilder();
        int runStart = -1;
        int prev = -1;
        for (int n : set) {
            if (runStart < 0) {
                runStart = n;
            }
            else if (n != prev + 1) {
                appendRun(sb, runStart, prev);
                runStart = n;
            }
            prev = n;
        }
        appendRun(sb, runStart, prev);
        return new VolumeNumber(set.first(), set.last(), sb.append("巻").toString());
    }

    private static void appendRun(StringBuilder sb, int from, int to) {
        if (sb.length() > 0) {
            sb.append("・");
        }
        sb.append(from);
        if (to != from) {
            sb.append("〜").append(to);
        }
    }

    // エントリの巻番号。サーバーの補正があればそれを、無ければファイル名から読む。
    public static VolumeNumber volumeOf(LibraryEntry e) {
        VolumeNumber v = parseVolumes(e.getVolumes());
        return v != null ? v : parseVolumeNumber(e.getName());
    }

    // 書庫管理タブの巻一覧の並び替え専用(次/前ファイル移動の順序も同じ比較を使う)。
    // 巻番号あり → 開始巻 → 終了巻 → ファイル名(空白正規化)の順。巻番号なしは後ろにファイル名順。
    // DEF.compareFileName()自体はアプリ全体で共用されているため変更しない。
    public static int compareVolumeEntry(LibraryEntry e1, LibraryEntry e2) {
        return compareVolume(volumeOf(e1), volumeOf(e2), e1.getName(), e2.getName());
    }

    public static int compareVolumeName(String name1, String name2) {
        return compareVolume(parseVolumeNumber(name1), parseVolumeNumber(name2), name1, name2);
    }

    private static int compareVolume(VolumeNumber v1, VolumeNumber v2, String name1, String name2) {
        if (v1.isValid() != v2.isValid()) {
            return v1.isValid() ? -1 : 1;
        }
        if (v1.isValid()) {
            if (v1.start != v2.start) {
                return v1.start < v2.start ? -1 : 1;
            }
            if (v1.end != v2.end) {
                return v1.end < v2.end ? -1 : 1;
            }
        }
        return DEF.compareFileName(normalizeWhitespace(name1), normalizeWhitespace(name2));
    }
}
