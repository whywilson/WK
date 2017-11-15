package cc.yuyeye.wk.Util;

import android.os.Environment;


import java.io.File;
import java.io.IOException;

import cc.yuyeye.wk.Common;

public class FileUtil {
    public static File updateDir = null;
    public static File updateFile = null;

    public static void createFile(String name) {
        if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
                .getExternalStorageState())) {
            updateDir = new File(Environment.getExternalStorageDirectory()
                    + "/" + Common.downloadDir);
            updateFile = new File(updateDir + "/" + name + ".apk");

            if (!updateDir.exists()) {
                updateDir.mkdirs();
            }
            if (!updateFile.exists()) {
                try {
                    updateFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
