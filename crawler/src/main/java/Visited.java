import com.google.firebase.database.ServerValue;

/**
 * All member variables have to be public.
 * Ignore your IDE warnings and do not amend!
 */
public class Visited {
    public String url;
    public Object timestamp;
    public Visited(){}
    public Visited(String url) {
        this.url = url;
        this.timestamp = ServerValue.TIMESTAMP;
    }
}
