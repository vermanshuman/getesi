package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "cadastralCategoryListBean")
@ViewScoped
public class CadastralCategoryListBean extends
        EntityLazyListPageBean<CadastralCategory> implements Serializable {

    private static final long serialVersionUID = 3443206858639238841L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        this.loadList(CadastralCategory.class, new Criterion[]
                {
                        Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                                Restrictions.isNull("isDeleted"))
                }, new Order[]
                {
                        Order.asc("code")
                });
    }

    @Override
    public void viewEntity() {
        RedirectHelper.goToOnlyView(PageTypes.CADASTRAL_CATEGORY_EDIT,
                this.getEntityEditId());
    }

    @Override
    protected void deleteEntityInternal(Long id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            CadastralCategory cadastralCategory = DaoManager
                    .get(CadastralCategory.class, id);

            cadastralCategory.setIsDeleted(Boolean.TRUE);

            DaoManager.save(cadastralCategory);
        }
    }

}
