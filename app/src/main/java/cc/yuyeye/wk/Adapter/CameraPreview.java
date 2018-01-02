package cc.yuyeye.wk.Adapter;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**AbasicCamerapreviewclass*/
public class CameraPreview extends SurfaceView implements
SurfaceHolder.Callback {
	private static final String TAG="CameraPreview";
	private SurfaceHolder mHolder;
	private Camera mCamera;

	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		

//InstallaSurfaceHolder.Callbacksowegetnotifiedwhenthe
//underlyingsurfaceiscreatedanddestroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
//deprecatedsetting,butrequiredonAndroidversionspriorto3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder){
//TheSurfacehasbeencreated,nowtellthecamerawheretodrawthe
//preview.
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
			mCamera.setDisplayOrientation(90);
		} catch (IOException e) {
			Log.d(TAG, "Errorsettingcamerapreview:" + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder){
//empty.TakecareofreleasingtheCamerapreviewinyouractivity.
	}

	public void surfaceChanged(SurfaceHolder holder,int format,int w,int h){
//Ifyourpreviewcanchangeorrotate,takecareofthoseeventshere.
//Makesuretostopthepreviewbeforeresizingorreformattingit.

		if (mHolder.getSurface() == null) {
//previewsurfacedoesnotexist
			return;
		}

//stoppreviewbeforemakingchanges
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
//ignore:triedtostopanon-existentpreview
		}

//setpreviewsizeandmakeanyresize,rotateor
//reformattingchangeshere

//startpreviewwithnewsettings
		try{
		mCamera.setPreviewDisplay(mHolder);
		mCamera.startPreview();

		}catch(Exception e){
			Log.d(TAG, "Errorstartingcamerapreview:" + e.getMessage());
		}
	}
}
