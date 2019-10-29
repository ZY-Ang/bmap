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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

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
            String environment = getEnvironment();
            FirebaseDatabase.getInstance().getReference(environment + "/usethissubtree")
                    .setValueAsync(environment.equals("production")
                            ? "for production! Use other trees for development"
                            : "for your own local development"
                    );
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("initFirebase failed. You probably forgot to download the firebase private key file " +
                    "and/or set the environment variable $GOOGLE_APPLICATION_CREDENTIALS to its directory. \n\n" +
                    "See https://firebase.google.com/docs/admin/setup#initialize_the_sdk for more information.");
            exit(1);
        }
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
        FirebaseDatabase.getInstance().getReference(environment + "/queue").orderByKey().limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
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
            FirebaseDatabase.getInstance().getReference(deletionRef[0]).runTransaction(new Transaction.Handler() {
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
                return new URL(url[0]).toString();
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

    /**
     * Adds the words' counts to the respective location in the necessary countries
     */
    private static void addWordsToCountries(Location[] locations, Map<String, Long> wordCounts) {
        System.out.println("addWordsToCountries = " + wordCounts.keySet().size());
        String environment = getEnvironment();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        for (String word : wordCounts.keySet()) {
            db.getReference(environment + "/words/" + word + "/potatoCountry").runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Long count = mutableData.getValue(Long.class);
                    if (count ==  null) mutableData.setValue(wordCounts.get(word));
                    else mutableData.setValue(count + wordCounts.get(word));
                    return Transaction.success(mutableData);
                }
                @Override
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {}
            });
//            for (Location location: locations) {
//                TODO: FIX LOCATIONS
//            }
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
            FirebaseDatabase.getInstance().getReference(environment + "/visited").push().setValue(new Visited(url), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {
                    if (error != null) System.err.println(error);
                    else committed[0] = true;
                    semaphore.release();
                }
            });
        } catch (Exception e) {}
        try { semaphore.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }
        return committed[0];
    }

    public static void main(String[] args) {
        System.out.println("Crawler running on environment = " + getEnvironment());
        initFirebase();
        
        int backoff = 1000;
        while (true) {
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
                addWordsToCountries(LocationService.getLocations(nextUrl), wordsOnPage);
                addUrlsToQueue(urlsOnPage.stream().filter(url -> !url.equals(nextUrl)).collect(Collectors.toList()));

                backoff = 1000;
            } else {
                // Try fetching next URL again in 1,2,4,... seconds. Huzzah exponential backoff algorithm! LOL
                try { Thread.sleep(backoff); } catch (InterruptedException e) {}
                backoff *= 2;
            }
        }
    }
}
