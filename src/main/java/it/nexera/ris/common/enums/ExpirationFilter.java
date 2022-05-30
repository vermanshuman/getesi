package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum ExpirationFilter {

    ALL(1l),
    EXPIRED(2l),
    TRA1TO3DAYS(3l),
    TRA4TO10DAYS(4l);

    private Long id;

    private ExpirationFilter(Long id) {
        this.id = id;
    }

    public static ExpirationFilter findById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (ExpirationFilter expirationFilter : ExpirationFilter.values()) {
                if (expirationFilter.getId().equals(id)) {
                    return expirationFilter;
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
