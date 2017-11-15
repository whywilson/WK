package cc.yuyeye.wk.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import cc.yuyeye.wk.Activity.LoveCounter;
import cc.yuyeye.wk.Common;
import cc.yuyeye.wk.DB.ChatDb;
import cc.yuyeye.wk.DB.chatListBean;
import cc.yuyeye.wk.Fragment.ChatFragment;
import cc.yuyeye.wk.MainActivity;
import cc.yuyeye.wk.R;
import cc.yuyeye.wk.Util.InternetUtil;
import cc.yuyeye.wk.Util.LogUtil;
import cc.yuyeye.wk.Util.SettingUtil;
import cc.yuyeye.wk.Util.TipViewController;
import cn.jpush.android.api.JPushInterface;

import static cc.yuyeye.wk.MainActivity.IMEI;
import static cc.yuyeye.wk.Runnable.wkRunnable.wk_reportUrl;

public class JPushReceiver extends BroadcastReceiver implements TipViewController.ViewDismissHandler {
    public static int notification_id;
    private String notifyExtra;

    @Override
    public void onViewDismiss() {
        sLastContent = null;
        mTipViewController = null;
    }

    public static final String TAG = "JPushReceiver";

    private Context context;
    private Bundle bundle;
    private String notifyTitle;
    private String notifyContent;
    private chatListBean chatListBean;
    private SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.PRC);
    public static String phoneAlias;

    private String currentTime;
    private MediaPlayer notifySound;

    private Intent i;
    private ChatDb chatDb;

    private String messageContent;
    private String messageTitle;

    private static CharSequence sLastContent = null;

    private TipViewController mTipViewController;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        SharedPreferences jpushSharePre = PreferenceManager.getDefaultSharedPreferences(context);
        phoneAlias = jpushSharePre.getString(SettingUtil.ID_KEY, "");

        bundle = intent.getExtras();

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            Log.d(TAG, "ACTION_REGISTRATION_ID.equals " + intent.getAction());
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "ACTION_MESSAG_RECEIVED");
            messageTitle = bundle.getString(JPushInterface.EXTRA_TITLE);
            messageContent = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            String report;

            if (!TextUtils.isEmpty(extras)) {
                try{
                    JSONObject extraJson = new JSONObject(extras);
                    if(extraJson.length() > 0){
                        report = extraJson.getString("report");
                        if(report.equals("now")){
                            new Thread(runWk_reportNow).start();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    if (messageTitle != null) {
                        SharedPreferences.Editor editor = jpushSharePre.edit();
                        editor.putString("sendPerson", messageTitle);
                        editor.apply();
                        MainActivity.sendPerson = messageTitle;
                        if (jpushSharePre.getBoolean(SettingUtil.SAVE_MSG_KEY, false)) {
                            addMsgToDb(context, messageTitle, messageContent);
                            addMsgToList(messageTitle, messageContent);
                        }
                    }
                    showPopWindowContent(messageContent);
                } catch (Exception e) {
                    LogUtil.e(TAG, "Tipview" + e);
                }
            }

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            notifyTitle = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
            notifyContent = bundle.getString(JPushInterface.EXTRA_ALERT);
            notifyExtra = bundle.getString(JPushInterface.EXTRA_EXTRA);
            Log.d(TAG, "ACTION_NOTIFICATION_RECEIVED");

            try {
                JSONObject extraJson = new JSONObject(notifyExtra);
                if (extraJson.length() > 0) {
                    notification_id = Integer.parseInt(extraJson.getString("builder_id"));
                    Log.i("notification_id", "notification_id  " + notification_id);
                    if (notification_id == 2) {
                        Intent i;
                        Bundle bundle = new Bundle();
                        bundle.putInt("Func", 1);
                        if (!Objects.equals(notifyContent, "")) {
                            bundle.putString("MacContent", notifyContent);
                        }
                        i = new Intent(context, LoveCounter.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtras(bundle);
                        context.startActivity(i);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                addMsgToDb(context, notifyTitle, notifyContent);
                addMsgToList(notifyTitle, notifyContent);
                notifySound = MediaPlayer.create(context, R.raw.receive_msg);
                notifySound.start();
            } catch (Exception e) {
                LogUtil.e(TAG, e);
            }

            SharedPreferences.Editor editor = jpushSharePre.edit();
            editor.putString("sendPerson", notifyTitle);
            editor.apply();

            MainActivity.receiveNotification = true;
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "ACTION_NOTIFICATION_OPENED");
            JPushInterface.reportNotificationOpened(context, bundle.getString(JPushInterface.EXTRA_MSG_ID));

            ChatFragment.msgTitle = notifyTitle;
            MainActivity.sendPerson = notifyTitle;
            ChatFragment.msgDetail = notifyContent;
            MainActivity.receiveNotification = true;

            Bundle bundle = new Bundle();
            bundle.putInt("msgType", 0);
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtras(bundle);
            context.startActivity(i);

        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[JPushReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

        } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            LogUtil.w(TAG, "[JPushReceiver]" + intent.getAction() + " connected state change to " + connected);
        } else {
            Log.d(TAG, "[JPushReceiver] Unhandled intent - " + intent.getAction());
        }

    }

    private void addMsgToDb(Context context, String title, String content) {
        currentTime = sDateFormat.format(new java.util.Date());
        chatDb = new ChatDb(context);
        chatDb.insert(phoneAlias, title, phoneAlias, currentTime, content);
    }

    private void addMsgToList(String title, String content) {
        chatListBean = new chatListBean();
        chatListBean.setMeSend(false);
        chatListBean.setIconUrl("http://bbs.tuling123.com/static/common/avatar-mid-img.png");
        chatListBean.setMsgContent(content);
        chatListBean.setcSend(title);

        //刷新ListView
        if (MainActivity.isForeground && chatListBean != null) {
            ChatFragment.chatAdaper.addItem(chatListBean);
            ChatFragment.chatMsgHandler.sendEmptyMessage(-1);
            LogUtil.i(TAG, "Is running background.");
        } else if (chatListBean == null) {
            LogUtil.i(TAG, "chatListBean is Null");
        } else {
            LogUtil.i(TAG, "Is NOT running. ");
        }
    }

    private void showPopWindowContent(CharSequence content) {
        if (sLastContent != null && sLastContent.equals(content) || content == null) {
            return;
        }
        sLastContent = content;

        if (mTipViewController != null) {
            mTipViewController.updateContent(content);
        } else {
            mTipViewController = new TipViewController(context, content);
            mTipViewController.setViewDismissHandler(this);
            mTipViewController.show();
        }
    }


    Runnable runWk_reportNow = new Runnable() {
        public void run() {
            String netType = InternetUtil.getNetworkState(Common.getContext()) + "";
            String localIp = InternetUtil.getLocalIp(Common.getContext()) + "";
            String netIp = InternetUtil.getNetIp()+"";

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair("IMEI", IMEI));
            nameValuePairs.add(new BasicNameValuePair("alias", phoneAlias));
            nameValuePairs.add(new BasicNameValuePair("nettype", netType));
            nameValuePairs.add(new BasicNameValuePair("ip", netIp));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(wk_reportUrl);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                entity.getContent();
            } catch (Exception e) {
                Log.e("log_tag", "联网错误 " + e.toString());
            }
        }
    };

}
