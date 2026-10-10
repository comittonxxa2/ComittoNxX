package src.comitton.fileaccess;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class WebDavUtil {
	// WebDAV用のURIを正規化する(スキーム補正+二重スラッシュ除去+二重エンコード防止)
	public static String normalizeUri(String uri) {
		if (uri == null || uri.isEmpty()) {
			return uri;
		}
		String normalized = uri.trim();
		// カスタムスキーム(webdav://, dav:// 等)を標準の http:// に変換
		if (normalized.startsWith("webdavs://")) {
			normalized = "http://" + normalized.substring("webdavs://".length());
		}
		else if (normalized.startsWith("davs://")) {
			normalized = "http://" + normalized.substring("davs://".length());
		}
		else if (normalized.startsWith("webdav://")) {
			normalized = "http://" + normalized.substring("webdav://".length());
		}
		else if (normalized.startsWith("dav://")) {
			normalized = "http://" + normalized.substring("dav://".length());
		}
		// スキームが未入力の場合("192.168.xx.xx" など)はデフォルトで http:// を補完
		if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
			normalized = "http://" + normalized;
		}
		// 事前にURLエンコードされている場合のためにデコード(二重エンコード防止)
		String decodedUri;
		try {
			decodedUri = URLDecoder.decode(normalized, StandardCharsets.UTF_8.name());
		}
		catch (Exception e) {
			decodedUri = normalized;
		}
		// http:// や https:// 以外の場所にある二重スラッシュ "//" を単一の "/" に統合
		return decodedUri.replaceAll("(?<!:)/{2,}", "/");
	}
}
