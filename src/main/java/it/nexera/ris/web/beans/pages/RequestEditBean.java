package it.nexera.ris.web.beans.pages;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import it.nexera.ris.common.enums.*;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.GeneralFunctionsHelper;
import it.nexera.ris.common.helpers.ListHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MailHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.RequestHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.SelectItemHelper;
import it.nexera.ris.common.helpers.SelectItemWrapperConverter;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.SubjectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.xml.wrappers.ConservatoriaSelectItem;
import it.nexera.ris.common.xml.wrappers.RequestViewWrapper;
import it.nexera.ris.common.xml.wrappers.RequestWrapper;
import it.nexera.ris.common.xml.wrappers.SelectItemWrapper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Agency;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.ClientServiceInfo;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.InputCard;
import it.nexera.ris.persistence.beans.entities.domain.InputCardManageField;
import it.nexera.ris.persistence.beans.entities.domain.Notary;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.RequestConservatory;
import it.nexera.ris.persistence.beans.entities.domain.RequestFormality;
import it.nexera.ris.persistence.beans.entities.domain.RequestHistory;
import it.nexera.ris.persistence.beans.entities.domain.RequestSubject;
import it.nexera.ris.persistence.beans.entities.domain.Residence;
import it.nexera.ris.persistence.beans.entities.domain.SectionA;
import it.nexera.ris.persistence.beans.entities.domain.SectionC;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.WLGInbox;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.base.AccessBean;
import it.nexera.ris.web.beans.wrappers.logic.SubjectWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UploadDocumentWrapper;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@ManagedBean(name = "requestEditBean")
@ViewScoped
public class RequestEditBean extends EntityEditPageBean<Request> implements Serializable {

    private static final long serialVersionUID = -669341430296239089L;
    private static final int correctNumberVATLength = 11;

    private String multipleTabPath; //crunch to render subject tab in multiple view

    private MenuModel topMenuModel;

    private int activeMenuTabNum;

    private Long selectedClientId;

    private List<SelectItem> clients;

    private Long selectedBillingClientId;

    private List<SelectItem> billingClients;

    private Long newSelectedBillingClientId;

    private Long selectedRequestTypeId;

    private List<SelectItem> requestTypes;

    private Long selectedServiceId;

    private List<String> selectedServiceIds;

    private List<SelectItem> services;

    private Long selectedRequestEnumTypeFirst;

    private List<SelectItem> requestEnumTypesFirst;

    private Long selectedRequestEnumTypeSecond;

    private List<SelectItem> requestEnumTypesSecond;

    private Long selectedUserId;

    private List<SelectItem> users;

    private String reason;

    private String reasonUnsuspend;

    private String reactivateReason;

    private Boolean performEdit;

    private List<InputCard> inputCardList;

    private RequestWrapper wrapper;

    private List<Document> documents;

    private List<Document> attachments;

    private Document xmlDocument;

    private List<Subject> xmlSubjects;

    private File xmlFile;

    private Long deleteDocumentId;

    private String deleteDocumentFileName;

    private String documentTitle;

    private Date documentDate;

    private UploadedFile document;

    private UploadedFile attachment;

    private Request dbRequest;

    private List<RequestViewWrapper> shownFields;

    private WLGInbox mail;

    private Long selectedOldRequestId;

    private Long startStateId;

    private Long startUserId;

    private Long selectedTemplateId;

    private List<SelectItem> templates;

    private List<SelectItem> agencyList;

    private boolean hasAgencyList;

    private Long selectedAgencyId;

    private List<SelectItem> agencyOfficeList;

    private boolean hasAgencyOfficeList;

    private Long selectedAgencyOfficeId;

    private List<SelectItem> notaryList;

    private Long selectedNotaryId;

    private Subject subject;

    private List<Subject> subjectsToRestore;

    private Subject selectedSubjectToRestore;

    private Residence residence;

    private Residence domicile;

    private boolean multipleCreate;

    private String areaOfClient;

    private String officeOfClient;

    private List<SelectItemWrapper<Client>> fiduciaryClients;

    private List<SelectItemWrapper<Client>> fiduciaryClientsSelected;

    private String redirectFromMail;

    private Request sameRequest;

    private String notificationUser;

    private Boolean shouldBeRedirected;

    private Boolean allEvaded;

    private Boolean isServiceUpdate;

    private SelectItemWrapperConverter<Client> clientSelectItemWrapperConverter;

    private Formality distraintFormality;

    private List<SubjectWrapper> subjectWrapperList;

    private SubjectWrapper subjectWrapper;

    private List<Formality> searchedFormalityList;

    private Long searchFormalityAggregationId;

    private String searchFormalityRG;

    private Date searchFormalityDate;

    private String searchFormalityRP;

    private Formality selectedSearchedFormality;

    private List<SelectItem> landAggregations;

    private Boolean redirected;

    private String multipleReqMessage;

    @Override
    protected void preLoad() throws PersistenceBeanException {
        if (Boolean.parseBoolean(getRequestParameter(RedirectHelper.MULTIPLE))) {
            setMultipleCreate(true);
        }
        if (getRequestParameter(RedirectHelper.MAIL) != null) {
            setRedirectFromMail(getRequestParameter(RedirectHelper.MAIL));
            try {
                WLGInbox mail = DaoManager.get(WLGInbox.class,new CriteriaAlias[]{
                        new CriteriaAlias("client", "client", JoinType.INNER_JOIN)
                },
                        new Criterion[]{
                                Restrictions.eq("id", Long.parseLong(getRequestParameter(RedirectHelper.MAIL)))
                });
                if(mail != null ) {
                    setMail(mail);
                }else
                    setMail(DaoManager.get(WLGInbox.class,
                            Long.parseLong(getRequestParameter(RedirectHelper.MAIL))));

            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        } else if (getRequestParameter(RedirectHelper.ARCHIVE_MAIL) != null) {
            try {
                setMail(DaoManager.get(WLGInbox.class,
                        Long.parseLong(getRequestParameter(RedirectHelper.ARCHIVE_MAIL))));
                getMail().setState(MailManagerStatuses.ARCHIVED.getId());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        if (getMail() != null) {
            try {
                List<String> onlyEmails = MailHelper.getOnlyEmails(getMail().getEmailFrom());
                if (!ValidationHelper.isNullOrEmpty(onlyEmails)) {
                    List<Client> clients = DaoManager.load(Client.class, new CriteriaAlias[]{
                            new CriteriaAlias("emails", "email", JoinType.INNER_JOIN)
                    }, new Criterion[]{
                            Restrictions.in("email.email", onlyEmails)
                    });
                    if (!ValidationHelper.isNullOrEmpty(clients) && getSelectedClientId() == null) {
                        setSelectedClientId(clients.get(0).getId());
                        onClientChange(false);
                    }
                }
            } catch (IllegalAccessException e) {
                LogHelper.log(log, e);
            }
        }

    }

    private void presetParamsFromMail(WLGInbox mail) {
        if (!ValidationHelper.isNullOrEmpty(mail.getClientFiduciary())) {
            getEntity().setClient(mail.getClientFiduciary());
            if (!ValidationHelper.isNullOrEmpty(mail.getManagers())) {
                getEntity().setRequestMangerList(mail.getManagers());
                prepareFiduciaryClientsList(mail.getManagers());
            }
        } else {
            getEntity().setClient(mail.getManagers().get(0));
        }

        if (!ValidationHelper.isNullOrEmpty(mail.getClientInvoice())) {
            setBillingClients(ComboboxHelper.fillList(Collections.singletonList(mail.getClientInvoice()),
                    true, false));
            setSelectedBillingClientId(mail.getClientInvoice().getId());
        }

    }

    private void prepareFiduciaryClientsList(List<Client> clients) {
        setFiduciaryClientsSelected(ComboboxHelper.fillWrapperList(clients));
        setFiduciaryClients(new ArrayList<>(getFiduciaryClientsSelected()));
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
    InstantiationException, IllegalAccessException {
        boolean dataFromEmail = false;
        setClientSelectItemWrapperConverter(new SelectItemWrapperConverter<>(Client.class));
        if (!ValidationHelper.isNullOrEmpty(getMail())) {
            getEntity().setNdg(!ValidationHelper.isNullOrEmpty(getMail().getNdg()) ? getMail().getNdg() : null);
            getEntity().setCdr(!ValidationHelper.isNullOrEmpty(getMail().getCdr()) ? getMail().getCdr() : null);
            if ((!ValidationHelper.isNullOrEmpty(getMail().getManagers()) ||
                    !ValidationHelper.isNullOrEmpty(getMail().getClientFiduciary()))) {
                presetParamsFromMail(getMail());
                dataFromEmail = true;
            }
        } else if (!ValidationHelper.isNullOrEmpty(getEntity().getRequestMangerList())) {
            prepareFiduciaryClientsList(getEntity().getRequestMangerList());
        }
        if (!ValidationHelper.isNullOrEmpty(getFiduciaryClients())) {
            getClientSelectItemWrapperConverter().getWrapperList().addAll(getFiduciaryClients());
        }
        executeJS("updateSubjects();");
        if (!ValidationHelper.isNullOrEmpty(getEntity().getSubject())
                && getEntity().getSubject().getTypeIsPhysicalPerson()) {
            checkFiscalCodeCalculated(getEntity().getSubject());
        }
        if (!getEntity().isNew() && ValidationHelper.isNullOrEmpty(getEntity().getService())
                && !ValidationHelper.isNullOrEmpty(getEntity().getMultipleServices())) {
            setMultipleCreate(true);
        }
        generateMenuModel();
        if (isMultipleCreate()) {
            setMultipleTabPath("requestComponents/SUBJECT_MASTERY.xhtml");
        } else {
            setMultipleTabPath("");
        }
        if (getCurrentUser().isExternal()) {
            setClients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                    Restrictions.eq("id", getCurrentUser().getClientId()),
                    Restrictions.or(
                            Restrictions.eq("id", getEntity().getClient() == null ? 0L : getEntity().getClient().getId()),
                            Restrictions.eq("external", Boolean.FALSE),
                            Restrictions.isNull("external")),
                    Restrictions.or(
                            Restrictions.eq("fiduciary", Boolean.FALSE),
                            Restrictions.isNull("fiduciary")),
                    Restrictions.or(
                            Restrictions.eq("manager", Boolean.FALSE),
                            Restrictions.isNull("manager"))
            }));
        } else {
            setClients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                    Restrictions.or(
                            Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted")),
                    Restrictions.or(
                            Restrictions.eq("id", getEntity().getClient() == null ? 0L : getEntity().getClient().getId()),
                            Restrictions.eq("external", Boolean.FALSE),
                            Restrictions.isNull("external")),
                    Restrictions.or(
                            Restrictions.eq("fiduciary", Boolean.FALSE),
                            Restrictions.isNull("fiduciary")),
                    Restrictions.or(
                            Restrictions.eq("manager", Boolean.FALSE),
                            Restrictions.isNull("manager"))
            }));
        }

        setNotaryList(ComboboxHelper.fillList(Notary.class, Order.asc("name"),
                new Criterion[]{}));

        if (getEntity().getClient() != null && getEntity().getClient().getId() > 0L) {
            setSelectedClientId(getEntity().getClient().getId());
            SelectItemHelper.addItemToListIfItIsNotInIt(getClients(), getEntity().getClient());
        }

        if (getEntity().getNotary() != null) {
            setSelectedNotaryId(getEntity().getNotary().getId());
        }

        onClientChange(dataFromEmail);
        setSelectedAgencyId(getEntity().getAgency() != null ? getEntity().getAgency().getId() : null);
        setSelectedAgencyOfficeId(getEntity().getOffice() != null ? getEntity().getOffice().getId() : null);

        if (!dataFromEmail) {
            setSelectedBillingClientId(getEntity().getBillingClient() != null ? getEntity().getBillingClient().getId() : null);
        }

        setRequestTypes(ComboboxHelper.fillList(RequestType.class));
        setSelectedRequestTypeId(getEntity().getRequestType() != null ? getEntity().getRequestType().getId() : null);
        onRequestTypeChange();

        setSelectedServiceId(getEntity().getService() != null ? getEntity().getService().getId() : null);
        setSelectedServiceIds(getEntity().getMultipleServices() != null ? getEntity().getMultipleServices().stream()
                .map(Service::getId).map(i -> Long.toString(i)).collect(Collectors.toList()) : null);
        onServiceChange();

        fillRequestEnumTypes();

        setUsers(ComboboxHelper.fillList(User.class, Order.asc("createDate"), new Criterion[]{
                Restrictions.and(
                        Restrictions.or(
                                Restrictions.eq("category", UserCategories.INTERNO),
                                Restrictions.isNull("category")
                        ),
                        Restrictions.eq("status", UserStatuses.ACTIVE)
                )}));
        setSelectedUserId(getEntity().getUser() != null ? getEntity().getUser().getId() : null);

        setDocuments(getEntity().getDocuments() != null ? getEntity().getDocuments() : new LinkedList<>());
        setAttachments(getEntity().getDocuments() != null ? getEntity().getDocuments() : new LinkedList<>());
        Formality formality = getEntity().getDistraintFormality();
        if (!ValidationHelper.isNullOrEmpty(formality)) {
            setDistraintFormality(formality);
            Document attachment = formality.getDocument();
            if (!ValidationHelper.isNullOrEmpty(attachment)
                    && getAttachments().stream().noneMatch(d -> d.getId().equals(attachment.getId()))) {
                getAttachments().add(attachment);
            }
        }
        setWrapper(new RequestWrapper(getEntity(), isMultipleCreate()));
        setSubject(getEntity().getSubject() != null ? getEntity().getSubject() : new Subject());
        setResidence(getEntity().getResidence() != null ? getEntity().getResidence() : new Residence());
        setDomicile(getEntity().getDomicile() != null ? getEntity().getDomicile() : new Residence());

        setStartStateId(getEntity().getStateId());
        setSelectedUserId(getEntity().getUser() != null ? getEntity().getUser().getId() : null);
        setShouldBeRedirected(true);
        setServiceUpdate(false);
        Hibernate.initialize(getEntity().getRequestFormalities());
        Hibernate.initialize(getEntity().getRequestSubjects());
        
        fillSubjectWrapperList(getEntity());
        setSubjectWrapper(new SubjectWrapper());

        setLandAggregations(ComboboxHelper.fillList(LandChargesRegistry.class, Order.asc("name")));
        if (getRequestParameter(RedirectHelper.MAIL) != null) {
            if(!ValidationHelper.isNullOrEmpty(getMail()) && !ValidationHelper.isNullOrEmpty(mail.getClient())) {
                SelectItemHelper.addItemToListIfItIsNotInIt(getClients(), mail.getClient());
                setSelectedClientId(mail.getClient().getId());
            }
        }
    }

    private void fillRequestEnumTypes() {
        List<SelectItem> items = new LinkedList<>();
        items.add(new SelectItem(RequestEnumTypes.SUBJECT.getId(), RequestEnumTypes.SUBJECT.toString()));
        items.add(new SelectItem(RequestEnumTypes.PROPERTY.getId(), RequestEnumTypes.PROPERTY.toString()));
        setRequestEnumTypesFirst(items);
        List<SelectItem> items2 = new LinkedList<>();
        items2.add(new SelectItem(RequestEnumTypes.COMMON.getId(), RequestEnumTypes.COMMON.toString()));
        items2.add(new SelectItem(RequestEnumTypes.MADE.getId(), RequestEnumTypes.MADE.toString()));
        setRequestEnumTypesSecond(items2);
        if (getEntity().getType() != null) {
            if (getEntity().getType() == RequestEnumTypes.SUBJECT
                    || getEntity().getType() == RequestEnumTypes.PROPERTY) {
                setSelectedRequestEnumTypeFirst(getEntity().getType().getId());
            } else {
                setSelectedRequestEnumTypeFirst(RequestEnumTypes.PROPERTY.getId());
                setSelectedRequestEnumTypeSecond(getEntity().getType().getId());
            }
        }
    }

    private void fillSubjectWrapperList(Request request) {
        setSubjectWrapperList(new LinkedList<>());
        if (!ValidationHelper.isNullOrEmpty(request.getSubjectList())) {
            for (Subject sbj : request.getSubjectList()) {
                SubjectWrapper sbjWrp = new SubjectWrapper();
                sbjWrp.setId(sbj.getId());
                sbjWrp.setName(sbj.getName());
                sbjWrp.setSurname(sbj.getSurname());
                sbjWrp.setBirthDate(sbj.getBirthDate());
                sbjWrp.setBirthCity(sbj.getBirthCity());
                sbjWrp.setCityId(sbj.getBirthCity() != null ? sbj.getBirthCity().getId() : null);
                sbjWrp.setProvinceId(sbj.getBirthProvince() != null ? sbj.getBirthProvince().getId() : null);
                sbjWrp.setBusinessName(sbj.getBusinessName());
                sbjWrp.setTypeId(sbj.getTypeId());
                sbjWrp.setSelectedSexTypeId(sbj.getSexType() != null ? sbj.getSexType().getId() : null);
                sbjWrp.setFiscalCode(sbj.getFiscalCode());
                sbjWrp.setNumberVAT(sbj.getNumberVAT());
                
                RequestSubject requestSubject = request.getRequestSubjects().stream()
                        .filter(f -> f.getSubject().getId().equals(sbj.getId()))
                        .filter(f -> f.getRequest().getId().equals(request.getId())).findFirst().orElse(null);
                if(!ValidationHelper.isNullOrEmpty(requestSubject)) {
                    sbjWrp.setSectionCType(requestSubject.getType());
                }
                
                if (sbj.getTypeIsPhysicalPerson()) {
                    sbjWrp.setSelectedNationId(sbj.getCountry() != null ? sbj.getCountry().getId() : null);
                } else {
                    sbjWrp.setSelectedJuridicalNationId(sbj.getCountry() != null ? sbj.getCountry().getId() : null);
                }
                getSubjectWrapperList().add(sbjWrp);
            }
        }
    }

    private void addMenuItem(String value) {
        DefaultMenuItem menuItem = new DefaultMenuItem(value);

        menuItem.setCommand("#{requestEditBean.goToTab(" +
                getTopMenuModel().getElements().size() + ")}");
        menuItem.setUpdate("form");

        getTopMenuModel().addElement(menuItem);
    }

    private void generateMenuModel() {
        setTopMenuModel(new DefaultMenuModel());
        if (isMultipleCreate()) {
            addMenuItem(ResourcesHelper.getString("requestSubjectTab"));
            addMenuItem(ResourcesHelper.getString("requestFirstTab"));
            addMenuItem(ResourcesHelper.getString("requestMultipleTab"));
            addMenuItem(ResourcesHelper.getString("requestLastTab"));
        } else {
            addMenuItem(ResourcesHelper.getString("requestFirstTab"));
            if (!ValidationHelper.isNullOrEmpty(getInputCardList())) {
                getInputCardList()
                .forEach(card -> addMenuItem(card.getName().toUpperCase()));
            }
            addMenuItem(ResourcesHelper.getString("requestLastTab"));
        }
    }

    public void prepareSubjectToCreateInSubjectList() {
        setSubjectWrapper(new SubjectWrapper());
    }

    public void saveSubjectToList() {
        if (!ValidationHelper.isNullOrEmpty(getSubjectWrapper().getNumberVAT())
                && !checkNumberVAT(getSubjectWrapper().getNumberVAT())) {
            this.getContext().addMessage(
                    null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper.getValidation("checkIVA"), null));
            return;
        }

        getSubjectWrapperList().add(getSubjectWrapper());
        executeJS("PF('createSubjectDlgWV').hide();");
        executeJS("updateSubjectTable()");
    }

    public void removeSubjectFromList(SubjectWrapper subjectWrapper) {
        if (subjectWrapper != null) {
            getSubjectWrapperList().remove(subjectWrapper);
        }
    }

    public Date getTodayDate(){
        return new Date();
    }

    private void checkFiscalCodeCalculated(Subject subject) {
        String fiscalCodeToCompare = "";
        try {
            if (!ValidationHelper.isNullOrEmpty(subject.getBirthCity())) {
                fiscalCodeToCompare = SubjectHelper.createFiscalCode(subject.getBirthCity().getId(), null,
                        subject.getSex(), subject.getName(), subject.getSurname(), subject.getBirthDate());
            } else {
                fiscalCodeToCompare = SubjectHelper.createFiscalCode(null, subject.getCountry().getId(),
                        subject.getSex(), subject.getName(), subject.getSurname(), subject.getBirthDate());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        if (!fiscalCodeToCompare.equalsIgnoreCase(subject.getFiscalCode())) {
            RequestContext.getCurrentInstance().getScriptsToExecute().clear();
            executeJS("PF('diffFiscalCodeWV').show();");
        }
    }

    public void restoreFiscalCode() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        boolean showFailedMsg = true;
        if ((SubjectType.PHYSICAL_PERSON.getId().equals(getWrapper().getSelectedPersonId())
                && !ValidationHelper.isNullOrEmpty(getSubject().getFiscalCode()))
                || (SubjectType.LEGAL_PERSON.getId().equals(getWrapper().getSelectedPersonId())
                        && !ValidationHelper.isNullOrEmpty(getSubject().getNumberVAT()))) {
            List<Subject> subjectList = DaoManager.load(Subject.class, new Criterion[]{
                    SubjectType.PHYSICAL_PERSON.getId().equals(getWrapper().getSelectedPersonId())
                    ? Restrictions.eq("fiscalCode", getSubject().getFiscalCode())
                            : Restrictions.eq("numberVAT", getSubject().getNumberVAT())
            });
            if (!ValidationHelper.isNullOrEmpty(subjectList)) {
                showFailedMsg = false;
                if (subjectList.size() == 1) {
                    selectRestoredSubject(subjectList.get(0));
                } else {
                    setSubjectsToRestore(subjectList);
                    executeJS("PF('subjectRestoreDlg').show();");
                }
            }
        }
        if (showFailedMsg) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    ResourcesHelper.getValidation("warning"),
                    ResourcesHelper.getValidation("noSubjectToRestore"));
        }
    }

    public void selectRestoredSubject() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedSubjectToRestore())) {
            selectRestoredSubject(getSelectedSubjectToRestore());
            setSubjectsToRestore(null);
            setSelectedSubjectToRestore(null);
        }
    }

    public void selectRestoredSubject(Subject subject)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (SubjectType.PHYSICAL_PERSON.getId().equals(subject.getTypeId())) {
            getSubject().setSurname(subject.getSurname());
            getSubject().setName(subject.getName());
            getWrapper().setSelectedSexTypeId(subject.getSex());
            getSubject().setBirthDate(subject.getBirthDate());
        } else {
            getSubject().setBusinessName(subject.getBusinessName());
            getSubject().setNumberVAT(subject.getNumberVAT());
        }
        getSubject().setFiscalCode(subject.getFiscalCode());
        if (!ValidationHelper.isNullOrEmpty(subject.getCountry())) {
            getWrapper().setSelectedNationId(subject.getCountry().getId());
            getWrapper().setSelectedJuridicalNationId(subject.getCountry().getId());
            getWrapper().setSelectProvinceId(-1L);
        } else {
            getWrapper().setSelectProvinceId(subject.getBirthProvince().getId());
            getWrapper().onChangeProvince();
            getWrapper().setSelectedCityId(subject.getBirthCity().getId());
        }
    }

    public void generateFiscalCode() throws Exception {
        if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectProvinceId())) {
            getWrapper().setSelectedCityId(null);
            getWrapper().setSelectedNationId(null);
        } else if (getWrapper().getSelectProvinceId() != -1) {
            getWrapper().setSelectedNationId(null);
        } else {
            getWrapper().setSelectedCityId(null);
        }

        String fiscalCode = SubjectHelper.createFiscalCode(getWrapper().getSelectedCityId(), getWrapper().getSelectedNationId(),
                getWrapper().getSelectedSexTypeId(), getSubject().getName(), getSubject().getSurname(), getSubject().getBirthDate());
        getSubject().setFiscalCode(fiscalCode);
    }

    public void handleDateSelect(SelectEvent event) {
        getSubject().setBirthDate((Date) event.getObject());
    }

    public void onClientChange(boolean dataFromEmail) throws IllegalAccessException, PersistenceBeanException {
        try {
            if (!ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
                Client client = DaoManager.get(Client.class, new Criterion[]{
                        Restrictions.eq("id", getSelectedClientId())});
                if (!dataFromEmail) {
                    setBillingClients(ComboboxHelper.fillList(client.getBillingRecipientList(),
                            true, false));
                }
                List<Client> clients;
                if (!ValidationHelper.isNullOrEmpty(getRedirectFromMail())
                        && !ValidationHelper.isNullOrEmpty(getMail())) {
                    clients = DaoManager.load(Client.class,
                            new CriteriaAlias[]{
                                    new CriteriaAlias("emails", "email", JoinType.INNER_JOIN)},
                            new Criterion[]{
                                    Restrictions.or(
                                            Restrictions.eq("deleted", Boolean.FALSE),
                                            Restrictions.isNull("deleted")),
                                    Restrictions.eq("email.email", getMail().getEmailCC())});
                } else {
                    clients = DaoManager.load(Client.class, new Criterion[]{
                            Restrictions.or(
                                    Restrictions.eq("deleted", Boolean.FALSE),
                                    Restrictions.isNull("deleted"))
//                            ,Restrictions.eq("fiduciary", true)
                            });
                }
                if (ValidationHelper.isNullOrEmpty(getFiduciaryClients())) {
                    setFiduciaryClients(new ArrayList<>());
                }
                
//                if (!ValidationHelper.isNullOrEmpty(clients)) {
//                    for (Client item : clients.stream()
//                            .filter(c -> getFiduciaryClients().stream().noneMatch(x -> x.getId().equals(c.getId())))
//                            .collect(Collectors.toList())) {
//                        SelectItemWrapper<Client> selectItem = new SelectItemWrapper<>(item);
//                        getFiduciaryClients().add(selectItem);
//                        getClientSelectItemWrapperConverter().getWrapperList().add(selectItem);
//                    }
//                }
                
                if (!ValidationHelper.isNullOrEmpty(clients)) {
                    List<Client> managers =  clients
                            .stream()
                            .filter(c -> (!ValidationHelper.isNullOrEmpty(c.getManager()) && c.getManager()))
                            .filter(c -> emptyIfNull(c.getReferenceClients()).stream()
                                    .anyMatch(rc-> rc.getId().equals(getSelectedClientId())))
                            .collect(Collectors.toList());

                    for (Client item : managers.stream()
                            .filter(c -> getFiduciaryClients().stream().noneMatch(x -> x.getId().equals(c.getId())))
                            .collect(Collectors.toList())) {
                        SelectItemWrapper<Client> selectItem = new SelectItemWrapper<>(item);
                        getFiduciaryClients().add(selectItem);
                        getClientSelectItemWrapperConverter().getWrapperList().add(selectItem);
                    }
                }

                if (client.isHasAgency()) {
                    List<Agency> list = client.getAgencies().stream().filter(f -> !ValidationHelper.isNullOrEmpty(f.getAgencyType()))
                            .filter(f -> f.getAgencyType().equals(AgencyType.FILIAL))
                            .collect(Collectors.toList());
                    if (!ValidationHelper.isNullOrEmpty(list)) {
                        setHasAgencyList(true);
                        setAgencyList(ComboboxHelper.fillList(list, true));
                    } else {
                        setHasAgencyList(false);
                        setAgencyList(ComboboxHelper.fillList(new ArrayList<>(), true));
                    }
                } else {
                    setHasAgencyList(false);
                }
                if (ValidationHelper.isNullOrEmpty(client.getHasAgencyOffice()) || client.getHasAgencyOffice()) {
                    List<Agency> list = client.getAgencies().stream().filter(f -> !ValidationHelper.isNullOrEmpty(f.getAgencyType()))
                            .filter(f -> f.getAgencyType().equals(AgencyType.OFFICE))
                            .collect(Collectors.toList());
                    if (!ValidationHelper.isNullOrEmpty(list)) {
                        setHasAgencyOfficeList(true);
                        setAgencyOfficeList(ComboboxHelper.fillList(list, true));
                    } else {
                        setHasAgencyOfficeList(false);
                        setAgencyOfficeList(ComboboxHelper.fillList(new ArrayList<>(), true));
                    }
                } else {
                    setHasAgencyOfficeList(false);
                }
            }else {
                setFiduciaryClients(new ArrayList<>());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

    }

    public void onRequestTypeChange() throws IllegalAccessException, PersistenceBeanException {
        setServices(RequestHelper.onRequestTypeChange(getSelectedRequestTypeId(), isMultipleCreate()));
    }

    public void onServiceChange() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setInputCardList(RequestHelper.onServiceChange(getSelectedServiceId()));
        generateMenuModel();
    }

    public void onMultipleServiceChange() throws PersistenceBeanException, IllegalAccessException {
        setInputCardList(RequestHelper.onMultipleServiceChange(getSelectedServiceIds().stream().map(Long::parseLong)
                .collect(Collectors.toList())));
    }

    /**
     * crutch. without this method fields:
     * selectedAgencyId
     * selectedAgencyOfficeId
     * selectedBillingClientId
     * do not set
     */
    public void onAgencyChange() {
    }

    private void updateForm() {
        RequestContext context = RequestContext.getCurrentInstance();
        context.update("form");
    }

    public void goPreviousTab() throws PersistenceBeanException, IllegalAccessException {
        cleanValidation();
        setActiveMenuTabNum(getActiveMenuTabNum() - 1);
        if (isMultipleCreate() && getActiveMenuTabNum() == 1) {
            setShownFields(null);
            return;
        }
        if (isMultipleCreate() && getActiveMenuTabNum() == 2) {
            onMultipleServiceChange();
        }
        if (getActiveMenuTabNum() > 0) {
            generateTab();
        } else {
            setShownFields(null);
        }

        updateForm();
    }

    public boolean goNextTab() throws PersistenceBeanException, IllegalAccessException {
        if (!valid()) {
            return false;
        }
        setActiveMenuTabNum(getActiveMenuTabNum() + 1);
        if (isMultipleCreate() && getActiveMenuTabNum() == 1) {
            return true;
        }
        if (isMultipleCreate() && getActiveMenuTabNum() == 2) {
            onMultipleServiceChange();
            generateTab();
        } else if (getInputCardList() != null && getActiveMenuTabNum() <= getInputCardList().size()) {
            generateTab();
        } else {
            setShownFields(null);
        }

        updateForm();

        return true;
    }

    public void goToTab(int num) throws PersistenceBeanException, IllegalAccessException {
        if (activeMenuTabNum < num) {
            while (activeMenuTabNum < num) {
                if (!goNextTab()) {
                    return;
                }
            }

        } else if (activeMenuTabNum > num) {
            while (activeMenuTabNum > num) {
                goPreviousTab();
            }
        }
    }

    public void generateTab() throws PersistenceBeanException, IllegalAccessException {
        List<InputCardManageField> listWithPos;
        List<InputCardManageField> listNoPos;
        if (isMultipleCreate()) {
            listWithPos = new ArrayList<>();
            listNoPos = new ArrayList<>(getInputCardList().get(0).getFields());
        } else {
            listWithPos = new ArrayList<>(getInputCardList()
                    .get(getActiveMenuTabNum() - 1).getFields().stream()
                    .filter(f -> f.getPosition() != null).collect(Collectors.toList()));

            listNoPos = new ArrayList<>(getInputCardList()
                    .get(getActiveMenuTabNum() - 1).getFields().stream()
                    .filter(f -> f.getPosition() == null).collect(Collectors.toList()));
        }
        List<RequestViewWrapper> wrappers = new ArrayList<>();
        for (int i = 0; ; i++) {
            if (listWithPos.isEmpty()) {
                break;
            }
            RequestViewWrapper wrapper = new RequestViewWrapper();
            wrapper.setLineNum(i);
            wrapper.setFields(new ArrayList<>());
            for (int j = 1; j <= 4; j++) {
                int pos = i * 4 + j;
                InputCardManageField field = listWithPos
                        .stream().filter(f -> f.getPosition() != null && f.getPosition().equals(pos))
                        .findFirst().orElse(null);
                if (field != null) {
                    listWithPos.remove(field);
                }
                if (field == null || !field.getField().getEnumPropsWrapper().isHasManyFields()) {
                    wrapper.getFields().add(field);
                } else {
                    wrapper.setOneElement(true);
                    wrapper.setField(field);
                    // break;
                }
            }
            wrappers.add(wrapper);
        }
        for (int i = wrappers.size(); ; i++) {
            if (listNoPos.isEmpty()) {
                break;
            }
            RequestViewWrapper wrapper = new RequestViewWrapper();
            RequestViewWrapper additional = null;
            wrapper.setLineNum(i);
            wrapper.setFields(new ArrayList<>());
            for (int j = 0; j < 4; j++) {
                if (!listNoPos.isEmpty()) {
                    InputCardManageField field = listNoPos.get(0);
                    listNoPos.remove(field);
                    if (field == null || !field.getField().getEnumPropsWrapper().isHasManyFields()) {
                        wrapper.getFields().add(field);
                    } else {
                        i++;
                        j--;
                        additional = new RequestViewWrapper();
                        additional.setLineNum(i);
                        additional.setOneElement(true);
                        additional.setField(field);
                        break;
                    }
                }
            }
            if (!ValidationHelper.isNullOrEmpty(wrapper.getFields())) {
                wrappers.add(wrapper);
            } else {
                additional.setLineNum(additional.getLineNum() - 1);
            }
            if (additional != null) {
                wrappers.add(additional);
            }
        }
        setShownFields(wrappers);
        if (isMultipleCreate()) {
            getWrapper().setLists(getInputCardList().get(0).getFields());
        } else {
            getWrapper().setLists(getInputCardList().get(getActiveMenuTabNum() - 1).getFields());
        }
    }

    public boolean valid() {
        if (isMultipleCreate() && getActiveMenuTabNum() == 0) {
            return validSubject();
        } else if ((!isMultipleCreate() && getActiveMenuTabNum() == 0)
                || (isMultipleCreate() && getActiveMenuTabNum() == 1)) {
            return validFirstTab();
        } else if (getActiveMenuTabNum() <= getInputCardList().size()) {
            return validGeneratedTab();
        } else return true;
    }

    private boolean validGeneratedTab() {
        cleanValidation();
        for (InputCardManageField field : getInputCardList().get(getActiveMenuTabNum() - 1).getFields()) {
            if (field.getState().equals(ManageTypeFieldsState.ENABLE_AND_MANDATORY)) {
                validateField(field);
            }
        }
        return !getValidationFailed();
    }

    private boolean validSubject() {
        cleanValidation();
        checkSubjectMastery();
        return !getValidationFailed();
    }

    private void checkSubjectMastery() {
        if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedPersonId()))
            addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_PERSON_TYPE.name());
        else {
            if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectProvinceId()))
                addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_PROVINCE.name());
            else if (getWrapper().getSelectProvinceId() != -1L) {
                if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedCityId()))
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_COMUNE.name());
            } else if (getWrapper().getSelectProvinceId() == -1L) {
                if (getWrapper().getSelectedPersonId().equals(1L)
                        && ValidationHelper.isNullOrEmpty(getWrapper().getSelectedNationId()))
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_NATIONS.name());
                else if (getWrapper().getSelectedPersonId().equals(2L)
                        && ValidationHelper.isNullOrEmpty(getWrapper().getSelectedJuridicalNationId()))
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_JURIDICAL_NATIONS.name());
            }
            if (getWrapper().getSelectedPersonId().equals(1L)) {
                if (ValidationHelper.isNullOrEmpty(getSubject().getSurname()))
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_SURNAME.name());
                if (ValidationHelper.isNullOrEmpty(getSubject().getName()))
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_NAME.name());
                if (getSubject().getSurname().length() < 2 || getSubject().getName().length() < 2) {

                    if (getSubject().getSurname().length() < 2)
                        addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_SURNAME.name());

                    if (getSubject().getName().length() < 2)
                        addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_NAME.name());
                }
                if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedSexTypeId()))
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_SEX_TYPE.name());
                if (ValidationHelper.isNullOrEmpty(getSubject().getBirthDate()))
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_BIRTHDAY.name());
                if (!ValidationHelper.isNullOrEmpty(getSubject().getBirthDate())
                        && (getSubject().getBirthDate().after(new Date())
                                || getSubject().getBirthDate().before(DateTimeHelper.getHundredFiveBefore()))) {
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_BIRTHDAY.name());

                }
                if (ValidationHelper.isNullOrEmpty(getSubject().getFiscalCode()))
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_FISCAL_CODE.name());
            } else if (getWrapper().getSelectedPersonId().equals(2L)) {
                if (ValidationHelper.isNullOrEmpty(getSubject().getBusinessName()))
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_SOCIAL_REGION.name());
                if (ValidationHelper.isNullOrEmpty(getSubject().getNumberVAT())
                        && ValidationHelper.isNullOrEmpty(getSubject().getFiscalCode())) {
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_IVA.name());
                    addRequiredFieldException("SUBJECT_MASTERY_IVA_FISCAL_CODE");
                }
                if (!ValidationHelper.isNullOrEmpty(getSubject().getNumberVAT())
                        && !checkNumberVAT(getSubject().getNumberVAT())) {
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_IVA.name());
                }
                if (!ValidationHelper.isNullOrEmpty(getSubject().getOldNumberVAT())
                        && !checkNumberVAT(getSubject().getOldNumberVAT())) {
                    addRequiredFieldException(DocumentValidation.SUBJECT_MASTERY_OLD_IVA.name());
                }

            }
        }
    }

    private boolean checkNumberVAT(String numberVAT) { //algorithm numberVAT
        Long[] array = new Long[correctNumberVATLength];
        Long controlCode = 0L;
        Long sum = 0L;
        Long lastDigit = 0L;
        if (numberVAT.length() == correctNumberVATLength && NumberUtils.isNumber(numberVAT)) {

            String[] temp = numberVAT.split("");

            for (int i = 0; i < temp.length; i++) {
                array[i] = Long.parseLong(temp[i]);
            }
            controlCode = array[correctNumberVATLength - 1];

            for (int i = 0; i < array.length - 1; i++) {
                if ((i + 1) % 2 == 0) {
                    array[i] *= 2;
                }
                if (array[i] > 9) {
                    array[i] = array[i] - 9;
                }
                sum += array[i];
            }
            lastDigit = sum % 10;
            if (lastDigit.equals(0L) && controlCode.equals(0L)) {
                return true;
            } else if ((10 - lastDigit) % 10 == controlCode) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void validateField(InputCardManageField field) {

        switch (field.getField()) {
        case CONSERVATORY:
            if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedConserItemId()))
                addRequiredFieldException(ManageTypeFields.CONSERVATORY.name());
            break;
        case TALOVARE:
            if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedTaloreItemId()))
                addRequiredFieldException(ManageTypeFields.TALOVARE.name());
            break;
        case PROPERTY_DATA:
            if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedBuildingId()))
                addRequiredFieldException("form:" + DocumentValidation.PROPERTY_DATA_LANDED_BUILDING.name());
            if (ValidationHelper.isNullOrEmpty(getEntity().getSheet()))
                addRequiredFieldException(DocumentValidation.PROPERTY_DATA_SHEET.name());
            if (ValidationHelper.isNullOrEmpty(getEntity().getParticle()))
                addRequiredFieldException(DocumentValidation.PROPERTY_DATA_PARTICLE.name());
            break;
        case SUBJECT_MASTERY:
            checkSubjectMastery();
            break;
        case CDR:
            if (ValidationHelper.isNullOrEmpty(getEntity().getCdr()))
                addRequiredFieldException(DocumentValidation.CDR.name());
            break;
        case NDG:
            if (ValidationHelper.isNullOrEmpty(getEntity().getNdg()))
                addRequiredFieldException(DocumentValidation.NDG.name());
            break;
        case ULTIMA_RESIDENZA:
            if (ValidationHelper.isNullOrEmpty(getEntity().getUltimaResidenza()))
                addRequiredFieldException(DocumentValidation.ULTIMA_RESIDENZA.name());
            break;
        case MANAGER:
            if (ValidationHelper.isNullOrEmpty(getEntity().getManager()))
                addRequiredFieldException(DocumentValidation.MANAGER.name());
            break;
        case POSITION_PRACTICE:
            if (ValidationHelper.isNullOrEmpty(getEntity().getPosition()))
                addRequiredFieldException(DocumentValidation.POSITION_PRACTICE.name());
            break;
        case FORMALITIES_AUTHORIZED:
            if (ValidationHelper.isNullOrEmpty(getEntity().getFormalityAuthorized()))
                addRequiredFieldException(DocumentValidation.FORMALITIES_AUTHORIZED.name());
            break;
        case NOTE:
            if (ValidationHelper.isNullOrEmpty(getEntity().getNote()))
                addRequiredFieldException(DocumentValidation.NOTE.name());
            break;
        case UPDATE_DATE:
            if (ValidationHelper.isNullOrEmpty(getEntity().getUpdateDate()))
                addRequiredFieldException(DocumentValidation.UPDATE_DATE.name());
            break;
        case PROVINCE:
            if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedProvincePropertyId()))
                addRequiredFieldException(DocumentValidation.STANDART_PROVINCE.name());
            break;
        case ACT_TYPE:
            if (ValidationHelper.isNullOrEmpty(getEntity().getActType()))
                addRequiredFieldException(DocumentValidation.ACT_TYPE.name());
            break;
        case ACT_NUMBER:
            if (ValidationHelper.isNullOrEmpty(getEntity().getActNumber()))
                addRequiredFieldException(DocumentValidation.ACT_NUMBER.name());
            break;
        case ACT_DATE:
            if (ValidationHelper.isNullOrEmpty(getEntity().getActDate()))
                addRequiredFieldException(DocumentValidation.ACT_DATE.name());
            break;
        case TERM_DATE:
            if (ValidationHelper.isNullOrEmpty(getEntity().getTermDate()))
                addRequiredFieldException(DocumentValidation.TERM_DATE.name());
            break;
        case REA_NUMBER:
            if (ValidationHelper.isNullOrEmpty(getEntity().getReaNumber()))
                addRequiredFieldException(DocumentValidation.REA_NUMBER.name());
            break;
        case NATURE_LEGAL:
            if (ValidationHelper.isNullOrEmpty(getEntity().getNatureLegal()))
                addRequiredFieldException(DocumentValidation.NATURE_LEGAL.name());
            break;
        case ISTAT:
            if (ValidationHelper.isNullOrEmpty(getEntity().getIstat()))
                addRequiredFieldException(DocumentValidation.ISTAT.name());
            break;
        case RESIDENCE:
            if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedResidenceProvinceId()))
                addRequiredFieldException(DocumentValidation.RESIDENCE_PROVINCE.name());
            else if (getWrapper().getSelectedResidenceProvinceId().equals(-1L)) {
                if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedResidenceNationId()))
                    addRequiredFieldException(DocumentValidation.RESIDENCE_NATIONS.name());
            } else if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedResidenceCityId()))
                addRequiredFieldException(DocumentValidation.RESIDENCE_COMUNE.name());
            if (ValidationHelper.isNullOrEmpty(getEntity().getResidence().getAddress()))
                addRequiredFieldException(DocumentValidation.RESIDENCE_ADDRESS.name());
            if (ValidationHelper.isNullOrEmpty(getEntity().getResidence().getCap()))
                addRequiredFieldException(DocumentValidation.RESIDENCE_CAP.name());
            break;
        case DOMICILE:
            if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedDomicileProvinceId()))
                addRequiredFieldException(DocumentValidation.DOMICILE_PROVINCE.name());
            else if (getWrapper().getSelectedDomicileProvinceId().equals(-1L)) {
                if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedDomicileNationId()))
                    addRequiredFieldException(DocumentValidation.DOMICILE_NATIONS.name());
            } else if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedDomicileCityId()))
                addRequiredFieldException(DocumentValidation.DOMICILE_COMUNE.name());
            if (ValidationHelper.isNullOrEmpty(getEntity().getDomicile().getAddress()))
                addRequiredFieldException(DocumentValidation.DOMICILE_ADDRESS.name());
            if (ValidationHelper.isNullOrEmpty(getEntity().getDomicile().getCap()))
                addRequiredFieldException(DocumentValidation.DOMICILE_CAP.name());
            break;
        case NOTARY:
            if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedNotaryId()))
                addRequiredFieldException(ManageTypeFields.NOTARY.name());
            break;
        }
    }

    private boolean validFirstTab() {
        cleanValidation();
        if (ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
            addRequiredFieldException("form:client");
        }
        if (ValidationHelper.isNullOrEmpty(getSelectedRequestTypeId())) {
            addRequiredFieldException("form:requestType");
        }
        if (!isMultipleCreate() && ValidationHelper.isNullOrEmpty(getSelectedServiceId())) {
            addRequiredFieldException("form:service");
        }
        if (isMultipleCreate() && ValidationHelper.isNullOrEmpty(getSelectedServiceIds())) {
            addRequiredFieldException("form:multipleService");
        }
        return !getValidationFailed();
    }

    public void onChangeProvince() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        getWrapper().onChangeProvince();
    }

    public void onChangeResidenceProvince() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        getWrapper().onChangeResidenceProvince();
    }

    public void onChangeDomicleProvince() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        getWrapper().onChangeDomicleProvince();
    }

    public void onChangePropertyProvince() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        getWrapper().onChangePropertyProvince();
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {

    }

    @Override
    public void onSave() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        prepareRequestToSave();

        getEntity().setClient(DaoManager.get(Client.class, this.getSelectedClientId()));

        getEntity().setDistraintFormality(getDistraintFormality());

        if (!ValidationHelper.isNullOrEmpty(this.getSelectedNotaryId()))
            getEntity().setNotary(DaoManager.get(Notary.class, this.getSelectedNotaryId()));
        else
            getEntity().setNotary(null);

        if (getSelectedBillingClientId() != null) {
            getEntity().setBillingClient(DaoManager.get(Client.class, getSelectedBillingClientId()));
        } else {
            getEntity().setBillingClient(null);
        }

        if (!ValidationHelper.isNullOrEmpty(getFiduciaryClientsSelected())) {
            getEntity().setRequestMangerList(new ArrayList<>());
            List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                    Restrictions.in("id", getFiduciaryClientsSelected().stream()
                            .map(SelectItemWrapper::getId).collect(Collectors.toList()))});
            getEntity().getRequestMangerList().addAll(clients);
        }
        if (getEntity().getStateId() == null) {
            getEntity().setStateId(RequestState.INSERTED.getId());
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedRequestEnumTypeFirst())) {
            if (getSelectedRequestEnumTypeFirst().equals(RequestEnumTypes.SUBJECT.getId())) {
                getEntity().setType(RequestEnumTypes.SUBJECT);
            } else {
                getEntity().setType(getSelectedRequestEnumTypeSecond() == null
                        ? RequestEnumTypes.PROPERTY
                                : RequestEnumTypes.getById(getSelectedRequestEnumTypeSecond()));
            }
        }
        getEntity().setRequestType(DaoManager.get(RequestType.class, getSelectedRequestTypeId()));
        if (isMultipleCreate()) {
            getEntity().setMultipleServices(DaoManager.load(Service.class, new Criterion[]{
                    Restrictions.in("id", getSelectedServiceIds().stream()
                            .map(Long::parseLong).collect(Collectors.toList()))
            }));
        } else {
            if(!ValidationHelper.isNullOrEmpty(getSelectedServiceId()))
                getEntity().setService(DaoManager.get(Service.class, getSelectedServiceId()));
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedAgencyId())) {
            getEntity().setAgency(DaoManager.get(Agency.class, getSelectedAgencyId()));
        } else {
            getEntity().setAgency(null);
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedAgencyOfficeId())) {
            getEntity().setOffice(DaoManager.get(Agency.class, getSelectedAgencyOfficeId()));
        } else {
            getEntity().setOffice(null);
        }

        if (getSubject() != null) {
            Subject tempSubject = getSubject();
            SubjectHelper.fillSubjectFromWrapper(tempSubject, getWrapper());
            Subject subjectFromDB = SubjectHelper.getSubjectIfExists(tempSubject, getWrapper().getSelectedPersonId());
            if (subjectFromDB != null) {
                setSubject(subjectFromDB);
                if (!ValidationHelper.isNullOrEmpty(tempSubject.getId())) {
                    DaoManager.getSession().evict(DaoManager.getSession().get(Subject.class, tempSubject.getId()));
                }
            }else {
                Subject newSubject = SubjectHelper.copySubject(tempSubject);
                setSubject(newSubject);
            }
        }
        if (!ValidationHelper.isNullOrEmpty(getMail())) {
            getEntity().setMail(getMail());
        }

        fillRequestExpirationDate();
        getEntity().setSubject(getSubject());
        if (!ValidationHelper.isNullOrEmpty(getResidence())) {
            getEntity().setResidence(getResidence());
        }
        if (!ValidationHelper.isNullOrEmpty(getDomicile())) {
            getEntity().setDomicile(getDomicile());
        }
        
        if(getEntity().isNew() &&  
                ValidationHelper.isNullOrEmpty(getEntity().getAggregationLandChargesRegistry())
                && !ValidationHelper.isNullOrEmpty(getEntity().getRequestType().getDefault_registry())) {
            getEntity().setAggregationLandChargesRegistry(getEntity().getRequestType().getDefault_registry());
        }

        if (ValidationHelper.isNullOrEmpty(getWrapper().getSelectedConservatoryItemId())) {
            getWrapper().setSelectedConservatoryItemId(new ArrayList<>());
        }
        if (!ValidationHelper.isNullOrEmpty(getWrapper().getSelectedConserItemId())) {
            getWrapper().getSelectedConserItemId().forEach(elem -> {
                if (!getWrapper().getSelectedConservatoryItemId().contains(elem)) {
                    getWrapper().getSelectedConservatoryItemId().add(elem);
                }
            });
        }
        if (!ValidationHelper.isNullOrEmpty(getWrapper().getSelectedTaloreItemId())) {
            getWrapper().getSelectedTaloreItemId().forEach(elem -> {
                if (!getWrapper().getSelectedConservatoryItemId().contains(elem)) {
                    getWrapper().getSelectedConservatoryItemId().add(elem);
                }
            });
        }

        boolean saved = false;
        if (!getEntity().isNew() && !ValidationHelper.isNullOrEmpty(getWrapper().getSelectedConservatoryItemId())
                && !ValidationHelper.isNullOrEmpty(getEntity().getAggregationLandChargesRegistry())
                && getWrapper().getSelectedConservatoryItemId().stream().anyMatch(c -> c.getId().equals(getEntity().getAggregationLandChargesRegistry().getId()))
                || ValidationHelper.isNullOrEmpty(getWrapper().getSelectedConservatoryItemId())) {

            saveAllDataRelatedToRequestOrNotify(getEntity());
            if (!ValidationHelper.isNullOrEmpty(getWrapper().getSelectedConservatoryItemId())) {
                getWrapper().getSelectedConservatoryItemId().remove(getWrapper().getSelectedConservatoryItemId()
                        .stream().filter(c -> c.getId().equals(getEntity().getAggregationLandChargesRegistry().getId()))
                        .findAny().orElse(null));
            }
            saved = true;
        }
        if (!ValidationHelper.isNullOrEmpty(getWrapper().getSelectedConservatoryItemId())) {
            List<ConservatoriaSelectItem> selectedConservatoryItemId = getWrapper().getSelectedConservatoryItemId();
            for (int i = 0; i < selectedConservatoryItemId.size(); i++) {
                ConservatoriaSelectItem item = selectedConservatoryItemId.get(i);
                Request request = null;
                try {
                    if (!saved) {                   // first element
                        request = getEntity();
                        saved = true;
                    } else {
                        request = getEntity().copy();
                    }
                } catch (CloneNotSupportedException e) {
                    LogHelper.log(log, e);
                }
                request.setAggregationLandChargesRegistry(DaoManager.get(AggregationLandChargesRegistry.class, item.getId()));

                saveAllDataRelatedToRequestOrNotify(request);
            }
        }
    }

    private void prepareRequestToSave() throws PersistenceBeanException, IllegalAccessException {
        DaoManager.getSession().clear();
        if (!ValidationHelper.isNullOrEmpty(getEntity().getId())) {
            getEntity().setRequestFormalities(DaoManager.load(RequestFormality.class, new Criterion[]{
                    Restrictions.eq("request.id", getEntity().getId())
            })
                    );
        }
    }

    /**
     * add only working days, without  Saturday and Sunday.
     *
     * @throws PersistenceBeanException work with DB
     * @throws IllegalAccessException   work with DB
     * @throws InstantiationException   work with DB
     */
    private void fillRequestExpirationDate() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!getEntity().isNew() && getEntity().getExpirationDate() != null) {
            return;
        }
        Long clientId;
        if (ValidationHelper.isNullOrEmpty(getSelectedBillingClientId())) {
            clientId = getSelectedClientId();
        } else {
            clientId = getSelectedBillingClientId();
        }
        ClientServiceInfo info = DaoManager.get(ClientServiceInfo.class, new Criterion[]{
                Restrictions.eq("client.id", clientId),
                Restrictions.eq("service.id", getSelectedServiceId())
        });
        Integer addDays;
        if (info == null || info.getDaysToExpire() == null) {
            addDays = 0;
        } else {
            addDays = info.getDaysToExpire();
        }
        Date dateToAdd;
        if (getMail() != null) {
            dateToAdd = getMail().getReceiveDate();
        } else {
            dateToAdd = getEntity().getCreateDate();
        }
        if (dateToAdd == null) {
            dateToAdd = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(dateToAdd);
        for (int i = 0; i < addDays; ) {
            c.add(Calendar.DATE, 1);
            if (c.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                i++;
            }
        }
        getEntity().setExpirationDate(c.getTime());
    }

    public void saveFiles(Request request, boolean beginTransaction) throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getAttachments())) {
            List<Document> resultDocuments = new LinkedList<>();
            if (!ValidationHelper.isNullOrEmpty(getEntity().getDistraintFormality())) {
                Document documentFormFormality = getEntity().getDistraintFormality().getDocument();
                if (!ValidationHelper.isNullOrEmpty(documentFormFormality)) {
                    Document document = getAttachments().stream().filter(a -> !ValidationHelper.isNullOrEmpty(a.getId())
                            && a.getId().equals(documentFormFormality.getId())).findFirst().orElse(null);
                    if (!ValidationHelper.isNullOrEmpty(document)) {
                        getAttachments().remove(document);
                        resultDocuments.add(documentFormFormality);
                    }
                }
            }
            for (Document tempDocument : getAttachments()) {
                if (!Hibernate.isInitialized(tempDocument.getFormality())) {
                    tempDocument.setFormality(DaoManager.load(Formality.class, new Criterion[]{
                            Restrictions.eq("document.id", tempDocument.getId())
                    }));
                }
                if (ValidationHelper.isNullOrEmpty(tempDocument.getRequest())) {
                    tempDocument.setRequest(request);
                    saveFile(tempDocument, beginTransaction);
                    resultDocuments.add(tempDocument);
                } else {
                    try {
                        if (!tempDocument.getRequest().getId().equals(request.getId())) {
                            Document document = tempDocument.clone();
                            document.setRequest(request);
                            saveFile(document, beginTransaction);
                            resultDocuments.add(document);
                        } else {
                            resultDocuments.add(tempDocument);
                        }
                    } catch (CloneNotSupportedException e) {
                        LogHelper.log(log, e);
                    }
                }
            }
            request.setDocuments(resultDocuments);
        }
        DaoManager.save(request, beginTransaction);
    }

    public void cancelSaveFile() {
        this.setDocument(null);
        this.setDocumentTitle(null);
        this.setDocumentDate(null);
    }

    public void cancelSaveAttachment() {
        this.setAttachment(null);
    }

    public void saveFile(Document document, boolean beginTransaction) throws PersistenceBeanException {

        String filePath = GeneralFunctionsHelper.saveUploadedFile(document.getNameByPathOrTitle(), null,
                document.getUploadedDocumentContent());

        if (ValidationHelper.isNullOrEmpty(document.getPath())) {
            document.setPath(filePath);
        }
        document.setTypeId(DocumentType.ALLEGATI.getId());
        DaoManager.save(document, beginTransaction);
    }

    public void fillUploadedFileDocumentList() {

        Document newDocument = new Document();
        newDocument.setCreateDate(getDocumentDate());
        if (!ValidationHelper.isNullOrEmpty(getDocumentTitle())) {
            newDocument.setTitle(getDocumentTitle());
        } else {
            newDocument.setTitle(getDocument().getFileName());
        }
        newDocument.setUploadedDocumentContent(getDocument().getContents());
        newDocument.setUploadedDocumentFileName(getDocument().getFileName());

        getDocuments().add(newDocument);

        this.setDocumentTitle(null);
        this.setDocumentDate(null);
    }

    public void fillUploadedFileAttachmentsList() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (ValidationHelper.isNullOrEmpty(getAttachment())) {
            return;
        }

        UploadDocumentWrapper wrapper = GeneralFunctionsHelper.handleFileUpload(getAttachment().getFileName(),
                getAttachment().getContents(), DocumentType.FORMALITY.getId(),
                getAttachment().getFileName(), new Date(), null, null, DaoManager.getSession());

        Document newAttachment = wrapper.getDocument();

        newAttachment.setTitle(getAttachment().getFileName());
        newAttachment.setUploadedDocumentContent(getAttachment().getContents());
        newAttachment.setUploadedDocumentFileName(getAttachment().getFileName());

        getAttachments().add(newAttachment);

        fillFieldsRelatedInRequest();
    }

    private void fillFieldsRelatedInRequest() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Document> documentList = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.or(
                        Restrictions.eq("title", getAttachment().getFileName()),
                        Restrictions.like("path", getAttachment().getFileName(), MatchMode.ANYWHERE))
        });

        documentList.forEach(DaoManager::refresh);

        if (!ValidationHelper.isNullOrEmpty(documentList)) {
            List<Formality> formalityList = documentList.stream().map(Document::getFormality)
                    .flatMap(List::stream).collect(Collectors.toList());

            if (!ValidationHelper.isNullOrEmpty(formalityList)) {
                Optional<Formality> formality = formalityList.stream()
                        .max((o1, o2) -> o1.getDocument().getCreateDate().compareTo(o2.getCreateDate()));

                if (formality.isPresent()) {
                    setDistraintFormality(formality.get());
                    setAggregationLand(formality.get());
                }
            }
        }
    }

    private void setAggregationLand(Formality formality) throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        String name = null;

        if (!ValidationHelper.isNullOrEmpty(formality.getReclamePropertyService())) {
            name = formality.getReclamePropertyService().getName();
        } else if (!ValidationHelper.isNullOrEmpty(formality.getProvincialOffice())) {
            name = formality.getProvincialOffice().getName();
        }

        log.info("LandChargesRegistry name is " + name);

        if (!ValidationHelper.isNullOrEmpty(name)) {
            AggregationLandChargesRegistry registry = DaoManager.get(AggregationLandChargesRegistry.class,
                    new Criterion[]{Restrictions.eq("name", name)});
            if (ValidationHelper.isNullOrEmpty(registry)) {
                LandChargesRegistry landChargesRegistry = DaoManager.get(LandChargesRegistry.class, new Criterion[]{
                        Restrictions.eq("name", name)
                });
                if (!ValidationHelper.isNullOrEmpty(landChargesRegistry)
                        && !ValidationHelper.isNullOrEmpty(landChargesRegistry.getAggregationLandChargesRegistries())) {
                    registry = landChargesRegistry.getAggregationLandChargesRegistries().get(0);
                }
            }
            getEntity().setAggregationLandChargesRegistry(registry);
        }
    }

    public void openDistraintFormalityDialog() {
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", false);
        options.put("draggable", false);
        options.put("modal", true);
        options.put("contentHeight", 900);
        options.put("contentWidth", 1700);
        SessionHelper.put("requestEditDistraintFormalityDialog", Boolean.TRUE);
        SessionHelper.put("listProperties", Boolean.TRUE);
        SessionHelper.put("editedRequestId", getEntity().getId());
        if (!ValidationHelper.isNullOrEmpty(getDistraintFormality())) {
            SessionHelper.put("distraintFormalityId", getDistraintFormality().getId());
        }
        SessionHelper.put("fromRequestEdit", Boolean.TRUE);
        RequestContext.getCurrentInstance().openDialog(PageTypes.REQUEST_FORMALITY_CREATE.getPage(), options, null);
    }

    public void onDistraintFormalityDialogClose(SelectEvent event) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        Formality formality = (Formality) event.getObject();
        if (formality != null && !formality.isNew()) {
            setDistraintFormality(formality);
            setAggregationLand(formality);
        }
    }

    public void searchFormalitiesForAssociation() throws PersistenceBeanException, IllegalAccessException {
        List<Criterion> criteria = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getSearchFormalityRG())) {
            criteria.add(Restrictions.eq("generalRegister", getSearchFormalityRG()));
        }
        if (!ValidationHelper.isNullOrEmpty(getSearchFormalityRP())) {
            criteria.add(Restrictions.eq("particularRegister", getSearchFormalityRP()));
        }
        if (!ValidationHelper.isNullOrEmpty(getSearchFormalityDate())) {
            criteria.add(Restrictions.eq("presentationDate", getSearchFormalityDate()));
        }
        if (!ValidationHelper.isNullOrEmpty(getSearchFormalityAggregationId())) {
            criteria.add(Restrictions.or(
                    Restrictions.eq("reclamePropertyService.id", getSearchFormalityAggregationId()),
                    Restrictions.eq("provincialOffice.id", getSearchFormalityAggregationId())));
        }
        if (!ValidationHelper.isNullOrEmpty(getDistraintFormality())) {
            criteria.add(Restrictions.ne("id", getDistraintFormality().getId()));
        }
        if (!ValidationHelper.isNullOrEmpty(criteria)) {
            List<Formality> formalities = DaoManager.load(Formality.class, criteria.toArray(new Criterion[0]));

            setSearchedFormalityList(formalities);
        }
    }

    public void associateFormalityToRequest() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedSearchedFormality())) {
            setDistraintFormality(getSelectedSearchedFormality());
            setAggregationLand(getSelectedSearchedFormality());
            RequestContext.getCurrentInstance().update("distraintFormalityTable");

        }
        cleanAssociateDialogParameters();
    }

    private void cleanAssociateDialogParameters() {
        setSearchFormalityRG(null);
        setSearchFormalityDate(null);
        setSearchFormalityRP(null);
        setSearchFormalityAggregationId(null);
        setSelectedSearchedFormality(null);
        if (getSearchedFormalityList() != null) {
            getSearchedFormalityList().clear();
        }
    }

    public void deleteDocument() throws PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getDeleteDocumentFileName())
                && !ValidationHelper.isNullOrEmpty(getDocuments())) {
            for (int i = 0; i < getDocuments().size(); ++i) {
                if (getDeleteDocumentFileName().equals(getDocuments().get(i).getTitle())) {
                    Document document = getDocuments().get(i);
                    getDocuments().remove(i);
                    if (!ValidationHelper.isNullOrEmpty(document.getRequest())) {
                        document.setRequest(null);
                        DaoManager.save(document);
                    }
                    //  DaoManager.remove(document, true);
                    setDeleteDocumentId(null);
                    setDeleteDocumentFileName(null);
                    return;
                }
            }
        }
    }

    public void downloadPdfFile(Document document) {
        String title = "";
        if (!ValidationHelper.isNullOrEmpty(document.getTitle())) {
            if (document.getTitle().endsWith(".pdf")) {
                title = document.getTitle();
            } else {
                title = document.getTitle() + ".pdf";
            }
        }
        try {
            if (!ValidationHelper.isNullOrEmpty(document.getPath())) {

                FileHelper.sendFile(title, FileHelper.loadContentByPath(document.getPath()));
            } else {
                FileHelper.sendFile(title, FileHelper.loadContentByPath(GeneralFunctionsHelper.
                        saveUploadedFile(document.getNameByPathOrTitle(), null, document.getUploadedDocumentContent())));
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void reactivateRequest() {
        getEntity().setStateId(RequestState.IN_WORK.getId());
    }

    public void generatePdf() {
        RedirectHelper.goTo(PageTypes.REQUEST_TEXT_EDIT, getEntity().getId());
    }

    private void createHistory(Request request, boolean beginTransaction) throws HibernateException, PersistenceBeanException {
        if (RequestHelper.isDifferent(getStartStateId(), request.getStateId())
                || RequestHelper.isDifferent(getStartUserId(),
                        request.getUser() == null ? null : request.getUser().getId())) {
            RequestHistory history = new RequestHistory();

            history.setDate(new Date());
            history.setRequest(request);
            history.setStateId(request.getStateId());
            history.setUser(request.getUser());

            if (RequestState.SUSPENDED.getId().equals(request.getStateId())) {
                history.setReason(getReason());
            } else if (RequestState.IN_WORK.getId().equals(request.getStateId())) {
                history.setReason(getReactivateReason());
            } else {
                history.setReason(getReasonUnsuspend());
            }

            DaoManager.save(history, beginTransaction);
        }
    }

    public void changeUser() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedUserId())) {
            getEntity().setUser(DaoManager.get(User.class, getSelectedUserId()));
            getEntity().setStateId(RequestState.IN_WORK.getId());
        }
    }

    public void suspendRequest() {
        this.getEntity().setStateId(RequestState.SUSPENDED.getId());
    }

    public void unsuspendRequest() throws IllegalAccessException, PersistenceBeanException {
        List<RequestHistory> histories = DaoManager.load(RequestHistory.class, new Criterion[]{
                Restrictions.eq("request.id", getEntity().getId() == null ? 0L : getEntity().getId())
        }, Order.desc("id"));

        if (histories != null) {
            for (RequestHistory history : histories) {
                if (!RequestState.SUSPENDED.getId().equals(history.getStateId())) {
                    getEntity().setStateId(history.getStateId());
                    return;
                }
            }
        }
    }

    public void printPDF() throws PersistenceBeanException, IllegalAccessException {
        fillTemplates();

        if (!ValidationHelper.isNullOrEmpty(getTemplates())) {
            if (getTemplates().size() == 1) {
                setSelectedTemplateId((Long) getTemplates().get(0).getValue());
                generate();
            } else {
                executeJS("PF('templates').show();");
            }
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    ResourcesHelper.getValidation("warning"),
                    ResourcesHelper.getValidation("noDocumentTemplates"));
        }
    }

    public void generate() throws PersistenceBeanException, IllegalAccessException {
        GeneralFunctionsHelper.showReport(getEntity(), getSelectedTemplateId(),
                getCurrentUser(), false, DaoManager.getSession());
    }

    private void fillTemplates() {
        if (getEntity().getType() != null) {
            try {
                setTemplates(GeneralFunctionsHelper.fillTemplates(
                        DocumentGenerationPlaces.REQUEST_MANAGEMENT,
                        getEntity().getType(), null, DaoManager.getSession()));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public boolean getCanEdit() {
        try {
            return AccessBean.canEditInPage(this.getCurrentPage());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return false;
    }

    public boolean getIsClientHasArea() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(this.getSelectedClientId())) {
            Client client = DaoManager.get(Client.class, new Criterion[]{
                    Restrictions.eq("id", getSelectedClientId())});
            if (!ValidationHelper.isNullOrEmpty(client.getArea())) {
                setAreaOfClient(client.getArea().getDescription());
                if (!ValidationHelper.isNullOrEmpty(client.getOffice())) {
                    setOfficeOfClient(client.getOffice().getDescription());
                } else {
                    setOfficeOfClient(null);
                }
                return true;
            }
        }
        return false;
    }

    public void editSubject() {
        setPerformEdit(true);
    }

    @Override
    public void afterSave() {
        if (getShouldBeRedirected())
            super.afterSave();
    }

    public void changeShouldBeRedirected(Boolean value) {
        setShouldBeRedirected(value);
        afterSave();
    }

    private void saveAllDataRelatedToRequestOrNotify(Request entity) throws PersistenceBeanException,
    InstantiationException, IllegalAccessException {
        getWrapper().saveFields(entity);
        saveMultipleSubjects(entity);
        List<Request> sameRequestList = getSameRequestsIfExist(entity);
        if (!findAppropriateRequest(sameRequestList)) {
            saveToDB(entity, false);
        } else {
            setShouldBeRedirected(false);
            notifyUser();
        }
    }

    private void saveMultipleSubjects(Request request) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getSubjectWrapperList())) {
            if (ValidationHelper.isNullOrEmpty(request.getSubjectList())) {
                request.setSubjectList(new LinkedList<>());
            } else {
                List<Subject> subjectList = request.getSubjectList().stream()
                        .filter(s -> getSubjectWrapperList().stream().anyMatch(
                                sw -> s.getId().equals(sw.getId()))).collect(Collectors.toList());
                request.setSubjectList(subjectList);
                getSubjectWrapperList().removeIf(sw -> !ValidationHelper.isNullOrEmpty(sw.getId()));
            }

            for (SubjectWrapper sbjWrp : getSubjectWrapperList()) {
                Subject subject = new Subject();
                subject.setId(sbjWrp.getId());
                subject.setTypeId(sbjWrp.getTypeId());

                if (sbjWrp.getProvinceId().equals(Province.FOREIGN_COUNTRY_ID)) {
                    subject.setCountry(DaoManager.get(Country.class,
                            sbjWrp.getTypeIsPhysicalPerson() ?
                                    sbjWrp.getSelectedNationId() : sbjWrp.getSelectedJuridicalNationId()));
                } else {
                    if (!ValidationHelper.isNullOrEmpty(sbjWrp.getBirthCity())) {
                        subject.setBirthCity(sbjWrp.getBirthCity());
                    } else {
                        subject.setBirthCity(DaoManager.get(City.class, sbjWrp.getCityId()));
                    }
                    subject.setBirthProvince(DaoManager.get(Province.class, sbjWrp.getProvinceId()));
                }

                subject.setNumberVAT(sbjWrp.getNumberVAT());
                subject.setFiscalCode(sbjWrp.getFiscalCode());
                if (sbjWrp.getTypeIsPhysicalPerson()) {
                    subject.setSex(sbjWrp.getSelectedSexTypeId());
                    subject.setName(sbjWrp.getName());
                    subject.setSurname(sbjWrp.getSurname());
                    subject.setBirthDate(sbjWrp.getBirthDate());
                    if (ValidationHelper.isNullOrEmpty(subject.getNumberVAT())) {
                        subject.setNumberVAT(sbjWrp.getFiscalCode());
                    }
                } else {
                    subject.setBusinessName(sbjWrp.getBusinessName());
                    subject.setNumberVAT(sbjWrp.getNumberVAT());
                    if (ValidationHelper.isNullOrEmpty(subject.getFiscalCode())) {
                        subject.setFiscalCode(sbjWrp.getNumberVAT());
                    }
                }
                Subject subjectFromDB = SubjectHelper.getSubjectIfExists(subject, sbjWrp.getTypeIsPhysicalPerson() ? SubjectType.PHYSICAL_PERSON.getId() : SubjectType.LEGAL_PERSON.getId());
                if (subjectFromDB != null) {
                    boolean isRequestSubjectExist = request.getSubjectList().stream()
                            .anyMatch(sw -> sw.getId().equals(subjectFromDB.getId()));
                    if(!isRequestSubjectExist) {
                        request.getSubjectList().add(subjectFromDB);
                        request.getSubjectTypeMapping().put(subjectFromDB.getId(),sbjWrp.getSectionCType());
                    }
                    if (!ValidationHelper.isNullOrEmpty(subject.getId())) {
                        DaoManager.getSession().evict(DaoManager.getSession().get(Subject.class, subject.getId()));
                    }
                    subject.setId(subjectFromDB.getId());
                }
                else {
                    DaoManager.save(subject);    
                    request.getSubjectList().add(subject);
                    request.getSubjectTypeMapping().put(subject.getId(),sbjWrp.getSectionCType());
                }
                getEntity().addRequestSubject(subject, DaoManager.getSession());
            }
        }
    }

    public void saveToDB(Request request, boolean beginTransaction) throws PersistenceBeanException, IllegalAccessException {
        DaoManager.save(request, beginTransaction);
        createHistory(request, beginTransaction);
        saveFiles(request, beginTransaction);
        if(getRedirected() != null)
            setShouldBeRedirected(getRedirected());
        else
            setShouldBeRedirected(true);
        afterSave();
    }

    private void notifyUser() throws PersistenceBeanException, IllegalAccessException {
        String notification = "";
        if (!RequestState.EVADED.getId().equals(getSameRequest().getStateId())) {
            notification = String.format(ResourcesHelper.getString("requestExistsNotification"),
                    getSameRequest().getCreateDateStr(), getSameRequest().getClient());
        } else if (RequestState.EVADED.getId().equals(getSameRequest().getStateId())) {
            String context = "";

            List<RequestConservatory> requestConservatoriesDB = DaoManager.load(RequestConservatory.class, new Criterion[]{
                    Restrictions.eq("request.id", getSameRequest().getId())
            });
            if (!ValidationHelper.isNullOrEmpty(requestConservatoriesDB)) {
                Comparator<RequestConservatory> comparator = Comparator.comparing(RequestConservatory::getConservatoryDate);
                RequestConservatory conservatory = requestConservatoriesDB.stream().min(comparator).get();

                context = " ed aggiornata in data" + conservatory.getConservatoryDateStr();
            }

            if (ValidationHelper.isNullOrEmpty(getEntity().getId()) && Boolean.TRUE.equals(getEntity().getService().getIsUpdate())) {
                notification = String.format(ResourcesHelper.getString("requestExistsNotificationEvadedAndServiceUpdate"),
                        context);
                setServiceUpdate(true);
            } else {
                notification = String.format(ResourcesHelper.getString("requestExistsNotificationEvaded"),
                        context);
            }
        }

        setNotificationUser(notification);
        RequestContext.getCurrentInstance().update("notifyUserDialog");
        executeJS("PF('notifyUserDialogWV').show();");
    }

    private List<Request> getSameRequestsIfExist(Request request) throws PersistenceBeanException, IllegalAccessException {
        List<Request> result = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(request.getClient())
                && !ValidationHelper.isNullOrEmpty(request.getSubject())
                && !ValidationHelper.isNullOrEmpty(request.getService())
                && (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())
                        || !ValidationHelper.isNullOrEmpty(request.getCity()))) {

            List<Criterion> restrictionsList = new ArrayList<>();

            restrictionsList.add(Restrictions.eq("client", request.getClient()));
            restrictionsList.add(Restrictions.eq("subject", request.getSubject()));
            restrictionsList.add(Restrictions.eq("service", request.getService()));
            restrictionsList.add(Restrictions.eq("notary", request.getNotary()));

            restrictionsList.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                    Restrictions.isNull("isDeleted")));

            if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())) {
                restrictionsList.add(Restrictions.eq("aggregationLandChargesRegistry",
                        request.getAggregationLandChargesRegistry()));
            } else {
                restrictionsList.add(Restrictions.eq("city", request.getCity()));
            }

            if (!ValidationHelper.isNullOrEmpty(request.getId())) {
                restrictionsList.add(Restrictions.ne("id", request.getId()));
            }
            result = DaoManager.load(Request.class, restrictionsList.toArray(new Criterion[0]));
        }
        return result;
    }

    private boolean findAppropriateRequest(List<Request> requestList) throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(requestList)) {
            setAllEvaded(true);

            for (Request item : requestList) {
                if (!RequestState.EVADED.getId().equals(item.getStateId())) {
                    setAllEvaded(false);
                    break;
                }
            }
            List<Service> services = DaoManager.load(Service.class);
            List<Service> servicesForUpdate = services.stream().map(Service::getServiceForUpdate).collect(Collectors.toList());
            if (getAllEvaded() && !servicesForUpdate.contains(getEntity().getService())) {
                return false;
            }

            Comparator<Request> comparator;
            if (!getAllEvaded()) {
                comparator = Comparator.comparing(Request::getCreateDate);
            } else if (getAllEvaded() && ValidationHelper.isNullOrEmpty(getEntity().getId())
                    && Boolean.TRUE.equals(getEntity().getService().getIsUpdate())) {
                comparator = Comparator.comparing(Request::getEvasionDate);
            } else {
                comparator = Comparator.comparing(Request::getEvasionDate);
            }

            Request maxDateRequest = requestList.stream().max(comparator).get();
            setSameRequest(maxDateRequest);
            return true;
        }
        return false;
    }

    public Boolean getIsSuspend() {
        return RequestState.SUSPENDED.getId().equals(getEntity().getStateId());
    }

    public Boolean getIsSubject() {
        return getEntity().getType() == null || RequestEnumTypes.SUBJECT.equals(this.getEntity().getType());
    }

    public Boolean getIsPhysicalPerson() {
        return getIsSubject() && (this.getEntity().getSubjectTypeId() == null
                || SubjectType.PHYSICAL_PERSON.getId()
                .equals(this.getEntity().getSubjectTypeId()));
    }

    public Boolean getIsLegalPerson() {
        return getIsSubject() && (this.getEntity().getSubjectTypeId() != null
                && SubjectType.LEGAL_PERSON.getId()
                .equals(this.getEntity().getSubjectTypeId()));
    }

    public Boolean getIsBuilding() {
        return !getIsSubject() && (this.getEntity().getPropertyTypeId() == null
                || RealEstateType.BUILDING.getId()
                .equals(this.getEntity().getPropertyTypeId()));
    }

    public void openEstateLocation() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {

        if(getEntity().getService() != null 
                && getEntity().getService().getRequestOutputType().equals(RequestOutputTypes.XML)) {

            if(getEntity().getTranscriptionActId() == null) {
                Formality formality = new Formality();
                List<Request> requestForcedList = new ArrayList<Request>();
                requestForcedList.add(this.getEntity());
                formality.setRequestForcedList(requestForcedList);
                DaoManager.save(formality,true);
                
                SectionA sectionA = new SectionA();
                String comapnyName = ApplicationSettingsHolder.getInstance()
                        .getByKey(ApplicationSettingsKeys.COMPANY_NAME).getValue();

                if(ValidationHelper.isNullOrEmpty(comapnyName))
                    comapnyName = "";

                String applicant = comapnyName.toUpperCase() + " PER ";
                if(!ValidationHelper.isNullOrEmpty(getEntity()) 
                        && !ValidationHelper.isNullOrEmpty(getEntity().getClient())) {
                    applicant = applicant + getEntity().getClientName().toUpperCase();
                }
                sectionA.setApplicant(applicant);

                String fiscalCodeAppliant = ApplicationSettingsHolder.getInstance()
                        .getByKey(ApplicationSettingsKeys.FISCAL_CODE).getValue();
                if(!ValidationHelper.isNullOrEmpty(fiscalCodeAppliant))
                    fiscalCodeAppliant = fiscalCodeAppliant.toUpperCase();
                sectionA.setFiscalCodeAppliant(fiscalCodeAppliant);

                String addressAppliant = ApplicationSettingsHolder.getInstance()
                        .getByKey(ApplicationSettingsKeys.ADDRESS).getValue();
                if(!ValidationHelper.isNullOrEmpty(addressAppliant))
                    addressAppliant = addressAppliant.toUpperCase();
                sectionA.setAddressAppliant(addressAppliant);
                
                sectionA.setFormality(formality);
                DaoManager.save(sectionA,true);
                
                List<RequestSubject> distinctByType = 
                        getEntity().getRequestSubjects().stream().
                        filter(rs -> !ValidationHelper.isNullOrEmpty(
                                rs.getType()))
                        .filter(ListHelper.distinctByKey(rs -> rs.getType()))
                        .collect(Collectors.toList());
                for(RequestSubject requestSubject : distinctByType) {
                    SectionC sectionC = new SectionC();
                    sectionC.setSectionCType(requestSubject.getType());
                    DaoManager.save(sectionC,true);
                    sectionC.setFormality(formality);
                    sectionC.setSubject(new ArrayList<Subject>());
                    sectionC.getSubject().add(requestSubject.getSubject());
                    DaoManager.save(sectionC,true);
                }

                this.getEntity().setTranscriptionActId(formality);
                setRedirected(Boolean.FALSE);
                this.pageSave();
                setRedirected(null);
            }
            SessionHelper.put("requestViewFormality", Boolean.TRUE);

            Request request =  DaoManager.get(Request.class,new CriteriaAlias[]{
                    new CriteriaAlias("transcriptionActId", "transcriptionActId", JoinType.INNER_JOIN)
            },
                    new Criterion[]{
                            Restrictions.eq("id", this.getEntity().getId())
            });

            SessionHelper.put("transcriptionActId", request.getTranscriptionActId().getId());
            SessionHelper.put("editRequestId", this.getEntity().getId());
            viewFormality();
        } else {
            RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_LIST, getEntityId());
        }
    }

    public void viewFormality() {
        RedirectHelper.goToOnlyView(PageTypes.REQUEST_FORMALITY, this.getEntity().getTranscriptionActId().getId());
    }

    public void validateMultipleRequest() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        boolean isShowConfirm = false;
        if (multipleCreate && getEntity().isNew()) {
            List<Request> listRequestBySubject = DaoManager.load(Request.class, new CriteriaAlias[]{
                            new CriteriaAlias("subject", "subject", JoinType.INNER_JOIN)},
                    new Criterion[]{
                            Restrictions.eq("subject.fiscalCode", getSubject().getFiscalCode())
                    }, Order.desc("createDate"));
            for(Request req : emptyIfNull(listRequestBySubject)){
                if(!ValidationHelper.isNullOrEmpty(getSelectedServiceIds())){
                    isShowConfirm = emptyIfNull(req.getMultipleServices()).stream().anyMatch(s -> getSelectedServiceIds().contains(s.getId().toString()));
                    if(isShowConfirm)
                        break;
                }
            }
            if(isShowConfirm){
                setMultipleReqMessage(String
                        .format(ResourcesHelper.getString("multipleRequestConfirm"), DateTimeHelper.toFormatedString( listRequestBySubject.get(0).getCreateDate(),
                                DateTimeHelper.getDatePatternWithSeconds())));
            }
        }
        if (isShowConfirm) {
            executeJS("PF('multipleRequestSave').show();");
        } else {
            pageSave();
        }
    }


    public Long getSelectedClientId() {
        return selectedClientId;
    }

    public void setSelectedClientId(Long selectedClientId) {
        this.selectedClientId = selectedClientId;
    }

    public List<SelectItem> getClients() {
        return clients;
    }

    public void setClients(List<SelectItem> clients) {
        this.clients = clients;
    }

    public Long getSelectedRequestTypeId() {
        return selectedRequestTypeId;
    }

    public void setSelectedRequestTypeId(Long selectedRequestTypeId) {
        this.selectedRequestTypeId = selectedRequestTypeId;
    }

    public List<SelectItem> getRequestTypes() {
        return requestTypes;
    }

    public void setRequestTypes(List<SelectItem> requestTypes) {
        this.requestTypes = requestTypes;
    }

    public Long getSelectedServiceId() {
        return selectedServiceId;
    }

    public void setSelectedServiceId(Long selectedServiceId) {
        this.selectedServiceId = selectedServiceId;
    }

    public List<SelectItem> getServices() {
        return services;
    }

    public void setServices(List<SelectItem> services) {
        this.services = services;
    }

    public Long getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(Long selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public List<SelectItem> getUsers() {
        return users;
    }

    public void setUsers(List<SelectItem> users) {
        this.users = users;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReasonUnsuspend() {
        return reasonUnsuspend;
    }

    public void setReasonUnsuspend(String reasonUnsuspend) {
        this.reasonUnsuspend = reasonUnsuspend;
    }

    public String getReactivateReason() {
        return reactivateReason;
    }

    public void setReactivateReason(String reactivateReason) {
        this.reactivateReason = reactivateReason;
    }

    public Boolean getPerformEdit() {
        return performEdit;
    }

    public void setPerformEdit(Boolean performEdit) {
        this.performEdit = performEdit;
    }

    public MenuModel getTopMenuModel() {
        return topMenuModel;
    }

    public void setTopMenuModel(MenuModel topMenuModel) {
        this.topMenuModel = topMenuModel;
    }

    public List<InputCard> getInputCardList() {
        return inputCardList;
    }

    public void setInputCardList(List<InputCard> inputCardList) {
        this.inputCardList = inputCardList;
    }

    public int getActiveMenuTabNum() {
        return activeMenuTabNum;
    }

    public void setActiveMenuTabNum(int activeMenuTabNum) {
        this.activeMenuTabNum = activeMenuTabNum;
    }

    public Long getSelectedRequestEnumTypeFirst() {
        return selectedRequestEnumTypeFirst;
    }

    public void setSelectedRequestEnumTypeFirst(Long selectedRequestEnumTypeFirst) {
        this.selectedRequestEnumTypeFirst = selectedRequestEnumTypeFirst;
    }

    public List<SelectItem> getRequestEnumTypesFirst() {
        return requestEnumTypesFirst;
    }

    public void setRequestEnumTypesFirst(List<SelectItem> requestEnumTypesFirst) {
        this.requestEnumTypesFirst = requestEnumTypesFirst;
    }

    public Long getSelectedRequestEnumTypeSecond() {
        return selectedRequestEnumTypeSecond;
    }

    public void setSelectedRequestEnumTypeSecond(Long selectedRequestEnumTypeSecond) {
        this.selectedRequestEnumTypeSecond = selectedRequestEnumTypeSecond;
    }

    public List<SelectItem> getRequestEnumTypesSecond() {
        return requestEnumTypesSecond;
    }

    public void setRequestEnumTypesSecond(List<SelectItem> requestEnumTypesSecond) {
        this.requestEnumTypesSecond = requestEnumTypesSecond;
    }

    public RequestWrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(RequestWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public Date getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(Date documentDate) {
        this.documentDate = documentDate;
    }

    public UploadedFile getDocument() {
        return document;
    }

    public void setDocument(UploadedFile document) {
        this.document = document;
    }

    public Long getDeleteDocumentId() {
        return deleteDocumentId;
    }

    public void setDeleteDocumentId(Long deleteDocumentId) {
        this.deleteDocumentId = deleteDocumentId;
    }

    public Request getDbRequest() {
        return dbRequest;
    }

    public void setDbRequest(Request dbRequest) {
        this.dbRequest = dbRequest;
    }

    public Document getXmlDocument() {
        return xmlDocument;
    }

    public void setXmlDocument(Document xmlDocument) {
        this.xmlDocument = xmlDocument;
    }

    public List<Subject> getXmlSubjects() {
        return xmlSubjects;
    }

    public void setXmlSubjects(List<Subject> xmlSubjects) {
        this.xmlSubjects = xmlSubjects;
    }

    public File getXmlFile() {
        return xmlFile;
    }

    public void setXmlFile(File xmlFile) {
        this.xmlFile = xmlFile;
    }

    public List<RequestViewWrapper> getShownFields() {
        return shownFields;
    }

    public void setShownFields(List<RequestViewWrapper> shownFields) {
        this.shownFields = shownFields;
    }

    public WLGInbox getMail() {
        return mail;
    }

    public void setMail(WLGInbox mail) {
        this.mail = mail;
    }

    public Long getSelectedOldRequestId() {
        return selectedOldRequestId;
    }

    public void setSelectedOldRequestId(Long selectedOldRequestId) {
        this.selectedOldRequestId = selectedOldRequestId;
    }

    public Long getStartStateId() {
        return startStateId;
    }

    public void setStartStateId(Long startStateId) {
        this.startStateId = startStateId;
    }

    public Long getStartUserId() {
        return startUserId;
    }

    public void setStartUserId(Long startUserId) {
        this.startUserId = startUserId;
    }

    public Long getSelectedTemplateId() {
        return selectedTemplateId;
    }

    public void setSelectedTemplateId(Long selectedTemplateId) {
        this.selectedTemplateId = selectedTemplateId;
    }

    public List<SelectItem> getTemplates() {
        return templates;
    }

    public void setTemplates(List<SelectItem> templates) {
        this.templates = templates;
    }

    public List<SelectItem> getAgencyList() {
        return agencyList;
    }

    public void setAgencyList(List<SelectItem> agencyList) {
        this.agencyList = agencyList;
    }

    public Long getSelectedAgencyId() {
        return selectedAgencyId;
    }

    public void setSelectedAgencyId(Long selectedAgencyId) {
        this.selectedAgencyId = selectedAgencyId;
    }

    public boolean getHasAgencyList() {
        return hasAgencyList;
    }

    public void setHasAgencyList(boolean hasAgencyList) {
        this.hasAgencyList = hasAgencyList;
    }

    public List<SelectItem> getAgencyOfficeList() {
        return agencyOfficeList;
    }

    public void setAgencyOfficeList(List<SelectItem> agencyOfficeList) {
        this.agencyOfficeList = agencyOfficeList;
    }

    public boolean getHasAgencyOfficeList() {
        return hasAgencyOfficeList;
    }

    public void setHasAgencyOfficeList(boolean hasAgencyOfficeList) {
        this.hasAgencyOfficeList = hasAgencyOfficeList;
    }

    public Long getSelectedAgencyOfficeId() {
        return selectedAgencyOfficeId;
    }

    public void setSelectedAgencyOfficeId(Long selectedAgencyOfficeId) {
        this.selectedAgencyOfficeId = selectedAgencyOfficeId;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Residence getResidence() {
        return residence;
    }

    public void setResidence(Residence residence) {
        this.residence = residence;
    }

    public Residence getDomicile() {
        return domicile;
    }

    public void setDomicile(Residence domicile) {
        this.domicile = domicile;
    }

    public boolean isMultipleCreate() {
        return multipleCreate;
    }

    public void setMultipleCreate(boolean multipleCreate) {
        this.multipleCreate = multipleCreate;
    }

    public List<String> getSelectedServiceIds() {
        return selectedServiceIds;
    }

    public void setSelectedServiceIds(List<String> selectedServiceIds) {
        this.selectedServiceIds = selectedServiceIds;
    }

    public String getMultipleTabPath() {
        return multipleTabPath;
    }

    public void setMultipleTabPath(String multipleTabPath) {
        this.multipleTabPath = multipleTabPath;
    }

    public Long getSelectedBillingClientId() {
        return selectedBillingClientId;
    }

    public void setSelectedBillingClientId(Long selectedBillingClientId) {
        this.selectedBillingClientId = selectedBillingClientId;
    }

    public List<SelectItem> getBillingClients() {
        return billingClients;
    }

    public void setBillingClients(List<SelectItem> billingClients) {
        this.billingClients = billingClients;
    }

    public Long getNewSelectedBillingClientId() {
        return newSelectedBillingClientId;
    }

    public void setNewSelectedBillingClientId(Long newSelectedBillingClientId) {
        this.newSelectedBillingClientId = newSelectedBillingClientId;
    }

    public List<Subject> getSubjectsToRestore() {
        return subjectsToRestore;
    }

    public void setSubjectsToRestore(List<Subject> subjectsToRestore) {
        this.subjectsToRestore = subjectsToRestore;
    }

    public Subject getSelectedSubjectToRestore() {
        return selectedSubjectToRestore;
    }

    public void setSelectedSubjectToRestore(Subject selectedSubjectToRestore) {
        this.selectedSubjectToRestore = selectedSubjectToRestore;
    }

    public String getAreaOfClient() {
        return areaOfClient;
    }

    public void setAreaOfClient(String areaOfClient) {
        this.areaOfClient = areaOfClient;
    }

    public String getOfficeOfClient() {
        return officeOfClient;
    }

    public void setOfficeOfClient(String officeOfClient) {
        this.officeOfClient = officeOfClient;
    }

    public List<SelectItemWrapper<Client>> getFiduciaryClients() {
        return fiduciaryClients;
    }

    public void setFiduciaryClients(List<SelectItemWrapper<Client>> fiduciaryClients) {
        this.fiduciaryClients = fiduciaryClients;
    }

    public List<SelectItemWrapper<Client>> getFiduciaryClientsSelected() {
        return fiduciaryClientsSelected;
    }

    public void setFiduciaryClientsSelected(List<SelectItemWrapper<Client>> fiduciaryClientsSelected) {
        this.fiduciaryClientsSelected = fiduciaryClientsSelected;
    }

    public String getRedirectFromMail() {
        return redirectFromMail;
    }

    public void setRedirectFromMail(String redirectFromMail) {
        this.redirectFromMail = redirectFromMail;
    }

    public String getDeleteDocumentFileName() {
        return deleteDocumentFileName;
    }

    public void setDeleteDocumentFileName(String deleteDocumentFileName) {
        this.deleteDocumentFileName = deleteDocumentFileName;
    }

    public Request getSameRequest() {
        return sameRequest;
    }

    public void setSameRequest(Request sameRequest) {
        this.sameRequest = sameRequest;
    }

    public String getNotificationUser() {
        return notificationUser;
    }

    public void setNotificationUser(String notificationUser) {
        this.notificationUser = notificationUser;
    }

    public Boolean getShouldBeRedirected() {
        return shouldBeRedirected;
    }

    public void setShouldBeRedirected(Boolean shouldBeRedirected) {
        this.shouldBeRedirected = shouldBeRedirected;
    }

    public Boolean getAllEvaded() {
        return allEvaded;
    }

    public void setAllEvaded(Boolean allEvaded) {
        this.allEvaded = allEvaded;
    }

    public Boolean getIsServiceUpdate() {
        return isServiceUpdate;
    }

    public void setServiceUpdate(Boolean serviceUpdate) {
        isServiceUpdate = serviceUpdate;
    }

    public List<SelectItem> getNotaryList() {
        return notaryList;
    }

    public void setNotaryList(List<SelectItem> notaryList) {
        this.notaryList = notaryList;
    }

    public Long getSelectedNotaryId() {
        return selectedNotaryId;
    }

    public void setSelectedNotaryId(Long selectedNotaryId) {
        this.selectedNotaryId = selectedNotaryId;
    }

    public List<Document> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Document> attachments) {
        this.attachments = attachments;
    }

    public UploadedFile getAttachment() {
        return attachment;
    }

    public void setAttachment(UploadedFile attachment) {
        this.attachment = attachment;
    }

    public SelectItemWrapperConverter<Client> getClientSelectItemWrapperConverter() {
        return clientSelectItemWrapperConverter;
    }

    public void setClientSelectItemWrapperConverter(SelectItemWrapperConverter<Client> clientSelectItemWrapperConverter) {
        this.clientSelectItemWrapperConverter = clientSelectItemWrapperConverter;
    }

    public Formality getDistraintFormality() {
        return distraintFormality;
    }

    public void setDistraintFormality(Formality distraintFormality) {
        this.distraintFormality = distraintFormality;
    }

    public List<SubjectWrapper> getSubjectWrapperList() {
        return subjectWrapperList;
    }

    public void setSubjectWrapperList(List<SubjectWrapper> subjectWrapperList) {
        this.subjectWrapperList = subjectWrapperList;
    }

    public SubjectWrapper getSubjectWrapper() {
        return subjectWrapper;
    }

    public void setSubjectWrapper(SubjectWrapper subjectWrapper) {
        this.subjectWrapper = subjectWrapper;
    }

    public List<Formality> getSearchedFormalityList() {
        return searchedFormalityList;
    }

    public void setSearchedFormalityList(List<Formality> searchedFormalityList) {
        this.searchedFormalityList = searchedFormalityList;
    }

    public Long getSearchFormalityAggregationId() {
        return searchFormalityAggregationId;
    }

    public void setSearchFormalityAggregationId(Long searchFormalityAggregationId) {
        this.searchFormalityAggregationId = searchFormalityAggregationId;
    }

    public String getSearchFormalityRG() {
        return searchFormalityRG;
    }

    public void setSearchFormalityRG(String searchFormalityRG) {
        this.searchFormalityRG = searchFormalityRG;
    }

    public Date getSearchFormalityDate() {
        return searchFormalityDate;
    }

    public void setSearchFormalityDate(Date searchFormalityDate) {
        this.searchFormalityDate = searchFormalityDate;
    }

    public String getSearchFormalityRP() {
        return searchFormalityRP;
    }

    public void setSearchFormalityRP(String searchFormalityRP) {
        this.searchFormalityRP = searchFormalityRP;
    }

    public Formality getSelectedSearchedFormality() {
        return selectedSearchedFormality;
    }

    public void setSelectedSearchedFormality(Formality selectedSearchedFormality) {
        this.selectedSearchedFormality = selectedSearchedFormality;
    }

    public List<SelectItem> getLandAggregations() {
        return landAggregations;
    }

    public void setLandAggregations(List<SelectItem> landAggregations) {
        this.landAggregations = landAggregations;
    }

    public Boolean getRedirected() {
        return redirected;
    }

    public void setRedirected(Boolean redirected) {
        this.redirected = redirected;
    }

    public String getDistraintFormalityTableNameValue() {
        String name = null;
        if(getDistraintFormality()!=null) {
            if(getDistraintFormality().getReclamePropertyService()!=null) {
                name = getDistraintFormality().getReclamePropertyService().getName();
            }
            if(name == null) {
                name = getDistraintFormality().getProvincialOfficeName();
            }
        }
        return name;
    }

    public String getMultipleReqMessage() {
        return multipleReqMessage;
    }

    public void setMultipleReqMessage(String multipleReqMessage) {
        this.multipleReqMessage = multipleReqMessage;
    }
}
