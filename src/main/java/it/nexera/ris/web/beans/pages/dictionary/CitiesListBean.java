package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Court;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "citiesListBean")
@ViewScoped
@Getter
@Setter
public class CitiesListBean extends EntityLazyListPageBean<City> implements Serializable {

    private static final long serialVersionUID = -397464426868428763L;

    private boolean canCreate;

    private List<SelectItem> provinces;

    private Long selectedProvinceId;
    
    private String name;

    private String cap;

    private String cfis;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        filterTableFromPanel();
        setCanCreate(true);
        this.loadList(City.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted"))
        }, new Order[]{
                Order.asc("code")
        });
        setProvinces(ComboboxHelper.fillList(Province.class));
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getName())) {
            restrictions.add(Restrictions.ilike("description", getName(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getCap())) {
            restrictions.add(Restrictions.ilike("cap", getCap(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getCfis())) {
            restrictions.add(Restrictions.ilike("cfis", getCap(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedProvinceId())) {
            restrictions.add(Restrictions.eq("province", DaoManager.get(Province.class, getSelectedProvinceId())));
        }

        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));
        this.loadList(City.class, restrictions.toArray(new Criterion[0]), new Order[]{
                Order.asc("code")
        });
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setName(null);
        setCap(null);
        setCfis(null);
        setSelectedProvinceId(null);
        filterTableFromPanel();
    }
}
