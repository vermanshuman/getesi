package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.DocumentUploadHelper;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.UploadFilesWithContent;
import it.nexera.ris.web.beans.wrappers.logic.FileWrapper;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@ManagedBean(name = "importDocumentsBean")
@ViewScoped
@Getter
@Setter
public class ImportDocumentsBean extends EntityEditPageBean<Document> implements Serializable {

    private static final long serialVersionUID = 8593338560221380830L;

    private Long selectedTypeDocumentId;

    private String documentTitle;

    private List<SelectItem> documentTypes;

    private boolean reportShouldBeGenerated;

    private StreamedContent reportFile;

    private List<UploadFilesWithContent> documents;

    private Date documentDate;

    private Long selectedTypeId;

    private List<FileWrapper> importedFiles;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {

        this.setDocumentTypes(ComboboxHelper.fillList(DocumentType.class, false));
        setSelectedTypeId(1L);
        setSelectedTypeDocumentId(1L);
        setImportedFiles(new LinkedList<>());
        this.setReportShouldBeGenerated(true);
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {

    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {
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
        clearDocumentData();
    }

    public Boolean getNeedDisableDocumentTitle() {
        return DocumentType.CADASTRAL.getId().equals(getSelectedTypeDocumentId());
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

    private void addFileToWrapperList(Path report) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(report, BasicFileAttributes.class);
        FileWrapper fileWrapper = new FileWrapper(report.getFileName().toString(),
                report.toAbsolutePath().toString(),
                new Date(attr.creationTime().toMillis()));
        getImportedFiles().add(0, fileWrapper);
    }

    public void clearDocumentData() {
        this.setDocumentTitle(null);
        this.setDocumentDate(null);
        this.setSelectedTypeDocumentId(0L);
        this.setReportShouldBeGenerated(true);
        this.setDocuments(null);
    }

}
