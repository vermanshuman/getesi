package it.nexera.ris.persistence.beans.entities.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

@Entity
@Table(name = "extra_request_cost")
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

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public ExtraCostType getType() {
        return type;
    }

    public void setType(ExtraCostType type) {
        this.type = type;
    }
}
