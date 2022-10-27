package org.socialhistoryservices.delivery.permission.service;

import org.socialhistoryservices.delivery.util.Mailer;
import org.socialhistoryservices.delivery.permission.entity.Permission;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Locale;

/**
 * Mailer to send confirmation mails when creating reservations.
 */
@Service
public class PermissionMailer extends Mailer {

    /**
     * Mails a requester when the permission request has been approved or
     * refused. If the permission request was denied access, the requester will
     * receive a mail without an access code. Else a mail with an access code will be sent.
     *
     * @param pm The permission to use for composing the mail.
     * @throws MailException Thrown when sending mail somehow failed.
     */
    public void mailPermissionOutcome(Permission pm) throws MailException {
        // Do not mail when mail is disabled or when there is no outcome yet
        if (!deliveryProperties.isMailEnabled() || pm.getDateGranted() == null) {
            return;
        }

        Model model = new ExtendedModelMap();
        model.addAttribute("permission", pm);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(deliveryProperties.getMailSystemAddressReadingRoom());
        msg.setTo(pm.getEmail());
        msg.setReplyTo(deliveryProperties.getMailReadingRoom());

        // Be sure the recipient will receive the message in their language,
        // instead of the language of the employee activating this mail function
        Locale rl = pm.getRequestLocale();
        model.addAttribute("locale", rl.toString());

        if (pm.getGranted()) {
            msg.setSubject(getMessage("permissionMail.approvedSubject",
                    "Delivery: Permission Request Approved", rl));
            msg.setText(templateToString("mail/permission_approved.mail.ftl", model, rl));
        }
        else {
            msg.setSubject(getMessage("permissionMail.refusedSubject",
                    "Delivery: Permission Request Refused", rl));
            msg.setText(templateToString("mail/permission_refused.mail.ftl", model, rl));
        }

        mailSender.send(msg);
    }

    /**
     * Mails a confirmation to the requester to tell the permission request
     * was successfully received.
     *
     * @param pm The permission to use for composing the mail.
     * @throws MailException Thrown when sending mail somehow failed.
     */
    public void mailConfirmation(Permission pm) throws MailException {
        // Do not mail when mail is disabled.
        if (!deliveryProperties.isMailEnabled()) {
            return;
        }

        Model model = new ExtendedModelMap();
        model.addAttribute("permission", pm);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(deliveryProperties.getMailSystemAddressReadingRoom());
        msg.setTo(pm.getEmail());
        msg.setReplyTo(deliveryProperties.getMailReadingRoom());

        msg.setSubject(getMessage("permissionMail.confirmationSubject",
                "Delivery: Permission Request Confirmation"));
        msg.setText(templateToString("mail/permission_confirmation.mail.ftl", model));

        mailSender.send(msg);
    }

    /**
     * Mails the reading room to inform them of a new permission request.
     *
     * @param pm The permission to use for composing the mail.
     * @throws MailException Thrown when sending mail somehow failed.
     */
    public void mailReadingRoom(Permission pm) throws MailException {
        // Do not mail when mail is disabled.
        if (!deliveryProperties.isMailEnabled()) {
            return;
        }

        Model model = new ExtendedModelMap();
        model.addAttribute("permission", pm);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(deliveryProperties.getMailSystemAddressReadingRoom());
        msg.setTo(deliveryProperties.getMailReadingRoom());

        msg.setSubject(getMessage("permissionMail.readingRoomSubject", "New permission request"));
        msg.setText(templateToString("mail/permission_readingroom.mail.ftl", model));

        mailSender.send(msg);
    }
}
