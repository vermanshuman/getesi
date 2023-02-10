package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.ClientType;
import it.nexera.ris.common.enums.RequestState;
import it.nexera.ris.common.enums.RoleTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.beans.entities.domain.readonly.WLGInboxShort;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.ScrollMode;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ServiceRequestWrapper implements Serializable {

    private static final long serialVersionUID = -1409309825656980411L;
    protected static transient final Log log = LogFactory.getLog(ServiceRequestWrapper.class);

    private Date createDate;
    private Boolean isDeleted;
    private Long clientId;
    private Long billingClientId;
    private Long typeId;
    private String clientName;
    private Long requestTypeId;
    private Long serviceId;
    private Date birthDate;
    private String name;
    private String reverseName;
    private String code;
    private Long stateId;
    private Long subjectId;
    private Long userId;
    private Long aggregationLandChargesRegistryId;
    private User user;
    private Long mailId;
    private Long userAreaId;
    private Long userOfficeId;
    private Long cityId;
    private Long provinceId;
    private Date expirationDate;
    private Boolean urgent;
    private Date evasionDate;
    private String numberActUpdate;
    private Double costEstateFormality;
    private Double costCadastral;
    private Double costPay;
    private String totalCost;
    private Long distraintFormalityId;
    private Long invoiceId;
    private Boolean haveDocuments;
    private Boolean haveAllegatiDocuments;
    private Boolean isExternal;
    private String createUserFullName;
    private User createUser;
    private Office office;
    private String clientNameProfessional;
    private int documentsCount;
    private String serviceName;
    private String serviceIcon;
    private Boolean serviceIsUpdate;
    private String requestTypeName;
    private String requestTypeIcon;
    private String aggregationLandCharRegName;
    private Long fiduciaryId;
    private Long managerId;
    private Long id;
    private Long createUserId;
    private String tempId;
    private List<Service> multipleServices;
    private String textInVisura;
    private List<SubjectWrapper> subjectWrapperList;
    private TypeFormality specialFormality;

    public Boolean getHaveDocuments() {
        if (haveDocuments == null && !ValidationHelper.isNullOrEmpty(getId())) {
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
        if (haveAllegatiDocuments == null && !ValidationHelper.isNullOrEmpty(getId())) {
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
                    clientName = String.format("%s (%s)", client, billingClient);
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
//
//    public List<Service> getMultipleServices() {
//        try {
//            return DaoManager.get(Request.class, getId())
//                    .getMultipleServices();
//        } catch (Exception e) {
//            LogHelper.log(log, e);
//            return null;
//        }
//    }

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

    public void setExternal(Boolean external) {
        isExternal = external;
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
                setCreateUserFullName(String.format("%s <br/> %s - %s", createUser.getFullname(), office.getCode(),
                        office.getDescription()));
            }
        }
        return createUserFullName;
    }

    public String getAggregationLandCharRegNameOrCity() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (ValidationHelper.isNullOrEmpty(getAggregationLandCharRegName())) {
            City city = DaoManager.get(City.class, getCityId());
            if (ValidationHelper.isNullOrEmpty(city)) {
                Province province = DaoManager.get(Province.class, getProvinceId());
                if (!ValidationHelper.isNullOrEmpty(province))
                    return province.getDescription();
            } else
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
        } else if (getSubjectId() == null && getDistraintFormalityId() == null && !ValidationHelper.isNullOrEmpty(getId())) {
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
        } else if (ValidationHelper.isNullOrEmpty(getId()) && !ValidationHelper.isNullOrEmpty(getSubjectWrapperList())) {
            return getSubjectWrapperList().stream().map(SubjectWrapper::getFullName).collect(Collectors.joining(" / "));
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
        } else if(!ValidationHelper.isNullOrEmpty(getId())){
            Request request = DaoManager.get(Request.class, this.getId());
            if (!ValidationHelper.isNullOrEmpty(request)) {
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
                if (client.getTypeId() == null || ClientType.PROFESSIONAL.getId().equals(client.getTypeId())
                        && !ValidationHelper.isNullOrEmpty(client.getNameProfessional())) {
                    clientNameProfessional = client.getNameProfessional();
                } else if (!(client.getTypeId() == null || ClientType.PROFESSIONAL.getId().equals(client.getTypeId()))
                        && !ValidationHelper.isNullOrEmpty(client.getNameOfTheCompany())) {
                    clientNameProfessional = client.getNameOfTheCompany();
                }
            }
        }
        return clientNameProfessional;
    }


    public String haveManagers() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (getMailId() != null) {
            WLGInbox wlgInbox = DaoManager.get(WLGInbox.class, getMailId());
            if (!ValidationHelper.isNullOrEmpty(wlgInbox) && !ValidationHelper.isNullOrEmpty(wlgInbox.getManagers())) {
                return wlgInbox.getManagers()
                        .stream()
                        .distinct()
                        .map(w -> w.toString())
                        .collect(Collectors.joining(","));
            }
        }
        return null;
    }

    public String getRequestTypeName() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRequestTypeId())) {
            RequestType requestType = DaoManager.get(RequestType.class, getRequestTypeId());
            if (requestType != null) {
                setRequestTypeName(requestType.getName());
            }
        }
        return requestTypeName;
    }

    public void setRequestTypeName(String requestTypeName) {
        this.requestTypeName = requestTypeName;
    }

    public String getRequestTypeIcon() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRequestTypeId())) {
            RequestType requestType = DaoManager.get(RequestType.class, getRequestTypeId());
            if (requestType != null) {
                setRequestTypeIcon(requestType.getIcon());
            }
        }
        return requestTypeIcon;
    }

    public void setRequestTypeIcon(String requestTypeIcon) {
        this.requestTypeIcon = requestTypeIcon;
    }

    public String getServiceName() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {

        if (!ValidationHelper.isNullOrEmpty(getServiceId())) {
            Service service = DaoManager.get(Service.class, getServiceId());
            if (service != null) {
                setServiceName(service.getName());
            }
        }
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAggregationLandCharRegName() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {

        if (!ValidationHelper.isNullOrEmpty(getAggregationLandChargesRegistryId())) {
            AggregationLandChargesRegistry aggregationLandChargesRegistry = DaoManager.get(
                    AggregationLandChargesRegistry.class, getAggregationLandChargesRegistryId());
            if (aggregationLandChargesRegistry != null) {
                setAggregationLandCharRegName(aggregationLandChargesRegistry.getName());
            }
        }
        return aggregationLandCharRegName;
    }

    public void setAggregationLandCharRegName(String aggregationLandCharRegName) {
        this.aggregationLandCharRegName = aggregationLandCharRegName;
    }

    public String getServiceIcon() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getServiceId())) {
            Service service = DaoManager.get(Service.class, getServiceId());
            if (service != null) {
                setServiceIcon(service.getIcon());
            }
        }
        return serviceIcon;
    }

    public Boolean getServiceIsUpdate() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getServiceId())) {
            Service service = DaoManager.get(Service.class, getServiceId());
            if (service != null) {
                setServiceIsUpdate(service.getIsUpdate());
            }
        }
        return serviceIsUpdate;
    }

    public Long getFiduciaryId() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getMailId())) {
            WLGInbox wlgInbox = DaoManager.get(WLGInbox.class, new CriteriaAlias[]{
                    new CriteriaAlias("clientFiduciary", "c", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("id", getMailId())
            });
            if (!ValidationHelper.isNullOrEmpty(wlgInbox) && !ValidationHelper.isNullOrEmpty(wlgInbox.getClientFiduciary())) {
                setFiduciaryId(wlgInbox.getClientFiduciary().getId());
            }
        }
        return fiduciaryId;
    }

    public void setFiduciaryId(Long fiduciaryId) {
        this.fiduciaryId = fiduciaryId;
    }

    public Long getManagerId() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getMailId())) {
            WLGInbox wlgInbox = DaoManager.get(WLGInbox.class, new CriteriaAlias[]{
                    new CriteriaAlias("client", "c", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("id", getMailId())
            });
            if (!ValidationHelper.isNullOrEmpty(wlgInbox) && !ValidationHelper.isNullOrEmpty(wlgInbox.getClient())) {
                setManagerId(wlgInbox.getClient().getId());
            }
        }
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public int getDocumentsCount() throws IllegalAccessException, PersistenceBeanException {
        if(!ValidationHelper.isNullOrEmpty(getId())){
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

        }
        return documentsCount;
    }

    public String getInvoiceNumber() throws PersistenceBeanException, InstantiationException, IllegalAccessException {

        if (!ValidationHelper.isNullOrEmpty(getInvoiceId())) {
            Invoice invoice = DaoManager.get(Invoice.class, getInvoiceId());
            if (!ValidationHelper.isNullOrEmpty(invoice))
                return invoice.getInvoiceNumber();
        }
        return "";
    }

    public String getTextInVisura() {
        try {
            if(this.getId() != null && this.getId() > 0){
                Request request = DaoManager.get(Request.class, this.getId());
                if(!ValidationHelper.isNullOrEmpty(request.getSpecialFormality())
                        && !ValidationHelper.isNullOrEmpty(request.getSpecialFormality().getTextInVisura())) {
                    textInVisura = request.getSpecialFormality().getTextInVisura();
                }
            }else if(!ValidationHelper.isNullOrEmpty(getSpecialFormality())
                        && !ValidationHelper.isNullOrEmpty(getSpecialFormality().getTextInVisura())) {
                    textInVisura = getSpecialFormality().getTextInVisura();
            }
        }catch(Exception e){
            LogHelper.log(log, e);
        }
        return textInVisura;
    }
}
