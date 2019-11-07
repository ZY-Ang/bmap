import LocationService.Location;
import LocationService.LocationService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.exit;

public class Main {
    private static String getEnvironment() {
        String environment = System.getenv("JAVA_ENVIRON");
        if (environment == null) {
            environment = "dev" + System.getProperty("user.name");
        }
        return environment;
    }

    /**
     * @link https://firebase.google.com/docs/admin/setup#initialize_the_sdk
     */
    private static void initFirebase() {
        try {
            FileInputStream serviceAccount = new FileInputStream(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://buzzwordmap.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("initFirebase failed. You probably forgot to download the firebase private key file " +
                    "and/or set the environment variable $GOOGLE_APPLICATION_CREDENTIALS to its directory. \n\n" +
                    "See https://firebase.google.com/docs/admin/setup#initialize_the_sdk for more information.");
            exit(1);
        }
    }

    /**
     * Deletes all elements in the production queue. Used for reseeding
     */
    private static void deleteProductionQueue() {
        final Semaphore sem = new Semaphore(0);
        FirebaseDatabase.getInstance().getReference("production/queue").setValue(null, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                System.out.println("Deleted queue");
                sem.release();
            }
        });
        try {
            sem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void seed() {
        String environment = getEnvironment();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        // 1. Comment and garbage
        db.getReference(environment + "/usethissubtree")
                .setValueAsync(environment.equals("production")
                        ? "for production! Use other trees for development"
                        : "for your own local development"
                );
        // 2. Seed queue with initial data
        db.getReference(environment + "/queue").addListenerForSingleValueEvent(new ValueEventListener() {
            /**
             * Seed an empty subtable with values here.
             * (Probably want distribution to be nicely evened out globally)
             */
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() == null || snapshot.getChildrenCount() == 0) {
                    String[] seed = {
                        "https://en.wikipedia.org/wiki/List_of_news_agencies",
                        "https://indiatimes.com/",
                        "http://chinadaily.com.cn/",
                        "https://news.com.au/",
                        "https://news.com.au/",
                        "https://mexiconewsdaily.com/",
                        "https://www.peruviantimes.com/",
                        "https://www.batimes.com.ar/",
                        "https://riotimesonline.com/",
                        "https://brazilian.report/",
                        "https://en.mercopress.com/",
                        "https://www.arabnews.com/",
                        "https://www.aljazeera.com/",
                        "https://www.bbc.co.uk/",
                        "https://www.independent.co.uk/",
                        "http://www.koreaherald.com/",
                        "https://www.koreatimes.co.kr/",
                        "https://en.yna.co.kr/",
                        "http://www.kcna.kp/",
                        "http://www.xinhuanet.com/",
                        "http://www.globaltimes.cn/",
                        "http://www.cctv.com/",
                        "http://english.cctv.com/",
                        "https://egyptianstreets.com/",
                        "http://www.masrawy.com/",
                        "http://www.ngmisr.com/",
                        "http://www.arabwestreport.info/",
                        "http://www.tuko.co.ke/",
                        "http://sudaneseonline.com/",
                        "http://themoroccantimes.com/",
                        "http://www.itnewsafrica.com/",
                        "https://www.afp.com/",
                        "https://www.france24.com/",
                        "https://www.thecanadianpress.com/",
                        "https://www.thecanadianpress.com/",
                        "https://www.cbc.ca/",
                        "http://www.canadanewsagency.com/",
                        "https://www.thelocal.se/",
                        "https://tt.se/",
                        "swedishpressagency.se/",
                        "https://www.newsroom.co.nz/",
                        "http://www.scoop.co.nz/",
                        "https://www.nzherald.co.nz",
                        "https://www.abc.net.au/",
                        "https://www.news.com.au/",
                        "https://www.theaustralian.com.au/",
                        "https://www.kyodonews.jp/",
                        "https://english.kyodonews.net/",
                        "https://www.japantimes.co.jp/",
                        "https://japantoday.com/",
                        "http://www.bernama.com/",
                        "http://www.mycen.com.my/"
                    };
                    addUrlsToQueue(Arrays.asList(seed));
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
        // TODO: Once we're ready: Comment out or remove the line below, check other TODOs, commit and push.
//        if (environment.equals("production")) exit(0);
    }
    
    /**
     * Gets and deletes the next URL in the queue.
     * @return {@code null} if get was unsuccessful or deletion was unsucessful or url is invalid,
     * {@code string} of the url otherwise.
     */
    private static String getAndDeleteNextUrl() {
        String environment = getEnvironment();
        final Semaphore dirSemaphore = new Semaphore(0);
        boolean[] transacted = {false};
        String[] url = new String[1];
        String[] deletionRef = new String[1];
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference(environment + "/queue").orderByKey().limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot querySnapshot) {
                try {
                    if (querySnapshot.getChildrenCount() != 1) deletionRef[0] = null;
                    else deletionRef[0] = environment + "/queue/" + querySnapshot.getChildren().iterator().next().getKey();
                } catch (Exception e) {}
                dirSemaphore.release();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println(databaseError);
                dirSemaphore.release();
            }
        });
        try {dirSemaphore.acquire();} catch (InterruptedException e) { e.printStackTrace(); }
        if (deletionRef[0] != null) {
            final Semaphore delSemaphore = new Semaphore(0);
            db.getReference(deletionRef[0]).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    String currentValue = mutableData.getValue(String.class);
                    // Another thread has taken this URL first (race condition)
                    if (currentValue == null) return Transaction.abort();
                    else {
                        url[0] = currentValue;
                        mutableData.setValue(null);
                        return Transaction.success(mutableData);
                    }
                }
                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    transacted[0] = b;
                    delSemaphore.release();
                }
            });
            try {delSemaphore.acquire();} catch (InterruptedException e) { e.printStackTrace(); }
        }
        if (transacted[0]) {
            try {
                URL parsedUrl = new URL(url[0]);
                if (parsedUrl.getHost() == null) throw new MalformedURLException("Invalid hostname");
                else return parsedUrl.toString();
            } catch (MalformedURLException e) {
                System.err.println(url[0] + " is not a valid URL!");
                return null;
            }
        }
        else return null;
    }

    /**
     * Adds a {@param listOfUrls} to the database queue to be crawled next.
     *
     * Non-blocking.
     */
    private static void addUrlsToQueue(List<String> listOfUrls) {
        System.out.println("addUrlsToQueue = " + listOfUrls.size());
        String environment = getEnvironment();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference queueRef = db.getReference(environment + "/queue");
        // Filter and exclude visited, or repeated in queue
        listOfUrls.forEach(url -> db.getReference(environment + "/visited")
                .orderByChild("url").equalTo(url).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot visitedFilterSnapshot) {
                if (visitedFilterSnapshot.getChildrenCount() == 0)
                    db.getReference(environment + "/queue").orderByValue().equalTo(url).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot queueFilterSnapshot) {
                        if (queueFilterSnapshot.getChildrenCount() == 0) queueRef.push().setValueAsync(url);
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        }));
    }

    private static boolean isValidWordForAddition(String word) {
        String trimmed = word.trim();
        boolean isNaturalNumber = true;
        try { Long num = Long.parseLong(trimmed); } catch (NumberFormatException e) { isNaturalNumber = false; }
        return trimmed.length() > 0 && !isNaturalNumber;
    }
    
    /**
     * Adds the words' counts to the respective location in the necessary countries
     */
    private static void addWords(Location[] locations, Map<String, Long> wordCounts) {
        System.out.println("addWords.countries = " + locations.length);
        System.out.println("addWords.wordCounts = " + wordCounts.keySet().size());
        String environment = getEnvironment();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        Set<String> countryCodes = new HashSet<>();
        for (Location location : locations) countryCodes.add(location.code);
        for (String word : wordCounts.keySet().stream().filter(Main::isValidWordForAddition).collect(Collectors.toList())) {
            for (String countryCode: countryCodes) {
                db.getReference(environment + "/words/" + word + "/" + countryCode).runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Long count = mutableData.getValue(Long.class);
                        if (count == null) mutableData.setValue(wordCounts.get(word));
                        else mutableData.setValue(count + wordCounts.get(word));
                        return Transaction.success(mutableData);
                    }
                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {}
                });
            }
        }
    }

    /**
     * @param locations - object obtained for a particular URL crawl.
     *                  Used to calculate weighted average distribution
     *                  of crawled sites so we can normalize the word
     *                  frequencies.
     * 
     *                  Multiple servers in a request that belong to a
     *                  country count as one.
     */
    private static void addCountries(Location[] locations) {
        System.out.println("addCountries.countries = " + locations.length);
        String environment = getEnvironment();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        Set<String> countryCodes = new HashSet<>();
        for (Location location : locations) countryCodes.add(location.code);
        for (String countryCode : countryCodes) {
            db.getReference(environment + "/countries/" + countryCode).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Long count = mutableData.getValue(Long.class);
                    if (count == null) mutableData.setValue(1);
                    else mutableData.setValue(count + 1);
                    return Transaction.success(mutableData);
                }
                @Override
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {}
            });
        }
    }
    
    /**
     * Marks a {@param url} as visited and not to be crawled again.
     * 
     * Note: Blocking. Should be a priority process.
     */
    private static boolean markVisited(String url) {
        String environment = getEnvironment();
        final boolean[] committed = {false};
        final Semaphore semaphore = new Semaphore(0);
        try {
            FirebaseDatabase.getInstance().getReference(environment + "/visited").push().setValue(new Visited(url), (error, ref) -> {
                if (error != null) System.err.println(error);
                else committed[0] = true;
                semaphore.release();
            });
        } catch (Exception e) { semaphore.release(); }
        try { semaphore.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }
        return committed[0];
    }

    /**
     * First queries our internal cache for results against a domain then the API.
     */
    private static Location[] getCountriesForUrl(String url) {
        try {
            String environment = getEnvironment();
            String domain = getDomainName(url);
            if (domain == null) return new Location[0];
            String domainId = domain.replaceAll("\\.", "-");
            final Semaphore getSemaphore = new Semaphore(0);
            List<Location> locations = new ArrayList<Location>();
            DatabaseReference cacheRef = FirebaseDatabase.getInstance().getReference(environment + "/geoCache/" + domainId);
            cacheRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        snapshot.getChildren().forEach(childLocationRef -> locations.add(childLocationRef.getValue(Location.class)));
                    }
                    getSemaphore.release();
                }
                @Override
                public void onCancelled(DatabaseError error) { getSemaphore.release(); }
            });
            getSemaphore.acquire();
            if (locations.isEmpty()) {
                System.out.println("getCountriesForUrl Cache Miss, adding...");
                LocationService locator = new LocationService();
                Location[] locationArr = locator.getLocations(domain);
                for (Location location : locationArr) {
                    // Cache results but non-blocking
                    String geoCachePath = environment + "/geoCache/" + domainId + "/" + location.code + "-" + location.ip.replaceAll("\\.", "-");
                    FirebaseDatabase.getInstance().getReference(geoCachePath).setValueAsync(location);
                }
                return locationArr;
            } else {
                System.out.println("getCountriesForUrl Cache Hit");
                return locations.toArray(new Location[0]);
            }

        } catch (Exception e) {
            return new Location[0];
        }
    }

    /**
     * @author https://stackoverflow.com/questions/9607903/get-domain-name-from-given-url
     */
    private static String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("Crawler running on environment = " + getEnvironment());
        initFirebase();
        seed();
        
        int backoff = 1000;
        // TODO: Update before scraper launch
        //         Use {@link https://currentmillis.com/}
        while (currentTimeMillis() < 1573624800000L) {
            String nextUrl = getAndDeleteNextUrl();
            if (nextUrl != null) {
                System.out.println("\n======================== " + nextUrl + " ========================");
                markVisited(nextUrl);
                List<String> urlsOnPage = new ArrayList<>();
                Map<String, Long> wordsOnPage = new HashMap<>();
                Crawler crawler = new Crawler(nextUrl);
                boolean crawlSuccess = crawler.crawl(urlsOnPage, wordsOnPage);
                if (!crawlSuccess) {
                    System.out.println("WARNING: Failed to crawl.");
                    continue;
                }
                Location[] locations = getCountriesForUrl(nextUrl);
                addWords(locations, wordsOnPage);
                addCountries(locations);
                addUrlsToQueue(urlsOnPage.stream().filter(url -> !url.equals(nextUrl)).collect(Collectors.toList()));

                backoff = 1000;
            } else {
                System.out.println("backoff = " + backoff);
                // Try fetching next URL again in 1,2,4,... seconds. Huzzah exponential backoff algorithm! LOL
                try { Thread.sleep(backoff); } catch (InterruptedException ignored) {}
                backoff *= 2;
            }
        }
    }
}
