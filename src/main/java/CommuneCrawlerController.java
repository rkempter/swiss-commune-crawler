import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Controls & initializes the crawler
 */
public class CommuneCrawlerController {

    public static void main(String[] args) throws Exception {
        Configuration propertiesFile = new PropertiesConfiguration(args[0]);

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(propertiesFile.getString("crawler.storageDir"));

        InputStream in = new FileInputStream(args[1]);
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);

        Map<String,String> seeds = new HashMap<String,String>();
        String line;
        while((line = br.readLine()) != null) {
            String[] commune = line.split(",");
            if (commune.length != 2) throw new Exception("File format error");
            seeds.put(commune[0].trim(), commune[1].trim());
        }

        for (Map.Entry<String,String> seed : seeds.entrySet()) {
            // Crawl only the communes that have not been crawled yet
            if(checkIfCrawled(propertiesFile.getString("crawler.downloadDir"), seed.getKey())) continue;

            PageFetcher pageFetcher = new PageFetcher(config);
            RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
            robotstxtConfig.setEnabled(false);
            RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
            CrawlController crawlController = new CrawlController(config, pageFetcher, robotstxtServer);
            crawlController.addSeed(seed.getValue());
            CommuneCrawler.configure(propertiesFile, seed.getKey());
            crawlController.start(CommuneCrawler.class, 8);
        }
    }

    /**
     * Check if a file path exists and has files
     * @param path
     * @param commune
     * @return
     */
    public static boolean checkIfCrawled(String path, String commune) {
        File testPath = new File(path, commune);

        return testPath.exists() && testPath.listFiles().length >= 2;
    }

}
