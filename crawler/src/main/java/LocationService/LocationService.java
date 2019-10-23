package LocationService;

public class LocationService {

  private LocationServiceInterface locationInterface;

  protected Location[] getLocations (String url) {
    return locationInterface.getLocations (url);
  }
}
