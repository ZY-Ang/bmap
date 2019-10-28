package LocationService;

import org.json.JSONObject;

/*Max 1000/day https://api.ipgeolocation.io/ipgeo?apiKey=298fc8e3058d4c21a028c55f82608196&ip=1.1.1.1*/
public class LocationIPGeolocationIO implements LocationServiceInterface {

  private static final String KEY = "298fc8e3058d4c21a028c55f82608196";
  private static final String host = "https://api.ipgeolocation.io/ipgeo";

  @Override
  public String getRequest (String ip) {
    return String.format ("%s?apiKey=%s&ip=%s", host, KEY, ip);
  }

  @Override
  public Location parseLocation (String data) {
    JSONObject jsonData = new JSONObject (data);
    return new Location (jsonData.getString ("ip"), jsonData.getString ("country_name"),
                         jsonData.getDouble ("latitude"), jsonData.getDouble ("longitude"));
  }
}
