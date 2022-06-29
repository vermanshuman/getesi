package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum InvoiceStatus {
    DRAFT(1l, true),
    TOSEND(2l, true),
    DELIVERED(3l, true),
    CREDITNOTE(4l, true),
    UNLOCKED(5l, true),
    EMAILSENT(6l, true),
    EMAILNOTSENT(7l, true);

    private Long id;

    private boolean needShow;

    private InvoiceStatus(Long id, boolean needShow) {
        this.id = id;
        this.needShow = needShow;
    }

    public static InvoiceStatus getById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (InvoiceStatus invoiceStatus : InvoiceStatus.values()) {
                if (invoiceStatus.getId().equals(id)) {
                    return invoiceStatus;
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