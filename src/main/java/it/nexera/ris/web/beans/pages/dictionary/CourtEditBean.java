package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Court;
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

@ManagedBean(name = "courtEditBean")
@ViewScoped
@Getter
@Setter
public class CourtEditBean extends EntityEditPageBean<Court>
        implements Serializable {

    private static final long serialVersionUID = 5223390653868500554L;

    private List<SelectItem> cities;
    private Long selectedCityId;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        setCities(ComboboxHelper.fillList(City.class, Order.asc("description"), new Criterion[]{
                Restrictions.eq("external", Boolean.TRUE)
        }));
        fillFields();
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        } else if (!ValidationHelper.isUnique(Court.class, "name",
                getEntity().getName(), this.getEntity().getId())) {
            addFieldException("form:name", "nameAlreadyInUse");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {

        if (!ValidationHelper.isNullOrEmpty(getSelectedCityId())) {
            getEntity().setCity(DaoManager.get(City.class, getSelectedCityId()));
        }
        setSelectedCityId(null);
        DaoManager.save(this.getEntity());
    }

    private void fillFields() {
        if (!ValidationHelper.isNullOrEmpty(getEntity().getCity()))
            setSelectedCityId(getEntity().getCity().getId());
    }
}
