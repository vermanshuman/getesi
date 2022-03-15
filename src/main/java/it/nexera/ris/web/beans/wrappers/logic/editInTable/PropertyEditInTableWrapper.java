package it.nexera.ris.web.beans.wrappers.logic.editInTable;

import it.nexera.ris.common.enums.RelationshipType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.PropertyEntityHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.omi.OMIHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static it.nexera.ris.common.helpers.TemplatePdfTableHelper.distinctByKey;

@Getter
@Setter
public class PropertyEditInTableWrapper extends BaseEditInTableWrapper {

    protected static transient final Log log = LogFactory.getLog(PropertyEditInTableWrapper.class);

    private CadastralCategory category;

    private Long selectedCategoryId;

    private String address;

    private Omi lastOmi;

    private Omi calculatedOmi;

    private String lastCommercial;

    private String calculatedCommercial;

    private List<CadastralDataEditInTableWrapper> cadastralData;

    private List<RelationshipEditInTableWrapper> relationshipList;

    private List<DatafromProperty> associatedWithEstateSituationDatafromProperties;

    private Long estateSituationId;

    private String consistency;

    private Double cadastralArea;

    private String revenue;

    private Long minimumValue;

    private Long maximumValue;

    private boolean calculateOmiValue;

    public PropertyEditInTableWrapper(Property property, EstateSituation situation) {
        super(property.getId(), property.getComment());
        this.category = property.getCategory();
        if (!ValidationHelper.isNullOrEmpty(property.getCategory())) {
            this.selectedCategoryId = property.getCategory().getId();
        }
        this.address = property.getAddress();
        this.cadastralData = CollectionUtils.emptyIfNull(property.getCadastralData())
                .stream()
                .filter(distinctByKey(x -> x.getId()))
                .map(CadastralDataEditInTableWrapper::new)
                .collect(Collectors.toList());

        initOmi(property);

        this.lastCommercial = PropertyEntityHelper.getEstimateLastCommercialValueRequestText(property)
                .replaceAll(",", ".");

        if (ValidationHelper.isNullOrEmpty(this.lastCommercial) &&
                !ValidationHelper.isNullOrEmpty(this.calculatedCommercial)) {

            this.lastCommercial = this.calculatedCommercial;
        }
        this.associatedWithEstateSituationDatafromProperties =
                property.getAssociatedDatafromPropertiesWithEstateSituationById(situation.getId());
        if (!ValidationHelper.isNullOrEmpty(this.associatedWithEstateSituationDatafromProperties)) {
            this.associatedWithEstateSituationDatafromProperties.forEach(x -> x.setAssociated(true));
            this.associatedWithEstateSituationDatafromProperties.forEach(x -> Hibernate.initialize(x.getSituationProperties()));
        }
        this.estateSituationId = situation.getId();

        if (!ValidationHelper.isNullOrEmpty(this.calculatedCommercial)) {
            if (this.calculatedCommercial.endsWith(".0")) {
                this.calculatedCommercial = Optional.ofNullable(this.calculatedCommercial)
                        .filter(sStr -> sStr.length() != 0)
                        .map(sStr -> sStr.substring(0, sStr.length() - 2))
                        .orElse(this.calculatedCommercial);
            }
            this.calculatedCommercial.replaceAll("\\.", ",");
        }

        if (!ValidationHelper.isNullOrEmpty(this.lastCommercial)) {
            if (this.lastCommercial.endsWith(".0")) {
                this.lastCommercial = Optional.ofNullable(this.lastCommercial)
                        .filter(sStr -> sStr.length() != 0)
                        .map(sStr -> sStr.substring(0, sStr.length() - 2))
                        .orElse(this.lastCommercial);
            }
            this.lastCommercial.replaceAll("\\.", ",");
            if (!ValidationHelper.isNullOrEmpty(this.calculatedCommercial)) {
                String value = this.lastCommercial;
                this.lastCommercial = this.calculatedCommercial;
                this.calculatedCommercial = value;
            }
        }
        this.consistency = property.getConsistency();
        this.cadastralArea = property.getCadastralArea();
        this.revenue = property.getRevenue();
    }

    private void initOmi(Property property) {
        String lastOmi = PropertyEntityHelper.getEstimateOMIRequestText(property);
        this.lastOmi = new Omi(lastOmi,
                lastOmi.equals(ResourcesHelper.getString("estateLocationOMINotInappropriate")),
                false, false, false, false);

        this.calculatedOmi = new Omi("", false, false, false, false, false);
        if (!ValidationHelper.isNullOrEmpty(property.getCategoryCode())
                && (property.getCategoryCode().startsWith("A") || property.getCategoryCode().startsWith("C"))) {
            try {
                OMIHelper.CalculatedOmi calculatedOmi = OMIHelper.calculateOMI(property, true);
                this.calculatedOmi.setMultipleCoordinates(calculatedOmi.isMultipleCoordinates());
                if (!ValidationHelper.isNullOrEmpty(calculatedOmi.isMultipleCoordinates()) &&
                        calculatedOmi.isMultipleCoordinates())
                    this.lastOmi.setValue(null);

                if (calculatedOmi.getValue() != 0d) {
                    this.calculatedOmi.setValue(String.valueOf(new BigDecimal(calculatedOmi.getValue())
                            .setScale(2, RoundingMode.HALF_UP).doubleValue()));
                    this.calculatedOmi.setSeveralZones(calculatedOmi.isSeveralZones());
                    this.calculatedOmi.setSeveralComprs(calculatedOmi.isSeveralComprs());

                    this.calculatedCommercial = String.valueOf(OMIHelper.calculateCommercialOmi(property, calculatedOmi.getValue()));
                }
                this.calculatedOmi.setCategoryCodeMissing(calculatedOmi.isCategoryCodeMissing());
                if (this.calculatedOmi.categoryCodeMissing) {
                    setCalculateOmiValue(Boolean.TRUE);
                } else
                    setCalculateOmiValue(Boolean.FALSE);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        if (!ValidationHelper.isNullOrEmpty(this.getLastOmi()) &&
                ValidationHelper.isNullOrEmpty(this.lastOmi.getValue()) &&
                !ValidationHelper.isNullOrEmpty(this.calculatedOmi.getValue())) {
            this.lastOmi.setValue(this.calculatedOmi.getValue());
        }

        if (!ValidationHelper.isNullOrEmpty(this.calculatedOmi.getValue())) {
            String value = this.calculatedOmi.getValue();
            if (value.endsWith(".0")) {
                value = Optional.ofNullable(this.calculatedOmi.getValue())
                        .filter(sStr -> sStr.length() != 0)
                        .map(sStr -> sStr.substring(0, sStr.length() - 2))
                        .orElse(this.calculatedOmi.getValue());
            }
            value = value.replaceAll("\\.", ",");
            this.calculatedOmi.setValue(value);
        }

        if (!ValidationHelper.isNullOrEmpty(this.lastOmi.getValue())) {
            String value = this.lastOmi.getValue();
            if (value.endsWith(".0")) {
                value = Optional.ofNullable(this.lastOmi.getValue())
                        .filter(sStr -> sStr.length() != 0)
                        .map(sStr -> sStr.substring(0, sStr.length() - 2))
                        .orElse(this.lastOmi.getValue());
            }
            value = value.replaceAll("\\.", ",");
            if (!ValidationHelper.isNullOrEmpty(this.calculatedOmi.value)) {
                this.lastOmi.setValue(this.calculatedOmi.getValue());
                this.calculatedOmi.setValue(value);
            } else {
                this.lastOmi.setValue(value);
            }
        }
    }

    public void prepareRelationship(Subject subject)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Property property = DaoManager.get(Property.class, getId());
        if (!ValidationHelper.isNullOrEmpty(property.getRelationships()) && !ValidationHelper.isNullOrEmpty(subject)) {
            this.relationshipList = property.getRelationships().stream()
                    .filter(r -> r.getSubject() != null)
                    .filter(r -> r.getSubject().getId().equals(subject.getId()))
                    .filter(r -> r.getRelationshipTypeId().equals(RelationshipType.MANUAL_ENTRY.getId()))
                    .map(RelationshipEditInTableWrapper::new).collect(Collectors.toList());
        }

        if (this.relationshipList == null) {
            this.relationshipList = new LinkedList<>();
        }
    }

    public void save() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Property property = DaoManager.get(Property.class, getId());
        saveOmiValues(property);
        if (isEdited()) {
            property.setCategory(getCategory());
            property.setAddress(getAddress());
            property.setComment(getComment());
            property.setConsistency(getConsistency());
            property.setCadastralArea(getCadastralArea());
            property.setRevenue(getRevenue());
            DaoManager.save(property, true);

        }
        if (ValidationHelper.isNullOrEmpty(property.getLastCommercialValue())
                || !ValidationHelper.isNullOrEmpty(getLastCommercial())
                && !property.getLastCommercialValue().equals(getLastCommercial())) {
            CommercialValueHistory history = new CommercialValueHistory();
            history.setCommercialValue(getLastCommercial());
            history.setProperty(property);
            history.setCommercialValueDate(new Date());
            history.setUser(DaoManager.get(User.class, UserHolder.getInstance().getCurrentUser().getId()));
            DaoManager.save(history, true);
        }

        for (CadastralDataEditInTableWrapper data : getCadastralData()) {
            data.save();
        }
        if (!ValidationHelper.isNullOrEmpty(getRelationshipList())) {
            for (RelationshipEditInTableWrapper relationship : getRelationshipList()) {
                relationship.save(property);
            }
        }
        if (!ValidationHelper.isNullOrEmpty(getAssociatedWithEstateSituationDatafromProperties())) {
            List<DatafromProperty> listToDelete = new ArrayList<>();
            for (DatafromProperty datafromProperty : getAssociatedWithEstateSituationDatafromProperties()) {
                if (!datafromProperty.isAssociated()) {
                    listToDelete.add(datafromProperty);
                    datafromProperty.getSituationProperties().removeIf(x -> x.getId().equals(property
                            .getSituationPropertyByEstateSituationId(getEstateSituationId()).getId()));
                }
                DaoManager.save(datafromProperty, true);
            }
            getAssociatedWithEstateSituationDatafromProperties().removeAll(listToDelete);
        }
    }

    private void saveOmiValues(Property property) throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        EstimateOMIHistory lastEstimateOMI = property.getLastEstimateOMI();

        if (ValidationHelper.isNullOrEmpty(lastEstimateOMI)
                && !ValidationHelper.isNullOrEmpty(getLastOmi().getValue())) {
            EstimateOMIHistory history = new EstimateOMIHistory();
            history.setEstimateOMI(getLastOmi().getValue());
            history.setProperty(property);
            history.setPropertyAssessmentDate(new Date());
            history.setUser(DaoManager.get(User.class, UserHolder.getInstance().getCurrentUser().getId()));
            DaoManager.save(history, true);

            property.getEstimateOMIHistory().add(history);
        } else if (!ValidationHelper.isNullOrEmpty(lastEstimateOMI)) {
            lastEstimateOMI.setEstimateOMI(getLastOmi().getValue());
            lastEstimateOMI.setPropertyAssessmentDate(new Date());
            DaoManager.save(lastEstimateOMI, true);
        }
    }

    public List<RelationshipEditInTableWrapper> getRelationshipListToShow() {
        return relationshipList.stream().filter(r -> !r.isToDelete()).collect(Collectors.toList());
    }

    public void categoryChange() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setCategory(DaoManager.get(CadastralCategory.class, getSelectedCategoryId()));
    }

    public void setCategory(CadastralCategory category) {
        this.category = category;
        setEdited(true);
    }

    public void setSelectedCategoryId(Long selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
        setEdited(true);
    }

    public void setAddress(String address) {
        this.address = address;
        setEdited(true);
    }

    public void changeOmi(Omi omi) {
        if (omi.isInappropriate()) {
            omi.setValue(ResourcesHelper.getString("estateLocationOMINotInappropriate"));
        } else {
            omi.setValue("");
        }
    }

    public void setLastCommercial(String lastCommercial) {
        this.lastCommercial = lastCommercial;
        setEdited(true);
    }

    public String getCategoryType() {
        if (!ValidationHelper.isNullOrEmpty(getCategory())) {
            return String.format("%s - %s", getCategory().getCode(), getCategory().getDescription());
        }
        return null;
    }


    public void reCalculateOMI() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        Property property = DaoManager.get(Property.class, getId());
        property.setCategory(getCategory());
        property.setAddress(getAddress());
        property.setComment(getComment());
        property.setConsistency(getConsistency());
        property.setCadastralArea(getCadastralArea());
        property.setRevenue(getRevenue());
        initOmi(property);
        save();
    }

    public void handleRowEditEvent() {
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Omi {
        private String value;
        private boolean inappropriate;
        private boolean severalZones;
        private boolean severalComprs;
        private boolean multipleCoordinates;
        private boolean categoryCodeMissing;
    }
}
