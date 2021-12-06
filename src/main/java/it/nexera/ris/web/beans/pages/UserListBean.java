package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.web.beans.EntityListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "userListBean")
@ViewScoped
public class UserListBean extends EntityListPageBean<User>
        implements Serializable {

    private static final long serialVersionUID = -190774165561455798L;

    private List<User> users;

    private List<SelectItem> roles;

    private Long selectedRole;

    private Long selectedRoleTemp;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        fillUsers();

        if (!this.isPostback()) {
            if (ValidationHelper.isNullOrEmpty(getRoles())) {
                setRoles(ComboboxHelper.fillList(Role.class, Order.asc("name"),
                        true));
            }
        }
    }

    public void fillUsers() throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        List<User> resListStep1 = new ArrayList<User>();

        if (getUsers() == null) {
            setUsers(DaoManager.load(User.class));
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedRole())) {
            for (User user : getUsers()) {
                for (Role r : user.getUserRoles()) {
                    if (r.getId().equals(getSelectedRole())) {
                        resListStep1.add(user);
                        break;
                    }
                }
            }

            setList(resListStep1);
        } else {
            setList(getUsers());
        }
    }

    public void selectedRoleChanged() {
        try {
            this.selectedRole = this.selectedRoleTemp;
            fillUsers();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public List<SelectItem> getRoles() {
        return roles;
    }

    public void setRoles(List<SelectItem> roles) {
        this.roles = roles;
    }

    public Long getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(Long selectedRole) {
        this.selectedRoleTemp = selectedRole;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

}
