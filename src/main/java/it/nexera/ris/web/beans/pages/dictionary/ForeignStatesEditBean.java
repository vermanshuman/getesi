package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Nationality;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "foreignStateEditBean")
@ViewScoped
@Getter
@Setter
public class ForeignStatesEditBean extends EntityEditPageBean<Country>
        implements Serializable {

    private static final long serialVersionUID = 4270363526275481372L;

    private String fiscalCode;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getFiscalCode())) {
            addRequiredFieldException("form:fiscalCode");
        } else if (ValidationHelper.isNullOrEmpty(this.getEntity().getCode())) {
            addRequiredFieldException("form:code");
        } else if (!ValidationHelper.isUnique(Country.class, "code",
                getEntity().getCode(), this.getEntity().getId())) {
            addFieldException("form:code", "codeAlreadyInUse");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {

        if(!ValidationHelper.isNullOrEmpty(this.getEntity().getFiscalCode())){
            List<Nationality> nationalityList = DaoManager.load(Nationality.class, new Criterion[]{
                    Restrictions.eq("cfis", this.getEntity().getFiscalCode()).ignoreCase()
            });
            if (!ValidationHelper.isNullOrEmpty(nationalityList)) {
                Nationality nationality = nationalityList.get(0);
                nationality.setCode(this.getEntity().getCode());
                nationality.setDescription(this.getEntity().getDescription());
                nationality.setCfis(this.getFiscalCode());
                DaoManager.save(nationality);
            }
        }
        DaoManager.save(this.getEntity());
    }

}
