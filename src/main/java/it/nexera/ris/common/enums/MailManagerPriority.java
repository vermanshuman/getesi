package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

import java.util.Arrays;

public enum MailManagerPriority {

    LOW(5, "fa-arrow-down grey-opacity"), NORMAL(3, null), HIGH(1, "fa-exclamation red");

    private int id;

    private String icon;

    private MailManagerPriority(int id, String icon) {
        this.id = id;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public static MailManagerPriority findById(Number id) {
        return Arrays.stream(values()).filter(item -> id.intValue() == item.getId()).findFirst().orElse(NORMAL);
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }
}
