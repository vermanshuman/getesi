package it.nexera.ris.persistence.beans.entities.domain;


import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "request_service")
@Getter
@Setter
public class RequestService implements Serializable {
    private static final long serialVersionUID = -5629977521055711960L;

    @EmbeddedId
    private RequestServicePK requestServicePK;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("requestId")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("serviceId")
    private Service service;

    @JoinColumn(name = "supplier_id")
    private Long supplier_id;

	public RequestService() {
    }

    public RequestService(Request request, Service service) {
        this.request = request;
        this.service = service;
        this.requestServicePK = new RequestServicePK(request.getId(), service.getId());
    }
}
