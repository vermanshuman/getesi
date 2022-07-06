package it.nexera.ris.common.helpers;

import it.nexera.ris.persistence.beans.entities.domain.WLGInbox;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Address;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

public class TransportLis implements TransportListener {

    private static Log log = LogFactory.getLog(TransportLis.class);

    public static final String MAIL_CC = "mailNotifyDeliveredHeader";

    public static final String MAIL_BODY = "mailNotifyDeliveredBody";

    private String emailFrom;

    public TransportLis() {
    }

    public TransportLis(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    @Override
    public void messageDelivered(TransportEvent transportEvent) {
        try {
            String mailTo = transportEvent.getMessage().getFrom()[0].toString();
            WLGInbox mail = new WLGInbox();
            mail.setEmailFrom(getEmailFrom());
            mail.setEmailTo(mailTo);
            mail.setEmailCC(ResourcesHelper.getString(MAIL_CC));
            mail.setEmailBCC(ResourcesHelper.getString(MAIL_CC));
            mail.setEmailSubject(ResourcesHelper.getString(MAIL_CC).toUpperCase());
            for (Address address : transportEvent.getMessage().getAllRecipients()) {
                mail.setEmailBody(ResourcesHelper.getString(MAIL_BODY) + " :" + address.toString() + "; ");
            }
            MailHelper.sendMail(mail, null);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    public void messageNotDelivered(TransportEvent transportEvent) {


    }

    @Override
    public void messagePartiallyDelivered(TransportEvent transportEvent) {

    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }
}
