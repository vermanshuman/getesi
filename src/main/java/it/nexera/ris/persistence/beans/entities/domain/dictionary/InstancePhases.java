package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.enums.DocumentGenerationPlaces;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.RequestEnumTypes;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "instance_phase")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "INSTANCE_PHASE_SEQ", allocationSize = 1)
public class InstancePhases extends IndexedEntity {

    private static final long serialVersionUID = -5252179405556255388L;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_place")
    private DocumentGenerationPlaces place;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private TemplateDocumentModel model;

    @Column(name = "document_type_id")
    private Long documentTypeId;

    @Column(name = "request_type_id")
    private Long requestTypeId;

    public String getDocumentType() {
        DocumentType type = DocumentType.getById(getDocumentTypeId());

        return type == null ? "" : type.toString();
    }

    public String getRequestType() {
        RequestEnumTypes type = RequestEnumTypes.getById(getRequestTypeId());

        return type == null ? "" : type.toString();
    }

    public TemplateDocumentModel getModel() {
        return model;
    }

    public void setModel(TemplateDocumentModel model) {
        this.model = model;
    }

    public DocumentGenerationPlaces getPlace() {
        return place;
    }

    public void setPlace(DocumentGenerationPlaces place) {
        this.place = place;
    }

    public Long getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Long documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public Long getRequestTypeId() {
        return requestTypeId;
    }

    public void setRequestTypeId(Long requestTypeId) {
        this.requestTypeId = requestTypeId;
    }
}
