package LocationService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Location {

  private static Map<String, String> countries = new HashMap<> ();

  public static void loadCountryMap () {
    Locale lang = new Locale.Builder ().setLanguage ("en").build ();
    countries = new HashMap<> ();
    for (String iso : Locale.getISOCountries ()) {
      Locale l = new Locale ("en", iso);
      countries.put (l.getDisplayCountry (lang), iso);
    }
  }
  /**
   * Required empty constructor for Firebase. DO NOT AMEND.
   */
  public Location () {
  }
  public Location (String ip, String country, double latitude, double longitude) {
    if (countries.isEmpty ()) {
      loadCountryMap ();
    }
    this.ip = ip;
    this.country = country;
    this.latitude = latitude;
    this.longitude = longitude;
    this.coords = String.format ("%f,%f", latitude, longitude);
    this.code = countries.get (country);
  }

  public String ip, country, coords, code;
  public double latitude, longitude;

  public Location (String ip, String country, String coords) {
    if (countries.isEmpty ()) {
      loadCountryMap ();
    }
    this.ip = ip;
    this.country = country;
    this.coords = coords;
    String c[] = coords.split (",");
    this.latitude = Double.parseDouble (c[0]);
    this.longitude = Double.parseDouble (c[1]);
    this.code = countries.get (country);
  }

  @Override
  public String toString () {
    return "Location{" +
        "ip='" + ip + '\'' +
        ",\tcountry='" + country + '\'' +
        ",\tcode='" + code + '\'' +
        ",\tcoords='" + coords + '\'' +
        ",\tlatitude=" + latitude +
        ",\tlongitude=" + longitude +
        '}';
  }
}
