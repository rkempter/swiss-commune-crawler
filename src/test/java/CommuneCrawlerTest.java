import com.uwyn.jhighlight.tools.FileUtils;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.*;

public class CommuneCrawlerTest {

    @org.junit.Test
    public void testGetPdfLinks() throws Exception {
        String testHtml = org.apache.commons.io.FileUtils.readFileToString(new File("/Users/rkempter/IdeaProjects/SwissCommuneCrawler/src/test/test_html.html"));
        CommuneCrawler crawler = new CommuneCrawler();
        Map<String,String> pdfLinks = crawler.getPdfLinks(testHtml);
        String[] resultLinks = {
            "http:​/​/​www.bremgarten.ch/​dl.php/​de/​52a83c41c86c8/​Bau-_und_Nutzungsordnung_23.5.2012.pdf",
            "http:​/​/​www.bremgarten.ch/​dl.php/​de/​50c5dd5f815c4/​BNO_Anhang_2_Gebuhrenreglement.pdf",
            "http:​/​/​www.bremgarten.ch/​dl.php/​de/​50c5de3eb35e5/​BNO_Anhang_3_Reglement_Gemeindebeitrage.pdf",
            "http:​/​/​www.bremgarten.ch/​dl.php/​de/​50c5dec878e37/​BNO_Anhang_4_Reglement_fur_das_Bauen_in_der_Altstadt.pdf",
            "http://www.bremgarten.ch/dl.php/de/50a10f588ac23/Bauzonenplan_2012-10-29.pdf", "http://www.bremgarten.ch/dl.php/de/0dhbo-2tjxx0/Kulturlandplan.pdf"
        };
        assertEquals(resultLinks.length, pdfLinks.values().toArray().length);
    }
}