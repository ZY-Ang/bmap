import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Web crawler handling HTTP connection and parsing
 */
public class Crawler {
    private String url; // url to crawl
    private HttpURLConnection connection; // Http/s connection

    /**
     * Constructor with creation of http connection
     * @param url to crawl
     */
    public Crawler(String url) {
        this.url = url;
        try {
            if (LinkManager.isHttps(url)) {
                connection = (HttpsURLConnection) new URL(url).openConnection();
            } else if (LinkManager.isHttp(url)) {
                connection = (HttpURLConnection) new URL(url).openConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates 
     * @param links - List of links to be added to
     * @param words - Hashmap (count) of words to be added
     *
     * @return {@code true} if the crawl update succeeded or
     *  {@code false} if the crawl did not succeed.
     */
    public boolean crawl(List<String> links, Map<String, Long> words) {
        StringBuilder stringBuilder = new StringBuilder();
        long time = -1;
        if (connection == null) {
            // Non-http/s url, invalid url found
            return false;
        }
        try {
            // Set http request headers
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");

            // Start timer
            long startTime = System.currentTimeMillis();
            // Wait for http response
            int responseCode = connection.getResponseCode();
            time = System.currentTimeMillis() - startTime;
            stringBuilder.append(String.format("%d ms - %s ", time, url));
            if (responseCode == HttpsURLConnection.HTTP_OK) { //
                // success, read http response body stream and convert to String
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse for links in html
                Document doc = Jsoup.parse(response.toString(), url);
                links.addAll(parseURL(doc));
                parseWord(doc).forEach(word -> {
                    long count = words.containsKey(word) ? words.get(word) : 0;
                    words.put(word, count + 1);
                });
            } else {
                stringBuilder.append("HTTP status code not OK");
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.printf("Unknown/invalid host: %s", url);
            return false;
        } catch (UncheckedIOException e) {
//            Http/s responses that cannot be parsed
//            e.printStackTrace();
            return false;
        }
        System.out.println(stringBuilder.toString());
        return true;
    }

    /**
     * Parse all <a> tag and return it as links
     * @return list of links parsed from html
     */
    private Set<String> parseURL(Document doc){
        Set<String> links = new HashSet<>();

        try {
            // Retrieve all <a> tag is html
            Elements elements = doc.select("a");
            for (Element e : elements) {
                // Retrieve link from <a> tag
                String s = e.attr("abs:href").trim();
                if (!s.isEmpty())
                    links.add(s);
            }
        } catch (UncheckedIOException e) {
//            Http/s responses that cannot be parsed
//            e.printStackTrace();
        }
        return links;
    }

    private List<String> parseWord(Document doc) {
        List<String> words = new ArrayList<>();
        try {
            String body = doc.body().text();
            words = Arrays.asList(body.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+"));

        } catch (UncheckedIOException e) {
//            Http/s responses that cannot be parsed
//            e.printStackTrace();
        }
        return words;
    }
}
