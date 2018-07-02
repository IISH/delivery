package org.socialhistoryservices.delivery.reproduction.service;

import org.socialhistoryservices.delivery.util.TemplatePreparationException;
import org.socialhistoryservices.delivery.api.SharedObjectRepositoryService;
import org.socialhistoryservices.delivery.api.SorMetadata;
import org.socialhistoryservices.delivery.config.DeliveryProperties;
import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction;
import org.socialhistoryservices.delivery.reproduction.entity.ReproductionStandardOption;
import org.socialhistoryservices.delivery.request.service.RequestMailer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.stereotype.Service;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import java.util.*;

/**
 * Mailer to send information/confirmation mails dealing with reproductions.
 */
@Service
public class ReproductionMailer extends RequestMailer {
    @Autowired
    private ReproductionPDF reproductionPDF;

    @Autowired
    private SharedObjectRepositoryService sorService;

    @Autowired
    private DeliveryProperties deliveryProperties;

    private static final Locale ENGLISH_LOCALE = StringUtils.parseLocaleString("en");

    /**
     * Mail a pending confirmation message to a customer who has just created a reproduction.
     * Also mail an email to the reading room for filling out the blanks in the offer.
     *
     * @param reproduction The reproduction to extract mail details from.
     * @throws MailException Thrown when sending mail somehow failed.
     */
    public void mailPending(Reproduction reproduction) throws MailException {
        assert reproduction.getStatus() == Reproduction.Status.WAITING_FOR_ORDER_DETAILS :
            "Can only mail pending when Reproduction status is WAITING_FOR_ORDER_DETAILS";

        Model model = getReproductionModel(reproduction);

        // Send an email to the customer
        String subjectCustomer = getMessage("reproductionMail.pendingSubjectCustomer", "Confirmation of reproduction",
            reproduction.getRequestLocale());
        sendMail(reproduction, subjectCustomer, "mail/reproduction_pending_customer.mail.ftl",
            model, reproduction.getRequestLocale());

        // Send an email to the reading room
        String subjectReadingRoom = getMessage("reproductionMail.pendingSubjectReadingRoom",
            "New reproduction waiting for offer", ENGLISH_LOCALE);
        sendMail(subjectReadingRoom, "mail/reproduction_pending_readingroom.mail.ftl", model, ENGLISH_LOCALE);
    }

    /**
     * Mail an offer ready message for a reproduction to the customer.
     *
     * @param reproduction The reproduction to extract mail details from.
     * @throws MailException Thrown when sending mail somehow failed.
     */
    public void mailOfferReady(Reproduction reproduction) throws MailException {
        assert reproduction.getStatus() == Reproduction.Status.HAS_ORDER_DETAILS :
            "Can only mail order ready when Reproduction status is HAS_ORDER_DETAILS";

        String subject = getMessage("reproductionMail.offerReadySubject",
            "Confirmation of reproduction - Your offer is ready", reproduction.getRequestLocale());
        sendMail(reproduction, subject, "mail/reproduction_offer_ready.mail.ftl", getReproductionModel(reproduction),
            reproduction.getRequestLocale());
    }

    /**
     * Mail a reminder message for a reproduction to the customer.
     *
     * @param reproduction The reproduction to extract mail details from.
     * @throws MailException Thrown when sending mail somehow failed
     */
    public void mailReminder(Reproduction reproduction) throws MailException{
        assert reproduction.getStatus() == Reproduction.Status.HAS_ORDER_DETAILS :
            "Can only mail order ready when Reproduction status is HAS_ORDER_DETAILS";

        String subject = getMessage("reproductionMail.offerReminderSubject",
            "Reminder of reproduction - Your order is not paid", reproduction.getRequestLocale());
        sendMail(reproduction, subject, "mail/reproduction_payment_reminder.mail.ftl", getReproductionModel(reproduction),
            reproduction.getRequestLocale());
    }

    /**
     * Mail payment confirmation message for a reproduction (with invoice PDF attachment) to the customer.
     * <p/>
     * Mail repro which items of an active reproduction have been sent to the printer
     * and the SOR links where the other items are available for download.
     *
     * @param reproduction The reproduction to extract mail details from.
     * @throws MailException Thrown when sending mail somehow failed.
     */
    public void mailPayedAndActive(Reproduction reproduction) throws MailException {
        try {
            assert reproduction.getStatus() == Reproduction.Status.ACTIVE :
                "Can only mail active and payed confirmation when Reproduction status is ACTIVE";

            List<HoldingReproduction> inSor = new ArrayList<>();
            List<HoldingReproduction> notInSor = new ArrayList<>();
            for (HoldingReproduction hr : reproduction.getHoldingReproductions()) {
                if (hr.isInSor())
                    inSor.add(hr);
                else
                    notInSor.add(hr);
            }

            Model model = getReproductionModel(reproduction);
            model.addAttribute("inSor", inSor);
            model.addAttribute("notInSor", notInSor);

            // Create the invoice PDF to attach
            byte[] pdf = reproductionPDF.getInvoice(reproduction, reproduction.getRequestLocale());

            // First sent the customer a confirmation email
            String subjectCustomer = getMessage("reproductionMail.payedSubject", "Confirmation of payment",
                reproduction.getRequestLocale());
            String invoiceFileName = getMessage("reproductionMail.invoice", "Invoice",
                reproduction.getRequestLocale());
            sendMailWithPdf(reproduction, subjectCustomer, "mail/reproduction_payed.mail.ftl", pdf,
                invoiceFileName + ".pdf", model, reproduction.getRequestLocale());

            // Obtain all SOR URLs
            Map<String, List<String>> urlsForHolding = getSorDownloadURLs(inSor);
            model.addAttribute("sorUrls", urlsForHolding);

            // Then sent the reading room / repro the confirmation
            String subjectRepro = getMessage("reproductionMail.activeReproductionSubject", "New active reproduction",
                ENGLISH_LOCALE);
            sendMail(subjectRepro, "mail/reproduction_active.mail.ftl", model, ENGLISH_LOCALE);
        }
        catch (TemplatePreparationException | MessagingException tpe) {
            throw new MailPreparationException(tpe);
        }
    }

    /**
     * Mail the study room when a reproduction is cancelled.
     *
     * @param reproduction The reproduction to extract mail details from.
     * @throws MailException Thrown when sending mail somehow failed.
     */
    public void mailCancelled(Reproduction reproduction) throws MailException {
        assert reproduction.getStatus() == Reproduction.Status.CANCELLED :
            "Can only mail active when Reproduction status is CANCELLED";

        String subject = getMessage("reproductionMail.cancelledReproductionSubject", "Reproduction cancelled",
            ENGLISH_LOCALE);
        sendMail(subject, "mail/reproduction_cancelled.mail.ftl", getReproductionModel(reproduction), ENGLISH_LOCALE);
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

    /**
     * Determine the SOR download URLs for the given holdings.
     *
     * @param hrs The holding reproductions.
     * @return The SOR download URLs for each holding.
     */
    private Map<String, List<String>> getSorDownloadURLs(List<HoldingReproduction> hrs) {
        String sorAddress = deliveryProperties.getSorAddress();
        String sorAccessToken = deliveryProperties.getSorAccessToken();
        Map<String, List<String>> urlsForHolding = new HashMap<>();

        for (HoldingReproduction hr : hrs) {
            List<String> urls = new ArrayList<>();
            Holding holding = hr.getHolding();

            // Obtain the metadata from the SOR
            SorMetadata sorMetadata = sorService.getMetadataForPid(holding.determinePid());
            ReproductionStandardOption.Level level = hr.getStandardOption().getLevel();

            // Determine the URLs based on their material type and content
            if (!sorMetadata.isMETS()) {
                urls.add(sorAddress + "/file/" + level.name().toLowerCase() + "/" + holding.determinePid()
                        + "?access_token=" + sorAccessToken + "&contentType=application/save");
            }
            else {
                switch (hr.getStandardOption().getMaterialType()) {
                    case BOOK:
                        if (sorMetadata.getFilePids().containsKey("archive pdf")) {
                            String pid = sorMetadata.getFilePids().get("archive pdf").get(0);
                            urls.add(sorAddress + "/file/master/" + pid + "?access_token="
                                + sorAccessToken + "&contentType=application/save");
                        }
                        else {
                            urls.add(sorAddress + "/pdf/" + holding.determinePid() + "?access_token="
                                + sorAccessToken + "&contentType=application/save");
                        }
                        break;
                    case SOUND:
                        String metsAudio = (level == ReproductionStandardOption.Level.MASTER) ? "archive audio" : "reference audio";
                        for (String pid : sorMetadata.getFilePids().get(metsAudio)) {
                            urls.add(sorAddress + "/file/" + level.name().toLowerCase() + "/" + pid + "?access_token="
                                + sorAccessToken + "&contentType=application/save");
                        }
                        break;
                    case MOVING_VISUAL:
                        String metsVideo = (level == ReproductionStandardOption.Level.MASTER) ? "archive video" : "reference video";
                        for (String pid : sorMetadata.getFilePids().get(metsVideo)) {
                            urls.add(sorAddress + "/file/" + level.name().toLowerCase() + "/" + pid + "?access_token="
                                + sorAccessToken + "&contentType=application/save");
                        }
                        break;
                    case VISUAL:
                        String metsVisual = (level == ReproductionStandardOption.Level.MASTER) ? "archive image" : "hires reference image";
                        String pid = sorMetadata.getFilePids().get(metsVisual).get(0);
                        urls.add(sorAddress + "/file/" + level.name().toLowerCase() + "/" + pid + "?access_token="
                            + sorAccessToken + "&contentType=application/save");
                        break;
                }
            }

            urlsForHolding.put(holding.getSignature(), urls);
        }

        return urlsForHolding;
    }
}
