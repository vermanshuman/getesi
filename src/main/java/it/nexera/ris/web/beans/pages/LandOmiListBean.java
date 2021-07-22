package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.persistence.beans.entities.domain.LandOmi;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Setter
@Getter
@ManagedBean(name = "landOmiListBean")
@ViewScoped
public class LandOmiListBean extends EntityLazyListPageBean<LandOmi> implements Serializable {

    private static final long serialVersionUID = -3419187146916989311L;

    private List<SelectItem> provinces;

    private List<SelectItem> cities;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
        getProvinces().add(new SelectItem(Province.FOREIGN_COUNTRY_ID, Province.FOREIGN_COUNTRY));
        setCities(ComboboxHelper.fillList(Collections.emptyList(), false, false));

        this.loadList(LandOmi.class, new Criterion[]{},
                new Order[]{Order.desc("year")});
    }
}

