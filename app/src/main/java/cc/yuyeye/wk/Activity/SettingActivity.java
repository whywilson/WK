package cc.yuyeye.wk.Activity;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.support.annotation.*;
import android.util.*;
import android.widget.*;
import cc.yuyeye.wk.*;
import cc.yuyeye.wk.Service.*;
import cc.yuyeye.wk.Util.*;
import cn.jpush.android.api.*;
import com.afollestad.materialdialogs.*;
import java.util.*;

import cc.yuyeye.wk.R;
import android.widget.MultiAutoCompleteTextView.*;

public class SettingActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private EditTextPreference mEtPreference_ID;
    private EditTextPreference mEtPreference_QQ;
    private Preference mAboutPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wk_preference);
        initPreferences();
    }

    private void initPreferences() {
        mEtPreference_ID = (EditTextPreference) findPreference(SettingUtil.ID_KEY);
        mEtPreference_QQ = (EditTextPreference) findPreference(SettingUtil.QQ_KEY);
        mAboutPreference = findPreference(SettingUtil.ABOUT_KEY);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Setup the initial values
        final SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        mEtPreference_ID.setSummary(sharedPreferences.getString(SettingUtil.ID_KEY, "Anonymous"));
        mEtPreference_QQ.setSummary(sharedPreferences.getString(SettingUtil.QQ_KEY, ""));
        if (mEtPreference_QQ.getSummary().equals("")) {
            mEtPreference_ID.setEnabled(true);
        }
        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        mAboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
				MaterialDialog.Builder about_dialog = new MaterialDialog.Builder(SettingActivity.this)
					.title(R.string.about)
					.content(R.string.about_app)
					.autoDismiss(false)
					.positiveText(R.string.official_website)
					.onPositive(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							String url =Common.url_domain;
							Uri webpage = Uri.parse(url);

							if (!url.startsWith("http://") && !url.startsWith("https://")) {
								webpage = Uri.parse("http://" + url);
							}

							Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
							if (intent.resolveActivity(MainActivity.mContext.getPackageManager()) != null) {
								MainActivity.mContext.startActivity(intent);
							}
							dialog.dismiss();
						}
					})
					.neutralText(R.string.check_update)
					.onNeutral(new MaterialDialog.SingleButtonCallback() {

						@Override
						public void onClick(MaterialDialog p1, DialogAction p2) {
							new Common.version(SettingActivity.this).execute();
							//	p1.dismiss();
						}
					});

                about_dialog.show();
				
                return true;
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case SettingUtil.QQ_KEY:
                mEtPreference_QQ.setSummary(sharedPreferences.getString(key, "null"));
                if (mEtPreference_QQ.getSummary().equals("")) {
                    mEtPreference_ID.setEnabled(true);
                } else {
                    mEtPreference_ID.setEnabled(false);
                }
                break;
            case SettingUtil.ID_KEY:
                mEtPreference_ID.setSummary(sharedPreferences.getString(key, "null"));
                JPushInterface.stopPush(Common.getContext());
                JPushInterface.setAlias(Common.getContext(), sharedPreferences.getString(key, "20"), new TagAliasCallback() {
                    @Override
                    public void gotResult(int i, String s, Set<String> set) {
                        Log.d("JPush", "Update Tag to " + s);
                    }
                });
                JPushInterface.resumePush(Common.getContext());
                break;
        }
    }
}

