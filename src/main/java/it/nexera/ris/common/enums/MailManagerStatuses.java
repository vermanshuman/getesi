package it.nexera.ris.common.enums;

import java.util.ArrayList;
import java.util.List;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum MailManagerStatuses {
    NEW(1l, true, false, true),
    READ(2l, true, false, true),
    ASSIGNED(3l, true, false, false),
    MANAGED(4l, true, false, true),
    CLOSED(5l, false, false, false),
    ARCHIVED(6l, false, false, false),
    CANCELED(7l, false, true, false),
    SUSPENDED(8l, true, false, true),
    DELETED(9l, false, false, false),
    PARTIAL(10l, true, false, true),
    EXTERNAL(11l, true, false, true),
    PROFILED(12l, true, false, true);

    private Long id;

    private boolean needShow;

    private boolean showTrash;

    private boolean showAvailable;

    private MailManagerStatuses(Long id, boolean needShow, boolean showTrash, boolean showAvailable) {
        this.id = id;
        this.needShow = needShow;
        this.showTrash = showTrash;
        this.showAvailable = showAvailable;
    }

    public static MailManagerStatuses findById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (MailManagerStatuses mailManagerStatuses : MailManagerStatuses
                    .values()) {
                if (mailManagerStatuses.getId().equals(id)) {
                    return mailManagerStatuses;
                }
            }
        }

        return null;
    }
    
    public static List<MailManagerStatuses> getStates(Long[] ids){
    	List<MailManagerStatuses> states = new ArrayList<MailManagerStatuses>();
    	if (!ValidationHelper.isNullOrEmpty(ids)) {
            for (MailManagerStatuses mailManagerStatuses : MailManagerStatuses
                    .values()) {
            	for(Long id: ids) {
	                if (mailManagerStatuses.getId().equals(id)) {
	                    states.add(mailManagerStatuses);
	                }
            	}
            }
        }
    	return states;
    }
    
    public static List<Long> getStatesIds(List<MailManagerStatuses> states){
    	List<Long> ids = new ArrayList<Long>();
    	for(MailManagerStatuses status: states) {
    		ids.add(status.getId());
    	}
    	return ids;
    }

    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public Long getId() {
        return id;
    }

    public boolean isNeedShow() {
        return needShow;
    }

    public boolean isShowTrash() {
        return showTrash;
    }

    public boolean isShowAvailable() {
        return showAvailable;
    }
}