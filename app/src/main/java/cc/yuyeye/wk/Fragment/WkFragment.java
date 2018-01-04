package cc.yuyeye.wk.Fragment;

import android.content.*;
import android.graphics.*;
import android.hardware.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.support.v4.app.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import cc.yuyeye.wk.*;
import cc.yuyeye.wk.Activity.*;
import cc.yuyeye.wk.Util.*;
import com.afollestad.materialdialogs.*;
import java.io.*;
import java.util.*;
import okhttp3.*;
import org.json.*;

import cc.yuyeye.wk.R;

import static cc.yuyeye.wk.MainActivity.isWkOrNot;
import static cc.yuyeye.wk.MainActivity.phoneAlias;

public class WkFragment extends Fragment implements SensorEventListener {

    private LinearLayout mainLayout;
    private LinearLayout wkImageView;
    private int Reset;
    private int TuringCode;
    private String jokeContent;
    private Sensor mSensor;
    private SensorManager mSensorManager;

	private ImageView sunRotate;

	private ImageView wImageView;

	private ImageView kImageView;

	private ImageView smileCloud;

	private Animation rotateAnim;

	private LinearInterpolator lin;

	private SensorEventListener mListener;

    public static WkFragment newInstance() {
        return new WkFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Reset = 0;
        // 获取传感器管理对象
        mSensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);
        // 获取传感器的类型(TYPE_ACCELEROMETER:加速度传感器)
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mListener = this;
		super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_main, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 为加速度传感器注册监听器
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

	@Override
	public void onPause() {
		mSensorManager.unregisterListener(this);
		super.onPause();
	}

    @Override
    public void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        sunRotate = (ImageView) getActivity().findViewById(R.id.sun_0);
        smileCloud = (ImageView) getActivity().findViewById(R.id.smileCloud);
        rotateAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        lin = new LinearInterpolator();
        rotateAnim.setInterpolator(lin);
		sunRotate.startAnimation(rotateAnim);
        mainLayout = (LinearLayout) getActivity().findViewById(R.id.setWallpaper);
        wkImageView = (LinearLayout) getActivity().findViewById(R.id.wkImage);
		wImageView = (ImageView) getActivity().findViewById(R.id.wImage);
		kImageView = (ImageView) getActivity().findViewById(R.id.kImage);
        if (isWkOrNot()) {
            wkImageView.setVisibility(View.VISIBLE);
            final Animation animFL = AnimationUtils.loadAnimation(Common.getContext(), R.anim.fade_left);
            final Animation animFR = AnimationUtils.loadAnimation(Common.getContext(), R.anim.fade_right);
            kImageView.startAnimation(animFR);
            wImageView.startAnimation(animFL);
        }
        if (InternetUtil.getNetworkState(getActivity()) == 0) {
            smileCloud.setImageResource(R.drawable.unhappy_0);
        }

        smileCloud.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View p1) {
					try {
						if (Common.isUpdate) {
							new Common.version(getActivity()).execute();
						} else {
							try {
								Intent intent = new Intent(getActivity(), CanteenIntroActivity.class);
								startActivity(intent);
							} catch (Exception e) {
								LogUtil.e("canteen", "start canteen error " + e.toString());
							}

						}

					} catch (Exception e) {
						Log.i("checkUpdate", "checkVersion错误" + e);
					}
				}


			});

        mainLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (InternetUtil.getNetworkState(getActivity()) != 0) {
						if (Reset > 0) {
							jokeTask dTask = new jokeTask();
							dTask.execute();
						} else {
							startMacWindow(1, getString(R.string.longPressToSetWallpaper) + "\n" + getString(R.string.getJokes));
							Reset++;
						}
					} else {
						startMacWindow(1, getString(R.string.longPressToSetWallpaper));
					}
				}
			});
        mainLayout.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View p1) {
					mSensorManager.unregisterListener(mListener);
					
					final View customView;
					try {
						customView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_webview, null);
					} catch (InflateException e) {
						throw new IllegalStateException("This device does not support Web Views.");
					}
					MaterialDialog dialog =
						new MaterialDialog.Builder(getActivity())
						.customView(customView, false)
						.cancelable(false)
						.autoDismiss(false)
						.titleGravity(GravityEnum.CENTER)
						//.titleColorRes(R.color.colorPrimary)
						.title("短爪快跑")
						.positiveText("嘚瑟一下")
						.positiveColor(Color.BLACK)
						.neutralText("关闭")
						.onNeutral(new MaterialDialog.SingleButtonCallback(){

							@Override
							public void onClick(MaterialDialog p1, DialogAction p2) {
								p1.dismiss();
							}
						})
						.onPositive(new MaterialDialog.SingleButtonCallback(){

							@Override
							public void onClick(MaterialDialog p1, DialogAction p2) {
								ToastUtil.showToast("有1000分了吗(⑉･̆-･̆⑉)");
//								Bitmap bitmap = ScreenShot();
//								Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, null, null));
//								Intent shareIntent = new Intent(Intent.ACTION_SEND);
//								shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//								shareIntent.setType("image/jpeg");
//								shareIntent.putExtra(Intent.EXTRA_SUBJECT, "短爪快跑");
//								//shareIntent.putExtra(Intent.EXTRA_TEXT, text);
//								shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//								getActivity().startActivity(Intent.createChooser(shareIntent, getActivity().getTitle()));
							}
						})
						.build();
						
					dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){

							@Override
							public void onDismiss(DialogInterface p1) {
								mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
							}
					});
					final WebView webView = (WebView) customView.findViewById(R.id.webview);
					final ProgressBar loading = (ProgressBar) customView.findViewById(R.id.load_progress);
					WebSettings websettings = webView.getSettings();
					websettings.setJavaScriptEnabled(true);		
					websettings.setSupportZoom(true);
					websettings.setBuiltInZoomControls(false);
					webView.setWebViewClient(new WebViewClient(){

							@Override
							public boolean shouldOverrideUrlLoading(WebView view, String url) {

								if (url.startsWith("http") | url.startsWith("https")) {
									super.shouldInterceptRequest(view, url);
								} else {
									Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
									startActivity(in);
									return true;
								}

								return false;
							}

							@Override
							public void onPageFinished(WebView view, String url) {
								loading.setVisibility(View.INVISIBLE);
								webView.setVisibility(View.VISIBLE);
								super.onPageFinished(view, url);
							}

						});
					String t_rex_url;
					if (Common.getSharedPreference().getBoolean("https_switch_key", true)) {
						t_rex_url = "https://yuyeye.cc/t-rex";
					} else {
						t_rex_url = "http://yuyeye.cc/t-rex";
					}
					webView.loadUrl(t_rex_url);

					dialog.show();
					//new Common.update(getActivity()).execute();
					//	screenShotToWallpaper(false);
					return true;
				}
			});
        sunRotate.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View p1) {
					if (isWkOrNot()) {
						startMacWindow(0, "");
					} else {
						startMacWindow(1, getString(R.string.longPressToSetting));
					}
				}
			});

        sunRotate.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View p1) {
					startActivity(new Intent(getActivity(), SettingActivity.class));
					return true;
				}
			});

        super.onActivityCreated(savedInstanceState);
    }

    public void startMacWindow(int Func, String MacContent) {
        Intent i = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("Func", Func);
        if (!Objects.equals(MacContent, "")) {
            bundle.putString("MacContent", MacContent);
        }

        i.putExtras(bundle);
        i.setClass(getContext(), LoveCounter.class);
        startActivity(i);
    }

	public Bitmap viewToBitmap(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap=view.getDrawingCache();
		return bitmap;
	}

	private Bitmap screenShotView(View view) {
		Bitmap temBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		return temBitmap;
	}
    public Bitmap ScreenShot() {
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

    private void saveToSD(Bitmap bmp, String dirName, String fileName) throws IOException {
        // 判断sd卡是否存在
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File dir = new File(dirName);
            // 判断文件夹是否存在，不存在则创建
            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(dirName + fileName);
            // 判断文件是否存在，不存在则创建
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file);
                // 第一参数是图片格式，第二个是图片质量，第三个是输出流
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                // 用完关闭
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void screenShotToWallpaper(Boolean b) {
        try {

            Bitmap bitmap = ScreenShot();
            if (b) {
                saveToSD(bitmap, Environment.getExternalStorageDirectory() + "/DCIM/Camera/", "wk.png");
            }

            wkSetWallpaper(bitmap);
            ToastUtil.showToast(getActivity(), getString(R.string.setWallpaperDone));
        } catch (IOException e) {
            ToastUtil.showToast(getString(R.string.setWallpaperFailed));
            e.printStackTrace();
        }
    }

    public static void wkSetWallpaper(final Bitmap bitmap) {
        Runnable run_SetWallpaper = new Runnable() {
            @Override
            public void run() {
                try {
                    Common.context.clearWallpaper();
                    Common.context.setWallpaper(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        run_SetWallpaper.run();
		// MainActivity.this.finish();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//		float cAlpha =(float)((Math.abs(sensorEvent.values[2]) - Math.abs(sensorEvent.values[0]) / 5) / 9.8);
		float cAlpha =(float)((Math.abs(sensorEvent.values[2]) + 2) / 9.8);
		if (sensorEvent.values[2] < 0) {
			if (sunRotate.getAnimation() != null) {
				sunRotate.clearAnimation();
			}

			sunRotate.setVisibility(View.INVISIBLE);
			smileCloud.setVisibility(View.INVISIBLE);
			mainLayout.setBackgroundResource(R.drawable.wk_bg_2);
			kImageView.setImageResource(R.drawable.karina_2);
			wImageView.setImageResource(R.drawable.wilson_2);
		} else {
			if (sunRotate.getAnimation() == null) {
				sunRotate.startAnimation(rotateAnim);
			}

			sunRotate.setVisibility(View.VISIBLE);
			smileCloud.setVisibility(View.VISIBLE);
			mainLayout.setBackgroundResource(R.drawable.wk_bg_1);
			kImageView.setImageResource(R.drawable.karina_1);
			wImageView.setImageResource(R.drawable.wilson_1);
		}
		sunRotate.setAlpha(cAlpha);
		smileCloud.setAlpha(cAlpha);
		mainLayout.setAlpha(cAlpha);
		kImageView.setAlpha(cAlpha);
		wImageView.setAlpha(cAlpha);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class jokeTask extends AsyncTask<Integer, Integer, String> {

        private String result;

        @Override
        protected void onPreExecute() {
            //第一个执行方法
            result = "";
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer[] params) {
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

            try {
                JSONObject jsonObj = new JSONObject(result);

                for (int i = 0; i < jsonObj.length(); i++) {
                    TuringCode = jsonObj.getInt("code");

                }
                if (TuringCode == 100000) {
                    jokeContent = jsonObj.getString("text");
                } else if (TuringCode == 200000) {
                    String turingUrl = jsonObj.getString("url");
                    jokeContent = jsonObj.getString("text") + "\n" + turingUrl;
                }
            } catch (JSONException e) {
                LogUtil.e("turing", "解析失败 " + e.toString());
            }

            return jokeContent;
        }

        @Override
        protected void onPostExecute(String result) {
            startMacWindow(1, result);
            super.onPostExecute(result);
        }

    }
}
