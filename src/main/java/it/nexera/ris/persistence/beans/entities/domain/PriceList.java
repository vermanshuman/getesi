package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.CostType;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CostConfiguration;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "price_list")
public class PriceList extends IndexedEntity {

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


    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public CostConfiguration getCostConfiguration() {
        return costConfiguration;
    }

    public void setCostConfiguration(CostConfiguration costConfiguration) {
        this.costConfiguration = costConfiguration;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Date getConfigureDate() {
        return configureDate;
    }

    public void setConfigureDate(Date configureDate) {
        this.configureDate = configureDate;
    }

    public String getFirstPrice() {
        return firstPrice;
    }

    public void setFirstPrice(String firstPrice) {
        this.firstPrice = firstPrice;
    }

    public String getNumberFirstBlock() {
        return numberFirstBlock;
    }

    public void setNumberFirstBlock(String numberFirstBlock) {
        this.numberFirstBlock = numberFirstBlock;
    }

    public String getNextPrice() {
        return nextPrice;
    }

    public void setNextPrice(String nextPrice) {
        this.nextPrice = nextPrice;
    }

    public String getNumberNextBlock() {
        return numberNextBlock;
    }

    public void setNumberNextBlock(String numberNextBlock) {
        this.numberNextBlock = numberNextBlock;
    }

    public Boolean getRequestEstimate() {
        return requestEstimate;
    }

    public void setRequestEstimate(Boolean requestEstimate) {
        this.requestEstimate = requestEstimate;
    }

    public String getNumberFormalityEstimate() {
        return numberFormalityEstimate;
    }

    public void setNumberFormalityEstimate(String numberFormalityEstimate) {
        this.numberFormalityEstimate = numberFormalityEstimate;
    }

    public String getPayNumberBlock() {
        return payNumberBlock;
    }

    public void setPayNumberBlock(String payNumberBlock) {
        this.payNumberBlock = payNumberBlock;
    }

    public String getPayNextPrice() {
        return payNextPrice;
    }

    public void setPayNextPrice(String payNextPrice) {
        this.payNextPrice = payNextPrice;
    }

	public Boolean getTaxable() {
		return taxable;
	}

	public void setTaxable(Boolean taxable) {
		this.taxable = taxable;
	}

	public Boolean getIsNegative() {
		return isNegative;
	}

	public void setIsNegative(Boolean isNegative) {
		this.isNegative = isNegative;
	}
	
}
