package cc.yuyeye.wk;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.yuyeye.wk.Util.SettingUtil;
import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.CustomPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;


public class Common extends Application {
    public static int localVersion;
    public static String localVersionName = "";
    public static int serverVersion;
    public static String serverVersionName = "0.0";
    public static String updateLog;
    public static Boolean isUpdate = false;
    public static Boolean isBetaUpdate = false;
    public static String apkDevUrl = "https://raw.githubusercontent.com/wilsonqq/WK/Dev/app/build.gradle";
    public static String apkMasterUrl = "https://raw.githubusercontent.com/wilsonqq/WK/master/app/build.gradle";
    public static String logDevUrl = "https://raw.githubusercontent.com/wilsonqq/WK/Dev/README.md";
    public static String logMasterUrl = "https://raw.githubusercontent.com/wilsonqq/WK/master/README.md";
    public static String gitVersionName;
    public static String gitVersionCode;
    public static String TAG = "WkApplication";
    public static String downloadDir = "Download/";
    public static Context context;
    public static BasicPushNotificationBuilder soundBuilder;
    public static String phoneAlias;
    public static SharedPreferences startSerSharePre;

    private CustomPushNotificationBuilder vibrateBuilder;
    private WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

    public WindowManager.LayoutParams getWkwmParams() {
        return wmParams;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        try {
            PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            localVersion = packageInfo.versionCode;
            localVersionName = packageInfo.versionName;
            Log.i(TAG, "Local Version " + localVersion + "  ----  " + localVersionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        //new Thread(runWk_version).start();
        context = getApplicationContext();
        startSerSharePre = PreferenceManager.getDefaultSharedPreferences(this);
        startJPush();
    }

    public static Context getContext() {
        return context;
    }

    public void startJPush() {
        phoneAlias = startSerSharePre.getString(SettingUtil.ID_KEY, "");
        if (phoneAlias != "") {
            JPushInterface.init(this);
            vibrateBuilder = new CustomPushNotificationBuilder(this, R.layout.customer_notitfication_layout, R.id.icon, R.id.title, R.id.text);
            vibrateBuilder.statusBarDrawable = R.drawable.ic_local_florist_white_48dp;
            vibrateBuilder.notificationFlags = Notification.FLAG_AUTO_CANCEL;
            vibrateBuilder.notificationDefaults = Notification.DEFAULT_LIGHTS;
            JPushInterface.setPushNotificationBuilder(2, vibrateBuilder);

            soundBuilder = new BasicPushNotificationBuilder(this);
            soundBuilder.statusBarDrawable = R.drawable.ic_bell;
            soundBuilder.notificationFlags = Notification.FLAG_AUTO_CANCEL;  //设置为自动消失和呼吸灯闪烁
            soundBuilder.notificationDefaults = Notification.DEFAULT_LIGHTS;
            JPushInterface.setPushNotificationBuilder(1, soundBuilder);
            JPushInterface.setDebugMode(false);
            JPushInterface.setLatestNotificationNumber(this, 3);

            JPushInterface.setAlias(this, phoneAlias, new TagAliasCallback() {
                @Override
                public void gotResult(int i, String s, Set<String> set) {
                    Log.d("JPush", "Set Tag result is " + s);
                }
            });
        }
    }

    public static StringBuilder inputStreamToString(InputStream is) {
        String line;
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        // Read response until the end
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return full string
        return total;
    }

    public static String removeStr(String a, String b) {
        return a.replaceAll(b, "");
    }

    public static Runnable runWk_version = new Runnable() {
        public void run() {

            try {

                //第一步：得到HttpClient对象，代表一个Http客户端
                HttpClient getVersionClient = new DefaultHttpClient();
                //第二步：得到HttpGet对象，代表请求的具体内容
                String apkUrl;
                if (startSerSharePre.getBoolean(SettingUtil.DEV_UPDATE_KEY, false)) {
                    apkUrl = apkDevUrl;
                    Log.i(TAG, "Dev Version");
                } else {
                    apkUrl = apkMasterUrl;
                }
                HttpGet request = new HttpGet(apkUrl);
                //第三步:执行请求。使用HttpClient的execute方法，执行刚才构建的请求
                HttpResponse response = getVersionClient.execute(request);
                //判断请求是否成功
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    Log.i(TAG, "请求服务器端成功 " + apkUrl);
                    //获得输入流
                    //第四步: 获取HttpResponse中的数据
                    InputStream inStream = response.getEntity().getContent();
                    String result = inputStreamToString(inStream).toString();
//                    Log.i(TAG, "请求内容为 " + result);
                    Pattern patternCode = Pattern.compile("versionCode (.*?) ");
                    Pattern patternName = Pattern.compile("versionName '(.*?)'");
                    Matcher matcherName = patternName.matcher(result);
                    Matcher matcherCode = patternCode.matcher(result);
                    try {
                        if (matcherCode.find() && matcherName.find()) {
                            serverVersion = Integer.parseInt(removeStr(removeStr(matcherCode.group(0), "versionCode"), " "));
                            serverVersionName = removeStr(removeStr(matcherName.group(0), "versionName"), "'");
                            Log.i(TAG, "Github Version " + serverVersion + " --- " + serverVersionName);

                            if (localVersion < serverVersion) {
                                isUpdate = true;
                                Log.i(TAG, " Update Release");
                            } else {
                                isBetaUpdate = Double.parseDouble(localVersionName) < Double.parseDouble(serverVersionName);
                                if (isBetaUpdate) {
                                    Log.i(TAG, " Beta Release");
                                }
                                isUpdate = false;
                            }

                        } else {
                            Log.i(TAG, "匹配失败");
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "匹配失败 " + e);
                        e.printStackTrace();
                    }

                    //关闭输入流
                    inStream.close();
                } else {
                    Log.i(TAG, "请求服务器Version失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                HttpClient getLogClient = new DefaultHttpClient();
                String logUrl;
                if (startSerSharePre.getBoolean(SettingUtil.DEV_UPDATE_KEY, false)) {
                    logUrl = logDevUrl;
                }else {
                    logUrl = logMasterUrl;
                }

                HttpGet request = new HttpGet(logUrl);
                HttpResponse response = getLogClient.execute(request);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    Log.i(TAG, "请求服务器端成功 " + logUrl);
                    InputStream inStream = response.getEntity().getContent();
                    String result = inputStreamToString(inStream).toString();
                    Pattern patternLog = Pattern.compile(serverVersionName + "[\\s\\S]*?```([\\s\\S]*?)```");
                    Matcher matcherLog = patternLog.matcher(result);
                    try {
                        if (matcherLog.find()) {
                            updateLog = matcherLog.group(0);
                            String[] logArray = updateLog.split("```");
                            updateLog = logArray[1];
                        } else {
                            Log.i(TAG, "匹配失败");
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "匹配失败 " + e);
                        e.printStackTrace();
                    }

                    //关闭输入流
                    inStream.close();
                } else {
                    Log.i(TAG, "请求服务器端Log失败");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };


}



