package LocationService;

public class Location {

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
        ", country='" + country + '\'' +
        ", coords='" + coords + '\'' +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        '}';
  }
}
