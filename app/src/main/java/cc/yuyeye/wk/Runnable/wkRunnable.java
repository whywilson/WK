package cc.yuyeye.wk.Runnable;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import cc.yuyeye.wk.Common;
import cc.yuyeye.wk.Util.InternetUtil;

import static cc.yuyeye.wk.MainActivity.IMEI;
import static cc.yuyeye.wk.MainActivity.getCurrentTime;
import static cc.yuyeye.wk.Fragment.ChatFragment.msgDetail;
import static cc.yuyeye.wk.MainActivity.phoneAlias;
import static cc.yuyeye.wk.MainActivity.sendPerson;
import static org.apache.http.protocol.HTTP.UTF_8;
import cc.yuyeye.wk.Fragment.*;
import cc.yuyeye.wk.Util.*;

public class wkRunnable {
    public static String wk_notifyUrl = "http://api.yuyeye.cc/wk_notify.php";
    public static String wk_messageUrl = "http://api.yuyeye.cc/wk_message.php";
    public static String wk_loginUrl = "http://api.yuyeye.cc/wk_login.php";
    public static String wk_reportUrl = "http://api.yuyeye.cc/wk_report.php";
    public static String wk_timeUrl = "http://api.yuyeye.cc/wk_time.php";
    public static String wk_msgAddUrl = "http://api.yuyeye.cc/wk_msgAdd.php";
	public static String report = "";
    private static String currentTime;

    public static Runnable runWk_message = new Runnable() {
        public void run() {
            String LOG_TAG = "runWk_message";

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair("alias", sendPerson));
            nameValuePairs.add(new BasicNameValuePair("title", phoneAlias));
            nameValuePairs.add(new BasicNameValuePair("msg_content", msgDetail));
            nameValuePairs.add(new BasicNameValuePair("report", report));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(wk_messageUrl);
                httppost.setEntity(new UrlEncodedFormEntity((nameValuePairs), UTF_8));
                httpclient.execute(httppost);
                report = "";
            } catch (Exception e) {
                Log.e(LOG_TAG, "联网错误 " + e.toString());
            }
        }
    };
	
    public static Runnable runWk_msgAdd = new Runnable() {
        String LOG_TAG = "runWk_msgAdd";

        public void run() {
            //获取当前时间
            String currentTime = getCurrentTime();
            Log.i(LOG_TAG, IMEI + " " + currentTime + " " + msgDetail);
            //confict to msg from wk
            //checkSendPersion();
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair("IMEI", IMEI));
            nameValuePairs.add(new BasicNameValuePair("alias", phoneAlias));
            nameValuePairs.add(new BasicNameValuePair("accept", sendPerson));
            nameValuePairs.add(new BasicNameValuePair("time", currentTime));
            nameValuePairs.add(new BasicNameValuePair("msg", msgDetail));

            try {
                if (!msgDetail.equals("")) {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(wk_msgAddUrl);
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                    HttpResponse response = httpclient.execute(httppost);
                    response.getEntity();
                    Log.i(LOG_TAG, "上传成功");
                } else {
                    Log.i(LOG_TAG, "内容为空，不上传");
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "上传失败 " + e.toString());
            }
        }
    };
	
    public static Runnable runWk_notify = new Runnable() {
        public void run() {
            String LOG_TAG = "runWk_notify";
            currentTime = getCurrentTime();

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair("alias", ChatFragment.msgAcceptPerson));
            nameValuePairs.add(new BasicNameValuePair("title", ChatFragment.msgTitle));
            nameValuePairs.add(new BasicNameValuePair("message", msgDetail));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(wk_notifyUrl);
                httppost.setEntity(new UrlEncodedFormEntity((nameValuePairs), UTF_8));
                httpclient.execute(httppost);
            } catch (Exception e) {
                Log.e(LOG_TAG, "联网错误 " + e.toString());
            }
        }
    };

    public static Runnable runWk_login = new Runnable() {
        public void run() {
            //获取当前时间
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.PRC);
            String currentTime = sDateFormat.format(new java.util.Date());
            String nettype = InternetUtil.getNetworkState(Common.getContext()) + "";
            String localIp = InternetUtil.getLocalIp(Common.getContext()) + "";
            String netIp = InternetUtil.getNetIp() + "";

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair("imei", IMEI));
            nameValuePairs.add(new BasicNameValuePair("alias", phoneAlias));
            nameValuePairs.add(new BasicNameValuePair("time", currentTime));
            nameValuePairs.add(new BasicNameValuePair("version", Integer.toString(Common.localVersion)));
            nameValuePairs.add(new BasicNameValuePair("vname", Common.localVersionName));
            nameValuePairs.add(new BasicNameValuePair("nettype", nettype));
            nameValuePairs.add(new BasicNameValuePair("ip", netIp));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(wk_loginUrl);
                httppost.setEntity(new UrlEncodedFormEntity((nameValuePairs), UTF_8));
                httpclient.execute(httppost);
                
				LogUtil.i("wkrunnable","login \n" + IMEI +" " + phoneAlias + " " + currentTime + " " + Common.localVersion + " " + Common.localVersionName + " " + currentTime + " " + nettype + " " + netIp);
            } catch (Exception e) {
                LogUtil.e("wkrunnable", "Login错误 " + e.toString());
            }
        }
    };


}
