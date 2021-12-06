package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.Subject;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class DocumentWrapper implements Serializable {

    private static final long serialVersionUID = -2632483185696412815L;

    private File file;

    private List<Subject> subjects;

    private Subject selectedSubject;

    private Document document;

    public DocumentWrapper(File file, Document document, List<Subject> subjects) {
        this.file = file;
        this.document = document;
        this.subjects = subjects;
    }

    public String getDialogHeader() {
        return getFile() == null ? "" : getFile().getName();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public Subject getSelectedSubject() {
        return selectedSubject;
    }

    public void setSelectedSubject(Subject selectedSubject) {
        this.selectedSubject = selectedSubject;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

}
