package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeAct;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
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

@ManagedBean(name = "typeActBean")
@ViewScoped
public class TypeActBean extends EntityLazyInListEditPageBean<TypeAct> implements Serializable {

    private static final long serialVersionUID = -1741091000449749081L;

    private List<SelectItem> types;

    private TypeActEnum selectedType;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        setTypes(ComboboxHelper.fillList(TypeActEnum.class));
        this.loadList(TypeAct.class, new Order[]{Order.asc("code")});
    }

    @Override
    protected void validate() throws PersistenceBeanException {
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
    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        getEntity().setType(getSelectedType());
        DaoManager.save(getEntity());
        setSelectedType(null);
    }

    @Override
    public void editEntity() {
        this.cleanValidation();
        if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
            try {
                this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                DaoManager.getSession().evict(this.getEntity());
                setSelectedType(getEntity().getType());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    @Override
    protected void setEditedValues() {
        setEditedEntity(getEntity());
    }

    public List<SelectItem> getTypes() {
        return types;
    }

    public void setTypes(List<SelectItem> types) {
        this.types = types;
    }

    public TypeActEnum getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(TypeActEnum selectedType) {
        this.selectedType = selectedType;
    }
}
