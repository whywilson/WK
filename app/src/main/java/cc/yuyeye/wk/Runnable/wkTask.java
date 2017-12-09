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
import android.os.*;
import android.content.*;

public class wkTask {
    public static String wk_notifyUrl = Common.url_domain + "wk_notify.php";
    public static String wk_messageUrl = Common.url_domain + "wk_message.php";
    public static String wk_loginUrl = Common.url_domain + "wk_login.php";
    public static String wk_reportUrl = Common.url_domain + "wk_report.php";
    public static String wk_timeUrl = Common.url_domain + "wk_time.php";
    public static String wk_msgAddUrl = Common.url_domain + "wk_msgAdd.php";
	public static String report = "";
    private static String currentTime;

	public static class msg_upload extends AsyncTask<Integer, Integer, Integer>
	{
		Context context;
		String alias;
		String title;
		String message;

		public msg_upload(Context context, String alias, String title, String message)
		{
			this.context = context;
			this.alias = alias;
			this.title = title;
			this.message = message;
		}


		@Override
		protected void onPreExecute()
		{
			// TODO: Implement this method
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Integer[] p1)
		{
			String LOG_TAG = "wk_msg_upload";
            String currentTime = getCurrentTime();
            Log.i(LOG_TAG, IMEI + " " + currentTime + " " + msgDetail);
            //confict to msg from wk
            //checkSendPersion();
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair("IMEI", IMEI));
            nameValuePairs.add(new BasicNameValuePair("alias", alias));
            nameValuePairs.add(new BasicNameValuePair("accept", title));
            nameValuePairs.add(new BasicNameValuePair("time", currentTime));
            nameValuePairs.add(new BasicNameValuePair("msg", message));

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
			return null;
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			super.onPostExecute(result);
		}

	}
    
	public static class wk_message extends AsyncTask<Integer, Integer, Integer>
	{
		Context context;
		String alias;
		String title;
		String message;
		String report;

		public wk_message(Context context, String alias, String title, String message, String report)
		{
			this.context = context;
			this.alias = alias;
			this.title = title;
			this.message = message;
			this.report = report;
		}

		@Override
		protected void onPreExecute()
		{
			// TODO: Implement this method
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Integer[] p1)
		{
			String LOG_TAG = "wk_message";

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair("alias", alias));
            nameValuePairs.add(new BasicNameValuePair("title", title));
            nameValuePairs.add(new BasicNameValuePair("msg_content", message));
            nameValuePairs.add(new BasicNameValuePair("report", report));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(wk_messageUrl);
                httppost.setEntity(new UrlEncodedFormEntity((nameValuePairs), UTF_8));
                httpclient.execute(httppost);
                report = "";
				return 1;
            } catch (Exception e) {
                Log.e(LOG_TAG, "联网错误 " + e.toString());
				return -1;
            }
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			switch (result)
			{
				case -1:
					ToastUtil.showSimpleToast("发送失败");
					break;
				case 1:
					break;
			}
			super.onPostExecute(result);
		}

	}
	public static class wk_notify extends AsyncTask<Integer, Integer, Integer>
	{
		Context context;
		String alias;
		String title;
		String message;

		public wk_notify(Context context, String alias, String title, String message)
		{
			this.context = context;
			this.alias = alias;
			this.title = title;
			this.message = message;
		}
		
		@Override
		protected void onPreExecute()
		{
			// TODO: Implement this method
			super.onPreExecute();
		}
		
		@Override
		protected Integer doInBackground(Integer[] p1)
		{
			String LOG_TAG = "wk_notify";
            currentTime = getCurrentTime();

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair("alias", alias));
            nameValuePairs.add(new BasicNameValuePair("title", title));
            nameValuePairs.add(new BasicNameValuePair("message", message));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(wk_notifyUrl);
                httppost.setEntity(new UrlEncodedFormEntity((nameValuePairs), UTF_8));
                httpclient.execute(httppost);
				return 1;
            } catch (Exception e) {
                Log.e(LOG_TAG, "联网错误 " + e.toString());
				return -1;
            }
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			switch (result)
			{
				case -1:
					ToastUtil.showSimpleToast("发送失败");
					break;
				case 1:
					break;
			}
			super.onPostExecute(result);
		}
		
	}
}
