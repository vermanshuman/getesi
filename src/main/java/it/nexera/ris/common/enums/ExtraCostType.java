package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum ExtraCostType {
    IPOTECARIO(1l),
    CATASTO(2l),
    ALTRO(3l),
    NAZIONALEPOSITIVA(4l),
	MARCA(5l),
	POSTALE(6l);

    private Long id;

    private ExtraCostType(Long id) {
        this.id = id;
    }

    public static ExtraCostType getById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (ExtraCostType extraCostType : ExtraCostType.values()) {
                if (extraCostType.getId().equals(id)) {
                    return extraCostType;
                }
            }
        }

        return null;
    }

    public static ExtraCostType getEnumByCode(String name) {
        for (ExtraCostType en : ExtraCostType.values()) {
            if (name.equals(en.name())) return en;
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