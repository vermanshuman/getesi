package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

import java.util.Arrays;

public enum MailManagerTypes {
    RECEIVED(2L, "fa fa-arrow-left", true, MailManagerStatuses.NEW),
    SENT(1L, "fa fa-arrow-right"),
    DRAFT(0L, "fa fa-spinner", true, MailManagerStatuses.NEW),
    STORAGE(3L, "fa fa-trash-o", true, MailManagerStatuses.DELETED, MailManagerStatuses.CANCELED);

    private Long id;

    private String iconStyle;

    private boolean isAmount;

    private MailManagerStatuses[] statuses;

    private MailManagerTypes(Long id, String iconStyle) {
        this(id, iconStyle, false);
    }

    private MailManagerTypes(Long id, String iconStyle, boolean isAmount, MailManagerStatuses... statuses) {
        this.id = id;
        this.iconStyle = iconStyle;
        this.isAmount = isAmount;
        this.statuses = statuses;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public Long getId() {
        return id;
    }

    public boolean isAmount() {
        return isAmount;
    }

    public String getIconStyle() {
        return iconStyle;
    }

    public MailManagerStatuses[] getStatuses() {
        return statuses;
    }

    public static MailManagerTypes getById(Long id) {
        return Arrays.stream(values())
                .filter((item) -> item.getId().equals(id))
                .findFirst().get();
    }
}
