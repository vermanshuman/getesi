package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;

import it.nexera.ris.persistence.beans.entities.domain.*;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DualListModel;

import it.nexera.ris.common.enums.AgencyType;
import it.nexera.ris.common.enums.BillingTypeFields;
import it.nexera.ris.common.enums.ClientTitleType;
import it.nexera.ris.common.enums.ClientType;
import it.nexera.ris.common.enums.CostType;
import it.nexera.ris.common.enums.EmailType;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.SelectItemHelper;
import it.nexera.ris.common.helpers.SelectItemWrapperConverter;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.xml.wrappers.SelectItemWrapper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Area;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.ClientEmailWrapper;
import it.nexera.ris.web.beans.wrappers.logic.InvoiceColumnWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RequestTypeInvoiceColumnWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ServiceWrapper;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@ManagedBean(name = "clientEditBean")
@ViewScoped
public class ClientEditBean extends EntityEditPageBean<Client>
        implements Serializable {

    private static final String ONLY_VIEW_CLIENT = "ONLY_VIEW_CLIENT";

    private final Long FIXED_COST_TYPE_ID = CostType.FIXED_COST.getId();

    private final Long SALARY_COST_TYPE_ID = CostType.SALARY_COST.getId();

    private static final long serialVersionUID = 5266236796429350096L;

    private boolean renderMenu;

    private boolean subjectInvoice;

    private boolean hasAgency;

    private boolean hasAgencyOffice;

    private boolean hasHeadquarters;

    private boolean onlyView;

    private Boolean fromClientSearchList;

    private Boolean foreignCountry;

    private Long addressProvinceId;

    private Long addressCityId;

    private Long addressOperationalHeadquartersProvinceId;

    private Long addressOperationalHeadquartersCityId;

    private Long selectedServiceId;

    private Long clientIbanId;

    private List<SelectItem> ibans;

    private List<SelectItem> provinces;

    private List<SelectItem> addressCities;

    private List<SelectItem> operationalHeadquartersProvinces;

    private List<SelectItem> addressOperationalHeadquartersCities;

    private List<SelectItem> clientTypes;

    private List<SelectItem> clientTitleTypes;

    private List<SelectItem> countries;

    private List<SelectItemWrapper<Area>> areas;

    private List<SelectItemWrapper<Area>> selectedAreas;

    private List<SelectItemWrapper<Office>> offices;

    private List<SelectItemWrapper<Office>> selectedOffices;

    private List<Agency> agencies;

    private List<Agency> agencyOffices;

    private List<ServiceWrapper> services;

    private int tempAgencyId;

    private int editAgencyId;

    private ServiceWrapper selectedService;

    private Long selectedCountryId;

    private List<ClientEmailWrapper> personalEmails;

    private AtomicLong tempEmailId;

    private String newEmail;

    private String newEmailDescription;

    private Long emailDeleteId;

    private String editEmail;

    private String editEmailDescription;

    private Long emailEditId;

    private Boolean lastStep;

    private Long referentDeleteId;

    // I duplicate this field to fix issue ART_RISFW-420.
    // Field entity of EntityEditPageBean becomes empty in a strange way.
    private Client entity;

    private List<ClientView> billingRecipientTable;

    private List<SelectItem> billingRecipients;

    private List<ClientView> referentRecipientTable;

    private List<SelectItem> referentRecipients;

    private Long selectedClientId;

    private Long selectedReferentClientId;

    private SelectItemWrapper<Area> selectedArea;

    private SelectItemWrapper<Office> selectedOffice;

    private Long deleteClientId;

    private Boolean selectedCostOutput;

    private Boolean renderImportant;

    private Long maximumFormalities;

    private DualListModel<InvoiceColumnWrapper> invoiceColumns;

    private boolean clientFiduciaryOrManager;

    private List<Office> allOfficesList;

    private SelectItemWrapperConverter<Area> areaConverter;

    private SelectItemWrapperConverter<Office> officeConverter;

    private Long selectedLinkedClient;

    private boolean priceListDialogOnlyView;

    private List<SelectItem> nonManagerOrFiduciaryClientLists;

    private Long[] selectedNonManagerOrFiduciaryClientIds;

    private PriceList negativePriceList;

    private List<SelectItem> requestTypeNames;

    private Long selectedBillingRequestTypeId;

    private List<RequestTypeInvoiceColumnWrapper> requestTypeInvoiceColumns;

    private List<SelectItem> paymentTypes;

    private Long paymentTypeId;

    private List<SelectItem> taxRates;

    private Boolean fromContactList;

    private Long selectedClientIdToCopy;

    private List<SelectItem> clientsToCopy;

    private String emptyPriceListMessage;

    private List<PriceList> priceListToCopy;

    /*
     * (non-Javadoc)
     *
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException, IllegalAccessException {
        setEntity(super.getEntity());
        if (!ValidationHelper.isNullOrEmpty((Boolean) SessionHelper.get(ONLY_VIEW_CLIENT))
                && ((Boolean) SessionHelper.get(ONLY_VIEW_CLIENT))) {
            SessionHelper.removeObject(ONLY_VIEW_CLIENT);
            this.setOnlyView(true);
        }

        if (!ValidationHelper.isNullOrEmpty((Boolean) SessionHelper.get("REDIRECT_FROM_CONTACT_LIST"))
                && ((Boolean) SessionHelper.get("REDIRECT_FROM_CONTACT_LIST"))) {
            SessionHelper.removeObject("REDIRECT_FROM_CONTACT_LIST");
            this.setFromContactList(true);
        }

        if (this.getSession().get("fromPatientSearchList") == Boolean.TRUE) {
            setFromClientSearchList(Boolean.TRUE);
            this.setRenderMenu(false);
        } else {
            this.setRenderMenu(true);
        }

        if (this.getEntity() != null && !this.getEntity().isNew()) {
            this.fillFields();
        }

        if(requestTypeInvoiceColumns == null)
            requestTypeInvoiceColumns = new ArrayList<RequestTypeInvoiceColumnWrapper>();
        this.setClientTypes(ComboboxHelper.fillList(ClientType.class, false));
        this.setPaymentTypes(ComboboxHelper.fillList(PaymentType.class, false));
        this.setClientTitleTypes(ComboboxHelper.fillList(ClientTitleType.class, false));

        setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
        getProvinces().add(new SelectItem(Province.FOREIGN_COUNTRY_ID, Province.FOREIGN_COUNTRY));
        this.setAddressCities(ComboboxHelper.fillList(new ArrayList<City>(), true));

        setOperationalHeadquartersProvinces(ComboboxHelper.fillList(Province.class));
        setIbans(ComboboxHelper.fillList(Iban.class));
        this.setAddressOperationalHeadquartersCities(ComboboxHelper.fillList(new ArrayList<City>(), true));

        this.setCountries(ComboboxHelper.fillList(Country.class, Order.asc("description"), new Criterion[]{
                Restrictions.ne("description", "ITALIA")
        }));

        if (!ValidationHelper.isNullOrEmpty(this.getAddressProvinceId())) {
            handleAddressProvinceChange();
        }

        if (!ValidationHelper.isNullOrEmpty(this.getAddressOperationalHeadquartersProvinceId())) {
            handleAddressOperationalHeadquartersProvinceChange();
        }

        setAgencies(getEntity().getAgencies() != null
                ? getEntity().getAgencies().stream()
                .filter(agency -> agency.getAgencyType() == null || agency.getAgencyType() == AgencyType.FILIAL)
                .collect(Collectors.toList())
                : new ArrayList<>());
        setAgencyOffices(getEntity().getAgencies() != null
                ? getEntity().getAgencies().stream()
                .filter(agency -> agency.getAgencyType() != null && AgencyType.OFFICE == agency.getAgencyType())
                .collect(Collectors.toList())
                : new LinkedList<>());

        this.initTempAgencyIds();
        this.initTempAgencyOfficeIds();

        List<Service> services = DaoManager.load(Service.class,Order.asc("name"));

        if (!ValidationHelper.isNullOrEmpty(services)) {
            setServices(services.stream().map(s -> new ServiceWrapper(s, getEntity())).collect(Collectors.toList()));
        }


        List<SelectItem> requestTypeNames = ComboboxHelper.fillList(RequestType.class, false);
        this.setRequestTypeNames(requestTypeNames.stream().sorted(Comparator.comparing(SelectItem::getLabel)).collect(Collectors.toList()));

        setForeignCountry(getEntity().getForeignCountry());

        setTempEmailId(new AtomicLong());

        setPersonalEmails(new ArrayList<>());

        if (!ValidationHelper.isNullOrEmpty(getEntity().getPersonalEmails())) {
            setPersonalEmails(getEntity().getPersonalEmails().stream()
                    .map(e -> new ClientEmailWrapper(e, getTempEmailId().incrementAndGet())).collect(Collectors.toList()));
        }

        if (!getEntity().isNew() && !ValidationHelper.isNullOrEmpty(getEntity().getBillingRecipientList())) {
            setBillingRecipientTable(DaoManager.load(ClientView.class, new Criterion[]{
                    Restrictions.in("id", getEntity().getBillingRecipientList().stream()
                            .map(Client::getId).collect(Collectors.toList()))
            }));
        }

        if (!getEntity().isNew() && !ValidationHelper.isNullOrEmpty(getEntity().getPaymentTypeList())) {
            setPaymentTypeId(getEntity().getPaymentTypeList().get(0).getId());
        }

        if (!getEntity().isNew() && !ValidationHelper.isNullOrEmpty(getEntity().getReferentRecipientList())) {
            setReferentRecipientTable(DaoManager.load(ClientView.class, new Criterion[]{
                    Restrictions.in("id", getEntity().getReferentRecipientList().stream()
                            .map(Client::getId).collect(Collectors.toList()))
            }));
        }

        loadClientList();
        loadReferentClientList();
        loadVariables();
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getIban())) {
            setClientIbanId(this.getEntity().getIban().getId());
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getCostOutput())) {
            setSelectedCostOutput(this.getEntity().getCostOutput());
        } else {
            setSelectedCostOutput(false);
        }

        this.setMaximumFormalities(getEntity().getMaxNumberAct());

        setHasAgency(this.getEntity().isHasAgency());
        setHasAgencyOffice(this.getEntity().getHasAgencyOffice() != null ? this.getEntity().getHasAgencyOffice() : false);

        checkFiduciaryAndManagerField();
        setRenderImportant(!isClientFiduciaryOrManager());

        setNonManagerOrFiduciaryClientLists(ComboboxHelper.fillList(Client.class, new Criterion[]{
                Restrictions.ne("id", this.getEntity().getId() != null ? this.getEntity().getId() : -1L),
                Restrictions.or(
                        Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted")),
                Restrictions.or(Restrictions.eq("fiduciary", Boolean.FALSE),
                        Restrictions.isNull("fiduciary")),
                Restrictions.or(Restrictions.eq("manager", Boolean.FALSE),
                        Restrictions.isNull("manager"))
        },  Order.asc("clientName"), null));

        if (!ValidationHelper.isNullOrEmpty(getEntity().getReferenceClients())) {
            setSelectedNonManagerOrFiduciaryClientIds(
                    getEntity().getReferenceClients()
                            .stream()
                            .map(c -> c.getId())
                            .toArray(Long[] :: new)
            );
        }
        initAreasAndOffices();
        setSelectedOfficeAndArea();
        setNegativePriceList(null);
        setTaxRates(new ArrayList<>());
        getTaxRates().add(SelectItemHelper.getNotSelected());
        List<TaxRate> activeTaxRates = DaoManager.load(TaxRate.class, new Criterion[]{
                Restrictions.and(
                        Restrictions.isNotNull("use"),
                        Restrictions.eq("use", Boolean.TRUE)
                )
        });
        activeTaxRates.forEach(tr -> {
            getTaxRates().add(new SelectItem(tr.getId(), tr.getPercentage() +  "% - " + tr.getDescription()));
        });

        List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"))});
        setClientsToCopy(ComboboxHelper.fillList(clients.stream()
                .filter(c -> (
                                (ValidationHelper.isNullOrEmpty(c.getManager()) || !c.getManager()) &&
                                        (ValidationHelper.isNullOrEmpty(c.getFiduciary()) || !c.getFiduciary())
                        )
                ).sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList()), Boolean.TRUE));
    }

    public void initAreasAndOffices() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setSelectedAreas(new ArrayList<>());
        setSelectedArea(SelectItemHelper.getNotSelectedWrapper());
        setSelectedOffices(new ArrayList<>());
        setSelectedOffice(SelectItemHelper.getNotSelectedWrapper());

        if(!ValidationHelper.isNullOrEmpty(getSelectedNonManagerOrFiduciaryClientIds())){
            List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                    Restrictions.in("id", getSelectedNonManagerOrFiduciaryClientIds())
            });

            if(!ValidationHelper.isNullOrEmpty(clients)) {
                List<Area> areas = clients.stream()
                        .filter(c -> !ValidationHelper.isNullOrEmpty(c.getAreas()))
                        .map(Client:: getAreas)
                        .flatMap(List:: stream).collect(Collectors.toList());
                setAreas(ComboboxHelper.fillWrapperList(emptyIfNull(areas), true));

                List<Office> offices = clients.stream()
                        .filter(c -> !ValidationHelper.isNullOrEmpty(c.getOffices()))
                        .map(Client:: getOffices)
                        .flatMap(List:: stream).collect(Collectors.toList());
                setAllOfficesList(offices);
            }
//        if(!ValidationHelper.isNullOrEmpty(getSelectedNonManagerOrFiduciaryClientId())){
//            Client client = DaoManager.get(Client.class, getSelectedNonManagerOrFiduciaryClientId());
//            setAreas(ComboboxHelper.fillWrapperList(emptyIfNull(client.getAreas()), true));
//            setAllOfficesList(client.getOffices());
//
        }else{
            setAreas(ComboboxHelper.fillWrapperList(Area.class, new Criterion[]{}, true));
            setAllOfficesList(DaoManager.load(Office.class));
        }

        if(getAllOfficesList().size() > 0) {
            Collections.sort(getAllOfficesList(), new Comparator<Office>() {
                @Override
                public int compare(final Office object1, final Office object2) {
                    return object1.getDescription().compareTo(object2.getDescription());
                }
            });
        }
        setOffices(ComboboxHelper.fillWrapperList(getAllOfficesList(), true));

        setAreaConverter(new SelectItemWrapperConverter<>(Area.class, new ArrayList<>(getAreas())));
        setOfficeConverter(new SelectItemWrapperConverter<>(Office.class, new ArrayList<>(getOffices())));

        if (!isClientFiduciaryOrManager()) {
            getAreas().remove(0);
            getOffices().remove(0);
        }
        setOfficesByArea();
    }

    private void setSelectedOfficeAndArea() {
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getArea())) {
            setSelectedArea(new SelectItemWrapper<>(this.getEntity().getArea()));
            setSelectedAreas(new ArrayList<>());
        } else if (!ValidationHelper.isNullOrEmpty(this.getEntity().getAreas())) {
            setSelectedAreas(this.getEntity().getAreas().stream()
                    .map(SelectItemWrapper::new).collect(Collectors.toList()));
            setSelectedArea(SelectItemHelper.getNotSelectedWrapper());
        } else {
            setSelectedAreas(new ArrayList<>());
            setSelectedArea(SelectItemHelper.getNotSelectedWrapper());
        }
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getOffice())) {
            setSelectedOffice(new SelectItemWrapper<>(this.getEntity().getOffice()));
            setSelectedOffices(new ArrayList<>());
        } else if (!ValidationHelper.isNullOrEmpty(this.getEntity().getOffices())) {
            setSelectedOffices(this.getEntity().getOffices().stream()
                    .map(SelectItemWrapper::new).collect(Collectors.toList()));
            setSelectedOffice(SelectItemHelper.getNotSelectedWrapper());
        } else {
            setSelectedOffices(new ArrayList<>());
            setSelectedOffice(SelectItemHelper.getNotSelectedWrapper());
        }
    }

    private void checkFiduciaryAndManagerField() {
        setClientFiduciaryOrManager(
                (!ValidationHelper.isNullOrEmpty(this.getEntity().getManager())
                        && this.getEntity().getManager())
                        || (!ValidationHelper.isNullOrEmpty(this.getEntity().getFiduciary())
                        && this.getEntity().getFiduciary()));
    }

    private void addOrRemoveNotSelectionFieldFromAreas() {
        if (isClientFiduciaryOrManager()) {
            getAreas().add(0, SelectItemHelper.getNotSelectedWrapper());
        } else {
            getAreas().remove(0);
            getSelectedAreas().removeIf(areaSelectItemWrapper -> areaSelectItemWrapper.getId().equals(0L));
        }
    }

    private void loadClientList() throws PersistenceBeanException, IllegalAccessException {
        List<Long> selectedIds;
        if (!ValidationHelper.isNullOrEmpty(getBillingRecipientTable())) {
            selectedIds = getBillingRecipientTable().stream().map(ClientView::getId).collect(Collectors.toList());
            if(!ValidationHelper.isNullOrEmpty(getEntity().getId())) {
                selectedIds.add(getEntity().getId());
            }
            setBillingRecipients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                    Restrictions.or(
                            Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted")),
                    Restrictions.not(Restrictions.in("id", selectedIds))
            }));
        } else {
            setBillingRecipients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                    Restrictions.or(
                            Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted"))
            }));
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
            SelectItemHelper.addItemsToListIfItIsNotInIt(getBillingRecipients(), this.getEntity().getReferenceClients());
        }
    }

    public void addInvoicesRecipient() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
            if (getBillingRecipientTable() == null) {
                setBillingRecipientTable(new ArrayList<>());
            }
            getBillingRecipientTable().add(DaoManager.get(ClientView.class, getSelectedClientId()));
            setSelectedClientId(null);
            loadClientList();
        }
    }

    public void deleteInvoicesRecipient() throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getDeleteClientId())
                && !ValidationHelper.isNullOrEmpty(getBillingRecipientTable())) {
            getBillingRecipientTable().stream().filter(c -> c.getId().equals(getDeleteClientId()))
                    .findAny().ifPresent(cv -> getBillingRecipientTable().remove(cv));
            setDeleteClientId(null);
            loadClientList();
        }
    }

    private void loadReferentClientList() throws PersistenceBeanException, IllegalAccessException {
        List<Long> selectedIds;
        if (!ValidationHelper.isNullOrEmpty(getReferentRecipientTable())) {
            selectedIds = getReferentRecipientTable().stream().map(ClientView::getId).collect(Collectors.toList());
            if(!ValidationHelper.isNullOrEmpty(getEntity().getId())) {
                selectedIds.add(getEntity().getId());
            }
            setReferentRecipients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                    Restrictions.or(
                            Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted")),
                    Restrictions.not(Restrictions.in("id", selectedIds))
            }));
        } else {
            setReferentRecipients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                    Restrictions.or(
                            Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted"))
            }));
        }
    }

    public void addReferentInvoicesRecipient() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedReferentClientId())) {
            if (getReferentRecipientTable() == null) {
                setReferentRecipientTable(new ArrayList<>());
            }
            getReferentRecipientTable().add(DaoManager.get(ClientView.class, getSelectedReferentClientId()));
            setSelectedReferentClientId(null);
            loadReferentClientList();
        }
    }

    public void deleteReferentInvoicesRecipient() throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getReferentDeleteId())
                && !ValidationHelper.isNullOrEmpty(getReferentRecipientTable())) {
            getReferentRecipientTable().stream().filter(c -> c.getId().equals(getReferentDeleteId()))
                    .findAny().ifPresent(cv -> getReferentRecipientTable().remove(cv));
            setReferentDeleteId(null);
            loadReferentClientList();
        }
    }

    private void fillFields() {
        this.setSubjectInvoice(this.getEntity().isSubjectInvoice());
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getArea())) {
            this.setHasAgency(true);
        } else {
            this.setHasAgency(false);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getOffice())) {
            this.setHasAgencyOffice(true);
        } else {
            this.setHasAgencyOffice(false);
        }
        this.setHasHeadquarters(this.getEntity().isHasHeadquarters());
        if (this.getEntity().getForeignCountry() != null && this.getEntity().getForeignCountry()) {
            setAddressProvinceId(Province.FOREIGN_COUNTRY_ID);
            this.setSelectedCountryId(this.getEntity().getCountry() != null
                    ? this.getEntity().getCountry().getId() : null);
        } else {
            this.setAddressProvinceId(this.getEntity().getAddressProvinceId() != null
                    ? this.getEntity().getAddressProvinceId().getId() : null);
            this.setAddressCityId(this.getEntity().getAddressCityId() != null
                    ? this.getEntity().getAddressCityId().getId() : null);
        }
        this.setAddressOperationalHeadquartersProvinceId(this.getEntity()
                .getAddressOperationalHeadquartersProvinceId() != null
                ? this.getEntity().getAddressOperationalHeadquartersProvinceId().getId()
                : null);
        this.setAddressOperationalHeadquartersCityId(this.getEntity()
                .getAddressOperationalHeadquartersCityId() != null
                ? this.getEntity().getAddressOperationalHeadquartersCityId().getId()
                : null);
    }

    public void fillPriceList() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedServiceId())) {
            ServiceWrapper service = getServices().stream()
                    .filter(s -> s.getService().getId().equals(getSelectedServiceId()))
                    .findAny().orElse(null);
            if (service != null) {
                service.fillPriceLists();
                service.getPriceLists()
                        .stream().filter(s -> !ValidationHelper.isNullOrEmpty(s.getTaxRate()))
                        .forEach( s -> {
                            s.setSelectedTaxRateId(s.getTaxRate().getId());
                        });
                service.fillTaxRateExtraCostLists();
                setSelectedService(service);
                try {
                    List<PriceList> priceLists = DaoManager.load(PriceList.class, new Criterion[]{
                            Restrictions.eq("service.id", getSelectedService().getService().getId()),
                            Restrictions.eq("client.id", getSelectedService().getClient().getId()),
                            Restrictions.eq("isNegative", true)
                    });
                    if(priceLists!=null && priceLists.size() >0 ) {
                        setNegativePriceList(priceLists.get(0));
                        if(!ValidationHelper.isNullOrEmpty(getNegativePriceList().getTaxRate()))
                            getNegativePriceList().setSelectedTaxRateId(getNegativePriceList().getTaxRate().getId());
                    }else {
                        setNegativePriceList(new PriceList());
                    }
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                executeJS("PF('configurePriceListWV').show();");
            }
        }
    }

    public void cancelPriceList() {
        setSelectedServiceId(null);
        setSelectedService(null);
        setNegativePriceList(null);
    }

    public void createNewAgency() {
        openAgencyDlg(true);
        SessionHelper.put("isNewAgency", Boolean.TRUE);
    }

    public void createNewAgencyOffice() {
        openAgencyDlg(false);
        SessionHelper.put("isNewAgency", Boolean.TRUE);
    }

    private void openAgencyDlg(boolean isFilial) {
        Map<String, Object> options = new HashMap<String, Object>();
        SessionHelper.put("typeAgency", isFilial);
        options.put("resizable", false);
        options.put("draggable", false);
        options.put("modal", true);
        options.put("width", 1200);
        options.put("height", 550);
        options.put("contentHeight", "100%");
        options.put("contentWidth", "100%");
        RequestContext.getCurrentInstance().openDialog("agencyDialog", options, null);
    }

    public void addReferentNew() {
        SessionHelper.put("fromClient", Boolean.TRUE);
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("resizable", true);
        options.put("draggable", false);
        options.put("modal", true);
        options.put("width", "450px");
        options.put("height", "520px");
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        RequestContext.getCurrentInstance()
                .openDialog(PageTypes.REFERENT_LIST.getPage(), options, null);
    }

    public void editAgency() {
        openAgencyDlg(true);

        for (Agency agency : this.getAgencies()) {
            if (agency.getTempId() == this.getEditAgencyId()) {
                agency.setAgencyType(AgencyType.FILIAL);
                SessionHelper.put("editAgency", agency);

                return;
            }
        }
    }

    public void editAgencyOffice() {
        openAgencyDlg(false);

        for (Agency agency : this.getAgencyOffices()) {
            if (agency.getTempId() == this.getEditAgencyId()) {
                agency.setAgencyType(AgencyType.OFFICE);
                SessionHelper.put("editAgency", agency);

                return;
            }
        }
    }

    public void showAgency() {
        openAgencyDlg(true);
        SessionHelper.put("isOnlyViewAgency", Boolean.TRUE);

        for (Agency agency : this.getAgencies()) {
            if (agency.getTempId() == this.getEditAgencyId()) {
                SessionHelper.put("editAgency", agency);

                return;
            }
        }
    }

    public void onAgencyDialogClose(SelectEvent event) {
        Agency agency = (Agency) event.getObject();

        if (agency != null) {
            if (agency.getTempId() == 0) {
                agency.setTempId(increaseAgencyTempId());
                this.getAgencies().add(agency);
            } else {
                for (int i = 0; i < this.getAgencies().size(); ++i) {
                    if (agency.getTempId() == this.getAgencies().get(i).getTempId()) {
                        this.getAgencies().set(i, agency);

                        return;
                    }
                }
            }
        }
    }

    public void onAgencyOfficeDialogClose(SelectEvent event) {
        Agency agency = (Agency) event.getObject();

        if (agency != null) {
            if (agency.getTempId() == 0) {
                agency.setTempId(increaseAgencyTempId());
                this.getAgencyOffices().add(agency);
            } else {
                for (int i = 0; i < this.getAgencyOffices().size(); ++i) {
                    if (agency.getTempId() == this.getAgencyOffices().get(i).getTempId()) {
                        this.getAgencyOffices().set(i, agency);

                        return;
                    }
                }
            }
        }
    }

    public void addNewEmail() {
        cleanValidation();

        if (ValidationHelper.isNullOrEmpty(getNewEmail()) || !ValidationHelper.checkMailCorrectFormat(getNewEmail())) {
            addFieldException("form:mail", "emailWrongFormat");
        }

        if (!getValidationFailed()) {
            ClientEmail clientEmail = new ClientEmail();

            clientEmail.setEmail(getNewEmail());
            clientEmail.setDescription(getNewEmailDescription());
            if (getPersonalEmails() == null) {
                setPersonalEmails(new ArrayList<>());
            }

            getPersonalEmails().add(new ClientEmailWrapper(clientEmail, getTempEmailId().incrementAndGet()));

            cleanEmailAddress();

            executeJS("PF('addEmailDlgWV').hide();");
        }
    }

    public void cancelSaveNewEmail() {
        cleanValidation();
        cleanEmailAddress();
    }

    private void cleanEmailAddress() {
        this.setNewEmail(null);
    }

    public void editClient() {
        this.setOnlyView(false);
    }

    public void deleteAgency() {
        if (this.getEditAgencyId() != 0) {
            try {
                Agency currentAgency = null;
                for (Agency agency : this.getAgencies()) {
                    if (getEditAgencyId() == agency.getTempId()) {
                        currentAgency = agency;
                        break;
                    }
                }
                this.getAgencies().remove(currentAgency);
                if (!ValidationHelper.isNullOrEmpty(currentAgency)) {
                    Request request = DaoManager.get(Request.class, new Criterion[]{
                            Restrictions.eq("agency.id", currentAgency.getId())
                    });
                    if (ValidationHelper.isNullOrEmpty(request)) {
                        if (!ValidationHelper.isNullOrEmpty(currentAgency.getId())) {
                            DaoManager.remove(Agency.class, currentAgency.getId(), true);
                        }
                    } else {
                        executeJS("PF('exceptionDialogWV').show();");
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void deleteAgencyOffice() {
        if (this.getEditAgencyId() != 0) {
            try {
                Agency currentAgency = null;
                for (Agency agency : this.getAgencyOffices()) {
                    if (getEditAgencyId() == agency.getTempId()) {
                        currentAgency = agency;
                        break;
                    }
                }
                this.getAgencyOffices().remove(currentAgency);
                if (!ValidationHelper.isNullOrEmpty(currentAgency)) {
                    Request request = DaoManager.get(Request.class, new Criterion[]{
                            Restrictions.eq("office.id", currentAgency.getId())
                    });
                    if (ValidationHelper.isNullOrEmpty(request)) {
                        if (!ValidationHelper.isNullOrEmpty(currentAgency.getId())) {
                            DaoManager.remove(Agency.class, currentAgency.getId(), true);
                        }
                    } else {
                        executeJS("PF('exceptionDialogWV').show();");
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void removeSessionAttributeAfterRender(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            clearSessionFields();
        }
    }

    public void handleAddressProvinceChange() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (!Province.FOREIGN_COUNTRY_ID.equals(this.getAddressProvinceId())) {
            this.setForeignCountry(Boolean.FALSE);


            this.setAddressCities(  ComboboxHelper.fillList(City.class, Order.asc("description"),
                    new Criterion[]{
                            Restrictions.eq("province.id", this.getAddressProvinceId()),
                            Restrictions.eq("external", Boolean.TRUE)
                    }));
        } else {
            this.setForeignCountry(Boolean.TRUE);
        }
    }

    public void handleAddressOperationalHeadquartersProvinceChange()
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        this.setAddressOperationalHeadquartersCities(ComboboxHelper.fillList(City.class, Order.asc("description"),
                new Criterion[]{
                        Restrictions.eq("province.id", this.getAddressOperationalHeadquartersProvinceId()),
                        Restrictions.eq("external", Boolean.TRUE)
                }));
    }

    @Override
    public void goBack() {
        try {
            if (!Boolean.TRUE.equals(this.getFromClientSearchList())) {
                RedirectHelper.goTo(PageTypes.CLIENT_LIST);
            } else {
                this.getSession().put("clientId", this.getEntity().getId());
            }
        } finally {
            clearSessionFields();
        }
    }

    private void clearSessionFields() {
        this.getSession().remove("fromClientSearchList");
        this.getSession().remove("clientName");
        this.getSession().remove("clientSurname");
        this.getSession().remove("clientDob");
        this.getSession().remove("clientFiscalCode");
    }

    /*
     * (non-Javadoc)
     *
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        if (this.getEntity().isNew()) {
            this.checkSameEntity();
        }
        cleanValidation();
        if (getRenderImportant()) {
            validFirstTab();
            validThirdTab();
        }
    }

    public void updateForms() {
        boolean oldState = isClientFiduciaryOrManager();
        checkFiduciaryAndManagerField();
        if (oldState != isClientFiduciaryOrManager()) {
            addOrRemoveNotSelectionFieldFromAreas();
        }
        setOfficesByArea();

        setRenderImportant(!isClientFiduciaryOrManager());
        RequestContext context = RequestContext.getCurrentInstance();
        context.update("wizard:typegrid");
        context.update("wizard:officeAddress");
    }

    private void checkSameEntity() {
        try {
            if (!ValidationHelper.isNullOrEmpty(getEntity().getNameOfTheCompany())) {
                Client client = DaoManager.get(Client.class, new Criterion[]{
                        Restrictions.eq("nameOfTheCompany", getEntity().getNameOfTheCompany())
                });

                if (client != null) {
                    addRequiredFieldException("form:nameOfTheCompany", "clientAlreadyPresent");
                }
            }
            if (!ValidationHelper.isNullOrEmpty(getEntity().getNumberVAT())) {
                Client client = DaoManager.get(Client.class, new Criterion[]{
                        Restrictions.eq("numberVAT", getEntity().getNumberVAT())
                });

                if (client != null) {
                    addRequiredFieldException("form:numberVAT", "clientAlreadyPresent");
                }
            }
            if (!ValidationHelper.isNullOrEmpty(getEntity().getFiscalCode())) {
                Client client = DaoManager.get(Client.class, new Criterion[]{
                        Restrictions.eq("fiscalCode", getEntity().getFiscalCode())
                });

                if (client != null) {
                    addRequiredFieldException("form:fiscalCode", "clientAlreadyPresent");
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        try {
            this.getEntity().setHasHeadquarters(this.isHasHeadquarters());
            this.getEntity().setSubjectInvoice(this.isSubjectInvoice());

            if (!ValidationHelper.isNullOrEmpty(this.getAddressCityId())) {
                this.getEntity().setAddressCityId(DaoManager.get(City.class, this.getAddressCityId()));
            } else {
                this.getEntity().setAddressCityId(null);
            }

            if (!ValidationHelper.isNullOrEmpty(this.getAddressOperationalHeadquartersCityId())) {
                this.getEntity().setAddressOperationalHeadquartersCityId(
                        DaoManager.get(City.class, this.getAddressOperationalHeadquartersCityId()));
            } else {
                this.getEntity().setAddressOperationalHeadquartersCityId(null);
            }

            if (!ValidationHelper.isNullOrEmpty(this.getAddressOperationalHeadquartersProvinceId())) {
                this.getEntity().setAddressOperationalHeadquartersProvinceId(
                        DaoManager.get(Province.class, this.getAddressOperationalHeadquartersProvinceId()));
            } else {
                this.getEntity().setAddressOperationalHeadquartersProvinceId(null);
            }

            if (!ValidationHelper.isNullOrEmpty(this.getAddressProvinceId())) {
                this.getEntity().setAddressProvinceId(DaoManager.get(Province.class, this.getAddressProvinceId()));
            } else {
                this.getEntity().setAddressProvinceId(null);
            }

            if (this.getAddressProvinceId() != null && !Province.FOREIGN_COUNTRY_ID.equals(this.getAddressProvinceId())) {
                Province province = DaoManager.get(Province.class, this.getAddressProvinceId());
                this.getEntity().setAddressProvinceId(province);
            }

            if (getForeignCountry() && !ValidationHelper.isNullOrEmpty(this.getSelectedCountryId())) {
                Country country = DaoManager.get(Country.class, this.getSelectedCountryId());
                this.getEntity().setCountry(country);
            }

            this.getEntity().setForeignCountry(getForeignCountry());
            if (!ValidationHelper.isNullOrEmpty(getBillingRecipientTable())) {
                getEntity().setBillingRecipientList(DaoManager.load(Client.class, new Criterion[]{
                        Restrictions.in("id", getBillingRecipientTable().stream()
                                .map(ClientView::getId).collect(Collectors.toList()))
                }));
            } else {
                getEntity().setBillingRecipientList(null);
            }

            if (!ValidationHelper.isNullOrEmpty(getReferentRecipientTable())) {
                getEntity().setReferentRecipientList(DaoManager.load(Client.class, new Criterion[]{
                        Restrictions.in("id", getReferentRecipientTable().stream()
                                .map(ClientView::getId).collect(Collectors.toList()))
                }));
            } else {
                getEntity().setReferentRecipientList(null);
            }

            if (!ValidationHelper.isNullOrEmpty(getSelectedArea()) && isHasAgency()
                    && isClientFiduciaryOrManager()) {
                this.getEntity().setArea(DaoManager.get(Area.class, new Criterion[]{
                        Restrictions.eq("id", getSelectedArea().getId())}));
                this.getEntity().setAreas(null);
            } else if (!ValidationHelper.isNullOrEmpty(getSelectedAreas()) && isHasAgency()
                    && !isClientFiduciaryOrManager()) {
                this.getEntity().setAreas(DaoManager.load(Area.class, new Criterion[]{
                        Restrictions.in("id", getSelectedAreas()
                                .stream().map(SelectItemWrapper::getId).collect(Collectors.toList()))
                }));
                this.getEntity().setArea(null);
            } else {
                this.getEntity().setAreas(null);
                this.getEntity().setArea(null);
            }

            if (!ValidationHelper.isNullOrEmpty(getSelectedNonManagerOrFiduciaryClientIds())) {
                List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                        Restrictions.in("id", getSelectedNonManagerOrFiduciaryClientIds())
                });
                this.getEntity().setReferenceClients(clients);

            }else {
                this.getEntity().setReferenceClients(null);
            }
//
//            if (!ValidationHelper.isNullOrEmpty(getSelectedNonManagerOrFiduciaryClientId())) {
//                this.getEntity().setClient(DaoManager.get(Client.class, getSelectedNonManagerOrFiduciaryClientId()));
//            } else {
//                this.getEntity().setClient(null);
//            }

            if (!ValidationHelper.isNullOrEmpty(getSelectedOffice()) && isHasAgencyOffice()
                    && isClientFiduciaryOrManager()) {
                this.getEntity().setOffice(DaoManager.get(Office.class, new Criterion[]{
                        Restrictions.eq("id", getSelectedOffice().getId())}));
                this.getEntity().setOffices(null);
            } else if (!ValidationHelper.isNullOrEmpty(getSelectedOffices()) && isHasAgencyOffice()
                    && !isClientFiduciaryOrManager()) {
                this.getEntity().setOffices(DaoManager.load(Office.class, new Criterion[]{
                        Restrictions.in("id", getSelectedOffices()
                                .stream().map(SelectItemWrapper::getId).collect(Collectors.toList()))
                }));
                this.getEntity().setOffice(null);
            } else {
                this.getEntity().setOffice(null);
                this.getEntity().setOffices(null);
            }

            this.getEntity().setHasAgency(isHasAgency());
            this.getEntity().setHasAgencyOffice(isHasAgencyOffice());

            if (!ValidationHelper.isNullOrEmpty(getClientIbanId())) {
                this.getEntity().setIban(DaoManager.get(Iban.class, new Criterion[]{
                        Restrictions.eq("id", getClientIbanId())}));
            }

            if (!ValidationHelper.isNullOrEmpty(getSelectedCostOutput())) {
                this.getEntity().setCostOutput(getSelectedCostOutput());
            }

            this.getEntity().setMaxNumberAct(getMaximumFormalities());

            if (ClientType.PROFESSIONAL.getId().equals(getEntity().getTypeId())) {
                this.getEntity().setClientName(this.getEntity().getNameProfessional() != null ? this.getEntity().getNameProfessional().toLowerCase() : "");
            }else {
                this.getEntity().setClientName(this.getEntity().getNameOfTheCompany() != null ? this.getEntity().getNameOfTheCompany().toLowerCase() : "");
            }

            if (!ValidationHelper.isNullOrEmpty(getPaymentTypeId())) {
                getEntity().setPaymentTypeList(DaoManager.load(PaymentType.class, new Criterion[]{
                        Restrictions.eq("id", getPaymentTypeId())
                }));
            } else {
                getEntity().setPaymentTypeList(null);
            }

            DaoManager.save(getEntity());
            this.saveAgencies();
            this.savePriceList();
            this.saveTaxRateExtraCostList();
            this.saveClientServiceInfo();
            this.saveEmails();
            this.saveInvoiceColumns();
            this.saveRequestTypeInvoicecolumns();
            if(!ValidationHelper.isNullOrEmpty(this.getFromContactList()) && this.getFromContactList()){
                RedirectHelper.goTo(PageTypes.CONTACT_LIST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveEmails() throws PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getEmailsForDisplay())) {
            for (ClientEmailWrapper em : getEmailsForDisplay()) {
                em.getClientEmail().setClient(getEntity());
                em.getClientEmail().setTypeId(EmailType.PERSONAL.getId());
                DaoManager.save(em.getClientEmail());
            }
        }

        if (!ValidationHelper.isNullOrEmpty(getEmailsForDelete())) {
            for (ClientEmailWrapper em : getEmailsForDelete()) {
                if (!em.getClientEmail().isNew()) {
                    DaoManager.remove(em.getClientEmail());
                }
            }
        }
    }

    private void saveClientServiceInfo() throws PersistenceBeanException {
        for (ServiceWrapper wrapper : getServices()) {
            if (!wrapper.getInfo().isNew() || !ValidationHelper.isNullOrEmpty(wrapper.getInfo().getDaysToExpire())) {
                DaoManager.save(wrapper.getInfo());
            }
        }
    }

    private void savePriceList() {
        for (PriceList priceList : getServices().stream().map(ServiceWrapper::getPriceLists).flatMap(List::stream)
                .filter(priceList -> !ValidationHelper.isNullOrEmpty(priceList.getPrice())
                        || !ValidationHelper.isNullOrEmpty(priceList.getFirstPrice())).collect(Collectors.toList())) {
            priceList.setClient(getEntity());
            if(!ValidationHelper.isNullOrEmpty(priceList.getSelectedTaxRateId())){
                try {
                    priceList.setTaxRate(DaoManager.get(TaxRate.class, priceList.getSelectedTaxRateId()));
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
            if (priceList.getConfigureDate() == null) {
                priceList.setConfigureDate(new Date());
            }

            try {
                DaoManager.save(priceList);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        if(getSelectedService() != null && getSelectedService().getService() != null &&
                getSelectedService().getService().getIsNegative() != null &&
                getSelectedService().getService().getIsNegative()) {
            if(ValidationHelper.isNullOrEmpty(getNegativePriceList().getClient()))
                getNegativePriceList().setClient(getSelectedService().getClient());
            if(ValidationHelper.isNullOrEmpty(getNegativePriceList().getService()))
                getNegativePriceList().setService(getSelectedService().getService());
            getNegativePriceList().setIsNegative(true);
            try {
                if(!ValidationHelper.isNullOrEmpty(getNegativePriceList().getSelectedTaxRateId())){
                    getNegativePriceList().setTaxRate(DaoManager.get(TaxRate.class, getNegativePriceList().getSelectedTaxRateId()));
                }
                DaoManager.save(getNegativePriceList());

            } catch (HibernateException | PersistenceBeanException | InstantiationException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }
        }
    }

    private void saveTaxRateExtraCostList() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        for (TaxRateExtraCost taxRateExtraCost :
                getServices().stream().map(
                        ServiceWrapper::getTaxRateExtraCosts).flatMap(List::stream).collect(Collectors.toList())) {
            taxRateExtraCost.setClientId(getEntity().getId());
            if(!ValidationHelper.isNullOrEmpty(taxRateExtraCost.getTaxRateId()))
                taxRateExtraCost.setTaxRate(DaoManager.get(TaxRate.class, taxRateExtraCost.getTaxRateId()));
            else
                taxRateExtraCost.setTaxRate(null);

            try {
                DaoManager.save(taxRateExtraCost);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        if(getSelectedService() != null && getSelectedService().getService() != null &&
                getSelectedService().getService().getIsNegative() != null &&
                getSelectedService().getService().getIsNegative()) {
            if(ValidationHelper.isNullOrEmpty(getNegativePriceList().getClient()))
                getNegativePriceList().setClient(getSelectedService().getClient());
            if(ValidationHelper.isNullOrEmpty(getNegativePriceList().getService()))
                getNegativePriceList().setService(getSelectedService().getService());
            getNegativePriceList().setIsNegative(true);

            try {
                DaoManager.save(getNegativePriceList());
            } catch (HibernateException | PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void setOfficesByArea() {
        final List<Long> areaIds;
        if (isHasAgency()) {
            if (!ValidationHelper.isNullOrEmpty(getSelectedArea()) && isClientFiduciaryOrManager()
                    && !getSelectedArea().getId().equals(0L)) {
                areaIds = Collections.singletonList(getSelectedArea().getId());
            } else if (!ValidationHelper.isNullOrEmpty(getSelectedAreas()) && !isClientFiduciaryOrManager()) {
                areaIds = getSelectedAreas().stream().filter(x -> !x.getId().equals(0L))
                        .map(SelectItemWrapper::getId).collect(Collectors.toList());
            } else {
                areaIds = null;
            }
        } else {
            areaIds = null;
        }

        setOffices(ComboboxHelper.fillWrapperList(getAllOfficesList().stream()
                .filter(x -> ValidationHelper.isNullOrEmpty(areaIds) || areaIds.contains(x.getArea().getId()))
                .collect(Collectors.toList()), isClientFiduciaryOrManager()));

        if (!ValidationHelper.isNullOrEmpty(getSelectedOffices()) && !isClientFiduciaryOrManager()) {
            setSelectedOffices(getSelectedOffices().stream().filter(office -> getOffices().contains(office))
                    .collect(Collectors.toList()));
        }
    }

    private void saveAgencies() {
        for (Agency agency : this.getAgencies()) {
            agency.setClient(this.getEntity());

            try {
                DaoManager.save(agency);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        for (Agency agency : this.getAgencyOffices()) {
            agency.setClient(this.getEntity());

            try {
                DaoManager.save(agency);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void prepareEditMail() {
        ClientEmailWrapper cew = getPersonalEmails().stream()
                .filter(em -> em.getId().equals(getEmailEditId())).findAny()
                .orElse(null);

        setEditEmailDescription(cew == null ? "" : cew.getClientEmail().getDescription());
        setEditEmail(cew == null ? "" : cew.getClientEmail().getEmail());
    }

    public void editEmail() {
        cleanValidation();

        if (ValidationHelper.isNullOrEmpty(getEditEmail())
                || !ValidationHelper.checkMailCorrectFormat(getEditEmail())
                || ValidationHelper.isNullOrEmpty(getEmailEditId())
                || ValidationHelper.isNullOrEmpty(getPersonalEmails())) {
            addFieldException("form:editMail", "emailWrongFormat");
        }
        if (ValidationHelper.isNullOrEmpty(getEditEmailDescription())) {
            addRequiredFieldException("form:editDescription");
        }
        if (!getValidationFailed()) {
            getPersonalEmails().stream().filter(em -> em.getId().equals(getEmailEditId()))
                    .forEach((em) -> {
                        em.getClientEmail().setEmail(getEditEmail());
                        em.getClientEmail().setDescription(getEditEmailDescription());
                    });

            executeJS("PF('editEmailDlgWV').hide();");
        }
    }

    public void deleteEmail() {
        if (!ValidationHelper.isNullOrEmpty(getEmailDeleteId()) && !ValidationHelper.isNullOrEmpty(getPersonalEmails())) {
            getPersonalEmails().stream()
                    .filter(em -> em.getId().equals(getEmailDeleteId()))
                    .forEach(em -> em.setDelete(Boolean.TRUE));
        }
    }

    public List<ClientEmailWrapper> getEmailsForDelete() {
        if (!ValidationHelper.isNullOrEmpty(getPersonalEmails())) {
            return getPersonalEmails().stream()
                    .filter(ClientEmailWrapper::getDelete)
                    .collect(Collectors.toList());
        }

        return null;
    }

    public List<ClientEmailWrapper> getEmailsForDisplay() {
        if (!ValidationHelper.isNullOrEmpty(getPersonalEmails())) {
            return getPersonalEmails().stream().filter(it -> !it.getDelete()).collect(Collectors.toList());
        }

        return null;
    }

    public SelectItem[] getYesNoValues() {
        return new SelectItem[]{
                new SelectItem(Boolean.TRUE, ResourcesHelper.getString("yes")),
                new SelectItem(Boolean.FALSE, ResourcesHelper.getString("no"))
        };
    }

    private void initTempAgencyIds() {
        for (Agency agency : this.getAgencies()) {
            agency.setTempId(increaseAgencyTempId());
        }
    }

    private void initTempAgencyOfficeIds() {
        for (Agency agency : this.getAgencyOffices()) {
            agency.setTempId(increaseAgencyTempId());
        }
    }

    private int increaseAgencyTempId() {
        return ++tempAgencyId;
    }

    public Boolean getIsProfessional() {
        return getEntity().getTypeId() == null || ClientType.PROFESSIONAL.getId().equals(getEntity().getTypeId());
    }

    public String getCityDescription() {
        return this.loadDescriptionById(City.class, this.getAddressCityId());
    }

    public String getProvinceDescription() {
        if (getForeignCountry()) {
            return Province.FOREIGN_COUNTRY;
        }
        return this.loadDescriptionById(Province.class, this.getAddressProvinceId());
    }

    public String getCountryDescription() {
        return this.loadDescriptionById(Country.class, this.getSelectedCountryId());
    }

    public String getCityOperationalHeadquartersDescription() {
        return this.loadDescriptionById(City.class, this.getAddressOperationalHeadquartersCityId());
    }

    public String getProvinceOperationalHeadquartersDescription() {
        return this.loadDescriptionById(Province.class, this.getAddressOperationalHeadquartersProvinceId());
    }

    private void validFirstTab() {
        if (ValidationHelper.isNullOrEmpty(getEntity().getTypeId())) {
            addRequiredFieldException("wizard:clientType");
        }
        if (getIsProfessional() && ValidationHelper.isNullOrEmpty(getEntity().getTitleTypeId())) {
            addRequiredFieldException("wizard:clientTitleType");
        }
        if (getIsProfessional() && ValidationHelper.isNullOrEmpty(getEntity().getNameProfessional())) {
            addRequiredFieldException("wizard:nameProfessional");
        }
        if (!getIsProfessional() && ValidationHelper.isNullOrEmpty(getEntity().getNameOfTheCompany())) {
            addRequiredFieldException("wizard:nameOfTheCompany");
        }
        if (ValidationHelper.isNullOrEmpty(getEntity().getNumberVAT())
                && ValidationHelper.isNullOrEmpty(getEntity().getFiscalCode())) {
            addRequiredFieldException("wizard:numberVAT");
        }
        if (!ValidationHelper.isNullOrEmpty(getEntity().getFiscalCode())
                && !(getEntity().getFiscalCode().length() == 11
                || getEntity().getFiscalCode().length() == 16)) {
            addFieldException("wizard:fiscalCode", "wizard:fiscalCodeWrongFormat");
        }
        if (!ValidationHelper.isNullOrEmpty(getEntity().getOperationalHeadquartersMail())
                && !ValidationHelper.checkMailCorrectFormat(getEntity().getOperationalHeadquartersMail())) {
            addFieldException("wizard:operationalHeadquartersMail", "wizard:emailWrongFormat");
        }
        if (ValidationHelper.isNullOrEmpty(this.getAddressProvinceId())) {
            addRequiredFieldException("wizard:province");
        }

        if ((getForeignCountry() == null || !getForeignCountry())
                && ValidationHelper.isNullOrEmpty(this.getAddressCityId())) {
            addRequiredFieldException("wizard:city");
        }

        if (getForeignCountry() != null && getForeignCountry()
                && ValidationHelper.isNullOrEmpty(this.getSelectedCountryId())) {
            addRequiredFieldException("wizard:country");
        }
        getValidationFailed();
    }

    private void validThirdTab() {
        if (isHasHeadquarters()) {

            if (ValidationHelper.isNullOrEmpty(getEntity().getAddressOperationalHeadquartersStreet())) {
                addRequiredFieldException("wizard:addressOperationalHeadquartersStreet");
            }

            if (ValidationHelper.isNullOrEmpty(getEntity().getAddressOperationalHeadquartersHouseNumber())) {
                addRequiredFieldException("wizard:addressOperationalHeadquartersHouseNumber");
            }

            if (ValidationHelper.isNullOrEmpty(getEntity().getAddressOperationalHeadquartersPostalCode())) {
                addRequiredFieldException("wizard:addressOperationalHeadquartersPostalCode");
            }

            if (ValidationHelper.isNullOrEmpty(this.getAddressOperationalHeadquartersProvinceId())) {
                addRequiredFieldException("wizard:operationalHeadquartersProvince");
            }

            if (ValidationHelper.isNullOrEmpty(this.getAddressOperationalHeadquartersCityId())) {
                addRequiredFieldException("wizard:operationalHeadquartersCity");
            }
        }

        getValidationFailed();
    }

    private void loadVariables() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        try {

            List<Long> requestTypeIds = new ArrayList<Long>();
            List<InvoiceColumnWrapper> sourceInvoiceColumns = new LinkedList<>();
            for (BillingTypeFields field : BillingTypeFields.values()) {
                sourceInvoiceColumns.add(new InvoiceColumnWrapper(field));
            }
            List<InvoiceColumnWrapper> targetInvoiceColumns = new LinkedList<>();
            if (!ValidationHelper
                    .isNullOrEmpty(this.getEntity().getClientInvoiceManageColumns())) {
                for(ClientInvoiceManageColumn clientInvoiceManageColumn :
                        this.getEntity().getClientInvoiceManageColumns()) {
                    if(clientInvoiceManageColumn.getRequestType() == null)
                        targetInvoiceColumns.add(new InvoiceColumnWrapper(clientInvoiceManageColumn));
                    else {
                        getRequestTypeNames().removeIf(s -> ((Long)s.getValue()).equals(clientInvoiceManageColumn.getRequestType().getId()));
                        if(!requestTypeIds.contains(clientInvoiceManageColumn.getRequestType().getId()))
                            requestTypeIds.add(clientInvoiceManageColumn.getRequestType().getId());
                    }
                }
            }
            for (InvoiceColumnWrapper invoiceColumnWrapper : targetInvoiceColumns) {
                sourceInvoiceColumns.stream().filter(c -> c.getField().getField().equals(invoiceColumnWrapper.getField().getField()))
                        .findAny().ifPresent(cv -> sourceInvoiceColumns.remove(cv));
            }

            targetInvoiceColumns.sort(new Comparator<InvoiceColumnWrapper>() {
                @Override
                public int compare(InvoiceColumnWrapper iw1, InvoiceColumnWrapper iw2) {
                    return iw1.getField().getPosition().compareTo(iw2.getField().getPosition());
                }
            });
            this.setInvoiceColumns(new DualListModel<>(sourceInvoiceColumns, targetInvoiceColumns));

            this.getRequestTypeInvoiceColumns().clear();

            if (!ValidationHelper
                    .isNullOrEmpty(requestTypeIds)) {
                List<ClientInvoiceManageColumn> clientInvoiceManageColumns = DaoManager.load(ClientInvoiceManageColumn.class, new CriteriaAlias[]{
                        new CriteriaAlias("client", "client", JoinType.INNER_JOIN),
                        new CriteriaAlias("requestType", "requestType", JoinType.INNER_JOIN)
                }, new Criterion[]{
                        Restrictions.and(Restrictions.eq("client.id", getEntity().getId())
                                ,Restrictions.in("requestType.id",requestTypeIds))
                });

                for(Long requestTypeId : requestTypeIds) {
                    List<ClientInvoiceManageColumn> serviceClientInvoiceManageColumns =
                            clientInvoiceManageColumns.stream().filter(
                                    c -> c.getRequestType().getId().equals(requestTypeId))
                                    .collect(Collectors.toList());

                    List<InvoiceColumnWrapper> serviceSourceInvoiceColumns = new LinkedList<>();
                    for (BillingTypeFields field : BillingTypeFields.values()) {
                        serviceSourceInvoiceColumns.add(new InvoiceColumnWrapper(field));
                    }
                    List<InvoiceColumnWrapper> serviceTargetInvoiceColumns = new LinkedList<>();
                    if (!ValidationHelper
                            .isNullOrEmpty(serviceClientInvoiceManageColumns)) {
                        for(ClientInvoiceManageColumn clientInvoiceManageColumn :
                                serviceClientInvoiceManageColumns) {
                            serviceTargetInvoiceColumns.add(new InvoiceColumnWrapper(clientInvoiceManageColumn));
                        }
                    }

                    for (InvoiceColumnWrapper invoiceColumnWrapper : serviceTargetInvoiceColumns) {
                        serviceSourceInvoiceColumns.stream().filter(c -> c.getField().getField().equals(invoiceColumnWrapper.getField().getField()))
                                .findAny().ifPresent(cv -> serviceSourceInvoiceColumns.remove(cv));
                    }

                    serviceTargetInvoiceColumns.sort(new Comparator<InvoiceColumnWrapper>() {
                        @Override
                        public int compare(InvoiceColumnWrapper iw1, InvoiceColumnWrapper iw2) {
                            return iw1.getField().getPosition().compareTo(iw2.getField().getPosition());
                        }
                    });

                    RequestTypeInvoiceColumnWrapper requestTypeInvoiceColumnWrapper = new RequestTypeInvoiceColumnWrapper(new DualListModel<>(serviceSourceInvoiceColumns, serviceTargetInvoiceColumns),
                            DaoManager.get(RequestType.class, requestTypeId));
                    getRequestTypeInvoiceColumns().add(requestTypeInvoiceColumnWrapper);
                }

            }

        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void saveInvoiceColumns() throws PersistenceBeanException, HibernateException, IllegalAccessException, InstantiationException {
        List<BillingTypeFields> fields = new ArrayList<BillingTypeFields>();
        for(int j =0; j < this.getInvoiceColumns().getTarget().size(); j++) {
            InvoiceColumnWrapper wrapper = this.getInvoiceColumns().getTarget().get(j);
            if (wrapper != null) {
                if (wrapper.getSelected()) {
                    wrapper.getField().setClient(getEntity());
                    wrapper.getField().setPosition(j+1);
                    DaoManager.save(wrapper.getField());
                    fields.add(wrapper.getField().getField());
                }
            }
        }

        if(!getEntity().isNew()) {
            for(BillingTypeFields field : BillingTypeFields.values()) {
                if(!fields.contains(field)) {
                    ClientInvoiceManageColumn clientInvoiceManageColumn =
                            DaoManager.get(ClientInvoiceManageColumn.class, new CriteriaAlias[]{
                                    new CriteriaAlias("client", "client", JoinType.INNER_JOIN)
                            }, new Criterion[]{
                                    Restrictions.and(Restrictions.eq("field", field),Restrictions.eq("client.id", getEntity().getId()),
                                            Restrictions.isNull("requestType"))
                            });

                    if(!ValidationHelper.isNullOrEmpty(clientInvoiceManageColumn))
                        DaoManager.remove(clientInvoiceManageColumn);
                }
            }
        }
    }

    public void addRequestTypeInvoiceColumns() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        getRequestTypeNames().removeIf(s -> ((Long)s.getValue()).equals(getSelectedBillingRequestTypeId()));
        List<InvoiceColumnWrapper> sourceInvoiceColumns = new LinkedList<>();

        for (BillingTypeFields field : BillingTypeFields.values()) {
            InvoiceColumnWrapper invoiceColumnWrapper = new InvoiceColumnWrapper(field);
            sourceInvoiceColumns.add(invoiceColumnWrapper);
        }
        List<InvoiceColumnWrapper> targetInvoiceColumns = new LinkedList<>();

        RequestTypeInvoiceColumnWrapper serviceInvoiceColumnWrapper = new RequestTypeInvoiceColumnWrapper(new DualListModel<>(sourceInvoiceColumns, targetInvoiceColumns),
                DaoManager.get(RequestType.class, getSelectedBillingRequestTypeId()));

        getRequestTypeInvoiceColumns().add(serviceInvoiceColumnWrapper);
    }

    private void saveRequestTypeInvoicecolumns() throws PersistenceBeanException, HibernateException, IllegalAccessException, InstantiationException {

        for(RequestTypeInvoiceColumnWrapper serviceInvoiceColumnWrapper : this.getRequestTypeInvoiceColumns()) {

            List<BillingTypeFields> fields = new ArrayList<BillingTypeFields>();
            for(int j =0; j < serviceInvoiceColumnWrapper.getInvoiceColumns().getTarget().size(); j++) {
                InvoiceColumnWrapper wrapper = serviceInvoiceColumnWrapper.getInvoiceColumns().getTarget().get(j);
                if (wrapper != null) {
                    if (wrapper.getSelected()) {
                        wrapper.getField().setClient(getEntity());
                        wrapper.getField().setPosition(j+1);
                        wrapper.getField().setRequestType(serviceInvoiceColumnWrapper.getRequestType());
                        DaoManager.save(wrapper.getField());
                        fields.add(wrapper.getField().getField());
                    }
                }
            }
            if(!getEntity().isNew()) {
                for(BillingTypeFields field : BillingTypeFields.values()) {
                    if(!fields.contains(field)) {
                        ClientInvoiceManageColumn clientInvoiceManageColumn =
                                DaoManager.get(ClientInvoiceManageColumn.class, new CriteriaAlias[]{
                                        new CriteriaAlias("client", "client", JoinType.INNER_JOIN),
                                        new CriteriaAlias("requestType", "requestType", JoinType.INNER_JOIN)
                                }, new Criterion[]{
                                        Restrictions.and(Restrictions.eq("field", field)
                                                ,Restrictions.eq("client.id", getEntity().getId())
                                                ,Restrictions.eq("requestType.id",serviceInvoiceColumnWrapper.getRequestType().getId()))
                                });
                        if(!ValidationHelper.isNullOrEmpty(clientInvoiceManageColumn))
                            DaoManager.remove(clientInvoiceManageColumn);
                    }
                }
            }
        }
    }

    public void copyPriceListClient() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        List<PriceList> priceListEntity = DaoManager.load(PriceList.class, new Criterion[]{
                Restrictions.eq("client.id", getEntityId())});
        for(PriceList price: priceListEntity) {
            DaoManager.remove(price, true);
        }

        for(PriceList priceList: getPriceListToCopy()) {
            DaoManager.getSession().evict(priceList);
            priceList.setId(null);
            PriceList newPrice = new PriceList();
            newPrice = priceList;
            newPrice.setClient(getEntity());
            newPrice.setCreateDate(new Date());
            newPrice.setUpdateDate(null);
            newPrice.setUpdateUserId(null);
            newPrice.setConfigureDate(null);
            DaoManager.save(newPrice, true);
        }
    }

    public void checkCopyPriceListClient() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        List<PriceList> priceLists = new ArrayList<>();
        setPriceListToCopy(priceLists);
        if(!ValidationHelper.isNullOrEmpty(getSelectedClientIdToCopy())) {
            priceLists = DaoManager.load(PriceList.class, new Criterion[]{
                    Restrictions.eq("client.id", getSelectedClientIdToCopy())});
            if(ValidationHelper.isNullOrEmpty(priceLists)) {
                System.out.println("pricelist empty");
                setEmptyPriceListMessage(ResourcesHelper.getString("clientEditEmptyPriceList"));
            } else {
                System.out.println("pricelist size :: "+priceLists.size());
                getPriceListToCopy().addAll(priceLists);
            }
            RequestContext.getCurrentInstance().update("confirmCopyPriceListDialog");
            executeJS("PF('confirmCopyPriceListDialogWV').show();");
        }
    }

    public void lastStepListener(boolean isLast) {
        setLastStep(isLast);
    }

    public boolean isRenderMenu() {
        return renderMenu;
    }

    public void setRenderMenu(boolean renderMenu) {
        this.renderMenu = renderMenu;
    }

    public boolean isSubjectInvoice() {
        return subjectInvoice;
    }

    public void setSubjectInvoice(boolean subjectInvoice) {
        this.subjectInvoice = subjectInvoice;
    }

    public boolean isHasAgency() {
        return hasAgency;
    }

    public void setHasAgency(boolean hasAgency) {
        this.hasAgency = hasAgency;
    }

    public boolean isHasHeadquarters() {
        return hasHeadquarters;
    }

    public void setHasHeadquarters(boolean hasHeadquarters) {
        this.hasHeadquarters = hasHeadquarters;
    }

    public boolean isOnlyView() {
        return onlyView;
    }

    public void setOnlyView(boolean onlyView) {
        this.onlyView = onlyView;
    }

    public Boolean getFromClientSearchList() {
        return fromClientSearchList;
    }

    public void setFromClientSearchList(Boolean fromClientSearchList) {
        this.fromClientSearchList = fromClientSearchList;
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

    public Long getAddressOperationalHeadquartersProvinceId() {
        return addressOperationalHeadquartersProvinceId;
    }

    public void setAddressOperationalHeadquartersProvinceId(
            Long addressOperationalHeadquartersProvinceId) {
        this.addressOperationalHeadquartersProvinceId = addressOperationalHeadquartersProvinceId;
    }

    public Long getAddressOperationalHeadquartersCityId() {
        return addressOperationalHeadquartersCityId;
    }

    public void setAddressOperationalHeadquartersCityId(
            Long addressOperationalHeadquartersCityId) {
        this.addressOperationalHeadquartersCityId = addressOperationalHeadquartersCityId;
    }

    public int getEditAgencyId() {
        return editAgencyId;
    }

    public void setEditAgencyId(int editAgencyId) {
        this.editAgencyId = editAgencyId;
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

    public List<SelectItem> getOperationalHeadquartersProvinces() {
        return operationalHeadquartersProvinces;
    }

    public void setOperationalHeadquartersProvinces(
            List<SelectItem> operationalHeadquartersProvinces) {
        this.operationalHeadquartersProvinces = operationalHeadquartersProvinces;
    }

    public List<SelectItem> getAddressOperationalHeadquartersCities() {
        return addressOperationalHeadquartersCities;
    }

    public void setAddressOperationalHeadquartersCities(
            List<SelectItem> addressOperationalHeadquartersCities) {
        this.addressOperationalHeadquartersCities = addressOperationalHeadquartersCities;
    }

    public List<Agency> getAgencies() {
        return agencies;
    }

    public void setAgencies(List<Agency> agencies) {
        this.agencies = agencies;
    }

    public int getTempAgencyId() {
        return tempAgencyId;
    }

    public void setTempAgencyId(int tempAgencyId) {
        this.tempAgencyId = tempAgencyId;
    }

    public List<SelectItem> getClientTypes() {
        return clientTypes;
    }

    public void setClientTypes(List<SelectItem> clientTypes) {
        this.clientTypes = clientTypes;
    }

    public List<SelectItem> getClientTitleTypes() {
        return clientTitleTypes;
    }

    public void setClientTitleTypes(List<SelectItem> clientTitleTypes) {
        this.clientTitleTypes = clientTitleTypes;
    }

    public Long getSelectedServiceId() {
        return selectedServiceId;
    }

    public void setSelectedServiceId(Long selectedServiceId) {
        this.selectedServiceId = selectedServiceId;
    }

    public List<ServiceWrapper> getServices() {
        return services;
    }

    public void setServices(List<ServiceWrapper> services) {
        this.services = services;
    }

    public ServiceWrapper getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(ServiceWrapper selectedService) {
        this.selectedService = selectedService;
    }

    public Boolean getForeignCountry() {
        return foreignCountry == null ? Boolean.FALSE : foreignCountry;
    }

    public void setForeignCountry(Boolean foreignCountry) {
        this.foreignCountry = foreignCountry;
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

    public List<ClientEmailWrapper> getPersonalEmails() {
        return personalEmails;
    }

    public void setPersonalEmails(List<ClientEmailWrapper> personalEmails) {
        this.personalEmails = personalEmails;
    }

    public AtomicLong getTempEmailId() {
        return tempEmailId;
    }

    public void setTempEmailId(AtomicLong tempEmailId) {
        this.tempEmailId = tempEmailId;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public Long getEmailDeleteId() {
        return emailDeleteId;
    }

    public void setEmailDeleteId(Long emailDeleteId) {
        this.emailDeleteId = emailDeleteId;
    }

    public String getEditEmail() {
        return editEmail;
    }

    public void setEditEmail(String editEmail) {
        this.editEmail = editEmail;
    }

    public Long getEmailEditId() {
        return emailEditId;
    }

    public void setEmailEditId(Long emailEditId) {
        this.emailEditId = emailEditId;
    }

    public String getNewEmailDescription() {
        return newEmailDescription;
    }

    public void setNewEmailDescription(String newEmailDescription) {
        this.newEmailDescription = newEmailDescription;
    }

    public String getEditEmailDescription() {
        return editEmailDescription;
    }

    public void setEditEmailDescription(String editEmailDescription) {
        this.editEmailDescription = editEmailDescription;
    }

    public Boolean getLastStep() {
        return lastStep;
    }

    public void setLastStep(Boolean lastStep) {
        this.lastStep = lastStep;
    }

    public Long getReferentDeleteId() {
        return referentDeleteId;
    }

    public void setReferentDeleteId(Long referentDeleteId) {
        this.referentDeleteId = referentDeleteId;
    }

    public boolean isHasAgencyOffice() {
        return hasAgencyOffice;
    }

    public void setHasAgencyOffice(boolean hasAgencyOffice) {
        this.hasAgencyOffice = hasAgencyOffice;
    }

    public List<Agency> getAgencyOffices() {
        return agencyOffices;
    }

    public void setAgencyOffices(List<Agency> agencyOffices) {
        this.agencyOffices = agencyOffices;
    }

    @Override
    public Client getEntity() {
        return entity;
    }

    @Override
    public void setEntity(Client entity) {
        this.entity = entity;
    }

    public List<ClientView> getBillingRecipientTable() {
        return billingRecipientTable;
    }

    public void setBillingRecipientTable(List<ClientView> billingRecipientTable) {
        this.billingRecipientTable = billingRecipientTable;
    }

    public List<SelectItem> getBillingRecipients() {
        return billingRecipients;
    }

    public void setBillingRecipients(List<SelectItem> billingRecipients) {
        this.billingRecipients = billingRecipients;
    }

    public Long getSelectedClientId() {
        return selectedClientId;
    }

    public void setSelectedClientId(Long selectedClientId) {
        this.selectedClientId = selectedClientId;
    }

    public Long getDeleteClientId() {
        return deleteClientId;
    }

    public void setDeleteClientId(Long deleteClientId) {
        this.deleteClientId = deleteClientId;
    }

    public Long getClientIbanId() {
        return clientIbanId;
    }

    public void setClientIbanId(Long clientIbanId) {
        this.clientIbanId = clientIbanId;
    }

    public Long getFIXED_COST_TYPE_ID() {
        return FIXED_COST_TYPE_ID;
    }

    public Long getSALARY_COST_TYPE_ID() {
        return SALARY_COST_TYPE_ID;
    }

    public List<SelectItemWrapper<Area>> getAreas() {
        return areas;
    }

    public void setAreas(List<SelectItemWrapper<Area>> areas) {
        this.areas = areas;
    }

    public SelectItemWrapper<Area> getSelectedArea() {
        return selectedArea;
    }

    public void setSelectedArea(SelectItemWrapper<Area> selectedArea) {
        this.selectedArea = selectedArea;
    }

    public SelectItemWrapper<Office> getSelectedOffice() {
        return selectedOffice;
    }

    public void setSelectedOffice(SelectItemWrapper<Office> selectedOffice) {
        this.selectedOffice = selectedOffice;
    }

    public List<SelectItemWrapper<Office>> getOffices() {
        return offices;
    }

    public void setOffices(List<SelectItemWrapper<Office>> offices) {
        this.offices = offices;
    }

    public List<SelectItem> getIbans() {
        return ibans;
    }

    public void setIbans(List<SelectItem> ibans) {
        this.ibans = ibans;
    }

    public Boolean getSelectedCostOutput() {
        return selectedCostOutput;
    }

    public void setSelectedCostOutput(Boolean selectedCostOutput) {
        this.selectedCostOutput = selectedCostOutput;
    }

    public Boolean getRenderImportant() {
        return renderImportant;
    }

    public void setRenderImportant(Boolean renderImportant) {
        this.renderImportant = renderImportant;
    }

    public List<ClientView> getReferentRecipientTable() {
        return referentRecipientTable;
    }

    public void setReferentRecipientTable(List<ClientView> referentRecipientTable) {
        this.referentRecipientTable = referentRecipientTable;
    }

    public List<SelectItem> getReferentRecipients() {
        return referentRecipients;
    }

    public void setReferentRecipients(List<SelectItem> referentRecipients) {
        this.referentRecipients = referentRecipients;
    }

    public Long getSelectedReferentClientId() {
        return selectedReferentClientId;
    }

    public void setSelectedReferentClientId(Long selectedReferentClientId) {
        this.selectedReferentClientId = selectedReferentClientId;
    }

    public Long getMaximumFormalities() {
        return maximumFormalities;
    }

    public void setMaximumFormalities(Long maximumFormalities) {
        this.maximumFormalities = maximumFormalities;
    }

    public DualListModel<InvoiceColumnWrapper> getInvoiceColumns() {
        return invoiceColumns;
    }

    public void setInvoiceColumns(DualListModel<InvoiceColumnWrapper> invoiceColumns) {
        this.invoiceColumns = invoiceColumns;
    }

    public List<SelectItemWrapper<Area>> getSelectedAreas() {
        return selectedAreas;
    }

    public void setSelectedAreas(List<SelectItemWrapper<Area>> selectedAreas) {
        this.selectedAreas = selectedAreas;
    }

    public List<SelectItemWrapper<Office>> getSelectedOffices() {
        return selectedOffices;
    }

    public void setSelectedOffices(List<SelectItemWrapper<Office>> selectedOffices) {
        this.selectedOffices = selectedOffices;
    }

    public boolean isClientFiduciaryOrManager() {
        return clientFiduciaryOrManager;
    }

    public void setClientFiduciaryOrManager(boolean clientFiduciaryOrManager) {
        this.clientFiduciaryOrManager = clientFiduciaryOrManager;
    }

    public List<Office> getAllOfficesList() {
        return allOfficesList;
    }

    public void setAllOfficesList(List<Office> allOfficesList) {
        this.allOfficesList = allOfficesList;
    }

    public SelectItemWrapperConverter<Area> getAreaConverter() {
        return areaConverter;
    }

    public void setAreaConverter(SelectItemWrapperConverter<Area> areaConverter) {
        this.areaConverter = areaConverter;
    }

    public SelectItemWrapperConverter<Office> getOfficeConverter() {
        return officeConverter;
    }

    public void setOfficeConverter(SelectItemWrapperConverter<Office> officeConverter) {
        this.officeConverter = officeConverter;
    }

    public Long getSelectedLinkedClient() {
        return selectedLinkedClient;
    }

    public void setSelectedLinkedClient(Long selectedLinkedClient) {
        this.selectedLinkedClient = selectedLinkedClient;
    }

    public boolean isPriceListDialogOnlyView() {
        return priceListDialogOnlyView;
    }

    public void setPriceListDialogOnlyView(boolean priceListDialogOnlyView) {
        this.priceListDialogOnlyView = priceListDialogOnlyView;
    }

    public PriceList getNegativePriceList() {
        return negativePriceList;
    }

    public void setNegativePriceList(PriceList negativePriceList) {
        this.negativePriceList = negativePriceList;
    }

    public List<SelectItem> getRequestTypeNames() {
        return requestTypeNames;
    }

    public Long getSelectedBillingRequestTypeId() {
        return selectedBillingRequestTypeId;
    }

    public void setRequestTypeNames(List<SelectItem> requestTypeNames) {
        this.requestTypeNames = requestTypeNames;
    }

    public void setSelectedBillingRequestTypeId(Long selectedBillingRequestTypeId) {
        this.selectedBillingRequestTypeId = selectedBillingRequestTypeId;
    }

    public List<RequestTypeInvoiceColumnWrapper> getRequestTypeInvoiceColumns() {
        return requestTypeInvoiceColumns;
    }

    public void setRequestTypeInvoiceColumns(List<RequestTypeInvoiceColumnWrapper> requestTypeInvoiceColumns) {
        this.requestTypeInvoiceColumns = requestTypeInvoiceColumns;
    }

    public List<SelectItem> getNonManagerOrFiduciaryClientLists() {
        return nonManagerOrFiduciaryClientLists;
    }

    public void setNonManagerOrFiduciaryClientLists(List<SelectItem> nonManagerOrFiduciaryClientLists) {
        this.nonManagerOrFiduciaryClientLists = nonManagerOrFiduciaryClientLists;
    }

    public Long[] getSelectedNonManagerOrFiduciaryClientIds() {
        return selectedNonManagerOrFiduciaryClientIds;
    }

    public void setSelectedNonManagerOrFiduciaryClientIds(Long[] selectedNonManagerOrFiduciaryClientIds) {
        this.selectedNonManagerOrFiduciaryClientIds = selectedNonManagerOrFiduciaryClientIds;
    }

    public List<SelectItem> getPaymentTypes() {
        return paymentTypes;
    }

    public void setPaymentTypes(List<SelectItem> paymentTypes) {
        this.paymentTypes = paymentTypes;
    }

    public Long getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(Long paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public List<SelectItem> getTaxRates() {
        return taxRates;
    }

    public void setTaxRates(List<SelectItem> taxRates) {
        this.taxRates = taxRates;
    }

    public Boolean getFromContactList() {
        return fromContactList;
    }

    public void setFromContactList(Boolean fromContactList) {
        this.fromContactList = fromContactList;
    }

    public Long getSelectedClientIdToCopy() {
        return selectedClientIdToCopy;
    }

    public void setSelectedClientIdToCopy(Long selectedClientIdToCopy) {
        this.selectedClientIdToCopy = selectedClientIdToCopy;
    }

    public List<SelectItem> getClientsToCopy() {
        return clientsToCopy;
    }

    public void setClientsToCopy(List<SelectItem> clientsToCopy) {
        this.clientsToCopy = clientsToCopy;
    }

    public String getEmptyPriceListMessage() {
        return emptyPriceListMessage;
    }

    public void setEmptyPriceListMessage(String emptyPriceListMessage) {
        this.emptyPriceListMessage = emptyPriceListMessage;
    }

    public List<PriceList> getPriceListToCopy() {
        return priceListToCopy;
    }

    public void setPriceListToCopy(List<PriceList> priceListToCopy) {
        this.priceListToCopy = priceListToCopy;
    }
}