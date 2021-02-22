package network.message.lan.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * @author mac
 * Wi-Fi工具类
 */
public class WiFiUtils {

    private static final String TAG = "WifiUtils";

    /**
     * 检查是否处于Wi-Fi网络状态
     * 需要ACCESS_NETWORK_STATE权限
     **/
    public static boolean isWiFiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 获取设备ip地址
     *
     * @return 返回ip地址
     **/
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                //当前使用2G/3G/4G网络
                Log.d(TAG, "getIPAddress: 当前使用数据流量，暂不支持热点，请打开无线网络");
                return null;
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                //当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
                //得到IPV4地址
                Log.d(TAG, "getIPAddress: 当前使用无线网络");
                return ipAddress;
            }
        } else {
            Log.d(TAG, "getIPAddress: 当前无网络连接，暂不支持热点，请打开无线网络");
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     */
    private static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

}
