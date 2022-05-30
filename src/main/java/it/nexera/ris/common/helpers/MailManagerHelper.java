package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.MailManagerStatuses;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.WLGFolder;
import it.nexera.ris.persistence.beans.entities.domain.WLGInbox;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.logic.ClientEmailWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ClientWrapper;
import it.nexera.ris.web.beans.wrappers.logic.MailEditWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ReferentEmailWrapper;
import org.hibernate.Transaction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MailManagerHelper extends BaseHelper {

    public static Set<String> loadAllSelectedEmails(MailEditWrapper mailEditWrapper) {
        return loadAllSelectedEmails(mailEditWrapper.getClientEmails());
    }

    public static Set<String> loadAllSelectedEmails(List<ClientWrapper> clientEmails) {
        Set<String> emails = new HashSet<>();

        clientEmails.forEach(client -> {
            client.getPersonalEmails().stream()
                    .filter(e -> e.getSelected() && !ValidationHelper.isNullOrEmpty(e.getClientEmail().getEmail().trim()))
                    .map(clientEmailWrapper -> clientEmailWrapper.getClientEmail().getEmail()).forEach(emails::add);
            client.getAdditionalEmails().stream()
                    .filter(e -> e.getSelected() && !ValidationHelper.isNullOrEmpty(e.getClientEmail().getEmail().trim()))
                    .map(email -> email.getClientEmail().getEmail()).forEach(emails::add);
            client.getReferents().stream()
                    .filter(r -> r.getSelected() && !ValidationHelper.isNullOrEmpty(r.getReferent().getEmail().trim()))
                    .map(r -> r.getReferent().getEmail()).forEach(emails::add);
        });

        return emails;
    }

    public static String generateSelectedEmailsStr(Set<String> selectedEmails) {
        StringBuffer result = new StringBuffer();
        selectedEmails.forEach(email -> {
            result.append(email);
            result.append(", ");
        });
        return result.length() > 0 ? result.toString().substring(0, result.length() - 2) : "";
    }

    public static void saveClientMailChanges(List<ClientWrapper> clientWrapperList) {
        Transaction tr = null;
        try {
            tr = DaoManager.getSession().beginTransaction();
            for (ClientWrapper cw : clientWrapperList) {
                for (ReferentEmailWrapper rw : cw.getReferents()) {
                    if (rw.getDeleted() != null && rw.getDeleted()) {
                        if (rw.getReferent().getId() != null) {
                            DaoManager.remove(rw.getReferent());
                        }
                    } else {
                        if (!ValidationHelper.isNullOrEmpty(rw.getReferent().getEmail())) {
                            DaoManager.save(rw.getReferent());
                        }
                    }
                }
                for (ClientEmailWrapper ew : cw.getPersonalEmails()) {
                    if (ew.getDelete() != null && ew.getDelete()) {
                        if (ew.getClientEmail().getId() != null) {
                            DaoManager.remove(ew.getClientEmail());
                        }
                    } else {
                        if (!ValidationHelper.isNullOrEmpty(ew.getClientEmail().getEmail())) {
                            DaoManager.save(ew.getClientEmail());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        } finally {
            if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                tr.commit();
            }
        }
    }

    public static String generateNewEmailToStr(String oldEmailTo, Set<String> selectedEmails) {
        StringBuffer emailsTo = oldEmailTo == null
                ? new StringBuffer()
                : new StringBuffer(oldEmailTo);

        if (emailsTo.length() > 0) {
            emailsTo.append(", ");
        }
        emailsTo.append(MailManagerHelper.generateSelectedEmailsStr(selectedEmails));
        return emailsTo.toString();
    }

    public static void changeFolder(WLGInbox inbox, WLGFolder folder, Long currentUserID)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        WLGInbox mail = DaoManager.get(WLGInbox.class, inbox.getId());
        DaoManager.getSession().refresh(mail);
        DaoManager.getSession().refresh(folder);
        if (mail != null) {
            mail.setFolder(folder);
            mail.setUserChangedFolder(DaoManager.get(User.class, currentUserID));
            if (folder.getDefaultFolder() != null && folder.getDefaultFolder()) {
                switch (folder.getMailType()) {
                    case SENT:
                        mail.setServerId(Long.parseLong(ApplicationSettingsHolder.getInstance()
                                .getByKey(ApplicationSettingsKeys.SENT_SERVER_ID).getValue()));
                        break;

                    case RECEIVED:
                        mail.setServerId(Long.parseLong(ApplicationSettingsHolder.getInstance()
                                .getByKey(ApplicationSettingsKeys.RECEIVED_SERVER_ID).getValue()));
                        break;

                    case DRAFT:
                        mail.setServerId(null);
                        mail.setState(MailManagerStatuses.NEW.getId());
                        log.info("setting mail :: "+mail.getId() + " state to :: "+MailManagerStatuses.findById(mail.getState())
                                + " by user:: "+ UserHolder.getInstance().getCurrentUser().getId());
                        break;

                    case STORAGE:
                        mail.setState(MailManagerStatuses.DELETED.getId());
                        log.info("setting mail :: "+mail.getId() + " state to :: "+MailManagerStatuses.findById(mail.getState())
                                + " by user:: "+UserHolder.getInstance().getCurrentUser().getId());
                        break;
                }
            }
            DaoManager.save(mail, true);
        }
    }
}
