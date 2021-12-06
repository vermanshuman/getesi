package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "serviceListBean")
@ViewScoped
public class ServiceListBean extends EntityLazyListPageBean<Service>
        implements Serializable {

    private static final long serialVersionUID = -8322426928287736397L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        this.loadList(Service.class, new Criterion[]
                {Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted"))}, new Order[]
                {Order.asc("name")});
    }

    @Override
    public void viewEntity() {
        SessionHelper.put("only_view_services", Boolean.TRUE);
        RedirectHelper.goTo(PageTypes.SERVICE_EDIT, this.getEntityEditId());
    }

}
