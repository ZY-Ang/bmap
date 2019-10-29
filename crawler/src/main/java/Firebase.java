import static java.lang.System.exit;

import java.io.FileInputStream;
import java.util.List;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import LocationService.Location;

public class Firebase {
    private static Firebase firebase;
    private String environment;
    Firebase() {
        initFirebase();
        String mode = System.getenv("JAVA_ENVIRON");
        if (mode == null) {
            this.environment = "dev" + System.getProperty("user.name");
            System.out.println("this.environment = " + this.environment);
        } else {
            this.environment = mode;
        }
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
    
    public void writeWords(Location location) {
        // Pushes a timestamp at database reference "test/potato"
            // with an autogenerated (sortable by entry order key)
            // every 5 minutes
            // Links:
            //  - https://firebase.google.com/docs/database/admin/start
            //  - https://firebase.google.com/docs/database/admin/save-data
            try {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(this.environment + "/words");

                ref.push().setValueAsync(location);
                System.out.println("one it");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void updateKeywordCounter() {

    }

    public void addUnvisitedUrls(List<String> urls) {
        for (String s : urls) {
            addUrl(s);
        }
    }

    private void addUrl(String url) {
        int hash = url.hashCode();
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(this.environment + "/visited/" + hash);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot ss : dataSnapshot.getChildren()) {
                            if (ss.getValue().equals(url))
                                return;
                        }
                    }
                    ref.push().setValueAsync(url);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println("Error: " + databaseError.getCode());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("toVisit");
            ref.limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();
                        String url = snapshot.child("link").getValue().toString();

                        // Remove child entry
                        snapshot.child("link").getRef().setValueAsync(null);

                        System.out.println(url);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println("Error: " + databaseError.getCode());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "test";
    }


}
