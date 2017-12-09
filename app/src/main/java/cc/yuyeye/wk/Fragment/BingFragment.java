package cc.yuyeye.wk.Fragment;

import android.graphics.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import cc.yuyeye.wk.*;
import cc.yuyeye.wk.Adapter.*;
import cc.yuyeye.wk.Util.*;
import com.afollestad.materialdialogs.*;
import com.android.volley.toolbox.*;
import java.util.*;

import cc.yuyeye.wk.R;

import static cc.yuyeye.wk.MainActivity.mImageCache;
import static cc.yuyeye.wk.MainActivity.mImageLoader;
import static cc.yuyeye.wk.MainActivity.mQueue;
import static cc.yuyeye.wk.Util.Images.imageUrls;
import java.text.*;

public class BingFragment extends Fragment {
    private String currentImageUrl = "";
//    private ImageView showImage;
    private GridView mGridView;
    private GridAdapter gridAdapter;
	private RecyclerView mRecyclerView;
	private BingAdapter bingAdapter;
    public static String[] imageUrls;
//    private SquareRelativeLayout showImageLayout;
    public Bitmap selectBitmap;

    public static BingFragment newInstance() {
        return new BingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        initData();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_bing, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.bing_recyclerview);
		
		mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3, OrientationHelper.VERTICAL, false));
		mRecyclerView.setAdapter(bingAdapter);
		final MaterialDialog bingDialog = new MaterialDialog.Builder(getActivity())
			.title("Bing")
			.customView(R.layout.image_item,false)
			.build();
		bingAdapter.setOnItemClickListener(new BingAdapter.OnItemClickListener(){

				@Override
				public void onItemClick(int position, BingAdapter.ViewHolder vh)
				{
					currentImageUrl = imageUrls[position];
					selectBitmap = mImageCache.getBitmap("#W0#H0" + imageUrls[position]);
					ImageDialogFragment fragment = ImageDialogFragment.newInstance(selectBitmap);
					fragment.show(getActivity().getFragmentManager(), "blur_image");
					
					
//					View view = bingDialog.getCustomView();
//					NetworkImageView image = (NetworkImageView) view.findViewById(R.id.image);
//					image.setImageBitmap(mImageCache.getBitmap("#W0#H0" + imageUrls[position]));
//					bingDialog.show();
				}
			});

        super.onActivityCreated(savedInstanceState);
    }

    private void initData() {
        List<String> list = new ArrayList<>();
        imageUrls = Images.imageUrls;
        getBingImageUrls();
        Collections.addAll(list, imageUrls);
		bingAdapter = new BingAdapter(getActivity(), list, mImageLoader);
        gridAdapter = new GridAdapter(getActivity(), list, mImageLoader);
    }

    public void getBingImageUrls() {
        Date dNow = new Date();
        Date dBefore;
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 32; i++) {
            calendar.setTime(dNow);
            calendar.add(Calendar.DAY_OF_MONTH, -i);
            dBefore = calendar.getTime();
			SimpleDateFormat sDateFormat;
			sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.PRC);
            if (i < 30) {
                imageUrls[i] = "https://yuyeye.cc/bing/bing_" + sDateFormat.format(dBefore).substring(0, 10) + ".jpg";
            } else {
                mQueue.getCache().remove("https://yuyeye.cc/bing/bing_" + sDateFormat.format(dBefore).substring(0, 10) + ".jpg");
            }
        }
        currentImageUrl = imageUrls[0];
    }
}
