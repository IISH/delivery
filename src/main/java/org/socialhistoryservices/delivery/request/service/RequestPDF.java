package org.socialhistoryservices.delivery.request.service;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.socialhistoryservices.delivery.util.TemplatePreparation;
import org.socialhistoryservices.delivery.util.TemplatePreparationException;
import org.springframework.ui.Model;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * PDF builder to create PDFs dealing with requests.
 */
public abstract class RequestPDF extends TemplatePreparation {
    /**
     * The factory to use to build PDFs using Apache FOP.
     */
    private static final FopFactory FOP_FACTORY;

    /*
     * Initialize the FOP factory with a path to the folder with resources,
     * so these resources can be used when building PDFs.
     */
    static {
        try {
            FOP_FACTORY = FopFactory.newInstance(RequestPDF.class.getResource("/").toURI());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e); // Should not fail, if so, throw a runtime exception
        }
    }

    /**
     * Creates and returns a PDF file.
     *
     * @param templateName The name of the template to use.
     * @param model        The model.
     * @param locale       The locale.
     * @return The array of bytes forming the PDF.
     * @throws TemplatePreparationException Thrown when the building goes wrong.
     */
    protected byte[] getPDF(String templateName, Model model, Locale locale) throws TemplatePreparationException {
        return createPDF(templateToString(templateName, model, locale));
    }

    /**
     * Creates a PDF using Apache FOP.
     *
     * @param view The view to transform.
     * @return The array of bytes forming the PDF.
     * @throws TemplatePreparationException Thrown when the building goes wrong.
     */
    private byte[] createPDF(String view) throws TemplatePreparationException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedOutputStream bufOut = new BufferedOutputStream(out);

        try {
            Fop fop = FOP_FACTORY.newFop(MimeConstants.MIME_PDF, bufOut);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            Result result = new SAXResult(fop.getDefaultHandler());
            Source source = new StreamSource(new StringReader(view));

            transformer.transform(source, result);
        }
        catch (FOPException | TransformerException e) {
            throw new TemplatePreparationException(e);
        }
        finally {
            try {
                bufOut.flush();
                bufOut.close();
            }
            catch (IOException e) {
                throw new TemplatePreparationException(e);
            }
        }

        return out.toByteArray();
    }
}
