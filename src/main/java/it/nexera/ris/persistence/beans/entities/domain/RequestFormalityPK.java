package it.nexera.ris.persistence.beans.entities.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
public class RequestFormalityPK implements Serializable {

    private static final long serialVersionUID = 3306619599510624153L;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "formality_id")
    private Long formalityId;

    public RequestFormalityPK() {}

    public RequestFormalityPK(Long requestId, Long formalityId) {
        this.requestId = requestId;
        this.formalityId = formalityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestFormalityPK that = (RequestFormalityPK) o;
        return Objects.equals(requestId, that.requestId) &&
                Objects.equals(formalityId, that.formalityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, formalityId);
    }
}
