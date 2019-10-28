package LocationService;

import java.util.ArrayList;

public class LocationService {

  private LocationServiceInterface locationInterface = new LocationIPGeolocation ();

  public void setLocationInterface (LocationServiceInterface locationInterface) {
    this.locationInterface = locationInterface;
  }

  public Location[] getLocations (String url) {
    ArrayList<Location> locations = new ArrayList<> ();
    for (String ip : locationInterface.getIps (url)) {
      Location location = this.getLocation (ip);
      if (location == null) {
        System.err.printf ("Could not locate %s\n", ip);
        continue;
      }
      locations.add (location);
    }
    return locations.toArray (new Location[0]);
  }

  private Location getLocation (String ip) {
    return locationInterface.getLocation (ip);
  }
}
