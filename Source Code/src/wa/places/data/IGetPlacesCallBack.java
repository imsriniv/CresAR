package wa.places.data;

import java.util.List;

import android.location.Location;

public interface IGetPlacesCallBack {

	public void update(List<Location> places);
	
}
