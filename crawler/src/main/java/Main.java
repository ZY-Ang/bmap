import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Firebase firebase = new Firebase();
//        String s = firebase.getUrl();
//        System.out.println(s);
        List<String> urls = new ArrayList<>();
        urls.add("https://firebase.google.com/");
        urls.add("https://google.com/");
        urls.add("https://yahoo.com/");
        urls.add("common_prefixDB");
//        urls.add("common_prefixCa");

        firebase.addUnvisitedUrls(urls);
        Thread.sleep(10000);

//        LocationServiceInterface ls = new LocationIPGeolocation();
//        Location[] locations = ls.getLocations("luminus.nus.edu.sg");
//        for (Location location : locations) {
//            System.out.printf("%s %s %n", location.country, location.ip);
//            firebase.writeWords(location);
//        }
//
//        Crawler crawler = new Crawler("https://yahoo.com");
//        List<String> links = new ArrayList<>();
//        List<String> words = new ArrayList<>();
//        crawler.crawl(links, words);
    }
}
