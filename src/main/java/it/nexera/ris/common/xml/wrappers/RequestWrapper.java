package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.InputCardManageField;
import it.nexera.ris.persistence.beans.entities.domain.Notary;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.Residence;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.model.SelectItem;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class RequestWrapper {

    private final Long ID_FOREIGN_COUNTRY = 112L;

    private List<ConservatoriaSelectItem> selectedConservatoryItemId;

    private List<ConservatoriaSelectItem> selectedConserItemId;

    private List<ConservatoriaSelectItem> selectedTaloreItemId;

    private List<ConservatoriaSelectItem> conserItems;

    private List<ConservatoriaSelectItem> taloreItems;

    private List<ConservatoriaSelectItem> conservatoryItems;

    private Long selectedBuildingId;

    private List<SelectItem> buildings;

    private Long selectedPersonId;

    private List<SelectItem> persons;

    private Long selectedSexTypeId;

    private List<SelectItem> sexTypes;

    private Long selectProvinceId;

    private Long selectedProvincePropertyId;

    private Long selectedCityPropertyId;

    private Long selectedResidenceProvinceId;

    private Long selectedDomicileProvinceId;

    private List<SelectItem> provinces;

    private Long selectedCityId;

    private List<SelectItem> cities;

    private List<SelectItem> propertyCities;

    private Long selectedResidenceCityId;

    private List<SelectItem> residenceCities;

    private Long selectedDomicileCityId;

    private List<SelectItem> domicleCities;

    private Long selectedNationId;

    private Long selectedResidenceNationId;

    private Long selectedDomicileNationId;

    private List<SelectItem> nations;

    private Long selectedJuridicalNationId;

    private List<SelectItem> juridicalNations;

    private List<SelectItem> notaries;

    private Long selectedNotaryId;

    private boolean reset;

    private String reaNumber;
    private String natureLegal;
    private String istat;
    private String ultimaResidenza;
    private String sheet;
    private String particle;
    private String sub;
    private String addressProperty;
    private String section;

    public RequestWrapper(Request request, boolean multiple, boolean reset, boolean resetWrapper) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        this(request, reset);
        if (multiple) {
            InputCardManageField field = new InputCardManageField();
            field.setField(ManageTypeFields.SUBJECT_MASTERY);
            setLists(Collections.singletonList(field));
        }
        if (!reset && resetWrapper) {
            setSelectedProvincePropertyId(null);
            setSelectedCityPropertyId(null);
            setReaNumber(null);
            setNatureLegal(null);
            setIstat(null);
            setSelectedBuildingId(null);
            setUltimaResidenza(null);
            setSheet(null);
            setParticle(null);
            setSub(null);
            setAddressProperty(null);
            setSection(null);
            setSelectedConserItemId(new LinkedList<>());
            setSelectedTaloreItemId(new LinkedList<>());
            setSelectedNotaryId(null);
        }
    }

    public RequestWrapper(Request request, boolean multiple, boolean reset) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        this(request, reset);
        if (multiple) {
            InputCardManageField field = new InputCardManageField();
            field.setField(ManageTypeFields.SUBJECT_MASTERY);
            setLists(Collections.singletonList(field));
        }
    }

    public RequestWrapper(Request request)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        this(request, false);
    }

    public RequestWrapper(Request request, boolean reset) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        //  if (!request.isNew()) {
        setSelectedConservatoryItemId(new LinkedList<>());
        setSelectedTaloreItemId(new LinkedList<>());
        setSelectedConserItemId(new LinkedList<>());

        if (!reset && request.getAggregationLandChargesRegistry() != null) {

            AggregationLandChargesRegistry aggregationLandChargesRegistry = DaoManager.get(
                    AggregationLandChargesRegistry.class,
                    new CriteriaAlias[]{
                            new CriteriaAlias("landChargesRegistries", "landChargesRegistries", JoinType.INNER_JOIN)
                    },
                    new Criterion[]{
                            Restrictions.eq("id", request.getAggregationLandChargesRegistry().getId())
                    }
            );

            if (!ValidationHelper.isNullOrEmpty(aggregationLandChargesRegistry.getLandChargesRegistries())) {
                for (LandChargesRegistry registry : aggregationLandChargesRegistry.getLandChargesRegistries()) {
                    if (!ValidationHelper.isNullOrEmpty(registry.getType())) {
                        if (LandChargesRegistryType.CONSERVATORY.name().equalsIgnoreCase(registry.getType().name())) {
                            ConservatoriaSelectItem item = new ConservatoriaSelectItem(request.getAggregationLandChargesRegistry());
                            if (!getSelectedConserItemId().contains(item)) {
                                getSelectedConserItemId().add(item);
                            }
                        } else if (LandChargesRegistryType.TAVOLARE.name().equalsIgnoreCase(registry.getType().name())) {
                            ConservatoriaSelectItem item = new ConservatoriaSelectItem(request.getAggregationLandChargesRegistry());
                            if (!getSelectedTaloreItemId().contains(item)) {
                                getSelectedTaloreItemId().add(item);
                            }

                        }
                    }
                }
            }
        }
        if (!reset) {
            setSelectedBuildingId(request.getPropertyTypeId());
            setSelectedProvincePropertyId(request.getProvince() != null ? request.getProvince().getId() : null);
            setReaNumber(request.getReaNumber());
            setNatureLegal(request.getNatureLegal());
            setIstat(request.getIstat());
            setUltimaResidenza(request.getUltimaResidenza());
            setSheet(request.getSheet());
            setParticle(request.getParticle());
            setSub(request.getSub());
            setAddressProperty(request.getAddressProperty());
            setSection(request.getSection());
            if (request.getNotary() != null)
                setSelectedNotaryId(request.getNotary().getId());

            if (!ValidationHelper.isNullOrEmpty(getSelectedProvincePropertyId())) {
                onChangePropertyProvince();
                setSelectedCityPropertyId(request.getCity() != null ? request.getCity().getId() : null);
            }
        }
        setSubjectFields(request);
        setResidenceFields(request);
        setDomicileFields(request);
        //}
    }

    private void setSubjectFields(Request request) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (request.getSubject() != null) {
            setSelectedPersonId(request.getSubject().getTypeId());
            setSelectedSexTypeId(request.getSubject().getSex());

            if (!ValidationHelper.isNullOrEmpty(request.getSubject().getBirthProvince())
                    && request.getSubject().getBirthProvince().getId().equals(ID_FOREIGN_COUNTRY)) {
                setSelectProvinceId(Province.FOREIGN_COUNTRY_ID);
            } else if (!ValidationHelper.isNullOrEmpty(request.getSubject().getBirthProvince())) {
                setSelectProvinceId(request.getSubject().getBirthProvince().getId());
            } else if (!ValidationHelper.isNullOrEmpty(request.getSubject().getCountry())) {
                setSelectProvinceId(Province.FOREIGN_COUNTRY_ID);
            } else {
                setSelectProvinceId(null);
            }

            setSelectedCityId(request.getSubject().getBirthCity() != null
                    ? request.getSubject().getBirthCity().getId() : null);
            setSelectedNationId(request.getSubject().getCountry() != null
                    ? request.getSubject().getCountry().getId() : null);
            if (!request.getSubject().getTypeIsPhysicalPerson()) {
                setSelectedJuridicalNationId(request.getSubject().getCountry() != null
                        ? request.getSubject().getCountry().getId() : null);
            }
            onChangeProvince();
        }
    }

    private void setResidenceFields(Request request) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (request.getResidence() != null) {
            setSelectedResidenceProvinceId(request.getResidence().getProvince() != null
                    ? request.getResidence().getProvince().getId() : null);
            setSelectedResidenceCityId(request.getResidence().getCity() != null
                    ? request.getResidence().getCity().getId() : null);
            if (request.getResidence().getCountry() != null) {
                setSelectedResidenceProvinceId(-1L);
                setSelectedResidenceNationId(request.getResidence().getCountry().getId());
            }
            onChangeResidenceProvince();
        }
    }

    private void setDomicileFields(Request request) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (request.getDomicile() != null) {
            setSelectedDomicileProvinceId(request.getDomicile().getProvince() != null
                    ? request.getDomicile().getProvince().getId() : null);
            setSelectedDomicileCityId(request.getDomicile().getCity() != null
                    ? request.getDomicile().getCity().getId() : null);
            if (request.getDomicile().getCountry() != null) {
                setSelectedDomicileProvinceId(-1L);
                setSelectedDomicileNationId(request.getDomicile().getCountry().getId());
            }
            onChangeDomicleProvince();
        }
    }

    public void setRequestFields(Request request) throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        request.setPropertyTypeId(getSelectedBuildingId());
        if (getSelectedProvincePropertyId() != null) {
            request.setProvince(DaoManager.get(Province.class, getSelectedProvincePropertyId()));
        }
        if (getSelectedCityPropertyId() != null) {
            request.setCity(DaoManager.get(City.class, getSelectedCityPropertyId()));
        }

        if (getSelectedNotaryId() != null)
            request.setNotary(DaoManager.get(Notary.class, getSelectedNotaryId()));
        else
            request.setNotary(null);

        request.setReaNumber(getReaNumber());
        request.setNatureLegal(getNatureLegal());
        request.setUltimaResidenza(getUltimaResidenza());
        request.setIstat(getIstat());
        request.setSheet(getSheet());
        request.setParticle(getParticle());
        request.setAddressProperty(getAddressProperty());
        request.setSection(getSection());
        request.setSub(getSub());
        setRequestSubjectFields(request);
        setRequestResidenceFields(request);
        setRequestDomicileFields(request);
    }


    private void setRequestSubjectFields(Request request) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (getSelectedPersonId() == null) {
            request.setSubject(null);
        } else {
            request.getSubject().setTypeId(getSelectedPersonId());
            request.getSubject().setSex(getSelectedSexTypeId());
            if (!ValidationHelper.isNull(getSelectProvinceId())) {
                if (getSelectProvinceId().equals(Province.FOREIGN_COUNTRY_ID)) {
                    request.getSubject().setBirthProvince(null);
                    request.getSubject().setBirthCity(null);
                    setSelectedCityId(null);
                } else {
                    setSelectedNationId(null);
                    setSelectedJuridicalNationId(null);
                    request.getSubject().setCountry(null);
                    request.getSubject().setForeignCountry(false);
                    request.getSubject().setBirthProvince(DaoManager.get(Province.class, getSelectProvinceId()));
                }
            }
            if (!ValidationHelper.isNull(getSelectedCityId())) {
                request.getSubject().setBirthCity(DaoManager.get(City.class, getSelectedCityId()));
            }
            if (!ValidationHelper.isNull(getSelectedNationId())) {
                request.getSubject().setCountry(DaoManager.get(Country.class, getSelectedNationId()));
                request.getSubject().setForeignCountry(true);
            }
            if (!ValidationHelper.isNull(getSelectedJuridicalNationId())) {
                request.getSubject().setCountry(DaoManager.get(Country.class, getSelectedJuridicalNationId()));
                request.getSubject().setForeignCountry(true);
            }
            if (request.isPhysicalPerson()) {
                request.getSubject().setBusinessName(null);
            } else {
                request.getSubject().setSurname(null);
            }

            if (request.getSubject().getTypeIsPhysicalPerson()) {
                if (ValidationHelper.isNullOrEmpty(request.getSubject().getNumberVAT())) {
                    request.getSubject().setNumberVAT(request.getSubject().getFiscalCode());
                }
            } else if (ValidationHelper.isNullOrEmpty(request.getSubject().getFiscalCode())) {
                request.getSubject().setFiscalCode(request.getSubject().getNumberVAT());
            }
        }
    }

    private void setRequestResidenceFields(Request request) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (getSelectedResidenceProvinceId() == null) {
            request.setResidence(null);
        } else {
            if (ValidationHelper.isNullOrEmpty(request.getResidence())) {
                request.setResidence(new Residence());
            }
            if (!ValidationHelper.isNull(getSelectedResidenceProvinceId())) {
                request.getResidence().setProvince(DaoManager.get(Province.class, getSelectedResidenceProvinceId()));
            }
            if (!ValidationHelper.isNull(getSelectedResidenceCityId())) {
                request.getResidence().setCity(DaoManager.get(City.class, getSelectedResidenceCityId()));
            }
            if (!ValidationHelper.isNull(getSelectedResidenceNationId())) {
                request.getResidence().setCountry(DaoManager.get(Country.class, getSelectedResidenceNationId()));
            }
        }
    }

    private void setRequestDomicileFields(Request request) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (getSelectedDomicileProvinceId() == null) {
            request.setDomicile(null);
        } else {
            if (ValidationHelper.isNullOrEmpty(request.getDomicile())) {
                request.setDomicile(new Residence());
            }
            if (!ValidationHelper.isNull(getSelectedDomicileProvinceId())) {
                request.getDomicile().setProvince(DaoManager.get(Province.class, getSelectedDomicileProvinceId()));
            }
            if (!ValidationHelper.isNull(getSelectedDomicileCityId())) {
                request.getDomicile().setCity(DaoManager.get(City.class, getSelectedDomicileCityId()));
            }
            if (!ValidationHelper.isNull(getSelectedDomicileNationId())) {
                request.getDomicile().setCountry(DaoManager.get(Country.class, getSelectedDomicileNationId()));
            }
        }
    }

    public void saveFields(Request request, Boolean saveSubject) throws PersistenceBeanException, InstantiationException, IllegalAccessException {
       /* request.setPropertyTypeId(getSelectedBuildingId());
        if (getSelectedProvincePropertyId() != null) {
            request.setProvince(DaoManager.get(Province.class, getSelectedProvincePropertyId()));
        }
        if (getSelectedCityPropertyId() != null) {
            request.setCity(DaoManager.get(City.class, getSelectedCityPropertyId()));
        }
        
        if (getSelectedNotaryId() != null)
        	request.setNotary(DaoManager.get(Notary.class, getSelectedNotaryId()));
        else
        	request.setNotary(null);

        request.setReaNumber(getReaNumber());
        request.setNatureLegal(getNatureLegal());
        request.setIstat(getIstat());
        */
        saveSubjectFields(request, saveSubject);
        saveResidenceFields(request);
        try {
            saveDomicileFields(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveSubjectFields(Request request, boolean saveSubject) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
       /* if (getSelectedPersonId() == null) {
            request.setSubject(null);
        } else {
            request.getSubject().setTypeId(getSelectedPersonId());
            request.getSubject().setSex(getSelectedSexTypeId());
            if (!ValidationHelper.isNull(getSelectProvinceId())) {
                if (getSelectProvinceId().equals(Province.FOREIGN_COUNTRY_ID)) {
                    request.getSubject().setBirthProvince(null);
                    request.getSubject().setBirthCity(null);
                    setSelectedCityId(null);
                } else{
                    setSelectedNationId(null);
                    setSelectedJuridicalNationId(null);
                    request.getSubject().setCountry(null);
                    request.getSubject().setForeignCountry(false);
                    request.getSubject().setBirthProvince(DaoManager.get(Province.class, getSelectProvinceId()));
                }
            }
            if (!ValidationHelper.isNull(getSelectedCityId())) {
                request.getSubject().setBirthCity(DaoManager.get(City.class, getSelectedCityId()));
            }
            if (!ValidationHelper.isNull(getSelectedNationId())) {
                request.getSubject().setCountry(DaoManager.get(Country.class, getSelectedNationId()));
                request.getSubject().setForeignCountry(true);
            }
            if (!ValidationHelper.isNull(getSelectedJuridicalNationId())) {
                request.getSubject().setCountry(DaoManager.get(Country.class, getSelectedJuridicalNationId()));
                request.getSubject().setForeignCountry(true);
            }
            if (request.isPhysicalPerson()) {
                request.getSubject().setBusinessName(null);
            } else {
                request.getSubject().setSurname(null);
            }

            if (request.getSubject().getTypeIsPhysicalPerson()) {
                if (ValidationHelper.isNullOrEmpty(request.getSubject().getNumberVAT())) {
                    request.getSubject().setNumberVAT(request.getSubject().getFiscalCode());
                }
            } else if (ValidationHelper.isNullOrEmpty(request.getSubject().getFiscalCode())) {
                request.getSubject().setFiscalCode(request.getSubject().getNumberVAT());
            }

            if(saveSubject)
                DaoManager.save(request.getSubject());
        }

        */
        if (getSelectedPersonId() == null) {
            request.setSubject(null);
        } else {
            if (saveSubject)
                DaoManager.save(request.getSubject());
        }

    }

    private void saveResidenceFields(Request request) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
       /* if (getSelectedResidenceProvinceId() == null) {
            request.setResidence(null);
        } else {
            if (ValidationHelper.isNullOrEmpty(request.getResidence())) {
                request.setResidence(new Residence());
            }
            if (!ValidationHelper.isNull(getSelectedResidenceProvinceId())) {
                request.getResidence().setProvince(DaoManager.get(Province.class, getSelectedResidenceProvinceId()));
            }
            if (!ValidationHelper.isNull(getSelectedResidenceCityId())) {
                request.getResidence().setCity(DaoManager.get(City.class, getSelectedResidenceCityId()));
            }
            if (!ValidationHelper.isNull(getSelectedResidenceNationId())) {
                request.getResidence().setCountry(DaoManager.get(Country.class, getSelectedResidenceNationId()));
            }
            DaoManager.save(request.getResidence());
        }

        */

        if (getSelectedResidenceProvinceId() == null) {
            request.setResidence(null);
        } else {
            DaoManager.save(request.getResidence());
        }
    }

    private void saveDomicileFields(Request request) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
     /*   if (getSelectedDomicileProvinceId() == null) {
            request.setDomicile(null);
        } else {
            if (ValidationHelper.isNullOrEmpty(request.getDomicile())) {
                request.setDomicile(new Residence());
            }
            if (!ValidationHelper.isNull(getSelectedDomicileProvinceId())) {
                request.getDomicile().setProvince(DaoManager.get(Province.class, getSelectedDomicileProvinceId()));
            }
            if (!ValidationHelper.isNull(getSelectedDomicileCityId())) {
                request.getDomicile().setCity(DaoManager.get(City.class, getSelectedDomicileCityId()));
            }
            if (!ValidationHelper.isNull(getSelectedDomicileNationId())) {
                request.getDomicile().setCountry(DaoManager.get(Country.class, getSelectedDomicileNationId()));
            }
            DaoManager.save(request.getDomicile());
        }
      */
        if (getSelectedDomicileProvinceId() == null) {
            request.setDomicile(null);
        } else {
            DaoManager.save(request.getDomicile());
        }
    }

    public void onChangeProvince() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        setCities(ComboboxHelper.fillList(City.class, Order.asc("description"), new Criterion[]{
                Restrictions.eq("province.id", getSelectProvinceId()),
                Restrictions.eq("external", Boolean.TRUE)
        }));
    }

    public void onChangeResidenceProvince() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setResidenceCities(ComboboxHelper.fillList(City.class, Order.asc("id"), new Criterion[]{
                Restrictions.eq("province.id", getSelectedResidenceProvinceId()),
                Restrictions.eq("external", Boolean.TRUE)
        }));
    }

    public void onChangePropertyProvince() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setPropertyCities(ComboboxHelper.fillList(City.class, Order.asc("id"), new Criterion[]{
                Restrictions.eq("province.id", getSelectedProvincePropertyId()),
                Restrictions.eq("external", Boolean.TRUE)
        }));
    }

    public void onChangeDomicleProvince() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setDomicleCities(ComboboxHelper.fillList(City.class, Order.asc("id"), new Criterion[]{
                Restrictions.eq("province.id", getSelectedDomicileProvinceId()),
                Restrictions.eq("external", Boolean.TRUE)
        }));
    }

    public void setLists(List<InputCardManageField> list) throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(list)) {
            List<ManageTypeFields> fields = list.stream().map(InputCardManageField::getField)
                    .collect(Collectors.toList());
            if (fields.contains(ManageTypeFields.CONSERVATORY)) {
                List<AggregationLandChargesRegistry> conservatories = DaoManager.load(AggregationLandChargesRegistry.class, new CriteriaAlias[]{
                                new CriteriaAlias("landChargesRegistries", "lCR", JoinType.INNER_JOIN)
                        },
                        new Criterion[]{Restrictions.eq("lCR.type", LandChargesRegistryType.CONSERVATORY)},
                        Order.asc("name"));

                setConserItems(conservatories.stream().map(ConservatoriaSelectItem::new).collect(Collectors.toList()));
            }
            if (fields.contains(ManageTypeFields.TALOVARE)) {
                List<AggregationLandChargesRegistry> conservatories = DaoManager.load(AggregationLandChargesRegistry.class, new CriteriaAlias[]{
                                new CriteriaAlias("landChargesRegistries", "lCR", JoinType.INNER_JOIN)
                        },
                        new Criterion[]{Restrictions.eq("lCR.type", LandChargesRegistryType.TAVOLARE)},
                        Order.asc("name"));

                setTaloreItems(conservatories.stream().map(ConservatoriaSelectItem::new).collect(Collectors.toList()));
            }

            if (fields.contains(ManageTypeFields.PROPERTY_DATA)) {
                setBuildings(ComboboxHelper.fillList(RealEstateType.class, false));
            }
            if (fields.contains(ManageTypeFields.SUBJECT_MASTERY)
                    || fields.contains(ManageTypeFields.SUBJECT_LIST)) {
                setPersons(ComboboxHelper.fillList(SubjectType.class, false));
                setSexTypes(ComboboxHelper.fillList(SexTypes.class, false));
                setJuridicalNations(ComboboxHelper.fillList(Country.class, true));
            }
            if (fields.contains(ManageTypeFields.SUBJECT_MASTERY)
                    || fields.contains(ManageTypeFields.SUBJECT_LIST)
                    || fields.contains(ManageTypeFields.PROVINCE)
                    || fields.contains(ManageTypeFields.RESIDENCE)
                    || fields.contains(ManageTypeFields.DOMICILE)) {
                setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
                getProvinces().add(new SelectItem(Province.FOREIGN_COUNTRY_ID, Province.FOREIGN_COUNTRY));
            }
            if (fields.contains(ManageTypeFields.RESIDENCE)
                    || fields.contains(ManageTypeFields.SUBJECT_MASTERY)
                    || fields.contains(ManageTypeFields.SUBJECT_LIST)
                    || fields.contains(ManageTypeFields.DOMICILE)) {
                setNations(ComboboxHelper.fillList(Country.class, true));
            }
            if (fields.contains(ManageTypeFields.NOTARY)) {
                setNotaries(ComboboxHelper.fillList(Notary.class, true));
            }
        }
    }

    public boolean renderSession() {
        return getSelectedBuildingId() != null && getSelectedBuildingId().equals(1L);
    }

    public List<ConservatoriaSelectItem> getSelectedConservatoryItemId() {

        return selectedConservatoryItemId;
    }

    public void setSelectedConservatoryItemId(List<ConservatoriaSelectItem> selectedConservatoryItemId) {
        this.selectedConservatoryItemId = selectedConservatoryItemId;
    }

    public List<ConservatoriaSelectItem> getConservatoryItems() {
        return conservatoryItems;
    }

    public void setConservatoryItems(List<ConservatoriaSelectItem> conservatoryItems) {
        this.conservatoryItems = conservatoryItems;
    }

    public Long getSelectedBuildingId() {
        return selectedBuildingId;
    }

    public void setSelectedBuildingId(Long selectedBuildingId) {
        this.selectedBuildingId = selectedBuildingId;
    }

    public List<SelectItem> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<SelectItem> buildings) {
        this.buildings = buildings;
    }

    public Long getSelectedPersonId() {
        return selectedPersonId;
    }

    public void setSelectedPersonId(Long selectedPersonId) {
        this.selectedPersonId = selectedPersonId;
    }

    public List<SelectItem> getPersons() {
        return persons;
    }

    public void setPersons(List<SelectItem> persons) {
        this.persons = persons;
    }

    public Long getSelectedSexTypeId() {
        return selectedSexTypeId;
    }

    public void setSelectedSexTypeId(Long selectedSexTypeId) {
        this.selectedSexTypeId = selectedSexTypeId;
    }

    public List<SelectItem> getSexTypes() {
        return sexTypes;
    }

    public void setSexTypes(List<SelectItem> sexTypes) {
        this.sexTypes = sexTypes;
    }

    public Long getSelectProvinceId() {
        return selectProvinceId;
    }

    public void setSelectProvinceId(Long selectProvinceId) {
        this.selectProvinceId = selectProvinceId;
    }

    public Long getSelectedProvincePropertyId() {
        return selectedProvincePropertyId;
    }

    public void setSelectedProvincePropertyId(Long selectedProvincePropertyId) {
        this.selectedProvincePropertyId = selectedProvincePropertyId;
    }

    public Long getSelectedResidenceProvinceId() {
        return selectedResidenceProvinceId;
    }

    public void setSelectedResidenceProvinceId(Long selectedResidenceProvinceId) {
        this.selectedResidenceProvinceId = selectedResidenceProvinceId;
    }

    public Long getSelectedDomicileProvinceId() {
        return selectedDomicileProvinceId;
    }

    public void setSelectedDomicileProvinceId(Long selectedDomicileProvinceId) {
        this.selectedDomicileProvinceId = selectedDomicileProvinceId;
    }

    public List<SelectItem> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<SelectItem> provinces) {
        this.provinces = provinces;
    }

    public Long getSelectedCityId() {
        return selectedCityId;
    }

    public void setSelectedCityId(Long selectedCityId) {
        this.selectedCityId = selectedCityId;
    }

    public List<SelectItem> getCities() {
        return cities;
    }

    public void setCities(List<SelectItem> cities) {
        this.cities = cities;
    }

    public Long getSelectedResidenceCityId() {
        return selectedResidenceCityId;
    }

    public void setSelectedResidenceCityId(Long selectedResidenceCityId) {
        this.selectedResidenceCityId = selectedResidenceCityId;
    }

    public List<SelectItem> getResidenceCities() {
        return residenceCities;
    }

    public void setResidenceCities(List<SelectItem> residenceCities) {
        this.residenceCities = residenceCities;
    }

    public Long getSelectedDomicileCityId() {
        return selectedDomicileCityId;
    }

    public void setSelectedDomicileCityId(Long selectedDomicileCityId) {
        this.selectedDomicileCityId = selectedDomicileCityId;
    }

    public List<SelectItem> getDomicleCities() {
        return domicleCities;
    }

    public void setDomicleCities(List<SelectItem> domicleCities) {
        this.domicleCities = domicleCities;
    }

    public Long getSelectedNationId() {
        return selectedNationId;
    }

    public void setSelectedNationId(Long selectedNationId) {
        this.selectedNationId = selectedNationId;
    }

    public Long getSelectedResidenceNationId() {
        return selectedResidenceNationId;
    }

    public void setSelectedResidenceNationId(Long selectedResidenceNationId) {
        this.selectedResidenceNationId = selectedResidenceNationId;
    }

    public Long getSelectedDomicileNationId() {
        return selectedDomicileNationId;
    }

    public void setSelectedDomicileNationId(Long selectedDomicileNationId) {
        this.selectedDomicileNationId = selectedDomicileNationId;
    }

    public List<SelectItem> getNations() {
        return nations;
    }

    public void setNations(List<SelectItem> nations) {
        this.nations = nations;
    }

    public Long getSelectedJuridicalNationId() {
        return selectedJuridicalNationId;
    }

    public void setSelectedJuridicalNationId(Long selectedJuridicalNationId) {
        this.selectedJuridicalNationId = selectedJuridicalNationId;
    }

    public List<SelectItem> getJuridicalNations() {
        return juridicalNations;
    }

    public void setJuridicalNations(List<SelectItem> juridicalNations) {
        this.juridicalNations = juridicalNations;
    }

    public Long getSelectedCityPropertyId() {
        return selectedCityPropertyId;
    }

    public void setSelectedCityPropertyId(Long selectedCityPropertyId) {
        this.selectedCityPropertyId = selectedCityPropertyId;
    }

    public List<SelectItem> getPropertyCities() {
        return propertyCities;
    }

    public void setPropertyCities(List<SelectItem> propertyCities) {
        this.propertyCities = propertyCities;
    }

    public List<ConservatoriaSelectItem> getSelectedTaloreItemId() {
        return selectedTaloreItemId;
    }

    public void setSelectedTaloreItemId(List<ConservatoriaSelectItem> selectedTaloreItemId) {
        this.selectedTaloreItemId = selectedTaloreItemId;
    }

    public List<ConservatoriaSelectItem> getSelectedConserItemId() {
        return selectedConserItemId;
    }

    public void setSelectedConserItemId(List<ConservatoriaSelectItem> selectedConserItemId) {
        this.selectedConserItemId = selectedConserItemId;
    }

    public List<ConservatoriaSelectItem> getConserItems() {
        return conserItems;
    }

    public void setConserItems(List<ConservatoriaSelectItem> conserItems) {
        this.conserItems = conserItems;
    }

    public List<ConservatoriaSelectItem> getTaloreItems() {
        return taloreItems;
    }

    public void setTaloreItems(List<ConservatoriaSelectItem> taloreItems) {
        this.taloreItems = taloreItems;
    }

    public List<SelectItem> getNotaries() {
        return notaries;
    }

    public void setNotaries(List<SelectItem> notaries) {
        this.notaries = notaries;
    }

    public Long getSelectedNotaryId() {
        return selectedNotaryId;
    }

    public void setSelectedNotaryId(Long selectedNotaryId) {
        this.selectedNotaryId = selectedNotaryId;
    }

    public boolean isReset() {
        return reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }
}
