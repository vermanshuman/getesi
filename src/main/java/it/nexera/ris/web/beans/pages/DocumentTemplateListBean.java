package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.entities.domain.DocumentTemplate;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

@ManagedBean(name = "documentTemplateListBean")
@ViewScoped
public class DocumentTemplateListBean extends
        EntityLazyListPageBean<DocumentTemplate> implements Serializable {

    private static final long serialVersionUID = 3643436130600164311L;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        this.loadList(DocumentTemplate.class, new Criterion[]{}, new Order[]{
                Order.asc("name")
        });
    }

}
