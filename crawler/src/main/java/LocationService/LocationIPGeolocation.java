package LocationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONObject;

public class LocationIPGeolocation implements LocationServiceInterface {

  private String host = "http://ipgeolocation.com/";
  HttpURLConnection connection = null;

  @Override
  public Location[] getLocations (String url) {
    String ips[] = this.getIps (url);
    ArrayList<Location> locations = new ArrayList<> ();
    for (String ip : ips) {
      try {
        connection = (HttpURLConnection) new URL (host + ip).openConnection ();
        connection.setRequestMethod ("GET");
        connection.addRequestProperty ("User-Agent", "Mozilla/5.0");

        int responseCode = connection.getResponseCode ();
        if (responseCode == HttpURLConnection.HTTP_OK) {
          BufferedReader in = new BufferedReader (new InputStreamReader (
              connection.getInputStream ()));
          String inputLine;
          StringBuffer response = new StringBuffer ();

          while ((inputLine = in.readLine ()) != null) {
            response.append (inputLine);
          }
          in.close ();
          JSONObject data = new JSONObject (response.toString ());
          locations.add (new Location (data.getString ("ip"), data.getString ("country")
              , data.getString ("coords")));
        }
      } catch (IOException e) {
        e.printStackTrace ();
      }
    }

    return locations.toArray (new Location[0]);
  }
}
