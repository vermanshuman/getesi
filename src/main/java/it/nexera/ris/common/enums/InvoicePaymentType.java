package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum InvoicePaymentType {
	Check(1l, true), 
	Transfer(2l, true),
	Cash(3l, true);
	
	private Long id;

    private boolean needShow;

    private InvoicePaymentType(Long id, boolean needShow) {
        this.id = id;
        this.needShow = needShow;
    }

    public static InvoicePaymentType getById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (InvoicePaymentType paymentType : InvoicePaymentType.values()) {
                if (paymentType.getId().equals(id)) {
                    return paymentType;
                }
            }
        }
        return null;
    }

    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public Long getId() {
        return id;
    }

    public boolean isNeedShow() {
        return needShow;
    }
}
