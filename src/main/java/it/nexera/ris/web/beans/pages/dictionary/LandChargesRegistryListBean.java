package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "landChargesRegistryListBean")
@ViewScoped
public class LandChargesRegistryListBean extends
        EntityLazyListPageBean<LandChargesRegistry> implements Serializable {

    private static final long serialVersionUID = 4380347375101763116L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        this.loadList(LandChargesRegistry.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted"))
        }, new Order[]{
                Order.asc("name")
        });
    }

    @Override
    protected void deleteEntityInternal(Long id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        try {
            super.deleteEntityInternal(id);
        } catch (Exception e) {
            try {
                LandChargesRegistry entity = DaoManager
                        .get(LandChargesRegistry.class, id);
                entity.setIsDeleted(Boolean.TRUE);

                DaoManager.save(entity);
            } catch (Exception e1) {
                LogHelper.log(log, e1);
            }

        }
    }

}
