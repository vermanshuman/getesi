package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "cityEditBean")
@ViewScoped
@Getter
@Setter
public class CityEditBean extends EntityEditPageBean<City> implements Serializable {

    private static final long serialVersionUID = 7773566537303641295L;

    private boolean canCreate;

    private List<SelectItem> provinces;

    private Long selectedProvinceId;

    @Override
    public void onLoad() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        setCanCreate(true);
        setProvinces(ComboboxHelper.fillList(Province.class));
        System.out.println(">>>>>>>>>>>>>>> " + getProvinces());
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        try {
            if (DaoManager.getCount(City.class, "id", new Criterion[]{
                    Restrictions.eq("cfis", getEntity().getCfis()),
                    Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted")),
                    Restrictions.ne("id", getEntity().isNew() ? 0L : getEntity().getId())
            }) > 0) {
                addException("cityWarning");
                getValidationFailed();
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {

        if (!ValidationHelper.isNullOrEmpty(getSelectedProvinceId())) {
            getEntity().setProvince(DaoManager.get(Province.class, getSelectedProvinceId()));
            DaoManager.save(getEntity());

            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                    ResourcesHelper.getValidation("successfullySaved"), "");
        } else {
            addException("warning");
        }
        setSelectedProvinceId(null);
    }

    @Override
    public void cleanValidation() {
        super.cleanValidation();
    }
}