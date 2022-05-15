package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.TaxRate;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "taxRateEditBean")
@ViewScoped
@Getter
@Setter
public class TaxRateEditBean extends EntityEditPageBean<TaxRate>
        implements Serializable {

    private static final long serialVersionUID = 2495954362545418676L;


    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {

        if (ValidationHelper.isNullOrEmpty(getEntity().getPercentage())) {
            addRequiredFieldException("form:percentage");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getDescription())) {
            addRequiredFieldException("form:description");
        }
        if (ValidationHelper.isNullOrEmpty(getEntity().getCodeSDI())) {
            addRequiredFieldException("form:codeSDI");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getUse())) {
            addRequiredFieldException("form:use");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {

        DaoManager.save(this.getEntity());
    }

}
