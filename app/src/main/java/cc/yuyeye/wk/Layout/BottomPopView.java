package cc.yuyeye.wk.Layout;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import cc.yuyeye.wk.R;
import cc.yuyeye.wk.Util.ScreenUtil;

public abstract class BottomPopView {
    private Context mContext;
    private View anchor;
    private LayoutInflater mInflater;
    private TextView mTvSetWallpaper;
    private TextView mTvSaveToSd;
    private TextView mTvCancel;
    private PopupWindow mPopupWindow;

    private android.view.WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private Window window;

    private TextView mTvShareTo;

    private TextView mTvClearAll;


    /**
     * @param context
     * @param anchor  依附在哪个View下面
     */
    public BottomPopView(Activity context, View anchor) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.anchor = anchor;
        windowManager = context.getWindowManager();
        window = context.getWindow();
        params = context.getWindow().getAttributes();
        init();
    }

    public void init() {
        View view = mInflater.inflate(R.layout.bottom_pop_window, null);
        params.dimAmount = 0.8f;
        //window.addFlags(LayoutParams.FLAG_DIM_BEHIND);
        mTvSetWallpaper = (TextView) view.findViewById(R.id.tv_setWallpaper);
        mTvSaveToSd = (TextView) view.findViewById(R.id.tv_saveToSd);
        mTvShareTo = (TextView) view.findViewById(R.id.tv_share);
        mTvClearAll = (TextView) view.findViewById(R.id.tv_clear);
        mTvCancel = (TextView) view.findViewById(R.id.tv_cancel);
        mTvSetWallpaper.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                onFirstButtonClick();
            }
        });
        mTvSaveToSd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                onSecondButtonClick();
                dismiss();
            }
        });
        mTvShareTo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View p1) {
                onThirdButtonClick();
                dismiss();
            }
        });
        mTvClearAll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View p1) {
                onForthButtonClick();
                dismiss();
            }

        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mPopupWindow = new PopupWindow(view, ScreenUtil.getScreenWidth(mContext), LinearLayout.LayoutParams.WRAP_CONTENT);
        //监听PopupWindow的dismiss，当dismiss时屏幕恢复亮度
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                params.alpha = 1.0f;
                window.setAttributes(params);
            }
        });
        mPopupWindow.setWidth(LayoutParams.MATCH_PARENT);
        mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        // 动画效果 从底部弹起
        mPopupWindow.setAnimationStyle(R.style.popFromBottom_animation);
    }

    /**
     * 显示底部对话框
     */
    public void show() {
        mPopupWindow.showAtLocation(anchor, Gravity.BOTTOM, 0, 0);
        params.alpha = 0.8f;
        window.setAttributes(params);
    }

    public abstract void onFirstButtonClick();

    public abstract void onSecondButtonClick();

    public abstract void onThirdButtonClick();

    public abstract void onForthButtonClick();

    public void setFirstText(String text) {
        mTvSetWallpaper.setText(text);
    }

    public void setSecondText(String text) {
        mTvSaveToSd.setText(text);
    }

    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }
}
