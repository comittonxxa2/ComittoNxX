package src.comitton.fileaccess;

public class SmbUrlInfo {
	private String domain = "";
	private String username = "";
	private String password = "";
	private String host = "";
	private int port = -1;
	private String share = "";
	private String path = "";

	public SmbUrlInfo() {}

	public SmbUrlInfo(String uri) {
		parseUri(uri);
	}

	public void parseUri(String smbUri) {
		if (smbUri == null || smbUri.isEmpty()) return;
		// "smb://"プレフィックスの除去
		String temp = smbUri.startsWith("smb://") ? smbUri.substring(6) : smbUri;
		// ユーザー情報(@)の分解 (例: domain;user:pass@host)
		int userAtIdx = temp.indexOf('@');
		if (userAtIdx != -1) {
			String userInfo = temp.substring(0, userAtIdx);
			temp = temp.substring(userAtIdx + 1);

			if (userInfo.contains(";")) {
				String[] parts = userInfo.split(";", 2);
				this.domain = parts[0];
				userInfo = parts[1];
			}
			if (userInfo.contains(":")) {
				String[] parts = userInfo.split(":", 2);
				this.username = parts[0];
				this.password = parts[1];
			}
			else {
				this.username = userInfo;
			}
		}
		// ホストと共有名/パスの分解
		int slashIdx = temp.indexOf('/');
		String hostPort;
		if (slashIdx != -1) {
			hostPort = temp.substring(0, slashIdx);
			String rawPath = temp.substring(slashIdx + 1);

			int shareSlashIdx = rawPath.indexOf('/');
			if (shareSlashIdx != -1) {
				this.share = rawPath.substring(0, shareSlashIdx);
				this.path = rawPath.substring(shareSlashIdx + 1);
			}
			else {
				this.share = rawPath;
				this.path = "";
			}
		}
		else {
			hostPort = temp;
		}
		// ポート番号の分解
		if (hostPort.contains(":")) {
			String[] parts = hostPort.split(":", 2);
			this.host = parts[0];
			try {
				this.port = Integer.parseInt(parts[1]);
			} catch (NumberFormatException ignored) {}
		}
		else {
			this.host = hostPort;
		}
	}

	// ゲッター群
	public String getDomain() { return domain; }
	public String getUsername() { return username; }
	public String getUser() { return username; }
	public String getPassword() { return password; }
	public String getHost() { return host; }
	public int getPort() { return port; }
	public String getShare() { return share; }
	public String getShareName() { return share; }
	public String getPath() { return path; }
	public String getRelativePath() { return path; }
}
