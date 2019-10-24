package LocationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public interface LocationServiceInterface {

  public int HTTP_TOO_MANY_REQUESTS = 429;

  String getRequest (String ip);

  Location getLocation (String data);

  public default Location[] getLocations (String url) {
    String ips[] = this.getIps (url);
    ArrayList<Location> locations = new ArrayList<> ();
    for (String ip : ips) {
      try {
        HttpURLConnection connection = makeConnection (ip);

        int responseCode = connection.getResponseCode ();
        while (responseCode != HttpURLConnection.HTTP_BAD_REQUEST) {
          if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader (new InputStreamReader (
                connection.getInputStream ()));
            String inputLine;
            StringBuffer response = new StringBuffer ();

            while ((inputLine = in.readLine ()) != null) {
              response.append (inputLine);
            }
            in.close ();
            locations.add (this.getLocation (response.toString ()));
            break;
          } else if (responseCode == HTTP_TOO_MANY_REQUESTS) {
            Thread.sleep (100);
            connection = makeConnection (ip);

            responseCode = connection.getResponseCode ();
          } else {
            break;
          }
        }
      } catch (IOException | InterruptedException e) {
        e.printStackTrace ();
      }
    }

    return locations.toArray (new Location[0]);
  }

  private HttpURLConnection makeConnection (String ip)
      throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL (this.getRequest (ip))
        .openConnection ();
    connection.setRequestMethod ("GET");
    connection.addRequestProperty ("User-Agent", "Mozilla/5.0");
    return connection;
  }

  private String[] getIps (String url) {
    try {
      return Arrays.stream (InetAddress.getAllByName (url))
          .map (InetAddress::getHostAddress)
          .filter (x -> x.matches ("([0-9]{1,3}+\\.){3}+[0-9]{1,3}+"))
          .toArray (String[]::new);
    } catch (UnknownHostException e) {
      System.err.printf ("Host: %s not found", url);
    }
    return new String[0];
  }

}
