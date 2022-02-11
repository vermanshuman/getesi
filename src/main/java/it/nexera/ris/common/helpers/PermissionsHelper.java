package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.SpecialPermissionTypes;
import it.nexera.ris.persistence.SessionManager;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.web.beans.wrappers.logic.SpecialPermissionWrapper;

public class PermissionsHelper extends BaseHelper {

    public static boolean getPermission(SpecialPermissionTypes type) {
        if (SessionManager.getInstance().getSessionBean() != null
                && UserHolder.getInstance().getCurrentUser() != null) {
            for (SpecialPermissionWrapper p : UserHolder.getInstance()
                    .getCurrentUser().getSpecialPermissions()) {
                if (p.getSpecialPermissionType().equals(type)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Boolean getPermissionByPage(PageTypes pageType) {
        if (SessionManager.getInstance().getSessionBean() != null
                && UserHolder.getInstance().getCurrentUser() != null) {
            for (SpecialPermissionTypes specPermType : SpecialPermissionTypes.values()) {
                if (specPermType.getPageType() != null
                        && specPermType.getPageType().equals(pageType)) {
                    for (SpecialPermissionWrapper p : UserHolder.getInstance()
                            .getCurrentUser().getSpecialPermissions()) {
                        if (p.getSpecialPermissionType().equals(specPermType)) {
                            return Boolean.TRUE;
                        }
                    }
                    return Boolean.FALSE;
                }
            }
        }
        return null;
    }

}
