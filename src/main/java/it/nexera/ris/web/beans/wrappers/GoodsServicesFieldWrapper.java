package it.nexera.ris.web.beans.wrappers;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.SelectItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoodsServicesFieldWrapper implements Serializable {

    private static final long serialVersionUID = 9158966223009456934L;

    private Long invoiceItemId;

    private Double invoiceTotalCost;

    private Double invoiceItemAmount;

    private List<SelectItem> ums;

    private Long selectedTaxRateId;

    private List<SelectItem> vatAmounts;

    private String description;

    private Double totalLine;

    private Integer counter;
}