package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum CostType {
    DEPENDING_ON_NUMBER_OF_FORMALITIES(1l),
    BASED_OF_NUMBER_OF_FORMALITIES_CONSULTED(3l),
    EXTRA_COST(4l),
    FIXED_COST(5l),
    SALARY_COST(6l);

    private Long id;

    private CostType(Long id) {
        this.id = id;
    }

    public static CostType findById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (CostType costType : CostType.values()) {
                if (costType.getId().equals(id)) {
                    return costType;
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
}
