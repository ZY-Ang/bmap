package LocationService;

import org.json.JSONObject;

/*Max 10000/Month  http://api.ipstack.com/137.132.217.36?access_key=cb77de9b598d7798172fabb40618a71a*/
public class LocationIPStack implements LocationServiceInterface {

  private static final String KEY = "cb77de9b598d7798172fabb40618a71a";
  private static final String host = "http://api.ipstack.com/";

  @Override
  public String getRequest (String ip) {
    return String.format ("%s%s?access_key=%s", host, ip, KEY);
  }

  @Override
  public Location getLocation (String data) {
    JSONObject jsonData = new JSONObject (data);
    return new Location (jsonData.getString ("ip"), jsonData.getString ("country_name"),
                         jsonData.getDouble ("latitude"), jsonData.getDouble ("longitude"));
  }

}
