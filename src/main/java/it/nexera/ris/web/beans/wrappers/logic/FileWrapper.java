package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.FileExtensionIconType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FileWrapper implements Serializable {

    private static final long serialVersionUID = 3995170043480471489L;

    private Long id;

    private String fileName;

    private String filePath;

    private Date createDate;

    public FileWrapper(String fileName, String filePath, Date createDate) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.createDate = createDate;
    }

    public FileWrapper(Long id, String fileName, String filePath) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
    }


    public FileWrapper(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getIcon() {
        return FileExtensionIconType.getFileIcon(getFileName());
    }

    public String getStyle() {
        return FileExtensionIconType.getFileStyle(getFileName());
    }

}
