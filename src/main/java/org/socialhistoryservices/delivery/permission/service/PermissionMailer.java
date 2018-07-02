package org.socialhistoryservices.delivery.permission.service;

import org.socialhistoryservices.delivery.util.Mailer;
import org.socialhistoryservices.delivery.permission.entity.Permission;
import org.socialhistoryservices.delivery.permission.entity.RecordPermission;
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
     * refused. If all the records were denied access, the requester will
     * receive a mail without an access code. Else a mail with an access code
     * will be sent.
     * @param pm The permission to use for composing the mail.
     * @throws org.springframework.mail.MailException Thrown when sending
     * mail somehow failed.
     */
    public void mailCode(Permission pm) throws MailException {
        // Do not mail when mail is disabled.
        if (!deliveryProperties.isMailEnabled()) {
            return;
        }

        Model model = new ExtendedModelMap();
        model.addAttribute("permission", pm);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(deliveryProperties.getMailSystemAddress());
        msg.setTo(pm.getEmail());
        msg.setReplyTo(getMessage("iisg.email", ""));

        // Be sure the recipient will receive the message in their language,
        // instead of the language of the employee activating this mail
        // function.
        Locale rl = pm.getRequestLocale();
        model.addAttribute("locale", rl.toString());

        // Set content and title based on which template to send.
        if (isCodeEligible(pm)) {
            msg.setSubject(getMessage("permissionMail.approvedSubject",
                                 "Delivery: Permission Request Approved",
                                 rl));
            msg.setText(templateToString("mail/permission_approved.mail.ftl",
                        model, rl));

        }
        else {
            msg.setSubject(getMessage("permissionMail.refusedSubject",
                    "Delivery: Permission Request Refused", rl));
            msg.setText(templateToString("mail/permission_refused.mail.ftl",
                        model, rl));
        }
        mailSender.send(msg);
    }

    /**
     * Check whether to send a permission code or not. Only send one if there
     * is at least one record granted permission.
     * @param pm The permission to check.
     * @return Whether to send a permission code (true) or not (false).
     */
    private boolean isCodeEligible(Permission pm) {
        for (RecordPermission rp : pm.getRecordPermissions()) {
            if (rp.getGranted()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mails a confirmation to the requester to tell the permission request
     * was successfully received.
     * @param pm The permission to use for composing the mail.
     * @throws org.springframework.mail.MailException Thrown when sending
     * mail somehow failed.
     */
    public void mailConfirmation(Permission pm) throws MailException {
        // Do not mail when mail is disabled.
        if (!deliveryProperties.isMailEnabled()) {
            return;
        }

        Model model = new ExtendedModelMap();
        model.addAttribute("permission", pm);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(deliveryProperties.getMailSystemAddress());
        msg.setTo(pm.getEmail());
        msg.setReplyTo(getMessage("iisg.email", ""));

        msg.setSubject(getMessage("permissionMail.confirmationSubject",
                             "Delivery: Permission Request Confirmation"));
        msg.setText(templateToString("mail/permission_confirmation.mail.ftl",
                    model));

        mailSender.send(msg);
    }

    /**
     * Mails the reading room to inform them of a new permission request.
     * @param pm The permission to use for composing the mail.
     * @throws org.springframework.mail.MailException Thrown when sending mail somehow failed.
     */
    public void mailReadingRoom(Permission pm) throws MailException {
        // Do not mail when mail is disabled.
        if (!deliveryProperties.isMailEnabled()) {
            return;
        }

        Model model = new ExtendedModelMap();
        model.addAttribute("permission", pm);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(deliveryProperties.getMailSystemAddress());
        msg.setTo(deliveryProperties.getMailReadingRoom());

        msg.setSubject(getMessage("permissionMail.readingRoomSubject", "New permission request"));
        msg.setText(templateToString("mail/permission_readingroom.mail.ftl", model));

        mailSender.send(msg);
    }
}
