package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "mail_template")
public class MailTemplate extends IndexedEntity {

    private static final long serialVersionUID = -8726327367282596003L;

    @Column(name = "mail_subject")
    private String mailSubject;

    @Column(name = "mail_body_html")
    private String mailBodyHtml;

    @Column(name = "template_type")
    private String templateType;

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getMailBodyHtml() {
        return mailBodyHtml;
    }

    public void setMailBodyHtml(String mailBodyHtml) {
        this.mailBodyHtml = mailBodyHtml;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }
}
