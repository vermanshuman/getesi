package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeAct;
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

@ManagedBean(name = "typeActBean")
@ViewScoped
@Getter
@Setter
public class TypeActBean extends EntityLazyListPageBean<TypeAct> implements Serializable {

    private static final long serialVersionUID = -1741091000449749081L;

    private List<SelectItem> types;
    private TypeActEnum selectedType;

    private String typeActCode;
    private String typeActDescription;
    private String typeActTextInVisura;
    private String typeActCodeInVisura;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        setTypes(ComboboxHelper.fillList(TypeActEnum.class));
        filterTableFromPanel();

    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getTypeActCode())) {
            restrictions.add(Restrictions.ilike("code", getTypeActCode(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(getTypeActDescription())) {
            restrictions.add(Restrictions.ilike("description", getTypeActDescription(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(getTypeActTextInVisura())) {
            restrictions.add(Restrictions.ilike("textInVisura", getTypeActTextInVisura(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getTypeActCodeInVisura())) {
            restrictions.add(Restrictions.ilike("codeInVisura", getTypeActCodeInVisura(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedType())) {
            restrictions.add(Restrictions.eq("type", getSelectedType()));
        }
        this.loadList(TypeAct.class, restrictions.toArray(new Criterion[0]),
                new Order[]{Order.asc("code")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setTypeActCode(null);
        setTypeActDescription(null);
        setTypeActTextInVisura(null);
        setTypeActCodeInVisura(null);
        setSelectedType(null);
        filterTableFromPanel();
    }
}
