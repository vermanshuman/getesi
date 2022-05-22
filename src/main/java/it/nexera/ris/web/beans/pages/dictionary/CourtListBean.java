package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "courtListBean")
@ViewScoped
@Getter
@Setter
public class CourtListBean extends
        EntityLazyListPageBean<Court> implements Serializable {

    private static final long serialVersionUID = 3952135359518006317L;

    private String name;

    private String taxCode;

    private Long selectedCityId;

    private List<SelectItem> courtCities;

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
        if (!ValidationHelper.isNullOrEmpty(getTaxCode())) {
            restrictions.add(Restrictions.ilike("fiscalCode", getTaxCode(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedCityId())) {
            restrictions.add(Restrictions.eq("city", DaoManager.get(City.class, getSelectedCityId())));
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
        if (!ValidationHelper.isNullOrEmpty(cityIds)) {
            setCourtCities(ComboboxHelper.fillList(City.class,
                    Order.asc("description"),
                    new Criterion[]{Restrictions.isNotNull("province.id")
                            , Restrictions.eq("external", Boolean.TRUE), Restrictions.in("id", cityIds)}, Boolean.FALSE));
        }
        this.loadList(Court.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("name")});

    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setName(null);
        setTaxCode(null);
        setSelectedCityId(null);
        filterTableFromPanel();
    }
}
