package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

import java.util.ArrayList;
import java.util.List;

public enum Activity {
    EXECUTION(1l),
    INFORMATION(2l);

    private Long id;

    private Activity(Long id) {
        this.id = id;
    }

    public static Activity findById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (Activity activity : Activity
                    .values()) {
                if (activity.getId().equals(id)) {
                    return activity;
                }
            }
        }

        return null;
    }

    public static List<Activity> getActivities(Long[] ids){
        List<Activity> activities = new ArrayList<Activity>();
        if (!ValidationHelper.isNullOrEmpty(ids)) {
            for (Activity activity : Activity
                    .values()) {
                for(Long id: ids) {
                    if (activity.getId().equals(id)) {
                        activities.add(activity);
                    }
                }
            }
        }
        return activities;
    }

    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public Long getId() {
        return id;
    }
}
