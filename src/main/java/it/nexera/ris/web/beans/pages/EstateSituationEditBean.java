package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.web.beans.wrappers.logic.PropertyGroupingWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RelationshipGroupingWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.annotations.ReattachIgnore;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.EstateSituationHelper;
import it.nexera.ris.common.helpers.PropertyEntityHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.TranscriptionAndCertificationHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.view.FormalityView;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Getter;
import lombok.Setter;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@ManagedBean(name = "estateSituationEditBean")
@ViewScoped
public class EstateSituationEditBean extends EntityEditPageBean<EstateSituation> implements Serializable {

    private static final long serialVersionUID = -7972078466849990413L;

    private Request requestEntity;

    @ReattachIgnore
    private List<Property> propertyList;

    private List<Property> selectedProperty;

    @ReattachIgnore
    private List<EstateFormality> formalityList;

    private List<EstateFormality> selectedFormality;

    private List<FormalityView> formalityPDFList;
    
    private List<FormalityView> subjectsList;

    private boolean needValidation;

    private boolean propertyConfirmed;

    private Map<Long,List<Long>> saveEstateSituationFormalityPropertiesMap;

    private List<EstateSituationFormalityProperty> estateSituationFormalityProperties;

    private Boolean requestHasDistraintFormality;

    private boolean otherTypeFormalities;
    
    private Boolean reportRelationship;

    private Boolean salesDevelopment;
   
    @Getter
    @Setter
    private TranscriptionAndCertificationHelper transcriptionAndCertificationHelper;
    
    @Getter
    @Setter
    private Boolean isTranscriptionCertification;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        setNeedValidation(true);
        String id = getRequestParameter(RedirectHelper.ID_PARAMETER);
        String requestId = getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
        if (!ValidationHelper.isNullOrEmpty(requestId) && !requestId.equals("null")) {
            setRequestEntity(DaoManager.get(Request.class, Long.parseLong(requestId)));
            Hibernate.initialize(getRequestEntity().getSituationEstateLocations());
        }
        if (!ValidationHelper.isNullOrEmpty(id)) {
            setEntityId(Long.parseLong(id));
        } else {
            setEntityId(null);
        }
        String salesDevelopment = getRequestParameter(RedirectHelper.SALES);
        if (!ValidationHelper.isNullOrEmpty(salesDevelopment) && !salesDevelopment.equals("null")) {
            setSalesDevelopment(Boolean.parseBoolean(salesDevelopment));
        }else {
            setSalesDevelopment(getEntity().getSalesDevelopment());
        }
        fillEstateWrapper();
        selectDatafromProperties();
        fillFormalityWrapper();
        fillFormalityByRequest();
        setOtherTypeFormalities(!ValidationHelper.isNullOrEmpty(getEntity().getOtherType())
                ? getEntity().getOtherType() : false);
        setReportRelationship(!ValidationHelper.isNullOrEmpty(getEntity().getReportRelationship())
                ? getEntity().getReportRelationship() : Boolean.TRUE);
        setTranscriptionAndCertificationHelper(new TranscriptionAndCertificationHelper());
        setIsTranscriptionCertification(getTranscriptionAndCertificationHelper().checkTranscriptionCertificationExists(getRequestEntity()));
    }

    private void selectDatafromProperties() {
        if (getEntity().isNew()) {
            return;
        }
        for (Property property : getPropertyList()) {
            if (!ValidationHelper.isNullOrEmpty(property.getDatafromProperties())) {
                for (DatafromProperty datafromProperty : property.getDatafromProperties()) {
                    //check if datafromProperty is checked for this property
                    if (!ValidationHelper.isNullOrEmpty(datafromProperty.getSituationProperties())
                            && datafromProperty.getSituationProperties().stream()
                            .anyMatch(sp -> sp.getProperty().getId().equals(property.getId())
                                    && sp.getSituation().getId().equals(getEntity().getId()))) {
                        datafromProperty.setAssociated(true);
                    }
                }
            }
        }
    }

    private void fillEstateWrapper() throws IllegalAccessException, PersistenceBeanException {
        List<Property> result = new ArrayList<>();
        List<Property> propertyList = EstateSituationHelper.loadProperty(getRequestEntity());
        setSelectedProperty(new ArrayList<>());

        if (!ValidationHelper.isNullOrEmpty(getEntity())) {
            result.addAll(getEntity().getPropertyList());
            result.forEach(f -> f.setVisible(true));
            getSelectedProperty().addAll(result);
        }

        if (propertyList != null) {
            for (Property property : propertyList) {
                if (!result.contains(property)) {
                    result.add(property);
                }
            }
        }

        setPropertyList(result);
        sortProperties();
    }

    private void fillFormalityWrapper() throws IllegalAccessException, PersistenceBeanException {
        List<EstateFormality> result = new ArrayList<>();
        List<EstateFormality> formalityListTemp = EstateSituationHelper.loadEstateFormality(getRequestEntity());
        setSelectedFormality(new ArrayList<>());

        if (!ValidationHelper.isNullOrEmpty(getEntity())) {
            result.addAll(getEntity().getEstateFormalityList());
            result.forEach(f -> f.setVisible(true));
            getSelectedFormality().addAll(result);
        }

        if (formalityListTemp != null) {
            for (EstateFormality formality : formalityListTemp) {
                if (!result.contains(formality)) {
                    result.add(formality);
                }
            }
        }
        if (!ValidationHelper.isNullOrEmpty(result)) {
            result.sort(Comparator.comparing(EstateFormality::getDate));
        }

        setFormalityList(result);
    }

    public void changeSaveMap(FormalityView formalityView, Long propertyId)
            throws PersistenceBeanException, IllegalAccessException {
        Map<Long, List<Long>> saveMap = getSaveEstateSituationFormalityPropertiesMap();
        if (saveMap.containsKey(formalityView.getId())) {
            if (saveMap.get(formalityView.getId()).contains(propertyId)) {
                saveMap.get(formalityView.getId()).remove(propertyId);
            } else {
                saveMap.get(formalityView.getId()).add(propertyId);
            }
        } else {
            saveMap.put(formalityView.getId(), new ArrayList<>());
            saveMap.get(formalityView.getId()).add(propertyId);
        }
        formalityView.changeVisible();
    }
    
    public void updateSaveMap()
            throws PersistenceBeanException, IllegalAccessException {
    	try {
    		
    		String value = FacesContext.getCurrentInstance().
    				getExternalContext().getRequestParameterMap().get("checkboxvalue");
    		if(!ValidationHelper.isNullOrEmpty(value)) {
    			String toks [] = value.split("\\_");
    			Long selectedId = Long.parseLong(toks[0].trim());
    			Boolean status = Boolean.parseBoolean(toks[1].trim());
    			
    			List<FormalityView> formalityViews = getFormalityPDFList();
    			
    			if(!ValidationHelper.isNullOrEmpty(formalityViews)) {
    				FormalityView formalityView = formalityViews.stream().
        					filter(f -> f.getId().equals(selectedId)).
        					findAny().orElse(null);
    				
    				List<Property> properties = formalityView.getPropertiesLoad();
    				properties.forEach(p -> p.setSelected(status));
    			}
    		}

			} catch (Exception e) {
				e.printStackTrace();
			}
    }

    private void fillFormalityByRequest() throws PersistenceBeanException, IllegalAccessException {
        List<FormalityView> result = new ArrayList<>();
        List<FormalityView> formalityList;
        if(ValidationHelper.isNullOrEmpty(getSalesDevelopment()) || !getSalesDevelopment()){
            if(!ValidationHelper.isNullOrEmpty(getRequestEntity())
                    && !ValidationHelper.isNullOrEmpty(getRequestEntity().getDistraintFormality())) {
                formalityList = EstateSituationHelper.loadFormalityViewByDistraint(getRequestEntity());
                setSubjectsList(formalityList);
            } else {
                formalityList = EstateSituationHelper.loadFormalityView(getRequestEntity());
            }

            if (!ValidationHelper.isNullOrEmpty(getEntity().getFormalityList())) {
                result = DaoManager.load(FormalityView.class, new Criterion[]{
                        Restrictions.in("id", getEntity().getFormalityList().stream()
                                .map(Formality::getId).collect(Collectors.toList()))
                });
                result.forEach(f -> f.setVisible(true));
            }

            if (formalityList != null) {
                for (FormalityView view : formalityList) {
                    if (!result.contains(view)) {
                        result.add(view);
                    }
                }
            }
            setRequestHasDistraintFormality(!ValidationHelper.isNullOrEmpty(getRequestEntity())
                    && !ValidationHelper.isNullOrEmpty(getRequestEntity().getDistraintFormality()));
            if (getRequestHasDistraintFormality()) {
                setSaveEstateSituationFormalityPropertiesMap(new HashMap<>());
                setEstateSituationFormalityProperties(DaoManager.load(EstateSituationFormalityProperty.class, new Criterion[]{
                        Restrictions.eq("estateSituations.id", getEntity().getId())
                }));

                if (getEntityId() == null && !ValidationHelper.isNullOrEmpty(result)) {
                    List<Property> propertiesByDistraintFormality = PropertyEntityHelper
                            .getPropertiesByFormalityIdThroughSectionB(getRequestEntity().getDistraintFormality().getId());

                    if (!ValidationHelper.isNullOrEmpty(propertiesByDistraintFormality)) {
                        for (FormalityView view : result) {
                            if (!ValidationHelper.isNullOrEmpty(view.getPropertiesLoad())) {
                                view.getPropertiesLoad().forEach(p -> p.setSelected(propertiesByDistraintFormality.stream()
                                        .anyMatch(pd -> pd.isPresumable(p))));
                                view.setVisible(view.getProperties().stream().anyMatch(Property::getSelected));
                                getSaveEstateSituationFormalityPropertiesMap().put(view.getId(), view.getPropertiesLoad().stream()
                                        .filter(Property::getSelected).map(Property::getId).collect(Collectors.toList()));
                            }
                        }
                    }
                }
                if (!ValidationHelper.isNullOrEmpty(result)
                        && !ValidationHelper.isNullOrEmpty(getEstateSituationFormalityProperties())) {
                    for (FormalityView view : result) {
                        List<Property> properties = getEstateSituationFormalityProperties().stream()
                                .filter(x -> x.getFormality() != null && x.getFormality().getId().equals(view.getId()))
                                .map(EstateSituationFormalityProperty::getProperty).collect(Collectors.toList());
                        if (!ValidationHelper.isNullOrEmpty(properties)) {
                            view.getPropertiesLoad().stream().filter(properties::contains).forEach(p -> p.setSelected(true));
                            getSaveEstateSituationFormalityPropertiesMap().put(view.getId(), view.getPropertiesLoad().stream()
                                    .filter(properties::contains).map(Property::getId).collect(Collectors.toList()));
                        }
                    }
                }
            }
        }else {
            setRequestHasDistraintFormality(getRequestEntity().getDistraintFormality() != null);
            formalityList = EstateSituationHelper.loadFormalityViewForSales(getRequestEntity());
            if (!ValidationHelper.isNullOrEmpty(getEntity().getFormalityList())) {
                result = DaoManager.load(FormalityView.class, new Criterion[]{
                        Restrictions.in("id", getEntity().getFormalityList().stream()
                                .map(Formality::getId).collect(Collectors.toList()))
                });
                result.forEach(f -> f.setVisible(true));
            }
            if (formalityList != null) {
                for (FormalityView view : formalityList) {
                    if (!result.contains(view)) {
                        result.add(view);
                    }
                }
            }
        }
        setFormalityPDFList(result);
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
        setSelectedVisible();
        if (isNeedValidation() && !ValidationHelper.isNullOrEmpty(getFormalityPDFList()) && getFormalityPDFList().stream()
                .anyMatch(f -> f.getVisible() != null && f.getVisible())) {
            List<Formality> formalityList = DaoManager.load(Formality.class, new Criterion[]{
                    Restrictions.in("id", getFormalityPDFList().stream()
                            .filter(f -> f.getVisible() != null && f.getVisible()).map(FormalityView::getId)
                            .collect(Collectors.toList()))
            });
            if (!EstateSituationHelper.isValidFormalityCadastral(getPropertyList().stream()
                            .filter(estate -> estate.getVisible() != null && estate.getVisible()).collect(Collectors.toList()),
                    formalityList)) {
                executeJS("PF('cadastralInvalidDlg').show();");
                setValidationFailed(true);
            }
        }
        if (!getValidationFailed() && !isPropertyConfirmed()
                && !ValidationHelper.isNullOrEmpty(getPropertyList()) && getPropertyList().stream()
                .anyMatch(f -> f.getVisible() != null && f.getVisible())) {

            boolean showWarning = false;

            List<Property> distinctProperties = getPropertyList().stream()
                    .filter(p -> p.getVisible() != null && p.getVisible())
                    .filter(EstateSituationHelper.distinctByKeys(Property::getCity, Property::getSectionCity))
                    .collect(Collectors.toList());

            if(distinctProperties != null && distinctProperties.size() > 1)
                showWarning = true;
            /*if (1 < getPropertyList().stream().filter(f -> f.getVisible() != null && f.getVisible())
                    .map(Property::getCity).map(City::getId).distinct().count()) {
                showWarning = true;
            }else if (1 < getPropertyList().stream().filter(f -> f.getVisible() != null && f.getVisible())
                    .filter(f -> StringUtils.isNotBlank(f.getSectionCity()))
                    .map(Property::getSectionCity).distinct().count()) {
                showWarning = true;
            }*/
            if(showWarning){
                executeJS("PF('propertyCityDlg').show();");
                setValidationFailed(true);
            }
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException,
            IOException, InstantiationException, IllegalAccessException {

        List<Formality> formalityList = null;
        if (!ValidationHelper.isNullOrEmpty(getFormalityPDFList()) && getFormalityPDFList().stream()
                .anyMatch(f -> f.getVisible() != null && f.getVisible())) {
            formalityList = DaoManager.load(Formality.class, new Criterion[]{
                    Restrictions.in("id", getFormalityPDFList().stream()
                            .filter(f -> f.getVisible() != null && f.getVisible()).map(FormalityView::getId)
                            .collect(Collectors.toList()))
            });
        }
        //Map<PropertyGroupingWrapper, List<Property>> cityMap = new HashMap<>();
        boolean isPropertyConfirmed = getEntity().isNew()
                && getPropertyList().stream().anyMatch(f -> f.getVisible() != null && f.getVisible());
        
        /*if(isPropertyConfirmed() || isPropertyConfirmed){
            for (Property property : getPropertyList().stream()
                    .filter(f -> f.getVisible() != null && f.getVisible()).collect(Collectors.toList())) {
                PropertyGroupingWrapper propertyGroupingWrapper =
                        new PropertyGroupingWrapper(property);
                if (!cityMap.containsKey(propertyGroupingWrapper)) {
                    cityMap.put(propertyGroupingWrapper, new LinkedList<>());
                }
                cityMap.get(propertyGroupingWrapper).add(property);
            }
        }*/
        /*for (Map.Entry<PropertyGroupingWrapper, List<Property>> entry : cityMap.entrySet()) {
            EstateSituation estateSituation = new EstateSituation();
            estateSituation.setRequest(getRequestEntity());
            estateSituation.setPropertyList(entry.getValue());
            estateSituation.setEstateFormalityList(getFormalityList().stream()
                    .filter(estate -> estate.getVisible() != null && estate.getVisible()).collect(Collectors.toList()));
            estateSituation.setFormalityList(formalityList);
            estateSituation.setOtherType(isOtherTypeFormalities());
            estateSituation.setReportRelationship(getReportRelationship());
            estateSituation.setSalesDevelopment(getSalesDevelopment());
            DaoManager.save(estateSituation);
            saveOrUpdateDatafromProperties(estateSituation);
            if (getRequestHasDistraintFormality()) {
                saveNewEstateSituationFormalityPropertyToDB(formalityList, estateSituation);
            }
        }*/
        
        if(isPropertyConfirmed() || isPropertyConfirmed){
        	saveEstateSituation(formalityList, getPropertyList());
        } 

       if(!isPropertyConfirmed){
            if(getEntity().isNew()){
                getEntity().setSalesDevelopment(getSalesDevelopment());
            }
            getEntity().setRequest(getRequestEntity());
            getEntity().setPropertyList(getPropertyList().stream()
                    .filter(estate -> estate.getVisible() != null && estate.getVisible()).collect(Collectors.toList()));
            getEntity().setEstateFormalityList(getFormalityList().stream()
                    .filter(estate -> estate.getVisible() != null && estate.getVisible()).collect(Collectors.toList()));

            getEntity().setFormalityList(formalityList);
            getEntity().setOtherType(isOtherTypeFormalities());
            getEntity().setReportRelationship(getReportRelationship());
            if (!ValidationHelper.isNullOrEmpty(getEntity().getPropertyList())
                    || !ValidationHelper.isNullOrEmpty(getEntity().getEstateFormalityList())
                    || !ValidationHelper.isNullOrEmpty(getEntity().getFormalityList())) {
                DaoManager.save(getEntity());
                saveOrUpdateDatafromProperties(getEntity());
                if (getRequestHasDistraintFormality()) {
                    removeEstateSituationFormalityPropertyFromDB();
                    clearSaveMapFromAlreadyExistingESFP();
                    saveNewEstateSituationFormalityPropertyToDB(formalityList, getEntity());
                }
            }
        }
    }
    
    private void saveEstateSituation(List<Formality> formalityList, List<Property> propertyList) 
    		throws HibernateException, PersistenceBeanException, IllegalAccessException, InstantiationException {
    	Map<PropertyGroupingWrapper, List<Property>> cityMap = new HashMap<>();
    	List<Property> newPropertyList = new ArrayList<>();
    	newPropertyList.addAll(getPropertyList());
    	if(!ValidationHelper.isNullOrEmpty(getEntity().getPropertyList())) {
    		for(Property property : getEntity().getPropertyList()) {
    			newPropertyList.removeIf(p -> p.getId().longValue() == property.getId());
    		}
    		for(Property property : newPropertyList) {
    			getPropertyList().removeIf(p -> p.getId().longValue() == property.getId());
    		}
    	}
    	for (Property property : newPropertyList.stream()
                .filter(f -> f.getVisible() != null && f.getVisible()).collect(Collectors.toSet())) {
            PropertyGroupingWrapper propertyGroupingWrapper =
                    new PropertyGroupingWrapper(property);
            if (!cityMap.containsKey(propertyGroupingWrapper)) {
                cityMap.put(propertyGroupingWrapper, new LinkedList<>());
            }
            cityMap.get(propertyGroupingWrapper).add(property);
        }
    	for (Map.Entry<PropertyGroupingWrapper, List<Property>> entry : cityMap.entrySet()) {
            EstateSituation estateSituation = new EstateSituation();
            estateSituation.setRequest(getRequestEntity());
            estateSituation.setPropertyList(entry.getValue());
            estateSituation.setEstateFormalityList(getFormalityList().stream()
                    .filter(estate -> estate.getVisible() != null && estate.getVisible()).collect(Collectors.toList()));
            estateSituation.setFormalityList(formalityList);
            estateSituation.setOtherType(isOtherTypeFormalities());
            estateSituation.setReportRelationship(getReportRelationship());
            estateSituation.setSalesDevelopment(getSalesDevelopment());
            DaoManager.save(estateSituation);
            saveOrUpdateDatafromProperties(estateSituation);
            if (getRequestHasDistraintFormality()) {
                saveNewEstateSituationFormalityPropertyToDB(formalityList, estateSituation);
            }
        }
    }

    private void saveOrUpdateDatafromProperties(EstateSituation estateSituation)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        for (Property property : estateSituation.getPropertyList()) {
            if (!ValidationHelper.isNullOrEmpty(property.getDatafromProperties())) {
                for (DatafromProperty datafromProperty : property.getDatafromProperties()) {
                    if (!Hibernate.isInitialized(datafromProperty.getSituationProperties())) {
                        DatafromProperty datafromPropertyReloaded = DaoManager.get(DatafromProperty.class, datafromProperty.getId());
                        datafromProperty.setSituationProperties(datafromPropertyReloaded.getSituationProperties());
                    }

                    if (datafromProperty.getSituationProperties() == null) {
                        datafromProperty.setSituationProperties(new ArrayList<>());
                    }
                    Optional<SituationProperty> situationProperty = datafromProperty.getSituationProperties()
                            .stream().filter(sp -> sp.getSituation().getId().equals(estateSituation.getId())
                                    && sp.getProperty().getId().equals(property.getId())).findFirst();

                    if (situationProperty.isPresent()) {
                        if (!datafromProperty.isAssociated()) {
                            datafromProperty.getSituationProperties().remove(situationProperty.get());
                        }
                    } else if (datafromProperty.isAssociated()) {
                        situationProperty = property.getSituationProperties().stream()
                                .filter(sp -> sp.getSituation().getId().equals(estateSituation.getId()))
                                .findFirst();
                        situationProperty.ifPresent(value -> datafromProperty.getSituationProperties().add(value));
                    }
                    DaoManager.save(datafromProperty);
                }
            }
        }
    }

    private void saveNewEstateSituationFormalityPropertyToDB(List<Formality> formalityList, EstateSituation estateSituation)
            throws PersistenceBeanException {
        List<EstateSituationFormalityProperty> newESFP = new ArrayList<>();
        for (Map.Entry<Long, List<Long>> save : getSaveEstateSituationFormalityPropertiesMap().entrySet()) {
            FormalityView formalityView = getFormalityPDFList().stream().filter(f -> f.getId().equals(save.getKey()))
                    .findAny().orElse(null);
            for (Long property : save.getValue()) {
                EstateSituationFormalityProperty estateSituationFormalityProperty
                        = new EstateSituationFormalityProperty();
                estateSituationFormalityProperty.setProperty(formalityView.getProperties().stream()
                        .filter(x -> x.getId().equals(property)).findFirst().orElse(null));
                estateSituationFormalityProperty.setFormality(formalityList.stream().filter(f -> f.getId()
                        .equals(formalityView.getId())).findAny().orElse(null));
                estateSituationFormalityProperty.setEstateSituations(estateSituation);
                DaoManager.save(estateSituationFormalityProperty);
                newESFP.add(estateSituationFormalityProperty);
            }
        }
        getEstateSituationFormalityProperties().addAll(newESFP);
    }

    private void clearSaveMapFromAlreadyExistingESFP() {
        for (EstateSituationFormalityProperty esfp : getEstateSituationFormalityProperties()) {
            if(!ValidationHelper.isNullOrEmpty(esfp.getFormality()))
                getSaveEstateSituationFormalityPropertiesMap().get(esfp.getFormality().getId()).remove(esfp.getProperty().getId());
        }
    }

    private void removeEstateSituationFormalityPropertyFromDB() throws PersistenceBeanException {
        //remove all entities that are not checked but exist in DB
        List<EstateSituationFormalityProperty> removeList = getEstateSituationFormalityProperties().stream()
                .filter(esfp -> !ValidationHelper.isNullOrEmpty(esfp.getFormality()))
                .filter(esfp -> !getSaveEstateSituationFormalityPropertiesMap().containsKey(esfp.getFormality().getId())
                        || !getSaveEstateSituationFormalityPropertiesMap().get(esfp.getFormality().getId())
                        .contains(esfp.getProperty().getId()))
                .collect(Collectors.toList());
        if (!ValidationHelper.isNullOrEmpty(removeList)) {
            for (EstateSituationFormalityProperty esfp : removeList) {
                DaoManager.remove(esfp);
            }
            getEstateSituationFormalityProperties().removeAll(removeList);
        }
    }

    public void setSelectedVisible() {
        for (Property selectedProperty : CollectionUtils.emptyIfNull(getSelectedProperty())) {
            for (Property property : getPropertyList()) {
                if (property.getId().equals(selectedProperty.getId())) {
                    property.setVisible(true);
                    break;
                }
            }
        }

        for (Property property : getPropertyList()) {
            if (CollectionUtils.emptyIfNull(getSelectedProperty()).stream().noneMatch(p -> p.getId().equals(property.getId()))) {
                property.setVisible(false);
            }
        }

        for (EstateFormality selectedFormality : CollectionUtils.emptyIfNull(getSelectedFormality())) {
            for (EstateFormality formality : getFormalityList()) {
                if (formality.getId().equals(selectedFormality.getId())) {
                    formality.setVisible(true);
                    break;
                }
            }
        }

        for (EstateFormality estateFormality : getFormalityList()) {
            if (emptyIfNull(getSelectedFormality()).stream().noneMatch(e -> e.getId().equals(estateFormality.getId()))) {
                estateFormality.setVisible(false);
            }
        }

    }

    public boolean isExistsInEstateSituation(Property property) {
        if (!ValidationHelper.isNullOrEmpty(getEntity())) {
            List<Long> propertyIds = getEntity().getPropertyList().stream().map(Property::getId).collect(Collectors.toList());
            return propertyIds.contains(property.getId());
        }
        return false;
    }

    @Override
    public void afterSave() {
        goBack();
    }

    @Override
    public void goBack() {
        RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_LIST, getRequestEntity().getId());
    }

    public void goCancel() throws PersistenceBeanException, IllegalAccessException {
        for (EstateFormality formality : getFormalityList()) {
            DaoManager.save(formality, true);
        }
        RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_LIST, getRequestEntity().getId());
    }
    
    private void sortProperties() {
    	Comparator<CadastralData> comparatorCadastralData = (o1, o2) -> {
            if(o1==null && o2==null) {
                return 0;
            }else if(o1==null) {
                return -1;
            }else if(o2==null) {
                return 1;
            }else {
                if(o1.getSheet() != null && o2.getSheet() != null) {
                    int result = extractInt(o1.getSheet()).compareTo(extractInt(o2.getSheet()));
                    if(result == 0) {
                        result = extractInt(o1.getParticle()).compareTo(extractInt(o2.getParticle()));
                    }
                    if(result == 0) {
                        result = extractInt(o1.getSub()).compareTo(extractInt(o2.getSub()));
                    }
                    return result;
                }else if(o1.getSheet()==null) {
                    return -1;
                }else if(o2.getSheet()==null) {
                    return 1;
                }else {
                    return 0;
                }
            }
        };

        Comparator<Property> comparatorProperty = (o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            } else {
                if (ValidationHelper.isNullOrEmpty(o1.getCadastralData())
                        && ValidationHelper.isNullOrEmpty(o2.getCadastralData())) {
                    return 0;
                } else if ((ValidationHelper.isNullOrEmpty(o1.getCadastralData())
                        || o1.getCadastralData().size() < 1)
                        && ((ValidationHelper.isNullOrEmpty(o2.getCadastralData())
                        || o2.getCadastralData().size() < 1))) {
                    return 0;
                } else if (!ValidationHelper.isNullOrEmpty(o1.getCadastralData())
                        && !ValidationHelper.isNullOrEmpty(o2.getCadastralData())
                        && o1.getCadastralData().size() > 0 && o2.getCadastralData().size() > 0) {
                    CadastralData data1 = o1.getCadastralData().stream().findFirst().orElse(null);
                    CadastralData data2 = o2.getCadastralData().stream().findFirst().orElse(null);
                    return comparatorCadastralData.compare(data1, data2);
                } else if (!ValidationHelper.isNullOrEmpty(o1.getCadastralData())
                        && o1.getCadastralData().size() > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        };

        propertyList = propertyList.stream().sorted(comparatorProperty).collect(Collectors.toList());
    }
    
    private static Integer extractInt(String s) {
        String num = "";
        if(StringUtils.isNotBlank(s))
            num = s.replaceAll("\\D", "");
        // return 0 if no digits found
        return num.isEmpty() ? 0 : Integer.parseInt(num);
    }
    
    public void openTranscriptionManagement() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
    	if(!ValidationHelper.isNullOrEmpty(getRequestEntity()) && !ValidationHelper.isNullOrEmpty(getRequestEntity().getId())) {
    		getTranscriptionAndCertificationHelper().openTranscriptionManagement(getRequestEntity().getId());
    	}
    }

    public void setEntityEditId(Long entityEditId) {
        this.getViewState().put("entityEditId", entityEditId);
    }

    public Long getEntityEditId() {
        return (Long) this.getViewState().get("entityEditId");
    }

    public Request getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(Request requestEntity) {
        this.requestEntity = requestEntity;
    }

    public List<Property> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    public List<EstateFormality> getFormalityList() {
        return formalityList;
    }

    public void setFormalityList(List<EstateFormality> formalityList) {
        this.formalityList = formalityList;
    }

    public List<FormalityView> getFormalityPDFList() {
        return formalityPDFList;
    }

    public void setFormalityPDFList(List<FormalityView> formalityPDFList) {
        this.formalityPDFList = formalityPDFList;
    }

    public boolean isNeedValidation() {
        return needValidation;
    }

    public void setNeedValidation(boolean needValidation) {
        this.needValidation = needValidation;
    }

    public boolean isPropertyConfirmed() {
        return propertyConfirmed;
    }

    public void setPropertyConfirmed(boolean propertyConfirmed) {
        this.propertyConfirmed = propertyConfirmed;
    }

    public List<Property> getSelectedProperty() {
        return selectedProperty;
    }

    public void setSelectedProperty(
            List<Property> selectedProperty) {
        this.selectedProperty = selectedProperty;
    }

    public List<EstateFormality> getSelectedFormality() {
        return selectedFormality;
    }

    public void setSelectedFormality(
            List<EstateFormality> selectedFormality) {
        this.selectedFormality = selectedFormality;
    }

    public List<EstateSituationFormalityProperty> getEstateSituationFormalityProperties() {
        return estateSituationFormalityProperties;
    }

    public void setEstateSituationFormalityProperties(List<EstateSituationFormalityProperty> estateSituationFormalityProperties) {
        this.estateSituationFormalityProperties = estateSituationFormalityProperties;
    }

    public Boolean getRequestHasDistraintFormality() {
        return requestHasDistraintFormality;
    }

    public void setRequestHasDistraintFormality(Boolean requestHasDistraintFormality) {
        this.requestHasDistraintFormality = requestHasDistraintFormality;
    }

    public Map<Long, List<Long>> getSaveEstateSituationFormalityPropertiesMap() {
        return saveEstateSituationFormalityPropertiesMap;
    }

    public void setSaveEstateSituationFormalityPropertiesMap(Map<Long, List<Long>> saveEstateSituationFormalityPropertiesMap) {
        this.saveEstateSituationFormalityPropertiesMap = saveEstateSituationFormalityPropertiesMap;
    }

    public boolean isOtherTypeFormalities() {
        return otherTypeFormalities;
    }

    public void setOtherTypeFormalities(boolean otherTypeFormalities) {
        this.otherTypeFormalities = otherTypeFormalities;
    }

	public Boolean getReportRelationship() {
		return reportRelationship;
	}

	public void setReportRelationship(Boolean reportRelationship) {
		this.reportRelationship = reportRelationship;
	}

	public List<FormalityView> getSubjectsList() {
		return subjectsList;
	}

	public void setSubjectsList(List<FormalityView> subjectsList) {
		this.subjectsList = subjectsList;
	}

    public Boolean getSalesDevelopment() {
        return salesDevelopment;
    }

    public void setSalesDevelopment(Boolean salesDevelopment) {
        this.salesDevelopment = salesDevelopment;
    }
}
