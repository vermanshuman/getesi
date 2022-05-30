package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Court;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeAct;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "typeActEditBean")
@ViewScoped
@Getter
@Setter
public class TypeActEditBean extends EntityEditPageBean<TypeAct>
        implements Serializable {

    private static final long serialVersionUID = 6221847312412989068L;

    private List<SelectItem> types;

    private TypeActEnum selectedType;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        setTypes(ComboboxHelper.fillList(TypeActEnum.class));
        fillFields();
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        try {
            if (DaoManager.getCount(TypeAct.class, "id", new Criterion[]{
                    Restrictions.eq("code", getEntity().getCode()),
                    Restrictions.eq("type", getSelectedType()),
                    Restrictions.ne("id", getEntity().isNew() ? 0L : getEntity().getId())
            }) > 0) {
                addException("typeActInUse");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        getEntity().setType(getSelectedType());
        DaoManager.save(this.getEntity());
    }

    private void fillFields() {
        if (!ValidationHelper.isNullOrEmpty(getEntity().getType()))
            setSelectedType(getEntity().getType());
    }
}
