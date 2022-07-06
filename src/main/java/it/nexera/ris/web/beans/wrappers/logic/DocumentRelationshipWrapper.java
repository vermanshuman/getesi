package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.Relationship;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

public class DocumentRelationshipWrapper implements Serializable {

    private static final long serialVersionUID = -7760098335186803064L;

    public transient final Log log = LogFactory
            .getLog(DocumentRelationshipWrapper.class);

    private Relationship relationship;

    private Document document;

    private Long id;

    public DocumentRelationshipWrapper(Relationship relationship, long id) {
        this.relationship = relationship;
        this.id = new Long(id);

        try {
            this.document = DaoManager.get(Document.class,
                    relationship.getTableId());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public DocumentRelationshipWrapper(Document document, long id) {
        this.document = document;
        this.id = new Long(id);
        this.relationship = new Relationship();
        this.relationship.setTableId(document.getId());
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
