package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.create.xls.XlsxHelper;
import it.nexera.ris.common.utils.ProcessMonitor;
import it.nexera.ris.common.xml.wrappers.importXLSX.*;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.services.base.BaseService;
import it.nexera.ris.web.services.base.ThreadFactoryEx;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.SessionImpl;
import org.hibernate.sql.JoinType;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ImportXLSXService extends BaseService {

    private static transient final Log log = LogFactory.getLog(BaseHelper.class);

    private static final int MAX_NUMBER_OF_ELEMENTS_BEFORE_COMMIT = 1000;

    private ProcessMonitor processMonitor;

    private Boolean showProgressPanel;

    private static Date INCORRECT_DATE_PRESENTATION;

    private Boolean updateData;

    public ImportXLSXService(boolean updateData) {
        super("ImportXLSXService");
        setUpdateData(updateData);
    }

    public void start() {
        setExecutorService(Executors
                .newSingleThreadExecutor(new ThreadFactoryEx(name)));
        System.out.println("Running " + name + "...");
        stopFlag = false;
        setShowProgressPanel(true);
        setNotWaitBeforeStop(true);
        getExecutorService().execute(this);
        socketPush();
        initializePresentationDate();

    }

    private void initializePresentationDate() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DATE, 1);
        c.set(Calendar.YEAR, 1970);
        Date dateOne = c.getTime();

        INCORRECT_DATE_PRESENTATION = dateOne;
    }

    @Override
    protected void postRoutineFunc() {
        super.postRoutineFunc();
        setShowProgressPanel(false);
        socketPush();
    }

    @Override
    public void routineFuncInternal() {
        PersistenceSession ps = null;
        String filePath = FileHelper.getPathToXml();
        try {
            LogHelper.log(log, "file path");
            LogHelper.log(log, filePath);
            ps = new PersistenceSession();
            Session session = ps.getSession();
            String permission = ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.PERMISSION_IMPORT_FORMALITY).getValue();
            LogHelper.log(log, "permission");
            LogHelper.log(log, permission);
            if (permission == null || Boolean.valueOf(permission)) {
                Long documentsFromDB = ConnectionManager.getCount(Document.class, "id", new CriteriaAlias[]{
                        new CriteriaAlias("formality", "f", JoinType.LEFT_OUTER_JOIN),
                        new CriteriaAlias("f.sectionA", "sa", JoinType.LEFT_OUTER_JOIN)
                }, new Criterion[]{
                        Restrictions.or(
                                Restrictions.eq("duplicate", false),
                                Restrictions.isNull("duplicate")
                        ),
                        Restrictions.eq("typeId", DocumentType.FORMALITY.getId()),
                        Restrictions.or(
                                Restrictions.isNull("f.id"),
                                Restrictions.isNull("sa.id")
                        )
                }, session);
                LogHelper.log(log, "documentsFromDB");
                LogHelper.log(log, documentsFromDB.toString());
                if (!ValidationHelper.isNullOrEmpty(documentsFromDB) && documentsFromDB != 0L) {
                    List<File> filePathList = getFilePathList(filePath);
                    for (File file : filePathList) {
                        LogHelper.log(log, "process file " + file.getName());
                        getProcessMonitor().setStatusStr(String.format("[%s] %s", file.getName(), "Parse excel ..."));
                        socketPush();
                        Sheet sheet = XlsxHelper.readSheet(file.getPath());
                        List<DocumentXLSXWrapper> documentXLSXWrapperList = fillDocumentFormalityList(sheet);
                        getProcessMonitor().setEndValue(documentXLSXWrapperList.size());
                        getProcessMonitor().setStatusStr("Save document from excel...");
                        LogHelper.log(log, "documentXLSXWrapperList size = " + documentXLSXWrapperList.size());
                        if (!ValidationHelper.isNullOrEmpty(documentXLSXWrapperList)) {
                            if (((SessionImpl) session).connection().isClosed()) {
                                ps = new PersistenceSession();
                                session = ps.getSession();
                            }
                            saveNewEntries(session, documentXLSXWrapperList, ps);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {
            if (ps != null) {
                ps.closeSession();
            }
            getProcessMonitor().resetCounters();
            postRoutineFunc();
            stop();
        }
    }

    private List<File> getFilePathList(String folderPath) {
        File folder = new File(folderPath);
        List<File> fileList = new ArrayList<>();
        File[] existsFiles = folder.listFiles();
        if (folder.isDirectory() && !ValidationHelper.isNullOrEmpty(existsFiles)) {
            for (File f : existsFiles) {
                if (f.getName().contains("xlsx")) {
                    fileList.add(f);
                }
            }
        } else if (folder.isFile()) {
            if (folder.getName().contains("xlsx")) {
                fileList.add(folder);
            }
        }
        return fileList;
    }

    @Override
    protected int getPollTimeKey() {
        return 0;
    }

    public void saveNewEntries(Session session, List<DocumentXLSXWrapper> documentXLSXWrapperList, PersistenceSession ps) throws PersistenceBeanException {
        int numberOfElements = 0;
        Transaction tr = null;
        int step = documentXLSXWrapperList.size() / 100;
        if (step == 0) {
            step = 1;
        }
        for (int i = 0; i < documentXLSXWrapperList.size() && !stopFlag; i++) {
            int num = i + 1;
            getProcessMonitor().setStartValue(num);
            if (i % step == 0) {
                socketPush();
            }
            List<Document> documents = documentXLSXWrapperList.get(i).getEntity(session);
            if (!ValidationHelper.isNullOrEmpty(documents)) {
                for (Document doc : documents) {
                    saveDocument(doc, session, documentXLSXWrapperList.get(i), tr);
                }
            }
            numberOfElements++;

            if (numberOfElements > MAX_NUMBER_OF_ELEMENTS_BEFORE_COMMIT) {
                if (tr != null && !tr.wasCommitted())
                    tr.commit();

                if (ps != null)
                    ps.closeSession();

                ps = new PersistenceSession();
                session = ps.getSession();

                tr = session.beginTransaction();

                numberOfElements = 0;
            }
        }
    }

    private void socketPush() {
        EventBus eventBus = EventBusFactory.getDefault().eventBus();
        eventBus.publish("/notify", "");
    }

    private void saveDocument(Document document, Session session, DocumentXLSXWrapper documentXLSXWrapper, Transaction tr) {
        try {
            if (document == null) {
                return;
            }
            if (tr == null || !tr.isActive()) {
                tr = session.beginTransaction();
            }
            ConnectionManager.save(document, false, session);
            if (ValidationHelper.isNullOrEmpty(document.getFormality())) {
                for (FormalityXLSXWrapper formalityXLSXWrapper : documentXLSXWrapper.getFormalityList()) {
                    Formality formality = formalityXLSXWrapper.toEntity(session);
                    if (!ValidationHelper.isNullOrEmpty(document.getFolder())
                            && document.getFolder().equalsIgnoreCase(documentXLSXWrapper.getFolder())) {
                        formality.setDocument(document);
                        ConnectionManager.save(formality, false, session);
                        saveFormalityReferences(formality, formalityXLSXWrapper.getSectionA(),
                                formalityXLSXWrapper.getSectionCList(), session);
                    }
                }
            } else {
                if (getUpdateData()) {
                    for (FormalityXLSXWrapper formalityXLSXWrapper : documentXLSXWrapper.getFormalityList()) {
                        Formality formality = formalityXLSXWrapper.toEntityExists(session);
                        if (formality.getId() == null) {
                            if (tr != null && tr.isActive()) {
                                tr.rollback();
                            }
                            return;
                        }
                        additionalCheckIncorrectData(formality, formalityXLSXWrapper, session);
                        saveFormalityReferences(formality, formalityXLSXWrapper.getSectionA(),
                                formalityXLSXWrapper.getSectionCList(), session);
                    }
                } else {
                    for (Formality formality : document.getFormality().stream().distinct().collect(Collectors.toList())) {
                        for (FormalityXLSXWrapper formalityXLSXWrapper : documentXLSXWrapper.getFormalityList()) {
                            additionalCheckIncorrectData(formality, formalityXLSXWrapper, session);
                            saveFormalityReferences(formality, formalityXLSXWrapper.getSectionA(),
                                    formalityXLSXWrapper.getSectionCList(), session);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        } finally {
            if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                tr.commit();
            }
        }
    }

    private void additionalCheckIncorrectData(Formality formality,
                                              FormalityXLSXWrapper formalityXLSXWrapper, Session session) {
        if (!DateTimeHelper.areDatesEqual(formalityXLSXWrapper.getPresentationDate(), formality.getPresentationDate())) {
            formality.setPresentationDate(formalityXLSXWrapper.getPresentationDate());
            ConnectionManager.save(formality, session);
        }
    }

    private void saveFormalityReferences(Formality formality, SectionAXLSXWrapper sectionAXLSXWrapper,
                                         List<SectionCXLSXWrapper> sectionCXLSXWrapperList, Session session)
            throws IllegalAccessException, InstantiationException, PersistenceBeanException {
        SectionA sectionA = sectionAXLSXWrapper.toEntity(formality);
        if (!ValidationHelper.isNullOrEmpty(formality.getType()) &&
                !ValidationHelper.isNullOrEmpty(sectionAXLSXWrapper.getFormality())) {
            switch (formality.getTypeEnum()) {
                case TYPE_I:
                    sectionA.setDerivedFrom(sectionAXLSXWrapper.getFormality().getSectionATextWrap());
                    break;
                case TYPE_T:
                    sectionA.setConventionDescription(sectionAXLSXWrapper.getFormality().getSectionATextWrap());
                    break;
                case TYPE_A:
                    sectionA.setAnnotationDescription(sectionAXLSXWrapper.getFormality().getSectionATextWrap());
                    break;
            }
        }
        sectionA.setFormality(formality);
        ConnectionManager.save(sectionA, false, session);

        for (SectionCXLSXWrapper sectionCXLSXWrapper : sectionCXLSXWrapperList) {
            if (getUpdateData()) {

                Subject subject = sectionCXLSXWrapper.getSubject().toEntity(session);
                subject.setBirthDate(sectionCXLSXWrapper.getSubject().getBirthDate());
                ConnectionManager.save(subject, false, session);
            } else {
                List<SectionC> sectionCList = sectionCXLSXWrapper.toEntity();
                for (SectionC sectionC : sectionCList) {
                    sectionC.setFormality(formality);
                    Subject subject = sectionCXLSXWrapper.getSubject().toEntity(session);
                    if (ValidationHelper.isNullOrEmpty(subject)) {
                        ConnectionManager.save(subject, false, session);
                    }

                    if (sectionC.getSubject() == null) {
                        sectionC.setSubject(new LinkedList<>());
                    }
                    sectionC.getSubject().add(subject);

                    ConnectionManager.save(sectionC, false, session);
                }
            }
        }
    }

    public List<DocumentXLSXWrapper> fillDocumentFormalityList(Sheet sheet) {
        if (ValidationHelper.isNullOrEmpty(sheet)) {
            return null;
        }
        List<DocumentXLSXWrapper> documentXLSXWrapperList = new LinkedList<>();
        Iterator<Row> iterator = sheet.iterator();
        if (iterator.hasNext()) {
            iterator.next();
        }
        getProcessMonitor().setEndValue(sheet.getLastRowNum());
        int i = 0;
        while (iterator.hasNext() && !stopFlag) {
            getProcessMonitor().setStartValue(i++);
            if (i % 100 == 0) {
                socketPush();
            }
            Row row = iterator.next();
            if (row != null) {
                DocumentXLSXWrapper document = filteredWrapperByNameOrFolder(row.getCell(24), row.getCell(23), row.getCell(13),
                        documentXLSXWrapperList);
                if (document == null) {
                    continue;
                }
                FormalityXLSXWrapper formality = new FormalityXLSXWrapper();
                formality.setDocument(document);
                Cell cell = row.getCell(14);
                if (cell != null) {
                    formality.setType(cell.getStringCellValue());
                }
                cell = row.getCell(15);
                if (cell != null && !cell.getStringCellValue().equalsIgnoreCase("null")) {
                    formality.setSectionATextWrap(cell.getStringCellValue());
                }
                cell = row.getCell(16);
                if (cell != null) {
                    String str = cell.getStringCellValue().replaceAll("\\D+", "");
                    if (!ValidationHelper.isNullOrEmpty(str)) {
                        formality.setParticularRegister(Double.parseDouble(str));
                    }
                }
                cell = row.getCell(17);
                if (cell != null) {
                    if (!ValidationHelper.isNullOrEmpty(cell.getStringCellValue())) {
                        try {
                            Date d = DateTimeHelper.fromString(cell.getStringCellValue(), DateTimeHelper.getDatePatternForXlsx());
                            if (d != null) {
                                formality.setPresentationDate(d);
                            } else {
                                d = DateTimeHelper.fromString(cell.getStringCellValue(), DateTimeHelper.getMySQLDatePattern());
                                if (d == null) {
                                    d = new Date(new Double(Double.parseDouble(cell.getStringCellValue())).longValue());
                                }
                                formality.setPresentationDate(d);
                            }
                        } catch (Exception e) {
                            LogHelper.log(log, e);
                        }
                    }
                }
                cell = row.getCell(34);
                if (cell != null) {
                    formality.setConservatory(cell.getStringCellValue());
                }
                if (!ValidationHelper.isNullOrEmpty(document.getFormalityList())) {
                    if (document.getFormalityList().contains(formality)) {
                        formality = document.getFormalityList().get(document.getFormalityList().indexOf(formality));
                    }

                } else {
                    document.getFormalityList().add(formality);
                    SectionAXLSXWrapper sectionA = new SectionAXLSXWrapper();
                    sectionA.setFormality(formality);
                    formality.setSectionA(sectionA);
                }
                SectionCXLSXWrapper sectionC = new SectionCXLSXWrapper();
                sectionC.setFormality(formality);
                cell = row.getCell(10);
                if (cell != null) {
                    sectionC.setSectionCType(cell.getStringCellValue());
                }
                formality.getSectionCList().add(sectionC);
                SubjectXLSXWrapper subject = new SubjectXLSXWrapper();
                cell = row.getCell(0);
                if (cell != null) {
                    subject.setType(cell.getStringCellValue());
                }
                cell = row.getCell(6);
                if (cell != null) {
                    try {
                        subject.setFiscalCode(cell.getStringCellValue());
                    } catch (IllegalStateException e) {
                        subject.setFiscalCode("" + new Double(cell.getNumericCellValue()).longValue());
                    }
                }
                if (subject.getType().equalsIgnoreCase("G")) {
                    cell = row.getCell(3);
                    if (cell != null) {
                        subject.setBusinessName(cell.getStringCellValue());
                    }
                    cell = row.getCell(4);
                    if (cell != null) {
                        subject.setBirthCity(cell.getStringCellValue());
                    }
                    cell = row.getCell(7);
                    if (cell != null) {
                        subject.setCfisCity(cell.getStringCellValue());
                    }
                } else {
                    cell = row.getCell(2);
                    if (cell != null) {
                        subject.setFirstName(cell.getStringCellValue());
                    }
                    cell = row.getCell(1);
                    if (cell != null) {
                        subject.setLastName(cell.getStringCellValue());
                    }
                    cell = row.getCell(4);
                    if (cell != null) {
                        subject.setBirthCity(cell.getStringCellValue());
                    }
                    cell = row.getCell(5);
                    if (cell != null) {
                        try {
                            if (!ValidationHelper.isNullOrEmpty(cell.getDateCellValue())) {
                                subject.setBirthDate(cell.getDateCellValue());
                            }
                        } catch (IllegalStateException e) {
                            if (!ValidationHelper.isNullOrEmpty(cell.getStringCellValue())) {
                                Date d = DateTimeHelper.fromString(cell.getStringCellValue(), DateTimeHelper.getDatePatternForXlsx());
                                subject.setBirthDate(d);
                            }
                        }
                    }
                    cell = row.getCell(7);
                    if (cell != null) {
                        subject.setCfisCity(cell.getStringCellValue());
                    }
                }
                sectionC.setSubject(subject);
            }
        }
        return documentXLSXWrapperList;
    }

    private DocumentXLSXWrapper filteredWrapperByNameOrFolder(Cell cellTitle, Cell cellPath,
                                                              Cell cellType, List<DocumentXLSXWrapper> documentXLSXWrapperList) {
        if (cellTitle == null) {
            return null;
        }
        DocumentXLSXWrapper document = null;
        String value = cellTitle.getStringCellValue();
        if (cellPath != null) {
            String path = new File(cellPath.getStringCellValue()).toPath().getFileName().toString();
            document = documentXLSXWrapperList.stream()
                    .filter(d -> d.getTitle().equalsIgnoreCase(value))
                    .filter(d -> path.equalsIgnoreCase(d.getFolder())).findAny().orElse(null);
            if (document == null) {
                document = new DocumentXLSXWrapper(value, path);
                if (cellType != null) {
                    document.setType(cellType.getStringCellValue());
                }
                documentXLSXWrapperList.add(document);
            }
        } else {
            document = documentXLSXWrapperList.stream()
                    .filter(d -> d.getTitle().equalsIgnoreCase(value)).findAny().orElse(null);
            if (document == null) {
                document = new DocumentXLSXWrapper(value);
                if (cellType != null) {
                    document.setType(cellType.getStringCellValue());
                }
                documentXLSXWrapperList.add(document);
            }
        }
        return document;
    }

    private String getValueFromCell(Cell cell) {
        if (cell != null) {
            switch (cell.getCellTypeEnum()) {
                case NUMERIC:
                    LogHelper.log(log, "NUMBER " + cell.getNumericCellValue());
                    return String.valueOf(cell.getNumericCellValue());

                case STRING:
                    return cell.getStringCellValue();
            }
        }
        LogHelper.log(log, "NOTHING " + cell.getCellTypeEnum().name());
        LogHelper.log(log, "NOTHING " + cell.getRichStringCellValue().getString());
        return null;
    }

    public ProcessMonitor getProcessMonitor() {
        return processMonitor;
    }

    public void setProcessMonitor(ProcessMonitor processMonitor) {
        this.processMonitor = processMonitor;
    }

    public void setShowProgressPanel(Boolean showProgressPanel) {
        this.showProgressPanel = showProgressPanel;
    }

    public Boolean getShowProgressPanel() {
        return showProgressPanel;
    }

    public Boolean getUpdateData() {
        return updateData;
    }

    public void setUpdateData(Boolean updateData) {
        this.updateData = updateData;
    }
}
