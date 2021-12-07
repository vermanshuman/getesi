package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.DocumentTagEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "request_print")
public class RequestPrint extends DocumentTagEntity {

    private static final long serialVersionUID = -7660169328637255515L;

    @Column(name = "body_content", columnDefinition = "LONGTEXT")
    private String bodyContent;

    @ManyToOne
    @JoinColumn(name = "document_template_id")
    private DocumentTemplate template;

    @Column(name = "show_all_properties")
    private Boolean showAllProperties;

    @Column(name = "show_all_syn_formalities")
    private Boolean showAllSyntheticFormalities;

    @Column(name = "show_old_syn_formalities")
    private Boolean showOldSynFormalities;

    @OneToOne(mappedBy = "requestPrint")
    private Request request;

    @Column(name = "need_validate_cadastral")
    private Boolean needValidateCadastral;

    @Column(name = "document_attached")
    private Boolean documentAttached;

    @Column(name = "email_opened")
    private Boolean emailOpened;

    @Column(name = "email_evasion_notification_sent")
    private Boolean emailEvasionNotificationSent;

    public boolean isBodySaved() {
        return !ValidationHelper.isNullOrEmpty(getBodyContent());
    }

    @Override
    public Subject getSubject() {
        if (getRequest() != null) {
            return getRequest().getSubject();
        }

        return null;
    }

    @Override
    public Client getClient() {
        if (getRequest() != null) {
            return getRequest().getClient();
        }

        return null;
    }

    @Override
    public AggregationLandChargesRegistry getAggregationLandChargesRegistry() {
        if (getRequest() != null) {
            return getRequest().getAggregationLandChargesRegistry();
        }

        return null;
    }

    @Override
    public RequestPrint clone() throws CloneNotSupportedException {
        RequestPrint requestPrintClone = new RequestPrint();
        requestPrintClone.setBodyContent(this.getBodyContent());
        requestPrintClone.setTemplate(this.getTemplate());
        requestPrintClone.setShowAllProperties(this.getShowAllProperties());
        requestPrintClone.setShowAllSyntheticFormalities(this.getShowAllSyntheticFormalities());
        requestPrintClone.setShowOldSynFormalities(this.getShowOldSynFormalities());
        requestPrintClone.setRequest(this.getRequest());
        requestPrintClone.setNeedValidateCadastral(this.getNeedValidateCadastral());
        requestPrintClone.setDocumentAttached(this.getDocumentAttached());
        requestPrintClone.setEmailOpened(this.getEmailOpened());
        requestPrintClone.setEmailEvasionNotificationSent(this.getEmailEvasionNotificationSent());
        return requestPrintClone;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public DocumentTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DocumentTemplate template) {
        this.template = template;
    }

    public Boolean getShowAllProperties() {
        return showAllProperties;
    }

    public void setShowAllProperties(Boolean showAllProperties) {
        this.showAllProperties = showAllProperties;
    }

    public Boolean getShowAllSyntheticFormalities() {
        return showAllSyntheticFormalities;
    }

    public void setShowAllSyntheticFormalities(
            Boolean showAllSyntheticFormalities) {
        this.showAllSyntheticFormalities = showAllSyntheticFormalities;
    }

    public Boolean getShowOldSynFormalities() {
        return showOldSynFormalities;
    }

    public void setShowOldSynFormalities(Boolean showOldSynFormalities) {
        this.showOldSynFormalities = showOldSynFormalities;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Boolean getNeedValidateCadastral() {
        return needValidateCadastral;
    }

    public void setNeedValidateCadastral(Boolean needValidateCadastral) {
        this.needValidateCadastral = needValidateCadastral;
    }

    public Boolean getDocumentAttached() {
        return documentAttached;
    }

    public void setDocumentAttached(Boolean documentAttached) {
        this.documentAttached = documentAttached;
    }

    public Boolean getEmailOpened() {
        return emailOpened;
    }

    public void setEmailOpened(Boolean emailOpened) {
        this.emailOpened = emailOpened;
    }

    public Boolean getEmailEvasionNotificationSent() {
        return emailEvasionNotificationSent;
    }

    public void setEmailEvasionNotificationSent(Boolean emailEvasionNotificationSent) {
        this.emailEvasionNotificationSent = emailEvasionNotificationSent;
    }
}
