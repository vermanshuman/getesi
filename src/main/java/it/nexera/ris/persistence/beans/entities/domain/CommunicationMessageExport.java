package it.nexera.ris.persistence.beans.entities.domain;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.File;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "communication_message_export")
public class CommunicationMessageExport extends IndexedEntity {

	private static final long serialVersionUID = 5356077422736134736L;

	@Column(name = "export_date", columnDefinition = "TIMESTAMP")
    private Date exportDate;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @ManyToOne
    @JoinColumn(name = "communication_message_id")
    CommunicationMessage communicationMessage;

    public String generateFilePath(String fileName) {
        String sb = FileHelper.getCommunicationFilePath() +
                DateTimeHelper.ToFilePathString(new Date()) +
                getId();
        setFilePath(sb + File.separator + fileName);
        return sb;
    }

    public String getFileName() {
        if (!ValidationHelper.isNullOrEmpty(this.getFilePath())) {
            return FileHelper.getFileName(this.getFilePath());
        }

        return "";
    }

}