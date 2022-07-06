package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.RelationshipType;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "relationship")
public class Relationship extends IndexedEntity {

    private static final long serialVersionUID = -7937802857166154807L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(name = "relationship_type_id")
    private Long relationshipTypeId;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "quota")
    private String quote;

    @Column(name = "property_type")
    private String propertyType;

    @Column(name = "derivata_data", length = 500)
    private String derivataData;

    @Column(name = "act_code", length = 50)
    private String actCode;

    @ManyToOne
    @JoinColumn(name = "table_id", insertable = false, updatable = false)
    private Document document;

    @Column(name = "cadastral_date")
    private Date cadastralDate;

    @Column(name = "section_c_type")
    private String sectionCType;

    @Column(name = "unita_neg")
    private String unitaNeg;

    @ManyToOne
    @JoinColumn(name = "formality_id")
    private Formality formality;

    @Column(name = "regime")
    private String regime;

    @Transient
    private boolean isNew;

    @Transient
    private String text;
    
    @Transient
    private boolean delete;

    public Relationship() {
    }

    public Relationship(Relationship other) {
        this.subject = other.subject;
        this.property = other.property;
        this.relationshipTypeId = other.relationshipTypeId;
        this.tableId = other.tableId;
        this.quote = other.quote;
        this.propertyType = other.propertyType;
        this.derivataData = other.derivataData;
        this.actCode = other.actCode;
        this.document = other.document;
        this.cadastralDate = other.cadastralDate;
        this.sectionCType = other.sectionCType;
        this.unitaNeg = other.unitaNeg;
        this.formality = other.formality;
        this.regime = other.regime;
        this.isNew = other.isNew;
    }

    public Relationship(Subject subject, Property property, Document document, String quote, String typeReport,
                        String regime) {
        setPropertyType(typeReport);
        setQuote(quote);
        setRegime(regime);
        setDerivataData(property.getArisingFromData());
        setProperty(property);
        setSubject(subject);
        setTableId(document.getId());
        setRelationshipTypeId(RelationshipType.CADASTRAL_DOCUMENT.getId());
    }

    public String getDocumentTitle() {
        return getDocument() == null ? "" : getDocument().getTitle();
    }

    public String getDocumentPath() {
        return getDocument() == null ? ""
                : getDocument().getPath().replace("\\", "\\\\");
    }

    public String getPropertyReportStr() {
        if (!ValidationHelper.isNullOrEmpty(getRegime())) {
            return String.format("%s per %s (%s)", getPropertyType(), getQuote(), getRegime());
        } else {
            return String.format("%s per %s", getPropertyType(), getQuote());
        }
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Long getRelationshipTypeId() {
        return relationshipTypeId;
    }

    public void setRelationshipTypeId(Long relationshipTypeId) {
        this.relationshipTypeId = relationshipTypeId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getDerivataData() {
        return derivataData;
    }

    public void setDerivataData(String derivataData) {
        this.derivataData = derivataData;
    }

    public String getActCode() {
        return actCode;
    }

    public void setActCode(String actCode) {
        this.actCode = actCode;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Date getCadastralDate() {
        return cadastralDate;
    }

    public void setCadastralDate(Date cadastralDate) {
        this.cadastralDate = cadastralDate;
    }

    public String getSectionCType() {
        return sectionCType;
    }

    public void setSectionCType(String sectionCType) {
        this.sectionCType = sectionCType;
    }

    public String getUnitaNeg() {
        return unitaNeg;
    }

    public void setUnitaNeg(String unitaNeg) {
        this.unitaNeg = unitaNeg;
    }

    public Formality getFormality() {
        return formality;
    }

    public void setFormality(Formality formality) {
        this.formality = formality;
    }

    public String getRegime() {
        return regime;
    }

    public void setRegime(String regime) {
        this.regime = regime;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }
}
