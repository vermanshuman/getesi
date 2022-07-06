package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.persistence.beans.entities.domain.Subject;

public class UploadSubjectWrapper {

    private Subject mainSubject;

    private Subject replacedSubject;

    private String relationshipQuote;

    private String relationshipPropertyType;

    private String relationshipRegimeConiugi;

    public UploadSubjectWrapper(Subject mainSubject, Subject replacedSubject, String relationshipQuote,
                                String relationshipPropertyType, String relationshipRegimeConiugi) {
        this.mainSubject = mainSubject;
        this.replacedSubject = replacedSubject;
        this.relationshipQuote = relationshipQuote;
        this.relationshipPropertyType = relationshipPropertyType;
        this.relationshipRegimeConiugi = relationshipRegimeConiugi;
    }

    public Subject getMainSubject() {
        return mainSubject;
    }

    public void setMainSubject(Subject mainSubject) {
        this.mainSubject = mainSubject;
    }

    public Subject getReplacedSubject() {
        return replacedSubject;
    }

    public void setReplacedSubject(Subject replacedSubject) {
        this.replacedSubject = replacedSubject;
    }

    public String getRelationshipQuote() {
        return relationshipQuote;
    }

    public void setRelationshipQuote(String relationshipQuote) {
        this.relationshipQuote = relationshipQuote;
    }

    public String getRelationshipPropertyType() {
        return relationshipPropertyType;
    }

    public void setRelationshipPropertyType(String relationshipPropertyType) {
        this.relationshipPropertyType = relationshipPropertyType;
    }

    public String getRelationshipRegimeConiugi() {
        return relationshipRegimeConiugi;
    }

    public void setRelationshipRegimeConiugi(String relationshipRegimeConiugi) {
        this.relationshipRegimeConiugi = relationshipRegimeConiugi;
    }
}
