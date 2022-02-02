package it.nexera.ris.persistence.beans.entities.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class RequestSubjectPK implements Serializable {

    private static final long serialVersionUID = -8731529927098612367L;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "subject_id")
    private Long subjectId;
    
    public RequestSubjectPK() {}

    public RequestSubjectPK(Long requestId, Long subjectId) {
        this.requestId = requestId;
        this.subjectId = subjectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestSubjectPK that = (RequestSubjectPK) o;
        return Objects.equals(requestId, that.requestId) &&
                Objects.equals(subjectId, that.subjectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, subjectId);
    }
}
