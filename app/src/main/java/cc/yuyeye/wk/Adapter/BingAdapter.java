package cc.yuyeye.wk.Adapter;

import android.content.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import cc.yuyeye.wk.*;
import com.android.volley.toolbox.*;
import java.util.*;


public class BingAdapter extends RecyclerView.Adapter<BingAdapter.ViewHolder> {
	
	private Context mContext;
    private List<String> mList;
    private ImageLoader mImageLoader;
    private LayoutInflater mInflater;
	private OnItemClickListener mItemListener;
    private List<String> mData = new ArrayList<>();

	public BingAdapter(Context context, List<String> list, ImageLoader imageLoader) {
        mContext = context;
		mInflater = LayoutInflater.from(context);
        mList = list;
        mImageLoader = imageLoader;
    }
	
    public BingAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.bing_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
		holder.imageView.setImageUrl(mList.get(position), mImageLoader);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_tittle;
        NetworkImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
			imageView = (NetworkImageView) itemView.findViewById(R.id.bing_image);
			imageView.setOnClickListener(new View.OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						if (mItemListener != null) {
							mItemListener.onItemClick(getLayoutPosition(), ViewHolder.this);
						}
					}
				});
        }
    }
	
	public interface OnItemClickListener {
        void onItemClick(int position, BingAdapter.ViewHolder vh);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemListener = listener;
    }
}
