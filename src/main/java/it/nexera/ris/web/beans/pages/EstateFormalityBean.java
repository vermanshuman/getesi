package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeAct;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.context.RequestContext;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean(name = "estateFormalityBean")
@ViewScoped
public class EstateFormalityBean extends EntityEditPageBean<EstateFormality> implements Serializable {

    private static final long serialVersionUID = 427964461816819242L;

    private List<SelectItem> estateFormalityTypeList;

    private EstateFormalityType selectedEstateFormalityTypeId;

    private List<EstateFormalitySuccess> formalitySuccessList;

    private EstateFormalitySuccess newFormalitySuccess;

    private EstateFormalitySuccess deleteFormalitySuccess;

    private String year;

    private Request requestEntity;

    private TypeAct selectedTypeAct;

    private TypeAct selectedFormalitySuccessTypeAct;

    private Long landChargesRegistryId;

    private List<SelectItem> typeActColeList;

    private List<SelectItem> landChargesRegistryList;

    private TypeActEnum selectedTypeActType;

    private List<SelectItem> typeActTypeList;

    private List<SelectItem> typeActDescriptionList;

    private boolean saveDuplicate;

    private boolean editExistFormality;

    private Date date;

    private String numRP;

    private Integer numRG;

    private Map<EstateFormality, List<Formality>> presumableFormalityListByEstateFormality;

    private List<Communication> formalityCommunicationList;

    private Communication newCommunication;

    private Communication deleteFormalityCommunication;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
    InstantiationException, IllegalAccessException {
        String requestId = getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
        if (!ValidationHelper.isNullOrEmpty(requestId)) {
            setRequestEntity(DaoManager.get(Request.class, Long.parseLong(requestId)));
        }
        String editId = getRequestParameter(RedirectHelper.ID_PARAMETER);
        if (!ValidationHelper.isNullOrEmpty(editId)) {
            setEntityId(Long.parseLong(editId));
            setFormalitySuccessList(DaoManager.load(EstateFormalitySuccess.class, new Criterion[]{
                    Restrictions.eq("estateFormality.id", getEntity().getId())
            }));
            setDate(getEntity().getDate());
            setNumRG(getEntity().getNumRG());
            setNumRP(getEntity().getNumRP());
            setEditExistFormality(true);
            setFormalityCommunicationList(DaoManager.load(Communication.class, new Criterion[] {
                    Restrictions.eq("estateFormality.id", getEntity().getId())
            }));

        } else {
            setEntityId(null);
            setFormalitySuccessList(new LinkedList<>());
            setFormalityCommunicationList(new LinkedList<>());
        }
        SessionHelper.removeObject("isNewProcedureForm");
        fillComboboxes();
        if (!ValidationHelper.isNullOrEmpty(getEntity().getTypeAct())) {
            setSelectedTypeAct(getEntity().getTypeAct());
            setSelectedTypeActType(getEntity().getTypeAct().getType());
        }
        setSelectedEstateFormalityTypeId(getEntity().getEstateFormalityType());
        if (!ValidationHelper.isNullOrEmpty(getEntity().getLandChargesRegistry())) {
            setLandChargesRegistryId(getEntity().getLandChargesRegistry().getId());
        }
        setNewFormalitySuccess(new EstateFormalitySuccess());
        setNewCommunication(new Communication());
    }

    private void fillComboboxes() throws PersistenceBeanException, IllegalAccessException {
        setEstateFormalityTypeList(ComboboxHelper.fillList(EstateFormalityType.class));
        setTypeActDescriptionList(ComboboxHelper.fillList(DaoManager.loadField(TypeAct.class, "description",
                String.class, new Criterion[]{}, Order.asc("description")).stream().distinct().toArray(String[]::new),
                true, false));
        setTypeActColeList(ComboboxHelper.fillList(TypeAct.class, true, false));
        setTypeActTypeList(ComboboxHelper.fillList(new TypeActEnum[]{TypeActEnum.TYPE_I, TypeActEnum.TYPE_T},
                true, false));
            setLandChargesRegistryList(ComboboxHelper.fillList(getRequestEntity()
                            .getAggregationLandChargesRegistry().getLandChargesRegistries(),
                    true, false));
        if (getLandChargesRegistryList().size() == 2) {
            setLandChargesRegistryId((Long) getLandChargesRegistryList().get(1).getValue());
        }
    }

    public List<TypeAct> completeTypeAct(String query) throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedTypeActType())) {
            return DaoManager.load(TypeAct.class, new Criterion[]{
                    (!ValidationHelper.isNullOrEmpty(query) ? Restrictions.or(
                            Restrictions.ilike("code", query, MatchMode.ANYWHERE),
                            Restrictions.ilike("description", query, MatchMode.ANYWHERE)
                            ) : Restrictions.isNotNull("code")),
                    Restrictions.eq("type", getSelectedTypeActType())
            });
        } else return new ArrayList<>();
    }

    public List<TypeAct> completeFormalitySuccessTypeAct(String query) throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.load(TypeAct.class, new Criterion[]{
                (!ValidationHelper.isNullOrEmpty(query) ? Restrictions.or(
                        Restrictions.ilike("code", query, MatchMode.ANYWHERE),
                        Restrictions.ilike("description", query, MatchMode.ANYWHERE)
                        ) : Restrictions.isNotNull("code")),
                Restrictions.eq("type", TypeActEnum.TYPE_A)
        });
    }

    public void handelTypeSelect() {

    }

    private boolean numRpValid() {
        if (!ValidationHelper.isNullOrEmpty(getNumRP()) && !ValidationHelper.isNullOrEmpty(getNumRG())) {
            try {
                Integer rp = Integer.parseInt(getNumRP());
                if (rp.compareTo(getNumRG()) > 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return true;
            }
        }
        return true;
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getDate())) {
            addRequiredFieldException("estateFormalityDate");
        }
        if (ValidationHelper.isNullOrEmpty(getNumRG())) {
            addRequiredFieldException("estateFormalityNumRG");
        }
        if (!numRpValid()) {
            addFieldException("estateFormalityNumRP", "rpValidateMessage");
            addFieldException("estateFormalityNumRG", "rpValidateMessage", false);

        }

        if(!ValidationHelper.isNullOrEmpty(getLandChargesRegistryId())){
            getEntity().setLandChargesRegistry(DaoManager.load(LandChargesRegistry.class,
                    new Criterion[]{Restrictions.eq("id", this.landChargesRegistryId)}).get(0));
        }

        if (ValidationHelper.isEstateFormalityExists(getEntity(), getRequestEntity(), DaoManager.getSession()) && isSaveDuplicate()) {
            addFieldException("estateFormalityConservatorship", "estateFormalityInUse");
            addFieldException("estateFormalityDate", "estateFormalityInUse");
            addFieldException("estateFormalityNumRG", "estateFormalityInUse", false);
            addFieldException("estateFormalityNumRP", "estateFormalityInUse", false);
            setSaveDuplicate(false);
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException,
    IOException, InstantiationException, IllegalAccessException {
        if (isSaveDuplicate()) {
            pageSaveDuplicate();
        } else {
            pageSaveOrigin();
        }
    }

    private void pageSaveOrigin() throws PersistenceBeanException, IllegalAccessException, InstantiationException {

        if (ValidationHelper.isEstateFormalityExists(getEntity(), getRequestEntity(), DaoManager.getSession())
                && !isEditExistFormality()) {
            return;
        }

        if (getEntityId() == null) {
            getEntity().setId(null);
        } else if (!Hibernate.isInitialized(getEntity().getRequestFormalities())) {
            getEntity().reloadRequestFormalities();
        }

        getEntity().setEstateFormalityType(getSelectedEstateFormalityTypeId());
        getEntity().setTypeAct(getSelectedTypeAct());
        getEntity().setLandChargesRegistry(DaoManager.get(LandChargesRegistry.class, new Criterion[]{
                Restrictions.eq("id", getLandChargesRegistryId())
        }));
        getEntity().setNumRG(getNumRG());
        getEntity().setNumRP(getNumRP());
        getEntity().setDate(getDate());
        if(ValidationHelper.isNullOrEmpty(getEntity().getRequestList()))
            getEntity().setRequestList(new LinkedList<>());
        if (getRequestEntity().getService().getIsUpdateAndNotNull()) {
            getEntity().setRequestListUpdate(new LinkedList<>());
            getEntity().getRequestListUpdate().add(getRequestEntity());
        }

        getEntity().getRequestList().add(getRequestEntity());
        getEntity().addRequestFormality(getRequestEntity(), DaoManager.getSession());

        getEntity().setAccountable(true);
        getEntity().setComment(String.format(ResourcesHelper.getString("estateFormalityCommentFormat"),
                getEntity().getTypeAct() != null ? getEntity().getTypeAct().getTextInVisura() : "",
                        getEntity().getDenominationPU(), DateTimeHelper.toStringDateWithDots(getEntity().getTitleDate()),
                        getEntity().getRepertoire()));

        removeDeletedEstateFormalitySuccessFromDB();

        removeDeletedEstateFormalityCommunicationFromDB();

        DaoManager.save(getEntity());

        for (EstateFormalitySuccess formalitySuccess : getFormalitySuccessList().stream()
                .filter(s -> ValidationHelper.isNullOrEmpty(getEntity().getEstateFormalitySuccessList())
                        || !getEntity().getEstateFormalitySuccessList().contains(s)).collect(Collectors.toList())) {
            formalitySuccess.setEstateFormality(getEntity());
            DaoManager.save(formalitySuccess);
        }

        for(Communication formolityCommunication : getFormalityCommunicationList()) {
            formolityCommunication.setEstateFormality(getEntity());
            DaoManager.save(formolityCommunication);
        }

        // Commenting this for scheda EstateSituationView - import formality.TBD later
        //checkPresumableFormalitiesForEstateFormalities();
    }

    @SuppressWarnings("unused")
    private void checkPresumableFormalitiesForEstateFormalities() throws PersistenceBeanException, IllegalAccessException {
        List<Formality> presumableFormalityListByEstateFormality =
                EstateSituationHelper.getPresumableFormalityListByEstateFormality(getRequestEntity(), getEntity());
        if (!ValidationHelper.isNullOrEmpty(presumableFormalityListByEstateFormality)) {
            setPresumableFormalityListByEstateFormality(new HashMap<>());
            getPresumableFormalityListByEstateFormality().put(getEntity(), presumableFormalityListByEstateFormality);
            RequestContext.getCurrentInstance().update("estateFormalityPresumableFormalityAssociationDialogId");
            executeJS("PF('estateFormalityPresumableFormalityAssociationDialog').show();");
            this.setRunAfterSave(false);
        }
    }

    public void associatePresumableFormalityWithEstateFormalityRequestSubjectBySectionC() throws PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getPresumableFormalityListByEstateFormality())) {
            EstateSituationHelper
            .associatePresumableFormalityWithRequestSubjectBySectionC(
                    getPresumableFormalityListByEstateFormality(), getRequestEntity());
        }
        this.goBack();
    }

    private void removeDeletedEstateFormalitySuccessFromDB() throws PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getEntity().getEstateFormalitySuccessList())) {
            List<EstateFormalitySuccess> listToRemove = new ArrayList<>();
            for (EstateFormalitySuccess formalitySuccess : getEntity().getEstateFormalitySuccessList()) {
                if (!getFormalitySuccessList().contains(formalitySuccess)) {
                    listToRemove.add(formalitySuccess);
                }
            }
            getEntity().getEstateFormalitySuccessList().removeAll(listToRemove);
            for (EstateFormalitySuccess formalitySuccess : listToRemove) {
                formalitySuccess.setEstateFormality(null);
                DaoManager.remove(formalitySuccess);
            }
        }
    }
    private void removeDeletedEstateFormalityCommunicationFromDB() throws PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getEntity().getCommunications())) {
            List<Communication> listToRemove = new ArrayList<>();
            for (Communication communication : getEntity().getCommunications()) {
                if (!getFormalityCommunicationList().contains(communication)) {
                    listToRemove.add(communication);
                }
            }
            getEntity().getCommunications().removeAll(listToRemove);
            for (Communication communication : listToRemove) {
                communication.setEstateFormality(null);
                DaoManager.remove(communication);
            }
        }
    }

    private void pageSaveDuplicate() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        EstateFormality estateFormality = new EstateFormality();
        estateFormality.setNumRP(getNumRP());
        estateFormality.setNumRG(getNumRG());
        estateFormality.setDate(getDate());

        estateFormality.setEstateFormalityType(getSelectedEstateFormalityTypeId());
        estateFormality.setTypeAct(getSelectedTypeAct());
        estateFormality.setLandChargesRegistry(DaoManager.get(LandChargesRegistry.class, new Criterion[]{
                Restrictions.eq("id", getLandChargesRegistryId())
        }));
        estateFormality.setRequestList(new LinkedList<>());
        if (getRequestEntity().getService().getIsUpdateAndNotNull()) {
            estateFormality.setRequestListUpdate(new LinkedList<>());
            estateFormality.getRequestListUpdate().add(getRequestEntity());
        }
        estateFormality.getRequestList().add(getRequestEntity());
        estateFormality.addRequestFormality(getRequestEntity(), DaoManager.getSession());

        estateFormality.setAccountable(true);
        estateFormality.setComment(String.format(ResourcesHelper.getString("estateFormalityCommentFormat"),
                getEntity().getTypeAct() != null ? getEntity().getTypeAct().getTextInVisura() : "",
                        getEntity().getDenominationPU(), DateTimeHelper.toStringDateWithDots(getEntity().getTitleDate()),
                        getEntity().getRepertoire()));

        DaoManager.save(estateFormality);
        for (EstateFormalitySuccess formalitySuccess : getFormalitySuccessList()) {
            formalitySuccess.setEstateFormality(estateFormality);
            DaoManager.save(formalitySuccess);
        }

        for(Communication formolityCommunication : getFormalityCommunicationList()) {
            formolityCommunication.setEstateFormality(estateFormality);
            DaoManager.save(formolityCommunication);
        }

        reInitializeEstateFormalitySuccessList();
    }

    private void reInitializeEstateFormalitySuccessList() {
        if (!ValidationHelper.isNullOrEmpty(getFormalitySuccessList())) {
            List<EstateFormalitySuccess> formalitySuccesses = new LinkedList<>();
            for (EstateFormalitySuccess estateFormalitySuccess : getFormalitySuccessList()) {
                formalitySuccesses.add(new EstateFormalitySuccess(estateFormalitySuccess));
            }
            setFormalitySuccessList(formalitySuccesses);
        }
    }

    @Override
    public void goBack() {

        if (isSaveDuplicate()) {
            setSaveDuplicate(false);
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO, "",
                    ResourcesHelper.getString("estateFormalitySaved"));
        } else {
            RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_VIEW,
                    getRequestEntity().getId(), null);
            setSaveDuplicate(false);
        }
    }

    public void addNewFormalitySuccess() {
        if (getSelectedFormalitySuccessTypeAct() != null) {
            getNewFormalitySuccess().setNoteType(NoteType.NOTE_TYPE_A);
            getNewFormalitySuccess().setActCode(Integer.parseInt(getSelectedFormalitySuccessTypeAct().getCode()));
            getNewFormalitySuccess().setDescription(getSelectedFormalitySuccessTypeAct().getDescription());
        }
        if (!ValidationHelper.isNullOrEmpty(getYear())) {
            Calendar c = new GregorianCalendar(Integer.parseInt(getYear()),
                    0, 1, 0, 0, 0);
            getNewFormalitySuccess().setYear(new Date(c.getTimeInMillis()));
        }
        if (!getNewFormalitySuccess().isEmpty()) {
            getFormalitySuccessList().add(getNewFormalitySuccess());
            setNewFormalitySuccess(new EstateFormalitySuccess());
            setYear(null);
            setSelectedFormalitySuccessTypeAct(null);
        }
    }

    public void addNewCommunicationFormality() {
        if(getNewCommunication() != null
                && (!ValidationHelper.isNullOrEmpty(getNewCommunication().getParticularRegister()) ||
                !ValidationHelper.isNullOrEmpty(getNewCommunication().getReceiveDate()) ||
                !ValidationHelper.isNullOrEmpty(getNewCommunication().getRemark()))){
            getFormalityCommunicationList().add(getNewCommunication());
            setNewCommunication(new Communication());
        }
    }

    public String getAssociateSubjectToRequest() {
        if(!ValidationHelper.isNullOrEmpty(getRequestEntity()) && 
                !ValidationHelper.isNullOrEmpty(getRequestEntity().getSubject())) {
            return String.format(ResourcesHelper.getString("associateSubjectToRequestConfirm"),
                    getRequestEntity().getSubject().getFullName());
        }else {
            return null;
        }
    }

    public void deleteFormalitySuccess() {
        getFormalitySuccessList().remove(getDeleteFormalitySuccess());
    }

    public void deleteCommunicationFormality() {
        getFormalityCommunicationList().remove(getDeleteFormalityCommunication());
    }

    public List<SelectItem> getEstateFormalityTypeList() {
        return estateFormalityTypeList;
    }

    public void setEstateFormalityTypeList(List<SelectItem> estateFormalityTypeList) {
        this.estateFormalityTypeList = estateFormalityTypeList;
    }

    public EstateFormalityType getSelectedEstateFormalityTypeId() {
        return selectedEstateFormalityTypeId;
    }

    public void setSelectedEstateFormalityTypeId(EstateFormalityType selectedEstateFormalityTypeId) {
        this.selectedEstateFormalityTypeId = selectedEstateFormalityTypeId;
    }

    public List<EstateFormalitySuccess> getFormalitySuccessList() {
        return formalitySuccessList;
    }

    public void setFormalitySuccessList(List<EstateFormalitySuccess> formalitySuccessList) {
        this.formalitySuccessList = formalitySuccessList;
    }

    public EstateFormalitySuccess getNewFormalitySuccess() {
        return newFormalitySuccess;
    }

    public void setNewFormalitySuccess(EstateFormalitySuccess newFormalitySuccess) {
        this.newFormalitySuccess = newFormalitySuccess;
    }

    public EstateFormalitySuccess getDeleteFormalitySuccess() {
        return deleteFormalitySuccess;
    }

    public void setDeleteFormalitySuccess(EstateFormalitySuccess deleteFormalitySuccess) {
        this.deleteFormalitySuccess = deleteFormalitySuccess;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Request getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(Request requestEntity) {
        this.requestEntity = requestEntity;
    }

    public TypeAct getSelectedTypeAct() {
        return selectedTypeAct;
    }

    public void setSelectedTypeAct(TypeAct selectedTypeAct) {
        this.selectedTypeAct = selectedTypeAct;
    }

    public List<SelectItem> getTypeActColeList() {
        return typeActColeList;
    }

    public void setTypeActColeList(List<SelectItem> typeActColeList) {
        this.typeActColeList = typeActColeList;
    }

    public TypeActEnum getSelectedTypeActType() {
        return selectedTypeActType;
    }

    public void setSelectedTypeActType(TypeActEnum selectedTypeActType) {
        this.selectedTypeActType = selectedTypeActType;
    }

    public List<SelectItem> getTypeActTypeList() {
        return typeActTypeList;
    }

    public void setTypeActTypeList(List<SelectItem> typeActTypeList) {
        this.typeActTypeList = typeActTypeList;
    }

    public List<SelectItem> getTypeActDescriptionList() {
        return typeActDescriptionList;
    }

    public void setTypeActDescriptionList(List<SelectItem> typeActDescriptionList) {
        this.typeActDescriptionList = typeActDescriptionList;
    }

    public List<SelectItem> getLandChargesRegistryList() {
        return landChargesRegistryList;
    }

    public void setLandChargesRegistryList(List<SelectItem> landChargesRegistryList) {
        this.landChargesRegistryList = landChargesRegistryList;
    }

    public Long getLandChargesRegistryId() {
        return landChargesRegistryId;
    }

    public void setLandChargesRegistryId(Long landChargesRegistryId) {
        this.landChargesRegistryId = landChargesRegistryId;
    }

    public TypeAct getSelectedFormalitySuccessTypeAct() {
        return selectedFormalitySuccessTypeAct;
    }

    public void setSelectedFormalitySuccessTypeAct(TypeAct selectedFormalitySuccessTypeAct) {
        this.selectedFormalitySuccessTypeAct = selectedFormalitySuccessTypeAct;
    }

    public boolean isSaveDuplicate() {
        return saveDuplicate;
    }

    public void setSaveDuplicate(boolean saveDuplicate) {
        this.saveDuplicate = saveDuplicate;
    }

    public boolean isEditExistFormality() {
        return editExistFormality;
    }

    public void setEditExistFormality(boolean editExistFormality) {
        this.editExistFormality = editExistFormality;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNumRP() {
        return numRP;
    }

    public void setNumRP(String numRP) {
        this.numRP = numRP;
    }

    public Integer getNumRG() {
        return numRG;
    }

    public void setNumRG(Integer numRG) {
        this.numRG = numRG;
    }

    public Map<EstateFormality, List<Formality>> getPresumableFormalityListByEstateFormality() {
        return presumableFormalityListByEstateFormality;
    }

    public void setPresumableFormalityListByEstateFormality(Map<EstateFormality, List<Formality>> presumableFormalityListByEstateFormality) {
        this.presumableFormalityListByEstateFormality = presumableFormalityListByEstateFormality;
    }

    public Communication getNewCommunication() {
        return newCommunication;
    }

    public void setNewCommunication(Communication newCommunication) {
        this.newCommunication = newCommunication;
    }

    public List<Communication> getFormalityCommunicationList() {
        return formalityCommunicationList;
    }

    public void setFormalityCommunicationList(List<Communication> formalityCommunicationList) {
        this.formalityCommunicationList = formalityCommunicationList;
    }

    public Communication getDeleteFormalityCommunication() {
        return deleteFormalityCommunication;
    }

    public void setDeleteFormalityCommunication(Communication deleteFormalityCommunication) {
        this.deleteFormalityCommunication = deleteFormalityCommunication;
    }
}
