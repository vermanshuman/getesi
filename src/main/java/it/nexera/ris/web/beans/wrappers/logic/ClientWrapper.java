package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.EmailType;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.ClientEmail;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClientWrapper implements Serializable {

    private static final long serialVersionUID = -1248890494296732494L;

    private transient final Log log = LogFactory
            .getLog(ClientWrapper.class);

    private String clientName;

    private String emailPEC;

    private List<ClientEmailWrapper> additionalEmails;

    private List<ClientEmailWrapper> personalEmails;

    private Client client;

    private Boolean selectedEmail;

    private Boolean selectedPEC;

    private Boolean addingNewMail;

    private Boolean hide;

    private Boolean addReferent;

    private Boolean addPersonalEmail;

    private String newEmail;

    private List<ReferentEmailWrapper> referents;

    public ClientWrapper(Client client) {
        this.client = client;
        this.clientName = client.toString();
        this.emailPEC = client.getMailPEC();
        this.additionalEmails = new ArrayList<>();
        this.personalEmails = new ArrayList<>();
        this.referents = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(client.getAdditionalEmails())) {
            client.getAdditionalEmails()
                    .forEach(cMail -> additionalEmails
                            .add(new ClientEmailWrapper(cMail)));
        }

        if (!ValidationHelper.isNullOrEmpty(client.getPersonalEmails())) {
            client.getPersonalEmails().forEach(
                    cMail -> personalEmails.add(new ClientEmailWrapper(cMail)));
        }

        if (!ValidationHelper.isNullOrEmpty(client.getReferents())) {
            client.getReferents()
                    .forEach(r -> referents.add(new ReferentEmailWrapper(r)));
        }
    }

    public void saveNewMail() {
        if (!ValidationHelper.isNullOrEmpty(getNewEmail())
                && getAddingNewMail()) {
            setAddingNewMail(Boolean.FALSE);

            ClientEmail mail = new ClientEmail();

            mail.setClient(getClient());
            mail.setEmail(getNewEmail());
            mail.setTypeId(EmailType.ADDITIONAL.getId());

            try {
                DaoManager.save(mail, true);
                getAdditionalEmails().add(new ClientEmailWrapper(mail));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void cancelSaveNewMail() {
        setAddingNewMail(Boolean.FALSE);
        setNewEmail("");
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getEmailPEC() {
        return emailPEC;
    }

    public void setEmailPEC(String emailPEC) {
        this.emailPEC = emailPEC;
    }

    public List<ClientEmailWrapper> getAdditionalEmails() {
        return additionalEmails;
    }

    public void setAdditionalEmails(List<ClientEmailWrapper> additionalEmails) {
        this.additionalEmails = additionalEmails;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Boolean getSelectedEmail() {
        return selectedEmail == null ? Boolean.FALSE : selectedEmail;
    }

    public void setSelectedEmail(Boolean selectedEmail) {
        this.selectedEmail = selectedEmail;
    }

    public Boolean getSelectedPEC() {
        return selectedPEC == null ? Boolean.FALSE : selectedPEC;
    }

    public void setSelectedPEC(Boolean selectedPEC) {
        this.selectedPEC = selectedPEC;
    }

    public Boolean getAddingNewMail() {
        return addingNewMail == null ? Boolean.FALSE : addingNewMail;
    }

    public void setAddingNewMail(Boolean addingNewMail) {
        this.addingNewMail = addingNewMail;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public List<ReferentEmailWrapper> getNotDeletedReferents() {
        if(!ValidationHelper.isNullOrEmpty(referents)) {
            return referents.stream().filter(rw -> rw.getDeleted() == null || !rw.getDeleted()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public List<ReferentEmailWrapper> getReferents() {
        return referents;
    }

    public void setReferents(List<ReferentEmailWrapper> referents) {
        this.referents = referents;
    }

    public List<ClientEmailWrapper> getPersonalEmails() {
        return personalEmails;
    }

    public List<ClientEmailWrapper> getNotDeletedPersonalEmails() {
        if(!ValidationHelper.isNullOrEmpty(personalEmails)) {
            List<ClientEmailWrapper> list = new ArrayList<>();
            for (ClientEmailWrapper rw : personalEmails) {
                if (rw.getDelete() == null || !rw.getDelete()) {
                    list.add(rw);
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

    public void setPersonalEmails(List<ClientEmailWrapper> personalEmails) {
        this.personalEmails = personalEmails;
    }

    public Boolean getHide() {
        return hide;
    }

    public void setHide(Boolean hide) {
        this.hide = hide;
    }

    public Boolean getAddReferent() {
        return addReferent;
    }

    public void setAddReferent(Boolean addReferent) {
        this.addReferent = addReferent;
    }

    public Boolean getAddPersonalEmail() {
        return addPersonalEmail;
    }

    public void setAddPersonalEmail(Boolean addPersonalEmail) {
        this.addPersonalEmail = addPersonalEmail;
    }
}
