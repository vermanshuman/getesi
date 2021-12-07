package it.nexera.ris.web.beans;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.SpecialPermissionTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.PermissionsHelper;
import it.nexera.ris.common.helpers.UsersRolesHelper;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.web.beans.base.AccessBean;
import org.hibernate.HibernateException;

import java.util.List;

public abstract class BaseEntityPageBean extends BaseValidationPageBean {

    private List<Role> userRoles;

    public boolean getCanView() {
        try {
            return AccessBean.canViewPage(this.getCurrentPage());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public boolean getCanCreate()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        try {
            return AccessBean.canCreateInPage(this.getCurrentPage());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public boolean getCanEdit()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        try {
            return AccessBean.canEditInPage(this.getCurrentPage());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public boolean getCanList()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        try {
            return AccessBean.canListPage(this.getCurrentPage());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public boolean getCanListCreatedByUser()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        try {
            return AccessBean.canListCreatedByUserPage(this.getCurrentPage());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public boolean getCanSuspendRequest()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.CAN_SUSPEND_REQUEST);
    }

    public boolean getCanChangeUserRequest()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.CAN_CHANGE_REQUEST_USER);
    }

    public boolean getCanSeeAllUsers()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.CAN_SEE_ALL_USERS);
    }

    public boolean getCanStartPractice()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.CAN_START_PRACTICE);
    }

    public boolean getCanChangeMailState()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.CAN_CHANGE_MAIL_STATE);
    }

    public boolean getCanAssignPractice()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.CAN_ASSIGN_PRACTICE);
    }

    public boolean getCanManageCosts()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.CAN_MANAGE_COSTS);
    }

    public boolean getCanDelete()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        try {
            return AccessBean.canDeleteInPage(this.getCurrentPage());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public List<Role> getUserRoles() {
        if (this.userRoles == null) {
            this.userRoles = UsersRolesHelper.getUserRoles();
        }

        return userRoles;
    }

    public boolean getUserEntityPage() {
        return PageTypes.USER_LIST.equals(this.getCurrentPage());
    }
}
