package cc.yuyeye.wk.Adapter;

import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.yuyeye.wk.Common;
import cc.yuyeye.wk.DB.chatListBean;
import cc.yuyeye.wk.R;
import cc.yuyeye.wk.Util.BitmapCache;
import cc.yuyeye.wk.Util.ToastUtil;

import static cc.yuyeye.wk.Fragment.ChatFragment.mChatDialogLists;
import static cc.yuyeye.wk.Fragment.ChatFragment.mChatMsgView;
import static cc.yuyeye.wk.Fragment.ChatFragment.mChatTools;
import static cc.yuyeye.wk.MainActivity.getCurrentTime;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private List<chatListBean> mItem;
    private Context context;
    private boolean isActionMode = false;
    private LayoutInflater mInflater;
    public RequestQueue requestQueue = Volley.newRequestQueue(Common.getContext());
    public ImageLoader loader = new ImageLoader(requestQueue, new BitmapCache());

    private Map<Integer, Boolean> checkBoxMap = new HashMap<>();
    private List<chatListBean> chatDialogIdList = new ArrayList<>();
    private List<Integer> chatDbIdList = new ArrayList<>();

    private RecyclerViewOnItemClickListener onItemClickListener;

    public ChatAdapter(Context context, List<chatListBean> mItem) {
        this.context = context;
        this.mItem = mItem;
        mInflater = LayoutInflater.from(context);
        selectAllItem(false);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView wk_chat_message;
        private TextView wk_chat_name;
        private TextView wk_chat_time;
        private NetworkImageView wk_chat_image;
        private AppCompatCheckBox wk_chat_checkbox;
        private RelativeLayout wk_chat_layout;
        private View itemView;
        private String iconUrl;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        ViewHolder viewHolder;
        if (viewType == msgViewType.MSG_SEND) {
            view = mInflater.inflate(R.layout.chat_dialog_right_item, parent, false);
            viewHolder = new ViewHolder(view);
            viewHolder.wk_chat_layout = (RelativeLayout) view.findViewById(R.id.chatdialogrightitemRelativeLayout1);
            viewHolder.wk_chat_message = (TextView) view.findViewById(R.id.wk_chat_me_message);
            viewHolder.wk_chat_name = (TextView) view.findViewById(R.id.wk_chat_right_name);
            viewHolder.wk_chat_checkbox = (AppCompatCheckBox) view.findViewById(R.id.chatdialogrightitemCheckBox);
            viewHolder.wk_chat_time = (TextView) view.findViewById(R.id.chatdialogrightTime);
            viewHolder.wk_chat_image = (NetworkImageView) view.findViewById(R.id.wk_chat_image_right);
            viewHolder.wk_chat_image.setDefaultImageResId(R.drawable.doraemon_0);
        } else {
            view = mInflater.inflate(R.layout.chat_dialog_left_item, parent, false);
            viewHolder = new ViewHolder(view);
            viewHolder.wk_chat_layout = (RelativeLayout) view.findViewById(R.id.chatdialogleftitemRelativeLayout1);
            viewHolder.wk_chat_message = (TextView) view.findViewById(R.id.wk_chat_message);
            viewHolder.wk_chat_name = (TextView) view.findViewById(R.id.wk_chat_left_name);
            viewHolder.wk_chat_time = (TextView) view.findViewById(R.id.chatdialogleftTime);
            viewHolder.wk_chat_image = (NetworkImageView) view.findViewById(R.id.wk_chat_image_left);
            viewHolder.wk_chat_image.setDefaultImageResId(R.drawable.ali_0);
            viewHolder.wk_chat_checkbox = (AppCompatCheckBox) view.findViewById(R.id.chatdialogleftitemCheckBox);
        }
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChatAdapter.ViewHolder holder, final int position) {
        holder.wk_chat_name.setText(mItem.get(position).getcSend());
        holder.wk_chat_message.setText(mItem.get(position).getMsgContent());
        holder.wk_chat_image.setImageUrl(mItem.get(position).getIconUrl(), loader);
        try {
            chatListBean entity = mItem.get(position);
            if (mItem.size() > 1) {
                chatListBean entity_before = mItem.get(Math.abs(position - 1));
                if (entity.getTime().substring(10, 16).equals(entity_before.getTime().substring(10, 16)) && position != 0) {
                    holder.wk_chat_time.setVisibility(TextView.GONE);
                } else {
                    if (entity.getTime().substring(0, 10).equals(getCurrentTime().substring(0, 10))) {
                        holder.wk_chat_time.setText(entity.getTime().substring(10, 16));
                    } else {
                        holder.wk_chat_time.setText(entity.getTime().substring(0, 16));
                    }
                }
            } else {
                holder.wk_chat_time.setText(entity.getTime().substring(0, 16));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isActionMode) {
            holder.wk_chat_checkbox.setVisibility(View.VISIBLE);
            holder.wk_chat_message.setDuplicateParentStateEnabled(true);
        } else {
            holder.wk_chat_checkbox.setVisibility(View.GONE);
            holder.wk_chat_message.setDuplicateParentStateEnabled(false);
        }
        //设置TAG
        holder.itemView.setTag(position);
        holder.wk_chat_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkBoxMap.put(position, b);
            }
        });
        //设置checkbox状态
        if (checkBoxMap.get(position) == null) {
            checkBoxMap.put(position, false);
        }
        holder.wk_chat_checkbox.setChecked(checkBoxMap.get(position));

        holder.wk_chat_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isActionMode) {
                    ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(mItem.get(position).getMsgContent().trim());
                    ToastUtil.showSimpleToast(context.getString(R.string.copied), 100);
                } else {
                    setSelectItem(position);
                }
            }
        });
        holder.wk_chat_message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                selectAllItem(false);
//                setActionMode();
//                setSelectItem(position);
//                notifyDataSetChanged();
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mItem.size();
    }

    public void addItem(chatListBean data) {
        mItem.add(data);
        notifyItemInserted(mItem.size());
    }

    @Override
    public void onClick(View view) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClickListener(view, (Integer) view.getTag());
        }
    }

    @Override
    public boolean onLongClick(View view) {
        selectAllItem(false);
        return (onItemClickListener != null) && onItemClickListener.onItemLongClickListener(view, (Integer) view.getTag());
    }

    public Boolean getActionMode() {
        return isActionMode;
    }

    public void setActionMode() {
        isActionMode = !isActionMode;
        showActionBar(isActionMode);
    }

    public void showActionBar(Boolean b) {
        if (b) {
            if (mChatMsgView.getVisibility() == View.VISIBLE) {
                final Animation animFLOut = AnimationUtils.loadAnimation(Common.getContext(), R.anim.fade_out);
                final Animation animFLIn = AnimationUtils.loadAnimation(Common.getContext(), R.anim.fade_in);
                mChatMsgView.startAnimation(animFLOut);
                animFLOut.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation p1) {
                    }

                    @Override
                    public void onAnimationEnd(Animation p1) {
                        mChatMsgView.setVisibility(View.GONE);
                        mChatTools.setVisibility(LinearLayout.VISIBLE);
                        mChatTools.startAnimation(animFLIn);
                    }

                    @Override
                    public void onAnimationRepeat(Animation p1) {
                    }
                });
            }
        } else {
            final Animation animFRIn = AnimationUtils.loadAnimation(Common.getContext(), R.anim.fade_in);
            final Animation animFROut = AnimationUtils.loadAnimation(Common.getContext(), R.anim.fade_out);
            mChatTools.startAnimation(animFROut);
            animFROut.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation p1) {
                }

                @Override
                public void onAnimationEnd(Animation p1) {
                    mChatTools.setVisibility(View.GONE);
                    mChatMsgView.setVisibility(View.VISIBLE);
                    mChatMsgView.startAnimation(animFRIn);
                }

                @Override
                public void onAnimationRepeat(Animation p1) {
                }
            });
        }
    }

    public void initMap() {
        for (int i = 0; i < mItem.size(); i++) {
            checkBoxMap.put(i, false);
        }
        chatDbIdList.clear();
        chatDialogIdList.clear();
    }

    //点击item选中CheckBox
    public void setSelectItem(int position) {
        //对当前状态取反
        if (checkBoxMap.get(position)) {
            checkBoxMap.put(position, false);
            chatDialogIdList.remove(mChatDialogLists.get(position));
            chatDbIdList.remove((Integer) mItem.get(position).getId());
        } else {
            checkBoxMap.put(position, true);
            chatDialogIdList.add(mChatDialogLists.get(position));
            chatDbIdList.add(mItem.get(position).getId());
        }
        notifyItemChanged(position);
    }

    public void selectAllItem(Boolean b) {
        for (int i = 0; i < mItem.size(); i++) {
            checkBoxMap.put(i, b);
            if (b) {
                chatDialogIdList.add(mChatDialogLists.get(i));
                chatDbIdList.add(mItem.get(i).getId());
            } else {
                chatDialogIdList.clear();
                chatDbIdList.clear();
            }
            notifyDataSetChanged();
        }

    }

    //返回集合给MainTabActivity
    public Map<Integer, Boolean> getCheckBoxMap() {
        return checkBoxMap;
    }

    public List<Integer> getChatDbId() {
        return chatDbIdList;
    }

    public List<chatListBean> getChatDialogId() {
        return chatDialogIdList;
    }

    public void setRecyclerViewOnItemClickListener(RecyclerViewOnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    //接口回调设置点击事件
    public interface RecyclerViewOnItemClickListener {
        void onItemClickListener(View view, int position);

        boolean onItemLongClickListener(View view, int position);
    }

    public int getItemViewType(int position) {
        chatListBean entity = mItem.get(position);

        if (entity.isMeSend()) {
            return msgViewType.MSG_SEND;
        } else {
            return msgViewType.MSG_COME;
        }
    }

    interface msgViewType {
        int MSG_SEND = 0;
        int MSG_COME = 1;
    }
}
