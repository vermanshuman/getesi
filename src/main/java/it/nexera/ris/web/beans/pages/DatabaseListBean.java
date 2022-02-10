package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.view.FormalityView;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.UploadFilesWithContent;
import it.nexera.ris.web.beans.wrappers.logic.DocumentWrapper;
import it.nexera.ris.web.beans.wrappers.logic.FileWrapper;
import it.nexera.ris.web.common.EntityLazyListModel;
import it.nexera.ris.web.common.ListPaginator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.*;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.sql.JoinType;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.*;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean(name = "databaseListBean")
@ViewScoped
public class DatabaseListBean extends EntityLazyListPageBean<Subject> implements Serializable {

    private static final long serialVersionUID = 3660894408028759761L;

    private LazyDataModel<Property> lazyRealEstateModel;

    private LazyDataModel<Document> lazyDocumentModel;

    private LazyDataModel<FormalityView> lazyFormalityModel;

    private LazyDataModel<Request> lazyRequestModel;

    private LazyDataModel<ReportFormalitySubject> lazyReportFormalitySubjectModel;

    private LazyDataModel<VisureRTF> lazyVisureRTFModel;

    private Long provinceFilterId;

    private List<SelectItem> provinces;

    private Long cityFilterId;

    private List<SelectItem> cities;

    private Long cadastreFilter;

    private List<SelectItem> cadastreTypes;

    private String sectionFilter;

    private String sheetFilter;

    private String particleFilter;

    private String subFilter;

    private List<SelectItem> documentTypes;

    private Long selectedTypeId;

    private Long selectedTypeDocumentId;

    private String documentTitle;

    private Date documentDate;

    private List<UploadFilesWithContent> documents;

    private Long downloadFileId;

    private Long selectedTemplateId;

    private List<SelectItem> templates;

    private Document printDocument;

    private DocumentWrapper currentDocumentWrapper;

    private String pathToFolder;

    private Long conservatoryFilterId;

    private List<SelectItem> conservatories;

    private String generalRegisterFilter;

    private String particularRegisterFilter;

    private String presentationDateFilter;

    private List<SelectItem> landAggregations;

    private Long aggregationFilterId;

    private String firstNameFilter;

    private String lastNameFilter;

    private Date birthDateFilter;

    private String fiscalCodeFiler;

    private String businessNameFilter;

    private String numberVatFilter;

    private String legalFiscalCodeFilter;

    private Long conservatoryFilterIdVisureRTF;

    private String firstNameFilterVisureRTF;

    private String lastNameFilterVisureRTF;

    private Date birthDateFilterVisureRTF;

    private String fiscalCodeFilerVisureRTF;

    private String businessNameFilterVisureRTF;

    private String numberVatFilterVisureRTF;

    private String legalFiscalCodeFilterVisureRTF;

    private String rfsFirstNameFilter;

    private String rfsLastNameFilter;

    private String rfsBusinessNameFilter;

    private Date rfsBirthDateFilter;

    private Long rfsTypeFormalityFilterId;

    private Long rfsConservatorshipFilterId;

    private Date rfsDateFilter;

    private String rfsFiscalCodeFilter;

    private String rfsNumberFilter;

    private String rfsVatNumberFilter;

    private int activeTabIndex;

    private VisureRTF downloadVisureRTF;

    private VisureRTF uploadVisureRTF;

    private VisureRTFUpload visureRTFUploadForChange;

    private Date uploadVisureRTFUpdateDate;

    private Date filterBirthDate;

    private String uploadVisureRTFNumFormality;

    private UploadedFile documentForUploadVisureRTF;

    private Long entityVisureUploadId;

    private String visureFileName;

    private UploadFilesWithContent fileForUploadVisureRTF;

    private VisureRTF downloadVisureUploadedRTF;

    private Date minimumDate;

    private String yearRange;

    private boolean isReportShouldBeGenerated;

    private StreamedContent reportFile;

    private String filterBirthCity;

    private String filterFiscalCode;

    private List<SelectItem> codeAndDescription;

    private Long annotationDescription;

    private List<FileWrapper> importedFiles;

    private String cogNome;

    private String nome;

    private String subjectBusinessName;

    private String subjectFiscalCodeVAT;

    private String subjectBirthPlace;

    private Date subjectBirthDate;

    private String nominativo;

    @Getter
    @Setter
    private String cityColumnFilter;

    @Getter
    @Setter
    private Date birthDateColumnFilter;

    @Getter
    @Setter
    private ListPaginator paginator;

    private void pageLoadStatic() {
        if (SessionHelper.get("requestFormalityView") != null)
            SessionHelper.removeObject("requestFormalityView");
        if (SessionHelper.get("transcriptionActId") != null)
            SessionHelper.removeObject("transcriptionActId");
        if (SessionHelper.get("requestViewFormality") != null)
            SessionHelper.removeObject("requestViewFormality");
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {

        setPaginator(new ListPaginator(10, 1, 1, 1, "ASC", "surname"));

        setMinimumDate(DateTimeHelper.fromString("01/01/1990", DateTimeHelper.getDatePattern(), null));

//        this.loadList(Subject.class, new Criterion[]{
//                Restrictions.ne("incomplete", true)
//        }, new Order[]{
//                Order.asc("surname"), Order.asc("name"), Order.asc("birthDate")
//        });

        this.setLazyModel(new EntityLazyListModel<>(Subject.class, new Criterion[]{
                Restrictions.ne("incomplete", true)
        }, new Order[]{
                Order.asc("surname"), Order.asc("name"), Order.asc("birthDate")
        }));
        getLazyModel().load((getPaginator().getTablePage() - 1) * getPaginator().getRowsPerPage(), getPaginator().getRowsPerPage(),
                getPaginator().getTableSortColumn(),
                (getPaginator().getTableSortOrder() == null || getPaginator().getTableSortOrder().equalsIgnoreCase("DESC")
                        || getPaginator().getTableSortOrder().equalsIgnoreCase("UNSORTED")) ? SortOrder.DESCENDING : SortOrder.ASCENDING, new HashMap<>());

        Integer totalPages = (int) Math.ceil((getLazyModel().getRowCount() * 1.0) / getPaginator().getRowsPerPage());
        if (totalPages == 0)
            totalPages = 1;

        getPaginator().setRowCount(getLazyModel().getRowCount());
        getPaginator().setTotalPages(totalPages);
        getPaginator().setPage(getPaginator().getCurrentPageNumber());

        this.setDocumentForUploadVisureRTF(null);
        this.setFileForUploadVisureRTF(null);
        this.setVisureFileName(null);

        this.setLazyRealEstateModel(new EntityLazyListModel<>(Property.class, new Criterion[]{
                Restrictions.isNull("modified")}, new Order[]{}));

        this.setLazyDocumentModel(new EntityLazyListModel<>(Document.class, null));

        setLazyRequestModel(new EntityLazyListModel<>(Request.class, new Order[]{}));

        setLazyReportFormalitySubjectModel
                (new EntityLazyListModel<>(ReportFormalitySubject.class, new Order[]{}));

        setLazyVisureRTFModel(new EntityLazyListModel<>(VisureRTF.class, new Order[]{}));

        filterFormalityTable();

        this.setDocumentTypes(ComboboxHelper.fillList(DocumentType.class, false));
        setSelectedTypeId(1L);
        setSelectedTypeDocumentId(1L);

        setCurrentDocumentWrapper(new DocumentWrapper(null, null, null));

        setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description"),
                new Criterion[]{Restrictions.isNotNull("description")}));

        setCities(ComboboxHelper.fillList(Collections.emptyList(), true, false));

        setCadastreTypes(ComboboxHelper.fillList(RealEstateType.class));

        setConservatories(ComboboxHelper.fillList(LandChargesRegistry.class, Order.asc("name")));
        setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name")));
        setCodeAndDescription(ComboboxHelper.fillListDictionary(TypeFormality.class, new Criterion[]{}));

        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get("activeTabIndex"))) {
            setActiveTabIndex((Integer) SessionHelper.get("activeTabIndex"));
        }

//        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
//                .findComponent("form:tabs:tableSubject");
//        dataTable.setFilters((Map<String, Object>) SessionHelper.get("filtersTableSubject"));

        fillImportedFileList();
    }

    private void fillImportedFileList() throws IOException {
        setImportedFiles(new LinkedList<>());

        Path dir = Paths.get(FileHelper.getApplicationProperties().getProperty("filesReportSavePath"));

        List<Path> reports = Files.list(dir).filter(f -> !Files.isDirectory(f))
                .sorted(Comparator.comparingLong((Path f) -> f.toFile().lastModified())).collect(Collectors.toList());

        for (Path report : reports) {
            addFileToWrapperList(report);
        }
    }

    private void addFileToWrapperList(Path report) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(report, BasicFileAttributes.class);
        FileWrapper fileWrapper = new FileWrapper(report.getFileName().toString(),
                report.toAbsolutePath().toString(),
                new Date(attr.creationTime().toMillis()));
        getImportedFiles().add(0, fileWrapper);
    }

    public StreamedContent downloadReport(FileWrapper fileWrapper) {
        Optional<byte[]> content = Optional.ofNullable(FileHelper.loadContentByPath(fileWrapper.getFilePath()));

        return content.map(x -> new DefaultStreamedContent(new ByteArrayInputStream(x),
                FileHelper.getFileExtension(fileWrapper.getFilePath()), fileWrapper.getFileName())).orElse(null);
    }

    public void filterReportFormalitySubjectTable() {
        List<Criterion> criterionList = new LinkedList<>();

        if (!ValidationHelper.isNullOrEmpty(this.getRfsBirthDateFilter())) {
            criterionList.add(Restrictions.ge("expirationDate",
                    DateTimeHelper.getDayStart(getRfsBirthDateFilter())));
            criterionList.add(Restrictions.le("expirationDate",
                    DateTimeHelper.getDayEnd(getRfsBirthDateFilter())));
        }

        if (!ValidationHelper.isNullOrEmpty(this.getRfsConservatorshipFilterId())) {
            criterionList.add(Restrictions.eq("landChargesRegistry.id", this.getRfsConservatorshipFilterId()));
        }

        if (!ValidationHelper.isNullOrEmpty(this.getRfsDateFilter())) {
            criterionList.add(Restrictions.ge("expirationDate",
                    DateTimeHelper.getDayStart(getRfsDateFilter())));
            criterionList.add(Restrictions.le("expirationDate",
                    DateTimeHelper.getDayEnd(getRfsDateFilter())));
        }

        if (!ValidationHelper.isNullOrEmpty(this.getRfsFiscalCodeFilter())) {
            criterionList.add(Restrictions.ilike("fiscalCode", this.getRfsFiscalCodeFilter(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(this.getRfsBusinessNameFilter())) {
            criterionList.add(Restrictions.ilike("businessName", this.getRfsBusinessNameFilter(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(this.getRfsFirstNameFilter())) {
            criterionList.add(Restrictions.ilike("name", getRfsFirstNameFilter(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(this.getRfsLastNameFilter())) {
            criterionList.add(Restrictions.ilike("surname", this.getRfsLastNameFilter(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(this.getRfsVatNumberFilter())) {
            criterionList.add(Restrictions.ilike("numberVAT", this.getRfsVatNumberFilter(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(this.getRfsTypeFormalityFilterId())) {
            criterionList.add(Restrictions.eq("typeFormalityId", this.getRfsTypeFormalityFilterId()));
        }

        setLazyReportFormalitySubjectModel
                (new EntityLazyListModel<>(ReportFormalitySubject.class,
                        criterionList.toArray(new Criterion[0]), new Order[]{}));
    }

    public void filterFormalityTable() {
        List<Criterion> criterionList = new LinkedList<>();

        if (!ValidationHelper.isNullOrEmpty(getConservatoryFilterId())) {
            criterionList.add(Restrictions.eq("conservatoryId", getConservatoryFilterId()));
        }
        try {
            if (!ValidationHelper.isNullOrEmpty(getAnnotationDescription())) {
                TypeFormality typeFormality = DaoManager.get(TypeFormality.class, getAnnotationDescription());
                criterionList.add(Restrictions.ilike("naturaAtto", typeFormality.getCode(),
                        MatchMode.ANYWHERE));
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (!ValidationHelper.isNullOrEmpty(getGeneralRegisterFilter())) {
            criterionList.add(Restrictions.ilike("generalRegister", getGeneralRegisterFilter(),
                    MatchMode.EXACT));
        }
        if (!ValidationHelper.isNullOrEmpty(getParticularRegisterFilter())) {
            criterionList.add(Restrictions.ilike("particularRegister", getParticularRegisterFilter(),
                    MatchMode.EXACT));
        }
        if (!ValidationHelper.isNullOrEmpty(getPresentationDateFilter())) {
            if (getPresentationDateFilter().length() == 4 && NumberUtils.isParsable(getPresentationDateFilter())) {
                Date date = DateTimeHelper.fromString(getPresentationDateFilter(), DateTimeHelper.getDatePattern());
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, Integer.parseInt(getPresentationDateFilter()));

                calendar.set(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                Date yearFirstDay = calendar.getTime();
                calendar.set(Calendar.MONTH, 11);
                calendar.set(Calendar.DAY_OF_MONTH, 31);
                Date yearLastDay = calendar.getTime();
                criterionList.add(Restrictions.gt("presentationDate", yearFirstDay));
                criterionList.add(Restrictions.lt("presentationDate", yearLastDay));
            }
        }

        this.setLazyFormalityModel(new EntityLazyListModel<>(FormalityView.class,
                criterionList.toArray(new Criterion[0]), new Order[]{}));
    }

    public void filterSubjectTable() {

        if (ValidationHelper.isNullOrEmpty(this.getNominativo()) &&
                ValidationHelper.isNullOrEmpty(this.getCogNome()) &&
                ValidationHelper.isNullOrEmpty(this.getNome()) &&
                ValidationHelper.isNullOrEmpty(this.getSubjectBusinessName()) &&
                ValidationHelper.isNullOrEmpty(this.getSubjectFiscalCodeVAT()) &&
                ValidationHelper.isNullOrEmpty(this.getSubjectBirthPlace()) &&
                ValidationHelper.isNullOrEmpty(this.getSubjectBirthDate()) &&
                ValidationHelper.isNullOrEmpty(this.getCityColumnFilter()) &&
                ValidationHelper.isNullOrEmpty(this.getBirthDateColumnFilter())) {
//            this.loadList(Subject.class, new Criterion[]{
//                    Restrictions.ne("incomplete", true)
//            }, new Order[]{
//                    Order.asc("surname"), Order.asc("name"), Order.asc("birthDate")
//            });

            this.setLazyModel(new EntityLazyListModel<>(Subject.class, new Criterion[]{
                    Restrictions.ne("incomplete", true)
            }, new Order[]{
                    Order.asc("surname"), Order.asc("name"), Order.asc("birthDate")
            }));
            getLazyModel().load((getPaginator().getTablePage() - 1) * getPaginator().getRowsPerPage(), getPaginator().getRowsPerPage(),
                    getPaginator().getTableSortColumn(),
                    (getPaginator().getTableSortOrder() == null || getPaginator().getTableSortOrder().equalsIgnoreCase("DESC")
                            || getPaginator().getTableSortOrder().equalsIgnoreCase("UNSORTED")) ? SortOrder.DESCENDING : SortOrder.ASCENDING, new HashMap<>());

            Integer totalPages = (int) Math.ceil((getLazyModel().getRowCount() * 1.0) / getPaginator().getRowsPerPage());
            if (totalPages == 0)
                totalPages = 1;

            getPaginator().setRowCount(getLazyModel().getRowCount());
            getPaginator().setTotalPages(totalPages);
            getPaginator().setPage(getPaginator().getCurrentPageNumber());

        } else {

            boolean showPopup = false;

            List<Criterion> criterionList = new LinkedList<>();

            if (!ValidationHelper.isNullOrEmpty(this.getCogNome()) && !ValidationHelper.isNullOrEmpty(this.getNome())) {
                criterionList.add(Restrictions.or(
                        Restrictions.like("surname", getCogNome(), MatchMode.ANYWHERE),
                        Restrictions.like("name", getNome(), MatchMode.ANYWHERE)));
            }


            if (!ValidationHelper.isNullOrEmpty(this.getCogNome())) {
                criterionList.add(Restrictions.ilike("surname", getCogNome(), MatchMode.ANYWHERE));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getNome())) {
                criterionList.add(Restrictions.ilike("", getNome(), MatchMode.ANYWHERE));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getSubjectBusinessName())) {
                criterionList.add(Restrictions.ilike("businessName", getSubjectBusinessName(), MatchMode.ANYWHERE));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getSubjectFiscalCodeVAT())) {
                criterionList.add(Restrictions.ilike("numberVAT", this.getSubjectFiscalCodeVAT(), MatchMode.ANYWHERE));
            }

            if (!ValidationHelper.isNullOrEmpty(criterionList)) {
                List<ReportFormalitySubject> reportFormalitySubjects = null;
                try {
                    reportFormalitySubjects = DaoManager.load(ReportFormalitySubject.class,
                            criterionList.toArray(new Criterion[0]));
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                if (!ValidationHelper.isNullOrEmpty(reportFormalitySubjects)) {
                    showPopup = true;
                }
            }

            if (!showPopup) {
                criterionList.clear();

                if (!ValidationHelper.isNullOrEmpty(this.getCogNome())) {
                    criterionList.add(Restrictions.ilike("lastName", getCogNome(), MatchMode.ANYWHERE));
                }

                if (!ValidationHelper.isNullOrEmpty(this.getNome())) {
                    criterionList.add(Restrictions.ilike("firstName", getNome(), MatchMode.ANYWHERE));
                }

                if (!ValidationHelper.isNullOrEmpty(this.getSubjectBusinessName())) {
                    criterionList.add(Restrictions.ilike("businessName", getSubjectBusinessName(), MatchMode.ANYWHERE));
                }

                if (!ValidationHelper.isNullOrEmpty(this.getSubjectFiscalCodeVAT())) {
                    criterionList.add(Restrictions.ilike("fiscalCodeVat", this.getSubjectFiscalCodeVAT(), MatchMode.ANYWHERE));
                }

                if (!ValidationHelper.isNullOrEmpty(criterionList)) {
                    List<VisureRTF> visureRTFs = null;
                    try {
                        visureRTFs = DaoManager.load(VisureRTF.class,
                                criterionList.toArray(new Criterion[0]));
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                    if (!ValidationHelper.isNullOrEmpty(visureRTFs)) {
                        showPopup = true;
                    }
                }
            }

            if (!showPopup) {
                criterionList.clear();

                if (!ValidationHelper.isNullOrEmpty(this.getNome()) && !ValidationHelper.isNullOrEmpty(this.getNome())) {
                    criterionList.add(Restrictions.ilike("name", getNome(), MatchMode.ANYWHERE));
                    criterionList.add(Restrictions.ilike("name", getCogNome(), MatchMode.ANYWHERE));

                } else if (!ValidationHelper.isNullOrEmpty(this.getNome())) {
                    criterionList.add(Restrictions.ilike("name", getNome(), MatchMode.ANYWHERE));
                } else if (!ValidationHelper.isNullOrEmpty(this.getCogNome())) {
                    criterionList.add(Restrictions.ilike("name", getCogNome(), MatchMode.ANYWHERE));
                }

                if (!ValidationHelper.isNullOrEmpty(this.getSubjectFiscalCodeVAT())) {
                    criterionList.add(Restrictions.ilike("fiscalCodeVat", this.getSubjectFiscalCodeVAT(), MatchMode.ANYWHERE));
                }

                if (!ValidationHelper.isNullOrEmpty(criterionList)) {
                    List<VisureDH> visureDHs = null;
                    try {
                        visureDHs = DaoManager.load(VisureDH.class,
                                criterionList.toArray(new Criterion[0]));
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }

                    if (!ValidationHelper.isNullOrEmpty(visureDHs)) {
                        showPopup = true;
                    }
                }
            }

            criterionList.clear();

            if (!ValidationHelper.isNullOrEmpty(getNominativo())) {
                criterionList.add(Restrictions.or(
                        Restrictions.ilike("name", getNominativo(), MatchMode.ANYWHERE),
                        Restrictions.ilike("surname", getNominativo(), MatchMode.ANYWHERE),
                        Restrictions.ilike("businessName", getNominativo(), MatchMode.ANYWHERE)));
            }

//            if (!ValidationHelper.isNullOrEmpty(getCogNome())) {
//                criterionList.add(Restrictions.ilike("surname", getCogNome(),
//                        MatchMode.ANYWHERE));
//            }
//
//            if (!ValidationHelper.isNullOrEmpty(getSubjectBusinessName())) {
//                criterionList.add(Restrictions.ilike("businessName", getSubjectBusinessName(),
//                        MatchMode.ANYWHERE));
//            }

            if (!ValidationHelper.isNullOrEmpty(getSubjectFiscalCodeVAT())) {
                criterionList.add(Restrictions.or(Restrictions.ilike("fiscalCode", getSubjectFiscalCodeVAT(),
                        MatchMode.ANYWHERE), Restrictions.ilike("numberVAT", getSubjectFiscalCodeVAT(),
                        MatchMode.ANYWHERE)));
            }

            if (!ValidationHelper.isNullOrEmpty(getSubjectBirthPlace()) && ValidationHelper.isNullOrEmpty(getCityColumnFilter())) {

                criterionList.add(Restrictions.ilike("b.description", getSubjectBirthPlace(),
                        MatchMode.ANYWHERE));
            }

            if (ValidationHelper.isNullOrEmpty(getSubjectBirthPlace()) && !ValidationHelper.isNullOrEmpty(getCityColumnFilter())) {

                criterionList.add(Restrictions.ilike("b.description", getCityColumnFilter(),
                        MatchMode.ANYWHERE));
            }

            if (!ValidationHelper.isNullOrEmpty(getSubjectBirthPlace()) && !ValidationHelper.isNullOrEmpty(getCityColumnFilter())) {

                criterionList.add(Restrictions.and(Restrictions.ilike("b.description", getSubjectBirthPlace(),
                        MatchMode.ANYWHERE),
                        Restrictions.ilike("b.description", getCityColumnFilter(),
                                MatchMode.ANYWHERE)));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getSubjectBirthDate()) && ValidationHelper.isNullOrEmpty(this.getBirthDateColumnFilter())) {

                criterionList.add(Restrictions.eq("birthDate", getSubjectBirthDate()));
            }

            if (ValidationHelper.isNullOrEmpty(this.getSubjectBirthDate()) && !ValidationHelper.isNullOrEmpty(this.getBirthDateColumnFilter())) {
                criterionList.add(Restrictions.eq("birthDate", getBirthDateColumnFilter()));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getSubjectBirthDate()) && !ValidationHelper.isNullOrEmpty(this.getBirthDateColumnFilter())) {
                criterionList.add(Restrictions.and(Restrictions.eq("birthDate", getSubjectBirthDate()),
                        Restrictions.eq("birthDate", getBirthDateColumnFilter())));
            }

            criterionList.add(Restrictions.ne("incomplete", true));

//            this.loadList(Subject.class, criterionList.toArray(new Criterion[0]),
//                    new Order[]{
//                            Order.asc("surname"), Order.asc("name"), Order.asc("birthDate")
//            }, new CriteriaAlias[]{
//                    new CriteriaAlias("birthCity", "b", JoinType.INNER_JOIN)
//            });

            this.setLazyModel(new EntityLazyListModel<>(Subject.class, criterionList.toArray(new Criterion[0]), new Order[]{
                    Order.asc("surname"), Order.asc("name"), Order.asc("birthDate")
            }, new CriteriaAlias[]{
                    new CriteriaAlias("birthCity", "b", JoinType.INNER_JOIN)
            }));
            getLazyModel().load((getPaginator().getTablePage() - 1) * getPaginator().getRowsPerPage(), getPaginator().getRowsPerPage(),
                    getPaginator().getTableSortColumn(),
                    (getPaginator().getTableSortOrder() == null || getPaginator().getTableSortOrder().equalsIgnoreCase("DESC")
                            || getPaginator().getTableSortOrder().equalsIgnoreCase("UNSORTED")) ? SortOrder.DESCENDING : SortOrder.ASCENDING, new HashMap<>());
            Integer totalPages = (int) Math.ceil((getLazyModel().getRowCount() * 1.0) / getPaginator().getRowsPerPage());
            if (totalPages == 0)
                totalPages = 1;

            getPaginator().setRowCount(getLazyModel().getRowCount());
            getPaginator().setTotalPages(totalPages);
            getPaginator().setPage(getPaginator().getCurrentPageNumber());


            if (showPopup) {
                executeJS("PF('vecchiDatiWV').show()");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void filterTableFromPanel() {
        try {
            Criteria crit = DaoManager.getSession().createCriteria(CadastralData.class);

            crit.createAlias("propertyList", "p", JoinType.INNER_JOIN);

            crit.setProjection(Projections.distinct(Projections.property("p.id")));

            if (!ValidationHelper.isNullOrEmpty(getSectionFilter())) {
                crit.add(Restrictions.like("section", getSectionFilter(), MatchMode.ANYWHERE));
            }

            if (!ValidationHelper.isNullOrEmpty(getSheetFilter())) {
                crit.add(Restrictions.like("sheet", getSheetFilter(), MatchMode.ANYWHERE));
            }

            if (!ValidationHelper.isNullOrEmpty(getParticleFilter())) {
                crit.add(Restrictions.like("particle", getParticleFilter(), MatchMode.ANYWHERE));
            }

            if (!ValidationHelper.isNullOrEmpty(getSubFilter())) {
                crit.add(Restrictions.like("sub", getSubFilter(), MatchMode.ANYWHERE));
            }

            List<Long> ids = crit.list();
            if (ValidationHelper.isNullOrEmpty(ids)) {
                ids = new ArrayList<>();
                ids.add(0L);
            }

            List<Criterion> criteria = new ArrayList<>(4);

            criteria.add(Restrictions.in("id", ids));

            if (!ValidationHelper.isNullOrEmpty(getProvinceFilterId())) {
                criteria.add(Restrictions.eq("province.id", getProvinceFilterId()));
            }

            if (!ValidationHelper.isNullOrEmpty(getCityFilterId())) {
                criteria.add(Restrictions.eq("city.id", getCityFilterId()));
            }

            if (!ValidationHelper.isNullOrEmpty(getCadastreFilter())) {
                criteria.add(Restrictions.eq("type", getCadastreFilter()));
            }

            this.setLazyRealEstateModel(new EntityLazyListModel<>(Property.class, criteria.toArray(new Criterion[0]), new Order[]{}));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public final void onTabChange(final TabChangeEvent event) {
        TabView tv = (TabView) event.getComponent();
        this.activeTabIndex = tv.getActiveIndex();
        SessionHelper.put("activeTabIndex", activeTabIndex);
    }

    public void filterRequestTable() {
        List<Criterion> criterionList = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getAggregationFilterId())) {
            criterionList.add(Restrictions.eq("aggregationLandChargesRegistry.id",
                    getAggregationFilterId()));
        }
        if (!ValidationHelper.isNullOrEmpty(getFirstNameFilter())) {
            criterionList.add(Restrictions.ilike("sub.name", getFirstNameFilter(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getLastNameFilter())) {
            criterionList.add(Restrictions.ilike("sub.surname", getLastNameFilter(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getBirthDateFilter())) {
            criterionList.add(Restrictions.eq("sub.birthDate", getBirthDateFilter()));
        }
        if (!ValidationHelper.isNullOrEmpty(getFiscalCodeFiler())) {
            criterionList.add(Restrictions.ilike("sub.fiscalCode", getFiscalCodeFiler(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getBusinessNameFilter())) {
            criterionList.add(Restrictions.ilike("sub.businessName", getBusinessNameFilter(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getNumberVatFilter())) {
            criterionList.add(Restrictions.ilike("sub.numberVAT", getNumberVatFilter(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getLegalFiscalCodeFilter())) {
            criterionList.add(Restrictions.ilike("sub.fiscalCode", getLegalFiscalCodeFilter(), MatchMode.ANYWHERE));
        }
        setLazyRequestModel(new EntityLazyListModel<>(Request.class, criterionList.toArray(new Criterion[0]),
                new Order[]{}, new CriteriaAlias[]{
                new CriteriaAlias("subject", "sub", JoinType.INNER_JOIN)
        }));
    }

    public void filterTableVisureRTF() {
        List<Criterion> criterionList = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getConservatoryFilterIdVisureRTF())) {
            criterionList.add(Restrictions.eq("landChargesRegistry.id",
                    getConservatoryFilterIdVisureRTF()));
        }
        if (!ValidationHelper.isNullOrEmpty(getFirstNameFilterVisureRTF())) {
            criterionList.add(Restrictions.ilike("firstName", getFirstNameFilterVisureRTF(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getLastNameFilterVisureRTF())) {
            criterionList.add(Restrictions.ilike("lastName", getLastNameFilterVisureRTF(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getBirthDateFilterVisureRTF())) {
            criterionList.add(Restrictions.eq("birthDate", getBirthDateFilterVisureRTF()));
        }
        if (!ValidationHelper.isNullOrEmpty(getFiscalCodeFilerVisureRTF())) {
            criterionList.add(Restrictions.ilike("fiscalCodeVat", getFiscalCodeFilerVisureRTF(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getBusinessNameFilterVisureRTF())) {
            criterionList.add(Restrictions.ilike("businessName", getBusinessNameFilterVisureRTF(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getNumberVatFilterVisureRTF())) {
            criterionList.add(Restrictions.ilike("fiscalCodeVat", getNumberVatFilterVisureRTF(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getLegalFiscalCodeFilterVisureRTF())) {
            criterionList.add(Restrictions.ilike("fiscalCodeVat", getLegalFiscalCodeFilterVisureRTF(), MatchMode.ANYWHERE));
        }

        setLazyVisureRTFModel(new EntityLazyListModel<>(VisureRTF.class, criterionList.toArray(new Criterion[0]),
                new Order[]{}));
    }

    public void filterByDate() {
        List<Criterion> restrictionsList = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getFilterBirthDate())) {
            restrictionsList.add(Restrictions.eq("birthDate", getFilterBirthDate()));
        }
        restrictionsList.add(Restrictions.ne("incomplete", true));

        this.loadList(Subject.class, restrictionsList.toArray(new Criterion[0]), new Order[]{
                Order.asc("surname"), Order.asc("name"), Order.asc("birthDate")});
    }

    public void createNewSubject() {
        RedirectHelper.goTo(PageTypes.SUBJECT);
    }

    public void createNewRealEstate() {
        RedirectHelper.goTo(PageTypes.REAL_ESTATE);
    }

    public void createNewFormality() {
        RedirectHelper.goTo(PageTypes.REQUEST_FORMALITY_CREATE);
    }

    @Override
    public void editEntity() {
        RedirectHelper.goTo(PageTypes.SUBJECT, this.getEntityEditId());
    }

    public void editEntityRealEstate() {
        RedirectHelper.goTo(PageTypes.REAL_ESTATE, this.getEntityEditId());
    }

    @Override
    public void viewEntity() {
        RedirectHelper.goToOnlyView(PageTypes.SUBJECT, this.getEntityEditId());
    }

    public void viewFormality() {
        RedirectHelper.goToOnlyView(PageTypes.REQUEST_FORMALITY, this.getEntityEditId());
    }

    public void viewEstateSituation() {
        RedirectHelper.goToOnlyView(PageTypes.REQUEST_ESTATE_SITUATION_LIST, this.getEntityEditId());
    }

    public void viewEntityRealEstate() {
        RedirectHelper.goTo(PageTypes.REAL_ESTATE_VIEW, this.getEntityEditId());
    }

    public void deleteEntityRealEstate() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, NumberFormatException, IOException {
        if (this.getEntityDeleteId() != null) {
            Transaction tr = null;
            try {
                tr = PersistenceSessionManager.getBean().getSession()
                        .beginTransaction();

                if (this.getEntityDeleteId() == null
                        || this.getEntityDeleteId() == 0l) {
                    return;
                }

                DaoManager.remove(Property.class, this.getEntityDeleteId());
            } catch (Exception e) {
                if (tr != null) {
                    tr.rollback();
                }
                if (e instanceof ConstraintViolationException) {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper.getValidation("deleteFail"), "");
                } else {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            e.getMessage(), e.getCause().getMessage());
                }

                LogHelper.log(log, e);
            } finally {
                if (tr != null && !tr.wasRolledBack() && tr.isActive()) {
                    try {
                        tr.commit();
                    } catch (Exception e) {
                        if (e instanceof ConstraintViolationException) {
                            MessageHelper.addGlobalMessage(
                                    FacesMessage.SEVERITY_ERROR,
                                    ResourcesHelper.getValidation("deleteFail"),
                                    "");
                        } else {
                            MessageHelper.addGlobalMessage(
                                    FacesMessage.SEVERITY_ERROR, e.getMessage(),
                                    e.getCause().getMessage());
                        }

                        tr.rollback();

                        LogHelper.log(log, e);
                    }
                    if (tr != null && !tr.wasRolledBack()) {
                        afterEntityRemoved();
                    }
                }
            }

            this.onLoad();
        }
    }

    public void showPDF() {
        if (!ValidationHelper.isNullOrEmpty(getDownloadFileId())) {
            String projectUrl = this.getRequest().getHeader("referer");
            projectUrl = projectUrl.substring(0,
                    projectUrl.indexOf(this.getCurrentPage().getPagesContext()))
                    + "/";

            PrintPDFHelper.generatePDFOnDocument(getDownloadFileId(),
                    projectUrl);
        }
    }

    private void fillTemplates(Document doc) {
        if (doc != null) {
            try {
                this.setTemplates(GeneralFunctionsHelper.fillTemplates(
                        DocumentGenerationPlaces.DATABASE_DOCUMENT, null,
                        doc.getTypeId(), DaoManager.getSession()));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void downloadFile() {
        if (!ValidationHelper.isNullOrEmpty(getDownloadFileId())) {
            Document doc = null;

            try {
                doc = DaoManager.get(Document.class, getDownloadFileId());
            } catch (Exception e) {
                LogHelper.log(log, e);
            } finally {
                setDownloadFileId(null);
            }

            if (doc != null) {
                File file = new File(doc.getPath());
                try {
                    FileHelper.sendFile(file.getName(),
                            new FileInputStream(file), (int) file.length());
                } catch (FileNotFoundException e) {
                    FacesMessage msg = new FacesMessage(
                            ResourcesHelper.getValidation("noDocumentOnServer"),
                            "");
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                }
            }
        }
    }

    public void downloadVisureRTF() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        VisureManageHelper.downloadVisureRTF(getDownloadVisureRTF().getId());
    }

    public void downloadVisureRTFasPDF() throws IllegalAccessException, PersistenceBeanException, InstantiationException {

        VisureManageHelper.downloadVisureRTFasPDF(getDownloadVisureRTF().getId());
    }

    public void downloadVisureUploadRTFasPDF() throws IllegalAccessException, PersistenceBeanException, InstantiationException {

        VisureManageHelper.downloadVisureRTFUploadedasPDF(getDownloadVisureUploadedRTF(), getEntityVisureUploadId());
    }

    public void cleanVisureFields() {
        setUploadVisureRTFNumFormality(null);
        setUploadVisureRTFUpdateDate(null);
        setVisureRTFUploadForChange(null);
        setVisureFileName(null);
        RequestContext.getCurrentInstance().update("attachVisureFile");
    }

    public void saveVisureFile() {

        if (ValidationHelper.isNullOrEmpty(getUploadVisureRTF())
                || ValidationHelper.isNullOrEmpty(getFileForUploadVisureRTF())) {
            return;
        }
        String dir = ApplicationSettingsHolder.getInstance().getByKey(ImportSettingsType.VISURE_RTF
                .getPathFilesServer()).getValue();

        String path = dir + File.separator + "upload";

        FacesMessage msg = new FacesMessage(ResourcesHelper.getValidation("successfullySaved"), "");
        try {
            if (getFileForUploadVisureRTF() != null) {

                File filePath = new File(path);
                String fileName = FileHelper.getFileNameWOExtension(getFileForUploadVisureRTF().getFileName()) + "_" +
                        DateTimeHelper.toXMLPatern(getUploadVisureRTFUpdateDate()) +
                        FileHelper.getFileExtension(getFileForUploadVisureRTF().getFileName());
                FileHelper.writeFileToFolder(fileName, filePath, getFileForUploadVisureRTF().getContent());

                saveVisureRTFUpload(path + File.separator + fileName);
                setDocumentForUploadVisureRTF(null);
                setFileForUploadVisureRTF(null);
                setVisureFileName(null);
                setVisureRTFUploadForChange(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = new FacesMessage(ResourcesHelper.getValidation("noDocumentOnServer"), "");
            LogHelper.log(log, e);
        }
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void cancelVisureFile() {
        setDocumentForUploadVisureRTF(null);
        setFileForUploadVisureRTF(null);
        setVisureFileName(null);
        setVisureRTFUploadForChange(null);
        RequestContext.getCurrentInstance().update("attachVisureFile");
    }

    private void saveVisureRTFUpload(String visuraRTFUploadPath) throws PersistenceBeanException {
        VisureRTFUpload visureRTFUpload;
        if (ValidationHelper.isNullOrEmpty(getVisureRTFUploadForChange())) {
            visureRTFUpload = new VisureRTFUpload();
        } else {
            visureRTFUpload = getVisureRTFUploadForChange();
        }
        visureRTFUpload.setUpdateDate(getUploadVisureRTFUpdateDate());
        visureRTFUpload.setNumFormality(Long.valueOf(getUploadVisureRTFNumFormality()));
        visureRTFUpload.setVisureRTF(getUploadVisureRTF());
        visureRTFUpload.setPath(visuraRTFUploadPath);

        DaoManager.saveWeak(visureRTFUpload, true);
    }

    public void downloadRequestPdf() throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
            List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                    Restrictions.eq("request.id", getEntityEditId()),
                    Restrictions.eq("typeId", DocumentType.REQUEST_REPORT.getId())
            });
            if (!ValidationHelper.isNullOrEmpty(documents)) {
                for (Document document : documents) {
                    File file = new File(document.getPath());
                    try (FileInputStream fis = new FileInputStream(file)) {
                        FileHelper.sendFile(file.getName(), fis, (int) file.length());
                    } catch (IOException ignored) {

                    }
                }
            }
        }
    }

    public void downloadVisureUploadPdf() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntityVisureUploadId())) {
            VisureRTFUpload visureRTFUpload = DaoManager.get(VisureRTFUpload.class, getEntityVisureUploadId());

            if (!ValidationHelper.isNullOrEmpty(visureRTFUpload)
                    && !ValidationHelper.isNullOrEmpty(visureRTFUpload.getPath())) {

                File file = new File(visureRTFUpload.getPath());
                try (FileInputStream fis = new FileInputStream(file)) {
                    FileHelper.sendFile(file.getName(), fis, (int) file.length());
                } catch (IOException ignored) {
                }
            }
        }
    }


    public void downloadFormalityPdf() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
            FormalityHelper.downloadFormalityPdf(getEntityEditId());
        }
    }

    public void editFormality() {
        RedirectHelper.goTo(PageTypes.REQUEST_FORMALITY_CREATE, null, getEntityEditId());
    }

    public void checkFormalityForcedRequestEstateSituation() throws Exception {
        if (FormalityHelper.isFormalityHasForcedRequestOrEstateSituation(getEntityEditId())) {
            executeJS("PF('formalityRequestForcedEstateSituationWV').show();");
        } else {
            deleteFormality();
        }
    }

    public void deleteFormality() throws Exception {
        if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
            FormalityHelper.deleteFormality(getEntityEditId());
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        if (getDocuments() == null) {
            setDocuments(new ArrayList<>());
        }
        getDocuments().add(new UploadFilesWithContent(file.getContents(), file.getFileName()));
    }

    public void removeDocument(UploadFilesWithContent doc) {
        getDocuments().remove(doc);
    }

    public void saveFile() throws InterruptedException, IOException {
        Long idForCurrentUser = UserHolder.getInstance().getCurrentUser().getId();

        DocumentUploadHelper documentUploadHelper = new DocumentUploadHelper(isReportShouldBeGenerated(), getDocuments(),
                getSelectedTypeDocumentId(), getDocumentTitle(), getDocumentDate(), idForCurrentUser, getSelectedTypeId());

        documentUploadHelper.setDaemon(true);
        documentUploadHelper.start();
        if (isReportShouldBeGenerated()) {
            documentUploadHelper.join();
            downloadExistingReport();
        }
    }

    public void clearDocumentData() {
        this.setDocuments(null);
        this.setDocumentTitle(null);
        this.setDocumentDate(null);
        this.setSelectedTypeDocumentId(0L);
        this.setReportShouldBeGenerated(true);
    }

    public void downloadExistingReport() throws IOException {
        Path dir = Paths.get(FileHelper.getApplicationProperties().getProperty("filesReportSavePath"));

        Optional<Path> lastFilePath = Files.list(dir).filter(f -> !Files.isDirectory(f))
                .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
        if (lastFilePath.isPresent()) {
            addFileToWrapperList(lastFilePath.get());
            String filePath = getImportedFiles().get(0).getFilePath();
            byte[] fileContent = FileHelper.loadContentByPath(filePath);
            if (fileContent != null) {
                InputStream stream = new ByteArrayInputStream(fileContent);
                reportFile = new DefaultStreamedContent(stream, FileHelper.getFileExtension(filePath),
                        "report" + FileHelper.getFileExtension(filePath));
            }
        }
    }

    public void saveFolder() throws PersistenceBeanException, IllegalAccessException {
        cleanValidation();
        if (ValidationHelper.isNullOrEmpty(getPathToFolder())) {
            addFieldException("path_to_folder", "requiredField");
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("warning"),
                    ResourcesHelper.getValidation("requiredField"));
            return;
        }
        File folder = new File(getPathToFolder());
        if (!folder.exists() || !folder.isDirectory()) {
            addFieldException("path_to_folder", "pathNotFound");
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("warning"),
                    ResourcesHelper.getValidation("pathNotFound"));
            return;
        }
        DocumentType type = DocumentType.getById(getSelectedTypeId());
        switch (type) {
            case FORMALITY: {
                ApplicationSettingsHolder.getInstance()
                        .applyNewValue(ApplicationSettingsKeys.DOCUMENT_PATH_FORMALITY, getPathToFolder());
                for (File fileEntry : folder.listFiles()) {
                    if (FileHelper.getFileExtension(fileEntry.getName()).replaceAll("\\.", "")
                            .equalsIgnoreCase(GeneralFunctionsHelper.APPLICATION_PDF)) {
                        String serverCopy = new StringJoiner(File.separator)
                                .add(FileHelper.getDocumentSavePath())
                                .add(DateTimeHelper.toFormatedString(new Date(), "yyyy\\MM\\d\\"))
                                .add(getCurrentUser().getId().toString()).add(fileEntry.getName()).toString();
                        File copy = new File(serverCopy);
                        try {
                            Files.createDirectories(copy.toPath().getParent());
                            Files.copy(fileEntry.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException io) {
                            LogHelper.log(log, io);
                        }
                        GeneralFunctionsHelper.handleFileUpload(copy.getAbsolutePath(), getSelectedTypeId(),
                                copy.getName(), new Date(), null, null, true, DaoManager.getSession());
                    }
                }
                break;
            }
            case CADASTRAL:
                ApplicationSettingsHolder.getInstance()
                        .applyNewValue(ApplicationSettingsKeys.DOCUMENT_PATH_CADASTRAL, getPathToFolder());
                break;
            case ESTATE_FORMALITY:
                ApplicationSettingsHolder.getInstance()
                        .applyNewValue(ApplicationSettingsKeys.DOCUMENT_PATH_ESTATE_FORMALITY, getPathToFolder());
                break;
            case OTHER:
                ApplicationSettingsHolder.getInstance()
                        .applyNewValue(ApplicationSettingsKeys.DOCUMENT_PATH_OTHER, getPathToFolder());
                break;
        }
        setPathToFolder(null);
        executeJS("PF('folderAddDlgWV').hide();");
    }

    public void handleUpload(FileUploadEvent event) {
        setVisureFileName(FilenameUtils.getName(event.getFile().getFileName()));
        String extension = FilenameUtils.getExtension(event.getFile().getFileName());
        if (extension == null || extension.equalsIgnoreCase("rtf")) {
            RequestContext.getCurrentInstance().execute("popupConfirm()");
        }
        UploadedFile file = event.getFile();
        setFileForUploadVisureRTF(new UploadFilesWithContent(file.getContents(), file.getFileName()));
    }

    public void prepareVisureToUpdate() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntityVisureUploadId())) {
            VisureRTFUpload visureRTFUpload = DaoManager.get(VisureRTFUpload.class, getEntityVisureUploadId());

            if (!ValidationHelper.isNullOrEmpty(visureRTFUpload)) {
                setVisureFileName(FilenameUtils.getName(visureRTFUpload.getPath()));
                setVisureRTFUploadForChange(visureRTFUpload);
                setUploadVisureRTFUpdateDate(visureRTFUpload.getUpdateDate());
                setUploadVisureRTFNumFormality(visureRTFUpload.getNumFormality().toString());
                setDocumentForUploadVisureRTF(null);
                setFileForUploadVisureRTF(null);
                RequestContext.getCurrentInstance().update("attachVisureFile");
            }
        }
    }

    public void clearUploadedFile() {
        this.setDocumentForUploadVisureRTF(null);
        this.setFileForUploadVisureRTF(null);
        this.setVisureFileName(null);
    }

    public void onFilterTableSubject(AjaxBehaviorEvent event) {
        DataTable table = (DataTable) event.getSource();
        SessionHelper.put("filtersTableSubject", table.getFilters());
    }

    public void cancelSaveFolder() {
        setPathToFolder(null);
    }

    public Boolean getNeedDisableDocumentTitle() {
        return DocumentType.CADASTRAL.getId().equals(getSelectedTypeDocumentId());
    }

    public LazyDataModel<Property> getLazyRealEstateModel() {
        return lazyRealEstateModel;
    }

    public void setLazyRealEstateModel(
            LazyDataModel<Property> lazyRealEstateModel) {
        this.lazyRealEstateModel = lazyRealEstateModel;
    }

    public Long getProvinceFilterId() {
        return provinceFilterId;
    }

    public void setProvinceFilterId(Long provinceFilterId) {
        this.provinceFilterId = provinceFilterId;
    }

    public List<SelectItem> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<SelectItem> provinces) {
        this.provinces = provinces;
    }

    public void onChangeProvince() {
        try {
            if (getProvinceFilterId() != null) {
                setCities(ComboboxHelper.fillList(
                        City.class, Order.asc("description"),
                        new Criterion[]{Restrictions.eq("province.id", getProvinceFilterId()),
                                Restrictions.eq("external", Boolean.TRUE)}));
            } else {
                setCities(ComboboxHelper.fillList(
                        City.class, Order.asc("description")));
                setCities(ComboboxHelper.fillList(
                        City.class, Order.asc("description"),
                        new Criterion[]{Restrictions.eq("external", Boolean.TRUE)}));
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public Long getCityFilterId() {
        return cityFilterId;
    }

    public void setCityFilterId(Long cityFilterId) {
        this.cityFilterId = cityFilterId;
    }

    public List<SelectItem> getCities() {
        return cities;
    }

    public void setCities(List<SelectItem> cities) {
        this.cities = cities;
    }

    public void onChangeCity() {
    }

    public Long getCadastreFilter() {
        return cadastreFilter;
    }

    public void setCadastreFilter(Long cadastreFilter) {
        this.cadastreFilter = cadastreFilter;
    }

    public List<SelectItem> getCadastreTypes() {
        return cadastreTypes;
    }

    public void setCadastreTypes(List<SelectItem> cadastreTypes) {
        this.cadastreTypes = cadastreTypes;
    }

    public String getSectionFilter() {
        return sectionFilter;
    }

    public void setSectionFilter(String sectionFilter) {
        this.sectionFilter = sectionFilter;
    }

    public String getSheetFilter() {
        return sheetFilter;
    }

    public void setSheetFilter(String sheetFilter) {
        this.sheetFilter = sheetFilter;
    }

    public String getParticleFilter() {
        return particleFilter;
    }

    public void setParticleFilter(String particleFilter) {
        this.particleFilter = particleFilter;
    }

    public String getSubFilter() {
        return subFilter;
    }

    public void setSubFilter(String subFilter) {
        this.subFilter = subFilter;
    }

    public LazyDataModel<Document> getLazyDocumentModel() {
        return lazyDocumentModel;
    }

    public void setLazyDocumentModel(LazyDataModel<Document> lazyDocumentModel) {
        this.lazyDocumentModel = lazyDocumentModel;
    }

    public List<SelectItem> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<SelectItem> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public Long getSelectedTypeId() {
        return selectedTypeId;
    }

    public void setSelectedTypeId(Long selectedTypeId) {
        if (selectedTypeId != null) {
            this.selectedTypeId = selectedTypeId;
        }
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public List<UploadFilesWithContent> getDocuments() {
        return documents;
    }

    public void setDocuments(List<UploadFilesWithContent> documents) {
        this.documents = documents;
    }

    public Date getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(Date documentDate) {
        this.documentDate = documentDate;
    }

    public Long getDownloadFileId() {
        return downloadFileId;
    }

    public void setDownloadFileId(Long downloadFileId) {
        this.downloadFileId = downloadFileId;
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

    public Document getPrintDocument() {
        return printDocument;
    }

    public void setPrintDocument(Document printDocument) {
        this.printDocument = printDocument;
    }

    public DocumentWrapper getCurrentDocumentWrapper() {
        return currentDocumentWrapper;
    }

    public void setCurrentDocumentWrapper(
            DocumentWrapper currentDocumentWrapper) {
        this.currentDocumentWrapper = currentDocumentWrapper;
    }

    public String getPathToFolder() {
        return pathToFolder;
    }

    public void setPathToFolder(String pathToFolder) {
        this.pathToFolder = pathToFolder;
    }

    public LazyDataModel<FormalityView> getLazyFormalityModel() {
        return lazyFormalityModel;
    }

    public void setLazyFormalityModel(LazyDataModel<FormalityView> lazyFormalityModel) {
        this.lazyFormalityModel = lazyFormalityModel;
    }

    public Long getConservatoryFilterId() {
        return conservatoryFilterId;
    }

    public void setConservatoryFilterId(Long conservatoryFilterId) {
        this.conservatoryFilterId = conservatoryFilterId;
    }

    public List<SelectItem> getConservatories() {
        return conservatories;
    }

    public void setConservatories(List<SelectItem> conservatories) {
        this.conservatories = conservatories;
    }


    public String getGeneralRegisterFilter() {
        return generalRegisterFilter;
    }

    public void setGeneralRegisterFilter(String generalRegisterFilter) {
        this.generalRegisterFilter = generalRegisterFilter;
    }

    public String getParticularRegisterFilter() {
        return particularRegisterFilter;
    }

    public void setParticularRegisterFilter(String particularRegisterFilter) {
        this.particularRegisterFilter = particularRegisterFilter;
    }

    public LazyDataModel<Request> getLazyRequestModel() {
        return lazyRequestModel;
    }

    public void setLazyRequestModel(LazyDataModel<Request> lazyRequestModel) {
        this.lazyRequestModel = lazyRequestModel;
    }

    public LazyDataModel<ReportFormalitySubject> getLazyReportFormalitySubjectModel() {
        return lazyReportFormalitySubjectModel;
    }

    public void setLazyReportFormalitySubjectModel(LazyDataModel<ReportFormalitySubject> lazyReportFormalitySubjectModel) {
        this.lazyReportFormalitySubjectModel = lazyReportFormalitySubjectModel;
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

    public String getFirstNameFilter() {
        return firstNameFilter;
    }

    public void setFirstNameFilter(String firstNameFilter) {
        this.firstNameFilter = firstNameFilter;
    }

    public String getLastNameFilter() {
        return lastNameFilter;
    }

    public void setLastNameFilter(String lastNameFilter) {
        this.lastNameFilter = lastNameFilter;
    }

    public Date getBirthDateFilter() {
        return birthDateFilter;
    }

    public void setBirthDateFilter(Date birthDateFilter) {
        this.birthDateFilter = birthDateFilter;
    }

    public String getFiscalCodeFiler() {
        return fiscalCodeFiler;
    }

    public void setFiscalCodeFiler(String fiscalCodeFiler) {
        this.fiscalCodeFiler = fiscalCodeFiler;
    }

    public String getBusinessNameFilter() {
        return businessNameFilter;
    }

    public void setBusinessNameFilter(String businessNameFilter) {
        this.businessNameFilter = businessNameFilter;
    }

    public String getNumberVatFilter() {
        return numberVatFilter;
    }

    public void setNumberVatFilter(String numberVatFilter) {
        this.numberVatFilter = numberVatFilter;
    }

    public String getLegalFiscalCodeFilter() {
        return legalFiscalCodeFilter;
    }

    public void setLegalFiscalCodeFilter(String legalFiscalCodeFilter) {
        this.legalFiscalCodeFilter = legalFiscalCodeFilter;
    }

    public String getPresentationDateFilter() {
        return presentationDateFilter;
    }

    public void setPresentationDateFilter(String presentationDateFilter) {
        this.presentationDateFilter = presentationDateFilter;
    }

    public String getRfsFirstNameFilter() {
        return rfsFirstNameFilter;
    }

    public void setRfsFirstNameFilter(String rfsFirstNameFilter) {
        this.rfsFirstNameFilter = rfsFirstNameFilter;
    }

    public Date getRfsBirthDateFilter() {
        return rfsBirthDateFilter;
    }

    public void setRfsBirthDateFilter(Date rfsBirthDateFilter) {
        this.rfsBirthDateFilter = rfsBirthDateFilter;
    }

    public Long getRfsConservatorshipFilterId() {
        return rfsConservatorshipFilterId;
    }

    public void setRfsConservatorshipFilterId(Long rfsConservatorshipFilterId) {
        this.rfsConservatorshipFilterId = rfsConservatorshipFilterId;
    }

    public Date getRfsDateFilter() {
        return rfsDateFilter;
    }

    public void setRfsDateFilter(Date rfsDateFilter) {
        this.rfsDateFilter = rfsDateFilter;
    }

    public String getRfsFiscalCodeFilter() {
        return rfsFiscalCodeFilter;
    }

    public void setRfsFiscalCodeFilter(String rfsFiscalCodeFilter) {
        this.rfsFiscalCodeFilter = rfsFiscalCodeFilter;
    }

    public String getRfsNumberFilter() {
        return rfsNumberFilter;
    }

    public void setRfsNumberFilter(String rfsNumberFilter) {
        this.rfsNumberFilter = rfsNumberFilter;
    }

    public int getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(int activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    public Long getSelectedTypeDocumentId() {
        return selectedTypeDocumentId;
    }

    public void setSelectedTypeDocumentId(Long selectedTypeDocumentId) {
        if (selectedTypeDocumentId != null) {
            this.selectedTypeDocumentId = selectedTypeDocumentId;
        }
    }

    public String getRfsLastNameFilter() {
        return rfsLastNameFilter;
    }

    public void setRfsLastNameFilter(String rfsLastNameFilter) {
        this.rfsLastNameFilter = rfsLastNameFilter;
    }

    public String getRfsBusinessNameFilter() {
        return rfsBusinessNameFilter;
    }

    public void setRfsBusinessNameFilter(String rfsBusinessNameFilter) {
        this.rfsBusinessNameFilter = rfsBusinessNameFilter;
    }

    public String getRfsVatNumberFilter() {
        return rfsVatNumberFilter;
    }

    public void setRfsVatNumberFilter(String rfsVatNumberFilter) {
        this.rfsVatNumberFilter = rfsVatNumberFilter;
    }

    public Long getRfsTypeFormalityFilterId() {
        return rfsTypeFormalityFilterId;
    }

    public void setRfsTypeFormalityFilterId(Long rfsTypeFormalityFilterId) {
        this.rfsTypeFormalityFilterId = rfsTypeFormalityFilterId;
    }

    public LazyDataModel<VisureRTF> getLazyVisureRTFModel() {
        return lazyVisureRTFModel;
    }

    public void setLazyVisureRTFModel(LazyDataModel<VisureRTF> lazyVisureRTFModel) {
        this.lazyVisureRTFModel = lazyVisureRTFModel;
    }

    public Long getConservatoryFilterIdVisureRTF() {
        return conservatoryFilterIdVisureRTF;
    }

    public void setConservatoryFilterIdVisureRTF(Long conservatoryFilterIdVisureRTF) {
        this.conservatoryFilterIdVisureRTF = conservatoryFilterIdVisureRTF;
    }

    public String getFirstNameFilterVisureRTF() {
        return firstNameFilterVisureRTF;
    }

    public void setFirstNameFilterVisureRTF(String firstNameFilterVisureRTF) {
        this.firstNameFilterVisureRTF = firstNameFilterVisureRTF;
    }

    public String getLastNameFilterVisureRTF() {
        return lastNameFilterVisureRTF;
    }

    public void setLastNameFilterVisureRTF(String lastNameFilterVisureRTF) {
        this.lastNameFilterVisureRTF = lastNameFilterVisureRTF;
    }

    public Date getBirthDateFilterVisureRTF() {
        return birthDateFilterVisureRTF;
    }

    public void setBirthDateFilterVisureRTF(Date birthDateFilterVisureRTF) {
        this.birthDateFilterVisureRTF = birthDateFilterVisureRTF;
    }

    public String getFiscalCodeFilerVisureRTF() {
        return fiscalCodeFilerVisureRTF;
    }

    public void setFiscalCodeFilerVisureRTF(String fiscalCodeFilerVisureRTF) {
        this.fiscalCodeFilerVisureRTF = fiscalCodeFilerVisureRTF;
    }

    public String getBusinessNameFilterVisureRTF() {
        return businessNameFilterVisureRTF;
    }

    public void setBusinessNameFilterVisureRTF(String businessNameFilterVisureRTF) {
        this.businessNameFilterVisureRTF = businessNameFilterVisureRTF;
    }

    public String getNumberVatFilterVisureRTF() {
        return numberVatFilterVisureRTF;
    }

    public void setNumberVatFilterVisureRTF(String numberVatFilterVisureRTF) {
        this.numberVatFilterVisureRTF = numberVatFilterVisureRTF;
    }

    public String getLegalFiscalCodeFilterVisureRTF() {
        return legalFiscalCodeFilterVisureRTF;
    }

    public void setLegalFiscalCodeFilterVisureRTF(String legalFiscalCodeFilterVisureRTF) {
        this.legalFiscalCodeFilterVisureRTF = legalFiscalCodeFilterVisureRTF;
    }

    public VisureRTF getDownloadVisureRTF() {
        return downloadVisureRTF;
    }

    public void setDownloadVisureRTF(VisureRTF downloadVisureRTF) {
        this.downloadVisureRTF = downloadVisureRTF;
    }

    public Date getUploadVisureRTFUpdateDate() {
        return uploadVisureRTFUpdateDate;
    }

    public void setUploadVisureRTFUpdateDate(Date uploadVisureRTFUpdateDate) {
        this.uploadVisureRTFUpdateDate = uploadVisureRTFUpdateDate;
    }

    public String getUploadVisureRTFNumFormality() {
        return uploadVisureRTFNumFormality;
    }

    public void setUploadVisureRTFNumFormality(String uploadVisureRTFNumFormality) {
        this.uploadVisureRTFNumFormality = uploadVisureRTFNumFormality;
    }

    public VisureRTF getUploadVisureRTF() {
        return uploadVisureRTF;
    }

    public void setUploadVisureRTF(VisureRTF uploadVisureRTF) {
        this.uploadVisureRTF = uploadVisureRTF;
    }

    public UploadedFile getDocumentForUploadVisureRTF() {
        return documentForUploadVisureRTF;
    }

    public void setDocumentForUploadVisureRTF(UploadedFile documentForUploadVisureRTF) {
        this.documentForUploadVisureRTF = documentForUploadVisureRTF;
    }

    public Long getEntityVisureUploadId() {
        return entityVisureUploadId;
    }

    public void setEntityVisureUploadId(Long entityVisureUploadId) {
        this.entityVisureUploadId = entityVisureUploadId;
    }

    public Date getFilterBirthDate() {
        return filterBirthDate;
    }

    public void setFilterBirthDate(Date filterBirthDate) {
        this.filterBirthDate = filterBirthDate;
    }

    public String getVisureFileName() {
        return visureFileName;
    }

    public void setVisureFileName(String visureFileName) {
        this.visureFileName = visureFileName;
    }

    public UploadFilesWithContent getFileForUploadVisureRTF() {
        return fileForUploadVisureRTF;
    }

    public void setFileForUploadVisureRTF(UploadFilesWithContent fileForUploadVisureRTF) {
        this.fileForUploadVisureRTF = fileForUploadVisureRTF;
    }

    public VisureRTF getDownloadVisureUploadedRTF() {
        return downloadVisureUploadedRTF;
    }

    public void setDownloadVisureUploadedRTF(VisureRTF downloadVisureUploadedRTF) {
        this.downloadVisureUploadedRTF = downloadVisureUploadedRTF;
    }

    public Date getMinimumDate() {

        System.out.println(minimumDate);
        return minimumDate;
    }

    public void setMinimumDate(Date minimumDate) {
        this.minimumDate = minimumDate;
    }

    public Date getMinAge() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int diff = 2029 - year;
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.YEAR, diff);
        return currentDate.getTime();
    }

    public Date getMaxAge() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int diff = 1990 - year;
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.YEAR, diff);
        int maxdiff = 2029 - year;
        setYearRange("c" + diff + ":c+" + maxdiff);
        return currentDate.getTime();
    }

    public void clearFiltraPanel() {
        setSubjectBirthPlace(null);
        setSubjectBirthDate(null);
    }

    public void handleRowsChange() {
        String rowsPerPage = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("tabs:rowsPerPageSelected");
        if (!ValidationHelper.isNullOrEmpty(rowsPerPage))
            getPaginator().setRowsPerPage(Integer.parseInt(rowsPerPage));

        Integer totalPages = getPaginator().getRowCount() / getPaginator().getRowsPerPage();
        Integer pageEnd = 10;
        if (pageEnd < totalPages)
            pageEnd = totalPages;
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= pageEnd; i++) {
            builder.append("<a class=\"ui-paginator-page ui-state-default ui-corner-all page_" + i + "\"");
            builder.append("tabindex=\"0\" href=\"#\" onclick=\"changePage(" + i + ",event)\">" + i + "</a>");
        }
        getPaginator().setPaginatorString(builder.toString());
        filterSubjectTable();
    }

    public void onPageChange() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        String rowsPerPage = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("tabs:rowsPerPageSelected");
        String pageNumber = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("tabs:pageNumber");
        if (!ValidationHelper.isNullOrEmpty(rowsPerPage))
            getPaginator().setRowsPerPage(Integer.parseInt(rowsPerPage));
        if (!ValidationHelper.isNullOrEmpty(pageNumber)) {
            Integer currentPage = Integer.parseInt(pageNumber);
            getPaginator().setCurrentPageNumber(currentPage);
            StringBuilder builder = new StringBuilder();
            String cls = "ui-paginator-first ui-state-default ui-corner-all";
            if (currentPage == 1) {
                cls += " ui-state-disabled";
            }
            builder.append("<a href=\"#\" class=\"" + cls + "\"");
            builder.append(" tabindex=\"-1\" onclick=\"firstPage(event)\">\n");
            builder.append("<span class=\"ui-icon ui-icon-seek-first\">F</span>\n</a>\n");
            builder.append("<a href=\"#\" onclick=\"previousPage(event)\"");
            cls = "ui-paginator-prev ui-corner-all";
            if (currentPage == 1) {
                cls += " ui-state-disabled";
            }
            builder.append(" class=\"" + cls + "\"");
            builder.append(" tabindex=\"-1\">\n");
            builder.append("<span class=\"ui-icon ui-icon-seek-prev\">P</span>\n</a>\n");

            getPaginator().setPageNavigationStart(builder.toString());
            if (currentPage == getPaginator().getTotalPages()) {
                builder.setLength(0);
                builder.append("<a href=\"#\" class=\"ui-paginator-next ui-state-default ui-corner-all ui-state-disabled\"");
                builder.append(" tabindex=\"0\">\n");
                builder.append("<span class=\"ui-icon ui-icon-seek-next\">N</span>\n</a>\n");
                builder.append("<a href=\"#\"");
                builder.append(" class=\"ui-paginator-last ui-state-default ui-corner-all ui-state-disabled\" tabindex=\"-1\">\n");
                builder.append("<span class=\"ui-icon ui-icon-seek-end\">E</span>\n</a>\n");
                getPaginator().setPageNavigationEnd(builder.toString());
            } else {
                builder.setLength(0);
                builder.append("<a href=\"#\" class=\"ui-paginator-next ui-state-default ui-corner-all\"  onclick=\"nextPage(event)\"");
                builder.append(" tabindex=\"0\">\n");
                builder.append("<span class=\"ui-icon ui-icon-seek-next\">N</span>\n</a>\n");
                builder.append("<a href=\"#\"");
                builder.append(" class=\"ui-paginator-last ui-state-default ui-corner-all\" tabindex=\"-1\" onclick=\"lastPage(event)\">\n");
                builder.append("<span class=\"ui-icon ui-icon-seek-end\">E</span>\n</a>\n");
                getPaginator().setPageNavigationEnd(builder.toString());
            }
            getPaginator().setTablePage(currentPage);
            filterSubjectTable();
        }
    }

    public void sortData() {
        String tableHeader = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap().get("tabs:tableHeader");

        if (!ValidationHelper.isNullOrEmpty(tableHeader)) {
            String sortOrder = getPaginator().getTableSortOrder();
            if (sortOrder.equalsIgnoreCase("DESC") || sortOrder.equalsIgnoreCase("UNSORTED")) {
                getPaginator().setTableSortOrder("ASC");
            } else {
                getPaginator().setTableSortOrder("DESC");
            }
            if (tableHeader.equalsIgnoreCase("nominativo_header")) {
                if (sortOrder.equalsIgnoreCase("DESC") || sortOrder.equalsIgnoreCase("UNSORTED")) {
                    ((List<Subject>) getLazyModel().getWrappedData())
                            .sort(Comparator.comparing(Subject::getFullName).reversed());
                } else {
                    ((List<Subject>) getLazyModel().getWrappedData())
                            .sort(Comparator.comparing(Subject::getFullName));
                }
            } else if (tableHeader.equalsIgnoreCase("fiscal_code_header")) {
                getPaginator().setTableSortColumn("fiscalCode");
                filterSubjectTable();
            }else if (tableHeader.equalsIgnoreCase("birth_date_header")) {
                getPaginator().setTableSortColumn("birthDate");
                filterSubjectTable();
            }
        }
    }

    public void filterColumn() {
        filterSubjectTable();
    }

    public boolean standardContainsFilterForStringColumn(Object columnValue, Object filterValue, Locale locale) {

        return columnValue.toString().contains(filterValue.toString());
    }

    public String getYearRange() {
        return yearRange;
    }

    public void setYearRange(String yearRange) {
        this.yearRange = yearRange;
    }

    public boolean isReportShouldBeGenerated() {
        return isReportShouldBeGenerated;
    }

    public void setReportShouldBeGenerated(boolean reportShouldBeGenerated) {
        isReportShouldBeGenerated = reportShouldBeGenerated;
    }

    public StreamedContent getReportFile() {
        return reportFile;
    }

    public void setReportFile(StreamedContent reportFile) {
        this.reportFile = reportFile;
    }

    public String getFilterBirthCity() {
        return filterBirthCity;
    }

    public void setFilterBirthCity(String filterBirthCity) {
        this.filterBirthCity = filterBirthCity;
    }

    public String getFilterFiscalCode() {
        return filterFiscalCode;
    }

    public void setFilterFiscalCode(String filterFiscalCode) {
        this.filterFiscalCode = filterFiscalCode;
    }

    public VisureRTFUpload getVisureRTFUploadForChange() {
        return visureRTFUploadForChange;
    }

    public void setVisureRTFUploadForChange(VisureRTFUpload visureRTFUploadForChange) {
        this.visureRTFUploadForChange = visureRTFUploadForChange;
    }

    public List<SelectItem> getCodeAndDescription() {
        return codeAndDescription;
    }

    public Long getAnnotationDescription() {
        return annotationDescription;
    }

    public void setCodeAndDescription(List<SelectItem> codeAndDescription) {
        this.codeAndDescription = codeAndDescription;
    }

    public void setAnnotationDescription(Long annotationDescription) {
        this.annotationDescription = annotationDescription;
    }

    public List<FileWrapper> getImportedFiles() {
        return importedFiles;
    }

    public void setImportedFiles(List<FileWrapper> importedFiles) {
        this.importedFiles = importedFiles;
    }

    public String getCogNome() {
        return cogNome;
    }

    public String getNome() {
        return nome;
    }

    public String getSubjectBusinessName() {
        return subjectBusinessName;
    }

    public String getSubjectFiscalCodeVAT() {
        return subjectFiscalCodeVAT;
    }

    public void setCogNome(String cogNome) {
        this.cogNome = cogNome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setSubjectBusinessName(String subjectBusinessName) {
        this.subjectBusinessName = subjectBusinessName;
    }

    public void setSubjectFiscalCodeVAT(String subjectFiscalCodeVAT) {
        this.subjectFiscalCodeVAT = subjectFiscalCodeVAT;
    }

    public Date getSubjectBirthDate() {
        return subjectBirthDate;
    }

    public void setSubjectBirthDate(Date subjectBirthDate) {
        this.subjectBirthDate = subjectBirthDate;
    }

    public String getSubjectBirthPlace() {
        return subjectBirthPlace;
    }

    public void setSubjectBirthPlace(String subjectBirthPlace) {
        this.subjectBirthPlace = subjectBirthPlace;
    }

    public String getNominativo() {
        return nominativo;
    }

    public void setNominativo(String nominativo) {
        this.nominativo = nominativo;
    }
}
