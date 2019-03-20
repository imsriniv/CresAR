package wa.places.main;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;



import wa.places.data.DirectionData;
import wa.places.data.ILocationConstants;
import wa.places.data.PlaceManager;
import wa.places.main.cameraview.CameraPreview;
import wa.places.main.subviews.HelpActivity;
import wa.places.main.subviews.SettingsActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener, SensorEventListener, OnClickListener{

	private Location lastKnownLocation;
	private LocationManager locManager;
	private int locationUpdateCount = 0;
	private DirectionData direction;
	private SensorManager sensorManager;
	private float[] accelerometerVals;
	private float[] magneticVals;
	private RelativeLayout placeView;
	private RelativeLayout infoView;
	private Map<String, Location> loc_idMap = new HashMap<String, Location>();
	
	private LayoutInflater inflater;
	
	/** Called when the activity is first created. */
	@Override
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.mainview);
		
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		// Create our Preview view and set it as the content of our activity.
		
		RelativeLayout rootLayout = (RelativeLayout)findViewById(R.id.mainview);
		rootLayout.addView(new CameraPreview(this));
		
		inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View azimuthView = inflater.inflate(R.layout.azimuthview, null);
		azimuthView.bringToFront();
		rootLayout.addView(azimuthView);
		
		placeView = (RelativeLayout)inflater.inflate(R.layout.placelayout, null);
		placeView.bringToFront();
		LayoutParams placeLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		placeLayout.addRule(RelativeLayout.CENTER_IN_PARENT);
		placeView.setLayoutParams(placeLayout);
		rootLayout.addView(placeView);
		
		infoView = (RelativeLayout)inflater.inflate(R.layout.infolayout, null);
		infoView.bringToFront();
		LayoutParams infolayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		infolayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		infoView.setLayoutParams(infolayout);
		rootLayout.addView(infoView);
		
		requestLocUpdate();
	}

	
	@Override
	public void onBackPressed() {
	
		super.onBackPressed();
		locManager.removeUpdates(this);
		unregisterSensors();
	}

	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		locManager.removeUpdates(this);
		unregisterSensors();
	}

	@Override
	protected void onPause() {
		
		super.onPause();
		locManager.removeUpdates(this);
		unregisterSensors();
	}

	
	@Override
	protected void onResume() {
		
		super.onResume();
		if(checkGPSEnable())
		{
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			registerSensors();
			locationUpdateCount = 0;
		}
		
		/*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String locIteration = prefs.getString("listPref", "Not found");
		Toast.makeText(this, locIteration, Toast.LENGTH_LONG).show();*/
		
		//registerSensors();
		
		placeView.removeAllViews();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.placesmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.help:
			
			Intent helpintent = new Intent(this, HelpActivity.class);
			startActivity(helpintent);
			break;
		
		case R.id.settings:

			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			
			break;
		default:
			break;
		}

		return true;
	}

	public void onLocationChanged(Location location) {

		
		++locationUpdateCount;
		
		if(locationUpdateCount<3)
		{
			if(lastKnownLocation == null || lastKnownLocation.getAccuracy()>location.getAccuracy())
			{
				lastKnownLocation = location;
			}	
		}
		else
		{
			if(direction!=null && direction.isDirectionFound())
			{

				//locManager.removeUpdates(this);
				locationUpdateCount=0;

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				String radius = prefs.getString("listPref", "100");
				String angle=prefs.getString("listpref1","15");
				
				
				// TODO: NOT RIGHT
				PlaceManager manager = null;
				try {
					manager = new PlaceManager(this);
					manager.open();
					Collection<Location> locs = manager.getPlaces(direction.getAzimuth(), lastKnownLocation, Float.valueOf(radius), Double.valueOf(angle));
					manager.close();
					
					int prevTextId = 0;
					
					placeView.removeAllViews();
					//infoView.removeAllViews();
					loc_idMap.clear();
					
					for(Location loc:locs)
					{
						int currentId = Integer.parseInt(loc.getExtras().getString(ILocationConstants._id));
						
						// text view for places
						TextView placeTextView = new TextView(this);
						placeTextView.setTypeface(null,Typeface.BOLD);
						placeTextView.setTextColor(Color.WHITE);
						placeTextView.setTextSize((float) 14);
						placeTextView.setId(currentId);
						placeTextView.setCompoundDrawablesWithIntrinsicBounds(0, getResources().getIdentifier("marker", "drawable", getPackageName()), 0, 0);
						
						String name = loc.getExtras().getString(ILocationConstants.name);
						placeTextView.setText(name);
						LayoutParams textLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
						placeTextView.setLayoutParams(textLayoutParams);
						
						
						if(prevTextId>0){
							
							textLayoutParams.addRule(RelativeLayout.RIGHT_OF, prevTextId);
							textLayoutParams.leftMargin = 10;
							placeTextView.requestLayout();
							
						}
						
						prevTextId = currentId;
						placeView.addView(placeTextView);
						placeTextView.setOnClickListener(this);
							
						loc_idMap.put(String.valueOf(currentId), loc);
						
					}
					
				} catch (SQLException e) {

					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
					if(manager!=null) manager.close();

				} catch (IOException e) {

					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
					if(manager!=null) manager.close();
				}

			}
			else
			{
				Toast.makeText(this, "Direction not found please try again", Toast.LENGTH_LONG).show();
			}
		}
		
	}

	public void onProviderDisabled(String provider) {
		
		Toast.makeText(this, "Provider " + provider + "disabled ", Toast.LENGTH_LONG).show();
	}

	public void onProviderEnabled(String provider) {
		
		Toast.makeText(this, "Provider " + provider + "turned on", Toast.LENGTH_LONG).show();

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		
		//Toast.makeText(this, "Provider " + provider + "status changed", Toast.LENGTH_LONG).show();

	}

	private boolean checkGPSEnable()
	{
		boolean enable = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		if(!enable)
		{
			Toast.makeText(this, "Enable GPS", Toast.LENGTH_LONG).show();
		}
		
		return enable;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// not necessary now
		
	}

	public void onSensorChanged(SensorEvent event) {
		
		switch (event.sensor.getType()) {

		case Sensor.TYPE_ACCELEROMETER:

			accelerometerVals = event.values.clone();
			break;
		
		case Sensor.TYPE_MAGNETIC_FIELD:

			magneticVals = event.values.clone();
			break;

		default:
			break;
		}
		
		
		if(accelerometerVals!=null && magneticVals!=null)
		{
			direction = new DirectionData();
			direction.findPhoneOrientation(accelerometerVals, magneticVals);
			
			TextView azimuthTextView = (TextView)findViewById(R.id.azimuthtext);
			
			//azimuthTextView.setText("Azimuth: " + direction.getAzimuth() + " Pitch: " + direction.getPitch() + " Roll: " + direction.getRoll());
			if(direction.getAzimuth()>0 && direction.getAzimuth()<=30)
			{
			 			 
			 azimuthTextView.setText("N");
			 }
			if(direction.getAzimuth()>30 && direction.getAzimuth()<=60)
			 {
			 azimuthTextView.setText("NE");
			 }
			 if(direction.getAzimuth()>60 && direction.getAzimuth()<=115)
			 {
			 azimuthTextView.setText("E");
			 }
			 if(direction.getAzimuth()>115 && direction.getAzimuth()<=150)
			 {
			 azimuthTextView.setText("SE");
			 }
			 if(direction.getAzimuth()>150 && direction.getAzimuth()<=205)
			 {
			 azimuthTextView.setText("S");
			 }
			 if(direction.getAzimuth()>205 && direction.getAzimuth()<=240)
			 {
			 azimuthTextView.setText("SW");
			 }
			if(direction.getAzimuth()>240 && direction.getAzimuth()<=290)
			{
			 azimuthTextView.setText("W");
			 }
			 if(direction.getAzimuth()>290 && direction.getAzimuth()<=330)
			 {
			 azimuthTextView.setText("NW");
			}
			accelerometerVals = null;
			magneticVals = null;
		}
		
	}
	
	
	private void unregisterSensors()
	{
		 sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
		 sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
	}
	
	private void registerSensors()
	{
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
	}


	private void requestLocUpdate()
	{
		if(checkGPSEnable())
		{
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			registerSensors();
			locationUpdateCount = 0;
		}
	}
	
	public void onClick(View v) {
		
		
		Button  infoButton=new Button(this);
		ImageView close=new ImageView(this);
		
		
		ImageView testImage=new ImageView(this);
		testImage.setBackgroundResource(getResources().getIdentifier("testimage", "drawable", getPackageName()));
		
		
		
		close.setBackgroundResource(getResources().getIdentifier("closebutton", "drawable", getPackageName()));
		
		Location currentLoc = loc_idMap.get(String.valueOf(v.getId()));
		infoButton.setText(currentLoc.getExtras().getString(ILocationConstants.desc));
		//Toast.makeText(this, ""+String.valueOf(v.getId()), Toast.LENGTH_LONG).show();
		//Toast.makeText(this, ""+(Location)filteredPlacesMap.get(String.valueOf(v.getId())), Toast.LENGTH_LONG).show();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		infoButton.setWidth(metrics.widthPixels);
		infoButton.setHeight((metrics.heightPixels)/3);
		infoButton.setBackgroundColor(Color.WHITE);
		
		
		LayoutParams infoLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		infoButton.setLayoutParams(infoLayoutParams);
		infoButton.requestLayout();
		infoView.setVisibility(View.VISIBLE);
		
		infoView.addView(infoButton);
		infoButton.setTypeface(null,Typeface.BOLD);
		infoView.addView(close);
	    
		//infoView.addView(testImage);
		
		locManager.removeUpdates(this);
		unregisterSensors();
		
		close.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				infoView.setVisibility(4);
				//placeView.removeAllViews();
				requestLocUpdate();
				
			}
		});
		
	}
}