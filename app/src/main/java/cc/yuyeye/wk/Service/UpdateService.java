package cc.yuyeye.wk.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import cc.yuyeye.wk.Util.FileUtil;
import cc.yuyeye.wk.Util.SettingUtil;
import cc.yuyeye.wk.MainActivity;
import cc.yuyeye.wk.R;
import cc.yuyeye.wk.Common;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class UpdateService extends Service {
    private static final int TIMEOUT = 10 * 1000;// 超时
    private static String down_url = "http://api.yuyeye.cc/WK-release.apk";
    private static final int DOWN_OK = 1;
    private static final int DOWN_ERROR = 0;
    private NotificationManager notificationManager;
    private Notification notification;

    private Intent updateIntent;
    private PendingIntent pendingIntent;

    private int notification_id = 0;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        FileUtil.createFile("WK");

        createNotification();

        createThread();

        return super.onStartCommand(intent, flags, startId);

    }

    /***
     * 开线程下载
     */
    public void createThread() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DOWN_OK:
                        // 下载完成，点击安装
                        Uri uri = Uri.fromFile(FileUtil.updateFile);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri,
                                "application/vnd.android.package-archive");

                        pendingIntent = PendingIntent.getActivity(
                                UpdateService.this, 0, intent, 0);
                        Notification.Builder builder = new Notification.Builder(UpdateService.this);
                        builder.setSmallIcon(R.drawable.ic_file_download_grey);
                        builder.setTicker("WK最新版下载完成");
                        builder.setContentTitle("下载完成"); //设置标题
                        builder.setContentText("点击安装更新"); //消息内容
                        builder.setWhen(System.currentTimeMillis()); //发送时间
                        builder.setDefaults(Notification.DEFAULT_ALL); //设置默认的提示音，振动方式，灯光
                        builder.setAutoCancel(true);//打开程序后图标消失

                        PendingIntent pendingIntent = PendingIntent.getActivity(UpdateService.this, 0, intent, 0);
                        builder.setFullScreenIntent(pendingIntent, true);
                        builder.setContentIntent(pendingIntent);
                        Notification notification1 = builder.build();

                        notificationManager.notify(notification_id, notification1);

                        stopService(updateIntent);
                        break;
                    case DOWN_ERROR:

                        Notification.Builder builderError = new Notification.Builder(UpdateService.this)
                                .setSmallIcon(R.drawable.ic_file_download_grey)
                                .setTicker("WK最新版下载失败")
                                .setContentTitle("下载失败")
                                .setContentText("请重新点击下载") //消息内容
                                .setWhen(System.currentTimeMillis()) //发送时间
                                .setDefaults(Notification.DEFAULT_ALL) //设置默认的提示音，振动方式，灯光
                                .setAutoCancel(true);//打开程序后图标消失

                        Notification notification2 = builderError.build();

                        notificationManager.notify(notification_id, notification2);
                        break;

                    default:
                        stopService(updateIntent);
                        break;
                }

            }

        };

        final Message message = new Message();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    down_url = PreferenceManager.getDefaultSharedPreferences(Common.getContext())
                            .getString(SettingUtil.UPDATE_SOURCE, down_url);

                    long downloadSize = downloadUpdateFile(down_url, FileUtil.updateFile.toString());
                    if (downloadSize > 0) {
                        // 下载成功
                        message.what = DOWN_OK;
                        handler.sendMessage(message);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    message.what = DOWN_ERROR;
                    handler.sendMessage(message);
                }

            }
        }).start();
    }

    /***
     * 创建通知栏
     */
    RemoteViews contentView;

    public void createNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new Notification();
        notification.icon = R.drawable.ic_file_download_grey;


        contentView = new RemoteViews(getPackageName(), R.layout.dload_notif_item);
        contentView.setTextViewText(R.id.notificationTitle, "正在下载");
        contentView.setTextViewText(R.id.notificationPercent, "0%");
        contentView.setProgressBar(R.id.notificationProgress, 100, 0, false);

        notification.contentView = contentView;

        updateIntent = new Intent(this, MainActivity.class);
        updateIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);

        notification.contentIntent = pendingIntent;

        notificationManager.notify(notification_id, notification);

    }

    /***
     * 下载文件
     *
     * @return
     * @throws MalformedURLException
     */
    public long downloadUpdateFile(String down_url, String file)
            throws Exception {
        int down_step = 5;// 提示step
        int totalSize;// 文件总大小
        int downloadCount = 0;// 已经下载好的大小
        int updateCount = 0;// 已经上传的文件大小
        InputStream inputStream;
        OutputStream outputStream;

        URL url = new URL(down_url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url
                .openConnection();
        httpURLConnection.setConnectTimeout(TIMEOUT);
        httpURLConnection.setReadTimeout(TIMEOUT);
        // 获取下载文件的size
        totalSize = httpURLConnection.getContentLength();
        if (httpURLConnection.getResponseCode() == 404) {
            throw new Exception("fail!");
        }
        inputStream = httpURLConnection.getInputStream();
        outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉
        byte buffer[] = new byte[1024];
        int readsize = 0;
        while ((readsize = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, readsize);
            downloadCount += readsize;// 时时获取下载到的大小
            /**
             * 每次增张5%
             */
            if (updateCount == 0
                    || (downloadCount * 100 / totalSize - down_step) >= updateCount) {
                updateCount += down_step;
                // 改变通知栏
                // notification.setLatestEventInfo(this, "正在下载...", updateCount
                // + "%" + "", pendingIntent);
                contentView.setTextViewText(R.id.notificationPercent,
                        updateCount + "%");
                contentView.setProgressBar(R.id.notificationProgress, 100,
                        updateCount, false);
                // show_view
                notificationManager.notify(notification_id, notification);

            }

        }
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }
        inputStream.close();
        outputStream.close();

        return downloadCount;

    }

}
