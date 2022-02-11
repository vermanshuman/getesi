package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeActNotConfigureException;
import it.nexera.ris.common.helpers.create.xls.CreateExcelDocumentImportReportHelper;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.web.beans.wrappers.UploadFilesWithContent;
import it.nexera.ris.web.beans.wrappers.logic.UploadDocumentWrapper;
import lombok.Data;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DocumentUploadHelper extends Thread {

    public transient final Log log = LogFactory.getLog(getClass());

    private boolean shouldBeReportGenerated;

    private List<UploadFilesWithContent> documents;

    private List<Long> documentsForReportIds;

    private Long selectedTypeDocumentId;

    private String documentTitle;

    private Date documentDate;

    private Long userId;

    private Long selectedTypeId;

    private PersistenceSession persistenceSession;

    public DocumentUploadHelper(boolean shouldBeReportGenerated, List<UploadFilesWithContent> documents,
                                Long selectedTypeDocumentId, String documentTitle, Date documentDate, Long userId,
                                Long selectedTypeId) {
        this.shouldBeReportGenerated = shouldBeReportGenerated;
        this.documents = documents;
        this.selectedTypeDocumentId = selectedTypeDocumentId;
        this.documentTitle = documentTitle;
        this.documentDate = documentDate;
        this.userId = userId;
        this.selectedTypeId = selectedTypeId;
        this.persistenceSession = new PersistenceSession();
    }

    private void saveUploadedFiles() {
        setDocumentsForReportIds(new ArrayList<>());
        for (UploadFilesWithContent file : getDocuments()) {
            UploadDocumentWrapper uploadDocument = null;
            //In some cases, the session does not work correctly and the document is not saved in the database
            try {
                log.info(file.getFileName());
                uploadDocument = GeneralFunctionsHelper
                        .handleFileUploadWithoutOpenExternalToolPage(file.getFileName(), file.getContent(), getSelectedTypeDocumentId(),
                                getDocumentTitle(), getDocumentDate(), null, null, getUserId(), getPersistenceSession().getSession());
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }
            if (ValidationHelper.isNullOrEmpty(uploadDocument)) {
                LogHelper.log(log, "Document was not created");
                continue;
            }
            if (DocumentType.ESTATE_FORMALITY.getId().equals(getSelectedTypeId())) {
                try {
                    ImportXMLHelper.handleXMLTagsEstateFormality(new File(uploadDocument.getDocument().getPath()), null,
                            uploadDocument.getDocument(), getPersistenceSession().getSession());
                } catch (TypeActNotConfigureException | PersistenceBeanException e) {
                    LogHelper.log(log, e);
                }
            }
            getDocumentsForReportIds().add(uploadDocument.getDocument().getId());
        }
    }

    private void generateReport() {
        try {
            Session ses = getPersistenceSession().getSession();
            List<Document> documentsForReport = ConnectionManager.load(Document.class,
                    new Criterion[]{Restrictions.in("id", getDocumentsForReportIds())}, ses);
            for (Document document : documentsForReport) {
                ConnectionManager.refresh(document, ses);
            }

            List<Document> structuredFiles = getNotDuplicatedAndFullFormalityList(documentsForReport);
            List<Document> duplicates = getDuplicatedFormalityList(documentsForReport);
            List<Document> otherFiles = getOtherImportedDocsList(documentsForReport, ListUtils.union(structuredFiles, duplicates));

            byte[] fileContent = new CreateExcelDocumentImportReportHelper().createReport(structuredFiles, duplicates, otherFiles);
            FileHelper.writeFileToFolder("report" + DateTimeHelper.toFileEntityDate(new Date()) + ".xls",
                    new File(FileHelper.getApplicationProperties().getProperty("filesReportSavePath")), fileContent);
        } catch (IOException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
    }

    private List<Document> getNotDuplicatedAndFullFormalityList(List<Document> documentsForReport) throws PersistenceBeanException {
        List<Document> result = new ArrayList<>();
        for (Document document : documentsForReport) {
            if (!ValidationHelper.isNullOrEmpty(document.getFormality())) {
                List<Long> ids = document.getFormality().stream().filter(x -> (!ValidationHelper.isNullOrEmpty(x.getSectionA())
                        && !ValidationHelper.isNullOrEmpty(x.getSectionC())))
                        .map(IndexedEntity::getId).collect(Collectors.toList());

                if (!ValidationHelper.isNullOrEmpty(ids)) {
                    List<Document> formalityDuplicated = ConnectionManager.load(Document.class, new Criterion[]{
                            Restrictions.in("formalityDuplicated", ids)}, getPersistenceSession().getSession());

                    if (!ValidationHelper.isNullOrEmpty(formalityDuplicated)) {
                        for (Document doc : formalityDuplicated) {
                            ids.remove(doc.getFormalityDuplicated());
                        }
                    }

                    if (!ValidationHelper.isNullOrEmpty(ids)) {
                        result.add(document);
                    }
                }
            }
        }
        return result;
    }

    private List<Document> getDuplicatedFormalityList(List<Document> documentsForReport) throws PersistenceBeanException {
        List<Document> result = new ArrayList<>();
        for (Document document : documentsForReport) {
            if (!ValidationHelper.isNullOrEmpty(document.getFormality())) {
                List<Long> ids = document.getFormality().stream().map(IndexedEntity::getId).collect(Collectors.toList());

                if (!ValidationHelper.isNullOrEmpty(ids)) {
                    List<Document> formalityDuplicated = ConnectionManager.load(Document.class, new Criterion[]{
                            Restrictions.in("formalityDuplicated", ids)}, getPersistenceSession().getSession());

                    if (!ValidationHelper.isNullOrEmpty(formalityDuplicated)) {
                        document.setFormalityDuplicatedDocument(formalityDuplicated.get(formalityDuplicated.size()-1));
                        result.add(document);
                    }
                }
            }
        }
        return result;
    }

    private List<Document> getOtherImportedDocsList(List<Document> documentsForReport, List<Document> usedDocs) {
        List<Document> result = new ArrayList<>(documentsForReport);
        result.removeAll(usedDocs);
        return result;
    }

    @Override
    public void run() {
        if (!ValidationHelper.isNullOrEmpty(getDocuments())) {
            saveUploadedFiles();
            if (isShouldBeReportGenerated()) {
                generateReport();
            }
        }
        getPersistenceSession().closeSession();
    }

}
