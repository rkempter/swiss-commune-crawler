import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controls & initializes the crawler
 */
public class CommuneCrawlerController {

    public static void main(String[] args) throws Exception {
        Configuration propertiesFile = new PropertiesConfiguration(args[0]);

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(propertiesFile.getString("crawler.storageDir"));
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        Map<String,String> seeds = new HashMap<String,String>();
        seeds.put("Bremgarten", "http://www.bremgarten.ch");
        seeds.put("Wohlen AG", "http://www.wohlen.ch");

        for (Map.Entry<String,String> seed : seeds.entrySet()) {
            CrawlController crawlController = new CrawlController(config, pageFetcher, robotstxtServer);
            crawlController.addSeed(seed.getValue());
            CommuneCrawler.configure(propertiesFile, seed.getKey());
            crawlController.start(CommuneCrawler.class, 8);
        }

    }

}
