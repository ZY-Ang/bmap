package LocationService;

import java.util.ArrayList;
import java.util.Map;

public class LocationService {

  public LocationService () {
    locationInterface = new LocationIPGeolocation ();

  }

  private static Map<String, String> countries;
  private LocationServiceInterface locationInterface;

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
