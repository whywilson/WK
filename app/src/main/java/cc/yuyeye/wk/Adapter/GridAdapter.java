package cc.yuyeye.wk.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import cc.yuyeye.wk.R;

public class GridAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mList;
    private ImageLoader mImageLoader;

    public GridAdapter(Context context, List<String> list, ImageLoader imageLoader) {
        mContext = context;
        mList = list;
        mImageLoader = imageLoader;
    }

    @Override
    public int getCount() {
        int ret = 0;
        if (mList != null) {
            ret = mList.size();
        }
        return ret;
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View ret;
        if (view != null) {
            ret = view;
        } else {
            ret = LayoutInflater.from(mContext).inflate(R.layout.image_item, null);
        }

        ViewHolder vHolder = (ViewHolder) ret.getTag();

        if (vHolder == null) {
            vHolder = new ViewHolder();
            vHolder.mNetworkImageView = (NetworkImageView) ret.findViewById(R.id.image);
            vHolder.mNetworkImageView.setMinimumHeight(vHolder.mNetworkImageView.getWidth());
            ret.setTag(vHolder);
        }        
        vHolder.mNetworkImageView.setImageUrl(mList.get(i), mImageLoader);

        return ret;
    }

    private class ViewHolder {
        public NetworkImageView mNetworkImageView;
    }
}
