import java.io.File;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.configuration.Configuration;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CommuneCrawler extends WebCrawler{

    private final Logger logger = LoggerFactory.getLogger(CommuneCrawler.class);

    // Keeps track of all endings that can be followed
    private static Pattern filters;

    // Keeps track of domains that can be followed
    private static String[] followFilters;

    private static Configuration config;

    private static PdfLinkProcessor processor;

    private static boolean downloaded = false;

    public static void configure(Configuration config, String village) {
        CommuneCrawler.config = config;

        CommuneCrawler.processor = new PdfLinkProcessor(new File(config.getString("crawler.downloadDir"), village).getAbsolutePath());

        filters = Pattern.compile(config.getString("crawler.filters"));
        followFilters = config.getString("crawler.allowedHosts").split(",");
    }

    @Override
    public boolean shouldVisit(Page page, WebURL url) {
        if(downloaded) return false;

        String href = url.getURL().toLowerCase();
        if(null == filters) return true;

        boolean allowedDomain = false;
        for(String domain : followFilters) {
            if(href.contains(domain)) allowedDomain = true;
        }

        return !filters.matcher(href).matches() && (
                page.getWebURL().getDomain().equals(url.getDomain()) || allowedDomain);
    }

    @Override
    public void visit(Page page) {
        if(page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();

            try {
                Map<String,String> pdfLinks = getPdfLinks(html);
                if(null != pdfLinks) {
                    int downloadCount = processor.processPdfLinks(pdfLinks);
                    if(downloadCount > 0) {
                        logger.info("Downloaded files, stop!");
                        this.getMyController().shutdown();
                    }
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Extracts from cleaned xml the links to the pdf files that should be downloaded
     * @param html
     * @return
     */
    protected Map<String,String> getPdfLinks(String html) throws
            XPathExpressionException, ParserConfigurationException {

        HtmlCleaner cleaner = new HtmlCleaner();
        TagNode root = cleaner.clean(html);
        Document doc = new DomSerializer(new CleanerProperties()).createDOM(root);
        Map<String,String> linkMap = new HashMap<String,String>();
        XPath xpath = XPathFactory.newInstance().newXPath();

        NodeList textNodes = (NodeList) xpath.evaluate(
                "//td//a[contains(., 'Bau') and (contains(., 'ordnung') or" +
                        " contains(., 'reglement'))]/text()", doc, XPathConstants.NODESET);
        NodeList pdfNodes = (NodeList) xpath.evaluate(
                "//td//a[contains(., 'Bau') and (contains(., 'ordnung') or" +
                        " contains(., 'reglement'))]/ancestor::tr[1]//a/" +
                        "@href[contains(.,'pdf')]", doc, XPathConstants.NODESET);

        if (pdfNodes.getLength() != textNodes.getLength()) return null;
        for (int i = 0; i < pdfNodes.getLength(); i++) {
            linkMap.put(
                    textNodes.item(i).getNodeValue(),
                    pdfNodes.item(i).getNodeValue()
            );
        }

        return linkMap;
    }
}
