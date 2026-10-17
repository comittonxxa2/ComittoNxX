package src.comitton.WebSharing;

import android.content.Context;
import android.net.wifi.WifiManager;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class NetworkUtils {

	// Wi-FiのIPアドレスを取得するメソッド
	public static String getWiFiIpAddress(Context context) {
		try {
			WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			if (wifiManager != null) {
				int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
				if (ipAddress != 0) {
					return String.format(
						"%d.%d.%d.%d",
						(ipAddress & 0xff),
						(ipAddress >> 8 & 0xff),
						(ipAddress >> 16 & 0xff),
						(ipAddress >> 24 & 0xff)
					);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
