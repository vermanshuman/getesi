package it.nexera.ris.persistence.view;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;

import it.nexera.ris.persistence.beans.entities.domain.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.ScrollMode;
import org.hibernate.annotations.Immutable;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import it.nexera.ris.common.annotations.View;
import it.nexera.ris.common.enums.ClientType;
import it.nexera.ris.common.enums.RequestState;
import it.nexera.ris.common.enums.RoleTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedView;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.persistence.beans.entities.domain.readonly.WLGInboxShort;

@javax.persistence.Entity
@Immutable
@Table(name = "request_subject_view")
@View(sql = RequestView.QUERY)
public class RequestView extends IndexedView {

    private static final long serialVersionUID = -4579866925819575398L;

    protected static transient final Log log = LogFactory.getLog(BaseHelper.class);

    public static final String CREATE_PART = "CREATE OR REPLACE VIEW request_subject_view ";

    public static final String FROM_PART = "FROM request request " +
           // "left join dic_service on request.service_id = dic_service.id " +
            //"inner join dic_request_type on request.request_type_id = dic_request_type.id " +
           // "left join dic_aggregation_land_char_reg on request.aggregation_land_char_reg_id = dic_aggregation_land_char_reg.id " +
            "left join formality on formality.id = request.distraint_act_id " +
            //"left join wlg_inbox on wlg_inbox.id = request.mail_id " +
            //"left join inbox_manager on inbox_manager.inbox_id = wlg_inbox.id " +
            "left join section_c on section_c.formality_id = formality.id " +
            "left join subject_section_c on subject_section_c.section_c_id = section_c.id " +
            "left join subject on ifnull(request.subject_id,subject_section_c.subject_id) = subject.id ";

    public static final String FIELDS = "request.ID ID,  "
            + "request.is_deleted is_deleted, "
            + "request.CREATE_DATE CREATE_DATE, "
            + "request.CREATE_USER_ID CREATE_USER_ID, "
            + "request.UPDATE_DATE UPDATE_DATE, "
            + "request.UPDATE_USER_ID UPDATE_USER_ID, "
            + "request.CLIENT_ID CLIENT_ID, "
            + "request.billing_client_id billing_client_id, "
            + "request.expiration_date expiration_date, "
            + "request.user_area_id user_area_id, "
            + "request.user_office_id user_office_id, "
            + "request.request_type_id request_type_id, "
            //+ "dic_request_type.name request_type_name,"
            //+ "dic_request_type.icon request_type_icon,"
            + "request.service_id service_id, "
           // + "dic_service.name service_name, "
           // + "dic_service.icon service_icon, "
           // + "dic_service.is_Update service_is_Update, "
            //+ "dic_aggregation_land_char_reg.name aggregation_land_char_reg_name, "
            + "request.subject_id subject_id, "
            + "request.state_id state_id, "
            + "request.user_id user_id, "
            + "request.city_id city_id, "
            + "request.mail_id mail_id, "
            + "request.number_act_update number_act_update, "
            + "request.cost_estate_formality cost_estate_formality, "
            + "request.cost_cadastral cost_cadastral, "
            + "request.cost_pay cost_pay, "
            + "request.total_cost total_cost, "
            + "request.evasion_date evasion_date, "
            + "request.aggregation_land_char_reg_id aggregation_land_char_reg_id, "
            + "request.urgent urgent,"
            + "subject.birth_date birth_date, "
            + "subject.type_id type_id, "
            + "request.province_id province_id, "
            + "request.invoice_id invoice_id, "
            //+ "wlg_inbox.client_fiduciary_id fiduciary_id, "
           // + "inbox_manager.client_id manager_id, "
            + "section_c.section_c_type section_c_type, "
            + "request.distraint_act_id distraint_act_id ";

    private static final String GROUP_BY_PART = " group by ID ";

    public static final String SELECT_PART = "AS SELECT GROUP_CONCAT(rqsts.nameR SEPARATOR ' / ') as name, "
            + " GROUP_CONCAT(rqsts.nameL SEPARATOR ' / ') as reverse_name, "
            + "rqsts.* FROM ("
            + "SELECT GROUP_CONCAT(DISTINCT CONCAT(subject.first_name,' ',subject.last_name) SEPARATOR' / ') as nameR,"
            + "GROUP_CONCAT(DISTINCT CONCAT(subject.last_name,' ',subject.first_name) SEPARATOR' / ') as nameL, "
            + "case when subject.fiscal_code IS NULL OR subject.fiscal_code = '' then subject.number_vat else subject.fiscal_code end code, "
            + FIELDS
            + FROM_PART
            + "where ((subject.last_name is not null and not subject.last_name='') or (subject.first_name is not null and not subject.first_name='') or (subject.type_id is null)) "
            + "and (section_c_type = 'Contro' or section_c_type is null)"
            + GROUP_BY_PART

            + " UNION ALL "

            + "SELECT "
            + "GROUP_CONCAT(DISTINCT subject.business_name SEPARATOR' / ') as nameR, "
            + "GROUP_CONCAT(DISTINCT subject.business_name SEPARATOR' / ') as nameL, "
            + "subject.number_vat code, "
            + FIELDS
            + FROM_PART
            + "where ((subject.business_name is not null and not subject.business_name='') or (subject.business_name is not null and not subject.business_name='')) "
            + "and (section_c_type = 'Contro' or section_c_type is null)"
            + GROUP_BY_PART
            + ") as rqsts group by rqsts.ID";

    protected static final String QUERY = CREATE_PART + SELECT_PART;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "billing_client_id")
    private Long billingClientId;

    //enum SubjectType
    @Column(name = "type_id")
    private Long typeId;

    @Transient
    private String clientName;

    @Column(name = "request_type_id")
    private Long requestTypeId;

//    @Column(name = "request_type_name")
//    private String requestTypeName;
//
//    @Column(name = "request_type_icon")
//    private String requestTypeIcon;

    @Column(name = "service_id")
    private Long serviceId;

//    @Column(name = "service_name")
//    private String serviceName;
//
//    @Column(name = "service_icon")
//    private String serviceIcon;
//
//    @Column(name = "service_is_Update")
    //private Boolean serviceIsUpdate;

    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "name")
    private String name;
    
    @Column(name = "reverse_name")
    private String reverseName;

    @Column(name = "code")
    private String code;

    @Column(name = "state_id")
    private Long stateId;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "aggregation_land_char_reg_id")
    private Long aggregationLandChargesRegistryId;

    @Transient
    private User user;

    @Column(name = "mail_id")
    private Long mailId;

    @Column(name = "user_area_id")
    private Long userAreaId;

    @Column(name = "user_office_id")
    private Long userOfficeId;

//    @Column(name = "aggregation_land_char_reg_name")
//    private String aggregationLandCharRegName;

    @Column(name = "city_id")
    private Long cityId;
    
    @Column(name = "province_id")
    private Long provinceId;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "urgent")
    private Boolean urgent;

    @Column(name = "evasion_date")
    private Date evasionDate;

    @Column(name = "number_act_update")
    private String numberActUpdate;

    @Column(name = "cost_estate_formality")
    private Double costEstateFormality;

    @Column(name = "cost_cadastral")
    private Double costCadastral;

    @Column(name = "cost_pay")
    private Double costPay;

    @Column(name = "total_cost")
    private String totalCost;

    @Column(name = "distraint_act_id")
    private Long distraintFormalityId;

    @Column(name = "invoice_id")
    private Long invoiceId;

    //    @Column(name = "fiduciary_id")
//    private Long fiduciaryId;
//    
//    @Column(name = "manager_id")
//    private Long managerId;

    @Transient
    private Boolean haveDocuments;

    @Transient
    private Boolean haveAllegatiDocuments;

    @Transient
    private Boolean isExternal;

    @Transient
    private String createUserFullName;

    @Transient
    private User createUser;

    @Transient
    private Office office;
    
    @Transient
    private String clientNameProfessional;
    
    @Transient
    private int documentsCount;
    
    @Transient
    private String serviceName;

    @Transient
    private String serviceIcon;

    @Transient
    private Boolean serviceIsUpdate;
    
    @Transient
    private String requestTypeName;

    @Transient
    private String requestTypeIcon;
    
    @Transient
    private String aggregationLandCharRegName;
    
    @Transient
    private Long fiduciaryId;
    
    @Transient
    private Long managerId;

    @Transient
    private Boolean manageTranscription;

    public Boolean getHaveDocuments() {
        if (haveDocuments == null) {
            try {
                haveDocuments = DaoManager.getSession()
                        .createQuery("select 1 from Document where request= :request_id and selectedForEmail = true")
                        .setLong("request_id", getId())
                        .setFetchSize(1).scroll(ScrollMode.FORWARD_ONLY).next();
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }
        }
        return haveDocuments;
    }

    public Boolean getHaveAllegatiDocuments() {
        if (haveAllegatiDocuments == null) {
            try {
                haveAllegatiDocuments = DaoManager.getSession()
                        .createQuery("select 1 from Document where request= :request_id and typeId = 8")
                        .setLong("request_id", getId())
                        .setFetchSize(1).scroll(ScrollMode.FORWARD_ONLY).next();
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }
        }
        return haveAllegatiDocuments;
    }

    public void setHaveDocuments(Boolean haveDocuments) {
        this.haveDocuments = haveDocuments;
    }

    public String getServiceIconStr() {
        return serviceIcon == null ? "fa-square-o" : serviceIcon;
    }

    public String getRequestTypeIconStr() {
        return requestTypeIcon == null ? "fa-square-o" : requestTypeIcon;
    }

    public String getClientName() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (clientName == null && clientId != null) {
            Client client = DaoManager.get(Client.class, clientId);
            Client billingClient = null;
            if (billingClientId != null) {
                billingClient = DaoManager.get(Client.class, billingClientId);
            }
            if (client == null) {
                clientName = "";
            } else {
                if (billingClient == null) {
                    clientName = client.toString();
                } else {
                    clientName = String.format("%s (%s)", client.toString(), billingClient.toString());
                }
            }
        }
        return clientName;
    }

    public String getStateDescription() {
        RequestState state = RequestState.getById(getStateId());
        return state == null ? "" : state.toString();
    }

    public String getUserName() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (user == null && userId != null) {
            user = DaoManager.get(User.class, userId);
        }
        return user == null ? "" : user.getFullname();
    }

    public String getServicesNames() {
        try {
            return DaoManager.get(Request.class, getId())
                    .getMultipleServices().stream()
                    .map(Service::getName).collect(Collectors.joining("</br>"));
        } catch (Exception e) {
            LogHelper.log(log, e);
            return "";
        }
    }
    
    public List<Service> getMultipleServices() {
        try {
            return DaoManager.get(Request.class, getId())
                    .getMultipleServices();
        } catch (Exception e) {
            LogHelper.log(log, e);
            return null;
        }
    }

    public Boolean getExternal() {
        if (isExternal == null || createUser == null) {
            User user = null;
            if (getCreateUserId() == null) {
                setExternal(false);
                return false;
            }
            try {
                user = DaoManager.get(User.class, getCreateUserId());
                if (user == null) {
                    setExternal(false);
                } else {
                    createUser = user;
                    setExternal(user.getUserRoles().stream().anyMatch(r -> r.getType() == RoleTypes.EXTERNAL));
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
                setExternal(false);
                return false;
            }
        }
        return isExternal;
    }

    public String getMailSubject() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getMailId())) {
            return DaoManager.get(WLGInboxShort.class, getMailId()).getEmailSubject();
        }
        return null;
    }

    public String getCreateUserFullName() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (getExternal()) {
            setCreateUserFullName(createUser.getFullname());
            if (getOffice() == null && !ValidationHelper.isNullOrEmpty(getUserOfficeId())) {
                Office office = DaoManager.get(Office.class, getUserOfficeId());
                setCreateUserFullName(String.format("%s - %s - %s", createUser.getFullname(), office.getCode(),
                        office.getDescription()));
            }
        }
        return createUserFullName;
    }

    public String getAggregationLandCharRegNameOrCity() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if(ValidationHelper.isNullOrEmpty(getAggregationLandCharRegName())) {
            City city = DaoManager.get(City.class,getCityId());
            if(ValidationHelper.isNullOrEmpty(city)) {
                Province province = DaoManager.get(Province.class,getProvinceId());
                if(!ValidationHelper.isNullOrEmpty(province))
                    return province.getDescription();
            }else
                return city.getDescription();
        } else {
            return getAggregationLandCharRegName();
        }
        
        return "";
    }

    public boolean getIsInserted() {
        if (!ValidationHelper.isNullOrEmpty(this.getStateId())
                && RequestState.INSERTED.getId().equals(this.getStateId())) {
            return true;
        }
        return false;
    }

    public String getNameStr() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (getSubjectId() != null && getDistraintFormalityId() == null) {
            return getReverseName();
        } else if (getSubjectId() == null && getDistraintFormalityId() == null) {
            Request request = DaoManager.get(Request.class, this.getId());
            List<Subject> subjects = DaoManager.load(Subject.class, new CriteriaAlias[]{
                    new CriteriaAlias("requestSubjects", "r", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("r.request", request)
            });
            if (!ValidationHelper.isNullOrEmpty(subjects)) {
                return subjects.stream().map(Subject::getFullName).collect(Collectors.joining(" / "));
            } else {
                return "";
            }
        } else {
            String result = getReverseName();
            if (!ValidationHelper.isNullOrEmpty(getSubjectId())) {
                Subject subject = DaoManager.get(Subject.class, getSubjectId());
                result = result.contains(subject.getFullName()) ? result : subject.getFullName() + " / " + result;
            }
            return result;
        }
    }

    public String getFormalityNumberStr() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getNumberActUpdate())) {
            return getNumberActUpdate();
        } else {
            Request request = DaoManager.get(Request.class, this.getId());
            if(!ValidationHelper.isNullOrEmpty(request)) {
                return request.getNumberActOrSumOfEstateFormalitiesAndOther().toString();
            }
        }
        return null;
    }

    public String getTotalCostDouble() {
        if (!ValidationHelper.isNullOrEmpty(getTotalCost())) {
            return totalCost.replaceAll(",", ".");
        }
        return "";
    }

    public String getClientNameProfessional(Long clientId) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (clientNameProfessional == null && clientId != null) {
            Client client = DaoManager.get(Client.class, clientId);
          
            if (client == null) {
                clientNameProfessional = "";
            } else {
                 if(client.getTypeId() == null || ClientType.PROFESSIONAL.getId().equals(client.getTypeId()) 
                         && !ValidationHelper.isNullOrEmpty(client.getNameProfessional())){
                     clientNameProfessional = client.getNameProfessional();
                 }else if( !( client.getTypeId() == null || ClientType.PROFESSIONAL.getId().equals(client.getTypeId()) ) 
                         && !ValidationHelper.isNullOrEmpty(client.getNameOfTheCompany())){
                     clientNameProfessional = client.getNameOfTheCompany();
                 }   
            }
        }
        return clientNameProfessional;
    }


    public String haveManagers() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (getMailId() != null) {
            WLGInbox wlgInbox =  DaoManager.get(WLGInbox.class, getMailId());
            if(!ValidationHelper.isNullOrEmpty(wlgInbox) && !ValidationHelper.isNullOrEmpty(wlgInbox.getManagers())) {
                return wlgInbox.getManagers()
                .stream()
                .distinct()
                .map(w -> w.toString())
                .collect(Collectors.joining(","));
            }
        }
        return null;
    }
    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getRequestTypeName() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if(!ValidationHelper.isNullOrEmpty(getRequestTypeId())) {
            RequestType requestType = DaoManager.get(RequestType.class, getRequestTypeId()); 
            if(requestType != null) {
               setRequestTypeName(requestType.getName()); 
            }
        }
        return requestTypeName;
    }

    public void setRequestTypeName(String requestTypeName) {
        this.requestTypeName = requestTypeName;
    }

    public String getRequestTypeIcon() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if(!ValidationHelper.isNullOrEmpty(getRequestTypeId())) {
            RequestType requestType = DaoManager.get(RequestType.class, getRequestTypeId()); 
            if(requestType != null) {
               setRequestTypeIcon(requestType.getIcon()); 
            }
        }
        return requestTypeIcon;
    }

    public void setRequestTypeIcon(String requestTypeIcon) {
        this.requestTypeIcon = requestTypeIcon;
    }

    public String getServiceName() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        
        if(!ValidationHelper.isNullOrEmpty(getServiceId())) {
            Service service = DaoManager.get(Service.class, getServiceId());
            if(service != null) {
               setServiceName(service.getName());
               setManageTranscription(service.getManageTranscription());
            }
        }
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAggregationLandCharRegName() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {

        if(!ValidationHelper.isNullOrEmpty(getAggregationLandChargesRegistryId())) {
            AggregationLandChargesRegistry aggregationLandChargesRegistry = DaoManager.get(
                    AggregationLandChargesRegistry.class, getAggregationLandChargesRegistryId()); 
            if(aggregationLandChargesRegistry != null) {
                setAggregationLandCharRegName(aggregationLandChargesRegistry.getName()); 
            }
        }
        return aggregationLandCharRegName;
    }

    public void setAggregationLandCharRegName(String aggregationLandCharRegName) {
        this.aggregationLandCharRegName = aggregationLandCharRegName;
    }

    public String getServiceIcon() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if(!ValidationHelper.isNullOrEmpty(getServiceId())) {
            Service service = DaoManager.get(Service.class, getServiceId()); 
            if(service != null) {
               setServiceIcon(service.getIcon()); 
            }
        }
        return serviceIcon;
    }

    public void setServiceIcon(String serviceIcon) {
        this.serviceIcon = serviceIcon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getMailId() {
        return mailId;
    }

    public void setMailId(Long mailId) {
        this.mailId = mailId;
    }

    public Long getRequestTypeId() {
        return requestTypeId;
    }

    public void setRequestTypeId(Long requestTypeId) {
        this.requestTypeId = requestTypeId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setExternal(Boolean external) {
        isExternal = external;
    }

    public void setCreateUserFullName(String createUserFullName) {
        this.createUserFullName = createUserFullName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Long getBillingClientId() {
        return billingClientId;
    }

    public void setBillingClientId(Long billingClientId) {
        this.billingClientId = billingClientId;
    }

    public void setHaveAllegatiDocuments(Boolean haveAllegatiDocuments) {
        this.haveAllegatiDocuments = haveAllegatiDocuments;
    }

    public Long getUserAreaId() { return userAreaId; }

    public void setUserAreaId(Long userAreaId) {
        this.userAreaId = userAreaId;
    }

    public Long getUserOfficeId() {
        return userOfficeId;
    }

    public void setUserOfficeId(Long userOfficeId) {
        this.userOfficeId = userOfficeId;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Boolean getUrgent() {
        return urgent;
    }

    public void setUrgent(Boolean urgent) {
        this.urgent = urgent;
    }

    public Date getEvasionDate() {
        return evasionDate;
    }

    public void setEvasionDate(Date evasionDate) {
        this.evasionDate = evasionDate;
    }

    public Long getAggregationLandChargesRegistryId() {
        return aggregationLandChargesRegistryId;
    }

    public void setAggregationLandChargesRegistryId(Long aggregationLandChargesRegistryId) {
        this.aggregationLandChargesRegistryId = aggregationLandChargesRegistryId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getNumberActUpdate() {
        return numberActUpdate;
    }

    public void setNumberActUpdate(String numberActUpdate) {
        this.numberActUpdate = numberActUpdate;
    }

    public Double getCostEstateFormality() {
        return costEstateFormality;
    }

    public void setCostEstateFormality(Double costEstateFormality) {
        this.costEstateFormality = costEstateFormality;
    }

    public Double getCostCadastral() {
        return costCadastral;
    }

    public void setCostCadastral(Double costCadastral) {
        this.costCadastral = costCadastral;
    }

    public Double getCostPay() {
        return costPay;
    }

    public void setCostPay(Double costPay) {
        this.costPay = costPay;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    public Boolean getServiceIsUpdate() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if(!ValidationHelper.isNullOrEmpty(getServiceId())) {
            Service service = DaoManager.get(Service.class, getServiceId()); 
            if(service != null) {
                setServiceIsUpdate(service.getIsUpdate()); 
            }
        }
        return serviceIsUpdate;
    }

    public void setServiceIsUpdate(Boolean serviceIsUpdate) {
        this.serviceIsUpdate = serviceIsUpdate;
    }

    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public Long getDistraintFormalityId() {
        return distraintFormalityId;
    }

    public void setDistraintFormalityId(Long distraintFormalityId) {
        this.distraintFormalityId = distraintFormalityId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getFiduciaryId() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if(!ValidationHelper.isNullOrEmpty(getMailId())) {
            WLGInbox wlgInbox = DaoManager.get(WLGInbox.class,  new CriteriaAlias[]{
                    new CriteriaAlias("clientFiduciary", "c", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("id", getMailId())
            });
            if(!ValidationHelper.isNullOrEmpty(wlgInbox) && !ValidationHelper.isNullOrEmpty(wlgInbox.getClientFiduciary())) {
                setFiduciaryId(wlgInbox.getClientFiduciary().getId());
            }
        }
        return fiduciaryId;
    }

    public void setFiduciaryId(Long fiduciaryId) {
        this.fiduciaryId = fiduciaryId;
    }

    public Long getManagerId() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if(!ValidationHelper.isNullOrEmpty(getMailId())) {
            WLGInbox wlgInbox = DaoManager.get(WLGInbox.class,  new CriteriaAlias[]{
                    new CriteriaAlias("client", "c", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("id", getMailId())
            });
            if(!ValidationHelper.isNullOrEmpty(wlgInbox) && !ValidationHelper.isNullOrEmpty(wlgInbox.getClient())) {
                setManagerId(wlgInbox.getClient().getId());
            }
        }
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public int getDocumentsCount() throws IllegalAccessException, PersistenceBeanException {
        List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request.id", getId()),
                Restrictions.eq("selectedForEmail", true)
//                ,
//                Restrictions.ne("typeId", DocumentType.FORMALITY.getId())
        });

        List<Document> formalities = DaoManager.load(Document.class, new CriteriaAlias[]{
                new CriteriaAlias("formality", "f", JoinType.INNER_JOIN),
                new CriteriaAlias("f.requestList", "r_f", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("r_f.id", getId()),
                Restrictions.eq("request.id", getId()),
                Restrictions.eq("selectedForEmail", true)
        });

        if (!ValidationHelper.isNullOrEmpty(formalities)) {
            for (Document temp : formalities) {
                if (!documents.contains(temp)) {
                    documents.add(temp);
                }
            }
        }
        documentsCount = documents != null ? documents.size() : 0;
        return documentsCount;
    }

    public String getInvoiceNumber() throws PersistenceBeanException, InstantiationException, IllegalAccessException {

        if(!ValidationHelper.isNullOrEmpty(getInvoiceId())){
            Invoice invoice = DaoManager.get(Invoice.class,getInvoiceId());
            if(!ValidationHelper.isNullOrEmpty(invoice))
                return invoice.getInvoiceNumber();
        }
       return "";
    }


    public void setDocumentsCount(int documentsCount) {
        this.documentsCount = documentsCount;
    }

    public String getReverseName() {
        return reverseName;
    }

    public void setReverseName(String reverseName) {
        this.reverseName = reverseName;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Boolean getManageTranscription() {
        return manageTranscription;
    }

    public void setManageTranscription(Boolean manageTranscription) {
        this.manageTranscription = manageTranscription;
    }
}
