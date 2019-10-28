package LocationService;

import org.json.JSONObject;

/* 1500/day */
public class LocationIPData implements LocationServiceInterface {

  private static final String KEY = "648c57fadf6c096ffe0722a7acf1d6583dad9f78d0dfe79fb07f4b3e";
  private static final String host = "https://api.ipdata.co/";

  @Override
  public String getRequest (String ip) {
    return String.format ("%s%s?api-key=%s", host, ip, KEY);
  }

  @Override
  public Location parseLocation (String data) {
    JSONObject jsonData = new JSONObject (data);
    return new Location (jsonData.getString ("ip"), jsonData.getString ("country_name"),
                         jsonData.getDouble ("latitude"), jsonData.getDouble ("longitude"));
  }
}
