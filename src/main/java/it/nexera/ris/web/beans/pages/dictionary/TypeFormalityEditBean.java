package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "typeFormalityEdit")
@ViewScoped
@Getter
@Setter
public class TypeFormalityEditBean extends EntityEditPageBean<TypeFormality>
        implements Serializable {

    private static final long serialVersionUID = 2495954362545418676L;

    private List<SelectItem> typeActEnumList;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        setTypeActEnumList(ComboboxHelper.fillList(TypeActEnum.class, true, false));
    }

    @Override
    public void onValidate() {

        try {
            if (DaoManager.getCount(TypeFormality.class, "id", new Criterion[]{
                    Restrictions.eq("code", getEntity().getCode()),
                    Restrictions.eq("type", getEntity().getType()),
                    Restrictions.ne("id", getEntity().isNew() ? 0L : getEntity().getId())
            }) > 0) {
                addException("typeFormalityInUse");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {

        DaoManager.save(this.getEntity());
    }

    public List<SelectItem> getTypeActEnumList() {
        return typeActEnumList;
    }

    public void setTypeActEnumList(List<SelectItem> typeActEnumList) {
        this.typeActEnumList = typeActEnumList;
    }
}