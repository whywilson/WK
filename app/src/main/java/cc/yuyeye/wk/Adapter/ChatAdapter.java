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
import android.widget.AdapterView.*;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<chatListBean> items;
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
        this.items = mItem;
        mInflater = LayoutInflater.from(context);
        selectAllItem(false);
    }

    public class ViewHolder_MSG_T extends RecyclerView.ViewHolder {
        private TextView wk_chat_message;
        private TextView wk_chat_name;
        private TextView wk_chat_time;
        private NetworkImageView wk_chat_image;
        private AppCompatCheckBox wk_chat_checkbox;
        private RelativeLayout wk_chat_layout;
        private View itemView;
		public chatListBean mItem;
        public int viewType;

        public ViewHolder_MSG_T(View view) {
            super(view);
            this.itemView = view;
			wk_chat_layout = (RelativeLayout) view.findViewById(R.id.chatdialogrightitemRelativeLayout1);
			wk_chat_message = (TextView) view.findViewById(R.id.wk_chat_me_message);
			wk_chat_name = (TextView) view.findViewById(R.id.wk_chat_right_name);
			wk_chat_checkbox = (AppCompatCheckBox) view.findViewById(R.id.chatdialogrightitemCheckBox);
			wk_chat_time = (TextView) view.findViewById(R.id.chatdialogrightTime);
			wk_chat_image = (NetworkImageView) view.findViewById(R.id.wk_chat_image_right);
			wk_chat_image.setDefaultImageResId(R.drawable.doraemon_0);
			wk_chat_message.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!isActionMode) {
							ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
							cmb.setText(wk_chat_message.getText().toString());
							ToastUtil.showSimpleToast(context.getString(R.string.copied), 100);
						} else {
							//setSelectItem(position);
						}
					}
				});
        }
    }
	
	public class ViewHolder_MSG_R extends RecyclerView.ViewHolder {
        private TextView wk_chat_message;
        private TextView wk_chat_name;
        private TextView wk_chat_time;
        private NetworkImageView wk_chat_image;
        private AppCompatCheckBox wk_chat_checkbox;
        private RelativeLayout wk_chat_layout;
        private View itemView;
		public chatListBean mItem;
        public int viewType;

        public ViewHolder_MSG_R(View view) {
            super(view);
            this.itemView = view;
			wk_chat_layout = (RelativeLayout) view.findViewById(R.id.chatdialogleftitemRelativeLayout1);
			wk_chat_message = (TextView) view.findViewById(R.id.wk_chat_message);
			wk_chat_name = (TextView) view.findViewById(R.id.wk_chat_left_name);
			wk_chat_time = (TextView) view.findViewById(R.id.chatdialogleftTime);
			wk_chat_image = (NetworkImageView) view.findViewById(R.id.wk_chat_image_left);
			wk_chat_image.setDefaultImageResId(R.drawable.ali_0);
			wk_chat_checkbox = (AppCompatCheckBox) view.findViewById(R.id.chatdialogleftitemCheckBox);
			wk_chat_message.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!isActionMode) {
							ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
							cmb.setText(wk_chat_message.getText().toString());
							ToastUtil.showSimpleToast(context.getString(R.string.copied), 100);
						} else {
							//setSelectItem(position);
						}
					}
				});
        }
    }
	

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == msgViewType.MSG_SEND) {
			return new ViewHolder_MSG_T(LayoutInflater.from(context).inflate(R.layout.chat_dialog_right_item, parent, false));
        } else {
			return new ViewHolder_MSG_R(LayoutInflater.from(context).inflate(R.layout.chat_dialog_left_item, parent, false));
			
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		switch (holder.getItemViewType()) {
			case msgViewType.MSG_SEND:
				ViewHolder_MSG_T vh = (ViewHolder_MSG_T)holder;
				vh.mItem = items.get(position);
				vh.wk_chat_name.setText(items.get(position).getcSend());
				vh.wk_chat_message.setText(items.get(position).getMsgContent());
				vh.wk_chat_image.setImageUrl(items.get(position).getIconUrl(), loader);
				try {
					chatListBean entity = items.get(position);
					if (items.size() > 1) {
						chatListBean entity_before = items.get(Math.abs(position - 1));
						if (entity.getTime().substring(10, 16).equals(entity_before.getTime().substring(10, 16)) && position != 0) {
							vh.wk_chat_time.setVisibility(TextView.GONE);
						} else {
							if (entity.getTime().substring(0, 10).equals(getCurrentTime().substring(0, 10))) {
								vh.wk_chat_time.setText(entity.getTime().substring(10, 16));
							} else {
								vh.wk_chat_time.setText(entity.getTime().substring(0, 16));
							}
						}
					} else {
						vh.wk_chat_time.setText(entity.getTime().substring(0, 16));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case msgViewType.MSG_COME:
				ViewHolder_MSG_R vhr = (ViewHolder_MSG_R)holder;
				vhr.mItem = items.get(position);
				vhr.mItem = items.get(position);
				vhr.wk_chat_name.setText(items.get(position).getcSend());
				vhr.wk_chat_message.setText(items.get(position).getMsgContent());
				vhr.wk_chat_image.setImageUrl(items.get(position).getIconUrl(), loader);
				try {
					chatListBean entity = items.get(position);
					if (items.size() > 1) {
						chatListBean entity_before = items.get(Math.abs(position - 1));
						if (entity.getTime().substring(10, 16).equals(entity_before.getTime().substring(10, 16)) && position != 0) {
							vhr.wk_chat_time.setVisibility(TextView.GONE);
						} else {
							if (entity.getTime().substring(0, 10).equals(getCurrentTime().substring(0, 10))) {
								vhr.wk_chat_time.setText(entity.getTime().substring(10, 16));
							} else {
								vhr.wk_chat_time.setText(entity.getTime().substring(0, 16));
							}
						}
					} else {
						vhr.wk_chat_time.setText(entity.getTime().substring(0, 16));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
		}

//        if (isActionMode) {
//            holder.wk_chat_checkbox.setVisibility(View.VISIBLE);
//            holder.wk_chat_message.setDuplicateParentStateEnabled(true);
//        } else {
//            holder.wk_chat_checkbox.setVisibility(View.GONE);
//            holder.wk_chat_message.setDuplicateParentStateEnabled(false);
//        }
//        //设置TAG
//        holder.itemView.setTag(position);
//        holder.wk_chat_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                checkBoxMap.put(position, b);
//            }
//        });
//        //设置checkbox状态
//        if (checkBoxMap.get(position) == null) {
//            checkBoxMap.put(position, false);
//        }
//        holder.wk_chat_checkbox.setChecked(checkBoxMap.get(position));
//
//        holder.wk_chat_message.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!isActionMode) {
//                    ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//                    cmb.setText(items.get(position).getMsgContent().trim());
//                    ToastUtil.showSimpleToast(context.getString(R.string.copied), 100);
//                } else {
//                    setSelectItem(position);
//                }
//            }
//        });
//        holder.wk_chat_message.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
////                selectAllItem(false);
////                setActionMode();
////                setSelectItem(position);
////                notifyDataSetChanged();
//                return true;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(chatListBean data) {
        items.add(data);
        notifyItemInserted(items.size());
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
        for (int i = 0; i < items.size(); i++) {
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
            chatDbIdList.remove((Integer) items.get(position).getId());
        } else {
            checkBoxMap.put(position, true);
            chatDialogIdList.add(mChatDialogLists.get(position));
            chatDbIdList.add(items.get(position).getId());
        }
        notifyItemChanged(position);
    }

    public void selectAllItem(Boolean b) {
        for (int i = 0; i < items.size(); i++) {
            checkBoxMap.put(i, b);
            if (b) {
                chatDialogIdList.add(mChatDialogLists.get(i));
                chatDbIdList.add(items.get(i).getId());
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
		//void OnItemClickListener(ViewHolder vh, int position);
        void onItemClickListener(View view, int position);
        boolean onItemLongClickListener(View view, int position);
    }

    public int getItemViewType(int position) {
        chatListBean entity = items.get(position);

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
