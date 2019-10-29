package LocationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;

public interface LocationServiceInterface {

  int HTTP_TOO_MANY_REQUESTS = 429;

  String getRequest (String ip);

  Location parseLocation (String data);

  default Location getLocation (String ip) {
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
          return this.parseLocation (response.toString ());
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

    return null;
  }

  private HttpURLConnection makeConnection (String ip)
      throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL (this.getRequest (ip))
        .openConnection ();
    connection.setRequestMethod ("GET");
    connection.addRequestProperty ("User-Agent", "Mozilla/5.0");
    return connection;
  }

  default String[] getIps (String url) {
    if (url == null) {
      return new String[0];
    }
    try {
      return Arrays.stream (InetAddress.getAllByName (url))
          .map (InetAddress::getHostAddress)
          .filter (x -> x.matches ("([0-9]{1,3}+\\.){3}+[0-9]{1,3}+"))
          .toArray (String[]::new);
    } catch (UnknownHostException e) {
      System.err.printf ("Host: %s not found\n", url);
    }
    return new String[0];
  }

}
