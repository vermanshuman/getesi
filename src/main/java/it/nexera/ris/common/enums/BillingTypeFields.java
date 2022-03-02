package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum BillingTypeFields {

    EXCEL_DATE(1l),
    EXCEL_USER(2l),
    EXCEL_OFFICE(3l),
    EXCEL_NAME(4l),
    EXCEL_CODE(5l),
    EXCEL_REQUEST_TYPE(6l),
    EXCEL_CONSERVATORIA(7l),
    EXCEL_FORMALITY(8l),
    EXCEL_MORTGAGE_EXPENSES(9l),
    EXCEL_CATASTAL_EXPENSES(10l),
    EXCEL_COMPENSATION(11l),
    EXCEL_TOTAL(12l),
    EXCEL_NOTE(13l),
    EXCEL_CDR(14l),
    EXCEL_NDG(15l),
    EXCEL_POSITION(16l),
    EXCEL_STAMPS(17l),
    EXCEL_POSTAL_EXPENSES(18l);

    private Long id;

    private BillingTypeFields(Long id) {
        this.id = id;
    }

    public static BillingTypeFields findById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (BillingTypeFields billingTypeFields : BillingTypeFields.values()) {
                if (billingTypeFields.getId().equals(id)) {
                    return billingTypeFields;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public Long getId() {
        return id;
    }

}

