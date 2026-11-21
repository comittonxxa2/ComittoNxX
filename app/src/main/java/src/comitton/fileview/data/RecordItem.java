package src.comitton.fileview.data;

import android.annotation.SuppressLint;
import android.net.Uri;

import java.text.SimpleDateFormat;

import src.comitton.common.DEF;
import src.comitton.fileview.filelist.ServerSelect;


public class RecordItem {
	public static final int TYPE_NONE		= -1;
	public static final int TYPE_IMAGE		=  0;	// イメージ
	public static final int TYPE_TEXT		=  1;	// テキスト
	public static final int TYPE_COMPTEXT	=  2;	// 圧縮ファイル中テキスト
	public static final int TYPE_FOLDER		=  3;	// ディレクトリオープン
	public static final int TYPE_SERVER		=  4;	// サーバ
	public static final int TYPE_MENU		=  5;	// オプションメニュー
	public static final int TYPE_IMAGEDIRECT		=  6;	// イメージ

	private int accessType;
	private int server;
	private String servername;
	private String path;
	private String file;
	private int type;
	private long date;
	private String image;
	private int chapter;
	private float pagerate;
	private int page;
	private String dispname;
	private String host;
	private String user;
	private String pass;
	private String port;
	private String provider;
	private String uri;
	private int item;
	private int icon;
	private boolean pasv;
	// 書庫管理タブ専用: 通常のファイル一覧(FileListArea)に合わせた既読状況表示・タイトル色分け。
	// 他タブ(栞/履歴/検索等)では未設定のまま(それぞれnull/0)で、従来の表示を維持する。
	private String readStatus;
	private int titleColorOverride;
	private String libraryFolderLabel;
	private String libraryVolumeLabel;

	public RecordItem() {
		this.server = DEF.INDEX_LOCAL;
		this.accessType = 0;
		this.servername = null;
		this.path = null;
		this.file = null;
		this.date = 0;
		this.image = null;
		this.dispname = null;
		this.host = null;
		this.user = null;
		this.pass = null;
		this.port = null;
		this.provider = null;
		this.uri = null;
	}

	public int getAccessType() {
		return accessType;
	}

	public void setAccessType(int accessType) {
		this.accessType = accessType;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getServer() {
		return server;
	}

	public void setServer(int server) {
		this.server = server;
	}

	public String getServerName() {
		return servername;
	}

	public void setServerName(String servername) {
		this.servername = servername == null ? "" : servername;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path == null ? "" : path;
	}

	public String getFile() {
		return this.file;
	}

	public void setFile(String file) {
		this.file = file == null ? "" : file;
	}

	// 日付取得
	public long getDate() {
		return this.date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	// ページ情報
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image == null ? "" : image;
	}

	// ページ情報
	public int getChapter() {
		return this.chapter;
	}

	public void setChapter(int chapter) {
		this.chapter = chapter;
	}

	public float getPageRate() {
		return this.pagerate;
	}

	public void setPageRate(float pagerate) {
		this.pagerate = pagerate;
	}

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getDispName() {
		return dispname;
	}

	public void setDispName(String dispname) {
		this.dispname = dispname == null ? "" : dispname;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host == null ? "" : host;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user == null ? "" : user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass == null ? "" : pass;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port == null ? "" : port;
	}

	public boolean getPasv() {
		return pasv;
	}

	public void setPasv(boolean pasv) {
		this.pasv = pasv;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider == null ? "" : provider;
	}

	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri == null ? "" : uri;
	}

	public int getItem() {
		return item;
	}

	public void setItem(int item) {
		this.item = item;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getReadStatus() {
		return readStatus;
	}

	public void setReadStatus(String readStatus) {
		this.readStatus = readStatus;
	}

	public int getTitleColorOverride() {
		return titleColorOverride;
	}

	public void setTitleColorOverride(int titleColorOverride) {
		this.titleColorOverride = titleColorOverride;
	}

	public String getLibraryVolumeLabel() {
		return libraryVolumeLabel;
	}

	public void setLibraryVolumeLabel(String libraryVolumeLabel) {
		this.libraryVolumeLabel = libraryVolumeLabel;
	}

	public String getLibraryFolderLabel() {
		return libraryFolderLabel;
	}

	public void setLibraryFolderLabel(String libraryFolderLabel) {
		this.libraryFolderLabel = libraryFolderLabel;
	}

	@SuppressLint("SimpleDateFormat")
	public String getDateStr() {
		String dateStr;
		if (date != 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss]");
			dateStr = sdf.format(date);
		} else {
			dateStr = "[----/--/-- --:--:--]";
		}
		return dateStr;
	}

	// ArrayListのindexOfから呼ばれる比較処理
	public boolean equals(Object obj) {
		RecordItem data = (RecordItem) obj;
		if (this.server != data.getServer()) {
			return false;
		} else if (!compare(this.path, data.getPath())) {
			return false;
		} else if (!compare(this.file, data.getFile())) {
			return false;
		}

		// 一致
		return true;
	}

	// 文字列比較
	private boolean compare(String str1, String str2) {
		if (str1 == null) {
			if (str2 != null) {
				return false;
			}
		} else if (!str1.equals(str2)) {
			return false;
		}
		return true;
	}
}
