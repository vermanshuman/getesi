package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.enums.RequestOutputTypes;
import it.nexera.ris.common.enums.ServiceReferenceTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.*;
import org.bouncycastle.ocsp.Req;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dic_service")
public class Service extends IndexedEntity {

    private static final long serialVersionUID = -9222962593310335885L;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "icon")
    private String icon;

    @ManyToOne
    @JoinColumn(name = "request_type_id")
    private RequestType requestType;

    @ManyToMany
    @JoinTable(name = "service_cost", joinColumns =
            {
                    @JoinColumn(name = "service_id", table = "dic_service")
            }, inverseJoinColumns =
            {
                    @JoinColumn(name = "cost_id", table = "dic_cost_configuration")
            })
    private List<CostConfiguration> costConfigurations;

    @Column(name = "estimate")
    private Boolean estimate;

    @Column(name = "is_Update")
    private Boolean isUpdate;

    @Column(name = "is_negative")
    private Boolean isNegative;

    @Column(name = "is_national")
    private Boolean isNational;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_for_update_id")
    private Service serviceForUpdate;

    @OneToOne(cascade = CascadeType.REMOVE, mappedBy = "serviceForUpdate", fetch = FetchType.LAZY)
    private Service oldService;

    @ManyToMany
    @JoinTable(name = "service_cost_update", joinColumns =
            {
                    @JoinColumn(name = "service_id", table = "dic_service")
            }, inverseJoinColumns =
            {
                    @JoinColumn(name = "cost_id", table = "dic_cost_configuration")
            })
    private List<CostConfiguration> costConfigurationsUpdate;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "data_group_id")
    private DataGroup group;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_output_type")
    private RequestOutputTypes requestOutputType;

    @Column(name = "email_text")
    private String emailText;

    @Column(name = "fixed_compensation")
    private String fixedCompensation;

    @Column(name = "conservation_costs")
    private String conservationCosts;

    @Column(name = "national_price")
    private Double nationalPrice;

    @Column(name = "unauthorized_quote")
    private Boolean unauthorizedQuote;

    @ManyToMany
    @JoinTable(name = "service_cost_unauthorized_quote", joinColumns =
            {
                    @JoinColumn(name = "service_id", table = "dic_service")
            }, inverseJoinColumns =
            {
                    @JoinColumn(name = "cost_id", table = "dic_cost_configuration")
            })
    private List<CostConfiguration> serviceCostUnauthorizedQuoteList;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_data")
    private ServiceReferenceTypes serviceReferenceType;

    @Column(name = "sales_development")
    private Boolean salesDevelopment;

    @Column(name = "land_omi")
    private Boolean landOmi;
    
    @ManyToOne
    @JoinColumn(name = "national_tax_rate")
    private TaxRate nationalTaxRate;
    
    @Column(name = "manage_transcription")
    private Boolean manageTranscription;
    
    @Column(name = "manage_certification")
    private Boolean manageCertification;

    @Column(name = "detail_properties")
    private Boolean detailProperties ;

    @Column(name = "physical_subject")
    private Boolean physicalSubject ;

    @Column(name = "giuridic_subject")
    private Boolean giuridicSubject ;

    @ManyToMany
    @JoinTable(name = "aggregation_az_services", joinColumns =
            {
                    @JoinColumn(name = "service_id", table = "dic_service")
            }, inverseJoinColumns =
            {
                    @JoinColumn(name = "aggregation_service_id", table = "aggregation_service")
            })
    private List<AggregationService> aggregationServices;

    @Column(name = "manage_renewal")
    private Boolean manageRenewal ;

    @ManyToOne(fetch = FetchType.LAZY, cascade={CascadeType.ALL})
    @JoinColumn(name="supplier_id")
    private Supplier supplier;

    @Override
    public String toString() {
        return this.getName();
    }

    public String getEmailTextCamelCase() {
        if (!ValidationHelper.isNullOrEmpty(this.getEmailText())) {

            return getEmailText().substring(0, 1).toUpperCase() + getEmailText().substring(1).toLowerCase();
        } else {
            return "";
        }
    }

    public Boolean getIsUpdateAndNotNull() {
        if (!ValidationHelper.isNullOrEmpty(getIsUpdate()) && getIsUpdate()) {
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public List<CostConfiguration> getCostConfigurations() {
        return costConfigurations;
    }

    public void setCostConfigurations(
            List<CostConfiguration> costConfigurations) {
        this.costConfigurations = costConfigurations;
    }

    public Boolean getEstimate() {
        return estimate;
    }

    public void setEstimate(Boolean estimate) {
        this.estimate = estimate;
    }

    public Boolean getIsUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(Boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    public Service getServiceForUpdate() {
        return serviceForUpdate;
    }

    public void setServiceForUpdate(Service serviceForUpdate) {
        this.serviceForUpdate = serviceForUpdate;
    }

    public Service getOldService() {
        return oldService;
    }

    public void setOldService(Service oldService) {
        this.oldService = oldService;
    }

    public List<CostConfiguration> getCostConfigurationsUpdate() {
        return costConfigurationsUpdate;
    }

    public void setCostConfigurationsUpdate(
            List<CostConfiguration> costConfigurationsUpdate) {
        this.costConfigurationsUpdate = costConfigurationsUpdate;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public DataGroup getGroup() {
        return group;
    }

    public void setGroup(DataGroup group) {
        this.group = group;
    }

    public RequestOutputTypes getRequestOutputType() {
        return requestOutputType;
    }

    public void setRequestOutputType(RequestOutputTypes requestOutputType) {
        this.requestOutputType = requestOutputType;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconStr() {
        return icon == null ? "fa-square-o" : icon;
    }

    public String getEmailText() {
        return emailText;
    }

    public void setEmailText(String emailText) {
        this.emailText = emailText;
    }

    public Boolean getIsNegative() {
        return isNegative;
    }

    public void setIsNegative(Boolean negative) {
        isNegative = negative;
    }

    public String getFixedCompensation() {
        return fixedCompensation;
    }

    public void setFixedCompensation(String fixedCompensation) {
        this.fixedCompensation = fixedCompensation;
    }

    public String getConservationCosts() {
        return conservationCosts;
    }

    public void setConservationCosts(String conservationCosts) {
        this.conservationCosts = conservationCosts;
    }

    public Boolean getIsNational() {
        return isNational;
    }

    public void setIsNational(Boolean national) {
        isNational = national;
    }

    public Double getNationalPrice() {
        return nationalPrice;
    }

    public void setNationalPrice(Double nationalPrice) {
        this.nationalPrice = nationalPrice;
    }

    public Boolean getUnauthorizedQuote() {
        return unauthorizedQuote;
    }

    public void setUnauthorizedQuote(Boolean unauthorizedQuote) {
        this.unauthorizedQuote = unauthorizedQuote;
    }

    public List<CostConfiguration> getServiceCostUnauthorizedQuoteList() {
        return serviceCostUnauthorizedQuoteList;
    }

    public void setServiceCostUnauthorizedQuoteList(List<CostConfiguration> serviceCostUnauthorizedQuoteList) {
        this.serviceCostUnauthorizedQuoteList = serviceCostUnauthorizedQuoteList;
    }

    public ServiceReferenceTypes getServiceReferenceType() {
        return serviceReferenceType;
    }

    public void setServiceReferenceType(ServiceReferenceTypes serviceReferenceType) {
        this.serviceReferenceType = serviceReferenceType;
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
    
    public TaxRate getNationalTaxRate() {
		return nationalTaxRate;
	}

	public void setNationalTaxRate(TaxRate nationalTaxRate) {
		this.nationalTaxRate = nationalTaxRate;
	}

	public Boolean getManageTranscription() {
		return manageTranscription;
	}

	public void setManageTranscription(Boolean manageTranscription) {
		this.manageTranscription = manageTranscription;
	}

	public Boolean getManageCertification() {
		return manageCertification;
	}

	public void setManageCertification(Boolean manageCertification) {
		this.manageCertification = manageCertification;
	}

    public Boolean getDetailProperties() {
        return detailProperties;
    }

    public void setDetailProperties(Boolean detailProperties) {
        this.detailProperties = detailProperties;
    }

    public Boolean getPhysicalSubject() {
        return physicalSubject;
    }

    public void setPhysicalSubject(Boolean physicalSubject) {
        this.physicalSubject = physicalSubject;
    }

    public Boolean getGiuridicSubject() {
        return giuridicSubject;
    }

    public void setGiuridicSubject(Boolean giuridicSubject) {
        this.giuridicSubject = giuridicSubject;
    }

/*    public AggregationService getAggregationService() {
        return aggregationService;
    }

    public void setAggregationService(AggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }*/

    public List<AggregationService> getAggregationServices() {
        return aggregationServices;
    }

    public void setAggregationServices(List<AggregationService> aggregationServices) {
        this.aggregationServices = aggregationServices;
    }

    public Boolean getManageRenewal() {
        return manageRenewal;
    }

    public void setManageRenewal(Boolean manageRenewal) {
        this.manageRenewal = manageRenewal;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }
}
