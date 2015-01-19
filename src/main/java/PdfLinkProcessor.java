import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * PdfLinkProcessor
 *
 * A set of incoming pdf links is downloaded and transformed into
 * a set of text documents
 */
public class PdfLinkProcessor {

    final Logger logger = LoggerFactory.getLogger(PdfLinkProcessor.class);

    private String storePath;

    public PdfLinkProcessor(String storePath) {
        this.storePath = storePath;
    }

    /**
     * Process list of links to pdf files. Download to local folder and
     * transformation to text file.
     * @param links
     * @return
     */
    public List<String> processPdfLinks(List<String> links) {
        List<String> txtPathes = new ArrayList<String>();
        for (String link : links) {
            try {
                File pdfFilePath = downloadPdf("TODO: Replace", link);
                File txtFilePath = processPdfToText(pdfFilePath);
                txtPathes.add(txtFilePath.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return txtPathes;
    }

    /**
     * Given url, downloads the file to a the local storePath
     * @param title
     * @param link
     * @return
     * @throws MalformedURLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private File downloadPdf(String title, String link) throws
            MalformedURLException, FileNotFoundException,
            IOException
    {
        logger.info("Start download of file at url {}", link);
        URL url = new URL(link);
        InputStream in = url.openStream();
        File pdfFilePath = new File(storePath, String.format("%d.pdf", link.hashCode()));
        logger.info("Downloading file to {}", pdfFilePath.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(pdfFilePath);

        int length;
        byte[] buffer = new byte[1024];

        while ((length = in.read(buffer)) > -1) {
            fos.write(buffer, 0, length);
        }

        fos.close();
        in.close();
        logger.info("Finished download of file at url {}", link);

        return pdfFilePath;
    }

    /**
     * Tranform pdf to text
     * @param pdfFilePath
     * @return
     */
    private File processPdfToText(File pdfFilePath) throws IOException{
        PDFTextStripper stripper = new PDFTextStripper("utf-8");
        File outputFile = new File(storePath,
                String.format("{}.txt", pdfFilePath.getAbsoluteFile().hashCode())
        );
        logger.info("Generating text file {}", outputFile.getAbsolutePath());
        Writer writer = new FileWriter(outputFile);
        PDFParser parser = new PDFParser(new FileInputStream(pdfFilePath));
        parser.parse();

        COSDocument cosDoc = parser.getDocument();
        PDDocument pdDoc = new PDDocument(cosDoc);

        stripper.writeText(pdDoc, writer);

        writer.close();
        cosDoc.close();
        pdDoc.close();

        return outputFile;
    }

}
