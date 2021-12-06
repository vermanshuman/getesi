package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DayPhrase;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "dayPhraseBean")
@ViewScoped
public class DayPhraseBean extends EntityLazyInListEditPageBean<DayPhrase> implements Serializable {

    private static final long serialVersionUID = 1891268440957376551L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        this.loadList(DayPhrase.class, new Order[]{});
    }

    @Override
    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(getEntity().getPhrase())) {
            addRequiredFieldException("form:phrase");
        }
    }

    @Override
    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        DaoManager.save(getEntity());
    }

    @Override
    protected void setEditedValues() {
        getEditedEntity().setPhrase(getEntity().getPhrase());
    }

    public boolean isCanCreateInBean() {
        return true;
    }
}
