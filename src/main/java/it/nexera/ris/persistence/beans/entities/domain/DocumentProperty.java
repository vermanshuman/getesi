package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "document_property")
public class DocumentProperty extends IndexedEntity {

    private static final long serialVersionUID = 2666050920892220381L;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(name = "office")
    private String office;

    @Column(name = "report_date")
    private Date reportDate;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(name = "date")
    private Date date;

    public DocumentProperty() {
    }

    public DocumentProperty(Document document, Property property, String office, Date reportDate, Province province, Date date) {
        this.document = document;
        this.property = property;
        this.office = office;
        this.reportDate = reportDate;
        this.province = province;
        this.date = date;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
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
}
