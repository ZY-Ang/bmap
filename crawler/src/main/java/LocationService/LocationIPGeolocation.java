package LocationService;

import org.json.JSONObject;

/*Max 2 requests/sec*/
public class LocationIPGeolocation implements LocationServiceInterface {

  private String host = "http://ipgeolocation.com/";

  @Override
  public String getRequest (String ip) {
    return host + ip;
  }

  @Override
  public Location parseLocation (String data) {
    JSONObject jsonData = new JSONObject (data);
    return new Location (jsonData.getString ("ip"), jsonData.getString ("country")
        , jsonData.getString ("coords"));
  }

}
