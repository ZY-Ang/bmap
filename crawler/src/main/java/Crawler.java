import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
     * Access url and parse for links on the web page
     * @return links found on the url's web page
     */
    public long crawl(List<String> result) {
        StringBuilder stringBuilder = new StringBuilder();
        long time = -1;
        if (connection == null) {
            // Non-http/s url, invalid url found
            return time;
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
                result.addAll(parseURL(response.toString(), url));
            } else {
                stringBuilder.append("HTTP status code not OK");
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.printf("Unknown/invalid host: %s", url);
        }
        System.out.println(stringBuilder.toString());
        return time;
    }

    /**
     * Parse all <a> tag and return it as links
     * @param html body to parse
     * @param url base url of the html page (to join with relative link to get complete link)
     * @return list of links parsed from html
     */
    private List<String> parseURL(String html, String url){
        List<String> links = new ArrayList<>();

        try {
            Document doc = Jsoup.parse(html, url);
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
}
