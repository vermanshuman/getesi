package it.nexera.ris.web.beans.pages.dictionary;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Court;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.SectionDFormat;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;

@ManagedBean(name = "formatiSezioneDListBean")
@ViewScoped
@Getter
@Setter
public class FormatiSezioneDListBean extends
        EntityLazyListPageBean<SectionDFormat> implements Serializable {
    
    private static final long serialVersionUID = 3240839909088723326L;

    private String name;

    private String text;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        filterTableFromPanel();
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getName())) {
            restrictions.add(Restrictions.ilike("name", getName(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getText())) {
            restrictions.add(Restrictions.ilike("text", getText(), MatchMode.ANYWHERE));
        }

        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));
        List<Court> courts = DaoManager.load(Court.class, new Criterion[]
                {Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted"))});
        List<Long> cityIds = new ArrayList<>();

        for (Court court : courts) {
            if (!ValidationHelper.isNullOrEmpty(court.getCity()) && !cityIds.contains(court.getCity().getId())) {
                cityIds.add(court.getCity().getId());
            }
        }
        this.loadList(SectionDFormat.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("name")});

    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setName(null);
        setText(null);
        filterTableFromPanel();
    }
}
