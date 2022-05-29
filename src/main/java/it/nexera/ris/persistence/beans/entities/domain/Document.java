package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.DocumentTagEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

@Entity
@Table(name = "document")
public class Document extends DocumentTagEntity implements Cloneable {

    private static final long serialVersionUID = 8983498181599341092L;

    private static transient final Log log = LogFactory.getLog(Document.class);

    private static transient final String TEXT_XML = "xml";

    private static transient final String P7M = "p7m";

    //enum DocumentType
    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "title")
    private String title;

    @Column(name = "path")
    private String path;

    @Column(name = "folder")
    private String folder;

    @Column(name = "date")
    private Date date;

    @Column(name = "selected_for_email")
    private Boolean selectedForEmail;

    @Column(name = "duplicate")
    private Boolean duplicate;

    @OneToMany(mappedBy = "document", cascade = javax.persistence.CascadeType.ALL)
    private List<Formality> formality;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "document_request",
            joinColumns = { @JoinColumn(name = "document_id") },
            inverseJoinColumns = { @JoinColumn(name = "request_id") }
    )
    private Set<Request> invoiceRequests = new HashSet<>();

    @Column(name = "cost")
    private String cost;

    @Column(name = "invoice_number")
    private Long invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_id")
    private WLGInbox mail;

    @Column(name = "formality_duplicated")
    private Long formalityDuplicated;
    
    @Column(name = "invoice_date")
    private Date invoiceDate;
    
    @Column(name = "report_numer")
    private Long reportNumber;

    @Transient
    private byte[] uploadedDocumentContent;

    @Transient
    private String uploadedDocumentFileName;

    @Transient
    private Boolean selectedForDialogList;

    @Transient
    private Boolean selectedFormalityForExternal;

    @Transient
    private Document formalityDuplicatedDocument;
    
    @Transient
    private String fileExtension;

    public String getDocumentPath() {
        return getPath() == null ? "" : getPath().replace("\\", "\\\\");
    }

    public String getCreateUserFullName() {
        if (this.getCreateUserId() != null) {
            try {
                User user = DaoManager.get(User.class, this.getCreateUserId());

                return user == null ? "" : user.getFullname();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return "";
    }

    public Boolean getCanGeneratePDF() {
        return false;
    }

    public String getTypeDescription() {
        DocumentType type = DocumentType.getById(getTypeId());

        return type == null ? "" : type.toString();
    }

    public boolean getCanDownload() {
        return Objects.equals(getTypeId(), DocumentType.CADASTRAL.getId())
                || Objects.equals(getTypeId(), DocumentType.ESTATE_FORMALITY.getId());
    }

    public boolean getIsFormalityType() {
        return Objects.equals(getTypeId(), DocumentType.FORMALITY.getId());
    }

    public String getOlnyName() {
        if (getPath() != null) {
            int i = getPath().lastIndexOf("\\") + 1;

            return getPath().substring(i < 0 ? 0 : i);
        }

        return "";
    }

    public String getNameByPathOrTitle() {
        if(!ValidationHelper.isNullOrEmpty(getUploadedDocumentFileName())) {
            return getUploadedDocumentFileName();
        }
        if (getPath() != null) {
            int i = getPath().lastIndexOf("\\") + 1;

            return getPath().substring(i < 0 ? 0 : i);
        }

        if(getTitle() != null) {
            return getTitle();
        }

        return "";
    }

    public void downloadFile() {
        if (ValidationHelper.isNullOrEmpty(this.getPath())) {
            log.warn("File download error: Document is null");
            return;
        }
        String title = getTitle();
        if (ValidationHelper.isNullOrEmpty(FileHelper.getFileExtension(getTitle()))) {
            title += FileHelper.getFileExtension(getPath());
        }

        File file = new File(this.getPath());
        try {
            FileHelper.sendFile(title, new FileInputStream(file), (int) file.length());
        } catch (FileNotFoundException e) {
            FacesMessage msg = new FacesMessage(ResourcesHelper.getValidation("noDocumentOnServer"), "");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    @Override
    public Document clone() throws CloneNotSupportedException {
        Document documentClone = new Document();
        documentClone.setTypeId(this.getTypeId());
        documentClone.setTitle(this.getTitle());
        documentClone.setPath(this.getPath());
        documentClone.setFolder(this.getFolder());
        documentClone.setDate(this.getDate());
        documentClone.setSelectedForEmail(this.getSelectedForEmail());
        documentClone.setDuplicate(this.getDuplicate());
        documentClone.setCost(this.getCost());
        if(!ValidationHelper.isNullOrEmpty(this.getFormality())) {
            documentClone.setFormality(new ArrayList<>(this.getFormality()));
        }
        documentClone.setRequest(this.getRequest());
        documentClone.setMail(this.getMail());
        documentClone.setFormalityDuplicated(this.getFormalityDuplicated());

        return documentClone;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Formality> getFormality() {
        return formality;
    }

    public void setFormality(List<Formality> formality) {
        this.formality = formality;
    }

    @Override
    public Subject getSubject() {
        return null;
    }

    @Override
    public Client getClient() {
        return null;
    }

    @Override
    public AggregationLandChargesRegistry getAggregationLandChargesRegistry() {
        return null;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Boolean getSelectedForEmail() {
        return selectedForEmail;
    }

    public void setSelectedForEmail(Boolean selectedForEmail) {
        this.selectedForEmail = selectedForEmail;
    }

    public Boolean getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Boolean duplicate) {
        this.duplicate = duplicate;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public byte[] getUploadedDocumentContent() {
        return uploadedDocumentContent;
    }

    public void setUploadedDocumentContent(byte[] uploadedDocumentContent) {
        this.uploadedDocumentContent = uploadedDocumentContent;
    }

    public String getUploadedDocumentFileName() {
        return uploadedDocumentFileName;
    }

    public void setUploadedDocumentFileName(String uploadedDocumentFileName) {
        this.uploadedDocumentFileName = uploadedDocumentFileName;
    }

    public Boolean getSelectedForDialogList() {
        return selectedForDialogList;
    }

    public void setSelectedForDialogList(Boolean selectedForDialogList) {
        this.selectedForDialogList = selectedForDialogList;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public WLGInbox getMail() {
        return mail;
    }

    public void setMail(WLGInbox mail) {
        this.mail = mail;
    }

    public Boolean getSelectedFormalityForExternal() {
        return selectedFormalityForExternal;
    }

    public void setSelectedFormalityForExternal(Boolean selectedFormalityForExternal) {
        this.selectedFormalityForExternal = selectedFormalityForExternal;
    }

    public Long getFormalityDuplicated() {
        return formalityDuplicated;
    }

    public void setFormalityDuplicated(Long formalityDuplicated) {
        this.formalityDuplicated = formalityDuplicated;
    }

    public Long getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(Long invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Set<Request> getInvoiceRequests() {
        return invoiceRequests;
    }

    public void setInvoiceRequests(Set<Request> invoiceRequests) {
        this.invoiceRequests = invoiceRequests;
    }

    public Document getFormalityDuplicatedDocument() {
        return formalityDuplicatedDocument;
    }

    public void setFormalityDuplicatedDocument(Document formalityDuplicatedDocument) {
        this.formalityDuplicatedDocument = formalityDuplicatedDocument;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Long getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(Long reportNumber) {
        this.reportNumber = reportNumber;
    }

    public String getFileExtension() {
        if (!ValidationHelper.isNullOrEmpty(getPath())) {
            fileExtension = FileHelper.getFileExtension(getPath());
        }else if (!ValidationHelper.isNullOrEmpty(getTitle())) {
            fileExtension = FileHelper.getFileExtension(getTitle());
        }
        return fileExtension;
    }
}
