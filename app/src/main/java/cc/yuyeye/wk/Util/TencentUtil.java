package cc.yuyeye.wk.Util;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TencentUtil {

    public SharedPreferences sharedPreferences;
    public String qq;

    public TencentUtil(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public List<String> getQqList() {
        File[] files = new File(getQqPath()).listFiles();
        List<String> qq_list = new ArrayList<>();
        try {
            for (File file : files) {
                if (file.isDirectory()) {
                    String qqFolder = file.getName();
                    if (isNumeric(qqFolder)) {
                        qq_list.add(qqFolder);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e("SearchQQ", e.toString());
        }
        return qq_list;
    }

    public String getQqPath() {
        return Environment.getExternalStorageDirectory() + "/tencent/MobileQQ/" + getQqNumb();
    }

    public String getQqNumb() {
        qq = sharedPreferences.getString(SettingUtil.QQ_KEY, "10000");
        return qq;
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public String getQqIconUrl(String qq) {
        return "http://q.qlogo.cn/headimg_dl?dst_uin=" + qq + "&spec=100";
    }

    public void saveQq(String qq) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SettingUtil.QQ_KEY, qq);
        editor.apply();
        createDir(qq);
    }

    public static boolean createDir(String qq) {
        File dir = new File(Environment.getExternalStorageDirectory() + "/tencent/MobileQQ/" + qq);
        if (dir.exists()) {
            return false;
        }
        if (dir.mkdirs()) {
            return true;
        } else {
            return false;
        }
    }

}
