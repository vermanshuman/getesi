package it.nexera.ris.persistence.beans.entities.domain;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "request_formality")
@Data
public class RequestFormality implements Serializable {

	private static final long serialVersionUID = 779658840573789250L;

	@EmbeddedId
    private RequestFormalityPK requestFormalityPK;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("requestId")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("formalityId")
    private EstateFormality formality;

    @Column(name = "document_id")
    private Long documentId;

    public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public EstateFormality getFormality() {
		return formality;
	}

	public void setFormality(EstateFormality formality) {
		this.formality = formality;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Long documentId) {
		this.documentId = documentId;
	}

	public RequestFormality() {
    }

    public RequestFormality(Request request, EstateFormality formality) {
        this.request = request;
        this.formality = formality;
        this.requestFormalityPK = new RequestFormalityPK(request.getId(), formality.getId());
    }
}
