package cc.yuyeye.wk.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import cc.yuyeye.wk.MainActivity;
import cc.yuyeye.wk.R;
import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;
import android.view.View.*;
import cc.yuyeye.wk.Util.*;


public class ImageDialogFragment extends BlurDialogFragment {

    private Bitmap image_bitmap;
    private int imageUrl_index;

    public static ImageDialogFragment newInstance(Bitmap bitmap) {
        ImageDialogFragment fragment = new ImageDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("bitmap", bitmap);
        fragment.setArguments(args);
        return fragment;
    }

    public static ImageDialogFragment newInstance(int url) {
        ImageDialogFragment fragment = new ImageDialogFragment();
        Bundle args = new Bundle();
        args.putInt("imageUrl_index", url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        Bundle args = getArguments();
        image_bitmap = args.getParcelable("bitmap");
        imageUrl_index = args.getInt("imageUrl_index");
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.image_dialog_fragment, null);
        final ImageView imageView = (ImageView) view.findViewById(R.id.dialog_image);
        imageView.setImageBitmap(MainActivity.mImageCache.getBitmap("#W0#H0" + BingFragment.imageUrls[imageUrl_index]));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUrl_index < BingFragment.imageUrls.length) {
                    imageUrl_index++;
                    imageView.setImageBitmap(MainActivity.mImageCache.getBitmap("#W0#H0" + BingFragment.imageUrls[imageUrl_index]));
                } else {
                    imageUrl_index = 0;
                }
            }
        });
		imageView.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View p1)
				{
					WkFragment.wkSetWallpaper(MainActivity.mImageCache.getBitmap("#W0#H0" + BingFragment.imageUrls[imageUrl_index]));
					ToastUtil.showToast("Set wallpaper sucessfully!");
					getActivity().finish();
					return true;
				}
			});
        builder.setView(view);
        return builder.create();
    }

    @Override
    protected boolean isActionBarBlurred() {
        return true;
    }

}
