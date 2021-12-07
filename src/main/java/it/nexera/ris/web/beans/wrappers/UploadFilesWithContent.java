package it.nexera.ris.web.beans.wrappers;

import java.io.Serializable;

public class UploadFilesWithContent implements Serializable {

    private static final long serialVersionUID = 1779042729605463764L;

    private byte[] content;

    private String fileName;

    public UploadFilesWithContent(byte[] content, String fileName) {
        this.content = content;
        this.fileName = fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
