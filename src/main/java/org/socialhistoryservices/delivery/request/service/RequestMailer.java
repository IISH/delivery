package org.socialhistoryservices.delivery.request.service;

import org.socialhistoryservices.delivery.util.Mailer;
import org.socialhistoryservices.delivery.request.entity.Request;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.ui.Model;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.Locale;

/**
 * Mailer to send information/confirmation mails dealing with requests.
 */
public abstract class RequestMailer extends Mailer {
    /**
     * Send information/confirmation mails to the reading room.
     *
     * @param subject      The subject of the email.
     * @param templateName The name of the template to use.
     * @param model        The model.
     * @param locale       The locale.
     * @throws MailException Thrown when sending mail somehow failed.
     */
    protected void sendMail(String subject, String templateName, Model model, Locale locale) throws MailException {
        sendMail(deliveryProperties.getMailReadingRoom(), subject, templateName, model, locale);
    }

    /**
     * Send information/confirmation mails dealing with requests
     *
     * @param request      The request in question.
     * @param subject      The subject of the email.
     * @param templateName The name of the template to use.
     * @param model        The model.
     * @param locale       The locale.
     * @throws MailException Thrown when sending mail somehow failed.
     */
    protected void sendMail(Request request, String subject, String templateName, Model model, Locale locale)
            throws MailException {
        sendMail(request.getEmail(), subject, templateName, model, locale);
    }

    /**
     * Send information/confirmation mails.
     *
     * @param to           The recipient.
     * @param subject      The subject of the email.
     * @param templateName The name of the template to use.
     * @param model        The model.
     * @param locale       The locale.
     * @throws MailException Thrown when sending mail somehow failed.
     */
    private void sendMail(String to, String subject, String templateName, Model model, Locale locale)
            throws MailException {
        // Do not mail when mail is disabled.
        if (!deliveryProperties.isMailEnabled()) {
            return;
        }

        model.addAttribute("locale", locale.toString());

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(deliveryProperties.getMailSystemAddress());
        msg.setTo(to);
        msg.setReplyTo(getMessage("iisg.email", ""));

        msg.setSubject(profile() + subject);
        msg.setText(templateToString(templateName, model, locale));

        mailSender.send(msg);
    }

    /**
     * Send mail with PDF attachment.
     *
     * @param request      The request in question.
     * @param subject      The subject of the email.
     * @param templateName The name of the template to use.
     * @param pdf          The PDF attachment.
     * @param pdfName      The name of the PDF attachment.
     * @param model        The model.
     * @param locale       The locale.
     * @throws MailException      Thrown when sending mail somehow failed.
     * @throws MessagingException Thrown when sending mail somehow failed.
     */
    protected void sendMailWithPdf(Request request, String subject, String templateName, byte[] pdf, String pdfName,
                                   Model model, Locale locale) throws MailException, MessagingException {
        sendMailWithPdf(request.getEmail(), subject, templateName, pdf, pdfName, model, locale);
    }

    /**
     * Send mail with PDF attachment.
     *
     * @param to           The recipient.
     * @param subject      The subject of the email.
     * @param templateName The name of the template to use.
     * @param pdf          The PDF attachment.
     * @param pdfName      The name of the PDF attachment.
     * @param model        The model.
     * @param locale       The locale.
     * @throws MailException      Thrown when sending mail somehow failed.
     * @throws MessagingException Thrown when sending mail somehow failed.
     */
    private void sendMailWithPdf(String to, String subject, String templateName, byte[] pdf, String pdfName,
                                 Model model, Locale locale) throws MailException, MessagingException {
        // Do not mail when mail is disabled.
        if (!deliveryProperties.isMailEnabled()) {
            return;
        }

        model.addAttribute("locale", locale.toString());

        MimeMessage msg = mailSender.createMimeMessage();
        msg.setFrom(new InternetAddress(deliveryProperties.getMailSystemAddress()));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setReplyTo(InternetAddress.parse(getMessage("iisg.email", "")));
        msg.setSubject(profile() + subject);

        // The mail wil consists of multiple parts, the content and the PDF attachment
        MimeMultipart mimeMultipart = new MimeMultipart();

        // First add the content
        MimeBodyPart content = new MimeBodyPart();
        content.setText(templateToString(templateName, model, locale));
        mimeMultipart.addBodyPart(content);

        // Secondly add the attachment
        MimeBodyPart attachment = new MimeBodyPart();
        attachment.setFileName(pdfName);
        attachment.setDataHandler(new DataHandler(new ByteArrayDataSource(pdf, "application/pdf")));
        mimeMultipart.addBodyPart(attachment);

        // Add the multiple parts as content to the email message
        msg.setContent(mimeMultipart);

        mailSender.send(msg);
    }

    /**
     * Set a value to indicate the environment
     *
     * @return The environment - in any
     */
    private String profile() {
        final String profile = deliveryProperties.getProfile();
        if (profile == null || profile.isEmpty()) {
            return "";
        } else
            return profile + " ";
    }
}
