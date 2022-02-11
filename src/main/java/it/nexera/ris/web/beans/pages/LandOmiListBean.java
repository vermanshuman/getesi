package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.LandOmi;
import it.nexera.ris.persistence.beans.entities.domain.WLGFolder;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.beans.entities.domain.readonly.ClientShort;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.common.EntityLazyListModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.model.LazyDataModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@ManagedBean(name = "landOmiListBean")
@ViewScoped
public class LandOmiListBean extends EntityLazyListPageBean<LandOmi> implements Serializable {

    private static final long serialVersionUID = -3419187146916989311L;

    private List<SelectItem> provinces;

    private List<SelectItem> cities;

    private Long selectedProvinceId;

    private String filterCityDescription;

    private String filterCityCfis;

    private LazyDataModel<LandOmi> lazyModel;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
        getProvinces().add(new SelectItem(Province.FOREIGN_COUNTRY_ID, Province.FOREIGN_COUNTRY));
        filterTableFromPanel();
    }

    public void filterTableFromPanel() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        if(!ValidationHelper.isNullOrEmpty(getFilterCityDescription()) ||
                !ValidationHelper.isNullOrEmpty(getFilterCityCfis())){
            setLazyModel(
                    new EntityLazyListModel(LandOmi.class, reloadFilters().toArray(new Criterion[0]), new Order[]{Order.desc("year")},
                            new CriteriaAlias[]{
                                    new CriteriaAlias("cities", "c", JoinType.INNER_JOIN)
                            }));
        }else {
            setLazyModel(
                    new EntityLazyListModel(LandOmi.class, reloadFilters().toArray(new Criterion[0]), new Order[]{Order.desc("year")}));
        }
    }

    @Override
    protected void loadList(Class<LandOmi> clazz, Order[] orders) {
        super.loadList(clazz, orders);
    }

    private List<Criterion> reloadFilters() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getSelectedProvinceId())) {
            restrictions.add(Restrictions.eq("province", DaoManager.get(Province.class, getSelectedProvinceId())));
        }
        List<Long> cityIds = null;

        if(!ValidationHelper.isNullOrEmpty(getFilterCityCfis()) && !ValidationHelper.isNullOrEmpty(getFilterCityDescription())){
            cityIds = DaoManager.loadIds(City.class, new Criterion[]{
                    Restrictions.eq("description", getFilterCityDescription()).ignoreCase(),
                    Restrictions.eq("cfis", getFilterCityCfis()).ignoreCase()
            });
        }else if (!ValidationHelper.isNullOrEmpty(getFilterCityDescription())) {
            cityIds = DaoManager.loadIds(City.class, new Criterion[]{
                    Restrictions.eq("description", getFilterCityDescription()).ignoreCase()
            });
        }else if (!ValidationHelper.isNullOrEmpty(getFilterCityCfis())) {
            cityIds = DaoManager.loadIds(City.class, new Criterion[]{
                    Restrictions.eq("cfis", getFilterCityCfis()).ignoreCase()
            });
        }
        if(!ValidationHelper.isNullOrEmpty(cityIds)){
            restrictions.add(Restrictions.in("c.id", cityIds));
        }
        return restrictions;
    }
}

