package it.nexera.ris.web.beans.pages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import it.nexera.ris.common.enums.DocumentGenerationPlaces;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.RequestState;
import it.nexera.ris.common.enums.ServiceReferenceTypes;
import it.nexera.ris.common.enums.UserCategories;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.EstateSituationHelper;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.GeneralFunctionsHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.PrintPDFHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.RequestHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.SaveRequestDocumentsHelper;
import it.nexera.ris.common.helpers.SelectItemHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.SubjectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.create.xls.CreateExcelRequestsReportHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.ReportFormalitySubject;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.RequestOLD;
import it.nexera.ris.persistence.beans.entities.domain.SectionC;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.VisureDH;
import it.nexera.ris.persistence.beans.entities.domain.VisureRTF;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.persistence.beans.entities.domain.readonly.RequestShort;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.persistence.view.RequestView;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.logic.RequestStateWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RequestTypeFilterWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ServiceFilterWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserFilterWrapper;
import org.apache.commons.lang.Validate;

@ManagedBean(name = "requestListBean")
@ViewScoped
public class RequestListBean extends EntityLazyListPageBean<RequestView>
        implements Serializable {

    private static transient final Log log = LogFactory.getLog(RequestListBean.class);

    private static final long serialVersionUID = -5590180358388236956L;

    private Date dateFrom;

    private Date dateTo;

    private Date dateFromEvasion;

    private Date dateToEvasion;

    private Date dateExpiration;

    private Long selectedClientId;

    private List<SelectItem> clients;

    private Long selectedRequestType;

    private List<SelectItem> requestTypes;

    private Long selectedServiceType;

    private List<SelectItem> serviceTypes;

    private String searchLastName;

    private String searchFiscalCode;

    private String searchCreateUser;

    private Long downloadRequestId;

    private Long selectedTemplateId;

    private List<SelectItem> templates;

    private Request downloadRequest;

    private List<RequestStateWrapper> stateWrappers;

    private RequestStateWrapper selectedStateForFilter;

    private List<UserFilterWrapper> userWrappers;

    private UserFilterWrapper selectedUserForFilter;

    private List<Document> requestDocuments;

    private Boolean showPrintButton;

    private Long selectedState;

    private List<SelectItem> statesForSelect;

    private Long selectedUser;

    private List<SelectItem> usersForSelect;

    private List<Criterion> filterRestrictions;

    private List<RequestView> allRequestViewsToModify;

    private String selectedUserType;

    private List<SelectItem> userTypes;

    private List<SelectItem> landAggregations;
    
    private Long aggregationFilterId;

    private byte[] anomalyRequestsFile;

    private Boolean createTotalCostSumDocumentRecord;
    
    private List<SelectItem> fiduciaryClients;
    
    private Long fiduciaryClientFilterId;
    
    private List<SelectItem> managerClients;
    
    private Long managerClientFilterid;
    
    private List<RequestStateWrapper> selectedRequestStates;
    
    private List<ServiceFilterWrapper> serviceWrappers; 

    private ServiceFilterWrapper selectedServiceForFilter;
    
    private List<SelectItem> servicesForSelect;
    
    private List<RequestTypeFilterWrapper> requestTypeWrappers;

    private RequestTypeFilterWrapper selectedRequestTypeForFilter;
    
    private List<SelectItem> requestTypesForSelect;
    
    private List<SelectItem> cities;
    
    private Integer expirationDays;

    private List<RequestState> selectedStates;

    private List<RequestType> selectedRequestTypes;

    private List<Service> selectedServices;
    
    private static final String KEY_CLIENT_ID = "KEY_CLIENT_ID_SESSION_KEY_NOT_COPY";
    private static final String KEY_STATES = "KEY_STATES_SESSION_KEY_NOT_COPY";
    private static final String KEY_REQUEST_TYPE = "KEY_REQUEST_TYPE_SESSION_KEY_NOT_COPY";
    private static final String KEY_SERVICES = "KEY_SERVICES_SESSION_KEY_NOT_COPY";
    private static final String KEY_CLIENT_MANAGER_ID = "KEY_CLIENT_MANAGER_ID_SESSION_KEY_NOT_COPY";
    private static final String KEY_CLIENT_FIDUCIARY_ID = "KEY_CLIENT_FIDUCIARY_ID_SESSION_KEY_NOT_COPY";
    private static final String KEY_AGGREAGATION = "KEY_AGGREAGATION_SESSION_KEY_NOT_COPY";
    private static final String KEY_DATE_EXPIRATION = "KEY_DATE_EXPIRATION_SESSION_KEY_NOT_COPY";
    private static final String KEY_DATE_FROM_REQ = "KEY_DATE_FROM_REQ_SESSION_KEY_NOT_COPY";
    private static final String KEY_DATE_TO_REQ = "KEY_DATE_TO_REQ_SESSION_KEY_NOT_COPY";
    private static final String KEY_DATE_FROM_EVASION = "KEY_DATE_FROM_EVASION_SESSION_KEY_NOT_COPY";
    private static final String KEY_DATE_TO_EVASION = "KEY_DATE_TO_EVASION_SESSION_KEY_NOT_COPY";
    
    
    

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {

        setSearchLastName((String) SessionHelper.get("searchLastName"));
        setSearchFiscalCode((String) SessionHelper.get("searchFiscalCode"));
        setSearchCreateUser((String) SessionHelper.get("searchCreateUser"));
        setStateWrappers(new ArrayList<>());
        setUserWrappers(new ArrayList<>());
        setStatesForSelect(new ArrayList<>());
        setUsersForSelect(new ArrayList<>());
        setServiceWrappers(new ArrayList<>());
        setServicesForSelect(new ArrayList<>());
        setRequestTypeWrappers(new ArrayList<>());
        setRequestTypesForSelect(new ArrayList<>());


            setClients(ComboboxHelper.fillList(DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"))
        }).stream()
                    .filter(c -> (
                            (ValidationHelper.isNullOrEmpty(c.getManager()) || !c.getManager()) &&
                            (ValidationHelper.isNullOrEmpty(c.getFiduciary()) || !c.getFiduciary())
                            )
                    ).sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList()),Boolean.TRUE));

        setRequestTypes(ComboboxHelper.fillList(RequestType.class, Boolean.FALSE));
        if(getRequestTypes().size() > 0) {
            Collections.sort(getRequestTypes(), new Comparator<SelectItem>() {
                @Override
                public int compare(final SelectItem object1, final SelectItem object2) {
                    return object1.getLabel().toUpperCase().compareTo(object2.getLabel().toUpperCase());
                }
            });
       }
        
        setServiceTypes(ComboboxHelper.fillList(Service.class, Boolean.TRUE));
        if(getServiceTypes().size() > 0) {
            Collections.sort(getServiceTypes(), new Comparator<SelectItem>() {
                @Override
                public int compare(final SelectItem object1, final SelectItem object2) {
                    return object1.getLabel().toUpperCase().compareTo(object2.getLabel().toUpperCase());
                }
            });
       }
        setUserTypes(ComboboxHelper.fillList(UserCategories.class, Boolean.FALSE));
        setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.TRUE));
        
        setFiduciaryClients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                Restrictions.eq("fiduciary", Boolean.TRUE),
        }, Boolean.FALSE));
        
        setManagerClients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                Restrictions.eq("manager", Boolean.TRUE),
        }, Boolean.FALSE));

       

        List<User> notExternalCategoryUsers = DaoManager.load(User.class
                , new Criterion[]{Restrictions.or(
                        Restrictions.eq("category", UserCategories.INTERNO),
                        Restrictions.isNull("category"))});

        notExternalCategoryUsers.forEach(u -> getUserWrappers().add(new UserFilterWrapper(u)));

        List<Service> services = DaoManager.load(Service.class, new Criterion[]{Restrictions.isNotNull("name")});
        if(!ValidationHelper.isNullOrEmpty(services)) {
            Collections.sort(services, new Comparator<Service>() {
                @Override
                public int compare(final Service object1, final Service object2) {
                    return object1.toString().toUpperCase().compareTo(object2.toString().toUpperCase());
                }
            });
            services.forEach(s -> getServiceWrappers().add(new ServiceFilterWrapper(s)));
        }
        
       
        if(!ValidationHelper.isNullOrEmpty(getSearchLastName()) || !ValidationHelper.isNullOrEmpty(getSearchFiscalCode())
                || !ValidationHelper.isNullOrEmpty(getSearchCreateUser())) {
            setSelectedAllStatesOnPanel(true);
        }
        
        Long dueRequestTypeId =(Long)SessionHelper.get("dueRequestTypeId");
        
        if(!ValidationHelper.isNullOrEmpty(dueRequestTypeId)) {
            SessionHelper.removeObject("dueRequestTypeId");
            List<RequestType> requestTypes = DaoManager.load(RequestType.class, new Criterion[]{Restrictions.isNotNull("name")});
            if (!ValidationHelper.isNullOrEmpty(requestTypes)) {
                Collections.sort(requestTypes, new Comparator<RequestType>() {
                    @Override
                    public int compare(final RequestType object1, final RequestType object2) {
                        return object1.toString().toUpperCase().compareTo(object2.toString().toUpperCase());
                    }
                });
                requestTypes.forEach(r -> {
                    getRequestTypeWrappers().add(new RequestTypeFilterWrapper(r.getId().equals(dueRequestTypeId),r));
                });
            }
            for(RequestState rs: RequestState.values()) {
                getStateWrappers().add(new RequestStateWrapper(!RequestState.EVADED.equals(rs) , rs));
            }
            Integer expirationDays = (Integer)SessionHelper.get("expirationDays");
            if(!ValidationHelper.isNullOrEmpty(expirationDays)) {
                SessionHelper.removeObject("expirationDays");
                setExpirationDays(expirationDays);
            }
        }else {
            setExpirationDays(null);
             Arrays.asList(RequestState.values()).forEach(st -> getStateWrappers()
                .add(new RequestStateWrapper(PageTypes.REPORT_LIST.equals(getCurrentPage())
                        ? RequestState.EVADED.equals(st) : st.isNeedShow(), st)));
             
            List<RequestType> requestTypes = DaoManager.load(RequestType.class, new Criterion[]{Restrictions.isNotNull("name")});
            if (!ValidationHelper.isNullOrEmpty(requestTypes)) {
                Collections.sort(requestTypes, new Comparator<RequestType>() {
                    @Override
                    public int compare(final RequestType object1, final RequestType object2) {
                        return object1.toString().toUpperCase().compareTo(object2.toString().toUpperCase());
                    }
                });
                requestTypes.forEach(r -> getRequestTypeWrappers().add(new RequestTypeFilterWrapper(r)));
            }

        }
        String filterStateBy =  (String)SessionHelper.get("REQUEST_LIST_FILTER_BY");
        if(!ValidationHelper.isNullOrEmpty(filterStateBy)){
            getStateWrappers().forEach(r -> {
                if (r.getState().equals(RequestState.valueOf(filterStateBy))){
                    r.setSelected(Boolean.TRUE);
                }else{
                    r.setSelected(Boolean.FALSE);
                }
            });
            SessionHelper.removeObject("REQUEST_LIST_FILTER_BY");
        }
        loadFilterValueFromSession();
        filterTableFromPanel();
    }

    public void loadRequestDocuments() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request.id", getEntityEditId()),
                Restrictions.eq("selectedForEmail", true)
                //,Restrictions.ne("typeId", DocumentType.FORMALITY.getId())
        });

        List<Document> formalities = DaoManager.load(Document.class, new CriteriaAlias[]{
                new CriteriaAlias("formality", "f", JoinType.INNER_JOIN),
                new CriteriaAlias("f.requestList", "r_f", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("r_f.id", getEntityEditId()),
                Restrictions.eq("request.id", getEntityEditId()),
                Restrictions.eq("selectedForEmail", true)
        });

        if (!ValidationHelper.isNullOrEmpty(formalities)) {
            for (Document temp : formalities) {
                if (!documents.contains(temp)) {
                    documents.add(temp);
                }
            }
        }
        setRequestDocuments(documents);
    }


    public void loadAllegatiDocuments() throws PersistenceBeanException, IllegalAccessException {
        List<Document> documents = getAllegatiDocuments(getEntityEditId());
        setRequestDocuments(documents);
    }

    private List<Document> getAllegatiDocuments(Long requestId)
        throws PersistenceBeanException, IllegalAccessException {
        List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request.id", requestId),
                Restrictions.eq("typeId", DocumentType.ALLEGATI.getId())
        });

        List<Document> formalities = DaoManager.load(Document.class, new CriteriaAlias[]{
                new CriteriaAlias("formality", "f", JoinType.INNER_JOIN),
                new CriteriaAlias("f.requestList", "r_f", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("r_f.id", requestId),
                Restrictions.eq("typeId", DocumentType.ALLEGATI.getId())
        });

        if (!ValidationHelper.isNullOrEmpty(formalities)) {
            for (Document temp : formalities) {
                if (!documents.contains(temp)) {
                    documents.add(temp);
                }
            }
        }
        return documents;
    }

    public void downloadPdfFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        try {

            Request request = DaoManager.get(Request.class, downloadRequestId);

            String body = getPdfRequestBody(request);
            updateFilterValueInSession();
            
            FileHelper.sendFile("richiesta-" + request.getStrId() + ".pdf",
                    PrintPDFHelper.convertToPDF(null, body, null,
                            DocumentType.ESTATE_FORMALITY));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static String getPdfRequestBody(Request request) {
        return getFirstPart(request) + getSecondPart(request) +
        		getThirdPart(request);
    }

    public static String getPdfRequestBody(Request request, Subject subject) {
        return getFirstPart(request, subject) + getSecondPart(request, subject) +
        		getThirdPart(request, subject);
    }

    public static String getThirdPart(Request request) {
    	return ( (request == null) || (request.getSubject() == null) ) ? "":
    		getThirdPart(request, request.getSubject());
    }

    public static String getThirdPart(Request request, Subject subject) {
        String thirdPart = "";

    	try {

    		if (!ValidationHelper.isNullOrEmpty(subject)) {

    			thirdPart += "<hr/>";
    			thirdPart += "<b>Richieste</b>:<br/>";

                List<RequestShort> requestList;
                List<Criterion> criteria = new ArrayList<Criterion>();

                List<Long> subjectsIds = EstateSituationHelper.getIdSubjects(request);
                subjectsIds.add(subject.getId());

				criteria.add(Restrictions.in("subject.id", subjectsIds));

				if(request != null)
					criteria.add(Restrictions.ne("id", request.getId()));

                criteria.add(Restrictions.or(Restrictions.eq("isDeleted", false),
                        Restrictions.isNull("isDeleted")));

    			requestList = DaoManager.load(RequestShort.class, criteria.toArray(new Criterion[0]),
    					Order.desc("createDate"));

    			
    			for(RequestShort r : requestList) {
    			    thirdPart +=
    			            (request.getSubject().getId().equals(r.getSubject().getId()) ? "" : "PRES - ") +
    			            r.getCreateDateStr() +
    			            " - " +
    			            r.getClientName() +
    			            " - " +
    			            r.getServiceName() +
    			            " - " +
    			            r.getAggregationLandChargesRegistryName() +
    			            "<br/>";

    			    if(!ValidationHelper.isNullOrEmpty(r.getMultipleServices())) {
    			        thirdPart += "<ul>";
    			        for(Service service : r.getMultipleServices()) {
    			            thirdPart += "<li>";
    			            thirdPart += service.getName();
    			            thirdPart += "</li>";
    			        }
    			        thirdPart += "</ul>";
    			    }
    			}

                List<RequestOLD> requestOLDS = DaoManager.load(RequestOLD.class, new Criterion[] {
                       subject.getTypeIsPhysicalPerson() ?
                                Restrictions.eq("fiscalCodeVat", subject.getFiscalCode()) :
                                Restrictions.eq("fiscalCodeVat", subject.getNumberVAT())});

                for(RequestOLD old : requestOLDS) {
                    thirdPart +=
                            old.getRequestDateString() + " - " +
                            old.getClient() + " - " +
                            old.getType() + " - " +
                            old.getLandChargesRegistry() +
                             "<br/>";
                }
//                
//                if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
//                    thirdPart += "<ul>";
//                    for(Service service : request.getMultipleServices()) {
//                        thirdPart += "<li>";
//                        thirdPart += service.getName();
//                        thirdPart += "</li>";
//                    }
//                    thirdPart += "</ul>";
//                }

                thirdPart += "<br/>";

                thirdPart += "<hr/>";

                thirdPart += "<b>Visure a testo:</b><br/>";

                List<VisureRTF> visureRTFS = DaoManager.load(VisureRTF.class, new Criterion[] {
                        subject.getTypeIsPhysicalPerson() ?
                                Restrictions.eq("fiscalCodeVat", subject.getFiscalCode()) :
                                Restrictions.eq("fiscalCodeVat", subject.getNumberVAT()) });

                for(VisureRTF rtf : visureRTFS) {
                    thirdPart +=
                            DateTimeHelper.toString(rtf.getUpdateDate()) + " - " +
                                    rtf.getNumFormality() + " - " +
                                    rtf.getLandChargesRegistry() +
                                    "<br/>";
                }

                thirdPart += "<br/>";

                thirdPart += "<hr/>";

                thirdPart += "<b>Visure DH:</b><br/>";

                List<VisureDH> visureDHS = DaoManager.load(VisureDH.class, new Criterion[] {
                        subject.getTypeIsPhysicalPerson() ?
                                Restrictions.eq("fiscalCodeVat", subject.getFiscalCode()) :
                                Restrictions.eq("fiscalCodeVat", subject.getNumberVAT())});

                for(VisureDH dh : visureDHS) {
                    thirdPart +=
                            dh.getType() + " - " +
                            DateTimeHelper.toString(dh.getUpdateDate()) + " - " +
                            dh.getNumFormality() + " - " +
                            dh.getNumberPractice() + " - " +
                            dh.getLandChargesRegistry() +
                            "<br/>";
                }

    			thirdPart += "<br/>";

    			thirdPart += "<hr/>";

                thirdPart += "<b>Formalit&agrave;:</b><br/>";

                int countOfRequests = 0;

                if (request != null) {
                    countOfRequests = 1;
                } else {
                    countOfRequests = requestList.size();
                }

                List<Formality> formalityList = new ArrayList<>();

                for (int i = 0; i < countOfRequests; ++i) {

    	            List<Long> listIds = EstateSituationHelper.getIdSubjects(subject);
    	            listIds.add(subject.getId());
    	        	criteria = new ArrayList<>();

    	        	criteria.add(Restrictions.in("sub.id", listIds));
    	            List<Formality> list =
    	            		DaoManager.load(Formality.class, new CriteriaAlias[]{new CriteriaAlias
    	                    ("sectionC", "sectionC", JoinType.INNER_JOIN),
    	                    new CriteriaAlias("sectionC.subject", "sub", JoinType.INNER_JOIN)
    	            }, criteria.toArray(new Criterion[0]));

    	            formalityList.addAll(list);
    	            }

                for (Formality f : formalityList) {
                    boolean isPresumptive = f.getSectionC().stream().map(SectionC::getSubject).flatMap(List::stream)
                            .noneMatch(x -> x.getId().equals(request.getSubject().getId()));

                    thirdPart +=
                            (isPresumptive ? "PRES - " : "") +
                                    f.getConservatoryStr() + " - " +
                                    DateTimeHelper.toString(f.getPresentationDate()) + " - " +
                                    (f.getType() == null || "null".equalsIgnoreCase(f.getType()) ? "" : f.getType().toUpperCase() + " - ") +
                                    (f.getParticularRegister() == null ? "" : f.getParticularRegister() + " - ") +
                                    (f.getGeneralRegister() == null ? "" : f.getGeneralRegister() + " - ") +
                                    f.getActType();

                    thirdPart += "<br/>";
                }

    			thirdPart += "<br/>";

    			thirdPart += "<hr/>";

    			thirdPart += "<b>Segnalazioni:</b><br/>";

    			List<ReportFormalitySubject> rfsList =
    					DaoManager.load(ReportFormalitySubject.class,
    							new Criterion[] {
									subject.getTypeIsPhysicalPerson() ?
									Restrictions.eq("fiscalCode", subject.getFiscalCode()) :
									Restrictions.eq("numberVAT", subject.getNumberVAT())
    					}, Order.desc("createDate"));

                for (ReportFormalitySubject rfs : rfsList) {
                    if (rfs.getTypeFormalityId().equals(1L)) {
                        thirdPart += "Trascrizione - ";
                    } else if (rfs.getTypeFormalityId().equals(2L)) {
                        thirdPart += "Iscrizione - ";
                    } else {
                        thirdPart += "Annotamento - ";
                    }
    				thirdPart +=
    						DateTimeHelper.toString(rfs.getDate()) + " - " +
                            (rfs.getNumber() == null ? "" : rfs.getNumber() + " - ") +
    						((rfs.getLandChargesRegistry() == null) ? "" : rfs.getLandChargesRegistry().getName()) +
    						"<br/>";
    			}


    			if(subject.getTypeIsPhysicalPerson()) {
    				thirdPart += "<hr/>";
        			thirdPart += "<b>Presumibili:</b><br/>";

        		
        			List<Subject> subjects = SubjectHelper.getPresumablesForSubject(
        			        subject);
        			
        			subjects.removeIf(s -> s.equals(subject));
        			for(Subject s :subjects) {

                        thirdPart += s.getFullName() + " - " + s.getSexType().getShortValue() + " - " +
                                DateTimeHelper.toString(s.getBirthDate()) + " - " +
                                ((s.getForeignCountry() != null && s.getForeignCountry())
                                        ? (s.getCountry().getDescription() + " (EE) ")
                                        : (s.getBirthCityDescription()
                                        + (s.getBirthProvince() != null ? s.getBirthProvince().getCode() : " ")))
                                + "nato il " + DateTimeHelper.toString(s.getBirthDate()) + " - " +
                                s.getFiscalCode() +
                                "<br/>";
                    }
                }
    		}
    	} catch (Exception e) {
            LogHelper.log(log, e);
    		return "ERROR IN THIRD PART";
    	}

    	return thirdPart;
    }

    private static String getSecondPart(Request request) {
    	return ( (request == null) || (request.getSubject() == null) ) ? "":
    		getSecondPart(request, request.getSubject());
    }

    private static String getSecondPart(Request request, Subject subject) {
        String secondPart = "";

        if (!ValidationHelper.isNullOrEmpty(request)) {

        if (!ValidationHelper.isNullOrEmpty(request.getNdg())) {
            secondPart += "NDG: " + request.getNdg() + "<br/>";
        }
        if (!ValidationHelper.isNullOrEmpty(request.getPosition())) {
            secondPart += "Posizione: " + request.getPosition() + "<br/>";
        }
        if (!ValidationHelper.isNullOrEmpty(request.getCreateUserId())) {
            secondPart += "Utente: " + request.getCreateUserName() + "<br/>";
        }
        if (!ValidationHelper.isNullOrEmpty(request.getUserOfficeId())) {
            Office office = new Office();
            try {
                office = DaoManager.get(Office.class, request.getUserOfficeId());
            } catch (PersistenceBeanException | InstantiationException | IllegalAccessException e) {
              //  LogHelper.log(log, e);
            }
            if (!ValidationHelper.isNullOrEmpty(office)) {
                secondPart += "Filiale: " + office.getCode() + " " + office.getDescription() + "<br/>";
            }
        }
        if (!ValidationHelper.isNullOrEmpty(request.getNote())) {
            secondPart += "Note: " + request.getNote() + "<br/>";
        } else if (!ValidationHelper.isNullOrEmpty(request.getUltimaResidenza())) {
            secondPart += "Note: " + request.getUltimaResidenza() + "<br/>";
        }

        }

        return secondPart;
    }

    public static String getFirstPart(Request request) {
    	return ( (request == null) || (request.getSubject() == null) ) ? "":
    		getFirstPart(request, request.getSubject());
    }

    private static String getFirstPart(Request request, Subject subject) {
        String result = "";

        if (!ValidationHelper.isNullOrEmpty(request)) {

        if (!ValidationHelper.isNullOrEmpty(request.getClientName())) {
            result += "Cliente: " + request.getClientName() + "<br/>";
        }
        if (!ValidationHelper.isNullOrEmpty(request.getCreateDate())) {
            result += "Data richiesta: " + request.getCreateDateStr() + "<br/>";
        }
        if (!ValidationHelper.isNullOrEmpty(request.getRequestType())) {
            result += "Servizio: " + request.getRequestTypeName() + "<br/>";
        }
        if (!ValidationHelper.isNullOrEmpty(request.getService())) {
            result += "Tipo Richiesta: " + request.getServiceName() + "<br/>";
            result += "Ufficio: " + request.getService().getEmailTextCamelCase() + " ";
        }
        if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())) {
            result += request.getAggregationLandChargesRegistryName() + "<br/>";
        } else if (!ValidationHelper.isNullOrEmpty(request.getCity())) {
            result += request.getCityDescription() + "<br/>";
        }

        if (!ValidationHelper.isNullOrEmpty(request.getUrgent()) && request.getUrgent()) {
            result += "Urgente: <b>S</b> <br/>";
        } else {
            result += "Urgente: <b>N</b> <br/>";
        }
        }
        
        if (!ValidationHelper.isNullOrEmpty(subject)) {
            if (subject.getTypeIsPhysicalPerson()) {
                result += "Soggetto: " + subject.getSurnameUpper() + " "
                        + subject.getNameUpper() + "<br/>";
                result += "Tipo: " + subject.getSexType().getShortValue() + "<br/>";
            } else if (!ValidationHelper.isNullOrEmpty(subject.getBusinessName())) {
                result += "Soggetto: " + subject.getBusinessName() + "<br/>";

            }
            if (!ValidationHelper.isNullOrEmpty(subject.getBirthCity()) &&
                !ValidationHelper.isNullOrEmpty(subject.getBirthProvince())) {

                result += "Dati Anagrafici: " + (subject.getTypeIsPhysicalPerson() ? "nato a " : "con sede in ")
                        +
                               ( (subject.getForeignCountry() != null &&
        		subject.getForeignCountry()) ?
        	( subject.getCountry().getDescription() + " (EE) " ) :

                        (subject.getBirthCityDescription() + " ( "
                        + subject.getBirthProvince().getCode() + " ) "));

                if (!ValidationHelper.isNullOrEmpty(subject.getBirthDate())) {
                    result += "il " + DateTimeHelper.toString(subject.getBirthDate());
                }
            }else if(!ValidationHelper.isNullOrEmpty(subject.getCountry())) {
            	result += "Dati Anagrafici: " + (subject.getTypeIsPhysicalPerson() ? "nato in " : "con sede in ")
                        +( subject.getCountry().getDescription() + " (EE) " );
            }

            result += "<br/>";
            if (!ValidationHelper.isNullOrEmpty(subject.getFiscalCode())) {
                result += "C.F. " + subject.getFiscalCode() + "<br/>";
            } else if (!ValidationHelper.isNullOrEmpty(subject.getNumberVAT())) {
                result += " P.IVA: " + subject.getNumberVAT() + "<br/>";
            }
        }

        return result;
    }

    public void prepareToModify() {
        setShowPrintButton(true);
        getStatesForSelect().add(SelectItemHelper.getNotSelected());
        getUsersForSelect().add(SelectItemHelper.getNotSelected());
        getServicesForSelect().add(SelectItemHelper.getNotSelected());
        getRequestTypesForSelect().add(SelectItemHelper.getNotSelected());
        Arrays.asList(RequestState.values()).forEach(st -> getStatesForSelect()
            .add(new SelectItem(st.getId(), st.toString())));
        getUserWrappers().forEach(u -> getUsersForSelect().add(new SelectItem(u.getId(), u.getValue())));
        getServiceWrappers().forEach(s -> getServicesForSelect().add(new SelectItem(s.getId(), s.getValue())));
        getRequestTypeWrappers().forEach(r -> getRequestTypesForSelect().add(new SelectItem(r.getId(), r.getValue())));
    }

    public void modifyRequests()
        throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        filterTableFromPanel();
        setAllRequestViewsToModify(
            DaoManager.load(RequestView.class, getFilterRestrictions().toArray(new Criterion[0])));
        List<Long> requestIdList = getAllRequestViewsToModify().stream()
            .map(RequestView::getId).collect(Collectors.toList());
        if (!ValidationHelper.isNullOrEmpty(getSelectedState())) {
            for (Long id : requestIdList) {
                RequestHelper.updateState(id, getSelectedState());
            }
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedUser())) {
            for (Long id : requestIdList) {
                RequestHelper.updateUser(id, getSelectedUser());
            }
        }
    }

    public void loadRequestsExcel() throws PersistenceBeanException,
            IllegalAccessException, IOException, InstantiationException {
        filterTableFromPanel();
        setAllRequestViewsToModify(DaoManager.load(RequestView.class, getFilterRestrictions().toArray(new Criterion[0])));
        List<Long> requestIdList = getAllRequestViewsToModify().stream().map(RequestView::getId).collect(Collectors.toList());
        if (!ValidationHelper.isNullOrEmpty(requestIdList)) {
            List<Request> requests = DaoManager.load(Request.class, new Criterion[]{Restrictions.in("id", requestIdList)});
            String fileName = "evasioni" + ".xls";

            boolean isItInvoiceReport = !ValidationHelper.isNullOrEmpty(getCreateTotalCostSumDocumentRecord())
                    && getCreateTotalCostSumDocumentRecord();

            byte[] generatedExcel;
            if (!ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
                generatedExcel = new CreateExcelRequestsReportHelper(isItInvoiceReport)
                        .convertFilteredRequestsToExcel(requests, getSelectedClientId());
            } else {
                generatedExcel = new CreateExcelRequestsReportHelper(isItInvoiceReport)
                        .convertFilteredRequestsToExcel(requests);
            }

            if (isItInvoiceReport) {
                fileName = createTotalCostSumDocumentRecord(requests, generatedExcel);
            }

            FileHelper.sendFile(fileName, generatedExcel);
        }
    }

    private String createTotalCostSumDocumentRecord(List<Request> requests, byte[] generatedExcel)
            throws IOException, PersistenceBeanException, IllegalAccessException {
        long invoiceNumber = SaveRequestDocumentsHelper.getLastInvoiceNumber() + 1;
        String fileName = "evasioni_" + invoiceNumber + ".xls";

        String path = FileHelper.writeFileToFolder(fileName,
                new File(FileHelper.getApplicationProperties().getProperty("requestReportSavePath")), generatedExcel);

        Document totalCostSumDocument = new Document();
        double totalCostSum = requests.stream().filter(x -> !ValidationHelper.isNullOrEmpty(x.getTotalCost()))
                .map(x -> Double.parseDouble(x.getTotalCostDouble())).reduce(0.0, Double::sum);
        totalCostSumDocument.setCost(String.format("%.2f", totalCostSum));
        totalCostSumDocument.setTypeId(DocumentType.INVOICE_REPORT.getId());
        totalCostSumDocument.setInvoiceNumber(invoiceNumber);
        totalCostSumDocument.setPath(path);
        totalCostSumDocument.setDate(new Date());
        totalCostSumDocument.setTitle(fileName.substring(0, fileName.indexOf(".xls")));

        DaoManager.save(totalCostSumDocument, true);

        return fileName;
    }

    public void loadRequestsPdf() throws PersistenceBeanException, IllegalAccessException {
        setAllRequestViewsToModify(DaoManager.load(RequestView.class,
            getFilterRestrictions().toArray(new Criterion[0])));
        List<Long> requestIdList = getAllRequestViewsToModify().stream()
            .map(RequestView::getId).collect(Collectors.toList());
        try {
            Map<String, byte[]> files = new HashMap<>();
            Integer fileCounter = 1;
            for (Long requestId : requestIdList) {
                Request request = DaoManager.get(Request.class, requestId);

                List<Document> documents = getAllegatiDocuments(requestId);
                for (Document document : documents) {
                    try {
                        if (!ValidationHelper.isNullOrEmpty(document)) {
                            File file = new File(document.getPath());
                            if (!ValidationHelper.isNullOrEmpty(document.getTitle())) {
                                String title = prepareDocumentTitle(document);
                                FileInputStream inputFile = new FileInputStream(file);
                                byte[] data = new byte[(int) file.length()];
                                inputFile.read(data);
                                files.put(fileCounter++ + "_" + title, data);
                                if (inputFile != null) {
                                    inputFile.close();
                                }
                            }
                        }
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                }

                String body = getPdfRequestBody(request);
                files.put(fileCounter++ + "_richiesta-" + request.getStrId() + ".pdf",
                        PrintPDFHelper.convertToPDF(null, body, null,
                                DocumentType.ESTATE_FORMALITY));
            }
            FileHelper.downloadFiles(files);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void downloadRequestFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
            Document document = DaoManager.get(Document.class, getEntityEditId());
            switch (DocumentType.getById(document.getTypeId())) {
                case CADASTRAL:
                case ESTATE_FORMALITY: {
                    String projectUrl = this.getRequest().getHeader("referer");
                    projectUrl = projectUrl.substring(0, projectUrl.indexOf(this.getCurrentPage().getPagesContext())) + "/";
                    PrintPDFHelper.generatePDFOnDocument(document.getId(), projectUrl);
                }
                break;
                case FORMALITY:
                case REQUEST_REPORT:
                case ALLEGATI:
                case OTHER: {
                    File file = new File(document.getPath());
                    if (!ValidationHelper.isNullOrEmpty(document)) {
                        String title = prepareDocumentTitle(document);
                        try {
                            FileHelper.sendFile(title,
                                new FileInputStream(file), (int) file.length());
                        } catch (Exception e) {
                            LogHelper.log(log, e);
                        }
                    }
                }
                break;
            }
        }
    }

    private String prepareDocumentTitle(Document document) {
        String path = !ValidationHelper.isNullOrEmpty(document.getPath()) ? document.getPath() : "";
        if (!ValidationHelper.isNullOrEmpty(document.getTitle())) {
            String title = document.getTitle();
            if (title.contains(".")) {
                int point = title.lastIndexOf(".");
                return title.substring(0, point) + path.substring(path.lastIndexOf("."));
            } else {
                return title + path.substring(path.lastIndexOf("."));
            }
        } else {
            return path.substring(path.lastIndexOf("\\") + 1);
        }
    }

    public void createNewRequest() {
        RedirectHelper.goTo(PageTypes.REQUEST_EDIT);
    }

    public void createNewMultipleRequest() {
        RedirectHelper.goToMultiple(PageTypes.REQUEST_EDIT);
    }

    public void manageRequest() {

        SessionHelper.put("searchLastName", getSearchLastName());
        SessionHelper.put("searchFiscalCode", getSearchFiscalCode());
        SessionHelper.put("searchCreateUser", getSearchCreateUser());
        updateFilterValueInSession();
        RedirectHelper.goTo(PageTypes.REQUEST_EDIT, getEntityEditId());
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = RequestHelper.filterTableFromPanel(getDateFrom(), getDateTo(), getDateFromEvasion(),
                getDateToEvasion(), getSelectedClientId(), getRequestTypeWrappers(), getStateWrappers(), getUserWrappers(),
                getServiceWrappers(), getSelectedUserType(),getAggregationFilterId(), getSelectedServiceType(), Boolean.FALSE);

        if (!ValidationHelper.isNullOrEmpty(getSearchLastName())) {
            restrictions.add(
                    Restrictions.or(
                            Restrictions.sqlRestriction("replace(replace(replace(name, '.', ''),'\\'',''), '  ', ' ') like '%"
                                    + getSearchLastName().replaceAll("\\.", "")
                                    .replaceAll("\\s+", " ")
                                    .replaceAll("'", "").trim() + "%'"),
                            Restrictions.sqlRestriction("replace(replace(replace(reverse_name, '.', ''),'\\'',''), '  ', ' ') like '%"
                                    + getSearchLastName().replaceAll("\\.", "")
                                    .replaceAll("\\s+", " ")
                                    .replaceAll("'", "").trim() + "%'")
                            )

            );
        }

        if (!ValidationHelper.isNullOrEmpty(getSearchFiscalCode())) {
            restrictions.add(Restrictions.ilike("code", getSearchFiscalCode().trim(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(getDateExpiration())) {
            restrictions.add(Restrictions.ge("expirationDate",
                    DateTimeHelper.getDayStart(getDateExpiration())));
            restrictions.add(Restrictions.le("expirationDate",
                    DateTimeHelper.getDayEnd(getDateExpiration())));
        }
        
        if (!ValidationHelper.isNullOrEmpty(getExpirationDays())) {
            Date dueDate = DateTimeHelper.addDays(DateTimeHelper.getNow(), getExpirationDays());
            restrictions.add(getExpirationDays() == 0 ? Restrictions.le("expirationDate",dueDate) : 
                Restrictions.ge("expirationDate",dueDate));
        }

        if (getCurrentUser().isExternal()) {
            restrictions.add(Restrictions.eq("createUserId", getCurrentUser().getId()));
        }

        if (!ValidationHelper.isNullOrEmpty(getSearchCreateUser())) {
            List<Long> longList = getUserListIds(getSearchCreateUser());
            if (!ValidationHelper.isNullOrEmpty(longList)) {
                restrictions.add(Restrictions.in("createUserId", longList));
            }
        }

        if (PageTypes.NOTARIAL_CERTIFICATION_LIST.equals(getCurrentPage())) {
            restrictions.add(Restrictions.isNotNull("distraintFormalityId"));
            if (!getCanList() && getCanListCreatedByUser()) {
                restrictions.add(Restrictions.eq("createUserId",
                        UserHolder.getInstance().getCurrentUser().getId()));
            }
        }

        if (!ValidationHelper.isNullOrEmpty(getFiduciaryClientFilterId())) {
            restrictions.add(Restrictions.eq("fiduciaryId",
            		getFiduciaryClientFilterId()));
        }
        

        if (!ValidationHelper.isNullOrEmpty(getManagerClientFilterid())) {
            restrictions.add(Restrictions.eq("managerId",
            		getManagerClientFilterid()));
        }
        
        setFilterRestrictions(restrictions);
        loadList(RequestView.class, restrictions.toArray(new Criterion[0]),
                new Order[]{Order.desc("createDate")});
        
        List<RequestView> requestList = DaoManager.load(RequestView.class, restrictions.toArray(new Criterion[0]));
        
        List<Long> cityIds = new ArrayList<Long>();
        
        
        for(RequestView request : requestList) {
            if(!ValidationHelper.isNullOrEmpty(request.getCityId()) && !cityIds.contains(request.getCityId())) {
                cityIds.add(request.getCityId());
            }
        }
        if(!ValidationHelper.isNullOrEmpty(cityIds)) {
            setCities(ComboboxHelper.fillList(City.class,
                    Order.asc("description"),
                    new Criterion[]{Restrictions.isNotNull("province.id")
                            , Restrictions.eq("external", Boolean.TRUE),Restrictions.in("id", cityIds)}, Boolean.FALSE));
        }
        
    }

    public void verifyRequests() throws PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        filterTableFromPanel();
        List<Criterion> criterionList = getFilterRestrictions();
        criterionList.addAll(Arrays.asList(
                Restrictions.or(
                        Restrictions.isNull("serviceIsUpdate"),
                        Restrictions.eq("serviceIsUpdate", Boolean.FALSE)
                ), Restrictions.isNotNull("numberActUpdate")));

        List<RequestView> requestViews = DaoManager.load(RequestView.class, criterionList.toArray(new Criterion[0]));
        List<Long> requestIdList = requestViews.stream().map(RequestView::getId).collect(Collectors.toList());
        if (!ValidationHelper.isNullOrEmpty(requestIdList)) {
            List<Request> anomalyRequests = DaoManager.load(Request.class, new Criterion[]{Restrictions.in("id", requestIdList)});
            anomalyRequests = anomalyRequests.stream().filter(r -> !r.getNumberActUpdate().equals(
                    Double.valueOf(r.getSumOfEstateFormalitiesAndCommunicationsAndSuccess()))).collect(Collectors.toList());
            if (!ValidationHelper.isNullOrEmpty(anomalyRequests)) {
                setAnomalyRequestsFile(new CreateExcelRequestsReportHelper().convertFilteredRequestsToExcel(anomalyRequests));
                return;
            }
        }
        executeJS("PF('nonAnomalyRequestFound').show()");
    }

    public void loadExcelWithAnomalyRequests() {
        if (!ValidationHelper.isNullOrEmpty(getAnomalyRequestsFile())) {
            String fileName = "Anomaly-requests" + ".xls";
            FileHelper.sendFile(fileName, getAnomalyRequestsFile());
            setAnomalyRequestsFile(null);
        }
    }

    private List<Long> getUserListIds(String searchCreateUser) throws PersistenceBeanException, IllegalAccessException {
        List<Long> longList = new ArrayList<>();
        List<User> users = DaoManager.load(User.class, new Criterion[]{
                Restrictions.or(
                        Restrictions.like("firstName", searchCreateUser),
                        Restrictions.like("lastName", searchCreateUser),
                        Restrictions.like("businessName", searchCreateUser)
                )
        });
        if (!ValidationHelper.isNullOrEmpty(users)) {
            for (User user : users) {
                longList.add(user.getId());
            }
        }
        return longList;
    }

    @Override
    protected void deleteEntityInternal(Long id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        Request request = DaoManager.get(Request.class, id);
        if (getCurrentUser().isExternal() && !request.getStateId().equals(RequestState.INSERTED.getId())) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    ResourcesHelper.getValidation("warning"),
                    ResourcesHelper.getValidation("cannotRemoveRequest"));
        } else {
            request.setIsDeleted(Boolean.TRUE);
            DaoManager.save(request);
        }
    }

    public void selectStateForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getStateWrappers())) {
            for (RequestStateWrapper wkrsw : this.getStateWrappers()) {
                if (wkrsw.getId()
                        .equals(this.getSelectedStateForFilter().getId())) {
                    wkrsw.setSelected(!wkrsw.getSelected().booleanValue());
                    break;
                }
            }
        }
    }

    public void selectUserForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getUserWrappers())) {
            for (UserFilterWrapper wkrsw : this.getUserWrappers()) {
                if (wkrsw.getId().equals(this.getSelectedUserForFilter().getId())) {
                    wkrsw.setSelected(!wkrsw.getSelected().booleanValue());
                    break;
                }
            }
        }
    }
    
    public void selectServiceForFilter() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(this.getServiceWrappers())) {
            for (ServiceFilterWrapper wkrsw : this.getServiceWrappers()) {
                if (wkrsw.getId().equals(this.getSelectedServiceForFilter().getId())) {
                    wkrsw.setSelected(!wkrsw.getSelected().booleanValue());
                    break;
                }
            }
        }
        selectServiceCheck();
    }
    
    public void selectServiceCheck() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        
        if (!ValidationHelper.isNullOrEmpty(this.getServiceWrappers())) {
            
            List<ServiceFilterWrapper> filterWrappers = 
                    this.getServiceWrappers().stream().
                    filter(sw -> Objects.nonNull(
                            sw.getService().getServiceReferenceType()))
                    .filter(distinctByKey(sw -> sw.getService().getServiceReferenceType()))
                    .collect(Collectors.toList());
            
            if(ValidationHelper.isNullOrEmpty(filterWrappers)) {
                setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.FALSE));
            }else {
                boolean isConservatory = false;
                boolean isComuni = false;
                for (ServiceFilterWrapper wkrsw : filterWrappers) {
                    if(wkrsw.getSelected()) {
                        if(wkrsw.getService().getServiceReferenceType() == ServiceReferenceTypes.COMMON) {
                            isComuni = true;
                        }else {
                            isConservatory = true;
                        }
                    }
                }
                if(isComuni && isConservatory) {
                    setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.FALSE));
                    getLandAggregations().addAll(getCities());
                }else if(isComuni) {
                    setLandAggregations(getCities());
                }else {
                    setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.FALSE));
                }
            }
        }else {
            setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.FALSE));
        }
    }
    
    public void selectRequestTypeForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getRequestTypeWrappers())) {
            for (RequestTypeFilterWrapper wkrsw : this.getRequestTypeWrappers()) {
                if (wkrsw.getId().equals(this.getSelectedRequestTypeForFilter().getId())) {
                    wkrsw.setSelected(!wkrsw.getSelected().booleanValue());
                    break;
                }
            }
        }
    }

    private void fillTemplates(Request rec) {
        if (rec != null) {
            try {
                this.setTemplates(GeneralFunctionsHelper.fillTemplates(
                        DocumentGenerationPlaces.REQUEST_MANAGEMENT,
                        rec.getType(), null, DaoManager.getSession()));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void generate() throws PersistenceBeanException, IllegalAccessException {
        if (getDownloadRequest() != null) {
            GeneralFunctionsHelper.showReport(getDownloadRequest(),
                    getSelectedTemplateId(), getCurrentUser(),
                    false, DaoManager.getSession());
            this.setDownloadRequest(null);
        }
    }

    public boolean getSelectedAllStatesOnPanel() {
        if (this.getStateWrappers() != null) {
            for (RequestStateWrapper wlrsw : this.getStateWrappers()) {
                if (!wlrsw.getSelected().booleanValue()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAllStatesOnPanel(boolean selectedAllStatesOnPanel) {
        if (this.getStateWrappers() != null) {
            for (RequestStateWrapper wlrsw : this.getStateWrappers()) {
                wlrsw.setSelected(selectedAllStatesOnPanel);
            }
        }
    }

    public boolean getSelectedAllUsersOnPanel() {
        if (this.getUserWrappers() != null) {
            for (UserFilterWrapper wlrsw : this.getUserWrappers()) {
                if (!wlrsw.getSelected().booleanValue()) {
                    return false;
                }
            }
        }

        return true;
    }
    
    public void setSelectedAllUsersOnPanel(boolean selectedAllStatesOnPanel) {
        if (this.getUserWrappers() != null) {
            for (UserFilterWrapper wlrsw : this.getUserWrappers()) {
                wlrsw.setSelected(selectedAllStatesOnPanel);
            }
        }
    }
    
    public boolean getSelectedAllServicesOnPanel() {
        if (this.getServiceWrappers() != null) {
            for (ServiceFilterWrapper wlrsw : this.getServiceWrappers()) {
                if (!wlrsw.getSelected().booleanValue()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAllServicesOnPanel(boolean selectedAllServicesOnPanel) {
        if (this.getServiceWrappers() != null) {
            for (ServiceFilterWrapper wlrsw : this.getServiceWrappers()) {
                wlrsw.setSelected(selectedAllServicesOnPanel);
            }
        }
    }
    
    public boolean getSelectedAllRequestTypesOnPanel() {
        if (this.getRequestTypeWrappers() != null) {
            for (RequestTypeFilterWrapper wlrsw : this.getRequestTypeWrappers()) {
                if (!wlrsw.getSelected().booleanValue()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAllRequestTypesOnPanel(boolean selectedAllRequestTypesOnPanel) {
        if (this.getRequestTypeWrappers() != null) {
            for (RequestTypeFilterWrapper wlrsw : this.getRequestTypeWrappers()) {
                wlrsw.setSelected(selectedAllRequestTypesOnPanel);
            }
        }
    }
    

    public void openRequestEditor() {
        RedirectHelper.goTo(PageTypes.REQUEST_TEXT_EDIT, getEntityEditId());
    }

    public void openRequestMail() {
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_VIEW, getEntityEditId());
    }
    
    public void openRequestSubject() {
        updateFilterValueInSession();
        RedirectHelper.goToOnlyView(PageTypes.SUBJECT, getEntityEditId());
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
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

    public Long getDownloadRequestId() {
        return downloadRequestId;
    }

    public void setDownloadRequestId(Long downloadRequestId) {
        this.downloadRequestId = downloadRequestId;
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

    public Request getDownloadRequest() {
        return downloadRequest;
    }

    public void setDownloadRequest(Request downloadRequest) {
        this.downloadRequest = downloadRequest;
    }

    public List<RequestStateWrapper> getStateWrappers() {
        return stateWrappers;
    }

    public void setStateWrappers(List<RequestStateWrapper> stateWrappers) {
        this.stateWrappers = stateWrappers;
    }

    public RequestStateWrapper getSelectedStateForFilter() {
        return selectedStateForFilter;
    }

    public void setSelectedStateForFilter(
            RequestStateWrapper selectedStateForFilter) {
        this.selectedStateForFilter = selectedStateForFilter;
    }

    public List<UserFilterWrapper> getUserWrappers() {
        return userWrappers;
    }

    public void setUserWrappers(List<UserFilterWrapper> userWrappers) {
        this.userWrappers = userWrappers;
    }

    public UserFilterWrapper getSelectedUserForFilter() {
        return selectedUserForFilter;
    }

    public void setSelectedUserForFilter(
            UserFilterWrapper selectedUserForFilter) {
        this.selectedUserForFilter = selectedUserForFilter;
    }

    public List<SelectItem> getRequestTypes() {
        return requestTypes;
    }

    public void setRequestTypes(List<SelectItem> requestTypes) {
        this.requestTypes = requestTypes;
    }

    public Long getSelectedRequestType() {
        return selectedRequestType;
    }

    public void setSelectedRequestType(Long selectedRequestType) {
        this.selectedRequestType = selectedRequestType;
    }

    public Long getSelectedServiceType() {
        return selectedServiceType;
    }

    public void setSelectedServiceType(Long selectedServiceType) {
        this.selectedServiceType = selectedServiceType;
    }

    public List<SelectItem> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List<SelectItem> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public String getSearchLastName() {
        return searchLastName;
    }

    public void setSearchLastName(String searchLastName) {
        this.searchLastName = searchLastName;
    }

    public String getSearchFiscalCode() {
        return searchFiscalCode;
    }

    public void setSearchFiscalCode(String searchFiscalCode) {
        this.searchFiscalCode = searchFiscalCode;
    }

    public List<Document> getRequestDocuments() {
        return requestDocuments;
    }

    public void setRequestDocuments(List<Document> requestDocuments) {
        this.requestDocuments = requestDocuments;
    }

    public Date getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(Date dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getSearchCreateUser() {
        return searchCreateUser;
    }

    public void setSearchCreateUser(String searchCreateUser) {
        this.searchCreateUser = searchCreateUser;
    }

    public Boolean getShowPrintButton() {
        return showPrintButton;
    }

    public void setShowPrintButton(Boolean showPrintButton) {
        this.showPrintButton = showPrintButton;
    }

    public Long getSelectedState() {
        return selectedState;
    }

    public void setSelectedState(Long selectedState) {
        this.selectedState = selectedState;
    }

    public Long getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(Long selectedUser) {
        this.selectedUser = selectedUser;
    }

    public List<SelectItem> getStatesForSelect() {
        return statesForSelect;
    }

    public void setStatesForSelect(List<SelectItem> statesForSelect) {
        this.statesForSelect = statesForSelect;
    }

    public List<SelectItem> getUsersForSelect() {
        return usersForSelect;
    }

    public void setUsersForSelect(List<SelectItem> usersForSelect) {
        this.usersForSelect = usersForSelect;
    }

    public List<RequestView> getAllRequestViewsToModify() {
        return allRequestViewsToModify;
    }

    public void setAllRequestViewsToModify(List<RequestView> allRequestViewsToModify) {
        this.allRequestViewsToModify = allRequestViewsToModify;
    }

    public List<Criterion> getFilterRestrictions() {
        return filterRestrictions;
    }

    public void setFilterRestrictions(List<Criterion> filterRestrictions) {
        this.filterRestrictions = filterRestrictions;
    }

    public String getSelectedUserType() {
        return selectedUserType;
    }

    public void setSelectedUserType(String selectedUserType) {
        this.selectedUserType = selectedUserType;
    }

    public List<SelectItem> getUserTypes() {
        return userTypes;
    }

    public void setUserTypes(List<SelectItem> userTypes) {
        this.userTypes = userTypes;
    }

    public Date getDateFromEvasion() {
        return dateFromEvasion;
    }

    public void setDateFromEvasion(Date dateFromEvasion) {
        this.dateFromEvasion = dateFromEvasion;
    }

    public Date getDateToEvasion() {
        return dateToEvasion;
    }

    public void setDateToEvasion(Date dateToEvasion) {
        this.dateToEvasion = dateToEvasion;
    }

    public List<SelectItem> getLandAggregations() {
        return landAggregations;
    }

    public void setLandAggregations(List<SelectItem> landAggregations) {
        this.landAggregations = landAggregations;
    }

    public Long getAggregationFilterId() {
        return aggregationFilterId;
    }

    public void setAggregationFilterId(Long aggregationFilterId) {
        this.aggregationFilterId = aggregationFilterId;
    }

    public byte[] getAnomalyRequestsFile() {
        return anomalyRequestsFile;
    }

    public void setAnomalyRequestsFile(byte[] anomalyRequestsFile) {
        this.anomalyRequestsFile = anomalyRequestsFile;
    }

    public Boolean getCreateTotalCostSumDocumentRecord() {
        return createTotalCostSumDocumentRecord;
    }

    public void setCreateTotalCostSumDocumentRecord(Boolean createTotalCostSumDocumentRecord) {
        this.createTotalCostSumDocumentRecord = createTotalCostSumDocumentRecord;
    }

	public List<SelectItem> getFiduciaryClients() {
		return fiduciaryClients;
	}

	public List<SelectItem> getManagerClients() {
		return managerClients;
	}

	public void setFiduciaryClients(List<SelectItem> fiduciaryClients) {
		this.fiduciaryClients = fiduciaryClients;
	}

	public void setManagerClients(List<SelectItem> managerClients) {
		this.managerClients = managerClients;
	}

	public Long getFiduciaryClientFilterId() {
		return fiduciaryClientFilterId;
	}


	public void setFiduciaryClientFilterId(Long fiduciaryClientFilterId) {
		this.fiduciaryClientFilterId = fiduciaryClientFilterId;
	}

	public Long getManagerClientFilterid() {
		return managerClientFilterid;
	}

	public void setManagerClientFilterid(Long managerClientFilterid) {
		this.managerClientFilterid = managerClientFilterid;
	}

    public List<RequestStateWrapper> getSelectedRequestStates() {
        return selectedRequestStates;
    }

    public void setSelectedRequestStates(List<RequestStateWrapper> selectedRequestStates) {
        this.selectedRequestStates = selectedRequestStates;
    }

    public List<ServiceFilterWrapper> getServiceWrappers() {
        return serviceWrappers;
    }

    public void setServiceWrappers(List<ServiceFilterWrapper> serviceWrappers) {
        this.serviceWrappers = serviceWrappers;
    }

    public ServiceFilterWrapper getSelectedServiceForFilter() {
        return selectedServiceForFilter;
    }

    public void setSelectedServiceForFilter(ServiceFilterWrapper selectedServiceForFilter) {
        this.selectedServiceForFilter = selectedServiceForFilter;
    }

    public List<SelectItem> getServicesForSelect() {
        return servicesForSelect;
    }

    public void setServicesForSelect(List<SelectItem> servicesForSelect) {
        this.servicesForSelect = servicesForSelect;
    }

    public List<RequestTypeFilterWrapper> getRequestTypeWrappers() {
        return requestTypeWrappers;
    }

    public RequestTypeFilterWrapper getSelectedRequestTypeForFilter() {
        return selectedRequestTypeForFilter;
    }

    public List<SelectItem> getRequestTypesForSelect() {
        return requestTypesForSelect;
    }

    public void setRequestTypeWrappers(List<RequestTypeFilterWrapper> requestTypeWrappers) {
        this.requestTypeWrappers = requestTypeWrappers;
    }

    public void setSelectedRequestTypeForFilter(RequestTypeFilterWrapper selectedRequestTypeForFilter) {
        this.selectedRequestTypeForFilter = selectedRequestTypeForFilter;
    }

    public void setRequestTypesForSelect(List<SelectItem> requestTypesForSelect) {
        this.requestTypesForSelect = requestTypesForSelect;
    }

    public List<SelectItem> getCities() {
        return cities;
    }

    public void setCities(List<SelectItem> cities) {
        this.cities = cities;
    }

    public Integer getExpirationDays() {
        return expirationDays;
    }

    public void setExpirationDays(Integer expirationDays) {
        this.expirationDays = expirationDays;
    }

    public Integer getRequestTypeSelected() {
        int selected = 0;
        for (RequestTypeFilterWrapper requestTypeFilterWrapper : requestTypeWrappers) {
            if(requestTypeFilterWrapper.getSelected()) {
                selected++;
            }
        }
        return selected;
    }

    public Integer getStateSelected() {
        int selected = 0;
        for (RequestStateWrapper requestStateWrapper : stateWrappers) {
            if(requestStateWrapper.getSelected()) {
                selected++;
            }
        }
        return selected;
    }

    public Integer getServiceSelected() {
        int selected = 0;
        for (ServiceFilterWrapper serviceFilterWrapper : serviceWrappers) {
            if(serviceFilterWrapper.getSelected()) {
                selected++;
            }
        }
        return selected;
    }

    public void reset() throws PersistenceBeanException, IOException, InstantiationException, IllegalAccessException {
        setSelectedClientId(null);
        setManagerClientFilterid(null);
        setFiduciaryClientFilterId(null);
        setAggregationFilterId(null);
        setStateWrappers(new ArrayList<>());
        setUserWrappers(new ArrayList<>());
        setServiceWrappers(new ArrayList<>());
        setRequestTypeWrappers(new ArrayList<>());
        setShowPrintButton(null);
        this.onLoad();
    }

    public void setSelectedStates(List<RequestState> selectedStates) {
        this.selectedStates = selectedStates;
    }

    public List<RequestState> getSelectedStates() {
        List<RequestState> selected = new ArrayList<>();
        for (RequestStateWrapper requestStateWrapper : stateWrappers) {
            if(requestStateWrapper.getSelected()) {
                selected.add(requestStateWrapper.getState());
            }
        }
        return selected;
    }

    public List<RequestType> getSelectedRequestTypes() {
        List<RequestType> selected = new ArrayList<>();
        for (RequestTypeFilterWrapper requestTypeFilterWrapper : requestTypeWrappers) {
            if(requestTypeFilterWrapper.getSelected()) {
                selected.add(requestTypeFilterWrapper.getRequestType());
            }
        }
        return selected;
    }

    public void setSelectedRequestTypes(List<RequestType> selectedRequestTypes) {
        this.selectedRequestTypes = selectedRequestTypes;
    }

    public List<Service> getSelectedServices() {
        List<Service> selected = new ArrayList<>();
        for (ServiceFilterWrapper serviceFilterWrapper : serviceWrappers) {
            if(serviceFilterWrapper.getSelected()) {
                selected.add(serviceFilterWrapper.getService());
            }
        }
        return selected;
    }

    public void setSelectedServices(List<Service> selectedServices) {
        this.selectedServices = selectedServices;
    }

    public void createNewMultipleRequests() {
        String queryParam = RedirectHelper.FROM_PARAMETER + "=RICHESTE_MULTIPLE";
        RedirectHelper.goToMultiple(PageTypes.REQUEST_EDIT,queryParam);
    }
    
    private void updateFilterValueInSession(){
    
        if(!ValidationHelper.isNullOrEmpty(getSelectedClientId()) ){
            SessionHelper.put(KEY_CLIENT_ID, getSelectedClientId());
        }
        if(!ValidationHelper.isNullOrEmpty(getStateWrappers()) ){
            SessionHelper.put(KEY_STATES, getStateWrappers());
        }
        if(!ValidationHelper.isNullOrEmpty(getRequestTypeWrappers()) ){
            SessionHelper.put(KEY_REQUEST_TYPE, getRequestTypeWrappers());
        }
        if(!ValidationHelper.isNullOrEmpty(getServiceWrappers()) ){
            SessionHelper.put(KEY_SERVICES, getServiceWrappers());
        }
        if(!ValidationHelper.isNullOrEmpty(getManagerClientFilterid()) ){
            SessionHelper.put(KEY_CLIENT_MANAGER_ID, getManagerClientFilterid());
        }
        if(!ValidationHelper.isNullOrEmpty(getFiduciaryClientFilterId()) ){
            SessionHelper.put(KEY_CLIENT_FIDUCIARY_ID, getFiduciaryClientFilterId());
        }
        if(!ValidationHelper.isNullOrEmpty(getAggregationFilterId()) ){
            SessionHelper.put(KEY_AGGREAGATION, getAggregationFilterId());
        }
        if(!ValidationHelper.isNullOrEmpty(getDateExpiration()) ){
            SessionHelper.put(KEY_DATE_EXPIRATION, getDateExpiration());
        }
        if(!ValidationHelper.isNullOrEmpty(getDateFrom()) ){
            SessionHelper.put(KEY_DATE_FROM_REQ, getDateFrom());
        }
        if(!ValidationHelper.isNullOrEmpty(getDateTo()) ){
            SessionHelper.put(KEY_DATE_TO_REQ, getDateTo());
        }
        if(!ValidationHelper.isNullOrEmpty(getDateFromEvasion()) ){
            SessionHelper.put(KEY_DATE_FROM_EVASION, getDateFromEvasion());
        }
        if(!ValidationHelper.isNullOrEmpty(getDateToEvasion()) ){
            SessionHelper.put(KEY_DATE_TO_EVASION, getDateToEvasion());
        }
        
        
    }
    
    private void loadFilterValueFromSession(){
    
         if(!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_CLIENT_ID))){
            setSelectedClientId((Long) SessionHelper.get(KEY_CLIENT_ID));
        }
        if(!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_STATES))){
             setStateWrappers((List<RequestStateWrapper>) SessionHelper.get(KEY_STATES));
        }
        if(!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_REQUEST_TYPE))){
            setRequestTypeWrappers((List<RequestTypeFilterWrapper>) SessionHelper.get(KEY_REQUEST_TYPE));
        }
        if(!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_SERVICES)) ){
            setServiceWrappers((List<ServiceFilterWrapper>) SessionHelper.get(KEY_SERVICES));
        }
        if(!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_CLIENT_MANAGER_ID)) ){
            setManagerClientFilterid((Long) SessionHelper.get(KEY_CLIENT_MANAGER_ID));
        }
        if(!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_CLIENT_FIDUCIARY_ID)) ){
            setFiduciaryClientFilterId((Long) SessionHelper.get(KEY_CLIENT_FIDUCIARY_ID));
        }
        if(!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_AGGREAGATION)) ){
            setAggregationFilterId((Long) SessionHelper.get(KEY_AGGREAGATION));
        }
        if(!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_DATE_EXPIRATION)) ){
            setDateExpiration((Date) SessionHelper.get(KEY_DATE_EXPIRATION));
        }
        if(!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_DATE_FROM_REQ)) ){
            setDateFrom((Date) SessionHelper.get(KEY_DATE_FROM_REQ));
        }
        if(!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_DATE_TO_REQ)) ){
            setDateTo((Date) SessionHelper.get(KEY_DATE_TO_REQ));
        }
        if(!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_DATE_FROM_EVASION)) ){
            setDateFromEvasion((Date) SessionHelper.get(KEY_DATE_FROM_EVASION));
        }
        if(!ValidationHelper.isNullOrEmpty(SessionHelper.get(KEY_DATE_TO_EVASION)) ){
            setDateToEvasion((Date) SessionHelper.get(KEY_DATE_TO_EVASION));
        }
        
    }
    
}