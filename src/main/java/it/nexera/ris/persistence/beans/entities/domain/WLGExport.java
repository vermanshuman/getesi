package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.FileExtensionIconType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.util.IOUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.persistence.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;

@Entity
@Table(name = "wlg_export",indexes = {@Index(columnList = "destination_path")})
public class WLGExport extends IndexedEntity {
    public transient final Log log = LogFactory.getLog(WLGExport.class);

    private static final long serialVersionUID = -400514876691385097L;

    @Column(name = "export_date", columnDefinition = "TIMESTAMP")
    private Date exportDate;

    @Column(name = "source_path", length = 500)
    private String sourcePath;

    @Column(name = "destination_path", length = 500)
    private String destinationPath;

    @Column(name = "image_selected")
    private Boolean imageSelected;

    @ManyToOne
    @JoinColumn(name = "wlg_inbox_id")
    private WLGInbox inbox;

    public WLGExport copy() throws PersistenceBeanException {
        if (new File(getDestinationPath()).exists()) {
            WLGExport export = new WLGExport();
            DaoManager.save(export, true);
            File filePath = new File(export.generateDestinationPath(getFileName()));
            try {
                FileHelper.writeFileToFolder(getFileName(),
                        filePath, FileHelper.loadContentByPath(getDestinationPath()));
            } catch (IOException e) {
                LogHelper.log(log, e);
            }
            DaoManager.save(export, true);
            return export;
        } else {
            LogHelper.log(log, "WARNING failed to copy file | no file on server: " + getDestinationPath());
        }
        return null;
    }

    public String generateDestinationPath(String fileName) {
        String sb = MailHelper.getDestinationPath() +
                DateTimeHelper.ToFilePathString(new Date()) +
                File.separator + getId();
        setDestinationPath(sb + File.separator + fileName);
        return sb;
    }

    public String getIcon() {
        return FileExtensionIconType.getFileIcon(getFileName());
    }

    public String getStyle() {
        return FileExtensionIconType.getFileStyle(getFileName());
    }

    public String getFileName() {
        if (!ValidationHelper.isNullOrEmpty(this.getDestinationPath())) {
            return FileHelper.getFileName(this.getDestinationPath());
        }

        return "";
    }

    public void downloadFile() {
        if (ValidationHelper.isNullOrEmpty(this.getDestinationPath())) {
            log.warn("File download error: Document is null");
            return;
        }

        File file = new File(this.getDestinationPath());
        try {
            FileHelper.sendFile(getFileName(), new FileInputStream(file),
                    (int) file.length());
        } catch (FileNotFoundException e) {
            FacesMessage msg = new FacesMessage(
                    ResourcesHelper.getValidation("noDocumentOnServer"), "");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    @Override
    public String toString() {
        return this.getFileName();
    }

    public Date getExportDate() {
        return exportDate;
    }

    public void setExportDate(Date exportDate) {
        this.exportDate = exportDate;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public Boolean getImageSelected() {
        return imageSelected;
    }

    public void setImageSelected(Boolean imageSelected) {
        this.imageSelected = imageSelected;
    }

    public WLGInbox getInbox() {
        return inbox;
    }

    public void setInbox(WLGInbox inbox) {
        this.inbox = inbox;
    }
}
