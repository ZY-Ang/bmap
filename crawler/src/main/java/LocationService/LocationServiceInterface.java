package LocationService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public interface LocationServiceInterface {

  public abstract Location[] getLocations (String url);

  public default String[] getIps (String url) {
    try {
      return Arrays.stream (InetAddress.getAllByName (url))
          .map (x -> x.getHostAddress ())
          .filter (x -> x.matches ("([0-9]{1,3}+\\.){3}+[0-9]{1,3}+"))
          .toArray (String[]::new);
    } catch (UnknownHostException e) {
      System.err.printf ("Host: %s not found", url);
    }
    return new String[0];
  }
}
