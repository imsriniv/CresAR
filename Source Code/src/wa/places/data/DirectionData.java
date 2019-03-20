package wa.places.data;

import android.content.Context;
import android.hardware.SensorManager;

public class DirectionData {

	private  double azimuth;
	private  double pitch;
	private  double roll;
	private  boolean directionFound = false;
	private Context ctx;
	
	public DirectionData()
	{
		
	}
	
	public  void findPhoneOrientation(float[] accelerometerVals, float[] magneticVals)
	{
		float inR[] = new float[9];
		float outR[] = new float[9];
		float I[] = new float[9];
		float orientation[] = new float[3];

		SensorManager.getRotationMatrix(inR, I, accelerometerVals, magneticVals);
		SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
		SensorManager.getOrientation(outR, orientation);

		azimuth = Math.round(Math.toDegrees(orientation[0]));
		azimuth = (azimuth + 360)%360;
		
		pitch = Math.round(Math.toDegrees(orientation[1]));
		roll = Math.round(Math.toDegrees(orientation[2]));
		
		directionFound = true;

		/*GeomagneticField geoField = new GeomagneticField(
		Double.valueOf(lastKnownLocation.getLatitude()).floatValue(),
		Double.valueOf(lastKnownLocation.getLongitude()).floatValue(),
		Double.valueOf(lastKnownLocation.getAltitude()).floatValue(),
		System.currentTimeMillis());
azimuth += geoField.getDeclination();
logText.setText(logText.getText() + " Azimuth-declination " + azimuth);*/

		
	}

	public  double getAzimuth()
	{
		return azimuth;
	}

	public  double getPitch()
	{
		return pitch;
	}
	
	public  double getRoll()
	{
		return roll;
	}
	
	public  boolean isDirectionFound()
	{
		return directionFound;
	}
	
}
