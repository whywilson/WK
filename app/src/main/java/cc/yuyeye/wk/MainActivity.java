package cc.yuyeye.wk;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cc.yuyeye.wk.Activity.LoginActivity;
import cc.yuyeye.wk.DB.ChatDb;
import cc.yuyeye.wk.Fragment.BingFragment;
import cc.yuyeye.wk.Fragment.ChatFragment;
import cc.yuyeye.wk.Fragment.WkFragment;
import cc.yuyeye.wk.Runnable.wkRunnable;
import cc.yuyeye.wk.Util.BitmapCache;
import cc.yuyeye.wk.Util.InternetUtil;
import cc.yuyeye.wk.Util.LogUtil;
import cc.yuyeye.wk.Util.SettingUtil;

import static cc.yuyeye.wk.Fragment.ChatFragment.mChatHeaderImage;
import static cc.yuyeye.wk.Fragment.ChatFragment.mChatMsgView;
import static cc.yuyeye.wk.Fragment.ChatFragment.mChatTitleTime;
import android.support.v4.app.*;

public class MainActivity extends AppCompatActivity {
    public static SharedPreferences sharedPreferences;
    public static String sendPerson = "所有人";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    public static ImageLoader.ImageCache mImageCache;
    public static ImageLoader mImageLoader;
    public static RequestQueue mQueue;
    public static ChatDb chatDb;
    public static SimpleDateFormat sDateFormat;
    public static String phoneAlias;
    public static String IMEI;
    public static Boolean receiveNotification = false;
    public static boolean isForeground = false;

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mQueue = Volley.newRequestQueue(this);
        mImageCache = new BitmapCache();
        mImageLoader = new ImageLoader(mQueue, mImageCache);
        sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.PRC);
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();
        checkPhoneAlias();
        checkSendPerson();
        if (phoneAlias != null) {
            chatDb = new ChatDb(this, true);
        }
        initImageLoader();
    }

    @Override
    protected void onResume() {
        if (InternetUtil.getNetworkState(this) != 0) {
            new Thread(runWk_login).start();
        }

		if(IMEI == "0"){
			ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},1);
		}
        isForeground = true;
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int p1, float p2, int p3) {

            }

            @Override
            public void onPageSelected(int p1) {
//					if(p1 != 1){
//						tabLayout.setVisibility(View.VISIBLE);
//					}
            }

            @Override
            public void onPageScrollStateChanged(int p1) {
//					if(p1 == 1){
//						tabLayout.setVisibility(View.VISIBLE);
//					}else{
//						tabLayout.setVisibility(View.INVISIBLE);
//					}
            }
        });
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        isForeground = false;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ChatFragment.newInstance();
                case 1:
                    return WkFragment.newInstance();
                case 2:
                    return BingFragment.newInstance();
                default:
                    return WkFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "问问";
                case 1:
                    return "W.K.";
                case 2:
                    return "看看";
            }
            return null;
        }
    }

    public static String getCurrentTime() {
        return sDateFormat.format(new Date());
    }

    public static void SaveChatRecord(String alias, String send, String receive, String msg) {
        String currentTime = getCurrentTime();
        chatDb.insert(alias, send, receive, currentTime, msg);
    }

    public void checkPhoneAlias() {
        phoneAlias = sharedPreferences.getString(SettingUtil.ID_KEY, "");
        if (phoneAlias.equals("")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public static void checkSendPerson() {
        sendPerson = sharedPreferences.getString("sendPerson", Common.getContext().getResources().getString(R.string.allContacts));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //View v = getCurrentFocus();
            View v = mChatMsgView;
            //消息编辑框与标题栏图标点击不失去焦点
            if (isShouldHideKeyboard(v, ev) && (isShouldHideKeyboard(mChatHeaderImage, ev) | isShouldHideKeyboard(mChatTitleTime, ev))) {
                hideKeyboard(v.getWindowToken());
                mViewPager.setFocusable(true);
                mViewPager.setFocusableInTouchMode(true);
                mViewPager.requestFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if ((v instanceof EditText) || v != null) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getRawX() > left && event.getRawX() < right
                    && event.getRawY() > top && event.getRawY() < bottom);
        }
        return false;
    }

    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public Runnable runWk_login = new Runnable() {
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
                HttpPost httppost = new HttpPost(wkRunnable.wk_loginUrl);
                httppost.setEntity(new UrlEncodedFormEntity((nameValuePairs), "UTF_8"));
                httpclient.execute(httppost);
            } catch (Exception e) {
                LogUtil.e("wkrunnable", "Login错误 " + e.toString());
            }
        }
    };

    private void initImageLoader() {
        mQueue = Volley.newRequestQueue(this);
        mImageCache = new BitmapCache();
        mImageLoader = new ImageLoader(mQueue, mImageCache);
    }
	
	public static boolean isWkOrNot() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Common.getContext());
        String wk = sp.getString(SettingUtil.ID_KEY, "");
        return wk.equals("Wilson") || wk.equals("wilson") || wk.equals("Karina") || wk.equals("karina");
    }
	
}
