package cc.yuyeye.wk.Fragment;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.toolbox.ImageLoader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cc.yuyeye.wk.Activity.LoginActivity;
import cc.yuyeye.wk.Adapter.ChatAdapter;
import cc.yuyeye.wk.DB.ChatDb;
import cc.yuyeye.wk.DB.chatListBean;
import cc.yuyeye.wk.Layout.CircleImageView;
import cc.yuyeye.wk.R;
import cc.yuyeye.wk.Runnable.wkRunnable;
import cc.yuyeye.wk.Util.BitmapCache;
import cc.yuyeye.wk.Util.InternetUtil;
import cc.yuyeye.wk.Util.LogUtil;
import cc.yuyeye.wk.Util.SettingUtil;
import cc.yuyeye.wk.Util.TencentUtil;
import cc.yuyeye.wk.Util.ToastUtil;

import static cc.yuyeye.wk.MainActivity.SaveChatRecord;
import static cc.yuyeye.wk.MainActivity.checkSendPerson;
import static cc.yuyeye.wk.MainActivity.phoneAlias;
import static cc.yuyeye.wk.MainActivity.mQueue;
import static cc.yuyeye.wk.MainActivity.sendPerson;
import static cc.yuyeye.wk.MainActivity.sharedPreferences;
import static cc.yuyeye.wk.Runnable.wkRunnable.runWk_msgAdd;
import static cc.yuyeye.wk.Runnable.wkRunnable.runWk_notify;
import static cc.yuyeye.wk.Runnable.wkRunnable.wk_timeUrl;

import cc.yuyeye.wk.*;

public class ChatFragment extends Fragment {

    public static List<chatListBean> mChatLists = new ArrayList<>();
    public static List<chatListBean> mChatDialogLists = new ArrayList<>();
    public static EditText mChatMsg;
    public static LinearLayout mChatTools;
    public Button mChatToolsCancel;
    public Button mChatToolsDelete;
    public Button mChatToolsShare;
    public Button mChatToolsSelectAll;
    private TextView mChatLastLoginTime;
    private int chatNetType;
    private LinearLayout mChatNetStatusBar;
    private LinearLayout mChatContactBar;
    public static ChatDb chatDb;
    public static String msgDetail = "";
    private boolean isEnterSend;
    private RecyclerView chatRecyclerView;
    public static LinearLayout mChatMsgView;
    private CircleImageView mChatCircleImage;
    public static CircleImageView mChatHeaderImage;
    private TextView mChatContact;
    public static ImageView mChatTitleTime;
    private int MSG_NOTIFICATION = 0;
    private int MSG_MESSAGE = 1;
    private int MSG_TYPE;
    private String lastLoginTime;
    public static String msgAcceptPerson = "";
    public static String msgTitle;
    public static ChatAdapter chatAdaper;
    public static Handler chatMsgHandler;
    private int TuringCode;
    private String TuringInfo;
    public static String qq;
    public static TencentUtil tencentUtil;
    private LinearLayoutManager mLayoutManager;
    private View contactDeleteView;
    private CircleImageView contactDeleteButton;
    public static ArrayList<String> contactList = new ArrayList<>();
    private TextView mChatNetType;
    private int contactId;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        tencentUtil = new TencentUtil(getActivity());
        qq = tencentUtil.getQqNumb();

        phoneAlias = sharedPreferences.getString(SettingUtil.ID_KEY, "");
        if (phoneAlias.equals("")) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        } else {
            chatDb = new ChatDb(getActivity(), true);
            initChatDialog();
            chatAdaper = new ChatAdapter(getActivity(), mChatDialogLists);
        }
        chatMsgHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                int what = msg.what;

                switch (what) {
                    case -1: //列表底部
                        chatAdaper.notifyItemChanged(what);
                        chatRecyclerView.smoothScrollToPosition(chatAdaper.getItemCount());
                        break;
                    case -2: //列表顶部
                        chatRecyclerView.smoothScrollToPosition(0);
                        break;
                    default:
                        chatAdaper.notifyItemChanged(what);
                        chatRecyclerView.scrollToPosition(what);
                        break;
                }
            }
        };

        checkSendPerson();

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_chat, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mChatMsg = (EditText) getActivity().findViewById(R.id.chatMsgEditText);
        mChatMsgView = (LinearLayout) getActivity().findViewById(R.id.chatMsgLinearLayout);
        mChatCircleImage = (CircleImageView) getActivity().findViewById(R.id.chatCircleImageView);
        mChatTools = (LinearLayout) getActivity().findViewById(R.id.chatActionToolBar);
        mChatToolsCancel = (Button) getActivity().findViewById(R.id.chatButtonCancel);
        mChatToolsDelete = (Button) getActivity().findViewById(R.id.chatButtonDelete);
        mChatToolsShare = (Button) getActivity().findViewById(R.id.chatButtonShare);
        mChatToolsSelectAll = (Button) getActivity().findViewById(R.id.chatButtonSelectAll);
        chatRecyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerview_chat);
        mChatContactBar = (LinearLayout) getActivity().findViewById(R.id.ll_contact);
        mChatNetStatusBar = (LinearLayout) getActivity().findViewById(R.id.ll_netStatus);
        mChatLastLoginTime = (TextView) getActivity().findViewById(R.id.tv_lastlogintime);
        mChatNetType = (TextView) getActivity().findViewById(R.id.tv_nettype);
        mChatContact = (TextView) getActivity().findViewById(R.id.tv_contacts);
        mChatHeaderImage = (CircleImageView) getActivity().findViewById(R.id.chatTitleHeader);
//        mChatTitleTime = (ImageView) getActivity().findViewById(R.id.chatTitleTimeIcon);
        LayoutInflater mInflater = LayoutInflater.from(getActivity());
        contactDeleteView = mInflater.inflate(R.layout.contact_delete_window, null);
        contactDeleteButton = (CircleImageView) contactDeleteView.findViewById(R.id.contactDelete);

        chooseQq();
        setQqIcon(qq);
        setTitleIcon();
        initContacts();

        mChatContactBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View p1) {
                if (sharedPreferences.getBoolean(SettingUtil.CONTACTS_TIPS, true)) {
                    ToastUtil.showToast(getResources().getString(R.string.longPressDeleteContact));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(SettingUtil.CONTACTS_TIPS, false);
                    editor.apply();
                }
                ContactDialog();
            }
        });
        mChatContactBar.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View p1) {
                new MaterialDialog.Builder(getActivity())
                        .title("Alert")
                        .content("Are you going to delete " + msgAcceptPerson)
                        .positiveText(R.string.confirm)
                        .negativeText(R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {

                            @Override
                            public void onClick(MaterialDialog p1, DialogAction p2) {
                                try {
                                    msgAcceptPerson = mChatContact.getText().toString();
                                    SharedPreferences.Editor editor = sharedPreferences.edit();

                                    if (contactId > 1) {
                                        MainActivity.chatDb.deleteByContact(phoneAlias, contactList.get(contactId));
                                        contactList.remove(contactId);

                                        ToastUtil.showToast(getString(R.string.contactDeleted) + msgAcceptPerson);

                                        contactId = contactList.size() - 1;
                                        msgAcceptPerson = contactList.get(contactId);
                                        if (!sendPerson.equals(msgAcceptPerson)) {
                                            editor.putString("sendPerson", msgAcceptPerson);
                                        }
                                        editor.apply();
                                        updateChatDialog();
                                    } else {
                                        ToastUtil(getString(R.string.wkCantDothat));
                                    }

                                } catch (Exception e) {
                                    ToastUtil(getString(R.string.deleteContactError) + e);
                                }
                            }

                            private void ToastUtil(String string) {
                                // TODO: Implement this method
                            }
                        })
                        .show();
                Vibrator vibrator = (Vibrator) getActivity().getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
                long[] pattern = {0, 10, 40, 10};
                vibrator.vibrate(pattern, -1);
                return true;
            }
        });
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                {
                    LinearSmoothScroller linearSmoothScroller =
                            new LinearSmoothScroller(recyclerView.getContext()) {
                                @Override
                                protected int calculateTimeForScrolling(int dx) {
                                    if (dx > 3000) {
                                        dx = 3000;
                                    }
                                    return super.calculateTimeForScrolling(dx);
                                }

//                                @Override
//                                public PointF computeScrollVectorForPosition(int targetPosition) {
//                                    return mLayoutManager.computeScrollVectorForPosition(targetPosition);
//                                }
                            };
                    linearSmoothScroller.setTargetPosition(position);
                    startSmoothScroll(linearSmoothScroller);
                }
            }
        });
//        chatRecyclerView.setItemAnimator(new DefaultItemAnimator());
        chatRecyclerView.setAdapter(chatAdaper);
        chatMsgHandler.sendEmptyMessage(-1);
        mChatMsg.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View p1, boolean p2) {
                isEnterSend = sharedPreferences.getBoolean(SettingUtil.ENTER_SEND_KEY, false);

                mChatMsg.setSingleLine(isEnterSend);
                if (isEnterSend) {
                    mChatMsg.setImeOptions(EditorInfo.IME_ACTION_SEND);
                } else {
                    mChatMsg.setImeOptions(EditorInfo.IME_ACTION_GO);
                    mChatMsg.setMaxLines(10);
                }
            }
        });
        mChatMsg.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    if (isEnterSend) {
                        if ((actionId == EditorInfo.IME_ACTION_SEND)
                                || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                            sendMSG(MSG_TYPE);
                            //让mPasswordEdit获取输入焦点
                            mChatMsg.requestFocus();
                            chatMsgHandler.sendEmptyMessage(-1);
                        }
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    LogUtil.e("SendClick", e);
                    return false;
                }

            }
        });
        mChatMsg.addTextChangedListener(new TextWatcher() {
            private CharSequence temp = "";

            @Override
            public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
                temp = p1;
            }

            @Override
            public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
            }

            @Override
            public void afterTextChanged(Editable p1) {
                if (temp.length() > 0) {
                    mChatCircleImage.setImageResource(R.drawable.send);
                } else {
                    setQqIcon(qq);
                }
            }
        });
//        mChatTitleTime.setOnClickListener(new OnClickListener() {
//
//            private String report;
//
//            @Override
//            public void onClick(View view) {
//                report = "now";
//                new Thread(wkRunnable.runWk_login).start();
//                new Thread(wkRunnable.runWk_message).start();
//                mChatLastLoginTime.setText("");
//                mChatNetType.setText("");
//                wk_timeTask wkTask = new wk_timeTask();
//                wkTask.execute();
//            }
//        });
        mChatHeaderImage.setOnClickListener(new OnClickListener() {

            @Override

            public void onClick(View p1) {
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (MSG_TYPE == MSG_NOTIFICATION) {
                    MSG_TYPE = MSG_MESSAGE;
                    mChatHeaderImage.setImageResource(R.drawable.ic_incognito);
                    mChatMsg.setHint(getResources().getString(R.string.inputToastMsgContent));
                } else if (MSG_TYPE == MSG_MESSAGE) {
                    MSG_TYPE = MSG_NOTIFICATION;
                    mChatHeaderImage.setImageResource(R.drawable.ic_notifications_white);
                    mChatMsg.setHint(getResources().getString(R.string.inputMsgContent));
                }
                editor.putInt(SettingUtil.MSG_TYPE_KEY, MSG_TYPE);
                editor.apply();
            }
        });
        mChatCircleImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View p1) {
                sendMSG(MSG_TYPE);
            }
        });

        mChatCircleImage.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View p1) {
                if (mChatMsg.getText().toString().length() > 0) {
                    sendMSG(MSG_TYPE);
                } else {
                    chooseQq();
                }
                return true;
            }

        });
		chatAdaper.setRecyclerViewOnItemClickListener(new ChatAdapter.RecyclerViewOnItemClickListener(){

				@Override
				public void onItemClickListener(View view, int position)
				{
					view.getTag(position);
				}

				@Override
				public boolean onItemLongClickListener(View view, int position)
				{
					// TODO: Implement this method
					return false;
				}
			});
		
		
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        if (InternetUtil.getNetworkState(getActivity()) != 0) {
            if (!sharedPreferences.getBoolean(SettingUtil.SHOW_NETTYPE_KEY, true)) {
                mChatNetStatusBar.setVisibility(View.GONE);
            } else {
                mChatNetStatusBar.setVisibility(View.VISIBLE);
            }
            mChatNetType.setText("");
            mChatLastLoginTime.setText("");
            wk_timeTask wkTask = new wk_timeTask();
            wkTask.execute();
        }
        super.onResume();
    }

    public void checkPhoneAlias() {
        phoneAlias = sharedPreferences.getString(SettingUtil.ID_KEY, "");
        if (phoneAlias.equals("")) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
    }

    private void initContacts() {
        contactList.add(getResources().getString(R.string.allContacts));
        contactList.add(phoneAlias);
        try {
            contactList = chatDb.getContacts(getActivity(), phoneAlias);
        } catch (Exception e) {
            Log.i("init", "iniContacts Error\n" + e);
        }
        mChatContact.setText(sendPerson);
    }


    private void ContactDialog() {
        contactList.add(getResources().getString(R.string.allContacts));
        contactList.add(phoneAlias);
        try {
            contactList = chatDb.getContacts(getActivity(), phoneAlias);
        } catch (Exception e) {
            Log.i("init", "iniContacts Error\n" + e);
        }
        new MaterialDialog.Builder(getActivity())
                .titleGravity(GravityEnum.CENTER)
                .titleColorRes(R.color.colorPrimary)
                .title(R.string.contacts)
                .items(contactList)
                .itemsGravity(GravityEnum.CENTER)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        msgAcceptPerson = text.toString();
                        if (!sendPerson.equals(msgAcceptPerson)) {
                            // updateChatDialog();
                            editor.putString("sendPerson", msgAcceptPerson);
                        }
                        editor.apply();
                        contactId = position;
                        mChatContact.setText(msgAcceptPerson);
                        sendPerson = msgAcceptPerson;
                        updateChatDialog();
                    }
                })
                .negativeText(R.string.cancel)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        addContactDialog();
                    }
                })
                .neutralColorRes(R.color.colorPrimary)
                .neutralText(R.string.add)
                .show();
    }

    private void addContactDialog() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.add)
                .inputRange(1,20)
                .input(getResources().getString(R.string.contacts), "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String contact = input.toString();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("sendPerson", contact);
                        editor.apply();
                        contactList.add(contact);
                        mChatContact.setText(contact);
                        sendPerson = contact;
                        initChatDialog();
                    }
                })
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.confirm)
                .show();
    }

    public void setTitleIcon() {
        MSG_TYPE = sharedPreferences.getInt(SettingUtil.MSG_TYPE_KEY, 0);
        if (MSG_TYPE == MSG_NOTIFICATION) {
            mChatHeaderImage.setImageResource(R.drawable.ic_notifications_white);
        } else if (MSG_TYPE == MSG_MESSAGE) {
            mChatMsg.setHint(R.string.inputToastMsgContent);
            mChatHeaderImage.setImageResource(R.drawable.ic_incognito);
        }
    }

    public void setQqIcon(String qq) {
        ImageLoader.ImageListener qqIconlistener = ImageLoader.getImageListener(mChatCircleImage, 0, 0);
        ImageLoader qqIconLoader = new ImageLoader(mQueue, new BitmapCache());
        if (Objects.equals(qq, "")) {
            qqIconLoader.get(tencentUtil.getQqIconUrl("10000"), qqIconlistener);
        } else {
            qqIconLoader.get(tencentUtil.getQqIconUrl(qq), qqIconlistener);
        }
    }

    private void sendMSG(int i) {
        msgDetail = mChatMsg.getText().toString();
        msgAcceptPerson = mChatContact.getText().toString();
        if (i == MSG_NOTIFICATION) {
            send_Notification(msgAcceptPerson, msgDetail);
        } else {
            send_Message(msgAcceptPerson, msgDetail);
        }
    }

    private void send_Notification(String msgAcceptPerson, String msgDetail) {
        if (!phoneAlias.equals("")) {

            msgTitle = phoneAlias;

            if (!msgDetail.equals("")) {
                SaveChatRecord(phoneAlias, phoneAlias, msgAcceptPerson, msgDetail);

                if (InternetUtil.getNetworkState(getActivity()) != 0) {
                    checkSendPerson();
                    //服务器添加数据库记录
                    new Thread(runWk_msgAdd).start();

                    chatListBean chatListBean = new chatListBean();
                    chatListBean.setMeSend(true);
                    chatListBean.setMsgContent(mChatMsg.getText().toString());
                    chatListBean.setcSend(phoneAlias);
                    chatListBean.setIconUrl(tencentUtil.getQqIconUrl(qq));
                    chatAdaper.addItem(chatListBean);

                    chatMsgHandler.sendEmptyMessage(-1);
                    if (msgAcceptPerson.equals(getString(R.string.Turing))) {
                        TuringTask dTask = new TuringTask();
                        dTask.execute();
                    } else {
                        new Thread(runWk_notify).start();
                    }
                } else {
                    ToastUtil.showToast(getResources().getString(R.string.networkError));
                }
                Animation animSwing = AnimationUtils.loadAnimation(getActivity(), R.anim.swing);

                mChatMsg.setText("");
                mChatCircleImage.startAnimation(animSwing);

                MediaPlayer sendSound = MediaPlayer.create(getActivity(), R.raw.hangouts_msg);
                sendSound.start();
            } else {
                if (qq.equals("")) {
                    chooseQq();
                }
            }

        } else {
            ToastUtil.showSimpleToast(getString(R.string.pleaseRegisterId));
        }
    }

    public void chooseQq() {
        qq = tencentUtil.getQqNumb();

        if (Objects.equals(qq, "")) {
            if (tencentUtil.getQqList().size() == 0) {
                inputQq();
            } else {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.selectQq)
                        .items(tencentUtil.getQqList())
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                qq = text.toString();
                                tencentUtil.saveQq(qq);
                                setQqIcon(qq);
                                return true;
                            }
                        })
                        .neutralText(R.string.inputQq)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                inputQq();
                            }
                        })
                        .positiveText(R.string.confirm)
                        .show();
            }
        }
    }

    public void inputQq() {
        MaterialDialog inputQqDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.inputQq)
                .inputRangeRes(5, 11, R.color.colorAccent)
                .positiveText(R.string.confirm)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorPrimary)
                .cancelable(false)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input("QQ", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (input.length() != 0) {
                            tencentUtil.saveQq(input.toString());
                        } else {
                            ToastUtil.showSimpleToast("QQ号不可为空");
                        }
                    }
                })
                .build();
        inputQqDialog.show();
    }


    public void updateChatDialog() {
        checkSendPerson();
        initChatDialog();
        chatAdaper.notifyDataSetChanged();
        chatMsgHandler.sendEmptyMessage(-1);
        mChatNetType.setText("");
        mChatLastLoginTime.setText("");
        wk_timeTask wkTask = new wk_timeTask();
        wkTask.execute();
    }

    public void initChatDialog() {
        try {
            mChatLists = chatDb.getChatRecord(phoneAlias, sendPerson, phoneAlias);
            if (mChatLists != null) {
                mChatDialogLists.clear();

                for (chatListBean c : mChatLists) {
                    chatListBean clb = new chatListBean();

                    if (c.getcSend().equals(phoneAlias)) {
                        clb.setMeSend(true);
                        clb.setIconUrl(tencentUtil.getQqIconUrl(qq));
                    } else {
                        clb.setMeSend(false);
                    }
                    clb.setId(c.getId());
                    clb.setMsgContent(c.getMsgContent());
                    clb.setcSend(c.getcSend());
                    clb.setTime(c.getTime());
                    mChatDialogLists.add(clb);
                }
                chatAdaper.notifyDataSetChanged();
//                chatAdaper.notifyItemInserted(mChatDialogLists.size() - 1);
                //todo 滑动啥
                chatRecyclerView.smoothScrollToPosition(chatAdaper.getItemCount());
                chatMsgHandler.sendEmptyMessage(-1);
            }
        } catch (Exception e) {
            LogUtil.e("init", "initChat Error " + e);
        }
    }

    public void send_Message(String msgAcceptPerson, String msgDetail) {
        if (!phoneAlias.equals("")) {

            sendPerson = msgAcceptPerson;
            msgTitle = phoneAlias;

            if (!msgDetail.equals("")) {
                //服务器API发送通知
                new Thread(wkRunnable.runWk_message).start();
                //服务器添加数据库记录
                if (sharedPreferences.getBoolean(SettingUtil.SAVE_MSG_KEY, false)) {
                    checkSendPerson();
                    new Thread(runWk_msgAdd).start();
					chatListBean chatListBean = new chatListBean();
                    chatListBean.setMeSend(true);
                    chatListBean.setMsgContent(msgDetail);
                    chatListBean.setcSend(phoneAlias);
                    chatListBean.setIconUrl(tencentUtil.getQqIconUrl(qq));
                    chatAdaper.addItem(chatListBean);
					chatMsgHandler.sendEmptyMessage(-1);
                    SaveChatRecord(phoneAlias, phoneAlias, msgAcceptPerson, msgDetail);
                }

                Animation animSwing = AnimationUtils.loadAnimation(getActivity(), R.anim.swing_wide);
                animSwing.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation p1) {
                        mChatMsg.setText("");
                        mChatMsg.setHint(getResources().getString(R.string.inputToastMsgContent));
                    }

                    @Override
                    public void onAnimationEnd(Animation p1) {
                        mChatMsg.setHint(getResources().getString(R.string.inputToastMsgContent));
                    }

                    @Override
                    public void onAnimationRepeat(Animation p1) {

                    }
                });
                mChatCircleImage.startAnimation(animSwing);
                MediaPlayer sendSound = MediaPlayer.create(getActivity(), R.raw.msg_send);
                sendSound.start();
            }
//            initChatDialog();
        }
    }

    public class wk_timeTask extends AsyncTask<Integer, Integer, String> {

        private String result;
        private InputStream is;
        private ArrayList<NameValuePair> nameValuePairs;

        @Override
        protected void onPreExecute() {
            result = "";

            nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("alias", sendPerson));
            is = null;
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer[] params) {

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(wk_timeUrl);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            } catch (Exception e) {
                LogUtil.e("time", "连接失败" + e.toString());
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf8"), 8);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
            } catch (Exception e) {
                LogUtil.e("time", "转换" + e.toString());
            }

            try {
                JSONArray jArray = new JSONArray(result);
                JSONObject jsonObj = jArray.getJSONObject(0);
                lastLoginTime = jsonObj.getString("time");
                chatNetType = jsonObj.getInt("nettype");
                Log.d("time", chatNetType + " " + lastLoginTime);
            } catch (JSONException e) {
                LogUtil.e("time", "解析失败 " + e.toString());
            }

            return lastLoginTime;
        }

        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            if (!Objects.equals(sendPerson, getString(R.string.allContacts)) && !Objects.equals(sendPerson, getString(R.string.Turing))) {
                Animation animLoading = AnimationUtils.loadAnimation(getActivity(), R.anim.popupfromtop_enter);
                switch (chatNetType) {
                    case 0:
                        mChatNetType.setText("");
                        break;
                    case 1:
                        mChatNetType.setText("WiFi ");
                        break;
                    case 2:
                        mChatNetType.setText("2G ");
                        break;
                    case 3:
                        mChatNetType.setText("3G ");
                        break;
                    case 4:
                        mChatNetType.setText("4G ");
                        break;
                    case 5:
                        mChatNetType.setText("E ");
                        break;
                    default:
                        mChatNetType.setText("");
                        break;
                }
                mChatNetType.startAnimation(animLoading);
                mChatLastLoginTime.setText(lastLoginTime);
                mChatLastLoginTime.startAnimation(animLoading);
            } else {
                mChatLastLoginTime.setText(lastLoginTime);
            }
            super.onPostExecute(result);
        }
    }

    public class TuringTask extends AsyncTask<Integer, Integer, String> {

        private String result;
        private InputStream is;
        private ArrayList<NameValuePair> nameValuePairs;

        //后面尖括号内分别是参数（例子里是线程休息时间），进度（publishProgress用到），返回值类型
        @Override
        protected void onPreExecute() {
            //第一个执行方法
            result = "";

            nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("key", "0aad89b1e6b74ab5932fe584269ea531"));
            nameValuePairs.add(new BasicNameValuePair("info", msgDetail));
            nameValuePairs.add(new BasicNameValuePair("userid", phoneAlias));

            is = null;
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer[] params) {
            //第二个执行方法,onPreExecute()执行完后执行
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://www.tuling123.com/openapi/api");
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            } catch (Exception e) {
                LogUtil.e("turing", "连接失败" + e.toString());
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf8"), 8);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
            } catch (Exception e) {
                LogUtil.e("turing", "转换" + e.toString());
            }

            try {
                JSONObject jsonObj = new JSONObject(result);

                for (int i = 0; i < jsonObj.length(); i++) {
                    TuringCode = jsonObj.getInt("code");

                }
                if (TuringCode == 100000) {
                    TuringInfo = jsonObj.getString("text");
                } else if (TuringCode == 200000) {
                    String turingUrl = jsonObj.getString("url");
                    TuringInfo = jsonObj.getString("text") + "\n" + turingUrl;
                }
            } catch (JSONException e) {
                LogUtil.e("turing", "解析失败 " + e.toString());
            }

            return TuringInfo;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            chatListBean chatListBean;
            chatListBean = new chatListBean();
            chatListBean.setMeSend(false);
            chatListBean.setMsgContent(result);
            chatListBean.setcSend(getString(R.string.Turing));
            chatListBean.setIconUrl("http://bbs.tuling123.com/static/common/avatar-mid-img.png");

            chatAdaper.addItem(chatListBean);
            chatRecyclerView.smoothScrollToPosition(chatAdaper.getItemCount());
            SaveChatRecord(phoneAlias, getString(R.string.Turing), phoneAlias, result);
            //服务器添加数据库记录
            if (sharedPreferences.getBoolean(SettingUtil.SAVE_MSG_KEY, false)) {
                sendPerson = phoneAlias;
                phoneAlias = getResources().getString(R.string.Turing);
                msgDetail = result;
                new Thread(runWk_msgAdd).start();
            }

        }
    }
}
