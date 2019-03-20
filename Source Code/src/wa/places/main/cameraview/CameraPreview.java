package wa.places.main.cameraview;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {

	private SurfaceHolder mHolder;
	private Camera mCamera;

	public CameraPreview(Context context) {
		super(context);

		
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		if (mHolder.getSurface() == null) { // preview surface does not exist
			return;
		}
		
		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		} 
		
		// set preview size and make any resize, rotate or reformatting changes here 
		// start preview with new settings
		
		try {
			
			Camera.Parameters params = mCamera.getParameters();
			params.setPreviewSize(width, height);
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(this.toString(),
					"Error starting camera preview: " + e.getMessage());
		}

	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			mCamera = Camera.open();
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(this.toString(),
					"Error setting camera preview: " + e.getMessage());
		}

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		
		if (mCamera != null) {
				try {
					mCamera.stopPreview();
				} catch (Exception ignore) {
				}
				try {
					mCamera.release();
				} catch (Exception ignore) {
				}
				mCamera = null;
			}
	}

}
