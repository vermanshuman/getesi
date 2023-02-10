package it.nexera.ris.persistence.beans.entities.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RequestServicePK implements Serializable {

    private static final long serialVersionUID = -4722437917088265446L;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "service_id")
    private Long serviceId;

    public RequestServicePK() {
    }

    public RequestServicePK(Long requestId, Long serviceId) {
        this.requestId = requestId;
        this.serviceId = serviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestServicePK that = (RequestServicePK) o;
        return Objects.equals(requestId, that.requestId) &&
                Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, serviceId);
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
}
