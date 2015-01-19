import java.io.File;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.configuration.Configuration;
import org.apache.xml.serialize.DOMSerializer;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommuneCrawler extends WebCrawler{

    // Keeps track of all endings that can be followed
    private static Pattern filters;

    // Keeps track of domains that can be followed
    private static Pattern followFilters;

    // Keeps track of which files can be downloaded
    private static Pattern downloadPattern;

    private static Configuration config;

    private static String[] domain;

    public static void configure(Configuration config, String[] domain) {
        CommuneCrawler.config = config;
        CommuneCrawler.domain = domain;

        if(null != config.getString("storageDir")) {
            File  storageFolder = new File(config.getString("storageDir"));
            if(!storageFolder.exists()) {
                storageFolder.mkdirs();
            }
        }
    }

    @Override
    public boolean shouldVisit(Page page, WebURL url) {

        String href = url.getURL().toLowerCase();

        if(null == filters) return true;

        return !filters.matcher(href).matches() && (
                page.getWebURL().getDomain().equals(url.getDomain()) ||
                        followFilters.matcher(url.getDomain()).matches());
    }

    @Override
    public void visit(Page page) {

        if(page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();

            try {
                List<String> pdfLinks = getPdfLinks(html);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Extracts from cleaned xml the links to the pdf files that should be downloaded
     * @param root
     * @return
     */
    protected ArrayList<String> getPdfLinks(String html) throws
            XPathExpressionException, ParserConfigurationException {

        HtmlCleaner cleaner = new HtmlCleaner();
        TagNode root = cleaner.clean(html);
        Document doc = new DomSerializer(new CleanerProperties()).createDOM(root);
        ArrayList pdfList = new ArrayList<String>();
        XPath xpath = XPathFactory.newInstance().newXPath();

        NodeList nodes = (NodeList) xpath.evaluate(
                "//td//a[contains(., 'Bau') and contains(., 'ordnung') or" +
                        " contains(., 'reglement')]/ancestor::tr[1]//a/" +
                        "@href[contains(.,'pdf')]", doc, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            pdfList.add(nodes.item(i).getNodeValue());
        }

        return pdfList;
    }
}
