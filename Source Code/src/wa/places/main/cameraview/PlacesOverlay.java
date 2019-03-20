package wa.places.main.cameraview;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.location.Location;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class PlacesOverlay extends SurfaceView {

	private SurfaceHolder mHolder;
	private List<Location> places;
	private Context ctx;
	
	public PlacesOverlay(Context context, List<Location> places) {
		super(context);
	
		this.ctx = context;
		mHolder = getHolder();
		mHolder.setFormat(PixelFormat.TRANSLUCENT);
		this.places=places;
		
	}

	@Override
	public void draw(Canvas canvas) {
		
		super.draw(canvas);
		Toast.makeText(ctx, places.size(), Toast.LENGTH_LONG).show();
		
	}

	

	

}
