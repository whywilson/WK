package cc.yuyeye.wk.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import java.util.Set;

import cc.yuyeye.wk.Common;
import cc.yuyeye.wk.R;
import cc.yuyeye.wk.Service.UpdateService;
import cc.yuyeye.wk.Util.SettingUtil;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class SettingActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private EditTextPreference mEtPreference_ID;
    private EditTextPreference mEtPreference_QQ;
    private ListPreference mListPreference_UpdateSource;
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
        mListPreference_UpdateSource = (ListPreference) findPreference(SettingUtil.UPDATE_SOURCE);
        mAboutPreference = findPreference(SettingUtil.ABOUT_KEY);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Setup the initial values
        final SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        mEtPreference_ID.setSummary(sharedPreferences.getString(SettingUtil.ID_KEY, "Anonymous"));
        mEtPreference_QQ.setSummary(sharedPreferences.getString(SettingUtil.QQ_KEY, ""));
        mListPreference_UpdateSource.setSummary(mListPreference_UpdateSource.getEntry());
        if (mEtPreference_QQ.getSummary().equals("")) {
            mEtPreference_ID.setEnabled(true);
        }
        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        mAboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle(getResources().getString(R.string.about)+" "+ Common.localVersion +" "+Common.localVersionName);
                builder.setMessage(getString(R.string.about_app) + "\n" + Common.updateLog);
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface p1, int p2) {
                    }
                });
                builder.setNeutralButton(R.string.downloadAgain, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface p1, int p2) {
                        if (sharedPreferences.getBoolean(SettingUtil.DEV_UPDATE_KEY, false)) {
                            Intent updateIntent = new Intent(SettingActivity.this, UpdateService.class);
                            updateIntent.putExtra("app_name", getResources().getString(R.string.app_name));
                            startService(updateIntent);
                        }
                    }
                });
                builder.show();
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
            case SettingUtil.UPDATE_SOURCE:
                mListPreference_UpdateSource.setSummary(mListPreference_UpdateSource.getEntry());
                break;
        }
    }
}

