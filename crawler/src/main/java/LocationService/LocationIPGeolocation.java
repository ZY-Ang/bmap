package LocationService;

import org.json.JSONObject;

/*Max 2 requests/sec*/
public class LocationIPGeolocation implements LocationServiceInterface {

  public static final int HTTP_TOO_MANY_REQUESTS = 429;

  private String host = "http://ipgeolocation.com/";

  @Override
  public String getRequest (String ip) {
    return host + ip;
  }

  @Override
  public Location getLocation (String data) {
    JSONObject jsonData = new JSONObject (data);
    return new Location (jsonData.getString ("ip"), jsonData.getString ("country")
        , jsonData.getString ("coords"));
  }

}
