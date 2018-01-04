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
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cc.yuyeye.wk.Activity.LoginActivity;
import cc.yuyeye.wk.Adapter.ChatAdapter;
import cc.yuyeye.wk.Common;
import cc.yuyeye.wk.DB.ChatDb;
import cc.yuyeye.wk.DB.chatListBean;
import cc.yuyeye.wk.Layout.CircleImageView;
import cc.yuyeye.wk.MainActivity;
import cc.yuyeye.wk.R;
import cc.yuyeye.wk.Runnable.wkTask;
import cc.yuyeye.wk.Util.BitmapCache;
import cc.yuyeye.wk.Util.InternetUtil;
import cc.yuyeye.wk.Util.LogUtil;
import cc.yuyeye.wk.Util.SettingUtil;
import cc.yuyeye.wk.Util.TencentUtil;
import cc.yuyeye.wk.Util.ToastUtil;
import cn.jpush.android.api.JPushInterface;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static cc.yuyeye.wk.MainActivity.SaveChatRecord;
import static cc.yuyeye.wk.MainActivity.checkSendPerson;
import static cc.yuyeye.wk.MainActivity.getCurrentTime;
import static cc.yuyeye.wk.MainActivity.mQueue;
import static cc.yuyeye.wk.MainActivity.phoneAlias;
import static cc.yuyeye.wk.MainActivity.sendPerson;
import android.content.*;

public class ChatFragment extends Fragment {

    //    public static List<chatListBean> mChatDialogLists = new ArrayList<>();
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
    private int deleteItemPosition = 0;

    public static boolean isForeground = false;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        tencentUtil = new TencentUtil(getActivity());
        qq = tencentUtil.getQqNumb();

        phoneAlias = Common.getSharedPreference().getString(SettingUtil.ID_KEY, "");

        chatMsgHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                int what = msg.what;

                if (chatAdaper != null) {
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
            }
        };

        checkSendPerson();

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (phoneAlias.equals("")) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        } else {
            chatDb = new ChatDb(getActivity(), true);
            initChatDialog();
            chatAdaper = new ChatAdapter(getActivity(), mChatDialogLists);
        }

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
                if (Common.getSharedPreference().getBoolean(SettingUtil.CONTACTS_TIPS, true)) {
                    ToastUtil.showToast(getResources().getString(R.string.longPressDeleteContact));
                    SharedPreferences.Editor editor = Common.getSharedPreference().edit();
                    editor.putBoolean(SettingUtil.CONTACTS_TIPS, false);
                    editor.apply();
                }
                showContactDialog();
            }
        });
        mChatContactBar.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View p1) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.alert)
                        .content(getString(R.string.confirm_delete) + mChatContact.getText() + " ?")
                        .positiveText(R.string.confirm)
                        .negativeColorRes(R.color.colorPrimary)
                        .negativeText(R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {

                            @Override
                            public void onClick(MaterialDialog p1, DialogAction p2) {
                                try {
                                    msgAcceptPerson = mChatContact.getText().toString();
                                    SharedPreferences.Editor editor = Common.getSharedPreference().edit();

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
                                        mChatContact.setText(msgAcceptPerson);
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
                isEnterSend = Common.getSharedPreference().getBoolean(SettingUtil.ENTER_SEND_KEY, false);

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

        mChatHeaderImage.setOnClickListener(new OnClickListener() {

            @Override

            public void onClick(View p1) {
                SharedPreferences.Editor editor = Common.getSharedPreference().edit();

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
        if (chatAdaper != null) {
            chatAdaper.setOnItemListener(new ChatAdapter.OnItemListener() {
                @Override
                public void OnItemClickListener(int position, ChatAdapter.ViewHolder vh) {

                }

                @Override
                public boolean OnItemLongClickListener(int position, ChatAdapter.ViewHolder vh) {
                    return true;
                }
            });
        }

        mChatToolsShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    List<chatListBean> chat_dialog_id_list = chatAdaper.getChatDialogId();
                    if (chat_dialog_id_list.size() > 0) {
                        String share_msg = getString(R.string.with) + sendPerson + getString(R.string.chatRecords);
                        Intent share_intent = new Intent();
                        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
                        share_intent.setType("text/plain");//设置分享内容的类型
                        share_intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.with) + sendPerson + getString(R.string.chatRecords));//添加分享内容标题

                        for (int i = 0; i < chat_dialog_id_list.size(); i++) {
                            share_msg += "\n" + chat_dialog_id_list.get(i).getTime();
                            share_msg += "\n" + chat_dialog_id_list.get(i).getcSend() + ": ";
                            share_msg += chat_dialog_id_list.get(i).getMsgContent();
                        }
                        share_intent.putExtra(Intent.EXTRA_TEXT, share_msg);//添加分享内容
                        //创建分享的Dialog
                        share_intent = Intent.createChooser(share_intent, getString(R.string.shareTo));
                        getActivity().startActivity(share_intent);

                        chatAdaper.showActionBar(false);
                        chatAdaper.setActionMode();
                        chatAdaper.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.NotSelectAnyRecords), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mChatToolsCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                chatAdaper.showActionBar(false);
                chatAdaper.setActionMode();
                chatAdaper.notifyDataSetChanged();
                mChatToolsSelectAll.setText(R.string.selectAll);
//                chatMsgHandler.sendEmptyMessage(-1);
            }
        });
        mChatToolsSelectAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> chat_db_id_list = chatAdaper.getChatDbId();
                if (chat_db_id_list.size() < chatAdaper.getItemCount()) {
                    chatAdaper.selectAllItem(true);
                    chatAdaper.notifyDataSetChanged();
                    mChatToolsSelectAll.setText(R.string.cancelSelectAll);
                } else {
                    chatAdaper.selectAllItem(false);
                    mChatToolsSelectAll.setText(R.string.selectAll);
                }
            }
        });
        mChatToolsDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<Integer, Boolean> chat_checkBox_map = chatAdaper.getCheckBoxMap();
                List<chatListBean> chat_dialog_id_list = chatAdaper.getChatDialogId();
                List<Integer> chat_db_id_list = chatAdaper.getChatDbId();
                if (chat_dialog_id_list.size() > 0 && chat_db_id_list.size() > 0) {
                    try {
                        for (int i = 0; i < chat_checkBox_map.size(); i++) {
                            if (chat_checkBox_map.get(i)) {
                                deleteItemPosition = i;
                                break;
                            }
                        }
                        for (int i = 0; i < chat_db_id_list.size(); i++) {
                            mChatDialogLists.remove(chat_dialog_id_list.get(i));
                            chatDb.deleteById(phoneAlias, chat_db_id_list.get(i));
                            //chatAdaper.notifyDataSetChanged();
                            chatAdaper.notifyItemRemoved(deleteItemPosition);
                            chatAdaper.notifyItemRangeChanged(deleteItemPosition, chatAdaper.getItemCount());
                        }
                        chatAdaper.selectAllItem(false);

                        //  chatMsgHandler.sendEmptyMessage(deleteItemPosition);
                    } catch (Exception e) {
                        LogUtil.e("wk_delete", getString(R.string.deleteRecordsError) + e);
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.NotSelectAnyRecords), Toast.LENGTH_SHORT).show();
                }
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        JPushInterface.clearAllNotifications(getActivity());
        if (InternetUtil.getNetworkState(getActivity()) != 0) {
            if (!Common.getSharedPreference().getBoolean(SettingUtil.SHOW_NETTYPE_KEY, true)) {
                mChatNetStatusBar.setVisibility(View.GONE);
            } else {
                mChatNetStatusBar.setVisibility(View.VISIBLE);
            }
            mChatNetType.setText("");
            mChatLastLoginTime.setText("");
            if (sendPerson != null) {
                new wk_timeTask(getActivity()).execute();
            }

        }
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        isForeground = isVisibleToUser;
        super.setUserVisibleHint(isVisibleToUser);
    }

    public void checkPhoneAlias() {
        phoneAlias = Common.getSharedPreference().getString(SettingUtil.ID_KEY, "");
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


    private void showContactDialog() {
        contactList.clear();
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
                        SharedPreferences.Editor editor = Common.getSharedPreference().edit();
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
                .inputRange(1, 20)
                .input(getResources().getString(R.string.contacts), "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String contact = input.toString();
                        SharedPreferences.Editor editor = Common.getSharedPreference().edit();
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
        MSG_TYPE = Common.getSharedPreference().getInt(SettingUtil.MSG_TYPE_KEY, 0);
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

                    new wkTask.msg_upload(getActivity(), phoneAlias, sendPerson, msgDetail).execute();
                    chatListBean chatListBean = new chatListBean();
                    chatListBean.setMeSend(true);
                    chatListBean.setMsgContent(mChatMsg.getText().toString());
                    chatListBean.setcSend(phoneAlias);
                    chatListBean.setTime(getCurrentTime());
                    chatListBean.setIconUrl(tencentUtil.getQqIconUrl(qq));
//                    mChatDialogLists.add(chatListBean);
                    chatAdaper.addItem(chatListBean);

                    chatMsgHandler.sendEmptyMessage(-1);
                    if (msgAcceptPerson.equals(getString(R.string.Turing))) {
                        TuringTask dTask = new TuringTask();
                        dTask.execute();
                    } else {
                        //new Thread(runWk_notify).start();
                        new wkTask.wk_notify(getActivity(), msgAcceptPerson, phoneAlias, msgDetail).execute();
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
        //initChatDialog();
        try {
            mChatDialogLists = chatDb.getChatRecord(phoneAlias, sendPerson, phoneAlias);
            chatAdaper = new ChatAdapter(getActivity(), mChatDialogLists);
            chatRecyclerView.setAdapter(chatAdaper);
            chatRecyclerView.smoothScrollToPosition(chatAdaper.getItemCount());
            chatMsgHandler.sendEmptyMessage(-1);
        } catch (Exception e) {
            LogUtil.e("init", "initChat Error " + e);
        }

        chatAdaper.notifyDataSetChanged();
        chatMsgHandler.sendEmptyMessage(-1);
        mChatNetType.setText("");
        mChatLastLoginTime.setText("");
        wk_timeTask wkTask = new wk_timeTask(getActivity());
        wkTask.execute();
    }

    public void initChatDialog() {
        try {
            mChatDialogLists = chatDb.getChatRecord(phoneAlias, sendPerson, phoneAlias);
            if (mChatDialogLists != null) {
//                mChatDialogLists.clear();
//
//                for (chatListBean c : mChatDialogLists) {
//                    chatListBean clb = new chatListBean();
//
//                    if (c.getcSend().equals(phoneAlias)) {
//                        clb.setMeSend(true);
//                        clb.setIconUrl(tencentUtil.getQqIconUrl(qq));
//                    } else {
//                        clb.setMeSend(false);
//                    }
//                    clb.setId(c.getId());
//                    clb.setMsgContent(c.getMsgContent());
//                    clb.setcSend(c.getcSend());
//                    clb.setTime(c.getTime());
//                    mChatDialogLists.add(clb);
//                }
                chatAdaper.notifyDataSetChanged();
////                chatAdaper.notifyItemInserted(mChatDialogLists.size() - 1);
//                //todo 滑动啥
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
                new wkTask.wk_message(getActivity(), sendPerson, phoneAlias, msgDetail, "").execute();
//                new Thread(wkTask.runWk_message).start();
                //服务器添加数据库记录
                if (Common.getSharedPreference().getBoolean(SettingUtil.SAVE_MSG_KEY, false)) {
                    checkSendPerson();
                    new wkTask.msg_upload(getActivity(), phoneAlias, sendPerson, msgDetail).execute();

                    chatListBean chatListBean = new chatListBean();
                    chatListBean.setMeSend(true);
                    chatListBean.setMsgContent(msgDetail);
                    chatListBean.setcSend(phoneAlias);
                    chatListBean.setIconUrl(tencentUtil.getQqIconUrl(qq));
//                    mChatDialogLists.add(chatListBean);
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
        String send_msg_time;
        boolean isRead = false;
		Context context;

		public wk_timeTask(Context context) {
			this.context = context;
		}
		
        @Override
        protected void onPreExecute() {
            result = "";
            if (chatAdaper != null) {
                chatAdaper.setIsRead(isRead);
            }
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer[] params) {
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("alias", sendPerson)
                        .add("accept", phoneAlias)
                        .build();
                //InputStream inputStream = context.getAssets().open("api.crt");
//				OkHttpClient client = HttpsUtil.getTrustAllClient();
                Request request = new Request.Builder()
                        .url(wkTask.wk_timeUrl)
                        .post(requestBody)
                        .build();
                Response response = Common.mClient.newCall(request).execute();
                result = response.body().string();
            } catch (Exception e) {
                LogUtil.e("wktime", "连接失败" + e.toString());
            }

            try {
                JSONArray jArray = new JSONArray(result);
                JSONObject jsonObj = jArray.getJSONObject(0);
                lastLoginTime = jsonObj.getString("time");
                chatNetType = jsonObj.getInt("nettype");
                send_msg_time = jsonObj.getString("msg_time");
            } catch (JSONException e) {
                LogUtil.e("wktime", "解析失败 " + e.toString() + "\nResult: " + result);
            }

            return lastLoginTime;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!sendPerson.equals(context.getResources().getString(R.string.allContacts))
                    && !sendPerson.equals(context.getResources().getString(R.string.Turing))) {
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

                try {
                    isRead = compare(send_msg_time, lastLoginTime);
                    chatAdaper.setIsRead(isRead);
                    chatAdaper.notifyItemChanged(chatAdaper.getItemCount());
                } catch (Exception e) {
                    LogUtil.e("wktime", result + "\n解析失败 " + e.toString());
                }
            } else {
                if (isForeground) {
                    mChatLastLoginTime.setText(lastLoginTime);
                }
            }
            super.onPostExecute(result);
        }
    }

    public boolean compare(String time1, String time2) throws ParseException {
        //如果想比较日期则写成"yyyy-MM-dd"就可以了
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        //将字符串形式的时间转化为Date类型的时间
        Date a = sdf.parse(time1);
        Date b = sdf.parse(time2);
        //Date类的一个方法，如果a早于b返回true，否则返回false
        if (a.before(b))
            return true;
        else
            return false;
        /*
         * 如果你不喜欢用上面这个太流氓的方法，也可以根据将Date转换成毫秒
		 if(a.getTime()-b.getTime()<0)
		 return true;
		 else
		 return false;
		 */
    }

    public class TuringTask extends AsyncTask<Integer, Integer, String> {

        private String result;

        @Override
        protected void onPreExecute() {
            result = "";
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer[] params) {
            RequestBody requestBody = new FormBody.Builder()
                    .add("key", "0aad89b1e6b74ab5932fe584269ea531")
                    .add("info", msgDetail)
                    .add("userid", phoneAlias)
                    .build();
            try {
                Request request = new Request.Builder()
                        .url("http://www.tuling123.com/openapi/api")
                        .post(requestBody)
                        .build();
                Response response = Common.mClient.newCall(request).execute();
                result = response.body().string();
            } catch (Exception e) {
                Log.e("turing", "url  " + e.toString());
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
            if (Common.getSharedPreference().getBoolean(SettingUtil.SAVE_MSG_KEY, false)) {
                sendPerson = phoneAlias;
                phoneAlias = getResources().getString(R.string.Turing);
                msgDetail = result;
                new wkTask.msg_upload(getActivity(), phoneAlias, sendPerson, msgDetail).execute();
            }

        }
    }
}
