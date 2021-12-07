package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.persistence.beans.entities.domain.LandCadastralCulture;
import it.nexera.ris.persistence.beans.entities.domain.LandCulture;
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
@ManagedBean(name = "landCultureListBean")
@ViewScoped
public class LandCultureListBean extends EntityLazyListPageBean<LandCulture> implements Serializable {

    private static final long serialVersionUID = 7141646207349762514L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        this.loadList(LandCulture.class, new Criterion[]{}, new Order[]{Order.asc("name")});
    }
}

