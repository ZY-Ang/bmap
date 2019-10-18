import static java.lang.Thread.sleep;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * A unit of concurrently task
 */
public class CrawlerTask extends RecursiveAction {
    private LinkManager linkManager;

    public CrawlerTask(LinkManager linkManager) {
        this.linkManager = linkManager;
    }

    @Override
    protected void compute() {
        // If timeout, abandon task
        if (Timer.getInstance().hasTimeout())
            return;

        // Retrieve link to crawl
        String url = linkManager.getLink();
        if (url == null) {
            return;
        }

        // Crawl and retrieve links while tracking runtime
        Crawler crawler = new Crawler(url);
        List<String> links = new ArrayList<>();
        long time = crawler.crawl(links);

        // Write out visited valid link and its runtime
        if (time > 0)
            linkManager.writeVisitedLink(url, time);
        // Add new links crawled
        int numOfUnvisitedLinks = linkManager.addLinks(links);

        // Recursively create new concurrent tasks
        List<CrawlerTask> list = new ArrayList<>();
        // Create new tasks when time out has not occur
        // Number of task created is equal to the number of unvisited links crawled
        for (int i = 0; i < numOfUnvisitedLinks && !Timer.getInstance().hasTimeout(); i++) {
            try {
                // Introduce delay before crawling the next link
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            CrawlerTask t2 = new CrawlerTask(linkManager);
            list.add(t2);
            t2.fork();
        }

        // Wait for all tasks forked
        for (CrawlerTask t : list) {
            t.join();
        }
    }
}
