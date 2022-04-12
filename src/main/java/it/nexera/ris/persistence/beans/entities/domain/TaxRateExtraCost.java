package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import javax.persistence.*;

@Entity
@Table(name = "tax_rate_extra_cost")
public class TaxRateExtraCost extends IndexedEntity {
    private static final long serialVersionUID = -5061999689981247463L;

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

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public TaxRate getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(TaxRate taxRate) {
        this.taxRate = taxRate;
    }

    public ExtraCostType getExtraCostType() {
        return extraCostType;
    }

    public void setExtraCostType(ExtraCostType extraCostType) {
        this.extraCostType = extraCostType;
    }
}