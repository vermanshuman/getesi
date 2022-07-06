package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.Subject;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class UploadDocumentWrapper implements Serializable {

    private static final long serialVersionUID = 1982071537768255988L;

    private Document document;

    private String fiscalCode;

    private List<Subject> subjects;

    private File file;

    private Map<File, List<Subject>> multiSubjects;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Map<File, List<Subject>> getMultiSubjects() {
        return multiSubjects;
    }

    public void setMultiSubjects(Map<File, List<Subject>> multiSubjects) {
        this.multiSubjects = multiSubjects;
    }

}
