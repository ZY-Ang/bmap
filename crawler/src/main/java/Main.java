import java.util.ArrayList;
import java.util.List;

import LocationService.Location;
import LocationService.LocationIPGeolocation;
import LocationService.LocationServiceInterface;

public class Main {

    public static void main(String[] args) {
        Firebase firebase = new Firebase();

        LocationServiceInterface ls = new LocationIPGeolocation();
        Location[] locations = ls.getLocations("luminus.nus.edu.sg");
        for (Location location : locations) {
            System.out.printf("%s %s %n", location.country, location.ip);
            firebase.writeWords(location);
        }

        Crawler crawler = new Crawler("https://yahoo.com");
        List<String> links = new ArrayList<>();
        List<String> words = new ArrayList<>();
        crawler.crawl(links, words);
    }
}
