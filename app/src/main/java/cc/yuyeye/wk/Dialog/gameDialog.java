package cc.yuyeye.wk.Dialog;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.webkit.*;
import android.widget.*;
import cc.yuyeye.wk.*;
import cc.yuyeye.wk.Util.*;
import cc.yuyeye.wk.Fragment.*;

public class gameDialog extends Dialog {

	public int back_count = 0;
	public Context context;

	private long boo;

	public gameDialog(Context context) {
		super(context);
	}

	public gameDialog(Context context, int themeResId) {
        super(context, themeResId);
	}

	protected gameDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LayoutInflater li = getLayoutInflater();
		final View view = li.inflate(R.layout.dialog_webview, null);
		setContentView(view);
		super.onCreate(savedInstanceState);

		Button bt_share = (Button) view.findViewById(R.id.bt_share);
		final WebView webView = (WebView) view.findViewById(R.id.webview);
		final ProgressBar loading = (ProgressBar) view.findViewById(R.id.load_progress);
		webView.setBackgroundColor(0);
		WebSettings websettings = webView.getSettings();
		websettings.setJavaScriptEnabled(true);
		websettings.setSupportZoom(true);
		websettings.setBuiltInZoomControls(false);
		webView.setDrawingCacheEnabled(true);
		webView.setWebViewClient(new WebViewClient(){

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {

					if (url.startsWith("http") | url.startsWith("https")) {
						super.shouldInterceptRequest(view, url);
					} else {
						Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						context.startActivity(in);
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
		bt_share.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1) {
					try {
						Bitmap bitmap = webviewToBitmap(webView);
						WkFragment.saveToSD(bitmap, Environment.getExternalStorageDirectory() + "/DCIM/Camera/", "wk.png");
						//Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, null,null));
//						uri = Uri.fromFile();
//						Intent intent = new Intent();
//						intent.setAction(Intent.ACTION_SEND);//设置分享行为
//						intent.setType("image/*");//设置分享内容的类型
//						intent.putExtra(Intent.EXTRA_STREAM, uri);
//						intent = Intent.createChooser(intent, "分享");
//						context.startActivity(intent);
						//context.startActivity(Intent.createChooser(shareIntent, "Share to"));
					} catch (Exception e) {
						ToastUtil.showSimpleToast("Oooops " + e.toString());
						Log.e("t-rex",e.toString());
					}
				}
			});
		webView.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View p1) {
					
					return true;
				}
			});
	}

	@Override
	public void onBackPressed() {
		if (back_count > 1) {
			//this.dismiss();
		}
		back_count ++;
		super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - boo) > 2000) {
				ToastUtil.showToast("再按一次返回键退出T-Rex");
				boo = System.currentTimeMillis();
			} else {
				this.dismiss();
			}
		}
		return false;
	}
	
	public Bitmap webviewToBitmap(WebView webView){
		Picture pic = webView.capturePicture();
		Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        pic.draw(canvas);
        return bitmap;
	}
	
	public Bitmap viewToBitmap(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
		return bitmap;
	}

}
