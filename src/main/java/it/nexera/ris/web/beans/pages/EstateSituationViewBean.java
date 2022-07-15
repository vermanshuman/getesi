package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.annotations.ReattachIgnore;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeActNotConfigureException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.view.FormalityView;
import it.nexera.ris.web.beans.EntityViewPageBean;
import it.nexera.ris.web.beans.wrappers.logic.UploadDocumentWrapper;
import it.nexera.ris.web.common.WrapperLazyModel;

import org.apache.commons.lang.WordUtils;
import org.hibernate.*;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.Serializable;
import java.lang.InstantiationException;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean(name = "estateSituationViewBean")
@ViewScoped
public class EstateSituationViewBean extends EntityViewPageBean<EstateSituation> implements Serializable {

	private static final long serialVersionUID = -1478494189075092730L;

	private boolean viewRelatedEstate;

	@ReattachIgnore
	private Request requestEntity;

	private List<Document> formalityDocumentList;

	private WrapperLazyModel<Property, Property> lazyPropertyListBuilding;

	private WrapperLazyModel<Property, Property> lazyPropertyListLand;

	private WrapperLazyModel<EstateFormality, EstateFormality> lazyFormalityList;

	private List<FormalityView> formalityPDFList;

	private List<Document> uploadedFiles;

	private Long downloadFileId;

	private Long selectedSubjectId;

	private List<Long> uploadedPdfFiles;

	private Subject subjectEntity;

	private Long entityEditId;

	private List<Long> presumableSubjectsIds;

	private List<Document> formalityDocList;

	private String fileOnServer;

	private Subject subjectFromFile;

	private List<Subject> presumableSubjects;

	private Boolean property;

	private String conservatoryFromFile;

	private boolean serverDoc;

	private DocumentSubject currentDocumentSubject;

	private String errorActCode;

	private String errorNoteType;

	private String errorDescription;

	private String errorCityNoPresent;

	private List<Request> previousRequestList;

	private String searchFormalityRG;

	private String searchFormalityRP;

	private Date searchFormalityDate;

	private Long searchFormalityAggregationId;

	private List<SelectItem> landAggregations;

	private List<Formality> searchedFormalityList;

	private List<Formality> selectedSearchedFormality;

	private Map<EstateFormality, List<Formality>> presumableFormalityListByEstateFormality;

	private List<EstateFormality> estateFormalitiesForPresumableFormalityCheck;

	private String previousRequestListMessage;

	private Long requestId;

	private String entityEditStato;

	private String selectedPropertyQuality;

	private String selectedLandCultureId;

	private List<SelectItem> landCultures;

	private CostManipulationHelper costManipulationHelper;

	private Boolean showAddNationalCostButton;

	@Override
	public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {
		String id = getRequestParameter(RedirectHelper.ID_PARAMETER);
		String requestId = getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
		setViewRelatedEstate(Boolean.valueOf(getRequestParameter(RedirectHelper.ONLY_VIEW)));
		if (!ValidationHelper.isNullOrEmpty(requestId) && !requestId.equals("null")) {
			setRequestEntity(DaoManager.get(Request.class, Long.parseLong(requestId)));
			setRequestId(Long.parseLong(requestId));
			if(!ValidationHelper.isNullOrEmpty(getRequestEntity()))
				Hibernate.initialize(getRequestEntity().getSituationEstateLocations());
		}
		if (isViewRelatedEstate()) {
			setSubjectEntity(DaoManager.get(Subject.class, id));
			fillSubjectFormality();
			fillSubjectProperty();
		} else {
			setEntity(new EstateSituation());
			fillEstateWrapper();
			fillFormalityWrapper();
			fillFileTable();
			fillFormalityDocuments();
			fillFormalityByRequest();
		}
		setUploadedPdfFiles(new LinkedList<>());
		setLandAggregations(ComboboxHelper.fillList(LandChargesRegistry.class, Order.asc("name")));
		checkPreviousRequest();
		if (!ValidationHelper.isNullOrEmpty(getRequestEntity())
				&& (ValidationHelper.isNullOrEmpty(getRequestEntity().getIncludeNationalCost())
				|| (!ValidationHelper.isNullOrEmpty(getRequestEntity().getIncludeNationalCost())
				&& !getRequestEntity().getIncludeNationalCost())))
			setShowAddNationalCostButton(true);
	}

	private void checkPreviousRequest() throws PersistenceBeanException, IllegalAccessException {
		if (!ValidationHelper.isNullOrEmpty(getRequestEntity()) && getRequestEntity().getSubject() == null) {
			return;
		}

		if (!ValidationHelper.isNullOrEmpty(getRequestEntity())
				&& ValidationHelper.isNullOrEmpty(getRequestEntity().getPropertyList())
				&& ValidationHelper.isNullOrEmpty(getRequestEntity().getEstateFormalityList())) {

			List<Long> subjectIds = EstateSituationHelper.getIdSubjects(getRequestEntity());
			List<Long> ids = EstateSituationHelper.getIdSubjects(getRequestEntity());

			if (!ids.contains(getRequestEntity().getSubject().getId())) {
				ids.add(getRequestEntity().getSubject().getId());
			}

			List<Request> requests = DaoManager.load(Request.class, new Criterion[]{
					Restrictions.in("subject.id", ids),
					(ValidationHelper.isNullOrEmpty(getRequestEntity().getAggregationLandChargesRegistry()) ?
							Restrictions.isNotNull("aggregationLandChargesRegistry") :
								Restrictions.eq("aggregationLandChargesRegistry.id",
										getRequestEntity().getAggregationLandChargesRegistry().getId())),
					Restrictions.eq("stateId", RequestState.EVADED.getId()),
					Restrictions.ne("id", getRequestEntity().getId())});

			if (!ValidationHelper.isNullOrEmpty(requests)) {
				setPreviousRequestList(requests);
				Request lastRequest = requests.get(requests.size()-1);
				if(!subjectIds.contains(lastRequest.getSubject().getId())) {
					setPreviousRequestListMessage(ResourcesHelper.getString("estateSituationPreviousRequest"));
				}else {
					Subject presumableSubject = lastRequest.getSubject();
					StringBuilder sb = new StringBuilder();
					sb.append(" (");
					if(presumableSubject.getTypeIsPhysicalPerson()) {
						sb.append(String.format("%s %s %s", presumableSubject.getSurname(), 
								presumableSubject.getName(),
								presumableSubject.getBirthDate() != null ?
										DateTimeHelper.toStringDateWithDots(
												presumableSubject.getBirthDate()) : ""));
					}else {
						sb.append(presumableSubject. getBusinessName());
					}
					sb.append(String.format(" %s %s",
							presumableSubject.getBirthCity() == null ? 
									presumableSubject.getCountry().getDescription()
									: presumableSubject.getBirthCityDescription(),
									presumableSubject.getFiscalCodeVATNamber()));

					sb.append(")");
					setPreviousRequestListMessage(
							String.format(
									ResourcesHelper.getString(
											"estateSituationPreviousRequestPresumableSubject"),sb.toString()));
				}
				executeJS("PF('previousRequest').show();");
			}
		}
	}

	public void associatePreviousData() throws PersistenceBeanException, IllegalAccessException, InstantiationException, CloneNotSupportedException {

		Comparator<Request> comparator = Comparator.comparing(Request::getEvasionDate);

		Request minEvasionDateRequest = getPreviousRequestList().stream()
				.filter(x -> x.getEvasionDate() != null).max(comparator).get();

		log.info("ID of the last request evaded - " + minEvasionDateRequest.getId());

		if (!ValidationHelper.isNullOrEmpty(minEvasionDateRequest.getPropertyList())) {
			List<Property> propertyList = minEvasionDateRequest.getPropertyList();
			propertyList.stream()
			.filter(p -> !p.getRequestList().contains(getRequestEntity()))
			.forEach(p -> p.getRequestList().add(getRequestEntity()));
			for (Property property : propertyList) {
				DaoManager.save(property);
			}
		}

		if (!ValidationHelper.isNullOrEmpty(minEvasionDateRequest.getEstateFormalityList())) {
			if (ValidationHelper.isNullOrEmpty(getRequestEntity().getEstateFormalityList())) {
				getRequestEntity().setEstateFormalityList(new ArrayList<>());
			}
			getRequestEntity().getEstateFormalityList().addAll(minEvasionDateRequest.getEstateFormalityList());
		}

		if (!ValidationHelper.isNullOrEmpty(minEvasionDateRequest.getSituationEstateLocations())) {
			saveNewEstateSituationEntities(minEvasionDateRequest);

		}

		if (!ValidationHelper.isNullOrEmpty(minEvasionDateRequest.getDocumentsRequest())) {
			getRequestEntity().setDocumentsRequest(new ArrayList<>());
			for (Document document : minEvasionDateRequest.getDocumentsRequest().stream()
					.filter(x -> !DocumentType.REQUEST_REPORT.getId().equals(x.getTypeId())
							&& !DocumentType.ALLEGATI.getId().equals(x.getTypeId())
							&& !DocumentType.OTHER.getId().equals(x.getTypeId())).collect(Collectors.toList())) {
				Document newDocument = document.clone();
				newDocument.setRequest(getRequestEntity());
				DaoManager.save(newDocument, true);
			}
		}

		if (!ValidationHelper.isNullOrEmpty(minEvasionDateRequest.getComments())) {
			saveNewComments(minEvasionDateRequest);
		}

		if(!ValidationHelper.isNullOrEmpty(getRequestEntity().getSubject()) && 
				!ValidationHelper.isNullOrEmpty(minEvasionDateRequest.getSubject()) &&
				!getRequestEntity().getSubject().equals(minEvasionDateRequest.getSubject())) {

			saveNewRelationships(minEvasionDateRequest);
		}

		//        if (!ValidationHelper.isNullOrEmpty(minEvasionDateRequest.getRequestPrint())) {
		//            RequestPrint newRequestPrint = minEvasionDateRequest.getRequestPrint().clone();
		//            newRequestPrint.setRequest(getRequestEntity());
		//            DaoManager.save(newRequestPrint, true);
		//            getRequestEntity().setRequestPrint(newRequestPrint);
		//        }

		if (!Hibernate.isInitialized(getRequestEntity().getRequestSubjects())) {
			getRequestEntity().reloadRequestSubjects();
		}

		DaoManager.save(getRequestEntity(), true);
		setRequestEntity(DaoManager.get(Request.class, getRequestId()));
		Hibernate.initialize(getRequestEntity().getSituationEstateLocations());
		Hibernate.initialize(getRequestEntity().getPropertyList());
		Hibernate.initialize(getRequestEntity().getEstateFormalityList());

		onLoad();
	}

	private void saveNewComments(Request minEvasionDateRequest) throws PersistenceBeanException {
		for (Comment comment : minEvasionDateRequest.getComments()) {
			Comment newComment = comment;
			newComment.setRequest(getRequestEntity());
			DaoManager.save(newComment, true);
		}
	}

	private void saveNewRelationships(Request minEvasionDateRequest) throws PersistenceBeanException {
		minEvasionDateRequest.getPropertyList()
		.stream()
		.forEach(p -> {
			try {
				List<Relationship> relationships = DaoManager.load(Relationship.class, new Criterion[]{
						Restrictions.and(Restrictions.eq("property", p),
								Restrictions.eq("subject", getRequestEntity().getSubject()))
				});
				if(ValidationHelper.isNullOrEmpty(relationships)) {
					List<Relationship> oldRelationships = DaoManager.load(Relationship.class, new Criterion[]{
							Restrictions.and(Restrictions.eq("property", p),
									Restrictions.eq("subject", minEvasionDateRequest.getSubject()))
					});

					for (Relationship relationship : oldRelationships) {
						Relationship newRelationship = new Relationship(relationship);;
						newRelationship.setSubject(getRequestEntity().getSubject());
						DaoManager.save(newRelationship, true);
					}
				}
			} catch (Exception e) {
				LogHelper.log(log, e);
			} 
		});
	}

	private void saveNewEstateSituationEntities(Request minEvasionDateRequest) throws PersistenceBeanException {
		DaoManager.refresh(minEvasionDateRequest);

		for (EstateSituation situation : minEvasionDateRequest.getSituationEstateLocations()) {
			EstateSituation newEstateSituation = new EstateSituation();
			newEstateSituation.setCommentInit(situation.getCommentInit());
			newEstateSituation.setComment(situation.getComment());
			newEstateSituation.setRequest(getRequestEntity());
			newEstateSituation.setPropertyList(new ArrayList<>(situation.getPropertyList()));
			newEstateSituation.setEstateFormalityList(new ArrayList<>(situation.getEstateFormalityList()));
			newEstateSituation.setFormalityList(new ArrayList<>(situation.getFormalityList()));
			DaoManager.save(newEstateSituation, true);
		}
	}

	@Override
	public void goBack() {
		if (isViewRelatedEstate()) {
			RedirectHelper.goToOnlyView(PageTypes.REQUEST_ESTATE_SITUATION_EDIT,
					getRequestEntity().getId(), null, true);
		} else {
			RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_LIST, getRequestEntity().getId());
		}
	}

	private void fillFormalityWrapper() throws IllegalAccessException, PersistenceBeanException {
		if (getEntity().isNew() && !ValidationHelper.isNullOrEmpty(getRequestEntity())) {
			setLazyFormalityList(EstateSituationHelper.loadLazyEstateFormality(getRequestEntity()));
		} else if (!ValidationHelper.isNullOrEmpty(getEntity())) {
			getEntity().getEstateFormalityList().forEach(estateFormality -> estateFormality.setVisible(true));
			setLazyFormalityList(EstateSituationHelper.loadLazyEstateFormality(getEntity()));
		}
	}

	private void fillEstateWrapper() throws IllegalAccessException, PersistenceBeanException, InstantiationException {

		if (!ValidationHelper.isNullOrEmpty(getRequestEntity()) 
				&&  !Hibernate.isInitialized(getRequestEntity().getDocumentsRequest())) {
			setRequestEntity(DaoManager.get(Request.class, getRequestEntity().getId()));
		}

		if (getEntity().isNew()) {
			setLazyPropertyListBuilding(EstateSituationHelper.loadLazyProperty(getRequestEntity(), RealEstateType.BUILDING.getId()));
			setLazyPropertyListLand(EstateSituationHelper.loadLazyProperty(getRequestEntity(), RealEstateType.LAND.getId()));
		} else {
			getEntity().getPropertyList().forEach(estate -> estate.setVisible(true));
			setLazyPropertyListBuilding(EstateSituationHelper.loadLazyProperty(getEntity(), RealEstateType.BUILDING.getId()));
			setLazyPropertyListLand(EstateSituationHelper.loadLazyProperty(getEntity(), RealEstateType.LAND.getId()));
		}
	}

	private void fillFormalityDocuments() throws PersistenceBeanException, IllegalAccessException {
		if (!ValidationHelper.isNullOrEmpty(getRequestEntity())) {
			List<Document> list = DaoManager.load(Document.class, new Criterion[]{
					Restrictions.eq("request.id", getRequestEntity().getId()),
					Restrictions.eq("typeId", DocumentType.ESTATE_FORMALITY.getId())
			});
			//list = list.stream().filter(distinctByKey(x -> x.getTitle())).collect(Collectors.toList());
			setFormalityDocumentList(list);
		}
	}

	private void fillSubjectFormality() throws PersistenceBeanException, IllegalAccessException {
		setLazyFormalityList(EstateSituationHelper.loadLazyEstateFormalityBySubject(getSubjectEntity().getId()));
	}

	private void fillSubjectProperty() throws PersistenceBeanException, IllegalAccessException {
		setLazyPropertyListBuilding(EstateSituationHelper.loadLazyPropertyBySubject(getRequestEntity(), getSubjectEntity().getId(),
				RealEstateType.BUILDING.getId()));
		setLazyPropertyListLand(EstateSituationHelper.loadLazyPropertyBySubject(getRequestEntity(), getSubjectEntity().getId(),
				RealEstateType.LAND.getId()));
	}

	public void saveDocumentXML(FileUploadEvent event)
			throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		Subject differentSubject = GeneralFunctionsHelper.checkSubjectEstateFormalityUpload(event.getFile().getFileName(),
				event.getFile().getContents(), getRequestEntity(), DaoManager.getSession());
		String conservatoryName = GeneralFunctionsHelper
				.checkConservatoryEstateFormalityUpload(event.getFile().getFileName(), event.getFile().getContents());
		setConservatoryFromFile(conservatoryName);
		if (!ValidationHelper.isNullOrEmpty(conservatoryName) && getRequestEntity().getAggregationLandChargesRegistry()
				.getLandChargesRegistries().stream().anyMatch(l -> l.getName().equals(conservatoryName.trim()))) {
			setFileOnServer(GeneralFunctionsHelper.saveUploadedFile(event.getFile().getFileName(), null,
					event.getFile().getContents()));
			setSubjectFromFile(differentSubject);
			executeJS("PF('subjectEstateFormalityUploadDialog').show();");
		} else {
			setSubjectFromFile(null);
			executeJS("PF('conservatoryEstateFormalityUploadDialog').show();");
		}
	}

	public void saveNewSubjectsEstateFormalities() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		if (isServerDoc()) {
			attachServiceDocumentSubject();
			setCurrentDocumentSubject(null);
			setServerDoc(false);
		} else {
			UploadDocumentWrapper wrapper = GeneralFunctionsHelper.handleFileUpload(getFileOnServer(),
					DocumentType.ESTATE_FORMALITY.getId(), getFileOnServer(),
					new Date(), null, getRequestEntity(), true, DaoManager.getSession());
			try {
				List<EstateFormality> estateFormalities = ImportXMLHelper.handleXMLTagsEstateFormality(new File(getFileOnServer()),
						DaoManager.get(Request.class, getRequestEntity().getId()), wrapper.getDocument(), DaoManager.getSession());
				setEstateFormalitiesForPresumableFormalityCheck(estateFormalities);
				fillFormalityWrapper();
				fillFormalityDocuments();
			} catch (TypeActNotConfigureException e) {
				setErrorActCode(e.getActCode());
				setErrorNoteType(e.getNoteType());
				setErrorDescription(e.getDescription());

				RequestContext.getCurrentInstance().update("estateFormalityErrorDialogId");

				executeJS("PF('estateFormalityErrorDialog').show();");
			} finally {
				setFileOnServer(null);
				setSubjectFromFile(null);
			}
			// Commenting this for scheda EstateSituationView - import formality.TBD later
			checkPresumableFormalitiesForEstateFormalities();
		}
	}

	@SuppressWarnings("unused")
	private void checkPresumableFormalitiesForEstateFormalities() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		if (!ValidationHelper.isNullOrEmpty(getEstateFormalitiesForPresumableFormalityCheck())) {
			setPresumableFormalityListByEstateFormality(new HashMap<>());
			for (EstateFormality estateFormality : getEstateFormalitiesForPresumableFormalityCheck()) {
				List<Formality> presumableFormalityListByEstateFormality =
						EstateSituationHelper.getPresumableFormalityListByEstateFormality(getRequestEntity(), estateFormality);
				if (!ValidationHelper.isNullOrEmpty(presumableFormalityListByEstateFormality)) {
					List<Formality> filteredFormalities = presumableFormalityListByEstateFormality.stream()
							.filter(i -> "O".equals(i.getStateStr()))
							.collect(Collectors.toList());
					getPresumableFormalityListByEstateFormality().put(estateFormality, filteredFormalities);
				}
			}
			setEstateFormalitiesForPresumableFormalityCheck(null);
		}
		if (!ValidationHelper.isNullOrEmpty(getPresumableFormalityListByEstateFormality())) {
			RequestContext.getCurrentInstance().update("estateFormalityPresumableFormalityAssociationDialogId");
			executeJS("PF('estateFormalityPresumableFormalityAssociationDialog').show();");
		}
	}

	public void associatePresumableFormalityWithEstateFormalityRequestSubjectBySectionC()
			throws PersistenceBeanException, IllegalAccessException {
		if (!ValidationHelper.isNullOrEmpty(getPresumableFormalityListByEstateFormality())) {
			EstateSituationHelper
			.associatePresumableFormalityWithRequestSubjectBySectionC(
					getPresumableFormalityListByEstateFormality(), getRequestEntity());
			fillFormalityByRequest();
		}
		setPresumableFormalityListByEstateFormality(null);
	}

	public void associateFormalitiesToRequest() throws PersistenceBeanException, IllegalAccessException {
		if (!ValidationHelper.isNullOrEmpty(getSelectedSearchedFormality())) {
			List<Long> idsFormality = getSelectedSearchedFormality().stream().map(IndexedEntity::getId)
					.collect(Collectors.toList());
			List<Formality> formalities = DaoManager.load(Formality.class, new Criterion[]{
					Restrictions.in("id", idsFormality)});
			for (Formality formality : formalities) {
				if (formality.getRequestForcedList() == null) {
					formality.setRequestForcedList(new ArrayList<Request>());
				}
				if (!formality.getRequestForcedList().contains(getRequestEntity())) {
					formality.getRequestForcedList().add(getRequestEntity());
					DaoManager.save(formality, true);
				}
			}
		}
		cleanAssociateDialogParametersAndUpdateFormalityPDFTab();
	}

	private void cleanAssociateDialogParametersAndUpdateFormalityPDFTab() throws PersistenceBeanException, IllegalAccessException {
		setSearchFormalityRG(null);
		setSearchFormalityDate(null);
		setSearchFormalityRP(null);
		setSearchFormalityAggregationId(null);
		if (getSelectedSearchedFormality() != null) {
			getSelectedSearchedFormality().clear();
		}
		if (getSearchedFormalityList() != null) {
			getSearchedFormalityList().clear();
		}

		fillFormalityByRequest();
	}

	public void notSaveNewSubjectsEstateFormalities() {
		FileHelper.delete(getFileOnServer());
		setFileOnServer(null);
		setSubjectFromFile(null);
		setCurrentDocumentSubject(null);
		setServerDoc(false);
	}

	public void savePropertyXML(FileUploadEvent event)
			throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		Subject differentSubject = GeneralFunctionsHelper.checkSubjectPropertyUpload(event.getFile().getFileName(),
				event.getFile().getContents(), getRequestEntity(), DaoManager.getSession());


		getViewState().put("estateSituationViewBean", this);
		if (differentSubject == null) {
			GeneralFunctionsHelper.handleFileUpload(event.getFile().getFileName(),
					event.getFile().getContents(), DocumentType.CADASTRAL.getId(),
					event.getFile().getFileName(), new Date(), null, getRequestEntity(), DaoManager.getSession());
			clearSession();
			fillEstateWrapper();
			fillFileTable();
			executeJS("PF('progressPanel').close();");
		} else {
			setFileOnServer(GeneralFunctionsHelper.saveUploadedFile(event.getFile().getFileName(), null,
					event.getFile().getContents()));
			setSubjectFromFile(differentSubject);
			executeJS("PF('subjectUploadDialog').show();");
		}
	}

	public void openCityErrorDialog() {
		RequestContext.getCurrentInstance().update("propertyErrorDialogId");
		executeJS("PF('propertyErrorDialogWV').show();");
	}

	public void saveNewSubjectsProperties() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		GeneralFunctionsHelper.handleFileUpload(getFileOnServer(),
				DocumentType.CADASTRAL.getId(), getFileOnServer(),
				new Date(), null, getRequestEntity(), true, DaoManager.getSession());
		clearSession();
		fillEstateWrapper();
		fillFileTable();
		setFileOnServer(null);
		setSubjectFromFile(null);
		executeJS("PF('progressPanel').close();");
	}

	public void notSaveNewSubjectsProperties() {
		executeJS("PF('progressPanel').close();");
		FileHelper.delete(getFileOnServer());
		setFileOnServer(null);
		setSubjectFromFile(null);
	}

	public void saveDocumentPDF(FileUploadEvent event)
			throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		UploadDocumentWrapper wrapper = GeneralFunctionsHelper.handleFileUpload(event.getFile().getFileName(),
				event.getFile().getContents(), DocumentType.FORMALITY.getId(),
				event.getFile().getFileName(), new Date(), null, getRequestEntity(), DaoManager.getSession());
		getUploadedPdfFiles().add(wrapper.getDocument().getId());
		addFormalityToRequestForcedListByDocumentIfItIsNotLinkedWithRequest(wrapper.getDocument().getId());
	}

	private void addFormalityToRequestForcedListByDocumentIfItIsNotLinkedWithRequest(Long documentId)
			throws PersistenceBeanException, IllegalAccessException {
		if (ValidationHelper.isNullOrEmpty(getRequestEntity().getSubject())) {
			List<Formality> formalities = DaoManager.load(Formality.class, new Criterion[]{
					Restrictions.eq("document.id", documentId)
			});
			if (!ValidationHelper.isNullOrEmpty(formalities)) {
				FormalityHelper.addRequestFormalityForcedRecordIfFormalitiesAreNotLinkedWithRequest(
						getRequestEntity(), formalities, true);
			}
		}
	}

	public void updatePDFTable() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		if (!ValidationHelper.isNullOrEmpty(getUploadedPdfFiles())) {

			List<Formality> formalities = DaoManager.load(Formality.class, new Criterion[]{
					Restrictions.in("document.id", getUploadedPdfFiles().toArray(new Long[0])),
					Restrictions.or(
							Restrictions.ne("subject.id", getRequestEntity().getSubject() != null ?
									getRequestEntity().getSubject().getId() : 0L),
							Restrictions.isNull("subject.id"))
			});
			if (!ValidationHelper.isNullOrEmpty(formalities)) {
				boolean needDialog = false;
				getRequestEntity().setAggregationLandChargesRegistry(DaoManager.get(AggregationLandChargesRegistry.class,
						getRequestEntity().getAggregationLandChargesRegistry().getId()));
				for (Formality formality : formalities) {
					if (getRequestEntity().getAggregationLandChargesRegistry().getLandChargesRegistries()
							.stream().map(LandChargesRegistry::getId)
							.noneMatch(id -> !ValidationHelper.isNullOrEmpty(formality.getReclamePropertyService())
									&& id.equals(formality.getReclamePropertyService().getId()))) {
						needDialog = true;
					} else if (!ValidationHelper.isNullOrEmpty(formality.getReclamePropertyService())) {
						getUploadedPdfFiles().retainAll(getUploadedPdfFiles().stream()
								.filter(d -> d.equals(formality.getDocument().getId())).collect(Collectors.toList()));
					}
					if (ValidationHelper.isNullOrEmpty(formality.getRequestForcedList())) {
						formality.setRequestForcedList(new ArrayList<>());
					}
					formality.getRequestForcedList().add(getRequestEntity());
					DaoManager.save(formality, true);
				}
				if (!needDialog && formalities.stream().anyMatch(f -> ValidationHelper.isNullOrEmpty(f.getReclamePropertyService()))) {
					boolean b = false;
					for (Formality formality : formalities) {
						if (ValidationHelper.isNullOrEmpty(formality.getReclamePropertyService())) {
							if (getRequestEntity().getAggregationLandChargesRegistry().getLandChargesRegistries()
									.stream().map(LandChargesRegistry::getId)
									.noneMatch(id -> !ValidationHelper.isNullOrEmpty(formality.getProvincialOffice())
											&& id.equals(formality.getProvincialOffice().getId()))) {
								b = true;
							} else if (!ValidationHelper.isNullOrEmpty(formality.getProvincialOffice())) {

								if (ValidationHelper.isNullOrEmpty(formality.getRequestForcedList())) {
									formality.setRequestForcedList(new ArrayList<>());
								}
								formality.getRequestForcedList().add(getRequestEntity());
								DaoManager.save(formality, true);
								getUploadedPdfFiles().retainAll(getUploadedPdfFiles().stream()
										.filter(d -> d.equals(formality.getDocument().getId())).collect(Collectors.toList()));
							}
						}
					}
					needDialog = b;
				}
				if (needDialog) {
					executeJS("PF('formalityDialog').show();");
				} else {
					saveNewFormalities();
				}
				executeJS("PF('pdfPoll').stop();");
			}
		}
	}

	public void saveNewFormalities() throws PersistenceBeanException, IllegalAccessException {
		List<Formality> formalities = DaoManager.load(Formality.class, new Criterion[]{
				Restrictions.in("document.id", getUploadedPdfFiles().toArray(new Long[0])),
				Restrictions.or(
						Restrictions.ne("subject.id",
								!ValidationHelper.isNullOrEmpty(getRequestEntity().getSubject()) ?
										getRequestEntity().getSubject().getId() : 0L),
						Restrictions.isNull("subject.id"))
		});
		if (!ValidationHelper.isNullOrEmpty(formalities)) {
			for (Formality formality : formalities) {
				LogHelper.log(log, (formality.getId() + " " + getRequestEntity().getId()));

				if (ValidationHelper.isNullOrEmpty(formality.getRequestForcedList())) {
					formality.setRequestForcedList(new ArrayList<>());
				}
				if (!formality.getRequestForcedList().contains(getRequestEntity())) {
					formality.getRequestForcedList().add(getRequestEntity());
				}
				DaoManager.save(formality, true);
			}

			fillFormalityByRequest();
			executeJS("updatePdfTable();");
			setUploadedPdfFiles(new LinkedList<>());
		}
	}

	public void notSaveNewFormalities() {
		setUploadedPdfFiles(new LinkedList<>());
	}

	private void fillFormalityByRequest() throws PersistenceBeanException, IllegalAccessException {
		if (!ValidationHelper.isNullOrEmpty(getRequestEntity()) &&
				!ValidationHelper.isNullOrEmpty(getRequestEntity().getDistraintFormality())) {
			setFormalityPDFList(EstateSituationHelper.loadFormalityViewByDistraint(getRequestEntity()));
		} else {
			setFormalityPDFList(EstateSituationHelper.loadFormalityView(getRequestEntity()));
		}
	}

	public void fillFileTable() throws PersistenceBeanException, IllegalAccessException {
		if (!ValidationHelper.isNullOrEmpty(getRequestEntity())) {
			setUploadedFiles(DaoManager.load(Document.class, new Criterion[]{
					Restrictions.eq("request.id", getRequestEntity().getId()),
					Restrictions.eq("typeId", DocumentType.CADASTRAL.getId())
			}));
		}
	}

	public void showPDF() {
		if (!ValidationHelper.isNullOrEmpty(getDownloadFileId())) {
			String projectUrl = this.getRequest().getHeader("referer");
			projectUrl = projectUrl.substring(0,
					projectUrl.indexOf(this.getCurrentPage().getPagesContext()))
					+ "/";

			PrintPDFHelper.generatePDFOnDocument(getDownloadFileId(), projectUrl);
		}
	}

	public void showXML() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
		if (!ValidationHelper.isNullOrEmpty(getDownloadFileId())) {
			Document document = DaoManager.get(Document.class, getDownloadFileId());
			if (document.getPath() != null) {
				String fileName = document.getPath();
				String extension = "";
				int i = fileName.lastIndexOf('.');
				if (i > 0) {
					extension = fileName.substring(i);
				}
				byte[] data = FileHelper.loadContentByPath(fileName);
				FileHelper.sendFile(document.getTitle() + extension, data);
			}
		}
	}

	public void removeFile() {
		Transaction t = null;
		try {
			t = DaoManager.getSession().beginTransaction();
			List<DocumentSubject> documentSubjectList = DaoManager.load(DocumentSubject.class, new Criterion[]{
					Restrictions.eq("document.id", getDownloadFileId())
			});
			if (documentSubjectList != null) {
				for (DocumentSubject documentSubject : documentSubjectList) {
					DaoManager.remove(documentSubject);
				}
			}

			List<Relationship> relationships = DaoManager.load(Relationship.class, new Criterion[]{
					Restrictions.eq("document.id", getDownloadFileId())
			});
			for (Relationship r : relationships) {
				r.setDocument(null);
				r.setTableId(null);
				DaoManager.save(r);
			}

			List<DocumentProperty> documentProperties = DaoManager.load(DocumentProperty.class, new Criterion[]{
					Restrictions.eq("document.id", getDownloadFileId())
			});
			if (Boolean.TRUE.equals(getProperty())) {
				List<Long> propertyIds;
				if (!ValidationHelper.isNullOrEmpty(documentProperties)) {
					propertyIds = documentProperties.stream().map(DocumentProperty::getProperty)
							.filter(Objects::nonNull).map(Property::getId).collect(Collectors.toList());
				} else {
					propertyIds = Collections.singletonList(0L);
				}
				for (Property property : DaoManager.load(Property.class, new Criterion[]{
						Restrictions.in("id", propertyIds)
				})) {
					property.getRequestList().removeAll(property.getRequestList().stream()
							.filter(request -> request.getId().equals(getRequestEntity().getId()))
							.collect(Collectors.toList()));
					DaoManager.save(property);
				}
			}
			for (DocumentProperty p : documentProperties) {
				DaoManager.remove(p);
			}
			if (Boolean.FALSE.equals(getProperty())) {
				for (EstateFormality formality : DaoManager.load(EstateFormality.class, new Criterion[]{
						Restrictions.eq("document.id", getDownloadFileId())
				})) {
					formality.getRequestList().removeIf(request -> request.getId().equals(getRequestEntity().getId()));
					formality.setDocument(null);
					DaoManager.save(formality);
				}
				for (EstateFormality formality : DaoManager.load(EstateFormality.class, new CriteriaAlias[]{
						new CriteriaAlias("requestFormalities", "rf", JoinType.INNER_JOIN)
				}, new Criterion[]{
						Restrictions.eq("rf.documentId", getDownloadFileId())
				})) {
					formality.getRequestList().removeIf(request -> request.getId().equals(getRequestEntity().getId()));
					formality.setDocument(null);
					DaoManager.save(formality);
				}
			}
			DaoManager.remove(Document.class, getDownloadFileId());
			t.commit();
			fillFileTable();
			fillFormalityDocuments();
			fillEstateWrapper();
			fillFormalityWrapper();
		} catch (Exception e) {
			LogHelper.log(log, e);
			if (t != null && t.isActive()) t.rollback();
		}
	}

	public void downloadEstateFormalityPDF() {
		if (!ValidationHelper.isNullOrEmpty(getDownloadFileId())) {
			String projectUrl = this.getRequest().getHeader("referer");
			projectUrl = projectUrl.substring(0,
					projectUrl.indexOf(this.getCurrentPage().getPagesContext()))
					+ "/";

			PrintPDFHelper.generatePDFOnDocument(getDownloadFileId(), projectUrl);
		}
	}

	public void showReplacedSubjectDesc()
			throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		Property property = DaoManager.get(Property.class, getEntityEditId());
		if (!ValidationHelper.isNullOrEmpty(property)) {
			setSubjectFromFile(property.getReplacedSubject());
		}
	}

	public void showFormalitySubjectDesc()
			throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		Formality formality = DaoManager.get(Formality.class, getEntityEditId());
		setPresumableSubjects(new ArrayList<>());
		if (!ValidationHelper.isNullOrEmpty(formality.getSectionC())) {
			List<Long> ids = EstateSituationHelper.getIdSubjects(getRequestEntity());

			for (SectionC sectionC : formality.getSectionC()) {
				if (!ValidationHelper.isNullOrEmpty(sectionC.getSubject())) {
					for (Subject sub : sectionC.getSubject()) {
						if (ids.contains(sub.getId())) {
							getPresumableSubjects().add(sub);
						}
					}
				}
			}
		}
	}

	public void createNewEstateFormality() throws PersistenceBeanException, IllegalAccessException {
		RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_FORMALITY, getRequestEntity().getId(), null);
	}

	public void editEstateFormality() throws PersistenceBeanException, IllegalAccessException {
		RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_FORMALITY, getRequestEntity().getId(), getEntityEditId());
	}

	public void deleteEstateFormality() throws PersistenceBeanException, IllegalAccessException, InstantiationException {

		if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
			EstateFormality estateFormality = DaoManager.get(EstateFormality.class, getEntityEditId());
			DaoManager.refresh(estateFormality);

			List<Request> existingRequestsWithoutCurrent = estateFormality.getRequestFormalities().stream().map(x -> x.getRequest())
					.filter(r -> !r.getId().equals(getRequestEntity().getId())).collect(Collectors.toList());

			estateFormality.setRequestList(existingRequestsWithoutCurrent);
			DaoManager.save(estateFormality, true);

			estateFormality.setRequestList(null);
			if (isViewRelatedEstate()) {
				fillSubjectFormality();
			} else {
				fillFormalityWrapper();
			}
		}
	}

	public void createNewFormality() throws PersistenceBeanException, IllegalAccessException {
		RedirectHelper.goTo(PageTypes.REQUEST_FORMALITY_CREATE, getRequestEntity().getId(), null);
	}

	public void viewFormality() throws PersistenceBeanException, IllegalAccessException {
		RedirectHelper.goTo(PageTypes.REQUEST_FORMALITY, getEntityEditId(), true);
	}

	public void editFormality() {
		SessionHelper.put("listProperties", Boolean.TRUE);
		SessionHelper.put("editedRequestId", getRequestEntity().getId());
		if (!ValidationHelper.isNullOrEmpty(getEntityEditStato()) && ("O".equals(getEntityEditStato()) || "T".equals(getEntityEditStato()))) {
			SessionHelper.put("formalityStato", getEntityEditStato());
			SessionHelper.put("cloneFormalityId",getEntityEditId());
			RedirectHelper.goTo(PageTypes.REQUEST_FORMALITY_CREATE, getRequestEntity().getId(), null);
		}else {
			RedirectHelper.goTo(PageTypes.REQUEST_FORMALITY_CREATE, getRequestEntity().getId(), getEntityEditId());
		}


	}

	public void deleteFormality() throws Exception {
		if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
			if (FormalityHelper.deleteFormalityFromRequest(getEntityEditId(), getRequestEntity().getId())) {
				getFormalityPDFList().removeIf(x -> x.getId().equals(getEntityEditId()));
			}
		}
	}

	public void createNewProperty() throws PersistenceBeanException, IllegalAccessException {
		RedirectHelper.goTo(PageTypes.REAL_ESTATE_EDIT, getRequestEntity().getId(), null);
	}

	public void editProperty() throws PersistenceBeanException, IllegalAccessException {
		RedirectHelper.goTo(PageTypes.REAL_ESTATE_EDIT, getRequestEntity().getId(), getEntityEditId());
	}

	public void deleteProperty() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
			Property property = DaoManager.get(Property.class, getEntityEditId());
			property.setRequestList(property.getRequestList().stream()
					.filter(r -> !r.getId().equals(getRequestEntity().getId())).collect(Collectors.toList()));
			DaoManager.save(property, true);
			if (isViewRelatedEstate()) {
				fillSubjectProperty();
			} else {
				fillEstateWrapper();
			}

		}
	}

	public void goCancel() throws PersistenceBeanException, IllegalAccessException {
		if(!ValidationHelper.isNullOrEmpty(getLazyFormalityList())){
			List<EstateFormality> formalityList = (List<EstateFormality>) getLazyFormalityList().getWrappedData();
			for (EstateFormality formality : formalityList) {
				DaoManager.save(formality, true);
			}
		}
		RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_LIST, getRequestEntity().getId());
	}

	public void loadFormalityDocs() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		if (ValidationHelper.isNullOrEmpty(getEntityEditId())) {
			setFormalityDocList(null);
			return;
		}
		if (ValidationHelper.isNullOrEmpty(getRequestEntity().getAggregationLandChargesRegistry())) {
			return;
		}

		Property property = DaoManager.get(Property.class, getEntityEditId());
		List<Property> tempList = RealEstateHelper.getCadastralDatesEqualsProperties(property, DaoManager.getSession());
		List<Long> chargesRegistryIds = getRequestEntity().getAggregationLandChargesRegistersIds();

		setFormalityDocList(DaoManager.load(Document.class, new CriteriaAlias[]{
				new CriteriaAlias("formality", "f", JoinType.INNER_JOIN),
				new CriteriaAlias("f.sectionB", "sb", JoinType.INNER_JOIN),
				new CriteriaAlias("sb.properties", "p", JoinType.INNER_JOIN)
		}, new Criterion[]{
				Restrictions.eq("p.type", property.getType()),
				Restrictions.eq("p.city.id", property.getCity().getId()),
				Restrictions.eq("p.province.id", property.getProvince().getId()),
				Restrictions.in("p.id", tempList.stream().map(Property::getId).collect(Collectors.toList())),
				Restrictions.or(
						Restrictions.in("f.reclamePropertyService.id", chargesRegistryIds),
						Restrictions.in("f.provincialOffice.id", chargesRegistryIds)
						)
		}));
	}

	public void loadMostRecentFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {

		List<Long> provinceIds = Collections.singletonList(0L);
		if (!ValidationHelper.isNullOrEmpty(getRequestEntity().getAggregationLandChargesRegistry()) &&
				!ValidationHelper.isNullOrEmpty(getRequestEntity().getAggregationLandChargesRegistry()
						.getLandChargesRegistries())) {
			provinceIds = getRequestEntity().getAggregationLandChargesRegistry().getLandChargesRegistries().stream()
					.map(LandChargesRegistry::getProvinces).filter(Objects::nonNull).flatMap(List::stream)
					.map(IndexedEntity::getId).collect(Collectors.toList());
		}

		List<DocumentSubject> documentSubjectList = DaoManager.load(DocumentSubject.class, new CriteriaAlias[]{
				new CriteriaAlias("document", "doc", JoinType.INNER_JOIN),
		}, new Criterion[]{
				Restrictions.eq("subject.id", getRequestEntity().getSubject().getId()),
				Restrictions.eq("type", DocumentType.CADASTRAL),
				Restrictions.in("province.id", provinceIds),
				Restrictions.isNull("doc.request.id")
		});
		if (ValidationHelper.isNullOrEmpty(documentSubjectList)) {
			MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
					ResourcesHelper.getValidation("warning"),
					ResourcesHelper.getString("estateFormalityPropertyNoDocumentIsFound"));
			return;
		}
		DocumentSubject documentSubject = documentSubjectList.get(0);
		if (documentSubjectList.size() > 1) {
			for (DocumentSubject dc : documentSubjectList) {
				if (dc.getCreateDate().after(documentSubject.getCreateDate())) {
					documentSubject = dc;
				}
			}
		}

		Document document = documentSubject.getDocument();
		document.setRequest(getRequestEntity());
		DaoManager.save(document, true);

		List<DocumentProperty> documentPropertyList = DaoManager.load(DocumentProperty.class, new Criterion[]{
				Restrictions.eq("document.id", documentSubject.getDocument().getId())
		});
		for (DocumentProperty dp : documentPropertyList) {
			if (dp.getProperty().getRequestList() == null) {
				dp.getProperty().setRequestList(new ArrayList<>());
			}
			dp.getProperty().getRequestList().add(getRequestEntity());
			DaoManager.save(dp.getProperty(), true);
		}
		clearSession();
		fillEstateWrapper();
		fillFileTable();
	}

	public void loadMostRecentPresumableFile() throws PersistenceBeanException, IllegalAccessException {
		setServerDoc(true);
		List<Long> officeIds = Collections.singletonList(0L);
		if (!ValidationHelper.isNullOrEmpty(getRequestEntity().getAggregationLandChargesRegistry()) &&
				!ValidationHelper.isNullOrEmpty(getRequestEntity().getAggregationLandChargesRegistry()
						.getLandChargesRegistries())) {
			officeIds = getRequestEntity().getAggregationLandChargesRegistry().getLandChargesRegistries().stream()
					.map(IndexedEntity::getId).collect(Collectors.toList());
		}

		List<DocumentSubject> documentSubjectList = DaoManager.load(DocumentSubject.class, new Criterion[]{
				Restrictions.eq("subject.id", getRequestEntity().getSubject().getId()),
				Restrictions.eq("useSubjectFromXml", true),
				Restrictions.eq("type", DocumentType.ESTATE_FORMALITY),
				Restrictions.in("office.id", officeIds)
		});

		if (ValidationHelper.isNullOrEmpty(documentSubjectList)) {
			List<Subject> subjectList = DaoManager.load(Subject.class, new CriteriaAlias[]{
					new CriteriaAlias("documentSubjectList", "ds", JoinType.INNER_JOIN)
			}, new Criterion[]{
					Restrictions.isNotNull("ds.document.id"),
					Restrictions.eq("ds.useSubjectFromXml", true),
					Restrictions.eq("ds.type", DocumentType.ESTATE_FORMALITY),
					Restrictions.in("ds.office.id", officeIds),
					Restrictions.or(
							Restrictions.eq("name", getRequestEntity().getSubject().getName()),
							Restrictions.eq("surname", getRequestEntity().getSubject().getSurname()),
							Restrictions.eq("birthDate", getRequestEntity().getSubject().getBirthDate()),
							Restrictions.eq("numberVAT", getRequestEntity().getSubject().getNumberVAT())
							)
			});
			if (!ValidationHelper.isNullOrEmpty(subjectList)) {
				Subject subject = subjectList.get(0);
				int equalsFieldsNum = 0;
				if (subjectList.size() > 1) {
					for (Subject sub : subjectList) {
						int num = 0;
						if (Objects.equals(sub.getName(), subject.getName())) {
							num++;
						}
						if (Objects.equals(sub.getSurname(), subject.getSurname())) {
							num++;
						}
						if (Objects.equals(sub.getBirthDate(), subject.getBirthDate())) {
							num++;
						}
						if (Objects.equals(sub.getNumberVAT(), subject.getNumberVAT())) {
							num++;
						}
						if (num > equalsFieldsNum) {
							subject = sub;
							equalsFieldsNum = num;
						}
						if (equalsFieldsNum == 4) {
							break;
						}
					}
				}
				documentSubjectList = DaoManager.load(DocumentSubject.class, new Criterion[]{
						Restrictions.eq("subject.id", subject.getId()),
						Restrictions.eq("useSubjectFromXml", true),
						Restrictions.eq("type", DocumentType.ESTATE_FORMALITY)
				});
			}
		}

		if (!ValidationHelper.isNullOrEmpty(documentSubjectList)) {
			DocumentSubject documentSubject = documentSubjectList.get(0);
			if (documentSubjectList.size() > 1) {
				for (DocumentSubject dc : documentSubjectList) {
					if (dc.getCreateDate().after(documentSubject.getCreateDate())) {
						documentSubject = dc;
					}
				}
			}
			setCurrentDocumentSubject(documentSubject);
			setConservatoryFromFile(documentSubject.getOffice().getName());
			setSubjectFromFile(documentSubject.getSubject());
			executeJS("PF('subjectEstateFormalityUploadDialog').show();");

		} else {
			MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
					ResourcesHelper.getValidation("warning"),
					ResourcesHelper.getString("estateFormalityNoDocumentIsFound"));
		}
	}

	private void attachServiceDocumentSubject() throws PersistenceBeanException, IllegalAccessException {
		DocumentSubject documentSubject = getCurrentDocumentSubject();
		Document document = documentSubject.getDocument();
		document.setRequest(getRequestEntity());
		StringJoiner joiner = new StringJoiner("_");
		joiner.add(document.getTitle());
		if (getRequestEntity().isPhysicalPerson()) {
			joiner.add(getRequestEntity().getSubject().getSurname().toUpperCase());
			joiner.add(getRequestEntity().getSubject().getName().toUpperCase());
			joiner.add(DateTimeHelper.toXMLPatern(getRequestEntity().getSubject().getBirthDate()));
			joiner.add(getRequestEntity().getSubject().getFiscalCode());
		} else {
			joiner.add(getRequestEntity().getSubject().getBusinessName());
			joiner.add(getRequestEntity().getSubject().getNumberVAT());
		}
		document.setTitle(joiner.toString());
		DaoManager.save(document, true);

		documentSubject.setUseSubjectFromXml(false);
		documentSubject.setSubject(getRequestEntity().getSubject());
		DaoManager.save(documentSubject, true);

		List<EstateFormality> estateFormalityList = DaoManager.load(EstateFormality.class, new Criterion[]{
				Restrictions.eq("document.id", documentSubject.getDocument().getId())
		});
		for (EstateFormality ef : estateFormalityList) {
			if (ef.getRequestList() == null) {
				ef.setRequestList(new ArrayList<>());
			}
			ef.getRequestList().add(getRequestEntity());
			DaoManager.save(ef, true);
		}
		fillFormalityWrapper();
		fillFormalityDocuments();
	}

	public void downloadFormalityPdf() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
			FormalityHelper.downloadFormalityPdf(getEntityEditId());
		}
	}

	public boolean hasFormalities(Property item) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		Session session = DaoManager.getSession();
		List<Long> landIds = item.getAggregationLandChargesRegistersIds(session);
		List<Long> propertyIds = RealEstateHelper.getCadastralDatesEqualsPropertiesIds(item, session);
		if (ValidationHelper.isNullOrEmpty(propertyIds)) {
			return false;
		} else {
			return 0 < ConnectionManager.getCount(Formality.class, "id", new CriteriaAlias[]{
					new CriteriaAlias("sectionB", "sb", JoinType.INNER_JOIN),
					new CriteriaAlias("sb.properties", "p", JoinType.INNER_JOIN)
			}, new Criterion[]{
					Restrictions.eq("p.type", item.getType()),
					Restrictions.eq("p.city.id", item.getCity().getId()),
					Restrictions.eq("p.province.id", item.getProvince().getId()),
					Restrictions.in("p.id", propertyIds),
					Restrictions.or(
							Restrictions.in("reclamePropertyService.id", landIds),
							Restrictions.in("provincialOffice.id", landIds)
							)
			}, session);
		}
	}

	public String getPropertyReportStr(String propertyType, String quote, String regime) {
		if (!ValidationHelper.isNullOrEmpty(regime)) {
			return String.format("%s per %s (%s)", propertyType, quote, regime);
		} else {
			return String.format("%s per %s", propertyType, quote);
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

		if (!ValidationHelper.isNullOrEmpty(criteria)) {
			List<Formality> formalities = DaoManager.load(Formality.class, criteria.toArray(new Criterion[0]));

			setSearchedFormalityList(formalities);
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

	public void selectLandCulture() throws HibernateException, IllegalAccessException, PersistenceBeanException {
		if(!ValidationHelper.isNullOrEmpty(getSelectedPropertyQuality())) {
			List<LandCulture> landCulturesTemp = DaoManager.load(LandCulture.class, new CriteriaAlias[] {} , new Criterion[] {},
					Order.asc("name"));
			setLandCultures(ComboboxHelper.fillList(landCulturesTemp.toArray()));
			RequestContext.getCurrentInstance().update("selectLandCultureDialogId");
			executeJS("PF('selectLandCultureDialog').show();");
		}
	}

	public void assosiateLandCulture() throws NumberFormatException, HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
		if(!ValidationHelper.isNullOrEmpty(getSelectedLandCultureId())) {
			LandCulture culture = DaoManager.get(LandCulture.class, Long.parseLong(getSelectedLandCultureId()));
			LandCadastralCulture lcc = new LandCadastralCulture();
			lcc.setDescription(getSelectedPropertyQuality());
			lcc.setLandCulture(culture);
			DaoManager.save(lcc, true);
			RedirectHelper.goTo(PageTypes.REQUEST_ESTATE_SITUATION_VIEW, getRequestId(), null);
		}
	}

	public void updateNationalCost() throws Exception {
		Request request = DaoManager.get(Request.class, new Criterion[]{
				Restrictions.eq("id", getRequestId())});

		setCostManipulationHelper(new CostManipulationHelper());
		getCostManipulationHelper().setIncludeNationalCost(true);
		getCostManipulationHelper().setRequestExtraCosts(new ArrayList<>());
		List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
				Restrictions.eq("requestId", request.getId())});
		if(!ValidationHelper.isNullOrEmpty(extraCosts))
			getCostManipulationHelper().setRequestExtraCosts(extraCosts);

		if(!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry()) &&
				!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getNational()) &&
				request.getAggregationLandChargesRegistry().getNational()) {

			getCostManipulationHelper().setIncludeNationalCost(false);
			executeJS("PF('includeNationalCost2DialogWV').show();");
			return;
		}
		if(!ValidationHelper.isNullOrEmpty(getCostManipulationHelper().getIncludeNationalCost())
				&& getCostManipulationHelper().getIncludeNationalCost()) {
			if(!ValidationHelper.isNullOrEmpty(request.getMail())) {
				List<Request> requestsWithSameMailId = DaoManager.load(Request.class,
						new Criterion[] {Restrictions.and(Restrictions.eq("mail.id", request.getMail().getId()),
								Restrictions.eq("subject.id", request.getSubject().getId()))
						});
				boolean haveAnyWithIncludeSet = requestsWithSameMailId.stream().anyMatch(
						x->!ValidationHelper.isNullOrEmpty(x.getIncludeNationalCost()) && x.getIncludeNationalCost());
				if(haveAnyWithIncludeSet) {
					getCostManipulationHelper().setIncludeNationalCost(false);
					executeJS("PF('includeNationalCostDialogWV').show();");
					return;
				}
			}

			if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getService())
					&& !ValidationHelper.isNullOrEmpty(request.getService().getNationalPrice())) {
				getCostManipulationHelper().setExtraCostOther(request.getService().getNationalPrice().toString());
				getCostManipulationHelper().setExtraCostOtherNote(ResourcesHelper.getString("requestServiceNationalPriceNote"));
				getCostManipulationHelper().addExtraCost("NAZIONALEPOSITIVA", getRequestId());

				Request requestDb  = DaoManager.get(Request.class, getRequestId());
				getCostManipulationHelper().saveRequestExtraCost(requestDb, null);
				CostCalculationHelper calculation = new CostCalculationHelper(requestDb);
				calculation.calculateAllCosts(true);
				setShowAddNationalCostButton(false);
			}
		}
	}

	public void deleteNationalCost() throws Exception {
		Request request = DaoManager.get(Request.class, new Criterion[]{
				Restrictions.eq("id", getRequestId())});

		setCostManipulationHelper(new CostManipulationHelper());
		getCostManipulationHelper().setIncludeNationalCost(false);
		getCostManipulationHelper().setRequestExtraCosts(new ArrayList<>());
		List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
				Restrictions.eq("requestId", request.getId())});
		if(!ValidationHelper.isNullOrEmpty(extraCosts)) {
			getCostManipulationHelper().setRequestExtraCosts(extraCosts);
			Optional<ExtraCost> nationalExtraCost =  getCostManipulationHelper().getRequestExtraCosts()
					.stream()
					.filter(ec -> ec.getType().equals(ExtraCostType.NAZIONALEPOSITIVA))
					.findFirst();
			if(nationalExtraCost.isPresent()) {
				getCostManipulationHelper().getRequestExtraCosts().remove(nationalExtraCost.get());
				getCostManipulationHelper().setIncludeNationalCost(null);
				Request requestDb  = DaoManager.get(Request.class, getRequestId());
				getCostManipulationHelper().saveRequestExtraCost(requestDb);
				CostCalculationHelper calculation = new CostCalculationHelper(requestDb);
				calculation.calculateAllCosts(true);
				setShowAddNationalCostButton(true);
			}
		}
	}

	public boolean isViewRelatedEstate() {
		return viewRelatedEstate;
	}

	public void setViewRelatedEstate(boolean viewRelatedEstate) {
		this.viewRelatedEstate = viewRelatedEstate;
	}

	public List<Document> getFormalityDocumentList() {
		return formalityDocumentList;
	}

	public void setFormalityDocumentList(List<Document> formalityDocumentList) {
		this.formalityDocumentList = formalityDocumentList;
	}

	public Request getRequestEntity() {
		return requestEntity;
	}

	public void setRequestEntity(Request requestEntity) {
		this.requestEntity = requestEntity;
	}

	public List<FormalityView> getFormalityPDFList() {
		return formalityPDFList;
	}

	public void setFormalityPDFList(List<FormalityView> formalityPDFList) {
		this.formalityPDFList = formalityPDFList;
	}

	public List<Document> getUploadedFiles() {
		return uploadedFiles;
	}

	public void setUploadedFiles(List<Document> uploadedFiles) {
		this.uploadedFiles = uploadedFiles;
	}

	public Long getDownloadFileId() {
		return downloadFileId;
	}

	public void setDownloadFileId(Long downloadFileId) {
		this.downloadFileId = downloadFileId;
	}

	public Long getSelectedSubjectId() {
		return selectedSubjectId;
	}

	public void setSelectedSubjectId(Long selectedSubjectId) {
		this.selectedSubjectId = selectedSubjectId;
	}

	public List<Long> getUploadedPdfFiles() {
		return uploadedPdfFiles;
	}

	public void setUploadedPdfFiles(List<Long> uploadedPdfFiles) {
		this.uploadedPdfFiles = uploadedPdfFiles;
	}

	public Subject getSubjectEntity() {
		return subjectEntity;
	}

	public void setSubjectEntity(Subject subjectEntity) {
		this.subjectEntity = subjectEntity;
	}

	public Long getEntityEditId() {
		return entityEditId;
	}

	public void setEntityEditId(Long entityEditId) {
		this.entityEditId = entityEditId;
	}

	public List<Document> getFormalityDocList() {
		return formalityDocList;
	}

	public void setFormalityDocList(List<Document> formalityDocList) {
		this.formalityDocList = formalityDocList;
	}

	public String getFileOnServer() {
		return fileOnServer;
	}

	public void setFileOnServer(String fileOnServer) {
		this.fileOnServer = fileOnServer;
	}

	public Subject getSubjectFromFile() {
		return subjectFromFile;
	}

	public void setSubjectFromFile(Subject subjectFromFile) {
		this.subjectFromFile = subjectFromFile;
	}

	public Boolean getProperty() {
		return property;
	}

	public void setProperty(Boolean property) {
		this.property = property;
	}

	public String getConservatoryFromFile() {
		return conservatoryFromFile;
	}

	public void setConservatoryFromFile(String conservatoryFromFile) {
		this.conservatoryFromFile = conservatoryFromFile;
	}

	public boolean isServerDoc() {
		return serverDoc;
	}

	public void setServerDoc(boolean serverDoc) {
		this.serverDoc = serverDoc;
	}

	public DocumentSubject getCurrentDocumentSubject() {
		return currentDocumentSubject;
	}

	public void setCurrentDocumentSubject(DocumentSubject currentDocumentSubject) {
		this.currentDocumentSubject = currentDocumentSubject;
	}

	public String getErrorActCode() {
		return errorActCode;
	}

	public void setErrorActCode(String errorActCode) {
		this.errorActCode = errorActCode;
	}

	public String getErrorNoteType() {
		return errorNoteType;
	}

	public void setErrorNoteType(String errorNoteType) {
		this.errorNoteType = errorNoteType;
	}

	public void setPresumableSubjectsIds(List<Long> presumableSubjectsIds) {
		this.presumableSubjectsIds = presumableSubjectsIds;
	}

	public void setPresumableSubjects(List<Subject> presumableSubjects) {
		this.presumableSubjects = presumableSubjects;
	}

	public List<Long> getPresumableSubjectsIds() {
		return presumableSubjectsIds;
	}

	public List<Subject> getPresumableSubjects() {
		return presumableSubjects;
	}

	public String getErrorCityNoPresent() {
		return errorCityNoPresent;
	}

	public void setErrorCityNoPresent(String errorCityNoPresent) {
		this.errorCityNoPresent = errorCityNoPresent;
	}

	public List<Request> getPreviousRequestList() {
		return previousRequestList;
	}

	public void setPreviousRequestList(List<Request> previousRequestList) {
		this.previousRequestList = previousRequestList;
	}

	public WrapperLazyModel<Property, Property> getLazyPropertyListBuilding() {
		return lazyPropertyListBuilding;
	}

	public void setLazyPropertyListBuilding(
			WrapperLazyModel<Property, Property> lazyPropertyListBuilding) {
		this.lazyPropertyListBuilding = lazyPropertyListBuilding;
	}

	public WrapperLazyModel<Property, Property> getLazyPropertyListLand() {
		return lazyPropertyListLand;
	}

	public void setLazyPropertyListLand(
			WrapperLazyModel<Property, Property> lazyPropertyListLand) {
		this.lazyPropertyListLand = lazyPropertyListLand;
	}

	public WrapperLazyModel<EstateFormality, EstateFormality> getLazyFormalityList() {
		return lazyFormalityList;
	}

	public void setLazyFormalityList(
			WrapperLazyModel<EstateFormality, EstateFormality> lazyFormalityList) {
		this.lazyFormalityList = lazyFormalityList;
	}

	public String getSearchFormalityRG() {
		return searchFormalityRG;
	}

	public void setSearchFormalityRG(String searchFormalityRG) {
		this.searchFormalityRG = searchFormalityRG;
	}

	public String getSearchFormalityRP() {
		return searchFormalityRP;
	}

	public void setSearchFormalityRP(String searchFormalityRP) {
		this.searchFormalityRP = searchFormalityRP;
	}

	public Date getSearchFormalityDate() {
		return searchFormalityDate;
	}

	public void setSearchFormalityDate(Date searchFormalityDate) {
		this.searchFormalityDate = searchFormalityDate;
	}

	public List<SelectItem> getLandAggregations() {
		return landAggregations;
	}

	public void setLandAggregations(List<SelectItem> landAggregations) {
		this.landAggregations = landAggregations;
	}

	public Long getSearchFormalityAggregationId() {
		return searchFormalityAggregationId;
	}

	public void setSearchFormalityAggregationId(Long searchFormalityAggregationId) {
		this.searchFormalityAggregationId = searchFormalityAggregationId;
	}

	public List<Formality> getSearchedFormalityList() {
		return searchedFormalityList;
	}

	public void setSearchedFormalityList(List<Formality> searchedFormalityList) {
		this.searchedFormalityList = searchedFormalityList;
	}

	public List<Formality> getSelectedSearchedFormality() {
		return selectedSearchedFormality;
	}

	public void setSelectedSearchedFormality(List<Formality> selectedSearchedFormality) {
		this.selectedSearchedFormality = selectedSearchedFormality;
	}

	public Map<EstateFormality,List<Formality>> getPresumableFormalityListByEstateFormality() {
		return presumableFormalityListByEstateFormality;
	}

	public void setPresumableFormalityListByEstateFormality(Map<EstateFormality,List<Formality>> presumableFormalityListByEstateFormality) {
		this.presumableFormalityListByEstateFormality = presumableFormalityListByEstateFormality;
	}

	public List<EstateFormality> getEstateFormalitiesForPresumableFormalityCheck() {
		return estateFormalitiesForPresumableFormalityCheck;
	}

	public String getPreviousRequestListMessage() {
		return previousRequestListMessage;
	}

	public void setPreviousRequestListMessage(String previousRequestListMessage) {
		this.previousRequestListMessage = previousRequestListMessage;
	}

	public void setEstateFormalitiesForPresumableFormalityCheck(List<EstateFormality> estateFormalitiesForPresumableFormalityCheck) {
		this.estateFormalitiesForPresumableFormalityCheck = estateFormalitiesForPresumableFormalityCheck;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public Long getRequestId() {
		return requestId;
	}

	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}

	public String getEntityEditStato() {
		return entityEditStato;
	}

	public void setEntityEditStato(String entityEditStato) {
		this.entityEditStato = entityEditStato;
	}

	public String getSelectedPropertyQuality() {
		return selectedPropertyQuality;
	}

	public void setSelectedPropertyQuality(String selectedPropertyQuality) {
		this.selectedPropertyQuality = selectedPropertyQuality;
	}

	public List<SelectItem> getLandCultures() {
		return landCultures;
	}

	public void setLandCultures(List<SelectItem> landCultures) {
		this.landCultures = landCultures;
	}

	public String getSelectedLandCultureId() {
		return selectedLandCultureId;
	}

	public void setSelectedLandCultureId(String selectedLandCultureId) {
		this.selectedLandCultureId = selectedLandCultureId;
	}

	public CostManipulationHelper getCostManipulationHelper() {
		return costManipulationHelper;
	}

	public void setCostManipulationHelper(CostManipulationHelper costManipulationHelper) {
		this.costManipulationHelper = costManipulationHelper;
	}

	public Boolean getShowAddNationalCostButton() {
		return showAddNationalCostButton;
	}

	public void setShowAddNationalCostButton(Boolean showAddNationalCostButton) {
		this.showAddNationalCostButton = showAddNationalCostButton;
	}
}
