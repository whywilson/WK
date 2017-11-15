package cc.yuyeye.wk.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import cc.yuyeye.wk.MainActivity;
import cc.yuyeye.wk.Util.LogUtil;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            LogUtil.d("JPush", "Boot Complete, start service");
			LogUtil.delFile();
            Intent newIntent = new Intent(context, MainActivity.class);
            context.startService(newIntent);
        }
    }
}
