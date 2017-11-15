package cc.yuyeye.wk.Util;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cc.yuyeye.wk.Common;
import cc.yuyeye.wk.R;


public class ToastUtil {
    private static Toast toast;
    private static CountDownTimer cdt;
    private static int duration = 200;

    public static void showToast(String content) {
        showToast(Common.getContext(), content);
    }

    public static void showToast(Context context, String content) {
        if (toast == null) {
            toast = new Toast(context);
        }
        View view = View.inflate(context, R.layout.toast, null);
        toast.setView(view);
        TextView tv_content = (TextView) view.findViewById(R.id.toastTextView);
        tv_content.setText(content);
        toast.setGravity(Gravity.BOTTOM, 0, ScreenUtil.getStatusHeight(context) * 3);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showSimpleToast(String content) {
        showSimpleToast(Common.getContext(), content, duration);
    }

    public static void showSimpleToast(String content, int duration) {
        showSimpleToast(Common.getContext(), content, duration);
    }

    public static void showSimpleToast(Context context, String content, int duration) {
        if (toast == null) {
            toast = new Toast(context);
        }

        View view = View.inflate(context, R.layout.toast_simple, null);
        toast.setView(view);
        TextView tv_content = (TextView) view.findViewById(R.id.toastTextView);
        tv_content.setText(content);
        toast.setGravity(Gravity.TOP, 0, ScreenUtil.getStatusHeight(context) * 3);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
        if (cdt == null) {
            cdt = new CountDownTimer(duration, duration) {

                @Override
                public void onTick(long arg0) {

                }

                @Override
                public void onFinish() {
                    toast.cancel();
                }
            };
        } else {
            cdt.cancel();
            cdt = new CountDownTimer(duration, duration) {

                @Override
                public void onTick(long arg0) {
                }

                @Override
                public void onFinish() {
                    toast.cancel();
                }
            };
        }
        cdt.start();
    }
}
