package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum SupportType {

    CD_ROM(1l, true),
    PEN_DRIVE(2l, true);

    private Long id;

    private boolean needShow;

    private SupportType(Long id, boolean needShow) {
        this.id = id;
        this.needShow = needShow;
    }

    public static SupportType getById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (SupportType supportType : SupportType.values()) {
                if (supportType.getId().equals(id)) {
                    return supportType;
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
