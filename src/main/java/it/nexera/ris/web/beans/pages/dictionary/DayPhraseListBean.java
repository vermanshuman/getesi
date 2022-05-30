package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Court;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DayPhrase;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
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

@ManagedBean(name = "dayPhraseListBean")
@ViewScoped
public class DayPhraseListBean extends EntityLazyListPageBean<DayPhrase> implements Serializable {

    private static final long serialVersionUID = 1891268440957376551L;

    private String phrase;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        filterTableFromPanel();
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getPhrase())) {
            restrictions.add(Restrictions.ilike("phrase", getPhrase(), MatchMode.ANYWHERE));
        }

        this.loadList(DayPhrase.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("phrase")});

    }

    public void clearFiltraPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setPhrase(null);
        filterTableFromPanel();
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }
}
