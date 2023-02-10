package it.nexera.ris.persistence.beans.entities.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "extra_request_cost")
@Getter
@Setter
public class ExtraCost extends IndexedEntity {
    private static final long serialVersionUID = -5061989689681247463L;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "price")
    private Double price;

    @Column(name = "note")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ExtraCostType type;

    private Boolean transcription;

    private Boolean certification;
}
