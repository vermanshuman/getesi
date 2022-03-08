package it.nexera.ris.persistence.beans.entities.domain;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollMode;
import org.hibernate.Session;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import it.nexera.ris.common.enums.NoteType;
import it.nexera.ris.common.enums.RequestEnumTypes;
import it.nexera.ris.common.enums.RequestState;
import it.nexera.ris.common.enums.SectionCType;
import it.nexera.ris.common.enums.UserCategories;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.EstateSituationHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.SubjectHelper;
import it.nexera.ris.common.helpers.TemplatePdfTableHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.DocumentTagEntity;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.persistence.interfaces.BeforeSave;

@Entity
@Table(name = "request")
public class Request extends DocumentTagEntity implements BeforeSave {

    public static transient final Log log = LogFactory.getLog(IndexedEntity.class);

    private static final long serialVersionUID = 3895721198863249854L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private Agency agency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id")
    private Agency office;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notary_id")
    private Notary notary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_client_id")
    private Client billingClient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_type_id", nullable = false)
    private RequestType requestType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "request_service", joinColumns = {
            @JoinColumn(name = "request_id", table = "request")
    }, inverseJoinColumns = {
            @JoinColumn(name = "service_id", table = "dic_service")
    })
    private List<Service> multipleServices;

    @Column(name = "authorized_quote")
    private Boolean authorizedQuote;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "request_manager", joinColumns = {
            @JoinColumn(name = "request_id", table = "request")
    }, inverseJoinColumns = {
            @JoinColumn(name = "client_id", table = "client")
    })
    private List<Client> requestMangerList;

    @Column(name = "state_id")
    private Long stateId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private RequestEnumTypes type;

    @Column(name = "subject_type_id")
    private Long subjectTypeId;

    @Column(name = "property_type_id")
    private Long propertyTypeId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "birth_date")
    private Date birthDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    private Province province;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(name = "sex_id")
    private Long sexId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aggregation_land_char_reg_id")
    private AggregationLandChargesRegistry aggregationLandChargesRegistry;

    @Column(name = "foreign_country")
    private Boolean foreignCountry;

    @Column(name = "legal")
    private Boolean legal;

    @Column(name = "estimate")
    private Boolean estimate;

    @Column(name = "urgent")
    private Boolean urgent;

    @Column(name = "section", length = 30)
    private String section;

    @Column(name = "sheet", length = 30)
    private String sheet;

    @Column(name = "particle", length = 30)
    private String particle;

    @Column(name = "sub", length = 30)
    private String sub;

    @Column(name = "address_property")
    private String addressProperty;

    @Column(name = "formality_authorized")
    private Long formalityAuthorized;

    @Column(name = "cdr")
    private String cdr;

    @Column(name = "ndg")
    private String ndg;

    @Column(name = "ultima_residenza")
    private String ultimaResidenza;

    @Column(name = "note")
    private String note;

    @Column(name = "actType")
    private String actType;

    @Column(name = "actNumber")
    private String actNumber;

    @Column(name = "actDate")
    private Date actDate;

    @Column(name = "termDate")
    private Date termDate;

    @Column(name = "reaNumber")
    private String reaNumber;

    @Column(name = "natureLegal")
    private String natureLegal;

    @Column(name = "istat")
    private String istat;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "residence")
    private Residence residence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domicile")
    private Residence domicile;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToMany
    @JoinTable(name = "request_document", joinColumns =
            {
                    @JoinColumn(name = "request_id", table = "request")
            }, inverseJoinColumns =
            {
                    @JoinColumn(name = "document_id", table = "document")
            })
    private List<Document> documents;

    @ManyToMany(mappedBy = "invoiceRequests",fetch = FetchType.LAZY)
    private Set<Document> invoiceDocuments = new HashSet<>();

    @OneToMany(mappedBy = "request",fetch = FetchType.LAZY)
    private List<Document> documentsRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_print_id")
    private RequestPrint requestPrint;

    @Column(name = "applicant", length = 200)
    private String applicant;

    @Column(name = "manager", length = 300)
    private String manager;

    @Column(name = "position", length = 200)
    private String position;

    @Column(name = "total_cost")
    private String totalCost;

    @Column(name = "save_cost_in_draft")
    private Boolean saveCostInDraft;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "update_request_id")
    private Request updateRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_id")
    private WLGInbox mail;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "comment")
    private String comment;

    @OneToMany(mappedBy = "request",fetch = FetchType.LAZY)
    private List<Comment> comments;

    @OneToMany(mappedBy = "request",fetch = FetchType.LAZY)
    private List<EstateSituation> situationEstateLocations;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "request_formality_update", joinColumns = {
            @JoinColumn(name = "request_id", table = "request")
    }, inverseJoinColumns = {
            @JoinColumn(name = "formality_id", table = "estate_formality")
    })
    private List<EstateFormality> estateFormalityListUpdate;

    @Transient
    private List<EstateFormality> estateFormalityList;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "request_formalitypdf", joinColumns = {
            @JoinColumn(name = "request_id", table = "request")
    }, inverseJoinColumns = {
            @JoinColumn(name = "formality_id", table = "formality")
    })
    private List<Formality> formalityPdfList;

    @ManyToMany(mappedBy = "requestForcedList",fetch = FetchType.LAZY)
    private List<Formality> formalityForcedList;

    @ManyToMany(mappedBy = "requestList",fetch = FetchType.LAZY)
    private List<Property> propertyList;

    @Column(name = "evasion_date")
    private Date evasionDate;

    @Column(name = "user_area_id")
    private Long userAreaId;

    @Column(name = "user_office_id")
    private Long userOfficeId;

    @Column(name = "init_cost")
    private String initCost;

    @Column(name = "number_act_update")
    private Double numberActUpdate;

    @Column(name = "cost_forced")
    private Double costForced;

    @Column(name = "cost_button_confirm_clicked")
    private Boolean costButtonConfirmClicked;

    @Column(name = "sent")
    private Boolean sent;

    @Column(name = "cost_cadastral")
    private Double costCadastral;

    @Column(name = "cost_extra")
    private Double costExtra;

    @Column(name = "cost_estate_formality")
    private Double costEstateFormality;

    @Column(name = "cost_pay")
    private Double costPay;

    @Column(name = "confirm_extra_costs_pressed")
    private Boolean confirmExtraCostsPressed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distraint_act_id")
    private Formality distraintFormality;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "taxable")
    private Double taxable;

    @Column(name = "comment_certification", columnDefinition = "TEXT")
    private String commentCertification;

//    @ManyToMany
//    @JoinTable(name = "request_subject", joinColumns = {
//            @JoinColumn(name = "request_id", table = "request")
//    }, inverseJoinColumns = {
//            @JoinColumn(name = "subject_id", table = "subject")
//    })
//    private List<Subject> subjectList;
//    
//    @OneToMany(mappedBy = "request", cascade = CascadeType.REFRESH)
//    private List<RequestFormality> requestFormalities;


    @OneToMany(mappedBy = "request", cascade = CascadeType.REFRESH)
    private List<RequestFormality> requestFormalities;

    @OneToMany(mappedBy = "request", cascade = CascadeType.REFRESH)
    private List<RequestSubject> requestSubjects;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcription_act_id")
    private Formality transcriptionActId;

    @Column(name = "cost_note", columnDefinition = "TEXT")
    private String costNote;

    @Column(name= "include_national_cost")
    private Boolean includeNationalCost;

    @Column(name = "regime")
    private Boolean regime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Transient
    private Boolean haveRequestReport;

    @Transient
    private boolean lastCostChanging;

    @Transient
    private boolean isCalledFromReportList;

    @Transient
    private Subject subjectToGenerateAlienatedTemplate;

    @Transient
    private Map<Long, String> subjectTypeMapping;

    @Transient
    private List<Subject> subjectList;

    @Transient
    private String tempId;

    @Transient
    private boolean selectedForMail;

    @Transient
    private String excelUserName;

    @Transient
    private boolean selected = true;

    @Transient
    private Boolean salesDevelopment;

    public Boolean getHaveRequestReport() {
        if (haveRequestReport == null) {
            try {
                haveRequestReport = DaoManager.getSession() // type=REQUEST_REPORT
                        .createQuery("select 1 from Document where request= :request_id and typeId=6")
                        .setLong("request_id", getId())
                        .setFetchSize(1).scroll(ScrollMode.FORWARD_ONLY).next();
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }
        }
        return haveRequestReport;
    }

    public Request copy() throws CloneNotSupportedException {
        Request request = new Request();
        request.setClient(getClient());
        request.setAgency(getAgency());
        request.setOffice(getOffice());
        request.setBillingClient(getBillingClient());
        request.setRequestType(getRequestType());
        request.setService(getService());
        if (!ValidationHelper.isNullOrEmpty(getMultipleServices())) {
            request.setMultipleServices(getMultipleServices());
        }
        request.setStateId(getStateId());
        request.setType(getType());
        request.setSubjectTypeId(getSubjectTypeId());
        request.setPropertyTypeId(getPropertyTypeId());
        request.setFirstName(getFirstName());
        request.setLastName(getLastName());
        request.setBirthDate(getBirthDate());
        request.setProvince(getProvince());
        request.setCity(getCity());
        request.setCountry(getCountry());
        request.setSexId(getSexId());
        request.setAggregationLandChargesRegistry(getAggregationLandChargesRegistry());
        request.setForeignCountry(getForeignCountry());
        request.setLegal(getLegal());
        request.setEstimate(getEstimate());
        request.setUrgent(getUrgent());
        request.setAddressProperty(getAddressProperty());
        request.setFormalityAuthorized(getFormalityAuthorized());
        request.setCdr(getCdr());
        request.setNdg(getNdg());
        request.setNote(getNote());
        request.setActType(getActType());
        request.setActNumber(getActNumber());
        request.setActDate(getActDate());
        request.setTermDate(getTermDate());
        request.setReaNumber(getReaNumber());
        request.setNatureLegal(getNatureLegal());
        request.setIstat(getIstat());
        if (!ValidationHelper.isNullOrEmpty(getResidence())) {
            request.setResidence(new Residence(getResidence()));
        }
        if (!ValidationHelper.isNullOrEmpty(getDomicile())) {
            request.setDomicile(new Residence(getDomicile()));
        }
        if (!ValidationHelper.isNullOrEmpty(getDocuments())) {
            List<Document> newDocuments = new ArrayList<>();
            for (Document document : getDocuments()) {
                Document newDocument = document.clone();
                newDocuments.add(newDocument);
            }
            request.setDocuments(newDocuments);
        } else {
            request.setDocuments(new ArrayList<>());
        }
        request.setUser(getUser());
        request.setSubject(getSubject());
        request.setApplicant(getApplicant());
        request.setManager(getManager());
        request.setPosition(getPosition());
        request.setMail(getMail());
        request.setSection(getSection());
        request.setSheet(getSheet());
        request.setParticle(getParticle());
        request.setSub(getSub());
        request.setExpirationDate(getExpirationDate());
        request.setComment(getComment());
        request.setUltimaResidenza(getUltimaResidenza());
        request.setCreateUserId(getCreateUserId());
        request.setCreateDate(getCreateDate());
        request.setCostForced(getCostForced());


        if (!ValidationHelper.isNullOrEmpty(getUser()) && !ValidationHelper.isNullOrEmpty(getUser().getArea())) {
            request.setUserAreaId(getUser().getArea().getId());
            if (!ValidationHelper.isNullOrEmpty(getUser().getOffice())) {
                request.setUserOfficeId(getUser().getOffice().getId());
            }
        } else {
            request.setUserAreaId(getUserAreaId());
            request.setUserOfficeId(getUserOfficeId());
        }

        request.setDistraintFormality(getDistraintFormality());

        if(!ValidationHelper.isNullOrEmpty(getRequestMangerList())) {
            request.setRequestMangerList(new ArrayList<Client>());
            request.getRequestMangerList().addAll(getRequestMangerList());
        }
        return request;
    }

    public Request reportCopy() throws CloneNotSupportedException {
        Request request = new Request();
        request.setClient(getClient());
        request.setAgency(getAgency());
        request.setOffice(getOffice());
        request.setBillingClient(getBillingClient());
        request.setRequestType(getRequestType());
        request.setService(getService());
        if (!ValidationHelper.isNullOrEmpty(getMultipleServices())) {
            request.setMultipleServices(getMultipleServices());
        }
        request.setStateId(getStateId());
        request.setType(getType());
        request.setSubjectTypeId(getSubjectTypeId());
        request.setPropertyTypeId(getPropertyTypeId());
        request.setFirstName(getFirstName());
        request.setLastName(getLastName());
        request.setBirthDate(getBirthDate());
        request.setProvince(getProvince());
        request.setCity(getCity());
        request.setCountry(getCountry());
        request.setSexId(getSexId());
        request.setAggregationLandChargesRegistry(getAggregationLandChargesRegistry());
        request.setForeignCountry(getForeignCountry());
        request.setLegal(getLegal());
        request.setEstimate(getEstimate());
        request.setUrgent(getUrgent());
        request.setAddressProperty(getAddressProperty());
        request.setFormalityAuthorized(getFormalityAuthorized());
        request.setCdr(getCdr());
        request.setNdg(getNdg());
        request.setNote(getNote());
        request.setActType(getActType());
        request.setActNumber(getActNumber());
        request.setActDate(getActDate());
        request.setTermDate(getTermDate());
        request.setReaNumber(getReaNumber());
        request.setNatureLegal(getNatureLegal());
        request.setIstat(getIstat());
        if (!ValidationHelper.isNullOrEmpty(getResidence())) {
            request.setResidence(new Residence(getResidence()));
        }
        if (!ValidationHelper.isNullOrEmpty(getDomicile())) {
            request.setDomicile(new Residence(getDomicile()));
        }
        if (!ValidationHelper.isNullOrEmpty(getDocuments())) {
            List<Document> newDocuments = new ArrayList<>();
            for (Document document : getDocuments()) {
                Document newDocument = document.clone();
                newDocuments.add(newDocument);
            }
            request.setDocuments(newDocuments);
        } else {
            request.setDocuments(new ArrayList<>());
        }
        request.setUser(getUser());
        request.setSubject(getSubject());
        request.setApplicant(getApplicant());
        request.setManager(getManager());
        request.setPosition(getPosition());
        request.setMail(getMail());
        request.setSection(getSection());
        request.setSheet(getSheet());
        request.setParticle(getParticle());
        request.setSub(getSub());
        request.setExpirationDate(getExpirationDate());
        request.setComment(getComment());
        request.setUltimaResidenza(getUltimaResidenza());
        request.setCreateUserId(getCreateUserId());
        request.setCreateDate(getCreateDate());
        request.setInitCost(getInitCost());
        request.setNumberActUpdate(getNumberActUpdate());
        request.setCostForced(getCostForced());
        request.setSent(getSent());

        if (!ValidationHelper.isNullOrEmpty(getUser()) && !ValidationHelper.isNullOrEmpty(getUser().getArea())) {
            request.setUserAreaId(getUser().getArea().getId());
            if (!ValidationHelper.isNullOrEmpty(getUser().getOffice())) {
                request.setUserOfficeId(getUser().getOffice().getId());
            }
        } else {
            request.setUserAreaId(getUserAreaId());
            request.setUserOfficeId(getUserOfficeId());
        }

        request.setDistraintFormality(getDistraintFormality());

        return request;
    }

    @Override
    public void beforeSave() {
        if (getRequestFormalities() == null && getRequestSubjects() == null) {
            return;
        }
        if(getRequestFormalities() != null) {
            List<RequestFormality> listToRemove = new ArrayList<>();
            List<RequestFormality> listToAdd = new ArrayList<>();
            emptyIfNull(getRequestFormalities()).stream()
                    .filter(f -> emptyIfNull(getEstateFormalityList()).stream().noneMatch(r -> r.getId().equals(f.getFormality().getId())))
                    .forEach(r -> {
                                DaoManager.removeWeak(r, false);
                                listToRemove.add(r);
                            }
                    );
            getRequestFormalities().removeAll(listToRemove);
            emptyIfNull(getEstateFormalityList()).stream()
                    .filter(f -> emptyIfNull(getRequestFormalities()).stream().noneMatch(r -> r.getFormality().getId().equals(f.getId())))
                    .forEach(r -> {
                                RequestFormality requestFormality = new RequestFormality(this, r);
                                DaoManager.saveWeak(requestFormality, false);
                                listToAdd.add(requestFormality);
                            }
                    );
            getRequestFormalities().addAll(listToAdd);
        }

        if(getRequestSubjects() != null) {
            List<RequestSubject> listToRemove = new ArrayList<>();
            List<RequestSubject> listToAdd = new ArrayList<>();
            try {
                if(!Hibernate.isInitialized(getRequestSubjects()))
                    Hibernate.initialize(getRequestSubjects());
            } catch (Exception e) {
                LogHelper.debugInfo(log, "Error in initializing request subjects");
            }
            emptyIfNull(getRequestSubjects()).stream()
                    .filter(f -> emptyIfNull(getSubjectList()).stream().noneMatch(r -> r.getId().equals(f.getSubject().getId())))
                    .forEach(r -> {
                                DaoManager.removeWeak(r, false);
                                listToRemove.add(r);
                            }
                    );
            getRequestSubjects().removeAll(listToRemove);
            emptyIfNull(getSubjectList()).stream()
                    .filter(f -> emptyIfNull(getRequestSubjects()).stream().noneMatch(r -> r.getSubject().getId().equals(f.getId())))
                    .forEach(r -> {
                                RequestSubject requestSubject = new RequestSubject(this, r);
                                Entry<Long, String> result = getSubjectTypeMapping().entrySet().stream()
                                        .filter(x -> x.getKey() == r.getId()).findFirst().orElse(null);
                                if(!ValidationHelper.isNullOrEmpty(result)) {
                                    requestSubject.setType(result.getValue());
                                }
                                DaoManager.saveWeak(requestSubject, false);
                                listToAdd.add(requestSubject);
                            }
                    );
            getRequestSubjects().addAll(listToAdd);
        }

    }

    public boolean getCostOutputCheck() {
        if (!ValidationHelper.isNullOrEmpty(this.getBillingClient())
                && !ValidationHelper.isNullOrEmpty(this.getBillingClient().getCostOutput())
                && this.getBillingClient().getCostOutput()) {
            return true;
        } else if (!ValidationHelper.isNullOrEmpty(this.getClient())
                && !ValidationHelper.isNullOrEmpty(this.getClient().getCostOutput())
                && this.getClient().getCostOutput()) {
            return true;
        }
        return false;
    }

    public String getConservatoryName() {
        if (!ValidationHelper.isNullOrEmpty(getAggregationLandChargesRegistry()) &&
                !ValidationHelper.isNullOrEmpty(getAggregationLandChargesRegistry().getLandChargesRegistries()) &&
                !ValidationHelper.isNullOrEmpty(getAggregationLandChargesRegistry().getLandChargesRegistries().get(0))) {
            return getAggregationLandChargesRegistry().getLandChargesRegistries().get(0).getName();
        }
        return "";
    }

    public String getSubjectName() {
        return !ValidationHelper.isNullOrEmpty(getSubject()) ? getSubject().getName() : null;
    }

    public String getLastBusinessName() {
        if (getSubject() != null) {
            if (getSubject().getTypeIsPhysicalPerson()) {
                return getSubject().getSurname();
            } else {
                return getSubject().getBusinessName();
            }
        }

        return "";
    }

    public String getFiscalCodeVATNamber() {
        if (getSubject() != null) {
            if (getSubject().getTypeIsPhysicalPerson()) {
                return getSubject().getFiscalCode();
            } else {
                return getSubject().getNumberVAT();
            }
        }

        return "";
    }

    public String getRequestUpdateDate() throws PersistenceBeanException, IllegalAccessException {
        return TemplatePdfTableHelper.getEstateFormalityConservationDate(this);
    }

    public List<Double> getSumOfGroupedEstateFormalities() throws PersistenceBeanException, IllegalAccessException {
        if(!Hibernate.isInitialized(this.getRequestFormalities())){
            reloadRequestFormalities();
        }
        List<Double> sum = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(this.getRequestFormalities())) {
            Map<Long, List<RequestFormality>> groups = this.getRequestFormalities().stream().filter(x -> x.getDocumentId() != null)
                    .collect(Collectors.groupingBy(RequestFormality::getDocumentId));
            for (Map.Entry<Long, List<RequestFormality>> entry : groups.entrySet()) {
                List<EstateFormality> estateFormalities = entry.getValue().stream().map(RequestFormality::getFormality).collect(Collectors.toList());
                int numberOfRelatedCommunications = (int) estateFormalities.stream()
                        .filter(estate -> estate.getCommunications() != null)
                        .mapToLong(e -> e.getCommunications().size()).sum();
                for (EstateFormality estateFormality : estateFormalities) {
                    if (!Hibernate.isInitialized(estateFormality.getEstateFormalitySuccessList())) {
                        try {
                            estateFormality.reloadEstateFormalitySuccessList();
                        } catch (PersistenceBeanException | IllegalAccessException e) {
                            LogHelper.log(log, e);
                        }
                    }
                }
                int numberOfRelatedEstateFormalitySuccess = (int) estateFormalities.stream()
                        .filter(estate -> estate.getEstateFormalitySuccessList() != null)
                        .flatMap(e -> e.getEstateFormalitySuccessList().stream())
                        .filter(success -> NoteType.NOTE_TYPE_A.equals(success.getNoteType()))
                        .count();
                sum.add((double)(estateFormalities.size() + numberOfRelatedCommunications + numberOfRelatedEstateFormalitySuccess));
            }
//            this.getRequestFormalities().stream().filter(x -> x.getDocumentId() != null)
//                    .collect(Collectors.groupingBy(RequestFormality::getDocumentId))
//                    .values().forEach(x -> sum.add((double) x.size()));
        }
        return sum;
    }

    public void reloadRequestFormalities() throws PersistenceBeanException, IllegalAccessException {
        setRequestFormalities(DaoManager.load(RequestFormality.class, new Criterion[]{
                Restrictions.eq("request.id", this.getId())
        }, new CriteriaAlias[]{
                new CriteriaAlias("formality", "f", JoinType.INNER_JOIN)}));
    }


    public Integer getSumOfEstateFormalities() {
        if (!ValidationHelper.isNullOrEmpty(this.getEstateFormalityList())) {
            return this.getEstateFormalityList().size();
        } else {
            return 0;
        }
    }

    public Integer getSumOfEstateFormalitiesAndCommunicationsAndSuccess() {
        if (!ValidationHelper.isNullOrEmpty(this.getEstateFormalityList())) {
            int numberOfRelatedCommunications = (int) this.getEstateFormalityList().stream()
                    .filter(estate -> estate.getCommunications() != null)
                    .mapToLong(e -> e.getCommunications().size()).sum();
            for (EstateFormality estateFormality : this.getEstateFormalityList()) {
                if (!Hibernate.isInitialized(estateFormality.getEstateFormalitySuccessList())) {
                    try {
                        estateFormality.reloadEstateFormalitySuccessList();
                    } catch (PersistenceBeanException | IllegalAccessException e) {
                        LogHelper.log(log, e);
                    }
                }
            }
            int numberOfRelatedEstateFormalitySuccess = (int) this.getEstateFormalityList().stream()
                    .filter(estate -> estate.getEstateFormalitySuccessList() != null)
                    .flatMap(e -> e.getEstateFormalitySuccessList().stream())
                    .filter(success -> NoteType.NOTE_TYPE_A.equals(success.getNoteType()))
                    .count();
            return this.getEstateFormalityList().size() + numberOfRelatedCommunications + numberOfRelatedEstateFormalitySuccess;
        } else {
            return 0;
        }
    }

    public Double getNumberActOrSumOfEstateFormalitiesAndOther() {
        if (!ValidationHelper.isNullOrEmpty(this.getNumberActUpdate())) {
            return this.getNumberActUpdate();
        } else {
            return Double.valueOf(getSumOfEstateFormalitiesAndCommunicationsAndSuccess());
        }
    }

    public Integer getNumberOfGroupsByDocumentOfEstateFormality() throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.getCountNotAnEntity(RequestFormality.class, "documentId",
                new Criterion[]{Restrictions.isNotNull("documentId"),
                        Restrictions.eq("request.id", this.getId())}).intValue();
    }

    public String getMailViewSubject() {
        if (getSubject() != null) {
            if (getSubject().getTypeIsPhysicalPerson()) {
                return getSubject().getSurname().toUpperCase() + "_" + getSubject().getName().toUpperCase() + "_"
                        + getSubject().getFiscalCode().toUpperCase();
            } else {
                return getSubject().getBusinessName().toUpperCase() + "_" + getSubject().getNumberVAT().toUpperCase();
            }
        }
        return "";
    }

    public List<Subject> getSubjectsRelatedToFormalityThroughSituation() throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getSituationEstateLocations())
                || ValidationHelper.isNullOrEmpty(this.getSituationEstateLocations().get(0).getFormalityList())) {
            return Collections.emptyList();
        }
        Formality tempFormality = this.getSituationEstateLocations().get(0).getFormalityList().get(0);

        List<Long> listIds = EstateSituationHelper.getIdSubjects(this);
        List<Subject> presumableSubjects = EstateSituationHelper.getListSubjects(listIds, this.getSubject());
        List<Subject> unsuitableSubjects = SubjectHelper.deleteUnsuitable(presumableSubjects,
                this.getSituationEstateLocations().get(0).getFormalityList());
        presumableSubjects.removeAll(unsuitableSubjects);
        presumableSubjects.removeIf(x -> x.getFullName().replaceAll("\\s+", " ")
                .equals(this.getSubject().getFullName().replaceAll("\\s+", " ")));
        presumableSubjects.add(this.getSubject());

        List<Subject> subContro = tempFormality.getSectionC().stream().filter(c -> "Contro".equals(c.getSectionCType())).map(SectionC::getSubject)
                .flatMap(List::stream).filter(s -> s.getRequestList().stream().anyMatch(r -> r.getId().equals(this.getId())))
                .collect(Collectors.toList());

        List<Subject> result = Stream.of(subContro, presumableSubjects).flatMap(Collection::stream).distinct().collect(Collectors.toList());

        return result;
    }

    public String getNameStr() throws PersistenceBeanException, IllegalAccessException {

        if (getSubject() != null && getSubject().getFullName() != null) {
            return getSubject().getFullName();
        } else {
            String result = "";
            List<Subject> subjects = DaoManager.load(Subject.class, new CriteriaAlias[]{
                            new CriteriaAlias("sectionC", "sc", JoinType.INNER_JOIN),
                            new CriteriaAlias("sc.formality", "f", JoinType.INNER_JOIN),
                            new CriteriaAlias("f.distraintRequests", "r", JoinType.INNER_JOIN)
                    },
                    new Criterion[]{
                            Restrictions.eq("sc.sectionCType", SectionCType.CONTRO.getName()),
                            Restrictions.eq("r.id", getId())
                    });
            if (!ValidationHelper.isNullOrEmpty(subjects)) {
                result = subjects.stream().map(Subject::getFullName).collect(Collectors.joining(" / "));
            }
            return result;
        }
    }

    public boolean isPhysicalPerson() {
        return getSubject() != null && getSubject().getTypeIsPhysicalPerson();
    }

    public String getUrgentStr() {
        return (getUrgent() != null && getUrgent()) ? "SI" : "NO";
    }

    public String getFormalityAuthorizedStr() {
        return getFormalityAuthorized() == null ? ""
                : getFormalityAuthorized().toString();
    }

    public String getCreateDateStr() {
        return DateTimeHelper.toString(getCreateDate());
    }

    public String getStateDescription() {
        RequestState state = RequestState.getById(getStateId());

        return state == null ? "" : state.toString();
    }

    public String getClientName() {
        return getClient() == null ? "" : getClient().toString();
    }

    public String getRequestTypeName() {
        return getRequestType() == null ? "" : getRequestType().toString();
    }

    public String getServiceName() {
        return getService() == null ? "" : getService().toString();
    }

    public String getMultipleServiceNames() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        Request request = DaoManager.get(Request.class,  new CriteriaAlias[]{
                new CriteriaAlias("multipleServices", "m", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("id", this.getId())});

        return ValidationHelper.isNullOrEmpty(request.getMultipleServices()) ? "" : request.getMultipleServices()
                .stream()
                .filter(s -> !ValidationHelper.isNullOrEmpty(s.getName()))
                .map(s -> s.toString())
                .collect(Collectors.joining(","));
    }

    public String getAggregationLandChargesRegistryName() {
        return getAggregationLandChargesRegistry() == null ? ""
                : getAggregationLandChargesRegistry().toString();
    }

    public String getAggregationLandCharRegNameOrCity() throws IllegalAccessException, PersistenceBeanException, InstantiationException {

        if(ValidationHelper.isNullOrEmpty(getAggregationLandChargesRegistryName())) {
            if(ValidationHelper.isNullOrEmpty(getCity())) {
                if(!ValidationHelper.isNullOrEmpty(getProvince())) {
                    return getProvince().getDescription();
                }
            }else {
                return city.getDescription();
            }
        } else {
            return getAggregationLandChargesRegistryName();
        }

        return "";
    }

    public List<Long> getAggregationLandChargesRegistersIds() {
        List<Long> chargesRegistryIds;
        if (!ValidationHelper.isNullOrEmpty(getAggregationLandChargesRegistry())) {
            chargesRegistryIds = getAggregationLandChargesRegistry().getAggregationLandChargesRegistersIds();
        } else {
            chargesRegistryIds = Collections.singletonList(0L);
        }
        return chargesRegistryIds;
    }

    public String getProvinceDescription() {
        if (getForeignCountry()) {
            return Province.FOREIGN_COUNTRY;
        }

        return getProvince() == null ? "" : getProvince().getDescription();
    }

    public String getCityDescription() {
        return getCity() == null ? "" : getCity().getDescription();
    }

    public String getCountryDescription() {
        return getCountry() == null ? "" : getCountry().getDescription();
    }

    public String getCreateUserName() {
        try {
            User user = DaoManager.get(User.class, getCreateUserId());

            return user == null ? "" : user.getFullname();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return "";
    }

    public String getRequestExcelUserName() {

        try {
            User user = DaoManager.get(User.class, getCreateUserId());
            if(!ValidationHelper.isNullOrEmpty(user)) {
                if(!ValidationHelper.isNullOrEmpty(user.getCategory()) && UserCategories.ESTERNO.name()
                        .equals(user.getCategory().name())) {
                    if(!ValidationHelper.isNullOrEmpty(user.getFullname()))
                        return user.getFullname();
                    else
                        return user.getBusinessName() != null ? user.getBusinessName() : "";
                }
            }

            if(!ValidationHelper.isNullOrEmpty(getRequestMangerList())) {
                return getRequestMangerList().stream().map(m -> m.toString())
                        .collect(Collectors.joining("\n"));
            }

            if(!ValidationHelper.isNullOrEmpty(getMail())) {
                WLGInbox mail = DaoManager.get(WLGInbox.class,new CriteriaAlias[]{
                        new CriteriaAlias("managers", "m", JoinType.INNER_JOIN)
                }, new Criterion[]{
                        Restrictions.eq("id", this.getMail().getId())
                });
                if(!ValidationHelper.isNullOrEmpty(mail) && !ValidationHelper.isNullOrEmpty(mail.getManagers())) {
                    return mail.getManagers().stream().map(m -> m.toString())
                            .collect(Collectors.joining("\n"));
                }
            }

        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return "";
    }
    public String getUserName() {
        try {
            return getUser() == null ? "" : getUser().getFullname();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return "";
    }

    public String getNotaryNameIfExists() {
        if(getNotary() != null && !ValidationHelper.isNullOrEmpty(getNotary().getName())) {
            return getNotary().getName();
        }
        return "";
    }

    public void reloadDocumentRequests(Session session) {
        this.setDocumentsRequest(ConnectionManager.load(Document.class, new CriteriaAlias[]{
                new CriteriaAlias("request", "r", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("r.id", this.getId())
        }, session));
    }

    public String getNotaryCityIfExists() {
        if(getNotary() != null && !ValidationHelper.isNullOrEmpty(getNotary().getCity())) {
            return getNotary().getCity();
        }
        return "";
    }

    public boolean isDeletedRequest() {
        return (this.getIsDeleted() == null || !this.getIsDeleted());
    }

    public boolean showAuthorizedQuote() throws IllegalAccessException, PersistenceBeanException, InstantiationException {

        if (!ValidationHelper.isNullOrEmpty(getClient())
                && !ValidationHelper.isNull(getClient().getMaxNumberAct()))
        {

            if (!Hibernate.isInitialized(getRequestFormalities())) {
                try {
                    this.reloadRequestFormalities();
                } catch (PersistenceBeanException | IllegalAccessException e) {
                    LogHelper.log(log, e);
                }
            }
            Request req = DaoManager.get(Request.class,
                    new Criterion[]{Restrictions.eq("id", getId())});
            Integer total = 0;;
            if(req.getSumOfEstateFormalities() != null)
                total += req.getSumOfEstateFormalities();

            if(req.getSumOfEstateFormalitiesAndCommunicationsAndSuccess() != null)
                total += req.getSumOfEstateFormalitiesAndCommunicationsAndSuccess();
            if(total > getClient().getMaxNumberAct())
                return true;
            else
                return false;
        }else {
            return false;
        }
    }

    public void reloadRequestSubjects()
            throws PersistenceBeanException, IllegalAccessException {
        setRequestSubjects(DaoManager.load(RequestSubject.class, new Criterion[]{
                Restrictions.eq("request.id", this.getId())
        }, new CriteriaAlias[]{
                new CriteriaAlias("subject", "s", JoinType.INNER_JOIN)}));
    }

    public List<EstateSituation> getSituationEstateLocations() {
        return situationEstateLocations;
    }

    public void setSituationEstateLocations(List<EstateSituation> situationEstateLocations) {
        this.situationEstateLocations = situationEstateLocations;
    }

    @Override
    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public RequestEnumTypes getType() {
        return type;
    }

    public void setType(RequestEnumTypes type) {
        this.type = type;
    }

    public Long getSubjectTypeId() {
        return subjectTypeId;
    }

    public void setSubjectTypeId(Long subjectTypeId) {
        this.subjectTypeId = subjectTypeId;
    }

    public Long getPropertyTypeId() {
        return propertyTypeId;
    }

    public void setPropertyTypeId(Long propertyTypeId) {
        this.propertyTypeId = propertyTypeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Long getSexId() {
        return sexId;
    }

    public void setSexId(Long sexId) {
        this.sexId = sexId;
    }

    public AggregationLandChargesRegistry getAggregationLandChargesRegistry() {
        return aggregationLandChargesRegistry;
    }

    public void setAggregationLandChargesRegistry(
            AggregationLandChargesRegistry aggregationLandChargesRegistry) {
        this.aggregationLandChargesRegistry = aggregationLandChargesRegistry;
    }

    public Boolean getLegal() {
        return legal;
    }

    public void setLegal(Boolean legal) {
        this.legal = legal;
    }

    public Boolean getEstimate() {
        return estimate;
    }

    public void setEstimate(Boolean estimate) {
        this.estimate = estimate;
    }

    public Boolean getUrgent() {
        return urgent;
    }

    public void setUrgent(Boolean urgent) {
        this.urgent = urgent;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public String getParticle() {
        return particle;
    }

    public void setParticle(String particle) {
        this.particle = particle;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getAddressProperty() {
        return addressProperty;
    }

    public void setAddressProperty(String addressProperty) {
        this.addressProperty = addressProperty;
    }

    public Long getFormalityAuthorized() {
        return formalityAuthorized;
    }

    public void setFormalityAuthorized(Long formalityAuthorized) {
        this.formalityAuthorized = formalityAuthorized;
    }

    public String getCdr() {
        return cdr;
    }

    public void setCdr(String cdr) {
        this.cdr = cdr;
    }

    public String getNdg() {
        return ndg;
    }

    public void setNdg(String ndg) {
        this.ndg = ndg;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public RequestPrint getRequestPrint() {
        return requestPrint;
    }

    public void setRequestPrint(RequestPrint requestPrint) {
        this.requestPrint = requestPrint;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Boolean getForeignCountry() {
        return foreignCountry == null ? Boolean.FALSE : foreignCountry;
    }

    public void setForeignCountry(Boolean foreignCountry) {
        this.foreignCountry = foreignCountry;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public String getTotalCostDouble() {
        if (!ValidationHelper.isNullOrEmpty(getTotalCost())) {
            return totalCost.replaceAll(",", ".");
        }
        return "";
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    public Boolean getSaveCostInDraft() {
        return saveCostInDraft == null ? Boolean.TRUE : saveCostInDraft;
    }

    public void setSaveCostInDraft(Boolean saveCostInDraft) {
        this.saveCostInDraft = saveCostInDraft;
    }

    public Request getUpdateRequest() {
        return updateRequest;
    }

    public void setUpdateRequest(Request updateRequest) {
        this.updateRequest = updateRequest;
    }

    public WLGInbox getMail() {
        return mail;
    }

    public void setMail(WLGInbox mail) {
        this.mail = mail;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getActType() {
        return actType;
    }

    public void setActType(String actType) {
        this.actType = actType;
    }

    public String getActNumber() {
        return actNumber;
    }

    public void setActNumber(String actNumber) {
        this.actNumber = actNumber;
    }

    public Date getActDate() {
        return actDate;
    }

    public void setActDate(Date actDate) {
        this.actDate = actDate;
    }

    public Date getTermDate() {
        return termDate;
    }

    public void setTermDate(Date termDate) {
        this.termDate = termDate;
    }

    public String getReaNumber() {
        return reaNumber;
    }

    public void setReaNumber(String reaNumber) {
        this.reaNumber = reaNumber;
    }

    public String getNatureLegal() {
        return natureLegal;
    }

    public void setNatureLegal(String natureLegal) {
        this.natureLegal = natureLegal;
    }

    public String getIstat() {
        return istat;
    }

    public void setIstat(String istat) {
        this.istat = istat;
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

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public Agency getOffice() {
        return office;
    }

    public void setOffice(Agency office) {
        this.office = office;
    }

    public List<EstateFormality> getEstateFormalityList() {

        if (this.estateFormalityList == null) {
            if (!Hibernate.isInitialized(getRequestFormalities())) {
                try {
                    this.reloadRequestFormalities();
                } catch (PersistenceBeanException | IllegalAccessException e) {
                    LogHelper.log(log, e);
                }
            }
            this.estateFormalityList = emptyIfNull(getRequestFormalities()).stream().map(RequestFormality::getFormality).collect(Collectors.toList());
        }
        return this.estateFormalityList;
    }

    public void setEstateFormalityList(List<EstateFormality> estateFormalityList) {
        this.estateFormalityList = estateFormalityList;
    }

    public List<Property> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    public String getComment() {
        if (comment == null) {
            comment = ResourcesHelper.getString("requestCommentDefaultValue");
        }
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Service> getMultipleServices() {
        return multipleServices;
    }

    public void setMultipleServices(List<Service> multipleServices) {
        this.multipleServices = multipleServices;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Client getBillingClient() {
        return billingClient;
    }

    public void setBillingClient(Client billingClient) {
        this.billingClient = billingClient;
    }

    public List<Formality> getFormalityPdfList() {
        return formalityPdfList;
    }

    public void setFormalityPdfList(List<Formality> formalityPdfList) {
        this.formalityPdfList = formalityPdfList;
    }

    public String getUltimaResidenza() {
        return ultimaResidenza;
    }

    public void setUltimaResidenza(String ultimaResidenza) {
        this.ultimaResidenza = ultimaResidenza;
    }

    public Date getEvasionDate() {
        return evasionDate;
    }

    public void setEvasionDate(Date evasionDate) {
        this.evasionDate = evasionDate;
    }

    public Long getUserAreaId() {
        return userAreaId;
    }

    public void setUserAreaId(Long userAreaId) {
        this.userAreaId = userAreaId;
    }

    public Long getUserOfficeId() {
        return userOfficeId;
    }

    public void setUserOfficeId(Long userOfficeId) {
        this.userOfficeId = userOfficeId;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<Comment> getCommentsWithInitialize() {
        if (!Hibernate.isInitialized(comments)) {
            log.info("Comments are not initialized");
            try {
                this.comments = DaoManager.load(Comment.class, new CriteriaAlias[]{
                        new CriteriaAlias("request", "r", JoinType.INNER_JOIN)
                }, new Criterion[]{
                        Restrictions.eq("r.id", getId())
                });
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }
        }
        return getComments();
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getInitCost() {
        return initCost;
    }

    public void setInitCost(String initCost) {
        this.initCost = initCost;
    }

    public Double getNumberActUpdate() {
        return numberActUpdate;
    }

    public void setNumberActUpdate(Double numberActUpdate) {
        this.numberActUpdate = numberActUpdate;
    }

    public Double getCostForced() {
        return costForced;
    }

    public void setCostForced(Double costForced) {
        this.costForced = costForced;
    }

    public Boolean getCostButtonConfirmClicked() {
        return costButtonConfirmClicked;
    }

    public void setCostButtonConfirmClicked(Boolean costButtonConfirmClicked) {
        this.costButtonConfirmClicked = costButtonConfirmClicked;
    }

    public List<Formality> getFormalityForcedList() {
        return formalityForcedList;
    }

    public void setFormalityForcedList(List<Formality> formalityForcedList) {
        this.formalityForcedList = formalityForcedList;
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    public Double getCostCadastral() {
        return costCadastral;
    }

    public void setCostCadastral(Double costCadastral) {
        this.costCadastral = costCadastral;
    }

    public Double getCostExtra() {
        return costExtra;
    }

    public void setCostExtra(Double costExtra) {
        this.costExtra = costExtra;
    }

    public Double getCostEstateFormality() {
        return costEstateFormality;
    }

    public void setCostEstateFormality(Double costEstateFormality) {
        this.costEstateFormality = costEstateFormality;
    }

    public Double getCostPay() {
        return costPay;
    }

    public void setCostPay(Double costPay) {
        this.costPay = costPay;
    }

    public List<Document> getDocumentsRequest() {
        return documentsRequest;
    }

    public void setDocumentsRequest(List<Document> documentsRequest) {
        this.documentsRequest = documentsRequest;
    }

    public Boolean getConfirmExtraCostsPressed() {
        return confirmExtraCostsPressed;
    }

    public void setConfirmExtraCostsPressed(Boolean confirmExtraCostsPressed) {
        this.confirmExtraCostsPressed = confirmExtraCostsPressed;
    }

    public List<EstateFormality> getEstateFormalityListUpdate() {
        return estateFormalityListUpdate;
    }

    public void setEstateFormalityListUpdate(List<EstateFormality> estateFormalityListUpdate) {
        this.estateFormalityListUpdate = estateFormalityListUpdate;
    }

    public List<Client> getRequestMangerList() {
        return requestMangerList;
    }

    public void setRequestMangerList(List<Client> requestMangerList) {
        this.requestMangerList = requestMangerList;
    }

    public List<RequestFormality> getRequestFormalities() {
        return requestFormalities;
    }

    public void setRequestFormalities(List<RequestFormality> requestFormalities) {
        this.requestFormalities = requestFormalities;
    }

    public Notary getNotary() {
        return notary;
    }

    public void setNotary(Notary notary) {
        this.notary = notary;
    }

    public Formality getDistraintFormality() {
        return distraintFormality;
    }

    public void setDistraintFormality(Formality distraintFormality) {
        this.distraintFormality = distraintFormality;
    }

    public Set<Document> getInvoiceDocuments() {
        return invoiceDocuments;
    }

    public void setInvoiceDocuments(Set<Document> invoiceDocuments) {
        this.invoiceDocuments = invoiceDocuments;
    }

    public void setLastCostChanging(boolean lastCostChanging) {
        this.lastCostChanging = lastCostChanging;
    }

    public boolean getLastCostChanging() {
        return lastCostChanging;
    }

    public boolean isCalledFromReportList() {
        return isCalledFromReportList;
    }

    public void setCalledFromReportList(boolean calledFromReportList) {
        isCalledFromReportList = calledFromReportList;
    }

    public Subject getSubjectToGenerateAlienatedTemplate() {
        return subjectToGenerateAlienatedTemplate;
    }

    public void setSubjectToGenerateAlienatedTemplate(Subject subjectToGenerateAlienatedTemplate) {
        this.subjectToGenerateAlienatedTemplate = subjectToGenerateAlienatedTemplate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Double getTaxable() {
        return taxable;
    }

    public void setTaxable(Double taxable) {
        this.taxable = taxable;
    }

    public Date getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(Date certificationDate) {
        this.certificationDate = certificationDate;
    }

    public String getCommentCertification() {
        return commentCertification;
    }

    public void setCommentCertification(String commentCertification) {
        this.commentCertification = commentCertification;
    }


    public Formality getTranscriptionActId() {
        return transcriptionActId;
    }

    public void setTranscriptionActId(Formality transcriptionActId) {
        this.transcriptionActId = transcriptionActId;
    }

    public Boolean getAuthorizedQuote() {
        return authorizedQuote;
    }

    public void setAuthorizedQuote(Boolean authorizedQuote) {
        this.authorizedQuote = authorizedQuote;
    }

    public Map<Long, String> getSubjectTypeMapping() {
        if(subjectTypeMapping == null)
            subjectTypeMapping = new HashMap<Long, String>();
        return subjectTypeMapping;
    }

    public void setSubjectTypeMapping(Map<Long, String> subjectTypeMapping) {
        this.subjectTypeMapping = subjectTypeMapping;
    }

    public List<Subject> getSubjectList() {
        if (this.subjectList == null) {
            if (!Hibernate.isInitialized(getRequestSubjects())) {
                try {
                    this.reloadRequestSubjects();
                } catch (PersistenceBeanException | IllegalAccessException e) {
                    LogHelper.log(log, e);
                }
            }
            this.subjectList = emptyIfNull(getRequestSubjects()).stream().map(RequestSubject::getSubject).collect(Collectors.toList());
        }
        return this.subjectList;
    }

    public void setSubjectList(List<Subject> subjectList) {
        this.subjectList = subjectList;
    }

    public String getCostNote() {
        return costNote;
    }

    public void setCostNote(String costNote) {
        this.costNote = costNote;
    }

    public List<RequestSubject> getRequestSubjects() {
        return requestSubjects;
    }

    public void setRequestSubjects(List<RequestSubject> requestSubjects) {
        this.requestSubjects = requestSubjects;
    }

    public boolean addRequestSubject(Subject subject, Session session) throws PersistenceBeanException, IllegalAccessException {
        RequestSubject requestSubject = new RequestSubject(this, subject);
        Entry<Long, String> result = getSubjectTypeMapping().entrySet().stream()
                .filter(x -> x.getKey() == subject.getId()).findFirst().orElse(null);
        if(!ValidationHelper.isNullOrEmpty(result)) {
            requestSubject.setType(result.getValue());
        }

        if(ValidationHelper.isNullOrEmpty(getRequestSubjects())) {
            setRequestSubjects(new ArrayList<>());
        }
        RequestSubject alreadyExistsRequestSubject = this.getRequestSubjects().stream()
                .filter(f -> f.getRequest().getId().equals(requestSubject.getRequest().getId()))
                .filter(f -> f.getSubject().getId().equals(requestSubject.getSubject().getId())).findFirst().orElse(null);

        if (!ValidationHelper.isNullOrEmpty(alreadyExistsRequestSubject)) {
            if(!ValidationHelper.isNullOrEmpty(result)) {
                requestSubject.setType(result.getValue());
            }

            ConnectionManager.saveObject(alreadyExistsRequestSubject, false, session);
            return false;
        } else {
            ConnectionManager.saveObject(requestSubject, false, session);
            this.getRequestSubjects().add(requestSubject);
            return true;
        }
    }

    public String getTempId() {
        return tempId;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }

    public boolean isSelectedForMail() {
        return selectedForMail;
    }

    public void setSelectedForMail(boolean selectedForMail) {
        this.selectedForMail = selectedForMail;
    }

    public Boolean getIncludeNationalCost() {
        return includeNationalCost;
    }

    public void setIncludeNationalCost(Boolean includeNationalCost) {
        this.includeNationalCost = includeNationalCost;
    }

    public String getExcelUserName() {
        return excelUserName;
    }

    public void setExcelUserName(String excelUserName) {
        this.excelUserName = excelUserName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Boolean getSalesDevelopment() {
        if(!ValidationHelper.isNullOrEmpty(getClient()) &&
                !ValidationHelper.isNullOrEmpty(getClient().getSalesDevelopment()) && getClient().getSalesDevelopment())
            return true;

        if(!ValidationHelper.isNullOrEmpty(getService()) &&
                !ValidationHelper.isNullOrEmpty(getService().getSalesDevelopment()) && getService().getSalesDevelopment())
            return true;

        return false;
    }

    public Boolean getRegime() {
        return regime;
    }

    public void setRegime(Boolean regime) {
        this.regime = regime;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public String getEvasionDateStr() {
        return DateTimeHelper.toString(getEvasionDate());
    }
}