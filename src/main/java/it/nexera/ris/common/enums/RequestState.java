package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum RequestState {

    INSERTED(1l, true),
    IN_WORK(2l, true),
    EVADED(3l, false),
    INVOICED(4l, false),
    SUSPENDED(5l, false),
    TO_BE_SENT(6l, true),
    SENT_TO_SDI(7l, false),
    FATTURATA(8l,true);

    private Long id;

    private boolean needShow;

    private RequestState(Long id, boolean needShow) {
        this.id = id;
        this.needShow = needShow;
    }

    public static RequestState getById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (RequestState requestState : RequestState.values()) {
                if (requestState.getId().equals(id)) {
                    return requestState;
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
