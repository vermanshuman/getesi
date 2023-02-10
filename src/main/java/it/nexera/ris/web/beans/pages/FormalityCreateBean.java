package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import it.nexera.ris.common.annotations.ReattachIgnore;
import it.nexera.ris.common.comparators.CadastralCategoryComparator;
import it.nexera.ris.common.enums.FormalityStateType;
import it.nexera.ris.common.enums.NoteType;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.PropertyTypeEnum;
import it.nexera.ris.common.enums.RealEstateType;
import it.nexera.ris.common.enums.RelationshipType;
import it.nexera.ris.common.enums.RequestState;
import it.nexera.ris.common.enums.RoleTypes;
import it.nexera.ris.common.enums.SectionCType;
import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.CalcoloCodiceFiscale;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.FormalityHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.RequestHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.SubjectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.CadastralData;
import it.nexera.ris.persistence.beans.entities.domain.EstateSituation;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.Relationship;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.SectionA;
import it.nexera.ris.persistence.beans.entities.domain.SectionB;
import it.nexera.ris.persistence.beans.entities.domain.SectionC;
import it.nexera.ris.persistence.beans.entities.domain.SectionD;
import it.nexera.ris.persistence.beans.entities.domain.SituationProperty;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Court;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Nationality;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Regime;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.SectionDFormat;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TipologieDiritti;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;
import it.nexera.ris.web.beans.EntityEditPageBean;

@ManagedBean(name = "formalityCreateBean")
@ViewScoped
public class FormalityCreateBean extends EntityEditPageBean<Formality> implements Serializable {

    private static final long serialVersionUID = 2255442898397167303L;

    private Long tempIdCounter;

    private Long requestId;

    private Long selectedReclamePropertyServiceId;

    private Long selectedProvincialOfficeId;

    private Long selectedId;

    private Long selectedType;

    private Long bargainingUnitIncrement;

    private Long selectedTypeId;

    private Long categoryId;

    private Long selectedCountryId;

    private List<SelectItem> addressCities;

    private List<SelectItem> addressCitiesSectionC;

    private List<SelectItem> conservatories;

    private List<SelectItem> categories;

    private List<SelectItem> types;

    private List<SelectItem> dicTypeFormalities;

    private List<SelectItem> provinces;

    private List<SelectItem> filteredProvinces;

    private List<SelectItem> realEstateTypes;

    private List<SelectItem> subjectTypes;

    private List<SelectItem> countries;

    private List<SelectItem> sexTypes;

    private List<SelectItem> propertyTypeList;

    private List<SelectItem> codeAndDescription;

    private List<String> units;

    private PropertyTypeEnum propertyType;

    private SectionA sectionA;

    @ReattachIgnore
    private SectionB editedSectionB;

    private String formalityType;

    private String dicTypeFormality;

    private String quote1;

    private String quote2;

    private Long annotationDescription;

    private Long derivedFrom;

    private Long conventionDescription;

    @ReattachIgnore
    private List<SectionB> sectionBList;

    @ReattachIgnore
    private SectionC aFavoreSectionC;

    @ReattachIgnore
    private SectionC controSectionC;

    @ReattachIgnore
    private SectionC debitoriSectionC;

    private SectionD sectionD;

    private Long addressProvinceId;

    private Long addressCityId;

    private Long addressProvinceIdC;

    private Long addressCityIdC;

    private Long entityEditId;

    private boolean saveScheda;

    private List<CadastralData> cadastralDataList;

    private List<CadastralData> cadastralDataForDeleteList;

    private Integer deleteTempId;

    private Property newProperty;

    private boolean renderForm;

    private boolean clickedCreatePropertyButton;

    private boolean building;

    private SectionCType sectionCType;

    private Boolean foreignCountry;

    private CadastralData newCadastralData;

    private CadastralData newCadastralDataDlg;

    private Subject newSubject;

    @ReattachIgnore
    private Relationship newRelationship;

    @ReattachIgnore
    private Subject editSubject;

    private String typeAtto;

    private AtomicInteger tempId;

    private boolean isValidated;

    @ReattachIgnore
    private Subject previousSubject;

    private RoleTypes roleType;

    private List<Subject> subjectsToDeleteOnSave;

    private String formalityId;

    private boolean isItDialog;

    private Long transcriptionActId;

    private Boolean viewFromRequest;

    private List<SelectItem> relationshipTypeList;

    private Long conservatoryFilterId;

    private String selectedCourt;

    private List<Property> propertyList;

    private List<Property> selectedSearchedProperty;

    private boolean listProperties;
    
    private Long editedRequestId;
    
    private List<SelectItem> regimeList;
    
    private Long selectedRegimeId;
    
    private List<SelectItem> sectionDs;
    
    private Long selectedSectionDId;
    
    private boolean fromRequestEdit;
    
    private List<Relationship> relationshipsToDeleteOnSave;
    
    private String statoStr;

  private Long cloneFormalityId;
    
    @Override
    protected void pageLoadStatic() throws PersistenceBeanException {

        if (SessionHelper.get("requestEditDistraintFormalityDialog") != null
                && ((Boolean) SessionHelper.get("requestEditDistraintFormalityDialog"))) {
            Long formalityId = SessionHelper.get("distraintFormalityId") == null
                    ? null : (Long) SessionHelper.get("distraintFormalityId");

            setFormalityId(formalityId != null ? String.valueOf(formalityId) : null);

            setItDialog(true);
            
            setRunAfterSave(false);
            setEntityId(formalityId);

            SessionHelper.removeObject("requestEditDistraintFormalityDialog");
            SessionHelper.removeObject("distraintFormalityId");
        } else {
            setItDialog(false);
        }

        if (SessionHelper.get("listProperties") != null
                && ((Boolean) SessionHelper.get("listProperties"))) {
            setListProperties(true);

            Long editedRequestId = SessionHelper.get("editedRequestId") == null
                    ? null : (Long) SessionHelper.get("editedRequestId");
            String statoStr = SessionHelper.get("formalityStato") == null
                    ? null : (String) SessionHelper.get("formalityStato");
            if (!ValidationHelper.isNullOrEmpty(statoStr)) {
                setStatoStr(statoStr);
                setCloneFormalityId(SessionHelper.get("cloneFormalityId") == null
                        ? null : (Long) SessionHelper.get("cloneFormalityId"));

                if (!ValidationHelper.isNullOrEmpty(getCloneFormalityId()) && "O".equals(getStatoStr()) || "T".equals(getStatoStr())) {
                    try {
                        Formality cloneFormality = DaoManager.get(Formality.class, getCloneFormalityId());
                        Formality newFormality = cloneFormality.cloneFormality();
                        newFormality.setState(FormalityStateType.MANUALE.getId());
                        setEntity(newFormality);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            setEditedRequestId(editedRequestId);
            SessionHelper.removeObject("listProperties");
            SessionHelper.removeObject("editedRequestId");
            SessionHelper.removeObject("formalityStato");
            SessionHelper.removeObject("cloneFormalityId");

        }else {
            setListProperties(false);
            setEditedRequestId(null);
        }
        if (SessionHelper.get("requestViewFormality") != null
                && ((Boolean) SessionHelper.get("requestViewFormality"))) {
            Long transcriptionActId = SessionHelper.get("transcriptionActId") == null
                    ? null : (Long) SessionHelper.get("transcriptionActId");

            setTranscriptionActId(transcriptionActId != null ? Long.valueOf(transcriptionActId) : null);
            setViewFromRequest(true);
            SessionHelper.removeObject("requestViewFormality");
            SessionHelper.removeObject("transcriptionActId");
            setFromRequestEdit(true);
        } else {
            setViewFromRequest(false);
        }
        
        if (SessionHelper.get("fromRequestEdit") != null) {
            setFromRequestEdit((Boolean)SessionHelper.get("fromRequestEdit"));
            SessionHelper.removeObject("fromRequestEdit");
        }
    }

    @Override

    public void onLoad() throws NumberFormatException, HibernateException,
    PersistenceBeanException, InstantiationException, IllegalAccessException {
        String requestId = getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
        if (!isItDialog()) {
            formalityId = getRequestParameter(RedirectHelper.ID_PARAMETER);
        }

        setBargainingUnitIncrement(0L);

        setaFavoreSectionC(new SectionC());
        getaFavoreSectionC().setSectionCType(SectionCType.A_FAVORE.getName());
        setControSectionC(new SectionC());
        getControSectionC().setSectionCType(SectionCType.CONTRO.getName());
        setDebitoriSectionC(new SectionC());
        getDebitoriSectionC().setSectionCType(SectionCType.DEBITORI_NON_DATORI_DI_IPOTECA.getName());

        loadSelectLists();

        setSectionD(new SectionD());

        if (!ValidationHelper.isNullOrEmpty(getSelectedType()) || !ValidationHelper.isNullOrEmpty(getEntity().getType())) {
            setSelectedType(NoteType.getEnumByString(getEntity().getTypeEnum().toString()).getId());
        }
        if (!ValidationHelper.isNullOrEmpty(formalityId)) {
            fillSectionAList(Long.valueOf(formalityId));
            fillSectionBList(Long.valueOf(formalityId));
            fillSectionCList(Long.valueOf(formalityId));
            fillSectionDList(Long.valueOf(formalityId));
        }
        if (!ValidationHelper.isNullOrEmpty(getCloneFormalityId())) {
            fillSectionAList(Long.valueOf(getCloneFormalityId()));
            List<SectionB> sectionBList = DaoManager.load(SectionB.class, new Criterion[]{Restrictions.eq("formality.id", getCloneFormalityId())});
            if (ValidationHelper.isNullOrEmpty(getSectionBList())) {
                setSectionBList(new ArrayList<>());
            }
            sectionBList.stream().forEach(sb -> {
                SectionB sectionB = new SectionB();
                sectionB.setBargainingUnit(sb.getBargainingUnit());
                Hibernate.initialize(sb.getProperties());
                if(!ValidationHelper.isNullOrEmpty(sb.getProperties())){
                    sectionB.setProperties(new ArrayList<>(sb.getProperties()));
                }
                sectionB.setFormality(getEntity());
                getSectionBList().add(sectionB);
            });
            for (int i = 0; i < sectionBList.size(); i++) {
                getUnits().add(String.valueOf(i + 1));
            }
            bargainingUnitIncrement = Long.valueOf(sectionBList.size());
            fillSectionCList(Long.valueOf(getCloneFormalityId()));
            List<SectionD> sectionDList = DaoManager.load(SectionD.class, new Criterion[]{Restrictions.eq("formality.id", getCloneFormalityId())});
            if (!ValidationHelper.isNullOrEmpty(sectionDList)){
                SectionD sectionD = sectionDList.get(0);
                SectionD newSectionD = new SectionD();
                newSectionD.setAdditionalInformation(sectionD.getAdditionalInformation());
                newSectionD.setFormality(getEntity());
                setSectionD(newSectionD);
            }
        }

        if (getEntity().getSectionA() != null){
            if(!ValidationHelper.isNullOrEmpty(getEntity().getSectionA().getLandChargesRegistry())) {
                setConservatoryFilterId(getEntity().getSectionA().getLandChargesRegistry().getId());    
            }
            if(!ValidationHelper.isNullOrEmpty(getEntity().getSectionA().getPublicOfficial())) {
                Court court = DaoManager.get(Court.class,
                        new Criterion[]{
                                Restrictions.eq("name", getEntity().getSectionA().getPublicOfficial())});
                if(!ValidationHelper.isNullOrEmpty(court))
                    setSelectedCourt(court.getName());
                else
                    setSelectedCourt(getEntity().getSectionA().getPublicOfficial());
            }
        }

        setNewCadastralData(new CadastralData());
        setNewCadastralDataDlg(new CadastralData());
        setNewProperty(new Property());
        setNewSubject(new Subject());
        setNewRelationship(new Relationship());

        setTempId(new AtomicInteger());

        updateFieldsCadastralData();

        showPartsOfSectionABySelectedType();


        if (!ValidationHelper.isNullOrEmpty(requestId) && !"null".equals(requestId)) {
            setRequestId(Long.parseLong(requestId));
        }
        //if (!getEntity().isNew()) {
            if (!ValidationHelper.isNullOrEmpty(getEntity().getReclamePropertyService())) {
                setSelectedId(getEntity().getReclamePropertyService().getId());
            } else if (!ValidationHelper.isNullOrEmpty(getEntity().getProvincialOffice())) {
                setSelectedId(getEntity().getProvincialOffice().getId());
            }
            setSectionA(getEntity().getSectionA());
        //}
        if (getSectionA() == null) {
            setSectionA(new SectionA());
        }
        setConservatories(ComboboxHelper.fillList(LandChargesRegistry.class));

        setTypes(ComboboxHelper.fillList(NoteType.class));

        setSubjectTypes(ComboboxHelper.fillList(SubjectType.class, false));

        this.setSexTypes(ComboboxHelper.fillList(SexTypes.class, false));

        this.setCountries(ComboboxHelper.fillList(Country.class,
                Order.asc("description"), new Criterion[]
                        {
                                Restrictions.ne("description", "ITALIA")
                        }));
        if (ValidationHelper.isNullOrEmpty(tempIdCounter)) {
            this.tempIdCounter = 0L;
        }
        setSelectedRegimeId(null);
       
    }

    private void fillSectionAList(Long formalityId) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (getEntity().getSectionA() != null && !ValidationHelper.isNullOrEmpty(getEntity().getType())) {
            NoteType id = NoteType.getEnumByString(getEntity().getTypeEnum().toString());
            String code = "";
            TypeFormality typeFormality = null;
            List<TypeFormality> typeFormalities = null;
            switch (id) {
            case NOTE_TYPE_I:
                code = getCodeSectionA(getEntity().getSectionA().getDerivedFrom());
                typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                        Restrictions.eq("code", code), Restrictions.ne("type", TypeActEnum.TYPE_PS)});
                if(!ValidationHelper.isNullOrEmpty(typeFormalities)) {
                    typeFormality = typeFormalities.get(0);
                }
                if (!ValidationHelper.isNullOrEmpty(typeFormality)) {
                    setDerivedFrom(typeFormality.getId());
                }
                break;
            case NOTE_TYPE_A:
                code = getCodeSectionA(getEntity().getSectionA().getAnnotationDescription());
                typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                        Restrictions.eq("code", code), Restrictions.ne("type", TypeActEnum.TYPE_PS)});
                if (!ValidationHelper.isNullOrEmpty(typeFormality)) {
                    setAnnotationDescription(typeFormality.getId());
                }
                break;
            case NOTE_TYPE_T:
                code = getCodeSectionA(getEntity().getSectionA().getConventionDescription());
                typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                        Restrictions.eq("code", code), Restrictions.ne("type", TypeActEnum.TYPE_PS)});
                if (!ValidationHelper.isNullOrEmpty(typeFormalities)) {
                    typeFormality = typeFormalities.get(0);
                    setConventionDescription(typeFormality.getId());
                }
                break;
            }
        }
    }

    private String getCodeSectionA(String value) {
        if (!ValidationHelper.isNullOrEmpty(value)) {

            if (value.charAt(0) == '0') {
                return value.substring(1, value.indexOf(' '));
            } else {
                if (value.contains(" ")) {
                    return value.substring(0, value.indexOf(' '));
                } else {
                    return value;
                }
            }
        }
        return "";
    }

    private void fillSectionBList(Long formalityId) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<SectionB> sectionBList = DaoManager.load(SectionB.class, new Criterion[]{Restrictions.eq("formality.id", formalityId)});
        if (ValidationHelper.isNullOrEmpty(getSectionBList())) {
            setSectionBList(new ArrayList<>());
        }
        getSectionBList().addAll(sectionBList);

        for (int i = 0; i < sectionBList.size(); i++) {
            getUnits().add(String.valueOf(i + 1));
        }

        bargainingUnitIncrement = Long.valueOf(sectionBList.size());
    }

    private void fillSectionCList(Long formalityId) throws PersistenceBeanException, IllegalAccessException {
        List<SectionC> sectionCList = DaoManager.load(SectionC.class, new Criterion[]{Restrictions.eq("formality.id", formalityId)});

        for (SectionC c : sectionCList) {
            for (Subject subject : c.getSubject()) {
                if (SectionCType.A_FAVORE.getName().equals(c.getSectionCType())) {
                    addToSectionCList(subject, getaFavoreSectionC());
                } else if (SectionCType.CONTRO.getName().equals(c.getSectionCType())) {
                    addToSectionCList(subject, getControSectionC());
                } else if (SectionCType.DEBITORI_NON_DATORI_DI_IPOTECA.getName().equals(c.getSectionCType())) {
                    addToSectionCList(subject, getDebitoriSectionC());
                }
            }
        }
    }

    private void addToSectionCList(Subject subject, SectionC sectionC) {
        if (ValidationHelper.isNullOrEmpty(sectionC.getSubject()))
            sectionC.setSubject(new ArrayList<>());
        if (!sectionC.getSubject().contains(subject))
            sectionC.getSubject().add(subject);
    }

    private void fillSectionDList(Long formalityId) throws PersistenceBeanException, IllegalAccessException {
        List<SectionD> sectionDList = DaoManager.load(SectionD.class, new Criterion[]{Restrictions.eq("formality.id", formalityId)});

        if (!ValidationHelper.isNullOrEmpty(sectionDList))
            setSectionD(sectionDList.get(0));
    }

    private void loadSelectLists() throws PersistenceBeanException, IllegalAccessException {
        setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
        getProvinces().add(new SelectItem(Province.FOREIGN_COUNTRY_ID,
                Province.FOREIGN_COUNTRY));
        if (getProvinces().size() == 2) {
            setAddressProvinceId((Long) getProvinces().get(1).getValue());
            /* handleAddressProvinceChange();*/
        } else {
            setAddressCities(ComboboxHelper.fillList(new ArrayList<City>(), true));
        }
        setRealEstateTypes(ComboboxHelper.fillList(RealEstateType.class, false));
        List<CadastralCategory> cadastralCategoryList = DaoManager.load(CadastralCategory.class);
        cadastralCategoryList.sort(new CadastralCategoryComparator());
        setCategories(ComboboxHelper.fillList(cadastralCategoryList, true, false));
        setPropertyTypeList(ComboboxHelper.fillList(PropertyTypeEnum.class, false, false));

        setCodeAndDescription(ComboboxHelper.fillListDictionary(TypeFormality.class,
                new Criterion[]{Restrictions.ne("type", TypeActEnum.TYPE_PS)}));

        setUnits(new ArrayList<>());

        setRelationshipTypeList(ComboboxHelper.fillList(TipologieDiritti.class, false, false));
        
        setRegimeList(ComboboxHelper.fillList(Regime.class, true, false));
        
        List<SectionDFormat> sectDFormats = DaoManager.load(SectionDFormat.class);
        List<SelectItem> items = new LinkedList<>();
        for(SectionDFormat sectionDFormat : sectDFormats) {
            items.add(new SelectItem(sectionDFormat.getId(), sectionDFormat.toString()));
        }
        setSectionDs(items);
    }

    public void fillFilteredProvinces() {
        try {
            LandChargesRegistry reg = DaoManager.get(LandChargesRegistry.class,
                    getSelectedId());

            if (!ValidationHelper.isNullOrEmpty(reg)) {
                filteredProvinces = ComboboxHelper.fillList(reg.getProvinces().toArray());
            }
        } catch (HibernateException | InstantiationException | IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
    }

    public void fillProperties(SectionB sectionB) throws HibernateException, 
            InstantiationException, IllegalAccessException, PersistenceBeanException {
        setPropertyList(new ArrayList<Property>());
        setEditedSectionB(sectionB);
        try {
            if(!ValidationHelper.isNullOrEmpty(getSelectedId())) {
                LandChargesRegistry landChargesRegistry = DaoManager.get(LandChargesRegistry.class,
                        new Criterion[]{
                                Restrictions.eq("id", getSelectedId())});
                getEntity().setReclamePropertyService(landChargesRegistry);
            }else {
                getEntity().setReclamePropertyService(null);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        
        if(getEntity() != null
                && !ValidationHelper.isNullOrEmpty(getEntity().getReclamePropertyService())
                && !ValidationHelper.isNullOrEmpty(getEntity().getReclamePropertyService().getAggregationLandChargesRegistries())) {

            List<Long> aggregationIds = getEntity().getReclamePropertyService().getAggregationLandChargesRegistries()
                    .stream()
                    .map(AggregationLandChargesRegistry::getId).collect(Collectors.toList());
            if(!ValidationHelper.isNullOrEmpty(aggregationIds)) {

                try {
                    Request request = DaoManager.get(Request.class, getEditedRequestId());
                    if(!ValidationHelper.isNullOrEmpty(request) 
                            && !ValidationHelper.isNullOrEmpty(request.getSubjectList())) {
                      
                        for(Subject subject : request.getSubjectList()) {
                            
                            List<Criterion> restrictionsList = new ArrayList<>();
                            restrictionsList.add(Restrictions.eq("stateId", RequestState.EVADED.getId()));
                            restrictionsList.add(Restrictions.in("aggregationLandChargesRegistry.id", aggregationIds));
                            restrictionsList.add(Restrictions.eq("subject", subject));
                            restrictionsList.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                                    Restrictions.isNull("isDeleted")));

                            List<Request> result =  DaoManager.load(Request.class, 
                                    null,restrictionsList.toArray(new Criterion[0]),
                                    new Order[]{Order.desc("evasionDate")});
                            
                            if(!ValidationHelper.isNullOrEmpty(result)) {
                                List<SituationProperty> situationProperties = result.get(0).getSituationEstateLocations().stream()
                                        .map(EstateSituation::getSituationProperties).flatMap(List::stream).collect(Collectors.toList());

                                if(!ValidationHelper.isNullOrEmpty(situationProperties)) {
                                    for(SituationProperty situationProperty : situationProperties) {
                                        if(!ValidationHelper.isNullOrEmpty(situationProperty.getProperty())) {
                                            boolean isPresent = false;
                                            if(!ValidationHelper.isNullOrEmpty(getEditedSectionB().getProperties())) {
                                                isPresent = getEditedSectionB().getProperties().stream().filter(o -> o.getId().equals(situationProperty.getProperty().getId())).findFirst().isPresent();
                                            }
                                            if(!isPresent) {
                                                situationProperty.getProperty().setCurrentRequest(result.get(0));
                                                getPropertyList().add(situationProperty.getProperty());
                                            }
                                                
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (HibernateException | InstantiationException | IllegalAccessException
                        | PersistenceBeanException e) {
                    LogHelper.log(log, e);
                }
            }
        }
    }

    public void associateProperties() throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedSearchedProperty())) {

            List<Long> idsProperty = getSelectedSearchedProperty().stream().map(IndexedEntity::getId)
                    .collect(Collectors.toList());

            List<Property> properties = DaoManager.load(Property.class, new Criterion[]{
                    Restrictions.in("id", idsProperty)});

            if (ValidationHelper.isNullOrEmpty(getEditedSectionB().getProperties())) {
                getEditedSectionB().setProperties(new ArrayList<>());
                getEditedSectionB().getProperties().addAll(properties);
            }else {
                for (Property property : properties) {
                    boolean isPresent = getEditedSectionB().getProperties().stream().filter(o -> o.getId().equals(property.getId())).findFirst().isPresent();
                    if(!isPresent) {
                        getEditedSectionB().getProperties().add(property);
                    }
                }
            }
            for (SectionB b : sectionBList) {
                if (b.getBargainingUnit().equals(getEditedSectionB().getBargainingUnit())) {
                    b.setProperties(getEditedSectionB().getProperties());
                }
            }
            
        }

        setCategoryId(new Long(0L));
        setCadastralDataList(new ArrayList<>());
        setNewProperty(new Property());
        setNewCadastralData(new CadastralData());
        setRenderForm(false);
        setBuilding(false);
        setAddressCityId(new Long(0L));
        setAddressProvinceId(new Long(0L));
        try {
            savePropertyRelationship(getaFavoreSectionC());
            savePropertyRelationship(getControSectionC());
            savePropertyRelationship(getDebitoriSectionC());
        } catch (Exception e) {
            LogHelper.log(log, e);
        } 
    }


    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {

    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException,
    IOException, InstantiationException, IllegalAccessException {

        fillFormality();

        if (validateFormalityBeforeSaving()) {
                saveFormalityToDB();

                saveSectionAToDB();
                saveSectionBToDB();
                saveSectionCToDB();
                saveSectionDToDB();

                addRequestFormalityForcedRecord();

            if (!ValidationHelper.isNullOrEmpty(getRelationshipsToDeleteOnSave())) {
                for (Relationship relationship : getRelationshipsToDeleteOnSave()) {
                        DaoManager.remove(relationship);
                }
            }
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "",
                    ResourcesHelper.getValidation("formalityCreateError"));
        }

        returnFormalityFromDialog();
    }


    private void addRequestFormalityForcedRecord() throws PersistenceBeanException, InstantiationException,
    IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getRequestId())) {
            Request request = DaoManager.get(Request.class, getRequestId());
            if (ValidationHelper.isNullOrEmpty(request.getSubject())) {
                FormalityHelper.addRequestFormalityForcedRecordIfFormalitiesAreNotLinkedWithRequest(request,
                		 Collections.singletonList(getEntity()), false);
            }
        }
    }

    private void saveFormalityToDB() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        DaoManager.save(getEntity());
    }

    private void fillFormality() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedId())) {
            getEntity().setReclamePropertyService(DaoManager.get(LandChargesRegistry.class,
                    getSelectedId()));
        } else {
            getEntity().setReclamePropertyService(null);
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedType())) {
            getEntity().setType(String.valueOf(NoteType.findById(getSelectedType())).toLowerCase());
        } else {
            getEntity().setType("");
        }

        if (ValidationHelper.isNullOrEmpty(getEntity().getId())) {
            getEntity().setState(FormalityStateType.MANUALE.getId());
        }
    }

    private boolean validateFormalityBeforeSaving() throws PersistenceBeanException, IllegalAccessException {
        List<Formality> formalityList = DaoManager.load(Formality.class, new Criterion[]{
                Restrictions.eq("generalRegister", getEntity().getGeneralRegister()),
                Restrictions.eq("particularRegister", getEntity().getParticularRegister()),
                Restrictions.eq("presentationDate", getEntity().getPresentationDate()),
                Restrictions.or(
                        Restrictions.eq("state", FormalityStateType.STRUTTURATA.getId()),
                        Restrictions.eq("state", FormalityStateType.TITOLO.getId()),
                        Restrictions.eq("state", FormalityStateType.MANUALE.getId())),
                Restrictions.or(
                        Restrictions.isNotNull("reclamePropertyService"),
                        Restrictions.isNotNull("provincialOffice")),
        });

        if (ValidationHelper.isNullOrEmpty(formalityList) || !ValidationHelper.isNullOrEmpty(getFormalityId())) {
            setValidated(true);
        } else {
            setValidated(false);
        }
        return isValidated();
    }

    private void saveSectionAToDB() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        getSectionA().setAnnotationDescription(getCodeAndDescriptionFormat(getAnnotationDescription(), false));
        getSectionA().setDerivedFrom(getCodeAndDescriptionFormat(getDerivedFrom(), false));

        getSectionA().setConventionDescription(getCodeAndDescriptionFormat(getConventionDescription(), false));

        if (!ValidationHelper.isNullOrEmpty(getAnnotationDescription())) {
            getSectionA().setDerivedFromCode(getCodeAndDescriptionFormat(getAnnotationDescription(), true));
        } else if (!ValidationHelper.isNullOrEmpty(getConventionDescription())) {
            getSectionA().setDerivedFromCode(getCodeAndDescriptionFormat(getConventionDescription(), true));
        } else if (!ValidationHelper.isNullOrEmpty(getDerivedFrom())) {
            getSectionA().setDerivedFromCode(getCodeAndDescriptionFormat(getDerivedFrom(), true));
        }
        if (!ValidationHelper.isNullOrEmpty(getConservatoryFilterId())) {
            LandChargesRegistry reg = DaoManager.get(LandChargesRegistry.class,
                    getConservatoryFilterId());
            getSectionA().setLandChargesRegistry(reg);
        }



        if (!ValidationHelper.isNullOrEmpty(getSelectedCourt())) {
            Court court = DaoManager.get(Court.class,
                    new Criterion[]{
                            Restrictions.eq("name", getSelectedCourt())});

            if(!ValidationHelper.isNullOrEmpty(court)) {
                if(!ValidationHelper.isNullOrEmpty(court.getCity()))
                    getSectionA().setSeat(court.getCity().getDescription());
                else
                    getSectionA().setSeat(null);

                if(!ValidationHelper.isNullOrEmpty(court.getFiscalCode()))
                    getSectionA().setFiscalCode(court.getFiscalCode());
                else
                    getSectionA().setFiscalCode(null);

                if(!ValidationHelper.isNullOrEmpty(court.getName()))
                    getSectionA().setPublicOfficial(court.getName());
                else
                    getSectionA().setPublicOfficial(null);
            }else {
                getSectionA().setPublicOfficial(getSelectedCourt());
            }
        }

        getSectionA().setFormality(getEntity());
        DaoManager.save(getSectionA());
    }

    private void saveSectionBToDB() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(sectionBList)) {
            for (SectionB b : sectionBList) {
                b.setFormality(getEntity());
                if (!ValidationHelper.isNullOrEmpty(b.getProperties())) {
                    for (Property property : b.getProperties()) {
                        List<CadastralData> list = new ArrayList<>();
                        for (Iterator<CadastralData> iterator = property.getCadastralData().iterator(); iterator.hasNext(); ) {
                            CadastralData cadastralData = iterator.next();
                            CadastralData data = cadastralData.loadOrCopy(DaoManager.getSession());
                            if (ValidationHelper.isNullOrEmpty(data.getId())) {
                                DaoManager.save(data);
                            }
                            list.add(data);
                        }
                        property.setCadastralData(list.stream()
                        .collect(Collectors.toSet()));
                        DaoManager.save(property);
                    }
                }
                DaoManager.save(b);
            }
        }
    }

    private void saveSectionCToDB() throws PersistenceBeanException, InstantiationException, IllegalAccessException {

    	 saveAllElementsOfSectionCToDB(getaFavoreSectionC());
         saveAllElementsOfSectionCToDB(getControSectionC());
         saveAllElementsOfSectionCToDB(getDebitoriSectionC());

        if (!ValidationHelper.isNullOrEmpty(getSubjectsToDeleteOnSave())) {
            for (Subject subject : getSubjectsToDeleteOnSave()) {
                for (Relationship relationship : subject.getRelationshipList()) {
                    DaoManager.remove(relationship);
                }
                DaoManager.remove(subject);
            }
        }
    }

    private void saveAllElementsOfSectionCToDB(SectionC tempSectionC) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        tempSectionC.setFormality(getEntity());
        if (!ValidationHelper.isNullOrEmpty(tempSectionC.getSubject())) {
            deleteOldSectionsC(getEntity(), tempSectionC.getSectionCType());
            List<Subject> newSubjects = new ArrayList<>();
            for (Iterator<Subject> iterator = tempSectionC.getSubject().iterator(); iterator.hasNext(); ) {
                Subject subject = iterator.next();
                
                
                
                if (!Hibernate.isInitialized(subject.getRelationshipList())) {
                    subject.setRelationshipList(DaoManager.get(Subject.class, subject.getId()).getRelationshipList());
                }
                if (!ValidationHelper.isNullOrEmpty(subject.getRelationshipList())) {
                    
                    for (Relationship relationship : subject.getRelationshipList()) {
                        if (ValidationHelper.isNullOrEmpty(relationship.getId())) {
                            relationship.setSubject(subject);

                            relationship.setSectionCType(tempSectionC.getSectionCType());
                            relationship.setFormality(getEntity());

                            if (!ValidationHelper.isNullOrEmpty(getSectionBList()) && relationship.isNew()) {
                                List<Property> properties = getSectionBList().stream().filter(x -> relationship.getUnitaNeg()
                                        .equals(x.getBargainingUnit())).map(SectionB::getProperties).flatMap(List::stream)
                                        .collect(Collectors.toList());
                                for (Property property : properties) {
                                    Relationship newRelationship = new Relationship(relationship);
                                    newRelationship.setProperty(property);
                                    DaoManager.save(newRelationship);
                                }
                            } else {
                                DaoManager.save(relationship);
                            }
                            
                           
                        } else {
                            DaoManager.save(relationship);
                        }
                       
                    }
                    
                }

                if (ValidationHelper.isNullOrEmpty(subject.getSectionC())) {
                    subject.setSectionC(new ArrayList<>());
                }

                newSubjects.add(subject);
                DaoManager.save(subject);

            }
            /* for (Subject subject : newSubjects) {
                subject.getSectionC().add(tempSectionC);
            }*/
            tempSectionC.setSubject(newSubjects);
            DaoManager.save(tempSectionC);
        }
    }

    private void deleteOldSectionsC(Formality entity, String sectionCType) throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(entity)) {
            return;
        }

        List<SectionC> sectionCListForDelete = DaoManager.load(SectionC.class, new Criterion[]{
                Restrictions.eq("formality.id", getEntity().getId()),
                Restrictions.eq("sectionCType", sectionCType)});

        if (!ValidationHelper.isNullOrEmpty(sectionCListForDelete)) {
            for (SectionC c : sectionCListForDelete) {
                c.setSubject(null);
                DaoManager.save(c);
                DaoManager.remove(c);
            }
        }


    }

    private void saveSectionDToDB() throws PersistenceBeanException {
     	getSectionD().setFormality(getEntity());
    	DaoManager.save(getSectionD());
    }

    public void saveSectionB() throws PersistenceBeanException, IllegalAccessException, InstantiationException {

        if (!ValidationHelper.isNullOrEmpty(getAddressProvinceId())) {
            Province province = DaoManager.get(Province.class, this.getAddressProvinceId());
            getNewProperty().setProvince(province);
        } else {
            getNewProperty().setProvince(null);
        }

        if (!ValidationHelper.isNullOrEmpty(getAddressCityId())) {
            City city = DaoManager.get(City.class, getAddressCityId());
            getNewProperty().setCity(city);
        } else {
            getNewProperty().setCity(null);
        }

        getNewProperty().setType(getSelectedTypeId());

        if (!ValidationHelper.isNullOrEmpty(this.getCategoryId()) && RealEstateType.BUILDING.getId().equals(getNewProperty().getType())) {
            getNewProperty().setCategory(DaoManager.get(CadastralCategory.class, getCategoryId()));
        } else if (RealEstateType.LAND.getId().equals(getNewProperty().getType())) {
            getNewProperty().setCategory(DaoManager.get(CadastralCategory.class, new Criterion[]{Restrictions.eq("codeInVisura", "T")}));
        } else if (ValidationHelper.isNullOrEmpty(getNewProperty().getCategory())) {
            getNewProperty().setCategory(null);
        }


        if (ValidationHelper.isNullOrEmpty(getNewProperty().getCadastralData())) {
            getNewProperty().setCadastralData(new HashSet<>());
            getNewProperty().getCadastralData().add(getNewCadastralData());
        } else if (!getNewProperty().getCadastralData().contains(getNewCadastralData())) {
            getNewProperty().getCadastralData().add(getNewCadastralData());
        }

        if (!ValidationHelper.isNullOrEmpty(getCadastralDataList())) {
            for (CadastralData data : getCadastralDataList()) {
                if (!getNewProperty().getCadastralData().contains(data))
                    getNewProperty().getCadastralData().add(data);
            }
        }

        if (ValidationHelper.isNullOrEmpty(getEditedSectionB().getProperties())) {
            getEditedSectionB().setProperties(new ArrayList<>());
            getEditedSectionB().getProperties().add(getNewProperty());
        } else if (ValidationHelper.isNullOrEmpty(getNewProperty().getId())) {
            getEditedSectionB().getProperties().add(getNewProperty());
        } else {
            List<Property> newPropertyList = new ArrayList<>();
            for (Property property : getEditedSectionB().getProperties()) {
                if (property.getId().equals(getNewProperty().getId())) {
                    newPropertyList.add(getNewProperty());
                } else {
                    newPropertyList.add(property);
                }
            }
            getEditedSectionB().setProperties(newPropertyList);
        }

        for (SectionB b : sectionBList) {
            if (b.getBargainingUnit().equals(getEditedSectionB().getBargainingUnit())) {
                b.setProperties(getEditedSectionB().getProperties());
            }
        }

        setCategoryId(new Long(0L));
        setCadastralDataList(new ArrayList<>());
        setNewProperty(new Property());
        setNewCadastralData(new CadastralData());
        setRenderForm(false);
        setBuilding(false);
        setAddressCityId(new Long(0L));
        setAddressProvinceId(new Long(0L));
    }

    public void saveSubjectSectionC() throws PersistenceBeanException, IllegalAccessException, InstantiationException {


        if (!ValidationHelper.isNullOrEmpty(getAddressProvinceIdC())) {
            Province province = DaoManager.get(Province.class, this.getAddressProvinceIdC());
            getNewSubject().setBirthProvince(province);
        } else {
            getNewSubject().setBirthProvince(null);
        }

        if (!ValidationHelper.isNullOrEmpty(getAddressCityIdC())) {
            City city = DaoManager.get(City.class, getAddressCityIdC());
            getNewSubject().setBirthCity(city);
        } else {
            getNewSubject().setBirthCity(null);
        }

        if (getForeignCountry()
                && !ValidationHelper.isNullOrEmpty(this.getSelectedCountryId())) {
            Country country = DaoManager.get(Country.class,
                    this.getSelectedCountryId());
            this.getNewSubject().setCountry(country);
        }
        this.getNewSubject().setForeignCountry(getForeignCountry());

        boolean invalid =
                ValidationHelper.isNullOrEmpty(getNewSubject().getTypeId()) ||
                (getNewSubject().getTypeIsPhysicalPerson() ?
                        (ValidationHelper.isNullOrEmpty(getNewSubject().getSurname()) ||
                                ValidationHelper.isNullOrEmpty(getNewSubject().getName()) ||
                                ValidationHelper.isNullOrEmpty(getNewSubject().getBirthDate()) ||
                                ValidationHelper.isNullOrEmpty(getNewSubject().getSex())) :
                                    (ValidationHelper.isNullOrEmpty(getNewSubject().getBusinessName()) ||
                                            ValidationHelper.isNullOrEmpty(getNewSubject().getNumberVAT()))) ||
                ValidationHelper.isNullOrEmpty(getNewSubject().getFiscalCode()) ||
                !(ValidationHelper.isNullOrEmpty(getNewSubject().getBirthProvince()) &&
                        ValidationHelper.isNullOrEmpty(getNewSubject().getBirthCity()) ||
                        !(ValidationHelper.isNullOrEmpty(getNewSubject().getCountry()) &&
                                ValidationHelper.isNullOrEmpty(getNewSubject().getForeignCountry())));

        boolean isExists = isExistsInCurrentLists(getNewSubject());

        if (!isExists && !invalid) {
            Subject subjectFromDB = SubjectHelper.getSubjectIfExists(getNewSubject(), getNewSubject().getTypeId());
            if (subjectFromDB != null) {
                setNewSubject(subjectFromDB);
            }else {
                Subject newSubject = SubjectHelper.copySubject(getNewSubject());
                setNewSubject(newSubject);
            }
            getNewSubject().setTempId(++tempIdCounter);

            switch (getSectionCType()) {
            case A_FAVORE:
                changeOrAddNewSubject(getaFavoreSectionC());
                break;
            case CONTRO:
                changeOrAddNewSubject(getControSectionC());
                break;
            case DEBITORI_NON_DATORI_DI_IPOTECA:
                changeOrAddNewSubject(getDebitoriSectionC());
                break;
            }
        }

        getNewSubject().updateRelationshipsToView();

        setNewSubject(new Subject());
        setAddressCityIdC(new Long(0L));
        setAddressProvinceIdC(new Long(0L));
        setSelectedCountryId(new Long(0L));

        if (invalid)
            executeJS("PF('invalidDataErrorDialogWV').show();");
        else if (isExists)
            executeJS("PF('alreadyExistingDataErrorDialogWV').show();");
        else
            executeJS("PF('sectionCAddDlgWV').hide();");
    }

    private void changeOrAddNewSubject(SectionC sectionToManipulate) {
        if (ValidationHelper.isNullOrEmpty(sectionToManipulate.getSubject()))
            sectionToManipulate.setSubject(new ArrayList<>());

        if (!ValidationHelper.isNullOrEmpty(getPreviousSubject())) {
            sectionToManipulate.getSubject().remove(getPreviousSubject());
            setPreviousSubject(new Subject());
        }
        sectionToManipulate.getSubject().add(getNewSubject());
    }

    private boolean isExistsInCurrentLists(Subject subject) {
        Boolean thereIsInFavore = checkSectionCList(subject, getaFavoreSectionC());
        if (thereIsInFavore != null) return thereIsInFavore;

        Boolean thereIsInContro = checkSectionCList(subject, getControSectionC());
        if (thereIsInContro != null) return thereIsInContro;

        Boolean thereIsInDebitori = checkSectionCList(subject, getDebitoriSectionC());
        if (thereIsInDebitori != null) return thereIsInDebitori;

        return false;
    }

    private Boolean checkSectionCList(Subject subject, SectionC sectionC) {
        if (!ValidationHelper.isNullOrEmpty(sectionC.getSubject())) {

            for (Subject tempSubject : sectionC.getSubject()) {
                if (subject.getTypeIsPhysicalPerson()) {
                    return comparePhysicaSubjects(subject, tempSubject);
                } else {
                    return compareLegalSubjects(subject, tempSubject);
                }
            }
        }
        return null;
    }


    private boolean comparePhysicaSubjects(Subject subject, Subject tempSubject) {
        if (subject.getName().equals(tempSubject.getName())
                && subject.getSurname().equals(tempSubject.getSurname())
                && subject.getBirthDate().equals(tempSubject.getBirthDate())
                && subject.getFiscalCode().equals(tempSubject.getFiscalCode())) {

            if (!ValidationHelper.isNullOrEmpty(subject.getBirthProvince())
                    && !ValidationHelper.isNullOrEmpty(tempSubject.getBirthProvince())
                    && subject.getBirthProvince().getId().equals(tempSubject.getBirthProvince().getId())
                    && subject.getBirthCity().getId().equals(tempSubject.getBirthCity().getId())) {
                return true;
            } else if (!ValidationHelper.isNullOrEmpty(subject.getCountry())
                    && !ValidationHelper.isNullOrEmpty(tempSubject.getCountry())
                    && subject.getCountry().getId().equals(tempSubject.getCountry().getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean compareLegalSubjects(Subject subject, Subject tempSubject) {
        if (!ValidationHelper.isNullOrEmpty(subject.getNumberVAT())
                && !ValidationHelper.isNullOrEmpty(subject.getBusinessName())
                && subject.getNumberVAT().equals(tempSubject.getNumberVAT())
                && RequestHelper.isBusinessNameFunctionallyEqual(subject.getBusinessName(),
                        tempSubject.getBusinessName())
                && subject.getFiscalCode().equals(tempSubject.getFiscalCode())) {

            if (!ValidationHelper.isNullOrEmpty(subject.getBirthProvince())
                    && !ValidationHelper.isNullOrEmpty(tempSubject.getBirthProvince())
                    && subject.getBirthProvince().getId().equals(tempSubject.getBirthProvince().getId())
                    && subject.getBirthCity().getId().equals(tempSubject.getBirthCity().getId())) {
                return true;
            } else if (!ValidationHelper.isNullOrEmpty(subject.getCountry())
                    && !ValidationHelper.isNullOrEmpty(tempSubject.getCountry())
                    && subject.getCountry().getId().equals(tempSubject.getCountry().getId())) {
                return true;
            }
        }
        return false;
    }

    public void saveRelationship() throws PersistenceBeanException, IllegalAccessException, HibernateException, InstantiationException {
        cleanValidation();
        if (ValidationHelper.isNullOrEmpty(getQuote1())
                || ValidationHelper.isNullOrEmpty(getQuote2())
                || Double.parseDouble(getQuote1().replaceAll(",", "."))
                / Double.parseDouble(getQuote2().replaceAll(",", ".")) > 1.0) {
            addRequiredFieldException("form:inputQuote1");
            addRequiredFieldException("form:inputQuote2");
            return;
        }

        String quote = getQuote1() + "/" + getQuote2();
        getNewRelationship().setQuote(quote);
        getNewRelationship().setNew(true);

        if(!ValidationHelper.isNullOrEmpty(getNewRelationship().getPropertyType())) {
            Long relationshipTypeId = Long.valueOf(getNewRelationship().getPropertyType());
            TipologieDiritti tioTipologieDiritti = DaoManager.get(TipologieDiritti.class, relationshipTypeId);
            if(!ValidationHelper.isNullOrEmpty(tioTipologieDiritti))
                getNewRelationship().setPropertyType(tioTipologieDiritti.getName());
        }
        if(!ValidationHelper.isNullOrEmpty(getSelectedRegimeId())) {
            Regime selectedRegime = DaoManager.get(Regime.class, getSelectedRegimeId());
            if(!ValidationHelper.isNullOrEmpty(selectedRegime))
                getNewRelationship().setRegime(selectedRegime.getText());
        }

        if (ValidationHelper.isNullOrEmpty(getEditSubject().getRelationshipList())) {
            getEditSubject().setRelationshipList(new ArrayList<>());
            getEditSubject().getRelationshipList().add(getNewRelationship());
        } else if (!getEditSubject().getRelationshipList().contains(getNewRelationship())) {
            getEditSubject().getRelationshipList().add(getNewRelationship());
        }

        addEditSubjectToCurrentList(getEditSubject());

        getEditSubject().updateRelationshipsToView();

        setQuote1("");
        setQuote2("");
        setNewRelationship(new Relationship());
        setEditSubject(new Subject());
    }

    private void addEditSubjectToCurrentList(Subject editSubject) {
        if (insertUpdatedSubject(editSubject, getaFavoreSectionC())) {
            return;
        }
        if (insertUpdatedSubject(editSubject, getControSectionC())) {
            return;
        }
        if (insertUpdatedSubject(editSubject, getDebitoriSectionC())) {
            return;
        }
    }

    private boolean insertUpdatedSubject(Subject editSubject, SectionC sectionC) {
        if (!ValidationHelper.isNullOrEmpty(sectionC.getSubject())) {
            for (Subject tempSubject : sectionC.getSubject()) {
                if (!ValidationHelper.isNullOrEmpty(tempSubject.getId())) {
                    if (tempSubject.getId().equals(editSubject.getId())) {
                        tempSubject = editSubject;
                        return true;
                    }
                } else {
                    if (tempSubject.getTempId().equals(editSubject.getTempId())) {
                        tempSubject = editSubject;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void cancelSaveFile() {

        setRenderForm(false);
        setBuilding(false);
        setNewCadastralData(new CadastralData());
        setNewProperty(new Property());
        setNewSubject(new Subject());

        setAddressCityId(new Long(0L));
        setAddressProvinceId(new Long(0L));
        setAddressCityIdC(new Long(0L));
        setAddressProvinceIdC(new Long(0L));
        setSelectedRegimeId(null);
    }
    
    public void cancelSectionD() {
        setSelectedSectionDId(null);
    }
    
    public void setSectionD() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        SectionDFormat sectionDFormat = DaoManager.get(SectionDFormat.class,
                new Criterion[]{
                        Restrictions.eq("id", getSelectedSectionDId())});
        getSectionD().setAdditionalInformation(sectionDFormat.getText());
        setSelectedSectionDId(null);
    }


    public void handleAddressProvinceChangeSectionB() throws PersistenceBeanException, IllegalAccessException {
        if (!Province.FOREIGN_COUNTRY_ID.equals(getAddressProvinceId())) {
            this.setForeignCountry(Boolean.FALSE);

            this.setAddressCities(ComboboxHelper.fillList(City.class,
                    Order.asc("description"), new Criterion[]{
                            Restrictions.eq("province.id",getAddressProvinceId()),
                            Restrictions.eq("external", Boolean.TRUE)
            }));
        } else {
            this.setForeignCountry(Boolean.TRUE);
        }
    }

    public void handleAddressProvinceChangeSectionC() throws PersistenceBeanException, IllegalAccessException {
        if (!Province.FOREIGN_COUNTRY_ID.equals(getAddressProvinceIdC())) {
            this.setForeignCountry(Boolean.FALSE);

            this.setAddressCitiesSectionC(ComboboxHelper.fillList(City.class,
                    Order.asc("description"), new Criterion[]{
                            Restrictions.eq("province.id",getAddressProvinceIdC()),
                            Restrictions.eq("external", Boolean.TRUE)
            }));
        } else {
            this.setForeignCountry(Boolean.TRUE);
        }
    }

    public void handleCreateProperty(SectionB sectionB) {
        setClickedCreatePropertyButton(true);
        setEditedSectionB(sectionB);
        fillFilteredProvinces();
        cancelSaveFile();
    }

    public void handleEditProperty(Property property, SectionB sectionB) throws PersistenceBeanException, IllegalAccessException {
        setClickedCreatePropertyButton(true);

        setEditedSectionB(sectionB);

        if(!ValidationHelper.isNullOrEmpty(property.getProvince()))
            setAddressProvinceId(property.getProvince().getId());
        if(!ValidationHelper.isNullOrEmpty(property.getCity()))
            setAddressCityId(property.getCity().getId());
        setSelectedTypeId(property.getType());
        setNewProperty(property);
        if(!ValidationHelper.isNullOrEmpty(property.getCadastralData()))
                setNewCadastralData(property.getCadastralData().stream().findFirst().orElse(null));

        fillFilteredProvinces();
        handleAddressProvinceChangeSectionB();
        handleAddressCityChange();
    }

    public void handleDeleteProperty(Property property, SectionB sectionB) throws PersistenceBeanException, IllegalAccessException {

        setEditedSectionB(sectionB);

        if (!ValidationHelper.isNullOrEmpty(property.getId())) {
            getEditedSectionB().getProperties().remove(property);

            detachRelatedSectionsB(property);

            DaoManager.remove(property);
        } else {
            getEditedSectionB().getProperties().remove(property);
        }


        for (SectionB b : sectionBList) {
            if (b.getBargainingUnit().equals(getEditedSectionB().getBargainingUnit())) {
                b.setProperties(getEditedSectionB().getProperties());
            }
        }
        try {
            deletePropertyRelationship(getaFavoreSectionC());
            deletePropertyRelationship(getControSectionC());
            deletePropertyRelationship(getDebitoriSectionC());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void detachRelatedSectionsB(Property property) throws PersistenceBeanException, IllegalAccessException {
        List<SectionB> sectionBS = DaoManager.load(SectionB.class, new CriteriaAlias[]{
                new CriteriaAlias("properties", "p", JoinType.INNER_JOIN)}, new Criterion[]{
                        Restrictions.eq("p.id", property.getId())});

        for (SectionB b : sectionBS) {
            List<Property> propertiesToSave = new ArrayList<>();
            for (Property bProperty : b.getProperties()) {
                if (!bProperty.getId().equals(property.getId())) {
                    propertiesToSave.add(bProperty);
                }
            }
            b.setProperties(propertiesToSave);
            DaoManager.save(b);
        }
    }


    public void handleEditSubject(Subject subject, SectionC sectionC) throws PersistenceBeanException, IllegalAccessException {
        setSectionCType(SectionCType.getByName(sectionC.getSectionCType()));
        setNewSubject(subject);
        setAddressProvinceIdC(subject.getSelectProvinceId());
        setAddressCityIdC(subject.getSelectedCityId());
        handleAddressProvinceChangeSectionC();

        setPreviousSubject(subject);
    }

    public void handleDeleteSubject(Subject subjectToDelete, SectionC sectionC) throws PersistenceBeanException, IllegalAccessException {
        sectionC.getSubject().remove(subjectToDelete);

        if (ValidationHelper.isNullOrEmpty(getSubjectsToDeleteOnSave())) {
            setSubjectsToDeleteOnSave(new ArrayList<>());
        }
        getSubjectsToDeleteOnSave().add(subjectToDelete);
    }


    public void handleAddressCityChange() {
        checkCanRender();
    }

    public void handleTypeChange() {
        checkCanRender();
    }

    private void checkCanRender() {
        setRenderForm(!ValidationHelper.isNullOrEmpty(getAddressProvinceId())
                && !ValidationHelper.isNullOrEmpty(getAddressCityId())
                && !ValidationHelper.isNullOrEmpty(getSelectedTypeId()));

        getIsBuilding();
    }

    public void createNewSectionB() {
        bargainingUnitIncrement++;

        if (ValidationHelper.isNullOrEmpty(getSectionBList())) {
            sectionBList = new ArrayList<>();
        }

        SectionB tempSectionB = new SectionB(String.valueOf(bargainingUnitIncrement));

        tempSectionB.setFormality(getEntity());
        tempSectionB.setBargainingUnit(String.valueOf(bargainingUnitIncrement));

        getUnits().add(tempSectionB.getBargainingUnit());

        sectionBList.add(tempSectionB);
    }

    public void createNewSectionC(String type) {
        setSectionCType(SectionCType.valueOf(type));
    }

    public void createNewRelationship(Subject subject) {
        setEditSubject(subject);
    }

    public void setSectionDFormat() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        if(!ValidationHelper.isNullOrEmpty(getSectionD()) 
                && !ValidationHelper.isNullOrEmpty(getSectionD().getAdditionalInformation())) {
        
            List<SectionDFormat> sectionDFormats = DaoManager.load(SectionDFormat.class,
                    new Criterion[]{
                            Restrictions.eq("text", getSectionD().getAdditionalInformation().trim())});
            
            
            if(!ValidationHelper.isNullOrEmpty(sectionDFormats))
                setSelectedSectionDId(sectionDFormats.get(0).getId());
            else
               setSelectedSectionDId(null); 
        }else {
            setSelectedSectionDId(null);
        }
    }

    public void editRelationship(Subject subject, Relationship relationship) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        setSelectedRegimeId(null);
        setEditSubject(subject);
        setNewRelationship(relationship);
        if (!ValidationHelper.isNullOrEmpty(relationship.getQuote())) {
            setQuote1(relationship.getQuote().substring(0, relationship.getQuote().indexOf("/")));
            setQuote2(relationship.getQuote().substring(relationship.getQuote().indexOf("/") + 1));
        } else {
            setQuote1("");
            setQuote2("");
        }
        
        if (!ValidationHelper.isNullOrEmpty(relationship.getRegime())) {
            Regime regime = DaoManager.get(Regime.class,
                    new Criterion[]{
                            Restrictions.eq("text", relationship.getRegime())});
            if(!ValidationHelper.isNullOrEmpty(regime))
                setSelectedRegimeId(regime.getId());
            
        }
        executeJS("PF('relationshipDlg').show();");
    }


    public List<Property> getPropertiesOfSectionBToView(String unit) {
        List<Property> propertyList = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(unit) && !ValidationHelper.isNullOrEmpty(getSectionBList()))
            for (SectionB b : getSectionBList()) {
                if (b.getBargainingUnit().equals(unit) && !ValidationHelper.isNullOrEmpty(b.getProperties())) {
                    propertyList.addAll(b.getProperties());
                }
            }
        return propertyList;

    }

    public void generateFiscalCode() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        City city = null;
        try {
            if (!ValidationHelper.isNullOrEmpty(this.getAddressCityIdC())) {
                city = DaoManager.get(City.class, this.getAddressCityIdC());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (ValidationHelper.isNullOrEmpty(this.getNewSubject().getName())) {
            return;
        }
        if (ValidationHelper.isNullOrEmpty(this.getNewSubject().getSurname())) {
            return;
        }
        if (ValidationHelper.isNullOrEmpty(this.getNewSubject().getSex())) {
            return;
        }
        if (ValidationHelper.isNullOrEmpty(this.getNewSubject().getBirthDate())) {
            return;
        }

        Nationality nationality = null;
        if (!ValidationHelper.isNullOrEmpty(getSelectedCountryId())) {
            nationality = DaoManager.get(Nationality.class, getSelectedCountryId());
        }

        String cfis = "";
        if (!ValidationHelper.isNullOrEmpty(city)) {
            cfis = city.getCfis();
        } else if (!ValidationHelper.isNullOrEmpty(nationality)) {
            cfis = nationality.getCfis();
        }

        try {
            this.getNewSubject().setFiscalCode(CalcoloCodiceFiscale.calcola(
                    this.getNewSubject().getName(), this.getNewSubject().getSurname(),
                    this.getNewSubject().getBirthDate(), cfis,
                    SexTypes.getById(this.getNewSubject().getSex())));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public boolean getIsPhysicalPerson() {
        return getNewSubject().getTypeId() == null || SubjectType.PHYSICAL_PERSON
                .getId().equals(getNewSubject().getTypeId());
    }

    public void getIsBuilding() {
        boolean flag = getSelectedTypeId() == null || RealEstateType.BUILDING.getId().equals(getSelectedTypeId());
        setBuilding(flag);
    }

    public String getCategoryDescription() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        return loadDescriptionById(CadastralCategory.class, getCategoryId());
    }

    public String getCodeAndDescriptionFormat(Long value, boolean onlyCode) throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(value)) {
            TypeFormality typeFormality = DaoManager.get(TypeFormality.class, new Criterion[]
                    {Restrictions.eq("id", value)});

            String result = typeFormality.getCode();
            if (!onlyCode && !ValidationHelper.isNullOrEmpty(typeFormality.getDescription())) {
                result += " " + typeFormality.getDescription();
            }
            return result;
        }
        return null;
    }

    public void showPartsOfSectionABySelectedType() {

        if (!ValidationHelper.isNullOrEmpty(getSelectedType())) {
            setTypeAtto(String.valueOf(NoteType.findById(getSelectedType())));
        } else {
            setTypeAtto("");
        }
    }
   
    @Override
    public void goBack() {
        if(getViewFromRequest() != null && getViewFromRequest()) {
            SessionHelper.put("requestFormalityView", Boolean.TRUE);
            SessionHelper.put("editRequestId", getEditedRequestId());
            RedirectHelper.goToOnlyView(PageTypes.REQUEST_FORMALITY, this.getTranscriptionActId());    	
        }else if (ValidationHelper.isNullOrEmpty(getRequestId())) {
            RedirectHelper.goTo(PageTypes.DATABASE_LIST);
        } else {
            RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_VIEW,
                    getRequestId(), null);
        }

    }

    @Override
    public void afterSave() {
        if (isValidated()) {
            goBack();
        } else {
            return;
        }
    }

    public void addCadastralData() {
        cleanValidation();
        if (!ValidationHelper.isNullOrEmpty(getNewCadastralData().getSheet())
                || !ValidationHelper.isNullOrEmpty(getNewCadastralData().getParticle())
                || !ValidationHelper.isNullOrEmpty(getNewCadastralData().getScheda())
                || !ValidationHelper.isNullOrEmpty(getNewCadastralData().getDataScheda())) {

            if (isSaveScheda()) {
                if (ValidationHelper.isNullOrEmpty(getNewCadastralData().getScheda())) {
                    addRequiredFieldException("schedaText");
                }
                if (ValidationHelper.isNullOrEmpty(getNewCadastralData().getDataScheda())) {
                    addRequiredFieldException("dataSchedaText");
                }
            } else {
                if (ValidationHelper.isNullOrEmpty(getNewCadastralData().getSheet())
                        || ValidationHelper.isNullOrEmpty(getNewCadastralData().getParticle())) {
                    setValidationFailed(true);
                }
            }

            if (getValidationFailed()) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                        ResourcesHelper.getValidation("validationFailed"),
                        ResourcesHelper.getValidation("cadastralDataRequired"));
                return;
            }
            if (getCadastralDataList() == null) {
                setCadastralDataList(new ArrayList<>());
            }
            if (!getCadastralDataList().contains(getNewCadastralData())) {
                addCadastralData(getNewCadastralData());
            }
            setNewCadastralData(new CadastralData());
            updateFieldsCadastralData();

        }

        executeJS("PF('cadastralDialogWV').show();");
    }

    private void addCadastralData(CadastralData data) {
        if (getCadastralDataList() == null) {
            setCadastralDataList(new ArrayList<>());
        }
        if (data != null && !getCadastralDataList().contains(data)) {
            data.setTempId(getTempId().getAndIncrement());
            getCadastralDataList().add(data);
        }
        setSaveScheda(false);
    }

    public void updateFieldsCadastralData() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralDataList())) {
            setNewCadastralData(getCadastralDataList().get(0));
        }
    }

    private void returnFormalityFromDialog(){
        if(isItDialog()){
            RequestContext.getCurrentInstance().closeDialog(getEntity());
        }
    }

    public void addCadastralDataDlg() {
        boolean isValid = true;
        cleanValidation();

        if (ValidationHelper.isNullOrEmpty(getNewCadastralDataDlg().getParticle())) {
            markInvalid("addCadastralDataDLGparticleDlg", "requiredField");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        addCadastralData(getNewCadastralDataDlg());
        setNewCadastralDataDlg(new CadastralData());
        updateFieldsCadastralData();
    }

    public void deleteCadastralData() {
        if (getCadastralDataList() != null) {
            for (CadastralData data : getCadastralDataList()) {
                if (data.getTempId().equals(getDeleteTempId())) {
                    if (getCadastralDataForDeleteList() == null) {
                        setCadastralDataForDeleteList(new ArrayList<>());
                    }

                    getCadastralDataForDeleteList().add(data);
                    getCadastralDataList().remove(data);

                    break;
                }
            }
            updateFieldsCadastralData();
        }
    }


    public List<String> filterCourts(String query) throws PersistenceBeanException, IllegalAccessException {

        List<String> courts = DaoManager.load(Court.class, new Criterion[]{
                Restrictions.or(Restrictions.like("name", query, MatchMode.ANYWHERE))}, 
                new Order[]{}, 10).stream().map(Court::toString).collect(Collectors.toList());

        if(courts == null) {
            courts = new ArrayList<String>();
        }

        if(courts.isEmpty() && !ValidationHelper.isNullOrEmpty(query))
            courts.add(query);

        return courts;
    }

    public void courtSelectListener(SelectEvent e) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        String courtName = (String) e.getObject();
        Court court = DaoManager.get(Court.class,
                new Criterion[]{
                        Restrictions.eq("name", courtName)});
        if(!ValidationHelper.isNullOrEmpty(court)) {
            if(!ValidationHelper.isNullOrEmpty(court.getCity()))
                getSectionA().setSeat(court.getCity().getDescription());
            else
                getSectionA().setSeat(null);

            if(!ValidationHelper.isNullOrEmpty(court.getFiscalCode()))
                getSectionA().setFiscalCode(court.getFiscalCode());
            else
                getSectionA().setFiscalCode(null);
        }
    }


    private void savePropertyRelationship(SectionC tempSectionC) 
            throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        
        if (!ValidationHelper.isNullOrEmpty(tempSectionC.getSubject())) {
            for (Iterator<Subject> iterator = tempSectionC.getSubject().iterator(); iterator.hasNext(); ) {
                Subject subject = iterator.next();
                if (!Hibernate.isInitialized(subject.getRelationshipList())) {
                    //subject.setRelationshipList(DaoManager.get(Subject.class, subject.getId()).getRelationshipList());
                    subject.setRelationshipList(DaoManager.get(Subject.class, new CriteriaAlias[]{
                            new CriteriaAlias("relationshipList", "r", JoinType.LEFT_OUTER_JOIN),
                            new CriteriaAlias("r.property", "p", JoinType.LEFT_OUTER_JOIN),
                            new CriteriaAlias("r.formality", "f", JoinType.LEFT_OUTER_JOIN)
                    }, new Criterion[]{
                            Restrictions.eq("id", subject.getId())
                    }).getRelationshipList());
                }
                List<Relationship> formalityRelationships = new ArrayList<Relationship>();
                
                if (!ValidationHelper.isNullOrEmpty(getSectionBList())){
                    for(SectionB sectionB : getSectionBList()) {
                        if (!ValidationHelper.isNullOrEmpty(sectionB.getProperties())){
                            for (Property property : sectionB.getProperties()) {
                                if (!ValidationHelper.isNullOrEmpty(property.getRelations())){
                                    
                                    for(Relationship relationship : property.getRelations()) {
                                        Relationship matchedRelationShip = null;
                                        if(relationship.isNew()) {
                                            matchedRelationShip = new Relationship(relationship);
                                        }else {
                                            matchedRelationShip = 
                                                    DaoManager.get(Relationship.class, new CriteriaAlias[]{
                                                            new CriteriaAlias("property", "p", JoinType.LEFT_OUTER_JOIN),
                                                            new CriteriaAlias("formality", "f", JoinType.LEFT_OUTER_JOIN)
                                                    }, new Criterion[]{
                                                            Restrictions.eq("id", relationship.getId())
                                                    });
                                        }
                                        if(matchedRelationShip != null && 
                                              matchedRelationShip.getProperty() != null 
                                              && matchedRelationShip.getProperty().getId().equals(property.getId()) && 
                                              matchedRelationShip.getFormality() != null 
                                              && matchedRelationShip.getFormality().getId().equals(getEntity().getId())
                                              ) {
                                          
                                      }else if(matchedRelationShip != null && 
                                              matchedRelationShip.getProperty() != null 
                                              && matchedRelationShip.getProperty().getId().equals(property.getId()) && 
                                              matchedRelationShip.getFormality() == null ) {
                                          Relationship formalityRelationship = null;
                                          
                                          List<Relationship> realtionships = DaoManager.load(Relationship.class, new Criterion[]{
                                                  Restrictions.eq("property", property),
                                                  Restrictions.eq("subject", subject),
                                                  Restrictions.eq("formality", getEntity())
                                          });
                                          if(!ValidationHelper.isNullOrEmpty(realtionships)) {
                                              formalityRelationship = realtionships.get(0);
                                          }else {
                                              formalityRelationship = new Relationship(); 
                                          }
                                          formalityRelationship.setProperty(property);
                                          formalityRelationship.setSubject(subject);
                                          formalityRelationship.setFormality(getEntity());
                                          formalityRelationship.setPropertyType(relationship.getPropertyType());
                                          formalityRelationship.setProperty(property);
                                          formalityRelationship.setQuote(relationship.getQuote());
                                          formalityRelationship.setRegime(relationship.getRegime());
                                          formalityRelationship.setSectionCType(tempSectionC.getSectionCType());
                                          formalityRelationship.setUnitaNeg(sectionB.getBargainingUnit());
                                          formalityRelationship.setRelationshipTypeId(RelationshipType.FORMALITY.getId());
                                          formalityRelationships.add(formalityRelationship);
                                      }
                                    }
                                }
                                
                            }      
                        }
                    }
                }
//                
                if(!ValidationHelper.isNullOrEmpty(formalityRelationships)) {
                    if (ValidationHelper.isNullOrEmpty(subject.getRelationshipList())) {
                        subject.setRelationshipList(new ArrayList<>());
                    }
                    for(Relationship formalityRelationship : formalityRelationships) {
                       if (!subject.getRelationshipList().contains(formalityRelationship)) {
                            subject.getRelationshipList().add(formalityRelationship);
                       }
                    }  
                    subject.updateRelationshipsToView();
                }
            }
        }
    }
    
    private void deletePropertyRelationship(SectionC tempSectionC) 
            throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {

        if (!ValidationHelper.isNullOrEmpty(tempSectionC.getSubject())) {
            for (Iterator<Subject> iterator = tempSectionC.getSubject().iterator(); iterator.hasNext(); ) {
                Subject subject = iterator.next();
                if (!Hibernate.isInitialized(subject.getRelationshipList())) {
                    //subject.setRelationshipList(DaoManager.get(Subject.class, subject.getId()).getRelationshipList());
                    subject.setRelationshipList(DaoManager.get(Subject.class, new CriteriaAlias[]{
                            new CriteriaAlias("relationshipList", "r", JoinType.LEFT_OUTER_JOIN),
                            new CriteriaAlias("r.property", "p", JoinType.LEFT_OUTER_JOIN),
                            new CriteriaAlias("r.formality", "f", JoinType.LEFT_OUTER_JOIN)
                    }, new Criterion[]{
                            Restrictions.eq("id", subject.getId())
                    }).getRelationshipList());
                }
                
                if (!ValidationHelper.isNullOrEmpty(subject.getRelationshipList())) {
                    for(Relationship relationship : subject.getRelationshipList()) {
                        Relationship matchedRelationShip = 
                                DaoManager.get(Relationship.class, new CriteriaAlias[]{
                                        new CriteriaAlias("property", "p", JoinType.LEFT_OUTER_JOIN),
                                        new CriteriaAlias("formality", "f", JoinType.LEFT_OUTER_JOIN)
                                }, new Criterion[]{
                                        Restrictions.eq("id", relationship.getId())
                                });


                        if(matchedRelationShip == null)
                            matchedRelationShip = new Relationship(relationship);

                        if(ValidationHelper.isNullOrEmpty(matchedRelationShip.getProperty()) ||
                                ValidationHelper.isNullOrEmpty(matchedRelationShip.getFormality()) || 
                                !matchedRelationShip.getFormality().getId().equals(getEntity().getId()))
                            continue;

                        List<Property> properties =  getSectionBList().stream().map(SectionB :: getProperties)
                                .flatMap(List::stream).collect(Collectors.toList());

                        boolean isPresent  = false;
                        for(Property property : properties) {
                            if(property.getId().equals(matchedRelationShip.getProperty().getId())) {
                                isPresent = true;
                                break;
                            }
                        }
                        if(!isPresent) {
                            if(!relationship.isNew()) {
                                if (ValidationHelper.isNullOrEmpty(getRelationshipsToDeleteOnSave())) {
                                    setRelationshipsToDeleteOnSave(new ArrayList<>());
                                }
                                getRelationshipsToDeleteOnSave().add(relationship);
                            }
                            relationship.setDelete(true);
                        }
                    }
                    subject.getRelationshipList().removeIf(r -> r.getDelete());
                    subject.updateRelationshipsToView();
                }
            }
        }
    }

    public Long getSelectedReclamePropertyServiceId() {
        return selectedReclamePropertyServiceId;
    }

    public void setSelectedReclamePropertyServiceId(Long selectedReclamePropertyServiceId) {
        this.selectedReclamePropertyServiceId = selectedReclamePropertyServiceId;
    }

    public Long getSelectedProvincialOfficeId() {
        return selectedProvincialOfficeId;
    }

    public void setSelectedProvincialOfficeId(Long selectedProvincialOfficeId) {
        this.selectedProvincialOfficeId = selectedProvincialOfficeId;
    }

    public List<SelectItem> getConservatories() {
        return conservatories;
    }

    public void setConservatories(List<SelectItem> conservatories) {
        this.conservatories = conservatories;
    }

    public SectionA getSectionA() {
        return sectionA;
    }

    public void setSectionA(SectionA sectionA) {
        this.sectionA = sectionA;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }

    public List<SelectItem> getTypes() {
        return types;
    }

    public void setTypes(List<SelectItem> types) {
        this.types = types;
    }

    public Long getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(Long selectedType) {
        this.selectedType = selectedType;
    }

    public String getFormalityType() {
        return formalityType;
    }

    public void setFormalityType(String formalityType) {
        this.formalityType = formalityType;
    }

    public boolean isRenderForm() {
        return renderForm;
    }

    public void setRenderForm(boolean renderForm) {
        this.renderForm = renderForm;
    }

    public List<SelectItem> getDicTypeFormalities() {
        return dicTypeFormalities;
    }

    public void setDicTypeFormalities(List<SelectItem> dicTypeFormalities) {
        this.dicTypeFormalities = dicTypeFormalities;
    }

    public String getDicTypeFormality() {
        return dicTypeFormality;
    }

    public void setDicTypeFormality(String dicTypeFormality) {
        this.dicTypeFormality = dicTypeFormality;
    }

    public Long getBargainingUnitIncrement() {
        return bargainingUnitIncrement;
    }

    public void setBargainingUnitIncrement(Long bargainingUnitIncrement) {
        this.bargainingUnitIncrement = bargainingUnitIncrement;
    }

    public List<SectionB> getSectionBList() {
        return sectionBList;
    }

    public void setSectionBList(List<SectionB> sectionBList) {
        this.sectionBList = sectionBList;
    }

    public Long getAddressProvinceId() {
        return addressProvinceId;
    }

    public void setAddressProvinceId(Long addressProvinceId) {
        this.addressProvinceId = addressProvinceId;
    }

    public Long getAddressCityId() {
        return addressCityId;
    }

    public void setAddressCityId(Long addressCityId) {
        this.addressCityId = addressCityId;
    }

    public List<SelectItem> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<SelectItem> provinces) {
        this.provinces = provinces;
    }

    public List<SelectItem> getAddressCities() {
        return addressCities;
    }

    public void setAddressCities(List<SelectItem> addressCities) {
        this.addressCities = addressCities;
    }

    public List<SelectItem> getRealEstateTypes() {
        return realEstateTypes;
    }

    public void setRealEstateTypes(List<SelectItem> realEstateTypes) {
        this.realEstateTypes = realEstateTypes;
    }

    public boolean isClickedCreatePropertyButton() {
        return clickedCreatePropertyButton;
    }

    public void setClickedCreatePropertyButton(boolean clickedCreatePropertyButton) {
        this.clickedCreatePropertyButton = clickedCreatePropertyButton;
    }

    public boolean isSaveScheda() {
        return saveScheda;
    }

    public void setSaveScheda(boolean saveScheda) {
        this.saveScheda = saveScheda;
    }

    public List<CadastralData> getCadastralDataList() {
        return cadastralDataList;
    }

    public void setCadastralDataList(List<CadastralData> cadastralDataList) {
        this.cadastralDataList = cadastralDataList;
    }

    public Property getNewProperty() {
        return newProperty;
    }

    public void setNewProperty(Property newProperty) {
        this.newProperty = newProperty;
    }

    public void setSelectedTypeId(Long selectedTypeId) {
        this.selectedTypeId = selectedTypeId;
    }

    public Long getSelectedTypeId() {
        return selectedTypeId;
    }

    public boolean isBuilding() {
        return building;
    }

    public void setBuilding(boolean building) {
        this.building = building;
    }

    public CadastralData getNewCadastralData() {
        return newCadastralData;
    }

    public void setNewCadastralData(CadastralData newCadastralData) {
        this.newCadastralData = newCadastralData;
    }

    public List<SelectItem> getCategories() {
        return categories;
    }

    public void setCategories(List<SelectItem> categories) {
        this.categories = categories;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public SectionB getEditedSectionB() {
        return editedSectionB;
    }

    public void setEditedSectionB(SectionB editedSectionB) {
        this.editedSectionB = editedSectionB;
    }

    public List<SelectItem> getSubjectTypes() {
        return subjectTypes;
    }

    public void setSubjectTypes(List<SelectItem> subjectTypes) {
        this.subjectTypes = subjectTypes;
    }

    public Subject getNewSubject() {
        return newSubject;
    }

    public void setNewSubject(Subject newSubject) {
        this.newSubject = newSubject;
    }

    public Long getSelectedCountryId() {
        return selectedCountryId;
    }

    public void setSelectedCountryId(Long selectedCountryId) {
        this.selectedCountryId = selectedCountryId;
    }

    public List<SelectItem> getCountries() {
        return countries;
    }

    public void setCountries(List<SelectItem> countries) {
        this.countries = countries;
    }

    public List<SelectItem> getSexTypes() {
        return sexTypes;
    }

    public void setSexTypes(List<SelectItem> sexTypes) {
        this.sexTypes = sexTypes;
    }

    public Boolean getForeignCountry() {
        return foreignCountry == null ? Boolean.FALSE : foreignCountry;
    }

    public void setForeignCountry(Boolean foreignCountry) {
        this.foreignCountry = foreignCountry;
    }

    public Long getEntityEditId() {
        return entityEditId;
    }

    public void setEntityEditId(Long entityEditId) {
        this.entityEditId = entityEditId;
    }

    public Relationship getNewRelationship() {
        return newRelationship;
    }

    public void setNewRelationship(Relationship newRelationship) {
        this.newRelationship = newRelationship;
    }

    public String getQuote1() {
        return quote1;
    }

    public void setQuote1(String quote1) {
        this.quote1 = quote1;
    }

    public String getQuote2() {
        return quote2;
    }

    public void setQuote2(String quote2) {
        this.quote2 = quote2;
    }

    public List<SelectItem> getPropertyTypeList() {
        return propertyTypeList;
    }

    public void setPropertyTypeList(List<SelectItem> propertyTypeList) {
        this.propertyTypeList = propertyTypeList;
    }

    public PropertyTypeEnum getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyTypeEnum propertyType) {
        this.propertyType = propertyType;
    }

    public SectionC getaFavoreSectionC() {
        return aFavoreSectionC;
    }

    public void setaFavoreSectionC(SectionC aFavoreSectionC) {
        this.aFavoreSectionC = aFavoreSectionC;
    }

    public SectionC getControSectionC() {
        return controSectionC;
    }

    public void setControSectionC(SectionC controSectionC) {
        this.controSectionC = controSectionC;
    }

    public SectionD getSectionD() {
        return sectionD;
    }

    public void setSectionD(SectionD sectionD) {
        this.sectionD = sectionD;
    }

    public List<SelectItem> getCodeAndDescription() {
        return codeAndDescription;
    }

    public void setCodeAndDescription(List<SelectItem> codeAndDescription) {
        this.codeAndDescription = codeAndDescription;
    }

    public Long getAnnotationDescription() {
        return annotationDescription;
    }

    public void setAnnotationDescription(Long annotationDescription) {
        this.annotationDescription = annotationDescription;
    }

    public Long getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(Long derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    public Long getConventionDescription() {
        return conventionDescription;
    }

    public void setConventionDescription(Long conventionDescription) {
        this.conventionDescription = conventionDescription;
    }

    public List<String> getUnits() {
        return units;
    }

    public void setUnits(List<String> units) {
        this.units = units;
    }

    public Long getAddressProvinceIdC() {
        return addressProvinceIdC;
    }

    public void setAddressProvinceIdC(Long addressProvinceIdC) {
        this.addressProvinceIdC = addressProvinceIdC;
    }

    public Long getAddressCityIdC() {
        return addressCityIdC;
    }

    public void setAddressCityIdC(Long addressCityIdC) {
        this.addressCityIdC = addressCityIdC;
    }

    public List<SelectItem> getAddressCitiesSectionC() {
        return addressCitiesSectionC;
    }

    public void setAddressCitiesSectionC(List<SelectItem> addressCitiesSectionC) {
        this.addressCitiesSectionC = addressCitiesSectionC;
    }

    public Subject getEditSubject() {
        return editSubject;
    }

    public void setEditSubject(Subject editSubject) {
        this.editSubject = editSubject;
    }

    public String getTypeAtto() {
        return typeAtto;
    }

    public void setTypeAtto(String typeAtto) {
        this.typeAtto = typeAtto;
    }

    public AtomicInteger getTempId() {
        return tempId;
    }

    public void setTempId(AtomicInteger tempId) {
        this.tempId = tempId;
    }

    public CadastralData getNewCadastralDataDlg() {
        return newCadastralDataDlg;
    }

    public void setNewCadastralDataDlg(CadastralData newCadastralDataDlg) {
        this.newCadastralDataDlg = newCadastralDataDlg;
    }

    public List<CadastralData> getCadastralDataForDeleteList() {
        return cadastralDataForDeleteList;
    }

    public void setCadastralDataForDeleteList(List<CadastralData> cadastralDataForDeleteList) {
        this.cadastralDataForDeleteList = cadastralDataForDeleteList;
    }

    public Integer getDeleteTempId() {
        return deleteTempId;
    }

    public void setDeleteTempId(Integer deleteTempId) {
        this.deleteTempId = deleteTempId;
    }

    public boolean isValidated() {
        return isValidated;
    }

    public void setValidated(boolean validated) {
        isValidated = validated;
    }

    public List<SelectItem> getFilteredProvinces() {
        return filteredProvinces;
    }

    public void setFilteredProvinces(List<SelectItem> filteredProvinces) {
        this.filteredProvinces = filteredProvinces;
    }

    public RoleTypes getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleTypes roleType) {
        this.roleType = roleType;
    }

    public SectionCType getSectionCType() {
        return sectionCType;
    }

    public void setSectionCType(SectionCType sectionCType) {
        this.sectionCType = sectionCType;
    }

    public Long getTempIdCounter() {
        return tempIdCounter;
    }

    public void setTempIdCounter(Long tempIdCounter) {
        this.tempIdCounter = tempIdCounter;
    }

    public SectionC getDebitoriSectionC() {
        return debitoriSectionC;
    }

    public void setDebitoriSectionC(SectionC debitoriSectionC) {
        this.debitoriSectionC = debitoriSectionC;
    }

    public Subject getPreviousSubject() {
        return previousSubject;
    }

    public void setPreviousSubject(Subject previousSubject) {
        this.previousSubject = previousSubject;
    }

    public List<Subject> getSubjectsToDeleteOnSave() {
        return subjectsToDeleteOnSave;
    }

    public void setSubjectsToDeleteOnSave(List<Subject> subjectsToDeleteOnSave) {
        this.subjectsToDeleteOnSave = subjectsToDeleteOnSave;
    }

    public String getFormalityId() {
        return formalityId;
    }

    public void setFormalityId(String formalityId) {
        this.formalityId = formalityId;
    }

    public boolean isItDialog() {
        return isItDialog;
    }

    public void setItDialog(boolean itDialog) {
        isItDialog = itDialog;
    }

    public Boolean getViewFromRequest() {
        return viewFromRequest;
    }

    public void setViewFromRequest(Boolean viewFromRequest) {
        this.viewFromRequest = viewFromRequest;
    }

    public Long getTranscriptionActId() {
        return transcriptionActId;
    }

    public void setTranscriptionActId(Long transcriptionActId) {
        this.transcriptionActId = transcriptionActId;
    }

    public List<SelectItem> getRelationshipTypeList() {
        return relationshipTypeList;
    }

    public void setRelationshipTypeList(List<SelectItem> relationshipTypeList) {
        this.relationshipTypeList = relationshipTypeList;
    }

    public Long getConservatoryFilterId() {
        return conservatoryFilterId;
    }

    public void setConservatoryFilterId(Long conservatoryFilterId) {
        this.conservatoryFilterId = conservatoryFilterId;
    }

    public String getSelectedCourt() {
        return selectedCourt;
    }

    public void setSelectedCourt(String selectedCourt) {
        this.selectedCourt = selectedCourt;
    }

    public List<Property> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    public List<Property> getSelectedSearchedProperty() {
        return selectedSearchedProperty;
    }

    public void setSelectedSearchedProperty(List<Property> selectedSearchedProperty) {
        this.selectedSearchedProperty = selectedSearchedProperty;
    }

    public boolean isListProperties() {
        return listProperties;
    }

    public void setListProperties(boolean listProperties) {
        this.listProperties = listProperties;
    }

    public Long getEditedRequestId() {
        return editedRequestId;
    }

    public void setEditedRequestId(Long editedRequestId) {
        this.editedRequestId = editedRequestId;
    }

    public List<SelectItem> getRegimeList() {
        return regimeList;
    }

    public void setRegimeList(List<SelectItem> regimeList) {
        this.regimeList = regimeList;
    }

    public Long getSelectedRegimeId() {
        return selectedRegimeId;
    }

    public void setSelectedRegimeId(Long selectedRegimeId) {
        this.selectedRegimeId = selectedRegimeId;
    }

    public List<SelectItem> getSectionDs() {
        return sectionDs;
    }

    public void setSectionDs(List<SelectItem> sectionDs) {
        this.sectionDs = sectionDs;
    }

    public Long getSelectedSectionDId() {
        return selectedSectionDId;
    }

    public void setSelectedSectionDId(Long selectedSectionDId) {
        this.selectedSectionDId = selectedSectionDId;
    }

    public boolean getFromRequestEdit() {
        return fromRequestEdit;
    }

    public void setFromRequestEdit(boolean fromRequestEdit) {
        this.fromRequestEdit = fromRequestEdit;
    }

    public List<Relationship> getRelationshipsToDeleteOnSave() {
        return relationshipsToDeleteOnSave;
    }

    public void setRelationshipsToDeleteOnSave(List<Relationship> relationshipsToDeleteOnSave) {
        this.relationshipsToDeleteOnSave = relationshipsToDeleteOnSave;
    }
    
    public String getStatoStr() {
        return statoStr;
    }

    public void setStatoStr(String statoStr) {
        this.statoStr = statoStr;
    }

    public Long getCloneFormalityId() {
        return cloneFormalityId;
    }

    public void setCloneFormalityId(Long cloneFormalityId) {
        this.cloneFormalityId = cloneFormalityId;
    }
}
