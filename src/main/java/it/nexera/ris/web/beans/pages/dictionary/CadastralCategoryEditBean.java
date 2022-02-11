package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "cadastralCategoryEditBean")
@ViewScoped
public class CadastralCategoryEditBean
        extends EntityEditPageBean<CadastralCategory> implements Serializable {

    private static final long serialVersionUID = -600044173159218279L;

    private boolean onlyView;

    @Override
    protected void preLoad() throws PersistenceBeanException {
        if ("true".equalsIgnoreCase(
                this.getRequestParameter(RedirectHelper.ONLY_VIEW))) {
            setOnlyView(true);
        }
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {

    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getCode())) {
            addRequiredFieldException("form:code");
        } else {
            Long countInDb = DaoManager.getCount(CadastralCategory.class, "id",
                    new Criterion[]
                            {
                                    Restrictions.or(
                                            Restrictions.eq("isDeleted", Boolean.FALSE),
                                            Restrictions.isNull("isDeleted")),
                                    Restrictions.ne("id", getEntity().isNew() ? 0l
                                            : getEntity().getId()),
                                    Restrictions.eq("code", this.getEntity().getCode())
                            });

            if (countInDb > 0l) {
                addFieldException("form:code", "codeAlreadyInUse");
            }
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        DaoManager.save(getEntity());
    }

    public void editEntity() {
        this.setOnlyView(false);
    }

    public boolean isOnlyView() {
        return onlyView;
    }

    public void setOnlyView(boolean onlyView) {
        this.onlyView = onlyView;
    }

}
