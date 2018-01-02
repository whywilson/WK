package cc.yuyeye.wk.Fragment;
import android.content.*;
import android.content.pm.*;
import android.hardware.*;
import android.support.v4.app.*;
import cc.yuyeye.wk.Adapter.*;
import android.view.*;
import android.os.*;
import android.widget.*;
import cc.yuyeye.wk.R;
import java.io.*;
import android.view.View.*;

public class CameraFragment extends Fragment {
	private boolean isForeground;
	private Camera mCamera;
	private CameraPreview mPreview;
	int frontIndex =-1;
	int backIndex = -1;
	int cameraCount = Camera.getNumberOfCameras();
	private static final int FRONT = 1;//前置摄像头标记
    private static final int BACK = 2;//后置摄像头标记
    private int currentCameraType = -1;//当前打开的摄像头标记
	
	public static CameraFragment newInstance() {
        return new CameraFragment();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tab_camera, container, false);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		isForeground = isVisibleToUser;
		super.setUserVisibleHint(isVisibleToUser);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Toast.makeText(getActivity(), "OnAvtivityCreate", Toast.LENGTH_SHORT).show();
		mCamera = getCameraInstance();
		mPreview = new CameraPreview(getActivity(), mCamera);
		FrameLayout preview = (FrameLayout)getActivity().findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		
		preview.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1) {
					try {
						changeCamera();
					} catch (IOException e) {}
				}
			});
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onPause() {
		//mCamera.release();
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		mCamera.release();
		super.onDestroyView();
	}

	private void changeCamera() throws IOException{
        mCamera.stopPreview();
        mCamera.release();
        if(currentCameraType == FRONT){
            mCamera = mCamera.open(BACK);
        }else if(currentCameraType == BACK){
            mCamera = mCamera.open(FRONT);
        }
        mCamera.setPreviewDisplay(mPreview.getHolder());
        mCamera.startPreview();
    }

	public static Camera getCameraInstance() {
		Camera c=null;
		try {
			c = Camera.open();//attempttogetaCamerainstance
		} catch (Exception e) {
//Cameraisnotavailable(inuseordoesnotexist)
		}
		return c;//returnsnullifcameraisunavailable
	}

	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}
}
