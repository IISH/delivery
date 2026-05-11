package nl.knaw.huc.di.delivery.reproduction.service;

import nl.knaw.huc.di.delivery.config.TemplatePreparationException;
import nl.knaw.huc.di.delivery.reproduction.entity.Reproduction;
import nl.knaw.huc.di.delivery.request.service.RequestPDF;
import org.springframework.stereotype.Service;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Locale;

/**
 * Creates an invoice of a reproduction in PDF format.
 */
@Service
public class ReproductionPDF extends RequestPDF {
    /**
     * Create an invoice of the given reproduction.
     *
     * @param reproduction The reproduction.
     * @return The bytestream forming the PDF with the invoice.
     * @throws TemplatePreparationException Thrown when it fails to produce a PDF.
     */
    public byte[] getInvoice(Reproduction reproduction, Locale locale) throws TemplatePreparationException {
        return getPDF("pdf/reproduction_invoice.pdf.ftl", getReproductionModel(reproduction), locale);
    }

    /**
     * Returns a model with the reproduction.
     *
     * @param reproduction The reproduction.
     * @return A model with the reproduction.
     */
    private Model getReproductionModel(Reproduction reproduction) {
        Model model = new ExtendedModelMap();
        model.addAttribute("reproduction", reproduction);
        return model;
    }
}
