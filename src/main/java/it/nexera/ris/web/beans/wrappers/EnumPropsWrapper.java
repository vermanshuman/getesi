package it.nexera.ris.web.beans.wrappers;

import java.lang.reflect.Method;
import java.util.Arrays;

public class EnumPropsWrapper {

    private Long id;

    private boolean hasManyFields;

    public EnumPropsWrapper(Long id) {
        this.id = id;
    }

    public EnumPropsWrapper(Long id, Boolean hasManyFields) {
        this.id = id;
        this.hasManyFields = hasManyFields;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isHasManyFields() {
        return hasManyFields;
    }

    public void setHasManyFields(boolean hasManyFields) {
        this.hasManyFields = hasManyFields;
    }

    public static EnumPropsWrapper getEnumPropsFor(Enum enumInstance) {
        try {
            Method method = Arrays.stream(enumInstance.getClass().getMethods())
                    .filter(item -> item.getReturnType() == EnumPropsWrapper.class).findFirst().orElse(null);
            return (EnumPropsWrapper) method.invoke(enumInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
