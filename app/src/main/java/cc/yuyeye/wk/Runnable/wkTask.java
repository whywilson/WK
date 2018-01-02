package cc.yuyeye.wk.Runnable;

import android.content.*;
import android.os.*;
import android.util.*;
import cc.yuyeye.wk.*;
import cc.yuyeye.wk.Util.*;
import okhttp3.*;

import static cc.yuyeye.wk.MainActivity.IMEI;
import static cc.yuyeye.wk.MainActivity.getCurrentTime;
import static cc.yuyeye.wk.Fragment.ChatFragment.msgDetail;
import static cc.yuyeye.wk.MainActivity.phoneAlias;
import static cc.yuyeye.wk.MainActivity.sendPerson;
import static org.apache.http.protocol.HTTP.UTF_8;

public class wkTask {
    public static String wk_notifyUrl = Common.url_domain + "wk_notify.php";
    public static String wk_messageUrl = Common.url_domain + "wk_message.php";
    public static String wk_loginUrl = Common.url_domain + "wk_login.php";
    public static String wk_reportUrl = Common.url_domain + "wk_report.php";
    public static String wk_timeUrl = Common.url_domain + "wk_time.php";
    public static String wk_msgAddUrl = Common.url_domain + "wk_msgAdd.php";
	public static String report = "";
    private static String currentTime;

	public static class msg_upload extends AsyncTask<Integer, Integer, Integer> {
		Context context;
		String alias;
		String title;
		String message;

		public msg_upload(Context context, String alias, String title, String message) {
			this.context = context;
			this.alias = alias;
			this.title = title;
			this.message = message;
		}


		@Override
		protected void onPreExecute() {
			// TODO: Implement this method
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Integer[] p1) {
			String LOG_TAG = "wk_msg_upload";
            try {
                if (!msgDetail.equals("")) {
                    RequestBody requestBody = new FormBody.Builder()
						.add("imei", IMEI)
						.add("alias", alias)
						.add("accept", title)
						.add("time", getCurrentTime())
						.add("msg", message)
						.build();
					try {
						//InputStream inputStream = context.getAssets().open("api.crt");
						OkHttpClient client = HttpsUtil.getTrustAllClient();
						Request request = new Request.Builder()
							.url(wk_msgAddUrl)
							.post(requestBody)
							.build();
						client.newCall(request).execute();
					} catch (Exception e) {
						Log.e(LOG_TAG, "upload " + e.toString());
					}
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
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
		}

	}

	public static class wk_message extends AsyncTask<Integer, Integer, Integer> {
		Context context;
		String alias;
		String title;
		String message;
		String report;

		public wk_message(Context context, String alias, String title, String message, String report) {
			this.context = context;
			this.alias = alias;
			this.title = title;
			this.message = message;
			this.report = report;
		}

		@Override
		protected void onPreExecute() {
			// TODO: Implement this method
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Integer[] p1) {
			String LOG_TAG = "wk_message";

            RequestBody requestBody = new FormBody.Builder()
				.add("alias", alias)
				.add("accept", title)
				.add("msg_content", message)
				.add("report", report)
				.build();
			try {
				//InputStream inputStream = context.getAssets().open("api.crt");
				OkHttpClient client = HttpsUtil.getTrustAllClient();
				Request request = new Request.Builder()
					.url(wk_messageUrl)
					.post(requestBody)
					.build();
				client.newCall(request).execute();
                report = "";
				return 1;
            } catch (Exception e) {
                Log.e(LOG_TAG, "联网错误 " + e.toString());
				return -1;
            }
		}

		@Override
		protected void onPostExecute(Integer result) {
			switch (result) {
				case -1:
					ToastUtil.showSimpleToast("发送失败");
					break;
				case 1:
					break;
			}
			super.onPostExecute(result);
		}

	}
	public static class wk_notify extends AsyncTask<Integer, Integer, Integer> {
		Context context;
		String alias;
		String title;
		String message;

		public wk_notify(Context context, String alias, String title, String message) {
			this.context = context;
			this.alias = alias;
			this.title = title;
			this.message = message;
		}

		@Override
		protected void onPreExecute() {
			// TODO: Implement this method
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Integer[] p1) {
			String LOG_TAG = "wk_notify";
            
            RequestBody requestBody = new FormBody.Builder()
				.add("alias", alias)
				.add("accept", title)
				.add("message", message)
				.build();
			try {
				//InputStream inputStream = context.getAssets().open("api.crt");
				OkHttpClient client = HttpsUtil.getTrustAllClient();
				Request request = new Request.Builder()
					.url(wk_notifyUrl)
					.post(requestBody)
					.build();
				client.newCall(request).execute();
				
				return 1;
            } catch (Exception e) {
                Log.e(LOG_TAG, "联网错误 " + e.toString());
				return -1;
            }
		}

		@Override
		protected void onPostExecute(Integer result) {
			switch (result) {
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
