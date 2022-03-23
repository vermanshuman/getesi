package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.CostType;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CostConfiguration;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "price_list")
@Getter
@Setter
public class PriceList extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -3426871475707072096L;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "cost_configuration_id")
    private CostConfiguration costConfiguration;

    @Column(name = "price")
    private String price;

    @Column(name = "configure_date")
    private Date configureDate;

    @Column(name = "first_price")
    private String firstPrice;

    @Column(name = "number_first_block")
    private String numberFirstBlock;

    @Column(name = "next_price")
    private String nextPrice;

    @Column(name = "number_next_block")
    private String numberNextBlock;

    @Column(name = "request_estimate")
    private Boolean requestEstimate;

    @Column(name = "number_formality_estimate")
    private String numberFormalityEstimate;

    @Column(name = "pay_number_block")
    private String payNumberBlock;

    @Column(name = "pay_next_price")
    private String payNextPrice;

    @Column(name = "taxable")
    private Boolean taxable;
    @Column(name = "is_negative")
    private Boolean isNegative;

    @ManyToOne
    @JoinColumn(name = "tax_rate_id")
    private TaxRate taxRate;

    @Transient
    private Long selectedTaxRateId;

    public PriceList() {

    }

    public PriceList(Service service, Client client,
                     CostConfiguration costConfiguration) {
        this.service = service;
        this.client = client;
        this.costConfiguration = costConfiguration;
    }

    public boolean getIsFixedOrSalaryCost() {
        if (!ValidationHelper.isNullOrEmpty(getCostConfiguration())
                && (CostType.FIXED_COST.getId().equals(getCostConfiguration().getTypeId())
                || CostType.SALARY_COST.getId().equals(getCostConfiguration().getTypeId()))) {
            return true;
        }
        return false;
    }
}