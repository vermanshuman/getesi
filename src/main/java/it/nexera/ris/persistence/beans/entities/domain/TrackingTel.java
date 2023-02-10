package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "tracking_tel")
public class TrackingTel extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = 1732109113162743853L;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private String telephone;

    private String type;
    
    @Column(name = "notes", columnDefinition="varchar(4000)")
    private String notes;
}