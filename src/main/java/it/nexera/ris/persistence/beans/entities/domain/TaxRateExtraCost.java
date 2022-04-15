package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "tax_rate_extra_cost")
public class TaxRateExtraCost extends IndexedEntity {
    private static final long serialVersionUID = -3171224132958181337L;

    @Column(name = "client_id")
    private Long clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_rate_id")
    private TaxRate taxRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ExtraCostType extraCostType;

    @Transient
    private Long taxRateId;

    public void setTaxRateExtraCost(TaxRateExtraCost taxRateExtraCost) {
        this.setId(taxRateExtraCost.getId());
        this.setCreateDate(taxRateExtraCost.getCreateDate());
        this.setCreateUserId(taxRateExtraCost.getCreateUserId());
        this.setUpdateDate(taxRateExtraCost.getUpdateDate());
        this.setUpdateUserId(taxRateExtraCost.getUpdateUserId());
        this.setClientId(taxRateExtraCost.getClientId());
        this.setService(taxRateExtraCost.getService());
        this.setTaxRate(taxRateExtraCost.getTaxRate());
        this.setExtraCostType(taxRateExtraCost.getExtraCostType());
    }
}