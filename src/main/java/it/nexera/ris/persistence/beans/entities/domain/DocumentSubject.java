package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "document_subject")
public class DocumentSubject extends IndexedEntity {

    private static final long serialVersionUID = 788643414587313723L;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "dic_land_chares_registry")
    private LandChargesRegistry office;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(name = "date")
    private Date date;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType type;

    @Column(name = "use_subject_from_xml")
    private Boolean useSubjectFromXml;

    public DocumentSubject() {
    }

    public DocumentSubject(Document document, Subject subject, LandChargesRegistry registry, Province province, Date date, DocumentType type) {
        this.document = document;
        this.subject = subject;
        this.province = province;
        this.date = date;
        this.type = type;
        this.office = registry;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public LandChargesRegistry getOffice() {
        return office;
    }

    public void setOffice(LandChargesRegistry office) {
        this.office = office;
    }

    public Boolean getUseSubjectFromXml() {
        return useSubjectFromXml;
    }

    public void setUseSubjectFromXml(Boolean useSubjectFromXml) {
        this.useSubjectFromXml = useSubjectFromXml;
    }
}
