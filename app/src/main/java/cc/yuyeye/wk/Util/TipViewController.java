package cc.yuyeye.wk.Util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.Locale;

import cc.yuyeye.wk.Fragment.ChatFragment;

import cc.yuyeye.wk.MainActivity;
import cc.yuyeye.wk.R;
import cc.yuyeye.wk.Runnable.wkTask;
import cc.yuyeye.wk.*;

public class TipViewController implements View.OnClickListener, View.OnTouchListener, ViewContainer.KeyEventHandler {

    private WindowManager mWindowManager;
    private Context mContext;
    private ViewContainer mWholeView;
    private View mContentView;
    private ViewDismissHandler mViewDismissHandler;
    private CharSequence mContent;
    private TextView mMsgContent;
    private SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm", Locale.PRC);
    private SharedPreferences mSharePre;

    private EditText mReplyContent;

    public TipViewController(Context application, CharSequence content) {
        mContext = application;
        mContent = content;
        mWindowManager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
        mSharePre = PreferenceManager.getDefaultSharedPreferences(application);
    }

    public void setViewDismissHandler(ViewDismissHandler viewDismissHandler) {
        mViewDismissHandler = viewDismissHandler;
    }

    public void updateContent(CharSequence content) {
        mContent = content;
        mMsgContent.setText(mContent);
    }

    public void replyMsg() {
        ChatFragment.msgDetail = mReplyContent.getText().toString();
		new wkTask.wk_message(Common.context, mSharePre.getString("sendPerson", "W.K."), Common.phoneAlias, mReplyContent.getText().toString(), "");
      //  new Thread(wkRunnable.runWk_message).start();
        if (mSharePre.getBoolean(SettingUtil.SAVE_MSG_KEY, false)) {
			new wkTask.msg_upload(Common.context, MainActivity.phoneAlias, mSharePre.getString("sendPerson", "W.K."), mReplyContent.getText().toString()).execute();
            MainActivity.SaveChatRecord(MainActivity.phoneAlias, MainActivity.phoneAlias, MainActivity.sendPerson, ChatFragment.msgDetail);
        }
        MediaPlayer sendSound = MediaPlayer.create(mContext, R.raw.msg_send);
        sendSound.start();
    }

    public void show() {

        ViewContainer view = (ViewContainer) View.inflate(mContext, R.layout.pop_view, null);

        Button mReplyBtn = (Button) view.findViewById( R.id.replyBtn);
        mReplyContent = (EditText) view.findViewById(R.id.replyContent);
        mMsgContent = (TextView) view.findViewById(R.id.pop_view_text);
        TextView mMsgTitle = (TextView) view.findViewById(R.id.msgTitle);
        TextView mMsgTime = (TextView) view.findViewById(R.id.msgTime);

        mMsgTitle.setText(mSharePre.getString("sendPerson", "W.K.") + " :");
        mMsgTime.setText(sDateFormat.format(new java.util.Date()));
        mMsgContent.setText(mContent);

        mWholeView = view;
        mContentView = view.findViewById(R.id.pop_view_content_view);

        // event listeners
        mContentView.setOnClickListener(this);
        mWholeView.setOnTouchListener(this);
        mWholeView.setKeyEventHandler(this);

        mReplyBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View p1) {
                replyMsg();

                removePoppedViewAndClear();
            }
        });
        mReplyContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    if ((actionId == EditorInfo.IME_ACTION_SEND)
                            || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        replyMsg();
                        removePoppedViewAndClear();
                    }
                    return true;
                } catch (Exception e) {
                    LogUtil.e("replyClick", e);
                    return false;
                }

            }
        });

        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.MATCH_PARENT;

        int flags = 0;
        int type;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //解决Android 7.1.1起不能再用Toast的问题（先解决crash）
            if (Build.VERSION.SDK_INT > 24) {
                type = WindowManager.LayoutParams.TYPE_PHONE;
            } else {
                type = WindowManager.LayoutParams.TYPE_TOAST;
            }
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(w, h, type, flags, PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP;
        layoutParams.windowAnimations = R.style.popFromTop_animation;
        mWindowManager.addView(mWholeView, layoutParams);
    }

    @Override
    public void onClick(View v) {
        removePoppedViewAndClear();
        MainActivity.receiveNotification = true;
        Intent i = new Intent(mContext, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
    }

    private void removePoppedViewAndClear() {

        // remove view
        if (mWindowManager != null && mWholeView != null) {
            mWindowManager.removeView(mWholeView);
        }

        if (mViewDismissHandler != null) {
            mViewDismissHandler.onViewDismiss();
        }

        // remove listeners
        mContentView.setOnClickListener(null);
        mWholeView.setOnTouchListener(null);
        mWholeView.setKeyEventHandler(null);
    }

    /**
     * touch the outside of the content view, remove the popped view
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        Rect rect = new Rect();
        mContentView.getGlobalVisibleRect(rect);
        if (!rect.contains(x, y)) {
            removePoppedViewAndClear();
        }
        return false;
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            removePoppedViewAndClear();
        }
    }

    public interface ViewDismissHandler {
        void onViewDismiss();
    }
}
