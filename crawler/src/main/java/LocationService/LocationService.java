package LocationService;

public class LocationService {

  private LocationServiceInterface locationInterface = new LocationIPGeolocation ();

  public Location[] getLocations (String url) {
    return locationInterface.getLocations (url);
  }
}
