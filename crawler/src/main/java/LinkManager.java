import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Concurrent link manager
 */
public class LinkManager {
    private Set<String> toVisit; // Links yet to visit
    private Set<String> visited; // Visited links
    private ReentrantLock visitedLock; // File read/write lock
    private ReentrantLock WRLock; // File read/write lock


    /**
     * Default constructor with predefined start url
     */
    public LinkManager() {
        visitedLock = new ReentrantLock();
        WRLock = new ReentrantLock();
        toVisit = new HashSet<>();
        visited = new HashSet<>();

        toVisit.add("https://jsoup.org/");
    }

    /**
     * Constructor with file read for initial links
     * @param fileName contains initial links to visit
     */
    public LinkManager(String fileName) {
        visitedLock = new ReentrantLock();
        WRLock = new ReentrantLock();
        toVisit = new HashSet<>();
        visited = new HashSet<>();

        // Read links from file, if failed/empty add defaults
        if (!readSource(fileName)) {
            System.out.println("Add predefined links instead...");
            toVisit.add("https://jsoup.org/");
        }
    }

    public static boolean isHttps(String url) {
        return url.indexOf("https://") == 0;
    }

    public static boolean isHttp(String url) {
        return url.indexOf("http://") == 0;
    }

    public static boolean isHttpOrHttps(String url) {
        return isHttp(url) || isHttps(url);
    }

    /**
     * Retrieve a unvisited link and set it as visited.
     * Lock is active to prevent retrieving of the same link.
     * @return Unvisited link
     */
    public String getLink() {
        String host = null;
        visitedLock.lock();
        Iterator<String> it = toVisit.iterator();
        if (it.hasNext()) {
            host = it.next();
            toVisit.remove(host);
            visited.add(host);
        }
        visitedLock.unlock();
        return host;
    }

    /**
     * Add new links to visit.
     * Filter away visited links and keep only unvisited links.
     * Lock to prevent concurrent modification when reading links using iterator.
     * @param links potential links to visit
     * @return number of links qualify as unvisited
     */
    public int addLinks(List<String> links) {
        int count = 0;
        visitedLock.lock();
        for (String s : links) {
            if (!visited.contains(s) && isHttpOrHttps(s)) {
                toVisit.add(s);
                count++;
            }
        }
        visitedLock.unlock();
        return count;
    }

    /**
     * Get number of unvisited links.
     * @return number of unvisited links
     */
    public int numToVisit() {
        return toVisit.size();
    }

    /**
     * Read links and add to unvisited links.
     * Utility method for constructor.
     * @param fileName to be read for links
     * @return true if link(s) found, otherwise false
     */
    private boolean readSource(String fileName) {
        List<String> list = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }
            bufferedReader.close();
        } catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        } catch(IOException ex) {
            ex.printStackTrace();
        }

        if (list.isEmpty()) {
            return false;
        } else {
            addLinks(list);
            return true;
        }
    }

    /**
     * Write link and runtime to file on new line for persistent storage.
     * @param link to be write out
     * @param time runtime to crawl the link
     */
    public void writeVisitedLink(String link, long time) {
        // The name of the file to open.
        String fileName = "visited.txt";
        String msg = String.format("%d ms - %s", time, link);
        WRLock.lock();
        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(msg);
            bufferedWriter.newLine();

            bufferedWriter.close();
        } catch(IOException ex) {
            System.out.println("Error writing to file '" + fileName + "'");
            // ex.printStackTrace();
        }
        WRLock.unlock();
    }
}
