package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.SectionDFormat;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "formatiSezioneDEditBean")
@ViewScoped
@Getter
@Setter
public class FormatiSezioneDEditBean extends EntityEditPageBean<SectionDFormat>
        implements Serializable {

    private static final long serialVersionUID = -2162732588445830766L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        } else if (!ValidationHelper.isUnique(SectionDFormat.class, "name",
                getEntity().getName(), this.getEntity().getId())) {
            addFieldException("form:name", "nameInUse");
        } else if (ValidationHelper.isNullOrEmpty(this.getEntity().getText())) {
            addRequiredFieldException("form:text");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        DaoManager.save(this.getEntity());
    }
}
