package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ClientTitleType;
import it.nexera.ris.common.enums.ClientType;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@javax.persistence.Entity
@Table(name = "client")
public class Client extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -6304034073590084586L;

    @Column(name = "subject_invoice")
    private boolean subjectInvoice;

    @Column(name = "has_agency")
    private boolean hasAgency;

    @Column(name = "has_agency_office")
    private Boolean hasAgencyOffice;

    @Column(name = "has_headquarters")
    private boolean hasHeadquarters;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @Column(name = "name_of_the_company")
    private String nameOfTheCompany;

    @Column(name = "number_vat")
    private String numberVAT;

    @Column(name = "fiscal_code")
    private String fiscalCode;

    @Column(name = "address_street")
    private String addressStreet;

    @Column(name = "address_house_number")
    private String addressHouseNumber;

    @Column(name = "address_postal_code")
    private String addressPostalCode;

    @Column(name = "phone")
    private String phone;

    @Column(name = "cell")
    private String cell;

    @Column(name = "fax")
    private String fax;

    @Column(name = "mail_pec")
    private String mailPEC;

    @Column(name = "address_oph_street")
    private String addressOperationalHeadquartersStreet;

    @Column(name = "address_oph_house_number")
    private String addressOperationalHeadquartersHouseNumber;

    @Column(name = "address_oph_postal_code")
    private String addressOperationalHeadquartersPostalCode;

    @Column(name = "oph_phone")
    private String operationalHeadquartersPhone;

    @Column(name = "oph_cell")
    private String operationalHeadquartersCell;

    @Column(name = "oph_fax")
    private String operationalHeadquartersFax;

    @Column(name = "oph_mail")
    private String operationalHeadquartersMail;

    @Column(name = "legal_representative")
    private String legalRepresentative;

    @Column(name = "commercial_manager")
    private String commercialManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_province_id")
    private Province addressProvinceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_city_id")
    private City addressCityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_oph_province_id")
    private Province addressOperationalHeadquartersProvinceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_oph_city_id")
    private City addressOperationalHeadquartersCityId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "client_billing_recipient", joinColumns = {
            @JoinColumn(name = "client_id", table = "client")
    }, inverseJoinColumns = {
    		@JoinColumn(name = "recipient_id", table = "client")
    })
    List<Client> billingRecipientList;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "client_referent", joinColumns = {
            @JoinColumn(name = "client_id", table = "client")
    }, inverseJoinColumns = {
            @JoinColumn(name = "referent_id", table = "client")
    })
    List<Client> referentRecipientList;

    @OneToMany(mappedBy = "client",fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Agency> agencies;

    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "title_type_id")
    private Long titleTypeId;

    @Column(name = "name_professional")
    private String nameProfessional;

    @OneToMany(mappedBy = "client",fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ClientEmail> emails;

    @OneToMany(mappedBy = "client",fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Referent> referents;

    @Column(name = "foreign_country")
    private Boolean foreignCountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(name = "code")
    private String code;

    @Column(name = "split_payment")
    private Boolean splitPayment;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "client_payment_type", joinColumns = {
            @JoinColumn(name = "client_id", table = "client")
    }, inverseJoinColumns = {
            @JoinColumn(name = "payment_type_id", table = "payment_type")
    })
    private List<PaymentType> paymentTypeList;
    
    @Column(name = "cost_output")
    private Boolean costOutput;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    @ManyToOne
    @JoinColumn(name = "office_id")
    private Office office;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "client_office", joinColumns = {
            @JoinColumn(name = "client_id", table = "client")
    }, inverseJoinColumns = {
            @JoinColumn(name = "office_id", table = "dic_office")
    })
    private List<Office> offices;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "client_area", joinColumns = {
            @JoinColumn(name = "client_id", table = "client")
    }, inverseJoinColumns = {
            @JoinColumn(name = "area_id", table = "dic_area")
    })
    private List<Area> areas;

    @Column(name = "manager")
    private Boolean manager;

    @ManyToOne
    @JoinColumn(name = "iban_id")
    private Iban iban;

    @Column(name = "automatic_invoice")
    private Boolean automaticInvoice;

    @Column(name = "request_invoice")
    private Boolean requestInvoice;

    @Column(name = "monthly_invoice")
    private Boolean monthlyInvoice;

    @Column(name = "fiduciary")
    private Boolean fiduciary;

    @Column(name = "external")
    private Boolean external;

    @ManyToMany(mappedBy = "managers")
    private List<WLGInbox> wlgInboxes;

    @Column(name = "max_number_act")
    private Long maxNumberAct;

    @OneToMany(mappedBy = "client", cascade = CascadeType.REMOVE)
    private List<ClientInvoiceManageColumn> clientInvoiceManageColumns;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="client_id")
    private Client client;

    @OneToMany(mappedBy = "client",fetch = FetchType.LAZY)
    private List<Client> clients;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "client_reference", joinColumns = {
            @JoinColumn(name = "client_id", table = "client")
    }, inverseJoinColumns = {
            @JoinColumn(name = "reference_id", table = "client")
    })
    private List<Client> referenceClients;
    
    @OneToMany(mappedBy = "client",fetch = FetchType.LAZY)
    private List<WLGInbox> wlgInboxList;
    
    @Column(name = "client_name")
    private String clientName;
    
    @Column(name = "show_cadastral_income")
    private Boolean showCadastralIncome;
    
    @Column(name="show_agricultural_income")
    private Boolean showAgriculturalIncome;

    @Column(name = "sales_development")
    private Boolean salesDevelopment;

    @Column(name = "land_omi")
    private Boolean landOmi;

    @Column(name = "send_formality")
    private Boolean sendFormality;

    @Column(name = "send_sales_development_formality")
    private Boolean sendSalesDevelopmentFormality;

    @Column(name = "regime")
    private Boolean regime;

    @Column(name = "address_SDI")
    private String addressSDI;

    @Column(name = "unauthorized_cost_pay")
    private Double unauthorizedCostPay;

    @Column(name = "unauthorized_cost_cadastral")
    private Double unauthorizedCostCadastral;

    @Column(name = "unauthorized_cost_formality")
    private Double unauthorizedCostFormality;

    @Column(name = "invoice_expiration_days")
    private Double invoiceExpirationDays;
    
    @ManyToOne
    @JoinColumn(name = "tax_rate_unauthorized_cost_pay")
    private TaxRate taxRateUnauthorizedCostPay;
    
    @ManyToOne
    @JoinColumn(name = "tax_rate_unauthorized_cost_cadastral")
    private TaxRate taxRateUnauthorizedCostCadastral;
    
    @ManyToOne
    @JoinColumn(name = "tax_rate_unauthorized_cost_formality")
    private TaxRate taxRateUnauthorizedCostFormality;

    @Override
    public String toString() {
        if (ClientType.PROFESSIONAL.getId().equals(getTypeId())) {
            return getNameProfessional() == null ? "" : getNameProfessional();
        } else {
            return getNameOfTheCompany() == null ? "" : getNameOfTheCompany();
        }
    }

    public String getTypeDescription() {
        ClientType type = ClientType.findById(getTypeId());

        return type == null ? "" : type.toString();
    }

    public String getTitleTypeDescription() {
        ClientTitleType type = ClientTitleType.findById(getTitleTypeId());

        return type == null ? "" : type.toString();
    }

    public List<ClientEmail> getAdditionalEmails() {
        if (!ValidationHelper.isNullOrEmpty(getEmails())) {
            return getEmails().stream().filter(ClientEmail::isAdditional)
                    .collect(Collectors.toList());
        }

        return null;
    }

    public List<ClientEmail> getPersonalEmails() {
        if (!ValidationHelper.isNullOrEmpty(getEmails())) {
            return getEmails().stream().filter(ClientEmail::isPersonal)
                    .collect(Collectors.toList());
        }

        return null;
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

    public Boolean getDeleted() {
        return deleted != null ? deleted : Boolean.FALSE;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getNameOfTheCompany() {
        return nameOfTheCompany;
    }

    public void setNameOfTheCompany(String nameOfTheCompany) {
        this.nameOfTheCompany = nameOfTheCompany;
    }

    public String getNumberVAT() {
        return numberVAT;
    }

    public void setNumberVAT(String numberVAT) {
        this.numberVAT = numberVAT;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressHouseNumber() {
        return addressHouseNumber;
    }

    public void setAddressHouseNumber(String addressHouseNumber) {
        this.addressHouseNumber = addressHouseNumber;
    }

    public String getAddressPostalCode() {
        return addressPostalCode;
    }

    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = addressPostalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getMailPEC() {
        return mailPEC;
    }

    public void setMailPEC(String mailPEC) {
        this.mailPEC = mailPEC;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getOperationalHeadquartersCell() {
        return operationalHeadquartersCell;
    }

    public void setOperationalHeadquartersCell(
            String operationalHeadquartersCell) {
        this.operationalHeadquartersCell = operationalHeadquartersCell;
    }

    public String getAddressOperationalHeadquartersStreet() {
        return addressOperationalHeadquartersStreet;
    }

    public void setAddressOperationalHeadquartersStreet(
            String addressOperationalHeadquartersStreet) {
        this.addressOperationalHeadquartersStreet = addressOperationalHeadquartersStreet;
    }

    public String getAddressOperationalHeadquartersHouseNumber() {
        return addressOperationalHeadquartersHouseNumber;
    }

    public void setAddressOperationalHeadquartersHouseNumber(
            String addressOperationalHeadquartersHouseNumber) {
        this.addressOperationalHeadquartersHouseNumber = addressOperationalHeadquartersHouseNumber;
    }

    public String getAddressOperationalHeadquartersPostalCode() {
        return addressOperationalHeadquartersPostalCode;
    }

    public void setAddressOperationalHeadquartersPostalCode(
            String addressOperationalHeadquartersPostalCode) {
        this.addressOperationalHeadquartersPostalCode = addressOperationalHeadquartersPostalCode;
    }

    public String getOperationalHeadquartersPhone() {
        return operationalHeadquartersPhone;
    }

    public void setOperationalHeadquartersPhone(
            String operationalHeadquartersPhone) {
        this.operationalHeadquartersPhone = operationalHeadquartersPhone;
    }

    public String getOperationalHeadquartersFax() {
        return operationalHeadquartersFax;
    }

    public void setOperationalHeadquartersFax(String operationalHeadquartersFax) {
        this.operationalHeadquartersFax = operationalHeadquartersFax;
    }

    public String getOperationalHeadquartersMail() {
        return operationalHeadquartersMail;
    }

    public void setOperationalHeadquartersMail(
            String operationalHeadquartersMail) {
        this.operationalHeadquartersMail = operationalHeadquartersMail;
    }

    public String getLegalRepresentative() {
        return legalRepresentative;
    }

    public void setLegalRepresentative(String legalRepresentative) {
        this.legalRepresentative = legalRepresentative;
    }

    public String getCommercialManager() {
        return commercialManager;
    }

    public void setCommercialManager(String commercialManager) {
        this.commercialManager = commercialManager;
    }

    public Province getAddressProvinceId() {
        return addressProvinceId;
    }

    public void setAddressProvinceId(Province addressProvinceId) {
        this.addressProvinceId = addressProvinceId;
    }

    public City getAddressCityId() {
        return addressCityId;
    }

    public void setAddressCityId(City addressCityId) {
        this.addressCityId = addressCityId;
    }

    public Province getAddressOperationalHeadquartersProvinceId() {
        return addressOperationalHeadquartersProvinceId;
    }

    public void setAddressOperationalHeadquartersProvinceId(
            Province addressOperationalHeadquartersProvinceId) {
        this.addressOperationalHeadquartersProvinceId = addressOperationalHeadquartersProvinceId;
    }

    public City getAddressOperationalHeadquartersCityId() {
        return addressOperationalHeadquartersCityId;
    }

    public void setAddressOperationalHeadquartersCityId(
            City addressOperationalHeadquartersCityId) {
        this.addressOperationalHeadquartersCityId = addressOperationalHeadquartersCityId;
    }

    public List<Agency> getAgencies() {
        return agencies;
    }

    public void setAgencies(List<Agency> agencies) {
        this.agencies = agencies;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getTitleTypeId() {
        return titleTypeId;
    }

    public void setTitleTypeId(Long titleTypeId) {
        this.titleTypeId = titleTypeId;
    }

    public String getNameProfessional() {
        return nameProfessional;
    }

    public void setNameProfessional(String nameProfessional) {
        this.nameProfessional = nameProfessional;
    }

    public List<ClientEmail> getEmails() {
        return emails;
    }

    public void setEmails(List<ClientEmail> emails) {
        this.emails = emails;
    }

    public List<Referent> getReferents() {
        return referents;
    }

    public void setReferents(List<Referent> referents) {
        this.referents = referents;
    }

    public Boolean getForeignCountry() {
        return foreignCountry;
    }

    public void setForeignCountry(Boolean foreignCountry) {
        this.foreignCountry = foreignCountry;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Boolean getHasAgencyOffice() {
        return hasAgencyOffice;
    }

    public void setHasAgencyOffice(Boolean hasAgencyOffice) {
        this.hasAgencyOffice = hasAgencyOffice;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Client> getBillingRecipientList() {
        return billingRecipientList;
    }

    public void setBillingRecipientList(List<Client> billingRecipientList) {
        this.billingRecipientList = billingRecipientList;
    }

    public Boolean getSplitPayment() {
        return splitPayment;
    }

    public void setSplitPayment(Boolean splitPayment) {
        this.splitPayment = splitPayment;
    }

    public Boolean getCostOutput() {
        return costOutput;
    }

    public void setCostOutput(Boolean costOutput) {
        this.costOutput = costOutput;
    }

	public List<PaymentType> getPaymentTypeList() {
		return paymentTypeList;
	}

	public void setPaymentTypeList(List<PaymentType> paymentTypeList) {
		this.paymentTypeList = paymentTypeList;
	}

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Boolean getManager() {
        return manager;
    }

    public void setManager(Boolean manager) {
        this.manager = manager;
    }

    public Iban getIban() {
        return iban;
    }

    public void setIban(Iban iban) {
        this.iban = iban;
    }

    public Boolean getAutomaticInvoice() {
        return automaticInvoice;
    }

    public void setAutomaticInvoice(Boolean automaticInvoice) {
        this.automaticInvoice = automaticInvoice;
    }

    public Boolean getRequestInvoice() {
        return requestInvoice;
    }

    public void setRequestInvoice(Boolean requestInvoice) {
        this.requestInvoice = requestInvoice;
    }

    public Boolean getMonthlyInvoice() {
        return monthlyInvoice;
    }

    public void setMonthlyInvoice(Boolean monthlyInvoice) {
        this.monthlyInvoice = monthlyInvoice;
    }

    public Boolean getFiduciary() {
        return fiduciary;
    }

    public void setFiduciary(Boolean fiduciary) {
        this.fiduciary = fiduciary;
    }

    public List<Client> getReferentRecipientList() {
        return referentRecipientList;
    }

    public void setReferentRecipientList(List<Client> referentRecipientList) {
        this.referentRecipientList = referentRecipientList;
    }

    public Boolean getExternal() { return external; }

    public void setExternal(Boolean external) { this.external = external; }

    public List<WLGInbox> getWlgInboxes() {
        return wlgInboxes;
    }

    public void setWlgInboxes(List<WLGInbox> wlgInboxes) {
        this.wlgInboxes = wlgInboxes;
    }

    public Long getMaxNumberAct() {
        return maxNumberAct;
    }

    public void setMaxNumberAct(Long maxNumberAct) {
        this.maxNumberAct = maxNumberAct;
    }

	public List<ClientInvoiceManageColumn> getClientInvoiceManageColumns() {
		return clientInvoiceManageColumns;
	}

	public void setClientInvoiceManageColumns(List<ClientInvoiceManageColumn> clientInvoiceManageColumns) {
		this.clientInvoiceManageColumns = clientInvoiceManageColumns;
	}

    public List<Office> getOffices() {
        return offices;
    }

    public void setOffices(List<Office> offices) {
        this.offices = offices;
    }

    public List<Area> getAreas() {
        return areas;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public List<WLGInbox> getWlgInboxList() {
        return wlgInboxList;
    }

    public void setWlgInboxList(List<WLGInbox> wlgInboxList) {
        this.wlgInboxList = wlgInboxList;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    public Boolean getShowCadastralIncome() {
        return showCadastralIncome;
    }

    public void setShowCadastralIncome(Boolean showCadastralIncome) {
        this.showCadastralIncome = showCadastralIncome;
    }

    public Boolean getShowAgriculturalIncome() {
        return showAgriculturalIncome;
    }

    public void setShowAgriculturalIncome(Boolean showAgriculturalIncome) {
        this.showAgriculturalIncome = showAgriculturalIncome;
    }

	public List<Client> getReferenceClients() {
		return referenceClients;
	}

	public void setReferenceClients(List<Client> referenceClients) {
		this.referenceClients = referenceClients;
	}

    public Boolean getSalesDevelopment() {
        return salesDevelopment;
    }

    public void setSalesDevelopment(Boolean salesDevelopment) {
        this.salesDevelopment = salesDevelopment;
    }

    public Boolean getLandOmi() {
        return landOmi;
    }

    public void setLandOmi(Boolean landOmi) {
        this.landOmi = landOmi;
    }

    public Boolean getSendFormality() {
        return sendFormality;
    }

    public void setSendFormality(Boolean sendFormality) {
        this.sendFormality = sendFormality;
    }

    public Boolean getSendSalesDevelopmentFormality() {
        return sendSalesDevelopmentFormality;
    }

    public void setSendSalesDevelopmentFormality(Boolean sendSalesDevelopmentFormality) {
        this.sendSalesDevelopmentFormality = sendSalesDevelopmentFormality;
    }

    public Boolean getRegime() {
        return regime;
    }

    public void setRegime(Boolean regime) {
        this.regime = regime;
    }

    public String getAddressSDI() {
        return addressSDI;
    }

    public void setAddressSDI(String addressSDI) {
        this.addressSDI = addressSDI;
    }

    public Double getUnauthorizedCostPay() {
        return unauthorizedCostPay;
    }

    public void setUnauthorizedCostPay(Double unauthorizedCostPay) {
        this.unauthorizedCostPay = unauthorizedCostPay;
    }

    public Double getUnauthorizedCostCadastral() {
        return unauthorizedCostCadastral;
    }

    public void setUnauthorizedCostCadastral(Double unauthorizedCostCadastral) {
        this.unauthorizedCostCadastral = unauthorizedCostCadastral;
    }

    public Double getUnauthorizedCostFormality() {
        return unauthorizedCostFormality;
    }

    public void setUnauthorizedCostFormality(Double unauthorizedCostFormality) {
        this.unauthorizedCostFormality = unauthorizedCostFormality;
    }

    public Double getInvoiceExpirationDays() {
        return invoiceExpirationDays;
    }

    public void setInvoiceExpirationDays(Double invoiceExpirationDays) {
        this.invoiceExpirationDays = invoiceExpirationDays;
    }

	public TaxRate getTaxRateUnauthorizedCostPay() {
		return taxRateUnauthorizedCostPay;
	}

	public void setTaxRateUnauthorizedCostPay(TaxRate taxRateUnauthorizedCostPay) {
		this.taxRateUnauthorizedCostPay = taxRateUnauthorizedCostPay;
	}

	public TaxRate getTaxRateUnauthorizedCostCadastral() {
		return taxRateUnauthorizedCostCadastral;
	}

	public void setTaxRateUnauthorizedCostCadastral(TaxRate taxRateUnauthorizedCostCadastral) {
		this.taxRateUnauthorizedCostCadastral = taxRateUnauthorizedCostCadastral;
	}

	public TaxRate getTaxRateUnauthorizedCostFormality() {
		return taxRateUnauthorizedCostFormality;
	}

	public void setTaxRateUnauthorizedCostFormality(TaxRate taxRateUnauthorizedCostFormality) {
		this.taxRateUnauthorizedCostFormality = taxRateUnauthorizedCostFormality;
	}
    
}
