import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PdfLinkProcessorTest {


    /**
     * Check if processPdfLinks downloads a pdf file and generates a text file
     * @throws Exception
     */
    @Test
    public void testProcessPdfLinks() throws Exception {
        List<String> pdfList = new ArrayList<String>();
        pdfList.add("http://www.bremgarten.ch/dl.php/de/52a83c41c86c8/Bau-_und_Nutzungsordnung_23.5.2012.pdf");
        String path = "/Users/rkempter/IdeaProjects/SwissCommuneCrawler/download/";
        File storagePath = new File(path);
        if(storagePath.exists()) storagePath.delete();
        storagePath.mkdir();

        File[] storedFiles = storagePath.listFiles();
        assertEquals(0, storedFiles.length);
        PdfLinkProcessor processor = new PdfLinkProcessor(path);
        processor.processPdfLinks(pdfList);
        storedFiles = storagePath.listFiles();
        assertEquals(2, storedFiles.length);
    }
}