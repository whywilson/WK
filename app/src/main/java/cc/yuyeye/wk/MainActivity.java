package cc.yuyeye.wk;

import android.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.preference.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.support.v7.app.*;
import android.telephony.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import cc.yuyeye.wk.*;
import cc.yuyeye.wk.Activity.*;
import cc.yuyeye.wk.DB.*;
import cc.yuyeye.wk.Fragment.*;
import cc.yuyeye.wk.Util.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import java.text.*;
import java.util.*;

import android.Manifest;
import cc.yuyeye.wk.R;

import static cc.yuyeye.wk.Fragment.ChatFragment.mChatHeaderImage;
import static cc.yuyeye.wk.Fragment.ChatFragment.mChatMsgView;
import static cc.yuyeye.wk.Fragment.ChatFragment.mChatTitleTime;

public class MainActivity extends AppCompatActivity
{
	public static Context mContext;
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

	private int REQUEST_CODE_ASK_PHONE_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);

		mContext = this;
		
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_ASK_PHONE_STATE);
            return;
        }
		
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
        if (phoneAlias != null)
		{
            chatDb = new ChatDb(this, true);
        }
        initImageLoader();

		if (InternetUtil.getNetworkState(this.getBaseContext()) != 0)
		{
			new Common.url().execute();
        } 
	}

    @Override
    protected void onResume()
	{
        if (InternetUtil.getNetworkState(this) != 0)
		{
			new Common.login().execute();
        }

        isForeground = true;
        super.onResume();
    }

	@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.allow_phone_status_permission, Toast.LENGTH_LONG).show();
                }
                finish();
                startActivity(getIntent());
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    @Override
    protected void onPause()
	{
        super.onPause();
        isForeground = false;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter
	{

        public SectionsPagerAdapter(FragmentManager fm)
		{
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
		{
            switch (position)
			{
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
        public int getCount()
		{
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position)
		{
            switch (position)
			{
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

    public static String getCurrentTime()
	{
        return sDateFormat.format(new Date());
    }

    public static void SaveChatRecord(String alias, String send, String receive, String msg)
	{
        String currentTime = getCurrentTime();
        chatDb.insert(alias, send, receive, currentTime, msg);
    }

    public void checkPhoneAlias()
	{
        phoneAlias = Common.getSharedPreference().getString(SettingUtil.ID_KEY, "");
        if (phoneAlias.equals(""))
		{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public static void checkSendPerson()
	{
        sendPerson = Common.getSharedPreference().getString("sendPerson", Common.getContext().getResources().getString(R.string.allContacts));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
	{
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
		{
            //View v = getCurrentFocus();
            View v = mChatMsgView;
            //消息编辑框与标题栏图标点击不失去焦点
            if (isShouldHideKeyboard(v, ev) && (isShouldHideKeyboard(mChatHeaderImage, ev) | isShouldHideKeyboard(mChatTitleTime, ev)))
			{
                hideKeyboard(v.getWindowToken());
                mViewPager.setFocusable(true);
                mViewPager.setFocusableInTouchMode(true);
                mViewPager.requestFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isShouldHideKeyboard(View v, MotionEvent event)
	{
        if ((v instanceof EditText) || v != null)
		{
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

    private void hideKeyboard(IBinder token)
	{
        if (token != null)
		{
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void initImageLoader()
	{
        mQueue = Volley.newRequestQueue(this);
        mImageCache = new BitmapCache();
        mImageLoader = new ImageLoader(mQueue, mImageCache);
    }

	public static boolean isWkOrNot()
	{
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Common.getContext());
        String wk = sp.getString(SettingUtil.ID_KEY, "");
        return wk.equals("Wilson") || wk.equals("wilson") || wk.equals("Karina") || wk.equals("karina");
    }

}
