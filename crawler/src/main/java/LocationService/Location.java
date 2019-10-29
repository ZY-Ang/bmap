package LocationService;

public class Location {

  /**
   * Required empty constructor for Firebase. DO NOT AMEND.
   */
  public Location() {}
  public Location (String ip, String country, double latitude, double longitude) {
    this.ip = ip;
    this.country = country;
    this.latitude = latitude;
    this.longitude = longitude;
    this.coords = String.format ("%f,%f", latitude, longitude);
  }

  public String ip, country, coords;
  public double latitude, longitude;

  public Location (String ip, String country, String coords) {
    this.ip = ip;
    this.country = country;
    this.coords = coords;
    String c[] = coords.split (",");
    this.latitude = Double.parseDouble (c[0]);
    this.longitude = Double.parseDouble (c[1]);
  }

  @Override
  public String toString () {
    return "Location{" +
        "ip='" + ip + '\'' +
        ",\tcountry='" + country + '\'' +
        ",\tcoords='" + coords + '\'' +
        ",\tlatitude=" + latitude +
        ",\tlongitude=" + longitude +
        '}';
  }
}
