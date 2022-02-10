package it.nexera.ris.persistence.beans.entities.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tax_rate")
@Getter
@Setter
public class TaxRate extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -1885656205391835695L;

    @Column(name = "percentage")
    private BigDecimal percentage;

    @Column(name = "description")
    private String description;

    @Column(name = "code_sdi")
    private String codeSDI;

    @Column(name = "uso")
    private Boolean use;
}