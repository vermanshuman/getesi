package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.RoleTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.base.AccessBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

@ManagedBean(name = "roleListBean")
@ViewScoped
public class RoleListBean extends EntityLazyListPageBean<Role> implements
        Serializable {

    private static final long serialVersionUID = 2934758631283242272L;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        this.loadList(Role.class, new Order[]{
                Order.asc("name")
        });
    }

    public boolean getCanDelete(Role role){
        try {
            if(RoleTypes.EXTERNAL.equals(role.getType())){
                return false;
            }
            return AccessBean.canDeleteInPage(this.getCurrentPage());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;

    }
}
