package wa.places.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

public class PlaceManager implements ILocationConstants{

	private SQLiteDatabase database;
	private DbHelper dbHelper;
	private String allColumns[] = new String[]{_id, name, desc, types, vicinity,lat,lng,accuracy,bearing,altitude,speed,time}; 
			
	
	public PlaceManager(Context context) {
		dbHelper = new DbHelper(context);
	}

	public void open() throws SQLException, IOException {
		database = dbHelper.openDataBase();
	}

	public void close() {
		dbHelper.close();
	}

	public Collection<Location> getPlaces(double direction, Location currentLocation, float radius,double angle)
	{
		List<Location> places = getPlaces();
		double currentLat = currentLocation.getLatitude();
		double currentLng = currentLocation.getLongitude();
		float result[] = new float[3];
		
		Map<String, Location> filteredPlacesMap = new HashMap<String, Location>();
		
		for(Location loc: places)
		{
			Location.distanceBetween(currentLat, currentLng, loc.getLatitude(), loc.getLongitude(), result);
			
			double locAzimuth = (result[1] + 360)%360;
			String key = loc.getExtras().getString(ILocationConstants.name);
			
			if(!((locAzimuth < (direction -angle)) || (locAzimuth > (direction + angle))) && (result[0]<=radius))
			{
				filteredPlacesMap.put(key, loc);
			}
		}
	
		return filteredPlacesMap.values();
		
	}
	
	
	public List<Location> getPlaces()
	{
		List<Location> places = new ArrayList<Location>();
		
		Cursor placesCursor = database.query(IDBConstants.PLACETABLE, allColumns, null, null, null, null, null);
		
		int rowCount = placesCursor.getCount();
		
		for(int i=0; i<rowCount; i++)
		{
			placesCursor.moveToNext();
			places.add(cursorToLocation(placesCursor));
		}
		
		placesCursor.close();
		return places;
	}
	
	private Location cursorToLocation(Cursor c)
	{
		Location loc = new Location(LocationManager.GPS_PROVIDER);
		loc.setExtras(new Bundle());
		
		loc.getExtras().putString(vicinity, c.getString(c.getColumnIndex(vicinity)));
		loc.getExtras().putString(types, c.getString(c.getColumnIndex(types)));
		loc.getExtras().putString(name, c.getString(c.getColumnIndex(name)).trim());
		loc.getExtras().putString(desc, c.getString(c.getColumnIndex(desc)));
		loc.getExtras().putString(_id, c.getString(c.getColumnIndex(_id)));
		
		loc.setLatitude(Double.valueOf(c.getString(c.getColumnIndex(lat))));
		loc.setLongitude(Double.valueOf(c.getString(c.getColumnIndex(lng))));
		loc.setAccuracy(Float.valueOf(c.getString(c.getColumnIndex(accuracy))));
		loc.setTime(Long.parseLong(c.getString(c.getColumnIndex(time))));
		loc.setAltitude(Double.valueOf(c.getString(c.getColumnIndex(altitude))));
		loc.setBearing(Float.valueOf(c.getString(c.getColumnIndex(bearing))));
		loc.setSpeed(Float.valueOf(c.getString(c.getColumnIndex(speed))));
		
		return loc;
	}
	
	
	public String addPlace(Location place)
	{
		long insertId = database.insert(IDBConstants.PLACETABLE, null, locationToContentValues(place));
		
		return String.valueOf(insertId);
	}
	
	private ContentValues locationToContentValues(Location place)
	{
		ContentValues values = new ContentValues();
		values.put(lat, place.getLatitude());
		values.put(lng, place.getLongitude());
		values.put(accuracy, place.getAccuracy());
		values.put(time, place.getTime());
		values.put(altitude, place.getAltitude());
		values.put(bearing, place.getBearing());
		values.put(speed, place.getSpeed());
		values.put(vicinity, place.getExtras().getString(vicinity));
		values.put(types, place.getExtras().getString(types));
		values.put(name, place.getExtras().getString(name));
		values.put(desc, place.getExtras().getString(desc));
		
		return values;
	}
	
}
