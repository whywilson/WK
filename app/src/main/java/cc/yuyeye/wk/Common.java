package cc.yuyeye.wk;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.support.v4.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import cc.yuyeye.wk.*;
import cc.yuyeye.wk.Util.*;
import cn.jpush.android.api.*;
import com.afollestad.materialdialogs.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.json.*;

import cc.yuyeye.wk.R;


public class Common extends Application
{
	public static String wk_url = "https://api.yuyeye.cc/wk_url.php";
	public static String url_domain;
	public static String url_login;
	public static String url_version;
	public static String url_apk;
	public static int new_version_code;
	public static String new_version_name;
	public static String new_version_log;
    public static Boolean isUpdate = false;
    public static Boolean isBetaUpdate = false;
    public static String TAG = "WkApplication";
    public static Context context;
    public static BasicPushNotificationBuilder soundBuilder;
    public static String phoneAlias;
    public static SharedPreferences startSerSharePre;

    private CustomPushNotificationBuilder vibrateBuilder;
    private WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

    public WindowManager.LayoutParams getWkwmParams()
	{
        return wmParams;
    }


    @Override
    public void onCreate()
	{
        super.onCreate();
        context = getApplicationContext();

        startSerSharePre = PreferenceManager.getDefaultSharedPreferences(this);
        startJPush();
    }

    public static Context getContext()
	{
        return context;
    }

    public void startJPush()
	{
        phoneAlias = startSerSharePre.getString(SettingUtil.ID_KEY, "");
        if (phoneAlias != "")
		{
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
					public void gotResult(int i, String s, Set<String> set)
					{
						Log.d("JPush", "Set Tag result is " + s);
					}
				});
        }
    }

    public static StringBuilder inputStreamToString(InputStream is)
	{
        String line;
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        // Read response until the end
        try
		{
            while ((line = rd.readLine()) != null)
			{
                total.append(line);
            }
        }
		catch (IOException e)
		{
            e.printStackTrace();
        }

        // Return full string
        return total;
    }

    public static String removeStr(String a, String b)
	{
        return a.replaceAll(b, "");
    }

	public static class url extends AsyncTask<Integer, Integer, String>
	{

        public String result;
        public InputStream is;
        public ArrayList<NameValuePair> nameValuePairs;

        @Override
        protected void onPreExecute()
		{
            result = "";

            nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("imei", MainActivity.IMEI));

            is = null;
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer[] params)
		{
            try
			{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(wk_url);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            }
			catch (Exception e)
			{
                Log.e(TAG, "url httpClient" + e.toString());
            }

            try
			{
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf8"), 8);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
				{
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
            }
			catch (Exception e)
			{
                Log.e(TAG, "url reader" + e.toString());
            }

            try
			{
                JSONArray jArray = new JSONArray(result);
                JSONObject jsonObj = jArray.getJSONObject(0);
				url_domain = jsonObj.getString("domain");
                url_login = url_domain + jsonObj.getString("login");
				url_version = url_domain + jsonObj.getString("version");
				new_version_code = jsonObj.getInt("code");
                new_version_name = jsonObj.getString("name");
            }
			catch (JSONException e)
			{
                Log.e(TAG, "url transfer " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result)
		{
			if ((new_version_code > getVersionCode())
				| (Integer.parseInt(new_version_name) > Integer.parseInt(getVersionName()) 
				&& startSerSharePre.getBoolean(SettingUtil.DEV_UPDATE_KEY, false)))
			{
				new version(MainActivity.mContext).execute();
			}

            new login().execute();

            super.onPostExecute(result);
        }
    }

	public static class login extends AsyncTask<Integer, Integer, String>
	{

        public String result;
        public InputStream is;
        public ArrayList<NameValuePair> nameValuePairs;
		public int user_id = 0;

        @Override
        protected void onPreExecute()
		{
            result = "";

            is = null;
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer[] params)
		{
            try
			{
				String nettype = InternetUtil.getNetworkState(Common.getContext()) + "";
				String localIp = InternetUtil.getLocalIp(Common.getContext()) + "";
				String netIp = InternetUtil.getNetIp() + "";
				nameValuePairs = new ArrayList<>();
				nameValuePairs.add(new BasicNameValuePair("imei", MainActivity.IMEI));
				nameValuePairs.add(new BasicNameValuePair("alias", phoneAlias));
				nameValuePairs.add(new BasicNameValuePair("version", Integer.toString(getVersionCode())));
				nameValuePairs.add(new BasicNameValuePair("vname", getVersionName()));
				nameValuePairs.add(new BasicNameValuePair("nettype", nettype));
				nameValuePairs.add(new BasicNameValuePair("local_ip", localIp));
				nameValuePairs.add(new BasicNameValuePair("ip", netIp));


                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url_login);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            }
			catch (Exception e)
			{
                Log.e(TAG, "login httpClient" + e.toString());
            }

            try
			{
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf8"), 8);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
				{
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
            }
			catch (Exception e)
			{
                Log.e(TAG, "login reader" + e.toString());
            }

            try
			{
                JSONArray jArray = new JSONArray(result);
                JSONObject jsonObj = jArray.getJSONObject(0);

                user_id = jsonObj.getInt("id");

            }
			catch (JSONException e)
			{
                Log.e(TAG, "login transfer " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result)
		{
            super.onPostExecute(result);
        }
    }

	public static class version extends AsyncTask<Integer, Integer, String>
	{
		public Context context;
		public MaterialDialog check_version_dialog;
        public String result;
        public InputStream is;
        public ArrayList<NameValuePair> nameValuePairs;
		public int user_id = 0;

		public version(Context context)
		{
			super();
			this.context = context;
		}

        @Override
        protected void onPreExecute()
		{
            result = "";
			check_version_dialog = new MaterialDialog.Builder(context)
				.title(R.string.checking)
				.progress(true, 1)
				.content(R.string.keep_online)
				.build();
            check_version_dialog.show();
            is = null;
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer[] params)
		{
            try
			{
				nameValuePairs = new ArrayList<>();
				nameValuePairs.add(new BasicNameValuePair("imei", MainActivity.IMEI));

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url_version);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            }
			catch (Exception e)
			{
                Log.e(TAG, "version httpClient" + e.toString());
            }

            try
			{
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf8"), 8);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
				{
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
            }
			catch (Exception e)
			{
                Log.e(TAG, "version reader" + e.toString());
            }

            try
			{
                JSONArray jArray = new JSONArray(result);
                JSONObject jsonObj = jArray.getJSONObject(0);

                new_version_code = jsonObj.getInt("version");
				new_version_name = jsonObj.getString("vname");
				new_version_log = jsonObj.getString("log");
				url_apk = jsonObj.getString("apk");
            }
			catch (JSONException e)
			{
                Log.e(TAG, "version transfer " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result)
		{
			MaterialDialog update_log_dialog = new MaterialDialog.Builder(context)
				.title(R.string.found_new_beta_version)
				.content(new_version_log)
				.positiveColorRes(R.color.colorPrimary)
				.negativeColorRes(R.color.colorAccent)
				.positiveText(R.string.update)
				.negativeText(R.string.cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback(){

					@Override
					public void onClick(MaterialDialog p1, DialogAction p2)
					{
						new update(context).execute();
					}
				})
				.onNegative(new MaterialDialog.SingleButtonCallback(){

					@Override
					public void onClick(MaterialDialog p1, DialogAction p2)
					{
						p1.dismiss();
					}
				})
				.build();
			if (new_version_code > getVersionCode())
			{
				isUpdate = true;
				update_log_dialog.setTitle(R.string.found_new_version);
				update_log_dialog.show();
			}
			else if (startSerSharePre.getBoolean(SettingUtil.DEV_UPDATE_KEY, false))
			{
				if (Integer.parseInt(new_version_name) > Integer.parseInt(getVersionName()))
				{
					isBetaUpdate = true;
					update_log_dialog.setTitle(R.string.found_new_beta_version);
					update_log_dialog.show();

				}
			}
			else
			{
				Toast.makeText(context, R.string.aleady_latest_version, Toast.LENGTH_LONG).show();
			}
			check_version_dialog.dismiss();
            super.onPostExecute(result);
        }
    }

	public static class update extends AsyncTask<Integer, Integer, Integer>
	{
		public Context context;
		public MaterialDialog download_pkg_dialog;
        public ArrayList<NameValuePair> nameValuePairs;

		public static final int TIMEOUT = 10 * 1000;// 超时
		public static String down_url = Common.url_apk;
		public static final int DOWN_OK = 1;
		public static final int DOWN_ERROR = 0;
		public static int down_status;
		public Intent updateIntent;
		public PendingIntent pendingIntent;

		public update(Context context)
		{
			super();
			this.context = context;
		}

        @Override
        protected void onPreExecute()
		{
			download_pkg_dialog = new MaterialDialog.Builder(context)
				.title(R.string.downloading)
				.progress(false, 100, true)
				.content(R.string.keep_online)
				.cancelable(false)
				.negativeText(R.string.cancel)
				.onNegative(new MaterialDialog.SingleButtonCallback(){

					@Override
					public void onClick(MaterialDialog p1, DialogAction p2)
					{
						p1.dismiss();
					}
				})
				.build();
            download_pkg_dialog.show();
			FileUtil.createFile("WK_" + new_version_name);
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer[] params)
		{
			try
			{
				//down_status = (int)downloadUpdateFile(down_url, FileUtil.updateFile.toString());

				int down_step = 1;// 提示step
				int totalSize;// 文件总大小
				int downloadCount = 0;// 已经下载好的大小
				int updateCount = 0;// 已经上传的文件大小
				InputStream inputStream;
				OutputStream outputStream;

				URL url = new URL(url_apk);
				HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
				httpURLConnection.setConnectTimeout(TIMEOUT);
				httpURLConnection.setReadTimeout(TIMEOUT);
				// 获取下载文件的size
				totalSize = httpURLConnection.getContentLength();
				if (httpURLConnection.getResponseCode() == 404)
				{
					throw new Exception("fail!");
				}
				inputStream = httpURLConnection.getInputStream();
				outputStream = new FileOutputStream(FileUtil.updateFile.toString(), false);// 文件存在则覆盖掉
				byte buffer[] = new byte[1024];
				int readsize = 0;
				while ((readsize = inputStream.read(buffer)) != -1)
				{
					outputStream.write(buffer, 0, readsize);
					downloadCount += readsize;// 时时获取下载到的大小

					if (updateCount == 0
						|| (downloadCount * 100 / totalSize - down_step) >= updateCount)
					{
						updateCount += down_step;
						download_pkg_dialog.incrementProgress(down_step);
					}

				}
				if (httpURLConnection != null)
				{
					httpURLConnection.disconnect();
				}
				inputStream.close();
				outputStream.close();

				down_status = downloadCount;
			}
			catch (Exception e)
			{
				Log.e(TAG, "update download " + e.toString());
				e.printStackTrace();
			}

            return down_status;
        }

        @Override
        protected void onPostExecute(Integer result)
		{
			if (down_status > 0)
			{
				installApk(FileUtil.updateFile);
//				Uri uri = Uri.fromFile(FileUtil.updateFile);
//				Intent intent = new Intent(Intent.ACTION_VIEW);
//				intent.setDataAndType(uri,"application/vnd.android.package-archive");
//				context.startActivity(intent);
			}
			else
			{
				ToastUtil.showToast("下载失败");
			}
			download_pkg_dialog.dismiss();
            super.onPostExecute(result);
        }

		protected void installApk(File file)
		{
			try
			{
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
				{ // 7.0+以上版本
					Uri apkUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);  //包名.fileprovider
					intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
				}
				else
				{
					intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
				}
				context.startActivity(intent);
			}
			catch (Exception e)
			{
				Log.e(TAG, "install apk " + e.toString());
			}

		}
    }

	public static int getVersionCode()
	{
        PackageManager packageManager = context.getPackageManager();
        try
		{
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int versionCode = packageInfo.versionCode;
            return versionCode;
        }
		catch (PackageManager.NameNotFoundException e)
		{
            e.printStackTrace();
        }

        return -1;
    }
	public static String getVersionName()
	{
        PackageManager packageManager = context.getPackageManager();
        try
		{
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            String versionName = packageInfo.versionName;
            return versionName;
        }
		catch (PackageManager.NameNotFoundException e)
		{
            e.printStackTrace();
        }

        return "0";
    }

}



