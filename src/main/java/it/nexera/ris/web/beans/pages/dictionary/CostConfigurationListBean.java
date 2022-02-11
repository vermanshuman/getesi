package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CostConfiguration;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "costConfigurationListBean")
@ViewScoped
public class CostConfigurationListBean extends
        EntityLazyListPageBean<CostConfiguration> implements Serializable {

    private static final long serialVersionUID = -5986543704405308230L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        this.loadList(CostConfiguration.class, new Criterion[]
                {Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted"))}, new Order[]
                {Order.asc("name")});
    }

    @Override
    public void viewEntity() {
        SessionHelper.put("only_view_cost_configuration", Boolean.TRUE);
        RedirectHelper.goTo(PageTypes.COST_CONFIGURATION_EDIT,
                this.getEntityEditId());
    }

    @Override
    protected void deleteEntityInternal(Long id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        try {
            super.deleteEntityInternal(id);
        } catch (Exception e) {
            try {
                CostConfiguration costConf = DaoManager
                        .get(CostConfiguration.class, id);
                costConf.setIsDeleted(Boolean.TRUE);

                DaoManager.save(costConf);
            } catch (Exception e1) {
                LogHelper.log(log, e1);
            }
        }
    }
}
