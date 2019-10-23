import LocationService.Location;
import LocationService.LocationIPGeolocation;

public class Test {

  public static void main (String[] args) {
    LocationIPGeolocation loc = new LocationIPGeolocation ();
    for (Location l : loc.getLocations ("netflix.com.sg")) {
      System.out.println (l);
    }
  }
}
