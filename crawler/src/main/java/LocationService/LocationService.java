package LocationService;

public class LocationService {

  private static LocationServiceInterface locationInterface = new LocationIPGeolocation();

  public static Location[] getLocations (String url) {
    return locationInterface.getLocations(url);
  }
}
