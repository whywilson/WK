package cc.yuyeye.wk.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

public class InternetUtil {
    //没有网络连接  
    public static final int NETWORN_NONE = 0;
    //wifi连接  
    public static final int NETWORN_WIFI = 1;
    //手机网络数据连接类型  
    public static final int NETWORN_2G = 2;
    public static final int NETWORN_3G = 3;
    public static final int NETWORN_4G = 4;
    public static final int NETWORN_MOBILE = 5;
    private static final String TAG = "InternetUtil";

    /**
     * 获取当前网络连接类型 
     * @param context
     * @return
     */
    public static int getNetworkState(Context context) {
        //获取系统的网络服务  
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //如果当前没有网络  
        if (null == connManager)
            return NETWORN_NONE;

        //获取当前网络类型，如果为空，返回无网络  
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return NETWORN_NONE;
        }

        // 判断是不是连接的是不是wifi  
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORN_WIFI;
                }
        }

        // 如果不是wifi，则判断当前连接的是运营商的哪种网络2g、3g、4g等  
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (null != networkInfo) {
            NetworkInfo.State state = networkInfo.getState();
            String strSubTypeName = networkInfo.getSubtypeName();
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    switch (activeNetInfo.getSubtype()) {
                        //如果是2g类型  
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g  
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g  
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g  
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return NETWORN_2G;
                        //如果是3g类型  
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g  
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return NETWORN_3G;
                        //如果是4g类型  
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return NETWORN_4G;
                        default:
                            //中国移动 联通 电信 三种3G制式  
                            if (strSubTypeName.equalsIgnoreCase("TD-SCDMA") || strSubTypeName.equalsIgnoreCase("WCDMA") || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                return NETWORN_3G;
                            } else {
                                return NETWORN_MOBILE;
                            }
                    }
                }
        }
        return NETWORN_NONE;
    }

    public static String getNetIp(){
        String netIp = "127.0.0.1";
        URL infoUrl;
        InputStream inStream;
        try {
            infoUrl = new URL("http://members.3322.org/dyndns/getip");

            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                while ((netIp = reader.readLine()) != null)
                    strber.append(netIp);
                inStream.close();
                netIp = strber.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return netIp;
    }

    public static String getLocalIp(Context context){
        String localIp = "127.0.0.1";
        if(getNetworkState(context) == 1){
            //获取wifi服务
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            //判断wifi是否开启
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            localIp = formatIpAddress(ipAddress);
        }else {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface
                        .getNetworkInterfaces(); en.hasMoreElements();) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                            localIp = inetAddress.getHostAddress().toString();
                        }
                    }
                }
            } catch (SocketException ex) {
                Log.e(TAG,"get GPRS ip Error " + ex);
            }
        }
        return localIp;
    }
    public static String formatIpAddress(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
} 
