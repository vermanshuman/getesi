package it.nexera.ris.web.beans.pages.dictionary;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Nationality;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ManagedBean(name = "foreignStateListBean")
@ViewScoped
public class ForeignStatesListBean extends
        EntityLazyListPageBean<Country> implements Serializable {

    private static final long serialVersionUID = -6260035111902337296L;

    private String code;

    private String description;

    private String requestFiscalCode;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        filterTableFromPanel();

    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getCode())) {
            restrictions.add(Restrictions.ilike("code", getCode(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(getDescription())) {
            restrictions.add(Restrictions.ilike("code", getDescription(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(getRequestFiscalCode())) {
            restrictions.add(Restrictions.ilike("fiscalCode", getRequestFiscalCode(), MatchMode.ANYWHERE));
        }
        this.loadList(Country.class,restrictions.toArray(new Criterion[0]),
                new Order[] {Order.asc("description")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setCode(null);
        setDescription(null);
        setRequestFiscalCode(null);
        filterTableFromPanel();
    }
}