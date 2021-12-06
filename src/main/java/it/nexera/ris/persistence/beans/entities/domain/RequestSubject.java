package it.nexera.ris.persistence.beans.entities.domain;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "request_subject")
@Data
public class RequestSubject implements Serializable {
    private static final long serialVersionUID = 1293656198974434567L;

    @EmbeddedId
    private RequestSubjectPK requestSubjectPK;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("requestId")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subjectId")
    private Subject subject;

    @Column(name = "type")
    private String type;

    public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public RequestSubject() {
    }

    public RequestSubject(Request request, Subject subject) {
        this.request = request;
        this.subject = subject;
        this.requestSubjectPK = new RequestSubjectPK(request.getId(), subject.getId());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}
