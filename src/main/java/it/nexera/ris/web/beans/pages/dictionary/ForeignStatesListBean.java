package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.*;

@ManagedBean(name = "foreignStatesListBean")
@ViewScoped
public class ForeignStatesListBean extends
        EntityLazyInListEditPageBean<Country> implements Serializable {

    private static final long serialVersionUID = -6260035111902337296L;

    @Override
    protected void setEditedValues() {
        this.getEditedEntity().setCode(this.getEntity().getCode());
        this.getEditedEntity()
                .setDescription(this.getEntity().getDescription());
    }

    @Override
    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getCode())) {
            addRequiredFieldException("form:code");
        } else if (!ValidationHelper.isUnique(Country.class, "code",
                getEntity().getCode(), this.getEntity().getId())) {
            addFieldException("form:code", "codeAlreadyInUse");
        }
    }

    @Override
    public void save() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        DaoManager.save(this.getEntity());
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        this.loadList(Country.class, new Criterion[]{}, 
        		new Order[] {Order.asc("description")});
    }

    @Override
    protected void deleteEntityInternal(Long id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        try {
            super.deleteEntityInternal(id);
        } catch (Exception e) {
            try {
                DaoManager.save(getEntity());
            } catch (Exception e1) {
                LogHelper.log(log, e1);
            }
        }
    }

    @Override
    public void editEntity() {
        this.cleanValidation();
        executeJS("setIcon();");
        if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
            try {
                this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                DaoManager.getSession().evict(this.getEntity());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }
    
    @Override
    public void resetFields() {
        super.resetFields();
    }

}
