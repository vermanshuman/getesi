package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum VatCollectability {

    IMMIDIATE(1l, true),
    SPLIT_PAYMENT(2l, true),
    DEFERRED(3l, false);

    private Long id;

    private boolean needShow;

    private VatCollectability(Long id, boolean needShow) {
        this.id = id;
        this.needShow = needShow;
    }

    public static VatCollectability getById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (VatCollectability vatCollectability : VatCollectability.values()) {
                if (vatCollectability.getId().equals(id)) {
                    return vatCollectability;
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

    public boolean isNeedShow() {
        return needShow;
    }

}
