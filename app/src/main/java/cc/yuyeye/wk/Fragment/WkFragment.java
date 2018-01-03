package cc.yuyeye.wk.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import cc.yuyeye.wk.Activity.CanteenIntroActivity;
import cc.yuyeye.wk.Activity.LoveCounter;
import cc.yuyeye.wk.Activity.SettingActivity;
import cc.yuyeye.wk.Common;
import cc.yuyeye.wk.R;
import cc.yuyeye.wk.Util.InternetUtil;
import cc.yuyeye.wk.Util.LogUtil;
import cc.yuyeye.wk.Util.ScreenUtil;
import cc.yuyeye.wk.Util.ToastUtil;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static cc.yuyeye.wk.MainActivity.isWkOrNot;
import static cc.yuyeye.wk.MainActivity.phoneAlias;

public class WkFragment extends Fragment implements SensorEventListener
{

    private LinearLayout mainLayout;
    private LinearLayout wkImageView;
    private int Reset;
    private int TuringCode;
    private String jokeContent;
    private Sensor mSensor;
    private SensorManager mSensorManager;

    public static WkFragment newInstance() {
        return new WkFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        Reset = 0;
        // 获取传感器管理对象
        mSensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);
        // 获取传感器的类型(TYPE_ACCELEROMETER:加速度传感器)
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        return inflater.inflate(R.layout.tab_main, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 为加速度传感器注册监听器
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
	{
        ImageView sunRotate = (ImageView) getActivity().findViewById(R.id.sun_0);
        ImageView smileCloud = (ImageView) getActivity().findViewById(R.id.smileCloud);
        Animation rotateAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        rotateAnim.setInterpolator(lin);
        sunRotate.startAnimation(rotateAnim);
        mainLayout = (LinearLayout) getActivity().findViewById(R.id.setWallpaper);
        wkImageView = (LinearLayout) getActivity().findViewById(R.id.wkImage);
        ImageView wImageView = (ImageView) getActivity().findViewById(R.id.wImage);
        ImageView kImageView = (ImageView) getActivity().findViewById(R.id.kImage);
        if (isWkOrNot())
		{
            wkImageView.setVisibility(View.VISIBLE);
            final Animation animFL = AnimationUtils.loadAnimation(Common.getContext(), R.anim.fade_left);
            final Animation animFR = AnimationUtils.loadAnimation(Common.getContext(), R.anim.fade_right);
            kImageView.startAnimation(animFR);
            wImageView.startAnimation(animFL);
        }
        if (InternetUtil.getNetworkState(getActivity()) == 0)
		{
            smileCloud.setImageResource(R.drawable.unhappy_0);
        }

        smileCloud.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					try
					{
						if (Common.isUpdate)
						{
							new Common.version(getActivity()).execute();
						}
						else
						{
							try
							{
								Intent intent = new Intent(getActivity(), CanteenIntroActivity.class);
								startActivity(intent);
							}
							catch (Exception e)
							{
								LogUtil.e("canteen", "start canteen error " + e.toString());
							}

						}

					}
					catch (Exception e)
					{
						Log.i("checkUpdate", "checkVersion错误" + e);
					}
				}


			});

        mainLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view)
				{
					if (InternetUtil.getNetworkState(getActivity()) != 0)
					{
						if (Reset > 0)
						{
							jokeTask dTask = new jokeTask();
							dTask.execute();
						}
						else
						{
							startMacWindow(1, getString(R.string.longPressToSetWallpaper) + "\n" + getString(R.string.getJokes));
							Reset++;
						}
					}
					else
					{
						startMacWindow(1, getString(R.string.longPressToSetWallpaper));
					}
				}
			});
        mainLayout.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View p1)
				{
					new Common.update(getActivity()).execute();
				//	screenShotToWallpaper(false);
					return true;
				}
			});
        sunRotate.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					if (isWkOrNot())
					{
						startMacWindow(0, "");
					}
					else
					{
						startMacWindow(1, getString(R.string.longPressToSetting));
					}
				}
			});

        sunRotate.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View p1)
				{
					startActivity(new Intent(getActivity(), SettingActivity.class));
					return true;
				}
			});

        super.onActivityCreated(savedInstanceState);
    }

    public void startMacWindow(int Func, String MacContent)
	{
        Intent i = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("Func", Func);
        if (!Objects.equals(MacContent, ""))
		{
            bundle.putString("MacContent", MacContent);
        }

        i.putExtras(bundle);
        i.setClass(getContext(), LoveCounter.class);
        startActivity(i);
    }

    public Bitmap ScreenShot()
	{
        // 获取windows中最顶层的view
        View view = getActivity().getWindow().getDecorView();
        view.buildDrawingCache();

        // 获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);

        // 获取屏幕宽和高
        int widths = ScreenUtil.getScreenWidth(getActivity());
        int heights = ScreenUtil.getScreenHeight(getActivity());

        // 允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);

        // 去掉状态栏
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
										 0, widths, heights);

        // 销毁缓存信息
        view.destroyDrawingCache();

        return bmp;
    }

    private void saveToSD(Bitmap bmp, String dirName, String fileName) throws IOException
	{
        // 判断sd卡是否存在
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED))
		{
            File dir = new File(dirName);
            // 判断文件夹是否存在，不存在则创建
            if (!dir.exists())
			{
                dir.mkdir();
            }

            File file = new File(dirName + fileName);
            // 判断文件是否存在，不存在则创建
            if (!file.exists())
			{
                file.createNewFile();
            }

            FileOutputStream fos;
            try
			{
                fos = new FileOutputStream(file);
                // 第一参数是图片格式，第二个是图片质量，第三个是输出流
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                // 用完关闭
                fos.flush();
                fos.close();
            }
			catch (IOException e)
			{
                e.printStackTrace();
            }
        }
    }

    public void screenShotToWallpaper(Boolean b)
	{
        try
		{

            Bitmap bitmap = ScreenShot();
            if (b)
			{
                saveToSD(bitmap, Environment.getExternalStorageDirectory() + "/DCIM/Camera/", "wk.png");
            }

            wkSetWallpaper(bitmap);
            ToastUtil.showToast(getActivity(), getString(R.string.setWallpaperDone));
        }
		catch (IOException e)
		{
            ToastUtil.showToast(getString(R.string.setWallpaperFailed));
            e.printStackTrace();
        }
    }

    public static void wkSetWallpaper(final Bitmap bitmap)
	{
        Runnable run_SetWallpaper = new Runnable() {
            @Override
            public void run()
			{
                try
				{
                    Common.context.clearWallpaper();
                    Common.context.setWallpaper(bitmap);
                }
				catch (IOException e)
				{
                    e.printStackTrace();
                }
            }
        };
        run_SetWallpaper.run();
		// MainActivity.this.finish();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class jokeTask extends AsyncTask<Integer, Integer, String>
	{

        private String result;
        
        @Override
        protected void onPreExecute()
		{
            //第一个执行方法
            result = "";
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer[] params)
		{
			RequestBody requestBody = new FormBody.Builder()
				.add("key", "0aad89b1e6b74ab5932fe584269ea531")
				.add("info", "笑话")
				.add("userid", phoneAlias)
				.build();
			try {
				Request request = new Request.Builder()
					.url("http://www.tuling123.com/openapi/api")
					.post(requestBody)
					.build();
				Response response = Common.mClient.newCall(request).execute();
				result = response.body().string();
			} catch (Exception e) {
				Log.e("turing", "url  " + e.toString());
			}
			
            try
			{
                JSONObject jsonObj = new JSONObject(result);

                for (int i = 0; i < jsonObj.length(); i++)
				{
                    TuringCode = jsonObj.getInt("code");

                }
                if (TuringCode == 100000)
				{
                    jokeContent = jsonObj.getString("text");
                }
				else if (TuringCode == 200000)
				{
                    String turingUrl = jsonObj.getString("url");
                    jokeContent = jsonObj.getString("text") + "\n" + turingUrl;
                }
            }
			catch (JSONException e)
			{
                LogUtil.e("turing", "解析失败 " + e.toString());
            }

            return jokeContent;
        }

        @Override
        protected void onPostExecute(String result)
		{
            startMacWindow(1, result);
            super.onPostExecute(result);
        }

    }
}
