package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "request_managed_by")
public class RequestManagedBy extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = 7406284487978300162L;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @Column(name = "managed_by")
    private Integer managedBy;

    @Column(name="notes", columnDefinition="varchar(400)")
    private String notes;
}