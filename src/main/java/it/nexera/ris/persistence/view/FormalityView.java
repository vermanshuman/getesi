package it.nexera.ris.persistence.view;

import it.nexera.ris.common.annotations.View;
import it.nexera.ris.common.enums.FormalityStateType;
import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.PropertyEntityHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedView;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@javax.persistence.Entity
@Immutable
@Table(name = "formality_section_a_view")
@View(sql = FormalityView.QUERY)
public class FormalityView extends IndexedView {

    private static final long serialVersionUID = 4821519429401215529L;

    protected static transient final Log log = LogFactory.getLog(BaseHelper.class);

    public static final String CREATE_PART = "CREATE OR REPLACE VIEW formality_section_a_view ";

    public static final String FROM_PART = "from ((formality left join section_a a on ((\n" +
            "        formality.id = a.formality_id)))\n" +
            "          left join dic_land_charges_registry conservatory on ((formality.reclame_property_service_id = conservatory.id))\n" +
            "          left join dic_land_charges_registry con on ((formality.provincial_office_id = con.id)))";

    public static final String COMMON_FIELDS = "" +
            "  formality.id                  as id, " +
            "  formality.create_date         as create_date, " +
            "  formality.create_user_id      as create_user_id, " +
            "  formality.update_date         as update_date, " +
            "  formality.update_user_id      as update_user_id, " +
            "  formality.type                as type, " +
            "  formality.general_register    as general_register, " +
            "  formality.particular_register as particular_register, " +
            "  formality.document_id         as document_id, " +
            "  a.title_description           as title_description, " +
            "  (CASE \n" +
            "     WHEN (derived_from IS NOT NULL AND derived_from <> '') THEN derived_from\n" +
            "     WHEN (convention_description IS NOT NULL AND convention_description <> '') THEN convention_description\n" +
            "     WHEN (annotation_description IS NOT NULL AND annotation_description <> '') THEN annotation_description\n" +
            "     ELSE ''\n" +
            "  END)                          as natura_atto, " +
            "  conservatory.name             as conservatory_name, " +
            "  formality.presentation_date   as presentation_date ";

    public static final String WHERE_PART = "    where ((isnull(a.mortgage_species) or (a.mortgage_species = '')) and\n" +
            "             (isnull(a.convention_species) or (a.convention_species = '')))\n" +
            "         OR ((isnull(a.mortgage_species) or (a.mortgage_species = '')) and\n" +
            "             (a.convention_species is not null))\n" +
            "         OR ((a.mortgage_species is not null)) ";

    public static final String SELECT_PART = "AS SELECT "
            + "formality.reclame_property_service_id as conservatory_id, "
            + COMMON_FIELDS
            + FROM_PART
            + WHERE_PART;

    protected static final String QUERY = CREATE_PART + SELECT_PART;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "conservatory_id")
    private Long conservatoryId;

    @Column(name = "conservatory_name")
    private String conservatoryName;

    @Transient
    private LandChargesRegistry conservatory;

    @Column(name = "type")
    private String type;

    @Column(name = "general_register")
    private String generalRegister;

    @Column(name = "particular_register")
    private String particularRegister;

    @Column(name = "presentation_date")
    private Date presentationDate;

    @Column(name = "title_description")
    private String descriptionSectionA;

    @Column(name = "natura_atto")
    private String naturaAtto;

    @Transient
    private Subject currentSubject;

    @Transient
    private Boolean visible;

    @Transient
    private List<Long> presumableSubjects;

    @Transient
    private List<Property> properties;

    public TypeActEnum getTypeEnum() {
        if (ValidationHelper.isNullOrEmpty(getType())) {
            return TypeActEnum.TYPE_A;
        }

        if (getType().equalsIgnoreCase("trascrizione")) {
            return TypeActEnum.TYPE_T;
        } else if (getType().equalsIgnoreCase("iscrizione")) {
            return TypeActEnum.TYPE_I;
        } else {
            return TypeActEnum.TYPE_A;
        }
    }

    public void changeVisible() throws PersistenceBeanException, IllegalAccessException {
        setVisible(hasSelectedProperties());
    }

    public boolean hasSelectedProperties() throws PersistenceBeanException, IllegalAccessException {
        return getPropertiesLoad().stream().anyMatch(Property::getSelected);
    }

    public boolean getPropertiesAreExist() throws PersistenceBeanException, IllegalAccessException {
        return getPropertiesLoad().size() > 0;
    }

    public String getPresentationDateStr() {
        return DateTimeHelper.toString(getPresentationDate());
    }

    public LandChargesRegistry getConservatory() throws PersistenceBeanException, IllegalAccessException,
            InstantiationException {
        if (conservatory == null && conservatoryId != null) {
            conservatory = DaoManager.get(LandChargesRegistry.class, getConservatoryId());
        }
        return conservatory;
    }

    public String getSpecialSubjectType(Long id) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Formality formality = DaoManager.get(Formality.class, getId());
        return formality.getSpecialSubjectType(id);
    }

    public String getCurrentSubjectType() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Formality formality = DaoManager.get(Formality.class, getId());
        if(!ValidationHelper.isNullOrEmpty(formality)) {
            return formality.getCurrentSubjectType(getCurrentSubject());
        } else {
            return "";
        }
    }

    public String getActType() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Formality formality = DaoManager.get(Formality.class, getId());
        if(!ValidationHelper.isNullOrEmpty(formality)) {
            return formality.getActType();
        } else {
            return "";
        }

    }

    public String getStatusStr() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        return DaoManager.get(Formality.class, getId()).getStateStr();
    }

    public List<Property> getPropertiesLoad() throws PersistenceBeanException, IllegalAccessException {
        if (getProperties() == null) {
            setProperties(PropertyEntityHelper.getPropertiesByFormalityIdThroughSectionB(getId()));
        }
        return getProperties();
    }

    public void setConservatory(LandChargesRegistry conservatory) {
        this.conservatory = conservatory;
    }

    public Long getConservatoryId() {
        return conservatoryId;
    }

    public void setConservatoryId(Long conservatoryId) {
        this.conservatoryId = conservatoryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGeneralRegister() {
        return generalRegister;
    }

    public void setGeneralRegister(String generalRegister) {
        this.generalRegister = generalRegister;
    }

    public String getParticularRegister() {
        return particularRegister;
    }

    public void setParticularRegister(String particularRegister) {
        this.particularRegister = particularRegister;
    }

    public Date getPresentationDate() {
        return presentationDate;
    }

    public void setPresentationDate(Date presentationDate) {
        this.presentationDate = presentationDate;
    }

    public String getConservatoryName() {
        return conservatoryName;
    }

    public void setConservatoryName(String conservatoryName) {
        this.conservatoryName = conservatoryName;
    }

    public String getDescriptionSectionA() {
        return descriptionSectionA;
    }

    public void setDescriptionSectionA(String descriptionSectionA) {
        this.descriptionSectionA = descriptionSectionA;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Subject getCurrentSubject() {
        return currentSubject;
    }

    public void setCurrentSubject(Subject currentSubject) {
        this.currentSubject = currentSubject;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getNaturaAtto() {
        return naturaAtto;
    }

    public void setNaturaAtto(String naturaAtto) {
        this.naturaAtto = naturaAtto;
    }

    public List<Long> getPresumableSubjects() {
        return presumableSubjects;
    }

    public void setPresumableSubjects(List<Long> presumableSubjects) {
        this.presumableSubjects = presumableSubjects;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }
}
