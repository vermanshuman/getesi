package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.persistence.beans.entities.domain.Notary;
import it.nexera.ris.persistence.beans.entities.domain.Referent;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "notaryListBean")
@ViewScoped
public class NotaryListBean extends EntityLazyListPageBean<Notary>
        implements Serializable {


    /**
	 * 
	 */
	private static final long serialVersionUID = -5997984513392754412L;

	@Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        this.loadList(Notary.class, new Order[]
                {
                        Order.asc("name")
                });
    }

    @Override
    public void viewEntity() {
        RedirectHelper.goToOnlyView(PageTypes.NOTARY_EDIT,
                this.getEntityEditId());
    }

}
