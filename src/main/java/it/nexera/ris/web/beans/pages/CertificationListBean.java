package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.create.xlsx.ImportRenewalExcelHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.persistence.view.RequestSubjectView;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.CertificationListRequestWrapper;
import it.nexera.ris.web.beans.wrappers.CertificationListTurnoverWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RequestStateWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserFilterWrapper;
import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.web.common.EntityLazyListModel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean(name = "certificationListBean")
@ViewScoped
@Getter
@Setter
public class CertificationListBean extends EntityLazyListPageBean<RequestSubjectView>
        implements Serializable {

	private static final long serialVersionUID = -2241406153851378404L;

	private static transient final Log log = LogFactory.getLog(CertificationListBean.class);


    private List<SelectItem> clients;

    private List<Client> clientList;

    private Long selectedClientId;

    private List<SelectItem> years;

    private Integer selectedYear;

    private Double monthJanFebAmount;

    private Double monthMarAprAmount;

    private Double monthMayJunAmount;

    private Double monthJulAugAmount;

    private Double monthSepOctAmount;

    private Double monthNovDecAmount;

    private List<CertificationListTurnoverWrapper> turnoverPerMonth;

    private List<CertificationListTurnoverWrapper> turnoverPerCustomer;

    public String[] months = new String[]{"Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"};

    private int quadrimesterStartIdx = 0;

    private int quadrimesterEndIdx = 3;

    private int activeTabIndex;

	private List<UserFilterWrapper> userWrappers;

	private UserFilterWrapper selectedUserForFilter;
    
    @ManagedProperty(value = "#{calendarBean}")
    private CalendarBean calendarBean;
    
    private List<CertificationListRequestWrapper> requestListTranscription;
    
    private List<CertificationListRequestWrapper> requestListCertification;
    
    private List<CertificationListRequestWrapper> requestListRenewal;
    
    private List<Document> requestDocuments;

	private Long managerClientFilterid;

	private List<SelectItem> managerClients;

	private Long fiduciaryClientFilterId;

	private List<SelectItem> fiduciaryClients;

	private Long aggregationFilterId;

	private List<SelectItem> landAggregations;

	private Date dateExpiration;

	private Date dateFrom;

	private Date dateTo;

	private Date dateFromEvasion;

	private Date dateToEvasion;

	private List<RequestStateWrapper> stateWrappers;

	private List<RequestState> selectedStates;

	private RequestStateWrapper selectedStateForFilter;

	private String searchLastName;

	private String searchFiscalCode;

	private String certificationSearchLastName;

	private String certificationSearchFiscalCode;

	private LazyDataModel<Request> certificationRequestModel;

	private UserFilterWrapper selectedCertUserForFilter;
	
	private String renewalSearchLastName;

	private String renewalSearchFiscalCode;

	private TranscriptionAndCertificationHelper transcriptionAndCertificationHelper;
	
	private List<SelectItem> renewalClients;
	
	private Long selectedRenewalClientId;
	
	private byte[] importExcelFile;
	
	private String importExcelFileName;

	private String selectedTab;

    @Override
    public void onLoad() throws HibernateException, IllegalAccessException, PersistenceBeanException  {
		if (SessionHelper.get("fromRequestEditPageTab") != null) {
			executeJS("$('#" + SessionHelper.get("fromRequestEditPageTab") + "').trigger('click');");
			SessionHelper.removeObject("fromRequestEditPageTab");
		}
        setClientList(DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted")),
				Restrictions.or(
						Restrictions.eq("brexa", Boolean.FALSE),
						Restrictions.isNull("brexa"))}));
        setClients(ComboboxHelper.fillList(getClientList().stream()
                .filter(c -> (
                                (ValidationHelper.isNullOrEmpty(c.getManager()) || !c.getManager()) &&
                                        (ValidationHelper.isNullOrEmpty(c.getFiduciary()) || !c.getFiduciary())
                        )
                ).sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList()), Boolean.TRUE));
        fillYears();

        setActiveTabIndex(0);
        setMonthJanFebAmount(0d);
        setMonthMarAprAmount(0d);
        setMonthMayJunAmount(0d);
        setMonthJulAugAmount(0d);
        setMonthSepOctAmount(0d);
        setMonthNovDecAmount(0d);
		setUserWrappers(new ArrayList<>());
		setStateWrappers(new ArrayList<>());

		List<User> notExternalCategoryUsers = DaoManager.load(User.class
				, new Criterion[]{Restrictions.or(
						Restrictions.eq("category", UserCategories.INTERNO),
						Restrictions.isNull("category")), Restrictions.eq("status", UserStatuses.ACTIVE),
						Restrictions.isNotNull("brexa"),
						Restrictions.eq("brexa", Boolean.TRUE)});

		notExternalCategoryUsers.forEach(u -> getUserWrappers().add(new UserFilterWrapper(u)));
		Arrays.asList(RequestState.values()).forEach(st -> getStateWrappers()
				.add(new RequestStateWrapper(false, st)));

		setFiduciaryClients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
				Restrictions.eq("fiduciary", Boolean.TRUE),
		}, Boolean.FALSE));

		setManagerClients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
				Restrictions.eq("manager", Boolean.TRUE),
		}, Boolean.FALSE));

		setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.TRUE));
		if (!ValidationHelper.isNullOrEmpty(getRequestParameter(RedirectHelper.CERTIFICATION_LIST))) {
			openCustomizedTab();
		}
		setTranscriptionAndCertificationHelper(new TranscriptionAndCertificationHelper());
    }

    private void fillYears() throws HibernateException, IllegalAccessException, PersistenceBeanException {
    	List<SelectItem> yearList = new ArrayList<SelectItem>();
    	Calendar calendar = Calendar.getInstance(Locale.ITALY);
		int year = calendar.get(Calendar.YEAR);
        yearList.add(new SelectItem(year));
        setYears(yearList);
    }

    public void setQuadrimesterIdx(int startIdx, int endIdx) {
        quadrimesterStartIdx = startIdx;
        quadrimesterEndIdx = endIdx;
    }

    public void loadTranscriptionTab() throws IllegalAccessException, PersistenceBeanException {
    	setRequestListTranscription(new ArrayList<>());
		List<Criterion> restrictions = new ArrayList<>();
		restrictions.add(Restrictions.eq("s.manageTranscription", Boolean.TRUE));
		restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
				Restrictions.isNull("isDeleted")));

		List<CriteriaAlias> criteriaAliases = new ArrayList<>();
		criteriaAliases.add(new CriteriaAlias("service", "s", JoinType.INNER_JOIN));
		criteriaAliases.add(new CriteriaAlias("mail", "m", JoinType.LEFT_OUTER_JOIN));

		if (!ValidationHelper.isNullOrEmpty(getSearchLastName()) || !ValidationHelper.isNullOrEmpty(getSearchFiscalCode())) {
			criteriaAliases.add(new CriteriaAlias("requestSubjects", "rs", JoinType.LEFT_OUTER_JOIN));
			criteriaAliases.add(new CriteriaAlias("rs.subject", "rss", JoinType.LEFT_OUTER_JOIN));
		}

		if (!ValidationHelper.isNullOrEmpty(getSearchLastName())) {
			String name = getSearchLastName().replaceAll("\\.", "")
					.replaceAll("\\s+", " ")
					.replaceAll("'", "").trim();
			String fullName [] = name.split(" ");
			String firstName = fullName[0];
			restrictions.add(Restrictions.or(
					Restrictions.ilike("rss.name", firstName, MatchMode.ANYWHERE),
					Restrictions.ilike("rss.surname", firstName, MatchMode.ANYWHERE)
			));
			if(fullName.length>1) {
				String lastName = fullName[1];
				restrictions.add(Restrictions.or(
						Restrictions.and(
							Restrictions.ilike("rss.name", firstName, MatchMode.ANYWHERE),
							Restrictions.ilike("rss.surname", lastName, MatchMode.ANYWHERE)
						),
						Restrictions.and(
								Restrictions.ilike("rss.name", lastName, MatchMode.ANYWHERE),
								Restrictions.ilike("rss.surname", firstName, MatchMode.ANYWHERE)
						)
				));
			}
		}

		if (!ValidationHelper.isNullOrEmpty(getSearchFiscalCode())) {
			restrictions.add(
					Restrictions.or(
							Restrictions.ilike("rss.fiscalCode", getSearchFiscalCode(), MatchMode.ANYWHERE),
							Restrictions.ilike("rss.numberVAT", getSearchFiscalCode(), MatchMode.ANYWHERE)
					)

			);
		}

		if (!ValidationHelper.isNullOrEmpty(userWrappers)) {
			List<Long> userIds = new ArrayList<>();

			userWrappers.stream().filter(UserFilterWrapper::getSelected).forEach(user -> userIds.add(user.getId()));

			if (!ValidationHelper.isNullOrEmpty(userIds)) {
				restrictions.add(Restrictions.in("user.id", userIds));
			}
		}

		if (!ValidationHelper.isNullOrEmpty(selectedClientId)) {
			restrictions.add(Restrictions.eq("client.id", selectedClientId));
		}

		if (!ValidationHelper.isNullOrEmpty(stateWrappers)) {
			List<Long> stateIds = new ArrayList<>();

			stateWrappers.stream().filter(RequestStateWrapper::getSelected).forEach(state -> stateIds.add(state.getId()));
			if (!ValidationHelper.isNullOrEmpty(stateIds)) {
				restrictions.add(Restrictions.in("stateId", stateIds));
			}
		}

		if (!ValidationHelper.isNullOrEmpty(fiduciaryClientFilterId)) {
			restrictions.add(Restrictions.eq("clientFiduciary.id", fiduciaryClientFilterId));
		}

		if (!ValidationHelper.isNullOrEmpty(aggregationFilterId)) {
			restrictions.add(Restrictions.eq("aggregationLandChargesRegistry.id", aggregationFilterId));
		}

		if (!ValidationHelper.isNullOrEmpty(dateExpiration)) {
			restrictions.add(Restrictions.ge("expirationDate", DateTimeHelper.getDayStart(getDateExpiration())));
			restrictions.add(Restrictions.le("expirationDate", DateTimeHelper.getDayEnd(getDateExpiration())));
		}

		if (!ValidationHelper.isNullOrEmpty(dateFrom)) {
			restrictions.add(Restrictions.ge("createDate", DateTimeHelper.getDayStart(dateFrom)));
		}

		if (!ValidationHelper.isNullOrEmpty(dateTo)) {
			restrictions.add(Restrictions.le("createDate", DateTimeHelper.getDayEnd(dateTo)));
		}

		if (!ValidationHelper.isNullOrEmpty(dateFromEvasion)) {
			restrictions.add(Restrictions.ge("evasionDate", DateTimeHelper.getDayStart(dateFromEvasion)));
		}

		if (!ValidationHelper.isNullOrEmpty(dateToEvasion)) {
			restrictions.add(Restrictions.le("evasionDate", DateTimeHelper.getDayEnd(dateToEvasion)));
		}

    	List<Request> transcriptionRequests = DaoManager.load(Request.class,
				criteriaAliases.toArray(new CriteriaAlias[0]), restrictions.toArray(new Criterion[0]), new Order[]{
                        Order.desc("createDate")});
    	for(Request request : transcriptionRequests) {
    		CertificationListRequestWrapper certificationListRequestWrapper = new CertificationListRequestWrapper();
    		certificationListRequestWrapper.setRequest(request);
    		if(!ValidationHelper.isNullOrEmpty(request.getClient())) {
    			certificationListRequestWrapper.setClientName(request.getClient().toString());
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getSpecialFormality()) && !ValidationHelper.isNullOrEmpty(request.getSpecialFormality().getTextInVisura())) {
    			certificationListRequestWrapper.setActType(request.getSpecialFormality().getTextInVisura());
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry()) && !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getName())) {
    			certificationListRequestWrapper.setConservatory(request.getAggregationLandChargesRegistry().getName());
    		}
    		/*if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())) { 
				if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getDicTypeFormality())) {
					certificationListRequestWrapper.setActType(request.getTranscriptionActId().getDicTypeFormality().getTextInVisura());
				}
				if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getReclamePropertyService())) {
					certificationListRequestWrapper.setConservatory(request.getTranscriptionActId().getReclamePropertyService().getName());
				}
    		}*/
    		if(!ValidationHelper.isNullOrEmpty(request.getRequestSubjects())) {
    			String subjectNames = "";
    			for(RequestSubject requestSubject : request.getRequestSubjects()) {
    				if(!ValidationHelper.isNullOrEmpty(requestSubject.getSubject().getTypeId()) && requestSubject.getSubject().getTypeId().equals(SubjectType.LEGAL_PERSON.getId())) {
    					subjectNames = subjectNames + requestSubject.getSubject().getBusinessName() + (subjectNames.isEmpty() ? "" : " - ");
    				} else {
    					String subject = requestSubject.getSubject().getSurname() + " " + requestSubject.getSubject().getName() + (subjectNames.isEmpty() ? "" : " - ");
    					subjectNames = subjectNames + subject.toUpperCase();
    				}
                }
    			certificationListRequestWrapper.setSubjectNames(subjectNames);
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getRequestMangerList())) {
    			String managerNames = "";
    			for(Client client : request.getRequestMangerList()) {
    				String manager = client.toString() + (managerNames.isEmpty() ? "" : " - ");
    				managerNames = managerNames + manager;
    			}
    			certificationListRequestWrapper.setManagers(managerNames);
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getFiduciary())) {
    			certificationListRequestWrapper.setFiduciary(request.getFiduciary());
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getNote())) {
    			certificationListRequestWrapper.setNote(request.getNote());
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getCreateDate())) {
    			certificationListRequestWrapper.setCreateDate(request.getCreateDate());
    		}
			if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
				Service certificateService = request
						.getMultipleServices()
						.stream()
						.filter(s -> s.getManageCertification() != null && s.getManageCertification())
						.findFirst().orElse(null);
				if(certificateService != null)
					certificationListRequestWrapper.setManageCertification(Boolean.TRUE);
			}else if(!ValidationHelper.isNullOrEmpty(request.getService())
					&& request.getService().getManageCertification() != null && request.getService().getManageCertification()){
				certificationListRequestWrapper.setManageCertification(Boolean.TRUE);
			}
    		getRequestListTranscription().add(certificationListRequestWrapper);
    	}
    }
    
    public void loadCertificationTab() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
    	setRequestListCertification(new ArrayList<>());
		List<Criterion> restrictions = new ArrayList<>();
		restrictions.add(Restrictions.eq("s.manageCertification", Boolean.TRUE));

		List<CriteriaAlias> criteriaAliases = new ArrayList<>();
		criteriaAliases.add(new CriteriaAlias("service", "s", JoinType.INNER_JOIN));

	//	if (!ValidationHelper.isNullOrEmpty(getCertificationSearchLastName()) || !ValidationHelper.isNullOrEmpty(getCertificationSearchFiscalCode())) {
			criteriaAliases.add(new CriteriaAlias("requestSubjects", "rs", JoinType.LEFT_OUTER_JOIN));
			criteriaAliases.add(new CriteriaAlias("rs.subject", "rss", JoinType.LEFT_OUTER_JOIN));
	//	}

		if (!ValidationHelper.isNullOrEmpty(getCertificationSearchLastName())) {
			String name = getCertificationSearchLastName().replaceAll("\\.", "")
					.replaceAll("\\s+", " ")
					.replaceAll("'", "").trim();
			String fullName [] = name.split(" ");
			String firstName = fullName[0];
			log.error(name);
			if(fullName.length>1) {
				String lastName = fullName[1];
				restrictions.add(Restrictions.or(
						Restrictions.and(
								Restrictions.ilike("rss.name", firstName, MatchMode.ANYWHERE),
								Restrictions.ilike("rss.surname", lastName, MatchMode.ANYWHERE)
						),
						Restrictions.and(
								Restrictions.ilike("rss.name", lastName, MatchMode.ANYWHERE),
								Restrictions.ilike("rss.surname", firstName, MatchMode.ANYWHERE)
						)
				));
			} else {
				restrictions.add(Restrictions.or(
						Restrictions.ilike("rss.name", firstName, MatchMode.ANYWHERE),
						Restrictions.ilike("rss.surname", firstName, MatchMode.ANYWHERE)
				));
			}
		}

		if (!ValidationHelper.isNullOrEmpty(getCertificationSearchFiscalCode())) {
			restrictions.add(
					Restrictions.or(
							Restrictions.ilike("rss.fiscalCode", getCertificationSearchFiscalCode(), MatchMode.ANYWHERE),
							Restrictions.ilike("rss.numberVAT", getCertificationSearchFiscalCode(), MatchMode.ANYWHERE)
					)

			);
		}
		if (!ValidationHelper.isNullOrEmpty(userWrappers)) {
			List<Long> userIds = new ArrayList<>();

			userWrappers.stream().filter(UserFilterWrapper::getSelected).forEach(user -> userIds.add(user.getId()));

			if (!ValidationHelper.isNullOrEmpty(userIds)) {
				restrictions.add(Restrictions.in("user.id", userIds));
			}
		}
		restrictions.add(
				Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
						Restrictions.isNull("isDeleted")));
		Arrays.stream(restrictions.toArray(new Criterion[0]))
				.forEach(r -> LogHelper.debugInfo(log, "Restriction(CertificationList) : " + r));

		setCertificationRequestModel(new EntityLazyListModel<>(Request.class, restrictions.toArray(new Criterion[0]),
				new Order[]{Order.desc("createDate")},criteriaAliases.toArray(new CriteriaAlias[0])));

  /*  	List<Request> transcriptionRequests = DaoManager.load(Request.class, criteriaAliases.toArray(new CriteriaAlias[0]), restrictions.toArray(new Criterion[0]),
				new Order[]{Order.desc("createDate")});
		log.error(transcriptionRequests.size());*/
    /*	for(Request request : transcriptionRequests) {
    		CertificationListRequestWrapper certificationListRequestWrapper = new CertificationListRequestWrapper();
    		if(!ValidationHelper.isNullOrEmpty(request.getCreateDate())){
    			certificationListRequestWrapper.setCreateDate(request.getCreateDate());
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getClient())) {
    			certificationListRequestWrapper.setClientName(request.getClient().toString());
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId())) { 
				if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getReclamePropertyService())) {
					certificationListRequestWrapper.setConservatory(request.getTranscriptionActId().getReclamePropertyService().getName());
				} else if(!ValidationHelper.isNullOrEmpty(request.getTranscriptionActId().getProvincialOffice())) {
					certificationListRequestWrapper.setConservatory(request.getTranscriptionActId().getProvincialOffice().getName());
				}
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getDistraintFormality())) {
    			
    			List<Subject> subjects = request.getDistraintFormality().getSectionC().stream()
	                .filter(x -> SectionCType.CONTRO.getName().equals(x.getSectionCType()))
	                .sorted(Comparator.comparingInt(o -> -o.getSubject().size()))
	                .map(SectionC::getSubject).flatMap(List::stream).distinct().collect(Collectors.toList());
    			String subjectNames = "";
    			for(Subject subject : subjects) {
                    String subjectName = subject.getSurname() + " " + subject.getName() + (subjectNames.isEmpty() ? "" : " - ");
                    subjectNames = subjectNames + subjectName.toUpperCase();
                }
    			certificationListRequestWrapper.setSubjectNames(subjectNames);
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getRequestMangerList())) {
    			String managerNames = "";
    			for(Client client : request.getRequestMangerList()) {
    				String manager = client.toString() + (managerNames.isEmpty() ? "" : " - ");
    				managerNames = managerNames + manager;
    			}
    			certificationListRequestWrapper.setManagers(managerNames);
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getFiduciary())) {
    			certificationListRequestWrapper.setFiduciary(request.getFiduciary());
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getNote())) {
    			certificationListRequestWrapper.setNote(request.getNote());
    		}
			certificationListRequestWrapper.setStateDescription(request.getStateDescription());
    		if(!ValidationHelper.isNullOrEmpty(request.getClientFiduciary())){
				certificationListRequestWrapper.setClientFiduciary(request.getClientFiduciary().toString());
				certificationListRequestWrapper.setClientNameProfessional(
						request.getClientNameProfessional(request.getClientFiduciary()));
			}
			if(!ValidationHelper.isNullOrEmpty(request.getMail())){
				certificationListRequestWrapper.setHaveMail(Boolean.TRUE);
			}
			certificationListRequestWrapper.setManagerId(request.getManagerId());
			certificationListRequestWrapper.setServiceName(request.getServiceName());
			if(!ValidationHelper.isNullOrEmpty(request.getRequestType()))
				certificationListRequestWrapper.setIconStyleClass(getItemIconStyleClass(request.getRequestType().getId()));
			certificationListRequestWrapper.setRequestTypeName(request.getRequestTypeName());
			if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())){
				certificationListRequestWrapper.setMultipleServices(request.getMultipleServices()
						.stream()
						.map(Service::getName)
						.collect(Collectors.toList()));
			}
			certificationListRequestWrapper.setRequest(request);
			certificationListRequestWrapper.setRequestId(request.getId());
    		getRequestListCertification().add(certificationListRequestWrapper);

    	}*/
    }
    
    public void loadRenewalTab() throws IllegalAccessException, PersistenceBeanException {
    	setRequestListRenewal(new ArrayList<>());
		List<Criterion> restrictions = new ArrayList<>();
		//restrictions.add(Restrictions.eq("s.manageTranscription", Boolean.TRUE));
		restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
				Restrictions.isNull("isDeleted")));

		restrictions.add(Restrictions.eq("s.manageRenewal", Boolean.TRUE));
		List<CriteriaAlias> criteriaAliases = new ArrayList<>();
		criteriaAliases.add(new CriteriaAlias("service", "s", JoinType.INNER_JOIN));

		if (!ValidationHelper.isNullOrEmpty(getRenewalSearchLastName()) || !ValidationHelper.isNullOrEmpty(getRenewalSearchFiscalCode())) {
			criteriaAliases.add(new CriteriaAlias("requestSubjects", "rs", JoinType.INNER_JOIN));
			criteriaAliases.add(new CriteriaAlias("rs.subject", "rss", JoinType.INNER_JOIN));
		}

		if (!ValidationHelper.isNullOrEmpty(getRenewalSearchLastName())) {
			String name = getRenewalSearchLastName().replaceAll("\\.", "")
					.replaceAll("\\s+", " ")
					.replaceAll("'", "").trim();
			String fullName [] = name.split(" ");
			String firstName = fullName[0];
			restrictions.add(Restrictions.or(
					Restrictions.ilike("rss.name", firstName, MatchMode.ANYWHERE),
					Restrictions.ilike("rss.surname", firstName, MatchMode.ANYWHERE)
			));
			if(fullName.length>1) {
				String lastName = fullName[1];
				restrictions.add(Restrictions.or(
						Restrictions.and(
							Restrictions.ilike("rss.name", firstName, MatchMode.ANYWHERE),
							Restrictions.ilike("rss.surname", lastName, MatchMode.ANYWHERE)
						),
						Restrictions.and(
								Restrictions.ilike("rss.name", lastName, MatchMode.ANYWHERE),
								Restrictions.ilike("rss.surname", firstName, MatchMode.ANYWHERE)
						)
				));
			}
		}

		if (!ValidationHelper.isNullOrEmpty(getRenewalSearchFiscalCode())) {
			restrictions.add(
					Restrictions.or(
							Restrictions.ilike("rss.fiscalCode", getRenewalSearchFiscalCode(), MatchMode.ANYWHERE),
							Restrictions.ilike("rss.numberVAT", getRenewalSearchFiscalCode(), MatchMode.ANYWHERE)
					)

			);
		}

		if (!ValidationHelper.isNullOrEmpty(userWrappers)) {
			List<Long> userIds = new ArrayList<>();

			userWrappers.stream().filter(UserFilterWrapper::getSelected).forEach(user -> userIds.add(user.getId()));

			if (!ValidationHelper.isNullOrEmpty(userIds)) {
				restrictions.add(Restrictions.in("user.id", userIds));
			}
		}

		if (!ValidationHelper.isNullOrEmpty(selectedClientId)) {
			restrictions.add(Restrictions.eq("client.id", selectedClientId));
		}

		if (!ValidationHelper.isNullOrEmpty(stateWrappers)) {
			List<Long> stateIds = new ArrayList<>();

			stateWrappers.stream().filter(RequestStateWrapper::getSelected).forEach(state -> stateIds.add(state.getId()));
			if (!ValidationHelper.isNullOrEmpty(stateIds)) {
				restrictions.add(Restrictions.in("stateId", stateIds));
			}
		}

		if (!ValidationHelper.isNullOrEmpty(fiduciaryClientFilterId)) {
			restrictions.add(Restrictions.eq("clientFiduciary.id", fiduciaryClientFilterId));
		}

		if (!ValidationHelper.isNullOrEmpty(aggregationFilterId)) {
			restrictions.add(Restrictions.eq("aggregationLandChargesRegistry.id", aggregationFilterId));
		}

		if (!ValidationHelper.isNullOrEmpty(dateExpiration)) {
			restrictions.add(Restrictions.ge("expirationDate", DateTimeHelper.getDayStart(getDateExpiration())));
			restrictions.add(Restrictions.le("expirationDate", DateTimeHelper.getDayEnd(getDateExpiration())));
		}

		if (!ValidationHelper.isNullOrEmpty(dateFrom)) {
			restrictions.add(Restrictions.ge("createDate", DateTimeHelper.getDayStart(dateFrom)));
		}

		if (!ValidationHelper.isNullOrEmpty(dateTo)) {
			restrictions.add(Restrictions.le("createDate", DateTimeHelper.getDayEnd(dateTo)));
		}

		if (!ValidationHelper.isNullOrEmpty(dateFromEvasion)) {
			restrictions.add(Restrictions.ge("evasionDate", DateTimeHelper.getDayStart(dateFromEvasion)));
		}

		if (!ValidationHelper.isNullOrEmpty(dateToEvasion)) {
			restrictions.add(Restrictions.le("evasionDate", DateTimeHelper.getDayEnd(dateToEvasion)));
		}

    	List<Request> renewalRequests = DaoManager.load(Request.class,
				criteriaAliases.toArray(new CriteriaAlias[0]), restrictions.toArray(new Criterion[0]), new Order[]{
                        Order.desc("createDate")});
    	for(Request request : renewalRequests) {
    		CertificationListRequestWrapper certificationListRequestWrapper = new CertificationListRequestWrapper();
    		certificationListRequestWrapper.setRequest(request);
    		if(!ValidationHelper.isNullOrEmpty(request.getClient())) {
    			certificationListRequestWrapper.setClientName(request.getClient().toString());
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getActType())) {
    			certificationListRequestWrapper.setActType(request.getActType());
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry()) && !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getName())) {
    			certificationListRequestWrapper.setConservatory(request.getAggregationLandChargesRegistry().getName());
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getRequestSubjects())) {
    			String subjectNames = "";
    			for(RequestSubject requestSubject : request.getRequestSubjects()) {
    				if(!ValidationHelper.isNullOrEmpty(requestSubject.getSubject().getTypeId()) && requestSubject.getSubject().getTypeId().equals(SubjectType.LEGAL_PERSON.getId())) {
    					subjectNames = subjectNames + requestSubject.getSubject().getBusinessName() + (subjectNames.isEmpty() ? "" : " - ");
    				} else {
    					String subject = requestSubject.getSubject().getSurname() + " " + requestSubject.getSubject().getName() + (subjectNames.isEmpty() ? "" : " - ");
    					subjectNames = subjectNames + subject.toUpperCase();
    				}
                }
    			certificationListRequestWrapper.setSubjectNames(subjectNames);
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getRequestMangerList())) {
    			String managerNames = "";
    			for(Client client : request.getRequestMangerList()) {
    				String manager = client.toString() + (managerNames.isEmpty() ? "" : " - ");
    				managerNames = managerNames + manager;
    			}
    			certificationListRequestWrapper.setManagers(managerNames);
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getFiduciary())) {
    			certificationListRequestWrapper.setFiduciary(request.getFiduciary());
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getNote())) {
    			certificationListRequestWrapper.setNote(request.getNote());
    		}
    		if(!ValidationHelper.isNullOrEmpty(request.getCreateDate())) {
    			certificationListRequestWrapper.setCreateDate(request.getCreateDate());
    		}
    		getRequestListRenewal().add(certificationListRequestWrapper);
    	}
    	
    	setRenewalClients(ComboboxHelper.fillList(DaoManager.load(Client.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"),
                        Restrictions.or(
                                Restrictions.eq("brexa", Boolean.FALSE),
                                Restrictions.isNull("brexa")))
        }).stream()
                .filter(c -> (
                                (ValidationHelper.isNullOrEmpty(c.getManager()) || !c.getManager()) &&
                                        (ValidationHelper.isNullOrEmpty(c.getFiduciary()) || !c.getFiduciary())
                        )
                ).sorted(Comparator.comparing(Client::toString)).collect(Collectors.toList()), Boolean.TRUE));
    }
    
    public void manageRequest() {
        RedirectHelper.goToRequestEditFromCertification(PageTypes.REQUEST_EDIT, getEntityEditId(),
				getSelectedTab());
    }
    
    public void openTranscriptionManagement() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
		//updateFilterValueInSession();
		getTranscriptionAndCertificationHelper().openTranscriptionManagement(getEntityEditId());
    }
    
    @Override
    protected void deleteEntityInternal(Long id) throws HibernateException, PersistenceBeanException,
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
    
    public void openRequestMail() {

        RedirectHelper.goToMailViewFromCertificationList(getEntityEditId());
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

	public Integer getStateSelected() {
		int selected = 0;
		for (RequestStateWrapper requestStateWrapper : stateWrappers) {
			if (requestStateWrapper.getSelected()) {
				selected++;
			}
		}
		return selected;
	}

	public List<RequestState> getSelectedStates() {
		List<RequestState> selected = new ArrayList<>();
		for (RequestStateWrapper requestStateWrapper : stateWrappers) {
			if (requestStateWrapper.getSelected()) {
				selected.add(requestStateWrapper.getState());
			}
		}
		return selected;
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

	public void prepareToModifyTranscriptionTab() throws IllegalAccessException, InstantiationException, PersistenceBeanException {
		loadTranscriptionTab();
	}

	public void prepareToModifyCertificationTab() throws IllegalAccessException, InstantiationException, PersistenceBeanException {
		loadCertificationTab();
	}
	
	public void prepareToModifyRenewalTab() throws IllegalAccessException, InstantiationException, PersistenceBeanException {
		loadRenewalTab();
	}

	public void reset() throws PersistenceBeanException, IOException, InstantiationException, IllegalAccessException {
		setSelectedClientId(null);
		setSelectedStates(null);
		stateWrappers.stream().filter(RequestStateWrapper::getSelected).forEach(state -> state.setSelected(false));
		setManagerClientFilterid(null);
		setFiduciaryClientFilterId(null);
		setAggregationFilterId(null);
		setDateExpiration(null);
		setDateFrom(null);
		setDateTo(null);
		setDateFromEvasion(null);
		setDateToEvasion(null);
	}

	private void openCustomizedTab() throws PersistenceBeanException, IllegalAccessException {
//		loadTranscriptionTab();
		//executeJS("$('.tab').removeClass('selected'); $('#content_tab2').addClass('selected'); $('.hide').hide(); $('#content_tab2').show();");
		executeJS("$('#tab2').trigger('click');");
//		RequestContext.getCurrentInstance().update("transcriptionTable");
	}
	
	public String getItemIconStyleClass(Long requestTypeId) {
        String iconStyleClass = "";
        if (!ValidationHelper.isNullOrEmpty(requestTypeId)) {
            try {
                RequestType requestTypeDTO = DaoManager.get(RequestType.class, requestTypeId);
                iconStyleClass = requestTypeDTO.getIcon();
                if (iconStyleClass.startsWith("fa")) {
                    iconStyleClass = "fa " + iconStyleClass;
                }
            } catch (HibernateException | InstantiationException | IllegalAccessException | PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        }
        return iconStyleClass;
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

	public void openRequestSubject() {
		RedirectHelper.goToOnlyView(PageTypes.SUBJECT, getEntityEditId());
	}

	public void selectCertUserForFilter() {
		if (!ValidationHelper.isNullOrEmpty(this.getUserWrappers())) {
			for (UserFilterWrapper wkrsw : this.getUserWrappers()) {
				if (wkrsw.getId().equals(this.getSelectedCertUserForFilter().getId())) {
					wkrsw.setSelected(!wkrsw.getSelected().booleanValue());
					break;
				}
			}
		}
	}
	
	public void handleImportExcelUpload(FileUploadEvent event) {
		setImportExcelFile(event.getFile().getContents());
		setImportExcelFileName(event.getFile().getFileName());
	}
	
	public void deleteImportExcel() {
		setImportExcelFile(null);
		setImportExcelFileName(null);
	}
	
	public void importRenewalExcel() throws HibernateException, IllegalAccessException, InstantiationException, PersistenceBeanException {
		ImportRenewalExcelHelper.importRenewalExcel(getImportExcelFile(), getSelectedRenewalClientId());
	}

	public void handleTabClick() {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String,String> params = context.getExternalContext().getRequestParameterMap();
		String selectedTab = params.get("selectedTab");
		setSelectedTab(selectedTab);
	}
}
