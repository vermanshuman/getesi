package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "comment")
public class Comment extends IndexedEntity {


    private static final long serialVersionUID = 1565477128393729066L;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @Column(name = "comment", columnDefinition = "LONGTEXT")
    private String comment;

    public Comment() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
