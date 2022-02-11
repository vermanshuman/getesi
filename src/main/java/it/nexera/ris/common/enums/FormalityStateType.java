package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum FormalityStateType {

    STRUTTURATA(1l),
    OTTICA(2l),
    TITOLO(3l),
    CARTACEA(4l),
    MANUALE(5l);

    private Long id;

    private FormalityStateType(Long id) {
        this.id = id;
    }

    public static FormalityStateType findById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (FormalityStateType type : FormalityStateType.values()) {
                if (type.getId().equals(id)) {
                    return type;
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
